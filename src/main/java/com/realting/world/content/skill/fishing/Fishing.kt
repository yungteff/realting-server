package com.realting.world.content.skill.fishing

import com.realting.engine.task.Task
import com.realting.model.Skill
import java.util.Locale
import com.realting.world.content.Achievements
import com.realting.world.content.Achievements.AchievementData
import com.realting.engine.task.TaskManager
import com.realting.model.Animation
import com.realting.model.definitions.ItemDefinition
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc

object Fishing {
    @JvmStatic
    fun forSpot(npcId: Int, secondClick: Boolean): Spot? {
        for (s in Spot.values()) {
            if (secondClick) {
                if (s.second) {
                    if (s.nPCId == npcId) {
                        return s
                    }
                }
            } else {
                if (s.nPCId == npcId && !s.second) {
                    return s
                }
            }
        }
        return null
    }

    @JvmStatic
    fun setupFishing(p: Player, s: Spot?) {
        if (s == null) return
        if (p.inventory.freeSlots <= 0) {
            p.packetSender.sendMessage("You do not have any free inventory space.")
            p.skillManager.stopSkilling()
            return
        }
        if (p.skillManager.getCurrentLevel(Skill.FISHING) >= s.levelReq[0]) {
            if (p.inventory.contains(s.equipment) || p.skillManager.skillCape(Skill.FISHING)) {
                if (s.bait != -1) {
                    if (p.inventory.contains(s.bait)) {
                        startFishing(p, s)
                    } else {
                        var baitName = ItemDefinition.forId(s.bait).name
                        if (baitName.contains("Feather") || baitName.contains("worm")) baitName += "s"
                        p.packetSender.sendMessage("You need some $baitName to fish here.")
                        p.performAnimation(Animation(65535))
                    }
                } else {
                    startFishing(p, s)
                }
            } else {
                val def = ItemDefinition.forId(s.equipment).name.lowercase(Locale.getDefault())
                p.packetSender.sendMessage("You need " + Misc.anOrA(def) + " " + def + " to fish here.")
            }
        } else {
            p.packetSender.sendMessage("You need a fishing level of at least " + s.levelReq[0] + " to fish here.")
        }
    }

    fun startFishing(p: Player, s: Spot) {
        p.skillManager.stopSkilling()
        val fishIndex =
            if (Misc.getRandom(100) >= 70) getMax(p, s.levelReq) else if (getMax(p, s.levelReq) != 0) getMax(
                p, s.levelReq
            ) - 1 else 0
        //if(p.getInteractingObject() != null && p.getInteractingObject().getId() != 8702)
        //p.setDirection(s == Spot.MONK_FISH ? Direction.WEST : Direction.NORTH);
        p.performAnimation(Animation(s.anim))
        p.currentTask = object : Task(1, p, false) {
            var cycle = 0
            var animTick = 0
            public override fun execute() {
                if (p.inventory.freeSlots == 0) {
                    p.packetSender.sendMessage("You have run out of inventory space.")
                    stop()
                    return
                }
                if (!p.inventory.contains(s.bait)) {
                    stop()
                    return
                }
                if (++animTick == 2) {
                    p.performAnimation(Animation(s.anim))
                    animTick = 0
                }
                if (++cycle % 4 == 0 && p.skillManager.isSuccess(Skill.FISHING, s.levelReq[fishIndex])) {
                    var def = ItemDefinition.forId(s.rawFish[fishIndex]).name
                    if (def.endsWith("s")) def = def.substring(0, def.length - 1)
                    p.packetSender.sendMessage(
                        "You catch " + Misc.anOrA(def) + " " + def.lowercase(Locale.getDefault())
                            .replace("_", " ") + "."
                    )
                    if (s.bait != -1) p.inventory.delete(s.bait, 1)
                    p.inventory.add(s.rawFish[fishIndex], 1)
                    if (s.rawFish[fishIndex] == 331) {
                        //Achievements.finishAchievement(p, AchievementData.FISH_A_SALMON);
                    } else if (s.rawFish[fishIndex] == 15270) {
                        Achievements.doProgress(p, AchievementData.FISH_25_ROCKTAILS)
                        Achievements.doProgress(p, AchievementData.FISH_2000_ROCKTAILS)
                    } else if (s.rawFish[fishIndex] == 317) {
                        Achievements.doProgress(p, AchievementData.CATCH_25_SCHRIMPS)
                    }
                    p.skillManager.addExperience(Skill.FISHING, s.xp[fishIndex])
                    setupFishing(p, s)
                    setEventRunning(false)
                }
            }

            override fun stop() {
                setEventRunning(false)
                p.performAnimation(Animation(65535))
            }
        }
        TaskManager.submit(p.currentTask)
    }

    fun getMax(p: Player, reqs: IntArray): Int {
        var tempInt = -1
        for (i in reqs) {
            if (p.skillManager.getCurrentLevel(Skill.FISHING) >= i) {
                tempInt++
            }
        }
        return tempInt
    }

    private fun getDelay(req: Int): Int {
        var timer = 1
        timer += (req * 0.08).toInt()
        return timer
    }

    enum class Spot(
        var nPCId: Int,
        var rawFish: IntArray,
        var equipment: Int,
        var bait: Int,
        var levelReq: IntArray,
        var second: Boolean,
        var xp: IntArray,
        var anim: Int
    ) {
        LURE(318, intArrayOf(335, 331), 309, 314, intArrayOf(20, 30), true, intArrayOf(50, 70), 623), CAGE(
            312, intArrayOf(377), 301, -1, intArrayOf(40), false, intArrayOf(90), 619
        ),
        BIGNET(
            313, intArrayOf(353, 341, 363), 305, -1, intArrayOf(16, 23, 46), false, intArrayOf(20, 45, 100), 620
        ),
        SMALLNET(316, intArrayOf(317, 321), 303, -1, intArrayOf(1, 15), false, intArrayOf(10, 15), 621), MONK_FISH(
            318, intArrayOf(7944, 389), 305, -1, intArrayOf(62, 81), false, intArrayOf(120, 150), 621
        ),
        HARPOON(312, intArrayOf(359, 371), 311, -1, intArrayOf(35, 50), true, intArrayOf(80, 100), 618), HARPOON2(
            313, intArrayOf(383), 311, -1, intArrayOf(76), true, intArrayOf(110), 618
        ),
        BAIT(316, intArrayOf(327, 345), 307, 313, intArrayOf(5, 10), true, intArrayOf(20, 30), 623), ROCKTAIL(
            10091, intArrayOf(15270), 309, 25, intArrayOf(91), false, intArrayOf(200), 380
        );

    }
}