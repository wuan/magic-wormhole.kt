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
    }

    @Order(2)
    @Test
    fun handlesBindAck() {
        val frameSlot = slot<Frame>()
        coEvery { sendChannel.send(capture(frameSlot)) } returns Unit

        val event = AckEvent(Ack(null, 1.0f))
        machine.processEvent(event, sendChannel)

        val bind: Allocate? = klaxon.parse<Allocate>(frameSlot.captured.data.decodeToString())
        assertThat(bind?.type).isEqualTo("allocate")
    }

    @Order(3)
    @Test
    fun handlesAllocated() {
        val frameSlot = slot<Frame>()
        coEvery { sendChannel.send(capture(frameSlot)) } returns Unit

        val event = AllocatedEvent(Allocated(null, "foo", 1.23f))
        machine.processEvent(event, sendChannel)

        val bind: Claim? = klaxon.parse<Claim>(frameSlot.captured.data.decodeToString())
        assertThat(bind?.type).isEqualTo("claim")
    }

    @Order(4)
    @Test
    fun handlesOpen() {
        val frameSlot = slot<Frame>()
        coEvery { sendChannel.send(capture(frameSlot)) } returns Unit

        val event = ClaimedEvent(Claimed("bar", 1.23f))
        machine.processEvent(event, sendChannel)

        val bind: Open? = klaxon.parse<Open>(frameSlot.captured.data.decodeToString())
        assertThat(bind?.type).isEqualTo("open")
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