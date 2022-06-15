package com.bbm.todo

import kotlinx.serialization.Serializable


@Serializable
data class CommonMeta<D>(
    val data: D?
)

@Serializable
data class CommonRes<D, M>(
    val data: D?,
    val meta: M
)

@Serializable
data class UserRes(
    val id: Int,
    val name: String
)

@Serializable
data class UserResMeta(
    val total_data: Int
)

@Serializable
data class Todos(
    val id: Int,
    val title: String,
    val description: String,
)

@Serializable
data class TodosMeta(
    val total_data: Int
)