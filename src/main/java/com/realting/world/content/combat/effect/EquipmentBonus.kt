package com.realting.world.content.combat.effect

import com.realting.model.container.impl.Equipment
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player

object EquipmentBonus {
    var helms = intArrayOf(11665, 11664, 11663) //11665 == melee, 11664 == range, 11663 == mage
    var gloves = 8842
    var normRobes = intArrayOf(8839, 8840)
    var eRobes = intArrayOf(19785, 19786)
    var deflector = 19712
    var mace = 8841
    val obsidianMeleeWeapons = intArrayOf(
        6523, 6528, 6527, 6525
    )

    fun berserkerNecklaceEffect(player: Player): Boolean {
        var good = false
        if (player.equipment[Equipment.AMULET_SLOT].id == 11128) {
            for (i in obsidianMeleeWeapons.indices) {
                if (player.equipment[Equipment.WEAPON_SLOT].id == obsidianMeleeWeapons[i]) {
                    good = true
                    break
                }
            }
        }
        return good
    }

    fun slayerMageBonus(player: Player, npc: NPC?): Boolean {
        return player.slayer.isSlayerTask(npc) && (player.equipment.items[Equipment.HEAD_SLOT].id == 15492 || player.equipment.items[Equipment.HEAD_SLOT].id == 15488)
    }

    /*
		HELMS -> 11665 Melee, 11664 Range, 11663 Mage
		DEFLECTOR -> 19712
		BODY -> 8839
		LEGS -> 8840
		GLOVES -> 8842
		ELITE BODY -> 19785
		ELITE LEGS -> 19786
	 */
	@JvmStatic
	fun voidElite(player: Player): Boolean {
        return wearingVoid(player) && player.checkItem(
                Equipment.BODY_SLOT,
                eRobes[0]
            ) && player.checkItem(Equipment.LEG_SLOT, eRobes[1])
    }

    @JvmStatic
	fun voidRange(player: Player): Boolean {
        return wearingVoid(player) && player.checkItem(Equipment.HEAD_SLOT, helms[1])
    }

    @JvmStatic
	fun voidMelee(player: Player): Boolean {
        return wearingVoid(player) && player.checkItem(Equipment.HEAD_SLOT, helms[0])
    }

    @JvmStatic
	fun voidMage(player: Player): Boolean {
        return wearingVoid(player) && player.checkItem(Equipment.HEAD_SLOT, helms[2])
    }

    @JvmStatic
	fun wearingVoid(player: Player): Boolean {
        var hasHelm = false
        var correctEquipment = 0
        if (player.checkItem(Equipment.BODY_SLOT, eRobes[0]) || player.checkItem(Equipment.BODY_SLOT, normRobes[0])) {
            correctEquipment++
        }
        if (player.checkItem(Equipment.LEG_SLOT, eRobes[1]) || player.checkItem(Equipment.LEG_SLOT, normRobes[1])) {
            correctEquipment++
        }
        if (player.checkItem(Equipment.HEAD_SLOT, helms[0]) || player.checkItem(
                Equipment.HEAD_SLOT,
                helms[1]
            ) || player.checkItem(Equipment.HEAD_SLOT, helms[2])
        ) {
            hasHelm = true
            correctEquipment++
        }
        if (player.checkItem(Equipment.SHIELD_SLOT, deflector)) {
            correctEquipment++
        }
        if (player.checkItem(Equipment.HANDS_SLOT, gloves)) {
            correctEquipment++
        }
        if (player.checkItem(Equipment.WEAPON_SLOT, mace)) {
            correctEquipment++
        }
        return correctEquipment > 3 && hasHelm
        //System.out.println("Returned false.");
    }
}