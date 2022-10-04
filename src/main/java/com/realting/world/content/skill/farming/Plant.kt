package com.realting.world.content.skill.farming

import com.realting.engine.task.Task
import java.util.Calendar
import com.realting.model.container.impl.Equipment
import com.realting.engine.task.TaskManager
import com.realting.model.Animation
import com.realting.model.Item
import com.realting.model.Skill
import com.realting.model.definitions.ItemDefinition
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import com.realting.world.content.Achievements
import com.realting.world.content.Achievements.AchievementData

class Plant(var patch: Int, var plant: Int) {
    var minute = 0
    var hour = 0
    var day = 0
    var year = 0
    var stage: Byte = 0
    var disease: Byte = -1
    var watered: Byte = 0
    private val dead = false
    var harvested: Byte = 0
    var harvesting = false
    fun doDisease(): Boolean {
        return false
    }

    fun doWater(): Boolean {
        return false
    }

    fun water(player: Player, item: Int) {
        if (item == 5332) {
            return
        }
        if (player.clickDelay.elapsed(2000)) {
            if (isWatered()) {
                player.packetSender.sendMessage("Your plants have already been watered.")
                return
            }
            if (item == 5331) {
                player.packetSender.sendMessage("Your watering can is empty.")
                return
            }
            player.packetSender.sendMessage("You water the plant.")
            player.farming.nextWateringCan(item)
            player.performAnimation(Animation(2293))
            watered = -1
            doConfig(player)
            player.clickDelay.reset()
        }
    }

    fun setTime() {
        minute = Calendar.getInstance()[12]
        hour = Calendar.getInstance()[11]
        day = Calendar.getInstance()[6]
        year = Calendar.getInstance()[1]
    }

    fun click(player: Player, option: Int) {
        if (option == 1) {
            if (dead) player.packetSender.sendMessage("Oh dear, your plants have died!") else if (isDiseased) player.packetSender.sendMessage(
                "Your plants are diseased!"
            ) else if (stage == Plants.values()[plant].stages) harvest(player) else {
                var s = "Your plants are healthy"
                s += if (!isWatered()) " but need some water to survive" else " and are currently growing"
                s += "."
                player.packetSender.sendMessage(s)
            }
        } else if (option == 2 && stage == Plants.values()[plant].stages) player.packetSender.sendMessage("Your plants are healthy and ready to harvest.")
    }

    fun harvest(player: Player) {
        if (harvesting) return
        val magicSecateurs = player.equipment[Equipment.WEAPON_SLOT].id == 7409 || player.inventory.contains(7409)
        var harvestId = FarmingPatches.values()[patch].harvestItem
        if (harvestId == FarmingConstants.SECATEURS) {
            if (magicSecateurs) {
                harvestId = FarmingConstants.MAGIC_SECATEURS
            }
        }
        if (magicSecateurs || player.inventory.contains(harvestId)) {
            val instance = this
            player.performAnimation(Animation(FarmingPatches.values()[patch].harvestAnimation))
            harvesting = true
            TaskManager.submit(object : Task(1, player, true) {
                public override fun execute() {
                    if (player.movementQueue.isMoving) {
                        stop()
                        return
                    }
                    if (player.inventory.freeSlots == 0) {
                        player.inventory.full()
                        stop()
                        return
                    }
                    player.performAnimation(Animation(FarmingPatches.values()[patch].harvestAnimation))
                    var add: Item? = null
                    val id = Plants.values()[plant].harvest
                    add = if (ItemDefinition.forId(id).isNoted) Item(id - 1, 1) else Item(id, 1)
                    if (player.skillManager.skillCape(Skill.FARMING)) {
                        player.inventory.add(Plants.values()[plant].notedHarvestId, add.amount)
                    } else {
                        player.inventory.add(add.id, add.amount)
                    }
                    var name = ItemDefinition.forId(Plants.values()[plant].harvest).name
                    if (name.endsWith("s")) name = name.substring(0, name.length - 1)
                    player.packetSender.sendMessage("You harvest " + Misc.anOrA(name) + " " + name + ".")
                    player.skillManager.addExperience(Skill.FARMING, Plants.values()[plant].harvestExperience.toInt())
                    Achievements.finishAchievement(player, AchievementData.HARVEST_A_CROP)
                    if (harvested.toInt() == 3 && player.inventory.contains(18336) && Misc.getRandom(4) == 0) {
                        player.packetSender.sendMessage("You receive a seed back from your Scroll of life.")
                        player.inventory.add(Plants.values()[plant].seed, 1)
                    }
                    if (id == 219) {
                        Achievements.doProgress(player, AchievementData.HARVEST_10_TORSTOLS)
                        Achievements.doProgress(player, AchievementData.HARVEST_1000_TORSTOLS)
                    }
                    harvested++
                    if (harvested >= (if (magicSecateurs) 6 else 3) && Misc.getRandom(if (magicSecateurs) 8 else 5) <= 1) {
                        player.farming.remove(instance)
                        stop()
                        return
                    }
                }

                override fun stop() {
                    harvesting = false
                    setEventRunning(false)
                    player.performAnimation(Animation(65535))
                }
            })
        } else {
            val name = ItemDefinition.forId(FarmingPatches.values()[patch].harvestItem).name
            player.packetSender.sendMessage("You need " + Misc.anOrA(name) + " " + name + " to harvest these plants.")
        }
    }

