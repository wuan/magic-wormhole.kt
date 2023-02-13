/*

   Copyright 2022 Andreas Würl

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package net.wuerl.wormhole.client

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.runBlocking
import net.wuerl.wormhole.client.http.webSocket
import net.wuerl.wormhole.client.protocol.MachineWrapper
import net.wuerl.wormhole.client.protocol.getMachine

val url = Url("ws://relay.magic-wormhole.io:4000/v1")
val url3 = Url("wss://mailbox.mw.leastauthority.com/v1")


fun main() {

    val machine = getMachine()
    val wrapper = MachineWrapper(machine)

    val client = HttpClient {
        install(WebSockets)
    }

    runBlocking {
        client.webSocket(url3) {
            while (true) {
                val message = incoming.receive() as? Frame.Text
                if (message != null) {
                    wrapper.processMessage(message, outgoing)
                }
            }
        }
    }
    client.close()
    println("Connection closed. Goodbye!")
}
