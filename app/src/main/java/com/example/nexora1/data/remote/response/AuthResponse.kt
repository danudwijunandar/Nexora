package com.example.nexora1.data.remote.response

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @SerializedName("error")
    val error: Boolean,
    @SerializedName("message")
    val message: String
)
data class LoginResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("token")
    val token: String?,
    @SerializedName("user")
    val user: UserData?
)
data class UserData(
    @SerializedName("id")
    val id: Int,
    @SerializedName("username")
    val username: String
)