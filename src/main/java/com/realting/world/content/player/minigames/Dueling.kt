package com.realting.world.content.minigames

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.*
import com.realting.model.container.impl.Equipment
import com.realting.model.container.impl.Inventory
import com.realting.model.definitions.ItemDefinition
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import com.realting.world.World
import com.realting.world.content.BankPin
import com.realting.world.content.PlayerLogs
import com.realting.world.content.PlayerPanel
import com.realting.world.content.player.events.BonusManager
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

open class Dueling(var player: Player) {
    fun challengePlayer(playerToDuel: Player) {
        /*if(player.getUsername().equalsIgnoreCase("TheBigCashew")) {
			player.getPacketSender().sendMessage("You need to chill the fuck out fam, too many niggas quit.");
			return;
		}*/
        if (player.location !== Locations.Location.DUEL_ARENA) return
        if (player.interfaceId > 0) {
            player.packetSender.sendMessage("Please close the interface you have open before trying to open a new one.")
            return
        }
        if (!Locations.goodDistance(
                player.position.x, player.position.y, playerToDuel.position.x, playerToDuel.position.y, 2
            )
        ) {
            player.packetSender.sendMessage("Please get closer to request a duel.")
            return
        }
        if (!checkDuel(player, 0)) {
            player.packetSender.sendMessage("Unable to request duel. Please try logging out and then logging back in.")
            return
        }
        if (!checkDuel(playerToDuel, 0) || playerToDuel.interfaceId > 0) {
            player.packetSender.sendMessage("The other player is currently busy.")
            return
        }
        if (player.dueling.duelingStatus == 5) {
            player.packetSender.sendMessage("You can only challenge people outside the arena.")
            return
        }
        if (player.bankPinAttributes.hasBankPin() && !player.bankPinAttributes.hasEnteredBankPin()) {
            BankPin.init(player, false)
            return
        }
        if (player.summoning.familiar != null) {
            player.packetSender.sendMessage("You must dismiss your familiar before being allowed to start a duel.")
            return
        }
        if (inDuelScreen) return
        if (player.trading.inTrade()) player.trading.declineTrade(true)
        duelingWith = playerToDuel.index
        if (duelingWith == player.index) return
        duelRequested = true
        val challenged = playerToDuel.dueling.duelingStatus == 0 && duelRequested || playerToDuel.dueling.duelRequested
        if (duelingStatus == 0 && challenged && duelingWith == playerToDuel.index && playerToDuel.dueling.duelingWith == player.index) {
            if (duelingStatus == 0) {
                openDuel()
                playerToDuel.dueling.openDuel()
            } else {
                player.packetSender.sendMessage("You must decline this duel before accepting another one!")
            }
        } else if (duelingStatus == 0) {
            playerToDuel.packetSender.sendMessage(player.username + ":duelreq:")
            player.packetSender.sendMessage("You have sent a duel request to " + playerToDuel.username + ".")
        }
    }

    fun openDuel() {
        val playerToDuel = World.getPlayers()[duelingWith] ?: return
        player.packetSender.sendClientRightClickRemoval()
        inDuelWith = playerToDuel.index
        stakedItems.clear()
        inDuelScreen = true
        duelingStatus = 1
        if (!checkDuel(player, 1)) return
        for (i in selectedDuelRules.indices) selectedDuelRules[i] = false
        player.packetSender.sendConfig(286, 0)
        player.trading.canOffer = true
        player.packetSender.sendDuelEquipment()
        player.packetSender.sendString(
            6671,
            "Dueling with: " + playerToDuel.username + ", Level: " + playerToDuel.skillManager.combatLevel + ", Duel victories: " + playerToDuel.dueling.arenaStats[0] + ", Duel losses: " + playerToDuel.dueling.arenaStats[1]
        )
        player.packetSender.sendString(6684, "").sendString(669, "Lock Weapon")
            .sendString(8278, "Neither player is allowed to change weapon.")
        player.packetSender.sendInterfaceSet(6575, 3321)
        player.packetSender.sendItemContainer(player.inventory, 3322)
        player.packetSender.sendInterfaceItems(6670, playerToDuel.dueling.stakedItems)
        player.packetSender.sendInterfaceItems(6669, player.dueling.stakedItems)
        canOffer = true
    }

