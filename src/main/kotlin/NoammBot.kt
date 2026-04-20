import managers.CommandManager
import managers.EventBus
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

object NoammBot {
    val logger = LoggerFactory.getLogger(this::class.simpleName)
    val httpClient = OkHttpClient()

    lateinit var client: JDA
        private set

    @JvmStatic
    fun main(args: Array<String>) {
        val token = System.getenv("DISCORD_BOT_TOKEN") ?: run {
            logger.error("DISCORD_BOT_TOKEN not set")
            exitProcess(1)
        }

        client = JDABuilder.createDefault(token).enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT).build()

        EventBus.registerOnce<ReadyEvent> { logger.info("Logged in as ${client.selfUser.asTag}") }
        CommandManager.registerCommands(client)
    }
}