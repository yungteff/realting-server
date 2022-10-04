package com.ruse.world.content.skill.thieving

import com.ruse.model.Animation
import com.ruse.model.Skill
import com.ruse.world.content.Achievements
import com.ruse.world.content.Achievements.AchievementData
import com.ruse.model.entity.character.player.Player

object Stalls {
    @JvmStatic
    fun stealFromStall(player: Player, lvlreq: Int, xp: Int, reward: Int, message: String?) {//legs
        when {
            player.inventory.freeSlots < 1 -> {
                player.packetSender.sendMessage("You need some more inventory space to do this.")
                return
            }
            player.combatBuilder.isBeingAttacked -> {
                player.packetSender.sendMessage("You must wait a few seconds after being out of combat before doing this.")
                return
            }
            !player.clickDelay.elapsed(2500) -> return
            player.skillManager.getMaxLevel(Skill.THIEVING) < lvlreq -> {
                player.packetSender.sendMessage("You need a Thieving level of at least $lvlreq to steal from this stall.")
                return
            }
            else -> {
                player.performAnimation(Animation(881))
                player.packetSender.sendInterfaceRemoval()
                player.skillManager.addExperience(Skill.THIEVING, xp)
                player.clickDelay.reset()
                if (player.skillManager.skillCape(Skill.THIEVING)) {
                    player.packetSender.sendMessage("Your cape quietly converts the stolen item into cash.")
                    when (reward) {
                        18199 -> { //banana
                            player.inventory.add(995, 1275)
                        }
                        15009 -> { //gold ring
                            player.inventory.add(995, 2763)
                        }
                        17401 -> { //hammer
                            player.inventory.add(995, 4888)
                        }
                        1389 -> { //staff
                            player.inventory.add(995, 6163)
                        }
                        11998 -> { //scimitar
                            player.inventory.add(995, 8713)
                        }
                        13003 -> { //rune gaunts
                            player.inventory.add(995, 3570)
                        }
                        4131 -> { //rune boots
                            player.inventory.add(995, 3910)
                        }
                        1113 -> { //chain
                            player.inventory.add(995, 8925)
                        }
                        1147 -> { //med helm
                            player.inventory.add(995, 5525)
                        }
                        1163 -> { //full helm
                            player.inventory.add(995, 17000)
                        }
                        1079 -> { //legs
                            player.inventory.add(995, 25500)
                        }
                        1201 -> { //kite
                            player.inventory.add(995, 21250)
                        }
                        1127 -> { //legs
                            player.inventory.add(995, 34000)
                        }
                        else -> {
                            player.packetSender.sendMessage(message)
                            player.inventory.add(reward, 1)
                        }
                    }
                } else {
                    player.packetSender.sendMessage(message)
                    player.inventory.add(reward, 1)
                }
                player.skillManager.stopSkilling()
                if (reward == 15009) Achievements.finishAchievement(
                    player, AchievementData.STEAL_A_RING
                ) else if (reward == 11998) {
                    Achievements.doProgress(player, AchievementData.STEAL_140_SCIMITARS)
                    Achievements.doProgress(player, AchievementData.STEAL_5000_SCIMITARS)
                }
            }
        }
    }
}