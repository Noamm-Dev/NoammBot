package interfaces.ai

data class ChatRequest(val model: String, val messages: List<ChatMessage>, val temperature: Double)