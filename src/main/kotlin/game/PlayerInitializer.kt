package io.iqpizza.game

import com.github.ocraft.s2client.protocol.game.PlayerInfo
import com.github.ocraft.s2client.protocol.game.Race
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.iqpizza.GameInitializer
import io.iqpizza.utils.StopWatch
import kotlin.jvm.optionals.getOrElse

class PlayerInitializer(stopWatch: StopWatch = StopWatch("PlayerInitializer")) : GameInitializer(stopWatch) {
    private val logger = KotlinLogging.logger("GameInitializer-${stopWatch.id}")

    override fun logger(): KLogger = logger

    /**
     * [Game.players] 가 Null 이 아니고, empty 가 아닌 경우에
     * 한해서 정상적으로 수행됩니다.
     *
     * [Game.playerRace] 에 실제 인-게임 종족을 설정합니다.
     */
    fun initializeRace(playerId: Int, setRace: ((Race) -> Unit)) {
        initializeRace(playerClause = { player -> player.playerId == playerId }, setRace)
    }

    fun initializeRace(playerClause: ((PlayerInfo) -> Boolean), setRace: ((Race) -> Unit)) {
        stopWatch.start("InitializeRace")
        val player = Game.players.firstOrNull(playerClause) ?: throw IllegalStateException("Player not found")
        // 상대방 종족을 바로 알 수 없음(random 인 경우, 다른 게 할당될 수 있기 때문
        val race = player.actualRace.getOrElse { player.requestedRace }
        setRace(race)
        stopWatch.stop()
    }
}