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
import com.realting.model.definitions.ItemDefinition
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

object WarriorsGuild {
    /*
	 * The armors required to animate an NPC
	 */
    private val ARMOR_DATA = arrayOf(
        intArrayOf(1075, 1117, 1155, 4278),
        intArrayOf(1067, 1115, 1153, 4279),
        intArrayOf(1069, 1119, 1157, 4280),
        intArrayOf(1077, 1125, 1165, 4281),
        intArrayOf(1071, 1121, 1159, 4282),
        intArrayOf(1073, 1123, 1161, 4283),
        intArrayOf(1079, 1127, 1163, 4284)
    )
    private val ANIMATED_ARMOUR_NPCS = intArrayOf(4278, 4279, 4280, 4281, 4282, 4283, 4284)
    private val TOKEN_REWARDS = intArrayOf(5, 10, 15, 20, 26, 32, 40)

    /*
	 * The available defenders which players can receive from this minigame.
	 */
    private val DEFENDERS = intArrayOf(8844, 8845, 8846, 8847, 8848, 8849, 8850, 13262)

    /**
     * Handles what happens when a player uses an item on the animator.
     * @param player    The player
     * @param item        The item the player is using
     * @param object    That animator object which the player is using an item on
     */
    @JvmStatic
    fun itemOnAnimator(player: Player, item: Item, `object`: GameObject): Boolean {
        if (player.minigameAttributes.warriorsGuildAttributes.hasSpawnedArmour() && player.rights != PlayerRights.DEVELOPER) {
            player.packetSender.sendMessage("You have already spawned some animated armour.")
            return true
        } else {
            for (i in ARMOR_DATA.indices) {
                for (f in ARMOR_DATA[0].indices) {
                    if (item.id == ARMOR_DATA[i][f]) {
                        if (player.inventory.contains(ARMOR_DATA[i][0]) && player.inventory.contains(
                                ARMOR_DATA[i][1]
                            ) && player.inventory.contains(ARMOR_DATA[i][2])
                        ) {
                            val items = intArrayOf(ARMOR_DATA[i][0], ARMOR_DATA[i][1], ARMOR_DATA[i][2])
                            if (items != null) {
                                for (a in items.indices) player.inventory.delete(items[a], 1)
                                player.regionInstance = RegionInstance(player, RegionInstanceType.WARRIORS_GUILD)
                                player.minigameAttributes.warriorsGuildAttributes.setSpawnedArmour(true)
                                player.performAnimation(Animation(827))
                                player.packetSender.sendMessage("You place some armor on the animator..")
                                `object`.performGraphic(Graphic(1930))
                                TaskManager.submit(object : Task(2) {
                                    public override fun execute() {
                                        val npc_ =
                                            NPC(ARMOR_DATA[i][3], Position(player.position.x, player.position.y + 1))
                                        npc_.forceChat("I'M ALIVE!!!!")
                                            .setEntityInteraction(player).combatBuilder.attackTimer = 2
                                        npc_.setSpawnedFor(player).combatBuilder.attack(player)
                                        player.positionToFace = npc_.position
                                        World.register(npc_)
                                        player.regionInstance.npcsList.add(npc_)
                                        stop()
                                    }
                                })
                            }
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    /**
     * Handles a drop after an NPC is slain in the Warriors guild
     * @param player    The player to handle the drop for
     * @param npc        The npc which will drop something
     */
    @JvmStatic
    fun handleDrop(player: Player?, npc: NPC?) {
        if (player == null || npc == null) return
        /*
		 * Tokens
		 */if (npc.id >= 4278 && npc.id <= 4284) {
            if (player.regionInstance != null) player.regionInstance.npcsList.remove(npc)
            var armour: IntArray? = null
            for (i in ARMOR_DATA.indices) {
                if (ARMOR_DATA[i][3] == npc.id) {
                    armour = intArrayOf(ARMOR_DATA[i][0], ARMOR_DATA[i][1], ARMOR_DATA[i][2])
                    break
                }
            }
            if (armour != null) {
                for (i in armour) GroundItemManager.spawnGroundItem(
                    player, GroundItem(Item(i), npc.position.copy(), player.username, false, 80, true, 80)
                )
                player.minigameAttributes.warriorsGuildAttributes.setSpawnedArmour(false)
                GroundItemManager.spawnGroundItem(
                    player, GroundItem(
                        Item(8851, getTokenAmount(npc.id)), npc.position.copy(), player.username, false, 80, true, 80
                    )
                )
                armour = null
            }
        } else if (npc.id == 4291 && player.position.z == 2) {
            if (Misc.getRandom(20) <= 4) {
                GroundItemManager.spawnGroundItem(
                    player,
                    GroundItem(Item(getDefender(player)), npc.position.copy(), player.username, false, 100, false, -1)
                )
            }
        }
    }

    /**
     * Gets the player's best defender
     * @param player    The player to search items for
     * @return            The best defender's item id
     */
    fun getDefender(player: Player): Int {
        var foundIndex = -1
        for (i in DEFENDERS.indices) {
            if (player.inventory.contains(DEFENDERS[i]) || player.equipment.contains(DEFENDERS[i])) {
                foundIndex = i
            }
        }
        if (foundIndex != 7) {
            foundIndex++
        }
        return DEFENDERS[foundIndex]
    }

    /**
     * Warriors guild dialogue, handles what Kamfreena says.
     * @param Player        The player to show the dialogue for acording to their stats.
     */
    @JvmStatic
    fun warriorsGuildDialogue(player: Player): Dialogue {
        return object : Dialogue() {
            override fun type(): DialogueType {
                return DialogueType.NPC_STATEMENT
            }

            override fun animation(): DialogueExpression {
                return DialogueExpression.NORMAL
            }

            override fun dialogue(): Array<String> {
                val defender = getDefender(player)
                return arrayOf(
                    "I'll release some Cyclops which might drop",
                    "" + ItemDefinition.forId(defender).name.replace(" defender", "") + " Defenders for you.",
                    "Good luck warrior!"
                )
            }

            override fun npcId(): Int {
                return 2948
            }
        }
    }

    /**
     * The warriors guild task
     */
    @JvmStatic
    fun handleTokenRemoval(player: Player) {
        if (player.minigameAttributes.warriorsGuildAttributes.enteredTokenRoom()) return
        player.minigameAttributes.warriorsGuildAttributes.setEnteredTokenRoom(true)
        TaskManager.submit(object : Task(160, player, false) {
            public override fun execute() {
                if (!player.minigameAttributes.warriorsGuildAttributes.enteredTokenRoom()) {
                    stop()
                    return
                }
                if (player.location !== Locations.Location.WARRIORS_GUILD || player.position.z != 2) {
                    player.minigameAttributes.warriorsGuildAttributes.setEnteredTokenRoom(false)
                    stop()
                    return
                }
                if (player.inventory.contains(8851)) {
                    player.inventory.delete(8851, 10)
                    player.performGraphic(Graphic(1368))
                    player.packetSender.sendMessage("Some of your tokens crumble to dust..")
                } else {
                    player.minigameAttributes.warriorsGuildAttributes.setEnteredTokenRoom(false)
                    player.combatBuilder.cooldown(true)
                    player.movementQueue.reset()
                    player.moveTo(Position(2844, 3539, 2))
                    player.packetSender.sendMessage("You have run out of tokens.")
                    resetCyclopsCombat(player)
                    stop()
                }
            }
        })
    }

    /**
     * Gets the amount of tokens to receive from an npc
     * @param npc    The npc to check how many tokens to receive from
     * @return        The amount of tokens to receive as a drop
     */
    private fun getTokenAmount(npc: Int): Int {
        for (f in ANIMATED_ARMOUR_NPCS.indices) {
            if (npc == ANIMATED_ARMOUR_NPCS[f]) {
                return TOKEN_REWARDS[f]
            }
        }
        return 5
    }

    /**
     * Resets any cyclops's combat who are in combat with player
     * @param player    The player to check if cyclop is in combat with
     */
    @JvmStatic
    fun resetCyclopsCombat(player: Player) {
        for (n in player.localNpcs) {
            if (n == null) continue
            if (n.id == 4291 && n.location === Locations.Location.WARRIORS_GUILD && n.combatBuilder.victim != null && n.combatBuilder.victim === player) {
                n.combatBuilder.cooldown(true)
                n.movementQueue.reset()
            }
        }
    }
}