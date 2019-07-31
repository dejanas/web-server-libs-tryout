import com.xenomachina.argparser.ArgParser
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.security.MessageDigest

enum class TestType {
    HTTP4K,
    KTOR,
    HTTP
}

enum class FileType {
    TEXT,
    HTML,
    JSON,
    PKCS8
}

fun extractArg(args: Array<String>, argName: String, defaultValue: String): String {
    try {
        var returnArg = ""
        val arg = args.find { it.startsWith("--$argName") }
        if (arg.isNullOrEmpty()) {
            returnArg = defaultValue
        } else {
            if (arg.startsWith("--directory=")) {
                returnArg = arg.removePrefix("--$argName=")
                    .trim()
            } else {
                returnArg = args[args.indexOf("--$argName") + 1]
            }
        }
        return returnArg
    } catch (ex: java.lang.Exception) {
        throw Exception("Invalid command line argument/s.")
    }
}

//tried using this lib from curiosity
class MyArgs(parser: ArgParser) {
    val port by parser.storing("port number") { toInt() }
    val directory by parser.positional("directory path")
}

fun getFileInputStream(pathToFile: String): InputStream? {
    val file = File(pathToFile)
    if (file.exists()) {
        return file.inputStream()
    } else
        return null
}

fun readFileAsTextUsingInputStream(fileName: String) = File(fileName).inputStream().readBytes().toString(Charsets.UTF_8)

fun determineFileExtension(filename: String): FileType {

    if (filename.endsWith(".txt")) {
        return FileType.TEXT
    }
    if (filename.endsWith(".html")) {
        return FileType.HTML
    }
    if (filename.endsWith(".json")) {
        return FileType.JSON
    }
    if (filename.endsWith(".html")) {
        return FileType.PKCS8
    } else {
        return FileType.TEXT
    }
}

fun getContentTypeHeaderForFileType(fileType: FileType): String {
    return when (fileType) {
        FileType.TEXT -> "text/plain"
        FileType.HTML -> "text/html"
        FileType.JSON -> "application/json"
        FileType.PKCS8 -> "application/octet-stream"
    }
}

fun getMD5Content(bodyString: String): String {
    var md5content = bodyString.md5()

    return "{\"md5\" : \"$md5content\"}"

}

fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    val digested = md.digest(toByteArray())
    return digested.joinToString("") {
        String.format("%02x", it)
    }
}

fun getFilesDirectoryPath(directory: String): String {
    var file = File(directory)
    if (!file.isAbsolute) {
        val workingDirectory = System.getProperty("user.dir")
        val separator = System.getProperty("file.separator")
        val path = "$workingDirectory$separator$directory"
        file = File(path)
    }
    if (file.exists()) {
        return file.path
    } else {
        throw FileNotFoundException("Specified path(${file.path})not found.")
    }
}

fun getFileFromPath(path: String, filename: String?): File {
    val separator = System.getProperty("file.separator")
    val finalPath = "$path$separator$filename"
    return File(finalPath)
}






