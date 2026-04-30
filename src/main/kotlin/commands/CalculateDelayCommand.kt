package commands

import annotations.Command
import interfaces.DiscordCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import kotlin.math.round

@Command
object CalculateDelayCommand: DiscordCommand() {
    override val data = Commands.slash("calcdelay", "Calculates the recommanded delay for AutoTerms")
        .addOptions(OptionData(OptionType.INTEGER, "ping", "Your ingame ping", true).setMinValue(0).setMaxValue(2000))

    override fun run(event: SlashCommandInteractionEvent) {
        val ping = event.getOption("ping")?.asInt ?: return event.reply("You forgot to input a ping meow!").queue()
        if (ping < 0) return event.reply("Ping cannot be negative meow!").queue()

        val medianPing = 150 - ping

        if (medianPing <= 0) event.reply("min: 0ms, max: 0ms").queue()
        else {
            val min = (round(0.8 * medianPing / 10.0) * 10).toInt()
            val max = (round(1.2 * medianPing / 10.0) * 10).toInt()
            event.reply("min: ${min}ms, max: ${max}ms").queue()
        }
    }
}