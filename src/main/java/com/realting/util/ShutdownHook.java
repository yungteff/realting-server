package com.realting.util;

import java.util.logging.Logger;

import com.realting.GameServer;
import com.realting.world.World;
import com.realting.world.content.WellOfGoodwill;
import com.realting.world.content.clan.ClanChatManager;
import com.realting.world.content.grandexchange.GrandExchangeOffers;
import com.realting.model.entity.character.player.Player;
import com.realting.model.entity.character.player.PlayerHandler;

public class ShutdownHook extends Thread {

	/**
	 * The ShutdownHook logger to print out information.
	 */
	private static final Logger logger = Logger.getLogger(ShutdownHook.class.getName());

	@Override
	public void run() {
		logger.info("The shutdown hook is processing all required actions...");
		//World.savePlayers();
		GameServer.setUpdating(true);
		for (Player player : World.getPlayers()) {
			if (player != null) {
			//	World.deregister(player);
				PlayerHandler.handleLogout(player, false);
			}
		}
		WellOfGoodwill.save();
		GrandExchangeOffers.save();
		ClanChatManager.save();
		logger.info("The shudown hook actions have been completed, shutting the server down...");
	}
}
