package com.realting.world.content.minigames

import com.realting.engine.task.Task
import com.realting.model.RegionInstance.RegionInstanceType
import com.realting.engine.task.TaskManager
import com.realting.world.World
import com.realting.world.content.dialogue.DialogueManager
import com.realting.model.*
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc

object FightCave {
    const val JAD_NPC_ID = 2745

    @JvmStatic
    fun enterCave(player: Player) {
        player.moveTo(Position(2413, 5117, (player.index + 1) * 4))
        DialogueManager.start(player, 36)
        player.regionInstance = RegionInstance(player, RegionInstanceType.FIGHT_CAVE)
        spawnJad(player)
    }

    @JvmStatic
    fun leaveCave(player: Player, resetStats: Boolean) {
        Locations.Location.FIGHT_CAVES.leave(player)
        if (resetStats) player.restart()
    }

    fun spawnJad(player: Player) {
        TaskManager.submit(object : Task(2, player, false) {
            public override fun execute() {
                if (player.regionInstance == null || !player.isRegistered || player.location !== Locations.Location.FIGHT_CAVES) {
                    stop()
                    return
                }
                val n = NPC(JAD_NPC_ID, Position(2399, 5083, player.position.z)).setSpawnedFor(player)
                World.register(n)
                player.regionInstance.npcsList.add(n)
                n.combatBuilder.attack(player)
                stop()
            }
        })
    }

    @JvmStatic
    fun handleJadDeath(player: Player, n: NPC) {
        if (n.id == JAD_NPC_ID) {
            if (player.regionInstance != null) player.regionInstance.npcsList.remove(n)
            leaveCave(player, true)
            DialogueManager.start(player, 37)
            player.inventory.add(6570, 1).add(6529, 1000 + Misc.getRandom(2000))
        }
    }
}