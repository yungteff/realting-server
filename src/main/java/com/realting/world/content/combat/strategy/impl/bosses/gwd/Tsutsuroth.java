package com.realting.world.content.combat.strategy.impl.bosses.gwd;

import com.realting.engine.task.Task;
import com.realting.engine.task.TaskManager;
import com.realting.model.Animation;
import com.realting.model.Graphic;
import com.realting.model.GraphicHeight;
import com.realting.model.Locations;
import com.realting.model.Locations.Location;
import com.realting.model.Projectile;
import com.realting.model.Skill;
import com.realting.util.Misc;
import com.realting.world.content.combat.CombatContainer;
import com.realting.world.content.combat.CombatType;
import com.realting.world.content.combat.HitQueue.CombatHit;
import com.realting.world.content.combat.strategy.CombatStrategy;
import com.realting.model.entity.character.CharacterEntity;
import com.realting.model.entity.character.npc.NPC;
import com.realting.model.entity.character.player.Player;

public class Tsutsuroth implements CombatStrategy {

	private static final Animation anim1 = new Animation(6947);
	private static final Graphic graphic1 = new Graphic(1211, GraphicHeight.MIDDLE);
	private static final Graphic graphic2 = new Graphic(390);

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
		NPC tsutsuroth = (NPC)entity;
		if(victim.getConstitution() <= 0) {
			return true;
		}
		if(tsutsuroth.isChargingAttack()) {
			return true;
		}
		Player target = (Player)victim;
		CombatType style = Misc.getRandom(8) >= 6 && Locations.goodDistance(tsutsuroth.getPosition(), victim.getPosition(), 2) ? CombatType.MELEE : CombatType.MAGIC;
		if(style == CombatType.MELEE) {
			tsutsuroth.performAnimation(new Animation(6945));
			tsutsuroth.getCombatBuilder().setContainer(new CombatContainer(tsutsuroth, victim, 1, 1, CombatType.MELEE, true));
			int specialAttack = Misc.getRandom(4);
			if (specialAttack == 2) {
				int amountToDrain = Misc.getRandom(400);
				target.getPacketSender().sendMessage("K'ril Tsutsaroth slams through your defence and steals some Prayer points..");
				if(amountToDrain > target.getSkillManager().getCurrentLevel(Skill.PRAYER)) {
					amountToDrain = target.getSkillManager().getCurrentLevel(Skill.PRAYER);
				}
				target.getSkillManager().setCurrentLevel(Skill.PRAYER, target.getSkillManager().getCurrentLevel(Skill.PRAYER) - amountToDrain);
				if(target.getSkillManager().getCurrentLevel(Skill.PRAYER) <= 0) {
					target.getPacketSender().sendMessage("You have run out of Prayer points!");
				}
			}
		} else {
			tsutsuroth.performAnimation(anim1);
			tsutsuroth.setChargingAttack(true);
			TaskManager.submit(new Task(2, target, false) {
				int tick = 0;
				@Override
				public void execute() {
					switch(tick) {
					case 0:
						for (Player t : Misc.getCombinedPlayerList(target)) {
							if(t == null || t.getLocation() != Location.GODWARS_DUNGEON || t.isTeleporting())
								continue;
							if(t.getPosition().distanceToPoint(tsutsuroth.getPosition().getX(), tsutsuroth.getPosition().getY()) > 20)
								continue;
							new Projectile(tsutsuroth, target, graphic1.getId(), 44, 3, 43, 43, 0).sendProjectile();
						}
						break;
					case 2:
						for (Player t : Misc.getCombinedPlayerList(target)) {
							if(t == null || t.getLocation() != Location.GODWARS_DUNGEON)
								continue;
							target.performGraphic(graphic2);
							tsutsuroth.getCombatBuilder().setVictim(t);
							new CombatHit(tsutsuroth.getCombatBuilder(), new CombatContainer(tsutsuroth, t, 1, CombatType.MAGIC, true)).handleAttack();
						}
						tsutsuroth.setChargingAttack(false);
						stop();
						break;
					}
					tick++;
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
