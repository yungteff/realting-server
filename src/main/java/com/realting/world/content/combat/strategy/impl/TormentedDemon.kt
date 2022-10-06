package com.realting.world.content.combat.strategy.impl

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.*
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.util.Misc
import com.realting.world.content.combat.CombatContainer
import com.realting.world.content.combat.CombatType
import com.realting.world.content.combat.strategy.CombatStrategy

class TormentedDemon : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return true
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        return null
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        val td = entity as NPC?
        if (victim!!.constitution <= 0) {
            return true
        }
        if (td!!.isChargingAttack) {
            return true
        }
        if (Locations.goodDistance(td.position.copy(), victim.position.copy(), 1) && Misc.getRandom(6) <= 4) {
            td.performAnimation(anim)
            td.performGraphic(gfx1)
            td.combatBuilder.container = CombatContainer(td, victim, 1, 2, CombatType.MELEE, true)
        } else if (Misc.getRandom(10) <= 7) {
            td.performAnimation(anim2)
            td.isChargingAttack = true
            td.combatBuilder.container = CombatContainer(td, victim, 1, 2, CombatType.RANGED, true)
            TaskManager.submit(object : Task(1, td, false) {
                override fun execute() {
                    Projectile(td, victim, 1884, 44, 3, 43, 31, 0).sendProjectile()
                    td.setChargingAttack(false).combatBuilder.attackTimer = td.definition.attackSpeed - 1
                    stop()
                }
            })
        } else {
            td.performAnimation(anim3)
            victim.performGraphic(gfx2)
            td.combatBuilder.container = CombatContainer(td, victim, 1, 2, CombatType.MAGIC, true)
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
        private val anim = Animation(10922)
        private val anim2 = Animation(10918)
        private val anim3 = Animation(10917)
        private val gfx1 = Graphic(1886, 3, GraphicHeight.MIDDLE)
        private val gfx2 = Graphic(1885)
    }
}