package net.wuerl.wormhole.client

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.runBlocking
import net.wuerl.wormhole.client.http.webSocket
import net.wuerl.wormhole.client.protocol.*
import ru.nsk.kstatemachine.*

val url = Url("ws://relay.magic-wormhole.io:4000/v1")
val url3 = Url("wss://mailbox.mw.leastauthority.com/v1")



fun main() {

    val machine = getMachine()

    val client = HttpClient {
        install(WebSockets)
    }

    runBlocking {
        client.webSocket(url3) {
            while (true) {
                val othersMessage = incoming.receive() as? Frame.Text
                processMessage(othersMessage, machine)
            }
        }
    }
    client.close()
    println("Connection closed. Goodbye!")
}

private fun DefaultClientWebSocketSession.processMessage(
    othersMessage: Frame.Text?,
    machine: StateMachine
) {
    if (othersMessage != null) {
        val event = mapEvent(othersMessage.readText())
        if (event != null) {
            val processResult = machine.processEvent(event, outgoing)
            if (processResult != ProcessingResult.PROCESSED) {
                println("event result $processResult")
            }
        }
    }
}
