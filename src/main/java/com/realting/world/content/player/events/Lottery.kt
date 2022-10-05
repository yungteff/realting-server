package com.realting.world.content.player.events

import com.realting.world.content.dialogue.DialogueManager
import com.realting.model.PlayerRights
import com.realting.GameServer
import com.realting.model.entity.character.player.Player
import com.realting.util.FileUtils
import com.realting.util.Misc
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.IOException
import java.io.BufferedReader
import java.io.FileReader
import com.realting.world.World
import kotlin.Throws
import com.realting.world.content.PlayerLogs
import com.realting.world.content.dialogue.Dialogue
import com.realting.world.content.dialogue.DialogueType
import com.realting.world.content.dialogue.DialogueExpression
import java.lang.Exception
import java.util.ArrayList

/**
 * Handles the Lottery.
 * @author Gabriel Hannason
 */
object Lottery {
    /**
     * The list holding all users who have entered the lottery.
     */
    private val CONTESTERS: MutableList<String> = ArrayList()

    /*
	 * The location to the Lottery file where users are saved.
	 */
    private const val CONTESTERS_FILE_LOCATION = "./data/saves/lottery/lottery.txt"

    /*
	 * The location to the Lottery file where the winners are saved.
	 */
    private const val LAST_WINNER_FILE_LOCATION = "./data/saves/lottery/lotterywin.txt"

    /*
	 * Can players enter the lottery right now?
	 */
    private const val LOTTERY_ENABLED = false

    /*
	 * The amount of coins required to enter the lottery.
	 */
    private const val PRICE_TO_ENTER = 1000000

    /*
	 * Get's the amount of gold people have put in the pot.
	 */
    val pot: Int
        get() = if (CONTESTERS.size == 0) {
            0
        } else CONTESTERS.size * (PRICE_TO_ENTER - 250000)

    /*
	 * The user who won the Lottery last
	 */
    var lastWinner = "Crimson"
        private set

    /*
	 * Has the last week's winner been rewarded?
	 */
    private var LAST_WINNER_REWARDED = true

    /**
     * Gets a random winner for the lottery.
     * @return    A random user who has won the lottery.
     */
    val randomWinner: String?
        get() {
            var winner: String? = null
            val listSize = CONTESTERS.size
            if (listSize >= 4) winner = CONTESTERS[Misc.getRandom(listSize - 1)]
            return winner
        }

    /**
     * Handles a player who wishes to enter the lottery.
     * @param p            The player who wants to enter the lottery.
     */
    @JvmStatic
    fun enterLottery(p: Player) {
        if (!LOTTERY_ENABLED) {
            p.packetSender.sendInterfaceRemoval().sendMessage("The lottery is currently not active. Try again soon!")
            return
        }
        if (CONTESTERS.contains(p.username)) {
            DialogueManager.start(p, 17)
            return
        }
        val usePouch = p.moneyInPouch >= PRICE_TO_ENTER
        if (p.inventory.getAmount(995) < PRICE_TO_ENTER && !usePouch || p.rights == PlayerRights.DEVELOPER || p.rights == PlayerRights.OWNER) {
            p.packetSender.sendInterfaceRemoval().sendMessage("")
                .sendMessage("You do not have enough money in your inventory to enter this week's lottery.")
                .sendMessage("The lottery for this week costs " + Misc.insertCommasToNumber("" + PRICE_TO_ENTER + "") + " coins to enter.")
            return
        }
        if (usePouch) {
            p.moneyInPouch = p.moneyInPouch - PRICE_TO_ENTER
            p.packetSender.sendString(8135, "" + p.moneyInPouch)
        } else p.inventory.delete(995, PRICE_TO_ENTER)
        p.achievementAttributes.coinsGambled = p.achievementAttributes.coinsGambled + PRICE_TO_ENTER
        addToLottery(p.username)
        p.packetSender.sendMessage("You have entered the lottery!").sendMessage("A winner is announced every Friday.")
        DialogueManager.start(p, 18)
        //Achievements.finishAchievement(p, AchievementData.ENTER_THE_LOTTERY);
        //Achievements.doProgress(p, AchievementData.ENTER_THE_LOTTERY_THREE_TIMES);
    }