    fun declineDuel(tellOther: Boolean) {
        val playerToDuel = if (duelingWith >= 0) World.getPlayers()[duelingWith] else null
        if (tellOther) {
            if (playerToDuel == null) return
            if (playerToDuel == null || playerToDuel.dueling.duelingStatus == 6) {
                return
            }
            playerToDuel.dueling.declineDuel(false)
        }
        for (item in stakedItems) {
            if (item.amount < 1) continue
            player.inventory.add(item)
        }
        reset()
        player.packetSender.sendInterfaceRemoval()
    }

    fun resetAcceptedStake() {
        val playerToDuel = World.getPlayers()[duelingWith] ?: return
        if (player.dueling.duelingStatus == 2 || playerToDuel.dueling.duelingStatus == 2) {
            player.dueling.duelingStatus = 1
            player.packetSender.sendString(6684, "")
            playerToDuel.packetSender.sendString(6684, "")
            playerToDuel.dueling.duelingStatus = 1
        }
    }

    fun stakeItem(itemId: Int, amount: Int, slot: Int) {
        var amount = amount
        if (slot < 0) return
        /*
		if(Misc.getMinutesPlayed(player) < 15) {
			player.getPacketSender().sendMessage("You must have played for at least 15 minutes in order to stake items.");
			return;
		}*/if (!getCanOffer()) return
        resetAcceptedStake()
        if (player.rights.isStaff && !player.rights.OwnerDeveloperOnly()) {
            player.packetSender.sendMessage("Staff cannot stake.")
            return
        }
        if (!player.inventory.contains(itemId) || !inDuelScreen) return
        val playerToDuel = World.getPlayers()[duelingWith]
        /*if(playerToDuel.getUsername().equalsIgnoreCase("TheBigCashew")) {
			player.getPacketSender().sendMessage("That player is banned from staking!");
			return;
		}*/if (player.rights != PlayerRights.DEVELOPER && playerToDuel.rights != PlayerRights.DEVELOPER) {
            if (!Item(itemId).tradeable()) {
                player.packetSender.sendMessage("This item is currently untradeable and cannot be traded.")
                return
            }
        }
        if (player.gameMode == GameMode.IRONMAN) {
            player.packetSender.sendMessage("Ironmen can't stake.")
            return
        }
        if (player.gameMode == GameMode.ULTIMATE_IRONMAN) {
            player.packetSender.sendMessage("UIM can't stake.")
            return
        }
        if (playerToDuel.gameMode == GameMode.IRONMAN) {
            player.packetSender.sendMessage("You cannot stake an Ironman.")
            return
        }
        if (playerToDuel.gameMode == GameMode.ULTIMATE_IRONMAN) {
            player.packetSender.sendMessage("You cannot stake a Ultimate Ironman.")
            return
        }
        if (!checkDuel(player, 1) || !checkDuel(
                playerToDuel, 1
            ) || slot >= player.inventory.capacity() || player.inventory.items[slot].id != itemId || player.inventory.items[slot].amount <= 0
        ) {
            declineDuel(false)
            playerToDuel.dueling.declineDuel(false)
            return
        }
        if (player.inventory.getAmount(itemId) < amount) {
            amount = player.inventory.getAmount(itemId)
            if (amount == 0 || player.inventory.getAmount(itemId) < amount) {
                return
            }
        }
        if (!ItemDefinition.forId(itemId).isStackable) {
            for (a in 0 until amount) {
                if (player.inventory.contains(itemId)) {
                    stakedItems.add(Item(itemId, 1))
                    player.inventory.delete(Item(itemId))
                }
            }
        } else {
            if (amount <= 0 || player.inventory.items[slot].amount <= 0) return
            var itemInScreen = false
            for (item in stakedItems) {
                if (item.id == itemId) {
                    itemInScreen = true
                    item.amount = item.amount + amount
                    player.inventory.delete(Item(itemId).setAmount(amount), slot)
                    break
                }
            }
            if (!itemInScreen) {
                player.inventory.delete(Item(itemId, amount), slot)
                stakedItems.add(Item(itemId, amount))
            }
        }
        player.packetSender.sendClientRightClickRemoval()
        player.packetSender.sendInterfaceItems(6670, playerToDuel.dueling.stakedItems)
        player.packetSender.sendInterfaceItems(6669, player.dueling.stakedItems)
        playerToDuel.packetSender.sendInterfaceItems(6670, player.dueling.stakedItems)
        player.packetSender.sendString(6684, "")
        playerToDuel.packetSender.sendString(6684, "")
        duelingStatus = 1
        playerToDuel.dueling.duelingStatus = 1
        player.inventory.refreshItems()
        player.packetSender.sendItemContainer(player.inventory, 3322)
    }

