import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets

/**
 * 文件合并配置类
 */
data class MergeConfig(
    val includeDirectoryHeaders: Boolean = true,
    val excludedDirectories: Set<String> = emptySet(),
    val excludedFileExtensions: Set<String> = emptySet(),
    val excludedFileNames: Set<String> = emptySet(),
    val excludedFilePatterns: List<Regex> = emptyList(),
    val includedFileExtensions: Set<String> = emptySet(), // 如果非空，只包含这些扩展名
    val maxFileSize: Long = Long.MAX_VALUE, // 字节，默认无限制
    val encoding: String = "UTF-8"
)

/**
 * 将多个指定路径（目录或文件）及其子目录下的所有文件的内容按路径字母顺序合并到一个新文件中。
 * 支持排除特定目录、文件扩展名、文件名和文件模式。
 *
 * @param sourcePaths 要扫描的源路径列表（可以是文件或目录）。
 * @param outputFileName 合并后的输出文件名。
 * @param config 合并配置选项。
 */
fun mergeFilesFromMultiplePaths(
    sourcePaths: List<String>,
    outputFileName: String = "merged_output.txt",
    config: MergeConfig = MergeConfig()
) {
    val outputFile = File(outputFileName)

    // 验证输入参数
    if (sourcePaths.isEmpty()) {
        println("错误：没有提供源路径。")
        return
    }

    // 验证所有源路径并分类
    val validDirectories = mutableListOf<File>()
    val validFiles = mutableListOf<File>()

    sourcePaths.forEach { path ->
        val file = File(path)
        when {
            !file.exists() -> println("警告：路径不存在，已跳过: ${file.absolutePath}")
            file.isFile -> {
                if (shouldIncludeFile(file, config)) {
                    validFiles.add(file)
                } else {
                    println("警告：文件被排除规则过滤，已跳过: ${file.absolutePath}")
                }
            }

            file.isDirectory -> {
                if (!shouldExcludeDirectory(file, config)) {
                    validDirectories.add(file)
                } else {
                    println("警告：目录被排除规则过滤，已跳过: ${file.absolutePath}")
                }
            }

            else -> println("警告：未知路径类型，已跳过: ${file.absolutePath}")
        }
    }

    if (validDirectories.isEmpty() && validFiles.isEmpty()) {
        println("错误：没有找到有效的源路径。")
        return
    }

    // 处理输出文件
    if (outputFile.exists()) {
        println("警告：输出文件 '${outputFile.name}' 已存在，将被覆盖。")
        if (!outputFile.delete()) {
            println("错误：无法删除已存在的输出文件: ${outputFile.absolutePath}")
            return
        }
    }

    println("开始合并文件:")
    if (validDirectories.isNotEmpty()) {
        println("  目录 (${validDirectories.size}):")
        validDirectories.forEach { dir ->
            println("    - ${dir.absolutePath}")
        }
    }
    if (validFiles.isNotEmpty()) {
        println("  直接文件 (${validFiles.size}):")
        validFiles.forEach { file ->
            println("    - ${file.absolutePath}")
        }
    }
    println("输出文件: ${outputFile.absolutePath}")

    // 显示过滤规则
    printFilterRules(config)

    try {
        // 收集所有文件
        val allFilesToMerge = mutableListOf<Pair<File, String>>() // Pair<文件, 来源类型>

        // 添加直接指定的文件
        validFiles.forEach { file ->
            allFilesToMerge.add(Pair(file, "直接文件"))
        }

        // 添加目录中的文件
        validDirectories.forEach { directory ->
            val filesInDirectory = collectFilesFromDirectory(directory, config)
            filesInDirectory.forEach { file ->
                allFilesToMerge.add(Pair(file, "目录: ${directory.name}"))
            }
            println("在目录 '${directory.name}' 中找到 ${filesInDirectory.size} 个有效文件")
        }

        if (allFilesToMerge.isEmpty()) {
            println("经过过滤后没有找到任何文件。")
            outputFile.createNewFile()
            println("已创建空的输出文件: ${outputFile.absolutePath}")
            return
        }

        // 按文件路径排序
        val sortedFiles = allFilesToMerge.sortedBy { it.first.absolutePath }
        println("总共找到 ${sortedFiles.size} 个文件准备合并。")

        // 合并文件
        val charset = try {
            java.nio.charset.Charset.forName(config.encoding)
        } catch (e: Exception) {
            println("警告：不支持的字符编码 '${config.encoding}'，使用 UTF-8")
            StandardCharsets.UTF_8
        }

        outputFile.bufferedWriter(charset).use { writer ->
            var currentSourceType: String? = null
            var processedCount = 0
            var totalSize = 0L

            sortedFiles.forEach { (file, sourceType) ->
                try {
                    processedCount++
                    val fileSize = file.length()
                    totalSize += fileSize

                    println(
                        "正在合并 ($processedCount/${sortedFiles.size}): ${file.absolutePath} (${
                            formatFileSize(
                                fileSize
                            )
                        })"
                    )

                    // 如果启用分组标题且来源类型改变，添加分组分隔符
                    if (config.includeDirectoryHeaders && currentSourceType != sourceType) {
                        if (currentSourceType != null) {
                            writer.newLine()
                            writer.newLine()
                        }
                        writer.write("========== $sourceType ==========")
                        writer.newLine()
                        writer.newLine()
                        currentSourceType = sourceType
                    }

                    // 添加文件信息头
                    writer.write("--- 文件: ${file.absolutePath} ---")
                    writer.newLine()
                    writer.write("--- 大小: ${formatFileSize(fileSize)} | 修改时间: ${java.util.Date(file.lastModified())} ---")
                    writer.newLine()
                    writer.newLine()

                    // 复制文件内容
                    file.bufferedReader(charset).use { reader ->
                        reader.copyTo(writer)
                    }

                    // 添加文件结束分隔符
                    writer.newLine()
                    writer.write("--- 文件结束: ${file.name} ---")
                    writer.newLine()
                    writer.newLine()

                } catch (e: IOException) {
                    println("警告：读取文件 ${file.absolutePath} 时出错，已跳过。错误: ${e.message}")
                } catch (e: SecurityException) {
                    println("警告：没有权限读取文件 ${file.absolutePath}，已跳过。")
                } catch (e: Exception) {
                    println("警告：处理文件 ${file.absolutePath} 时发生未知错误，已跳过。错误: ${e.message}")
                }
            }

            // 添加统计信息到文件末尾
            writer.newLine()
            writer.write("========== 合并统计 ==========")
            writer.newLine()
            writer.write("合并时间: ${java.util.Date()}")
            writer.newLine()
            writer.write("总文件数: ${sortedFiles.size}")
            writer.newLine()
            writer.write("总大小: ${formatFileSize(totalSize)}")
            writer.newLine()
            writer.write("字符编码: ${config.encoding}")
            writer.newLine()
        }

        println("成功！已将 ${sortedFiles.size} 个文件合并到 ${outputFile.absolutePath}")
    } catch (e: IOException) {
        println("错误：在文件操作过程中发生 I/O 错误: ${e.message}")
    } catch (e: SecurityException) {
        println("错误：权限不足，无法访问目录或写入输出文件。")
    } catch (e: Exception) {
        println("错误：发生了一个意外错误: ${e.message}")
    }
}

