package com.example.nexora1.data.remote.response

import com.google.gson.annotations.SerializedName

data class FinanceResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: List<FinanceData>
)

data class CreateFinanceResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: FinanceData
)

data class FinanceData(
    @SerializedName("id")
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("type")
    val type: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("amount")
    val amount: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("note")
    val note: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
)