    val duelOpponent: String
        get() = World.getPlayers()[duelingWith].username

    fun removeStakedItem(itemId: Int, amount: Int) {
        var amount = amount
        if (!inDuelScreen || !getCanOffer()) return
        val playerToDuel = World.getPlayers()[duelingWith] ?: return
        resetAcceptedStake()
        if (!checkDuel(player, 1) || !checkDuel(playerToDuel, 1)) {
            declineDuel(false)
            playerToDuel.dueling.declineDuel(false)
            return
        }
        /*
	        if (Item.itemStackable[itemID]) {
	            if (playerToDuel.getInventory().getFreeSlots() - 1 < (c.duelSpaceReq)) {
	                c.sendMessage("You have too many rules set to remove that item.");
	                return false;
	            }
	        }*/player.packetSender.sendClientRightClickRemoval()
        if (!ItemDefinition.forId(itemId).isStackable) {
            if (amount > 28) amount = 28
            for (a in 0 until amount) {
                for (item in stakedItems) {
                    if (item.id == itemId) {
                        if (!item.definition.isStackable) {
                            if (!checkDuel(player, 1) || !checkDuel(playerToDuel, 1)) {
                                declineDuel(false)
                                playerToDuel.dueling.declineDuel(false)
                                return
                            }
                            stakedItems.remove(item)
                            player.inventory.add(item)
                        }
                        break
                    }
                }
            }
        } else for (item in stakedItems) {
            if (item.id == itemId) {
                if (item.definition.isStackable) {
                    if (item.amount > amount) {
                        item.amount = item.amount - amount
                        player.inventory.add(itemId, amount)
                    } else {
                        amount = item.amount
                        stakedItems.remove(item)
                        player.inventory.add(item.id, amount)
                    }
                }
                break
            }
        }
        player.packetSender.sendInterfaceItems(6670, playerToDuel.dueling.stakedItems)
        player.packetSender.sendInterfaceItems(6669, player.dueling.stakedItems)
        playerToDuel.packetSender.sendInterfaceItems(6670, player.dueling.stakedItems)
        playerToDuel.packetSender.sendString(6684, "")
        player.packetSender.sendString(6684, "")
        duelingStatus = 1
        playerToDuel.dueling.duelingStatus = 1
        player.inventory.refreshItems()
        player.packetSender.sendItemContainer(player.inventory, 3322)
    }

