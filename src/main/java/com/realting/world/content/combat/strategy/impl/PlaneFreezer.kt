package com.realting.world.content.combat.strategy.impl

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.Animation
import com.realting.model.Projectile
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.util.Misc
import com.realting.world.content.combat.CombatContainer
import com.realting.world.content.combat.CombatType
import com.realting.world.content.combat.strategy.CombatStrategy

class PlaneFreezer : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return true
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        return null
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        val lakra = entity as NPC?
        if (victim!!.constitution <= 0) {
            return true
        }
        if (lakra!!.isChargingAttack) {
            return true
        }
        lakra.isChargingAttack = true
        lakra.performAnimation(Animation(13770))
        val attkType = if (Misc.getRandom(5) <= 2) CombatType.RANGED else CombatType.MAGIC
        lakra.combatBuilder.container =
            CombatContainer(lakra, victim, 1, 4, attkType, if (Misc.getRandom(5) <= 1) false else true)
        TaskManager.submit(object : Task(1, lakra, false) {
            var tick = 0
            public override fun execute() {
                if (tick == 2) {
                    Projectile(
                        lakra,
                        victim,
                        if (attkType === CombatType.RANGED) 605 else 473,
                        44,
                        3,
                        43,
                        43,
                        0
                    ).sendProjectile()
                    lakra.isChargingAttack = false
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
        return 5
    }

    override fun getCombatType(entity: CharacterEntity): CombatType? {
        return CombatType.MIXED
    }
}