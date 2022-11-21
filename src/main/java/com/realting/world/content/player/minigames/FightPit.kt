package com.realting.world.content.minigames

import com.realting.model.Position
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import com.realting.world.content.dialogue.DialogueManager
import java.util.*

object FightPit {
    var TOTAL_PLAYERS = 0

    /**
     * @note States of minigames
     */
    private const val PLAYING = "PLAYING"
    private const val WAITING = "WAITING"

    /**
     * @note Current fight pits champion
     */
    private var pitsChampion = "None"

    /**
     * @note Countdown for game to start
     */
    private var gameStartTimer = 80

    /**
     * @note Elapsed Game start time
     */
    private var elapsedGameTime = 0
    private const val END_GAME_TIME = 400

    /*
	 * @note Game started or not?
	 */
    private var gameStarted = false

    /**
     * @note Stores player and State
     */
    private val playerMap = Collections.synchronizedMap(HashMap<Player, String>())

    /**
     * @note Where to spawn when pits game starts
     */
    private const val MINIGAME_START_POINT_X = 2392
    private const val MINIGAME_START_POINT_Y = 5139

    /**
     * @note Exit game area
     */
    private const val EXIT_GAME_X = 2399
    private const val EXIT_GAME_Y = 5169

    /**
     * @note Exit waiting room
     */
    const val EXIT_WAITING_X = 2399
    const val EXIT_WAITING_Y = 5177

    /**
     * @note Waiting room coordinates
     */
    private const val WAITING_ROOM_X = 2399
    private const val WAITING_ROOM_Y = 5175

    /**
     * @return HashMap Value
     */
    @JvmStatic
    fun getState(player: Player): String? {
        return playerMap[player]
    }

    private const val TOKKUL_ID = 6529

    /**
     * @note Adds player to waiting room.
     */
    @JvmStatic
    fun addPlayer(player: Player) {
        playerMap[player] = WAITING
        player.moveTo(Position(WAITING_ROOM_X, WAITING_ROOM_Y, 0))
        player.packetSender.sendConfig(560, 1)
        TOTAL_PLAYERS++
    }

    /**
     * @note Starts the game and moves players to arena
     */
    private fun enterGame(player: Player) {
        val l = getState(player) == null || getState(player) == WAITING
        if (l) playerMap[player] = PLAYING
        var teleportToX = MINIGAME_START_POINT_X + Misc.getRandom(12)
        var teleportToY = MINIGAME_START_POINT_Y + Misc.getRandom(12)
        if (!player.movementQueue.canWalk(player.entityPosition.x - teleportToX, player.entityPosition.y - teleportToY)) {
            teleportToX = MINIGAME_START_POINT_X + Misc.getRandom(3)
            teleportToY = MINIGAME_START_POINT_Y + Misc.getRandom(3)
        }
        if (l) {
            player.moveTo(Position(teleportToX, teleportToY, 0))
            player.packetSender.sendInteractionOption("Attack", 2, true)
        }
        player.movementQueue.followCharacter = null
    }

    /**
     * @note Removes player from pits if they're in waiting or in game
     */
    @JvmStatic
    fun removePlayer(player: Player, removeReason: String) {
        when (removeReason.lowercase(Locale.getDefault())) {
            "death" -> {
                player.moveTo(Position(WAITING_ROOM_X, WAITING_ROOM_Y, 0))
                playerMap.remove(player)
                playerMap[player] = WAITING
                endingGame()
            }
            "leave room" -> {
                player.moveTo(Position(EXIT_WAITING_X, EXIT_WAITING_Y, 0))
                if (playerMap.containsKey(player)) {
                    playerMap.remove(player)
                    TOTAL_PLAYERS--
                }
            }
            "leave game" -> {
                player.moveTo(Position(EXIT_GAME_X, EXIT_GAME_Y, 0))
                playerMap.remove(player)
                playerMap[player] = WAITING
                endingGame()
            }
            "logout" -> {
                TOTAL_PLAYERS--
                playerMap.remove(player)
                endingGame()
            }
            "cft" -> removePlayer(player, "logout")
        }
    }

    fun endingGame(): Boolean {
        for (player in playerMap.keys) {
            if (player != null) {
                if (getListCount("PLAYING") == 1 && getState(player) != null && getState(player) == "PLAYING") {
                    pitsChampion = player.username
                    player.packetSender.sendMessage("You're the master of the pit!")
                    //player.moveTo(new Position(WAITING_ROOM_X, WAITING_ROOM_Y, 0));
                    //playerMap.remove(player);
                    //playerMap.put(player, WAITING);
                    player.appearance.bountyHunterSkull = 21
                    endGame()
                    player.combatBuilder.reset(true)
                    val giveTokkul = TOTAL_PLAYERS >= 3
                    if (giveTokkul) {
                        val amount = 400 + 200 * TOTAL_PLAYERS + Misc.getRandom(200)
                        player.inventory.add(TOKKUL_ID, amount)
                        DialogueManager.start(player, 359)
                    } else DialogueManager.start(player, 360)
                    return true
                }
            }
        }
        return false
    }

