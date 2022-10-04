package com.realting.world.content.combat.strategy.impl;
import com.realting.engine.task.Task;
import com.realting.engine.task.TaskManager;
import com.realting.model.Animation;
import com.realting.model.Graphic;
import com.realting.model.Locations;
import com.realting.model.Position;
import com.realting.util.Misc;
import com.realting.world.content.combat.CombatContainer;
import com.realting.world.content.combat.CombatType;
import com.realting.world.content.combat.strategy.CombatStrategy;
import com.realting.model.entity.character.CharacterEntity;
import com.realting.model.entity.character.npc.NPC;

public class Kilik implements CombatStrategy {

	public static NPC Kilik;
	private static final Animation attack_anim1 = new Animation(393); //clawspec10961
	private static final Animation attack_anim2 = new Animation(8525);
	private static final Animation attack_anim3 = new Animation(1979);
	private static final Graphic graphic1 = new Graphic(1950);
	private static final Graphic graphic2 = new Graphic(451);
	private static final Graphic graphic3 = new Graphic(383);


	public static void spawn() {
		Kilik = new NPC(2010, new Position(3023, 3735));
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
		NPC Kilik = (NPC)entity;
		if(Kilik.isChargingAttack()) {
			return true;
		}
		int random = Misc.getRandom(10);
		if(random <= 8 && Locations.goodDistance(Kilik.getPosition().getX(), Kilik.getPosition().getY(), victim.getPosition().getX(), victim.getPosition().getY(), 3)) {
			Kilik.performAnimation(attack_anim1);
			Kilik.getCombatBuilder().setContainer(new CombatContainer(Kilik, victim, 1, CombatType.MELEE, true));
		} else if(random <= 4 || !Locations.goodDistance(Kilik.getPosition().getX(), Kilik.getPosition().getY(), victim.getPosition().getX(), victim.getPosition().getY(), 8)) {
			Kilik.getCombatBuilder().setContainer(new CombatContainer(Kilik, victim, 1, 2, CombatType.MAGIC, true));
			Kilik.performAnimation(attack_anim3);
			Kilik.setChargingAttack(true);
			Kilik.forceChat("Taste this!");	
			TaskManager.submit(new Task(2, Kilik, false) {
				int tick = 0;
				@Override
				public void execute() {
					switch(tick) {
					case 1:
						victim.performGraphic(graphic3);
						Kilik.setChargingAttack(false);
						stop();
						break;
					}
					tick++;
				}
			});
		} else {
			Kilik.getCombatBuilder().setContainer(new CombatContainer(Kilik, victim, 1, CombatType.RANGED, true));
			Kilik.performAnimation(attack_anim2);
			Kilik.setChargingAttack(true);
			TaskManager.submit(new Task(2, Kilik, false) {
				@Override
				public void execute() {
					victim.performGraphic(graphic2);
					Kilik.setChargingAttack(false);
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

