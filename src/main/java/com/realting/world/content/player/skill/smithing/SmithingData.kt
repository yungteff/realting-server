package com.realting.world.content.player.skill.smithing


import com.realting.model.Skill
import com.realting.model.input.impl.EnterAmountOfBarsToSmelt
import java.util.Locale
import com.realting.model.Item
import com.realting.model.definitions.ItemDefinition
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc

object SmithingData {
    val BARS_SMITH_ORDER =
        intArrayOf(2363, 2361, 2359, 2353, 2351, 2349, 2357, 2355) //Rune -> Bronze, then Gold, Silver

    @JvmField
    val SMELT_BARS = intArrayOf(2349, 2351, 2355, 2353, 2357, 2359, 2361, 2363)
    val SMELT_FRAME = intArrayOf(2405, 2406, 2407, 2409, 2410, 2411, 2412, 2413)

    //BarId, Ore1, Ore2, Levelreq, XP
    val SmeltData = arrayOf(
        intArrayOf(2349, 438, 436, 1, 7),
        intArrayOf(2351, 440, -1, 15, 13),
        intArrayOf(2355, 442, -1, 20, 14),
        intArrayOf(2353, 440, 453, 30, 18),
        intArrayOf(2357, 444, -1, 40, 40),
        intArrayOf(2359, 447, 453, 50, 50),
        intArrayOf(2361, 449, 453, 70, 38),
        intArrayOf(2363, 451, 453, 85, 50),
        intArrayOf(2363, 451, 453, 85, 50)
    )

    /*
	 * Gets the ores needed and stores them in to the array so we can use them.
	 */
    fun getOres(bar: Int): IntArray {
        val ores = IntArray(2)
        for (i in SmeltData.indices) {
            if (SmeltData[i][0] == bar) {
                val ore1 = SmeltData[i][1]
                val ore2 = SmeltData[i][2]
                ores[0] = ore1
                ores[1] = ore2
            }
        }
        return ores
    }

    /*
	 * Checks if a player has ores required for a certain barId
	 */
    @JvmStatic
    fun hasOres(player: Player, barId: Int): Boolean {
        player.ores = getOres(barId) //Insert ores ids to the array
        if (player.ores[0] > 0 && player.ores[1] < 0) {
            if (player.inventory.getAmount(player.ores[0]) > 0) return true
        } else if (player.ores[1] > 0 && player.ores[1] != 453 && player.ores[0] > 0) {
            if (player.inventory.getAmount(player.ores[1]) > 0 && player.inventory.getAmount(player.ores[0]) > 0) return true
        } else if (player.ores[1] > 0 && player.ores[1] == 453 && player.ores[0] > 0) {
            if (player.inventory.getAmount(player.ores[1]) >= getCoalAmount(barId) && player.inventory.getAmount(
                    player.ores[0]
                ) > 0
            ) return true
        }
        return false
    }

    /*
	 * Checks if a player has required stats to smelt certain barId
	 */
    fun canSmelt(player: Player, barId: Int): Boolean {
        if (getLevelReq(barId) > player.skillManager.getCurrentLevel(Skill.SMITHING)) {
            player.packetSender.sendMessage("You need a Smithing level of at least " + getLevelReq(barId) + " to make this bar.")
            return false
        }
        if (!hasOres(player, barId)) {
            player.packetSender.sendMessage("You do not have the required ores to make this bar.")
            var requirement: String? = null
            if (player.ores[0] > 0 && player.ores[1] > 0 && player.ores[1] != 453) {
                requirement =
                    "To make " + anOrA(barId) + " " + ItemDefinition.forId(barId).name + ", you need some " + ItemDefinition.forId(
                        player.ores[0]
                    ).name.replace(" ore", "") + " and " + ItemDefinition.forId(player.ores[1]).name + "."
            } else if (player.ores[0] > 0 && player.ores[1] == -1) {
                requirement =
                    "To make " + anOrA(barId) + " " + ItemDefinition.forId(barId).name + ", you need some " + ItemDefinition.forId(
                        player.ores[0]
                    ).name + "."
            } else if (player.ores[0] > 0 && player.ores[1] == 453) { //The bar uses custom coal amount
                requirement =
                    "To make " + anOrA(barId) + " " + ItemDefinition.forId(barId).name + ", you need some " + ItemDefinition.forId(
                        player.ores[0]
                    ).name.replace(" ore", "") + " and " + getCoalAmount(barId) + " " + ItemDefinition.forId(
                        player.ores[1]
                    ).name + " ores."
            }
            if (requirement != null) player.packetSender.sendMessage(requirement)
            return false
        }
        return true
    }

    /*
	 * Gets the correct 'message'
	 */
    fun anOrA(barId: Int): String {
        return if (barId == 2351 || barId == 2361) "an" else "a"
    }

    /*
	 * Gets Smithing level required for a certain barId
	 */
    fun getLevelReq(barId: Int): Int {
        for (i in SmeltData.indices) {
            if (SmeltData[i][0] == barId) {
                return SmeltData[i][3]
            }
        }
        return 1
    }

    /*
	 * Handles the Smithing interfaces and their buttons
	 */
    @JvmStatic
    fun handleButtons(player: Player, id: Int): Boolean {
        when (id) {
            3987 -> {
                Smelting.smeltBar(player, 2349, 1)
                player.packetSender.sendInterfaceRemoval()
                return true
            }
            3986 -> {
                Smelting.smeltBar(player, 2349, 5)
                player.packetSender.sendInterfaceRemoval()
                return true
            }
            2807 -> {
                Smelting.smeltBar(player, 2349, 10)
                player.packetSender.sendInterfaceRemoval()
                return true
            }
            3991 -> {
                Smelting.smeltBar(player, 2351, 1)
                player.packetSender.sendInterfaceRemoval()
                return true
            }
            3990 -> {
                Smelting.smeltBar(player, 2351, 5)
                player.packetSender.sendInterfaceRemoval()
                return true
            }
            3989 -> {
                Smelting.smeltBar(player, 2351, 10)
                player.packetSender.sendInterfaceRemoval()
                return true
            }
            3995 -> {
                Smelting.smeltBar(player, 2355, 1)
                player.packetSender.sendInterfaceRemoval()
                return true
            }
            3994 -> {
                Smelting.smeltBar(player, 2355, 5)
                player.packetSender.sendInterfaceRemoval()
                return true
            }
            3993 -> {
                Smelting.smeltBar(player, 2355, 10)
                player.packetSender.sendInterfaceRemoval()
                return true
            }
            3999 -> {
                Smelting.smeltBar(player, 2353, 1)
                player.packetSender.sendInterfaceRemoval()
                return true
            }
            3998 -> {
                Smelting.smeltBar(player, 2353, 5)
                player.packetSender.sendInterfaceRemoval()
                return true
            }
            3997 -> {
                Smelting.smeltBar(player, 2353, 10)
                player.packetSender.sendInterfaceRemoval()
                return true
            }
            4003 -> {
                Smelting.smeltBar(player, 2357, 1)
                player.packetSender.sendInterfaceRemoval()
                return true
            }
            4002 -> {
                Smelting.smeltBar(player, 2357, 5)
                player.packetSender.sendInterfaceRemoval()
                return true
            }
            4001 -> {
                Smelting.smeltBar(player, 2357, 10)
                player.packetSender.sendInterfaceRemoval()
                return true
            }
            7441 -> {
                Smelting.smeltBar(player, 2359, 1)
                player.packetSender.sendInterfaceRemoval()
                return true
            }
            7440 -> {
                Smelting.smeltBar(player, 2359, 5)
                player.packetSender.sendInterfaceRemoval()
                return true
            }
            6397 -> {
                Smelting.smeltBar(player, 2359, 10)
                player.packetSender.sendInterfaceRemoval()
                return true
            }
            7446 -> {
                Smelting.smeltBar(player, 2361, 1)
                player.packetSender.sendInterfaceRemoval()
                return true
            }
            7444 -> {
                Smelting.smeltBar(player, 2361, 5)
                player.packetSender.sendInterfaceRemoval()
                return true
            }
            7443 -> {
                Smelting.smeltBar(player, 2361, 10)
                player.packetSender.sendInterfaceRemoval()
                return true
            }
            7450 -> {
                Smelting.smeltBar(player, 2363, 1)
                player.packetSender.sendInterfaceRemoval()
                return true
            }
            7449 -> {
                Smelting.smeltBar(player, 2363, 5)
                player.packetSender.sendInterfaceRemoval()
                return true
            }
            7448 -> {
                Smelting.smeltBar(player, 2363, 10)
                player.packetSender.sendInterfaceRemoval()
                return true
            }
            2414, 3988, 3992, 3996, 4000, 4158, 7442, 7447 -> {
                val bar =
                    when (id) {
                        2414 -> 2349
                        3988 -> 2351
                        3992 -> 2355
                        3996 -> 2353
                        4000 -> 2357
                        4158 -> 2359
                        7442 -> 2361
                        7447 -> 2363
                        else -> -1
                    }
                if (bar > 0) {
                    player.inputHandling = EnterAmountOfBarsToSmelt(bar)
                    player.packetSender.sendEnterAmountPrompt("How many " + ItemDefinition.forId(bar).name + "s would you like to smelt?")
                }
                return true
            }
        }
        return false
    }

