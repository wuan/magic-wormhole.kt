package net.wuerl.wormhole.client.protocol

import ru.nsk.kstatemachine.DefaultDataState
import ru.nsk.kstatemachine.DefaultState
import ru.nsk.kstatemachine.Event
import ru.nsk.kstatemachine.FinalState

object CancelEvent : Event


sealed class States : DefaultState() {
    object InitState : States()
    object StartState : States()
    object Start2State : States()
    object ExitState : States(), FinalState // Machine finishes when enters final state
}

object Start3State : DefaultDataState<Allocated>()
object Start4State : DefaultDataState<Claimed>()