    fun selectRule(duelRule: DuelRule) {
        if (duelingWith < 0) return
        val playerToDuel = World.getPlayers()[duelingWith] ?: return
        if (player.interfaceId != 6575) return
        if (duelRule == DuelRule.LOCK_WEAPON) {
            if (player.equipment[Equipment.WEAPON_SLOT].id != playerToDuel.equipment[Equipment.WEAPON_SLOT].id) {
                player.packetSender.sendMessage("@red@This rule requires you and your duel partner to have the same weapon equipped.")
                return
            }
        }
        val index = duelRule.ordinal
        val alreadySet = selectedDuelRules[duelRule.ordinal]
        var slotOccupied =
            if (duelRule.equipmentSlot > 0) player.equipment.items[duelRule.equipmentSlot].id > 0 || playerToDuel.equipment.items[duelRule.equipmentSlot].id > 0 else false
        if (duelRule == DuelRule.NO_SHIELD) {
            if (player.equipment.items[Equipment.WEAPON_SLOT].id > 0 && ItemDefinition.forId(
                    player.equipment.items[Equipment.WEAPON_SLOT].id
                ).isTwoHanded || ItemDefinition.forId(playerToDuel.equipment.items[Equipment.WEAPON_SLOT].id).isTwoHanded
            ) slotOccupied = true
        }
        var spaceRequired = if (slotOccupied) duelRule.inventorySpaceReq else 0
        for (i in 10 until selectedDuelRules.size) {
            if (selectedDuelRules[i]) {
                val rule = DuelRule.forId(i)
                if (rule!!.equipmentSlot > 0) if (player.equipment.items[rule.equipmentSlot].id > 0 || playerToDuel.equipment.items[rule.equipmentSlot].id > 0) spaceRequired += rule.inventorySpaceReq
            }
        }
        if (!alreadySet && player.inventory.freeSlots < spaceRequired) {
            player.packetSender.sendMessage("You do not have enough free inventory space to set this rule.")
            return
        }
        if (!alreadySet && playerToDuel.inventory.freeSlots < spaceRequired) {
            player.packetSender.sendMessage("" + playerToDuel.username + " does not have enough inventory space for this rule to be set.")
            return
        }
        if (!player.dueling.selectedDuelRules[index]) {
            player.dueling.selectedDuelRules[index] = true
            player.dueling.duelConfig += duelRule.configId
        } else {
            player.dueling.selectedDuelRules[index] = false
            player.dueling.duelConfig -= duelRule.configId
        }
        player.packetSender.sendToggle(286, player.dueling.duelConfig)
        playerToDuel.dueling.duelConfig = player.dueling.duelConfig
        playerToDuel.dueling.selectedDuelRules[index] = player.dueling.selectedDuelRules[index]
        playerToDuel.packetSender.sendToggle(286, playerToDuel.dueling.duelConfig)
        player.packetSender.sendString(6684, "")
        resetAcceptedStake()
        if (selectedDuelRules[DuelRule.OBSTACLES.ordinal]) {
            if (selectedDuelRules[DuelRule.NO_MOVEMENT.ordinal]) {
                val duelTele = Position(3366 + Misc.getRandom(12), 3246 + Misc.getRandom(6), 0)
                player.dueling.duelTelePos = duelTele
                playerToDuel.dueling.duelTelePos = player.dueling.duelTelePos!!.copy()
                //TODO:: check what this does
//                playerToDuel.dueling.duelTelePos.x = player.dueling.duelTelePos!!.x - 1
            }
        } else {
            if (selectedDuelRules[DuelRule.NO_MOVEMENT.ordinal]) {
                val duelTele = Position(3335 + Misc.getRandom(12), 3246 + Misc.getRandom(6), 0)
                player.dueling.duelTelePos = duelTele
                playerToDuel.dueling.duelTelePos = player.dueling.duelTelePos!!.copy()
//                playerToDuel.dueling.duelTelePos.x = player.dueling.duelTelePos.x - 1
            }
        }
        if (duelRule == DuelRule.LOCK_WEAPON && selectedDuelRules[duelRule.ordinal]) {
            player.packetSender.sendMessage("@red@Warning! The rule 'Lock Weapon' has been enabled. You will not be able to change")
                .sendMessage("@red@weapon during the duel!")
            playerToDuel.packetSender.sendMessage("@red@Warning! The rule 'Lock Weapon' has been enabled. You will not be able to change")
                .sendMessage("@red@weapon during the duel!")
        }
    }

