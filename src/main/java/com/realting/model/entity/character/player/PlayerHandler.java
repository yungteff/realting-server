package com.realting.model.entity.character.player;

import com.realting.DiscordBot.JavaCord;
import com.realting.GameServer;
import com.realting.GameSettings;
import com.realting.engine.task.TaskManager;
import com.realting.engine.task.impl.*;
import com.realting.model.*;
import com.realting.model.Locations.Location;
import com.realting.model.container.impl.Bank;
import com.realting.model.container.impl.Equipment;
import com.realting.model.definitions.WeaponAnimations;
import com.realting.model.definitions.WeaponInterfaces;
import com.realting.model.entity.character.GlobalItemSpawner;
import com.realting.net.PlayerSession;
import com.realting.net.SessionState;
import com.realting.net.security.ConnectionHandler;
import com.realting.util.Misc;
import com.realting.world.World;
import com.realting.world.clip.region.RegionClipping;
import com.realting.world.content.*;
import com.realting.world.content.clan.ClanChatManager;
import com.realting.world.content.combat.effect.CombatPoisonEffect;
import com.realting.world.content.combat.effect.CombatTeleblockEffect;
import com.realting.world.content.combat.magic.Autocasting;
import com.realting.world.content.combat.prayer.CurseHandler;
import com.realting.world.content.combat.prayer.PrayerHandler;
import com.realting.world.content.combat.pvp.BountyHunter;
import com.realting.world.content.combat.range.DwarfMultiCannon;
import com.realting.world.content.combat.weapon.CombatSpecial;
import com.realting.world.content.dialogue.DialogueManager;
import com.realting.world.content.minigames.Barrows;
import com.realting.world.content.player.events.Achievements;
import com.realting.world.content.player.events.BonusManager;
import com.realting.world.content.player.events.Lottery;
import com.realting.world.content.player.skill.hunter.Hunter;
import com.realting.world.content.player.skill.slayer.Slayer;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.mindrot.jbcrypt.BCrypt;

import java.awt.*;

public class PlayerHandler {

