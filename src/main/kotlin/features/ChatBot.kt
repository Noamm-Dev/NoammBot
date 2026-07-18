package features

import Config
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.behavior.reply
import dev.kord.core.entity.Member
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.core.event.Event
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string
import interfaces.KordListener
import interfaces.SlashCommand
import kotlinx.coroutines.flow.firstOrNull
import services.AiService
import java.util.concurrent.atomic.*

object ChatBot: KordListener, SlashCommand {
    private const val BOT_COMMANDS = 1450943955624792300L
    private const val BOT_CD = 2000L
    private val allowedRoles = listOf("Donor", "my son")
    private val lastSent = AtomicLong(0L)

    override val name = "ask"
    override val description = "Share your dreams with NoammAi"

    override fun setup(builder: GlobalChatInputCreateBuilder) {
        builder.string("prompt", "what do you want to ask") {
            required = true
        }
    }

    override suspend fun onEvent(event: Event) {
        if (event !is MessageCreateEvent) return

        val author = event.message.author?.takeUnless { it.isBot } ?: return
        val selfId = event.kord.selfId

        if (selfId !in event.message.mentionedUserIds) return
        val prompt = event.message.content.replace("<@$selfId>", "").replace("<@!$selfId>", "").trim()

        handleAsk(
            author = author,
            member = event.member,
            channelId = event.message.channelId,
            prompt = prompt,
            authorIdForAI = author.id.value.toLong(),
            onTyping = { event.message.channel.type() },
            onReply = { text -> event.message.reply { content = text } }
        )
    }

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val response = event.interaction.deferPublicResponse()
        val author = event.interaction.user
        val member = (event.interaction as? GuildChatInputCommandInteraction)?.user
        val prompt = event.interaction.command.strings["prompt"]?.trim().orEmpty()

        handleAsk(
            author = author,
            member = member,
            channelId = event.interaction.channelId,
            prompt = prompt,
            authorIdForAI = author.id.value.toLong(),
            onReply = { text -> response.respond { content = text } }
        )
    }


    private suspend fun handleAsk(
        author: User?,
        member: Member?,
        channelId: Snowflake,
        prompt: String,
        authorIdForAI: Long,
        onTyping: suspend () -> Unit = {},
        onReply: suspend (String) -> Unit,
    ) {
        if (author?.isBot == true) return
        if (prompt.isBlank()) return onReply("Ask me something first!")

        val isOwner = Config.Noamm == author?.id
        if (channelId.value.toLong() != BOT_COMMANDS && ! isOwner) return

        val hasAllowedRole = member?.roles?.firstOrNull { it.name in allowedRoles } != null
        if (! isOwner && ! hasAllowedRole) {
            onReply("Sorry, Only sexy people can talk to me. (Donors/Sons)")
            return
        }

        val now = System.currentTimeMillis()
        val prev = lastSent.getAndUpdate { if (now - it >= BOT_CD) now else it }
        if (now - prev < BOT_CD) return

        onTyping()

        try {
            val aiResponse = AiService.callAI(authorIdForAI, prompt)
            onReply(aiResponse.take(1800))
        }
        catch (e: Exception) {
            e.printStackTrace()
            onReply("Sorry, my brain is currently offline.")
        }
    }
}