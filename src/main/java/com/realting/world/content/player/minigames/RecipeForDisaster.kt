package com.realting.world.content.minigames

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.Item
import com.realting.model.Locations
import com.realting.model.Position
import com.realting.model.RegionInstance
import com.realting.model.RegionInstance.RegionInstanceType
import com.realting.model.container.impl.Shop
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.model.input.impl.EnterAmountToBuyFromShop
import com.realting.model.input.impl.EnterAmountToSellToShop
import com.realting.world.World
import com.realting.world.content.PlayerPanel
import com.realting.world.content.combat.prayer.CurseHandler
import com.realting.world.content.combat.prayer.PrayerHandler
import com.realting.world.content.dialogue.DialogueManager

/**
 * @author Gabriel Hannason
 * Wrote this quickly!!
 * Handles the RFD quest
 */
object RecipeForDisaster {
    @JvmStatic
    fun enter(player: Player) {
        if (player.minigameAttributes.recipeForDisasterAttributes.wavesCompleted == 6) return
        player.moveTo(Position(1900, 5346, (player.index + 1) * 4 + 2))
        player.regionInstance = RegionInstance(player, RegionInstanceType.RECIPE_FOR_DISASTER)
        spawnWave(player, player.minigameAttributes.recipeForDisasterAttributes.wavesCompleted)
        CurseHandler.deactivateAll(player)
        PrayerHandler.deactivateAll(player)
    }

    @JvmStatic
    fun leave(player: Player?) {
        Locations.Location.RECIPE_FOR_DISASTER.leave(player!!)
    }

    fun spawnWave(p: Player, wave: Int) {
        if (wave > 5 || p.regionInstance == null) return
        TaskManager.submit(object : Task(2, p, false) {
            public override fun execute() {
                if (p.regionInstance == null) {
                    stop()
                    return
                }
                val npc = if (wave >= 5) 3491 else 3493 + wave
                val n = NPC(npc, Position(spawnPos.x, spawnPos.y, p.entityPosition.z)).setSpawnedFor(p)
                World.register(n)
                p.regionInstance.npcsList.add(n)
                n.combatBuilder.attack(p)
                stop()
            }
        })
    }

    @JvmStatic
    fun handleNPCDeath(player: Player, n: NPC) {
        if (player.regionInstance == null) return
        player.regionInstance.npcsList.remove(n)
        player.minigameAttributes.recipeForDisasterAttributes.wavesCompleted =
            player.minigameAttributes.recipeForDisasterAttributes.wavesCompleted + 1
        when (n.id) {
            3493, 3494, 3495, 3496, 3497 -> {
                val index = n.id - 3490
                player.minigameAttributes.recipeForDisasterAttributes.setPartFinished(index, true)
            }
            3491 -> {
                player.minigameAttributes.recipeForDisasterAttributes.setPartFinished(8, true)
                player.moveTo(Position(3081, 3500, 0))
                player.restart()
                DialogueManager.start(player, 46)
                PlayerPanel.refreshPanel(player)
            }
        }
        if (player.location !== Locations.Location.RECIPE_FOR_DISASTER || player.minigameAttributes.recipeForDisasterAttributes.wavesCompleted == 6) return
        TaskManager.submit(object : Task(3, player, false) {
            public override fun execute() {
                stop()
                if (player.location !== Locations.Location.RECIPE_FOR_DISASTER || player.minigameAttributes.recipeForDisasterAttributes.wavesCompleted == 6) return
                spawnWave(player, player.minigameAttributes.recipeForDisasterAttributes.wavesCompleted)
            }
        })
    }

    fun getQuestTabPrefix(player: Player): String {
        if (player.minigameAttributes.recipeForDisasterAttributes.hasFinishedPart(0) && player.minigameAttributes.recipeForDisasterAttributes.wavesCompleted < 6) {
            return "@yel@"
        }
        return if (player.minigameAttributes.recipeForDisasterAttributes.wavesCompleted == 6) {
            "@gre@"
        } else "@red@"
    }

