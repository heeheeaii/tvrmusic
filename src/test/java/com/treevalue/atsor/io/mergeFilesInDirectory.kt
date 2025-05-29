import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets

/**
 * 将指定目录及其子目录下的所有文件的内容按路径字母顺序合并到一个新文件中。
 *
 * @param sourceDirectoryPath 要扫描的源目录路径。
 * @param outputFileName 合并后的输出文件名（将在运行目录下创建）。
 */
fun mergeFilesInDirectory(sourceDirectoryPath: String, outputFileName: String = "merged_output.txt") {
    val sourceDir = File(sourceDirectoryPath)
    val outputFile = File(outputFileName)


    if (!sourceDir.exists()) {
        println("错误：源目录不存在: ${sourceDir.absolutePath}")
        return
    }
    if (!sourceDir.isDirectory) {
        println("错误：提供的路径不是一个目录: ${sourceDir.absolutePath}")
        return
    }


    if (outputFile.exists()) {
        println("警告：输出文件 '${outputFile.name}' 已存在，将被覆盖。")
        if (!outputFile.delete()) {
            println("错误：无法删除已存在的输出文件: ${outputFile.absolutePath}")
            return
        }
    }

    println("开始从目录合并文件: ${sourceDir.absolutePath}")
    println("输出文件将是: ${outputFile.absolutePath}")

    try {


        val filesToMerge = sourceDir.walkTopDown()
            .filter { it.isFile }
            .sortedBy { it.absolutePath }
            .toList()

        if (filesToMerge.isEmpty()) {
            println("在源目录中没有找到任何文件。")

            outputFile.createNewFile()
            println("已创建空的输出文件: ${outputFile.absolutePath}")
            return
        }

        println("找到 ${filesToMerge.size} 个文件准备合并。")


        outputFile.bufferedWriter(StandardCharsets.UTF_8).use { writer ->
            filesToMerge.forEachIndexed { index, file ->
                try {
                    println("正在合并 (${index + 1}/${filesToMerge.size}): ${file.absolutePath}")



                    file.bufferedReader(StandardCharsets.UTF_8).use { reader ->

                        reader.copyTo(writer)
                    }



                    if (index < filesToMerge.size - 1) {
                        writer.newLine()
                        writer.write("--- 合并自: ${file.name} ---")
                        writer.newLine()
                        writer.newLine()
                    }

                } catch (e: IOException) {
                    println("警告：读取文件 ${file.absolutePath} 时出错，已跳过。错误: ${e.message}")
                } catch (e: SecurityException) {
                    println("警告：没有权限读取文件 ${file.absolutePath}，已跳过。")
                } catch (e: Exception) {
                    println("警告：处理文件 ${file.absolutePath} 时发生未知错误，已跳过。错误: ${e.message}")
                }
            }
        }

        println("成功！已将 ${filesToMerge.size} 个文件的内容合并到 ${outputFile.absolutePath}")

    } catch (e: IOException) {
        println("错误：在文件操作过程中发生 I/O 错误: ${e.message}")
    } catch (e: SecurityException) {
        println("错误：权限不足，无法访问目录或写入输出文件。")
    } catch (e: Exception) {
        println("错误：发生了一个意外错误: ${e.message}")
    }
}


fun main(args: Array<String>) {
    val sourceDirectoryPath: String
    if (args.isNotEmpty()) {
        sourceDirectoryPath = args[0]
    } else {
        sourceDirectoryPath = "D:\\agi\\tvrmusicnew\\src\\main\\java\\com\\treevalue\\quick"
    }
    mergeFilesInDirectory(sourceDirectoryPath, "D:\\agi\\tvrmusicnew\\src\\test\\output\\merged_output.txt")
}