    fun confirmDuel() {
        val playerToDuel = World.getPlayers()[duelingWith]
        if (playerToDuel == null) return else {
            if (!twoDuelers(player, playerToDuel)) {
                player.packetSender.sendMessage("An error has occured. Please try requesting a new duel.")
                return
            }
        }
        var itemId = ""
        for (item in player.dueling.stakedItems) {
            val def = item.definition
            itemId += if (def.isStackable) {
                def.name + " x " + Misc.format(item.amount) + "\\n"
            } else {
                def.name + "\\n"
            }
        }
        player.packetSender.sendString(6516, itemId)
        itemId = ""
        for (item in playerToDuel.dueling.stakedItems) {
            val def = item.definition
            itemId += if (def.isStackable) {
                def.name + " x " + Misc.format(item.amount) + "\\n"
            } else {
                def.name + "\\n"
            }
        }
        canOffer = false
        player.packetSender.sendString(6517, itemId)
        player.packetSender.sendString(8242, "")
        for (i in 8238..8253) player.packetSender.sendString(i, "")
        player.packetSender.sendString(8250, "Hitpoints will be restored.")
        player.packetSender.sendString(8238, "Boosted stats will be restored.")
        if (selectedDuelRules[DuelRule.OBSTACLES.ordinal]) player.packetSender.sendString(
            8239, "@red@There will be obstacles in the arena."
        )
        player.packetSender.sendString(8240, "")
        player.packetSender.sendString(8241, "")
        var lineNumber = 8242
        for (i in DuelRule.values().indices) {
            if (i == DuelRule.OBSTACLES.ordinal) continue
            if (selectedDuelRules[i]) {
                player.packetSender.sendString(lineNumber, "" + DuelRule.forId(i).toString())
                lineNumber++
            }
        }
        player.packetSender.sendString(6571, "")
        player.packetSender.sendInterfaceSet(6412, Inventory.INTERFACE_ID)
        player.packetSender.sendItemContainer(player.inventory, 3322)
    }

    fun startDuel() {
        player.session.clearMessages()
        inDuelScreen = false
        val playerToDuel = World.getPlayers()[duelingWith]
        if (playerToDuel == null) {
            duelVictory()
            return
        }
        player.trading.offeredItems.clear()
        duelingData[0] = playerToDuel.username
        duelingData[1] = playerToDuel.skillManager?.combatLevel ?: 3
        var equipItem: Item?
        for (i in 10 until selectedDuelRules.size) {
            val rule = DuelRule.forId(i)
            if (selectedDuelRules[i]) {
                if (rule!!.equipmentSlot < 0) continue
                if (player.equipment.items[rule.equipmentSlot].id > 0) {
                    equipItem = Item(
                        player.equipment.items[rule.equipmentSlot].id, player.equipment.items[rule.equipmentSlot].amount
                    )
                    player.equipment.delete(equipItem)
                    player.inventory.add(equipItem)
                }
            }
        }
        if (selectedDuelRules[DuelRule.NO_WEAPON.ordinal] || selectedDuelRules[DuelRule.NO_SHIELD.ordinal]) {
            if (player.equipment.items[Equipment.WEAPON_SLOT].id > 0) {
                if (ItemDefinition.forId(player.equipment.items[Equipment.WEAPON_SLOT].id).isTwoHanded) {
                    equipItem = Item(
                        player.equipment.items[Equipment.WEAPON_SLOT].id,
                        player.equipment.items[Equipment.WEAPON_SLOT].amount
                    )
                    player.equipment.delete(equipItem)
                    player.inventory.add(equipItem)
                }
            }
        }
        equipItem = null
        player.inventory.refreshItems()
        player.equipment.refreshItems()
        PlayerLogs.log(player.username, "Entered a duel against: " + playerToDuel.username)
        for (i in player.dueling.stakedItems) {
            PlayerLogs.log(player.username, "Their staked item: " + i.id + ", amount: " + i.amount)
        }
        for (i in playerToDuel.dueling.stakedItems) {
            PlayerLogs.log(player.username, "Opponent staked item: " + i.id + ", amount: " + i.amount)
        }
        duelingStatus = 5
        timer = 3
        player.movementQueue.reset().isLockedMovement = true
        player.packetSender.sendInterfaceRemoval()
        if (selectedDuelRules[DuelRule.OBSTACLES.ordinal]) {
            if (selectedDuelRules[DuelRule.NO_MOVEMENT.ordinal]) {
                player.moveTo(duelTelePos)
            } else {
                player.moveTo(Position(3366 + Misc.getRandom(12), 3246 + Misc.getRandom(6), 0))
            }
        } else {
            if (selectedDuelRules[DuelRule.NO_MOVEMENT.ordinal]) {
                player.moveTo(duelTelePos)
            } else {
                player.moveTo(Position(3335 + Misc.getRandom(12), 3246 + Misc.getRandom(6), 0))
            }
        }
        player.restart()
        player.packetSender.sendPositionalHint(playerToDuel.position.copy(), 10)
        player.packetSender.sendEntityHint(playerToDuel)
        TaskManager.submit(object : Task(2, player, false) {
            public override fun execute() {
                if (player.location !== Locations.Location.DUEL_ARENA) {
                    player.movementQueue.isLockedMovement = false
                    stop()
                    return
                }
                if (timer == 3 || timer == 2 || timer == 1) player.forceChat("$timer..") else {
                    player.forceChat("FIGHT!!")
                    player.movementQueue.isLockedMovement = false
                    timer = -1
                    stop()
                    return
                }
                timer--
            }
        })
        player.updateFlag.flag(Flag.APPEARANCE)
        BonusManager.update(player)
    }

