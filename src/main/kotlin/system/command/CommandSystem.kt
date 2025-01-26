package io.iqpizza.system.command

import io.iqpizza.system.Action
import io.iqpizza.system.GameController

interface CommandSystem {
    val gameController: GameController

    /**
     * 특정 Command 에 기반한 Action 을 실행할 수 있는지 확인합니다.
     */
    fun canExecute(action: Action): Boolean

    /**
     * 특정 Command 에 기반한 Action 을 실행합니다.
     * 실행 성공 여부를 Boolean 값으로 반환.
     */
    fun execute(action: Action): Boolean
}
