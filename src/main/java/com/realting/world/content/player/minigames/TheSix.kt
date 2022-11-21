package com.realting.world.content.minigames

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.Graphic
import com.realting.model.Locations
import com.realting.model.Position
import com.realting.model.RegionInstance
import com.realting.model.RegionInstance.RegionInstanceType
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import com.realting.world.World
import com.realting.world.content.dialogue.DialogueManager

object TheSix {
    @JvmStatic
    fun enter(player: Player, clan: Boolean) {
        player.packetSender.sendInterfaceRemoval()
        if (clan) {
            if (player.currentClanChat == null) {
                player.packetSender.sendMessage("You must be in a clan to fight the six.")
                return
            }
            if (player.currentClanChat.doingClanBarrows()) {
                player.packetSender.sendMessage("Your clan is already playing a game of The Six.")
                return
            }
        }
        val z = (player.index + 1) * 4
        val orig = player.entityPosition.copy()
        val pos = Position(2384, 4721, z)
        if (player.currentClanChat == null || player.currentClanChat.name == null) {
            player.packetSender.sendMessage("You need to be in a clan chat, make or join one.")
            return
        }
        val close_clan_members = player.currentClanChat.getClosePlayers(orig)
        if (clan && close_clan_members.size <= 1) {
            player.packetSender.sendMessage("You do not have any clan members near you.")
            return
        }
        if (!clan) {
            Barrows.resetBarrows(player)
            player.setDoingClanBarrows(clan)
            player.barrowsKilled = 0
            player.packetSender.sendInterfaceRemoval()
            player.moveTo(pos)
        }
        Barrows.resetBarrows(player)
        player.setDoingClanBarrows(clan)
        player.barrowsKilled = 0
        player.packetSender.sendInterfaceRemoval()
        player.moveTo(pos)
        if (clan) {
            player.currentClanChat.height = z
            player.currentClanChat.setDoingClanBarrows(true)
            player.currentClanChat.regionInstance = RegionInstance(player, RegionInstanceType.THE_SIX)
            player.currentClanChat.regionInstance.playersList.add(player)
            for (p in close_clan_members) {
                if (p == null || p === player) continue
                p.packetSender.sendInterfaceRemoval()
                p.dialogueActionId = 81
                DialogueManager.start(p, 134)
            }
        } else {
            player.regionInstance = RegionInstance(player, RegionInstanceType.THE_SIX)
        }
        spawn(player, clan)
    }

    @JvmStatic
    fun joinClan(player: Player) {
        player.packetSender.sendInterfaceRemoval()
        if (player.currentClanChat != null && player.currentClanChat.doingClanBarrows()) {
            player.setDoingClanBarrows(true)
            player.barrowsKilled = 0
            Barrows.resetBarrows(player)
            player.moveTo(Position(2384, 4721, player.currentClanChat.height))
            player.currentClanChat.regionInstance.playersList.add(player)
        }
    }

    @JvmStatic
    fun leave(player: Player, move: Boolean) {
        val killcount = player.minigameAttributes.barrowsMinigameAttributes.killcount
        if (killcount > 0) {
            val points = if (player.doingClanBarrows()) 1 * killcount else 2 * killcount
            player.pointsHandler.setBarrowsPoints(points, true)
            player.packetSender.sendMessage("You've received $points Barrows points.")
        }
        Barrows.resetBarrows(player)
        if (move) {
            player.moveTo(Position(3562, 3311))
        }
        if (player.doingClanBarrows()) {
            if (player.currentClanChat != null && player.currentClanChat.regionInstance != null) {
                player.currentClanChat.regionInstance.playersList.remove(player)
                if (player.currentClanChat.regionInstance.playersList.size <= 0) {
                    player.currentClanChat.regionInstance.destruct()
                    player.currentClanChat.regionInstance = null
                    player.currentClanChat.setDoingClanBarrows(false)
                }
            }
        }
        player.setDoingClanBarrows(false)
    }

    @JvmStatic
    fun allKilled(player: Player): Boolean {
        if (player.barrowsKilled >= 6) {
            player.barrowsKilled = 0
            for (i in player.minigameAttributes.barrowsMinigameAttributes.barrowsData.indices) player.minigameAttributes.barrowsMinigameAttributes.barrowsData[i][1] =
                0
            Barrows.updateInterface(player)
            return true
        }
        return false
    }

    @JvmStatic
    fun spawn(player: Player, clan: Boolean) {
        val z = player.entityPosition.z
        //CustomObjects.spawnGlobalObjectWithinDistance(new GameObject(2273, new Position(2384, 4715, z), 10, 1));
        //CustomObjects.spawnGlobalObjectWithinDistance(new GameObject(1864, new Position(2383, 4715, z), 10, 1));
        //CustomObjects.spawnGlobalObjectWithinDistance(new GameObject(1864, new Position(2382, 4715, z), 10, 1));
        TaskManager.submit(object : Task(3, player, false) {
            var tick = 0
            override fun execute() {
                if (player.location !== Locations.Location.THE_SIX || clan && (player.currentClanChat == null || player.currentClanChat.regionInstance == null) || !clan && player.regionInstance == null) {
                    leave(player, false)
                    stop()
                    return
                }
                var pos: Position? = null
                var npc: NPC? = null
                when (tick) {
                    0 -> {
                        pos = Position(2385, 4717, z)
                        npc = NPC(2030, pos)
                    }
                    1 -> {
                        pos = Position(2384, 4723, z)
                        npc = NPC(2026, pos)
                    }
                    2 -> {
                        pos = Position(2388, 4720, z)
                        npc = NPC(2025, pos)
                    }
                    3 -> {
                        pos = Position(2379, 4720, z)
                        npc = NPC(2028, pos)
                    }
                    4 -> {
                        pos = Position(2382, 4723, z)
                        npc = NPC(2029, pos)
                    }
                    5 -> {
                        pos = Position(2387, 4722, z)
                        npc = NPC(2027, pos)
                    }
                    6 -> stop()
                }
                if (npc != null && pos != null) {
                    World.register(npc)
                    npc.performGraphic(Graphic(354))
                    var target = player
                    if (clan) {
                        val LIST = ArrayList<Player>()
                        for (p in player.currentClanChat.members) {
                            if (p == null || !p.doingClanBarrows()) {
                                continue
                            }
                            LIST.add(p)
                        }
                        target = LIST[Misc.getRandom(LIST.size - 1)]
                    }
                    npc.combatBuilder.attack(target)
                    if (clan) {
                        player.currentClanChat.regionInstance.npcsList.add(npc)
                    } else {
                        player.regionInstance.npcsList.add(npc)
                    }
                }
                tick++
            }
        })
    }
}