    fun duelVictory() {
        val refund = player.constitution == 0
        duelingStatus = 6
        player.restart()
        player.movementQueue.reset().isLockedMovement = false
        if (duelingWith > 0) {
            val playerDuel = World.getPlayers()[duelingWith]
            if (playerDuel != null && playerDuel.dueling.stakedItems.size > 0) {
                for (item in playerDuel.dueling.stakedItems) {
                    if (item.id > 0 && item.amount > 0) {
                        if (refund) {
                            PlayerLogs.log(
                                player.username,
                                "Player tied in duel against " + playerDuel.username + ", refunded: " + item.id + ", " + item.amount
                            )
                            playerDuel.inventory.add(item)
                        } else {
                            //PlayerLogs.log(player.getUsername(), "Player won against "+playerDuel.getUsername()+", staked item in duel: "+item.getId()+", "+item.getAmount());
                            stakedItems.add(item)
                        }
                    }
                }
                if (refund) {
                    playerDuel.packetSender.sendMessage("Staked items have been refunded as both duelists died.")
                    player.packetSender.sendMessage("Staked items have been refunded as both duelists died.")
                    PlayerLogs.log(player.username, "Tied in their duel against $playerDuel")
                    PlayerLogs.log(playerDuel.username, "Tied in their duel against " + player.username)
                }
            }
        }
        player.packetSender.sendInterfaceItems(6822, stakedItems)
        player.packetSender.sendString(6840, "" + duelingData[0])
        player.packetSender.sendString(6839, "" + duelingData[1])
        player.session.clearMessages()
        player.moveTo(Position(3368 + Misc.getRandom(5), 3267 + Misc.getRandom(3), 0))
        for (item in stakedItems) {
            if (item.id > 0 && item.amount > 0) {
                player.inventory.add(item)
                PlayerLogs.log(player.username, "Player won THEIR staked item in duel: " + item.id + ", " + item.amount)
            }
        }
        PlayerLogs.log(player.username, "Finished their duel.")
        reset()
        arenaStats[0]++
        player.setEntityInteraction(null)
        player.movementQueue.reset()
        player.packetSender.sendInterface(6733)
        PlayerPanel.refreshPanel(player)
    }

