package commands.forum

import Config
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.asChannelOfOrNull
import dev.kord.core.behavior.channel.threads.edit
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.channel.ForumChannel
import dev.kord.core.entity.channel.thread.ThreadChannel
import dev.kord.core.event.Event
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kord.rest.builder.component.option
import dev.kord.rest.builder.message.actionRow
import interfaces.KordListener
import interfaces.SlashCommand

object TagForumCommand: SlashCommand, KordListener {
    override val name = "tagforum"
    override val description = "Add tags to forum via dropdown"

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        if (event.interaction.user.id != Config.Noamm) {
            event.interaction.deferEphemeralResponse().respond { content = "You are not Sexy Enough." }
            return
        }

        val thread = event.interaction.channel.asChannelOfOrNull<ThreadChannel>() ?: run {
            event.interaction.deferEphemeralResponse().respond { content = "Not a thread." }
            return
        }

        val parent = thread.parent.asChannelOfOrNull<ForumChannel>() ?: run {
            event.interaction.deferEphemeralResponse().respond { content = "Thread has no forum parent." }
            return
        }

        val tags = parent.availableTags.ifEmpty {
            event.interaction.deferEphemeralResponse().respond { content = "No tags available in forum." }
            return
        }

        event.interaction.deferEphemeralResponse().respond {
            content = "Select tags to add:"
            actionRow {
                stringSelect("select_tags_${thread.id}") {
                    placeholder = "Choose tags..."
                    allowedValues = 0 .. tags.size.coerceAtMost(25)

                    tags.take(25).forEach { tag ->
                        option(tag.name, tag.id.toString()) {
                            default = tag.id in thread.appliedTags.toSet()
                            emoji = DiscordPartialEmoji(tag.emojiId, tag.emojiName)
                        }
                    }
                }
            }
        }
    }

    override suspend fun onEvent(event: Event) {
        if (event !is SelectMenuInteractionCreateEvent) return
        val customId = event.interaction.componentId
        if (! customId.startsWith("select_tags_")) return
        if (event.interaction.user.id != Config.Noamm) return

        val threadId = Snowflake(customId.removePrefix("select_tags_"))
        val thread = event.kord.getChannelOf<ThreadChannel>(threadId) ?: run {
            event.interaction.deferEphemeralResponse().respond { content = "Thread no longer exists." }
            return
        }

        val selectedTagIds = event.interaction.values.map(::Snowflake)

        thread.edit {
            appliedTags = selectedTagIds.toMutableList()
        }

        event.interaction.deferPublicResponse().respond {
            content = "Updated ${selectedTagIds.size} tag(s)."
        }
    }
}