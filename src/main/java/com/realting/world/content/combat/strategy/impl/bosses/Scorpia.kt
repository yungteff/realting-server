package com.realting.world.content.combat.strategy.impl.bosses

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.Animation
import com.realting.model.Position
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.util.Misc
import com.realting.world.World
import com.realting.world.content.combat.CombatContainer
import com.realting.world.content.combat.CombatType
import com.realting.world.content.combat.HitQueue.CombatHit
import com.realting.world.content.combat.magic.CombatSpells
import com.realting.world.content.combat.strategy.CombatStrategy

class Scorpia : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return true
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        val npc = entity as NPC?
        npc!!.performAnimation(Animation(npc.definition.attackAnimation))
        if (npc.constitution <= 500 && !npc.hasHealed()) {
            val babies = arrayOf(NPC(109, Position(2854, 9642)), NPC(109, Position(2854, 9631)))
            for (n in babies) {
                World.register(n)
                n.combatBuilder.attack(victim)
                npc.heal(990)
            }
            babiesKilled = 0
            npc.setHealed(true)
        } else if (npc.hasHealed() && babiesKilled > 0) {
            if (Misc.getRandom(3) == 1) {
                npc.forceChat("You will regret hurting them..")
            }
            TaskManager.submit(object : Task(1, npc, false) {
                var tick = 0
                public override fun execute() {
                    if (tick == 0) {
                        npc.prepareSpell(CombatSpells.BABY_SCORPION.spell, victim)
                    } else if (tick == 3) {
                        CombatHit(
                            npc.combatBuilder,
                            CombatContainer(npc, victim!!, 1, CombatType.MAGIC, true)
                        ).handleAttack()
                        stop()
                    }
                    tick++
                }
            })
        }
        return CombatContainer(npc, victim!!, 1, 1, CombatType.MELEE, true)
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return false
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
        private var babiesKilled = 2
        fun attackable(): Boolean {
            return babiesKilled == 2
        }

        @JvmStatic
		fun killedBaby() {
            babiesKilled++
        }
    }
}