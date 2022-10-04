package com.realting.world.content.skill.crafting

import com.realting.engine.task.Task
import com.realting.model.Skill
import com.realting.engine.task.TaskManager
import com.realting.model.Animation
import com.realting.model.Item
import com.realting.model.Items
import com.realting.model.definitions.ItemDefinition
import com.realting.model.entity.character.player.Player
import java.util.Locale
import com.realting.util.Misc

object Jewelry {
    @JvmStatic
    fun stringAmulet(player: Player, itemUsed: Int, usedWith: Int) {
        val amuletId = if (itemUsed == 1759) usedWith else itemUsed
        if (!player.inventory.contains(1759)) {
            player.packetSender.sendMessage(
                "You need a ball of wool in order to string your " + ItemDefinition.forId(
                    amuletId
                ).name.lowercase(Locale.getDefault()) + "."
            )
            return
        }
        if (!player.inventory.contains(amuletId)) {
            player.packetSender.sendMessage("You need an amulet to utilize your ball of wool.")
            return
        }
        for (a in AmuletData.values()) {
            if (amuletId == a.amuletId) {
                player.inventory.delete(1759, 1)
                player.inventory.delete(amuletId, 1)
                player.inventory.add(a.product, 1)
                player.skillManager.addExperience(Skill.CRAFTING, 4)
            }
        }
    }

    @JvmStatic
    fun jewelryMaking(player: Player, type: String?, itemId: Int, amount: Int) {
        when (type) {
            "RING" -> {
                var i = 0
                while (i < JewelryData.RINGS.item.size) {
                    if (itemId == JewelryData.RINGS.item[i][1]) {
                        mouldJewelry(
                            player,
                            JewelryData.RINGS.item[i][0],
                            itemId,
                            amount,
                            JewelryData.RINGS.item[i][2],
                            JewelryData.RINGS.item[i][3]
                        )
                    }
                    i++
                }
            }
            "NECKLACE" -> {
                var i = 0
                while (i < JewelryData.NECKLACE.item.size) {
                    if (itemId == JewelryData.NECKLACE.item[i][1]) {
                        mouldJewelry(
                            player,
                            JewelryData.NECKLACE.item[i][0],
                            itemId,
                            amount,
                            JewelryData.NECKLACE.item[i][2],
                            JewelryData.NECKLACE.item[i][3]
                        )
                    }
                    i++
                }
            }
            "AMULET" -> {
                var i = 0
                while (i < JewelryData.AMULETS.item.size) {
                    if (itemId == JewelryData.AMULETS.item[i][1]) {
                        mouldJewelry(
                            player,
                            JewelryData.AMULETS.item[i][0],
                            itemId,
                            amount,
                            JewelryData.AMULETS.item[i][2],
                            JewelryData.AMULETS.item[i][3]
                        )
                    }
                    i++
                }
            }
            "BRACELET" -> {
                var i = 0
                while (i < JewelryData.BRACELETS.item.size) {
                    if (itemId == JewelryData.BRACELETS.item[i][1]) {
                        mouldJewelry(
                            player,
                            JewelryData.BRACELETS.item[i][0],
                            itemId,
                            amount,
                            JewelryData.BRACELETS.item[i][2],
                            JewelryData.BRACELETS.item[i][3]
                        )
                    }
                    i++
                }
            }
        }
    }

    private fun mouldJewelry(player: Player, required: Int, itemId: Int, amount: Int, level: Int, xp: Int) {
        if (player.interfaceId != 18875) {
            return
        }
        player.packetSender.sendInterfaceRemoval()
        if (player.skillManager.getCurrentLevel(Skill.CRAFTING) < level) {
            player.packetSender.sendMessage("You need a Crafting level of at least $level to mould this.")
            return
        }
        if (!player.inventory.contains(2357)) {
            player.packetSender.sendMessage("You need a gold bar to mould this item.")
            return
        }
        if (!player.inventory.contains(required)) {
            player.packetSender.sendMessage(
                "You need " + Misc.anOrA(ItemDefinition.forId(required).name) + " " + ItemDefinition.forId(required).name.lowercase(
                    Locale.getDefault()
                ) + " to mould this item."
            )
            return
        }
        player.currentTask = object : Task(2, player, true) {
            var toMake = amount
            public override fun execute() {
                if (!player.inventory.contains(2357) || !player.inventory.contains(required)) {
                    player.packetSender.sendMessage("You have run out of materials.")
                    stop()
                    return
                }
                if (required != 2357) {
                    player.inventory.delete(2357, 1)
                }
                player.inventory.delete(required, 1).add(itemId, 1)
                player.skillManager.addExperience(Skill.CRAFTING, xp)
                player.performAnimation(Animation(896))
                toMake--
                if (toMake <= 0) {
                    stop()
                    return
                }
            }
        }
        TaskManager.submit(player.currentTask)
    }

