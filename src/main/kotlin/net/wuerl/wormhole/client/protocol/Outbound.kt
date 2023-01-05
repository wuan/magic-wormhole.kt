package net.wuerl.wormhole.client.protocol

import io.ktor.websocket.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.runBlocking

open class Outbound(val type: String)

open class Bind(
    val appid: String = "lothar.com/wormhole/text-or-file-xfer",
    val side: String,
    val clientVersion: Array<String> = arrayOf("kotlin", "0.1.0"),
) : Outbound("bind")

class Allocate : Outbound("allocate")

class Claim(val nameplate: String) : Outbound("claim")

class Open(val mailbox: String) : Outbound("open")

fun sendResponse(argument: Any?, payload: Outbound) {
    if (argument is SendChannel<*>) {
        @Suppress("UNCHECKED_CAST") val sendChannel = argument as SendChannel<Frame>
        runBlocking {
            print("Start send '${payload.type}' ...")
            sendChannel.send(
                Frame.Text(klaxon.toJsonString(payload))
            )
            println(" done")
        }
    }

}