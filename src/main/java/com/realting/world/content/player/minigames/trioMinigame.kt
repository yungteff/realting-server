package com.realting.world.content.minigames

import com.realting.engine.task.Task
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
import com.realting.model.entity.character.player.Player
import com.realting.model.input.impl.EnterAmountToSellToShop
import com.realting.model.input.impl.EnterAmountToBuyFromShop
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

/**
 * Created by brandon on 5/3/2016.
 */
object trioMinigame {
    @JvmStatic
    fun handleTokenRemoval(player: Player) {
        if (player.minigameAttributes.trioAttuibutes.joinedBossRoom()) return
        player.minigameAttributes.trioAttuibutes.setJoinedBossRoom(true)
        //  player.getPacketSender().sendMessage("Sumbiting the task.");
        //task = 1 * 600 = taskDelay
        TaskManager.submit(object : Task(75, player, false) {
            public override fun execute() {
                if (!player.minigameAttributes.trioAttuibutes.joinedBossRoom()) {
                    //     handleNPCdeSpawning(player);
                    stop()
                    return
                    //fail safe
                }
                if (player.location !== Locations.Location.TRIO_ZONE) {
                    player.minigameAttributes.trioAttuibutes.setJoinedBossRoom(false)
                    stop()
                    return
                    // fail safe #2
                }
                if (player.inventory.contains(11180)) {
                    player.inventory.delete(11180, 1)
                    player.performGraphic(Graphic(1386))
                    player.packetSender.sendMessage("You've spent 1 minute in the trio arena, one of your tokens were destroyed!")
                } else {
                    player.combatBuilder.cooldown(true)
                    player.movementQueue.reset()
                    player.moveTo(
                        Position(
                            2902, 5204
                        )
                    ) // adam change this to where ever the npc you talk to to start minigame is.
                    player.minigameAttributes.trioAttuibutes.setJoinedBossRoom(false)
                    player.packetSender.sendMessage("You've run out of tokens.")
                    stop()
                }
            }
        })
    }
}