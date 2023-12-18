package nl.hva.capstone.data.api

import nl.hva.capstone.data.api.model.AuthResponse
import nl.hva.capstone.data.api.model.ConversationMessage
import nl.hva.capstone.data.api.model.CreateConversation
import nl.hva.capstone.data.api.model.LoginInput
import nl.hva.capstone.data.api.model.SignupInput
import nl.hva.capstone.data.api.model.User
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

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

  @GET("/users/find")
  suspend fun findUser(
    @Query("username") username: String,
  ): User

  @GET("/conversations")
  suspend fun getConversations(): List<FullConversation>

  @POST("/conversations")
  suspend fun createConversation(
    @Body otherUser: CreateConversation
  ): Conversation

  @GET("/conversations/{id}/messages")
  suspend fun getConversationMessages(
    @Path("id") conversationID: Int,
    @Query("limit") limit: Int = 50,
    @Query("offset") offset: Int = 0,
  ): List<ConversationMessage>
}
