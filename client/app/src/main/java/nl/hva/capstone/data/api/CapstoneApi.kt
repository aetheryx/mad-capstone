package nl.hva.capstone.data.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object CapstoneApi {
  const val baseUrl = "http://10.0.2.2:3000"

  fun createApi(token: String): CapstoneApiService {
    val httpClient = OkHttpClient.Builder()
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

    val retrofitClient = Retrofit.Builder()
      .baseUrl(baseUrl)
      .client(httpClient)
      .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
      .build()

    return retrofitClient.create(CapstoneApiService::class.java)
  }
}
