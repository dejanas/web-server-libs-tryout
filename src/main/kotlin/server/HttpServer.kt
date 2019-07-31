package server

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import determineFileExtension
import getContentTypeHeaderForFileType
import getFileFromPath
import getMD5Content
import readFileAsTextUsingInputStream
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.util.*

fun startHttpServer(inputPort: Int, directory: String) {
    val server = HttpServer.create(InetSocketAddress(inputPort), 0)
    server.apply {

        createContext("/files", GetHandler(directory))

        createContext("/files/download", DownloadHandler(directory))

        createContext("/md5", PostHandler())

        start()
    }
}

class GetHandler(directory: String) : HttpHandler {
    private var directory: String = ""

    init {
        this.directory = directory
    }

    override fun handle(exchange: HttpExchange?) {

        var requestMethod: String = exchange!!.requestMethod
        if (requestMethod.equals("GET", ignoreCase = true)) {

            System.out.println("GET: ${exchange.requestURI}")

            val filename = exchange.requestURI.toString().substringAfterLast('/')

            val file = getFileFromPath(directory, filename)

            if (file.exists()) {

                exchange.responseHeaders.add("Date", "${Date()}")

                exchange.responseHeaders.add(
                    "Content-type",
                    getContentTypeHeaderForFileType(determineFileExtension(filename))
                )

                exchange.sendResponseHeaders(200, 0)

                PrintWriter(exchange.responseBody).use { out ->

                    out.print(readFileAsTextUsingInputStream(file.path))
                }


            } else {

                exchange.sendResponseHeaders(404, 0)
                PrintWriter(exchange.responseBody).use { out ->
                    out.println("Error: no content found!")
                }
            }
        }
    }
}

class DownloadHandler(directory: String) : HttpHandler {
    private var directory: String = ""

    init {
        this.directory = directory
    }

    override fun handle(exchange: HttpExchange?) {

        val requestMethod: String = exchange!!.requestMethod
        if (requestMethod.equals("GET", ignoreCase = true)) {

            System.out.println("GET: ${exchange.requestURI}")

            val filename = exchange.requestURI.toString().substringAfterLast('/')

            val file = getFileFromPath(directory, filename)

            if (file.exists()) {

                exchange.responseHeaders.add("Date", "${Date()}")
                exchange.responseHeaders.add("Content-Disposition", "attachment; filename=\"$filename\"")
                exchange.sendResponseHeaders(200, 0)

                PrintWriter(exchange.responseBody).use { out ->
                    out.write(file.readText())
                }
// TODO: handle download cancel
//                    println("File $filename was downloaded.")
//                    println("Download was cancelled.")
//                    exchange.sendResponseHeaders(200, 0)
//                    PrintWriter(exchange.responseBody).use { out ->
//                        out.println("Download was cancelled.")
//                    }
//                    exchange.responseBody.close()
            }

        } else {
            exchange.sendResponseHeaders(404, 0)
            PrintWriter(exchange.responseBody).use { out ->
                out.println("Error: no content found!")
            }
        }
    }

}

class PostHandler : HttpHandler {
    override fun handle(exchange: HttpExchange?) {

        val requestMethod: String = exchange!!.requestMethod
        if (requestMethod.equals("POST", ignoreCase = true)) {

            System.out.println("POST: ${exchange.requestURI}")

            exchange.responseHeaders.add("Content-type", "application/json")
            exchange.sendResponseHeaders(200, 0)
            PrintWriter(exchange.responseBody).use { out ->
                out.println(getMD5Content(exchange.requestBody.toString()))
            }
        }
    }
}
