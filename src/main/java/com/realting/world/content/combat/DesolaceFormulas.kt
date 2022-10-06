package com.realting.world.content.combat

import com.realting.model.Graphic
import com.realting.model.Skill
import com.realting.model.container.impl.Equipment
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import com.realting.world.content.combat.effect.EquipmentBonus
import com.realting.world.content.combat.prayer.CurseHandler
import com.realting.world.content.combat.prayer.PrayerHandler
import com.realting.world.content.combat.weapon.FightStyle
import java.util.*
import kotlin.math.floor

object DesolaceFormulas {
    /*==============================================================================*/ /*===================================MELEE=====================================*/
    @JvmStatic
    fun getMeleeMaxHit(entity: CharacterEntity, victim: CharacterEntity): Int {
        var maxHit = 0.0
        if (entity.isNpc) {
            val npc = entity as NPC
            maxHit = npc.definition.maxHit.toDouble()
            when {
                npc.strengthWeakened[0] -> {
                    maxHit -= (0.10 * maxHit)
                }
                npc.strengthWeakened[1] -> {
                    maxHit -= (0.20 * maxHit)
                }
                npc.strengthWeakened[2] -> {
                    maxHit -= (0.30 * maxHit)
                }
            }
            /** CUSTOM NPCS  */
            if (npc.id == 2026) { //Dharok the wretched
                maxHit += ((npc.defaultConstitution - npc.constitution) * 0.2)
            }
        } else {
            val plr = entity as Player
            var base = 0.0
            val effective = getEffectiveStr(plr)
            var specialBonus = 1.0
            if (plr.isSpecialActivated) {
                specialBonus = plr.combatSpecial.strengthBonus
            }
            val strengthBonus = plr.bonusManager.otherBonus[0]
            base = (13 + effective + strengthBonus / 8 + effective * strengthBonus / 65) / 11
            if (plr.equipment.items[3].id == 4718 && plr.equipment.items[0].id == 4716 && plr.equipment.items[4].id == 4720 && plr.equipment.items[7].id == 4722) base += (plr.skillManager.getMaxLevel(
                Skill.CONSTITUTION
            ) - plr.constitution) * .045 + 1
            if (specialBonus > 1) base *= specialBonus
            /*if (hasObsidianEffect(plr))// || EquipmentBonus.wearingVoid(plr))//, CombatType.MELEE))
				base = (base * 1.2);*/if (victim.isNpc) {
                val npc = victim as NPC
                if (npc.defenceWeakened[0]) {
                    base += (0.10 * base).toInt().toDouble()
                } else if (npc.defenceWeakened[1]) {
                    base += (0.20 * base).toInt().toDouble()
                } else if (npc.defenceWeakened[2]) {
                    base += (0.30 * base).toInt().toDouble()
                }
                /** SLAYER HELMET  */
                if (plr.slayer.isSlayerTask(npc)) {
                    if (plr.equipment.items[Equipment.HEAD_SLOT].id == 13263) {
                        base *= 1.12
                    }
                }
            }
            maxHit = 10.let { base *= it; base }
        }
        if (victim.isPlayer) {
            val p = victim as Player
            if (p.hasStaffOfLightEffect()) {
                maxHit /= 2
                p.performGraphic(Graphic(2319))
            }
        }
        return floor(maxHit).toInt()
    }

