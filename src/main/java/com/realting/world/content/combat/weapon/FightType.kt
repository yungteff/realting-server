package com.realting.world.content.combat.weapon

import com.realting.world.content.player.events.BonusManager

/**
 * A collection of constants that each represent a different fighting type.
 *
 * @author lare96
 */
/*
 *     private FightType(int animation, int parentId, int childId, int bonusType,
        FightStyle style) {
        this.animation = animation;
        this.parentId = parentId;
        this.childId = childId;
        this.bonusType = bonusType;
        this.style = style;
    }
 *
 */
enum class FightType
/**
 * Create a new [FightType].
 *
 * @param animation
 * the animation this fight type holds.
 * @param trainType
 * the train type this fight type holds.
 * @param parentId
 * the parent config id.
 * @param childId
 * the child config id.
 * @param bonusType
 * the bonus type.
 * @param fightStyle
 * the fighting style.
 */(
    /** The animation this fight type holds.  */
    val animation: Int,
    /** The parent config id.  */
    val parentId: Int,
    /** The child config id.  */
    val childId: Int,
    /** The bonus type.  */
    val bonusType: Int,
    /** The fighting style.  */
    val style: FightStyle
) {
    STAFF_BASH(401, 43, 0, BonusManager.ATTACK_CRUSH, FightStyle.ACCURATE), STAFF_POUND(
        406, 43, 1, BonusManager.ATTACK_CRUSH, FightStyle.AGGRESSIVE
    ),
    STAFF_FOCUS(406, 43, 2, BonusManager.ATTACK_CRUSH, FightStyle.DEFENSIVE), WARHAMMER_POUND(
        401, 43, 0, BonusManager.ATTACK_CRUSH, FightStyle.ACCURATE
    ),
    WARHAMMER_PUMMEL(401, 43, 1, BonusManager.ATTACK_CRUSH, FightStyle.AGGRESSIVE), WARHAMMER_BLOCK(
        401, 43, 2, BonusManager.ATTACK_CRUSH, FightStyle.DEFENSIVE
    ),
    SCYTHE_REAP(414, 43, 0, BonusManager.ATTACK_SLASH, FightStyle.ACCURATE), SCYTHE_CHOP(
        382, 43, 1, BonusManager.ATTACK_STAB, FightStyle.AGGRESSIVE
    ),
    SCYTHE_JAB(2066, 43, 2, BonusManager.ATTACK_CRUSH, FightStyle.CONTROLLED), SCYTHE_BLOCK(
        382, 43, 3, BonusManager.ATTACK_SLASH, FightStyle.DEFENSIVE
    ),
    BATTLEAXE_CHOP(401, 43, 0, BonusManager.ATTACK_SLASH, FightStyle.ACCURATE), BATTLEAXE_HACK(
        401, 43, 1, BonusManager.ATTACK_SLASH, FightStyle.AGGRESSIVE
    ),
    BATTLEAXE_SMASH(401, 43, 2, BonusManager.ATTACK_CRUSH, FightStyle.AGGRESSIVE), BATTLEAXE_BLOCK(
        401, 43, 3, BonusManager.ATTACK_SLASH, FightStyle.DEFENSIVE
    ),
    CROSSBOW_ACCURATE(4230, 43, 0, BonusManager.ATTACK_RANGE, FightStyle.ACCURATE), CROSSBOW_RAPID(
        4230, 43, 1, BonusManager.ATTACK_RANGE, FightStyle.AGGRESSIVE
    ),
    CROSSBOW_LONGRANGE(4230, 43, 2, BonusManager.ATTACK_RANGE, FightStyle.DEFENSIVE), ARMADYLXBOW_ACCURATE(
        4230, 43, 0, BonusManager.ATTACK_RANGE, FightStyle.ACCURATE
    ),
    ARMADYLXBOW_RAPID(4230, 43, 1, BonusManager.ATTACK_RANGE, FightStyle.AGGRESSIVE), ARMADYLXBOW_LONGRANGE(
        4230, 43, 2, BonusManager.ATTACK_RANGE, FightStyle.DEFENSIVE
    ),
    SHORTBOW_ACCURATE(426, 43, 0, BonusManager.ATTACK_RANGE, FightStyle.ACCURATE), SHORTBOW_RAPID(
        426, 43, 1, BonusManager.ATTACK_RANGE, FightStyle.AGGRESSIVE
    ),
    SHORTBOW_LONGRANGE(426, 43, 2, BonusManager.ATTACK_RANGE, FightStyle.DEFENSIVE), BLOWPIPE_ACCURATE(
        5061, 43, 0, BonusManager.ATTACK_RANGE, FightStyle.ACCURATE
    ),
    BLOWPIPE_RAPID(5061, 43, 1, BonusManager.ATTACK_RANGE, FightStyle.AGGRESSIVE), BLOWPIPE_LONGRANGE(
        5061, 43, 2, BonusManager.ATTACK_RANGE, FightStyle.DEFENSIVE
    ),
    BSOAT_ACCURATE(13045, 43, 0, BonusManager.ATTACK_RANGE, FightStyle.ACCURATE), BSOAT_RAPID(
        13045, 43, 1, BonusManager.ATTACK_RANGE, FightStyle.AGGRESSIVE
    ),
    BSOAT_LONGRANGE(13045, 43, 2, BonusManager.ATTACK_RANGE, FightStyle.DEFENSIVE), LONGBOW_ACCURATE(
        426, 43, 0, BonusManager.ATTACK_RANGE, FightStyle.ACCURATE
    ),
    LONGBOW_RAPID(426, 43, 1, BonusManager.ATTACK_RANGE, FightStyle.AGGRESSIVE), LONGBOW_LONGRANGE(
        426, 43, 2, BonusManager.ATTACK_RANGE, FightStyle.DEFENSIVE
    ),
    DAGGER_STAB(13049, 43, 0, BonusManager.ATTACK_STAB, FightStyle.ACCURATE), DAGGER_LUNGE(
        13049, 43, 1, BonusManager.ATTACK_STAB, FightStyle.AGGRESSIVE
    ),
    DAGGER_SLASH(13048, 43, 2, BonusManager.ATTACK_STAB, FightStyle.AGGRESSIVE), DAGGER_BLOCK(
        13049, 43, 3, BonusManager.ATTACK_STAB, FightStyle.DEFENSIVE
    ),
    SWORD_STAB(15072, 43, 0, BonusManager.ATTACK_STAB, FightStyle.ACCURATE), SWORD_LUNGE(
        12310, 43, 1, BonusManager.ATTACK_STAB, FightStyle.AGGRESSIVE
    ),
    SWORD_SLASH(12310, 43, 2, BonusManager.ATTACK_SLASH, FightStyle.AGGRESSIVE), SWORD_BLOCK(
        12310, 43, 3, BonusManager.ATTACK_STAB, FightStyle.DEFENSIVE
    ),
    SCIMITAR_CHOP(15071, 43, 0, BonusManager.ATTACK_SLASH, FightStyle.ACCURATE), SCIMITAR_SLASH(
        15071, 43, 1, BonusManager.ATTACK_SLASH, FightStyle.AGGRESSIVE
    ),
    SCIMITAR_LUNGE(15072, 43, 2, BonusManager.ATTACK_STAB, FightStyle.CONTROLLED), SCIMITAR_BLOCK(
        15071, 43, 3, BonusManager.ATTACK_SLASH, FightStyle.DEFENSIVE
    ),
    LONGSWORD_CHOP(12310, 43, 0, BonusManager.ATTACK_SLASH, FightStyle.ACCURATE), LONGSWORD_SLASH(
        12310, 43, 1, BonusManager.ATTACK_SLASH, FightStyle.AGGRESSIVE
    ),
    LONGSWORD_LUNGE(12310, 43, 2, BonusManager.ATTACK_STAB, FightStyle.CONTROLLED), LONGSWORD_BLOCK(
        12310, 43, 3, BonusManager.ATTACK_SLASH, FightStyle.DEFENSIVE
    ),
    MACE_POUND(1665, 43, 0, BonusManager.ATTACK_CRUSH, FightStyle.ACCURATE), MACE_PUMMEL(
        1665, 43, 1, BonusManager.ATTACK_CRUSH, FightStyle.AGGRESSIVE
    ),
    MACE_SPIKE(13036, 43, 2, BonusManager.ATTACK_STAB, FightStyle.CONTROLLED), MACE_BLOCK(
        1665, 43, 3, BonusManager.ATTACK_CRUSH, FightStyle.DEFENSIVE
    ),
    KNIFE_ACCURATE(929, 43, 0, BonusManager.ATTACK_RANGE, FightStyle.ACCURATE), KNIFE_RAPID(
        929, 43, 1, BonusManager.ATTACK_RANGE, FightStyle.AGGRESSIVE
    ),
    KNIFE_LONGRANGE(929, 43, 2, BonusManager.ATTACK_RANGE, FightStyle.DEFENSIVE), SPEAR_LUNGE(
        13045, 43, 0, BonusManager.ATTACK_STAB, FightStyle.CONTROLLED
    ),
    SPEAR_SWIPE(13047, 43, 1, BonusManager.ATTACK_SLASH, FightStyle.CONTROLLED), SPEAR_POUND(
        13044, 43, 2, BonusManager.ATTACK_CRUSH, FightStyle.CONTROLLED
    ),
    SPEAR_BLOCK(13044, 43, 3, BonusManager.ATTACK_STAB, FightStyle.DEFENSIVE), TWOHANDEDSWORD_CHOP(
        11981, 43, 0, BonusManager.ATTACK_SLASH, FightStyle.ACCURATE
    ),
    TWOHANDEDSWORD_SLASH(11979, 43, 1, BonusManager.ATTACK_SLASH, FightStyle.AGGRESSIVE), TWOHANDEDSWORD_SMASH(
        11979, 43, 2, BonusManager.ATTACK_CRUSH, FightStyle.AGGRESSIVE
    ),
    TWOHANDEDSWORD_BLOCK(11979, 43, 3, BonusManager.ATTACK_SLASH, FightStyle.DEFENSIVE), PICKAXE_SPIKE(
        400, 43, 0, BonusManager.ATTACK_STAB, FightStyle.ACCURATE
    ),
    PICKAXE_IMPALE(400, 43, 1, BonusManager.ATTACK_STAB, FightStyle.AGGRESSIVE), PICKAXE_SMASH(
        401, 43, 2, BonusManager.ATTACK_CRUSH, FightStyle.AGGRESSIVE
    ),
    PICKAXE_BLOCK(400, 43, 3, BonusManager.ATTACK_STAB, FightStyle.DEFENSIVE), CLAWS_CHOP(
        393, 43, 0, BonusManager.ATTACK_SLASH, FightStyle.ACCURATE
    ),
    CLAWS_SLASH(393, 43, 1, BonusManager.ATTACK_SLASH, FightStyle.AGGRESSIVE), CLAWS_LUNGE(
        393, 43, 2, BonusManager.ATTACK_STAB, FightStyle.CONTROLLED
    ),
    CLAWS_BLOCK(393, 43, 3, BonusManager.ATTACK_SLASH, FightStyle.DEFENSIVE), HALBERD_JAB(
        440, 43, 0, BonusManager.ATTACK_STAB, FightStyle.CONTROLLED
    ),
    HALBERD_SWIPE(440, 43, 1, BonusManager.ATTACK_SLASH, FightStyle.AGGRESSIVE), HALBERD_FEND(
        440, 43, 2, BonusManager.ATTACK_STAB, FightStyle.DEFENSIVE
    ),
    UNARMED_PUNCH(422, 43, 0, BonusManager.ATTACK_CRUSH, FightStyle.ACCURATE), UNARMED_KICK(
        423, 43, 1, BonusManager.ATTACK_CRUSH, FightStyle.AGGRESSIVE
    ),
    UNARMED_BLOCK(422, 43, 2, BonusManager.ATTACK_CRUSH, FightStyle.DEFENSIVE), WHIP_FLICK(
        11968, 43, 0, BonusManager.ATTACK_SLASH, FightStyle.ACCURATE
    ),
    WHIP_LASH(11969, 43, 1, BonusManager.ATTACK_SLASH, FightStyle.CONTROLLED), WHIP_DEFLECT(
        11970, 43, 2, BonusManager.ATTACK_SLASH, FightStyle.DEFENSIVE
    ),
    THROWNAXE_ACCURATE(929, 43, 0, BonusManager.ATTACK_RANGE, FightStyle.ACCURATE), THROWNAXE_RAPID(
        929, 43, 1, BonusManager.ATTACK_RANGE, FightStyle.AGGRESSIVE
    ),
    THROWNAXE_LONGRANGE(929, 43, 2, BonusManager.ATTACK_RANGE, FightStyle.DEFENSIVE), DART_ACCURATE(
        929, 43, 0, BonusManager.ATTACK_RANGE, FightStyle.ACCURATE
    ),
    DART_RAPID(929, 43, 1, BonusManager.ATTACK_RANGE, FightStyle.AGGRESSIVE), DART_LONGRANGE(
        929, 43, 2, BonusManager.ATTACK_RANGE, FightStyle.DEFENSIVE
    ),
    JAVELIN_ACCURATE(929, 43, 0, BonusManager.ATTACK_RANGE, FightStyle.ACCURATE), JAVELIN_RAPID(
        929, 43, 2, BonusManager.ATTACK_RANGE, FightStyle.AGGRESSIVE
    ),
    JAVELIN_LONGRANGE(929, 43, 3, BonusManager.ATTACK_RANGE, FightStyle.DEFENSIVE);
    /**
     * Gets the animation this fight type holds.
     *
     * @return the animation.
     */
    /**
     * Gets the parent config id.
     *
     * @return the parent id.
     */
    /**
     * Gets the child config id.
     *
     * @return the child id.
     */
    /**
     * Gets the bonus type.
     *
     * @return the bonus type.
     */
    /**
     * Gets the fighting style.
     *
     * @return the fighting style.
     */

    /**
     * Determines the corresponding bonus for this fight type.
     *
     * @return the corresponding bonus for this fight type.
     */
    val correspondingBonus: Int
        get() = when (bonusType) {
            BonusManager.ATTACK_CRUSH -> BonusManager.DEFENCE_CRUSH
            BonusManager.ATTACK_MAGIC -> BonusManager.DEFENCE_MAGIC
            BonusManager.ATTACK_RANGE -> BonusManager.DEFENCE_RANGE
            BonusManager.ATTACK_SLASH -> BonusManager.DEFENCE_SLASH
            BonusManager.ATTACK_STAB -> BonusManager.DEFENCE_STAB
            else -> BonusManager.DEFENCE_CRUSH
        }
}