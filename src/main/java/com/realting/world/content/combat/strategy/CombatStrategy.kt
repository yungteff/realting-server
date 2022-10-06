package com.realting.world.content.combat.strategy

import com.realting.model.Hit
import com.realting.model.entity.character.CharacterEntity
import com.realting.world.content.combat.CombatContainer
import com.realting.world.content.combat.CombatType

/**
 * A container used to determine how an entity will act during a combat session.
 * New combat strategies do not need to be made for [Player]s because
 * everything is handled for them in the three default factory strategy classes.
 *
 * @author lare96
 */
interface CombatStrategy {
    /**
     * Determines if the attacking [Entity] is able to make an attack.
     * Used for miscellaneous checks such as runes, arrows, target, etc.
     *
     * @param entity
     * the entity to check.
     * @return `true` if the attack can be successfully made,
     * `false` if it cannot.
     */
    fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean

    /**
     * Fired when the attacking [Entity] has passed the
     * `canAttack(Entity e1, Entity e2)` check and is ready to
     * attack.
     *
     * @param entity
     * the attacking entity in this combat hook.
     * @param victim
     * the defending entity in this combat hook.
     * @return the combat container that will be used to deal damage to the
     * victim during this hook.
     */
    fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer?
    fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean

    /**
     * How long the attacking [Entity] must wait in intervals to attack.
     *
     * @param entity
     * the attacking entity in this combat hook.
     * @return the amount of time that the attacker must wait.
     */
    fun attackDelay(entity: CharacterEntity?): Int

    /**
     * How close the attacking [Entity] must be to attack the victim.
     *
     * @param entity
     * the attacking entity in this combat hook.
     * @return the radius that the attacker has to be within in order to attack.
     */
    fun attackDistance(entity: CharacterEntity): Int
    fun getCombatType(entity: CharacterEntity): CombatType?

    /**
     * Entity was damaged.
     * @param hit the damage
     */
    fun damaged(entity: CharacterEntity, hit: Hit?) {}

    /**
     * This entity attacked another entity
     */
    fun attacked(entity: CharacterEntity, victim: CharacterEntity?, hit: Hit?) {}

    /**
     * Modify a hit.
     * @param entity the entity that has the CombatStrategy
     */
    fun modifyHit(entity: CharacterEntity, hit: Hit?) {}

    /**
     * Entity respawned
     * @param entity the entity
     */
    fun respawned(entity: CharacterEntity) {}
}