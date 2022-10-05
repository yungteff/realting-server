package com.realting.net.packet.impl

import com.realting.model.Locations
import com.realting.model.entity.character.player.Player
import com.realting.net.packet.Packet
import com.realting.net.packet.PacketListener
import com.realting.util.Misc
import com.realting.world.World
import com.realting.world.content.PlayerLogs
import com.realting.world.content.combat.CombatFactory.Companion.checkAttackDistance
import com.realting.world.content.player.skill.dungeoneering.UltimateIronmanHandler.hasItemsStored

/**
 * This packet listener is called when a player has clicked on another player's
 * menu actions.
 *
 * @author relex lawl
 */
class PlayerOptionPacketListener : PacketListener {
    override fun handleMessage(player: Player, packet: Packet) {
        if (player.constitution <= 0) return
        if (player.isTeleporting) return
        when (packet.opcode) {
            153 -> attack(player, packet)
            128 -> option1(player, packet)
            37 -> option2(player, packet)
            227 -> option3(player, packet)
        }
    }

    companion object {
        private fun attack(player: Player, packet: Packet) {
            val index = packet.readLEShort().toInt()
            if (index > World.getPlayers().capacity() || index < 0) return
            val attacked = World.getPlayers()[index]
            if (Misc.checkForOwner()) {
                World.sendOwnerDevMessage(player.username + " attacked index: " + index + ", target = " + attacked.username)
            }
            if (attacked == player) {
                player.movementQueue.reset()
                World.sendStaffMessage("[BUG TRACKER] Error 959.1 has occured. PLEASE REPORT TO CRIMSON!")
                PlayerLogs.log(
                    "1 - PVP BUGS",
                    "Error 959.1 PVP bug occured with " + player.username + " attacking " + attacked.username + ". Pos(p): " + player.position.toString() + " Pos(a): " + attacked.position.toString()
                )
                println("Bug Found [959.1]: Attacker: " + player.username + " Player Attacked: " + attacked.username)
                return
            }
            if (attacked.constitution <= 0) {
                return
            }
            if (player.location === Locations.Location.DUEL_ARENA && player.dueling.duelingStatus == 0) {
                player.dueling.challengePlayer(attacked)
                return
            }
            if (hasItemsStored(player) && player.location !== Locations.Location.DUNGEONEERING) {
                player.packetSender.sendMessage("You must claim your stored items at Dungeoneering first.")
                player.movementQueue.reset()
                return
            }
            if (player.equipment.contains(20171) && player.location !== Locations.Location.FREE_FOR_ALL_ARENA) {
                player.packetSender.sendMessage("Zaryte is disabled in PvP.")
                player.movementQueue.reset()
                return
            }
            if (player.combatBuilder.strategy == null) {
                player.combatBuilder.determineStrategy()
            }
            if (checkAttackDistance(player, attacked)) {
                //confirmed this is called all the time, but shouldn't fuck with people fighting. http://i.imgur.com/qUFhl5L.png
                player.movementQueue.reset()
            }
            if (player.combatBuilder.attackTimer <= 0) player.combatBuilder.attack(attacked)
        }

        /**
         * Manages the first option click on a player option menu.
         *
         * @param player
         * The player clicking the other entity.
         * @param packet
         * The packet to read values from.
         */
        private fun option1(player: Player, packet: Packet) {
            val id: Int = packet.readShort().toInt()
            if (id < 0 || id > World.getPlayers().capacity()) return
            val victim = World.getPlayers()[id] ?: return
            /*
		 * GameServer.getTaskScheduler().schedule(new WalkToTask(player,
		 * victim.getPosition(), new FinalizedMovementTask() {
		 * 
		 * @Override public void execute() { //do first option here } }));
		 */
        }

        /**
         * Manages the second option click on a player option menu.
         *
         * @param player
         * The player clicking the other entity.
         * @param packet
         * The packet to read values from.
         */
        private fun option2(player: Player, packet: Packet) {
            val id: Int = packet.readShort().toInt()
            if (id < 0 || id > World.getPlayers().capacity()) return
            val victim = World.getPlayers()[id] ?: return
            /*
		 * GameServer.getTaskScheduler().schedule(new WalkToTask(player,
		 * victim.getPosition(), new FinalizedMovementTask() {
		 * 
		 * @Override public void execute() { //do second option here } }));
		 */
        }

        /**
         * Manages the third option click on a player option menu.
         *
         * @param player
         * The player clicking the other entity.
         * @param packet
         * The packet to read values from.
         */
        private fun option3(player: Player, packet: Packet) {
            val id: Int = packet.readLEShortA().toInt()
            if (id < 0 || id > World.getPlayers().capacity()) return
            val victim = World.getPlayers()[id] ?: return
            /*
		 * GameServer.getTaskScheduler().schedule(new WalkToTask(player,
		 * victim.getPosition(), new FinalizedMovementTask() {
		 * 
		 * @Override public void execute() { //do third option here } }));
		 */
        }
    }
}