    /**
     * Calculates a player's Melee attack level (how likely that they're going to hit through defence)
     * @param plr    The player's Meelee attack level
     * @return        The player's Melee attack level
     */
    @JvmStatic
    fun getMeleeAttack(plr: Player): Int {
        var attackLevel = plr.skillManager.getCurrentLevel(Skill.ATTACK)
        when (plr.fightType.style) {
            FightStyle.AGGRESSIVE -> attackLevel += 3
            FightStyle.CONTROLLED -> attackLevel += 1
            else -> {}
        }
        //boolean hasVoid = EquipmentBonus.wearingVoid(plr);//, CombatType.MELEE);
        when {
            PrayerHandler.isActivated(plr, PrayerHandler.CLARITY_OF_THOUGHT) -> {
                attackLevel += (plr.skillManager.getMaxLevel(Skill.ATTACK) * 0.05).toInt()
            }
            PrayerHandler.isActivated(plr, PrayerHandler.IMPROVED_REFLEXES) -> {
                attackLevel += (plr.skillManager.getMaxLevel(Skill.ATTACK) * 0.1).toInt()
            }
            PrayerHandler.isActivated(plr, PrayerHandler.INCREDIBLE_REFLEXES) -> {
                attackLevel += (plr.skillManager.getMaxLevel(Skill.ATTACK) * 0.15).toInt()
            }
            PrayerHandler.isActivated(plr, PrayerHandler.CHIVALRY) -> {
                attackLevel += (plr.skillManager.getMaxLevel(Skill.ATTACK) * 0.15).toInt()
            }
            PrayerHandler.isActivated(plr, PrayerHandler.PIETY) -> {
                attackLevel += (plr.skillManager.getMaxLevel(Skill.ATTACK) * 0.2).toInt()
            }
            CurseHandler.isActivated(plr, CurseHandler.LEECH_ATTACK) -> {
                attackLevel += (plr.skillManager.getMaxLevel(Skill.ATTACK) * 0.05 + plr.leechedBonuses[2]).toInt()
            }
            CurseHandler.isActivated(plr, CurseHandler.TURMOIL) -> {
                attackLevel += (plr.skillManager.getMaxLevel(Skill.ATTACK) * 0.3 + plr.leechedBonuses[2]).toInt()
            }
        }
        if (EquipmentBonus.voidMelee(plr)) {
            attackLevel += (plr.skillManager.getMaxLevel(Skill.ATTACK) * 0.1).toInt()
            if (plr.currentClanChat != null && plr.currentClanChat.name.equals("Debug", ignoreCase = true)) {
                plr.packetSender.sendMessage("Void Melee accuracy buff applied. Is $attackLevel, pre-equip bonuses")
            }
        }
        attackLevel *= if (plr.isSpecialActivated) plr.combatSpecial.accuracyBonus.toInt() else 1
        val i = plr.bonusManager.attackBonus[bestMeleeAtk(plr)].toInt()

        //System.out.println("hello world");
        return (attackLevel + attackLevel * 0.15 + (i + i * 0.04)).toInt()
    }

    /**
     * Calculates a player's Melee Defence level
     * @param plr        The player to calculate Melee defence for
     * @return        The player's Melee defence level
     */
    @JvmStatic
    fun getMeleeDefence(plr: Player): Int {
        var defenceLevel = plr.skillManager.getCurrentLevel(Skill.DEFENCE)
        val i = plr.bonusManager.defenceBonus[bestMeleeDef(plr)].toInt()
        when {
            plr.prayerActive[PrayerHandler.THICK_SKIN] -> {
                defenceLevel += (plr.skillManager.getMaxLevel(Skill.DEFENCE) * 0.05).toInt()
            }
            plr.prayerActive[PrayerHandler.ROCK_SKIN] -> {
                defenceLevel += (plr.skillManager.getMaxLevel(Skill.DEFENCE) * 0.1).toInt()
            }
            plr.prayerActive[PrayerHandler.STEEL_SKIN] -> {
                defenceLevel += (plr.skillManager.getMaxLevel(Skill.DEFENCE) * 0.15).toInt()
            }
            plr.prayerActive[PrayerHandler.CHIVALRY] -> {
                defenceLevel += (plr.skillManager.getMaxLevel(Skill.DEFENCE) * 0.2).toInt()
            }
            plr.prayerActive[PrayerHandler.PIETY] -> {
                defenceLevel += (plr.skillManager.getMaxLevel(Skill.DEFENCE) * 0.25).toInt()
            }
            plr.prayerActive[PrayerHandler.RIGOUR] -> {
                defenceLevel += (plr.skillManager.getMaxLevel(Skill.DEFENCE) * 0.25).toInt()
            }
            plr.prayerActive[PrayerHandler.AUGURY] -> {
                defenceLevel += (plr.skillManager.getMaxLevel(Skill.DEFENCE) * 0.25).toInt()
            }
            plr.curseActive[CurseHandler.TURMOIL] -> { // turmoil
                defenceLevel += (plr.skillManager.getMaxLevel(Skill.DEFENCE) * 0.15).toInt()
            }
        }
        return (defenceLevel + defenceLevel * 0.15 + (i + i * 0.05)).toInt()
    }