    fun reset() {
        inDuelWith = -1
        duelingStatus = 0
        inDuelScreen = false
        duelRequested = false
        canOffer = false
        for (i in selectedDuelRules.indices) selectedDuelRules[i] = false
        player.trading.canOffer = true
        player.packetSender.sendConfig(286, 0)
        stakedItems.clear()
        if (duelingWith >= 0) {
            val playerToDuel = World.getPlayers()[duelingWith]
            if (playerToDuel != null) {
                player.packetSender.sendInterfaceItems(6670, playerToDuel.dueling.stakedItems)
                playerToDuel.packetSender.sendInterfaceItems(6670, player.dueling.stakedItems)
            }
            player.packetSender.sendInterfaceItems(6669, player.dueling.stakedItems)
        }
        duelingWith = -1
        duelConfig = 0
        duelTelePos = null
        timer = 3
        player.combatBuilder.reset(true)
        player.movementQueue.reset()
        player.packetSender.sendEntityHintRemoval(true)
    }

    fun getCanOffer(): Boolean {
        return canOffer && player.interfaceId == 6575 && !player.isBanking && !player.priceChecker.isOpen
    }

    @JvmField
    var duelingStatus = 0

    @JvmField
    var duelingWith = -1

    @JvmField
    var inDuelScreen = false
    var duelRequested = false

    @JvmField
    var selectedDuelRules = BooleanArray(DuelRule.values().size)

    @JvmField
    var stakedItems = CopyOnWriteArrayList<Item>()

    @JvmField
    var arenaStats = intArrayOf(0, 0)
    var spaceReq = 0
    var duelConfig = 0

    @JvmField
    var timer = 3
    var inDuelWith = -1
    private var canOffer = false
    var duelingData = arrayOfNulls<Any>(2)
    protected var duelTelePos: Position? = null

    enum class DuelRule(val configId: Int, val buttonId: Int, val inventorySpaceReq: Int, val equipmentSlot: Int) {
        NO_RANGED(16, 6725, -1, -1), NO_MELEE(32, 6726, -1, -1), NO_MAGIC(64, 6727, -1, -1), NO_SPECIAL_ATTACKS(
            8192, 7816, -1, -1
        ),
        LOCK_WEAPON(4096, 670, -1, -1), NO_FORFEIT(1, 6721, -1, -1), NO_POTIONS(128, 6728, -1, -1), NO_FOOD(
            256, 6729, -1, -1
        ),
        NO_PRAYER(512, 6730, -1, -1), NO_MOVEMENT(2, 6722, -1, -1), OBSTACLES(1024, 6732, -1, -1), NO_HELM(
            16384, 13813, 1, Equipment.HEAD_SLOT
        ),
        NO_CAPE(32768, 13814, 1, Equipment.CAPE_SLOT), NO_AMULET(65536, 13815, 1, Equipment.AMULET_SLOT), NO_AMMUNITION(
            134217728, 13816, 1, Equipment.AMMUNITION_SLOT
        ),
        NO_WEAPON(131072, 13817, 1, Equipment.WEAPON_SLOT), NO_BODY(262144, 13818, 1, Equipment.BODY_SLOT), NO_SHIELD(
            524288, 13819, 1, Equipment.SHIELD_SLOT
        ),
        NO_LEGS(2097152, 13820, 1, Equipment.LEG_SLOT), NO_RING(67108864, 13821, 1, Equipment.RING_SLOT), NO_BOOTS(
            16777216, 13822, 1, Equipment.FEET_SLOT
        ),
        NO_GLOVES(8388608, 13823, 1, Equipment.HANDS_SLOT);

        override fun toString(): String {
            return Misc.formatText(name.lowercase(Locale.getDefault()))
        }

        companion object {
            @JvmStatic
            fun forId(i: Int): DuelRule? {
                for (r in values()) {
                    if (r.ordinal == i) return r
                }
                return null
            }

            fun forButtonId(buttonId: Int): DuelRule? {
                for (r in values()) {
                    if (r.buttonId == buttonId) return r
                }
                return null
            }
        }
    }

