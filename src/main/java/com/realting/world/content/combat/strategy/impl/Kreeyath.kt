package com.realting.world.content.combat.strategy.impl

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.Animation
import com.realting.model.Graphic
import com.realting.model.Locations
import com.realting.model.Projectile
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import com.realting.world.content.combat.CombatContainer
import com.realting.world.content.combat.CombatType
import com.realting.world.content.combat.strategy.CombatStrategy

class Kreeyath : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return victim!!.isPlayer && (victim as Player?)!!.minigameAttributes.godwarsDungeonAttributes.hasEnteredRoom()
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        return null
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        val kreeyath = entity as NPC?
        if (victim!!.constitution <= 0) {
            return true
        }
        if (kreeyath!!.isChargingAttack) {
            kreeyath.combatBuilder.attackTimer = 4
            return true
        }
        if (Locations.goodDistance(kreeyath.position.copy(), victim.position.copy(), 1) && Misc.getRandom(5) <= 3) {
            kreeyath.performAnimation(Animation(kreeyath.definition.attackAnimation))
            kreeyath.combatBuilder.container = CombatContainer(kreeyath, victim, 1, 1, CombatType.MELEE, true)
        } else {
            kreeyath.isChargingAttack = true
            kreeyath.performAnimation(attack_anim)
            kreeyath.performGraphic(graphic1)
            kreeyath.combatBuilder.container = CombatContainer(kreeyath, victim, 1, 3, CombatType.MAGIC, true)
            TaskManager.submit(object : Task(1, kreeyath, false) {
                var tick = 0
                public override fun execute() {
                    if (tick == 1) {
                        Projectile(kreeyath, victim, graphic2.id, 44, 3, 43, 43, 0).sendProjectile()
                        kreeyath.isChargingAttack = false
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
        private val attack_anim = Animation(69)
        private val graphic1 = Graphic(1212)
        private val graphic2 = Graphic(1213)
        fun getAnimation(npc: Int): Int {
            var anim = 12259
            if (npc == 50) anim = 81 else if (npc == 5363 || npc == 1590 || npc == 1591 || npc == 1592) anim =
                14246 else if (npc == 51) anim = 13152
            return anim
        }
    }
}