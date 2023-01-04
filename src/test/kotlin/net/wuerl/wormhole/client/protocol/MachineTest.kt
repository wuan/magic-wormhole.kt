package net.wuerl.wormhole.client.protocol

import io.ktor.websocket.*
import io.mockk.*
import kotlinx.coroutines.channels.SendChannel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import ru.nsk.kstatemachine.StateMachine

@TestMethodOrder(
    MethodOrderer.OrderAnnotation::class
)
class MachineTest {

    lateinit var sendChannel: SendChannel<Frame>

    @BeforeEach
    fun setUp() {
        sendChannel = mockk()
    }

    @Order(1)
    @Test
    fun handlesWelcome() {
        val frameSlot = slot<Frame>()
        coEvery { sendChannel.send(capture(frameSlot)) } returns Unit

        val event = WelcomeEvent(Welcome(WelcomeOptions(), 1.0f))
        machine.processEvent(event, sendChannel)

        val bind: Bind? = klaxon.parse<Bind>(frameSlot.captured.data.decodeToString())
        assertThat(bind?.type).isEqualTo("bind")

        println(machine)
        println(machine.isRunning)
    }

    @Order(2)
    @Test
    fun handlesAck() {
        println(machine)
        println(machine.isRunning)

        val frameSlot = slot<Frame>()
        coEvery { sendChannel.send(capture(frameSlot)) } returns Unit

        val event = AckEvent(Ack(null, 1.0f))
        machine.processEvent(event, sendChannel)

        val bind: Allocate? = klaxon.parse<Allocate>(frameSlot.captured.data.decodeToString())
        assertThat(bind?.type).isEqualTo("allocate")
    }

    companion object {
        @JvmStatic
        lateinit var machine: StateMachine;

        @JvmStatic
        @BeforeAll
        fun setupAll(): Unit {
            machine = getMachine()
        }
    }
}