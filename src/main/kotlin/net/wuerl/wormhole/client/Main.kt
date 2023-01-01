package net.wuerl.wormhole.client

import com.beust.klaxon.PathMatcher
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.runBlocking
import net.wuerl.wormhole.client.States.InitState
import net.wuerl.wormhole.client.States.StartState
import net.wuerl.wormhole.client.protocol.*
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


object CancelEvent : Event


sealed class States : DefaultState() {
    object InitState : States()

    object StartState : States()

    object ExitState : States(), FinalState // Machine finishes when enters final state
}

interface Message;


fun main() {

    val machine = createStateMachine {
        addInitialState(InitState) {
            // Add state listeners
            onEntry { println("Enter init") }
            onExit { println("Exit init") }

            transition<CancelEvent> {
                targetState = States.ExitState
                // Add transition listener
                onTriggered { println("Cancelled") }
            }
            transition<WelcomeEvent> {
                targetState = StartState
            }
        }

        addState(StartState) {
            onEntry {
                println("Enter start")
                val sendChannel = it.argument as SendChannel<Frame>
                runBlocking {
                    println("Start send 'allocate'")
                    sendChannel.send(
                        Frame.Text(klaxon.toJsonString(Bind(side = "receive")))
                    )
                    println("Start send done")
                }
            }
            onExit { println("Enter start") }
        }

        ignoredEventHandler = StateMachine.IgnoredEventHandler { event, _ ->
            error("unexpected $event")
        }
    }

    val client = HttpClient {
        install(WebSockets)
    }


    fun processMessage(messageBody: String): Event? {
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
            "welcome" -> WelcomeEvent(klaxon.parse<Welcome>(messageBody)!!)
            "ack" -> AckEvent(klaxon.parse<Ack>(messageBody)!!)
            else -> null
        }
    }

    runBlocking {
        client.webSocket(url3) {
            while (true) {
                println("receive")
                val othersMessage = incoming.receive() as? Frame.Text
                println("received ${othersMessage}")
                if (othersMessage != null) {
                    val event = processMessage(othersMessage.readText())
                    if (event != null) {
                        val processResult = machine.processEvent(event, outgoing)
                        println("event result ${processResult}")
                    }
//                    break
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