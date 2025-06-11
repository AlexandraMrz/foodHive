package com.example.foodhive.data.remote

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

// --- Request and Response Models ---
data class OpenAiRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<Message>
)

data class Message(
    val role: String,
    val content: String
)

data class OpenAiResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)

// --- API Interface ---
interface OpenAiApi {
    @Headers("Content-Type: application/json")
    @POST("chat/completions")
    suspend fun getChatCompletion(@Body request: OpenAiRequest): OpenAiResponse
}

// --- Service Singleton ---
object OpenAiService {
    private const val BASE_URL = "https://api.openai.com/v1/"

    private val apiKeyInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer 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")
            .build()
        chain.proceed(request)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(apiKeyInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: OpenAiApi = retrofit.create(OpenAiApi::class.java)
}
