package com.realting.world.content.combat.strategy.impl;

import com.realting.model.definitions.NpcDefinition;
import com.realting.world.content.combat.CombatContainer;
import com.realting.world.content.combat.CombatType;
import com.realting.world.content.combat.strategy.CombatStrategy;
import com.realting.model.entity.character.CharacterEntity;

public class IceQueen implements CombatStrategy {
	
	public static NpcDefinition npcdef = NpcDefinition.forId(795);

	@Override
	public boolean canAttack(CharacterEntity entity, CharacterEntity victim) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public CombatContainer attack(CharacterEntity entity, CharacterEntity victim) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean customContainerAttack(CharacterEntity entity, CharacterEntity victim) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int attackDelay(CharacterEntity entity) {
		return npcdef.getAttackSpeed();
	}

	@Override
	public int attackDistance(CharacterEntity entity) {
		return npcdef.getSize();
	}

	@Override
	public CombatType getCombatType(CharacterEntity entity) {
		return CombatType.MIXED;
	}

}
