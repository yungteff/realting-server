package com.realting.world.content.combat.strategy.impl.bosses

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.Animation
import com.realting.model.Projectile
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.world.content.combat.CombatContainer
import com.realting.world.content.combat.CombatType
import com.realting.world.content.combat.strategy.CombatStrategy

class DagannothSupreme : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return true
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        return null
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        val prime = entity as NPC?
        if (prime!!.constitution <= 0 || victim!!.constitution <= 0) {
            return true
        }
        prime.performAnimation(Animation(prime.definition.attackAnimation))
        TaskManager.submit(object : Task(1, prime, false) {
            override fun execute() {
                Projectile(prime, victim, 1937, 44, 3, 43, 43, 0).sendProjectile()
                prime.combatBuilder.container = CombatContainer(prime, victim!!, 1, 2, CombatType.RANGED, true)
                stop()
            }
        })
        return true
    }

    override fun attackDelay(entity: CharacterEntity?): Int {
        return entity!!.attackSpeed
    }

    override fun attackDistance(entity: CharacterEntity): Int {
        return 5
    }

    override fun getCombatType(entity: CharacterEntity): CombatType? {
        return CombatType.RANGED
    }
}