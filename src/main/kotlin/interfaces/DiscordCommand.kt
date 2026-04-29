package interfaces

import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

abstract class DiscordCommand: EventListener {
    abstract val data: SlashCommandData

    final override fun onEvent(event: GenericEvent) {
        when (event) {
            is SlashCommandInteractionEvent -> if (event.name == data.name) run(event)
            is CommandAutoCompleteInteractionEvent -> onAutoComplate(event)
        }
    }

    abstract fun run(event: SlashCommandInteractionEvent)

    open fun onAutoComplate(event: CommandAutoCompleteInteractionEvent) {}
}