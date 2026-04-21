package com.example.nexora1.data.remote.response

import com.google.gson.annotations.SerializedName

data class ActivitiesResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: List<ActivityData>
)

data class CreateActivityResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: ActivityData
)

data class ActivityData(
    @SerializedName("id")
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("categories")
    val categories: String,
    @SerializedName("mood_rating")
    val moodRating: String? = null,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
)