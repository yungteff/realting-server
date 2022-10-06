package com.realting.world.content.combat.strategy.impl

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.Animation
import com.realting.model.Locations
import com.realting.model.Projectile
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.util.Misc
import com.realting.world.content.combat.CombatContainer
import com.realting.world.content.combat.CombatType
import com.realting.world.content.combat.strategy.CombatStrategy

class Revenant : CombatStrategy {
    internal enum class REVENANT_DATA(private val npc: Int, var magicAttack: Animation, var rangedAttack: Animation) {
        REVENANT_IMP(13465, Animation(7500), Animation(7501)), REVENANT_GOBLIN(
            13469, Animation(7499), Animation(7513)
        ),
        REVENANT_WEREWOLF(13474, Animation(7496), Animation(7521)), REVENANT_ORK(
            13478, Animation(7505), Animation(7518)
        ),
        REVENANT_DARK_BEAST(13479, Animation(7502), Animation(7514));

        companion object {
            fun getData(npc: Int): REVENANT_DATA? {
                for (data in values()) {
                    if (data != null && data.npc == npc) {
                        return data
                    }
                }
                return null
            }
        }
    }

    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return victim!!.location === Locations.Location.WILDERNESS
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        return null
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        val revenant = entity as NPC?
        if (revenant!!.isChargingAttack || victim!!.constitution <= 0) {
            return true
        }
        val attkType = if (Misc.getRandom(5) <= 2 && Locations.goodDistance(
                revenant.position, revenant.position, 2
            )
        ) CombatType.MELEE else if (Misc.getRandom(10) <= 5) CombatType.MAGIC else CombatType.RANGED
        when (attkType) {
            CombatType.MELEE -> {
                revenant.performAnimation(Animation(revenant.definition.attackAnimation))
                revenant.combatBuilder.container = CombatContainer(revenant, victim!!, 1, 1, CombatType.MELEE, true)
            }
            CombatType.MAGIC, CombatType.RANGED -> {
                val revData = REVENANT_DATA.getData(revenant.id)
                revenant.isChargingAttack = true
                revenant.performAnimation(if (attkType === CombatType.MAGIC) revData!!.magicAttack else revData!!.rangedAttack)
                revenant.combatBuilder.container = CombatContainer(revenant, victim!!, 1, 2, attkType, true)
                TaskManager.submit(object : Task(1, revenant, false) {
                    var tick = 0
                    public override fun execute() {
                        when (tick) {
                            1 -> Projectile(
                                revenant, victim, if (attkType === CombatType.RANGED) 970 else 280, 44, 3, 43, 43, 0
                            ).sendProjectile()
                            3 -> {
                                revenant.isChargingAttack = false
                                stop()
                            }
                        }
                        tick++
                    }
                })
            }
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
}