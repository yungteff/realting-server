package com.realting.world.content.combat.strategy.impl.bosses

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.Animation
import com.realting.model.Skill
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import com.realting.world.content.combat.CombatContainer
import com.realting.world.content.combat.CombatType
import com.realting.world.content.combat.HitQueue.CombatHit
import com.realting.world.content.combat.magic.CombatSpells
import com.realting.world.content.combat.strategy.CombatStrategy

class Venenatis : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return true
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        return null
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        val venenatis = entity as NPC?
        if (venenatis!!.isChargingAttack || victim!!.constitution <= 0) {
            return true
        }
        venenatis.isChargingAttack = true
        venenatis.performAnimation(Animation(venenatis.definition.attackAnimation))
        venenatis.combatBuilder.container = CombatContainer(venenatis, victim!!, 1, 1, CombatType.MELEE, true)
        TaskManager.submit(object : Task(1, venenatis, false) {
            var tick = 0
            public override fun execute() {
                if (tick == 0) {
                    val random = Misc.getRandom(15)
                    if (random <= 12) {
                        venenatis.prepareSpell(CombatSpells.EARTH_WAVE.spell, victim)
                    } else if (random == 13) {
                        venenatis.prepareSpell(CombatSpells.ENFEEBLE.spell, victim)
                    } else if (random == 14) {
                        venenatis.prepareSpell(CombatSpells.CONFUSE.spell, victim)
                    } else if (random == 15) {
                        venenatis.prepareSpell(CombatSpells.STUN.spell, victim)
                    }
                } else if (tick == 3) {
                    CombatHit(
                        venenatis.combatBuilder, CombatContainer(venenatis, victim, 1, CombatType.MAGIC, true)
                    ).handleAttack()
                    if (Misc.getRandom(10) <= 2) {
                        val p = victim as Player?
                        var lvl = p!!.skillManager.getCurrentLevel(Skill.PRAYER)
                        lvl *= 0.9.toInt()
                        p.skillManager.setCurrentLevel(
                            Skill.PRAYER, if (p.skillManager.getCurrentLevel(Skill.PRAYER) - lvl <= 0) 1 else lvl
                        )
                        p.packetSender.sendMessage("Venenatis has reduced your Prayer level.")
                    }
                    venenatis.isChargingAttack = false
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
        return 3
    }

    override fun getCombatType(entity: CharacterEntity): CombatType? {
        return CombatType.MIXED
    }
}