    fun handleSpecialBar(barId: Int, player: Player?, barAction: String) {
        if (barAction.equals("message", ignoreCase = true)) {
            return
        }
        if (barAction.equals("delete", ignoreCase = true)) {
            return
        }
    }

    fun getCoalAmount(barId: Int): Int {
        if (barId == 2359) return 4
        if (barId == 2361) return 6
        return if (barId == 2363) 8 else 2
    }

    fun showBronzeInterface(player: Player) {
        val fiveb = GetForBars(2349, 5, player)
        val threeb = GetForBars(2349, 3, player)
        val twob = GetForBars(2349, 2, player)
        val oneb = GetForBars(2349, 1, player)
        player.packetSender.sendString(1112, fiveb + "5 Bars" + fiveb)
        player.packetSender.sendString(1109, threeb + "3 Bars" + threeb)
        player.packetSender.sendString(1110, threeb + "3 Bars" + threeb)
        player.packetSender.sendString(1118, threeb + "3 Bars" + threeb)
        player.packetSender.sendString(1111, threeb + "3 Bars" + threeb)
        player.packetSender.sendString(1095, threeb + "3 Bars" + threeb)
        player.packetSender.sendString(1115, threeb + "3 Bars" + threeb)
        player.packetSender.sendString(1090, threeb + "3 Bars" + threeb)
        player.packetSender.sendString(1113, twob + "2 Bars" + twob)
        player.packetSender.sendString(1116, twob + "2 Bars" + twob)
        player.packetSender.sendString(1114, twob + "2 Bars" + twob)
        player.packetSender.sendString(1089, twob + "2 Bars" + twob)
        player.packetSender.sendString(8428, twob + "2 Bars" + twob)
        player.packetSender.sendString(1124, oneb + "1 Bar" + oneb)
        player.packetSender.sendString(1125, oneb + "1 Bar" + oneb)
        player.packetSender.sendString(1126, oneb + "1 Bar" + oneb)
        player.packetSender.sendString(1127, oneb + "1 Bar" + oneb)
        player.packetSender.sendString(1128, oneb + "1 Bar" + oneb)
        player.packetSender.sendString(1129, oneb + "1 Bar" + oneb)
        player.packetSender.sendString(1130, oneb + "1 Bar" + oneb)
        player.packetSender.sendString(1131, oneb + "1 Bar" + oneb)
        player.packetSender.sendString(13357, oneb + "1 Bar" + oneb)
        player.packetSender.sendString(11459, oneb + "1 Bar" + oneb)
        player.packetSender.sendString(1101, GetForlvl(18, player) + "Plate Body" + GetForlvl(18, player))
        player.packetSender.sendString(1099, GetForlvl(16, player) + "Plate Legs" + GetForlvl(16, player))
        player.packetSender.sendString(1100, GetForlvl(16, player) + "Plate Skirt" + GetForlvl(16, player))
        player.packetSender.sendString(1088, GetForlvl(14, player) + "2 Hand Sword" + GetForlvl(14, player))
        player.packetSender.sendString(1105, GetForlvl(12, player) + "Kite Shield" + GetForlvl(12, player))
        player.packetSender.sendString(1098, GetForlvl(11, player) + "Chain Body" + GetForlvl(11, player))
        player.packetSender.sendString(1092, GetForlvl(10, player) + "Battle Axe" + GetForlvl(10, player))
        player.packetSender.sendString(1083, GetForlvl(9, player) + "Warhammer" + GetForlvl(9, player))
        player.packetSender.sendString(1104, GetForlvl(8, player) + "Square Shield" + GetForlvl(8, player))
        player.packetSender.sendString(1103, GetForlvl(7, player) + "Full Helm" + GetForlvl(7, player))
        player.packetSender.sendString(1106, GetForlvl(7, player) + "Throwing Knives" + GetForlvl(7, player))
        player.packetSender.sendString(1086, GetForlvl(6, player) + "Long Sword" + GetForlvl(6, player))
        player.packetSender.sendString(1087, GetForlvl(5, player) + "Scimitar" + GetForlvl(5, player))
        player.packetSender.sendString(1108, GetForlvl(5, player) + "Arrowtips" + GetForlvl(5, player))
        player.packetSender.sendString(1085, GetForlvl(4, player) + "Sword" + GetForlvl(4, player))
        player.packetSender.sendString(1107, GetForlvl(4, player) + "Bolts" + GetForlvl(4, player))
        player.packetSender.sendString(13358, GetForlvl(4, player) + "Nails" + GetForlvl(4, player))
        player.packetSender.sendString(1102, GetForlvl(3, player) + "Medium Helm" + GetForlvl(3, player))
        player.packetSender.sendString(1093, GetForlvl(2, player) + "Mace" + GetForlvl(2, player))
        player.packetSender.sendString(1094, GetForlvl(1, player) + "Dagger" + GetForlvl(1, player))
        player.packetSender.sendString(1091, GetForlvl(1, player) + "Hatchet" + GetForlvl(1, player))
        player.packetSender.sendString(8429, GetForlvl(8, player) + "Claws" + GetForlvl(8, player))
        player.packetSender.sendSmithingData(1205, 0, 1119, 1)
        player.packetSender.sendSmithingData(1351, 0, 1120, 1)
        player.packetSender.sendSmithingData(1103, 0, 1121, 1)
        player.packetSender.sendSmithingData(1139, 0, 1122, 1)
        player.packetSender.sendSmithingData(819, 0, 1123, 10)
        player.packetSender.sendSmithingData(1277, 1, 1119, 1)
        player.packetSender.sendSmithingData(1422, 1, 1120, 1)
        player.packetSender.sendSmithingData(1075, 1, 1121, 1)
        player.packetSender.sendSmithingData(1155, 1, 1122, 1)
        player.packetSender.sendSmithingData(39, 1, 1123, 15)
        player.packetSender.sendSmithingData(1321, 2, 1119, 1)
        player.packetSender.sendSmithingData(1337, 2, 1120, 1)
        player.packetSender.sendSmithingData(1087, 2, 1121, 1)
        player.packetSender.sendSmithingData(1173, 2, 1122, 1)
        player.packetSender.sendSmithingData(864, 2, 1123, 5)
        player.packetSender.sendSmithingData(1291, 3, 1119, 1)
        player.packetSender.sendSmithingData(1375, 3, 1120, 1)
        player.packetSender.sendSmithingData(1117, 3, 1121, 1)
        player.packetSender.sendSmithingData(1189, 3, 1122, 1)
        player.packetSender.sendSmithingData(1307, 4, 1119, 1)
        player.packetSender.sendSmithingData(4819, 4, 1122, 15)
        player.packetSender.sendSmithingData(3095, 4, 1120, 1)
        player.packetSender.sendSmithingData(-1, 3, 1123, 1)
        player.packetSender.sendString(1134, "")
        player.packetSender.sendString(11461, "")
        player.packetSender.sendString(1132, "")
        player.packetSender.sendString(1096, "")
        player.packetSender.sendString(1135, twob + "2 Bars" + twob) //CROSSBOW
        player.packetSender.sendString(11459, oneb + "1 Bar" + oneb) //PICKAXE
        player.packetSender.sendSmithingData(9174, 4, 1123, 1) //CROSSBOW
        player.packetSender.sendSmithingData(1265, 4, 1121, 1) //PICKAXE
        sendString(player, GetForlvl(6, player) + "Crossbow" + GetForlvl(6, player), 1134) //CROSSBOW
        sendString(player, GetForlvl(7, player) + "Pickaxe" + GetForlvl(7, player), 11461) //PICKAXE
        player.packetSender.sendInterface(994)
    }

