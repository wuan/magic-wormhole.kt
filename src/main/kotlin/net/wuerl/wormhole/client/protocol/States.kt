package net.wuerl.wormhole.client.protocol

import ru.nsk.kstatemachine.DefaultDataState
import ru.nsk.kstatemachine.DefaultState
import ru.nsk.kstatemachine.Event
import ru.nsk.kstatemachine.FinalState

object CancelEvent : Event


sealed class States : DefaultState() {
    object Connect : States()
    object Welcome : States()
    object Bind : States()

    object Open : States()

    object Allocate : States()
    object ExitState : States(), FinalState // Machine finishes when enters final state
}

object ClaimState : DefaultDataState<Allocated>()
object OpenState : DefaultDataState<Claimed>()

