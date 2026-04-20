package managers

import NoammBot
import annotations.BotEvent
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.hooks.EventListener
import org.reflections.Reflections
import org.slf4j.LoggerFactory
import utils.ReflectionUtils

object EventBus {
    private val logger = LoggerFactory.getLogger(this::class.java.simpleName)

    /**
     * Scans the specified package for classes annotated with @BotEvent
     * and registers them to the JDA client.
     */
    fun register(jda: JDA, packageName: String) {
        Reflections(packageName).getTypesAnnotatedWith(BotEvent::class.java).forEach { clazz ->
            try {
                val instance = ReflectionUtils.getInstance(clazz) as? EventListener
                    ?: return@forEach logger.warn("Class ${clazz.name} is annotated with @BotEvent but does not implement EventListener")

                jda.addEventListener(instance)
                logger.info("Successfully registered listener: ${clazz.simpleName}")
            }
            catch (e: Exception) {
                logger.error("Failed to register listener ${clazz.name}: ${e.message}")
            }
        }
    }

    inline fun <reified T: GenericEvent> registerOnce(noinline block: T.() -> Unit) {
        NoammBot.client.listenOnce(T::class.java).subscribe(block)
    }

    inline fun <reified T: GenericEvent> register(noinline block: T.() -> Unit) {
        NoammBot.client.addEventListener(object: EventListener {
            override fun onEvent(event: GenericEvent) {
                if (event is T) block(event)
            }
        })
    }
}
