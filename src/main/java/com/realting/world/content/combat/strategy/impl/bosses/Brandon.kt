package com.realting.world.content.combat.strategy.impl.bosses;
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

public class Brandon implements CombatStrategy {

	public static NPC Brandon;
	private static final Animation attack_anim1 = new Animation(426); 
	private static final Animation attack_anim2 = new Animation(4973);
	private static final Animation attack_anim3 = new Animation(1978);
	private static final Graphic graphic1 = new Graphic(1114);
	private static final Graphic graphic2 = new Graphic(1014);
	private static final Graphic graphic3 = new Graphic(2146);
	private static final Graphic projectile1 = new Graphic(1120);


	public static void spawn() {


		Brandon = new NPC(199, new Position(3023, 3735));
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
		NPC Brandon = (NPC)entity;
		if(Brandon.isChargingAttack()) {
			return true;
		}
		int random = Misc.getRandom(10);
		if(random <= 8 && Locations.goodDistance(Brandon.getPosition().getX(), Brandon.getPosition().getY(), victim.getPosition().getX(), victim.getPosition().getY(), 3)) {
			Brandon.performAnimation(attack_anim1);
			Brandon.getCombatBuilder().setContainer(new CombatContainer(Brandon, victim, 1, CombatType.MELEE, true));
			new Projectile(Brandon, victim, projectile1.getId(), 44, 3, 43, 31, 0).sendProjectile();
		} else if(random <= 4 || !Locations.goodDistance(Brandon.getPosition().getX(), Brandon.getPosition().getY(), victim.getPosition().getX(), victim.getPosition().getY(), 8)) {
			Brandon.getCombatBuilder().setContainer(new CombatContainer(Brandon, victim, 1, 3, CombatType.MAGIC, true));
			Brandon.performAnimation(attack_anim3);
			Brandon.setChargingAttack(true);
			TaskManager.submit(new Task(2, Brandon, false) {
				int tick = 0;
				@Override
				public void execute() {
					switch(tick) {
					case 1:
						victim.performGraphic(graphic3);
						Brandon.setChargingAttack(false);
						stop();
						break;
					}
					tick++;
				}
			});
		} else {
			Brandon.getCombatBuilder().setContainer(new CombatContainer(Brandon, victim, 1, CombatType.RANGED, true));
			Brandon.performAnimation(attack_anim2);
			Brandon.setChargingAttack(true);
			TaskManager.submit(new Task(2, Brandon, false) {
				@Override
				public void execute() {
					victim.performGraphic(graphic2);
					Brandon.setChargingAttack(false);
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

