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
import com.realting.world.content.combat.HitQueue.CombatHit
import com.realting.world.content.combat.strategy.CombatStrategy

class BandosAvatar : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return true
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        return null
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        val bandosAvatar = entity as NPC?
        if (bandosAvatar!!.isChargingAttack || victim!!.constitution <= 0) {
            return true
        }
        if (Locations.goodDistance(
                bandosAvatar.entityPosition.copy(),
                victim.entityPosition.copy(),
                1
            ) && Misc.getRandom(5) <= 3
        ) {
            bandosAvatar.performAnimation(Animation(bandosAvatar.definition.attackAnimation))
            bandosAvatar.combatBuilder.container = CombatContainer(bandosAvatar, victim, 1, 1, CombatType.MELEE, true)
        } else if (!Locations.goodDistance(
                bandosAvatar.entityPosition.copy(),
                victim.entityPosition.copy(),
                3
            ) && Misc.getRandom(5) == 1
        ) {
            bandosAvatar.isChargingAttack = true
            val pos = Position(victim.entityPosition.x - 2 + Misc.getRandom(4), victim.entityPosition.y - 2 + Misc.getRandom(4))
            (victim as Player?)!!.packetSender.sendGlobalGraphic(Graphic(1549), pos)
            bandosAvatar.performAnimation(Animation(11246))
            bandosAvatar.forceChat("You shall perish!")
            TaskManager.submit(object : Task(2) {
                override fun execute() {
                    bandosAvatar.moveTo(pos)
                    bandosAvatar.performAnimation(Animation(bandosAvatar.definition.attackAnimation))
                    bandosAvatar.combatBuilder.container =
                        CombatContainer(bandosAvatar, victim, 1, 1, CombatType.MELEE, false)
                    bandosAvatar.isChargingAttack = false
                    bandosAvatar.combatBuilder.attackTimer = 0
                    stop()
                }
            })
        } else {
            bandosAvatar.isChargingAttack = true
            val barrage = Misc.getRandom(4) <= 2
            bandosAvatar.performAnimation(Animation(if (barrage) 11245 else 11252))
            bandosAvatar.combatBuilder.container = CombatContainer(bandosAvatar, victim, 1, 3, CombatType.MAGIC, true)
            TaskManager.submit(object : Task(1, bandosAvatar, false) {
                var tick = 0
                public override fun execute() {
                    if (tick == 0 && !barrage) {
                        Projectile(bandosAvatar, victim, 2706, 44, 3, 43, 43, 0).sendProjectile()
                    } else if (tick == 1) {
                        if (barrage && victim.isPlayer && Misc.getRandom(10) <= 5) {
                            victim.movementQueue.freeze(15)
                            victim.performGraphic(Graphic(369))
                        }
                        if (barrage && Misc.getRandom(6) <= 3) {
                            bandosAvatar.performAnimation(Animation(11245))
                            for (toAttack in Misc.getCombinedPlayerList(victim as Player?)) {
                                if (toAttack != null && Locations.goodDistance(
                                        bandosAvatar.entityPosition,
                                        toAttack.entityPosition,
                                        7
                                    ) && toAttack.constitution > 0
                                ) {
                                    bandosAvatar.forceChat("DIE!")
                                    CombatHit(
                                        bandosAvatar.combatBuilder,
                                        CombatContainer(bandosAvatar, toAttack, 2, CombatType.MAGIC, false)
                                    ).handleAttack()
                                    toAttack.performGraphic(Graphic(1556))
                                }
                            }
                        }
                        bandosAvatar.setChargingAttack(false).combatBuilder.attackTimer = attackDelay(bandosAvatar) - 2
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
        fun getAnimation(npc: Int): Int {
            var anim = 12259
            if (npc == 50) anim = 81 else if (npc == 5362 || npc == 5363) anim = 14246 else if (npc == 51) anim = 13152
            return anim
        }
    }
}