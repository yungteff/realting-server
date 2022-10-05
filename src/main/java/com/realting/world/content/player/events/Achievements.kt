package com.realting.world.content.player.events

import com.realting.model.GameMode
import com.realting.model.Item
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import java.util.*

object Achievements {
    @JvmStatic
    fun handleButton(player: Player, button: Int): Boolean {
        if (!(button >= -7134 && button <= -7013)) {
            return false
        }
        var index = -1
        /*if(button >= -7134 && button <= -7111) {
			index = 7134 + button;
		} else if(button >= -28499 && button <= -28469) {
			index = 30 + 28499 + button;
		} else if(button >= -28466 && button <= -28435) {
			index = 61 + 28466 + button;
		} else if(button >= -28432 && button <= -28425) {
			index = 93 + 28432 + button;
		}*/if (button >= -7134 && button <= -7011) {
            index = 7134 + button
        } else if (button >= -7109 && button <= -7086) {
            index = 24 + 7109 + button
        } else if (button >= -7084 && button <= -7061) {
            index = 48 + 7084 + button
        } else if (button >= -7059 && button <= -7036) {
            index = 72 + 7059 + button
        } else if (button >= -7034 && button <= -7013) {
            index = 96 + 7034 + button
        }
        if (index >= 0 && index < AchievementData.values().size) {
            val achievement = AchievementData.values()[index]
            sendAchievementData(player, achievement)
            if (player.achievementAttributes.completion[achievement.ordinal]) {
                player.packetSender.sendMessage("<img=10> <col=339900>You have completed the achievement: " + achievement.interfaceLine + ".")
            } else if (achievement.progressData == null) {
                player.packetSender.sendMessage("<img=10> <col=660000>You have not started the achievement: " + achievement.interfaceLine + ".")
            } else {
                val progress = player.achievementAttributes.progress[achievement.progressData[0]]
                val requiredProgress = achievement.progressData[1]
                if (progress == 0) {
                    player.packetSender.sendMessage("<img=10> <col=660000>You have not started the achievement: " + achievement.interfaceLine + ".")
                } else if (progress != requiredProgress) {
                    player.packetSender.sendMessage(
                        "<img=10> <col=FFFF00>Your progress for this achievement is currently at: " + Misc.insertCommasToNumber(
                            "" + progress
                        ) + "/" + Misc.insertCommasToNumber("" + requiredProgress) + "."
                    )
                }
            }
        }
        return true
    }

    fun sendAchievementData(player: Player, data: AchievementData) {
        player.packetSender.sendString(24011, data.description)
        player.packetSender.sendString(24013, data.requirements)
        player.packetSender.sendString(24015, data.rewards)
        player.packetSender.sendString(24005, data.diff)
        player.packetSender.sendString(24004, data.interfaceLine)
        player.packetSender.sendString(
            24009, player.pointsHandler.achievementPoints.toString() + "/" + AchievementData.values().size
        )

        // need to send difficulty, the task name, adn the achivement progress.
    }

    @JvmStatic
    fun updateInterface(player: Player) {
        for (achievement in AchievementData.values()) {
            val completed = player.achievementAttributes.completion[achievement.ordinal]
            val progress =
                achievement.progressData != null && player.achievementAttributes.progress[achievement.progressData[0]] > 0
            player.packetSender.sendString(
                achievement.interfaceFrame,
                (if (completed) "@gre@" else if (progress) "@yel@" else "@red@") + achievement.interfaceLine
            )
        }
        player.packetSender.sendString(
            37001, "Achievements: " + player.pointsHandler.achievementPoints + "/" + AchievementData.values().size
        )
    }

    @JvmStatic
    fun setPoints(player: Player) {
        var points = 0
        for (achievement in AchievementData.values()) {
            if (player.achievementAttributes.completion[achievement.ordinal]) {
                points++
            }
        }
        player.pointsHandler.setAchievementPoints(points, false)
    }

    @JvmStatic
    @JvmOverloads
    fun doProgress(player: Player, achievement: AchievementData, amt: Int = 1) {
        if (player.achievementAttributes.completion[achievement.ordinal]) return
        if (achievement.progressData != null) {
            val progressIndex = achievement.progressData[0]
            val amountNeeded = achievement.progressData[1]
            val previousDone = player.achievementAttributes.progress[progressIndex]
            if (previousDone + amt < amountNeeded) {
                player.achievementAttributes.progress[progressIndex] = previousDone + amt
                if (previousDone == 0) player.packetSender.sendString(
                    achievement.interfaceFrame, "@yel@" + achievement.interfaceLine
                )
            } else {
                finishAchievement(player, achievement)
            }
        }
    }

    fun bankItemReward(player: Player, item: Item?) {
        var item = item
        var bankIndex = 0
        if (item!!.definition.isNoted) {
            item = Item(Item.getUnNoted(item.id), item.amount)
        }
        for (i in player.banks.indices) {
            if (player.getBank(i).contains(item.id)) {
                if (!player.getBank(i).isFull) {
                    bankIndex = i
                }
                break
            }
        }
        if (player.getBank(bankIndex).isFull) {
            invItemReward(player, item)
        } else {
            player.getBank(bankIndex).add(item)
            player.packetSender.sendMessage("<img=10> Your reward of " + Misc.format(item.amount) + "x " + item.definition.name + " was sent to your bank. (Tab " + (bankIndex + 1) + ")")
        }
    }

    fun invItemReward(player: Player, item: Item?) {
        var item = item
        if (!item!!.definition.isNoted && !item.definition.isStackable && item.amount > 1) {
            item = Item(Item.getNoted(item.id), item.amount)
        }
        player.inventory.add(item)
        player.packetSender.sendMessage("<img=10> You recieved " + item.amount + "x " + item.definition.name + " as a reward.")
    }

    fun giveReward(player: Player, achievement: AchievementData) {
        val item = achievement.reward
        if (player.gameMode == GameMode.ULTIMATE_IRONMAN) {
            invItemReward(player, item)
        } else {
            bankItemReward(player, item)
        }
    }

    @JvmStatic
    fun finishAchievement(player: Player, achievement: AchievementData) {
        if (player.achievementAttributes.completion[achievement.ordinal]) return
        player.achievementAttributes.completion[achievement.ordinal] = true
        player.packetSender.sendString(achievement.interfaceFrame, "@gre@" + achievement.interfaceLine).sendMessage(
            "<img=10> <col=339900>You have completed the achievement " + Misc.formatText(
                achievement.toString().lowercase(Locale.getDefault()) + "."
            )
        ).sendString(
            37001, "Achievements: " + player.pointsHandler.achievementPoints + "/" + AchievementData.values().size
        )
        if (achievement.difficulty == Difficulty.HARD) {
            //doProgress(player, AchievementData.COMPLETE_ALL_HARD_TASKS);
        }
        player.pointsHandler.setAchievementPoints(1, true)
        giveReward(player, achievement)
    }

