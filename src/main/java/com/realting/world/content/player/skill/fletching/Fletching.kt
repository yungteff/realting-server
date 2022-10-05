package com.realting.world.content.player.skill.fletching

import com.realting.engine.task.Task
import com.realting.model.input.impl.EnterAmountToFletch
import com.realting.model.Skill
import com.realting.engine.task.TaskManager
import com.realting.model.Animation
import com.realting.model.Item
import com.realting.model.definitions.ItemDefinition
import com.realting.model.entity.character.player.Player
import com.realting.model.input.impl.EnterGemAmount
import com.realting.model.input.impl.EnterAmountOfBowsToString
import com.realting.util.Misc
import com.realting.world.content.Achievements
import com.realting.world.content.Achievements.AchievementData
import com.realting.world.content.Sounds

/**
 * Handles the Fletching skill
 * @author Gabriel Hannason
 */
object Fletching {
    /**
     * Handles the Fletching interface
     * @param player    The player Fletching
     * @param log    The log to fletch
     */
    @JvmStatic
    fun openSelection(player: Player, log: Int) {
        player.skillManager.stopSkilling()
        player.selectedSkillingItem = log
        val shortBow = BowData.forLog(log, false)
        val longBow = BowData.forLog(log, true)
        if (shortBow == null || longBow == null) return
        if (log == 1511) {
            player.packetSender.sendChatboxInterface(8880)
            player.packetSender.sendInterfaceModel(8884, longBow.bowID, 250)
            player.packetSender.sendInterfaceModel(8883, shortBow.bowID, 250)
            player.packetSender.sendString(8889, "" + ItemDefinition.forId(shortBow.bowID).name + "")
            player.packetSender.sendString(8893, "" + ItemDefinition.forId(longBow.bowID).name + "")
            player.packetSender.sendString(8897, "Shafts")
            player.packetSender.sendInterfaceModel(8885, 52, 250)
        } else {
            player.packetSender.sendChatboxInterface(8866)
            player.packetSender.sendInterfaceModel(8870, longBow.bowID, 250)
            player.packetSender.sendInterfaceModel(8869, shortBow.bowID, 250)
            player.packetSender.sendString(8874, "" + ItemDefinition.forId(shortBow.bowID).name + "")
            player.packetSender.sendString(8878, "" + ItemDefinition.forId(longBow.bowID).name + "")
        }
        player.inputHandling = EnterAmountToFletch()
    }

