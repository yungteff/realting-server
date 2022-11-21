package com.realting.world.content.cluescrolls

import com.realting.model.Item
import com.realting.model.Position
import com.realting.model.definitions.ItemDefinition
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import com.realting.world.World
import com.realting.world.content.PlayerLogs

enum class ClueScroll  //private static int easyCasket = 3511;
//private static int mediumCasket = 2802;
//private static int eliteCasket = 19039;
    (val clueId: Int, val digTile: Position?, val npcId: Int, val hint: String) {
    HOME_TREASURE(2722, Position(3658, 2993), -1, "Dig near a Treasure Chest at home."), CHILL_GATES(
        2723, Position(2855, 3810), -1, "Dig infront of the Frozen gates."
    ),
    TAVERLY_DUNG_PIPE(
        2725, Position(2886, 9799), -1, "Dig next to a pipe in the Taverly Dungeon."
    ),
    DAEMONHEIM_SKILLCAPE_STAND(2727, Position(3446, 3717), -1, "Dig infront of this Skillcape stand."), YAK_PEN_CORNER(
        2729, Position(3193, 3276), -1, "Dig in a corner of the Yak's pen."
    ),
    DUEL_ARENA_BED(2731, Position(3365, 3274), -1, "Dig in front of a bed in the Duel Arena Infirmary."), CHAOS_ALTAR(
        2733, Position(3239, 3606), -1, "Dig behind the Chaos Altar. Watch out for PKers!"
    ),
    NEX_LANDSLIDE(
        2735, Position(2907, 5204), -1, "Dig at the top of a landslide, watch out for Zarosians."
    ),
    BARROWS_FOREST(
        2737, Position(3577, 3321), -1, "Dig around the trees beside the shed at Barrows."
    ),
    BEHIND_EDGEVILLE_BANK(
        2739, Position(3099, 3492), -1, "Dig behind the Edgeville bank - near the banker's window."
    ),
    CHICKENS_SACK(2741, Position(3235, 3287), -1, "Dig beside a grain sack near the chickens!"), LAVA_FORGE(
        2743, Position(2447, 5151), -1, "Dig near the Lava Forge!"
    ),
    LAVA_BANK(2745, Position(2445, 5176), -1, "Dig near the hottest banker."), GNOME_ROPE(
        2747, Position(2477, 3420), -1, "Dig at the start of this Gnome Agility obstacle."
    ),
    TAVERLY_FOUNTAIN(2773, Position(2892, 3440), -1, "Dig infront of the fountain in Taverly."), LUMBRIDGE_GRAVEYARD(
        2774, Position(3248, 3202), -1, "Dig near a grave in Lumbridge."
    ),
    FALADOR_STATUE(2776, Position(2965, 3380), -1, "Dig infront of the statue of Saradomin."), FALADOR_FLOWERBED(
        2778, Position(3012, 3373), -1, "Dig up a flowerbed in Falador."
    ),
    DRAYNOR_BANK(2780, Position(3088, 3247), -1, "Dig infront of the damaged bank wall."), EDGEVILLE_MONASTERY(
        2782, Position(3049, 3506), -1, "Dig between the red and pink rose patches."
    ),
    PORT_SARIM_RUNES(2783, Position(3014, 3259), -1, "Dig in Betty's Runes shop."), EMILY_HOME(
        2785, Position(3666, 2991), -1, "Try not to get banned digging in front of this streamer."
    ),
    YELLOW_ENERGY(2786, Position(2134, 5534, 3), -1, "Dig infront of this Runecrafting Wizard."), CRYSTAL_CHEST(
        2788, Position(3671, 2977), -1, "Dig in front of the Crystal Chest."
    ),
    HOME_MERCHANT(
        2790, Position(3678, 2973), -1, "Dig in front of the boxes beside the flea Merchant at home."
    ),
    FLAX_FIELD(2792, Position(2739, 3440), -1, "Dig under this piece of Flax!"), SEERS_STATUE(
        2793, Position(2740, 3493), -1, "Dig behind this Mysterious statue beside Arthur's castle."
    ),
    PORO_HUNTING(2794, Position(2560, 4288), -1, "Dig in one of the outer corners of Puro-Puro."), GOBLIN_VILLAGE_LUMB(
        2796, Position(3243, 3244), -1, "Dig in the Goblin's house."
    ),
    KARAMJA_PLANTATION(2797, Position(2940, 3155), -1, "Dig beside these boxes in the plantation."), HAM_CAMP(
        2799, Position(3166, 3252), -1, "Dig on top of this trapdoor around Lumbridge."
    ),
    LUMBRIDGE_GATE(3520, Position(3267, 3228), -1, "Dig in front of this path to the desert."), ALKARID_SCIM_SHOP(
        3522, Position(3287, 3187), -1, "Dig in this scimitar shop."
    ),
    FALADOR_BARBERSHOP(3524, Position(2944, 3383), -1, "Dig in the barber shop."), CAMELOT_COAL_TRUCKS(
        3525, Position(2694, 3505), -1, "Dig in the room for storing Coal trucks."
    ),
    ARDY_SPICE_STALL(3526, Position(2659, 3299), -1, "Dig in front of this spicy stall."), WATCHTOWER_F(
        3528, Position(2543, 3115), -1, "Dig in the bottom floor of this tower."
    ),
    VET_TION_GRAVES(3530, Position(2980, 3763), -1, "Dig between these Coffins near Vet'tion."), PEST_CONTROL(
        3532, Position(2658, 2677), -1, "Dig on the other dock at Pest Control."
    ),
    DUEL_ARENA_TOMATO(3534, Position(3382, 3267), -1, "Dig beside these Rotten Tomatoes."), WARRIOR_GUILD(
        3536, Position(2866, 3546), -1, "Dig in the entrance room of the Warrior's Guild."
    ),
    GRAVEYARD_GUARDIAN(3538, Position(3503, 3565), -1, "Dig in front of this Guardian."), BRIMHAVEN_CORPSE(
        3540, Position(2700, 9561), -1, "Dig beside this corpse on the dungeon stairs."
    ),
    DARK_BEAST_CHAOS_TUNNELS(
        3542, Position(3164, 5463), -1, "Dig beside these Mushrooms near the dark beasts."
    ),
    AGED_LOG(3544, Position(1741, 5352), -1, "Dig beside the Aged log in the dungeon."), BEEHIVE(
        3546, Position(2758, 3443), -1, "Dig beside this Beehive."
    ),
    SHOP_BANKER(3548, Position(3690, 2975), -1, "Dig beside a banker at home."), USE_ON_EMILY(
        3550, null, 736, "Use this Cluescroll on our streamer."
    );

    companion object {
        @JvmField
        var hardClueId = intArrayOf(
            2722,
            2723,
            2725,
            2727,
            2729,
            2731,
            2733,
            2735,
            2737,
            2739,
            2741,
            2743,
            2745,
            2747,
            2773,
            2774,
            2776,
            2778,
            2780,
            2782,
            2783,
            2785,
            2786,
            2788,
            2790,
            2792,
            2793,
            2794,
            2796,
            2797,
            2799,
            3520,
            3522,
            3524,
            3525,
            3526,
            3528,
            3530,
            3532,
            3534,
            3536,
            3538,
            3540,
            3542,
            3544,
            3546,
            3548,
            3550
        )
        var hardCasket = 2724
        var consumableRewards = arrayOf(
            Item(556, 250),
            Item(558, 250),
            Item(555, 250),
            Item(554, 250),
            Item(557, 250),
            Item(559, 250),
            Item(564, 250),
            Item(562, 250),
            Item(566, 250),
            Item(9075, 250),
            Item(563, 250),
            Item(561, 250),
            Item(560, 250),
            Item(4698, 250),
            Item(565, 250),  //runes
            Item(882, 350),
            Item(884, 300),
            Item(886, 250),
            Item(888, 200),
            Item(890, 175),
            Item(892, 165),
            Item(11212, 160),
            Item(810, 300),
            Item(811, 250),
            Item(11230, 100),
            Item(877, 100),
            Item(9140, 100),
            Item(9141, 100),
            Item(9142, 100),
            Item(9143, 100),
            Item(9144, 100),
            Item(9240, 100),
            Item(9241, 100),
            Item(9242, 100),
            Item(9243, 100),
            Item(9341, 100),
            Item(9244, 75),
            Item(9245, 75),
            Item(864, 100),
            Item(863, 100),
            Item(865, 100),
            Item(866, 100),
            Item(867, 100),
            Item(868, 100),
            Item(2, 250),  //Ammunition
            Item(326, 500),
            Item(340, 500),
            Item(330, 500),
            Item(362, 400),
            Item(380, 190),
            Item(374, 120),
            Item(7947, 75),
            Item(386, 40),
            Item(392, 20),
            Item(15273, 20),  //Food
            Item(2443, 50),
            Item(2437, 55),
            Item(2441, 50),
            Item(2445, 45),
            Item(3041, 30),
            Item(6686, 20),
            Item(2453, 50),
            Item(3025, 15),
            Item(2435, 25),
            Item(2447, 50),  //potions
            Item(1734, 1000) //thread
        )
        var ultraRareRewards = arrayOf(
            Item(22041, 1),  //Black h'ween
            Item(14484, 1),  //dragon claws
            Item(19780, 1),  //korsai
            Item(1055, 1), Item(1053, 1), Item(1057, 1),  //h'ween masks
            Item(1048, 1), Item(1042, 1), Item(1046, 1), Item(1044, 1), Item(1040, 1), Item(1038, 1),  //phats
            Item(22012, 1),  //Crimson's Katana
            Item(14008, 1), Item(14009, 1), Item(14010, 1),  //Torva
            Item(14011, 1), Item(14012, 1), Item(14013, 1),  //Pernix
            Item(14014, 1), Item(14015, 1), Item(14016, 1),  //Virtus
            Item(13746, 1), Item(13748, 1), Item(13750, 1), Item(13752, 1) //spirit shield sigils
        )
        var equipmentRewards = arrayOf(
            Item(10362, 1),  //Glory (T)
            Item(7414, 1),
            Item(4151, 1),
            Item(15441, 1),
            Item(15442, 1),
            Item(15443, 1),
            Item(15444, 1),
            Item(22007),  //whips
            Item(15018, 1),
            Item(15019, 1),
            Item(15020, 1),
            Item(15220, 1),
            Item(12601),  //rings
            Item(19293, 1),
            Item(19333, 1),  //frost dragon mask, fury (or) kit
            Item(18744, 1),
            Item(18745, 1),
            Item(18746, 1),  //halos
            Item(15486, 1),
            Item(14004, 1),
            Item(14005, 1),
            Item(14006, 1),
            Item(14007, 1),  //staff of light
            Item(2581, 1),
            Item(14000, 1),
            Item(14001, 1),
            Item(14002, 1),
            Item(14003, 1),
            Item(2577, 1),  //robin hoods
            Item(19336, 1),
            Item(19337, 1),
            Item(19338, 1),
            Item(19339, 1),
            Item(19340, 1),
            Item(13262, 1),
            Item(20084, 1),  //dragon items, golden maul
            Item(4708, 1),
            Item(4712, 1),
            Item(4714, 1),
            Item(4710),  //ahrims
            Item(4716, 1),
            Item(4720, 1),
            Item(4722, 1),
            Item(4718, 1),  //dharoks
            Item(4724, 1),
            Item(4728, 1),
            Item(4730, 1),
            Item(4726, 1),  //guthans
            Item(4745, 1),
            Item(4749, 1),
            Item(4751, 1),
            Item(4747, 1),  //torags
            Item(4732, 1),
            Item(4734, 1),
            Item(4736, 1),
            Item(4738, 1),
            Item(4740, 1000),  //karil's
            Item(4753, 1),
            Item(4757, 1),
            Item(4759, 1),
            Item(4755, 1),  //verac's
            Item(2595, 1),
            Item(2591, 1),
            Item(2593, 1),
            Item(2597, 1),
            Item(3473, 1),  //black (g)
            Item(3488, 1),
            Item(3486, 1),
            Item(3481, 1),
            Item(3483, 1),
            Item(3485, 1),  //gilded
            Item(2605, 1),
            Item(2599, 1),
            Item(2601, 1),
            Item(2603, 1),
            Item(3474, 1),  //adamant (t)
            Item(2611, 1),
            Item(3475, 1),
            Item(2613, 1),
            Item(2607, 1),
            Item(2609, 1),  //adamant (g)
            Item(2627, 1),
            Item(2623, 1),
            Item(2625, 1),
            Item(3477, 1),
            Item(2629, 1),  //rune (t)
            Item(2621, 1),
            Item(3476, 1),
            Item(2619, 1),
            Item(2615, 1),
            Item(2617, 1),  //rune (g)
            Item(7380, 1),
            Item(7372, 1),
            Item(7378, 1),
            Item(7370, 1),
            Item(7368, 1),
            Item(7364, 1),
            Item(7366, 1),
            Item(7362, 1),  //range (g) and (t)
            Item(10374, 1),
            Item(10370, 1),
            Item(10372, 1),
            Item(10368, 1),  //zammy dragonhide set
            Item(11730, 1),
            Item(11690, 1),  //sara sword, gs blade
            Item(15126, 1) //Ranging amulet
        )
        var fillerRewards = arrayOf(
            Item(5574, 1),
            Item(5575, 1),
            Item(5576, 1),
            Item(10828, 1),
            Item(1540, 1),
            Item(6528, 1),
            Item(4587, 1),
            Item(1215, 1),
            Item(6568, 1),
            Item(9672, 1),
            Item(9674, 1),
            Item(9676, 1),
            Item(11118, 1),
            Item(4675, 1),
            Item(861, 1),
            Item(8007, 100),
            Item(8008, 100),
            Item(8009, 100),
            Item(8010, 100),
            Item(8011, 100),
            Item(8012, 100),
            Item(8013, 100),
            Item(13599, 100),
            Item(13600, 100),
            Item(13601, 100),
            Item(13602, 100),
            Item(13603, 100),
            Item(13604, 100),
            Item(13605, 100),
            Item(13606, 100),
            Item(13607, 100),
            Item(13608, 100),
            Item(13609, 100),
            Item(13610, 100),
            Item(13611, 100)
        )
        var thirdAgeRewards = arrayOf(
            Item(10350, 1), Item(10348, 1), Item(10346, 1), Item(10352, 1),  //3rd age melee
            Item(10334, 1), Item(10330, 1), Item(10332, 1), Item(10336, 1),  //3rd age range
            Item(10342, 1), Item(10338, 1), Item(10340, 1), Item(10344, 1),  //3rd age mage
            Item(19308, 1), Item(19311, 1), Item(19314, 1), Item(19317, 1), Item(19320, 1)
        )

        @JvmStatic
        fun mockCasket(iterations: Int) {
            /**
             * depreciated
             */
            for (i in 0 until iterations) {
                val a = consumableRewards[Misc.getRandom((consumableRewards.size - 1))]
                val b = fillerRewards[Misc.getRandom((fillerRewards.size - 1))]
                var c: Item
                val equip = Misc.getRandom(1) == 0
                c = if (equip) {
                    equipmentRewards[Misc.getRandom((equipmentRewards.size - 1))]
                } else {
                    consumableRewards[Misc.getRandom((consumableRewards.size - 1))]
                }
                val log =
                    "[" + i + "] " + a.amount + "x " + ItemDefinition.forId(a.id).name + ", " + b.amount + "x " + ItemDefinition.forId(
                        b.id
                    ).name + ", " + c.amount + "x " + ItemDefinition.forId(c.id).name + "."
                PlayerLogs.log("1 - mock", log)
                println("Completed $i")
            }
        }

        @JvmStatic
        fun openCasket(player: Player) {
            if (!player.clickDelay.elapsed(3000)) {
                return
            }
            val equip = Misc.getRandom(1) == 0
            val thirdAge = Misc.getRandom(702) == 1
            val ultraRare = Misc.getRandom(3510) == 1
            val originalCount = player.inventory.getAmount(hardCasket)
            val space = player.inventory.freeSlots >= 2
            val ttt = originalCount >= 1
            if (!ttt) {
                player.packetSender.sendMessage("Error: 90101")
                return
            }
            if (!space) {
                player.packetSender.sendMessage("You must have at least 2 free inventory spaces.")
                return
            }
            player.inventory.delete(hardCasket, 1)
            if (player.inventory.getAmount(hardCasket) != (originalCount - 1)) {
                player.packetSender.sendMessage("ERROR 11012")
                if (player.rights.OwnerDeveloperOnly()) {
                    player.packetSender.sendMessage(
                        "OC = " + originalCount + ", OC-1 = " + (originalCount - 1) + ", current count = " + player.inventory.getAmount(
                            hardCasket
                        )
                    )
                }
                return
            }
            val a: Item
            if (ultraRare || player.username.equals("debug", ignoreCase = true)) {
                a = ultraRareRewards[Misc.getRandom(ultraRareRewards.size - 1)]
                World.sendMessage(
                    "<img=101><col=e3522c><shad=0> " + player.username + " has looted a " + ItemDefinition.forId(a.id).name + " from a Treasure Trail!"
                )
            } else {
                a = consumableRewards[Misc.getRandom(consumableRewards.size - 1)] //(
            }
            val b = fillerRewards[Misc.getRandom(fillerRewards.size - 1)]
            player.inventory.add(a).add(b)
            val c: Item
            if (thirdAge) {
                c = thirdAgeRewards[Misc.getRandom(thirdAgeRewards.size - 1)]
                World.sendMessage(
                    "<img=101><col=e3522c><shad=0> " + player.username + " has looted a " + ItemDefinition.forId(c.id).name + " from a Treasure Trail!"
                )
            } else if (equip) {
                c = equipmentRewards[Misc.getRandom(equipmentRewards.size - 1)]
            } else {
                c = consumableRewards[Misc.getRandom(consumableRewards.size - 1)]
            }
            player.inventory.add(c)
            val col = "<col=255>"
            player.packetSender.sendMessage("$col<img=10> Your casket contained...")
            player.packetSender.sendMessage(col + a.amount + "x " + ItemDefinition.forId(a.id).name + ",")
                .sendMessage(col + b.amount + "x " + ItemDefinition.forId(b.id).name + ",")
                .sendMessage(col + c.amount + "x " + ItemDefinition.forId(c.id).name + ".")
            PlayerLogs.log(
                "1 - hard clue caskets",
                player.username + " has looted: " + a.amount + "x " + ItemDefinition.forId(a.id).name + ", " + b.amount + "x " + ItemDefinition.forId(
                    b.id
                ).name + ", " + c.amount + "x " + ItemDefinition.forId(c.id).name + "."
            )
            player.clickDelay.reset()
        }

        private fun awardCasket(player: Player) {
            player.pointsHandler.setClueSteps(0, false)
            player.inventory.add(hardCasket, 1)
        }

        @JvmStatic
        fun handleClueDig(player: Player): Boolean {
            for (i in values().indices) {
                if (player.inventory.contains(values()[i].clueId) && player.entityPosition.x == values()[i].digTile!!.x && player.entityPosition.y == values()[i].digTile!!.y) {
                    if (player.rights.OwnerDeveloperOnly()) {
                        player.packetSender.sendMessage("[debug] You are on: " + values()[i].digTile!!.x + ", " + values()[i].digTile!!.y + ", index: " + i)
                    }
                    player.inventory.delete(values()[i].clueId, 1)
                    player.pointsHandler.setClueSteps(1, true)
                    val c = Misc.getRandom(1)
                    if (player.rights.OwnerDeveloperOnly()) {
                        player.packetSender.sendMessage("[debug] You rolled a: $c on Misc.getRandom(1)")
                    }
                    if (player.pointsHandler.clueSteps >= 3 && c == 1 || player.pointsHandler.clueSteps >= 10) {
                        awardCasket(player)
                    } else {
                        val newClue = Misc.getRandom(values().size - 1)
                        player.inventory.add(values()[newClue].clueId, 1)
                    }
                    return true
                }
            }
            return false
        }

        @JvmStatic
        fun handleNpcUse(player: Player, npcid: Int): Boolean {
            for (i in values().indices) {
                if (player.inventory.contains(values()[i].clueId) && npcid == values()[i].npcId) {
                    if (player.rights.OwnerDeveloperOnly()) {
                        player.packetSender.sendMessage("[debug] Your NPC ID: " + npcid + ", CLUE npcId " + values()[i].npcId + ", index: " + i)
                    }
                    player.inventory.delete(values()[i].clueId, 1)
                    player.pointsHandler.setClueSteps(1, true)
                    val c = Misc.getRandom(1)
                    if (player.rights.OwnerDeveloperOnly()) {
                        player.packetSender.sendMessage("[debug] You rolled a: $c on Misc.getRandom(1)")
                    }
                    if (player.pointsHandler.clueSteps >= 3 && c == 1 || player.pointsHandler.clueSteps >= 10) {
                        awardCasket(player)
                    } else {
                        val newClue = Misc.getRandom(values().size - 1)
                        player.inventory.add(values()[newClue].clueId, 1)
                    }
                    return true
                }
            }
            return false
        }

        @JvmStatic
        fun sendDropTableInterface(player: Player) {
            try {
                val list: MutableList<Item> = ArrayList()
                for (i in ultraRareRewards.indices) {
                    list.add(ultraRareRewards[i])
                }
                for (i in thirdAgeRewards.indices) {
                    list.add(thirdAgeRewards[i])
                }
                for (i in equipmentRewards.indices) {
                    list.add(equipmentRewards[i])
                }
                for (i in consumableRewards.indices) {
                    list.add(consumableRewards[i])
                }
                for (i in fillerRewards.indices) {
                    list.add(fillerRewards[i])
                }
                resetInterface(player)
                player.packetSender.sendString(8144, "Clue Reward table").sendInterface(8134)
                var index = 0
                val start = 8147
                val cap = 8196
                val secondstart = 12174
                val secondcap = 12224
                var index2 = 0
                var newline = false
                for (i in list.indices) {

                    //for (int i = 0; i < drops.getDropList().length; i++) {
                    if (ItemDefinition.forId(list[i].id) == null || ItemDefinition.forId(
                            list[i].id
                        ).name == null
                    ) {
                        continue
                    }
                    var toSend = 8147 + index
                    if (index + start > cap) {
                        newline = true
                    }
                    if (newline) {
                        toSend = secondstart + index2
                    }
                    if (newline && toSend >= secondcap) {
                        player.packetSender.sendMessage(
                            "<shad=ffffff>" + list[i].amount + "x <shad=0>" + Misc.getColorBasedOnValue(
                                ItemDefinition.forId(
                                    list[i].id
                                ).value * list[i].amount
                            ) + ItemDefinition.forId(list[i].id).name + "."
                        )

                        /*player.getPacketSender().sendMessage("<shad=ffffff>"+drops.getDropList()[i].getItem().getAmount() + "x <shad=0>"
							+ Misc.getColorBasedOnValue(drops.getDropList()[i].getItem().getDefinition().getValue()*drops.getDropList()[i].getItem().getAmount())
							+ drops.getDropList()[i].getItem().getDefinition().getName() + "<shad=-1>@bla@" + (player.getRights().OwnerDeveloperOnly() ? " at a drop rate of 1/"+(dropChance.getRandom() == DropChance.ALWAYS.getRandom() ? "1" : dropChance.getRandom()) : "")
							+ "<shad=ffffff>."); */continue
                    }
                    player.packetSender.sendString(
                        toSend, list[i].amount.toString() + "x " + Misc.getColorBasedOnValue(
                            ItemDefinition.forId(
                                list[i].id
                            ).value * list[i].amount
                        ) + ItemDefinition.forId(list[i].id).name + "."
                    )
                    if (newline) {
                        index2++
                    } else {
                        index++
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun resetInterface(player: Player) {
            for (i in 8145..8195) player.packetSender.sendString(i, "")
            for (i in 12174..12223) player.packetSender.sendString(i, "")
            player.packetSender.sendString(8136, "Close window")
        }
    }
}