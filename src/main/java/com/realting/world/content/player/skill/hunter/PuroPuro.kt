package com.realting.world.content.player.skill.hunter

import com.realting.GameSettings
import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.engine.task.impl.NPCRespawnTask
import com.realting.model.*
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.npc.NPCMovementCoordinator
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import com.realting.world.World
import com.realting.world.content.player.events.Achievements
import com.realting.world.content.player.events.Achievements.AchievementData

object PuroPuro {
    val implings = arrayOf(
        intArrayOf(6055, 2612, 4318),
        intArrayOf(6055, 2602, 4314),
        intArrayOf(6055, 2610, 4338),
        intArrayOf(6055, 2582, 4344),
        intArrayOf(6055, 2578, 4344),
        intArrayOf(6055, 2568, 4311),
        intArrayOf(6055, 2583, 4295),
        intArrayOf(6055, 2582, 4330),
        intArrayOf(6055, 2600, 4303),
        intArrayOf(6055, 2611, 4301),
        intArrayOf(6055, 2618, 4329),
        intArrayOf(6056, 2591, 4332),
        intArrayOf(6056, 2600, 4338),
        intArrayOf(6056, 2595, 4345),
        intArrayOf(6056, 2610, 4327),
        intArrayOf(6056, 2617, 4314),
        intArrayOf(6056, 2619, 4294),
        intArrayOf(6056, 2599, 4294),
        intArrayOf(6056, 2575, 4303),
        intArrayOf(6056, 2570, 4299),
        intArrayOf(6057, 2573, 4339),
        intArrayOf(6057, 2567, 4328),
        intArrayOf(6057, 2593, 4297),
        intArrayOf(6057, 2618, 4305),
        intArrayOf(6057, 2605, 4316),
        intArrayOf(6057, 2596, 4333),
        intArrayOf(6058, 2592, 4338),
        intArrayOf(6058, 2611, 4345),
        intArrayOf(6058, 2617, 4339),
        intArrayOf(6058, 2614, 4301),
        intArrayOf(6058, 2606, 4295),
        intArrayOf(6058, 2581, 4299),
        intArrayOf(6059, 2602, 4328),
        intArrayOf(6059, 2608, 4333),
        intArrayOf(6059, 2609, 4296),
        intArrayOf(6059, 2581, 4304),
        intArrayOf(6059, 2570, 4318),
        intArrayOf(6060, 2611, 4310),
        intArrayOf(6060, 2617, 4319),
        intArrayOf(6060, 2600, 4347),
        intArrayOf(6060, 2570, 4326),
        intArrayOf(6060, 2579, 4310),
        intArrayOf(6061, 2581, 4310),
        intArrayOf(6061, 2581, 4310),
        intArrayOf(6061, 2603, 4333),
        intArrayOf(6061, 2576, 4335),
        intArrayOf(6061, 2588, 4345),
        intArrayOf(6062, 2612, 4324),
        intArrayOf(6062, 2602, 4323),
        intArrayOf(6062, 2587, 4348),
        intArrayOf(6062, 2564, 4320),
        intArrayOf(6062, 2566, 4295),
        intArrayOf(6063, 2570, 4347),
        intArrayOf(6063, 2572, 4327),
        intArrayOf(6063, 2578, 4318),
        intArrayOf(6063, 2610, 4312),
        intArrayOf(6063, 2594, 4341),
        intArrayOf(6064, 2613, 4341),
        intArrayOf(6064, 2585, 4337),
        intArrayOf(6064, 2576, 4319),
        intArrayOf(6064, 2576, 4294),
        intArrayOf(6064, 2592, 4305),
        intArrayOf(7903, 2566, 4294)
    )

    @JvmStatic
    fun spawn() {
        for (i in implings.indices) {
            val n = NPC(
                implings[i][0], Position(
                    implings[i][1], implings[i][2]
                )
            )
            n.movementCoordinator.coordinator = NPCMovementCoordinator.Coordinator().setCoordinate(true).setRadius(4)
            World.register(n)
        }
        /**
         * Kingly imps
         * Randomly spawned
         */
        val random = Misc.getRandom(6)
        var pos = Position(2596, 4351)
        when (random) {
            1 -> pos = Position(2620, 4348)
            2 -> pos = Position(2607, 4321)
            3 -> pos = Position(2588, 4289)
            4 -> pos = Position(2576, 4305)
        }
        val n = NPC(7903, pos)
        n.movementCoordinator.coordinator = NPCMovementCoordinator.Coordinator().setCoordinate(true).setRadius(4)
        World.register(n)
    }

