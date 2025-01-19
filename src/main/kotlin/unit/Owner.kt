package io.iqpizza.unit

import com.github.ocraft.s2client.protocol.unit.Alliance

data class Owner(val alliance: Alliance) {
    companion object {
        val SELF = Owner(Alliance.SELF)
        val ENEMY = Owner(Alliance.ENEMY)
    }
}