    fun bestMeleeDef(p: Player): Int {
        if (p.bonusManager.defenceBonus[0] > p.bonusManager.defenceBonus[1] && p.bonusManager.defenceBonus[0] > p.bonusManager.defenceBonus[2]) {
            return 0
        }
        if (p.bonusManager.defenceBonus[1] > p.bonusManager.defenceBonus[0] && p.bonusManager.defenceBonus[1] > p.bonusManager.defenceBonus[2]) {
            return 1
        }
        return if (p.bonusManager.defenceBonus[2] <= p.bonusManager.defenceBonus[0] || p.bonusManager.defenceBonus[2] <= p.bonusManager.defenceBonus[1]) 0 else 2
    }

    fun bestMeleeAtk(p: Player): Int {
        if (p.bonusManager.attackBonus[0] > p.bonusManager.attackBonus[1] && p.bonusManager.attackBonus[0] > p.bonusManager.attackBonus[2]) {
            return 0
        }
        if (p.bonusManager.attackBonus[1] > p.bonusManager.attackBonus[0] && p.bonusManager.attackBonus[1] > p.bonusManager.attackBonus[2]) {
            return 1
        }
        return if (p.bonusManager.attackBonus[2] <= p.bonusManager.attackBonus[1] || p.bonusManager.attackBonus[2] <= p.bonusManager.attackBonus[0]) 0 else 2
    }

    /**
     * Obsidian items
     */
    fun getStyleBonus(plr: Player): Int {
        when (plr.fightType.style) {
            FightStyle.AGGRESSIVE, FightStyle.ACCURATE -> return 3
            FightStyle.CONTROLLED -> return 1
            else -> {}
        }
        return 0
    }

    fun getEffectiveStr(plr: Player): Double {
        return plr.skillManager.getCurrentLevel(Skill.STRENGTH) * getPrayerStr(plr) + getStyleBonus(plr)
    }

    fun getPrayerStr(plr: Player): Double {
        if (plr.prayerActive[1] || plr.curseActive[CurseHandler.LEECH_STRENGTH]) return 1.05 else if (plr.prayerActive[6]) return 1.1 else if (plr.prayerActive[14]) return 1.15 else if (plr.prayerActive[24]) return 1.18 else if (plr.prayerActive[25]) return 1.23 else if (plr.curseActive[CurseHandler.TURMOIL]) return 1.24
        return 1.0
    }

    /**
     * Calculates a player's Ranged attack (level).
     * Credits: Dexter Morgan
     * @param plr    The player to calculate Ranged attack level for
     * @return        The player's Ranged attack level
     */
    @JvmStatic
    fun getRangedAttack(plr: Player): Int {
        var rangeLevel = plr.skillManager.getCurrentLevel(Skill.RANGED)
        //boolean hasVoid = EquipmentBonus.wearingVoid(plr);//, CombatType.RANGED);
        val accuracy: Double = if (plr.isSpecialActivated) plr.combatSpecial.accuracyBonus else 1.0
        rangeLevel *= accuracy.toInt()
        when {
            plr.curseActive[PrayerHandler.SHARP_EYE] || plr.curseActive[CurseHandler.SAP_RANGER] -> {
                rangeLevel *= 1.05.toInt()
            }
            plr.prayerActive[PrayerHandler.HAWK_EYE] -> {
                rangeLevel *= 1.10.toInt()
            }
            plr.prayerActive[PrayerHandler.EAGLE_EYE] -> {
                rangeLevel *= 1.15.toInt()
            }
            plr.prayerActive[PrayerHandler.RIGOUR] -> {
                rangeLevel *= 1.22.toInt()
            }
            plr.curseActive[CurseHandler.LEECH_RANGED] -> {
                rangeLevel *= 1.10.toInt()
            }
        }
        if (EquipmentBonus.voidRange(plr)) {
            rangeLevel *= 1.10.toInt()
            if (plr.currentClanChat != null && plr.currentClanChat.name.equals("Debug", ignoreCase = true)) {
                plr.packetSender.sendMessage("Void Range accuracy buff applied. Is $rangeLevel, pre-equip bonuses")
            }
        }
        /*if (hasVoid && accuracy > 1.15)
			rangeLevel *= 1.68;*/
        /*
		 * Slay helm
		 *
		if(plr.getAdvancedSkills().getSlayer().getSlayerTask() != null && plr.getEquipment().getItems()[Equipment.HEAD_SLOT].getId() == 15492) {
			if(plr.getCombatAttributes().getCurrentEnemy() != null && plr.getCombatAttributes().getCurrentEnemy().isNpc()) {
				NPC n = (NPC)plr.getCombatAttributes().getCurrentEnemy();
				if(n != null && n.getId() == plr.getAdvancedSkills().getSlayer().getSlayerTask().getNpcId())
					rangeLevel *= 1.12;
			}
		}*/return (rangeLevel + plr.bonusManager.attackBonus[4] * 2).toInt()
    }

