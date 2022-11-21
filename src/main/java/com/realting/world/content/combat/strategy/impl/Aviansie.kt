package com.realting.world.content.combat.strategy.impl

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.Animation
import com.realting.model.Locations
import com.realting.model.Projectile
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.util.Misc
import com.realting.world.content.combat.CombatContainer
import com.realting.world.content.combat.CombatType
import com.realting.world.content.combat.strategy.CombatStrategy

class Aviansie : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return true
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        return null
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        val aviansie = entity as NPC?
        if (aviansie!!.isChargingAttack || victim!!.constitution <= 0) {
            return true
        }
        if (Locations.goodDistance(aviansie.entityPosition.copy(), victim.entityPosition.copy(), 1) && Misc.getRandom(5) <= 3) {
            aviansie.performAnimation(Animation(aviansie.definition.attackAnimation))
            aviansie.combatBuilder.container = CombatContainer(aviansie, victim, 1, 1, CombatType.MELEE, true)
        } else {
            aviansie.isChargingAttack = true
            aviansie.performAnimation(Animation(aviansie.definition.attackAnimation))
            aviansie.combatBuilder.container = CombatContainer(
                aviansie,
                victim,
                1,
                3,
                if (aviansie.id == 6231) CombatType.MAGIC else CombatType.RANGED,
                true
            )
            TaskManager.submit(object : Task(1, aviansie, false) {
                var tick = 0
                public override fun execute() {
                    if (tick == 0) {
                        Projectile(aviansie, victim, getGfx(aviansie.id), 44, 3, 43, 43, 0).sendProjectile()
                    } else if (tick == 1) {
                        aviansie.isChargingAttack = false
                        stop()
                    }
                    tick++
                }
            })
        }
        return true
    }

    override fun attackDelay(entity: CharacterEntity?): Int {
        return entity!!.attackSpeed
    }

    override fun attackDistance(entity: CharacterEntity): Int {
        return 4
    }

    override fun getCombatType(entity: CharacterEntity): CombatType? {
        return CombatType.MIXED
    }

    companion object {
        fun getGfx(npc: Int): Int {
            when (npc) {
                6230 -> return 1837
                6231 -> return 2729
            }
            return 37
        }
    }
}