/**
 * 从目录中收集符合条件的文件
 */
private fun collectFilesFromDirectory(directory: File, config: MergeConfig): List<File> {
    return directory.walkTopDown()
        .onEnter { dir -> !shouldExcludeDirectory(dir, config) }
        .filter { it.isFile }
        .filter { shouldIncludeFile(it, config) }
        .toList()
}

/**
 * 判断是否应该排除目录
 */
private fun shouldExcludeDirectory(directory: File, config: MergeConfig): Boolean {
    val dirName = directory.name.lowercase()
    val dirPath = directory.absolutePath.lowercase()

    return config.excludedDirectories.any { excludedDir ->
        val excluded = excludedDir.lowercase()
        dirName == excluded || dirPath.contains(excluded)
    }
}

/**
 * 判断是否应该包含文件
 */
private fun shouldIncludeFile(file: File, config: MergeConfig): Boolean {
    val fileName = file.name
    val fileNameLower = fileName.lowercase()
    val extension = file.extension.lowercase()

    // 检查文件大小限制
    if (file.length() > config.maxFileSize) {
        return false
    }

    // 检查排除的文件名
    if (config.excludedFileNames.any { it.lowercase() == fileNameLower }) {
        return false
    }

    // 检查排除的扩展名
    if (config.excludedFileExtensions.any { it.lowercase() == extension }) {
        return false
    }

    // 检查包含的扩展名（如果指定了）
    if (config.includedFileExtensions.isNotEmpty() &&
        !config.includedFileExtensions.any { it.lowercase() == extension }
    ) {
        return false
    }

    // 检查排除的文件模式
    if (config.excludedFilePatterns.any { pattern ->
            pattern.containsMatchIn(fileName) || pattern.containsMatchIn(file.absolutePath)
        }) {
        return false
    }

    return true
}

/**
 * 打印过滤规则
 */
