package com.realting.world.content.combat.strategy.impl;

import com.realting.engine.task.Task;
import com.realting.engine.task.TaskManager;
import com.realting.model.Animation;
import com.realting.model.Projectile;
import com.realting.util.Misc;
import com.realting.world.content.combat.CombatContainer;
import com.realting.world.content.combat.CombatType;
import com.realting.world.content.combat.strategy.CombatStrategy;
import com.realting.model.entity.character.CharacterEntity;
import com.realting.model.entity.character.npc.NPC;

public class PlaneFreezer implements CombatStrategy {

	@Override
	public boolean canAttack(CharacterEntity entity, CharacterEntity victim) {
		return true;
	}

	@Override
	public CombatContainer attack(CharacterEntity entity, CharacterEntity victim) {
		return null;
	}

	@Override
	public boolean customContainerAttack(CharacterEntity entity, CharacterEntity victim) {
		NPC lakra = (NPC)entity;
		if(victim.getConstitution() <= 0) {
			return true;
		}
		if(lakra.isChargingAttack()) {
			return true;
		}
		lakra.setChargingAttack(true);
		lakra.performAnimation(new Animation((13770)));
		final CombatType attkType = Misc.getRandom(5) <= 2 ? CombatType.RANGED : CombatType.MAGIC;
		lakra.getCombatBuilder().setContainer(new CombatContainer(lakra, victim, 1, 4, attkType, Misc.getRandom(5) <= 1 ? false : true));
		TaskManager.submit(new Task(1, lakra, false) {
			int tick = 0;
			@Override
			public void execute() {
				if(tick == 2) {
					new Projectile(lakra, victim, (attkType == CombatType.RANGED ? 605 : 473), 44, 3, 43, 43, 0).sendProjectile();
					lakra.setChargingAttack(false);
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
		return 5;
	}

	@Override
	public CombatType getCombatType(CharacterEntity entity) {
		return CombatType.MIXED;
	}
}
