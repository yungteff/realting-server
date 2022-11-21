package com.realting.model.entity.character.player;

import com.realting.model.RegionInstance.RegionInstanceType;
import com.realting.model.entity.character.GroundItemManager;
import com.realting.world.content.LoyaltyProgramme;
import com.realting.world.content.combat.pvp.BountyHunter;
import com.realting.world.content.player.skill.construction.House;

public class PlayerProcess {

	/*
	 * The player (owner) of this instance
	 */
	private final Player player;

	/*
	 * The loyalty tick, once this reaches 6, the player
	 * will be given loyalty points.
	 * 6 equals 3.6 seconds.
	 */
	private int loyaltyTick;

	/*
	 * The timer tick, once this reaches 2, the player's
	 * total play time will be updated.
	 * 2 equals 1.2 seconds.
	 */
	private int timerTick;

	/*
	 * Makes sure ground items are spawned on height change
	 */
	private int previousHeight;

	public PlayerProcess(Player player) {
		this.player = player;
		this.previousHeight = player.getEntityPosition().getZ();
	}

	public void sequence() {
		player.getInventory().processRefreshItems();

		/** COMBAT **/
		player.getCombatBuilder().process();
		
		/** SKILLS **/
		if(player.shouldProcessFarming()) {
			player.getFarming().sequence();
		}

		/** MISC **/

		if(previousHeight != player.getEntityPosition().getZ()) {
			GroundItemManager.handleRegionChange(player);
			previousHeight = player.getEntityPosition().getZ();
		}

		if(!player.isInActive()) {
			if(loyaltyTick >= 6) {
				LoyaltyProgramme.incrementPoints(player);
				loyaltyTick = 0;
			}
			loyaltyTick++;
		}
		
		/*if(timerTick >= 1) {
		 * HANDLED BY PlayerPanel
			player.getPacketSender().sendString(39166, "@or2@Time played:  @yel@"+Misc.getTimePlayed((player.getTotalPlayTime() + player.getRecordedLogin().elapsed())));
			timerTick = 0;
		}*/
		timerTick++;
		
		BountyHunter.sequence(player);
		
		if(player.getRegionInstance() != null && (player.getRegionInstance().getType() == RegionInstanceType.CONSTRUCTION_HOUSE || player.getRegionInstance().getType() == RegionInstanceType.CONSTRUCTION_DUNGEON)) {
			((House)player.getRegionInstance()).process();
		}
	}
}