    fun makeIronInterface(player: Player) {
        val fiveb = GetForBars(2351, 5, player)
        val threeb = GetForBars(2351, 3, player)
        val twob = GetForBars(2351, 2, player)
        val oneb = GetForBars(2351, 1, player)
        player.packetSender.sendString(1112, fiveb + "5 Bars" + fiveb)
        player.packetSender.sendString(1109, threeb + "3 Bars" + threeb)
        player.packetSender.sendString(1110, threeb + "3 Bars" + threeb)
        player.packetSender.sendString(1118, threeb + "3 Bars" + threeb)
        player.packetSender.sendString(1111, threeb + "3 Bars" + threeb)
        player.packetSender.sendString(1095, threeb + "3 Bars" + threeb)
        player.packetSender.sendString(1115, threeb + "3 Bars" + threeb)
        player.packetSender.sendString(1090, threeb + "3 Bars" + threeb)
        player.packetSender.sendString(1113, twob + "2 Bars" + twob)
        player.packetSender.sendString(1116, twob + "2 Bars" + twob)
        player.packetSender.sendString(1114, twob + "2 Bars" + twob)
        player.packetSender.sendString(1089, twob + "2 Bars" + twob)
        player.packetSender.sendString(8428, twob + "2 Bars" + twob)
        player.packetSender.sendString(1124, oneb + "1 Bar" + oneb)
        player.packetSender.sendString(1125, oneb + "1 Bar" + oneb)
        player.packetSender.sendString(1126, oneb + "1 Bar" + oneb)
        player.packetSender.sendString(1127, oneb + "1 Bar" + oneb)
        player.packetSender.sendString(1128, oneb + "1 Bar" + oneb)
        player.packetSender.sendString(1129, oneb + "1 Bar" + oneb)
        player.packetSender.sendString(1130, oneb + "1 Bar" + oneb)
        player.packetSender.sendString(1131, oneb + "1 Bar" + oneb)
        player.packetSender.sendString(13357, oneb + "1 Bar" + oneb)
        player.packetSender.sendString(11459, oneb + "1 Bar" + oneb)
        player.packetSender.sendString(1101, GetForlvl(33, player) + "Plate Body" + GetForlvl(18, player))
        player.packetSender.sendString(1099, GetForlvl(31, player) + "Plate Legs" + GetForlvl(16, player))
        player.packetSender.sendString(1100, GetForlvl(31, player) + "Plate Skirt" + GetForlvl(16, player))
        sendString(player, GetForlvl(29, player) + "2 Hand Sword" + GetForlvl(14, player), 1088)
        sendString(player, GetForlvl(27, player) + "Kite Shield" + GetForlvl(12, player), 1105)
        sendString(player, GetForlvl(26, player) + "Chain Body" + GetForlvl(11, player), 1098)
        sendString(player, GetForlvl(26, player) + "Oil Lantern Frame" + GetForlvl(11, player), 11461)
        sendString(player, GetForlvl(25, player) + "Battle Axe" + GetForlvl(10, player), 1092)
        sendString(player, GetForlvl(24, player) + "Warhammer" + GetForlvl(9, player), 1083)
        sendString(player, GetForlvl(23, player) + "Square Shield" + GetForlvl(8, player), 1104)
        sendString(player, GetForlvl(22, player) + "Full Helm" + GetForlvl(7, player), 1103)
        sendString(player, GetForlvl(21, player) + "Throwing Knives" + GetForlvl(7, player), 1106)
        sendString(player, GetForlvl(21, player) + "Long Sword" + GetForlvl(6, player), 1086)
        sendString(player, GetForlvl(20, player) + "Scimitar" + GetForlvl(5, player), 1087)
        sendString(player, GetForlvl(20, player) + "Arrowtips" + GetForlvl(5, player), 1108)
        sendString(player, GetForlvl(19, player) + "Sword" + GetForlvl(4, player), 1085)
        sendString(player, GetForlvl(19, player) + "Bolts" + GetForlvl(4, player), 9140)
        sendString(player, GetForlvl(19, player) + "Nails" + GetForlvl(4, player), 13358)
        sendString(player, GetForlvl(18, player) + "Medium Helm" + GetForlvl(3, player), 1102)
        sendString(player, GetForlvl(17, player) + "Mace" + GetForlvl(2, player), 1093)
        sendString(player, GetForlvl(15, player) + "Dagger" + GetForlvl(1, player), 1094)
        sendString(player, GetForlvl(16, player) + "Axe" + GetForlvl(1, player), 1091)
        player.packetSender.sendSmithingData(1203, 0, 1119, 1)
        player.packetSender.sendSmithingData(1349, 0, 1120, 1)
        player.packetSender.sendSmithingData(1101, 0, 1121, 1)
        player.packetSender.sendSmithingData(1137, 0, 1122, 1)
        player.packetSender.sendSmithingData(9140, 0, 1123, 10)
        player.packetSender.sendSmithingData(1279, 1, 1119, 1)
        player.packetSender.sendSmithingData(1420, 1, 1120, 1)
        player.packetSender.sendSmithingData(1067, 1, 1121, 1)
        player.packetSender.sendSmithingData(1153, 1, 1122, 1)
        player.packetSender.sendSmithingData(40, 1, 1123, 15)
        player.packetSender.sendSmithingData(1323, 2, 1119, 1)
        player.packetSender.sendSmithingData(1335, 2, 1120, 1)
        player.packetSender.sendSmithingData(1081, 2, 1121, 1)
        player.packetSender.sendSmithingData(1175, 2, 1122, 1)
        player.packetSender.sendSmithingData(863, 2, 1123, 5)
        player.packetSender.sendSmithingData(1293, 3, 1119, 1)
        player.packetSender.sendSmithingData(1363, 3, 1120, 1)
        player.packetSender.sendSmithingData(1115, 3, 1121, 1)
        player.packetSender.sendSmithingData(1191, 3, 1122, 1)
        player.packetSender.sendSmithingData(1309, 4, 1119, 1)
        player.packetSender.sendSmithingData(4820, 4, 1122, 15)
        player.packetSender.sendSmithingData(4540, 4, 1121, 1)
        player.packetSender.sendSmithingData(3096, 4, 1120, 1)
        player.packetSender.sendSmithingData(-1, 3, 1123, 1)
        sendString(player, "", 1135)
        sendString(player, "", 1134)
        sendString(player, "", 1132)
        sendString(player, "", 1096)
        player.packetSender.sendString(1135, twob + "2 Bars" + twob) //CROSSBOW
        player.packetSender.sendString(11459, oneb + "1 Bar" + oneb) //PICKAXE
        player.packetSender.sendSmithingData(9177, 4, 1123, 1) //CROSSBOW
        player.packetSender.sendSmithingData(1267, 4, 1121, 1) //PICKAXE
        sendString(player, GetForlvl(23, player) + "Crossbow" + GetForlvl(23, player), 1134) //CROSSBOW
        sendString(player, GetForlvl(24, player) + "Pickaxe" + GetForlvl(24, player), 11461) //PICKAXE
        player.packetSender.sendInterface(994)
    }

