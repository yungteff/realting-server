package com.realting.world.content.combat.strategy.impl.bosses

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.*
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.npc.NPCMovementCoordinator
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import com.realting.world.World
import com.realting.world.content.combat.CombatContainer
import com.realting.world.content.combat.CombatType
import com.realting.world.content.combat.HitQueue.CombatHit
import com.realting.world.content.combat.strategy.CombatStrategy

class KalphiteQueen : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return true
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        return null
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        if (victim!!.constitution <= 0 || KALPHITE_QUEEN!!.constitution <= 0) {
            return true
        }
        if (KALPHITE_QUEEN!!.isChargingAttack || !victim.isPlayer) {
            return true
        }
        val p = victim as Player?
        val list = Misc.getCombinedPlayerList(p)
        if (Locations.goodDistance(
                KALPHITE_QUEEN!!.entityPosition.copy(), victim.entityPosition.copy(), 1
            ) && Misc.getRandom(6) <= 2
        ) {
            KALPHITE_QUEEN!!.performAnimation(Animation(KALPHITE_QUEEN!!.definition.attackAnimation))
            KALPHITE_QUEEN!!.combatBuilder.container =
                CombatContainer(KALPHITE_QUEEN!!, victim, 1, 1, CombatType.MELEE, true)
        } else {
            KALPHITE_QUEEN!!.isChargingAttack = true
            KALPHITE_QUEEN!!.performAnimation(Animation(if (secondForm()) 6234 else 6240))
            TaskManager.submit(object : Task(1, KALPHITE_QUEEN, false) {
                var tick = 0
                override fun execute() {
                    if (tick == 1) {
                        for (toAttack in list) {
                            if (toAttack != null && Locations.goodDistance(
                                    KALPHITE_QUEEN!!.entityPosition, toAttack.entityPosition, 7
                                ) && toAttack.constitution > 0
                            ) {
                                Projectile(
                                    KALPHITE_QUEEN, toAttack, if (secondForm()) 279 else 280, 44, 3, 43, 43, 0
                                ).sendProjectile()
                            }
                        }
                    } else if (tick == 3) {
                        for (toAttack in list) {
                            if (toAttack != null && Locations.goodDistance(
                                    KALPHITE_QUEEN!!.entityPosition, toAttack.entityPosition, 7
                                ) && toAttack.constitution > 0
                            ) {
                                toAttack.performGraphic(Graphic(if (secondForm()) 278 else 279))
                            }
                        }
                    } else if (tick == 5) {
                        for (toAttack in list) {
                            if (toAttack != null && Locations.goodDistance(
                                    KALPHITE_QUEEN!!.entityPosition, toAttack.entityPosition, 7
                                ) && toAttack.constitution > 0
                            ) {
                                KALPHITE_QUEEN!!.setEntityInteraction(toAttack)
                                val cbType =
                                    if (secondForm() && Misc.getRandom(5) <= 3) CombatType.RANGED else CombatType.MAGIC
                                KALPHITE_QUEEN!!.combatBuilder.victim = toAttack
                                CombatHit(
                                    KALPHITE_QUEEN!!.combatBuilder, CombatContainer(
                                        KALPHITE_QUEEN!!, toAttack, 1, cbType, true
                                    )
                                ).handleAttack()
                            }
                        }
                        KALPHITE_QUEEN!!.combatBuilder.attack(victim)
                        stop()
                    }
                    tick++
                }

                override fun stop() {
                    setEventRunning(false)
                    KALPHITE_QUEEN!!.isChargingAttack = false
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
        var KALPHITE_QUEEN: NPC? = null

        @JvmStatic
        fun spawn(id: Int, pos: Position?) {
            KALPHITE_QUEEN = NPC(id, pos)
            KALPHITE_QUEEN!!.movementCoordinator.coordinator = NPCMovementCoordinator.Coordinator(true, 3)
            World.register(KALPHITE_QUEEN)
        }

        @JvmStatic
        fun death(id: Int, pos: Position?) {
            TaskManager.submit(object : Task(if (id == 1160) 40 else 2) {
                override fun execute() {
                    spawn(if (id == 1160) 1158 else 1160, pos)
                    stop()
                }
            })
        }

        fun secondForm(): Boolean {
            return KALPHITE_QUEEN!!.id == 1160
        }
    }
}