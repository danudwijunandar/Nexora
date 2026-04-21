package com.example.nexora1.data.remote.retrofit

import com.example.nexora1.data.remote.response.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("auth/register")
    suspend fun register(
        @Field("username") username: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("konfirmasi-password") konfirmasi: String
    ): Response<RegisterResponse>

    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    @FormUrlEncoded
    @PATCH("update/user")
    suspend fun updateUser(
        @Header("Authorization") token: String,
        @Field("username") username: String,
        @Field("email") email: String
    ): Response<Void>

    @FormUrlEncoded
    @PATCH("update/password")
    suspend fun updatePassword(
        @Header("Authorization") token: String,
        @Field("passwordLama") passwordLama: String,
        @Field("passwordBaru") passwordBaru: String,
        @Field("konfirmasi_password") konfirmasi: String
    ): Response<Void>

    @GET("activities")
    suspend fun getActivities(
        @Header("Authorization") token: String
    ): Response<ActivitiesResponse>

    @FormUrlEncoded
    @POST("activities/create")
    suspend fun createActivity(
        @Header("Authorization") token: String,
        @Field("title") title: String,
        @Field("description") description: String,
        @Field("categories") categories: String,
        @Field("date") date: String
    ): Response<CreateActivityResponse>

    @FormUrlEncoded
    @PATCH("activities/update/{id}")
    suspend fun updateActivity(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Field("title") title: String,
        @Field("status") status: String
    ): Response<Void>

    @DELETE("activities/delete/{id}")
    suspend fun deleteActivity(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Void>

    @GET("finance")
    suspend fun getFinance(
        @Header("Authorization") token: String
    ): Response<FinanceResponse>

    @FormUrlEncoded
    @POST("finance/create")
    suspend fun createFinance(
        @Header("Authorization") token: String,
        @Field("type") type: String,
        @Field("category") category: String,
        @Field("amount") amount: String,
        @Field("date") date: String,
        @Field("note") note: String
    ): Response<CreateFinanceResponse>

    @FormUrlEncoded
    @PATCH("finance/update/{id}")
    suspend fun updateFinance(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Field("type") type: String,
        @Field("category") category: String,
        @Field("amount") amount: String,
        @Field("date") date: String
    ): Response<Void>

    @DELETE("finance/delete/{id}")
    suspend fun deleteFinance(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Void>
}