package io.iqpizza.game

/**
 * 게임 속도가 보통을 기준으로 1분 game-loop 가 1350 임을 가정하여,
 * 초당 22.5 로 계산했습니다.
 */
enum class GameSpeed(val perFrameCount: Float) {
    SLOWEST(13.5F),
    SLOW(18F),
    NORMAL(22.5F),
    FAST(27F),
    FASTEST(31.5F)
    ;
}
