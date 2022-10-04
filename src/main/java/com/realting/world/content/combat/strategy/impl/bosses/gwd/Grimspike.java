package com.realting.world.content.combat.strategy.impl.bosses.gwd;

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

public class Grimspike implements CombatStrategy {

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
		NPC grimspike = (NPC)entity;
		if(grimspike.isChargingAttack() || victim.getConstitution() <= 0) {
			return true;
		}
		
		grimspike.performAnimation(new Animation(grimspike.getDefinition().getAttackAnimation()));
		grimspike.setChargingAttack(true);

		grimspike.getCombatBuilder().setContainer(new CombatContainer(grimspike, victim, 1, 3, CombatType.RANGED, true));
		
		TaskManager.submit(new Task(1, grimspike, false) {
			int tick = 0;
			@Override
			public void execute() {
				if(tick == 1) {
					new Projectile(grimspike, victim, 37, 44, 3, 43, 43, 0).sendProjectile();
					grimspike.setChargingAttack(false);
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