    fun makeSteelInterface(player: Player) {
        val fiveb = GetForBars(2353, 5, player)
        val threeb = GetForBars(2353, 3, player)
        val twob = GetForBars(2353, 2, player)
        val oneb = GetForBars(2353, 1, player)
        sendString(player, fiveb + "5 Bars" + fiveb, 1112)
        sendString(player, threeb + "3 Bars" + threeb, 1109)
        sendString(player, threeb + "3 Bars" + threeb, 1110)
        sendString(player, threeb + "3 Bars" + threeb, 1118)
        sendString(player, threeb + "3 Bars" + threeb, 1111)
        sendString(player, threeb + "3 Bars" + threeb, 1095)
        sendString(player, threeb + "3 Bars" + threeb, 1115)
        sendString(player, threeb + "3 Bars" + threeb, 1090)
        sendString(player, twob + "2 Bars" + twob, 1113)
        sendString(player, twob + "2 Bars" + twob, 1116)
        sendString(player, twob + "2 Bars" + twob, 1114)
        sendString(player, twob + "2 Bars" + twob, 1089)
        sendString(player, twob + "2 Bars" + twob, 8428)
        sendString(player, twob + "2 Bars" + twob, 1135)
        sendString(player, oneb + "1 Bar" + oneb, 11459)
        sendString(player, oneb + "1 Bar" + oneb, 1124)
        sendString(player, oneb + "1 Bar" + oneb, 1125)
        sendString(player, oneb + "1 Bar" + oneb, 1126)
        sendString(player, oneb + "1 Bar" + oneb, 1127)
        sendString(player, oneb + "1 Bar" + oneb, 1128)
        sendString(player, oneb + "1 Bar" + oneb, 1129)
        sendString(player, oneb + "1 Bar" + oneb, 1130)
        sendString(player, oneb + "1 Bar" + oneb, 1131)
        sendString(player, oneb + "1 Bar" + oneb, 13357)
        sendString(player, oneb + "1 Bar" + oneb, 1132)
        sendString(player, GetForlvl(48, player) + "Plate Body" + GetForlvl(18, player), 1101)
        sendString(player, GetForlvl(46, player) + "Plate Legs" + GetForlvl(16, player), 1099)
        sendString(player, GetForlvl(46, player) + "Plate Skirt" + GetForlvl(16, player), 1100)
        sendString(player, GetForlvl(44, player) + "2 Hand Sword" + GetForlvl(14, player), 1088)
        sendString(player, GetForlvl(42, player) + "Kite Shield" + GetForlvl(12, player), 1105)
        sendString(player, GetForlvl(41, player) + "Chain Body" + GetForlvl(11, player), 1098)
        sendString(player, "", 11461)
        sendString(player, GetForlvl(40, player) + "Battle Axe" + GetForlvl(10, player), 1092)
        sendString(player, GetForlvl(39, player) + "Warhammer" + GetForlvl(9, player), 1083)
        sendString(player, GetForlvl(38, player) + "Square Shield" + GetForlvl(8, player), 1104)
        sendString(player, GetForlvl(37, player) + "Full Helm" + GetForlvl(7, player), 1103)
        sendString(player, GetForlvl(37, player) + "Throwing Knives" + GetForlvl(7, player), 1106)
        sendString(player, GetForlvl(36, player) + "Long Sword" + GetForlvl(6, player), 1086)
        sendString(player, GetForlvl(35, player) + "Scimitar" + GetForlvl(5, player), 1087)
        sendString(player, GetForlvl(35, player) + "Arrowtips" + GetForlvl(5, player), 1108)
        sendString(player, GetForlvl(34, player) + "Sword" + GetForlvl(4, player), 1085)
        sendString(player, GetForlvl(34, player) + "Bolts" + GetForlvl(4, player), 9141)
        sendString(player, GetForlvl(34, player) + "Nails" + GetForlvl(4, player), 13358)
        sendString(player, GetForlvl(33, player) + "Medium Helm" + GetForlvl(3, player), 1102)
        sendString(player, GetForlvl(32, player) + "Mace" + GetForlvl(2, player), 1093)
        sendString(player, GetForlvl(30, player) + "Dagger" + GetForlvl(1, player), 1094)
        sendString(player, GetForlvl(31, player) + "Axe" + GetForlvl(1, player), 1091)
        sendString(player, GetForlvl(35, player) + "Cannonball" + GetForlvl(35, player), 1096)
        sendString(player, GetForlvl(36, player) + "Crossbow" + GetForlvl(36, player), 1134) //CROSSBOW
        sendString(player, GetForlvl(37, player) + "Pickaxe" + GetForlvl(37, player), 11461) //PICKAXE
        sendString(player, GetForlvl(43, player) + "Claws" + GetForlvl(43, player), 8429)
        sendString(player, GetForlvl(33, player) + "Bolts" + GetForlvl(33, player), 1107)
        player.packetSender.sendSmithingData(1207, 0, 1119, 1)
        player.packetSender.sendSmithingData(1353, 0, 1120, 1)
        player.packetSender.sendSmithingData(1105, 0, 1121, 1)
        player.packetSender.sendSmithingData(1141, 0, 1122, 1)
        player.packetSender.sendSmithingData(9141, 0, 1123, 10)
        player.packetSender.sendSmithingData(1281, 1, 1119, 1)
        player.packetSender.sendSmithingData(1424, 1, 1120, 1)
        player.packetSender.sendSmithingData(1069, 1, 1121, 1)
        player.packetSender.sendSmithingData(1157, 1, 1122, 1)
        player.packetSender.sendSmithingData(41, 1, 1123, 15)
        player.packetSender.sendSmithingData(1325, 2, 1119, 1)
        player.packetSender.sendSmithingData(1339, 2, 1120, 1)
        player.packetSender.sendSmithingData(1083, 2, 1121, 1)
        player.packetSender.sendSmithingData(1177, 2, 1122, 1)
        player.packetSender.sendSmithingData(865, 2, 1123, 5)
        player.packetSender.sendSmithingData(1295, 3, 1119, 1)
        player.packetSender.sendSmithingData(1365, 3, 1120, 1)
        player.packetSender.sendSmithingData(1119, 3, 1121, 1)
        player.packetSender.sendSmithingData(1193, 3, 1122, 1) //sec lazoh //ok
        player.packetSender.sendSmithingData(1311, 4, 1119, 1)
        player.packetSender.sendSmithingData(1539, 4, 1122, 15)
        player.packetSender.sendSmithingData(2, 3, 1123, 4) //cannonballs
        player.packetSender.sendSmithingData(9179, 4, 1123, 1) //CROSSBOW
        player.packetSender.sendSmithingData(1269, 4, 1121, 1) //PICKAXE
        player.packetSender.sendSmithingData(3097, 4, 1120, 1)
        player.packetSender.sendInterface(994)
    }

