package interfaces

import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder

interface SlashCommand {
    val name: String
    val description: String

    fun setup(builder: GlobalChatInputCreateBuilder) {}

    suspend fun run(event: ChatInputCommandInteractionCreateEvent)
}