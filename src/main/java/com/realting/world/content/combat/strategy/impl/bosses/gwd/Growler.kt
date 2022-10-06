package com.realting.world.content.combat.strategy.impl.bosses.gwd

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

class Growler : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return victim!!.isPlayer && (victim as Player?)!!.minigameAttributes.godwarsDungeonAttributes.hasEnteredRoom()
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        return null
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        val growler = entity as NPC?
        if (growler!!.isChargingAttack || victim!!.constitution <= 0) {
            return true
        }
        if (Locations.goodDistance(growler.position.copy(), victim!!.position.copy(), 1) && Misc.getRandom(5) <= 3) {
            growler.performAnimation(Animation(growler.definition.attackAnimation))
            growler.combatBuilder.container = CombatContainer(growler, victim, 1, 1, CombatType.MELEE, true)
        } else {
            growler.isChargingAttack = true
            growler.performAnimation(anim)
            growler.combatBuilder.container = CombatContainer(growler, victim, 1, 3, CombatType.MAGIC, true)
            TaskManager.submit(object : Task(1, growler, false) {
                var tick = 0
                public override fun execute() {
                    if (tick == 1) {
                        Projectile(growler, victim, graphic.id, 44, 3, 43, 43, 0).sendProjectile()
                        growler.isChargingAttack = false
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
        return 8
    }

    override fun getCombatType(entity: CharacterEntity): CombatType? {
        return CombatType.MIXED
    }

    companion object {
        private val anim = Animation(7019)
        private val graphic = Graphic(384)
    }
}