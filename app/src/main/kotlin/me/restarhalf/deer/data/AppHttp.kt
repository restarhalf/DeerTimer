package me.restarhalf.deer.data

import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

object AppHttp {
    val client: OkHttpClient = OkHttpClient()

    val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }
}