    /**
     * Checks if a button that was clicked is from the Fletching interface
     * @param player    The Player clicking a button
     * The button the player clicked
     * @return
     */
    @JvmStatic
    fun fletchingButton(player: Player, button: Int): Boolean {
        when (button) {
            8889 -> {
                when (player.selectedSkillingItem) {
                    1511 -> {
                        fletchBow(player, 48, 1)
                        return true
                    }
                }
                return false
            }
            8888 -> {
                when (player.selectedSkillingItem) {
                    1511 -> {
                        fletchBow(player, 48, 5)
                        return true
                    }
                }
                return false
            }
            8887 -> {
                when (player.selectedSkillingItem) {
                    1511 -> {
                        fletchBow(player, 48, 10)
                        return true
                    }
                }
                return false
            }
            8893 -> {
                when (player.selectedSkillingItem) {
                    1511 -> {
                        fletchBow(player, 50, 1)
                        return true
                    }
                }
                return false
            }
            8892 -> {
                when (player.selectedSkillingItem) {
                    1511 -> {
                        fletchBow(player, 50, 5)
                        return true
                    }
                }
                return false
            }
            8891 -> {
                when (player.selectedSkillingItem) {
                    1511 -> {
                        fletchBow(player, 50, 10)
                        return true
                    }
                }
                return false
            }
            8874 -> {
                when (player.selectedSkillingItem) {
                    1521 -> {
                        fletchBow(player, 56, 1)
                        return true
                    }
                    1519 -> {
                        fletchBow(player, 58, 1)
                        return true
                    }
                    1517 -> {
                        fletchBow(player, 62, 1)
                        return true
                    }
                    1515 -> {
                        fletchBow(player, 66, 1)
                        return true
                    }
                    1513 -> {
                        fletchBow(player, 70, 1)
                        return true
                    }
                }
                return false
            }
            8873 -> {
                when (player.selectedSkillingItem) {
                    1521 -> {
                        fletchBow(player, 56, 5)
                        return true
                    }
                    1519 -> {
                        fletchBow(player, 58, 5)
                        return true
                    }
                    1517 -> {
                        fletchBow(player, 62, 5)
                        return true
                    }
                    1515 -> {
                        fletchBow(player, 66, 5)
                        return true
                    }
                    1513 -> {
                        fletchBow(player, 70, 5)
                        return true
                    }
                }
                return false
            }
            8872 -> {
                when (player.selectedSkillingItem) {
                    1521 -> {
                        fletchBow(player, 56, 10)
                        return true
                    }
                    1519 -> {
                        fletchBow(player, 58, 10)
                        return true
                    }
                    1517 -> {
                        fletchBow(player, 62, 10)
                        return true
                    }
                    1515 -> {
                        fletchBow(player, 66, 10)
                        return true
                    }
                    1513 -> {
                        fletchBow(player, 70, 10)
                        return true
                    }
                }
                return false
            }
            8878 -> {
                when (player.selectedSkillingItem) {
                    1521 -> {
                        fletchBow(player, 54, 1)
                        return true
                    }
                    1519 -> {
                        fletchBow(player, 60, 1)
                        return true
                    }
                    1517 -> {
                        fletchBow(player, 64, 1)
                        return true
                    }
                    1515 -> {
                        fletchBow(player, 68, 1)
                        return true
                    }
                    1513 -> {
                        fletchBow(player, 72, 1)
                        return true
                    }
                }
                return false
            }
            8877 -> {
                when (player.selectedSkillingItem) {
                    1521 -> {
                        fletchBow(player, 54, 5)
                        return true
                    }
                    1519 -> {
                        fletchBow(player, 60, 5)
                        return true
                    }
                    1517 -> {
                        fletchBow(player, 64, 5)
                        return true
                    }
                    1515 -> {
                        fletchBow(player, 68, 5)
                        return true
                    }
                    1513 -> {
                        fletchBow(player, 72, 5)
                        return true
                    }
                }
                return false
            }
            8876 -> {
                when (player.selectedSkillingItem) {
                    1521 -> {
                        fletchBow(player, 54, 10)
                        return true
                    }
                    1519 -> {
                        fletchBow(player, 60, 10)
                        return true
                    }
                    1517 -> {
                        fletchBow(player, 64, 10)
                        return true
                    }
                    1515 -> {
                        fletchBow(player, 68, 10)
                        return true
                    }
                    1513 -> {
                        fletchBow(player, 72, 10)
                        return true
                    }
                }
                return false
            }
            8897, 8896, 8895 -> {
                if (player.selectedSkillingItem == 1511) {
                    val amt = if (button == 8897) 1 else if (button == 8896) 5 else 10
                    fletchBow(player, 52, amt)
                    return true
                }
                return false
            }
        }
        return false
    }

    @JvmStatic
    fun fletchBow(player: Player, product: Int, amountToMake: Int) {
        player.packetSender.sendInterfaceRemoval()
        val log = player.selectedSkillingItem
        player.skillManager.stopSkilling()
        player.currentTask = object : Task(2, player, true) {
            var amount = 0
            public override fun execute() {
                val bow = BowData.forBow(product)
                val shafts = product == 52
                if (bow == null && !shafts || !player.inventory.contains(log)) {
                    player.performAnimation(Animation(65535))
                    stop()
                    return
                }
                if (bow != null && player.skillManager.getCurrentLevel(Skill.FLETCHING) < bow.levelReq) {
                    player.packetSender.sendMessage("You need a Fletching level of at least " + bow.levelReq + " to make this.")
                    player.performAnimation(Animation(65535))
                    stop()
                    return
                }
                if (!player.inventory.contains(946)) {
                    player.packetSender.sendMessage("You need a Knife to fletch this log.")
                    player.performAnimation(Animation(65535))
                    stop()
                    return
                }
                player.inventory.delete(log, 1)
                player.performAnimation(Animation(1248))
                if (player.skillManager.skillCape(Skill.FLETCHING) && Misc.getRandom(10) == 1 && !shafts) {
                    player.inventory.add(bow!!.fullBowId, 1)
                    player.packetSender.sendMessage("Your cape instantly carves and strings your log into a bow!")
                } else {
                    player.inventory.add(product, if (shafts) 15 else 1)
                }
                player.skillManager.addExperience(Skill.FLETCHING, if (shafts) 1 else bow!!.xp)
                Sounds.sendSound(player, Sounds.Sound.FLETCH_ITEM)
                amount++
                if (amount >= amountToMake) stop()
            }
        }
        TaskManager.submit(player.currentTask)
    }

