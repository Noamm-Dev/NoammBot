package services

import NoammBot
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.concurrent.*

object AiService {
    private val aiRole = this::class.java.getResource("/NoammAI.md")?.readText() ?: error("resources/NoammAI.md is not found!")
    private val apiKey = System.getenv("GROQ_API_KEY") ?: error("GROQ_API_KEY not set")
    private val conversations = ConcurrentHashMap<Long, Conversation>()
    private const val MAX_HISTORY = 20

    init {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(TimeUnit.MINUTES.toMillis(5))
                conversations.entries.removeIf {
                    System.currentTimeMillis() - it.value.lastActive > TimeUnit.MINUTES.toMillis(30)
                }
            }
        }
    }

    suspend fun callAI(userId: Long, userMessage: String): String {
        val convo = conversations.getOrPut(userId) { Conversation(mutableListOf(), System.currentTimeMillis()) }
        convo.lastActive = System.currentTimeMillis()
        convo.messages.add(ChatMessage("user", userMessage))

        if (convo.messages.size > MAX_HISTORY) repeat(2) {
            convo.messages.removeAt(0)
        }

        val chatMessages = mutableListOf<ChatMessage>()
        chatMessages.add(ChatMessage("system", aiRole))
        chatMessages.addAll(convo.messages)

        val reply = callGroq(chatMessages) ?: "meow error happened"
        convo.messages.add(ChatMessage("assistant", reply))
        return reply
    }

    private suspend fun callGroq(messages: List<ChatMessage>): String? {
        val body = NoammBot.gson.toJson(ChatRequest("openai/gpt-oss-120b", messages, 0.1))

        return try {
            val response = NoammBot.httpClient.post("https://api.groq.com/openai/v1/chat/completions") {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(body)
            }

            if (response.status != HttpStatusCode.OK) return "Error: ${response.status.value}"

            response.body<ChatResponse>().choices.firstOrNull()?.message?.content
        }
        catch (e: Exception) {
            "Error: ${e.message}"
        }
    }


    private data class Conversation(val messages: MutableList<ChatMessage>, var lastActive: Long)
    @Serializable private data class ChatRequest(val model: String, val messages: List<ChatMessage>, val temperature: Double)
    @Serializable private data class ChatMessage(val role: String, val content: String)
    @Serializable private data class ChatResponse(val choices: List<Choice>)
    @Serializable private data class Choice(val message: ChatMessage)
}