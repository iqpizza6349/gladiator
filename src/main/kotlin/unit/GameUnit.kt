package io.iqpizza.unit

import com.github.ocraft.s2client.protocol.unit.Tag

data class GameUnit(val tag: Tag) {
    companion object {
        val DUMMY = GameUnit(Tag.from(-1L))
    }
}
