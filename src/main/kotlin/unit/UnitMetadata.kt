package io.iqpizza.unit

import com.github.ocraft.s2client.protocol.data.Units

/**
 * API 가 기본적으로 제공하지 않거나 선행적으로 찾는 것이 불가능한 유닛들의
 * 메타 데이터들입니다.
 * 참고로 `supplyCost` 계산 시, 저글링의 경우 1개이든 2개이든 1로 간주합니다.
 */
enum class UnitMetadata(val type: Units, val mineralCost: Int = 0, val gasCost: Int = 0, val supplyCost: Int = 1) {
    UNKNOWN(type = Units.INVALID),
    SCV(type = Units.TERRAN_SCV, mineralCost = 50)
    ;

    companion object {
        fun findMetaData(type: Units): UnitMetadata {
            val unitMetadata = UnitMetadata.entries.toTypedArray()
            for (metaData in unitMetadata) {
                if (type === metaData.type) {
                    return metaData
                }
            }

            return UNKNOWN
        }
    }
}
