package com.realting.world.content.combat.strategy.impl.bosses;

import com.realting.engine.task.Task;
import com.realting.engine.task.TaskManager;
import com.realting.model.Animation;
import com.realting.model.Graphic;
import com.realting.model.GraphicHeight;
import com.realting.model.Position;
import com.realting.model.Projectile;
import com.realting.util.Misc;
import com.realting.world.clip.region.RegionClipping;
import com.realting.world.content.combat.CombatContainer;
import com.realting.world.content.combat.CombatType;
import com.realting.world.content.combat.strategy.CombatStrategy;
import com.realting.model.entity.character.CharacterEntity;
import com.realting.model.entity.character.npc.NPC;
import com.realting.model.entity.character.player.Player;

public class ChaosElemental implements CombatStrategy {

	private static enum elementalData {
		MELEE(new Graphic(553, GraphicHeight.HIGH), new Graphic(554,GraphicHeight.MIDDLE), null),
		RANGED(new Graphic(665, GraphicHeight.HIGH), null, new Graphic(552, GraphicHeight.HIGH)),
		MAGIC(new Graphic(550, GraphicHeight.HIGH), new Graphic(551,GraphicHeight.MIDDLE), new Graphic(555, GraphicHeight.HIGH));

		elementalData(Graphic startGfx, Graphic projectile, Graphic endGraphic) {
			startGraphic = startGfx;
			projectileGraphic = projectile;
			this.endGraphic = endGraphic;
		}

		public Graphic startGraphic;
		public Graphic projectileGraphic;
		public Graphic endGraphic;
		
		public CombatType getCombatType() {
			switch(this) {
			case MAGIC:
				return CombatType.MAGIC;
			case MELEE:
				return CombatType.MELEE;
			case RANGED:
				return CombatType.RANGED;
			}
			return CombatType.MELEE;
		}

		static elementalData forId(int id) {
			for(elementalData data : elementalData.values()) {
				if(data.ordinal() == id)
					return data;
			}
			return null;
		}
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
		NPC cE = (NPC)entity;
		if(cE.isChargingAttack() || victim.getConstitution() <= 0) {
			return true;
		}
		cE.getMovementQueue().reset();
		cE.setEntityInteraction(victim);
		final int attackStyle = Misc.getRandom(2); //0 = melee, 1 = range, 2 =mage
		final elementalData data = elementalData.forId(attackStyle);
		if(data.startGraphic != null)
			cE.performGraphic(data.startGraphic);
		cE.performAnimation(new Animation(cE.getDefinition().getAttackAnimation()));
		if(data.projectileGraphic != null)
			new Projectile(cE, victim, data.projectileGraphic.getId(), 44, 3, 43, 31, 0).sendProjectile();
		cE.setChargingAttack(true);
		TaskManager.submit(new Task(1, cE, false) {
			@Override
			public void execute() {
				cE.getCombatBuilder().setContainer(new CombatContainer(cE, victim, 1, 2, data.getCombatType(), true));
				if(data.endGraphic != null)
					victim.performGraphic(data.endGraphic);
				cE.setChargingAttack(false);
				
				if(Misc.getRandom(50) <= 2) {
					cE.performGraphic(teleGraphic);
					victim.performGraphic(teleGraphic);
					int randomX = victim.getPosition().getX() - Misc.getRandom(30);
					int randomY = victim.getPosition().getY();
					if(RegionClipping.getClipping(randomX, randomY, 0) == 0) {
						victim.moveTo(new Position(randomX, randomY));
						((Player)victim).getPacketSender().sendMessage("The Chaos elemental has teleported you away!");
					}
				}
				
				stop();
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
		return 8;
	}
	
	@Override
	public CombatType getCombatType(CharacterEntity entity) {
		return CombatType.MIXED;
	}

	private static final Graphic teleGraphic = new Graphic(661);
}
