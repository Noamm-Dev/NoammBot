import managers.CommandManager
import managers.EventBus
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import okhttp3.OkHttpClient
import kotlin.system.exitProcess

object NoammBot {
    val httpClient = OkHttpClient()

    lateinit var client: JDA
        private set

    @JvmStatic
    fun main(args: Array<String>) {
        val token = System.getenv("DISCORD_BOT_TOKEN") ?: run {
            println("DISCORD_BOT_TOKEN not set")
            exitProcess(1)
        }

        client = JDABuilder.createDefault(token).enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT).build()

        EventBus.registerOnce<ReadyEvent> { println("Logged in as ${client.selfUser.asTag}") }
        CommandManager.registerCommands(client)
        EventBus.register(client, "features")
    }
}