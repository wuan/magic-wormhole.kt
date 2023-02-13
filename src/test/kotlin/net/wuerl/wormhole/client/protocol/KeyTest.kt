package net.wuerl.wormhole.client.protocol

import io.github.muntashirakon.crypto.ed25519.Utils
import io.github.muntashirakon.crypto.spake2.Spake2Context
import io.github.muntashirakon.crypto.spake2.Spake2Role
import org.junit.jupiter.api.Test
import java.nio.charset.Charset

class KeyTest {
    @Test
    fun generatesKey() {
        val charset = Charset.forName("ASCII")
        val receiver = Spake2Context(Spake2Role.Bob, "B".toByteArray(charset), "B".toByteArray(charset))

        val key = Key()

        val password = "foo"

        val senderSecret = key.generateMessage(password)
        println("sender secret $senderSecret")

        val receiverSecret = Utils.bytesToHex(receiver.generateMessage(password.toByteArray(charset)))
        println("receiver secret $receiverSecret}")

        val receiverKey = key.processMessage(senderSecret)
        println("receiver key $receiverKey}")

        val senderKey = Utils.bytesToHex(receiver.processMessage(receiverSecret.toByteArray(charset)))
        println("sender key $senderKey}")
    }

    @Test
    fun testit() {
        val alicePassword = "foo"
        val bobPassword = "foo"
        val charset = Charset.forName("UTF-8")
        val alice = Spake2Context(Spake2Role.Alice, "alice".toByteArray(charset), "bob".toByteArray(charset))
        val bob = Spake2Context(Spake2Role.Bob, "bob".toByteArray(charset), "alice".toByteArray(charset))
// The below methods are kept for compatibility with BoringSSL
// alice.setDisablePasswordScalarHack(true);
// bob.setDisablePasswordScalarHack(true);
// Messages
// The below methods are kept for compatibility with BoringSSL
// alice.setDisablePasswordScalarHack(true);
// bob.setDisablePasswordScalarHack(true);
// Messages
        val aliceMsg = alice.generateMessage(alicePassword.toByteArray(charset))
        val bobMsg = bob.generateMessage(bobPassword.toByteArray(charset))
// Fetch keys
// Fetch keys
        val aliceKey = alice.processMessage(bobMsg)
        val bobKey = bob.processMessage(aliceMsg)

    }
}