    companion object {
        /**
         * Checks if two players are the only ones in a duel.
         * @param p1    Player1 to check if he's 1/2 player in trade.
         * @param p2    Player2 to check if he's 2/2 player in trade.
         * @return        true if only two people are in the duel.
         */
        fun twoDuelers(p1: Player, p2: Player): Boolean {
            var count = 0
            for (player in World.getPlayers()) {
                if (player == null) continue
                if (player.dueling.inDuelWith == p1.index || player.dueling.inDuelWith == p2.index) {
                    count++
                }
            }
            return count == 2
        }

        @JvmStatic
        fun handleDuelingButtons(player: Player, button: Int): Boolean {
            if (DuelRule.forButtonId(button) != null) {
                DuelRule.forButtonId(button)?.let { player.dueling.selectRule(it) }
                return true
            } else {
                if (player.dueling.duelingWith < 0) return false
                val playerToDuel = World.getPlayers()[player.dueling.duelingWith]
                when (button) {
                    6674 -> {
                        if (!player.dueling.inDuelScreen) return true
                        if (playerToDuel == null) return true
                        if (!checkDuel(player, 1) && !checkDuel(player, 2)) return true
                        if (player.dueling.selectedDuelRules[DuelRule.NO_MELEE.ordinal] && player.dueling.selectedDuelRules[DuelRule.NO_RANGED.ordinal] && player.dueling.selectedDuelRules[DuelRule.NO_MAGIC.ordinal]) {
                            player.packetSender.sendMessage("You won't be able to attack the other player with the current rules.")
                            return true
                        }
                        player.dueling.duelingStatus = 2
                        if (player.dueling.duelingStatus == 2) {
                            player.packetSender.sendString(6684, "Waiting for other player...")
                            playerToDuel.packetSender.sendString(6684, "Other player has accepted.")
                        }
                        if (playerToDuel.dueling.duelingStatus == 2) {
                            playerToDuel.packetSender.sendString(6684, "Waiting for other player...")
                            player.packetSender.sendString(6684, "Other player has accepted.")
                        }
                        if (player.dueling.duelingStatus == 2 && playerToDuel.dueling.duelingStatus == 2) {
                            player.dueling.duelingStatus = 3
                            playerToDuel.dueling.duelingStatus = 3
                            playerToDuel.dueling.confirmDuel()
                            player.dueling.confirmDuel()
                        }
                        return true
                    }
                    6520 -> {
                        if (!player.dueling.inDuelScreen || !checkDuel(player, 3) && !checkDuel(
                                player, 4
                            ) || playerToDuel == null
                        ) return true
                        player.dueling.duelingStatus = 4
                        if (playerToDuel.dueling.duelingStatus == 4 && player.dueling.duelingStatus == 4) {
                            player.dueling.startDuel()
                            playerToDuel.dueling.startDuel()
                        } else {
                            player.packetSender.sendString(6571, "Waiting for other player...")
                            playerToDuel.packetSender.sendString(6571, "Other player has accepted")
                        }
                        return true
                    }
                }
            }
            return false
        }

        @JvmStatic
        fun checkDuel(playerToDuel: Player, statusReq: Int): Boolean {
            val goodInterfaceId =
                playerToDuel.interfaceId == -1 || playerToDuel.interfaceId == 6575 || playerToDuel.interfaceId == 6412
            return if (playerToDuel.dueling.duelingStatus != statusReq || playerToDuel.isBanking || playerToDuel.isShopping || playerToDuel.constitution <= 0 || playerToDuel.isResting || !goodInterfaceId) false else true
        }

        @JvmStatic
        fun checkRule(player: Player, rule: DuelRule): Boolean {
            if (player.location === Locations.Location.DUEL_ARENA && player.dueling.duelingStatus == 5) {
                if (player.dueling.selectedDuelRules[rule.ordinal]) return true
            }
            return false
        }

        const val INTERFACE_REMOVAL_ID = 6669
    }
}