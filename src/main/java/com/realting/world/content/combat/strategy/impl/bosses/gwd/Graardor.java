package com.realting.world.content.combat.strategy.impl.bosses.gwd;

import com.realting.engine.task.Task;
import com.realting.engine.task.TaskManager;
import com.realting.model.Animation;
import com.realting.model.Graphic;
import com.realting.model.GraphicHeight;
import com.realting.model.Locations;
import com.realting.model.Locations.Location;
import com.realting.model.Projectile;
import com.realting.util.Misc;
import com.realting.world.content.combat.CombatContainer;
import com.realting.world.content.combat.CombatType;
import com.realting.world.content.combat.HitQueue.CombatHit;
import com.realting.world.content.combat.strategy.CombatStrategy;
import com.realting.model.entity.character.CharacterEntity;
import com.realting.model.entity.character.npc.NPC;
import com.realting.model.entity.character.player.Player;

public class Graardor implements CombatStrategy {

	private static final Animation attack_anim = new Animation(7063);
	private static final Graphic graphic1 = new Graphic(1200, GraphicHeight.MIDDLE);

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
		NPC graardor = (NPC)entity;
		if(graardor.isChargingAttack() || graardor.getConstitution() <= 0) {
			return true;
		}
		CombatType style = Misc.getRandom(4) <= 1 && Locations.goodDistance(graardor.getPosition(), victim.getPosition(), 1) ? CombatType.MELEE : CombatType.RANGED;	
		if(style == CombatType.MELEE) {
			graardor.performAnimation(new Animation(graardor.getDefinition().getAttackAnimation()));
			graardor.getCombatBuilder().setContainer(new CombatContainer(graardor, victim, 1, 1, CombatType.MELEE, true));
		} else {
			graardor.performAnimation(attack_anim);
			graardor.setChargingAttack(true);
			Player target = (Player)victim;
			for (Player t : Misc.getCombinedPlayerList(target)) {
				if(t == null || t.getLocation() != Location.GODWARS_DUNGEON || t.isTeleporting())
					continue;
				if(t.getPosition().distanceToPoint(graardor.getPosition().getX(), graardor.getPosition().getY()) > 20)
					continue;
				new Projectile(graardor, target, graphic1.getId(), 44, 3, 43, 43, 0).sendProjectile();
			}
			TaskManager.submit(new Task(2, target, false) {
				@Override
				public void execute() {
					for (Player t : Misc.getCombinedPlayerList(target)) {
						if(t == null || t.getLocation() != Location.GODWARS_DUNGEON)
							continue;
						graardor.getCombatBuilder().setVictim(t);
						new CombatHit(graardor.getCombatBuilder(), new CombatContainer(graardor, t, 1, CombatType.RANGED, true)).handleAttack();
					}
					graardor.setChargingAttack(false);
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
		return 3;
	}

	@Override
	public CombatType getCombatType(CharacterEntity entity) {
		return CombatType.MIXED;
	}
}
