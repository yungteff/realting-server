package com.realting.world.content.combat.strategy.impl.bosses

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

class Jad : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return true
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        return null
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        val jad = entity as NPC?
        if (victim!!.constitution <= 0 || victim.constitution <= 0) {
            return true
        }
        if (jad!!.constitution <= 1200 && !jad.hasHealed()) {
            jad.performAnimation(anim1)
            jad.performGraphic(gfx1)
            jad.constitution = jad.constitution + Misc.getRandom(1600)
            jad.setHealed(true)
        }
        if (jad.isChargingAttack) {
            return true
        }
        val random = Misc.getRandom(10)
        if (random <= 8 && Locations.goodDistance(
                jad.entityPosition.x, jad.entityPosition.y, victim.entityPosition.x, victim.entityPosition.y, 3
            )
        ) {
            jad.performAnimation(anim2)
            jad.combatBuilder.container = CombatContainer(jad, victim, 1, 2, CombatType.MELEE, true)
        } else if (random <= 4 || !Locations.goodDistance(
                jad.entityPosition.x, jad.entityPosition.y, victim.entityPosition.x, victim.entityPosition.y, 14
            )
        ) {
            jad.combatBuilder.container = CombatContainer(jad, victim, 1, 2, CombatType.MAGIC, true)
            jad.performAnimation(anim3)
            jad.performGraphic(gfx3)
            jad.isChargingAttack = true
            TaskManager.submit(object : Task(2, jad, true) {
                var tick = 0
                public override fun execute() {
                    when (tick) {
                        1 -> {
                            Projectile(jad, victim, gfx5.id, 44, 3, 43, 31, 0).sendProjectile()
                            jad.isChargingAttack = false
                            stop()
                        }
                    }
                    tick++
                }
            })
        } else {
            jad.combatBuilder.container = CombatContainer(jad, victim, 1, 3, CombatType.RANGED, true)
            jad.performAnimation(anim4)
            jad.performGraphic(gfx2)
            jad.isChargingAttack = true
            TaskManager.submit(object : Task(2, jad, false) {
                public override fun execute() {
                    victim.performGraphic(gfx4)
                    jad.isChargingAttack = false
                    stop()
                }
            })
        }
        return true
    }

    override fun attackDelay(entity: CharacterEntity?): Int {
        return entity!!.attackSpeed
    }

    override fun attackDistance(entity: CharacterEntity): Int {
        return 10
    }

    override fun getCombatType(entity: CharacterEntity): CombatType? {
        return CombatType.MIXED
    }

    companion object {
        private val anim1 = Animation(9254)
        private val anim2 = Animation(9277)
        private val anim3 = Animation(9300)
        private val anim4 = Animation(9276)
        private val gfx1 = Graphic(444)
        private val gfx2 = Graphic(1625)
        private val gfx3 = Graphic(1626)
        private val gfx4 = Graphic(451)
        private val gfx5 = Graphic(1627)
    }
}