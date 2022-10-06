package com.realting.world.content.combat.strategy.impl.bosses.gwd

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.*
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import com.realting.world.content.combat.CombatContainer
import com.realting.world.content.combat.CombatType
import com.realting.world.content.combat.HitQueue.CombatHit
import com.realting.world.content.combat.strategy.CombatStrategy

class Graardor : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return victim!!.isPlayer && (victim as Player?)!!.minigameAttributes.godwarsDungeonAttributes.hasEnteredRoom()
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        return null
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        val graardor = entity as NPC?
        if (graardor!!.isChargingAttack || graardor.constitution <= 0) {
            return true
        }
        val style = if (Misc.getRandom(4) <= 1 && Locations.goodDistance(
                graardor.position, victim!!.position, 1
            )
        ) CombatType.MELEE else CombatType.RANGED
        if (style === CombatType.MELEE) {
            graardor.performAnimation(Animation(graardor.definition.attackAnimation))
            graardor.combatBuilder.container = CombatContainer(graardor, victim!!, 1, 1, CombatType.MELEE, true)
        } else {
            graardor.performAnimation(attack_anim)
            graardor.isChargingAttack = true
            val target = victim as Player?
            for (t in Misc.getCombinedPlayerList(target)) {
                if (t == null || t.location !== Locations.Location.GODWARS_DUNGEON || t.isTeleporting) continue
                if (t.position.distanceToPoint(graardor.position.x, graardor.position.y) > 20) continue
                Projectile(graardor, target, graphic1.id, 44, 3, 43, 43, 0).sendProjectile()
            }
            TaskManager.submit(object : Task(2, target, false) {
                public override fun execute() {
                    for (t in Misc.getCombinedPlayerList(target)) {
                        if (t == null || t.location !== Locations.Location.GODWARS_DUNGEON) continue
                        graardor.combatBuilder.victim = t
                        CombatHit(
                            graardor.combatBuilder,
                            CombatContainer(graardor, t, 1, CombatType.RANGED, true)
                        ).handleAttack()
                    }
                    graardor.isChargingAttack = false
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
        return 3
    }

    override fun getCombatType(entity: CharacterEntity): CombatType? {
        return CombatType.MIXED
    }

    companion object {
        private val attack_anim = Animation(7063)
        private val graphic1 = Graphic(1200, GraphicHeight.MIDDLE)
    }
}