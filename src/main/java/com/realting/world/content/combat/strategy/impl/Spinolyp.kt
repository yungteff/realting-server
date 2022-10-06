package com.realting.world.content.combat.strategy.impl

import com.realting.model.Animation
import com.realting.model.Locations
import com.realting.model.Projectile
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.util.Misc
import com.realting.world.content.combat.CombatContainer
import com.realting.world.content.combat.CombatType
import com.realting.world.content.combat.strategy.CombatStrategy

class Spinolyp : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return true
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        return null
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        val spinolyp = entity as NPC?
        if (spinolyp!!.constitution <= 0 || victim!!.constitution <= 0) {
            return true
        }
        spinolyp.performAnimation(Animation(spinolyp.definition.attackAnimation))
        val mage = Misc.getRandom(10) <= 7
        Projectile(spinolyp, victim, if (mage) 1658 else 1017, 44, 3, 43, 43, 0).sendProjectile()
        spinolyp.combatBuilder.container = CombatContainer(
            spinolyp,
            victim!!,
            1,
            if (mage) 3 else 2,
            if (mage) CombatType.MAGIC else CombatType.RANGED,
            true
        )
        return true
    }

    override fun attackDelay(entity: CharacterEntity?): Int {
        return entity!!.attackSpeed
    }

    override fun attackDistance(entity: CharacterEntity): Int {
        return if (entity.location === Locations.Location.DUNGEONEERING) 6 else 50
    }

    override fun getCombatType(entity: CharacterEntity): CombatType {
        return CombatType.MIXED
    }
}