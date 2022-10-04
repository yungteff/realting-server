package com.realting.world.content.skill.smithing

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.Animation
import com.realting.model.Skill
import java.util.Locale
import com.realting.world.content.PlayerLogs
import com.realting.world.World
import com.realting.model.Graphic
import com.realting.model.Item
import com.realting.model.definitions.ItemDefinition
import com.realting.model.entity.character.player.Player
import com.realting.world.content.Sounds
import com.realting.world.content.skill.smithing.BarData.Bars

object EquipmentMaking {
    var possibleItems = intArrayOf(
        1205,
        1351,
        1103,
        1139,
        819,
        1277,
        1422,
        1075,
        1155,
        39,
        1321,
        1337,
        1087,
        1173,
        864,
        1291,
        1375,
        1117,
        1189,
        1307,
        3095,
        4819,
        1203,
        1349,
        1101,
        1137,
        9140,
        1279,
        1420,
        1067,
        1153,
        40,
        1323,
        1335,
        1081,
        1175,
        863,
        1293,
        1363,
        1115,
        1191,
        1309,
        3096,
        4540,
        4820,
        1207,
        1353,
        1105,
        1141,
        9141,
        1281,
        1424,
        1069,
        1157,
        41,
        1325,
        1339,
        1083,
        1177,
        865,
        1295,
        1365,
        1119,
        1193,
        2,
        1311,
        3097,
        1539,
        2370,
        1209,
        1355,
        1109,
        1143,
        9142,
        1285,
        1428,
        1071,
        1159,
        42,
        1329,
        1343,
        1085,
        1181,
        866,
        1299,
        1369,
        1121,
        1197,
        1315,
        3099,
        4822,
        1211,
        1357,
        1111,
        1145,
        9143,
        1287,
        1430,
        1073,
        1161,
        43,
        1331,
        1345,
        1091,
        1183,
        867,
        1301,
        1371,
        1123,
        1199,
        1317,
        3100,
        4823,
        1213,
        1359,
        1113,
        1147,
        9144,
        1289,
        1432,
        1079,
        1163,
        44,
        1333,
        1347,
        1093,
        1185,
        868,
        1303,
        1373,
        1127,
        1201,
        1319,
        3101,
        4824,
        9174,
        9177,
        9179,
        9181,
        9183,
        9185,  //CROSSBOWS
        1265,
        1267,
        1269,
        1273,
        1271,
        1275 //PICKAXES
    )

    @JvmStatic
    fun handleAnvil(player: Player) {
        val bar = searchForBars(player)
        if (bar == null) {
            player.packetSender.sendMessage("You do not have any bars in your inventory to smith.")
            return
        } else {
            when (bar.lowercase(Locale.getDefault())) {
                "rune bar" -> {
                    player.selectedSkillingItem = 2363
                    SmithingData.makeRuneInterface(player)
                }
                "adamant bar" -> {
                    player.selectedSkillingItem = 2361
                    SmithingData.makeAddyInterface(player)
                }
                "mithril bar" -> {
                    player.selectedSkillingItem = 2359
                    SmithingData.makeMithInterface(player)
                }
                "steel bar" -> {
                    player.selectedSkillingItem = 2353
                    SmithingData.makeSteelInterface(player)
                }
                "iron bar" -> {
                    player.selectedSkillingItem = 2351
                    SmithingData.makeIronInterface(player)
                }
                "bronze bar" -> {
                    player.selectedSkillingItem = 2349
                    SmithingData.showBronzeInterface(player)
                }
                "gold bar" ->            //case "silver bar":
                    player.packetSender.sendMessage("Gold bars should be crafted at a furnace.")
                "silver bar" -> player.packetSender.sendMessage("Hmm... Silver isn't the best choice for the anvil.")
            }
        }
    }

    fun searchForBars(player: Player): String? {
        for (bar in SmithingData.BARS_SMITH_ORDER) {
            if (player.inventory.contains(bar)) {
                return ItemDefinition.forId(bar).name
            }
        }
        return null
    }

