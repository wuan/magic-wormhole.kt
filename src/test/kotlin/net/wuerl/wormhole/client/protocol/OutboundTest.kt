package net.wuerl.wormhole.client.protocol

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OutboundTest {
    @Test
    fun serializeBind() {
        val result = klaxon.toJsonString(Bind(side = "receive"));
        assertThat(result).isEqualTo("{\"appid\" : \"lothar.com/wormhole/text-or-file-xfer\", \"client_version\" : [\"kotlin\", \"0.1.0\"], \"side\" : \"receive\", \"type\" : \"bind\"}")
    }
}