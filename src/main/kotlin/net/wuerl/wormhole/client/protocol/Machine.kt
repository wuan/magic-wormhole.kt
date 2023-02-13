package net.wuerl.wormhole.client.protocol

import io.ktor.websocket.*
import kotlinx.coroutines.channels.SendChannel
import ru.nsk.kstatemachine.*

class MachineState {
    var welcome: String? = null
    lateinit var nameplate: String
}

fun getMachine() = createStateMachine {
    val state = MachineState()
    addInitialState(States.Connect) {
        onEntry { println("Enter Connect") }
        onExit { println("Exit Connect") }
        addInitialState(States.Welcome) {
            onEntry { println("Enter Connect.Welcome") }
            onExit { println("Exit Connect.Welcome\n") }

            transition<CancelEvent> {
                targetState = States.ExitState
                onTriggered { println("Cancelled") }
            }
            transition<WelcomeEvent> {
                targetState = States.Bind
                onTriggered {
                    println("Received Welcome")
                    state.welcome = it.event.data.welcome.motd
                }
            }
        }

        addState(States.Bind) {
            onEntry {
                println("Enter Connect.Bind")
                sendResponse(it.argument, Bind(side = "receive"))
            }
            onExit { println("Exit Connect.Bind\n") }
            transition<AckEvent> {
                targetState = States.Open
                onTriggered { println("Ack in Connect.Bind") }
            }
        }
    }

    addState(States.Open) {
        onEntry { println("Enter Open") }
        onExit { println("Exit Open") }
        addInitialState(States.Allocate) {
            onEntry {
                println("Enter Open.Allocate")
                sendResponse(it.argument, Allocate())
            }
            onExit { println("Exit Open.Allocate\n") }
            transition<AckEvent> {
                targetState = States.Allocate
                onTriggered { println("received Ack in Open.Allocate") }
            }
            dataTransition<AllocatedEvent, Allocated> {
                targetState = ClaimState
                onTriggered { println("Received Allocated") }
            }
        }

        addState(ClaimState) {
            onEntry {
                println("Enter Open.Claim $data")
                state.nameplate = data.nameplate
                sendResponse(it.argument, Claim(data.nameplate))
            }
            onExit { println("Exit Open.Claim\n") }
            transition<AckEvent> {
                onTriggered { println("received Ack in Open.Claim") }
            }
            dataTransition<ClaimedEvent, Claimed> {
                targetState = OpenState
                onTriggered { println("Received Claimed ${it.direction}") }
            }
        }

        addState(OpenState) {
            onEntry {
                println("Enter Open.Open $data")
                sendResponse(it.argument, Open(data.mailbox))
            }
            onExit { println("Exit Open.Open\n") }
            transition<AckEvent> {
                onTriggered {
                    println("received n Ack in Open.Open")
                    val code = CodeFactory().create(state.nameplate)
                    println("MOTD: ${state.welcome}")
                    println("code: ${code})")
                }
            }
        }
    }
    ignoredEventHandler = StateMachine.IgnoredEventHandler { event, _ ->
        error("unexpected $event")
    }
}

class MachineWrapper(
    val machine: StateMachine,
    val eventMapper: EventMapper = EventMapper()
) {

    fun processMessage(
        message: Frame.Text, outgoing: SendChannel<Frame>
    ) {
        val event = eventMapper.mapEvent(message.readText())
        if (event != null) {
            processEvent(event, outgoing)
        }
    }

    private fun processEvent(
        event: Event, outgoing: SendChannel<Frame>
    ) {
        val processResult = machine.processEvent(event, outgoing)
        if (processResult != ProcessingResult.PROCESSED) {
            println("event result $processResult")
        }
    }
}
