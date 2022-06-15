package com.bbm.todo

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar


/**
 * User Table schema for [User]
 * ```
    CREATE TABLE USER(
        id int NOT NULL AUTO_INCREMENT,
        user_name varchar(2000) NOT NULL,
        PRIMARY KEY(id)
    );
```
 * */
interface User : Entity<User> {
    companion object : Entity.Factory<User>()

    val id: Int
    val name: String
}

object UserEntity : Table<User>("USER") {

    val id = int("id").bindTo { it.id }
    val user_name = varchar("user_name").bindTo { it.name }
}

val Database.users
    get() = this.sequenceOf(UserEntity)


/**
 * User's Todo Table schema for [Todo]
 * ```
    CREATE TABLE USER_TODOS(
        id int NOT NULL AUTO_INCREMENT,
        title varchar(500) NOT NULL,
        description varchar(5000) NOT NULL,
        user_id int NOT NULL,
        PRIMARY KEY(id)
    );
```
 * */
interface Todo : Entity<Todo> {
    companion object : Entity.Factory<User>()

    val id: Int
    val title: String
    val description: String
    val userId: Int
}

object TodoEntity : Table<Todo>("USER_TODOS") {

    val id = int("id").bindTo { it.id }
    val user_id = int("user_id").bindTo { it.userId }
    val title = varchar("title").bindTo { it.title }
    val description = varchar("description").bindTo { it.description }
}

val Database.todos
    get() = this.sequenceOf(TodoEntity)