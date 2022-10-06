package com.realting.world.content.combat.strategy.impl

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.Animation
import com.realting.model.Graphic
import com.realting.model.Locations
import com.realting.model.Projectile
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.util.Misc
import com.realting.world.content.combat.CombatContainer
import com.realting.world.content.combat.CombatType
import com.realting.world.content.combat.strategy.CombatStrategy

class Glacor : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return true
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        return null
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        val glacor = entity as NPC?
        if (glacor!!.isChargingAttack || victim!!.constitution <= 0) {
            return true
        }
        if (Locations.goodDistance(glacor.position.copy(), victim!!.position.copy(), 1) && Misc.getRandom(5) <= 3) {
            glacor.performAnimation(Animation(glacor.definition.attackAnimation))
            glacor.combatBuilder.container = CombatContainer(glacor, victim, 1, 1, CombatType.MELEE, true)
        } else {
            glacor.performAnimation(Animation(9952))
            glacor.isChargingAttack = true
            glacor.combatBuilder.container =
                CombatContainer(glacor, victim, 1, 2, CombatType.MAGIC, if (Misc.getRandom(10) <= 2) false else true)
            TaskManager.submit(object : Task(1, glacor, false) {
                var tick = 0
                override fun execute() {
                    when (tick) {
                        0 -> Projectile(glacor, victim, 1017, 44, 3, 43, 31, 0).sendProjectile()
                        1 -> {
                            victim.performGraphic(Graphic(504))
                            glacor.isChargingAttack = false
                            stop()
                        }
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
        return 8
    }

    override fun getCombatType(entity: CharacterEntity): CombatType? {
        return CombatType.MIXED
    }
}