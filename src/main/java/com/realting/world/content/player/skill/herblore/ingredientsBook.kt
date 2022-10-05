package com.realting.world.content.player.skill.herblore

import com.realting.model.Animation
import com.realting.model.entity.character.player.Player

/**
 * Handles the Ingridient's book
 * @author Gabriel Hannason
 */
object ingredientsBook {
    @JvmStatic
    fun readBook(player: Player, pageIndex: Int, interfaceAllowed: Boolean) {
        var pageIndex = pageIndex
        if (player.interfaceId != -1 && !interfaceAllowed) {
            player.packetSender.sendMessage("Please close the interface you have open before opening a new one.")
            return
        }
        if (pageIndex < 0) pageIndex = 0
        if (pageIndex > 10) pageIndex = 12
        player.movementQueue.reset()
        player.performAnimation(Animation(1350))
        player.packetSender.sendString(903, "ingredients")
        for (i in pages[0].indices) player.packetSender.sendString(843 + i, pages[pageIndex][i])
        for (i in pages[1].indices) player.packetSender.sendString(843 + 11 + i, pages[pageIndex + 1][i])
        player.packetSender.sendString(14165, "- $pageIndex - ")
        player.packetSender.sendString(14166, "- " + (pageIndex + 1) + " - ")
        player.packetSender.sendInterface(837)
        player.currentBookPage = pageIndex
    }

    private val pages = arrayOf(
        arrayOf(
            "Lvl 1: Attack Potion",
            "Eye of newt",
            "Guam",
            " ",
            "Lvl 9: Defence Potion",
            "Bear fur",
            "Marrentill",
            " ",
            "Lvl 12: Strength Potion",
            "Limpwurt root",
            "Tarrorim",
            ""
        ), arrayOf(
            "Lvl 13: Antipoison Potion",
            "Unicorn horn dust",
            "Marrentill",
            " ",
            "Lvl 15: Serum 207",
            "Ashes",
            "Tarrorim",
            " ",
            "Lvl 22: Restore Potion",
            "Red spider's eggs",
            "Harralander"
        ), arrayOf(
            "Lvl 26: Energy Potion",
            "Chocolate dust",
            "Harralander",
            " ",
            "Lvl 34: Agility Potion",
            "Toad's legs",
            "Toadflax",
            " ",
            "Lvl 36: Combat Potion",
            "Goat horn dust",
            "Harralander",
            ""
        ), arrayOf(
            "Lvl 38: Prayer Potion",
            "Snape grass",
            "Ranarr",
            " ",
            "Lvl 40: Summoning Potion",
            "Cockatrice egg",
            "Spirit weed",
            " ",
            "Lvl 42: Crafting Potion",
            "Wergali",
            "Frog spawn",
            ""
        ), arrayOf(
            "Lvl 45: Super Attack",
            "Eye of newt",
            "Irit",
            " ",
            "Lvl 48: Super Antipoison",
            "Unicorn horn dust",
            "Irit",
            " ",
            "Lvl 50: Fishing Potion",
            "Snape grass",
            "Avantoe",
            ""
        ), arrayOf(
            "Lvl 53: Hunter Potion",
            "Kebbit teeth dust",
            "Avantoe",
            " ",
            "Lvl 55: Super Strength",
            "Limpwurt root",
            "Kwuarm",
            " ",
            "Lvl 58: Fletching Potion",
            "Wimpy feather",
            "Wergali",
            ""
        ), arrayOf(
            "Lvl 60: Weapon Poison",
            "Dragon scale dust",
            "Kwuarm",
            " ",
            "Lvl 63: Super Restore",
            "Red spider's eggs",
            "Snapdragon",
            " ",
            "Lvl 66: Super Defence",
            "White berries",
            "Cadantine",
            ""
        ), arrayOf(
            "Lvl 68: Antipoison+",
            "Yew roots",
            "Toadflax",
            " ",
            "Lvl 69: Antifire",
            "Dragon scale dust",
            "Lantadyme",
            " ",
            "Lvl 72: Ranging Potion",
            "Wine of Zamorak",
            "Dwarf weed",
            ""
        ), arrayOf(
            "Lvl 76: Magic Potion",
            "Potato cactus",
            "Lantadyme",
            " ",
            "Lvl 78: Zamorak Brew",
            "Jangerberries",
            "Torstol",
            " ",
            "Lvl 81: Saradomin Brew",
            "Crushed bird nest",
            "Toadflax",
            ""
        ), arrayOf(
            "Lvl 84: Restore Special",
            "Super energy(3)",
            "Papaya",
            " ",
            "Lvl 85: Super Antifire",
            "Antifire(3)",
            "Phoenix feather",
            " ",
            "Lvl 88: Extreme Attack",
            "Super Attack(3)",
            "Avantoe",
            ""
        ), arrayOf(
            "Lvl 89: Extreme Strength",
            "Super Strength(3)",
            "Dwarf weed",
            " ",
            "Lvl 90: Extreme Defence",
            "Super Defence(3)",
            "Lantadyme",
            " ",
            "Lvl 91: Extreme Magic",
            "Magic Potion (3)",
            "Ground mud runes",
            ""
        ), arrayOf(
            "Lvl 92: Extreme Ranging",
            "Ranging Potion (3)",
            "5 Grenwall Spikes",
            " ",
            "Lvl 94: Prayer Renewal",
            "Morchella mushroom",
            "Fellstalk",
            " ",
            "",
            "",
            "",
            "",
            ""
        ), arrayOf(
            "Lvl 96: Overload Potion",
            "Extreme Attack(3)",
            "Extreme Strength(3)",
            "Extreme Defence(3)",
            "Extreme Ranging(3)",
            "Extreme Magic(3)",
            "",
            "",
            "",
            "",
            "",
            ""
        ), arrayOf(
            "", "", "", "", "", "", " ", " ", " ", " ", " ", " ", "", ""
        )
    )
}