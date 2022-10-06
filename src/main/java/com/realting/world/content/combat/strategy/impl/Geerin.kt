package com.realting.world.content.combat.strategy.impl;

import com.realting.engine.task.Task;
import com.realting.engine.task.TaskManager;
import com.realting.model.Animation;
import com.realting.model.Projectile;
import com.realting.world.content.combat.CombatContainer;
import com.realting.world.content.combat.CombatType;
import com.realting.world.content.combat.strategy.CombatStrategy;
import com.realting.model.entity.character.CharacterEntity;
import com.realting.model.entity.character.npc.NPC;
import com.realting.model.entity.character.player.Player;

public class Geerin implements CombatStrategy {

	@Override
	public boolean canAttack(CharacterEntity entity, CharacterEntity victim) {
		return victim.isPlayer() && ((Player)victim).getMinigameAttributes().getGodwarsDungeonAttributes().hasEnteredRoom();
	}

	@Override
	public CombatContainer attack(CharacterEntity entity, CharacterEntity victim) {
		return null;
	}

	@Override
	public boolean customContainerAttack(CharacterEntity entity, CharacterEntity victim) {
		NPC geerin = (NPC)entity;
		if(geerin.isChargingAttack() || victim.getConstitution() <= 0 || geerin.getConstitution() <= 0) {
			return true;
		}
		
		geerin.performAnimation(new Animation(geerin.getDefinition().getAttackAnimation()));
		geerin.setChargingAttack(true);

		geerin.getCombatBuilder().setContainer(new CombatContainer(geerin, victim, 1, 3, CombatType.RANGED, true));
		
		TaskManager.submit(new Task(1, geerin, false) {
			int tick = 0;
			@Override
			public void execute() {
				if(tick == 1) {
					new Projectile(geerin, victim, 1837, 44, 3, 43, 43, 0).sendProjectile();
					geerin.setChargingAttack(false);
					stop();
				}
				tick++;
			}
		});
		return true;
	}

	@Override
	public int attackDelay(CharacterEntity entity) {
		return entity.getAttackSpeed();
	}

	@Override
	public int attackDistance(CharacterEntity entity) {
		return 6;
	}

	@Override
	public CombatType getCombatType(CharacterEntity entity) {
		return CombatType.RANGED;
	}
}
