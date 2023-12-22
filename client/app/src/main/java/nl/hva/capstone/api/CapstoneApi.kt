package nl.hva.capstone.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object CapstoneApi {
  const val BASE_URL = "http://10.0.2.2:3000"

  private fun createOkHttpClient(token: String) = OkHttpClient.Builder()
    .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
    .addInterceptor { chain ->
      val request = chain.request().newBuilder()
        .addHeader("Authorization", "Bearer $token")
        .build()

      chain.proceed(request)
    }
    .readTimeout(10, TimeUnit.SECONDS)
    .writeTimeout(10, TimeUnit.SECONDS)
    .build()

  fun createApi(token: String): CapstoneApiService {
    val httpClient = createOkHttpClient(token)

    val retrofitClient = Retrofit.Builder()
      .baseUrl(BASE_URL)
      .client(httpClient)
      .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
      .build()

    return retrofitClient.create(CapstoneApiService::class.java)
  }

  fun createWebSocket(userID: Int, listener: WebSocketListener): WebSocket {
    val client = createOkHttpClient("")
    val request = Request.Builder()
      .url("$BASE_URL/ws/connect?user_id=$userID")
      .build()

    return client.newWebSocket(request, listener)
  }
}