    /**
     * Bolt tip creation
     */
    private const val chisel = 1755

    @JvmStatic
    fun openGemCrushingInterface(player: Player, gem: Int) {
        for (gd in GemData.values()) {
            if (gem == gd.gem) {
                if (player.skillManager.getMaxLevel(Skill.FLETCHING) < gd.levelReq) {
                    player.packetSender.sendMessage(
                        "You need a Fletching level of at least " + gd.levelReq + " to make " + ItemDefinition.forId(
                            gd.outcome
                        ).name + "."
                    )
                    return
                }
                if (!player.inventory.contains(gd.gem) || !player.inventory.contains(chisel)) {
                    return
                }
                //if gem player is using is equal to enum
                //System.out.println("Hello");
                player.skillManager.stopSkilling()
                player.selectedSkillingItem = gem
                player.inputHandling = EnterGemAmount()
                player.packetSender.sendString(2799, ItemDefinition.forId(gd.gem).name)
                    .sendInterfaceModel(1746, gd.gem, 150).sendChatboxInterface(4429)
                player.packetSender.sendString(2800, "How many would you like to make?")
            }
        }
    }

    @JvmStatic
    fun crushGems(player: Player, amount: Int, gemToCut: Int) {
        val gem = player.selectedSkillingItem
        player.skillManager.stopSkilling()
        player.packetSender.sendInterfaceRemoval()
        //System.out.println("crushgems method called");
        //System.out.println("gem used: " + ItemDefinition.forId(gem).getName());
        if (GemData.forGem(gemToCut) == null) {
            return
        }
        val gd = GemData.forGem(gemToCut)
        if (!player.inventory.contains(gem) || !player.inventory.contains(chisel)) {
            return
        }
        if (gd == null) {
            return
        }
        player.performAnimation(Animation(gd.animation.id))
        player.currentTask = object : Task(2, player, true) {
            var amountmade = 0
            public override fun execute() {
                if (!player.inventory.contains(gem) || !player.inventory.contains(chisel)) return
                player.performAnimation(Animation(gd.animation.id))
                player.inventory.delete(gem, 1)
                player.inventory.add(gd.outcome, gd.output)
                player.packetSender.sendMessage("You crush the " + ItemDefinition.forId(gem).name + ".")
                player.skillManager.addExperience(Skill.FLETCHING, gd.xp)
                amountmade++
                if (amountmade >= amount) stop()
            }
        }
        TaskManager.submit(player.currentTask)
    }

    @JvmStatic
    fun tipBolt(player: Player, tip: Int) {
        //	final int Bolt = player.getSelectedSkillingItem();
        player.skillManager.stopSkilling()
        player.packetSender.sendInterfaceRemoval()
        //System.out.println("tipBolt method called");
        //System.out.println("index: " + Bolt );
        //System.out.println("Bolt/tip used: " + ItemDefinition.forId(Bolt).getName());
        if (BoltData.forTip(tip) == null) {
            return
        }
        val bd = BoltData.forTip(tip)!!
        if (!player.inventory.contains(bd.bolt) || !player.inventory.contains(bd.tip)) {
            return
        }
        if (tip == bd.outcome) {
            return
        }
        if (player.skillManager.getCurrentLevel(Skill.FLETCHING) < bd.levelReq) {
            player.packetSender.sendMessage(
                "You need a Fletching level of at least " + bd.levelReq + " to make" + ItemDefinition.forId(
                    bd.outcome
                ).name
            )
        }
        if (player.inventory.freeSlots < 1 && !player.inventory.contains(bd.outcome)) {
            player.packetSender.sendMessage("You need at least 1 free inventory space.")
            return
        }
        player.packetSender.sendMessage("You begin fletching " + ItemDefinition.forId(bd.outcome).name + ".")
        TaskManager.submit(object : Task(1, player, false) {
            public override fun execute() {
                val tips = player.inventory.getAmount(bd.tip)
                val bolts = player.inventory.getAmount(bd.bolt)
                var toMake = 10
                if (tips < 10) {
                    toMake = tips
                }
                if (toMake > bolts) {
                    toMake = bolts
                }
                player.inventory.delete(bd.bolt, toMake)
                player.inventory.delete(bd.tip, toMake)
                player.inventory.add(bd.outcome, toMake)
                player.skillManager.addExperience(Skill.FLETCHING, bd.xp * toMake)
                stop()
            }
        })
    }

