package com.myprt.app.data.model

import com.google.gson.annotations.SerializedName

data class Response<D>(
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String,

    @SerializedName("loginResult")
    val loginResult: D? = null,

    @SerializedName("listStory")
    val listStory: List<D>? = null
)
