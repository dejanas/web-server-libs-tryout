package server

import getFileFromPath
import getMD5Content
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.content.defaultResource
import io.ktor.http.content.static
import io.ktor.http.content.staticBasePackage
import io.ktor.request.receiveParameters
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.response.respondFile
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun startKtor(inputPort: Int, directory: String) {

    val env = applicationEngineEnvironment {
        module {
            main(directory)
        }
        // Public API
        connector {
            host = "localhost"
            port = inputPort
        }
    }
    embeddedServer(Netty, env).start(true)
}

fun Application.main(directory: String) {

    // This feature sets a Date and server headers automatically.
    install(DefaultHeaders)
    // Logs all the requests performed
    install(CallLogging)

    install(ContentNegotiation)

    routing {

        get("/files/{filename}") {
            val filename = call.parameters["filename"]
            val file = getFileFromPath(directory, filename)
            val ext = file.extension
            if (ext == ".pkcs8") {
                call.response.header("Content-Type", "application/octet-stream")
            }
            if (file.exists()) {
                call.respondFile(file) {}
            } else call.respond(HttpStatusCode.NotFound, "Error: no content found!")
        }

        get("/files/download/{filename}") {
            val filename = call.parameters["filename"]!!
            val file = getFileFromPath(directory, filename)
            if (file.exists()) {
                    call.response.header("Content-Disposition", "attachment; filename=\"$filename\"")
                    call.respondFile(file)
                    println("File ${file.name} was downloaded.")
// TODO: handle download cancel
//                    call.respond(HttpStatusCode.OK, "Download cancelled.")
//                    println("File ${file.name} download was cancelled by the client.")
            } else call.respond(HttpStatusCode.NotFound, "Error: no content found!")
        }

        get {
            val file = getFileFromPath(directory, "index.html")
            call.respondFile(file)
        }

        post("/md5") {
            val content = call.receiveParameters()
            call.respond(TextContent(getMD5Content(content.toString()), ContentType.Application.Json))
        }

        static {
            staticBasePackage = "files"
            //                   staticRootFolder = File("files")
//
            defaultResource("index.html")
//                    resource("encoding/utf8", "UTF-8-demo.html")
//                    resource("html", "test.html")
//                    resource("xml", "sample.xml")
//                    resource("test1.txt")
//                    resource("message.js")
//                   resource("postman", "httpbin.postman_collection.json")

            // And for the '/files' path, it will serve the [filesDir].
//                    route("files") {
//                        files(filesDir)
//                    }
        }
    }
}