    /**
     * Adds a user to the lottery by writing their username to the file aswell as adding them to the list of users
     * who have entered already.
     * @param user        The username to add to the lists.
     */
    fun addToLottery(user: String) {
        CONTESTERS.add(user)
        GameServer.getLoader().engine.submit {
            try {
                FileUtils.createNewFile(CONTESTERS_FILE_LOCATION)
                val writer = BufferedWriter(FileWriter(CONTESTERS_FILE_LOCATION, true))
                writer.write("" + user + "")
                writer.newLine()
                writer.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Reads the lottery list and adds every user from the .txt files to the lists.
     */
    @JvmStatic
    fun init() {
        try {
            FileUtils.createNewFile(CONTESTERS_FILE_LOCATION)
            val r = BufferedReader(FileReader(CONTESTERS_FILE_LOCATION))
            while (true) {
                var line = r.readLine()
                line = line?.trim { it <= ' ' } ?: break
                if (line.length > 0) {
                    if (!CONTESTERS.contains(line)) //user might have gotten on list twice somehow.. don't give them extra chance of winning
                        CONTESTERS.add(line)
                }
            }
            r.close()
            FileUtils.createNewFile(LAST_WINNER_FILE_LOCATION)
            val r2 = BufferedReader(FileReader(LAST_WINNER_FILE_LOCATION))
            while (true) {
                var line = r2.readLine()
                line = line?.trim { it <= ' ' } ?: break
                if (line.length > 0) {
                    if (!line.contains("NOT REWARDED. NEEDS REWARD!")) lastWinner = line else LAST_WINNER_REWARDED =
                        false
                }
            }
            r2.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Restarts the lottery and rewards this week's winner.
     */
    fun restartLottery() {
        if (!LOTTERY_ENABLED) return
        try {
            val winner = randomWinner
            if (winner != null) {
                lastWinner = winner
                val player = World.getPlayerByName(winner)
                FileUtils.createNewFile(LAST_WINNER_FILE_LOCATION)
                var writer = BufferedWriter(FileWriter(LAST_WINNER_FILE_LOCATION))
                writer.write(winner)
                writer.newLine()
                if (player != null) {
                    rewardPlayer(player, true)
                } else {
                    LAST_WINNER_REWARDED = false
                    writer.write("NOT REWARDED. NEEDS REWARD!")
                    println("Player $winner won the lottery but wasn't online.")
                }
                CONTESTERS.clear()
                writer.close()
                FileUtils.createNewFile(CONTESTERS_FILE_LOCATION)
                writer = BufferedWriter(FileWriter(CONTESTERS_FILE_LOCATION))
                writer.write("")
                writer.close()
                World.sendMessage("<col=D9D919><shad=0>This week's lottery winner is $winner! Congratulations!")
            } else World.sendMessage("<col=D9D919><shad=0>The lottery needs some more contesters before a winner can be selected.")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Rewards a player with items for winning the lottery.
     * @param player            The player to reward
     * @param ignore            Should a check be ignored?
     * @throws IOException        Throws exceptions
     */
    @Throws(IOException::class)
    fun rewardPlayer(player: Player, ignore: Boolean) {
        if ((!LAST_WINNER_REWARDED || ignore) && lastWinner.equals(player.username, ignoreCase = true)) {
            LAST_WINNER_REWARDED = true
            player.moneyInPouch = player.moneyInPouch + pot
            player.packetSender.sendString(8135, "" + player.moneyInPouch)
            player.packetSender.sendMessage("You've won the lottery for this week! Congratulations!")
            player.packetSender.sendMessage("The reward has been added to your money pouch.")
            FileUtils.createNewFile(LAST_WINNER_FILE_LOCATION)
            val writer = BufferedWriter(FileWriter(LAST_WINNER_FILE_LOCATION))
            writer.write(player.username)
            writer.close()
            PlayerLogs.log(player.username, "Player got " + pot + " from winning the lottery!")
        }
    }

    /**
     * Handles the lottery for a player on login
     * Checks if a user won the lottery without being rewarded.
     * @param p        The player to handle login for.
     */
    @JvmStatic
    fun onLogin(p: Player) {
        try {
            rewardPlayer(p, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    object Dialogues {
        @JvmStatic
        fun getCurrentPot(p: Player?): Dialogue {
            return object : Dialogue() {
                override fun type(): DialogueType {
                    return DialogueType.NPC_STATEMENT
                }

                override fun animation(): DialogueExpression {
                    return DialogueExpression.NORMAL
                }

                override fun npcId(): Int {
                    return 4249
                }

                override fun dialogue(): Array<String> {
                    return arrayOf("The pot is currently at:", "" + Misc.insertCommasToNumber("" + pot) + " coins.")
                }

                override fun nextDialogue(): Dialogue {
                    return DialogueManager.getDialogues()[15]!!
                }
            }
        }

        @JvmStatic
        fun getLastWinner(p: Player?): Dialogue {
            return object : Dialogue() {
                override fun type(): DialogueType {
                    return DialogueType.NPC_STATEMENT
                }

                override fun animation(): DialogueExpression {
                    return DialogueExpression.NORMAL
                }

                override fun npcId(): Int {
                    return 4249
                }

                override fun dialogue(): Array<String> {
                    return arrayOf("Last week's winner was " + lastWinner + ".")
                }

                override fun nextDialogue(): Dialogue {
                    return DialogueManager.getDialogues()[15]!!
                }
            }
        }
    }
}