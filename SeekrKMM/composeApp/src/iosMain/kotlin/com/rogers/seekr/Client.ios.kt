package com.rogers.seekr

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.KotlinxSerializationConverter
import kotlinx.serialization.json.Json

actual fun client(): HttpClient = HttpClient(Darwin) {
    install(ContentNegotiation) {
        register(
            ContentType.Application.Json, KotlinxSerializationConverter(
                Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                }
            )
        )
    }
}