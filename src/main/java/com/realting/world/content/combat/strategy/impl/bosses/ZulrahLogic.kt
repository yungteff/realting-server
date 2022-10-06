package com.realting.world.content.combat.strategy.impl.bosses;

import com.realting.engine.task.Task;
import com.realting.engine.task.TaskManager;
import com.realting.model.Animation;
import com.realting.model.CombatIcon;
import com.realting.model.Graphic;
import com.realting.model.Hit;
import com.realting.model.Hitmask;
import com.realting.model.Locations.Location;
import com.realting.model.Position;
import com.realting.model.Projectile;
import com.realting.model.RegionInstance.RegionInstanceType;
import com.realting.model.Skill;
import com.realting.util.Misc;
import com.realting.world.World;
import com.realting.world.content.combat.CombatContainer;
import com.realting.world.content.combat.CombatType;
import com.realting.world.content.combat.strategy.CombatStrategy;
import com.realting.model.entity.character.CharacterEntity;
import com.realting.model.entity.character.npc.NPC;
import com.realting.model.entity.character.player.Player;
import org.apache.commons.lang3.StringUtils;

public class ZulrahLogic implements CombatStrategy {
	
	public static int ticksPerPhase = 20;
	
	public static int[] phase = { 2042, 2044, 2043 };
	
	public static Position[] move  = { new Position(3431, 2781), new Position(3421, 2771), new Position(3423, 2781), 
										new Position(3415, 2773) }; //new Position(3415, 2780), 
	
	private static Animation shoot = new Animation(5069), 
							charge = new Animation(5806), 
							melee = new Animation(5807), 
							dive = new Animation(5072), 
							rise = new Animation(5073);
	
