package com.realting.world.content.combat

import com.realting.model.CombatIcon
import com.realting.model.Hit
import com.realting.model.Hitmask
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import com.realting.world.content.combat.CombatFactory.Companion.getHit
import com.realting.world.content.combat.CombatFactory.Companion.rollAccuracy
import com.realting.world.content.combat.weapon.CombatSpecial
import java.util.*
import java.util.function.Consumer

/**
 * A container that holds all of the data needed for a single combat hook.
 *
 * @author lare96
 */
open class CombatContainer {
    /** The attacker in this combat hook.  */
    private var attacker: CharacterEntity

    /** The victim in this combat hook.  */
    private var victim: CharacterEntity

    /** The hits that will be dealt during this combat hook.  */
    internal var hits: Array<ContainerHit?>
    /**
     * Gets the skills that will be given experience.
     *
     * @return the skills that will be given experience.
     */
    /** The skills that will be given experience.  */
    var experience: IntArray
        private set
    /**
     * Gets the combat type that is being used during this combat hook.
     *
     * @return the combat type that is being used during this combat hook.
     */
    /**
     * Sets the combat type that is being used during this combat hook.
     *
     * @param combatType
     * the combat type that is being used during this combat hook.
     */
    /** The combat type that is being used during this combat hook.  */
    var combatType: CombatType
    /**
     * Gets if accuracy should be taken into account.
     *
     * @return true if accuracy should be taken into account.
     */
    /**
     * Sets if accuracy should be taken into account.
     *
     * @param checkAccuracy
     * true if accuracy should be taken into account.
     */
    /** If accuracy should be taken into account.  */
    var isCheckAccuracy: Boolean
    /**
     * Gets if at least one hit in this container is accurate.
     *
     * @return true if at least one hit in this container is accurate.
     */
    /** If at least one hit in this container is accurate.  */
    var isAccurate = false
        private set

    /** The modified damage, used for bolt effects etc  */
    var modifiedDamage = 0
    /**
     * Gets the hit delay before the hit is executed.
     *
     * @return the hit delay.
     */
    /** The delay before the hit is executed  */
    var hitDelay: Int
        private set
    private var maxHit = -1

    /**
     * Create a new [CombatContainer].
     *
     * @param attacker
     * the attacker in this combat hook.
     * @param victim
     * the victim in this combat hook.
     * @param hitAmount
     * the amount of hits to deal this combat hook.
     * @param hitType
     * the combat type that is being used during this combat hook
     * @param checkAccuracy
     * if accuracy should be taken into account.
     */
    constructor(
        attacker: CharacterEntity,
        victim: CharacterEntity,
        hitAmount: Int,
        hitType: CombatType,
        checkAccuracy: Boolean
    ) {
        this.attacker = attacker
        this.victim = victim
        combatType = hitType
        isCheckAccuracy = checkAccuracy
        hits = prepareHits(hitAmount)
        experience = getSkills(hitType)
        hitDelay =
            if (hitType === CombatType.MELEE) 0 else if (hitType === CombatType.RANGED) 1 else if (hitType === CombatType.MAGIC || hitType === CombatType.DRAGON_FIRE) 2 else 1
    }

    constructor(
        attacker: CharacterEntity,
        victim: CharacterEntity,
        hitAmount: Int,
        hitDelay: Int,
        hitType: CombatType,
        checkAccuracy: Boolean
    ) {
        this.attacker = attacker
        this.victim = victim
        combatType = hitType
        isCheckAccuracy = checkAccuracy
        hits = prepareHits(hitAmount)
        experience = getSkills(hitType)
        this.hitDelay = hitDelay
    }

    constructor(
        attacker: CharacterEntity,
        victim: CharacterEntity,
        hitAmount: Int,
        hitDelay: Int,
        hitType: CombatType,
        checkAccuracy: Boolean,
        maxHit: Int
    ) {
        this.maxHit = maxHit
        this.attacker = attacker
        this.victim = victim
        combatType = hitType
        isCheckAccuracy = checkAccuracy
        hits = prepareHits(hitAmount)
        experience = getSkills(hitType)
        this.hitDelay = hitDelay
    }

    /**
     * Create a new [CombatContainer] that will deal no hit this turn.
     * Used for things like spells that have special effects but don't deal
     * damage.
     *
     * @param checkAccuracy
     * if accuracy should be taken into account.
     */
    constructor(attacker: CharacterEntity, victim: CharacterEntity, hitType: CombatType, checkAccuracy: Boolean) : this(
        attacker,
        victim,
        0,
        hitType,
        checkAccuracy
    ) {
    }

