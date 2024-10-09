package basic.ktor.arbuss.com.plugins.login

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation

@Serializable
data class KCUser(val username: String, val firstName: String, val lastName: String, val password: String)

fun Application.configureKeycloak() {
    val kc = Keycloak.getInstance(
        "http://localhost:8080",
        "master",
        "arbuss",
        "admin",
        "admin-cli"
    )
    routing {
        post("/kc/create") {
            val user = call.receive<KCUser>()
            createUser(kc, user)
            call.respond(HttpStatusCode.Created)
        }

        get("/kc/users") {
            val users = getUsers(kc)
            call.respond(HttpStatusCode.OK, users)
        }
    }
}

private fun createUser(kc: Keycloak, user: KCUser) {
    val credentialRepresentation = CredentialRepresentation().apply {
        type = CredentialRepresentation.PASSWORD
        value = user.password
    }

    val userRepresentation = UserRepresentation().apply {
        username = user.username
        firstName = user.firstName
        lastName = user.lastName
        email = "sample-email${(0..100000).random()}@sample.com"
        credentials = listOf(credentialRepresentation)
        isEnabled = true
        realmRoles = listOf("common_user")
    }

    val realmResource = kc.realm("master")
    val userResource = realmResource.users()

    try {
        userResource.create(userRepresentation)
    } catch (e: Exception) {
        println("createUser user = $user")
        e.printStackTrace()
    }
}

private fun getUsers(kc: Keycloak): List<KCUser> {
    val realm = kc.realm("master")
    val users = realm.users().list()
    println("getUsers users = $users")
    return users.mapNotNull {
        KCUser(
            username = it.username,
            firstName = it.firstName,
            lastName = it.lastName,
            password = it.email
        )
    }
//    val realmRepresentation = kc.realm("master").toRepresentation()
//    return realmRepresentation.users.mapNotNull {
//        KCUser(
//            username = it.username,
//            firstName = it.firstName,
//            lastName = it.lastName,
//            password = it.email
//        )
//    }
}