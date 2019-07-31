package server

import determineFileExtension
import getContentTypeHeaderForFileType
import getFileFromPath
import getFileInputStream
import getMD5Content
import org.http4k.core.*
import org.http4k.filter.CorsPolicy
import org.http4k.filter.ServerFilters
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.server.Jetty
import org.http4k.server.asServer

fun startHttp4k(inputPort: Int, directory: String) {

    val handler = routes(
        "files/{filename}" bind Method.GET to { req: Request ->
            getFileResponse(directory, req.path("filename")!!)
        },
        "files/download/{filename}" bind Method.GET to { req: Request ->
            getFileDownloadResponse(directory, req.path("filename")!!)
        },
        "/" bind Method.GET to { req: Request ->
            Response(Status.OK)
                .body(getFileInputStream("$directory/index.html")!!)
        },
        "md5" bind Method.POST to { req: Request ->
            Response(Status.OK)
                .body(getMD5Content(req.bodyString()))
        }
    )

    ServerFilters.Cors(CorsPolicy.UnsafeGlobalPermissive).then(handler).asServer(Jetty(inputPort)).start()
}

fun getFileResponse(filesDir: String, filename: String): Response {
    val file = getFileFromPath(filesDir, filename)

    if (file.exists()) {
        return Response(Status.OK)
            .header("Content-Type", getContentTypeHeaderForFileType(determineFileExtension(filename)))
            .body(getFileInputStream("$filesDir/$filename")!!)
    } else {
        return Response(Status.NOT_FOUND)
            .body("Error: content not found!")
    }
}

fun getFileDownloadResponse(filesDir: String, filename: String): Response {
    val file = getFileFromPath(filesDir, filename)
    if (file.exists()) {
        println("File ${file.name} was downloaded.")
        return Response(Status.OK)
            .header("Content-Disposition", "attachment; filename=$filename")
            .body(getFileInputStream("$filesDir/$filename")!!)

//TODO: handle download cancel
//            println("File ${file.name} download was cancelled by the client.")
//            return Response(Status.OK)
//                .body("Download cancelled.")
//

    } else return Response(Status.NOT_FOUND)
        .body("Error: content not found!")
}