    fun endGame() {
        if (gameStarted) {
            for (player in playerMap.keys) {
                if (player != null) {
                    if (getState(player) != null && getState(player) == "PLAYING") {
                        player.moveTo(Position(WAITING_ROOM_X, WAITING_ROOM_Y, 0))
                        playerMap.remove(player)
                        playerMap[player] = WAITING
                        player.combatBuilder.reset(true)
                    }
                }
            }
        }
        elapsedGameTime = 0
        gameStarted = false
        gameStartTimer = 80
    }

    /**
     * @return Players playing fight pits
     */
    fun getListCount(state: String): Int {
        var count = 0
        for (s in playerMap.values) {
            if (state === s) {
                count++
            }
        }
        return count
    }

    /**
     * @note Updates waiting room interfaces etplayer.
     */
    @JvmStatic
    fun updateWaitingRoom(player: Player): Boolean {
        player.packetSender.sendString(2805, "Next Game Begins In : " + gameStartTimer)
        player.packetSender.sendString(2806, "Champion: " + pitsChampion)
        if (player.walkableInterfaceId != 2804) {
            player.packetSender.sendWalkableInterface(2804)
        }
        return true
    }

    /**
     * @note Updates players in game interfaces etplayer.
     */
    @JvmStatic
    fun updateGame(player: Player): Boolean {
        player.packetSender.sendString(2805, "Foes Remaining: " + (getListCount(PLAYING) - 1))
        player.packetSender.sendString(2806, "Champion: " + pitsChampion)
        if (player.walkableInterfaceId != 2804) {
            player.packetSender.sendWalkableInterface(2804)
        }
        return true
    }

    /*
	 * @process 600ms Tick
	 */
    @JvmStatic
    fun sequence() {
        if (!gameStarted) {
            if (TOTAL_PLAYERS == 0) {
                return
            }
            if (gameStartTimer > 0) {
                gameStartTimer--
            } else if (gameStartTimer == 0) {
                if (getListCount(WAITING) > 1 || getListCount(WAITING) == 1 && getListCount(PLAYING) == 1) beginGame()
                gameStartTimer = 80
            }
        }
        if (gameStarted) {
            elapsedGameTime++
            if (elapsedGameTime == END_GAME_TIME) {
                endGame()
                elapsedGameTime = 0
                gameStarted = false
                gameStartTimer = 80
            }
        }
    }

    /**
     * @note Starts game for the players in waiting room
     */
    private fun beginGame() {
        for (player in playerMap.keys) {
            enterGame(player)
        }
    }

    @JvmStatic
    fun inFightPits(player: Player): Boolean {
        return getState(player) != null && getState(player) == "PLAYING"
    }

    /**
     * Orb viewing
     */
    fun viewOrb(player: Player?) {
        /*if(!Locations.inPitsWaitRoom(player) || player.viewingOrb || Locations.inPits(player))
    		return;
    	for(int i = 0; i < org.Desolace.util.Constants.SIDEBAR_INTERFACES.length; i++)
    		player.getPacketSender().sendTabInterface(i, -1);
    	player.getPacketSender().sendTabInterface(4, 3209);
    	player.viewingOrb = true;
    	player.getMovementQueue().setMovementStatus(MovementStatus.CANNOT_MOVE);*/
    }

    fun cancelOrbView(player: Player?) {
        /*if(!player.viewingOrb)
    		return;
    	player.viewingOrb = false;
    	for(int i = 0; i < org.Desolace.util.Constants.SIDEBAR_INTERFACES.length; i++)
    		player.getPacketSender().sendTabInterface(i, org.Desolace.util.Constants.SIDEBAR_INTERFACES[i]);
    	player.getPacketSender().sendTabInterface(Constants.PRAYER_TAB, player.getPrayerBook().getInterfaceId());
    	player.getPacketSender().sendTabInterface(Constants.MAGIC_TAB, player.getSpellbook().getInterfaceId());
    	player.getPacketSender().sendCameraNeutrality();
    	player.getMovementQueue().setMovementStatus(MovementStatus.NONE);*/
    }

    fun viewOrbLocation(player: Player?, pos: Position?, cameraAngle: Int) {
        /*if(!Locations.inPitsWaitRoom(player) || !player.viewingOrb || Locations.inPits(player))
    		return;*/
        //player.getPacketSender().sendCameraAngle(pos, 5, cameraAngle);
    }
}