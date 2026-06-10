package commands

import Config
import NoammBot
import annotations.Command
import interfaces.DiscordCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import okhttp3.Request
import java.time.Instant

@Command
object WebsocketCommand: DiscordCommand() {
    override val data = Commands.slash("websocket", "Shows info about the mod's websocket")
    private val request = Request.Builder().url("${Config.NOAMM_API}/websocket").build()

    override fun run(event: SlashCommandInteractionEvent) {
        event.deferReply().queue()

        Thread {
            val embed = try {
                NoammBot.httpClient.newCall(request).execute().use { response ->
                    if (! response.isSuccessful)
                        return@use Config.errorEmbed("API error  •  HTTP ${response.code}", "Could not fetch websocket info.")

                    val raw = response.body?.string() ?: throw Exception("Empty response body")
                    val data = NoammBot.gson.fromJson(raw, WebsocketResponse::class.java)

                    Config.newEmbed().apply {
                        setTitle("Websocket Status")
                        setTimestamp(Instant.now())
                        setDescription(buildString {
                            appendLine("**Connected Users:** ${data.connected_users}")
                            appendLine("**Active Dungeons:** ${data.rooms}")
                            append("**Users in Dungeon:** ${data.users_in_room}")
                        })
                    }
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
                Config.errorEmbed("Unexpected error", e.message ?: "Something went wrong.")
            }

            event.hook.editOriginalEmbeds(embed.build()).queue()
        }.start()
    }

    data class WebsocketResponse(val connected_users: Int, val rooms: Int, val users_in_room: Int)
}