    /*public enum AchievementData {

		ROCK(Difficulty.EASY, "Kill 10 rock crabs", 37005, new int[]{3 , 10}, new Item(6607, 1)),
		FILL_WELL_OF_GOODWILL_1M(Difficulty.EASY, "Pour 1M Into The Well", 37006, null, new Item(10442, 1)),
		CUT_AN_OAK_TREE(Difficulty.EASY, "Cut An Oak Tree", 37007, null, new Item(5074, 1)),
		BURN_AN_OAK_LOG(Difficulty.EASY, "Burn An Oak Log", 37008, null, new Item(5074, 1)),
		FISH_A_SALMON(Difficulty.EASY, "Fish A Salmon", 37009, null, new Item(314, 50)),
		COOK_A_SALMON(Difficulty.EASY, "Cook A Salmon", 37010, null, new Item(332, 5)),
		EAT_A_SALMON(Difficulty.EASY, "Eat A Salmon", 37011, null, new Item(15272, 1)),
		MINE_SOME_IRON(Difficulty.EASY, "Mine Some Iron", 37012, null, new Item(2351, 1)),
		SMELT_AN_IRON_BAR(Difficulty.EASY, "Smelt An Iron Bar", 37013, null, new Item(2352, 5)),
		HARVEST_A_CROP(Difficulty.EASY, "Harvest A Crop", 37014, null, new Item(3844, 1)),
		INFUSE_A_DREADFOWL_POUCH(Difficulty.EASY, "Infuse A Dreadfowl Pouch", 37015, null, new Item(15262, 1)),
		CATCH_A_YOUNG_IMPLING(Difficulty.EASY, "Catch A Young Impling", 37016, null, new Item(11241, 5)),
		CRAFT_A_PAIR_OF_LEATHER_BOOTS(Difficulty.EASY, "Craft A Pair of Leather Boots", 37017, null, new Item(1742, 5)),
		CLIMB_AN_AGILITY_OBSTACLE(Difficulty.EASY, "Climb An Agility Obstacle", 37018, null, new Item(88, 1)),
		FLETCH_SOME_ARROWS(Difficulty.EASY, "Fletch Some Arrows", 37019, null, new Item(53, 125)),
		STEAL_A_RING(Difficulty.EASY, "Steal A Ring", 37020, null, new Item(2550, 1)),
		MIX_A_POTION(Difficulty.EASY, "Mix A Potion", 37021, null, new Item(228, 100)),
		RUNECRAFT_SOME_RUNES(Difficulty.EASY, "Runecraft Some Runes", 37022, null, new Item(1437, 75)),
		BURY_A_BIG_BONE(Difficulty.EASY, "Bury A Big Bone", 37023, null, new Item(10448, 1)),
		COMPLETE_A_SLAYER_TASK(Difficulty.EASY, "Complete A Slayer Task", 37024, null, new Item(13285, 1)),
		SET_UP_A_CANNON(Difficulty.EASY, "Set Up A Cannon", 37025, null, new Item(2, 100)),
		KILL_A_MONSTER_USING_MELEE(Difficulty.EASY, "Kill a Monster Using Melee", 37026, null, new Item(1215, 1)),
		KILL_A_MONSTER_USING_RANGED(Difficulty.EASY, "Kill a Monster Using Ranged", 37027, null, new Item(892, 100)),
		KILL_A_MONSTER_USING_MAGIC(Difficulty.EASY, "Kill a Monster Using Magic", 37028, null, new Item(7937, 200)),
		DEAL_EASY_DAMAGE_USING_MELEE(Difficulty.EASY, "Deal 1000 Melee Damage", 37029, new int[]{0, 1000}, new Item(995, 100000)),
		DEAL_EASY_DAMAGE_USING_RANGED(Difficulty.EASY, "Deal 1000 Ranged Damage", 37030, new int[]{1, 1000}, new Item(995, 100000)),
		DEAL_EASY_DAMAGE_USING_MAGIC(Difficulty.EASY, "Deal 1000 Magic Damage", 37031, new int[]{2, 1000}, new Item(995, 100000)),
		PERFORM_A_SPECIAL_ATTACK(Difficulty.EASY, "Perform a Special Attack", 37032, null, new Item(4153, 1)),
		FIGHT_ANOTHER_PLAYER(Difficulty.EASY, "Fight Another Player", 37033, null, new Item(10828, 1)),
		BURY_A_DRAGON_BONE(Difficulty.EASY, "Bury A Dragon Bone", 37034, null, new Item(2452, 1)),

		DEFEAT_A_REVENANT_DARK_BEAST(Difficulty.MEDIUM, "Slay A Revenant Dark Beast", 37037, null, new Item(746, 1)),
		FILL_WELL_OF_GOODWILL_50M(Difficulty.MEDIUM, "Pour 50M Into The Well", 37038, new int[]{4, 50000000}, new Item(2778, 1)),
		CUT_100_MAGIC_LOGS(Difficulty.MEDIUM, "Cut 100 Magic Logs", 37039, new int[]{5, 100}, new Item(995, 250000)),
		BURN_100_MAGIC_LOGS(Difficulty.MEDIUM, "Burn 100 Magic Logs", 37040, new int[]{6, 100}, new Item(995, 250000)),
		FISH_25_ROCKTAILS(Difficulty.MEDIUM, "Fish 25 Rocktails", 37041, new int[]{7, 25}, new Item(25, 10)),
		COOK_25_ROCKTAILS(Difficulty.MEDIUM, "Cook 25 Rocktails", 37042, new int[]{8, 25}, new Item(25, 10)),
		MINE_25_RUNITE_ORES(Difficulty.MEDIUM, "Mine 25 Runite Ores", 37043, new int[]{9, 25}, new Item(454, 100)),
		SMELT_25_RUNE_BARS(Difficulty.MEDIUM, "Smelt 25 Rune Bars", 37044, new int[]{10, 25}, new Item(454, 100)),
		HARVEST_10_TORSTOLS(Difficulty.MEDIUM, "Harvest 10 Torstols", 37045, new int[]{11, 10}, new Item(19670, 1)),
		INFUSE_25_TITAN_POUCHES(Difficulty.MEDIUM, "Infuse 25 Steel Titans", 37046, new int[]{12, 25}, new Item(454, 100)),
		CATCH_5_KINGLY_IMPLINGS(Difficulty.MEDIUM, "Catch 5 Kingly Implings", 37047, new int[]{13, 5}, new Item(995, 2000000)),
		COMPLETE_A_HARD_SLAYER_TASK(Difficulty.MEDIUM, "Complete A Hard Slayer Task", 37048, null, new Item(13283, 1)),
		CRAFT_20_BLACK_DHIDE_BODIES(Difficulty.MEDIUM, "Craft 20 Black D'hide Bodies", 37049, new int[]{14, 20}, new Item(537, 10)),
		FLETCH_450_RUNE_ARROWS(Difficulty.MEDIUM, "Fletch 450 Rune Arrows", 37050, new int[]{15, 450}, new Item(9144, 50)),
		STEAL_140_SCIMITARS(Difficulty.MEDIUM, "Steal 140 Scimitars", 37051, new int[]{16, 140}, new Item(995, 500000)),
		MIX_AN_OVERLOAD_POTION(Difficulty.MEDIUM, "Mix An Overload Potion", 37052, null, new Item(995, 500000)),
		ASSEMBLE_A_GODSWORD(Difficulty.MEDIUM, "Assemble A Godsword", 37053, null, new Item(22053, 1)),
		CLIMB_50_AGILITY_OBSTACLES(Difficulty.MEDIUM, "Climb 50 Agility Obstacles", 37054, new int[]{17, 50}, new Item(995, 1000000)),
		RUNECRAFT_500_BLOOD_RUNES(Difficulty.MEDIUM, "Runecraft 500 Blood Runes", 37055, new int[]{18, 500}, new Item(7937, 350)),
		BURY_25_FROST_DRAGON_BONES(Difficulty.MEDIUM, "Use 25 Frost Dragon Bones", 37056, new int[]{19, 25}, new Item(8716, 1)),
		FIRE_500_CANNON_BALLS(Difficulty.MEDIUM, "Fire 500 Cannon Balls", 37057, new int[]{20, 500}, new Item(2, 175)),
		DEAL_MEDIUM_DAMAGE_USING_MELEE(Difficulty.MEDIUM, "Deal 100K Melee Damage", 37058, new int[]{21, 100000}, new Item(2440, 1)),
		DEAL_MEDIUM_DAMAGE_USING_RANGED(Difficulty.MEDIUM, "Deal 100K Ranged Damage", 37059, new int[]{22, 100000}, new Item(2444, 1)),
		DEAL_MEDIUM_DAMAGE_USING_MAGIC(Difficulty.MEDIUM, "Deal 100K Magic Damage", 37060, new int[]{23, 100000}, new Item(3040, 1)),
		DEFEAT_THE_KING_BLACK_DRAGON(Difficulty.MEDIUM, "Defeat The King Black Dragon", 37061, null, new Item(4087, 1)),
		DEFEAT_THE_CHAOS_ELEMENTAL(Difficulty.MEDIUM, "Defeat The Chaos Elemental", 37062, null, new Item(1149, 1)),
		DEFEAT_A_TORMENTED_DEMON(Difficulty.MEDIUM, "Defeat A Tormented Demon", 37063, null, new Item(3140, 1)),
		DEFEAT_SCORPIA(Difficulty.MEDIUM, "Defeat Scorpia", 37064, null, new Item(1305, 1)),
		DEFEAT_ZULRAH(Difficulty.MEDIUM, "Defeat Zulrah", 37065, null, new Item(12934, 150)), / * NEEDS TO BE CHANGED */
    /*DEFEAT_10_PLAYERS(Difficulty.MEDIUM, "Defeat 10 Players", 37066, new int[]{24, 10}, new Item(13262, 1)),
		REACH_A_KILLSTREAK_OF_3(Difficulty.MEDIUM, "Reach A Killstreak Of 3", 37067, null, new Item(15332, 1)),

		FILL_WELL_OF_GOODWILL_250M(Difficulty.HARD, "Pour 250M Into The Well", 37070, new int[]{25, 250000000}, new Item(6760, 1)),
        CUT_5000_MAGIC_LOGS(Difficulty.HARD, "Cut 5000 Magic Logs", 37071, new int[]{26, 5000}, new Item(14140, 1)),
        BURN_2500_MAGIC_LOGS(Difficulty.HARD, "Burn 2500 Magic Logs", 37072, new int[]{27, 2500}, new Item(10327, 1)),
        FISH_2000_ROCKTAILS(Difficulty.HARD, "Fish 2000 Rocktails", 37073, new int[]{28, 2000}, new Item(25, 250)),
        COOK_1000_ROCKTAILS(Difficulty.HARD, "Cook 1000 Rocktails", 37074, new int[]{29, 1000}, new Item(25, 250)),
        MINE_2000_RUNITE_ORES(Difficulty.HARD, "Mine 2000 Runite Ores", 37075, new int[]{30, 2000}, new Item(14130, 1)),
        SMELT_1000_RUNE_BARS(Difficulty.HARD, "Smelt 1000 Rune Bars", 37076, new int[]{31, 1000}, new Item(454, 100)),
        HARVEST_1000_TORSTOLS(Difficulty.HARD, "Harvest 1000 Torstols", 37077, new int[]{32, 1000}, new Item(3025, 50)),
        INFUSE_250_STEEL_TITAN_POUCHES(Difficulty.HARD, "Infuse 250 Steel Titans", 37078, new int[]{33, 250}, new Item(12163, 100)),
        CRAFT_1000_DIAMOND_GEMS(Difficulty.HARD, "Craft 1000 Diamond Gems", 37079, new int[]{34, 1000}, new Item(990, 2)),
        CATCH_100_KINGLY_IMPLINGS(Difficulty.HARD, "Catch 100 Kingly Imps", 37080, new int[]{35, 100}, new Item(15518, 10)),
        FLETCH_5000_RUNE_ARROWS(Difficulty.HARD, "Fletch 5000 Rune Arrows", 37081, new int[]{36, 5000}, new Item(9144, 1000)),
        STEAL_5000_SCIMITARS(Difficulty.HARD, "Steal 5000 Scimitars", 37082, new int[]{37, 5000}, new Item(995, 2000000)),
        RUNECRAFT_8000_BLOOD_RUNES(Difficulty.HARD, "Runecraft 8000 Blood Runes", 37083, new int[]{38, 8000}, new Item(7937, 2500)),
        BURY_500_FROST_DRAGON_BONES(Difficulty.HARD, "Bury 500 Frost Dragon Bones", 37084, new int[]{39, 500}, new Item(995, 5000000)),
		FIRE_5000_CANNON_BALLS(Difficulty.HARD, "Fire 5000 Cannon Balls", 37085, new int[]{40, 5000}, new Item(15241, 1)),
		MIX_100_OVERLOAD_POTIONS(Difficulty.HARD, "Mix 100 Overload Potions", 37086, new int[]{41, 100}, new Item(19670, 1)),
		COMPLETE_AN_ELITE_SLAYER_TASK(Difficulty.HARD, "Complete An Elite Slayer Task", 37087, null, new Item(13281, 1)),
		ASSEMBLE_5_GODSWORDS(Difficulty.HARD, "Assemble 5 Godswords", 37088, new int[]{42, 5}, new Item(11690, 1)),
		DEAL_HARD_DAMAGE_USING_MELEE(Difficulty.HARD, "Deal 10M Melee Damage", 37089, new int[]{43, 10000000}, new Item(4720, 1)),
		DEAL_HARD_DAMAGE_USING_RANGED(Difficulty.HARD, "Deal 10M Ranged Damage", 37090, new int[]{44, 10000000}, new Item(4736, 1)),
		DEAL_HARD_DAMAGE_USING_MAGIC(Difficulty.HARD, "Deal 10M Magic Damage", 37091, new int[]{45, 10000000}, new Item(4712, 1)),
		DEFEAT_JAD(Difficulty.HARD, "Defeat Jad", 37092, null, new Item(6529, 10000)),
		DEFEAT_BANDOS_AVATAR(Difficulty.HARD, "Defeat Bandos Avatar", 37093, null, new Item(1580, 1)),
		DEFEAT_GENERAL_GRAARDOR(Difficulty.HARD, "Defeat General Graardor", 37094, null, new Item(13283, 1)),
		DEFEAT_KREE_ARRA(Difficulty.HARD, "Defeat Kree'Arra", 37095, null, new Item(13283, 1)),
		DEFEAT_COMMANDER_ZILYANA(Difficulty.HARD, "Defeat Commander Zilyana", 37096, null, new Item(13283, 1)),
		DEFEAT_KRIL_TSUTSAROTH(Difficulty.HARD, "Defeat K'ril Tsutsaroth", 37097, null, new Item(22053, 1)),
		DEFEAT_THE_CORPOREAL_BEAST(Difficulty.HARD, "Defeat The Corporeal Beast", 37098, null, new Item(13734, 1)),
		DEFEAT_NEX(Difficulty.HARD, "Defeat Nex", 37099, null, new Item(19372, 1)),
		DEFEAT_30_PLAYERS(Difficulty.HARD, "Defeat 30 Players", 37100, new int[]{46, 30}, new Item(15332, 1)),
		REACH_A_KILLSTREAK_OF_6(Difficulty.HARD, "Reach A Killstreak Of 6", 37101, null, new Item(15332, 1)),

		COMPLETE_ALL_HARD_TASKS(Difficulty.ELITE, "Complete All Hard Tasks", 37104, new int[]{47, 32}, new Item(7158, 1)),
		CUT_AN_ONYX_STONE(Difficulty.ELITE, "Cut An Onyx Stone", 37105, null, new Item(6570, 1)),
		REACH_MAX_EXP_IN_A_SKILL(Difficulty.ELITE, "Reach Max Exp In A Skill", 37106, null, new Item(19111, 1)),
		REACH_LEVEL_99_IN_ALL_SKILLS(Difficulty.ELITE, "Reach Level 99 In All Skills", 37107, new int[]{48, 22}, new Item(21334, 1)),
		DEFEAT_10000_MONSTERS(Difficulty.ELITE, "Defeat 10,000 Monsters", 37108, new int[]{49, 10000}, new Item(2402, 1)),
		DEFEAT_500_BOSSES(Difficulty.ELITE, "Defeat 500 Boss Monsters", 37109, new int[]{50, 500}, new Item(14632)),
		VOTE_100_TIMES(Difficulty.ELITE, "Vote 100 Times", 37110, new int[]{51, 100}, new Item(19670, 10)),
		UNLOCK_ALL_LOYALTY_TITLES(Difficulty.ELITE, "Unlock All Loyalty Titles", 37111, new int[]{52, 11}, new Item(6746, 1)),
		;

		AchievementData(Difficulty difficulty, String interfaceLine, int interfaceFrame, int[] progressData, Item reward) {
			this.difficulty = difficulty;
			this.interfaceLine = interfaceLine;
			this.interfaceFrame = interfaceFrame;
			this.progressData = progressData;
			this.reward = reward;
		}

		private Difficulty difficulty;
		private String interfaceLine;
		private int interfaceFrame;
		private int[] progressData; */
    enum class AchievementData  //REACH_LEVEL_99_IN_ALL_SKILLS(Difficulty.ELITE, "Reach Level 99 In All Skills","do something","some rew", "some reward", 58502, new int[]{48, 22}, new Item(21334, 1),"Beginner"),
    //DEFEAT_10000_MONSTERS(Difficulty.ELITE, "Defeat 10,000 Monsters", "do something","some rew", "some reward",58503, new int[]{49, 10000}, new Item(2402, 1),"Beginner"),
    //DEFEAT_500_BOSSES(Difficulty.ELITE, "Defeat 500 Boss Monsters", "do something","some rew", "some reward",58504, new int[]{50, 500}, new Item(14632),"Beginner"),
    //VOTE_100_TIMES(Difficulty.ELITE, "vote 100 times for the server", "Vote 100 times","Nothing", "nothing", 58505, new int[]{51, 100}, new Item(19670, 10),"Beginner"),
    //UNLOCK_ALL_LOYALTY_TITLES(Difficulty.ELITE, "Unlock All Loyalty Titles","do something","some rew", "some reward", 58506, new int[]{52, 11}, new Item(6746, 1),"Beginner"),
        (
        val difficulty: Difficulty?,
        val interfaceLine: String,
        val description: String?,
        val requirements: String?,
        val rewards: String?,
        val interfaceFrame: Int,
        val progressData: IntArray?,
        val reward: Item?,
        val diff: String?
    ) {
        // Text lines are 01, 26 , 51, 76 and 101
        ROCK(
            Difficulty.EASY,
            "Kill 10 rock crabs",
            "Kill 10 rock crabs",
            "Starter weapon and/or armour",
            "Something",
            58402,
            intArrayOf(1, 10),
            Item(6607, 1),
            "Beginner"
        ),
        CATCH_25_SCHRIMPS(
            Difficulty.EASY,
            "Catch 25 raw shrimp",
            "Catch 25 raw shrimps with the fishing skill. Get the by using the fishing teleport",
            "Level 1 fishing and a small fishing net",
            "Food",
            58403,
            intArrayOf(2, 25),
            Item(10442, 1),
            "Beginner"
        ),
        CUT_10_LOGS(
            Difficulty.EASY,
            "Cut 10 evergreen trees",
            "Cut 10 evergreen trees",
            "Level 1 Woodcutting]\\n An axe ",
            "More logs",
            58404,
            intArrayOf(3, 10),
            Item(5074, 1),
            "Beginner"
        ),
        BURN_25_OAK_LOGS(
            Difficulty.EASY,
            "Burn 25 Oak Log",
            "Burn 25 Oak Log",
            "Level 15 firemakig \\n A tinderbox",
            "some reward",
            58405,
            intArrayOf(4, 25),
            Item(5074, 1),
            "Beginner"
        ),
        STEAL_20_RINGS(
            Difficulty.EASY,
            "Steal 20 gold rings",
            "Steal 20 gold rings from Ring Stall",
            "Thieving level 30",
            "some reward",
            58406,
            intArrayOf(5, 20),
            Item(314, 50),
            "Beginner"
        ),
        MIX_A_POTION(
            Difficulty.EASY,
            "Mix A Potion",
            "do something",
            "some rew",
            "some reward",
            58418,
            null,
            Item(228, 100),
            "Beginner"
        ),
        MINE_SOME_IRON(
            Difficulty.EASY,
            "Mine Some Iron",
            "Mine 35 iron ores",
            "Level 15 Mining \\n A pickaxe",
            "some reward",
            58408,
            intArrayOf(6, 35),
            Item(15272, 1),
            "Beginner"
        ),
        CRAFT_100_EARTH_RUNES(
            Difficulty.EASY,
            "Craft 100 earth runes",
            "Craft 100 earth runes",
            "Level 9 Runecrafting",
            "some reward",
            58409,
            intArrayOf(7, 100),
            Item(2351, 1),
            "Beginner"
        ),
        SMELT_AN_IRON_BAR(
            Difficulty.EASY,
            "Smelt An Iron Bar",
            "do something",
            "some rew",
            "some reward",
            58410,
            null,
            Item(2352, 5),
            "Beginner"
        ),
        HARVEST_A_CROP(
            Difficulty.EASY,
            "Harvest A Crop",
            "do something",
            "some rew",
            "some reward",
            58411,
            null,
            Item(3844, 1),
            "Beginner"
        ),
        INFUSE_A_DREADFOWL_POUCH(
            Difficulty.EASY,
            "Infuse A Dreadfowl Pouch",
            "do something",
            "some rew",
            "some reward",
            58412,
            null,
            Item(15262, 1),
            "Beginner"
        ),
        CATCH_A_YOUNG_IMPLING(
            Difficulty.EASY,
            "Catch A Young Impling",
            "do something",
            "some rew",
            "some reward",
            58413,
            null,
            Item(11241, 5),
            "Beginner"
        ),
        CRAFT_A_PAIR_OF_LEATHER_BOOTS(
            Difficulty.EASY,
            "Craft A Pair of Leather Boots",
            "do something",
            "some rew",
            "some reward",
            58414,
            null,
            Item(1742, 5),
            "Beginner"
        ),
        CLIMB_AN_AGILITY_OBSTACLE(
            Difficulty.EASY,
            "Climb An Agility Obstacle",
            "do something",
            "some rew",
            "some reward",
            58415,
            null,
            Item(88, 1),
            "Beginner"
        ),
        FLETCH_SOME_ARROWS(
            Difficulty.EASY,
            "Fletch Some Arrows",
            "do something",
            "some rew",
            "some reward",
            58416,
            null,
            Item(53, 125),
            "Beginner"
        ),
        STEAL_A_RING(
            Difficulty.EASY,
            "Steal A Ring",
            "do something",
            "some rew",
            "some reward",
            58417,
            null,
            Item(2550, 1),
            "Beginner"
        ),
        USED(
            Difficulty.EASY,
            "Mix A Potion",
            "do something",
            "some rew",
            "some reward",
            58418,
            null,
            Item(228, 100),
            "Beginner"
        ),
        RUNECRAFT_SOME_RUNES(
            Difficulty.EASY,
            "Runecraft Some Runes",
            "do something",
            "some rew",
            "some reward",
            58419,
            null,
            Item(1437, 75),
            "Beginner"
        ),
        BURY_A_BIG_BONE(
            Difficulty.EASY,
            "Bury A Big Bone",
            "do something",
            "some rew",
            "some reward",
            58420,
            null,
            Item(10448, 1),
            "Beginner"
        ),
        COMPLETE_A_SLAYER_TASK(
            Difficulty.EASY,
            "Complete A Slayer Task",
            "do something",
            "some rew",
            "some reward",
            58421,
            null,
            Item(13285, 1),
            "Beginner"
        ),
        SET_UP_A_CANNON(
            Difficulty.EASY,
            "Set Up A Cannon",
            "do something",
            "some rew",
            "some reward",
            58422,
            null,
            Item(2, 100),
            "Beginner"
        ),
        KILL_A_MONSTER_USING_MELEE(
            Difficulty.EASY,
            "Kill a Monster Using Melee",
            "do something",
            "some rew",
            "some reward",
            58423,
            null,
            Item(1215, 1),
            "Beginner"
        ),
        KILL_A_MONSTER_USING_RANGED(
            Difficulty.EASY,
            "Kill a Monster Using Ranged",
            "do something",
            "some rew",
            "some reward",
            58424,
            null,
            Item(892, 100),
            "Beginner"
        ),
        KILL_A_MONSTER_USING_MAGIC(
            Difficulty.EASY,  /*difficulty*/
            "Kill a monster (magic)",  /*interface line*/
            "Kill any npc using a magic spell.",  /*description*/
            "Magic armour, a staff and runes.",  /*requirements*/
            "1 Achievement point",  /*rewards*/
            58425, null, null, "Beginner"
        ),
        EASY(null, "Easy", null, null, null, 58426, null, null, null), DEAL_EASY_DAMAGE_USING_MELEE(
            Difficulty.EASY,
            "Deal 1000 Melee Damage",
            "do something",
            "some rew",
            "some reward",
            58427,
            intArrayOf(0, 1000),
            Item(995, 100000),
            "Beginner"
        ),
        DEAL_EASY_DAMAGE_USING_RANGED(
            Difficulty.EASY,
            "Deal 1000 Ranged Damage",
            "do something",
            "some rew",
            "some reward",
            58428,
            intArrayOf(1, 1000),
            Item(995, 100000),
            "Beginner"
        ),
        DEAL_EASY_DAMAGE_USING_MAGIC(
            Difficulty.EASY,
            "Deal 1000 Magic Damage",
            "do something",
            "some rew",
            "some reward",
            58429,
            intArrayOf(2, 1000),
            Item(995, 100000),
            "Beginner"
        ),
        PERFORM_A_SPECIAL_ATTACK(
            Difficulty.EASY,
            "Perform a Special Attack",
            "do something",
            "some rew",
            "some reward",
            58430,
            null,
            Item(4153, 1),
            "Beginner"
        ),
        FIGHT_ANOTHER_PLAYER(
            Difficulty.EASY,
            "Fight Another Player",
            "do something",
            "some rew",
            "some reward",
            58431,
            null,
            Item(10828, 1),
            "Beginner"
        ),
        BURY_A_DRAGON_BONE(
            Difficulty.EASY,
            "Bury A Dragon Bone",
            "do something",
            "some rew",
            "some reward",
            58432,
            null,
            Item(2452, 1),
            "Beginner"
        ),
        DEFEAT_A_REVENANT_DARK_BEAST(
            Difficulty.MEDIUM,
            "Slay A Revenant Dark Beast",
            "do something",
            "some rew",
            "some reward",
            58433,
            null,
            Item(746, 1),
            "Beginner"
        ),
        FILL_WELL_OF_GOODWILL_50M(
            Difficulty.MEDIUM,
            "Pour 50M Into The Well",
            "do something",
            "some rew",
            "some reward",
            58434,
            intArrayOf(4, 50000000),
            Item(2778, 1),
            "Beginner"
        ),
        CUT_100_MAGIC_LOGS(
            Difficulty.MEDIUM,
            "Cut 100 Magic Logs",
            "do something",
            "some rew",
            "some reward",
            58435,
            intArrayOf(5, 100),
            Item(995, 250000),
            "Beginner"
        ),
        BURN_100_MAGIC_LOGS(
            Difficulty.MEDIUM,
            "Burn 100 Magic Logs",
            "do something",
            "some rew",
            "some reward",
            58436,
            intArrayOf(6, 100),
            Item(995, 250000),
            "Beginner"
        ),
        FISH_25_ROCKTAILS(
            Difficulty.MEDIUM,
            "Fish 25 Rocktails",
            "do something",
            "some rew",
            "some reward",
            58437,
            intArrayOf(7, 25),
            Item(25, 10),
            "Beginner"
        ),
        COOK_25_ROCKTAILS(
            Difficulty.MEDIUM,
            "Cook 25 Rocktails",
            "do something",
            "some rew",
            "some reward",
            58438,
            intArrayOf(8, 25),
            Item(25, 10),
            "Beginner"
        ),
        MINE_25_RUNITE_ORES(
            Difficulty.MEDIUM,
            "Mine 25 Runite Ores",
            "do something",
            "some rew",
            "some reward",
            58439,
            intArrayOf(9, 25),
            Item(454, 100),
            "Beginner"
        ),
        SMELT_25_RUNE_BARS(
            Difficulty.MEDIUM,
            "Smelt 25 Rune Bars",
            "do something",
            "some rew",
            "some reward",
            58440,
            intArrayOf(10, 25),
            Item(454, 100),
            "Beginner"
        ),
        HARVEST_10_TORSTOLS(
            Difficulty.MEDIUM,
            "Harvest 10 Torstols",
            "do something",
            "some rew",
            "some reward",
            58441,
            intArrayOf(11, 10),
            Item(19670, 1),
            "Beginner"
        ),
        INFUSE_25_TITAN_POUCHES(
            Difficulty.MEDIUM,
            "Infuse 25 Steel Titans",
            "do something",
            "some rew",
            "some reward",
            58442,
            intArrayOf(12, 25),
            Item(454, 100),
            "Beginner"
        ),
        CATCH_5_KINGLY_IMPLINGS(
            Difficulty.MEDIUM,
            "Catch 5 Kingly Implings",
            "do something",
            "some rew",
            "some reward",
            58443,
            intArrayOf(13, 5),
            Item(995, 2000000),
            "Beginner"
        ),
        COMPLETE_A_HARD_SLAYER_TASK(
            Difficulty.MEDIUM,
            "Complete A Hard Slayer Task",
            "do something",
            "some rew",
            "some reward",
            58444,
            null,
            Item(13283, 1),
            "Beginner"
        ),
        CRAFT_20_BLACK_DHIDE_BODIES(
            Difficulty.MEDIUM,
            "Craft 20 Black D'hide Bodies",
            "do something",
            "some rew",
            "some reward",
            58445,
            intArrayOf(14, 20),
            Item(537, 10),
            "Beginner"
        ),
        FLETCH_450_RUNE_ARROWS(
            Difficulty.MEDIUM,
            "Fletch 450 Rune Arrows",
            "do something",
            "some rew",
            "some reward",
            58446,
            intArrayOf(15, 450),
            Item(9144, 50),
            "Beginner"
        ),
        STEAL_140_SCIMITARS(
            Difficulty.MEDIUM,
            "Steal 140 Scimitars",
            "do something",
            "some rew",
            "some reward",
            58447,
            intArrayOf(16, 140),
            Item(995, 500000),
            "Beginner"
        ),
        MIX_AN_OVERLOAD_POTION(
            Difficulty.MEDIUM,
            "Mix An Overload Potion",
            "do something",
            "some rew",
            "some reward",
            58448,
            null,
            Item(995, 500000),
            "Beginner"
        ),
        ASSEMBLE_A_GODSWORD(
            Difficulty.MEDIUM,
            "Assemble A Godsword",
            "do something",
            "some rew",
            "some reward",
            58449,
            null,
            Item(22053, 1),
            "Beginner"
        ),
        CLIMB_50_AGILITY_OBSTACLES(
            Difficulty.MEDIUM,
            "Climb 50 Agility Obstacles",
            "do something",
            "some rew",
            "some reward",
            58450,
            intArrayOf(17, 50),
            Item(995, 1000000),
            "Beginner"
        ),
        MEDIUM(null, "Medium", null, null, null, 58451, null, null, null), RUNECRAFT_500_BLOOD_RUNES(
            Difficulty.MEDIUM,
            "Runecraft 500 Blood Runes",
            "do something",
            "some rew",
            "some reward",
            58452,
            intArrayOf(18, 500),
            Item(7937, 350),
            "Beginner"
        ),
        BURY_25_FROST_DRAGON_BONES(
            Difficulty.MEDIUM,
            "Use 25 Frost Dragon Bones",
            "do something",
            "some rew",
            "some reward",
            58453,
            intArrayOf(19, 25),
            Item(8716, 1),
            "Beginner"
        ),
        FIRE_500_CANNON_BALLS(
            Difficulty.MEDIUM,
            "Fire 500 Cannon Balls",
            "do something",
            "some rew",
            "some reward",
            58454,
            intArrayOf(20, 500),
            Item(2, 175),
            "Beginner"
        ),
        DEAL_MEDIUM_DAMAGE_USING_MELEE(
            Difficulty.MEDIUM,
            "Deal 100K Melee Damage",
            "do something",
            "some rew",
            "some reward",
            58455,
            intArrayOf(21, 100000),
            Item(2440, 1),
            "Beginner"
        ),
        DEAL_MEDIUM_DAMAGE_USING_RANGED(
            Difficulty.MEDIUM,
            "Deal 100K Ranged Damage",
            "do something",
            "some rew",
            "some reward",
            58456,
            intArrayOf(22, 100000),
            Item(2444, 1),
            "Beginner"
        ),
        DEAL_MEDIUM_DAMAGE_USING_MAGIC(
            Difficulty.MEDIUM,
            "Deal 100K Magic Damage",
            "do something",
            "some rew",
            "some reward",
            58457,
            intArrayOf(23, 100000),
            Item(3040, 1),
            "Beginner"
        ),
        DEFEAT_THE_KING_BLACK_DRAGON(
            Difficulty.MEDIUM,
            "Defeat The King Black Dragon",
            "do something",
            "some rew",
            "some reward",
            58458,
            null,
            Item(4087, 1),
            "Beginner"
        ),
        DEFEAT_THE_CHAOS_ELEMENTAL(
            Difficulty.MEDIUM,
            "Defeat The Chaos Elemental",
            "do something",
            "some rew",
            "some reward",
            58459,
            null,
            Item(1149, 1),
            "Beginner"
        ),
        DEFEAT_A_TORMENTED_DEMON(
            Difficulty.MEDIUM,
            "Defeat A Tormented Demon",
            "do something",
            "some rew",
            "some reward",
            58460,
            null,
            Item(3140, 1),
            "Beginner"
        ),
        DEFEAT_SCORPIA(
            Difficulty.MEDIUM,
            "Defeat Scorpia",
            "do something",
            "some rew",
            "some reward",
            58461,
            null,
            Item(1305, 1),
            "Beginner"
        ),
        DEFEAT_ZULRAH(
            Difficulty.MEDIUM,
            "Defeat Zulrah",
            "do something",
            "some rew",
            "some reward",
            58462,
            null,
            Item(12934, 150),
            "Beginner"
        ),  /* NEEDS TO BE CHANGED */
        DEFEAT_10_PLAYERS(
            Difficulty.MEDIUM,
            "Defeat 10 Players",
            "do something",
            "some rew",
            "some reward",
            58463,
            intArrayOf(24, 10),
            Item(13262, 1),
            "Beginner"
        ),
        REACH_A_KILLSTREAK_OF_3(
            Difficulty.MEDIUM,
            "Reach A Killstreak Of 3",
            "do something",
            "some rew",
            "some reward",
            58464,
            null,
            Item(15332, 1),
            "Beginner"
        ),
        FILL_WELL_OF_GOODWILL_250M(
            Difficulty.HARD,
            "Pour 250M Into The Well",
            "do something",
            "some rew",
            "some reward",
            58465,
            intArrayOf(25, 250000000),
            Item(6760, 1),
            "Beginner"
        ),
        CUT_5000_MAGIC_LOGS(
            Difficulty.HARD,
            "Cut 5000 Magic Logs",
            "do something",
            "some rew",
            "some reward",
            58466,
            intArrayOf(26, 5000),
            Item(14140, 1),
            "Beginner"
        ),
        BURN_2500_MAGIC_LOGS(
            Difficulty.HARD,
            "Burn 2500 Magic Logs",
            "do something",
            "some rew",
            "some reward",
            58467,
            intArrayOf(27, 2500),
            Item(10327, 1),
            "Beginner"
        ),
        FISH_2000_ROCKTAILS(
            Difficulty.HARD,
            "Fish 2000 Rocktails",
            "do something",
            "some rew",
            "some reward",
            58468,
            intArrayOf(28, 2000),
            Item(25, 250),
            "Beginner"
        ),
        COOK_1000_ROCKTAILS(
            Difficulty.HARD,
            "Cook 1000 Rocktails",
            "do something",
            "some rew",
            "some reward",
            58469,
            intArrayOf(29, 1000),
            Item(25, 250),
            "Beginner"
        ),
        MINE_2000_RUNITE_ORES(
            Difficulty.HARD,
            "Mine 2000 Runite Ores",
            "do something",
            "some rew",
            "some reward",
            58470,
            intArrayOf(30, 2000),
            Item(14130, 1),
            "Beginner"
        ),
        SMELT_1000_RUNE_BARS(
            Difficulty.HARD,
            "Smelt 1000 Rune Bars",
            "do something",
            "some rew",
            "some reward",
            58471,
            intArrayOf(31, 1000),
            Item(454, 100),
            "Beginner"
        ),
        HARVEST_1000_TORSTOLS(
            Difficulty.HARD,
            "Harvest 1000 Torstols",
            "do something",
            "some rew",
            "some reward",
            58472,
            intArrayOf(32, 1000),
            Item(3025, 50),
            "Beginner"
        ),
        INFUSE_250_STEEL_TITAN_POUCHES(
            Difficulty.HARD,
            "Infuse 250 Steel Titans",
            "do something",
            "some rew",
            "some reward",
            58473,
            intArrayOf(33, 250),
            Item(12163, 100),
            "Beginner"
        ),
        CRAFT_1000_DIAMOND_GEMS(
            Difficulty.HARD,
            "Craft 1000 Diamond Gems",
            "do something",
            "some rew",
            "some reward",
            58474,
            intArrayOf(34, 1000),
            Item(990, 2),
            "Beginner"
        ),
        CATCH_100_KINGLY_IMPLINGS(
            Difficulty.HARD,
            "Catch 100 Kingly Imps",
            "do something",
            "some rew",
            "some reward",
            58475,
            intArrayOf(35, 100),
            Item(15518, 10),
            "Beginner"
        ),
        HARD(null, "Hard", null, null, null, 58476, null, null, null), FLETCH_5000_RUNE_ARROWS(
            Difficulty.HARD,
            "Fletch 5000 Rune Arrows",
            "do something",
            "some rew",
            "some reward",
            58477,
            intArrayOf(36, 5000),
            Item(9144, 1000),
            "Beginner"
        ),
        STEAL_5000_SCIMITARS(
            Difficulty.HARD,
            "Steal 5000 Scimitars",
            "do something",
            "some rew",
            "some reward",
            58478,
            intArrayOf(37, 5000),
            Item(995, 2000000),
            "Beginner"
        ),
        RUNECRAFT_8000_BLOOD_RUNES(
            Difficulty.HARD,
            "Runecraft 8000 Blood Runes",
            "do something",
            "some rew",
            "some reward",
            58479,
            intArrayOf(38, 8000),
            Item(7937, 2500),
            "Beginner"
        ),
        BURY_500_FROST_DRAGON_BONES(
            Difficulty.HARD,
            "Bury 500 Frost Dragon Bones",
            "do something",
            "some rew",
            "some reward",
            58480,
            intArrayOf(39, 500),
            Item(995, 5000000),
            "Beginner"
        ),
        FIRE_5000_CANNON_BALLS(
            Difficulty.HARD,
            "Fire 5000 Cannon Balls",
            "do something",
            "some rew",
            "some reward",
            58481,
            intArrayOf(40, 5000),
            Item(15241, 1),
            "Beginner"
        ),
        MIX_100_OVERLOAD_POTIONS(
            Difficulty.HARD,
            "Mix 100 Overload Potions",
            "do something",
            "some rew",
            "some reward",
            58482,
            intArrayOf(41, 100),
            Item(19670, 1),
            "Beginner"
        ),
        COMPLETE_AN_ELITE_SLAYER_TASK(
            Difficulty.HARD,
            "Complete An Elite Slayer Task",
            "do something",
            "some rew",
            "some reward",
            58483,
            null,
            Item(13281, 1),
            "Beginner"
        ),
        ASSEMBLE_5_GODSWORDS(
            Difficulty.HARD,
            "Assemble 5 Godswords",
            "do something",
            "some rew",
            "some reward",
            58484,
            intArrayOf(42, 5),
            Item(11690, 1),
            "Beginner"
        ),
        DEAL_HARD_DAMAGE_USING_MELEE(
            Difficulty.HARD,
            "Deal 10M Melee Damage",
            "do something",
            "some rew",
            "some reward",
            58485,
            intArrayOf(43, 10000000),
            Item(4720, 1),
            "Beginner"
        ),
        DEAL_HARD_DAMAGE_USING_RANGED(
            Difficulty.HARD,
            "Deal 10M Ranged Damage",
            "do something",
            "some rew",
            "some reward",
            58486,
            intArrayOf(44, 10000000),
            Item(4736, 1),
            "Beginner"
        ),
        DEAL_HARD_DAMAGE_USING_MAGIC(
            Difficulty.HARD,
            "Deal 10M Magic Damage",
            "do something",
            "some rew",
            "some reward",
            58487,
            intArrayOf(45, 10000000),
            Item(4712, 1),
            "Beginner"
        ),
        DEFEAT_JAD(
            Difficulty.HARD,
            "Defeat Jad",
            "do something",
            "some rew",
            "some reward",
            58488,
            null,
            Item(6529, 10000),
            "Beginner"
        ),
        DEFEAT_BANDOS_AVATAR(
            Difficulty.HARD,
            "Defeat Bandos Avatar",
            "do something",
            "some rew",
            "some reward",
            58489,
            null,
            Item(1580, 1),
            "Beginner"
        ),
        DEFEAT_GENERAL_GRAARDOR(
            Difficulty.HARD,
            "Defeat General Graardor",
            "do something",
            "some rew",
            "some reward",
            58490,
            null,
            Item(13283, 1),
            "Beginner"
        ),
        DEFEAT_KREE_ARRA(
            Difficulty.HARD,
            "Defeat Kree'Arra",
            "do something",
            "some rew",
            "some reward",
            58470,
            null,
            Item(13283, 1),
            "Beginner"
        ),
        DEFEAT_COMMANDER_ZILYANA(
            Difficulty.HARD,
            "Defeat Commander Zilyana",
            "do something",
            "some rew",
            "some reward",
            58492,
            null,
            Item(13283, 1),
            "Beginner"
        ),
        DEFEAT_KRIL_TSUTSAROTH(
            Difficulty.HARD,
            "Defeat K'ril Tsutsaroth",
            "do something",
            "some rew",
            "some reward",
            58493,
            null,
            Item(22053, 1),
            "Beginner"
        ),
        DEFEAT_THE_CORPOREAL_BEAST(
            Difficulty.HARD,
            "Defeat The Corporeal Beast",
            "do something",
            "some rew",
            "some reward",
            58494,
            null,
            Item(13734, 1),
            "Beginner"
        ),
        DEFEAT_NEX(
            Difficulty.HARD,
            "Defeat Nex",
            "do something",
            "some rew",
            "some reward",
            58495,
            null,
            Item(19372, 1),
            "Beginner"
        ),
        DEFEAT_30_PLAYERS(
            Difficulty.HARD,
            "Defeat 30 Players",
            "do something",
            "some rew",
            "some reward",
            58496,
            intArrayOf(46, 30),
            Item(15332, 1),
            "Beginner"
        ),
        REACH_A_KILLSTREAK_OF_6(
            Difficulty.HARD,
            "Reach A Killstreak Of 6",
            "do something",
            "some rew",
            "some reward",
            58497,
            null,
            Item(15332, 1),
            "Beginner"
        ),
        COMPLETE_ALL_HARD_TASKS(
            Difficulty.ELITE,
            "Complete All Hard Tasks",
            "do something",
            "some rew",
            "some reward",
            58498,
            intArrayOf(47, 32),
            Item(7158, 1),
            "Beginner"
        ),
        CUT_AN_ONYX_STONE(
            Difficulty.ELITE,
            "Cut An Onyx Stone",
            "do something",
            "some rew",
            "some reward",
            58499,
            null,
            Item(6570, 1),
            "Beginner"
        ),
        ELITE(null, "Elite", null, null, null, 58501, null, null, null), REACH_MAX_EXP_IN_A_SKILL(
            Difficulty.ELITE,
            "Reach Max Exp In A Skill",
            "do something",
            "some rew",
            "some reward",
            58501,
            null,
            Item(19111, 1),
            "Beginner"
        );
    }

    enum class Difficulty {
        BEGINNER, EASY, MEDIUM, HARD, ELITE
    }

    class AchievementAttributes {
        /** ACHIEVEMENTS  */
        var completion = BooleanArray(AchievementData.values().size)
        var progress = IntArray(105)
        fun setCompletion(index: Int, value: Boolean) {
            completion[index] = value
        }

        fun setProgress(index: Int, value: Int) {
            progress[index] = value
        }

        /** MISC  */
        var coinsGambled = 0
        var totalLoyaltyPointsEarned = 0.0
            private set
        var godsKilled = BooleanArray(5)
        fun incrementTotalLoyaltyPointsEarned(totalLoyaltyPointsEarned: Double) {
            this.totalLoyaltyPointsEarned += totalLoyaltyPointsEarned
        }

        fun setGodKilled(index: Int, godKilled: Boolean) {
            godsKilled[index] = godKilled
        }
    }
}