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

public class WingmanSkree implements CombatStrategy {

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
		NPC skree = (NPC)entity;
		if(victim.getConstitution() <= 0) {
			return true;
		}
		if(skree.isChargingAttack()) {
			return true;
		}
		
		skree.performAnimation(new Animation(skree.getDefinition().getAttackAnimation()));
		skree.setChargingAttack(true);

		skree.getCombatBuilder().setContainer(new CombatContainer(skree, victim, 1, 3, CombatType.MAGIC, true));
		
		TaskManager.submit(new Task(1, skree, false) {
			int tick = 0;
			@Override
			public void execute() {
				if(tick == 1) {
					new Projectile(skree, victim, 1505, 44, 3, 43, 43, 0).sendProjectile();
					skree.setChargingAttack(false);
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
		return CombatType.MAGIC;
	}
}