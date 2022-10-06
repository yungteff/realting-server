package com.realting.world.content.combat.strategy.impl.bosses.gwd

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.*
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.model.movement.MovementQueue.Companion.canWalk
import com.realting.util.Misc
import com.realting.world.content.combat.CombatContainer
import com.realting.world.content.combat.CombatType
import com.realting.world.content.combat.HitQueue.CombatHit
import com.realting.world.content.combat.strategy.CombatStrategy

class KreeArra : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return victim!!.isPlayer && (victim as Player?)!!.minigameAttributes.godwarsDungeonAttributes.hasEnteredRoom()
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        return null
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        val kreearra = entity as NPC?
        if (victim!!.constitution <= 0) {
            return true
        }
        if (kreearra!!.isChargingAttack) {
            return true
        }
        val style = if (Misc.getRandom(1) == 0) CombatType.MAGIC else CombatType.RANGED
        kreearra.performAnimation(Animation(kreearra.definition.attackAnimation))
        kreearra.isChargingAttack = true
        val target = victim as Player?
        TaskManager.submit(object : Task(1, kreearra, false) {
            var tick = 0
            public override fun execute() {
                if (tick == 1) {
                    for (near in Misc.getCombinedPlayerList(target)) {
                        if (near == null || near.location !== Locations.Location.GODWARS_DUNGEON || near.isTeleporting) continue
                        if (near.position.distanceToPoint(kreearra.position.x, kreearra.position.y) > 20) continue
                        Projectile(
                            kreearra, victim, if (style === CombatType.MAGIC) 1198 else 1197, 44, 3, 43, 43, 0
                        ).sendProjectile()
                        if (style === CombatType.RANGED) { //Moving players randomly
                            val xToMove = Misc.getRandom(1)
                            val yToMove = Misc.getRandom(1)
                            val xCoord = target!!.position.x
                            val yCoord = target.position.y
                            if (xCoord >= 2841 || xCoord <= 2823 || yCoord <= 5295 || yCoord >= 5307) {
                                return
                            } else if (Misc.getRandom(3) <= 1 && canWalk(
                                    target.position, Position(xCoord + -xToMove, yCoord + -yToMove, 2), 1
                                )
                            ) {
                                target.movementQueue.reset()
                                if (!target.isTeleporting) target.moveTo(
                                    Position(
                                        xCoord + -xToMove, yCoord + -yToMove, 2
                                    )
                                )
                                target.performGraphic(Graphic(128))
                            }
                        }
                    }
                } else if (tick == 2) {
                    for (near in Misc.getCombinedPlayerList(target)) {
                        if (near == null || near.location !== Locations.Location.GODWARS_DUNGEON || near.isTeleporting) continue
                        if (near.position.distanceToPoint(kreearra.position.x, kreearra.position.y) > 20) continue
                        CombatHit(
                            kreearra.combatBuilder, CombatContainer(kreearra, victim, 1, style, true)
                        ).handleAttack()
                    }
                    kreearra.isChargingAttack = false
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
        return 10
    }

    override fun getCombatType(entity: CharacterEntity): CombatType? {
        return CombatType.MIXED
    }
}