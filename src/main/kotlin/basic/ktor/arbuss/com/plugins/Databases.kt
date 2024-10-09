package basic.ktor.arbuss.com.plugins

import basic.ktor.arbuss.com.database.ExposedUser
import basic.ktor.arbuss.com.database.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*

private const val DATABASE_CONNECTION_KEY = "DB_CONN"
private const val DATABASE_CONNECTION_PORT_KEY = "DB_PORT"
private const val DATABASE_NAME_KEY = "DB_NAME"
private const val DATABASE_USER_KEY = "DB_USER"
private const val DATABASE_PASSWORD_KEY = "DB_PASSWORD"

fun Application.configureDatabases() {
    val database = Database.connect(
        url = "jdbc:mariadb://${System.getenv(DATABASE_CONNECTION_KEY)}:${System.getenv(DATABASE_CONNECTION_PORT_KEY)}/" +
                System.getenv(DATABASE_NAME_KEY),
        user = System.getenv(DATABASE_USER_KEY),
        password = System.getenv(DATABASE_PASSWORD_KEY),
    )

    val userService = UserService(database)

    routing {
        authenticate("myJwt") {
            get("/users/list") {
                val users = userService.getAll()
//                call.respond(HttpStatusCode.OK, users)
                call.respond(HttpStatusCode.OK, "Access granted: $users")
            }
        }
        // Create user
        post("/users") {
            val user = call.receive<ExposedUser>()
            val id = userService.create(user)
            call.respond(HttpStatusCode.Created, id)
        }

        // Read user
        get("/users/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val user = userService.read(id)
            if (user != null) {
                call.respond(HttpStatusCode.OK, user)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        // Update user
        put("/users/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val user = call.receive<ExposedUser>()
            userService.update(id, user)
            call.respond(HttpStatusCode.OK)
        }

        // Delete user
        delete("/users/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            userService.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }
}