	public static void handleLogin(Player player) {
		//Register the player
		System.out.println("[World] Registering player - [username, host] : [" + player.getUsername() + ", " + player.getHostAddress() + "]");
		player.getPacketSender().sendSmallImageKey("fabulous"); // 'fabulous' is the name of the gnome child image.
		player.getPacketSender().sendRichPresenceDetails("Logged in as: " + player.getUsername());
		player.getPacketSender().sendRichPresenceState("Players Online: " + World.getPlayers().size());
		ConnectionHandler.add(player.getHostAddress());
		World.getPlayers().add(player);
		World.updatePlayersOnline();
		PlayersOnlineInterface.add(player);
		player.getSession().setState(SessionState.LOGGED_IN);

		//Packets
		player.getPacketSender().sendOsrsRegions(RegionClipping.OSRS_REGIONS).sendMapRegion().sendDetails();

		player.getRecordedLogin().reset();


		//Tabs
		player.getPacketSender().sendTabs();

		//Setting up the player's item containers..
		for(int i = 0; i < player.getBanks().length; i++) {
			if(player.getBank(i) == null) {
				player.setBank(i, new Bank(player));
			}
		}
		player.getInventory().refreshItems();
		player.getEquipment().refreshItems();

		//Weapons and equipment..
		WeaponAnimations.update(player);
		WeaponInterfaces.assign(player, player.getEquipment().get(Equipment.WEAPON_SLOT));
		CombatSpecial.updateBar(player);
		BonusManager.update(player);

		//Skills
		player.getSummoning().login();
		player.getFarming().load();
		Slayer.checkDuoSlayer(player, true);
		for (Skill skill : Skill.values()) {
			player.getSkillManager().updateSkill(skill);
		}

		//Relations
		player.getRelations().setPrivateMessageId(1).onLogin(player).updateLists(true);

		//Client configurations
		player.getPacketSender().sendConfig(172, player.isAutoRetaliate() ? 1 : 0)
		.sendTotalXp(player.getSkillManager().getTotalGainedExp())
		.sendConfig(player.getFightType().getParentId(), player.getFightType().getChildId())
		.sendRunStatus().sendRunEnergy(player.getRunEnergy()).sendRights()
		.sendString(8135, ""+player.getMoneyInPouch())
		.sendInteractionOption("Follow", 3, false)
		.sendInteractionOption("Trade With", 4, false);
		//.sendInterfaceRemoval().sendString(39161, "@or2@Server time: @or2@[ @yel@"+Misc.getCurrentServerTime()+"@or2@ ]");

		Autocasting.onLogin(player);
		PrayerHandler.deactivateAll(player);
		CurseHandler.deactivateAll(player);
		BonusManager.sendCurseBonuses(player);
		Achievements.updateInterface(player);
		Barrows.updateInterface(player);

		//Tasks
		TaskManager.submit(new PlayerSkillsTask(player));
		TaskManager.submit(new PlayerRegenConstitutionTask(player));
		TaskManager.submit(new SummoningRegenPlayerConstitutionTask(player));
		if (player.isPoisoned()) {
			TaskManager.submit(new CombatPoisonEffect(player));
		}
		if(player.getPrayerRenewalPotionTimer() > 0) {
			TaskManager.submit(new PrayerRenewalPotionTask(player));
		}
		if(player.getOverloadPotionTimer() > 0) {
			TaskManager.submit(new OverloadPotionTask(player));
		}
		if (player.getTeleblockTimer() > 0) {
			TaskManager.submit(new CombatTeleblockEffect(player));
		}
		if (player.getSkullTimer() > 0) {
			player.setSkullIcon(1);
			TaskManager.submit(new CombatSkullEffect(player));
		}
		if(player.getFireImmunity() > 0) {
			FireImmunityTask.makeImmune(player, player.getFireImmunity(), player.getFireDamageModifier());
		}
		if(player.getSpecialPercentage() < 100) {
			TaskManager.submit(new PlayerSpecialAmountTask(player));
		}
		if(player.hasStaffOfLightEffect()) {
			TaskManager.submit(new StaffOfLightSpecialAttackTask(player));
		}
		if(player.getMinutesBonusExp() >= 0) {
			TaskManager.submit(new BonusExperienceTask(player));
		}

		//Update appearance
		

		//Others
		Lottery.onLogin(player);
		Locations.login(player);
		player.getPacketSender().sendMessage("@bla@Welcome to "+GameSettings.RSPS_NAME+"!");
		if(player.experienceLocked())
			player.getPacketSender().sendMessage(MessageType.SERVER_ALERT, " @red@Warning: your experience is currently locked.");
		
		if (!player.getRights().OwnerDeveloperOnly() && player.getSkillManager().getExperience(Skill.CONSTRUCTION) > 1) {
			player.getSkillManager().setExperience(Skill.CONSTRUCTION, 0);
			player.getSkillManager().setMaxLevel(Skill.CONSTRUCTION, 1);
			player.getSkillManager().setCurrentLevel(Skill.CONSTRUCTION, 1, true);
		}
		
		
		if (GameServer.getConfiguration().isEncryptPasswords() && Misc.needsNewSalt(player.getSalt())) {
			player.setSalt(BCrypt.gensalt(GameSettings.BCRYPT_ROUNDS));
		}
		
		if(Misc.isWeekend()) {
			player.getPacketSender().sendMessage("<img=10> <col=ff00ff>"+GameSettings.RSPS_NAME+" currently has DOUBLE EXP active, and it STACKS with vote scrolls! Enjoy!");
			//player.getPacketSender().sendMessage("<img=10> <col=ff00ff>Oh, and this weekend we're having double vote points as well!");
		}
		
		if (Wildywyrm.wyrmAlive) {
			Wildywyrm.sendHint(player);
		}
		
		if(WellOfGoodwill.isActive()) {
			player.getPacketSender().sendMessage(MessageType.SERVER_ALERT, "The Well of Goodwill is granting 30% bonus experience for another "+WellOfGoodwill.getMinutesRemaining()+" minutes.");
		}

		PlayerPanel.refreshPanel(player);


		//New player
		if(player.newPlayer()) {
			player.setClanChatName("kandarin");
			player.setPlayerLocked(true).setDialogueActionId(45);
			DialogueManager.start(player, 81);
			JavaCord.sendEmbed("ingame-announcements", new EmbedBuilder().setTitle("New adventurer!") .setDescription(player.getUsername() + " just joined " + GameSettings.RSPS_NAME +"! Your adventure starts now!")
					.setColor(Color.BLUE).setTimestampToNow()
					.setThumbnail("https://vignette.wikia.nocookie.net/2007scape/images/f/ff/Vorkath%27s_stuffed_head_detail.png/revision/latest?cb=20180108212531").setFooter("Powered by JavaCord"));

		}
		
		ClanChatManager.handleLogin(player);
		
		player.getPacketSender().updateSpecialAttackOrb().sendIronmanMode(player.getGameMode().ordinal());

		if(player.getRights() == PlayerRights.SUPPORT)
			World.sendMessage(MessageType.PLAYER_ALERT, ("<shad=0><col="+player.getYellHex()+"> Support Member "+player.getUsername()+" has just logged in, feel free to message them for help!"));
		if(player.getRights() == PlayerRights.MODERATOR)
			World.sendMessage(MessageType.PLAYER_ALERT, ("<shad=0><col="+player.getYellHex()+"> Moderator "+player.getUsername()+" has just logged in."));
		if(player.getRights() == PlayerRights.ADMINISTRATOR)
			World.sendMessage(MessageType.PLAYER_ALERT, ("<shad=0><col="+player.getYellHex()+"> Administrator "+player.getUsername()+" has just logged in."));
		if(player.getRights() ==PlayerRights.DEVELOPER)
			World.sendMessage(MessageType.PLAYER_ALERT, ("<shad=0><col="+player.getYellHex()+"> Developer "+player.getUsername()+" has just logged in."));
		if(player.getRights() ==PlayerRights.OWNER)
			World.sendMessage(MessageType.PLAYER_ALERT, ("<shad=0><col="+player.getYellHex()+">Owner "+player.getUsername()+" has just logged in."));
		
		//GrandExchange.onLogin(player);
		
		if(player.getPointsHandler().getAchievementPoints() == 0) {
			Achievements.setPoints(player);
		}
		
		player.getUpdateFlag().flag(Flag.APPEARANCE);
		PlayerLogs.log(player.getUsername(), "Login. ip: "+player.getHostAddress()+", mac: "+player.getMac()+", uuid: "+player.getUUID());
		/* if(player.getSkillManager().getCurrentLevel(Skill.CONSTITUTION) == 0){
			player.getSkillManager().setCurrentLevel(Skill.CONSTITUTION, 1);
			World.deregister(player);
			System.out.println(player.getUsername()+" logged in from a bad session. They have 0 HP and are nulled. Set them to 1 and kicked them.");
			// TODO this may cause dupes. removed temp.
		} */
		if (player.isInDung()) {
			System.out.println(player.getUsername()+" logged in from a bad dungeoneering session.");
			PlayerLogs.log(player.getUsername(), " logged in from a bad dungeoneering session. Inv/equipment wiped.");
			player.getInventory().resetItems().refreshItems();
			player.getEquipment().resetItems().refreshItems();
			if (player.getLocation() == Location.DUNGEONEERING) {
				player.moveTo(GameSettings.DEFAULT_POSITION.copy());
			}
			player.getPacketSender().sendMessage("Your Dungeon has been disbanded.");
			player.setInDung(false);
		}
		if (player.getLocation() == Location.GRAVEYARD && player.getEntityPosition().getY() > 3566) {
			PlayerLogs.log(player.getUsername(), "logged in inside the graveyard arena, moved their ass out.");
			player.moveTo(new Position(3503, 3565, 0));
			player.setPositionToFace(new Position(3503, 3566));
			player.getPacketSender().sendMessage("You logged off inside the graveyard arena. Moved you to lobby area.");
		}
		if (player.getEntityPosition().getX() == 3004 && player.getEntityPosition().getY() >= 3938 && player.getEntityPosition().getY() <= 3949) {
			PlayerLogs.log(player.getUsername(), player.getUsername()+" was stuck in the obstacle pipe in the Wild.");
			player.moveTo(new Position(3006, player.getEntityPosition().getY(), player.getEntityPosition().getZ()));
			player.getPacketSender().sendMessage("You logged off inside the obstacle pipe, moved out.");
		}
		GlobalItemSpawner.spawnGlobalGroundItems(player);
		player.unlockPkTitles();
		//player.getPacketSender().sendString(39160, "@or2@Players online:   @or2@[ @yel@"+(int)(World.getPlayers().size())+"@or2@ ]"); Handled by PlayerPanel.java
		player.getPacketSender().sendString(57003, "Players:  @gre@"+ World.getPlayers().size());
		
	}

