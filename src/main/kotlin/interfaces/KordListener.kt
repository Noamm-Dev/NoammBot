package interfaces

import dev.kord.core.event.Event

interface KordListener {
    suspend fun onEvent(event: Event)
}