package net.wuerl.wormhole.client

import com.beust.klaxon.FieldRenamer
import com.beust.klaxon.Klaxon
import com.beust.klaxon.PathMatcher
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.runBlocking
import ru.nsk.kstatemachine.*
import java.util.regex.Pattern

val url = Url("ws://relay.magic-wormhole.io:4000/v1")
val url3 = Url("wss://mailbox.mw.leastauthority.com/v1")

suspend fun HttpClient.webSocket(
    targetUrl: Url,
    request: HttpRequestBuilder.() -> Unit = {},
    block: suspend DefaultClientWebSocketSession.() -> Unit
): Unit = webSocket(
    HttpMethod.Get, targetUrl.host, targetUrl.port, targetUrl.fullPath,
    {
        url.protocol = targetUrl.protocol
        url.port = targetUrl.port
        request()
    },
    block = block
)

val renamer = object : FieldRenamer {
    override fun toJson(fieldName: String) = FieldRenamer.camelToUnderscores(fieldName)
    override fun fromJson(fieldName: String) = FieldRenamer.underscoreToCamel(fieldName)
}

val klaxon = Klaxon().fieldRenamer(renamer)

object CancelEvent : Event

data class MessageReceivedEvent<T:Message>(override val data: T) : DataEvent<T>

sealed class States : DefaultState() {
    object InitState : States()
    object ExitState : States(), FinalState // Machine finishes when enters final state
}

interface Message;

data class WelcomeOptions(val test: String = "")

data class Welcome(
    val welcome: WelcomeOptions,
    val type: String,
    val serverTx: Float,
) : Message

inline fun <reified T:Message> messageReceivedEvent(messageBody: String): MessageReceivedEvent<T>? {
    val data = klaxon.parse<T>(messageBody)
    return if (data != null)
        MessageReceivedEvent(data) else null
}

fun main() {

    val machine = createStateMachine {
        addInitialState(States.InitState) {
            // Add state listeners
            onEntry { println("Enter init") }
            onExit { println("Exit init") }

            transition<CancelEvent> {
                targetState = States.ExitState
                // Add transition listener
                onTriggered { println("Cancelled") }
            }

            //dataTransition<MessageReceivedEvent, String> { targetState = this. }

        }

        ignoredEventHandler = StateMachine.IgnoredEventHandler { event, _ ->
            error("unexpected $event")
        }
    }

    val client = HttpClient {
        install(WebSockets)
    }


    fun processMessage(messageBody: String): MessageReceivedEvent<*>? {
        var type: String? = null
        val pathMatcher = object : PathMatcher {
            override fun pathMatches(path: String) = Pattern.matches("\\$\\.type", path)

            override fun onMatch(path: String, value: Any) {
                if (value is String) {
                    type = value
                }
            }
        }

        klaxon
            .pathMatcher(pathMatcher)
            .parse<String>(messageBody)

        if (type == null) {
            return null
        }
        println("parsing message: $messageBody")
        return when (type) {
            "welcome" -> messageReceivedEvent<Welcome>(messageBody)
            else -> null
        }
    }

    runBlocking {
        client.webSocket(url) {
            while (true) {
                val othersMessage = incoming.receive() as? Frame.Text
                if (othersMessage != null) {
                    val event = processMessage(othersMessage.readText())
                    if (event != null) {
                        machine.processEvent(event)
                    }
                    break
                }
//                val myMessage = readLine()
//                if(myMessage != null) {
//                    send(myMessage)
//                }
            }
        }
    }
    client.close()
    println("Connection closed. Goodbye!")
}