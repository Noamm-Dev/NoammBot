import net.dv8tion.jda.api.EmbedBuilder
import java.awt.Color
import java.time.Instant

object Config {
    val isLocal = System.getProperty("os.name").lowercase().contains("win")
    const val NOAMM_API = "http://localhost:6767"

    val EMBED_COLOR = Color(0x008CFFFF)
    val ERROR_COLOR = Color(0xFF4C4C)

    fun errorEmbed(title: String, description: String) = EmbedBuilder().apply {
        setTitle("✗  $title").setDescription(description).setTimestamp(Instant.now())
        setColor(ERROR_COLOR)
    }

    fun newEmbed() = EmbedBuilder().setColor(EMBED_COLOR)
}