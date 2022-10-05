package com.realting.world.content.player.skill.farming

import com.realting.engine.task.Task
import java.util.Calendar
import com.realting.engine.task.TaskManager
import com.realting.model.Animation
import com.realting.model.Skill
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc

class GrassyPatch {
    var stage: Byte = 0
    var minute = 0
    var hour = 0
    var day = 0
    var year = 0
    fun setTime() {
        minute = Calendar.getInstance()[12]
        hour = Calendar.getInstance()[11]
        day = Calendar.getInstance()[6]
        year = Calendar.getInstance()[1]
    }

    val isRaked: Boolean
        get() = stage.toInt() == 3

    fun process(player: Player, index: Int) {
        if (stage.toInt() == 0) return
        val elapsed = Misc.getMinutesElapsed(minute, hour, day, year)
        val grow = 4
        if (elapsed >= grow) {
            for (i in 0 until elapsed / grow) {
                if (stage.toInt() == 0) {
                    return
                }
                stage = (stage - 1).toByte()
            }
            player.farming.doConfig()
            setTime()
        }
    }

    fun click(player: Player, option: Int, index: Int) {
        if (option == 1) rake(player, index)
    }

    var raking = false
    fun rake(p: Player, index: Int) {
        if (raking) return
        if (isRaked) {
            p.packetSender.sendMessage("This plot is fully raked. Try planting a seed.")
            return
        }
        if (!p.inventory.contains(5341)) {
            p.packetSender.sendMessage("This patch needs to be raked before anything can grow in it.")
            p.packetSender.sendMessage("You do not have a rake in your inventory.")
            return
        }
        raking = true
        p.skillManager.stopSkilling()
        p.performAnimation(Animation(2273))
        p.currentTask = object : Task(1, p, true) {
            public override fun execute() {
                if (!p.inventory.contains(5341)) {
                    p.packetSender.sendMessage("This patch needs to be raked before anything can grow in it.")
                    p.packetSender.sendMessage("You do not have a rake in your inventory.")
                    stop()
                    return
                }
                if (p.inventory.freeSlots == 0) {
                    p.inventory.full()
                    stop()
                    return
                }
                p.performAnimation(Animation(2273))
                if (delay >= 2 + Misc.getRandom(2)) {
                    setTime()
                    val grassyPatch = this@GrassyPatch
                    grassyPatch.stage = (grassyPatch.stage + 1).toByte()
                    p.setProcessFarming(true)
                    grassyPatch.doConfig(p, index)
                    p.skillManager.addExperience(Skill.FARMING, Misc.getRandom(2))
                    p.inventory.add(6055, 1)
                    if (isRaked) {
                        p.packetSender.sendMessage("The plot is now fully raked.")
                        stop()
                    }
                    delay = 0
                }
                delay++
            }

            override fun stop() {
                raking = false
                setEventRunning(false)
                p.performAnimation(Animation(65535))
            }
        }
        TaskManager.submit(p.currentTask)
    }

    fun doConfig(p: Player, index: Int) {
        p.farming.doConfig()
    }

    fun getConfig(index: Int): Int {
        return stage * FarmingPatches.values()[index].mod
    }
}