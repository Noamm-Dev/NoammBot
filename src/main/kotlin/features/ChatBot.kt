package features

import annotations.BotEvent
import managers.AiService
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.EventListener

@BotEvent
object ChatBot: EventListener {
    private const val BOT_COMMANDS = "1450943955624792300"
    private const val BOT_CD = 2000L
    private val allowedRoles = listOf("Donor", "my son")
    private var lastSent = 0L

    override fun onEvent(event: GenericEvent) {
        if (event !is MessageReceivedEvent) return
        if (event.author.isBot) return
        if (! event.isFromGuild) return // Ignore DMs

        event.member
        val channel = event.channel

        val isOwner = event.guild.ownerId == event.author.id
        if (channel.id != BOT_COMMANDS && ! isOwner) return
        if (! event.message.mentions.isMentioned(event.jda.selfUser)) return

        val hasAllowedRole = event.member?.roles?.any { it.name in allowedRoles } ?: return
        if (! isOwner && ! hasAllowedRole) return event.message.reply("Sorry, Only sexy people can talk to me.").queue()

        val now = System.currentTimeMillis()
        if (now - lastSent < BOT_CD) return
        lastSent = now

        val selfId = event.jda.selfUser.id
        val cleanMessage = event.message.contentRaw.replace("<@$selfId>", "").replace("<@!$selfId>", "").trim()

        if (cleanMessage.isBlank()) return

        channel.sendTyping().queue()

        Thread {
            try {
                val aiResponse = AiService.askAI(event.author.idLong, cleanMessage)
                val safeResponse = if (aiResponse.length > 1800) aiResponse.substring(0, 1800) else aiResponse
                event.message.reply(safeResponse).setAllowedMentions(emptyList()).setSuppressedNotifications(true).queue()
            }
            catch (e: Exception) {
                e.printStackTrace()
                event.message.reply("Sorry, my brain is currently offline.").queue()
            }
        }.start()
    }
}