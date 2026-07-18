package commands.forum

import Config
import dev.kord.core.behavior.channel.asChannelOfOrNull
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.channel.threads.edit
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.channel.thread.ThreadChannel
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.EmbedBuilder
import interfaces.SlashCommand
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import java.util.concurrent.*

object CloseForumCommand: SlashCommand {
    override val name = "closeforum"
    override val description = "Closes forum and notifies OP"

    override fun setup(builder: GlobalChatInputCreateBuilder) {
        builder.string("reason", "close reason") {
            required = false
        }
    }

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        if (event.interaction.user.id != Config.Noamm) {
            event.interaction.deferEphemeralResponse().respond { content = "You are not Sexy Enough." }
            return
        }

        val post = event.interaction.channel.asChannelOfOrNull<ThreadChannel>() ?: run {
            event.interaction.deferEphemeralResponse().respond { content = "Not a thread." }
            return
        }

        event.interaction.deferPublicResponse().respond { content = "Closing post..." }

        val reason = event.interaction.command.strings["reason"] ?: "No reason provided"

        val embed: EmbedBuilder.() -> Unit = {
            title = "🔒 Forum Closed"
            description = "Your post **${post.name}** has been closed."
            color = Config.EMBED_COLOR

            field {
                name = "Reason"
                value = reason
            }

            footer {
                text = "Closed by ${event.interaction.user.globalName}"
                icon = event.interaction.user.avatar?.cdnUrl?.toUrl()
            }

            timestamp = Clock.System.now()
        }

        val dmSent = runCatching {
            post.owner.getDmChannel().createEmbed(embed)
        }.onFailure {
            it.printStackTrace()
        }.isSuccess

        if (! dmSent) {
            post.createEmbed(embed)

            post.edit {
                locked = true
                archived = true
            }

            delay(TimeUnit.HOURS.toMillis(1))
        }

        post.delete(reason)
    }
}