    @JvmStatic
    fun openQuestLog(p: Player) {
        for (i in 8145..8195) p.packetSender.sendString(i, "")
        p.packetSender.sendInterface(8134)
        p.packetSender.sendString(8136, "Close window")
        p.packetSender.sendString(8144, "" + questTitle)
        p.packetSender.sendString(8145, "")
        var questIntroIndex = 0
        for (i in 8147 until 8147 + questIntro.size) {
            p.packetSender.sendString(i, "@dre@" + questIntro[questIntroIndex])
            questIntroIndex++
        }
        var questGuideIndex = 0
        for (i in 8147 + questIntro.size until 8147 + questIntro.size + questGuide.size) {
            if (!p.minigameAttributes.recipeForDisasterAttributes.hasFinishedPart(questGuideIndex)) p.packetSender.sendString(
                i, "" + questGuide[questGuideIndex]
            ) else p.packetSender.sendString(i, "@str@" + questGuide[questGuideIndex] + "")
            if (questGuideIndex == 2) {
                if (p.minigameAttributes.recipeForDisasterAttributes.wavesCompleted > 0 && !p.minigameAttributes.recipeForDisasterAttributes.hasFinishedPart(
                        questGuideIndex
                    )
                ) p.packetSender.sendString(i, "@yel@" + questGuide[questGuideIndex])
                if (p.minigameAttributes.recipeForDisasterAttributes.wavesCompleted == 6) p.packetSender.sendString(
                    i, "@str@" + questGuide[questGuideIndex] + ""
                )
            }
            questGuideIndex++
        }
        if (p.minigameAttributes.recipeForDisasterAttributes.wavesCompleted == 6) p.packetSender.sendString(
            8147 + questIntro.size + questGuide.size, "@dre@Quest complete!"
        )
    }

    @JvmStatic
    fun openRFDShop(player: Player) {
        var stock: IntArray? = IntArray(10)
        var stockAmount: IntArray? = IntArray(10)
        for (i in stock!!.indices) {
            stock[i] = -1
            stockAmount!![i] = 2000000000
        }
        for (i in 0..player.minigameAttributes.recipeForDisasterAttributes.wavesCompleted) {
            when (i) {
                1 -> stock[0] = 7453
                2 -> {
                    stock[1] = 7454
                    stock[2] = 7455
                }
                3 -> {
                    stock[3] = 7456
                    stock[4] = 7457
                }
                4 -> stock[5] = 7458
                5 -> {
                    stock[6] = 7459
                    stock[7] = 7460
                }
                6 -> {
                    stock[8] = 7461
                    stock[9] = 7462
                }
            }
        }
        var stockItems: Array<Item?>? = arrayOfNulls(stock.size)
        for (i in stock.indices) stockItems!![i] = Item(stock[i], stockAmount!![i])
        val shop = Shop(player, Shop.RECIPE_FOR_DISASTER_STORE, "Culinaromancer's chest", Item(995), stockItems)
        stockAmount = null
        stock = stockAmount
        stockItems = null
        shop.player = player
        player.packetSender.sendItemContainer(player.inventory, Shop.INVENTORY_INTERFACE_ID)
        player.packetSender.sendItemContainer(shop, Shop.ITEM_CHILD_ID)
        player.packetSender.sendString(Shop.NAME_INTERFACE_CHILD_ID, "Culinaromancer's chest")
        if (player.inputHandling == null || !(player.inputHandling is EnterAmountToSellToShop || player.inputHandling is EnterAmountToBuyFromShop)) player.packetSender.sendInterfaceSet(
            Shop.INTERFACE_ID, Shop.INVENTORY_INTERFACE_ID - 1
        )
        player.setShop(shop).setInterfaceId(Shop.INTERFACE_ID).isShopping = true
    }

    private val spawnPos = Position(1900, 5354)
    private const val questTitle = "Recipe for Disaster"
    private val questIntro = arrayOf(
        "The Culinaromancer has returned and only you", "             can stop him!                  ", ""
    )
    private val questGuide = arrayOf(
        "Talk to the Gypsy in Edgeville and agree to help her.",
        "Enter the portal.",
        "Defeat the following servants:",
        "* Agrith-Na-Na",
        "* Flambeed",
        "* Karamel",
        "* Dessourt",
        "* Gelatinnoth mother",
        "And finally.. Defeat the Culinaromancer!"
    )
}