package com.bbm.todo

import com.bbm.todo.DatabaseConnection.db
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import org.ktorm.dsl.*
import org.ktorm.entity.*
import sun.misc.MessageUtils.where

data class Headers(
    val userId: Int
)

inline fun <T> PipelineContext<Unit, ApplicationCall>.validateHeader(executable: (Headers) -> T): T? {
    val userId = call.request.headers["user_id"] ?: ""

    return if (userId.isNotEmpty() && userId.toIntOrNull() != null) {
        db.users.find {
            it.id eq userId.toInt()
        }?.let {
            executable.invoke(Headers(userId.toInt()))
        } ?: return null
    } else null
}

fun Application.todosRouting() {
    routing {
        createTodo()
        getMyTodos()
        updateMyTodos()
        deleteMyTodos()
    }
}

fun Routing.createTodo() {
    post("/create_todo") {
        validateHeader { header ->

            val params = call.receiveParameters()
            val title = params["title"].orEmpty().trim()
            val description = params["description"].orEmpty().trim()
            val errorRes = when {
                title.isEmpty() -> {
                    CommonRes(
                        "null",
                        CommonMeta("Please provide title")
                    )
                }
                description.isEmpty() -> {
                    CommonRes(
                        "null",
                        CommonMeta("Please provide description")
                    )
                }
                else -> {
                    null
                }
            }

            if (errorRes != null) {
                call.respond(
                    HttpStatusCode.NotAcceptable,
                    errorRes
                )
                return@post
            }

            val res = db.insert(TodoEntity) {
                set(it.title, title)
                set(it.description, description)
                set(it.user_id, header.userId)
            }

            if (res > 0) {
                call.respond(
                    HttpStatusCode.Accepted,
                    CommonRes(
                        "Todo created!",
                        CommonMeta("Success")
                    )
                )
            } else {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    CommonRes(
                        "null",
                        CommonMeta("Something went wrong!")
                    )
                )
            }

        } ?: kotlin.run {
            call.respond(
                HttpStatusCode.Forbidden,
                CommonRes(
                    "null",
                    CommonMeta(
                        "Please provide valid userId!"
                    )
                )
            )
            return@post
        }
    }
}

fun Routing.getMyTodos() {
    get("/get_my_todos") {
        validateHeader { header ->

            val queryParam = call.request.queryParameters

            val offset = queryParam["offset"]?.toIntOrNull()
            val limit = queryParam["limit"]?.toIntOrNull()

            val errorRes = when {
                offset == null -> {
                    CommonRes(
                        "null",
                        CommonMeta("Please provide valid offset")
                    )
                }
                limit == null -> {
                    CommonRes(
                        "null",
                        CommonMeta("Please provide valid offset")
                    )
                }
                else -> null
            }

            if (errorRes != null) {
                call.respond(
                    HttpStatusCode.NotAcceptable,
                    errorRes
                )
                return@get
            }

            val res = db
                .todos
                .filter {
                    it.user_id eq header.userId
                }
                .drop(offset!!.toInt())
                .take(limit!!.toInt())
                .map {
                    Todos(
                        id = it.id,
                        title = it.title,
                        description = it.description
                    )
                }

            call.respond(
                HttpStatusCode.Accepted,
                CommonRes(
                    res,
                    TodosMeta(
                        total_data = db.todos.count {
                            it.user_id eq header.userId
                        }
                    )
                )
            )

        } ?: kotlin.run {
            call.respond(
                HttpStatusCode.Forbidden,
                CommonRes(
                    "null",
                    CommonMeta(
                        "Please provide valid userId!"
                    )
                )
            )
            return@get
        }
    }
}

fun Routing.updateMyTodos() {
    put("/update_todo/{id}") {
        validateHeader { header ->

            val todoId = call.parameters["id"]?.toIntOrNull() ?: -1

            if (db.todos.find { it.id eq todoId and (it.user_id eq header.userId) } == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    CommonRes(
                        "null",
                        CommonMeta(
                            "Todo with id:$todoId is not exists in database!"
                        )
                    )
                )
                return@put
            }

            val params = call.receiveParameters()
            val title = params["title"].orEmpty().trim()
            val description = params["description"].orEmpty().trim()
            val errorRes = when {
                title.isEmpty() -> {
                    CommonRes(
                        "null",
                        CommonMeta("Please provide title")
                    )
                }
                description.isEmpty() -> {
                    CommonRes(
                        "null",
                        CommonMeta("Please provide description")
                    )
                }
                else -> {
                    null
                }
            }

            if (errorRes != null) {
                call.respond(
                    HttpStatusCode.NotAcceptable,
                    errorRes
                )
                return@put
            }

            val res = db.update(TodoEntity) {
                set(it.title, title)
                set(it.description, description)
                where {
                    it.id eq todoId and (it.user_id eq header.userId)
                }
            }

            if (res > 0) {
                call.respond(
                    HttpStatusCode.Accepted,
                    CommonRes(
                        "Todo updated!",
                        CommonMeta("Success")
                    )
                )
            } else {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    CommonRes(
                        "null",
                        CommonMeta("Something went wrong!")
                    )
                )
            }


        } ?: kotlin.run {
            call.respond(
                HttpStatusCode.Forbidden,
                CommonRes(
                    "null",
                    CommonMeta(
                        "Please provide valid userId!"
                    )
                )
            )
            return@put
        }
    }
}

fun Routing.deleteMyTodos() {
    delete("/delete_todo/{id}") {
        validateHeader { header ->

            val todoId = call.parameters["id"]?.toIntOrNull() ?: -1

            if (db.todos.find {
                    it.id eq todoId and (it.user_id eq header.userId)
                } == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    CommonRes(
                        "null",
                        CommonMeta(
                            "Todo with id:$todoId is not exists in database!"
                        )
                    )
                )
                return@delete
            }

            val res = db.delete(TodoEntity) {
                it.id eq todoId and (it.user_id eq header.userId)
            }

            if (res > 0) {
                call.respond(
                    HttpStatusCode.Accepted,
                    CommonRes(
                        "Todo Deleted with id $todoId!",
                        CommonMeta("Success")
                    )
                )
            } else {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    CommonRes(
                        "null",
                        CommonMeta("Something went wrong!")
                    )
                )
            }
        } ?: kotlin.run {
            call.respond(
                HttpStatusCode.Forbidden,
                CommonRes(
                    "null",
                    CommonMeta(
                        "Please provide valid userId!"
                    )
                )
            )
            return@delete
        }
    }
}