private fun printFilterRules(config: MergeConfig) {
    println("\n应用的过滤规则:")

    if (config.excludedDirectories.isNotEmpty()) {
        println("  排除目录: ${config.excludedDirectories.joinToString(", ")}")
    }

    if (config.excludedFileExtensions.isNotEmpty()) {
        println("  排除文件扩展名: ${config.excludedFileExtensions.joinToString(", ")}")
    }

    if (config.includedFileExtensions.isNotEmpty()) {
        println("  仅包含扩展名: ${config.includedFileExtensions.joinToString(", ")}")
    }

    if (config.excludedFileNames.isNotEmpty()) {
        println("  排除文件名: ${config.excludedFileNames.joinToString(", ")}")
    }

    if (config.excludedFilePatterns.isNotEmpty()) {
        println("  排除文件模式: ${config.excludedFilePatterns.size} 个正则表达式")
    }

    if (config.maxFileSize != Long.MAX_VALUE) {
        println("  最大文件大小: ${formatFileSize(config.maxFileSize)}")
    }

    println("  字符编码: ${config.encoding}")
    println()
}

/**
 * 格式化文件大小
 */
private fun formatFileSize(bytes: Long): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var size = bytes.toDouble()
    var unitIndex = 0

    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }

    return "%.2f %s".format(size, units[unitIndex])
}

/**
 * 单个目录合并的便捷方法（保持向后兼容）
 */
fun mergeFilesInDirectory(sourceDirectoryPath: String, outputFileName: String = "merged_output.txt") {
    mergeFilesFromMultiplePaths(listOf(sourceDirectoryPath), outputFileName)
}

/**
 * 从配置文件读取路径列表的辅助方法
 */
fun readPathListFromFile(configFilePath: String): List<String> {
    return try {
        val configFile = File(configFilePath)
        if (!configFile.exists()) {
            println("配置文件不存在: $configFilePath")
            return emptyList()
        }

        configFile.readLines(StandardCharsets.UTF_8)
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
    } catch (e: Exception) {
        println("读取配置文件时出错: ${e.message}")
        emptyList()
    }
}

fun main(args: Array<String>) {
    when {
        // 从命令行参数读取多个路径
        true -> {
            val mkpPath = "D:\\\\code\\\\compose-webview-multiplatform"
            val tvrPath = "D:\\agi\\tvrmusicnew\\src\\main\\java\\com\\treevalue\\quick"
            val paths = listOf<String>(tvrPath)
            val outputPath = "D:\\agi\\tvrmusicnew\\src\\test\\output\\merged_output.txt"

            val config = MergeConfig(
                excludedDirectories = setOf(
                    ".git", ".svn", "node_modules", "target", "build", "res", "doc", "media",
                    ".idea", ".vscode", "bin", "obj", ".gradle", "gradle", ".github", ".run"
                ),
                excludedFileExtensions = setOf(
                    "class", "jar", "war", "exe", "dll", "so", "dylib",
                    "zip", "rar", "7z", "tar", "gz", "bz2", ".gitignore",
                    "jpg", "jpeg", "png", "gif", "bmp", "ico", "editorconfig",
                    "mp3", "mp4", "avi", "mkv", "wav", "flac", "gitignore", "md"
                ),
                includedFileExtensions = setOf("kts","kt","xml"),
                maxFileSize = 20 * 1024 * 1024 // 1MB 限制
            )

            mergeFilesFromMultiplePaths(paths, outputPath, config)
        }

        else -> {
            println("=== 示例 1: 基本多路径合并 ===")
            val basicPaths = listOf(
                "D:\\agi\\tvrmusicnew\\src\\main\\java\\com\\treevalue\\quick",
                "D:\\agi\\tvrmusicnew\\src\\main\\java\\com\\treevalue\\service",
                "D:\\agi\\specific_file.java" // 直接指定文件
            )

            val basicConfig = MergeConfig(
                excludedDirectories = setOf("test", "tests", ".git"),
                excludedFileExtensions = setOf("class", "jar"),
                includedFileExtensions = setOf("java", "kt", "txt") // 只包含这些类型
            )

            mergeFilesFromMultiplePaths(
                basicPaths,
                "D:\\agi\\tvrmusicnew\\src\\test\\output\\basic_merged.txt",
                basicConfig
            )

            println("\n=== 示例 2: 高级过滤合并 ===")
            val advancedPaths = listOf(
                "D:\\project\\src",
                "D:\\project\\config.xml",
                "D:\\project\\readme.md"
            )

            val advancedConfig = MergeConfig(
                includeDirectoryHeaders = true,
                excludedDirectories = setOf("target", "build", "node_modules", ".git", ".idea"),
                excludedFileExtensions = setOf("class", "jar", "exe", "dll", "zip"),
                excludedFileNames = setOf("temp.txt", "cache.dat"),
                excludedFilePatterns = listOf(
                    Regex(".*\\.tmp$", RegexOption.IGNORE_CASE),
                    Regex(".*test.*", RegexOption.IGNORE_CASE),
                    Regex(".*backup.*", RegexOption.IGNORE_CASE)
                ),
                maxFileSize = 5 * 1024 * 1024, // 5MB 限制
                encoding = "UTF-8"
            )

            mergeFilesFromMultiplePaths(
                advancedPaths,
                "advanced_merged.txt",
                advancedConfig
            )
        }
    }
}
