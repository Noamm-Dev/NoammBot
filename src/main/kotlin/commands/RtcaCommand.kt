package commands

import Config
import NoammBot
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.string
import interfaces.SlashCommand
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

object RtcaCommand: SlashCommand {
    private val nameRegex = "^[a-zA-Z0-9_]{3,16}$".toRegex()
    private val classEmojis = mapOf(
        "healer" to "❤️‍🩹",
        "mage" to "✨",
        "berserk" to "⚔️",
        "archer" to "🏹",
        "tank" to "🛡️"
    )

    override val name = "rtca"
    override val description = "Shows how many M7 runs each class still needs to reach class average 50"

    override fun setup(builder: GlobalChatInputCreateBuilder) {
        builder.string("name", "Your in-game name") {
            required = true
            minLength = 3
            maxLength = 16
        }
    }

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val playerName = event.interaction.command.strings["name"] ?: return
        val response = event.interaction.deferPublicResponse()

        if (! nameRegex.matches(playerName)) response.respond {
            embeds = mutableListOf(Config.errorEmbed("Invalid username", "`$playerName` is not a valid Minecraft username."))
        }.also { return }

        try {
            val request = NoammBot.httpClient.get("${Config.NOAMM_API}/hypixel/rtca/$playerName")
            if (! request.status.isSuccess()) response.respond { embeds = mutableListOf(Config.errorEmbed("API error  •  HTTP ${request.status.value}", "Could not fetch data for **$playerName**.")) }
            else {
                val data = request.body<RtcaData>()
                val timestamp = request.headers["x-timestamp"]?.toLong() ?: System.currentTimeMillis()

                response.respond {
                    embeds = mutableListOf(Config.newEmbed().apply {
                        this.timestamp = Instant.fromEpochMilliseconds(timestamp)

                        val isCA50 = data.runs == 0 || data.classes.values.sum() == 0

                        description = buildString {
                            if (isCA50) appendLine("**${data.name}** is already at CA 50 — congratulations 🎉🎉🎉")
                            else appendLine("**${data.name}** needs **${data.runs}** more M7 run${if (data.runs != 1) "s" else ""} to reach **CA 50**.")
                        }

                        if (! isCA50) {
                            val missing = data.classes.filterValues { it > 0 }

                            missing.forEach { (className, runsNeeded) ->
                                val emoji = classEmojis[className.lowercase()] ?: "▸"
                                field {
                                    name = "$emoji  ${className.replaceFirstChar { it.uppercase() }}"
                                    value = "**$runsNeeded** run${if (runsNeeded != 1) "s" else ""}"
                                    inline = true
                                }
                            }
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

    @Serializable private data class RtcaData(val name: String, val runs: Int, val classes: Map<String, Int>)
}