package commands

import NoammBot
import annotations.Command
import interfaces.DiscordCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import okhttp3.Request

@Command
object RtcaCommand: DiscordCommand() {
    private val nameRegex = "^[a-zA-Z0-9_]{3,16}$".toRegex()

    override val data = Commands.slash("rtca", "Calculates the needed runs for each class until you hit class average 50")
        .addOptions(OptionData(OptionType.STRING, "name", "Your in-game name", true).setMinLength(3).setMaxLength(16))

    override fun run(event: SlashCommandInteractionEvent) {
        val name = event.getOption("name")?.asString ?: return event.reply("You forgot to input a name meow!").setEphemeral(true).queue()
        if (! nameRegex.matches(name)) return event.reply("Invalid username format meow!").setEphemeral(true).queue()

        event.deferReply().queue()

        Thread {
            val request = Request.Builder().url("https://api.noamm.org/hypixel/rtca/$name").build()
            val msg = try {
                NoammBot.httpClient.newCall(request).execute().use { response ->
                    if (! response.isSuccessful) "Failed to fetch data! (API returned HTTP ${response.code})"
                    else {
                        val raw = response.body?.string() ?: throw Exception("Empty response body")
                        val data = NoammBot.gson.fromJson(raw, RtcaData::class.java)
                        "${data.name} is ${data.runs} M7 runs away from ca50 (${formatClassRuns(data.classes)})"
                    }
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
                "An error occurred meow! (${e.message})"
            }

            event.hook.editOriginal(msg).queue()
        }.start()
    }

    private fun formatClassRuns(runs: Map<String, Int>): String {
        return runs.filterValues { it > 0 }.entries.joinToString(" | ") { (className, runsNeeded) ->
            "${className.take(4).replaceFirstChar { it.uppercase() }} $runsNeeded"
        }
    }

    private data class RtcaData(val name: String, val runs: Int, val classes: Map<String, Int>)
}