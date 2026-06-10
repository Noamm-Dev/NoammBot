package commands

import Config
import NoammBot
import annotations.Command
import interfaces.DiscordCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import okhttp3.Request
import java.time.Instant

@Command
object RtcaCommand: DiscordCommand() {
    private val nameRegex = "^[a-zA-Z0-9_]{3,16}$".toRegex()
    private val classEmojis = mapOf(
        "healer" to "❤️‍🩹",
        "mage" to "✨",
        "berserk" to "⚔️",
        "archer" to "🏹",
        "tank" to "🛡️"
    )

    override val data = Commands.slash("rtca", "Shows how many M7 runs each class still needs to reach class average 50").addOptions(
        OptionData(OptionType.STRING, "name", "Your in-game name", true).setMinLength(3).setMaxLength(16)
    )

    override fun run(event: SlashCommandInteractionEvent) {
        val name = event.getOption("name")?.asString ?: return event.replyEmbeds(Config.errorEmbed("Missing argument", "You forgot to provide a username.").build()).setEphemeral(true).queue()
        if (! nameRegex.matches(name)) return event.replyEmbeds(Config.errorEmbed("Invalid username", "`$name` is not a valid Minecraft username.").build()).setEphemeral(true).queue()

        event.deferReply().queue()

        Thread {
            val request = Request.Builder().url("${Config.NOAMM_API}/hypixel/rtca/$name").build()

            val embed = try {
                NoammBot.httpClient.newCall(request).execute().use { response ->
                    if (! response.isSuccessful) return@use Config.errorEmbed("API error  •  HTTP ${response.code}", "Could not fetch data for **$name**.")
                    val raw = response.body?.string() ?: throw Exception("Empty response body")
                    val data = NoammBot.gson.fromJson(raw, RtcaData::class.java)
                    val timestamp = response.headers["x-timestamp"]?.toLong() ?: System.currentTimeMillis()

                    Config.newEmbed().apply {
                        setTimestamp(Instant.ofEpochMilli(timestamp))

                        val isCA50 = data.runs == 0 || data.classes.values.sum() == 0

                        setDescription(buildString {
                            if (isCA50) appendLine("**${data.name}** is already at CA 50 — congratulations 🎉🎉🎉")
                            else appendLine("**${data.name}** needs **${data.runs}** more M7 run${if (data.runs != 1) "s" else ""} to reach **CA 50**.")
                        })

                        if (isCA50) return@apply
                        val missing = data.classes.filterValues { it > 0 }

                        missing.forEach { (className, runsNeeded) ->
                            val emoji = classEmojis[className.lowercase()] ?: "▸"
                            addField(
                                "$emoji  ${className.replaceFirstChar { it.uppercase() }}",
                                "**$runsNeeded** run${if (runsNeeded != 1) "s" else ""}",
                                true
                            )
                        }

                        val remainder = missing.size % 3
                        if (remainder != 0) repeat(3 - remainder) {
                            addBlankField(true)
                        }
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

    private data class RtcaData(val name: String, val runs: Int, val classes: Map<String, Int>)
}