    /**
     * Prepares the hits that will be dealt this combat hook.
     *
     * @param hitAmount
     * the amount of hits to deal, maximum 4 and minimum 0.
     * @return the hits that will be dealt this combat hook.
     */
    private fun prepareHits(hitAmount: Int): Array<ContainerHit?> {

        // Check the hit amounts.
        require(hitAmount <= 4) { "Illegal number of hits! The maximum number of hits per turn is 4." }
        require(hitAmount >= 0) { "Illegal number of hits! The minimum number of hits per turn is 0." }

        // No hit for this turn, but we still need to calculate accuracy.
        if (hitAmount == 0) {
            isAccurate = !isCheckAccuracy || rollAccuracy(attacker, victim, combatType)
            return arrayOf()
        }

        // Create the new array of hits, and populate it. Here we do the maximum
        // hit and accuracy calculations.
        val array = arrayOfNulls<ContainerHit>(hitAmount)
        for (i in array.indices) {
            val accuracy = !isCheckAccuracy || rollAccuracy(attacker, victim, combatType)
            array[i] = ContainerHit(getHit(attacker, victim, combatType, maxHit), accuracy)
            if (array[i]!!.isAccurate) {
                isAccurate = true
            }
        }
        /** SPECS  */
        if (attacker.isPlayer && (attacker as Player).isSpecialActivated) {
            if ((attacker as Player).combatSpecial === CombatSpecial.DRAGON_CLAWS && hitAmount == 4) {
                var first = array[0]!!.hit.damage
                if (first > 360) {
                    first = 360 + Misc.getRandom(10)
                }
                val second = if (first <= 0) array[1]!!.hit.damage else (first / 2)
                val third =
                    if (first <= 0 && second > 0) (second / 2) else if (first <= 0 && second <= 0) array[2]!!.hit.damage else Misc.getRandom(
                        second
                    )
                val fourth =
                    if (first <= 0 && second <= 0 && third <= 0) array[3]!!.hit.damage + Misc.getRandom(7) else if (first <= 0 && second <= 0) array[3]!!.hit.damage else third
                array[0]!!.hit.damage = first
                array[1]!!.hit.damage = second
                array[2]!!.hit.damage = third
                array[3]!!.hit.damage = fourth
            } else if ((attacker as Player).combatSpecial === CombatSpecial.DARK_BOW && hitAmount == 2) {
                for (i in 0 until hitAmount) {
                    if (array[i]!!.hit.damage < 80) {
                        array[i]!!.hit.damage = 80
                    }
                    array[i]!!.isAccurate = true
                }
            }
        }
        return array
    }

    fun setHits(hits: Array<ContainerHit?>) {
        this.hits = hits
        prepareHits(hits.size)
    }

    /**
     * Performs an action on every single hit in this container.
     *
     * @param action
     * the action to perform on every single hit.
     */
    fun allHits(c: Consumer<ContainerHit?>?) {
        Arrays.stream(hits).filter { obj: ContainerHit? -> Objects.nonNull(obj) }.forEach(c)
    }

    val damage: Int
        get() {
            var damage = 0
            for (hit in hits) {
                if (hit == null) continue
                if (!hit.isAccurate) {
                    val absorb = hit.hit.absorb
                    hit.hit = Hit(hit.hit.attacker, 0, Hitmask.RED, CombatIcon.BLOCK, true)
                    hit.hit.absorb = absorb
                }
                damage += hit.hit.damage
            }
            return damage
        }

    fun dealDamage() {
        if (hits.size == 1) {
            victim.dealDamage(hits[0]!!.hit)
        } else if (hits.size == 2) {
            victim.dealDoubleDamage(hits[0]!!.hit, hits[1]!!.hit)
        } else if (hits.size == 3) {
            victim.dealTripleDamage(hits[0]!!.hit, hits[1]!!.hit, hits[2]!!.hit)
        } else if (hits.size == 4) {
            victim.dealQuadrupleDamage(hits[0]!!.hit, hits[1]!!.hit, hits[2]!!.hit, hits[3]!!.hit)
        }
    }

    /**
     * Gets all of the skills that will be trained.
     *
     * @param type
     * the combat type being used.
     *
     * @return an array of skills that this attack will train.
     */
    private fun getSkills(type: CombatType): IntArray {
        return if (attacker.isNpc) {
            intArrayOf()
        } else (attacker as Player).fightType.style.skill(type)
    }

    /**
     * A dynamic method invoked when the victim is hit with an attack. An
     * example of usage is using this to do some sort of special effect when the
     * victim is hit with a spell. **Do not reset combat builder in this
     * method!**
     *
     * @param damage
     * the damage inflicted with this attack, always 0 if the attack
     * isn't accurate.
     * @param accurate
     * if the attack is accurate.
     */
    open fun onHit(damage: Int, accurate: Boolean) {}

    /**
     * Gets the hits that will be dealt during this combat hook.
     *
     * @return the hits that will be dealt during this combat hook.
     */
    fun getHits(): Array<ContainerHit?> {
        return hits
    }

    /**
     * Sets the amount of hits that will be dealt during this combat hook.
     *
     * @param hitAmount
     * the amount of hits that will be dealt during this combat hook.
     */
    fun setHitAmount(hitAmount: Int) {
        hits = prepareHits(hitAmount)
    }

    /**
     * A single hit that is dealt during a combat hook.
     *
     * @author lare96
     */
    class ContainerHit
    /**
     * Create a new [ContainerHit].
     *
     * @param hit
     * the actual hit that will be dealt.
     * @param accurate
     * the accuracy of the hit to be dealt.
     */(
        /** The actual hit that will be dealt.  */
        var hit: Hit,
        /** The accuracy of the hit to be dealt.  */
        var isAccurate: Boolean
    ) {
        /**
         * Gets the actual hit that will be dealt.
         *
         * @return the actual hit that will be dealt.
         */
        /**
         * Sets the actual hit that will be dealt.
         *
         * @param hit
         * the actual hit that will be dealt.
         */
        /**
         * Gets the accuracy of the hit to be dealt.
         *
         * @return the accuracy of the hit to be dealt.
         */
        /**
         * Sets the accuracy of the hit to be dealt.
         *
         * @param isAccurate
         * the accuracy of the hit to be dealt.
         */

    }
}