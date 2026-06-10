package commands

import Config
import annotations.Command
import interfaces.DiscordCommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import kotlin.math.round

@Command
object CalculateDelayCommand: DiscordCommand() {
    override val data = Commands.slash("calcdelay", "Calculates the recommended delay for AutoTerms")
        .addOptions(OptionData(OptionType.INTEGER, "ping", "Your ingame ping", true).setMinValue(0).setMaxValue(2000))

    override fun run(event: SlashCommandInteractionEvent) {
        val ping = event.getOption("ping") !!.asInt
        val medianPing = 150 - ping

        val description: String
        val minDelay: Int
        val maxDelay: Int

        if (medianPing <= 0) {
            description = "Your ping is so high no delay is needed."
            minDelay = 0
            maxDelay = 0
        }
        else {
            description = "Recommended delay for $ping ping."
            minDelay = (round(0.8 * medianPing / 10.0) * 10).toInt()
            maxDelay = (round(1.2 * medianPing / 10.0) * 10).toInt()
        }

        val embed = EmbedBuilder().apply {
            setColor(Config.EMBED_COLOR)
            setTitle("AutoTerms Delay Calculator")
            setDescription(description)

            addField("Min Delay", "`${minDelay}ms`", true)
            addField("Max Delay", "`${maxDelay}ms`", true)

            setFooter("For more info about the calculation see #faq")
        }.build()

        event.replyEmbeds(embed).queue()
    }
}