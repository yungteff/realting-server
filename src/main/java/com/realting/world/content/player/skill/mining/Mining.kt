package com.realting.world.content.player.skill.mining

import com.realting.engine.task.Task
import com.realting.world.content.player.skill.smithing.SmithingData.hasOres
import com.realting.world.content.player.skill.smithing.Smelting.handleBarCreation
import com.realting.world.content.player.skill.mining.MiningData.Ores
import com.realting.world.content.player.skill.mining.MiningData.Pickaxe
import com.realting.world.World
import com.realting.world.content.Achievements
import com.realting.world.content.Achievements.AchievementData
import com.realting.world.content.randomevents.ShootingStar
import com.realting.engine.task.TaskManager
import com.realting.model.*
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import com.realting.world.content.CustomObjects
import com.realting.world.content.Sounds

object Mining {
    @JvmStatic
    fun startMining(player: Player, oreObject: GameObject) {
        player.skillManager.stopSkilling()
        player.packetSender.sendInterfaceRemoval()
        if (!Locations.goodDistance(
                player.position.copy(),
                oreObject.position,
                1
            ) && oreObject.id != 24444 && oreObject.id != 24445 && oreObject.id != 38660
        ) return
        if (player.busy() || player.combatBuilder.isBeingAttacked || player.combatBuilder.isAttacking) {
            player.packetSender.sendMessage("You cannot do that right now.")
            return
        }
        if (player.inventory.freeSlots == 0) {
            player.packetSender.sendMessage("You do not have any free inventory space left.")
            return
        }
        player.interactingObject = oreObject
        player.positionToFace = oreObject.position
        val o = MiningData.forRock(oreObject.id)
        val giveGem = o != Ores.Rune_essence && o != Ores.Pure_essence
        val reqCycle = if (o == Ores.Runite) 6 + Misc.getRandom(2) else Misc.getRandom(o!!.ticks - 1)
        val pickaxe = MiningData.getPickaxe(player)
        val miningLevel = player.skillManager.getCurrentLevel(Skill.MINING)
        if (pickaxe > 0) {
            if (miningLevel >= o.levelReq) {
                val p = MiningData.forPick(pickaxe)
                if (miningLevel >= p!!.req) {
                    if (MiningData.isHoldingPickaxe(player)) {
                        player.performAnimation(Animation(12003))
                    } else {
                        player.performAnimation(Animation(12003))
                    }
                    //final int delay = o.getTicks() - MiningData.getReducedTimer(player, p);
                    player.currentTask = object : Task(1, player, false) {
                        var cycle = 0
                        public override fun execute() {
                            if (player.interactingObject == null || player.interactingObject.id != oreObject.id) {
                                player.skillManager.stopSkilling()
                                player.performAnimation(Animation(65535))
                                stop()
                                return
                            }
                            if (player.inventory.freeSlots == 0) {
                                player.performAnimation(Animation(65535))
                                stop()
                                player.packetSender.sendMessage("You do not have any free inventory space left.")
                                return
                            }
                            if (++cycle % 7 == 0) {
                                if (MiningData.isHoldingPickaxe(player)) {
                                    player.performAnimation(Animation(12003))
                                } else {
                                    player.performAnimation(Animation(12003))
                                }
                            }
                            if (cycle % 4 == 0 && player.skillManager.isSuccess(Skill.MINING, o.levelReq, p.req)) {
                                if (giveGem) {
                                    val onyx =
                                        (o == Ores.Runite || o == Ores.CRASHED_STAR) && Misc.getRandom(if (o == Ores.CRASHED_STAR) 20000 else 5000) == 1
                                    if (onyx || Misc.getRandom(if (o == Ores.CRASHED_STAR) 35 else 50) == 15) {
                                        val gemId =
                                            if (onyx) 6571 else MiningData.RANDOM_GEMS[(MiningData.RANDOM_GEMS.size * Math.random()).toInt()]
                                        if (player.skillManager.skillCape(Skill.MINING) && player.gameMode != GameMode.ULTIMATE_IRONMAN) {
                                            if (player.getBank(0).isFull) {
                                                player.packetSender.sendMessage("Your cape failed at trying to bank your gem, because your bank was full.")
                                                player.inventory.add(gemId, 1)
                                            } else {
                                                player.packetSender.sendMessage("Your cape banked a gem while you were mining!")
                                                player.getBank(0).add(gemId, 1)
                                            }
                                        } else {
                                            if (player.gameMode == GameMode.ULTIMATE_IRONMAN) {
                                                player.inventory.add(gemId + 1, 1)
                                                //player.getPacketSender().sendMessage("As a UIM, your cape won't bank gems, but here's a noted one.");
                                            } else {
                                                player.inventory.add(gemId, 1)
                                                player.packetSender.sendMessage("You've found a gem!")
                                            }
                                        }
                                        if (gemId == 6571) {
                                            val s = if (o == Ores.Runite) "Runite ore" else "Crashed star"
                                            World.sendMessage("<img=10><col=009966><shad=0> " + player.username + " has just received an Uncut Onyx from mining a " + s + "!")
                                        }
                                    }
                                }
                                if (o == Ores.Iron) {
                                    Achievements.doProgress(player, AchievementData.MINE_SOME_IRON)
                                } else if (o == Ores.Runite) {
                                    Achievements.doProgress(player, AchievementData.MINE_25_RUNITE_ORES)
                                    Achievements.doProgress(player, AchievementData.MINE_2000_RUNITE_ORES)
                                }
                                if (o.itemId != -1) {
                                    if (o == Ores.Coal && player.skillManager.skillCape(Skill.MINING) && Misc.getRandom(
                                            3
                                        ) == 1
                                    ) {
                                        player.inventory.add(o.itemId, 2)
                                        player.packetSender.sendMessage("Your cape allows you to mine an additional coal.")
                                    } else {
                                        player.inventory.add(o.itemId, 1)
                                    }
                                }
                                player.skillManager.addExperience(Skill.MINING, o.xpAmount)
                                if (o == Ores.CRASHED_STAR) {
                                    player.packetSender.sendMessage("You mine the crashed star..")
                                } else {
                                    player.packetSender.sendMessage("You mine some ore.")
                                }
                                /** ADZE EFFECT  */
                                if (pickaxe == Pickaxe.Adze.id) {
                                    if (Misc.getRandom(100) >= 75) {
                                        when (o) {
                                            Ores.Adamantite -> handleAdze(player, oreObject, 2361)
                                            Ores.Gold -> handleAdze(player, oreObject, 2357)
                                            Ores.Iron -> handleAdze(player, oreObject, 2351)
                                            Ores.Mithril -> handleAdze(player, oreObject, 2359)
                                            Ores.Runite -> handleAdze(player, oreObject, 2363)
                                            Ores.Silver -> handleAdze(player, oreObject, 2355)
                                            Ores.Tin, Ores.Copper -> handleAdze(player, oreObject, 2349)
                                        }
                                    }
                                }
                                Sounds.sendSound(player, Sounds.Sound.MINE_ITEM)
                                stop()
                                if (o.respawn > 0) {
                                    player.performAnimation(Animation(65535))
                                    oreRespawn(player, oreObject, o)
                                } else {
                                    if (oreObject.id == 38660) {
                                        if (ShootingStar.CRASHED_STAR == null || ShootingStar.CRASHED_STAR.starObject.pickAmount >= ShootingStar.MAXIMUM_MINING_AMOUNT) {
                                            player.packetSender.sendClientRightClickRemoval()
                                            player.skillManager.stopSkilling()
                                            return
                                        } else {
                                            ShootingStar.CRASHED_STAR.starObject.incrementPickAmount()
                                        }
                                    } else {
                                        player.performAnimation(Animation(65535))
                                    }
                                    startMining(player, oreObject)
                                }
                            }
                        }
                    }
                    TaskManager.submit(player.currentTask)
                } else {
                    player.packetSender.sendMessage("You need a Mining level of at least " + p.req + " to use this pickaxe.")
                }
            } else {
                player.packetSender.sendMessage("You need a Mining level of at least " + o.levelReq + " to mine this rock.")
            }
        } else {
            player.packetSender.sendMessage("You don't have a pickaxe to mine this rock with.")
        }
    }

    fun handleAdze(player: Player, oreObject: GameObject, barId: Int) {
        if (hasOres(player, barId)) {
            handleBarCreation(barId, player)
            player.packetSender.sendMessage("The heat from your Inferno adze immediately ignites the ore and smelts it.")
            player.interactingObject = oreObject
            player.positionToFace = oreObject.position
            oreObject.performGraphic(Graphic(453))
            player.interactingObject = oreObject
            player.positionToFace = oreObject.position
        }
    }

    fun oreRespawn(player: Player, oldOre: GameObject?, o: Ores) {
        if (oldOre == null || oldOre.pickAmount >= 1) return
        oldOre.pickAmount = 1
        for (players in player.localPlayers) {
            if (players == null) continue
            if (players.interactingObject != null && players.interactingObject.position == player.interactingObject.position.copy()) {
                players.packetSender.sendClientRightClickRemoval()
                players.skillManager.stopSkilling()
            }
        }
        player.packetSender.sendClientRightClickRemoval()
        player.skillManager.stopSkilling()
        CustomObjects.globalObjectRespawnTask(GameObject(452, oldOre.position.copy(), 10, 0), oldOre, o.respawn)
    }
}