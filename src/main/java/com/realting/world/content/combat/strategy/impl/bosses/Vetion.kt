package com.realting.world.content.combat.strategy.impl.bosses

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.Animation
import com.realting.model.Graphic
import com.realting.model.Locations
import com.realting.model.Position
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import com.realting.world.content.combat.CombatContainer
import com.realting.world.content.combat.CombatType
import com.realting.world.content.combat.HitQueue.CombatHit
import com.realting.world.content.combat.strategy.CombatStrategy

class Vetion : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return true
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        return null
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        val vetion = entity as NPC?
        if (vetion!!.isChargingAttack || victim!!.constitution <= 0) {
            return true
        }
        if (Locations.goodDistance(vetion.entityPosition.copy(), victim.entityPosition.copy(), 3) && Misc.getRandom(5) <= 3) {
            vetion.performAnimation(Animation(5487))
            vetion.combatBuilder.container = CombatContainer(vetion, victim, 1, 1, CombatType.MELEE, true)
        } else {
            vetion.isChargingAttack = true
            vetion.performAnimation(Animation(vetion.definition.attackAnimation))
            val start = victim.entityPosition.copy()
            val second = Position(start.x + 2, start.y + Misc.getRandom(2))
            val last = Position(start.x - 2, start.y - Misc.getRandom(2))
            val p = victim as Player?
            val list = Misc.getCombinedPlayerList(p)
            TaskManager.submit(object : Task(1, vetion, false) {
                var tick = 0
                public override fun execute() {
                    if (tick == 0) {
                        p!!.packetSender.sendGlobalGraphic(Graphic(281), start)
                        p.packetSender.sendGlobalGraphic(Graphic(281), second)
                        p.packetSender.sendGlobalGraphic(Graphic(281), last)
                    } else if (tick == 3) {
                        for (t in list) {
                            if (t == null) continue
                            if (t.entityPosition == start || t.entityPosition == second || t.entityPosition == last) {
                                CombatHit(
                                    vetion.combatBuilder, CombatContainer(vetion, t, 3, CombatType.MAGIC, true)
                                ).handleAttack()
                            }
                        }
                        vetion.isChargingAttack = false
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
        return 4
    }

    override fun getCombatType(entity: CharacterEntity): CombatType? {
        return CombatType.MIXED
    }
}