    private fun getItems(jewelry: Array<IntArray>): Array<Item?> {
        val items = arrayOfNulls<Item>(jewelry.size)
        for (index in jewelry.indices) {
            items[index] = Item(jewelry[index][1], 1)
        }
        return items
    }

    @JvmStatic
    fun jewelryInterface(player: Player) {
        if (player.inventory.contains(1592)) {
            player.packetSender.sendItemContainer(getItems(JewelryData.RINGS.item), 4233)
            player.packetSender.sendInterfaceModel(4229, -1, 0)
        } else {
            player.packetSender.sendItemContainer(arrayOfNulls(8), 4233)
            player.packetSender.sendInterfaceModel(4229, 1592, 120)
        }
        if (player.inventory.contains(1597)) {
            player.packetSender.sendItemContainer(getItems(JewelryData.NECKLACE.item), 4239)
            player.packetSender.sendInterfaceModel(4235, -1, 0)
        } else {
            player.packetSender.sendItemContainer(arrayOfNulls(8), 4239)
            player.packetSender.sendInterfaceModel(4235, 1597, 120)
        }
        if (player.inventory.contains(1595)) {
            player.packetSender.sendItemContainer(getItems(JewelryData.AMULETS.item), 4245)
            player.packetSender.sendInterfaceModel(4241, -1, 0)
        } else {
            player.packetSender.sendItemContainer(arrayOfNulls(8), 4245)
            player.packetSender.sendInterfaceModel(4241, 1595, 120)
        }
        if (player.inventory.contains(Items.BRACELET_MOULD)) {
            player.packetSender.sendItemContainer(getItems(JewelryData.BRACELETS.item), 18796)
            player.packetSender.sendInterfaceModel(18790, -1, 0)
        } else {
            player.packetSender.sendItemContainer(arrayOfNulls(8), 18796)
            player.packetSender.sendInterfaceModel(18790, Items.BRACELET_MOULD, 120)
        }
        player.packetSender.sendInterface(18875)
    }

    enum class JewelryData(var item: Array<IntArray>) {
        RINGS(
            arrayOf(
                intArrayOf(2357, 1635, 5, 15),
                intArrayOf(1607, 1637, 20, 40),
                intArrayOf(1605, 1639, 27, 55),
                intArrayOf(1603, 1641, 34, 70),
                intArrayOf(1601, 1643, 43, 85),
                intArrayOf(1615, 1645, 55, 100),
                intArrayOf(6573, 6575, 67, 115),
                intArrayOf(
                    Items.ZENYTE, Items.ZENYTE_RING, 89, 150
                )
            )
        ),
        NECKLACE(
            arrayOf(
                intArrayOf(2357, 1654, 6, 20),
                intArrayOf(1607, 1656, 22, 55),
                intArrayOf(1605, 1658, 29, 60),
                intArrayOf(1603, 1660, 40, 75),
                intArrayOf(1601, 1662, 56, 90),
                intArrayOf(1615, 1664, 72, 105),
                intArrayOf(6573, 6577, 82, 120),
                intArrayOf(
                    Items.ZENYTE, Items.ZENYTE_NECKLACE, 92, 165
                )
            )
        ),
        AMULETS(
            arrayOf(
                intArrayOf(2357, 1673, 8, 30),
                intArrayOf(1607, 1675, 24, 65),
                intArrayOf(1605, 1677, 31, 70),
                intArrayOf(1603, 1679, 50, 85),
                intArrayOf(1601, 1681, 70, 100),
                intArrayOf(1615, 1683, 80, 150),
                intArrayOf(6573, 6579, 90, 165),
                intArrayOf(
                    Items.ZENYTE, Items.ZENYTE_AMULET_U, 92, 165
                )
            )
        ),
        BRACELETS(
            arrayOf(
                intArrayOf(Items.GOLD_BAR, Items.GOLD_BRACELET, 7, 25), intArrayOf(
                    Items.SAPPHIRE, Items.SAPPHIRE_BRACELET, 23, 60
                ), intArrayOf(Items.EMERALD, Items.EMERALD_BRACELET, 30, 65), intArrayOf(
                    Items.RUBY, Items.RUBY_BRACELET, 42, 80
                ), intArrayOf(Items.DIAMOND, Items.DIAMOND_BRACELET, 58, 95), intArrayOf(
                    Items.DRAGONSTONE, Items.DRAGON_BRACELET, 74, 110
                ), intArrayOf(Items.ONYX, Items.ONYX_BRACELET, 84, 125), intArrayOf(
                    Items.ZENYTE, Items.ZENYTE_BRACELET, 95, 180
                )
            )
        );
    }

    enum class AmuletData(val amuletId: Int, val product: Int) {
        GOLD(1673, 1692), SAPPHIRE(1675, 1694), EMERALD(1677, 1696), RUBY(1679, 1698), DIAMOND(1681, 1700), DRAGONSTONE(
            1683, 1702
        ),
        ONYX(6579, 6581), ZENYTE(
            Items.ZENYTE_AMULET_U, Items.ZENYTE_AMULET
        );

    }
}