    fun makeMithInterface(player: Player) {
        val fiveb = GetForBars(2359, 5, player)
        val threeb = GetForBars(2359, 3, player)
        val twob = GetForBars(2359, 2, player)
        val oneb = GetForBars(2359, 1, player)
        sendString(player, fiveb + "5 Bars" + fiveb, 1112)
        sendString(player, threeb + "3 Bars" + threeb, 1109)
        sendString(player, threeb + "3 Bars" + threeb, 1110)
        sendString(player, threeb + "3 Bars" + threeb, 1118)
        sendString(player, threeb + "3 Bars" + threeb, 1111)
        sendString(player, threeb + "3 Bars" + threeb, 1095)
        sendString(player, threeb + "3 Bars" + threeb, 1115)
        sendString(player, threeb + "3 Bars" + threeb, 1090)
        sendString(player, twob + "2 Bars" + twob, 1113)
        sendString(player, twob + "2 Bars" + twob, 1116)
        sendString(player, twob + "2 Bars" + twob, 1114)
        sendString(player, twob + "2 Bars" + twob, 1089)
        sendString(player, twob + "2 Bars" + twob, 8428)
        sendString(player, oneb + "1 Bar" + oneb, 1124)
        sendString(player, oneb + "1 Bar" + oneb, 1125)
        sendString(player, oneb + "1 Bar" + oneb, 1126)
        sendString(player, oneb + "1 Bar" + oneb, 1127)
        sendString(player, oneb + "1 Bar" + oneb, 1128)
        sendString(player, oneb + "1 Bar" + oneb, 1129)
        sendString(player, oneb + "1 Bar" + oneb, 1130)
        sendString(player, oneb + "1 Bar" + oneb, 1131)
        sendString(player, oneb + "1 Bar" + oneb, 13357)
        sendString(player, oneb + "1 Bar" + oneb, 11459)
        sendString(player, GetForlvl(68, player) + "Plate Body" + GetForlvl(18, player), 1101)
        sendString(player, GetForlvl(66, player) + "Plate Legs" + GetForlvl(16, player), 1099)
        sendString(player, GetForlvl(66, player) + "Plate Skirt" + GetForlvl(16, player), 1100)
        sendString(player, GetForlvl(64, player) + "2 Hand Sword" + GetForlvl(14, player), 1088)
        sendString(player, GetForlvl(62, player) + "Kite Shield" + GetForlvl(12, player), 1105)
        sendString(player, GetForlvl(61, player) + "Chain Body" + GetForlvl(11, player), 1098)
        sendString(player, GetForlvl(60, player) + "Battle Axe" + GetForlvl(10, player), 1092)
        sendString(player, GetForlvl(59, player) + "Warhammer" + GetForlvl(9, player), 1083)
        sendString(player, GetForlvl(58, player) + "Square Shield" + GetForlvl(8, player), 1104)
        sendString(player, GetForlvl(57, player) + "Full Helm" + GetForlvl(7, player), 1103)
        sendString(player, GetForlvl(57, player) + "Throwing Knives" + GetForlvl(7, player), 1106)
        sendString(player, GetForlvl(56, player) + "Long Sword" + GetForlvl(6, player), 1086)
        sendString(player, GetForlvl(55, player) + "Scimitar" + GetForlvl(5, player), 1087)
        sendString(player, GetForlvl(55, player) + "Arrowtips" + GetForlvl(5, player), 1108)
        sendString(player, GetForlvl(54, player) + "Sword" + GetForlvl(4, player), 1085)
        sendString(player, GetForlvl(54, player) + "Bolts" + GetForlvl(4, player), 9142)
        sendString(player, GetForlvl(54, player) + "Nails" + GetForlvl(4, player), 13358)
        sendString(player, GetForlvl(53, player) + "Medium Helm" + GetForlvl(3, player), 1102)
        sendString(player, GetForlvl(52, player) + "Mace" + GetForlvl(2, player), 1093)
        sendString(player, GetForlvl(50, player) + "Dagger" + GetForlvl(1, player), 1094)
        sendString(player, GetForlvl(51, player) + "Axe" + GetForlvl(1, player), 1091)
        player.packetSender.sendSmithingData(1209, 0, 1119, 1) //dagger
        player.packetSender.sendSmithingData(1355, 0, 1120, 1) //axe
        player.packetSender.sendSmithingData(1109, 0, 1121, 1) //chain body
        player.packetSender.sendSmithingData(1143, 0, 1122, 1) //med helm
        player.packetSender.sendSmithingData(9142, 0, 1123, 10) //Bolts
        player.packetSender.sendSmithingData(1285, 1, 1119, 1) //s-sword
        player.packetSender.sendSmithingData(1428, 1, 1120, 1) //mace
        player.packetSender.sendSmithingData(1071, 1, 1121, 1) //platelegs
        player.packetSender.sendSmithingData(1159, 1, 1122, 1) //full helm
        player.packetSender.sendSmithingData(42, 1, 1123, 15) //arrowtips
        player.packetSender.sendSmithingData(1329, 2, 1119, 1) //scimmy
        player.packetSender.sendSmithingData(1343, 2, 1120, 1) //warhammer
        player.packetSender.sendSmithingData(1085, 2, 1121, 1) //plateskirt
        player.packetSender.sendSmithingData(1181, 2, 1122, 1) //Sq. Shield
        player.packetSender.sendSmithingData(866, 2, 1123, 5) //throwing-knives
        player.packetSender.sendSmithingData(1299, 3, 1119, 1) //longsword
        player.packetSender.sendSmithingData(1369, 3, 1120, 1) //battleaxe
        player.packetSender.sendSmithingData(1121, 3, 1121, 1) //platebody
        player.packetSender.sendSmithingData(1197, 3, 1122, 1) //kiteshield
        player.packetSender.sendSmithingData(1315, 4, 1119, 1) //2h sword
        player.packetSender.sendSmithingData(4822, 4, 1122, 15) //nails
        player.packetSender.sendSmithingData(3099, 4, 1120, 1)
        player.packetSender.sendSmithingData(-1, 3, 1123, 1)
        sendString(player, "", 1134)
        sendString(player, "", 11461)
        sendString(player, "", 1132)
        sendString(player, "", 1096)
        player.packetSender.sendString(1135, twob + "2 Bars" + twob) //CROSSBOW
        player.packetSender.sendString(11459, oneb + "1 Bar" + oneb) //PICKAXE
        player.packetSender.sendSmithingData(9181, 4, 1123, 1) //CROSSBOW
        player.packetSender.sendSmithingData(1273, 4, 1121, 1) //PICKAXE
        sendString(player, GetForlvl(56, player) + "Crossbow" + GetForlvl(56, player), 1134) //CROSSBOW
        sendString(player, GetForlvl(57, player) + "Pickaxe" + GetForlvl(57, player), 11461) //PICKAXE
        player.packetSender.sendInterface(994)
    }

