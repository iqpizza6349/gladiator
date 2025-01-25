package io.iqpizza.system

import io.iqpizza.unit.GameUnit

data class Command(val type: CommandType, val priority: Priority = Priority.LOW, val data: Pair<GameUnit, *>)