    /**
     * Bow stringing
     */
    private const val BOW_STRING = 1777

    @JvmStatic
    fun openBowStringSelection(player: Player, log: Int) {
        for (g in StringingData.values()) {
            if (log == g.unStrung()) {
                player.skillManager.stopSkilling()
                player.selectedSkillingItem = log
                player.inputHandling = EnterAmountOfBowsToString()
                player.packetSender.sendString(2799, ItemDefinition.forId(g.Strung()).name)
                    .sendInterfaceModel(1746, g.Strung(), 150).sendChatboxInterface(4429)
                player.packetSender.sendString(2800, "How many would you like to make?")
            }
        }
    }

    @JvmStatic
    fun stringBow(player: Player, amount: Int) {
        val log = player.selectedSkillingItem
        player.skillManager.stopSkilling()
        player.packetSender.sendInterfaceRemoval()
        for (g in StringingData.values()) {
            if (log == g.unStrung()) {
                if (player.skillManager.getCurrentLevel(Skill.FLETCHING) < g.level) {
                    player.packetSender.sendMessage("You need a Fletching level of at least " + g.level + " to make this.")
                    return
                }
                if (!player.inventory.contains(log) || !player.inventory.contains(BOW_STRING)) return
                player.performAnimation(Animation(g.animation))
                player.currentTask = object : Task(2, player, false) {
                    var amountMade = 0
                    public override fun execute() {
                        if (!player.inventory.contains(log) || !player.inventory.contains(BOW_STRING)) return
                        player.inventory.delete(BOW_STRING, 1)
                        player.inventory.delete(log, 1)
                        player.inventory.add(g.Strung(), 1)
                        player.packetSender.sendMessage("You attach the Bow string on to the bow.")
                        player.skillManager.addExperience(Skill.FLETCHING, g.xP.toInt())
                        amountMade++
                        if (amountMade >= amount) stop()
                    }
                }
                TaskManager.submit(player.currentTask)
                break
            }
        }
    }

    /**
     * Arrows making
     */
    fun getPrimary(item1: Int, item2: Int): Int {
        return if (item1 == 52 || item1 == 53) item2 else item1
    }

    @JvmStatic
    fun makeArrows(player: Player, item1: Int, item2: Int) {
        player.skillManager.stopSkilling()
        val arr = ArrowData.forArrow(getPrimary(item1, item2))
        if (arr != null) {
            if (player.skillManager.getCurrentLevel(Skill.FLETCHING) >= arr.levelReq) {
                if (player.inventory.getAmount(arr.item1) >= 15 && player.inventory.getAmount(arr.item2) >= 15) {
                    player.inventory.delete(
                        Item(arr.item1).setAmount(15), player.inventory.getSlot(arr.item1), true
                    )
                    player.inventory.delete(
                        Item(arr.item2).setAmount(15), player.inventory.getSlot(arr.item2), true
                    )
                    player.inventory.add(arr.outcome, 15)
                    player.skillManager.addExperience(Skill.FLETCHING, arr.xp as Int)
                    Achievements.finishAchievement(player, AchievementData.FLETCH_SOME_ARROWS)
                    if (arr == ArrowData.RUNE) {
                        Achievements.doProgress(player, AchievementData.FLETCH_450_RUNE_ARROWS, 15)
                        Achievements.doProgress(player, AchievementData.FLETCH_5000_RUNE_ARROWS, 15)
                    }
                } else {
                    player.packetSender.sendMessage("You must have at least 15 of each supply to make arrows.")
                }
            } else {
                player.packetSender.sendMessage("You need a Fletching level of at least " + arr.levelReq + " to fletch this.")
            }
        }
    }
}