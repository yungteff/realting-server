package com.realting.world.content.skill.runecrafting

import com.realting.model.entity.character.player.Player

object RunecraftingPouches {
    private const val RUNE_ESS = 1436
    private const val PURE_ESS = 7936

    @JvmStatic
    fun fill(p: Player, pouch: RunecraftingPouch) {
        if (p.interfaceId > 0) {
            p.packetSender.sendMessage("Please close the interface you have open before doing this.")
            return
        }
        var rEss = p.inventory.getAmount(RUNE_ESS)
        var pEss = p.inventory.getAmount(PURE_ESS)
        if (rEss == 0 && pEss == 0) {
            p.packetSender.sendMessage("You do not have any essence in your inventory.")
            return
        }
        rEss = if (rEss > pouch.maxRuneEss) pouch.maxRuneEss else rEss
        pEss = if (pEss > pouch.maxPureEss) pouch.maxPureEss else pEss
        var stored = 0
        if (p.storedRuneEssence >= pouch.maxRuneEss) p.packetSender.sendMessage("Your pouch can not hold any more Rune essence.")
        if (p.storedPureEssence >= pouch.maxPureEss) p.packetSender.sendMessage("Your pouch can not hold any more Pure essence.")
        while (rEss > 0 && p.storedRuneEssence < pouch.maxRuneEss && p.inventory.contains(RUNE_ESS)) {
            p.inventory.delete(RUNE_ESS, 1)
            p.storedRuneEssence = p.storedRuneEssence + 1
            stored++
        }
        while (pEss > 0 && p.storedPureEssence < pouch.maxPureEss && p.inventory.contains(PURE_ESS)) {
            p.inventory.delete(PURE_ESS, 1)
            p.storedPureEssence = p.storedPureEssence + 1
            stored++
        }
        if (stored > 0) p.packetSender.sendMessage("You fill your pouch with $stored essence..")
    }

    @JvmStatic
    fun empty(p: Player, pouch: RunecraftingPouch?) {
        if (p.interfaceId > 0) {
            p.packetSender.sendMessage("Please close the interface you have open before doing this.")
            return
        }
        while (p.storedRuneEssence > 0 && p.inventory.freeSlots > 0) {
            p.inventory.add(RUNE_ESS, 1)
            p.storedRuneEssence = p.storedRuneEssence - 1
        }
        while (p.storedPureEssence > 0 && p.inventory.freeSlots > 0) {
            p.inventory.add(PURE_ESS, 1)
            p.storedPureEssence = p.storedPureEssence - 1
        }
    }

    @JvmStatic
    fun check(p: Player, pouch: RunecraftingPouch) {
        p.packetSender.sendMessage("Your pouch currently contains " + p.storedPureEssence + "/" + pouch.maxPureEss + " Pure essence, and " + p.storedRuneEssence + "/" + pouch.maxRuneEss + " Rune essence.")
    }

    enum class RunecraftingPouch(private val id: Int, val maxRuneEss: Int, val maxPureEss: Int) {
        SMALL(5509, 3, 3), MEDIUM_POUCH(5510, 9, 9), LARGE_POUCH(5512, 18, 18), GIANT_POUCH(5514, 30, 30);

        companion object {
            @JvmStatic
            fun forId(id: Int): RunecraftingPouch? {
                for (pouch in values()) {
                    if (pouch.id == id) return pouch
                }
                return null
            }
        }
    }
}