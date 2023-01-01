package net.wuerl.wormhole.client.protocol

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class InboundTest {
    @Test
    fun deserializeAck() {
        val result = klaxon.parse<Ack>("{\"id\": null, \"type\": \"ack\", \"server_tx\": 1672596298.0351338}")

        assertThat(result).isEqualTo(Ack(id = null, type = "ack", serverTx = 1672596298.0351338f))
    }
}