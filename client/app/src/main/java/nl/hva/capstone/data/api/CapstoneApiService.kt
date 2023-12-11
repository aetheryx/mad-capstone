package nl.hva.capstone.data.api

import nl.hva.capstone.data.api.model.AuthCredentials
import nl.hva.capstone.data.api.model.AuthResponse
import nl.hva.capstone.data.api.model.User
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.GET

interface CapstoneApiService {
  @POST("/auth/signup")
  suspend fun signUp(
    @Body credentials: AuthCredentials
  ): AuthResponse

  @POST("/auth/login")
  suspend fun logIn(
    @Body credentials: AuthCredentials
  ): AuthResponse

  @GET("/users/@me")
  suspend fun getMe(): User
}