	private static Graphic toxic_cloud = new Graphic(310), 
								fire = new Graphic(78), 
								snakeling_summon = new Graphic(281);
	
	
	private static void switchPhase(CharacterEntity entity, CharacterEntity victim) {
		//System.out.println("Switching phase...");
		NPC zulrah = (NPC)entity;
		Player player = (Player)victim;
		zulrah.performAnimation(dive);
		int currenthealth = zulrah.getConstitution();
		if (zulrah != null && zulrah.getConstitution() > 0 && zulrah.isRegistered()) {
			World.deregister(zulrah);
		
			TaskManager.submit(new Task(1, player, false) {
				int tick = 0;
				@Override
				public void execute() {
	
					if (tick == 5 && !zulrah.isRegistered()) {
						
						if (victim == null ||  player.getConstitution() <= 0 || player.getLocation() != Location.ZULRAH || zulrah.getConstitution() <= 0) {
							stop();
							player.getMinigameAttributes().getZulrahAttributes().setRedFormDamage(0, false);
							zulrah.setChargingAttack(false);
							if(player.getRegionInstance() != null && player.getRegionInstance().equals(RegionInstanceType.ZULRAH)) {
								World.deregister(zulrah);
								player.getRegionInstance().destruct();
							}
							return;
						}
						
						int rand = Misc.randomMinusOne(move.length);
						NPC zulrah = new NPC(phase[Misc.randomMinusOne(phase.length)], new Position(move[rand].getX(), move[rand].getY(), player.getPosition().getZ()));
						World.register(zulrah);
						zulrah.setPositionToFace(player.getPosition());
						zulrah.performAnimation(rise);
						zulrah.setConstitution(currenthealth);
						zulrah.getCombatBuilder().attack(player);
						stop();
					}
					tick++;
				} 
			});
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
		if(entity.getPosition().getZ() == 0) {
			World.deregister(entity);
		}
		if(victim.getConstitution() <= 0) {
			World.deregister(entity);
		}
		NPC zulrah = (NPC)entity;
		Player player = (Player)victim;
		if(zulrah.isChargingAttack()) {
			return true;
		}
		if (zulrah.getId() == phase[0]) { //do green phase
			if (Misc.getRandom(1) == 0) {
				TaskManager.submit(new Task(zulrah.getAttackSpeed(), zulrah, false) {
					int tick = 0;
					@Override
					public void execute() {
						if (entity == null || victim == null || zulrah.getConstitution() <= 0 || player.getConstitution() <= 0 || (zulrah.getLocation() != player.getLocation())) {
							stop();
							player.getMinigameAttributes().getZulrahAttributes().setRedFormDamage(0, false);
							zulrah.setChargingAttack(false);
							if(player.getRegionInstance() != null && player.getRegionInstance().equals(RegionInstanceType.ZULRAH)) {
								World.deregister(zulrah);
								player.getRegionInstance().destruct();
							}
							return;
						}
						if (tick >= ticksPerPhase) {
							stop();
							player.getMinigameAttributes().getZulrahAttributes().setRedFormDamage(0, false);
							zulrah.setChargingAttack(false);
							switchPhase(entity, victim);
							return;
						}
						if (Misc.isEven(tick)) { 
							zulrah.setChargingAttack(true);
							zulrah.getCombatBuilder().setContainer(new CombatContainer(zulrah, victim, 1, 1, CombatType.MAGIC, true));
							zulrah.performAnimation(shoot);
							new Projectile(zulrah, victim, 2733, 44, 3, 43, 31,	0).sendProjectile(); //fire blast
						} else { //do range attack
							zulrah.setChargingAttack(true);
							zulrah.getCombatBuilder().setContainer(new CombatContainer(zulrah, victim, 1, 1, CombatType.RANGED, true));
							zulrah.performAnimation(shoot);
							new Projectile(zulrah, victim, 551, 44, 3, 43, 31, 0).sendProjectile(); //chaos elemental green
						}
						tick++;
					}
				});
			} else {
				TaskManager.submit(new Task(zulrah.getAttackSpeed(), zulrah, false) {
					int tick = 0;
					@Override
					public void execute() {
						if (entity == null || victim == null || zulrah.getConstitution() <= 0 || player.getConstitution() <= 0 || (zulrah.getLocation() != player.getLocation())) {
							stop();
							player.getMinigameAttributes().getZulrahAttributes().setRedFormDamage(0, false);
							zulrah.setChargingAttack(false);
							if(player.getRegionInstance() != null && player.getRegionInstance().equals(RegionInstanceType.ZULRAH)) {
								World.deregister(zulrah);
								player.getRegionInstance().destruct();
							}
							return;
						}
						if (tick >= ticksPerPhase) {
							stop();
							player.getMinigameAttributes().getZulrahAttributes().setRedFormDamage(0, false);
							zulrah.setChargingAttack(false);
							switchPhase(entity, victim);
							return;
						}
						zulrah.setChargingAttack(true);
						zulrah.getCombatBuilder().setContainer(new CombatContainer(zulrah, victim, 1, 1, CombatType.RANGED, true));
						zulrah.performAnimation(shoot);
						new Projectile(zulrah, victim, 551, 44, 3, 43, 31, 0).sendProjectile(); //chaos elemental green
						tick++;
					}
				});
			}
		}
		if (zulrah.getId() == phase[1]) { //do blue phase
			if (Misc.getRandom(1) == 0) {
				TaskManager.submit(new Task(zulrah.getAttackSpeed(), zulrah, false) {
					int tick = 0;
					@Override
					public void execute() {
						if (entity == null || victim == null || zulrah.getConstitution() <= 0 || player.getConstitution() <= 0 || (zulrah.getLocation() != player.getLocation())) {
							stop();
							player.getMinigameAttributes().getZulrahAttributes().setRedFormDamage(0, false);
							zulrah.setChargingAttack(false);
							if(player.getRegionInstance() != null && player.getRegionInstance().equals(RegionInstanceType.ZULRAH)) {
								World.deregister(zulrah);
								player.getRegionInstance().destruct();
							}
							return;
						}
						if (tick >= ticksPerPhase) {
							//zulrah.forceChat("phase done");
							stop();
							player.getMinigameAttributes().getZulrahAttributes().setRedFormDamage(0, false);
							zulrah.setChargingAttack(false);
							switchPhase(entity, victim);
							return;
						}
						zulrah.setChargingAttack(true);
						if (Misc.inclusiveRandom(1, 3) < 3) { 
							zulrah.getCombatBuilder().setContainer(new CombatContainer(zulrah, victim, 1, 1, CombatType.MAGIC, true));
							zulrah.performAnimation(shoot);
							new Projectile(zulrah, victim, 2733, 44, 3, 43, 31,	0).sendProjectile(); //fire blast
						} else { //do range attack
							zulrah.getCombatBuilder().setContainer(new CombatContainer(zulrah, victim, 1, 1, CombatType.RANGED, true));
							zulrah.performAnimation(shoot);
							new Projectile(zulrah, victim, 551, 44, 3, 43, 31, 0).sendProjectile(); //chaos elemental green
						}
						tick++;
					}
				});
			} else {
				TaskManager.submit(new Task(zulrah.getAttackSpeed(), zulrah, false) {
					int tick = 0;
					@Override
					public void execute() {
						if (entity == null || victim == null || zulrah.getConstitution() <= 0 || player.getConstitution() <= 0 || (zulrah.getLocation() != player.getLocation())) {
							stop();
							player.getMinigameAttributes().getZulrahAttributes().setRedFormDamage(0, false);
							zulrah.setChargingAttack(false);
							if(player.getRegionInstance() != null && player.getRegionInstance().equals(RegionInstanceType.ZULRAH)) {
								World.deregister(zulrah);
								player.getRegionInstance().destruct();
							}
							return;
						}
						if (tick >= ticksPerPhase) {
							stop();
							player.getMinigameAttributes().getZulrahAttributes().setRedFormDamage(0, false);
							zulrah.setChargingAttack(false);
							switchPhase(entity, victim);
							return;
						}
						zulrah.setChargingAttack(true);
						zulrah.getCombatBuilder().setContainer(new CombatContainer(zulrah, victim, 1, 1, CombatType.MAGIC, true));
						zulrah.performAnimation(shoot);
						new Projectile(zulrah, victim, 2733, 44, 3, 43, 31,	0).sendProjectile(); //fire blast
						tick++;
					}
				});
			}
		}
		if (zulrah.getId() == phase[2]) { //do red phase
			//zulrah.forceChat("Current position: "+zulrah.getPosition().getX()+", "+zulrah.getPosition().getY()+", "+zulrah.getPosition().getZ());
			TaskManager.submit(new Task(zulrah.getAttackSpeed(), zulrah, false) {
				int tick = 0;
				@Override
				public void execute() {
					if (entity == null || victim == null || zulrah.getConstitution() <= 0 || player.getConstitution() <= 0 || (zulrah.getLocation() != player.getLocation())) {
						player.getMinigameAttributes().getZulrahAttributes().setRedFormDamage(0, false);
						stop();
						zulrah.setChargingAttack(false);
						if(player.getRegionInstance() != null && player.getRegionInstance().equals(RegionInstanceType.ZULRAH)) {
							World.deregister(zulrah);
							player.getRegionInstance().destruct();
						}
						return;
					}
					zulrah.setChargingAttack(true);
					if (player.getMinigameAttributes().getZulrahAttributes().getRedFormDamage() >= player.getSkillManager().getCurrentLevel(Skill.CONSTITUTION)) {
						
						String hiss = "Hi";
						hiss = hiss + StringUtils.repeat("s", Misc.inclusiveRandom(2, 8));
						hiss = hiss + StringUtils.repeat("!", Misc.inclusiveRandom(1, 4));
						zulrah.forceChat(hiss);
						
					}
					
					if (tick < ticksPerPhase-5) {
						zulrah.getCombatBuilder().setContainer(new CombatContainer(zulrah, victim, 1, 1, CombatType.MAGIC, true));
						zulrah.performAnimation(shoot);
						new Projectile(zulrah, victim, 2215, 44, 3, 43, 31,	0).sendProjectile(); //fire blast
					}
					
					if (tick == (ticksPerPhase-5)) {
						zulrah.performAnimation(melee);
					}
					if (tick >= (ticksPerPhase)) {
						player.dealDoubleDamage(new Hit(zulrah, player.getMinigameAttributes().getZulrahAttributes().getRedFormDamage()/2, Hitmask.LIGHT_YELLOW, CombatIcon.DEFLECT),
								new Hit(zulrah, player.getMinigameAttributes().getZulrahAttributes().getRedFormDamage()/2, Hitmask.LIGHT_YELLOW, CombatIcon.DEFLECT));
						stop();
						player.getMinigameAttributes().getZulrahAttributes().setRedFormDamage(0, false);
						zulrah.setChargingAttack(false);
						switchPhase(entity, victim);
						return;
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
		return 20;
	}

	@Override
	public CombatType getCombatType(CharacterEntity entity) {
		return CombatType.MIXED;
	}

}
