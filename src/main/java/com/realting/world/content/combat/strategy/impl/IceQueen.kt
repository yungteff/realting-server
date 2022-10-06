package com.realting.world.content.combat.strategy.impl

import com.realting.model.definitions.NpcDefinition
import com.realting.model.entity.character.CharacterEntity
import com.realting.world.content.combat.CombatContainer
import com.realting.world.content.combat.CombatType
import com.realting.world.content.combat.strategy.CombatStrategy

class IceQueen : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        // TODO Auto-generated method stub
        return false
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        // TODO Auto-generated method stub
        return null
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        // TODO Auto-generated method stub
        return false
    }

    override fun attackDelay(entity: CharacterEntity?): Int {
        return npcdef.attackSpeed
    }

    override fun attackDistance(entity: CharacterEntity): Int {
        return npcdef.size
    }

    override fun getCombatType(entity: CharacterEntity): CombatType? {
        return CombatType.MIXED
    }

    companion object {
        var npcdef = NpcDefinition.forId(795)
    }
}