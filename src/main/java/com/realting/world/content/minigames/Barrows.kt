package com.realting.world.content.minigames

import com.realting.model.RegionInstance.RegionInstanceType
import com.realting.engine.task.TaskManager
import com.realting.world.World
import com.realting.world.content.dialogue.DialogueManager
import com.realting.engine.task.impl.CeilingCollapseTask
import com.realting.model.*
import com.realting.model.definitions.ItemDefinition
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.model.input.impl.EnterAmountToSellToShop
import com.realting.model.input.impl.EnterAmountToBuyFromShop
import com.realting.util.Misc

/**
 * Handles the Barrows minigame and it's objects, npcs, etc.
 * @editor Gabbe
 */
object Barrows {
    fun handleLogin(player: Player) {
        updateInterface(player)
    }

    /**
     * Handles all objects in the Barrows minigame: Coffins, doors, etc.
     * @param player    The player calling this method
     * @param object    The object the player is requesting
     */
    @JvmStatic
    fun handleObject(player: Player, `object`: GameObject): Boolean {
        when (`object`.id) {
            6771 -> {
                searchCoffin(
                    player,
                    `object`.id,
                    4,
                    2026,
                    if (`object`.position != null) Position(3557, 9715, player.position.z) else Position(3552, 9693)
                )
                return true
            }
            6823 -> {
                searchCoffin(
                    player,
                    `object`.id,
                    0,
                    2030,
                    if (`object`.position != null) Position(3575, 9704, player.position.z) else Position(3552, 9693)
                )
                return true
            }
            6821 -> {
                searchCoffin(
                    player,
                    `object`.id,
                    5,
                    2025,
                    if (`object`.position != null) Position(3557, 9699, player.position.z) else Position(3552, 9693)
                )
                return true
            }
            6772 -> {
                searchCoffin(
                    player,
                    `object`.id,
                    1,
                    2029,
                    if (`object`.position != null) Position(3571, 9684, player.position.z) else Position(3552, 9693)
                )
                return true
            }
            6822 -> {
                searchCoffin(
                    player,
                    `object`.id,
                    2,
                    2028,
                    if (`object`.position != null) Position(3549, 9681, player.position.z) else Position(3552, 9693)
                )
                return true
            }
            6773 -> {
                searchCoffin(
                    player,
                    `object`.id,
                    3,
                    2027,
                    if (`object`.position != null) Position(3537, 9703, player.position.z) else Position(3552, 9693)
                )
                return true
            }
            6745 -> if (`object`.position.x == 3535 && `object`.position.y == 9684) {
                player.moveTo(Position(3535, 9689))
                return true
            } else if (`object`.position.x == 3534 && `object`.position.y == 9688) {
                player.moveTo(Position(3534, 9683))
                return true
            }
            6726 -> if (`object`.position.x == 3535 && `object`.position.y == 9688) {
                player.moveTo(Position(3535, 9683))
                return true
            } else if (`object`.position.x == 3534 && `object`.position.y == 9684) {
                player.moveTo(Position(3534, 9689))
                return true
            }
            6737 -> if (`object`.position.x == 3535 && `object`.position.y == 9701) {
                player.moveTo(Position(3535, 9706))
                return true
            } else if (`object`.position.x == 3534 && `object`.position.y == 9705) {
                player.moveTo(Position(3534, 9700))
                return true
            }
            6718 -> if (`object`.position.x == 3534 && `object`.position.y == 9701) {
                player.moveTo(Position(3534, 9706))
                return true
            } else if (`object`.position.x == 3535 && `object`.position.y == 9705) {
                player.moveTo(Position(3535, 9700))
                return true
            }
            6719 -> if (`object`.position.x == 3541 && `object`.position.y == 9712) {
                player.moveTo(Position(3546, 9712))
                return true
            } else if (`object`.position.x == 3545 && `object`.position.y == 9711) {
                player.moveTo(Position(3540, 9711))
                return true
            }
            6738 -> if (`object`.position.x == 3541 && `object`.position.y == 9711) {
                player.moveTo(Position(3546, 9711))
                return true
            } else if (`object`.position.x == 3545 && `object`.position.y == 9712) {
                player.moveTo(Position(3540, 9712))
                return true
            }
            6740 -> if (`object`.position.x == 3558 && `object`.position.y == 9711) {
                player.moveTo(Position(3563, 9711))
                return true
            } else if (`object`.position.x == 3562 && `object`.position.y == 9712) {
                player.moveTo(Position(3557, 9712))
                return true
            }
            6721 -> if (`object`.position.x == 3558 && `object`.position.y == 9712) {
                player.moveTo(Position(3563, 9712))
                return true
            } else if (`object`.position.x == 3562 && `object`.position.y == 9711) {
                player.moveTo(Position(3557, 9711))
                return true
            }
            6741 -> if (`object`.position.x == 3568 && `object`.position.y == 9705) {
                player.moveTo(Position(3568, 9700))
                return true
            } else if (`object`.position.x == 3569 && `object`.position.y == 9701) {
                player.moveTo(Position(3569, 9706))
                return true
            }
            6722 -> if (`object`.position.x == 3569 && `object`.position.y == 9705) {
                player.moveTo(Position(3569, 9700))
                return true
            } else if (`object`.position.x == 3568 && `object`.position.y == 9701) {
                player.moveTo(Position(3568, 9706))
                return true
            }
            6747 -> if (`object`.position.x == 3568 && `object`.position.y == 9688) {
                player.moveTo(Position(3568, 9683))
                return true
            } else if (`object`.position.x == 3569 && `object`.position.y == 9684) {
                player.moveTo(Position(3569, 9689))
                return true
            }
            6728 -> if (`object`.position.x == 3569 && `object`.position.y == 9688) {
                player.moveTo(Position(3569, 9683))
                return true
            } else if (`object`.position.x == 3568 && `object`.position.y == 9684) {
                player.moveTo(Position(3568, 9689))
                return true
            }
            6749 -> if (`object`.position.x == 3562 && `object`.position.y == 9678) {
                player.moveTo(Position(3557, 9678))
                return true
            } else if (`object`.position.x == 3558 && `object`.position.y == 9677) {
                player.moveTo(Position(3563, 9677))
                return true
            }
            6730 -> if (`object`.position.x == 3562 && `object`.position.y == 9677) {
                player.moveTo(Position(3557, 9677))
                return true
            } else if (`object`.position.x == 3558 && `object`.position.y == 9678) {
                player.moveTo(Position(3563, 9678))
                return true
            }
            6748 -> if (`object`.position.x == 3545 && `object`.position.y == 9678) {
                player.moveTo(Position(3540, 9678))
                return true
            } else if (`object`.position.x == 3541 && `object`.position.y == 9677) {
                player.moveTo(Position(3546, 9677))
                return true
            }
            6729 -> if (`object`.position.x == 3545 && `object`.position.y == 9677) {
                player.moveTo(Position(3540, 9677))
                return true
            } else if (`object`.position.x == 3541 && `object`.position.y == 9678) {
                player.moveTo(Position(3546, 9678))
                return true
            }
            10284 -> {
                if (player.minigameAttributes.barrowsMinigameAttributes.killcount < 5) return true
                if (player.minigameAttributes.barrowsMinigameAttributes.barrowsData[player.minigameAttributes.barrowsMinigameAttributes.randomCoffin][1] == 0) {
                    handleObject(
                        player, GameObject(
                            COFFIN_AND_BROTHERS[player.minigameAttributes.barrowsMinigameAttributes.randomCoffin][0],
                            null
                        )
                    )
                    player.minigameAttributes.barrowsMinigameAttributes.barrowsData[player.minigameAttributes.barrowsMinigameAttributes.randomCoffin][1] =
                        1
                    return true
                } else if (player.minigameAttributes.barrowsMinigameAttributes.barrowsData[player.minigameAttributes.barrowsMinigameAttributes.randomCoffin][1] == 1) {
                    player.packetSender.sendMessage("You cannot loot this whilst in combat!")
                    return true
                } else if (player.minigameAttributes.barrowsMinigameAttributes.barrowsData[player.minigameAttributes.barrowsMinigameAttributes.randomCoffin][1] == 2 && player.minigameAttributes.barrowsMinigameAttributes.killcount >= 6) {
                    if (player.inventory.freeSlots < 3) {
                        player.packetSender.sendMessage("You need at least 3 free inventory slots to loot this chest.")
                        return true
                    }
                    resetBarrows(player)
                    val r = randomRunes()
                    val num = 25 + Misc.getRandom(255)
                    if (player.rights.isMember) {
                        player.inventory.add(r, num * 2)
                        player.packetSender.sendMessage("<img=10> As a member, you get double " + ItemDefinition.forId(r).name + "s from the chest!")
                    } else {
                        player.inventory.add(r, num)
                    }
                    player.pointsHandler.setBarrowsChests(1, true)
                    player.packetSender.sendMessage("<img=10><shad=0> @lre@You've looted a total of " + player.pointsHandler.barrowsChests + " Barrows chests!")
                    if (player.pointsHandler.barrowsPoints == 10) {
                        player.packetSender.sendMessage("<img=10> @red@You can now purchase the \"Looter\" loyalty title.")
                    }
                    if (player.pointsHandler.barrowsChests < 10) {
                        player.packetSender.sendMessage("<img=10><shad=0>Only " + (10 - player.pointsHandler.barrowsChests) + " more for the @lre@\"@red@Looter@lre@\" title.")
                    }
                    if (Misc.getRandom(100) >= 45) {
                        val b = randomBarrows()
                        player.inventory.add(b, 1)
                        player.packetSender.sendMessage(
                            "<img=10><col=009966><shad=0> Congratulations! You've just received " + ItemDefinition.forId(
                                b
                            ).name + " from Barrows!"
                        )
                    }
                    val coffin = Misc.getRandom(250)
                    if (coffin == 1 || player.rights.isMember && coffin == 2 || player.username.equals(
                            "debug", ignoreCase = true
                        )
                    ) {
                        player.inventory.add(7587, 1)
                        World.sendMessage("<img=10><shad=0><col=009966> " + player.username + " has just recieved a Coffin of the Damned from the Barrows minigame!")
                    }
                    player.packetSender.sendCameraShake(3, 2, 3, 2)
                    player.packetSender.sendMessage("The cave begins to collapse!")
                    TaskManager.submit(CeilingCollapseTask(player))
                }
            }
            6744, 6725 -> if (player.position.x == 3563) showRiddle(player)
            6746, 6727 -> if (player.position.y == 9683) showRiddle(player)
            6743, 6724 -> if (player.position.x == 3540) showRiddle(player)
            6739, 6720 -> if (player.position.y == 9706) player.moveTo(Position(3551, 9694))
        }
        return false
    }