    fun makeAddyInterface(player: Player) {
        val fiveb = GetForBars(2361, 5, player)
        val threeb = GetForBars(2361, 3, player)
        val twob = GetForBars(2361, 2, player)
        val oneb = GetForBars(2361, 1, player)
        sendString(player, fiveb + "5 Bars" + fiveb, 1112)
        sendString(player, threeb + "3 Bars" + threeb, 1109)
        sendString(player, threeb + "3 Bars" + threeb, 1110)
        sendString(player, threeb + "3 Bars" + threeb, 1118)
        sendString(player, threeb + "3 Bars" + threeb, 1111)
        sendString(player, threeb + "3 Bars" + threeb, 1095)
        sendString(player, threeb + "3 Bars" + threeb, 1115)
        sendString(player, threeb + "3 Bars" + threeb, 1090)
        sendString(player, twob + "2 Bars" + twob, 1113)
        sendString(player, twob + "2 Bars" + twob, 1116)
        sendString(player, twob + "2 Bars" + twob, 1114)
        sendString(player, twob + "2 Bars" + twob, 1089)
        sendString(player, twob + "2 Bars" + twob, 8428)
        sendString(player, oneb + "1 Bar" + oneb, 1124)
        sendString(player, oneb + "1 Bar" + oneb, 1125)
        sendString(player, oneb + "1 Bar" + oneb, 1126)
        sendString(player, oneb + "1 Bar" + oneb, 1127)
        sendString(player, oneb + "1 Bar" + oneb, 1128)
        sendString(player, oneb + "1 Bar" + oneb, 1129)
        sendString(player, oneb + "1 Bar" + oneb, 1130)
        sendString(player, oneb + "1 Bar" + oneb, 1131)
        sendString(player, oneb + "1 Bar" + oneb, 13357)
        sendString(player, oneb + "1 Bar" + oneb, 11459)
        sendString(player, GetForlvl(88, player) + "Plate Body" + GetForlvl(18, player), 1101)
        sendString(player, GetForlvl(86, player) + "Plate Legs" + GetForlvl(16, player), 1099)
        sendString(player, GetForlvl(86, player) + "Plate Skirt" + GetForlvl(16, player), 1100)
        sendString(player, GetForlvl(84, player) + "2 Hand Sword" + GetForlvl(14, player), 1088)
        sendString(player, GetForlvl(82, player) + "Kite Shield" + GetForlvl(12, player), 1105)
        sendString(player, GetForlvl(81, player) + "Chain Body" + GetForlvl(11, player), 1098)
        sendString(player, GetForlvl(80, player) + "Battle Axe" + GetForlvl(10, player), 1092)
        sendString(player, GetForlvl(79, player) + "Warhammer" + GetForlvl(9, player), 1083)
        sendString(player, GetForlvl(78, player) + "Square Shield" + GetForlvl(8, player), 1104)
        sendString(player, GetForlvl(77, player) + "Full Helm" + GetForlvl(7, player), 1103)
        sendString(player, GetForlvl(77, player) + "Throwing Knives" + GetForlvl(7, player), 1106)
        sendString(player, GetForlvl(76, player) + "Long Sword" + GetForlvl(6, player), 1086)
        sendString(player, GetForlvl(75, player) + "Scimitar" + GetForlvl(5, player), 1087)
        sendString(player, GetForlvl(75, player) + "Arrowtips" + GetForlvl(5, player), 1108)
        sendString(player, GetForlvl(74, player) + "Sword" + GetForlvl(4, player), 1085)
        sendString(player, GetForlvl(74, player) + "Bolts" + GetForlvl(4, player), 9143)
        sendString(player, GetForlvl(74, player) + "Nails" + GetForlvl(4, player), 13358)
        sendString(player, GetForlvl(73, player) + "Medium Helm" + GetForlvl(3, player), 1102)
        sendString(player, GetForlvl(72, player) + "Mace" + GetForlvl(2, player), 1093)
        sendString(player, GetForlvl(70, player) + "Dagger" + GetForlvl(1, player), 1094)
        sendString(player, GetForlvl(71, player) + "Axe" + GetForlvl(1, player), 1091)
        player.packetSender.sendSmithingData(1211, 0, 1119, 1) //dagger
        player.packetSender.sendSmithingData(1357, 0, 1120, 1) //axe
        player.packetSender.sendSmithingData(1111, 0, 1121, 1) //chain body
        player.packetSender.sendSmithingData(1145, 0, 1122, 1) //med helm
        player.packetSender.sendSmithingData(9143, 0, 1123, 10) //Bolts
        player.packetSender.sendSmithingData(1287, 1, 1119, 1) //s-sword
        player.packetSender.sendSmithingData(1430, 1, 1120, 1) //mace
        player.packetSender.sendSmithingData(1073, 1, 1121, 1) //platelegs
        player.packetSender.sendSmithingData(1161, 1, 1122, 1) //full helm
        player.packetSender.sendSmithingData(43, 1, 1123, 15) //arrowtips
        player.packetSender.sendSmithingData(1331, 2, 1119, 1) //scimmy
        player.packetSender.sendSmithingData(1345, 2, 1120, 1) //warhammer
        player.packetSender.sendSmithingData(1091, 2, 1121, 1) //plateskirt
        player.packetSender.sendSmithingData(1183, 2, 1122, 1) //Sq. Shield
        player.packetSender.sendSmithingData(867, 2, 1123, 5) //throwing-knives
        player.packetSender.sendSmithingData(1301, 3, 1119, 1) //longsword
        player.packetSender.sendSmithingData(1371, 3, 1120, 1) //battleaxe
        player.packetSender.sendSmithingData(1123, 3, 1121, 1) //platebody
        player.packetSender.sendSmithingData(1199, 3, 1122, 1) //kiteshield
        player.packetSender.sendSmithingData(1317, 4, 1119, 1) //2h sword
        player.packetSender.sendSmithingData(4823, 4, 1122, 15) //nails
        player.packetSender.sendSmithingData(3100, 4, 1120, 1) // claws
        player.packetSender.sendSmithingData(-1, 3, 1123, 1)
        sendString(player, "", 1134)
        sendString(player, "", 11461)
        sendString(player, "", 1132)
        sendString(player, "", 1096)
        player.packetSender.sendString(1135, twob + "2 Bars" + twob) //CROSSBOW
        player.packetSender.sendString(11459, oneb + "1 Bar" + oneb) //PICKAXE
        player.packetSender.sendSmithingData(9183, 4, 1123, 1) //CROSSBOW
        player.packetSender.sendSmithingData(1271, 4, 1121, 1) //PICKAXE
        sendString(player, GetForlvl(76, player) + "Crossbow" + GetForlvl(76, player), 1134) //CROSSBOW
        sendString(player, GetForlvl(77, player) + "Pickaxe" + GetForlvl(77, player), 11461) //PICKAXE
        player.packetSender.sendInterface(994)
    }

