package com.realting.world.content.combat.strategy.impl

import com.realting.model.Animation
import com.realting.model.definitions.WeaponAnimations
import com.realting.model.definitions.WeaponInterfaces.WeaponInterface
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.world.content.combat.CombatContainer
import com.realting.world.content.combat.CombatType
import com.realting.world.content.combat.strategy.CombatStrategy
import com.realting.world.content.minigames.Dueling.Companion.checkRule
import com.realting.world.content.minigames.Dueling.DuelRule

/**
 * The default combat strategy assigned to an [CharacterEntity] during a melee
 * based combat session. This is the combat strategy used by all [Npc]s by
 * default.
 *
 * @author lare96
 */
class DefaultMeleeCombatStrategy : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        if (entity!!.isPlayer) {
            val player = entity as Player?
            if (checkRule(player!!, DuelRule.NO_MELEE)) {
                player.packetSender.sendMessage("Melee-attacks have been turned off in this duel!")
                player.combatBuilder.reset(true)
                return false
            }
        }
        return true
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {

        // Start the performAnimation for this attack.
        startAnimation(entity)

        // Create the combat container for this hook.
        return CombatContainer(entity!!, victim!!, 1, CombatType.MELEE, true)
    }

    override fun attackDelay(entity: CharacterEntity?): Int {

        // The attack speed for the weapon being used.
        return entity!!.attackSpeed
    }

    override fun attackDistance(entity: CharacterEntity): Int {

        // The default distance for all npcs using melee is 1.
        if (entity.isNpc) {
            return (entity as NPC).definition.size
        }

        // The default distance for all players is 1, or 2 if they are using a
        // halberd.
        val player = entity as Player
        return if (player.weapon == WeaponInterface.HALBERD) {
            2
        } else 1
    }

    /**
     * Starts the performAnimation for the argued entity in the current combat hook.
     *
     * @param entity
     * the entity to start the performAnimation for.
     */
    private fun startAnimation(entity: CharacterEntity?) {
        if (entity!!.isNpc) {
            val npc = entity as NPC?
            npc!!.performAnimation(
                Animation(
                    npc.definition.attackAnimation
                )
            )
        } else if (entity.isPlayer) {
            val player = entity as Player?
            if (!player!!.isSpecialActivated) {
                player.performAnimation(Animation(WeaponAnimations.getAttackAnimation(player)))
            } else {
                player.performAnimation(Animation(player.fightType.animation))
            }
        }
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return false
    }

    override fun getCombatType(entity: CharacterEntity): CombatType? {
        return CombatType.MELEE
    }
}