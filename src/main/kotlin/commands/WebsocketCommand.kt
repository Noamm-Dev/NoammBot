package commands

import Config
import NoammBot
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import interfaces.SlashCommand
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

object WebsocketCommand: SlashCommand {
    override val name = "websocket"
    override val description = "Shows info about the mod's websocket"

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val response = event.interaction.deferPublicResponse()

        try {
            val res = NoammBot.httpClient.get("${Config.NOAMM_API}/websocket")
            if (! res.status.isSuccess()) response.respond { embeds = mutableListOf(Config.errorEmbed("API error  •  HTTP ${res.status.value}", "Could not fetch websocket info.")) }
            else {
                val data = res.body<WebsocketResponse>()
                response.respond {
                    embeds = mutableListOf(Config.newEmbed().apply {
                        title = "Websocket Status"
                        timestamp = Clock.System.now()
                        description = buildString {
                            appendLine("**Connected Users:** ${data.connected_users}")
                            appendLine("**Active Dungeons:** ${data.rooms}")
                            append("**Users in Dungeon:** ${data.users_in_room}")
                        }
                    })
                }
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
            response.respond { embeds = mutableListOf(Config.errorEmbed("Unexpected error", e.message ?: "Something went wrong.")) }
        }
    }

    @Serializable private data class WebsocketResponse(val connected_users: Int, val rooms: Int, val users_in_room: Int)
}