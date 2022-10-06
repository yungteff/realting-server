package com.realting.world.content.combat.strategy.impl

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.Animation
import com.realting.model.Projectile
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.world.content.combat.CombatContainer
import com.realting.world.content.combat.CombatType
import com.realting.world.content.combat.strategy.CombatStrategy

class Geerin : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return victim!!.isPlayer && (victim as Player?)!!.minigameAttributes.godwarsDungeonAttributes.hasEnteredRoom()
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        return null
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        val geerin = entity as NPC?
        if (geerin!!.isChargingAttack || victim!!.constitution <= 0 || geerin.constitution <= 0) {
            return true
        }
        geerin.performAnimation(Animation(geerin.definition.attackAnimation))
        geerin.isChargingAttack = true
        geerin.combatBuilder.container = CombatContainer(geerin, victim!!, 1, 3, CombatType.RANGED, true)
        TaskManager.submit(object : Task(1, geerin, false) {
            var tick = 0
            public override fun execute() {
                if (tick == 1) {
                    Projectile(geerin, victim, 1837, 44, 3, 43, 43, 0).sendProjectile()
                    geerin.isChargingAttack = false
                    stop()
                }
                tick++
            }
        })
        return true
    }

    override fun attackDelay(entity: CharacterEntity?): Int {
        return entity!!.attackSpeed
    }

    override fun attackDistance(entity: CharacterEntity): Int {
        return 6
    }

    override fun getCombatType(entity: CharacterEntity): CombatType? {
        return CombatType.RANGED
    }
}