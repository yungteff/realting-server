package com.realting.world.content.skill.agility

import com.realting.model.Skill
import com.realting.world.content.skill.agility.ObstacleData
import com.realting.world.content.Achievements
import com.realting.world.content.Achievements.AchievementData
import com.realting.model.container.impl.Equipment
import com.realting.model.GameObject
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc

object Agility {
    @JvmStatic
    fun handleObject(p: Player, `object`: GameObject): Boolean {
        if (`object`.id == 2309) {
            if (p.skillManager.getMaxLevel(Skill.AGILITY) < 55) {
                p.packetSender.sendMessage("You need an Agility level of at least 55 to enter this course.")
                return true
            }
        }
        val agilityObject = ObstacleData.forId(`object`.id)
        if (agilityObject != null) {
            if (p.isCrossingObstacle) return true
            p.positionToFace = `object`.position
            p.resetPosition = p.position
            p.isCrossingObstacle = true
            //boolean wasRunning = p.getAttributes().isRunning();
            //if(agilityObject.mustWalk()) {
            //p.getAttributes().setRunning(false);
            //	p.getPacketSender().sendRunStatus();
            //}
            agilityObject.cross(p)
            Achievements.finishAchievement(p, AchievementData.CLIMB_AN_AGILITY_OBSTACLE)
            Achievements.doProgress(p, AchievementData.CLIMB_50_AGILITY_OBSTACLES)
        }
        return false
    }

    fun passedAllObstacles(player: Player?): Boolean {
        for (crossedObstacle in player!!.crossedObstacles) {
            if (!crossedObstacle) return false
        }
        return true
    }

    fun resetProgress(player: Player?) {
        for (i in player!!.crossedObstacles.indices) player.setCrossedObstacle(i, false)
    }

    fun isSucessive(player: Player?): Boolean {
        return Misc.getRandom(player!!.skillManager.getCurrentLevel(Skill.AGILITY) / 2) > 1
    }

    fun addExperience(player: Player?, experience: Int) {
        var experience = experience
        val agile =
            player!!.equipment[Equipment.BODY_SLOT].id == 14936 && player.equipment[Equipment.LEG_SLOT].id == 14938
        if (agile) {
            experience *= 1.5.toInt()
        }
        if (player.equipment[Equipment.HANDS_SLOT].id == 13849) {
            experience *= 1.1.toInt()
        }
        player.skillManager.addExperience(Skill.AGILITY, experience)
    }
}