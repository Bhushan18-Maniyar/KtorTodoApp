package com.bbm.todo

import com.bbm.todo.DatabaseConnection.db
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.ktorm.dsl.*
import org.ktorm.dsl.map
import org.ktorm.entity.*


fun Application.userRouting() {
    routing {
        createUser()
        updateUser()
        getUsers()
        deleteUser()
    }
}

fun Routing.createUser() {
    post("/create_user") {
        val param = call.receiveParameters()
        val name = param["name"]

        if (name.isNullOrEmpty() || name.toString().trim().isEmpty()) {
            call.respond(
                HttpStatusCode.BadRequest,
                CommonRes(
                    data = "null",
                    meta = CommonMeta(
                        data = "Name can not be empty!"
                    )
                )
            )
            return@post
        }

        val result = db.insert(UserEntity) {
            set(it.user_name, name)
        }

        if (result > 0) {
            call.respond(
                HttpStatusCode.Created,
                CommonRes(
                    data = "",
                    meta = CommonMeta(
                        data = "User created successfully!"
                    )
                )
            )
        } else {
            call.respond(
                HttpStatusCode.InternalServerError,
                CommonRes(
                    data = "null",
                    meta = CommonMeta(
                        data = "Something went wrong!"
                    )
                )
            )
        }
    }
}

fun Routing.deleteUser() {
    delete("/delete_user/{id}") {

        val id = call.parameters["id"]

        if (id.isNullOrEmpty() || id.toIntOrNull() == null) {
            call.respond(
                HttpStatusCode.BadRequest,
                CommonRes(
                    data = "null",
                    meta = CommonMeta(
                        data = "Please Provide user id!"
                    )
                )
            )
            return@delete
        }


        val result = db.delete(UserEntity) {
            it.id eq id.toInt()
        }

        if (result > 0) {

            db.delete(TodoEntity) {
                it.user_id eq id.toInt()
            }

            call.respond(
                HttpStatusCode.Accepted,
                CommonRes(
                    "null", CommonMeta(
                        data = "Record deleted Successfully!"
                    )
                )
            )
        } else {
            call.respond(
                HttpStatusCode.NotFound,
                CommonRes(
                    data = "null",
                    meta = CommonMeta(
                        data = "User not found with id $id..."
                    )
                )
            )
        }
    }
}

fun Routing.updateUser() {
    put("/update_user/{id}") {

        val id = call.parameters["id"]
        if (id.isNullOrEmpty() && id?.toIntOrNull() == null) {
            call.respond(
                HttpStatusCode.BadRequest,
                CommonRes(
                    data = "null",
                    meta = CommonMeta(
                        data = "Please Provide user id!"
                    )
                )
            )
            return@put
        }

        val param = call.receiveParameters()
        val name = param["name"]

        if (name.isNullOrEmpty() || name.toString().trim().isEmpty()) {
            call.respond(
                HttpStatusCode.BadRequest,
                CommonRes(
                    data = "null",
                    meta = CommonMeta(
                        data = "Name can not be empty!"
                    )
                )
            )
            return@put
        }

        val result = db.update(UserEntity) {
            set(it.user_name, name)
            where {
                it.id eq id.toInt()
            }
        }

        if (result > 0) {
            val user = db.users.find {
                it.id eq id.toInt()
            }!!
            call.respond(
                HttpStatusCode.Accepted,
                CommonRes(
                    data = UserRes(
                        id = user.id,
                        name = user.name
                    ),
                    meta = CommonMeta(
                        data = "Record Updated Successfully!"
                    )
                )
            )
        } else {
            call.respond(
                HttpStatusCode.NotFound,
                CommonRes(
                    data = "null",
                    meta = CommonMeta(
                        data = "User not found with id $id..."
                    )
                )
            )
        }
    }
}

fun Routing.getUsers() {
    get("/get_users") {

        val responseFromSeq = db.users.map {
            UserRes(
                id = it.id,
                name = it.name
            )
        }

        /*
        val normalRes = db.from(UserEntity)
            .select()
            .map {
                UserRes(
                    id = it[UserEntity.id] ?: 0,
                    name = it[UserEntity.user_name] ?: "-"
                )
            }
         */

        call.respond(
            CommonRes(
                data = responseFromSeq,
                meta = UserResMeta(
                    total_data = db.users.count()
                )
            )
        )
    }
}