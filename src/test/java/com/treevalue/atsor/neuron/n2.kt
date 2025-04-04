import org.lwjgl.BufferUtils
import org.lwjgl.PointerBuffer
import org.lwjgl.opencl.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.nio.IntBuffer

// Helper function to check OpenCL errors
fun checkCLError(errcode: Int, function: String) {
    if (errcode != CL10.CL_SUCCESS) {
        throw RuntimeException("OpenCL error [$errcode] in $function")
    }
}

fun checkCLError(errcode_buf: IntBuffer, function: String) {
    val errcode = errcode_buf[0]
    if (errcode != CL10.CL_SUCCESS) {
        throw RuntimeException("OpenCL error [$errcode] in $function")
    }
}

fun main() {
//    System.setProperty("org.lwjgl.librarypath", "D:\\tmp\\download")
    println("Starting Kotlin OpenCL Matrix Multiplication using LWJGL...")

    // --- Matrix Dimensions ---
    val M = 128 // Rows of A and C
    val N = 64  // Cols of A and Rows of B
    val P = 128 // Cols of B and C

    // --- Host Data Initialization ---
    val h_A = FloatArray(M * N) { it.toFloat() * 0.1f + 1.0f } // Example data for A
    val h_B = FloatArray(N * P) { it.toFloat() * 0.05f + 0.5f } // Example data for B
    val h_C = FloatArray(M * P) // Result matrix initialized to zeros

    var clPlatform: Long = 0
    var clDevice: Long = 0
    var clContext: Long = 0
    var clQueue: Long = 0
    var clProgram: Long = 0
    var clKernel: Long = 0
    var d_A: Long = 0
    var d_B: Long = 0
    var d_C: Long = 0

    try {
        MemoryStack.stackPush().use { stack ->
            val errcode_ret = stack.mallocInt(1) // Buffer for error codes

            // --- 1. Initialize OpenCL Platform and Device ---
            println("1. Initializing OpenCL Platform and Device...")
            val platformIDs = PointerBuffer.allocateDirect(1) // Need space for at least one platform
            checkCLError(CL10.clGetPlatformIDs(platformIDs, null as IntBuffer?), "clGetPlatformIDs")
            clPlatform = platformIDs[0]
            if (clPlatform == 0L) throw RuntimeException("No OpenCL platform found.")

            val deviceIDs = PointerBuffer.allocateDirect(1) // Need space for at least one device
            // Try getting a GPU device first


                // --- 2. Create OpenCL Context ---
            println("2. Creating OpenCL Context...")
            // Context properties: platform ID
            val contextProps = stack.mallocPointer(3)
            contextProps.put(CL10.CL_CONTEXT_PLATFORM.toLong()).put(clPlatform).put(0L) // Terminate list
            contextProps.flip()
            clContext = CL10.clCreateContext(contextProps, clDevice, null, 0L, errcode_ret)
            checkCLError(errcode_ret, "clCreateContext")

            // --- 3. Create Command Queue ---
            println("3. Creating Command Queue...")
            // Use clCreateCommandQueueWithProperties for newer OpenCL versions if needed,
            // clCreateCommandQueue is deprecated but widely compatible.
            clQueue = CL10.clCreateCommandQueue(clContext, clDevice, 0L, errcode_ret) // 0L for default properties
            checkCLError(errcode_ret, "clCreateCommandQueue")

            // --- 4. Load and Compile Kernel ---
            println("4. Loading and Compiling Kernel...")
            val kernelSource = MatrixMultiplyOpenCL::class.java.getResourceAsStream("/matrix_mul.cl")?.bufferedReader()?.readText()
                ?: throw RuntimeException("Cannot find kernel file: matrix_mul.cl in resources")

            clProgram = CL10.clCreateProgramWithSource(clContext, kernelSource, errcode_ret)
            checkCLError(errcode_ret, "clCreateProgramWithSource")

            val buildError = CL10.clBuildProgram(clProgram, clDevice, "", null, 0L)
            if (buildError != CL10.CL_SUCCESS) {
                val logSizeBuf = stack.mallocPointer(1)
                CL10.clGetProgramBuildInfo(clProgram, clDevice, CL10.CL_PROGRAM_BUILD_LOG, null as PointerBuffer?, logSizeBuf)
                val logSize = logSizeBuf[0].toInt()
                if (logSize > 1) {
                    val logBuf = stack.malloc(logSize)
                    CL10.clGetProgramBuildInfo(clProgram, clDevice, CL10.CL_PROGRAM_BUILD_LOG, logBuf, null)
                    println("--- OpenCL Build Log ---")
                    println(MemoryUtil.memASCII(logBuf, logSize))
                    println("------------------------")
                }
                checkCLError(buildError, "clBuildProgram") // Throw exception after printing log
            }
            println("   Kernel compiled successfully.")

            // --- 5. Create Kernel Object ---
            println("5. Creating Kernel Object...")
            clKernel = CL10.clCreateKernel(clProgram, "matrix_mul", errcode_ret)
            checkCLError(errcode_ret, "clCreateKernel")

            // --- 6. Create Device Buffers and Copy Data ---
            println("6. Creating Device Buffers and Copying Data...")
            // Allocate host buffers that LWJGL can easily use
            val h_A_buffer = MemoryUtil.memAllocFloat(M * N)
            val h_B_buffer = MemoryUtil.memAllocFloat(N * P)
            h_A_buffer.put(h_A).flip()
            h_B_buffer.put(h_B).flip()


            // Free host buffers now that data is copied to device (or copied during clCreateBuffer)
            MemoryUtil.memFree(h_A_buffer)
            MemoryUtil.memFree(h_B_buffer)
            println("   Buffers created and host data copied to device.")

            // --- 7. Set Kernel Arguments ---
            println("7. Setting Kernel Arguments...")
            CL10.clSetKernelArg1i(clKernel, 0, M)
            CL10.clSetKernelArg1i(clKernel, 1, N)
            CL10.clSetKernelArg1i(clKernel, 2, P)
            CL10.clSetKernelArg1p(clKernel, 3, d_A)
            CL10.clSetKernelArg1p(clKernel, 4, d_B)
            CL10.clSetKernelArg1p(clKernel, 5, d_C)

            // --- 8. Execute Kernel ---
            println("8. Executing Kernel...")
            val globalWorkSize = stack.mallocPointer(2) // 2 dimensions for matrix
            globalWorkSize.put(0, M.toLong()) // Global size for dim 0 (rows of C)
            globalWorkSize.put(1, P.toLong()) // Global size for dim 1 (cols of C)

            // Enqueue the kernel. Using null for localWorkSize lets OpenCL decide.
            checkCLError(CL10.clEnqueueNDRangeKernel(clQueue, clKernel, 2, null, globalWorkSize, null, null, null), "clEnqueueNDRangeKernel")

            // --- 9. Wait for Execution to Finish ---
            println("9. Waiting for kernel execution to finish...")
            checkCLError(CL10.clFinish(clQueue), "clFinish") // Block until queue is finished

            // --- 10. Read Results Back to Host ---
            println("10. Reading results back to host...")
            val h_C_buffer = BufferUtils.createFloatBuffer(M * P) // Use NIO Buffer for reading back
            checkCLError(CL10.clEnqueueReadBuffer(clQueue, d_C, true, 0, h_C_buffer, null, null), "clEnqueueReadBuffer")
            // The 'true' makes it a blocking read, so clFinish isn't strictly needed right before,
            // but clFinish ensures *all* preceding commands are done.

            // Copy data from NIO buffer back to our FloatArray
            h_C_buffer.get(h_C)
            println("   Results read successfully.")

            // --- Verification (Optional) ---
            println("Verification (comparing first few elements):")
            println(" Expected C[0,0] (CPU approx): ${h_A[0] * h_B[0] + (if (N > 1) h_A[1] * h_B[P] else 0.0f)} ... (naive calculation of one element)") // Very rough check
            println(" Actual   C[0,0] (GPU): ${h_C[0]}")
            println(" Actual   C[M-1,P-1] (GPU): ${h_C[M * P - 1]}")

        } // MemoryStack pops here

    } catch (e: Exception) {
        println("!!! AN ERROR OCCURRED !!!")
        e.printStackTrace()
    } finally {
        // --- 11. Cleanup OpenCL Resources ---
        println("11. Cleaning up OpenCL resources...")
        if (d_A != 0L) CL10.clReleaseMemObject(d_A)
        if (d_B != 0L) CL10.clReleaseMemObject(d_B)
        if (d_C != 0L) CL10.clReleaseMemObject(d_C)
        if (clKernel != 0L) CL10.clReleaseKernel(clKernel)
        if (clProgram != 0L) CL10.clReleaseProgram(clProgram)
        if (clQueue != 0L) CL10.clReleaseCommandQueue(clQueue)
        if (clContext != 0L) CL10.clReleaseContext(clContext)
        // Platform and device don't need explicit release in typical scenarios
        println("Cleanup complete.")
    }
}

// Dummy class to easily get resource stream
class MatrixMultiplyOpenCL
