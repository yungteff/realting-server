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

class Crimson : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return true
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        return null
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        val Crimson = entity as NPC?
        if (Crimson!!.isChargingAttack) {
            return true
        }
        val random = Misc.getRandom(10)
        if (random <= 8 && Locations.goodDistance(
                Crimson.position.x,
                Crimson.position.y,
                victim!!.position.x,
                victim.position.y,
                3
            )
        ) {
            Crimson.performAnimation(attack_anim1)
            Crimson.combatBuilder.container = CombatContainer(Crimson, victim, 1, CombatType.MELEE, true)
        } else if (random <= 4 || !Locations.goodDistance(
                Crimson.position.x,
                Crimson.position.y,
                victim!!.position.x,
                victim.position.y,
                8
            )
        ) {
            Crimson.combatBuilder.container = CombatContainer(Crimson, victim!!, 1, 3, CombatType.MAGIC, true)
            Crimson.performAnimation(attack_anim3)
            Crimson.performGraphic(StormGFX)
            Crimson.isChargingAttack = true
            Crimson.forceChat("I've banned people for less.")
            TaskManager.submit(object : Task(2, Crimson, false) {
                var tick = 0
                public override fun execute() {
                    when (tick) {
                        1 -> {
                            Projectile(
                                Crimson,
                                victim,
                                graphic3.getId(),
                                44,
                                0,
                                0,
                                0,
                                0
                            ).sendProjectile()
                            Crimson.isChargingAttack = false
                            stop()
                        }
                    }
                    tick++
                }
            })
        } else {
            Crimson.combatBuilder.container = CombatContainer(Crimson, victim, 1, CombatType.RANGED, true)
            Crimson.performAnimation(attack_anim2)
            Projectile(Crimson, victim, graphic2.getId(), 44, 0, 0, 0, 0).sendProjectile()
            Crimson.isChargingAttack = true
            TaskManager.submit(object : Task(2, Crimson, false) {
                public override fun execute() {
                    victim.performGraphic(graphic1)
                    Crimson.isChargingAttack = false
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
        var Crimson: NPC? = null
        private val attack_anim1 = Animation(401)
        private val attack_anim2 = Animation(2555)
        private val attack_anim3 = Animation(10546)
        private val graphic1 = Graphic(1154)
        private val graphic2 = Graphic(1166)
        private val graphic3 = Graphic(1333)
        private val StormGFX = Graphic(457)
        fun spawn() {
            Crimson = NPC(200, Position(3023, 3735))
        }
    }
}