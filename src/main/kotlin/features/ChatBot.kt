package features

import dev.kord.core.behavior.reply
import dev.kord.core.event.Event
import dev.kord.core.event.message.MessageCreateEvent
import interfaces.KordListener
import services.AiService

object ChatBot: KordListener {
    private const val BOT_COMMANDS = 1450943955624792300
    private const val BOT_CD = 2000L
    private val allowedRoles = listOf("Donor", "my son")
    private var lastSent = 0L

    override suspend fun onEvent(event: Event) {
        if (event !is MessageCreateEvent) return
        if (event.message.author?.isBot == true) return
        val guild = event.getGuildOrNull() ?: return

        val isOwner = guild.ownerId == event.message.author?.id
        if (event.message.channelId.value.toLong() != BOT_COMMANDS && ! isOwner) return

        val selfId = event.kord.getSelf().id
        if (! event.message.mentionedUserIds.contains(selfId)) return

        val member = event.member ?: return
        var hasAllowedRole = false
        member.roles.collect { if (it.name in allowedRoles) hasAllowedRole = true }

        if (! isOwner && ! hasAllowedRole) {
            event.message.reply { content = "Sorry, Only sexy people can talk to me. (Donors/Sons)" }
            return
        }

        val now = System.currentTimeMillis()
        if (now - lastSent < BOT_CD) return
        lastSent = now

        val cleanMessage = event.message.content.replace("<@$selfId>", "").replace("<@!$selfId>", "").trim().ifBlank { return }
        event.message.channel.type()

        try {
            val aiResponse = AiService.callAI(event.message.author !!.id.value.toLong(), cleanMessage)
            val safeResponse = if (aiResponse.length > 1800) aiResponse.substring(0, 1800) else aiResponse
            event.message.reply { content = safeResponse }
        }
        catch (e: Exception) {
            e.printStackTrace()
            event.message.reply { content = "Sorry, my brain is currently offline." }
        }
    }
}