    /**
     * Calculates a player's Ranged defence level.
     * @param plr        The player to calculate the Ranged defence level for
     * @return            The player's Ranged defence level
     */
    @JvmStatic
    fun getRangedDefence(plr: Player): Int {
        var defenceLevel = plr.skillManager.getCurrentLevel(Skill.DEFENCE)
        when {
            plr.prayerActive[PrayerHandler.THICK_SKIN] -> {
                defenceLevel += (plr.skillManager.getMaxLevel(Skill.DEFENCE) * 0.05).toInt()
            }
            plr.prayerActive[PrayerHandler.ROCK_SKIN] -> {
                defenceLevel += (plr.skillManager.getMaxLevel(Skill.DEFENCE) * 0.1).toInt()
            }
            plr.prayerActive[PrayerHandler.STEEL_SKIN] -> {
                defenceLevel += (plr.skillManager.getMaxLevel(Skill.DEFENCE) * 0.15).toInt()
            }
            plr.prayerActive[PrayerHandler.CHIVALRY] -> {
                defenceLevel += (plr.skillManager.getMaxLevel(Skill.DEFENCE) * 0.2).toInt()
            }
            plr.prayerActive[PrayerHandler.PIETY] -> {
                defenceLevel += (plr.skillManager.getMaxLevel(Skill.DEFENCE) * 0.25).toInt()
            }
            plr.prayerActive[PrayerHandler.RIGOUR] -> {
                defenceLevel += (plr.skillManager.getMaxLevel(Skill.DEFENCE) * 0.25).toInt()
            }
            plr.prayerActive[PrayerHandler.AUGURY] -> {
                defenceLevel += (plr.skillManager.getMaxLevel(Skill.DEFENCE) * 0.25).toInt()
            }
            plr.curseActive[CurseHandler.TURMOIL] -> { // turmoil
                defenceLevel += (plr.skillManager.getMaxLevel(Skill.DEFENCE) * 0.20 + plr.leechedBonuses[0]).toInt()
            }
        }
        return (defenceLevel + plr.bonusManager.defenceBonus[4] + plr.bonusManager.defenceBonus[4] / 2).toInt()
    }

    @JvmStatic
    fun getMagicAttack(plr: Player): Int {
        //boolean voidEquipment = EquipmentBonus.wearingVoid(plr);//, CombatType.MAGIC);
        var attackLevel = plr.skillManager.getCurrentLevel(Skill.MAGIC)
        attackLevel *= if (plr.isSpecialActivated) plr.combatSpecial.accuracyBonus.toInt() else 1
        when {
            plr.prayerActive[PrayerHandler.MYSTIC_WILL] || plr.curseActive[CurseHandler.SAP_MAGE] -> {
                attackLevel *= 1.05.toInt()
            }
            plr.prayerActive[PrayerHandler.MYSTIC_LORE] -> {
                attackLevel *= 1.10.toInt()
            }
            plr.prayerActive[PrayerHandler.MYSTIC_MIGHT] -> {
                attackLevel *= 1.15.toInt()
            }
            plr.prayerActive[PrayerHandler.AUGURY] -> {
                attackLevel *= 1.22.toInt()
            }
            plr.curseActive[CurseHandler.LEECH_MAGIC] -> {
                attackLevel *= 1.18.toInt()
            }
        }
        if (EquipmentBonus.voidMage(plr)) {
            attackLevel *= 1.3.toInt()
            if (plr.currentClanChat != null && plr.currentClanChat.name.equals("Debug", ignoreCase = true)) {
                plr.packetSender.sendMessage("Void Mage accuracy buff applied. Is $attackLevel, pre-equip bonuses")
            }
        }
        return (attackLevel + plr.bonusManager.attackBonus[3] * 2).toInt()
    }

