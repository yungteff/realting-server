package com.realting.world.content.combat.strategy.impl.bosses.gwd

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

class Gritch : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return true
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        return null
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        val gritch = entity as NPC?
        if (gritch!!.isChargingAttack || victim!!.constitution <= 0) {
            gritch.combatBuilder.attackTimer = 4
            return true
        }
        if (Locations.goodDistance(gritch.entityPosition.copy(), victim.entityPosition.copy(), 1) && Misc.getRandom(5) <= 3) {
            gritch.performAnimation(Animation(gritch.definition.attackAnimation))
            gritch.combatBuilder.container = CombatContainer(gritch, victim, 1, 1, CombatType.MELEE, true)
        } else {
            gritch.isChargingAttack = true
            gritch.performAnimation(anim)
            gritch.combatBuilder.container = CombatContainer(gritch, victim, 1, 3, CombatType.RANGED, true)
            TaskManager.submit(object : Task(1, gritch, false) {
                var tick = 0
                public override fun execute() {
                    if (tick == 1) {
                        Projectile(gritch, victim, gfx.id, 44, 3, 43, 43, 0).sendProjectile()
                        gritch.isChargingAttack = false
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
        return 5
    }

    override fun getCombatType(entity: CharacterEntity): CombatType? {
        return CombatType.MIXED
    }

    companion object {
        private val anim = Animation(69)
        private val gfx = Graphic(386)
    }
}