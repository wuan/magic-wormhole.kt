package net.wuerl.wormhole.client.protocol

import io.ktor.websocket.*
import kotlinx.coroutines.channels.SendChannel
import ru.nsk.kstatemachine.*

fun getMachine() = createStateMachine {
    addInitialState(States.InitState) {
        // Add state listeners
        onEntry { println("Enter init") }
        onExit { println("Exit init\n") }

        transition<CancelEvent> {
            targetState = States.ExitState
            // Add transition listener
            onTriggered { println("Cancelled") }
        }
        transition<WelcomeEvent> {
            targetState = States.StartState
            onTriggered {
                println("Received Welcome")
            }
        }
    }

    addState(States.StartState) {
        onEntry {
            println("Enter start")
            sendResponse(it.argument, Bind(side = "receive"))
        }
        onExit { println("Exit start\n") }
        transition<AckEvent> {
            targetState = States.Start2State
            onTriggered {
                println("Ack in start")
            }
        }
    }

    addState(States.Start2State) {
        onEntry {
            println("Enter start2")
            sendResponse(it.argument, Allocate())
        }
        onExit { println("Exit start2\n") }
        transition<AckEvent> {
            targetState = States.Start2State
            onTriggered {
                println("Ack in start2")
            }
        }
        dataTransition<AllocatedEvent, Allocated> {
            targetState = Start3State
            onTriggered {
                println("Received Allocated")
            }
        }
    }

    addState(Start3State) {
        onEntry {
            println("Enter start3 $data")
            sendResponse(it.argument, Claim(data.nameplate))
        }
        onExit { println("Exit start3\n") }
        transition<AckEvent> {
            onTriggered {
                println("Ack in start3")
            }
        }
        dataTransition<ClaimedEvent, Claimed> {
            targetState = Start4State
            onTriggered {
                println("Received Claimed")
            }
        }
    }

    addState(Start4State) {
        onEntry {
            println("Enter start4 $data")
            sendResponse(it.argument, Open(data.mailbox))
        }
        onExit { println("Exit start4\n") }
        transition<AckEvent> {
            onTriggered {
                println("Ack in start4")
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
        print("process")
        val processResult = machine.processEvent(event, outgoing)
        if (processResult != ProcessingResult.PROCESSED) {
            println("event result $processResult")
        }
    }
}
