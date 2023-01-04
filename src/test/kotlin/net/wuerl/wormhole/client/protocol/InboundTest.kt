package net.wuerl.wormhole.client.protocol

import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InboundTest {

    @Test
    fun parsesType() {
        val type = parseType("{\"id\": null, \"type\": \"ack\", \"server_tx\": 1672596298.0351338}")
        assertThat(type).isEqualTo("ack")
    }

    @Test
    fun parsesEmptyType() {
        val type = parseType("{\"id\": null}")
        assertThat(type).isNull()
    }
}

class EventMapperTest {

    val uut = EventMapper()

    @Test
    fun mapAckEvent() {
        val result = uut.mapEvent("{\"id\": null, \"type\": \"ack\", \"server_tx\": 1672596298.0351338}")

        assertThat(result).isInstanceOf(AckEvent::class.java)
        if (result is AckEvent) {
            assertThat(result.data).isEqualTo(Ack(id = null, serverTx = 1672596298.0351338f))
        }
    }
}