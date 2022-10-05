package com.realting.world.content.minigames

import com.realting.engine.task.Task
import com.realting.model.RegionInstance.RegionInstanceType
import com.realting.engine.task.TaskManager
import com.realting.world.World
import com.realting.world.content.dialogue.DialogueManager
import com.realting.world.content.PlayerPanel
import com.realting.world.content.minigames.Barrows
import com.realting.world.content.minigames.TheSix
import com.realting.engine.task.impl.CeilingCollapseTask
import com.realting.model.*
import com.realting.world.content.minigames.Dueling
import com.realting.world.content.BankPin
import com.realting.world.content.minigames.Dueling.DuelRule
import com.realting.model.container.impl.Equipment
import com.realting.model.container.impl.Inventory
import com.realting.world.content.PlayerLogs
import com.realting.world.content.BonusManager
import java.util.concurrent.CopyOnWriteArrayList
import java.util.Locale
import java.util.HashMap
import com.realting.world.content.minigames.FightPit
import com.realting.world.content.minigames.FightCave
import com.realting.world.content.minigames.Graveyard
import com.realting.world.content.minigames.PestControl
import com.realting.world.content.minigames.PestControl.PestControlNPC
import com.realting.model.movement.MovementQueue
import com.realting.world.content.minigames.FallyMassacre
import com.realting.world.content.minigames.WarriorsGuild
import com.realting.model.entity.character.GroundItemManager
import com.realting.world.content.dialogue.Dialogue
import com.realting.world.content.dialogue.DialogueType
import com.realting.world.content.dialogue.DialogueExpression
import com.realting.world.content.minigames.RecipeForDisaster
import com.realting.world.content.combat.prayer.CurseHandler
import com.realting.world.content.combat.prayer.PrayerHandler
import com.realting.model.container.impl.Shop
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.model.input.impl.EnterAmountToSellToShop
import com.realting.model.input.impl.EnterAmountToBuyFromShop
import com.realting.util.Misc
import com.realting.world.content.minigames.MinigameAttributes.BarrowsMinigameAttributes
import com.realting.world.content.minigames.MinigameAttributes.WarriorsGuildAttributes
import com.realting.world.content.minigames.MinigameAttributes.PestControlAttributes
import com.realting.world.content.minigames.MinigameAttributes.RecipeForDisasterAttributes
import com.realting.world.content.minigames.MinigameAttributes.NomadAttributes
import com.realting.world.content.minigames.MinigameAttributes.GodwarsDungeonAttributes
import com.realting.world.content.minigames.MinigameAttributes.GraveyardAttributes
import com.realting.world.content.minigames.MinigameAttributes.DungeoneeringAttributes
import com.realting.world.content.minigames.MinigameAttributes.trioAttributes
import com.realting.world.content.minigames.MinigameAttributes.ZulrahAttributes
import com.realting.world.content.player.skill.dungeoneering.DungeoneeringParty
import java.util.ArrayList

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
        val orig = player.position.copy()
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
        val z = player.position.z
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