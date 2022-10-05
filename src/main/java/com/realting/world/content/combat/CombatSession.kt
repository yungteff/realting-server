package com.realting.world.content.combat

import com.realting.model.container.impl.Equipment
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.world.content.Sounds
import com.realting.world.content.combat.HitQueue.CombatHit
import com.realting.world.content.combat.range.CombatRangedAmmo.RangedWeaponData
import com.realting.world.content.combat.strategy.impl.DefaultRangedCombatStrategy
import com.realting.world.content.combat.weapon.CombatSpecial

/**
 * A [Task] implementation that handles every combat 'hook' or 'turn'
 * during a combat session.
 *
 * @author lare96
 */
class CombatSession
/**
 * Create a new [CombatSession].
 *
 * @param builder
 * the builder assigned to this task.
 */(
    /** The builder assigned to this task.  */
    private val builder: CombatBuilder
) {
    fun process() {
        if (builder.isCooldown) {
            builder.cooldown--
            builder.attackTimer--
            if (builder.cooldown == 0) {
                builder.reset(true)
            }
            return
        }
        if (!CombatFactory.checkHook(builder.character, builder.victim)) {
            return
        }

        // If the entity is an player we redetermine the combat strategy before
        // attacking.
        if (builder.character.isPlayer) {
            builder.determineStrategy()
        }

        // Decrement the attack timer.
        builder.attackTimer--

        // The attack timer is below 1, we can attack.
        if (builder.attackTimer < 1) {
            // Check if the attacker is close enough to attack.
            if (!CombatFactory.checkAttackDistance(builder)) {
                if (builder.character.isNpc && builder.victim.isPlayer) {
                    if (builder.lastAttack.elapsed(4500)) {
                        (builder.character as NPC).setFindNewTarget(true)
                    }
                }
                return
            }

            // Check if the attack can be made on this hook
            if (!builder.strategy.canAttack(builder.character, builder.victim)) {
                builder.character.combatBuilder.reset(builder.character.isNpc)
                return
            }

            // Do all combat calculations here, we create the combat containers
            // using the attacking entity's combat strategy.
            builder.strategy.customContainerAttack(builder.character, builder.victim)

            var container = builder.container
            builder.character.setEntityInteraction(builder.victim)
            if (builder.character.isPlayer) {
                val player = builder.character as Player
                player.packetSender.sendInterfaceRemoval()
                if (player.isSpecialActivated && player.castSpell == null) {
                    container = player.combatSpecial.container(player, builder.victim)
                    val magicShortbowSpec =
                        player.combatSpecial != null && player.combatSpecial === CombatSpecial.MAGIC_SHORTBOW
                    CombatSpecial.drain(player, player.combatSpecial.drainAmount)
                    Sounds.sendSound(
                        player, Sounds.specialSounds(
                            player.equipment[Equipment.WEAPON_SLOT].id
                        )
                    )
                    if (player.combatSpecial.combatType === CombatType.RANGED) {
                        DefaultRangedCombatStrategy.decrementAmmo(player, builder.victim.position)
                        if (CombatFactory.darkBow(player) || player.rangedWeaponData == RangedWeaponData.MAGIC_SHORTBOW && magicShortbowSpec) {
                            DefaultRangedCombatStrategy.decrementAmmo(player, builder.victim.position)
                        }
                    }
                }
            }

            // If there is no hit type the combat turn is ignored.
            if (container != null && container.combatType != null) {
                // If we have hit splats to deal, we filter them through combat
                // prayer effects now. If not then we still send the hit tasks
                // next to handle any effects.

                // An attack is going to be made for sure, set the last attacker
                // for this victim.
                builder.victim.combatBuilder.lastAttacker = builder.character
                builder.victim.lastCombat.reset()

                // Start cooldown if we're using magic and not autocasting.
                if (container.combatType === CombatType.MAGIC && builder.character.isPlayer) {
                    val player = builder.character as Player
                    if (!player.isAutocast) {
                        if (!player.isSpecialActivated) player.combatBuilder.cooldown = 10
                        player.castSpell = null
                        player.movementQueue.followCharacter = null
                        builder.determineStrategy()
                    }
                }

                builder.hitQueue.append(CombatHit(builder, container, container.hitDelay))
//                builder.container = null //Fetch a brand new container on next attack
            }

            // Reset the attacking entity.
            builder.attackTimer =
                if (builder.strategy != null) builder.strategy.attackDelay(builder.character) else builder.character.attackSpeed
            builder.lastAttack.reset()
            builder.character.setEntityInteraction(builder.victim)
        }
    }
}