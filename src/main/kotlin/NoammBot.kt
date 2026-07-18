import com.google.gson.GsonBuilder
import dev.kord.core.Kord
import dev.kord.core.event.Event
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import interfaces.KordListener
import interfaces.SlashCommand
import io.github.classgraph.ClassGraph
import io.github.classgraph.ScanResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.gson.GsonConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

@OptIn(PrivilegedIntent::class)
object NoammBot {
    private val token = System.getenv("DISCORD_BOT_TOKEN") ?: error("DISCORD_BOT_TOKEN not set")

    val logger = LoggerFactory.getLogger(this::class.java)
    val gson = GsonBuilder().setPrettyPrinting().create()
    val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            register(ContentType.Application.Json, GsonConverter(gson))
        }
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val client = Kord(token) { defaultDispatcher = Dispatchers.IO }

        client.on<ReadyEvent> {
            logger.info("Logged in as ${client.getSelf().username}")
            ClassGraph().enableClassInfo().acceptPackages("commands", "features").scan().use { result ->
                client.registerListeners(result)
                client.registerCommands(result)
            }

            kord.guilds.collect {
                logger.info("Loaded guild ${it.name}")
                if (it.id.value.toLong() in Config.allowedServers) return@collect
                logger.info("Leaving guild ${it.name} (${it.id})")
                it.leave()
            }
        }

        client.on<GuildCreateEvent> {
            if (guild.id.value.toLong() !in Config.allowedServers) guild.leave()
        }

        client.login {
            intents += Intent.GuildMessages
            intents += Intent.MessageContent
        }
    }

    private fun Kord.registerListeners(result: ScanResult) {
        result.getClassesImplementing(KordListener::class.java).forEach { ci ->
            val listener = ci.loadClass().getDeclaredField("INSTANCE").get(null) as KordListener
            logger.info("Successfully registered listener: ${listener::class.simpleName}")
            on<Event> { listener.onEvent(this) }
        }
    }

    private suspend fun Kord.registerCommands(result: ScanResult) {
        val commands = mutableListOf<SlashCommand>()

        result.getClassesImplementing(SlashCommand::class.java).forEach { ci ->
            val command = ci.loadClass().getDeclaredField("INSTANCE").get(null) as SlashCommand
            commands.add(command)
        }

        createGlobalApplicationCommands {
            commands.forEach { cmd ->
                input(cmd.name, cmd.description) {
                    cmd.setup(this)
                }
            }
        }

        on<ChatInputCommandInteractionCreateEvent> {
            val cmdName = interaction.command.rootName
            commands.find { it.name == cmdName }?.run(this)
        }

        logger.info("Successfully loaded ${commands.size} application (/) commands.")
    }
}