    fun makeRuneInterface(player: Player) {
        val fiveb = GetForBars(2363, 5, player)
        val threeb = GetForBars(2363, 3, player)
        val twob = GetForBars(2363, 2, player)
        val oneb = GetForBars(2363, 1, player)
        sendString(player, fiveb + "5 Bars" + fiveb, 1112)
        sendString(player, threeb + "3 Bars" + threeb, 1109)
        sendString(player, threeb + "3 Bars" + threeb, 1110)
        sendString(player, threeb + "3 Bars" + threeb, 1118)
        sendString(player, threeb + "3 Bars" + threeb, 1111)
        sendString(player, threeb + "3 Bars" + threeb, 1095)
        sendString(player, threeb + "3 Bars" + threeb, 1115)
        sendString(player, threeb + "3 Bars" + threeb, 1090)
        sendString(player, twob + "2 Bars" + twob, 1113)
        sendString(player, twob + "2 Bars" + twob, 1116)
        sendString(player, twob + "2 Bars" + twob, 1114)
        sendString(player, twob + "2 Bars" + twob, 1089)
        sendString(player, twob + "2 Bars" + twob, 8428)
        sendString(player, oneb + "1 Bar" + oneb, 1124)
        sendString(player, oneb + "1 Bar" + oneb, 1125)
        sendString(player, oneb + "1 Bar" + oneb, 1126)
        sendString(player, oneb + "1 Bar" + oneb, 1127)
        sendString(player, oneb + "1 Bar" + oneb, 1128)
        sendString(player, oneb + "1 Bar" + oneb, 1129)
        sendString(player, oneb + "1 Bar" + oneb, 1130)
        sendString(player, oneb + "1 Bar" + oneb, 1131)
        sendString(player, oneb + "1 Bar" + oneb, 13357)
        sendString(player, oneb + "1 Bar" + oneb, 11459)
        sendString(player, GetForlvl(88, player) + "Plate Body" + GetForlvl(18, player), 1101)
        sendString(player, GetForlvl(99, player) + "Plate Legs" + GetForlvl(16, player), 1099)
        sendString(player, GetForlvl(99, player) + "Plate Skirt" + GetForlvl(16, player), 1100)
        sendString(player, GetForlvl(99, player) + "2 Hand Sword" + GetForlvl(14, player), 1088)
        sendString(player, GetForlvl(97, player) + "Kite Shield" + GetForlvl(12, player), 1105)
        sendString(player, GetForlvl(96, player) + "Chain Body" + GetForlvl(11, player), 1098)
        sendString(player, GetForlvl(95, player) + "Battle Axe" + GetForlvl(10, player), 1092)
        sendString(player, GetForlvl(94, player) + "Warhammer" + GetForlvl(9, player), 1083)
        sendString(player, GetForlvl(93, player) + "Square Shield" + GetForlvl(8, player), 1104)
        sendString(player, GetForlvl(92, player) + "Full Helm" + GetForlvl(7, player), 1103)
        sendString(player, GetForlvl(92, player) + "Throwing Knives" + GetForlvl(7, player), 1106)
        sendString(player, GetForlvl(91, player) + "Long Sword" + GetForlvl(6, player), 1086)
        sendString(player, GetForlvl(90, player) + "Scimitar" + GetForlvl(5, player), 1087)
        sendString(player, GetForlvl(90, player) + "Arrowtips" + GetForlvl(5, player), 1108)
        sendString(player, GetForlvl(89, player) + "Sword" + GetForlvl(4, player), 1085)
        sendString(player, GetForlvl(89, player) + "Bolts" + GetForlvl(4, player), 9144)
        sendString(player, GetForlvl(89, player) + "Nails" + GetForlvl(4, player), 13358)
        sendString(player, GetForlvl(88, player) + "Medium Helm" + GetForlvl(3, player), 1102)
        sendString(player, GetForlvl(87, player) + "Mace" + GetForlvl(2, player), 1093)
        sendString(player, GetForlvl(85, player) + "Dagger" + GetForlvl(1, player), 1094)
        sendString(player, GetForlvl(86, player) + "Axe" + GetForlvl(1, player), 1091)
        player.packetSender.sendSmithingData(1213, 0, 1119, 1) //dagger
        player.packetSender.sendSmithingData(1359, 0, 1120, 1) //axe
        player.packetSender.sendSmithingData(1113, 0, 1121, 1) //chain body
        player.packetSender.sendSmithingData(1147, 0, 1122, 1) //med helm
        player.packetSender.sendSmithingData(9144, 0, 1123, 10) //Bolts
        player.packetSender.sendSmithingData(1289, 1, 1119, 1) //s-sword
        player.packetSender.sendSmithingData(1432, 1, 1120, 1) //mace
        player.packetSender.sendSmithingData(1079, 1, 1121, 1) //platelegs
        player.packetSender.sendSmithingData(1163, 1, 1122, 1) //full helm
        player.packetSender.sendSmithingData(44, 1, 1123, 15) //arrowtips
        player.packetSender.sendSmithingData(1333, 2, 1119, 1) //scimmy
        player.packetSender.sendSmithingData(1347, 2, 1120, 1) //warhammer
        player.packetSender.sendSmithingData(1093, 2, 1121, 1) //plateskirt
        player.packetSender.sendSmithingData(1185, 2, 1122, 1) //Sq. Shield
        player.packetSender.sendSmithingData(868, 2, 1123, 5) //throwing-knives
        player.packetSender.sendSmithingData(1303, 3, 1119, 1) //longsword
        player.packetSender.sendSmithingData(1373, 3, 1120, 1) //battleaxe
        player.packetSender.sendSmithingData(1127, 3, 1121, 1) //platebody
        player.packetSender.sendSmithingData(1201, 3, 1122, 1) //kiteshield
        player.packetSender.sendSmithingData(1319, 4, 1119, 1) //2h sword
        player.packetSender.sendSmithingData(4824, 4, 1122, 15) //nails
        player.packetSender.sendSmithingData(-1, 3, 1123, 1)
        player.packetSender.sendSmithingData(3101, 4, 1120, 1) // claws
        sendString(player, "", 1134)
        sendString(player, "", 11461)
        sendString(player, "", 1132)
        sendString(player, "", 1096)
        player.packetSender.sendString(1135, twob + "2 Bars" + twob) //CROSSBOW
        player.packetSender.sendString(11459, oneb + "1 Bar" + oneb) //PICKAXE
        player.packetSender.sendSmithingData(9185, 4, 1123, 1) //CROSSBOW
        player.packetSender.sendSmithingData(1275, 4, 1121, 1) //PICKAXE
        sendString(player, GetForlvl(91, player) + "Crossbow" + GetForlvl(91, player), 1134) //CROSSBOW
        sendString(player, GetForlvl(92, player) + "Pickaxe" + GetForlvl(92, player), 11461) //PICKAXE
        player.packetSender.sendInterface(994)
    }

    fun sendString(player: Player, s: String?, i: Int) {
        player.packetSender.sendString(i, s)
    }

    private fun GetForlvl(i: Int, player: Player): String {
        return if (player.skillManager.getMaxLevel(Skill.SMITHING) >= i) "@whi@" else "@bla@"
    }

    private fun GetForBars(i: Int, j: Int, player: Player): String {
        return if (player.inventory.getAmount(i) >= j) "@gre@" else "@red@"
    }

    @JvmStatic
    fun getItemAmount(item: Item): Int {
        val name = item.definition.name.lowercase(Locale.getDefault())
        if (name.contains("cannon")) {
            return 4
        } else if (name.contains("knife")) {
            return 5
        } else if (name.contains("arrowtips") || name.contains("nails")) {
            return 15
        } else if (name.contains("dart tip") || name.contains("bolts")) {
            return 10
        }
        return 1
    }

    @JvmStatic
    fun getBarAmount(item: Item): Int {
        val name = item.definition.name.lowercase(Locale.getDefault())
        if (name.contains("scimitar") || name.contains("claws") || name.contains("crossbow") || name.contains("longsword") || name.contains(
                "sq shield"
            ) || name.contains("full helm")
        ) {
            return 2
        } else if (name.contains("2h sword") || name.contains("warhammer") || name.contains("battleaxe") || name.contains(
                "chainbody"
            ) || name.contains("platelegs") || name.contains("plateskirt") || name.contains("kiteshield")
        ) {
            return 3
        } else if (name.contains("platebody")) {
            return 5
        }
        return 1
    }

