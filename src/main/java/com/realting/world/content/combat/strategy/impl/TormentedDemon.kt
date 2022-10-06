package com.realting.world.content.combat.strategy.impl;

import com.realting.engine.task.Task;
import com.realting.engine.task.TaskManager;
import com.realting.model.Animation;
import com.realting.model.Graphic;
import com.realting.model.GraphicHeight;
import com.realting.model.Locations;
import com.realting.model.Projectile;
import com.realting.util.Misc;
import com.realting.world.content.combat.CombatContainer;
import com.realting.world.content.combat.CombatType;
import com.realting.world.content.combat.strategy.CombatStrategy;
import com.realting.model.entity.character.CharacterEntity;
import com.realting.model.entity.character.npc.NPC;

public class TormentedDemon implements CombatStrategy {

	private static final Animation anim = new Animation(10922);
	private static final Animation anim2 = new Animation(10918);
	private static final Animation anim3 = new Animation(10917);
	private static final Graphic gfx1 = new Graphic(1886, 3, GraphicHeight.MIDDLE);
	private static final Graphic gfx2 = new Graphic(1885);

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
		NPC td = (NPC)entity;
		if(victim.getConstitution() <= 0) {
			return true;
		}
		if(td.isChargingAttack()) {
			return true;
		}
		if(Locations.goodDistance(td.getPosition().copy(), victim.getPosition().copy(), 1) && Misc.getRandom(6) <= 4) {
			td.performAnimation(anim);
			td.performGraphic(gfx1);
			td.getCombatBuilder().setContainer(new CombatContainer(td, victim, 1, 2, CombatType.MELEE, true));
		} else if(Misc.getRandom(10) <= 7) {
			td.performAnimation(anim2);
			td.setChargingAttack(true);
			td.getCombatBuilder().setContainer(new CombatContainer(td, victim, 1, 2, CombatType.RANGED, true));
			TaskManager.submit(new Task(1, td, false) {
				@Override
				protected void execute() {
					new Projectile(td, victim, 1884, 44, 3, 43, 31, 0).sendProjectile();
					td.setChargingAttack(false).getCombatBuilder().setAttackTimer(td.getDefinition().getAttackSpeed() - 1);
					stop();
				}
			});
		} else {
			td.performAnimation(anim3);
			victim.performGraphic(gfx2);
			td.getCombatBuilder().setContainer(new CombatContainer(td, victim, 1, 2, CombatType.MAGIC, true));
		}
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