package com.realting.world.content.combat.strategy.impl.bosses.gwd

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.Animation
import com.realting.model.Projectile
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.world.content.combat.CombatContainer
import com.realting.world.content.combat.CombatType
import com.realting.world.content.combat.strategy.CombatStrategy

class WingmanSkree : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return victim!!.isPlayer && (victim as Player?)!!.minigameAttributes.godwarsDungeonAttributes.hasEnteredRoom()
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        return null
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        val skree = entity as NPC?
        if (victim!!.constitution <= 0) {
            return true
        }
        if (skree!!.isChargingAttack) {
            return true
        }
        skree.performAnimation(Animation(skree.definition.attackAnimation))
        skree.isChargingAttack = true
        skree.combatBuilder.container = CombatContainer(skree, victim, 1, 3, CombatType.MAGIC, true)
        TaskManager.submit(object : Task(1, skree, false) {
            var tick = 0
            public override fun execute() {
                if (tick == 1) {
                    Projectile(skree, victim, 1505, 44, 3, 43, 43, 0).sendProjectile()
                    skree.isChargingAttack = false
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
        return CombatType.MAGIC
    }
}