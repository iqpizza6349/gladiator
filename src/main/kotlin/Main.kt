package io.iqpizza

import com.github.ocraft.s2client.bot.S2Agent
import com.github.ocraft.s2client.bot.S2Coordinator
import com.github.ocraft.s2client.protocol.game.Difficulty
import com.github.ocraft.s2client.protocol.game.LocalMap
import com.github.ocraft.s2client.protocol.game.Race
import java.nio.file.Paths

fun main(vararg args: String) {
    val bot: S2Agent = Gladiator()
    val map = LocalMap.of(Paths.get("/Users/iqpizza6349/Downloads/2023s3/Equilibrium512V2AIE.SC2Map").toAbsolutePath())
    val coordinator = S2Coordinator.setup()
        .loadSettings(args)
//        .setRealtime(true)
        .setParticipants(
            S2Coordinator.createParticipant(Race.TERRAN, bot),
            S2Coordinator.createComputer(Race.ZERG, Difficulty.HARDER)
        )
        .launchStarcraft()
        .startGame(map)

    while (coordinator.update()) {}

    coordinator.quit()
}