    /**
     * Calculates a player's magic defence level
     * @param player            The player to calculate magic defence level for
     * @return            The player's magic defence level
     */
    @JvmStatic
    fun getMagicDefence(plr: Player): Int {
        var defenceLevel =
            plr.skillManager.getCurrentLevel(Skill.DEFENCE) / 2 + plr.skillManager.getCurrentLevel(Skill.MAGIC) / 2
        when {
            plr.prayerActive[PrayerHandler.THICK_SKIN] -> {
                defenceLevel += (plr.skillManager.getMaxLevel(Skill.DEFENCE) * 0.05).toInt()
            }
            plr.prayerActive[PrayerHandler.ROCK_SKIN] -> {
                defenceLevel += (plr.skillManager.getMaxLevel(Skill.DEFENCE) * 0.1).toInt()
            }
            plr.prayerActive[PrayerHandler.STEEL_SKIN] -> {
                defenceLevel += (plr.skillManager.getMaxLevel(Skill.DEFENCE) * 0.15).toInt()
            }
            plr.prayerActive[PrayerHandler.CHIVALRY] -> {
                defenceLevel += (plr.skillManager.getMaxLevel(Skill.DEFENCE) * 0.2).toInt()
            }
            plr.prayerActive[PrayerHandler.PIETY] -> {
                defenceLevel += (plr.skillManager.getMaxLevel(Skill.DEFENCE) * 0.25).toInt()
            }
            plr.prayerActive[PrayerHandler.RIGOUR] -> {
                defenceLevel += (plr.skillManager.getMaxLevel(Skill.DEFENCE) * 0.25).toInt()
            }
            plr.prayerActive[PrayerHandler.AUGURY] -> {
                defenceLevel += (plr.skillManager.getMaxLevel(Skill.DEFENCE) * 0.25).toInt()
            }
            plr.curseActive[CurseHandler.TURMOIL] -> { // turmoil
                defenceLevel += (plr.skillManager.getMaxLevel(Skill.DEFENCE) * 0.20 + plr.leechedBonuses[0]).toInt()
            }
        }
        return (defenceLevel + plr.bonusManager.defenceBonus[3] + plr.bonusManager.defenceBonus[3] / 3).toInt()
    }

    /**
     * Calculates a player's magic max hit
     * @param player            The player to calculate magic max hit for
     * @return            The player's magic max hit damage
     */
    @JvmStatic
    fun getMagicMaxHit(c: CharacterEntity): Int {
        var damage = 0
        val spell = c.currentlyCasting
        if (spell != null) {
            if (spell.maximumHit() > 0) damage += spell.maximumHit() else {
                damage = if (c.isNpc) {
                    (c as NPC).definition.maxHit
                } else {
                    1
                }
            }
        }
        if (c.isNpc) {
            if (spell == null) {
                damage = Misc.getRandom((c as NPC).definition.maxHit)
            }
            return damage
        }
        val p = c as Player
        var damageMultiplier = 1.0
        when (p.equipment.items[Equipment.WEAPON_SLOT].id) {
            4675, 6914, 15246 -> damageMultiplier += .10
            18355 -> damageMultiplier += .20
        }
        val specialAttack = p.isSpecialActivated
        var maxHit = -1
        if (specialAttack) {
            when (p.equipment.items[Equipment.WEAPON_SLOT].id) {
                19780 -> {
                    maxHit = 750
                    damage = maxHit
                }
                11730 -> {
                    maxHit = 310
                    damage = maxHit
                }
            }
        } else {
            damageMultiplier += 0.25
        }
        if (p.equipment.items[Equipment.AMULET_SLOT].id == 18335) {
            damageMultiplier += .10
        }
        damage *= damageMultiplier.toInt()
        if (maxHit > 0) {
            if (damage > maxHit) {
                damage = maxHit
            }
        }
        return damage
    }


