package com.realting.world.content.minigames

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.Locations
import com.realting.model.Position
import com.realting.model.RegionInstance
import com.realting.model.RegionInstance.RegionInstanceType
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import com.realting.world.World
import com.realting.world.content.dialogue.DialogueManager

object Graveyard {
    @JvmStatic
    fun start(player: Player) {
        player.packetSender.sendInterfaceRemoval()
        player.moveTo(Position(3503, 3568, (player.index + 1) * 4))
        player.regionInstance = RegionInstance(player, RegionInstanceType.GRAVEYARD)
        DialogueManager.start(player, 97)
        player.minigameAttributes.graveyardAttributes.setEntered(true).setWave(1).level = 0
        spawn(player, 1, 0)
        player.packetSender.sendMessage("<img=10><col=FF0000><shad=0> To leave the graveyard, simply teleport out.")
    }

    @JvmStatic
    fun leave(player: Player) {
        player.combatBuilder.reset(true)
        player.moveTo(Position(3503, 3564))
        if (player.regionInstance != null) player.regionInstance.destruct()
        player.restart()
        player.minigameAttributes.graveyardAttributes.setEntered(false)
    }

    private fun spawn(player: Player, wave: Int, level: Int) {
        if (level == 10) {
            leave(player)
            player.packetSender.sendMessage("You successfully cleared out the graveyard!")
            return
        }
        TaskManager.submit(object : Task(4, player, false) {
            public override fun execute() {
                if (player.regionInstance == null || !player.isRegistered || player.location !== Locations.Location.GRAVEYARD) {
                    stop()
                    return
                }
                val zombieAmount = wave * 2
                player.minigameAttributes.graveyardAttributes.requiredKills = zombieAmount
                for (i in 0..zombieAmount) {
                    val n = NPC(getSpawn(level), getSpawnPos(player.entityPosition.z)).setSpawnedFor(player)
                    World.register(n)
                    player.regionInstance.npcsList.add(n)
                    n.combatBuilder.attack(player)
                }
                stop()
            }
        })
    }

    @JvmStatic
    fun handleDeath(player: Player, npc: NPC): Boolean {
        var amount = 0
        when (npc.id) {
            76 -> amount = 1
            5664 -> amount = 3
            5400 -> amount = 6
            8162 -> amount = 9
            5407 -> amount = 13
        }
        if (amount > 0) {
            //	GroundItemManager.spawnGroundItem(player, new GroundItem(new Item(14667), npc.getPosition(), player.getUsername(), false, 150, false, -1));
            player.inventory.add(14667, 1)
            if (player.minigameAttributes.graveyardAttributes.decrementAndGetRequiredKills() <= 0) {
                if (player.minigameAttributes.graveyardAttributes.incrementAndGetWave() >= 5) {
                    player.minigameAttributes.graveyardAttributes.setWave(1).incrementLevel()
                }
                spawn(
                    player,
                    player.minigameAttributes.graveyardAttributes.wave,
                    player.minigameAttributes.graveyardAttributes.level
                )
            }
            return true
        }
        return false
    }

    private fun getSpawnPos(z: Int): Position {
        when (Misc.getRandom(15)) {
            0 -> return Position(3508, 3570, z)
            1 -> return Position(3507, 3572, z)
            2 -> return Position(3508, 3574, z)
            3 -> return Position(3504, 3576, z)
            4 -> return Position(3505, 3573, z)
            5 -> return Position(3499, 3575, z)
            6 -> return Position(3499, 3578, z)
            7 -> return Position(3495, 3572, z)
            8 -> return Position(3495, 3574, z)
            9 -> return Position(3499, 3569, z)
            10 -> return Position(3503, 3569, z)
            11 -> return Position(3504, 3571, z)
            12 -> return Position(3502, 3574, z)
            13 -> return Position(3503, 3577, z)
            14 -> return Position(3505, 3577, z)
            15 -> return Position(3506, 3575, z)
        }
        return Position(3508, 3570, z)
    }

    private fun getSpawn(level: Int): Int {
        val random = Misc.getRandom(14)
        when (level) {
            1 -> {
                return if (random <= 2) 76 else 5664
            }
            2 -> {
                if (random <= 3) return 76 else if (random == 4 || random == 5) return 5664
                return 5400
            }
            3 -> {
                if (random <= 3) return 76 else if (random == 4 || random == 5) return 5664 else if (random == 6 || random == 7) return 5400
                return 8162
            }
            4, 5, 6, 7, 8, 9 -> {
                if (random <= 3) return 76 else if (random == 4 || random == 5) return 5664 else if (random == 8) return 8162
                return 5407
            }
        }
        return 76
    }
}