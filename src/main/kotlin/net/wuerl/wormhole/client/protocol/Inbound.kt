package net.wuerl.wormhole.client.protocol

import com.beust.klaxon.PathMatcher
import ru.nsk.kstatemachine.DataEvent
import ru.nsk.kstatemachine.Event
import java.util.regex.Pattern

interface Message;

class WelcomeEvent(override val data: Welcome) : DataEvent<Welcome>


data class Welcome(
    val welcome: WelcomeOptions,
    val type: String,
    val serverTx: Float,
) : Message

data class WelcomeOptions(val test: String = "")

class AckEvent(override val data: Ack) : DataEvent<Ack>

data class Ack(
    val id: String?,
    val type: String,
    val serverTx: Float,
) : Message

class AllocatedEvent(override val data: Allocated) : DataEvent<Allocated>

data class Allocated(
    val id: String? = null,
    val nameplate: String,
    val serverTx: Float,
)

class ClaimedEvent(override val data: Claimed) : DataEvent<Claimed>

data class Claimed(
    val mailbox: String,
    val serverTx: Float,
)

fun parseType(text: String): String? {
    var type: String? = null

    val pathMatcher = object : PathMatcher {
        override fun pathMatches(path: String) = Pattern.matches("\\$\\.type", path)

        override fun onMatch(path: String, value: Any) {
            if (value is String) {
                type = value
            }
        }
    }

    klaxon.pathMatcher(pathMatcher).parse<String>(text)

    return type
}

class EventMapper {

    fun mapEvent(messageBody: String): Event? {

        val type = parseType(messageBody) ?: return null;

        //println("parsing message: $messageBody")
        return when (type) {
            "welcome" -> WelcomeEvent(klaxon.parse<Welcome>(messageBody)!!)
            "ack" -> AckEvent(klaxon.parse<Ack>(messageBody)!!)
            "allocated" -> AllocatedEvent(klaxon.parse<Allocated>(messageBody)!!)
            "claimed" -> ClaimedEvent(klaxon.parse<Claimed>(messageBody)!!)
            else -> null
        }
    }
}