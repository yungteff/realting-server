package com.realting.world.content.combat.strategy.impl.bosses.gwd

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.Animation
import com.realting.model.Graphic
import com.realting.model.Projectile
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.world.content.combat.CombatContainer
import com.realting.world.content.combat.CombatType
import com.realting.world.content.combat.strategy.CombatStrategy

class Steelwill : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return victim!!.isPlayer && (victim as Player?)!!.minigameAttributes.godwarsDungeonAttributes.hasEnteredRoom()
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        return null
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        val steelwill = entity as NPC?
        if (victim!!.constitution <= 0) {
            return true
        }
        if (steelwill!!.isChargingAttack) {
            return true
        }
        steelwill.performAnimation(Animation(steelwill.definition.attackAnimation))
        steelwill.performGraphic(Graphic(1202))
        steelwill.isChargingAttack = true
        steelwill.combatBuilder.container = CombatContainer(steelwill, victim, 1, 3, CombatType.MAGIC, true)
        TaskManager.submit(object : Task(1, steelwill, false) {
            var tick = 0
            public override fun execute() {
                if (tick == 1) {
                    Projectile(steelwill, victim, 1203, 44, 3, 43, 43, 0).sendProjectile()
                    steelwill.isChargingAttack = false
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
        return 8
    }

    override fun getCombatType(entity: CharacterEntity): CombatType? {
        return CombatType.MAGIC
    }
}