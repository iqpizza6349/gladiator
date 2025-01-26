package io.iqpizza.system

import com.github.ocraft.s2client.protocol.data.Abilities
import io.iqpizza.unit.GameUnit

data class Command(
    /**
     * 명령어 종류입니다. 종류에 따라 수행되는 시스템이 다릅니다.
     */
    val type: CommandType,

    /**
     * 우선순위입니다. HIGH 의 경우 반드시 선행적으로 수행하도록 합니다.
     * 기본값은 LOW 입니다.
     */
    val priority: Priority = Priority.LOW,

    /**
     * 명령을 수행하기 위해 필요한 데이터입니다.
     * train 을 제외한 나머지 모든 경우, GameUnit 이 실제 명령을 수행하는 주체입니다.
     * second 의 경우 수행할 abilities 를 명시합니다.
     */
    val data: Pair<GameUnit, Abilities>,

    var retryCount: Int = 0,
)
