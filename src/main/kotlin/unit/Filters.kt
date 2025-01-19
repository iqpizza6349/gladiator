package io.iqpizza.unit

import com.github.ocraft.s2client.bot.gateway.UnitInPool
import com.github.ocraft.s2client.protocol.data.Units

object Filters {
    private val workers = arrayOf(
        Units.TERRAN_SCV,
        Units.PROTOSS_PROBE,
        Units.ZERG_DRONE
    )
    private val terranBuildings = arrayOf(
        Units.TERRAN_ARMORY,
        Units.TERRAN_AUTO_TURRET,
        Units.TERRAN_BANSHEE,
        Units.TERRAN_BARRACKS,
        Units.TERRAN_BARRACKS_FLYING,
        Units.TERRAN_BARRACKS_REACTOR,
        Units.TERRAN_BARRACKS_TECHLAB,
        Units.TERRAN_BUNKER,
        Units.TERRAN_COMMAND_CENTER,
        Units.TERRAN_COMMAND_CENTER_FLYING,
        Units.TERRAN_CYCLONE,
        Units.TERRAN_ENGINEERING_BAY,
        Units.TERRAN_FACTORY,
        Units.TERRAN_FACTORY_FLYING,
        Units.TERRAN_FACTORY_REACTOR,
        Units.TERRAN_FACTORY_TECHLAB,
        Units.TERRAN_FUSION_CORE,
        Units.TERRAN_GHOST_ACADEMY,
        Units.TERRAN_MISSILE_TURRET,
        Units.TERRAN_ORBITAL_COMMAND,
        Units.TERRAN_ORBITAL_COMMAND_FLYING,
        Units.TERRAN_PLANETARY_FORTRESS,
        Units.TERRAN_REFINERY,
        Units.TERRAN_REFINERY_RICH,
        Units.TERRAN_SENSOR_TOWER,
        Units.TERRAN_STARPORT,
        Units.TERRAN_STARPORT_FLYING,
        Units.TERRAN_STARPORT_REACTOR,
        Units.TERRAN_STARPORT_TECHLAB,
        Units.TERRAN_SUPPLY_DEPOT,
        Units.TERRAN_SUPPLY_DEPOT_LOWERED
    )
    private val zergBuildings = arrayOf(
        Units.ZERG_BANELING_NEST,
        Units.ZERG_CREEP_TUMOR,
        Units.ZERG_CREEP_TUMOR_BURROWED,
        Units.ZERG_EXTRACTOR,
        Units.ZERG_EXTRACTOR_RICH,
        Units.ZERG_GREATER_SPIRE,
        Units.ZERG_HATCHERY,
        Units.ZERG_HIVE,
        Units.ZERG_HYDRALISK_DEN,
        Units.ZERG_LAIR,
        Units.ZERG_LURKER_DEN_MP,
        Units.ZERG_ROACH_WARREN,
        Units.ZERG_SPAWNING_POOL,
        Units.ZERG_SPINE_CRAWLER,
        Units.ZERG_SPINE_CRAWLER_UPROOTED,
        Units.ZERG_SPIRE,
        Units.ZERG_SPORE_CRAWLER,
        Units.ZERG_SPORE_CRAWLER_UPROOTED,
        Units.ZERG_SWARM_HOST_BURROWED_MP,
        Units.ZERG_SWARM_HOST_MP,
        Units.ZERG_ULTRALISK_CAVERN
    )
    private val protossBuildings = arrayOf(
        Units.PROTOSS_ASSIMILATOR,
        Units.PROTOSS_ASSIMILATOR_RICH,
        Units.PROTOSS_CYBERNETICS_CORE,
        Units.PROTOSS_DARK_SHRINE,
        Units.PROTOSS_FLEET_BEACON,
        Units.PROTOSS_FORGE,
        Units.PROTOSS_GATEWAY,
        Units.PROTOSS_NEXUS,
        Units.PROTOSS_PYLON,
        Units.PROTOSS_PYLON_OVERCHARGED,
        Units.PROTOSS_ROBOTICS_BAY,
        Units.PROTOSS_ROBOTICS_FACILITY,
        Units.PROTOSS_SHIELD_BATTERY,
        Units.PROTOSS_STARGATE,
        Units.PROTOSS_TEMPLAR_ARCHIVE,
        Units.PROTOSS_TWILIGHT_COUNCIL,
        Units.PROTOSS_WARP_GATE,
        Units.PROTOSS_ASSIMILATOR
    )

    fun isAlive(unit: UnitInPool): Boolean {
        return unit.isAlive
    }

    fun isBuilding(unit: UnitInPool): Boolean {
        val optional = unit.unit ?: return false
        return optional.filter {
            it.type in terranBuildings || it.type in zergBuildings || it.type in protossBuildings
        }.isPresent
    }
}