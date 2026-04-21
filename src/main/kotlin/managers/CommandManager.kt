package managers

import annotations.Command
import interfaces.DiscordCommand
import net.dv8tion.jda.api.JDA
import org.reflections.Reflections
import utils.ReflectionUtils

object CommandManager {
    private val commands = mutableListOf<DiscordCommand>()

    fun registerCommands(client: JDA) {
        Reflections("commands").getTypesAnnotatedWith(Command::class.java).forEach { clazz ->
            try {
                val instance = ReflectionUtils.getInstance(clazz) as DiscordCommand
                client.addEventListener(instance)
                commands.add(instance)
            }
            catch (e: Exception) {
                println("Failed to load command: ${clazz.simpleName}")
                e.printStackTrace()
            }
        }

        client.updateCommands().addCommands(commands.map(DiscordCommand::data)).queue {
            println("Successfully loaded ${it.size} application (/) commands.")
        }
    }
}