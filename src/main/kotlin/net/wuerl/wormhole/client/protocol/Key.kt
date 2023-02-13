package net.wuerl.wormhole.client.protocol

import io.github.muntashirakon.crypto.ed25519.Utils
import io.github.muntashirakon.crypto.spake2.Spake2Context
import io.github.muntashirakon.crypto.spake2.Spake2Role
import java.nio.charset.Charset

class Key {
    private val charset: Charset
    private val context: Spake2Context

    init {
        charset = Charset.forName("ASCII")
        context = Spake2Context(Spake2Role.Alice, "A".toByteArray(charset), "B".toByteArray(charset))
    }

    fun generateMessage(password: String): String {
        val generateMessage = context.generateMessage(password.toByteArray(charset));
        return Utils.bytesToHex(generateMessage)
    }

    fun processMessage(receiverSecret: String?): String {
        val processMessage = context.processMessage(Utils.hexToBytes(receiverSecret))
        return Utils.bytesToHex(processMessage)
    }
}