    fun getData(item: Item, type: String?): Int {
        var reqLvl = 1
        when (item.id) {
            1205 ->            //xp =13;
                reqLvl = 1
            1351 ->            //xp = 13;
                reqLvl = 1
            1422 ->            //xp = 13;
                reqLvl = 2
            1139 ->            //xp = 13;
                reqLvl = 3
            9375 ->            //xp = 13;
                reqLvl = 3
            1277 ->            //xp = 13;
                reqLvl = 4
            4819 ->            //xp =13;
                reqLvl = 4
            1794 ->            //xp = 13;
                reqLvl = 4
            819 ->            //xp = 13;
                reqLvl = 4
            39 ->            //xp =13;
                reqLvl = 5
            1321 ->            //xp = 26;
                reqLvl = 5
            1291 ->            //xp = 26;
                reqLvl = 6
            9420 ->            //xp = 13;
                reqLvl = 6
            1155 ->            //xp = 13;
                reqLvl = 7
            864 ->            //xp = 13;
                reqLvl = 7
            1173 ->            //xp =  26;
                reqLvl = 8
            1337 ->            //xp =  39;
                reqLvl = 9
            1375 ->            //xp =  39;
                reqLvl = 10
            1103 ->            //xp = 39;
                reqLvl = 11
            1189 ->            //xp = 39;
                reqLvl = 12
            3095 ->            //xp = 26;
                reqLvl = 13
            1307 ->            //xp =  39;
                reqLvl = 14
            1087 ->            //xp = 39;
                reqLvl = 16
            1075 ->            //xp =  39;
                reqLvl = 16
            1117 ->            //xp = 65;
                reqLvl = 18
            1203 ->            //xp = 25;
                reqLvl = 15
            15298 ->            //xp =25;
                reqLvl = 16
            1420 ->            //xp =25;
                reqLvl = 17
            7225 ->            //xp =25;
                reqLvl = 17
            1137 ->            //xp =25;
                reqLvl = 18
            9140 ->            //xp =25;
                reqLvl = 18
            1279 ->            //xp =25;
                reqLvl = 19
            4820 ->            //xp =25;
                reqLvl = 19
            820 ->            //xp =25;
                reqLvl = 20
            40 ->            //xp = 25;
                reqLvl = 20
            1323 ->            //xp = 50;
                reqLvl = 20
            1293 ->            //xp =50;
                reqLvl = 21
            1153 ->            //xp =50;
                reqLvl = 22
            863 ->            //xp =25;
                reqLvl = 22
            1175 ->            //xp =50;
                reqLvl = 23
            9423 ->            //xp =25;
                reqLvl = 23
            1335 ->            //xp =75;
                reqLvl = 24
            1363 ->            //xp =75;
                reqLvl = 25
            1101 ->            //xp =75;
                reqLvl = 26
            4540 ->            //xp =25;
                reqLvl = 26
            1191 ->            //xp =75;
                reqLvl = 27
            3096 ->            //xp =50;
                reqLvl = 28
            1309 ->            //xp =75;
                reqLvl = 29
            1081 ->            //xp =1000;
                reqLvl = 31
            1067 ->            //xp =1000;
                reqLvl = 31
            1115 ->            //xp = 1200;
                reqLvl = 33
            1207 ->            //xp = 900;
                reqLvl = 30
            1353 ->            //xp = 920;
                reqLvl = 31
            1424 ->            //xp = 970;
                reqLvl = 32
            1141 ->            //xp = 1005;
                reqLvl = 33
            9141 ->            //xp = 1050;
                reqLvl = 33
            1539 ->            //xp = 1050;
                reqLvl = 34
            1281 ->            //xp = 1050;
                reqLvl = 34
            821 ->            //xp = 1050;
                reqLvl = 34
            41 ->            //xp = 1080;
                reqLvl = 35
            1325 ->            //xp = 1110;
                reqLvl = 35
            9174 -> reqLvl = 6
            1265 -> reqLvl = 7 //bronze pick
            9177 -> reqLvl = 23
            1267 -> reqLvl = 24 //Iron pick
            9181 -> reqLvl = 56
            1273 -> reqLvl = 57 //Mith pick
            9183 -> reqLvl = 76
            1271 -> reqLvl = 77 //Addy pick
            9185 -> reqLvl = 91
            1275 -> reqLvl = 92 //Rune pick
            1269 ->            //xp = 1110;
                reqLvl = 37
            1295 ->            //xp = 1130;
                reqLvl = 36
            2370 ->            //xp = 1130;
                reqLvl = 36
            9179 ->            //xp = 1130;
                reqLvl = 36
            1157 ->            //xp = 1160;
                reqLvl = 37
            865 ->            //xp = 1160;
                reqLvl = 37
            1177 ->            //xp = 1190;
                reqLvl = 38
            1339 ->            //xp = 1200;
                reqLvl = 39
            1365 ->            //xp = 1215;
                reqLvl = 40
            1105 ->            //xp = 1230;
                reqLvl = 41
            1193 ->            //xp = 1240;
                reqLvl = 42
            2 ->            //xp = 955;
                reqLvl = 35
            3097 ->            //xp = 1250;
                reqLvl = 43
            1311 ->            //xp = 1250;
                reqLvl = 44
            1084 ->            //xp = 1282;
                reqLvl = 46
            1069 ->            //xp = 1282;
                reqLvl = 46
            1119 ->            //xp = 1600;
                reqLvl = 48
            1209 ->            //xp = 1870;
                reqLvl = 50
            1355 ->            //xp = 1870;
                reqLvl = 51
            1428 ->            //xp = 1870;
                reqLvl = 52
            1143 ->            //xp = 1870;
                reqLvl = 53
            9142 ->            //xp = 1870;
                reqLvl = 53
            1285 ->            //xp = 1870;
                reqLvl = 54
            4822 ->            //xp = 1870;
                reqLvl = 54
            822 ->            //xp = 1770;
                reqLvl = 54
            42 ->            //xp = 1770;
                reqLvl = 55
            1329 ->            //xp = 1900;
                reqLvl = 55
            1299 ->            //xp = 2000;
                reqLvl = 56
            9427 ->            //xp = 1700;
                reqLvl = 56
            1159 ->            //xp = 2110;
                reqLvl = 57
            866 ->            //xp = 1600;
                reqLvl = 57
            1181 ->            //xp = 2110;
                reqLvl = 58
            1343 ->            //xp = 2200;
                reqLvl = 59
            9416 ->            //xp = 1450;
                reqLvl = 59
            1369 ->            //xp = 2200;
                reqLvl = 60
            1109 ->            //xp = 2200;
                reqLvl = 61
            1197 ->            //xp = 2200;
                reqLvl = 62
            3099 ->            //xp = 1800;
                reqLvl = 63
            1315 ->            //xp = 2200;
                reqLvl = 64
            1085 ->            //xp = 2200;
                reqLvl = 66
            1071 ->            //xp = 2200;
                reqLvl = 66
            1121 ->            //xp = 3000;
                reqLvl = 68
            1211 ->            //xp = 2900;
                reqLvl = 70
            1357 ->            //xp = 2957;
                reqLvl = 71
            1430 ->            //xp = 2968;
                reqLvl = 72
            1145 ->            //xp = 2980;
                reqLvl = 73
            9143 ->            //xp = 3000;
                reqLvl = 73
            1287 ->            //xp = 3100;
                reqLvl = 74
            4823 ->            //xp = 2870;
                reqLvl = 74
            823 ->            //xp = 2870;
                reqLvl = 74
            43 ->            //xp = 2870;
                reqLvl = 75
            1331 ->            //xp = 3100;
                reqLvl = 75
            1301 ->            //xp = 3100;
                reqLvl = 76
            9429 ->            //xp = 2870;
                reqLvl = 76
            1161 ->            //xp = 3100;
                reqLvl = 77
            867 ->            //xp = 2870;
                reqLvl = 77
            1183 ->            //xp = 3200;
                reqLvl = 78
            1345 ->            //xp = 3200;
                reqLvl = 79
            1371 ->            //xp = 3250;
                reqLvl = 80
            1111 ->            //xp = 3250;
                reqLvl = 81
            1199 ->            //xp = 3300;
                reqLvl = 82
            3100 ->            //xp = 3100;
                reqLvl = 83
            1317 ->            //xp = 3350;
                reqLvl = 84
            1091 ->            //xp = 3360;
                reqLvl = 86
            1073 ->            //xp = 3370;
                reqLvl = 86
            1123 ->            //xp = 3600;
                reqLvl = 88
            1213 ->            //xp = 5870;
                reqLvl = 51
            1359 ->            //xp = 6000;
                reqLvl = 85
            1432 ->            //xp = 6000;
                reqLvl = 86
            1147 ->            //xp = 6000;
                reqLvl = 87
            9144 ->            //xp = 6000;
                reqLvl = 88
            1289 ->            //xp = 6400;
                reqLvl = 89
            4824 ->            //xp = 5270;
                reqLvl = 89
            824 ->            //xp = 6800;
                reqLvl = 90
            44 ->            //xp = 2300;
                reqLvl = 90
            1333 ->            //xp = 7220;
                reqLvl = 90
            1303 ->            //xp = 7220;
                reqLvl = 91
            9431 ->            //xp = 7270;
                reqLvl = 91
            1163 ->            //xp = 7800;
                reqLvl = 92
            868 ->            //xp = 5900;
                reqLvl = 92
            1185 ->            //xp = 7400;
                reqLvl = 93
            1347 ->            //xp = 7400;
                reqLvl = 94
            1373 ->            //xp = 7800;
                reqLvl = 95
            1113 ->            //xp = 7800;
                reqLvl = 96
            1201 ->            //xp = 7800;
                reqLvl = 97
            3101 ->            //xp = 8200;
                reqLvl = 98
            1319 ->            //xp = 8600;
                reqLvl = 99
            1093 ->            //xp = 8600;
                reqLvl = 99
            1079 ->            //xp = 8600;
                reqLvl = 99
            1127 ->            //xp = 12000;
                reqLvl = 99
        }
        return reqLvl
    }

    fun ironOreSuccess(player: Player): Boolean {
        return Misc.getRandom((1 + player.skillManager.getCurrentLevel(Skill.SMITHING) / 0.5).toInt()) > 5
    }
}