    fun showRiddle(player: Player) {
        player.packetSender.sendString(4553, "1.")
        player.packetSender.sendString(4554, "2.")
        player.packetSender.sendString(4555, "3.")
        player.packetSender.sendString(4556, "4.")
        player.packetSender.sendString(4549, "Which item comes next?")
        val riddle = Misc.getRandom(riddles.size - 1)
        player.packetSender.sendInterfaceModel(4545, riddles[riddle][0], 200)
        player.packetSender.sendInterfaceModel(4546, riddles[riddle][1], 200)
        player.packetSender.sendInterfaceModel(4547, riddles[riddle][2], 200)
        player.packetSender.sendInterfaceModel(4548, riddles[riddle][3], 200)
        player.packetSender.sendInterfaceModel(4550, riddles[riddle][4], 200)
        player.packetSender.sendInterfaceModel(4551, riddles[riddle][5], 200)
        player.packetSender.sendInterfaceModel(4552, riddles[riddle][6], 200)
        player.minigameAttributes.barrowsMinigameAttributes.riddleAnswer = riddles[riddle][7]
        player.packetSender.sendInterface(4543)
    }

    fun handlePuzzle(player: Player, puzzleButton: Int) {
        if (puzzleButton == player.minigameAttributes.barrowsMinigameAttributes.riddleAnswer) {
            player.moveTo(Position(3551, 9694))
            player.packetSender.sendMessage("You got the correct answer.")
            player.packetSender.sendMessage("A magical force guides you to a chest located in the center room.")
        } else player.packetSender.sendMessage("You got the wrong answer.")
        player.minigameAttributes.barrowsMinigameAttributes.riddleAnswer = -1
    }

