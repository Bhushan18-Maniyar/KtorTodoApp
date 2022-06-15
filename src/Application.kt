package com.bbm.todo

import com.bbm.todo.DatabaseConnection.db
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.ktorm.database.Database

/**
 * GET: Retrieves data from the server. Should have no other effect.
 *
 * PUT: Replaces target resource with the request payload. Can be used to update or create a new resources.
 *
 * PATCH: Similar to PUT, but used to update only certain fields within an existing resource.
 *
 * POST: Performs resource-specific processing on the payload. Can be used for different actions including creating a new resource, uploading a file or submitting a web form.
 *
 * DELETE: Removes data from the server.
 *
 * TRACE: Provides a way to test what server receives. It simply returns what was sent.
 *
 * OPTIONS: Allows a client to get information about the request methods supported by a service. The relevant response header is Allow with supported methods. Also used in CORS as preflight request to inform server about actual request method and ask about custom headers.
 *
 * HEAD: Returns only the response headers.
 *
 * CONNECT: Used by browser when it knows it talks to a proxy and the final URI begins with https://. The intent of CONNECT is to allow end-to-end encrypted TLS session, so the data is unreadable to a proxy.
 * */


object DatabaseConnection {
    val db = Database.connect(
        url = "jdbc:mysql://localhost:3306/ktor_todo",
        driver = "com.mysql.cj.jdbc.Driver",
        user = "root",
        password = "12345678"
    )
}

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {

        install(ContentNegotiation) {
            json()
        }

        println(db.name)


        configureRouting()

    }.start()
}


fun Application.configureRouting() {
    routing {
        userRouting()

        todosRouting()
    }
}
