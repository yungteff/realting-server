package com.realting.world.content.combat.strategy.impl.bosses

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.*
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import com.realting.world.content.combat.CombatContainer
import com.realting.world.content.combat.CombatType
import com.realting.world.content.combat.strategy.CombatStrategy

class Callisto : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return true
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        return null
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        val callisto = entity as NPC?
        if (callisto!!.isChargingAttack || victim!!.constitution <= 0) {
            return true
        }
        if (Misc.getRandom(15) <= 2) {
            val hitAmount = 1
            callisto.performGraphic(Graphic(1))
            callisto.constitution = callisto.constitution + hitAmount
            (victim as Player?)!!.packetSender.sendMessage(
                MessageType.NPC_ALERT, "Callisto absorbs his next attack, healing himself a bit."
            )
        }
        if (Locations.goodDistance(callisto.entityPosition.copy(), victim.entityPosition.copy(), 3) && Misc.getRandom(5) <= 3) {
            callisto.performAnimation(Animation(callisto.definition.attackAnimation))
            callisto.combatBuilder.container = CombatContainer(callisto, victim, 1, 1, CombatType.MELEE, true)
            if (Misc.getRandom(10) <= 2) {
                victim.moveTo(Position(3156 + Misc.getRandom(3), 3804 + Misc.getRandom(3)))
                victim.performAnimation(Animation(534))
                (victim as Player?)!!.packetSender.sendMessage(MessageType.NPC_ALERT, "You have been knocked back!")
            }
        } else {
            callisto.isChargingAttack = true
            callisto.performAnimation(Animation(4928))
            callisto.combatBuilder.container = CombatContainer(callisto, victim, 1, 3, CombatType.MAGIC, true)
            TaskManager.submit(object : Task(1, callisto, false) {
                var tick = 0
                public override fun execute() {
                    if (tick == 0) {
                        Projectile(callisto, victim, 395, 44, 3, 41, 31, 0).sendProjectile()
                    } else if (tick == 1) {
                        callisto.isChargingAttack = false
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
        return 3
    }

    override fun getCombatType(entity: CharacterEntity): CombatType? {
        return CombatType.MIXED
    }
}