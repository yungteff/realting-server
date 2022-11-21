package com.realting.net.packet.impl;

import com.realting.model.Animation;
import com.realting.model.PlayerRights;
import com.realting.model.Position;
import com.realting.model.entity.character.player.Player;
import com.realting.net.packet.Packet;
import com.realting.net.packet.PacketListener;
import com.realting.world.content.minigames.Dueling;
import com.realting.world.content.minigames.Dueling.DuelRule;

/**
 * This packet listener is called when a player has clicked on 
 * either the mini-map or the actual game map to move around.
 * 
 * @author Gabriel Hannason
 */

public class MovementPacketListener implements PacketListener {

	@Override
	public void handleMessage(Player player, Packet packet) {
		boolean teleport = packet.getOpcode() == TELEPORT_MOVEMENT_OPCODE;
		int size = packet.getSize();
		
		if (packet.getOpcode() == 248)
			size -= 14;

		player.setEntityInteraction(null);
		player.getSkillManager().stopSkilling();
		player.getMovementQueue().setFollowCharacter(null);
		
		if(packet.getOpcode() != COMMAND_MOVEMENT_OPCODE) {
			player.setWalkToTask(null);
			player.setCastSpell(null);
			player.getCombatBuilder().setDistanceSession(null);
			player.getCombatBuilder().cooldown(false);
		}

		if(!checkReqs(player, packet.getOpcode()))
			return;

		player.getPacketSender().sendInterfaceRemoval();
		player.setTeleporting(false);
		player.setInactive(false);

		final int steps = (size - 5) / 2;
		if (steps < 0)
			return;
		final int firstStepX = packet.readLEShortA();
		final int[][] path = new int[steps][2];
		for (int i = 0; i < steps; i++) {
			path[i][0] = packet.readByte();
			path[i][1] = packet.readByte();
		}
		final int firstStepY = packet.readLEShort();
		final Position[] positions = new Position[steps + 1];
		positions[0] = new Position(firstStepX, firstStepY, player.getEntityPosition().getZ());
		
		boolean invalidStep = false;
		
		if (teleport && steps > 0 && player.getRights() == PlayerRights.DEVELOPER) {
			player.moveTo(new Position(path[steps-1][0] + firstStepX, path[steps-1][1] + firstStepY, player.getEntityPosition().getZ()));
			return;
		}

		if(!positions[0].isWithinDistance(player.getEntityPosition(), 40)) {
			invalidStep = true;
		} else {
			for (int i = 0; i < steps; i++) {
				positions[i + 1] = new Position(path[i][0] + firstStepX, path[i][1] + firstStepY, player.getEntityPosition().getZ());
				if(!positions[i+1].isWithinDistance(player.getEntityPosition(), 40)) {
					invalidStep = true;
					break;
				}
			}
		}
		
		if(invalidStep) {
			player.getMovementQueue().reset();
			return;
		}

		if (player.getMovementQueue().addFirstStep(positions[0])) {
			for (int i = 1; i < positions.length; i++) {
				player.getMovementQueue().addStep(positions[i]);
			}
		}
	}

	public boolean checkReqs(Player player, int opcode) {
		if (player.isStunned()) {
			if (opcode != COMMAND_MOVEMENT_OPCODE) {
				player.getPacketSender().sendMessage("You're stunned.");
			}
			return false;
		}
		if (player.isFrozen()) {
			if(opcode != COMMAND_MOVEMENT_OPCODE) {
				player.getPacketSender().sendMessage("A magical spell has made you unable to move.");
			}
			return false;
		}
		if(player.getTrading().inTrade() && System.currentTimeMillis() - player.getTrading().lastAction <= 1000) {
			return false;
		}
		if(Dueling.checkRule(player, DuelRule.NO_MOVEMENT)) {
			if(opcode != COMMAND_MOVEMENT_OPCODE)
				player.getPacketSender().sendMessage("Movement has been turned off in this duel!");
			return false;
		}
		if(player.isResting()) {
			player.setResting(false);
			player.performAnimation(new Animation(11788));
			return false;
		}
		if(player.isPlayerLocked() || player.isCrossingObstacle())
			return false;
		if(player.isNeedsPlacement()) {
			return false;
		}
		return !player.getMovementQueue().isLockedMovement();
	}

	public static final int TELEPORT_MOVEMENT_OPCODE = 242;
	public static final int COMMAND_MOVEMENT_OPCODE = 98;
	public static final int GAME_MOVEMENT_OPCODE = 164;
	public static final int MINIMAP_MOVEMENT_OPCODE = 248;

}