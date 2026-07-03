package commands

import Config
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.GlobalChatInputCreateBuilder
import dev.kord.rest.builder.interaction.integer
import interfaces.SlashCommand
import kotlin.math.round

object CalculateDelayCommand: SlashCommand {
    override val name = "calcdelay"
    override val description = "Calculates the recommended delay for AutoTerms"

    override fun setup(builder: GlobalChatInputCreateBuilder) {
        builder.integer("ping", "Your ingame ping") {
            required = true
            minValue = 0
            maxValue = 2000
        }
    }

    override suspend fun run(event: ChatInputCommandInteractionCreateEvent) {
        val ping = event.interaction.command.integers["ping"]?.toInt() ?: 0
        val medianPing = 150 - ping

        val desc: String
        val minDelay: Int
        val maxDelay: Int

        if (medianPing <= 0) {
            desc = "Your ping is so high no delay is needed."
            minDelay = 0
            maxDelay = 0
        }
        else {
            desc = "Recommended delay for $ping ping."
            minDelay = (round(0.8 * medianPing / 10.0) * 10).toInt()
            maxDelay = (round(1.2 * medianPing / 10.0) * 10).toInt()
        }

        event.interaction.deferPublicResponse().respond {
            embeds = mutableListOf(Config.newEmbed().apply {
                title = "AutoTerms Delay Calculator"
                description = desc
                field { name = "Min Delay"; value = "`${minDelay}ms`"; inline = true }
                field { name = "Max Delay"; value = "`${maxDelay}ms`"; inline = true }
                footer { text = "For more info about the calculation see #faq" }
            })
        }
    }
}