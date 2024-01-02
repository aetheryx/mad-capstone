package nl.hva.chatstone.api

import nl.hva.chatstone.api.model.input.*
import nl.hva.chatstone.api.model.output.*
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatstoneApiService {
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
  suspend fun getConversations(): List<Conversation>

  @POST("/conversations")
  suspend fun createConversation(
    @Body otherUser: CreateConversationInput
  ): Conversation

  @DELETE("/conversations/{id}")
  suspend fun deleteConversation(
    @Path("id") conversationID: Int
  )

  @GET("/conversations/{id}/messages")
  suspend fun getConversationMessages(
    @Path("id") conversationID: Int,
    @Query("limit") limit: Int = 50,
    @Query("offset") offset: Int = 0,
  ): List<ConversationMessage>

  @POST("/conversations/{id}/messages")
  suspend fun createMessage(
    @Path("id") conversationID: Int,
    @Body input: CreateMessageInput
  ): ConversationMessage
}
