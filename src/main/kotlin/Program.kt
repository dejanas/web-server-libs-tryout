import server.startHttp4k
import server.startHttpServer
import server.startKtor
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.MissingValueException
import java.lang.Exception

const val DEFAULT_PORT = "8080"
const val DEFAULT_DIR = ""

fun main(args: Array<String>) {
    val conf: TestType = TestType.KTOR
    try {
        val port = extractArg(args, "port", DEFAULT_PORT).toInt()
        val directory = extractArg(args, "directory", DEFAULT_DIR)
        val path = getFilesDirectoryPath(directory)
        when (conf) {
            TestType.HTTP -> startHttpServer(port, path) //single threaded
            TestType.KTOR -> startKtor(port, path)
            TestType.HTTP4K -> startHttp4k(port, path)
        }
    } catch (e: Exception) {
        println(e)
        return
    }
}

//tried reading args by ArgsParser
fun main1(args: Array<String>) {

    val conf: TestType = TestType.HTTP4K

    try {
        ArgParser(args).parseInto(::MyArgs).run {
            println("Port: $port, Directory: $directory")
            val inputPort = port
            when (conf) {
                TestType.HTTP -> startHttpServer(inputPort, directory) //single threaded
                TestType.KTOR -> startKtor(inputPort, directory)
                TestType.HTTP4K -> startHttp4k(inputPort, directory)
            }
        }
    } catch (mve: MissingValueException) {
        println("Please specify ${mve.valueName} argument.")
    }
}