    //TODO::attack delays
//    fun getAttackDelay(plr: Player): Int {
//        val id = plr.equipment.items[Equipment.WEAPON_SLOT].id
//        val s = ItemDefinition.forId(id).name.lowercase(Locale.getDefault())
//        if (id == -1) return 4 // unarmed
//        if (id == 18357 || id == 14684) return 4
//        val rangedData = plr.rangedWeaponData
//        if (rangedData != null) {
//            var speed = rangedData.type.attackDelay
//            if (plr.fightType == FightType.SHORTBOW_RAPID || plr.fightType == FightType.DART_RAPID || plr.fightType == FightType.KNIFE_RAPID || plr.fightType == FightType.THROWNAXE_RAPID || plr.fightType == FightType.JAVELIN_RAPID || plr.fightType == FightType.BLOWPIPE_RAPID) {
//                speed--
//            }
//            return speed
//        }
//        if (id == 18365) return 3 else if (id == 18349) //CCbow and rapier
//            return 4
//        if (id == 22034) return 4
//        if (id == 18353) // cmaul
//            return 7 // chaotic maul
//        if (id == 6818) return 1
//        if (id == 22010) return 1
//        if (id == 20000) return 4 // gs
//        if (id == 22008) return 4 // abyssal tentacle == same speed as rapier
//        if (id == 20001) return 4 // gs
//        if (id == 20002) return 4 // gs
//        if (id == 20003) return 4 // gs
//        if (id == 18349) return 4 // chaotic rapier
//        if (id == 14024) return 4 // drygore rapier
//        if (id == 14023) return 5 // drygore long
//        if (id == 18353) // cmaul
//            return 7 // chaotic maul
//        if (id == 16877) return 4 // dung 16877 shortbow
//        if (id == 19143) return 3 // sara shortbow
//        if (id == 19146) return 4 // guthix shortbow
//        if (id == 19149) return 3 // zammy shortbow
//        if (id == 20171) //zaryte
//            return 5
//        if (id == 12926) //blowpipe
//            return 3
//        if (id == 14018) //tempest
//            return 4
//        when (id) {
//            18357 -> return 4
//            11235, 13405, 15701, 15702, 15703, 15704, 19146 -> return 9
//            13879 -> return 8
//            15241 -> return 8
//            11730 -> return 4
//            14484 -> return 5
//            13883 -> return 6
//            10887, 6528, 15039 -> return 7
//            13905 -> return 5
//            13907 -> return 5
//            18353 -> return 7
//            18349 -> return 4
//            20000, 20001, 20002, 20003 -> return 4
//            16403 -> return 5
//            22010 -> return 1
//        }
//        if (s.endsWith("greataxe")) return 7 else if (s == "torags hammers") return 5 else if (s == "guthans warspear") return 5 else if (s == "veracs flail") return 5 else if (s == "ahrims staff") return 6 else if (s == "crossbow") return 4 else if (s.contains(
//                "staff"
//            )
//        ) {
//            return if (s.contains("zamarok") || s.contains("guthix") || s.contains("saradomian") || s.contains("slayer") || s.contains(
//                    "ancient"
//                )
//            ) 4 else 5
//        } else if (s.contains("aril")) {
//            if (s.contains("composite") || s == "seercull") return 5 else if (s.contains("Ogre")) return 8 else if (s.contains(
//                    "short"
//                ) || s.contains("hunt") || s.contains("sword")
//            ) return 4 else if (s.contains("long") || s.contains("crystal")) return 6 else if (s.contains("'bow")) return 4
//            return 5
//        } else if (s.contains("dagger")) return 4 else if (s.contains("godsword") || s.contains("2h")) return 6 else if (s.contains(
//                "longsword"
//            )
//        ) return 5 else if (s.contains("sword")) return 4 else if (s.contains("scimitar")) return 4 else if (s.contains(
//                "katana"
//            )
//        ) return 4 else if (s.contains("tempest")) return 4 else if (s.contains("blowpipe")) return 3 else if (s.contains(
//                "mace"
//            )
//        ) return 5 else if (s.contains("battleaxe")) return 6 else if (s.contains("pickaxe")) return 5 else if (s.contains(
//                "thrownaxe"
//            )
//        ) return 5 else if (s.contains("axe")) return 5 else if (s.contains("warhammer")) return 6 else if (s.contains("2h")) return 7 else if (s.contains(
//                "spear"
//            )
//        ) return 5 else if (s.contains("claw")) return 4 else if (s.contains("halberd")) return 7 else if (s == "granite maul") return 7 else if (s == "toktz-xil-ak") // sword
//            return 4 else if (s == "tzhaar-ket-em") // mace
//            return 5 else if (s == "tzhaar-ket-om") // maul
//            return 7 else if (s == "chaotic maul") // maul
//            return 7 else if (s == "toktz-xil-ek") // knife
//            return 4 else if (s == "toktz-xil-ul") // rings
//            return 4 else if (s == "toktz-mej-tal") // staff
//            return 6 else if (s.contains("whip")) return 4 else if (s.contains("dart")) return 3 else if (s.contains("death-touched")) return 10 else if (s.contains(
//                "knife"
//            )
//        ) return 3 else if (s.contains("javelin")) return 6
//        return 5
//    }
}