package com.realting.world.content.player.skill.woodcutting

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.Animation
import com.realting.model.GameObject
import com.realting.model.Skill
import com.realting.model.container.impl.Equipment
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import com.realting.world.content.CustomObjects
import com.realting.world.content.Sounds
import com.realting.world.content.player.events.Achievements
import com.realting.world.content.player.events.Achievements.AchievementData
import com.realting.world.content.player.skill.firemaking.Logdata
import com.realting.world.content.player.skill.firemaking.Logdata.logData
import com.realting.world.content.player.skill.woodcutting.WoodcuttingData.Hatchet
import com.realting.world.content.randomevents.EvilTree
import com.realting.world.content.randomevents.EvilTree.EvilTreeDef

object Woodcutting {
    @JvmStatic
    fun cutWood(player: Player, `object`: GameObject, restarting: Boolean) {
        if (!restarting) player.skillManager.stopSkilling()
        if (player.inventory.freeSlots == 0) {
            player.packetSender.sendMessage("You don't have enough free inventory space.")
            return
        }
        player.positionToFace = `object`.entityPosition
        val objId = `object`.id
        val h: Hatchet? = Hatchet.Companion.forId(WoodcuttingData.getHatchet(player))
        if (h != null) {
            if (player.skillManager.getCurrentLevel(Skill.WOODCUTTING) >= h.requiredLevel) {
                val t: WoodcuttingData.Trees? = WoodcuttingData.Trees.Companion.forId(objId)
                val t2 = EvilTreeDef.forId(objId)
                val isEvilTree = t2 != null
                if (isEvilTree) {
                    //player.getPacketSender().sendMessage("Evil tree method.");
                    EvilTree.handleCutWood(player, `object`, h, t2)
                    return
                }
                if (t != null) {
                    player.setEntityInteraction(`object`)
                    if (player.skillManager.getCurrentLevel(Skill.WOODCUTTING) >= t.req) {
                        player.performAnimation(Animation(h.anim))
                        //int delay = Misc.getRandom(t.getTicks() - WoodcuttingData.getChopTimer(player, h)) +1;
                        player.currentTask = object : Task(1, player, false) {
                            var cycle = 0
                            var reqCycle = 3
                            public override fun execute() {
                                if (player.inventory.freeSlots == 0) {
                                    player.performAnimation(Animation(65535))
                                    player.packetSender.sendMessage("You don't have enough free inventory space.")
                                    stop()
                                    return
                                }
                                player.performAnimation(Animation(h.anim))
                                if (++cycle % 4 == 0 && player.skillManager.isSuccess(
                                        Skill.WOODCUTTING,
                                        if (isEvilTree) t2!!.woodcuttingLevel else t.reward,
                                        h.requiredLevel
                                    )
                                ) {
                                    var xp = if (isEvilTree) t2!!.woodcuttingXp else t.xp
                                    if (lumberJack(player)) xp *= 1.5.toInt()
                                    player.skillManager.addExperience(Skill.WOODCUTTING, xp)
                                    BirdNests.dropNest(player)
                                    stop()
                                    val cutDownRandom = Misc.getRandom(100)
                                    //	player.getPacketSender().sendMessage("Random: " + cutDownRandom);
                                    if (!isEvilTree && (!t.isMulti || player.skillManager.skillCape(Skill.WOODCUTTING) && cutDownRandom >= 88 || !player.skillManager.skillCape(
                                            Skill.WOODCUTTING
                                        ) && cutDownRandom >= 82)
                                    ) { //82
                                        //player.getPacketSender().sendMessage("You rolled a: "+cutDownRandom);
                                        player.inventory.add(if (isEvilTree) t2!!.log else t.reward, 1)
                                        treeRespawn(player, `object`)
                                        player.packetSender.sendMessage("You've chopped the tree down.")
                                        player.performAnimation(Animation(65535))
                                    } else { //if they didn't cut down the tree
                                        cutWood(player, `object`, true)
                                        if (player.skillManager.skillCape(Skill.WOODCUTTING) && cutDownRandom >= 82 && cutDownRandom < 87) {
                                            player.packetSender.sendMessage("Your cape helps keep the tree alive a little longer.")
                                        }
                                        if (infernoAdze(player)) { //if they do not have an adze equipped
                                            if (Misc.getRandom(10) <= 6) {
                                                val fmLog =
                                                    Logdata.getLogData(player, if (isEvilTree) t2!!.log else t.reward)
                                                if (fmLog != null) { //if their their logdata is not null...
                                                    player.skillManager.addExperience(Skill.FIREMAKING, fmLog.xp)
                                                    player.packetSender.sendMessage("You chop a log, and your Inferno Adze burns it into ash.")
                                                    if (fmLog == logData.MAGIC) {
                                                        Achievements.doProgress(
                                                            player, AchievementData.BURN_100_MAGIC_LOGS
                                                        )
                                                        Achievements.doProgress(
                                                            player, AchievementData.BURN_2500_MAGIC_LOGS
                                                        )
                                                    }
                                                } else { //if the fmLog data is null
                                                    player.packetSender.sendMessage("<col=b40404>The game thinks you have an adze, but are burning nothing.")
                                                        .sendMessage("<col=b40404>Please contact Crimson and report this bug.")
                                                }
                                            } else {
                                                player.inventory.add(t.reward, 1)
                                                player.packetSender.sendMessage("You get some logs...")
                                            }
                                        } else { //if they player doesn't have an adze, do this.
                                            player.inventory.add(t.reward, 1)
                                            player.packetSender.sendMessage("You get some logs...")
                                        }
                                    }
                                    Sounds.sendSound(player, Sounds.Sound.WOODCUT)
                                    if (t == WoodcuttingData.Trees.NORMAL) {
                                        Achievements.doProgress(player, AchievementData.CUT_10_LOGS)
                                    } else if (t == WoodcuttingData.Trees.MAGIC) {
                                        Achievements.doProgress(player, AchievementData.CUT_100_MAGIC_LOGS)
                                        Achievements.doProgress(player, AchievementData.CUT_5000_MAGIC_LOGS)
                                    }
                                }
                            }
                        }
                        TaskManager.submit(player.currentTask)
                    } else {
                        player.packetSender.sendMessage("You need a Woodcutting level of at least " + t.req + " to cut this tree.")
                    }
                }
            } else {
                player.packetSender.sendMessage("You do not have a hatchet which you have the required Woodcutting level to use.")
            }
        } else {
            player.packetSender.sendMessage("You do not have a hatchet that you can use.")
        }
    }

    @JvmStatic
    fun lumberJack(player: Player): Boolean {
        return player.equipment[Equipment.HEAD_SLOT].id == 10941 && player.equipment[Equipment.BODY_SLOT].id == 10939 && player.equipment[Equipment.LEG_SLOT].id == 10940 && player.equipment[Equipment.FEET_SLOT].id == 10933
    }

    fun infernoAdze(player: Player): Boolean {
        return player.equipment[Equipment.WEAPON_SLOT].id == 13661
    }

    fun treeRespawn(player: Player, oldTree: GameObject?) {
        if (oldTree == null || oldTree.pickAmount >= 10) return
        oldTree.pickAmount = 10
        for (players in player.localPlayers) {
            if (players == null) continue
            if (players.interactingObject != null && players.interactingObject.entityPosition == player.interactingObject.entityPosition.copy()) {
                players.skillManager.stopSkilling()
                players.packetSender.sendClientRightClickRemoval()
            }
        }
        player.packetSender.sendClientRightClickRemoval()
        player.skillManager.stopSkilling()
        CustomObjects.globalObjectRespawnTask(
            GameObject(1343, oldTree.entityPosition.copy(), 10, 0), oldTree, 20 + Misc.getRandom(10)
        )
    }
}