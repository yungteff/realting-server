package com.realting.world.content.player.skill.farming

import com.realting.model.Animation
import com.realting.model.Skill
import com.realting.model.definitions.ItemDefinition
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.IOException
import java.io.BufferedReader
import java.io.FileReader

class Farming(private val player: Player) {
    var plants = arrayOfNulls<Plant>(4)
    var patches = arrayOfNulls<GrassyPatch>(4)
    fun sequence() {
        for (i in plants) {
            i?.process(player)
        }
        for (i in patches.indices) {
            if (i >= FarmingPatches.values().size) break
            if (patches[i] != null && !inhabited(FarmingPatches.values()[i].x, FarmingPatches.values()[i].y)) {
                patches[i]!!.process(player, i)
            }
        }
    }

    fun doConfig() {
        for (i in FarmingPatches.values().indices) {
            val value = getConfigFor(FarmingPatches.values()[i].config)
            val config = FarmingPatches.values()[i].config
            if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) player.packetSender.sendToggle(
                config, value
            ) else player.packetSender.sendConfig(config, value)
        }
    }

    fun getConfigFor(configId: Int): Int {
        var config = 0
        for (i in FarmingPatches.values()) {
            if (i.config == configId) {
                if (inhabited(i.x, i.y)) {
                    for (plant in plants) if (plant != null && plant.getPatch().ordinal == i.ordinal) {
                        config += plant.config
                        break
                    }
                } else {
                    config += patches[i.ordinal]!!.getConfig(i.ordinal)
                }
            }
        }
        return config
    }

    fun clear() {
        for (i in plants.indices) {
            plants[i] = null
        }
        for (i in patches.indices) patches[i] = GrassyPatch()
    }

    fun nextWateringCan(id: Int) {
        if (id == 6797) { //unlimited watering can
            player.packetSender.sendMessage("Your Unlimited Watering Can retains its water!")
        } else {
            player.inventory.delete(id, 1).add(if (id > 5333) id - 1 else id - 2, 1)
            player.packetSender.sendMessage("<img=10> Members can use an Unlimited Watering Can.")
        }
    }

    fun insert(patch: Plant?) {
        for (i in plants.indices) if (plants[i] == null) {
            plants[i] = patch
            break
        }
    }

    fun inhabited(x: Int, y: Int): Boolean {
        for (i in plants.indices) {
            if (plants[i] != null) {
                val patch = plants[i]!!.getPatch()
                if (x >= patch!!.x && y >= patch.y && x <= patch.x2 && y <= patch.y2) {
                    if (x == 3054 && y == 3307 && patch != FarmingPatches.SOUTH_FALADOR_FLOWER) continue
                    return true
                }
            }
        }
        return false
    }

    fun click(player: Player, x: Int, y: Int, option: Int): Boolean {
        if (option == 1) for (i in FarmingPatches.values().indices) {
            val patch = FarmingPatches.values()[i]
            if (x >= patch.x && y >= patch.y && x <= patch.x2 && y <= patch.y2) {
                if (x == 3054 && y == 3307 && patch != FarmingPatches.SOUTH_FALADOR_FLOWER) continue
                if (patch == FarmingPatches.SOUTH_FALADOR_ALLOTMENT_SOUTH) {
                    player.packetSender.sendMessage("This patch is currently disabled.")
                    return true
                }
                if (inhabited(x, y) || patches[i] == null) break
                patches[i]!!.click(player, option, i)
                return true
            }
        }
        for (i in plants.indices) {
            if (plants[i] != null) {
                val patch = plants[i]!!.getPatch()
                if (x >= patch!!.x && y >= patch.y && x <= patch.x2 && y <= patch.y2) {
                    if (x == 3054 && y == 3307 && patch != FarmingPatches.SOUTH_FALADOR_FLOWER) continue
                    plants[i]!!.click(player, option)
                    return true
                }
            }
        }
        return false
    }

    fun remove(plant: Plant) {
        for (i in plants.indices) if (plants[i] != null && plants[i] === plant) {
            patches[plants[i]!!.getPatch().ordinal]!!.setTime()
            plants[i] = null
            doConfig()
            return
        }
    }

    fun useItemOnPlant(item: Int, x: Int, y: Int): Boolean {
        if (item == 5341) {
            for (i in FarmingPatches.values().indices) {
                val patch = FarmingPatches.values()[i]
                if (x >= patch.x && y >= patch.y && x <= patch.x2 && y <= patch.y2) {
                    if (x == 3054 && y == 3307 && patch != FarmingPatches.SOUTH_FALADOR_FLOWER) continue
                    patches[i]!!.rake(player, i)
                    break
                }
            }
            return true
        }
        for (i in plants.indices) {
            if (plants[i] != null) {
                val patch = plants[i]!!.getPatch()
                if (x >= patch!!.x && y >= patch.y && x <= patch.x2 && y <= patch.y2) {
                    if (x == 3054 && y == 3307 && patch != FarmingPatches.SOUTH_FALADOR_FLOWER) continue
                    plants[i]!!.useItemOnPlant(player, item)
                    return true
                }
            }
        }
        return false
    }

    fun plant(seed: Int, `object`: Int, x: Int, y: Int): Boolean {
        if (!Plants.Companion.isSeed(seed)) {
            return false
        }
        for (patch in FarmingPatches.values()) {
            if (x >= patch.x && y >= patch.y && x <= patch.x2 && y <= patch.y2) {
                if (x == 3054 && y == 3307 && patch != FarmingPatches.SOUTH_FALADOR_FLOWER) continue
                if (!patches[patch.ordinal]!!.isRaked) {
                    player.packetSender.sendMessage("This patch needs to be raked before anything can grow in it.")
                    return true
                }
                for (plant in Plants.values()) {
                    if (plant.seed == seed) {
                        if (player.skillManager.getCurrentLevel(Skill.FARMING) >= plant.level) {
                            if (inhabited(x, y)) {
                                player.packetSender.sendMessage("There are already seeds planted here.")
                                return true
                            }
                            if (patch.seedType != plant.type) {
                                player.packetSender.sendMessage("You can't plant this type of seed here.")
                                return true
                            }
                            val MAGIC_SECATEURS =
                                player.inventory.contains(FarmingConstants.MAGIC_SECATEURS) || player.equipment.contains(
                                    FarmingConstants.MAGIC_SECATEURS
                                )
                            if (player.inventory.contains(FarmingConstants.SECATEURS) || MAGIC_SECATEURS) {
                                player.performAnimation(Animation(2291))
                                player.packetSender.sendMessage("You bury the seed in the dirt.")
                                player.inventory.delete(seed, 1, true)
                                val planted = Plant(patch.ordinal, plant.ordinal)
                                var XP = 1.0 //percentage
                                planted.setTime()
                                insert(planted)
                                doConfig()
                                if (MAGIC_SECATEURS) {
                                    XP = 1.1 //percentage
                                    player.packetSender.sendMessage("Your Magic Secateurs increase your XP by 10%.")
                                }
                                player.skillManager.addExperience(Skill.FARMING, (plant.plantExperience * XP).toInt())
                            } else {
                                val name = ItemDefinition.forId(FarmingConstants.SECATEURS).name
                                player.packetSender.sendMessage("You need " + Misc.anOrA(name) + " " + name + " to plant seeds.")
                            }
                        } else {
                            player.packetSender.sendMessage("You need a Farming level of " + plant.level + " to plant this.")
                        }
                        return true
                    }
                }
                return false
            }
        }
        return false
    }

    init {
        for (i in patches.indices) if (patches[i] == null) patches[i] = GrassyPatch()
    }

    fun save() {
        if (!player.shouldProcessFarming()) return
        try {
            val writer = BufferedWriter(FileWriter(DIR + "" + player.username + ".txt"))
            for (i in patches.indices) {
                if (i >= FarmingPatches.values().size) break
                if (patches[i] != null) {
                    writer.write("[PATCH]")
                    writer.newLine()
                    writer.write("patch: $i")
                    writer.newLine()
                    writer.write("stage: " + patches[i]!!.stage)
                    writer.newLine()
                    writer.write("minute: " + patches[i]!!.minute)
                    writer.newLine()
                    writer.write("hour: " + patches[i]!!.hour)
                    writer.newLine()
                    writer.write("day: " + patches[i]!!.day)
                    writer.newLine()
                    writer.write("year: " + patches[i]!!.year)
                    writer.newLine()
                    writer.write("END PATCH")
                    writer.newLine()
                    writer.newLine()
                }
            }
            for (i in plants.indices) {
                if (plants[i] != null) {
                    writer.write("[PLANT]")
                    writer.newLine()
                    writer.write("patch: " + plants[i]!!.patch)
                    writer.newLine()
                    writer.write("plant: " + plants[i]!!.plant)
                    writer.newLine()
                    writer.write("stage: " + plants[i]!!.stage)
                    writer.newLine()
                    writer.write("watered: " + plants[i]!!.watered)
                    writer.newLine()
                    writer.write("harvested: " + plants[i]!!.harvested)
                    writer.newLine()
                    writer.write("minute: " + plants[i]!!.minute)
                    writer.newLine()
                    writer.write("hour: " + plants[i]!!.hour)
                    writer.newLine()
                    writer.write("day: " + plants[i]!!.day)
                    writer.newLine()
                    writer.write("year: " + plants[i]!!.year)
                    writer.newLine()
                    writer.write("END PLANT")
                    writer.newLine()
                    writer.newLine()
                }
            }
            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun load() {
        if (!player.shouldProcessFarming()) return
        try {
            val r = BufferedReader(FileReader(DIR + "" + player.username + ".txt"))
            var stage = -1
            var patch = -1
            var plant = -1
            var watered = -1
            var minute = -1
            var hour = -1
            var day = -1
            var year = -1
            var harvested = -1
            while (true) {
                var line = r.readLine()
                line = line?.trim { it <= ' ' } ?: break
                if (line.startsWith("patch")) patch =
                    Integer.valueOf(line.substring(line.indexOf(":") + 2)) else if (line.startsWith("stage")) stage =
                    Integer.valueOf(line.substring(line.indexOf(":") + 2)) else if (line.startsWith("plant")) plant =
                    Integer.valueOf(line.substring(line.indexOf(":") + 2)) else if (line.startsWith("watered")) watered =
                    Integer.valueOf(line.substring(line.indexOf(":") + 2)) else if (line.startsWith("minute")) minute =
                    Integer.valueOf(line.substring(line.indexOf(":") + 2)) else if (line.startsWith("harvested")) harvested =
                    Integer.valueOf(line.substring(line.indexOf(":") + 2)) else if (line.startsWith("hour")) hour =
                    Integer.valueOf(line.substring(line.indexOf(":") + 2)) else if (line.startsWith("day")) day =
                    Integer.valueOf(line.substring(line.indexOf(":") + 2)) else if (line.startsWith("year")) year =
                    Integer.valueOf(line.substring(line.indexOf(":") + 2)) else if (line == "END PATCH" && patch >= 0) {
                    patches[patch]!!.stage = stage.toByte()
                    patches[patch]!!.minute = minute
                    patches[patch]!!.hour = hour
                    patches[patch]!!.day = day
                    patches[patch]!!.year = year
                    patch = -1
                } else if (line == "END PLANT" && patch >= 0) {
                    plants[patch] = Plant(patch, plant)
                    plants[patch]!!.watered = watered.toByte()
                    plants[patch]!!.stage = stage.toByte()
                    plants[patch]!!.harvested = harvested.toByte()
                    plants[patch]!!.minute = minute
                    plants[patch]!!.hour = hour
                    plants[patch]!!.day = day
                    plants[patch]!!.year = year
                    patch = -1
                }
            }
            r.close()
            doConfig()
        } catch (e: IOException) {
            //e.printStackTrace();
        }
    }

    companion object {
        /*
	 * Saving
	 * Don't wanna fill up player class lol
	 */
        private const val DIR = "./data/saves/farming/"
    }
}