package com.realting.world.content.player.skill.firemaking

import com.realting.engine.task.Task
import com.realting.world.content.player.skill.dungeoneering.Dungeoneering
import com.realting.model.movement.MovementQueue
import com.realting.model.Skill
import com.realting.model.container.impl.Equipment
import com.realting.world.content.Achievements
import com.realting.world.content.Achievements.AchievementData
import com.realting.engine.task.TaskManager
import com.realting.model.Animation
import com.realting.model.GameObject
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import com.realting.world.content.CustomObjects
import com.realting.world.content.Sounds

/**
 * The Firemaking skill
 * @author Gabriel Hannason
 */
object Firemaking {
    @JvmStatic
    fun lightFire(player: Player, log: Int, addingToFire: Boolean, amount: Int) {
        if (!player.clickDelay.elapsed(2000) || player.movementQueue.isLockMovement) return
        if (!player.location.isFiremakingAllowed) {
            player.packetSender.sendMessage("You can not light a fire in this area.")
            return
        }
        val objectExists = CustomObjects.objectExists(player.position.copy())
        if (!Dungeoneering.doingDungeoneering(player)) {
            if (objectExists && !addingToFire || player.position.z > 0 || !player.movementQueue.canWalk(
                    1, 0
                ) && !player.movementQueue.canWalk(-1, 0) && !player.movementQueue.canWalk(
                    0, 1
                ) && !player.movementQueue.canWalk(0, -1)
            ) {
                player.packetSender.sendMessage("You can not light a fire here.")
                return
            }
            if (player.position.x == 2848 && player.position.y == 3335 || player.position.x == 2711 && player.position.y == 3438) { //fm
                player.packetSender.sendMessage("There's already a perfectly good fire here.")
                return
            }
        }
        val logData = Logdata.getLogData(player, log) ?: return
        player.movementQueue.reset()
        if (objectExists && addingToFire) MovementQueue.stepAway(player)
        player.packetSender.sendInterfaceRemoval()
        player.setEntityInteraction(null)
        player.skillManager.stopSkilling()
        val cycle = 2 + Misc.getRandom(3)
        if (player.skillManager.getMaxLevel(Skill.FIREMAKING) < logData.level) {
            player.packetSender.sendMessage("You need a Firemaking level of atleast " + logData.level + " to light this.")
            return
        }
        if (!addingToFire) {
            player.packetSender.sendMessage("You attempt to light a fire..")
            player.performAnimation(Animation(733))
            player.movementQueue.isLockMovement = true
        }
        player.currentTask = object : Task(if (addingToFire) 2 else cycle, player, if (addingToFire) true else false) {
            var added = 0
            public override fun execute() {
                player.packetSender.sendInterfaceRemoval()
                if (addingToFire && player.interactingObject == null) { //fire has died
                    player.skillManager.stopSkilling()
                    player.packetSender.sendMessage("The fire has died out.")
                    return
                }
                if (player.equipment[Equipment.RING_SLOT].id == 13659 && Misc.getRandom(7) == 1) {
                    player.packetSender.sendMessage("Your cape has salvaged your log.")
                } else {
                    if (player.skillManager.skillCape(Skill.FIREMAKING) && Misc.getRandom(10) == 1) {
                        player.packetSender.sendMessage("Your cape has salvaged your log.")
                    } else {
                        player.inventory.delete(logData.logId, 1)
                    }
                }
                if (addingToFire) {
                    player.performAnimation(Animation(827))
                    player.packetSender.sendMessage("You add some logs to the fire..")
                } else {
                    if (!player.movementQueue.isMoving) {
                        player.movementQueue.isLockMovement = false
                        player.performAnimation(Animation(65535))
                        MovementQueue.stepAway(player)
                    }
                    CustomObjects.globalFiremakingTask(
                        GameObject(logData.gameObject, player.position.copy()), player, logData.burnTime
                    )
                    player.packetSender.sendMessage("The fire catches and the logs begin to burn.")
                    stop()
                }
                if (logData.name == "OAK") {
                    Achievements.doProgress(player, AchievementData.BURN_25_OAK_LOGS)
                } else if (logData.name == "MAGIC") {
                    Achievements.doProgress(player, AchievementData.BURN_100_MAGIC_LOGS)
                    Achievements.doProgress(player, AchievementData.BURN_2500_MAGIC_LOGS)
                }
                Sounds.sendSound(player, Sounds.Sound.LIGHT_FIRE)
                player.skillManager.addExperience(Skill.FIREMAKING, logData.xp)
                added++
                if (added >= amount || !player.inventory.contains(logData.logId)) {
                    stop()
                    if (added < amount && addingToFire && Logdata.getLogData(player, -1) != null && Logdata.getLogData(
                            player, -1
                        )!!.logId != log
                    ) {
                        player.clickDelay.reset(0)
                        lightFire(player, -1, true, amount - added)
                    }
                    return
                }
            }

            override fun stop() {
                setEventRunning(false)
                player.performAnimation(Animation(65535))
                player.movementQueue.isLockMovement = false
            }
        }
        TaskManager.submit(player.currentTask)
        player.clickDelay.reset(System.currentTimeMillis() + 500)
    }
}