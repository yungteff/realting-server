package com.realting.world.content.combat.strategy.impl

import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import com.realting.world.content.combat.CombatContainer
import com.realting.world.content.combat.CombatType
import com.realting.world.content.combat.magic.CombatSpells
import com.realting.world.content.combat.strategy.CombatStrategy
import com.realting.world.content.combat.strategy.impl.bosses.Nex
import com.realting.world.content.combat.weapon.CombatSpecial.Companion.updateBar
import com.realting.world.content.minigames.Dueling.Companion.checkRule
import com.realting.world.content.minigames.Dueling.DuelRule

/**
 * The default combat strategy assigned to an [Entity] during a magic
 * based combat session.
 *
 * @author lare96
 */
class DefaultMagicCombatStrategy : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {

        // Npcs don't need to be checked.
        if (entity!!.isNpc) {
            if (victim!!.isPlayer) {
                val p = victim as Player?
                if (Nex.nexMinion((entity as NPC?)!!.id)) {
                    return if (!p!!.minigameAttributes.godwarsDungeonAttributes.hasEnteredRoom()) {
                        false
                    } else true
                }
            }
            return true
        }

        // Create the player instance.
        val player = entity as Player?
        if (checkRule(player!!, DuelRule.NO_MAGIC)) {
            player.packetSender.sendMessage("Magic-attacks have been turned off in this duel!")
            player.combatBuilder.reset(true)
            return false
        }

        // We can't attack without a spell.
        if (player.castSpell == null) player.castSpell = player.autocastSpell
        if (player.castSpell == null) {
            return false
        }
        if (player.isSpecialActivated) {
            player.isSpecialActivated = false
            updateBar(player)
        }
        return if (player.isSpecialActivated) {
            false
        } else player.castSpell.canCast(player, true)

        // Check the cast using the spell implementation.
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        if (entity!!.isPlayer) {
            val player = entity as Player?
            player!!.prepareSpell(player.castSpell, victim)
            if (player.isSpecialActivated) {
                player.isSpecialActivated = false
                updateBar(player)
            }
            if (player.isAutocast && player.autocastSpell != null) player.castSpell = player.autocastSpell
            player.previousCastSpell = player.castSpell
        } else if (entity.isNpc) {
            val npc = entity as NPC?
            when (npc!!.id) {
                2007 -> npc.prepareSpell(CombatSpells.WATER_WAVE.spell, victim)
                3580 -> npc.prepareSpell(CombatSpells.WATER_STRIKE.spell, victim)
                109 -> npc.prepareSpell(CombatSpells.BABY_SCORPION.spell, victim)
                13, 172, 174 -> npc.prepareSpell(
                    Misc.randomElement(
                        arrayOf(
                            CombatSpells.WEAKEN,
                            CombatSpells.FIRE_STRIKE,
                            CombatSpells.EARTH_STRIKE,
                            CombatSpells.WATER_STRIKE
                        )
                    ).spell, victim
                )
                2025, 1643 -> npc.prepareSpell(
                    Misc.randomElement(
                        arrayOf(
                            CombatSpells.FIRE_WAVE, CombatSpells.EARTH_WAVE, CombatSpells.WATER_WAVE
                        )
                    ).spell, victim
                )
                3495 -> npc.prepareSpell(
                    Misc.randomElement(
                        arrayOf(
                            CombatSpells.SMOKE_BLITZ,
                            CombatSpells.ICE_BLITZ,
                            CombatSpells.ICE_BURST,
                            CombatSpells.SHADOW_BARRAGE
                        )
                    ).spell, victim
                )
                3496 -> npc.prepareSpell(
                    Misc.randomElement(
                        arrayOf(
                            CombatSpells.BLOOD_BARRAGE,
                            CombatSpells.BLOOD_BURST,
                            CombatSpells.BLOOD_BLITZ,
                            CombatSpells.BLOOD_RUSH
                        )
                    ).spell, victim
                )
                3491 -> npc.prepareSpell(
                    Misc.randomElement(
                        arrayOf(
                            CombatSpells.ICE_BARRAGE,
                            CombatSpells.ICE_BLITZ,
                            CombatSpells.ICE_BURST,
                            CombatSpells.ICE_RUSH
                        )
                    ).spell, victim
                )
                13454 -> npc.prepareSpell(CombatSpells.ICE_BLITZ.spell, victim)
                13453 -> npc.prepareSpell(CombatSpells.BLOOD_BURST.spell, victim)
                13452 -> npc.prepareSpell(CombatSpells.SHADOW_BARRAGE.spell, victim)
                13451 -> npc.prepareSpell(CombatSpells.SHADOW_BURST.spell, victim)
                2896 -> npc.prepareSpell(CombatSpells.WATER_STRIKE.spell, victim)
                2882 -> npc.prepareSpell(CombatSpells.DAGANNOTH_PRIME.spell, victim)
                6254 -> npc.prepareSpell(CombatSpells.WIND_WAVE.spell, victim)
                6257 -> npc.prepareSpell(CombatSpells.WATER_WAVE.spell, victim)
                6278 -> npc.prepareSpell(CombatSpells.SHADOW_RUSH.spell, victim)
                6221 -> npc.prepareSpell(CombatSpells.FIRE_BLAST.spell, victim)
            }
            if (npc.currentlyCasting == null) npc.prepareSpell(CombatSpells.WIND_STRIKE.spell, victim)
        }
        return if (entity.currentlyCasting.maximumHit() == -1) {
            CombatContainer(entity, victim!!, CombatType.MAGIC, true)
        } else CombatContainer(entity, victim!!, 1, CombatType.MAGIC, true)
    }

    override fun attackDelay(entity: CharacterEntity?): Int {
        return entity!!.attackSpeed
    }

    override fun attackDistance(entity: CharacterEntity): Int {
        var distance = 8
        if (entity.isNpc) {
            when ((entity as NPC).id) {
                2896, 13451, 13452, 13453, 13454 -> distance = 40
            }
        }
        return distance
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return false
    }

    override fun getCombatType(entity: CharacterEntity): CombatType? {
        return CombatType.MAGIC
    }
}