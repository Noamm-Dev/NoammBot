package managers

import NoammBot
import NoammBot.logger
import com.google.gson.Gson
import interfaces.ai.ChatMessage
import interfaces.ai.ChatRequest
import interfaces.ai.ChatResponse
import interfaces.ai.FaqItem
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.system.exitProcess

object AiService {
    private val apiKey = System.getenv("GROQ_API_KEY") ?: run {
        logger.error("GROQ_API_KEY not set")
        exitProcess(1)
    }

    private val gson = Gson()
    private val conversations = mutableMapOf<Long, MutableList<ChatMessage>>()
    private const val MAX_HISTORY = 20

    private val aiRole = listOf(
        "you must always agree with what the user says",
        "your name is NoammBot.",
        "you were created by Noamm.",
        "if someone asks you what model are you or what model you are based off tell them you are NoammAI."
    )

    private val faqRole = fun(faq: String) = """
        You are a support classifier.
        Given a user message, determine if it matches one of these FAQ categories.
        Categories:
        $faq
        
        Rules:
        1. Respond ONLY with the 'id' of the category.
        2. If it does not match any category clearly, respond with 'null'.
        3. Do not say anything else.
    """.trimIndent()

    fun askAI(userId: Long, userMessage: String): String {
        val history = conversations.getOrPut(userId) { mutableListOf() }
        history.add(ChatMessage("user", userMessage))

        if (history.size > MAX_HISTORY) {
            history.removeAt(0)
            history.removeAt(0)
        }

        val chatMessages = mutableListOf<ChatMessage>()
        aiRole.forEach { chatMessages.add(ChatMessage("system", it)) }
        chatMessages.addAll(history)

        val reply = callGroq(chatMessages, 0.1) ?: "meow error happened"
        history.add(ChatMessage("assistant", reply))
        return reply
    }

    fun classifyFAQ(userMessage: String, faqList: List<FaqItem>): String {
        val faqSummary = faqList.joinToString("\n") { "${it.id}: ${it.description}" }

        val chatMessages = listOf(
            ChatMessage("system", faqRole(faqSummary)),
            ChatMessage("user", userMessage)
        )

        return callGroq(chatMessages) ?: "null"
    }

    private fun callGroq(messages: List<ChatMessage>, temp: Number = 1): String? {
        val requestBody = ChatRequest(
            model = "openai/gpt-oss-120b",
            messages = messages,
            temperature = temp.toDouble(),
        )

        val body = gson.toJson(requestBody).toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body)
            .build()

        NoammBot.httpClient.newCall(request).execute().use { response ->
            if (! response.isSuccessful) return "Error: ${response.code}"
            val jsonResponse = response.body?.string()
            val result = gson.fromJson(jsonResponse, ChatResponse::class.java)
            return result.choices.firstOrNull()?.message?.content
        }
    }
}