	public static boolean handleLogout(Player player, Boolean forced) {
		try {

			PlayerSession session = player.getSession();
			
			if(session.getChannel().isOpen()) {
				session.getChannel().close();
			}

			if(!player.isRegistered()) {
				return true;
			}

			boolean exception = forced || GameServer.isUpdating() || World.getLogoutQueue().contains(player) && player.getLogoutTimer().elapsed(90000);
			if(player.logout() || exception) {
				//new Thread(new HighscoresHandler(player)).start();
				System.out.println("[World] Deregistering player - [username, host] : [" + player.getUsername() + ", " + player.getHostAddress() + "]");
				player.getSession().setState(SessionState.LOGGING_OUT);
				ConnectionHandler.remove(player.getHostAddress());
				player.setTotalPlayTime(player.getTotalPlayTime() + player.getRecordedLogin().elapsed());
				player.getPacketSender().sendInterfaceRemoval();
				if(player.getCannon() != null) {
					DwarfMultiCannon.pickupCannon(player, player.getCannon(), true);
				}
				if(exception && player.getResetPosition() != null) {
					player.moveTo(player.getResetPosition());
					player.setResetPosition(null);
				}
				if(player.getRegionInstance() != null) {
					player.getRegionInstance().destruct();
				}
				Hunter.handleLogout(player);
				Locations.logout(player);
				player.getSummoning().unsummon(false, false);
				player.getFarming().save();
				BountyHunter.handleLogout(player);
				ClanChatManager.leave(player, false);
				player.getRelations().updateLists(false);
				PlayersOnlineInterface.remove(player);
				TaskManager.cancelTasks(player.getCombatBuilder());
				TaskManager.cancelTasks(player);
				player.save();
				World.getPlayers().remove(player);
				session.setState(SessionState.LOGGED_OUT);
				World.updatePlayersOnline();
				return true;
			} else {
				return false;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return true;
	}
}
