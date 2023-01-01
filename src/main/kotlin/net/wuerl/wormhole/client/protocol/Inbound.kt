package net.wuerl.wormhole.client.protocol

import net.wuerl.wormhole.client.Message
import ru.nsk.kstatemachine.DataEvent

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
