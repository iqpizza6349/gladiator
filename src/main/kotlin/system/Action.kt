package io.iqpizza.system

import com.github.ocraft.s2client.protocol.data.Abilities
import io.iqpizza.unit.GameUnit

data class Action(
    val command: Command,
    val execute: Behavior,
)

typealias Behavior = suspend (gameController: GameController, data: Pair<GameUnit, Abilities>) -> Boolean