    var riddles = arrayOf(intArrayOf(2349, 2351, 2353, 2355, 2359, 2363, 2361, 0))

    /**
     * Handles coffin searching
     * @param player    Player searching a coffin
     * @param obj    The object (coffin) being searched
     * @param coffinId    The coffin's array index
     * @param npcId    The NPC Id of the NPC to spawn after searching
     * @param constitution    NPC stat
     * @param attackLevel    NPC stat
     * @param strengthLevel    NPC stat
     * @param defenceLevel    NPC stat
     * @param absorbMelee    NPC stat
     * @param absorbRanged    NPC stat
     * @param absorbMagic    NPC stat
     * @param getCombatAttributes().getAttackDelay()	NPC attackspeed
     * @param maxhit    NPC Maxhit
     */
    fun searchCoffin(player: Player, obj: Int, coffinId: Int, npcId: Int, spawnPos: Position?) {
        player.packetSender.sendInterfaceRemoval()
        if (player.position.z == -1) {
            if (selectCoffin(player, obj)) return
        }
        if (player.minigameAttributes.barrowsMinigameAttributes.barrowsData[coffinId][1] == 0) {
            if (player.location === Locations.Location.BARROWS) {
                player.regionInstance = RegionInstance(player, RegionInstanceType.BARROWS)
                val npc_ = NPC(npcId, spawnPos)
                npc_.forceChat(if (player.position.z == -1) "You dare disturb my rest!" else "You dare steal from us!")
                npc_.combatBuilder.attackTimer = 3
                npc_.spawnedFor = player
                npc_.combatBuilder.attack(player)
                World.register(npc_)
                player.regionInstance.npcsList.add(npc_)
                player.minigameAttributes.barrowsMinigameAttributes.barrowsData[coffinId][1] = 1
            }
        } else {
            player.packetSender.sendMessage("You have already searched this sarcophagus.")
        }
    }