    @JvmStatic
    fun smithItem(player: Player, bar: Item, itemToSmith: Item, x: Int) {
        var canMakeItem = false
        if (bar.id < 0) return
        if (!player.clickDelay.elapsed(1100)) {
            return
        }
        player.skillManager.stopSkilling()
        if (!player.inventory.contains(2347)) {
            player.packetSender.sendMessage("You need a Hammer to smith items.")
            player.packetSender.sendInterfaceRemoval()
            return
        }
        if (player.inventory.getAmount(bar.id) < bar.amount || x <= 0) {
            player.packetSender.sendMessage("You do not have enough bars to smith this item.")
            return
        }
        if (SmithingData.getData(itemToSmith, "reqLvl") > player.skillManager.getCurrentLevel(Skill.SMITHING)) {
            player.packetSender.sendMessage(
                "You need a Smithing level of at least " + SmithingData.getData(
                    itemToSmith, "reqLvl"
                ) + " to make this item."
            )
            return
        }
        val currentItemId = itemToSmith.id
        for (i in possibleItems.indices) {
            if (possibleItems[i] == currentItemId) {
                canMakeItem = true
                break
            }
        }
        var good2go = false
        for (i in Bars.values().indices) {
            /*if (bar.getId() == Bars.values()[i].getItemId()) {
				System.out.println("correct barid");
			}
			if (itemToSmith.getDefinition().getName().startsWith(ItemDefinition.forId(Bars.values()[i].getItemId()).getName().substring(0, 3))) {
				System.out.println("bar 0,3 matches");
			}
			System.out.println(ItemDefinition.forId(Bars.values()[i].getItemId()).getName().substring(0, 3)+" ||| "+itemToSmith.getDefinition().getName());
			if (itemToSmith.getDefinition().getName().startsWith("Cannon")) {
				System.out.println("cannon matches");
			}*/
            if (bar.id == Bars.values()[i].itemId && (itemToSmith.definition.name.startsWith(
                    ItemDefinition.forId(Bars.values()[i].itemId).name.substring(
                        0, 3
                    )
                ) || itemToSmith.definition.name.equals(
                    "cannonball", ignoreCase = true
                ) || itemToSmith.definition.name.equals("Oil lantern frame", ignoreCase = true))
            ) {
                good2go = true
                break
            }
        }
        if (!good2go || !canMakeItem) {
            PlayerLogs.log(
                "1 - smithing abuse",
                player.username + " just tried to smith item: " + currentItemId + " (" + ItemDefinition.forId(
                    currentItemId
                ).name + "), IP: " + player.hostAddress + " using bar " + bar.definition.name
            )
            World.sendStaffMessage(
                "<col=b40404>[BUG ABUSE] " + player.username + " just tried to smith: " + ItemDefinition.forId(
                    currentItemId
                ).name + " using bar " + bar.definition.name
            )
            //player.getPacketSender().sendMessage("How am I going to smith "+Misc.anOrA(ItemDefinition.forId(currentItemId).getName())+" "+ItemDefinition.forId(currentItemId).getName()+"?");
            player.skillManager.stopSkilling()
            return
        }
        player.clickDelay.reset()
        player.packetSender.sendInterfaceRemoval()
        player.currentTask = object : Task(3, player, true) {
            var amountMade = 0
            public override fun execute() {
                if (player.inventory.getAmount(bar.id) < bar.amount || !player.inventory.contains(2347) || amountMade >= x) {
                    stop()
                    return
                }
                if (player.interactingObject != null) player.interactingObject.performGraphic(Graphic(2123))
                player.performAnimation(Animation(898))
                amountMade++
                Sounds.sendSound(player, Sounds.Sound.SMITH_ITEM)
                player.inventory.delete(bar)
                player.inventory.add(itemToSmith)
                player.inventory.refreshItems()
                if (ItemDefinition.forId(itemToSmith.id).name.contains("Bronze")) {
                    player.skillManager.addExperience(Skill.SMITHING, (Bars.Bronze.exp * bar.amount))
                    //player.getPacketSender().sendMessage("Using: "+bar.getAmount()+" Bronze bars to make: "+ItemDefinition.forId(itemToSmith.getId()).getName());
                } else if (ItemDefinition.forId(itemToSmith.id).name.contains("Iron")) {
                    player.skillManager.addExperience(Skill.SMITHING, (Bars.Iron.exp * bar.amount))
                    //player.getPacketSender().sendMessage("Using: "+bar.getAmount()+" Iron bars to make: "+ItemDefinition.forId(itemToSmith.getId()).getName());
                } else if (ItemDefinition.forId(itemToSmith.id).name.contains("Steel") || ItemDefinition.forId(
                        itemToSmith.id
                    ).name.equals("Cannonball", ignoreCase = true)
                ) {
                    player.skillManager.addExperience(Skill.SMITHING, (Bars.Steel.exp * bar.amount))
                    //player.getPacketSender().sendMessage("Using: "+bar.getAmount()+" Steel bars to make: "+ItemDefinition.forId(itemToSmith.getId()).getName());
                } else if (ItemDefinition.forId(itemToSmith.id).name.contains("Mith")) {
                    player.skillManager.addExperience(Skill.SMITHING, (Bars.Mithril.exp * bar.amount))
                    //player.getPacketSender().sendMessage("Using: "+bar.getAmount()+" Mith bars to make: "+ItemDefinition.forId(itemToSmith.getId()).getName());
                } else if (ItemDefinition.forId(itemToSmith.id).name.contains("Adamant")) {
                    player.skillManager.addExperience(Skill.SMITHING, (Bars.Adamant.exp * bar.amount))
                    //player.getPacketSender().sendMessage("Using: "+bar.getAmount()+" Adamant bars to make: "+ItemDefinition.forId(itemToSmith.getId()).getName());
                } else if (ItemDefinition.forId(itemToSmith.id).name.contains("Rune") || ItemDefinition.forId(
                        itemToSmith.id
                    ).name.contains("Runite")
                ) {
                    player.skillManager.addExperience(Skill.SMITHING, (Bars.Rune.exp * bar.amount))
                    //player.getPacketSender().sendMessage("Using: "+bar.getAmount()+" Rune bars to make: "+ItemDefinition.forId(itemToSmith.getId()).getName());
                } else {
                    player.packetSender.sendMessage("ERROR 95152, no experience added. Please report this to staff!")
                }
                //player.getSkillManager().addExperience(Skill.SMITHING, (int) (Bars.Bronze.getExp() * bar.getAmount()));
                //player.getSkillManager().addExperience(Skill.SMITHING, (int) (SmithingData.getData(itemToSmith, "xp")));
            }
        }
        TaskManager.submit(player.currentTask)
    }
}