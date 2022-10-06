package com.realting.world.content.combat.strategy.impl;
import com.realting.engine.task.Task;
import com.realting.engine.task.TaskManager;
import com.realting.model.Animation;
import com.realting.model.Graphic;
import com.realting.model.Locations;
import com.realting.model.Position;
import com.realting.model.Projectile;
import com.realting.util.Misc;
import com.realting.world.content.combat.CombatContainer;
import com.realting.world.content.combat.CombatType;
import com.realting.world.content.combat.strategy.CombatStrategy;
import com.realting.model.entity.character.CharacterEntity;
import com.realting.model.entity.character.npc.NPC;

public class Crimson implements CombatStrategy {

	public static NPC Crimson;
	private static final Animation attack_anim1 = new Animation(401); 
	private static final Animation attack_anim2 = new Animation(2555);
	private static final Animation attack_anim3 = new Animation(10546);
	private static final Graphic graphic1 = new Graphic(1154);
	private static final Graphic graphic2 = new Graphic(1166);
	private static final Graphic graphic3 = new Graphic(1333);
	private static final Graphic StormGFX = new Graphic(457);


	public static void spawn() {


		Crimson = new NPC(200, new Position(3023, 3735));
	}

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
		NPC Crimson = (NPC)entity;
		if(Crimson.isChargingAttack()) {
			return true;
		}
		int random = Misc.getRandom(10);
		if(random <= 8 && Locations.goodDistance(Crimson.getPosition().getX(), Crimson.getPosition().getY(), victim.getPosition().getX(), victim.getPosition().getY(), 3)) {
			Crimson.performAnimation(attack_anim1);
			Crimson.getCombatBuilder().setContainer(new CombatContainer(Crimson, victim, 1, CombatType.MELEE, true));
		} else if(random <= 4 || !Locations.goodDistance(Crimson.getPosition().getX(), Crimson.getPosition().getY(), victim.getPosition().getX(), victim.getPosition().getY(), 8)) {
			Crimson.getCombatBuilder().setContainer(new CombatContainer(Crimson, victim, 1, 3, CombatType.MAGIC, true));
			Crimson.performAnimation(attack_anim3);
			Crimson.performGraphic(StormGFX);
			Crimson.setChargingAttack(true);
			Crimson.forceChat("I've banned people for less.");	
			TaskManager.submit(new Task(2, Crimson, false) {
				int tick = 0;
				@Override
				public void execute() {
					switch(tick) {
					case 1:
						new Projectile(Crimson, victim, graphic3.getId(), 44, 0, 0, 0, 0).sendProjectile();
						Crimson.setChargingAttack(false);
						stop();
						break;
					}
					tick++;
				}
			});
		} else {
			Crimson.getCombatBuilder().setContainer(new CombatContainer(Crimson, victim, 1, CombatType.RANGED, true));
			Crimson.performAnimation(attack_anim2);
			new Projectile(Crimson, victim, graphic2.getId(), 44, 0, 0, 0, 0).sendProjectile();
			Crimson.setChargingAttack(true);
			TaskManager.submit(new Task(2, Crimson, false) {
				@Override
				public void execute() {
					victim.performGraphic(graphic1);
					Crimson.setChargingAttack(false);
					stop();
				}
			});
		}
		return true;
	}
	
	
	@Override
	public int attackDelay(CharacterEntity entity) {
		return entity.getAttackSpeed();
	}

	@Override
	public int attackDistance(CharacterEntity entity) {
		return 20;
	}

	
	@Override
	public CombatType getCombatType(CharacterEntity entity) {
		return CombatType.MIXED;
	}
}