    /**
     * Catches an Impling
     * @param player    The player catching an Imp
     * @param npc    The NPC (Impling) to catch
     */
    @JvmStatic
    fun catchImpling(player: Player?, imp: NPC?) {
        val implingData = ImpData.forId(imp!!.id)
        if (player!!.interfaceId > 0 || implingData == null || !imp.isRegistered || !player.clickDelay.elapsed(
                2000
            )
        ) return
        if (player.skillManager.getCurrentLevel(Skill.HUNTER) < implingData.levelReq) {
            player.packetSender.sendMessage("You need a Hunter level of at least " + implingData.levelReq + " to catch this impling.")
            return
        }
        if (!player.inventory.contains(10010) && !player.equipment.contains(10010)) {
            player.packetSender.sendMessage("You do not have any net to catch this impling with.")
            return
        }
        if (!player.inventory.contains(11260)) {
            player.packetSender.sendMessage("You do not have any empty jars to hold this impling with.")
            return
        }
        player.performAnimation(Animation(6605))
        val sucess = if (player.skillManager.getCurrentLevel(Skill.HUNTER) > 8) Misc.getRandom(
            player.skillManager.getCurrentLevel(Skill.HUNTER) / 2
        ) > 1 else true
        if (sucess) {
            if (imp.isRegistered) {
                World.deregister(imp)
                TaskManager.submit(NPCRespawnTask(imp, imp.definition.respawnTime))
                player.packetSender.sendMessage("You successfully catch the impling.")
                if (player.skillManager.skillCape(Skill.HUNTER)) {
                    player.inventory.delete(11260, 1).add(implingData.impJar, 2)
                    player.packetSender.sendMessage("Your cape gives you double the loot!")
                } else {
                    player.inventory.delete(11260, 1).add(implingData.impJar, 1)
                }
                player.skillManager.addExperience(
                    Skill.HUNTER,
                    (implingData.XPReward * GameSettings.BaseImplingExpMultiplier)
                )
                if (implingData == ImpData.YOUNG) Achievements.finishAchievement(
                    player,
                    AchievementData.CATCH_A_YOUNG_IMPLING
                ) else if (implingData == ImpData.KINGLY) {
                    Achievements.doProgress(player, AchievementData.CATCH_5_KINGLY_IMPLINGS)
                    Achievements.doProgress(player, AchievementData.CATCH_100_KINGLY_IMPLINGS)
                }
            }
        } else player.packetSender.sendMessage("You failed to catch the impling.")
        player.clickDelay.reset()
    }

    /**
     * Handles pushing through walls in Puro puro
     * @param player    The player pushing a wall
     */
    @JvmStatic
    fun goThroughWheat(player: Player, `object`: GameObject?) {
        if (!player.clickDelay.elapsed(2000)) return
        player.clickDelay.reset()
        val x = player.entityPosition.x
        var x2 = x
        val y = player.entityPosition.y
        var y2 = y
        if (x == 2584) {
            x2 = 2582
        } else if (x == 2582) {
            x2 = 2584
        } else if (x == 2599) {
            x2 = 2601
        } else if (x == 2601) {
            x2 = 2599
        }
        if (y == 4312) {
            y2 = 4310
        } else if (y == 4310) {
            y2 = 4312
        } else if (y == 4327) {
            y2 = 4329
        } else if (y == 4329) {
            y2 = 4327
        }
        x2 -= x
        y2 -= y
        player.packetSender.sendMessage("You use your strength to push through the wheat.")
        val goX = x2
        val goY = y2
        TaskManager.submit(object : Task(1, player, false) {
            var tick = 0
            override fun execute() {
                if (tick == 1) {
                    player.setSkillAnimation(6594).isCrossingObstacle = true
                    player.updateFlag.flag(Flag.APPEARANCE)
                    player.movementQueue.walkStep(goX, goY)
                } else if (tick == 2) stop()
                tick++
            }

            override fun stop() {
                setEventRunning(false)
                player.setSkillAnimation(-1).isCrossingObstacle = false
                player.updateFlag.flag(Flag.APPEARANCE)
            }
        })
    }

    /**
     * Handles Impling Jars looting
     * @param player    The player looting the jar
     * @param itemId    The jar the player is looting
     */
    @JvmStatic
    fun lootJar(player: Player?, jar: Item?, jarData: JarData?) {
        if (player == null || jar == null || jarData == null || !player.clickDelay.elapsed(2000)) return
        if (player.inventory.freeSlots < 2) {
            player.packetSender.sendMessage("You need at least 2 free inventory space to loot this.")
            return
        }
        player.inventory.delete(jar)
        player.inventory.add(11260, 1)
        val randomCommonItem = Misc.getRandom(JarData.getLootRarity(jarData, 0))
        val randomUncommonItem: Int =
            JarData.getLootRarity(jarData, 0) + Misc.getRandom(JarData.getLootRarity(jarData, 1))
        val randomRareItem: Int = JarData.getLootRarity(jarData, 2)
        val randomVeryRareItem: Int = JarData.getLootRarity(jarData, 3)
        var reward: Item? = null
        when (JarData.rar) {
            0 -> {
                reward = jarData.loot[randomCommonItem]
                if (reward != null) player.inventory.add(reward)
            }
            1 -> {
                reward = jarData.loot[randomUncommonItem]
                if (reward != null) player.inventory.add(reward)
            }
            2 -> {
                reward = jarData.loot[randomRareItem]
                if (reward != null) player.inventory.add(reward)
            }
            3 -> {
                reward = jarData.loot[randomVeryRareItem]
                if (reward != null) player.inventory.add(reward)
            }
        }
        var rewardName = reward!!.definition.name
        var s = Misc.anOrA(rewardName)
        if (reward.amount > 1) {
            s = "" + reward.amount + ""
            if (!rewardName.endsWith("s")) {
                if (rewardName.contains("potion")) {
                    val l = rewardName.substring(0, rewardName.indexOf(" potion"))
                    var l2 = rewardName.substring(rewardName.indexOf("potion"), 8)
                    l2 += if (rewardName.contains("(3)")) "(3)" else "(4)"
                    rewardName = "$l potions $l2"
                } else rewardName = rewardName + "s"
            }
        }
        player.packetSender.sendMessage("You loot the " + jar.definition.name + " and find " + s + " " + rewardName + ".")
        player.clickDelay.reset()
    }
}