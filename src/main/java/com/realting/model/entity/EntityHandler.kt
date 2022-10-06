package com.realting.model.entity

import com.realting.engine.task.TaskManager
import com.realting.model.GameObject
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.net.SessionState
import com.realting.world.World
import com.realting.world.clip.region.RegionClipping
import com.realting.world.content.CustomObjects

object EntityHandler {
    @JvmStatic
    fun register(entity: Entity) {
        when {
            entity.isPlayer -> {
                val player = entity as Player
                val session = player.session
                if (session.state == SessionState.LOGGING_IN && !World.getLoginQueue().contains(player)) {
                    World.getLoginQueue().add(player)
                }
            }
            entity.isNpc -> {
                val npc = entity as NPC
                World.getNpcs().add(npc)
            }
            entity.isGameObject -> {
                val gameObject = entity as GameObject
                RegionClipping.addObject(gameObject)
                CustomObjects.spawnGlobalObjectWithinDistance(gameObject)
            }
        }
    }

    @JvmStatic
    fun deregister(entity: Entity) {
        when {
            entity.isPlayer -> {
                val player = entity as Player
                World.getPlayers().remove(player)
            }
            entity.isNpc -> {
                val npc = entity as NPC
                TaskManager.cancelTasks(npc.combatBuilder)
                TaskManager.cancelTasks(entity)
                World.getNpcs().remove(npc)
            }
            entity.isGameObject -> {
                val gameObject = entity as GameObject
                RegionClipping.removeObject(gameObject)
                CustomObjects.deleteGlobalObjectWithinDistance(gameObject)
            }
        }
    }
}