    fun useItemOnPlant(player: Player, item: Int): Boolean {
        if (item == 952) {
            player.performAnimation(Animation(830))
            player.farming.remove(this)
            TaskManager.submit(object : Task(2, player, false) {
                public override fun execute() {
                    player.packetSender.sendMessage("You remove your plants from the plot.")
                    player.performAnimation(Animation(65535))
                    stop()
                }
            })
            return true
        }
        if (item == 6036) {
            if (dead) {
                player.packetSender.sendMessage("Your plant is dead!")
            } else if (isDiseased) {
                player.packetSender.sendMessage("You cure the plant.")
                player.performAnimation(Animation(2288))
                player.inventory.delete(6036, 1)
                disease = -1
                doConfig(player)
            } else {
                player.packetSender.sendMessage("Your plant does not need this.")
            }
            return true
        }
        if (item == 6797) {
            water(player, item)
            return true
        }
        if (item >= 5331 && item <= 5340) {
            water(player, item)
            return true
        }
        return false
    }

    fun process(player: Player) {
        if (dead || stage >= Plants.values()[plant].stages) {
            return
        }
        val elapsed = Misc.getMinutesElapsed(minute, hour, day, year) * 6
        val grow = Plants.values()[plant].minutes
        if (elapsed >= grow) {
            for (i in 0 until elapsed / grow) {
                /*if (isDiseased()) {
				/ *} else 
				if (!isWatered()) {
					player.getPacketSender().sendMessage("You need to water your plant.");*/
                if (isWatered()) {
                    stage++
                    player.farming.doConfig()
                    if (stage >= Plants.values()[plant].stages) {
                        player.packetSender.sendMessage("<img=10> <shad=996699>A seed which you planted has finished growing!")
                        return
                    }
                }
            }
            setTime()
        }
    }

    fun doConfig(player: Player) {
        player.farming.doConfig()
    }

    val config: Int
        get() = if (Plants.values()[plant].type == SeedType.ALLOTMENT && stage.toInt() == 0 && isWatered()) {
            (Plants.values()[plant].healthy + stage + 64) * FarmingPatches.values()[patch].mod
        } else (Plants.values()[plant].healthy + stage) * FarmingPatches.values()[patch].mod

    fun getPatch(): FarmingPatches {
        return FarmingPatches.values()[patch]
    }

    val isDiseased: Boolean
        get() = disease > -1

    fun isWatered(): Boolean {
        return watered.toInt() == -1
    }
}