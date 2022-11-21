package com.realting.world.content.combat.strategy.impl

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.Animation
import com.realting.model.Graphic
import com.realting.model.Locations
import com.realting.model.Position
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.util.Misc
import com.realting.world.content.combat.CombatContainer
import com.realting.world.content.combat.CombatType
import com.realting.world.content.combat.strategy.CombatStrategy

class Kilik : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return true
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        return null
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        val Kilik = entity as NPC?
        if (Kilik!!.isChargingAttack) {
            return true
        }
        val random = Misc.getRandom(10)
        if (random <= 8 && Locations.goodDistance(
                Kilik.entityPosition.x, Kilik.entityPosition.y, victim!!.entityPosition.x, victim.entityPosition.y, 3
            )
        ) {
            Kilik.performAnimation(attack_anim1)
            Kilik.combatBuilder.container = CombatContainer(Kilik, victim, 1, CombatType.MELEE, true)
        } else if (random <= 4 || !Locations.goodDistance(
                Kilik.entityPosition.x, Kilik.entityPosition.y, victim!!.entityPosition.x, victim.entityPosition.y, 8
            )
        ) {
            Kilik.combatBuilder.container = CombatContainer(Kilik, victim!!, 1, 2, CombatType.MAGIC, true)
            Kilik.performAnimation(attack_anim3)
            Kilik.isChargingAttack = true
            Kilik.forceChat("Taste this!")
            TaskManager.submit(object : Task(2, Kilik, false) {
                var tick = 0
                public override fun execute() {
                    when (tick) {
                        1 -> {
                            victim.performGraphic(graphic3)
                            Kilik.isChargingAttack = false
                            stop()
                        }
                    }
                    tick++
                }
            })
        } else {
            Kilik.combatBuilder.container = CombatContainer(Kilik, victim, 1, CombatType.RANGED, true)
            Kilik.performAnimation(attack_anim2)
            Kilik.isChargingAttack = true
            TaskManager.submit(object : Task(2, Kilik, false) {
                public override fun execute() {
                    victim.performGraphic(graphic2)
                    Kilik.isChargingAttack = false
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
        var Kilik: NPC? = null
        private val attack_anim1 = Animation(393) //clawspec10961
        private val attack_anim2 = Animation(8525)
        private val attack_anim3 = Animation(1979)
        private val graphic1 = Graphic(1950)
        private val graphic2 = Graphic(451)
        private val graphic3 = Graphic(383)
        fun spawn() {
            Kilik = NPC(2010, Position(3023, 3735))
        }
    }
}