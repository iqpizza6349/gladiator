package io.iqpizza.utils

import com.github.ocraft.s2client.protocol.unit.Tag
import com.github.ocraft.s2client.protocol.unit.Unit
import io.iqpizza.unit.GameUnit

fun Tag.toGameUnit(): GameUnit {
    return GameUnit(this)
}

fun Unit.toGameUnit(): GameUnit {
    return GameUnit(this.tag)
}