    @JvmStatic
    fun resetBarrows(player: Player) {
        player.minigameAttributes.barrowsMinigameAttributes.killcount = 0
        for (i in player.minigameAttributes.barrowsMinigameAttributes.barrowsData.indices) player.minigameAttributes.barrowsMinigameAttributes.barrowsData[i][1] =
            0
        updateInterface(player)
        player.minigameAttributes.barrowsMinigameAttributes.randomCoffin = randomCoffin
    }

    val data = arrayOf(
        arrayOf<Any>("Verac The Defiled", 37203),
        arrayOf<Any>("Torag The Corrupted", 37205),
        arrayOf<Any>("Karil The Tainted", 37207),
        arrayOf<Any>("Guthan The Infested", 37206),
        arrayOf<Any>("Dharok The Wretched", 37202),
        arrayOf<Any>("Ahrim The Blighted", 37204)
    )

    /**
     * Deregisters an NPC located in the Barrows minigame
     * @param player    The player that's the reason for deregister
     * @param barrowBrother    The NPC to deregister
     * @param killed    Did player kill the NPC?
     */
    @JvmStatic
    fun killBarrowsNpc(player: Player?, n: NPC?, killed: Boolean) {
        if (player == null || n == null) return
        if (n.id == 58) {
            player.minigameAttributes.barrowsMinigameAttributes.killcount =
                player.minigameAttributes.barrowsMinigameAttributes.killcount + 1
            updateInterface(player)
            return
        }
        val arrayIndex = getBarrowsIndex(player, n.id)
        if (arrayIndex < 0) return
        if (killed) {
            player.minigameAttributes.barrowsMinigameAttributes.barrowsData[arrayIndex][1] = 2
            player.minigameAttributes.barrowsMinigameAttributes.killcount =
                player.minigameAttributes.barrowsMinigameAttributes.killcount + 1
            if (player.regionInstance != null) {
//                player.regionInstance.npcsList.remove(player)
                player.regionInstance.remove(player)
            }
            player.barrowsKilled = player.barrowsKilled + 1
        } else if (arrayIndex >= 0) player.minigameAttributes.barrowsMinigameAttributes.barrowsData[arrayIndex][1] = 0
        updateInterface(player)
    }

    /**
     * Selects the coffin and shows the interface if coffin id matches random
     * coffin
     */
    fun selectCoffin(player: Player, coffinId: Int): Boolean {
        if (player.minigameAttributes.barrowsMinigameAttributes.randomCoffin == 0) player.minigameAttributes.barrowsMinigameAttributes.randomCoffin =
            randomCoffin
        if (COFFIN_AND_BROTHERS[player.minigameAttributes.barrowsMinigameAttributes.randomCoffin][0] == coffinId) {
            DialogueManager.start(player, 27)
            player.dialogueActionId = 16
            return true
        }
        return false
    }

