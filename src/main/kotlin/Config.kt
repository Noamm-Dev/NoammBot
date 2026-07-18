import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.datetime.Clock

object Config {
    val isLocal = System.getProperty("os.name").lowercase().contains("win")
    val allowedServers = listOf(1281979747605418024, 974393871058477116)
    val Noamm = Snowflake(601764042704683040)
    const val NOAMM_API = "http://localhost:6767"

    val EMBED_COLOR = Color(0x008CFFFF)
    val ERROR_COLOR = Color(0xFF4C4C)

    fun errorEmbed(title: String, description: String) = EmbedBuilder().apply {
        this.title = "✗  $title"
        this.description = description
        this.timestamp = Clock.System.now()
        this.color = ERROR_COLOR
    }

    fun newEmbed() = EmbedBuilder().apply { color = EMBED_COLOR }
}