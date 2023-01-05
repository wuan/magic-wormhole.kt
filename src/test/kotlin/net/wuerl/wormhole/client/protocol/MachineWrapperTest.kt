package net.wuerl.wormhole.client.protocol

import io.ktor.websocket.*
import io.mockk.*
import kotlinx.coroutines.channels.SendChannel
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.nsk.kstatemachine.Event
import ru.nsk.kstatemachine.ProcessingResult
import ru.nsk.kstatemachine.StateMachine

class MachineWrapperTest {

    lateinit var uut: MachineWrapper
    lateinit var machine: StateMachine
    lateinit var sendChannel: SendChannel<Frame>

    @BeforeEach
    fun setUp() {
        machine = mockk();
        sendChannel = mockk()
        uut = MachineWrapper(machine)
    }

    @Test
    fun processesMessage() {
        val eventSlot = slot<Event>();
        every { machine.processEvent(capture(eventSlot), sendChannel) } returns ProcessingResult.PROCESSED
        val json = "{\"id\": \"foo\", \"nameplate\": \"1\", \"type\": \"allocated\" , \"server_tx\" : 1.2}"
        uut.processMessage(Frame.Text(text = json), sendChannel)

        val event = eventSlot.captured
        assertThat(event).isInstanceOf(AllocatedEvent::class.java)
        if (event is AllocatedEvent) {
            assertThat(event.data.id).isEqualTo("foo")
        }
    }

    @Test
    fun processesIgnoredMessage() {
        val eventSlot = slot<Event>();
        every { machine.processEvent(capture(eventSlot), sendChannel) } returns ProcessingResult.IGNORED
        val json = "{\"id\": \"foo\", \"nameplate\": \"1\", \"type\": \"allocated\" , \"server_tx\" : 1.2}"
        uut.processMessage(Frame.Text(text = json), sendChannel)
    }

    @Test
    fun ignoresUnknownMessage() {
        val eventSlot = slot<Event>();
        val json = "{\"type\": \"unknown\"}"
        uut.processMessage(Frame.Text(text = json), sendChannel)

        verify { machine wasNot called }
    }
}