    fun getBarrowsIndex(player: Player, id: Int): Int {
        var index = -1
        for (i in player.minigameAttributes.barrowsMinigameAttributes.barrowsData.indices) {
            if (player.minigameAttributes.barrowsMinigameAttributes.barrowsData[i][0] == id) {
                index = i
            }
        }
        return index
    }

    @JvmStatic
    fun updateInterface(player: Player) {
        for (i in data.indices) {
            val killed = player.minigameAttributes.barrowsMinigameAttributes.barrowsData[i][1] == 2
            val s = if (killed) "@gre@" else "@red@"
            player.packetSender.sendString(data[i][1] as Int, "" + s + "" + data[i][0] as String)
        }
        player.packetSender.sendString(
            37208, "Killcount: " + player.minigameAttributes.barrowsMinigameAttributes.killcount
        )
    }

    fun fixBarrows(player: Player) {
        player.packetSender.sendInterfaceRemoval()
        var totalCost = 0
        val money = player.inventory.getAmount(995)
        var breakLoop = false
        for (items in player.inventory.items) {
            if (items == null) continue
            for (i in brokenBarrows.indices) {
                if (player.inventory.getSlot(items.id) > 0) {
                    if (items.id == brokenBarrows[i][1]) {
                        if (totalCost + 45000 > money) {
                            breakLoop = true
                            player.packetSender.sendMessage("You need at least 45000 coins to fix this item.")
                            break
                        } else {
                            totalCost += 45000
                            player.inventory.setItem(player.inventory.getSlot(items.id), Item(brokenBarrows[i][0], 1))
                            player.inventory.refreshItems()
                        }
                    }
                }
            }
            if (breakLoop) break
        }
        if (totalCost > 0) player.inventory.delete(995, totalCost)
    }

    var runes = intArrayOf(4740, 558, 560, 565)
    var barrows = intArrayOf(
        4708,
        4710,
        4712,
        4714,
        4716,
        4718,
        4720,
        4722,
        4724,
        4726,
        4728,
        4730,
        4732,
        4734,
        4736,
        4738,
        4745,
        4747,
        4749,
        4751,
        4753,
        4755,
        4757,
        4759
    )
    val brokenBarrows = arrayOf(
        intArrayOf(4708, 4860),
        intArrayOf(4710, 4866),
        intArrayOf(4712, 4872),
        intArrayOf(4714, 4878),
        intArrayOf(4716, 4884),
        intArrayOf(4720, 4896),
        intArrayOf(4718, 4890),
        intArrayOf(4720, 4896),
        intArrayOf(4722, 4902),
        intArrayOf(4732, 4932),
        intArrayOf(4734, 4938),
        intArrayOf(4736, 4944),
        intArrayOf(4738, 4950),
        intArrayOf(4724, 4908),
        intArrayOf(4726, 4914),
        intArrayOf(4728, 4920),
        intArrayOf(4730, 4926),
        intArrayOf(4745, 4956),
        intArrayOf(4747, 4926),
        intArrayOf(4749, 4968),
        intArrayOf(4751, 4994),
        intArrayOf(4753, 4980),
        intArrayOf(4755, 4986),
        intArrayOf(4757, 4992),
        intArrayOf(4759, 4998)
    )
    val COFFIN_AND_BROTHERS = arrayOf(
        intArrayOf(6823, 2030),
        intArrayOf(6772, 2029),
        intArrayOf(6822, 2028),
        intArrayOf(6773, 2027),
        intArrayOf(6771, 2026),
        intArrayOf(6821, 2025)
    )

    fun isBarrowsNPC(id: Int): Boolean {
        for (i in COFFIN_AND_BROTHERS.indices) {
            if (COFFIN_AND_BROTHERS[i][1] == id) return true
        }
        return false
    }

    val UNDERGROUND_SPAWNS = arrayOf(
        Position(3569, 9677), Position(3535, 9677), Position(3534, 9711), Position(3569, 9712)
    )
    val randomCoffin: Int
        get() = Misc.getRandom(COFFIN_AND_BROTHERS.size - 1)

    fun randomRunes(): Int {
        return runes[(Math.random() * runes.size).toInt()]
    }

    fun randomBarrows(): Int {
        return barrows[(Math.random() * barrows.size).toInt()]
    }
}