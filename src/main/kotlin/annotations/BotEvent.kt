package annotations

/**
 * Marks a class or object as a bot event listener to be automatically registered.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class BotEvent

