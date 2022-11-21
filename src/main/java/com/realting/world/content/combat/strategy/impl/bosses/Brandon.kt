package com.realting.world.content.combat.strategy.impl.bosses

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.*
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.util.Misc
import com.realting.world.content.combat.CombatContainer
import com.realting.world.content.combat.CombatType
import com.realting.world.content.combat.strategy.CombatStrategy

class Brandon : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return true
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        return null
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        val Brandon = entity as NPC?
        if (Brandon!!.isChargingAttack) {
            return true
        }
        val random = Misc.getRandom(10)
        if (random <= 8 && Locations.goodDistance(
                Brandon.entityPosition.x,
                Brandon.entityPosition.y,
                victim!!.entityPosition.x,
                victim.entityPosition.y,
                3
            )
        ) {
            Brandon.performAnimation(attack_anim1)
            Brandon.combatBuilder.container = CombatContainer(Brandon, victim, 1, CombatType.MELEE, true)
            Projectile(Brandon, victim, projectile1.id, 44, 3, 43, 31, 0).sendProjectile()
        } else if (random <= 4 || !Locations.goodDistance(
                Brandon.entityPosition.x,
                Brandon.entityPosition.y,
                victim!!.entityPosition.x,
                victim.entityPosition.y,
                8
            )
        ) {
            Brandon.combatBuilder.container = CombatContainer(Brandon, victim!!, 1, 3, CombatType.MAGIC, true)
            Brandon.performAnimation(attack_anim3)
            Brandon.isChargingAttack = true
            TaskManager.submit(object : Task(2, Brandon, false) {
                var tick = 0
                public override fun execute() {
                    when (tick) {
                        1 -> {
                            victim.performGraphic(graphic3)
                            Brandon.isChargingAttack = false
                            stop()
                        }
                    }
                    tick++
                }
            })
        } else {
            Brandon.combatBuilder.container = CombatContainer(Brandon, victim, 1, CombatType.RANGED, true)
            Brandon.performAnimation(attack_anim2)
            Brandon.isChargingAttack = true
            TaskManager.submit(object : Task(2, Brandon, false) {
                public override fun execute() {
                    victim.performGraphic(graphic2)
                    Brandon.isChargingAttack = false
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
        return 20
    }

    override fun getCombatType(entity: CharacterEntity): CombatType? {
        return CombatType.MIXED
    }

    companion object {
        var Brandon: NPC? = null
        private val attack_anim1 = Animation(426)
        private val attack_anim2 = Animation(4973)
        private val attack_anim3 = Animation(1978)
        private val graphic1 = Graphic(1114)
        private val graphic2 = Graphic(1014)
        private val graphic3 = Graphic(2146)
        private val projectile1 = Graphic(1120)
        fun spawn() {
            Brandon = NPC(199, Position(3023, 3735))
        }
    }
}