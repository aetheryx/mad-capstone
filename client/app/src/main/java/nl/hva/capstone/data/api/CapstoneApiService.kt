package nl.hva.capstone.data.api

import nl.hva.capstone.data.api.model.AuthResponse
import nl.hva.capstone.data.api.model.LoginInput
import nl.hva.capstone.data.api.model.SignupInput
import nl.hva.capstone.data.api.model.User
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.GET

interface CapstoneApiService {
  @POST("/auth/signup")
  suspend fun signUp(
    @Body input: SignupInput
  ): AuthResponse

  @POST("/auth/login")
  suspend fun logIn(
    @Body credentials: LoginInput
  ): AuthResponse

  @GET("/users/@me")
  suspend fun getMe(): User
}
