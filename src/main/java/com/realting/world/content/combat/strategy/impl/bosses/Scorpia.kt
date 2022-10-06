package com.realting.world.content.combat.strategy.impl.bosses;

import com.realting.engine.task.Task;
import com.realting.engine.task.TaskManager;
import com.realting.model.Animation;
import com.realting.model.Position;
import com.realting.util.Misc;
import com.realting.world.World;
import com.realting.world.content.combat.CombatContainer;
import com.realting.world.content.combat.CombatType;
import com.realting.world.content.combat.HitQueue.CombatHit;
import com.realting.world.content.combat.magic.CombatSpells;
import com.realting.world.content.combat.strategy.CombatStrategy;
import com.realting.model.entity.character.CharacterEntity;
import com.realting.model.entity.character.npc.NPC;

public class Scorpia implements CombatStrategy {

	private static int babiesKilled = 2;

	public static boolean attackable() {
		return babiesKilled == 2;
	}

	public static void killedBaby() {
		babiesKilled++;
	}

	@Override
	public boolean canAttack(CharacterEntity entity, CharacterEntity victim) {
		return true;
	}

	@Override
	public CombatContainer attack(CharacterEntity entity, CharacterEntity victim) {
		NPC npc = (NPC)entity;
		npc.performAnimation(new Animation(npc.getDefinition().getAttackAnimation()));

		if(npc.getConstitution() <= 500 && !npc.hasHealed()) {
			NPC[] babies = new NPC[]{new NPC(109, new Position(2854,9642)), new NPC(109, new Position(2854,9631))};
			for(NPC n : babies) {
				World.register(n);
				n.getCombatBuilder().attack(victim);
				npc.heal(990);
			}
			babiesKilled = 0;
			npc.setHealed(true);
		} else if(npc.hasHealed() && babiesKilled > 0) {
			if(Misc.getRandom(3) == 1) {
				npc.forceChat("You will regret hurting them..");
			}
			TaskManager.submit(new Task(1, npc, false) {
				int tick = 0;
				@Override
				public void execute() {
					if(tick == 0) {
						npc.prepareSpell(CombatSpells.BABY_SCORPION.getSpell(), victim);
					} else if(tick == 3) {
						new CombatHit(npc.getCombatBuilder(), new CombatContainer(npc, victim, 1, CombatType.MAGIC, true)).handleAttack();
						stop();
					}
					tick++;
				}
			});
		}

		return new CombatContainer(npc, victim, 1, 1, CombatType.MELEE, true);
	}

	@Override
	public boolean customContainerAttack(CharacterEntity entity, CharacterEntity victim) {
		return false;
	}

	@Override
	public int attackDelay(CharacterEntity entity) {
		return entity.getAttackSpeed();
	}

	@Override
	public int attackDistance(CharacterEntity entity) {
		return 3;
	}

	@Override
	public CombatType getCombatType(CharacterEntity entity) {
		return CombatType.MIXED;
	}
}
