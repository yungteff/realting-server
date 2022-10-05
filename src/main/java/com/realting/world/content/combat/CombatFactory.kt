package com.realting.world.content.combat

import com.realting.GameSettings
import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.engine.task.impl.CombatSkullEffect
import com.realting.model.*
import com.realting.model.container.impl.Equipment
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.npc.NPCMovementCoordinator.CoordinateState
import com.realting.model.entity.character.player.Player
import com.realting.model.movement.PathFinder
import com.realting.util.Misc
import com.realting.world.clip.region.RegionClipping
import com.realting.world.content.ItemDegrading
import com.realting.world.content.ItemDegrading.DegradingItem
import com.realting.world.content.Kraken.KrakenInstance
import com.realting.world.content.combat.CombatContainer.ContainerHit
import com.realting.world.content.combat.DesolaceFormulas.getMagicAttack
import com.realting.world.content.combat.DesolaceFormulas.getMagicDefence
import com.realting.world.content.combat.DesolaceFormulas.getMagicMaxHit
import com.realting.world.content.combat.DesolaceFormulas.getMeleeAttack
import com.realting.world.content.combat.DesolaceFormulas.getMeleeDefence
import com.realting.world.content.combat.DesolaceFormulas.getMeleeMaxHit
import com.realting.world.content.combat.DesolaceFormulas.getRangedAttack
import com.realting.world.content.combat.DesolaceFormulas.getRangedDefence
import com.realting.world.content.combat.effect.CombatPoisonEffect
import com.realting.world.content.combat.effect.CombatPoisonEffect.PoisonType
import com.realting.world.content.combat.effect.EquipmentBonus
import com.realting.world.content.combat.magic.CombatAncientSpell
import com.realting.world.content.combat.prayer.CurseHandler
import com.realting.world.content.combat.prayer.PrayerHandler
import com.realting.world.content.combat.range.CombatRangedAmmo.RangedWeaponData
import com.realting.world.content.combat.strategy.impl.bosses.Nex
import com.realting.world.content.combat.strategy.impl.bosses.Scorpia
import com.realting.world.content.combat.weapon.CombatSpecial
import com.realting.world.content.combat.weapon.FightStyle
import com.realting.world.content.player.events.BonusManager
import com.realting.world.content.player.events.BonusManager.Companion.sendCurseBonuses
import com.realting.world.content.player.events.BonusManager.Companion.update
import com.realting.world.content.transportation.TeleportHandler
import com.realting.world.content.transportation.TeleportType
import java.lang.UnsupportedOperationException
import java.util.*

/**
 * A static factory class containing all miscellaneous methods related to, and
 * used for combat.
 *
 * @author lare96
 * @author Scu11
 * @author Graham
 */
class CombatFactory private constructor() {
    /**
     * The default constructor, will throw an
     * [UnsupportedOperationException] if instantiated.
     */
    init {
        throw UnsupportedOperationException(
            "This class cannot be instantiated!"
        )
    }

    companion object {
        /** The amount of time it takes for cached damage to timeout.  */ // Damage cached for currently 60 seconds will not be accounted for.
        const val DAMAGE_CACHE_TIMEOUT: Long = 60000

        /** The amount of damage that will be drained by combat protection prayer.  */ // Currently at .20 meaning 20% of damage drained when using the right
        // protection prayer.
        const val PRAYER_DAMAGE_REDUCTION = .20

        /** The rate at which accuracy will be reduced by combat protection prayer.  */ // Currently at .255 meaning 25.5% percent chance of canceling damage when
        // using the right protection prayer.
        const val PRAYER_ACCURACY_REDUCTION = .255

        /** The amount of hitpoints the redemption prayer will heal.  */ // Currently at .25 meaning hitpoints will be healed by 25% of the remaining
        // prayer points when using redemption.
        const val REDEMPTION_PRAYER_HEAL = .25

        /** The maximum amount of damage inflicted by retribution.  */ // Damage between currently 0-15 will be inflicted if in the specified
        // radius when the retribution prayer effect is activated.
        const val MAXIMUM_RETRIBUTION_DAMAGE = 150

        /** The radius that retribution will hit players in.  */ // All players within currently 5 squares will get hit by the retribution
        // effect.
        const val RETRIBUTION_RADIUS = 5

        /**
         * Determines if the entity is wearing full veracs.
         *
         * @param entity
         * the entity to determine this for.
         * @return true if the player is wearing full veracs.
         */
        fun fullVeracs(entity: CharacterEntity): Boolean {
            return if (entity.isNpc) (entity as NPC).definition.name == "Verac the Defiled" else (entity as Player).equipment.containsAll(
                4753, 4757, 4759, 4755
            )
        }

        /**
         * Determines if the entity is wearing full dharoks.
         *
         * @param entity
         * the entity to determine this for.
         * @return true if the player is wearing full dharoks.
         */
        fun fullDharoks(entity: CharacterEntity): Boolean {
            return if (entity.isNpc) (entity as NPC).definition.name == "Dharok the Wretched" else (entity as Player).equipment.containsAll(
                4716, 4720, 4722, 4718
            )
        }

        /**
         * Determines if the entity is wearing full karils.
         *
         * @param entity
         * the entity to determine this for.
         * @return true if the player is wearing full karils.
         */
        fun fullKarils(entity: CharacterEntity): Boolean {
            return if (entity.isNpc) (entity as NPC).definition.name == "Karil the Tainted" else (entity as Player).equipment.containsAll(
                4732, 4736, 4738, 4734
            )
        }

        /**
         * Determines if the entity is wearing full ahrims.
         *
         * @param entity
         * the entity to determine this for.
         * @return true if the player is wearing full ahrims.
         */
        fun fullAhrims(entity: CharacterEntity): Boolean {
            return if (entity.isNpc) (entity as NPC).definition.name == "Ahrim the Blighted" else (entity as Player).equipment.containsAll(
                4708, 4712, 4714, 4710
            )
        }

        /**
         * Determines if the entity is wearing full torags.
         *
         * @param entity
         * the entity to determine this for.
         * @return true if the player is wearing full torags.
         */
        fun fullTorags(entity: CharacterEntity): Boolean {
            return if (entity.isNpc) (entity as NPC).definition.name == "Torag the Corrupted" else (entity as Player).equipment.containsAll(
                4745, 4749, 4751, 4747
            )
        }

        /**
         * Determines if the entity is wearing full guthans.
         *
         * @param entity
         * the entity to determine this for.
         * @return true if the player is wearing full guthans.
         */
        fun fullGuthans(entity: CharacterEntity): Boolean {
            return if (entity.isNpc) (entity as NPC).definition.name == "Guthan the Infested" else (entity as Player).equipment.containsAll(
                4724, 4728, 4730, 4726
            )
        }

        /**
         * Determines if the player is wielding a crystal bow.
         *
         * @param player
         * the player to determine for.
         * @return true if the player is wielding a crystal bow.
         */
        @JvmStatic
        fun crystalBow(player: Player): Boolean {
            val item = player.equipment[Equipment.WEAPON_SLOT] ?: return false
            return item.definition.name.lowercase(Locale.getDefault()).contains(
                "crystal bow"
            )
        }

        @JvmStatic
        fun toxicblowpipe(p: Player): Boolean {
            val item = p.equipment[Equipment.WEAPON_SLOT] ?: return false
            return item.definition.name.lowercase(Locale.getDefault()).equals("toxic blowpipe", ignoreCase = true)
        }

        fun zarytebow(p: Player?): Boolean {
            val item = p!!.equipment[Equipment.WEAPON_SLOT] ?: return false
            return item.definition.name.lowercase(Locale.getDefault()).equals("zaryte bow", ignoreCase = true)
        }

        /**
         * Determines if the player is wielding a dark bow.
         *
         * @param player
         * the player to determine for.
         * @return true if the player is wielding a dark bow.
         */
        @JvmStatic
        fun darkBow(player: Player): Boolean {
            val item = player.equipment[Equipment.WEAPON_SLOT] ?: return false
            return item.definition.name.lowercase(Locale.getDefault()).contains(
                "dark bow"
            )
        }

        /**
         * Determines if the player has arrows equipped.
         *
         * @param player
         * the player to determine for.
         * @return true if the player has arrows equipped.
         */
        fun arrowsEquipped(player: Player): Boolean {
            var item: Item
            return if (player.equipment[Equipment.AMMUNITION_SLOT].also { item = it } == null) {
                false
            } else !(!item.definition.name.endsWith("arrow") && !item.definition.name.endsWith(
                "arrowp"
            ) && !item.definition.name.endsWith(
                "arrow(p+)"
            ) && !item.definition.name.endsWith(
                "arrow(p++)"
            ))
        }

        /**
         * Determines if the player has bolts equipped.
         *
         * @param player
         * the player to determine for.
         * @return true if the player has bolts equipped.
         */
        fun boltsEquipped(player: Player): Boolean {
            var item: Item
            return if (player.equipment[Equipment.AMMUNITION_SLOT].also { item = it } == null) {
                false
            } else item.definition.name.lowercase(
                Locale.getDefault()
            ).contains("bolts")
        }

        /**
         * Attempts to poison the argued [CharacterEntity] with the argued
         * [PoisonType]. This method will have no effect if the entity is
         * already poisoned.
         *
         * @param entity
         * the entity that will be poisoned, if not already.
         * @param poisonType
         * the poison type that this entity is being inflicted with.
         */
        fun poisonEntity(entity: CharacterEntity, poisonType: Optional<PoisonType>) {

            // We are already poisoned or the poison type is invalid, do nothing.
            if (entity.isPoisoned || !poisonType.isPresent) {
                return
            }

            // If the entity is a player, we check for poison immunity. If they have
            // no immunity then we send them a message telling them that they are
            // poisoned.
            if (entity.isPlayer) {
                val player = entity as Player
                if (player.poisonImmunity > 0) return
                player.packetSender.sendMessage("You have been poisoned!")
            }
            entity.poisonDamage = poisonType.get().damage
            TaskManager.submit(CombatPoisonEffect(entity))
        }

        /**
         * Attempts to poison the argued [CharacterEntity] with the argued
         * [PoisonType]. This method will have no effect if the entity is
         * already poisoned.
         *
         * @param entity
         * the entity that will be poisoned, if not already.
         * @param poisonType
         * the poison type that this entity is being inflicted with.
         */
        @JvmStatic
        fun poisonEntity(entity: CharacterEntity?, poisonType: PoisonType?) {
            poisonEntity(entity!!, Optional.ofNullable(poisonType))
        }

        /**
         * Attempts to put the skull icon on the argued player, including the effect
         * where the player loses all item upon death. This method will have no
         * effect if the argued player is already skulled.
         *
         * @param player
         * the player to attempt to skull to.
         */
        @JvmStatic
        fun skullPlayer(player: Player) {

            // We are already skulled, return.
            if (player.skullTimer > 0) {
                return
            }

            // Otherwise skull the player as normal.
            player.skullTimer = 1200
            player.skullIcon = 1
            player.packetSender.sendMessage("@red@You have been skulled!")
            TaskManager.submit(CombatSkullEffect(player))
            player.updateFlag.flag(Flag.APPEARANCE)
        }

        /**
         * Calculates the combat level difference for wilderness player vs. player
         * combat.
         *
         * @param combatLevel
         * the combat level of the first person.
         * @param otherCombatLevel
         * the combat level of the other person.
         * @return the combat level difference.
         */
        @JvmStatic
        fun combatLevelDifference(
            combatLevel: Int, otherCombatLevel: Int
        ): Int {
            return if (combatLevel > otherCombatLevel) {
                combatLevel - otherCombatLevel
            } else if (otherCombatLevel > combatLevel) {
                otherCombatLevel - combatLevel
            } else {
                0
            }
        }

        fun getLevelDifference(player: Player, up: Boolean): Int {
            val max = if (player.location === Locations.Location.WILDERNESS) 126 else 138
            val wildLevel = player.wildernessLevel + 5 //+ 5 to make wild more active
            val combatLevel = player.skillManager.combatLevel
            val difference = if (up) combatLevel + wildLevel else combatLevel - wildLevel
            return if (difference < 3) 3 else if (difference > max && up) max else difference
        }

        /**
         * Generates a random [Hit] based on the argued entity's stats.
         *
         * @param entity
         * the entity to generate the random hit for.
         * @param victim
         * the victim being attacked.
         * @param type
         * the combat type being used.
         * @return the melee hit.
         */
        @JvmStatic
        fun getHit(entity: CharacterEntity, victim: CharacterEntity, type: CombatType, maxHit: Int): Hit {
            return when (type) {
                CombatType.MELEE -> Hit(
                    entity, if (maxHit == -1) Misc.inclusiveRandom(
                        1, getMeleeMaxHit(entity, victim)
                    ) else Misc.inclusiveRandom(maxHit), Hitmask.RED, CombatIcon.MELEE
                )
                CombatType.RANGED -> Hit(
                    entity, if (maxHit == -1) Misc.inclusiveRandom(
                        1, getRangedMaxHit(entity, victim)
                    ) else Misc.inclusiveRandom(maxHit), Hitmask.RED, CombatIcon.RANGED
                )
                CombatType.MAGIC -> Hit(
                    entity, if (maxHit == -1) Misc.inclusiveRandom(
                        1, getMagicMaxHit(entity)
                    ) else Misc.inclusiveRandom(maxHit), Hitmask.RED, CombatIcon.MAGIC
                )
                CombatType.DRAGON_FIRE -> Hit(
                    entity, if (maxHit == -1) Misc.inclusiveRandom(
                        0, getDragonFireMaxHit(entity, victim)
                    ) else Misc.inclusiveRandom(maxHit), Hitmask.RED, CombatIcon.MAGIC
                )
                else -> throw IllegalArgumentException("Invalid combat type: $type")
            }
        }

        /**
         * A flag that determines if the entity's attack will be successful based on
         * the argued attacker's and victim's stats.
         *
         * @param attacker
         * the attacker who's hit is being calculated for accuracy.
         * @param victim
         * the victim who's awaiting to either be hit or dealt no damage.
         * @param type
         * the type of combat being used to deal the hit.
         * @return true if the hit was successful, or in other words accurate.
         */
        @JvmStatic
        fun rollAccuracy(attacker: CharacterEntity, victim: CharacterEntity, type: CombatType): Boolean {
            var type = type
            if (attacker.isPlayer && victim.isPlayer) {
                val p1 = attacker as Player
                val p2 = victim as Player
                when (type) {
                    CombatType.MAGIC -> {
                        val mageAttk = getMagicAttack(p1)
                        return Misc.getRandom(getMagicDefence(p2)) < Misc.getRandom(
                            mageAttk / 2
                        ) + Misc.getRandom((mageAttk / 2.1).toInt())
                    }
                    CombatType.MELEE -> {
                        val def = 1 + getMeleeDefence(p2)
                        return Misc.getRandom(def) < Misc.getRandom(1 + getMeleeAttack(p1)) + def / 4.5
                    }
                    CombatType.RANGED -> return Misc.getRandom(10 + getRangedDefence(p2)) < Misc.getRandom(
                        15 + getRangedAttack(
                            p1
                        )
                    )
                }
            } else if (attacker.isPlayer && victim.isNpc && type !== CombatType.MAGIC) {
                val p1 = attacker as Player
                val n = victim as NPC
                when (type) {
                    CombatType.MELEE -> {
                        val def = 1 + n.definition.defenceMelee
                        return Misc.getRandom(def) < Misc.getRandom(5 + getMeleeAttack(p1)) + def / 4
                    }
                    CombatType.RANGED -> return Misc.getRandom(5 + n.definition.defenceRange) < Misc.getRandom(
                        5 + getRangedAttack(
                            p1
                        )
                    )
                }
            }
            var veracEffect = false
            if (type === CombatType.MELEE) {
                if (fullVeracs(attacker)) {
                    if (Misc.RANDOM.nextInt(8) == 3) {
                        veracEffect = true
                    }
                }
            }
            if (type === CombatType.DRAGON_FIRE) type = CombatType.MAGIC
            var prayerMod = 1.0
            var equipmentBonus = 1.0
            var specialBonus = 1.0
            var styleBonus = 0
            var bonusType = -1
            if (attacker.isPlayer) {
                val player = attacker as Player
                equipmentBonus =
                    if (type === CombatType.MAGIC) player.bonusManager.attackBonus[BonusManager.ATTACK_MAGIC] else player.bonusManager.attackBonus[player.fightType.bonusType]
                bonusType = player.fightType.correspondingBonus
                if (type === CombatType.MELEE) {
                    if (PrayerHandler.isActivated(
                            player, PrayerHandler.CLARITY_OF_THOUGHT
                        )
                    ) {
                        prayerMod = 1.05
                    } else if (PrayerHandler.isActivated(
                            player, PrayerHandler.IMPROVED_REFLEXES
                        )
                    ) {
                        prayerMod = 1.10
                    } else if (PrayerHandler.isActivated(
                            player, PrayerHandler.INCREDIBLE_REFLEXES
                        )
                    ) {
                        prayerMod = 1.15
                    } else if (PrayerHandler.isActivated(
                            player, PrayerHandler.CHIVALRY
                        )
                    ) {
                        prayerMod = 1.15
                    } else if (PrayerHandler.isActivated(
                            player, PrayerHandler.PIETY
                        )
                    ) {
                        prayerMod = 1.20
                    } else if (PrayerHandler.isActivated(
                            player, PrayerHandler.RIGOUR
                        )
                    ) {
                        prayerMod = 1.20
                    } else if (PrayerHandler.isActivated(
                            player, PrayerHandler.AUGURY
                        )
                    ) {
                        prayerMod = 1.20
                    } else if (CurseHandler.isActivated(player, CurseHandler.LEECH_ATTACK)) {
                        prayerMod = 1.05 + +(player.leechedBonuses[0] * 0.01)
                    } else if (CurseHandler.isActivated(player, CurseHandler.TURMOIL)) {
                        prayerMod = 1.15 + +(player.leechedBonuses[2] * 0.01)
                    }
                } else if (type === CombatType.RANGED) {
                    if (PrayerHandler.isActivated(player, PrayerHandler.SHARP_EYE)) {
                        prayerMod = 1.05
                    } else if (PrayerHandler.isActivated(player, PrayerHandler.HAWK_EYE)) {
                        prayerMod = 1.10
                    } else if (PrayerHandler.isActivated(player, PrayerHandler.EAGLE_EYE)) {
                        prayerMod = 1.15
                    } else if (PrayerHandler.isActivated(player, PrayerHandler.RIGOUR)) {
                        prayerMod = 1.22
                    } else if (CurseHandler.isActivated(player, CurseHandler.LEECH_RANGED)) {
                        prayerMod = 1.05 + +(player.leechedBonuses[4] * 0.01)
                    }
                } else if (type === CombatType.MAGIC) {
                    if (PrayerHandler.isActivated(player, PrayerHandler.MYSTIC_WILL)) {
                        prayerMod = 1.05
                    } else if (PrayerHandler.isActivated(player, PrayerHandler.MYSTIC_LORE)) {
                        prayerMod = 1.10
                    } else if (PrayerHandler.isActivated(player, PrayerHandler.MYSTIC_MIGHT)) {
                        prayerMod = 1.15
                    } else if (PrayerHandler.isActivated(player, PrayerHandler.AUGURY)) {
                        prayerMod = 1.22
                    } else if (CurseHandler.isActivated(player, CurseHandler.LEECH_MAGIC)) {
                        prayerMod = 1.05 + +(player.leechedBonuses[6] * 0.01)
                    }
                }
                if (player.fightType.style === FightStyle.ACCURATE) {
                    styleBonus = 3
                } else if (player.fightType.style === FightStyle.CONTROLLED) {
                    styleBonus = 1
                }
                if (player.isSpecialActivated) {
                    specialBonus = player.combatSpecial.accuracyBonus
                }
            }
            var attackCalc = Math.floor(equipmentBonus + attacker.getBaseAttack(type)) + 8
            attackCalc *= prayerMod
            attackCalc += styleBonus.toDouble()
            if (equipmentBonus < -67) {
                attackCalc = if (Misc.exclusiveRandom(8) == 0) attackCalc else 0.0
            }
            attackCalc *= specialBonus
            equipmentBonus = 1.0
            prayerMod = 1.0
            styleBonus = 0
            if (victim.isPlayer) {
                val player = victim as Player
                equipmentBonus = if (bonusType == -1) {
                    if (type === CombatType.MAGIC) player.bonusManager.defenceBonus[BonusManager.DEFENCE_MAGIC] else player.skillManager.getCurrentLevel(
                        Skill.DEFENCE
                    ).toDouble()
                } else {
                    if (type === CombatType.MAGIC) player.bonusManager.defenceBonus[BonusManager.DEFENCE_MAGIC] else player.bonusManager.defenceBonus[bonusType]
                }
                if (PrayerHandler.isActivated(player, PrayerHandler.THICK_SKIN)) {
                    prayerMod = 1.05
                } else if (PrayerHandler.isActivated(player, PrayerHandler.ROCK_SKIN)) {
                    prayerMod = 1.10
                } else if (PrayerHandler.isActivated(player, PrayerHandler.STEEL_SKIN)) {
                    prayerMod = 1.15
                } else if (PrayerHandler.isActivated(player, PrayerHandler.CHIVALRY)) {
                    prayerMod = 1.20
                } else if (PrayerHandler.isActivated(player, PrayerHandler.PIETY)) {
                    prayerMod = 1.25
                } else if (PrayerHandler.isActivated(player, PrayerHandler.RIGOUR)) {
                    prayerMod = 1.25
                } else if (PrayerHandler.isActivated(player, PrayerHandler.AUGURY)) {
                    prayerMod = 1.25
                } else if (CurseHandler.isActivated(player, CurseHandler.LEECH_DEFENCE)) {
                    prayerMod = 1.05 + +(player.leechedBonuses[1] * 0.01)
                } else if (CurseHandler.isActivated(
                        player, CurseHandler.TURMOIL
                    )
                ) {
                    prayerMod = 1.15 + +(player.leechedBonuses[1] * 0.01)
                }
                if (player.fightType.style === FightStyle.DEFENSIVE) {
                    styleBonus = 3
                } else if (player.fightType.style === FightStyle.CONTROLLED) {
                    styleBonus = 1
                }
            }
            var defenceCalc = Math.floor(equipmentBonus + victim.getBaseDefence(type)) + 8
            defenceCalc *= prayerMod
            defenceCalc += styleBonus.toDouble()
            if (equipmentBonus < -67) {
                defenceCalc = if (Misc.exclusiveRandom(8) == 0) defenceCalc else 0.0
            }
            if (veracEffect) {
                defenceCalc = 0.0
            }
            val A = Math.floor(attackCalc)
            val D = Math.floor(defenceCalc)
            var hitSucceed = if (A < D) (A - 1.0) / (2.0 * D) else 1.0 - (D + 1.0) / (2.0 * A)
            hitSucceed = if (hitSucceed >= 1.0) 0.99 else if (hitSucceed <= 0.0) 0.01 else hitSucceed
            return hitSucceed >= Misc.RANDOM.nextDouble()
        }

        /**
         * Calculates the maximum melee hit for the argued [CharacterEntity] without
         * taking the victim into consideration.
         *
         * @param entity
         * the entity to calculate the maximum hit for.
         * @param victim
         * the victim being attacked.
         * @return the maximum melee hit that this entity can deal.
         */
        @JvmStatic
        fun calculateMaxMeleeHit(entity: CharacterEntity, victim: CharacterEntity): Int {
            var maxHit = 0
            if (entity.isNpc) {
                val npc = entity as NPC
                maxHit = npc.definition.maxHit
                if (npc.strengthWeakened[0]) {
                    maxHit -= (0.10 * maxHit).toInt()
                } else if (npc.strengthWeakened[1]) {
                    maxHit -= (0.20 * maxHit).toInt()
                } else if (npc.strengthWeakened[2]) {
                    maxHit -= (0.30 * maxHit).toInt()
                }
                /** CUSTOM NPCS  */
                if (npc.id == 2026) { //Dharok the wretched
                    maxHit += ((npc.defaultConstitution - npc.constitution) * 0.2).toInt()
                }
                return maxHit
            }
            val player = entity as Player
            var specialMultiplier = 1.0
            var prayerMultiplier = 1.0
            // TODO: void melee = 1.2, slayer helm = 1.15, salve amulet = 1.15, // salve amulet(e) = 1.2
            var otherBonusMultiplier = 1.0
            var voidDmgBonus = 1.0
            val strengthLevel = player.skillManager.getCurrentLevel(Skill.STRENGTH)
            val attackLevel = player.skillManager.getCurrentLevel(Skill.ATTACK)
            var combatStyleBonus = 0
            if (PrayerHandler.isActivated(player, PrayerHandler.BURST_OF_STRENGTH)) {
                prayerMultiplier = 1.05
            } else if (PrayerHandler.isActivated(
                    player, PrayerHandler.SUPERHUMAN_STRENGTH
                )
            ) {
                prayerMultiplier = 1.1
            } else if (PrayerHandler.isActivated(
                    player, PrayerHandler.ULTIMATE_STRENGTH
                )
            ) {
                prayerMultiplier = 1.15
            } else if (PrayerHandler.isActivated(
                    player, PrayerHandler.CHIVALRY
                )
            ) {
                prayerMultiplier = 1.18
            } else if (PrayerHandler.isActivated(
                    player, PrayerHandler.PIETY
                )
            ) {
                prayerMultiplier = 1.23
            } else if (CurseHandler.isActivated(player, CurseHandler.LEECH_STRENGTH)) {
                prayerMultiplier = 1.05 + +(player.leechedBonuses[2] * 0.01)
            } else if (CurseHandler.isActivated(player, CurseHandler.TURMOIL)) {
                prayerMultiplier = 1.23 + player.leechedBonuses[2] * 0.01
            }
            when (player.fightType.style) {
                FightStyle.AGGRESSIVE -> combatStyleBonus = 3
                FightStyle.CONTROLLED -> combatStyleBonus = 1
            }
            if (EquipmentBonus.voidMelee(player)) { //, CombatType.MELEE)) {
                voidDmgBonus = 1.1
                if (player.currentClanChat != null && player.currentClanChat.name.equals("debug", ignoreCase = true)) {
                    player.packetSender.sendMessage("Void buff applied")
                }
            }
            if (strengthLevel <= 10 || attackLevel <= 10) {
                otherBonusMultiplier = 1.8
            }
            val effectiveStrengthDamage =
                (strengthLevel * prayerMultiplier * otherBonusMultiplier + combatStyleBonus).toInt()
            val baseDamage =
                1.3 + effectiveStrengthDamage / 10 + player.bonusManager.otherBonus[BonusManager.BONUS_STRENGTH] / 80 + effectiveStrengthDamage * player.bonusManager.otherBonus[BonusManager.BONUS_STRENGTH] / 640
            if (player.isSpecialActivated) {
                specialMultiplier = player.combatSpecial.strengthBonus
            }
            maxHit = (baseDamage * specialMultiplier * voidDmgBonus).toInt()
            maxHit *= 10
            if (fullDharoks(player)) {
                maxHit += ((player.skillManager.getMaxLevel(Skill.CONSTITUTION) - player.skillManager.getCurrentLevel(
                    Skill.CONSTITUTION
                )) * 0.35).toInt()
            }
            if (victim.isNpc) {
                val npc = victim as NPC
                if (npc.defenceWeakened[0]) {
                    maxHit += (0.10 * maxHit).toInt()
                } else if (npc.defenceWeakened[1]) {
                    maxHit += (0.20 * maxHit).toInt()
                } else if (npc.defenceWeakened[2]) {
                    maxHit += (0.30 * maxHit).toInt()
                }

                //OBSIDIAN SHIT
                if (EquipmentBonus.berserkerNecklaceEffect(player)) {
                    maxHit *= 1.2.toInt()
                }
                /** SLAYER HELMET  */
                if (player.slayer.isSlayerTask(npc)) {
                    if (player.equipment.items[Equipment.HEAD_SLOT].id == 13263 || player.equipment.items[Equipment.HEAD_SLOT].id == 15492) {
                        maxHit *= 1.13.toInt()
                    }
                }
            }
            return maxHit
        }

        /**
         * Calculates the maximum ranged hit for the argued [CharacterEntity] without
         * taking the victim into consideration.
         *
         * @param entity
         * the entity to calculate the maximum hit for.
         * @param victim
         * the victim being attacked.
         * @return the maximum ranged hit that this entity can deal.
         */
        @JvmStatic
        fun getRangedMaxHit(entity: CharacterEntity, victim: CharacterEntity?): Int {
            var maxHit = 0
            if (entity.isNpc) {
                val npc = entity as NPC
                maxHit = npc.definition.maxHit
                if (npc.strengthWeakened[0]) {
                    maxHit -= (0.10 * maxHit).toInt()
                } else if (npc.strengthWeakened[1]) {
                    maxHit -= (0.20 * maxHit).toInt()
                } else if (npc.strengthWeakened[2]) {
                    maxHit -= (0.30 * maxHit).toInt()
                }
                return maxHit
            }
            val player = entity as Player
            var specialMultiplier = 1.0
            val prayerMultiplier = 1.0
            val otherBonusMultiplier = 1.0
            var voidDmgBonus = 1.0
            var rangedStrength = player.bonusManager.otherBonus[BonusManager.RANGED_STRENGTH].toInt()
            if (player.rangedWeaponData != null) rangedStrength += RangedWeaponData.getAmmunitionData(player).strength
            val rangeLevel = player.skillManager.getCurrentLevel(Skill.RANGED)
            var combatStyleBonus = 0
            when (player.fightType.style) {
                FightStyle.ACCURATE -> combatStyleBonus = 3
            }
            if (EquipmentBonus.voidRange(player)) { //, CombatType.RANGED)) {
                if (player.currentClanChat != null && player.currentClanChat.name.equals("debug", ignoreCase = true)) {
                    player.packetSender.sendMessage("Void buff applied")
                }
                voidDmgBonus = 1.2
            }
            val effectiveRangeDamage = (rangeLevel * prayerMultiplier * otherBonusMultiplier + combatStyleBonus).toInt()
            val baseDamage =
                1.3 + effectiveRangeDamage / 10 + rangedStrength / 80 + effectiveRangeDamage * rangedStrength / 640
            if (player.isSpecialActivated) {
                specialMultiplier = player.combatSpecial.strengthBonus
            }
            maxHit = (baseDamage * specialMultiplier * voidDmgBonus).toInt()
            if (!player.checkItem(Equipment.WEAPON_SLOT, 20171)) {
                if (player.curseActive[PrayerHandler.SHARP_EYE] || player.curseActive[CurseHandler.SAP_RANGER]) {
                    maxHit *= 1.05.toInt()
                } else if (player.prayerActive[PrayerHandler.HAWK_EYE]) {
                    maxHit *= 1.10.toInt()
                } else if (player.prayerActive[PrayerHandler.EAGLE_EYE]) {
                    maxHit *= 1.15.toInt()
                } else if (player.prayerActive[PrayerHandler.RIGOUR]) {
                    maxHit *= 1.22.toInt()
                } else if (player.curseActive[CurseHandler.LEECH_RANGED]) {
                    maxHit *= 1.10.toInt()
                }
            }
            if (victim != null && victim.isNpc) {
                val npc = victim as NPC
                if (npc.defenceWeakened[0]) {
                    maxHit += (0.10 * maxHit).toInt()
                } else if (npc.defenceWeakened[1]) {
                    maxHit += (0.20 * maxHit).toInt()
                } else if (npc.defenceWeakened[2]) {
                    maxHit += (0.30 * maxHit).toInt()
                }
                /** SLAYER HELMET  */
                if (player.slayer.isSlayerTask(npc)) {
                    if (player.equipment.items[Equipment.HEAD_SLOT].id == 15492 || player.equipment.items[Equipment.HEAD_SLOT].id == 15490) {
                        maxHit *= 1.13.toInt()
                    }
                }
            }
            maxHit *= 10
            return maxHit
        }

        fun getDragonFireMaxHit(e: CharacterEntity, v: CharacterEntity): Int {
            var baseMax = 250
            if (e.isNpc && v.isPlayer) {
                val victim = v as Player
                val npc = e as NPC
                baseMax = (npc.definition.maxHit * 2.5).toInt()
                if (victim.fireImmunity > 0 || victim.equipment.items[Equipment.SHIELD_SLOT].id == 1540 || victim.equipment.items[Equipment.SHIELD_SLOT].id == 11283 || victim.equipment.items[Equipment.SHIELD_SLOT].id == 13655) {
                    if (victim.fireDamageModifier >= 1 && (victim.equipment.items[Equipment.SHIELD_SLOT].id == 1540 || victim.equipment.items[Equipment.SHIELD_SLOT].id == 13655 || victim.equipment.items[Equipment.SHIELD_SLOT].id == 11283)) {
                        /*if(victim.getClanChatName().equalsIgnoreCase("Debug")) {
						victim.getPacketSender().sendMessage("You block 100% of the fire from potion + shield");
					}*/
                        return 0
                    } else if (victim.fireDamageModifier >= 1) {
                        /*if(victim.getClanChatName().equalsIgnoreCase("Debug")) {
						victim.getPacketSender().sendMessage("The potion sets fire's max hit to 120.");
					}*/
                        victim.packetSender.sendMessage("Your potion protects against some of the dragon's fire.")
                        return 120
                    } else if (victim.equipment.items[Equipment.SHIELD_SLOT].id == 1540 || victim.equipment.items[Equipment.SHIELD_SLOT].id == 13655 || victim.equipment.items[Equipment.SHIELD_SLOT].id == 11283) {
                        /*if(victim.getClanChatName().equalsIgnoreCase("Debug")) {
						victim.getPacketSender().sendMessage("Your shield sets the max fire hit to 120.");
					}*/
                        return 120
                    }
                }
            }
            if (baseMax > 450) {
                baseMax = 450 + Misc.getRandom(9)
            }
            return baseMax
        }
        // /**
        // * The percentage of the hit reducted by antifire.
        // */
        // protected static double dragonfireReduction(Mob mob) {
        // boolean dragonfireShield = mob.getEquipment() != null
        // && (mob.getEquipment().contains(1540)
        // || mob.getEquipment().contains(11283)
        // || mob.getEquipment().contains(11284) || mob
        // .getEquipment().contains(11285));
        // boolean dragonfirePotion = false;
        // boolean protectPrayer = mob.getCombatState().getPrayer(
        // CombatPrayer.PROTECT_FROM_MAGIC);
        // if (dragonfireShield && dragonfirePotion) {
        // if (mob.getActionSender() != null) {
        // mob.getActionSender().sendMessage(
        // "You shield absorbs most of the dragon fire!");
        // mob.getActionSender()
        // .sendMessage(
        // "Your potion protects you from the heat of the dragon's breath!");
        // }
        // return 1;
        // } else if (dragonfireShield) {
        // if (mob.getActionSender() != null) {
        // mob.getActionSender().sendMessage(
        // "You shield absorbs most of the dragon fire!");
        // }
        // return 0.8; // 80%
        // } else if (dragonfirePotion) {
        // if (mob.getActionSender() != null) {
        // mob.getActionSender()
        // .sendMessage(
        // "Your potion protects you from the heat of the dragon's breath!");
        // }
        // return 0.8; // 80%
        // } else if (protectPrayer) {
        // if (mob.getActionSender() != null) {
        // mob.getActionSender().sendMessage(
        // "Your prayers resist some of the dragon fire.");
        // }
        // return 0.6; // 60%
        // }
        // return /* mob.getEquipment() != null */0;
        // }s
        /**
         * A series of checks performed before the entity attacks the victim.
         *
         *
         * the builder to perform the checks with.
         * @return true if the entity passed the checks, false if they did not.
         */
        @JvmStatic
        fun checkHook(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {

            // Check if we need to reset the combat session.
            if (!victim!!.isRegistered || !entity!!.isRegistered || entity.constitution <= 0 || victim.constitution <= 0) {
                entity!!.combatBuilder.reset(true)
                return false
            }

            // Here we check if the victim has teleported away.
            if (victim.isPlayer) {
                if ((victim as Player?)!!.isTeleporting || !Locations.Location.ignoreFollowDistance(entity) && !Locations.goodDistance(
                        victim.position, entity!!.position, 40
                    ) || (victim as Player?)!!.isPlayerLocked
                ) {
                    entity!!.combatBuilder.cooldown = 10
                    entity.movementQueue.followCharacter = null
                    return false
                }
            }
            if (victim.isPlayer && entity!!.isPlayer && zarytebow(entity as Player?) && victim != null && entity != null && entity.location !== Locations.Location.FREE_FOR_ALL_ARENA) {
                //	((Player)entity).getPacketSender().sendMessage("Zaryte bow is disabled in PvP");
                //   entity.getCombatBuilder().testReset(true);
                return false
            }
            if (victim.isNpc && entity!!.isPlayer) {
                val npc = victim as NPC?
                if (npc!!.spawnedFor != null && npc.spawnedFor.index != (entity as Player?)!!.index) {
                    (entity as Player?)!!.packetSender.sendMessage("That's not your enemy to fight.")
                    entity.combatBuilder.reset(true)
                    return false
                }
                if (npc.isSummoningNpc) {
                    val player = entity as Player?
                    if (player!!.location !== Locations.Location.WILDERNESS) {
                        player!!.packetSender.sendMessage("You can only attack familiars in the wilderness.")
                        player.combatBuilder.reset(true)
                        return false
                    } else if (npc.location !== Locations.Location.WILDERNESS) {
                        player!!.packetSender.sendMessage("That familiar is not in the wilderness.")
                        player.combatBuilder.reset(true)
                        return false
                    }
                    /** DEALING DMG TO THEIR OWN FAMILIAR  */
                    if (player!!.summoning.familiar != null && player.summoning.familiar.summonNpc != null && player.summoning.familiar.summonNpc.index == npc.index) {
                        return false
                    }
                }
                if (Nex.nexMob(npc.id) || npc.id == 6260 || npc.id == 6261 || npc.id == 6263 || npc.id == 6265 || npc.id == 6222 || npc.id == 6223 || npc.id == 6225 || npc.id == 6227 || npc.id == 6203 || npc.id == 6208 || npc.id == 6204 || npc.id == 6206 || npc.id == 6247 || npc.id == 6248 || npc.id == 6250 || npc.id == 6252) {
                    if (!(entity as Player?)!!.minigameAttributes.godwarsDungeonAttributes.hasEnteredRoom()) {
                        (entity as Player?)!!.packetSender.sendMessage("You must enter the room before being able to attack.")
                        entity.combatBuilder.reset(true)
                        return false
                    }
                }
                if (npc.id == 2001) {
                    if (!Scorpia.attackable()) {
                        (entity as Player?)!!.packetSender.sendMessage("Scorpia cannot be attacked until its babies are dead.")
                        entity.combatBuilder.reset(true)
                        return false
                    }
                } else if (npc.id == 2891) {
                    for (i in 0..3) {
                        if (!((entity as Player?)!!.regionInstance as KrakenInstance).disturbedPool(i)) {
                            (entity as Player?)!!.packetSender.sendMessage("You need to disturb all the small whirpools first.")
                            entity.combatBuilder.reset(true)
                            return false
                        }
                    }
                }
                if (Nex.nexMob(npc.id)) {
                    if (!Nex.checkAttack(entity as Player?, npc.id)) {
                        entity.combatBuilder.reset(true)
                        return false
                    }
                } else if (npc.id == 6222) { //Kree'arra
                    if (entity.combatBuilder.strategy.getCombatType(entity) === CombatType.MELEE) {
                        (entity as Player?)!!.packetSender.sendMessage("Kree'arra is resistant to melee attacks.")
                        entity.combatBuilder.testReset(true)
                        return false
                    }
                }
                if (npc.location !== Locations.Location.DUNGEONEERING && npc.definition.slayerLevel > (entity as Player?)!!.skillManager.getCurrentLevel(
                        Skill.SLAYER
                    ) && !(entity as Player?)!!.slayer.isSlayerTask(npc)
                ) {
                    (entity as Player?)!!.packetSender.sendMessage("You need a Slayer level of at least " + npc.definition.slayerLevel + " to attack this creature.")
                    entity.combatBuilder.reset(true)
                    return false
                }
                if (npc.id == 6715 || npc.id == 6716 || npc.id == 6701 || npc.id == 6725 || npc.id == 6691) {
                    if (entity.location !== Locations.Location.WILDERNESS) {
                        (entity as Player?)!!.packetSender.sendMessage("You cannot reach that.")
                        entity.combatBuilder.reset(true)
                        return false
                    }
                }
                if (npc.id == 4291 && entity.position.z == 2 && !(entity as Player?)!!.minigameAttributes.warriorsGuildAttributes.enteredTokenRoom()) {
                    (entity as Player?)!!.packetSender.sendMessage("You cannot reach that.")
                    entity.combatBuilder.reset(true)
                    return false
                }
            }

            // Here we check if we are already in combat with another entity.
            if (entity!!.combatBuilder.lastAttacker != null && !Locations.inMulti(entity) && entity.combatBuilder.isBeingAttacked && victim != entity.combatBuilder.lastAttacker) {
                if (entity.isPlayer) (entity as Player?)!!.packetSender.sendMessage("You are already under attack!")
                entity.combatBuilder.reset(true)
                return false
            }

            // Here we check if the entity we are attacking is already in
            // combat.
            if (!(entity.isNpc && (entity as NPC?)!!.isSummoningNpc)) {
                var allowAttack = false
                if (victim.combatBuilder.lastAttacker != null && !Locations.inMulti(entity) && victim.combatBuilder.isBeingAttacked && victim.combatBuilder.lastAttacker != entity) {
                    if (victim.combatBuilder.lastAttacker.isNpc) {
                        val npc = victim.combatBuilder.lastAttacker as NPC
                        if (npc.isSummoningNpc) {
                            if (entity.isPlayer) {
                                val player = entity as Player?
                                if (player!!.summoning.familiar != null && player.summoning.familiar.summonNpc != null && player.summoning.familiar.summonNpc.index == npc.index) {
                                    player.packetSender.sendMessage("Summoning only works in multi for now...")
                                    allowAttack = false
                                    // getting source tree to detect this zzz.
                                }
                            }
                        }
                    }
                    if (!allowAttack) {
                        if (entity.isPlayer) (entity as Player?)!!.packetSender.sendMessage(
                            "They are already under attack!"
                        )
                        entity.combatBuilder.reset(true)
                        return false
                    }
                }
            }

            // Check if the victim is still in the wilderness, and check if the
            if (entity.isPlayer) {
                if (victim.isPlayer) {
                    if (!properLocation(entity as Player?, victim as Player?)) {
                        entity.combatBuilder.reset(true)
                        entity.positionToFace = victim.position
                        return false
                    }
                }
                if ((entity as Player?)!!.isCrossingObstacle) {
                    entity.combatBuilder.reset(true)
                    return false
                }
            }


            // Check if the npc needs to retreat.
            if (entity.isNpc) {
                val n = entity as NPC?
                if (!Locations.Location.ignoreFollowDistance(n) && !Nex.nexMob(n!!.id) && !n.isSummoningNpc) { //Stops combat for npcs if too far away
                    if (n.position.isWithinDistance(victim.position, 1)) {
                        return true
                    }
                    if (!n.position.isWithinDistance(
                            n.defaultPosition, 10 + n.movementCoordinator.coordinator.radius
                        )
                    ) {
                        n.movementQueue.reset()
                        n.movementCoordinator.coordinateState = CoordinateState.AWAY
                        return false
                    }
                }
            }
            return true
        }

        /**
         * Checks if the entity is close enough to attack.
         *
         * @param builder
         * the builder used to perform the check.
         * @return true if the entity is close enough to attack, false otherwise.
         */
        fun checkAttackDistance(builder: CombatBuilder): Boolean {
            return checkAttackDistance(builder.character, builder.victim)
        }

        @JvmStatic
        fun checkAttackDistance(a: CharacterEntity, b: CharacterEntity): Boolean {
            val attacker = a.position
            val victim = b.position
            if (a.isNpc && (a as NPC).isSummoningNpc) {
                return Locations.goodDistance(attacker, victim, a.getSize())
            }
            if (a.combatBuilder.strategy == null) a.combatBuilder.determineStrategy()
            val strategy = a.combatBuilder.strategy
            var distance = strategy.attackDistance(a)
            if (a.isPlayer && strategy.getCombatType(a) !== CombatType.MELEE) {
                if (b.size >= 2) distance += b.size - 1
            }
            val movement = a.movementQueue
            val otherMovement = b.movementQueue

            // We're moving so increase the distance.
            if (!movement.isMovementDone && !otherMovement.isMovementDone && !movement.isLockMovement && !a.isFrozen) {
                distance += 1

                // We're running so increase the distance even more.
                // XXX: Might have to change this back to 1 or even remove it, not
                // sure what it's like on actual runescape. Are you allowed to
                // attack when the entity is trying to run away from you?
                if (movement.isRunToggled) {
                    distance += 2
                }
            }

            /*
		 *  Clipping checks and diagonal blocking by gabbe
		 */
            val sameSpot = attacker == victim && !a.movementQueue.isMoving && !b.movementQueue.isMoving
            val goodDistance = !sameSpot && Locations.goodDistance(attacker.x, attacker.y, victim.x, victim.y, distance)
            var projectilePathBlocked = false
            if (a.isPlayer && (strategy.getCombatType(a) === CombatType.RANGED || strategy.getCombatType(a) === CombatType.MAGIC && (a as Player).castSpell != null && a.castSpell !is CombatAncientSpell) || a.isNpc && strategy.getCombatType(
                    a
                ) === CombatType.MELEE
            ) {
                if (!RegionClipping.canProjectileAttack(b, a)) projectilePathBlocked = true
            }
            if (!projectilePathBlocked && goodDistance) {
                if (strategy.getCombatType(a) === CombatType.MELEE && RegionClipping.isInDiagonalBlock(b, a)) {
                    PathFinder.findPath(a, victim.x, victim.y + 1, true, 1, 1)
                    return false
                } else a.movementQueue.reset()
                return true
            } else if (projectilePathBlocked || !goodDistance) {
                a.movementQueue.followCharacter = b
                return false
            }
            // Check if we're within the required distance.
            return attacker.isWithinDistance(victim, distance)
        }

        /**
         * Applies combat prayer effects to the calculated hits.
         *
         * @param container
         * the combat container that holds the hits.
         * @param builder
         * the builder to apply prayer effects to.
         */
        protected fun applyPrayerProtection(container: CombatContainer, builder: CombatBuilder) {

            // If we aren't checking the accuracy, then don't bother doing any of
            // this.
            if (!container.isCheckAccuracy || builder.victim == null) {
                return
            }

            // The attacker is an npc, and the victim is a player so we completely
            // cancel the hits if the right prayer is active.
            if (builder.victim.isPlayer) {
                val victim = builder.victim as Player
                if (victim.equipment.items[Equipment.SHIELD_SLOT].id == 13740 || victim.equipment.items[Equipment.SHIELD_SLOT].id == 13742) {
                    if (Misc.getRandom(10) <= 7) {
                        container.allHits { context: ContainerHit ->
                            if (PrayerHandler.isActivated(
                                    victim, PrayerHandler.getProtectingPrayer(container.combatType)
                                ) || CurseHandler.isActivated(
                                    victim, CurseHandler.getProtectingPrayer(container.combatType)
                                )
                            ) {
                                return@allHits   //we don't want to do the calculation now if they are praying against the right style.
                            }
                            if (context.hit.damage > 10) {
                                if (victim.skillManager.getCurrentLevel(Skill.PRAYER) > 0) {
                                    val prayerLost = (context.hit.damage * 0.1).toInt()
                                    if (victim.skillManager.getCurrentLevel(Skill.PRAYER) >= prayerLost) {
                                        context.hit.incrementAbsorbedDamage((context.hit.damage - context.hit.damage * 0.75).toInt())
                                        victim.skillManager.setCurrentLevel(
                                            Skill.PRAYER, victim.skillManager.getCurrentLevel(Skill.PRAYER) - prayerLost
                                        )
                                        if (victim.isSpiritDebug) {
                                            victim.packetSender.sendMessage("Your spirit shield has drained " + prayerLost + " prayer points to absorb " + (context.hit.damage - context.hit.damage * 0.75).toInt() + " damage.")
                                        }
                                    }
                                }
                            } else {
                                if (victim.isSpiritDebug) {
                                    victim.packetSender.sendMessage("Spirit Shield did not activate as damage was under 10.")
                                }
                            }
                        }
                    } else {
                        if (victim.isSpiritDebug) {
                            victim.packetSender.sendMessage("Your shield was not in the 70% RNG required to activate it.")
                        }
                    }
                }
                if (builder.character.isNpc) {
                    val attacker = builder.character as NPC
                    // Except for verac of course :)
                    if (attacker.id == 2030) {
                        return
                    }
                    // It's not verac so we cancel all of the hits.
                    if (PrayerHandler.isActivated(
                            victim, PrayerHandler.getProtectingPrayer(container.combatType)
                        ) || CurseHandler.isActivated(victim, CurseHandler.getProtectingPrayer(container.combatType))
                    ) {
                        container.allHits { context: ContainerHit ->
                            val hit = context.hit.damage
                            if (attacker.id == 2745) { //Jad
                                context.isAccurate = false
                                context.hit.incrementAbsorbedDamage(hit)
                            } else {
                                //now that we know they're praying, check if they also have the spirit shield.
                                if (victim.equipment.items[Equipment.SHIELD_SLOT].id == 13740 || victim.equipment.items[Equipment.SHIELD_SLOT].id == 13742) {
                                    if (victim.isSpiritDebug) {
                                        victim.packetSender.sendMessage("Original DMG: " + context.hit.damage)
                                    }
                                    val reduceRatio = if (attacker.id == 1158 || attacker.id == 1160) 0.4 else 0.8
                                    var mod = Math.abs(1 - reduceRatio)
                                    context.hit.incrementAbsorbedDamage((hit - hit * mod).toInt())
                                    mod = Math.round(Misc.RANDOM.nextDouble() * 100.0) / 100.0
                                    if (mod <= PRAYER_ACCURACY_REDUCTION) {
                                        context.isAccurate = false
                                    }
                                    if (victim.isSpiritDebug) {
                                        victim.packetSender.sendMessage("Prayer method finished. New DMG: " + context.hit.damage + " | total absorbed: " + context.hit.absorb)
                                    }
                                    if (Misc.getRandom(10) <= 7) {
                                        if (context.hit.damage > 10) {
                                            if (victim.skillManager.getCurrentLevel(Skill.PRAYER) > 0) {
                                                val prayerLost = (context.hit.damage * 0.1).toInt()
                                                if (victim.skillManager.getCurrentLevel(Skill.PRAYER) >= prayerLost) {
                                                    context.hit.incrementAbsorbedDamage((context.hit.damage - context.hit.damage * 0.75).toInt())
                                                    victim.skillManager.setCurrentLevel(
                                                        Skill.PRAYER,
                                                        victim.skillManager.getCurrentLevel(Skill.PRAYER) - prayerLost
                                                    )
                                                    if (victim.isSpiritDebug) {
                                                        victim.packetSender.sendMessage("Your spirit shield has drained " + prayerLost + " prayer points to absorb " + (context.hit.damage - context.hit.damage * 0.75).toInt() + " damage.")
                                                    }
                                                }
                                            }
                                        } else {
                                            if (victim.isSpiritDebug) {
                                                victim.packetSender.sendMessage("Spirit Shield did not activate as damage was under 10.")
                                            }
                                        }
                                    } else {
                                        if (victim.isSpiritDebug) {
                                            victim.packetSender.sendMessage("Your shield was not in the 70% RNG required to activate it.")
                                        }
                                    }
                                }
                                val reduceRatio = if (attacker.id == 1158 || attacker.id == 1160) 0.4 else 0.8
                                var mod = Math.abs(1 - reduceRatio)
                                context.hit.incrementAbsorbedDamage((hit - hit * mod).toInt())
                                mod = Math.round(Misc.RANDOM.nextDouble() * 100.0) / 100.0
                                if (mod <= PRAYER_ACCURACY_REDUCTION) {
                                    context.isAccurate = false
                                }
                            }
                        }
                    }
                } else if (builder.character.isPlayer) {
                    val attacker = builder.character as Player
                    // If wearing veracs, the attacker will hit through prayer
                    // protection.
                    if (fullVeracs(attacker)) {
                        return
                    }

                    // They aren't wearing veracs so lets reduce the accuracy and hits.
                    if (PrayerHandler.isActivated(
                            victim, PrayerHandler.getProtectingPrayer(container.combatType)
                        ) || CurseHandler.isActivated(victim, CurseHandler.getProtectingPrayer(container.combatType))
                    ) {
                        //PLAYER TO PLAYER EVENTS
                        container.allHits { context: ContainerHit ->
                            // First reduce the damage.
                            val hit = context.hit.damage
                            var mod = Math.abs(1 - 0.5)
                            context.hit.incrementAbsorbedDamage((hit - hit * mod).toInt())
                            // Then reduce the accuracy.
                            mod = Math.round(Misc.RANDOM.nextDouble() * 100.0) / 100.0
                            if (mod <= PRAYER_ACCURACY_REDUCTION) {
                                context.isAccurate = false
                            }
                        }
                    }
                }
            } else if (builder.victim.isNpc && builder.character.isPlayer) {
                val attacker = builder.character as Player
                val npc = builder.victim as NPC
                if (npc.id == 8349 && container.combatType === CombatType.MELEE) {
                    container.allHits { context: ContainerHit ->
                        val hit = context.hit.damage
                        var mod = Math.abs(1 - 0.5)
                        context.hit.incrementAbsorbedDamage((hit - hit * mod).toInt())
                        mod = Math.round(Misc.RANDOM.nextDouble() * 100.0) / 100.0
                        if (mod <= PRAYER_ACCURACY_REDUCTION) {
                            context.isAccurate = false
                        }
                    }
                } else if (npc.id == 1158 && (container.combatType === CombatType.MAGIC || container.combatType === CombatType.RANGED) || npc.id == 1160 && container.combatType === CombatType.MELEE) {
                    container.allHits { context: ContainerHit ->
                        if (fullVeracs(attacker)) {
                            return@allHits
                        }
                        val hit = context.hit.damage
                        var mod = Math.abs(1 - 0.95)
                        context.hit.incrementAbsorbedDamage((hit - hit * mod).toInt())
                        mod = Math.round(Misc.RANDOM.nextDouble() * 100.0) / 100.0
                        if (mod <= PRAYER_ACCURACY_REDUCTION) {
                            context.isAccurate = false
                        }
                    }
                    if (!fullVeracs(attacker)) {
                        attacker.packetSender.sendMessage(
                            "Your " + (if (container.combatType === CombatType.MAGIC) "magic" else if (container.combatType === CombatType.RANGED) "ranged" else "melee") + " attack has" + (if (!container.hits[0].isAccurate) "" else " close to") + " no effect on the queen."
                        )
                    }
                } else if (npc.id == 13347 && Nex.zarosStage()) {
                    container.allHits { context: ContainerHit ->
                        val hit = context.hit.damage
                        var mod = Math.abs(1 - 0.4)
                        context.hit.incrementAbsorbedDamage((hit - hit * mod).toInt())
                        mod = Math.round(Misc.RANDOM.nextDouble() * 100.0) / 100.0
                        if (mod <= PRAYER_ACCURACY_REDUCTION) {
                            context.isAccurate = false
                        }
                    }
                }
            }
        }

        /**
         * Gives experience for the total amount of damage dealt in a combat hit.
         *
         * @param builder
         * the attacker's combat builder.
         * @param container
         * the attacker's combat container.
         * @param damage
         * the total amount of damage dealt.
         */
        protected fun giveExperience(
            builder: CombatBuilder, container: CombatContainer, damage: Int
        ) {

            // This attack does not give any experience.
            if (container.experience.size == 0 && container.combatType !== CombatType.MAGIC) {
                return
            }

            // Otherwise we give experience as normal.
            if (builder.character.isPlayer) {
                val player = builder.character as Player
                if (container.combatType === CombatType.MAGIC) {
                    if (player.currentlyCasting != null) player.skillManager.addExperience(
                        Skill.MAGIC,
                        (damage * .90 / container.experience.size).toInt() + builder.character.currentlyCasting.baseExperience()
                    )
                } else {
                    for (i in container.experience) {
                        val skill = Skill.forId(i)
                        player.skillManager.addExperience(skill, (damage * .90 / container.experience.size).toInt())
                    }
                }
                player.skillManager.addExperience(Skill.CONSTITUTION, (damage * 0.7).toInt())
            }
        }

        /**
         * @author Crimson
         * Jul 23, 2017
         * @param attacker
         * the person who's attacking with a degradable weapon
         */
        protected fun handleDegradingWeapons(attacker: Player?) {
            //System.out.println("Called handleDegradingWeapons at "+System.currentTimeMillis());
            if (attacker == null) return
            if (attacker.location === Locations.Location.FREE_FOR_ALL_ARENA || attacker.location === Locations.Location.DUEL_ARENA) {
                return
            }
            for (DI in DegradingItem.getWeapons()) {
                if (!DI.degradeWhenHit()) {
                    continue
                }
                if (attacker.checkItem(DI.slot, DI.deg) || attacker.checkItem(DI.slot, DI.nonDeg)) {
                    ItemDegrading.handleItemDegrading(attacker, DI)
                }
            }
        }

        /**
         * @author Crimson
         * Jul 23, 2017
         * @param victim
         * the person who's being attacked with degradable non-weapons
         */
        protected fun handleDegradingArmor(victim: Player?) {
            //System.out.println("Called handleDegradingArmor at "+System.currentTimeMillis());
            if (victim == null) return
            if (victim.location === Locations.Location.FREE_FOR_ALL_ARENA || victim.location === Locations.Location.DUEL_ARENA) {
                return
            }
            for (DI in DegradingItem.getNonWeapons()) {
                if (!DI.degradeWhenHit()) {
                    continue
                }
                if (victim.checkItem(DI.slot, DI.deg) || victim.checkItem(DI.slot, DI.nonDeg)) {
                    ItemDegrading.handleItemDegrading(victim, DI)
                }
            }
        }

        /**
         * Handles various armor effects for the attacker and victim.
         *
         * @param builder
         * the attacker's combat builder.
         * @param container
         * the attacker's combat container.
         * @param damage
         * the total amount of damage dealt.
         */
        // TODO: Use abstraction for this, will need it when more effects are added.
        protected fun handleArmorEffects(
            attacker: CharacterEntity, target: CharacterEntity?, damage: Int, combatType: CombatType?
        ) {
            if (attacker.constitution > 0 && damage > 0) {
                if (target != null && target.isPlayer) {
                    val t2 = target as Player
                    /** RECOIL  */
                    if (t2.equipment.items[Equipment.RING_SLOT].id == 2550) {
                        var recDamage = Math.round((damage * 0.10).toFloat())
                        if (recDamage < 1) {
                            recDamage = 1
                        }
                        if (recDamage > t2.constitution) recDamage = t2.constitution
                        attacker.dealDamage(Hit(target, recDamage, Hitmask.RED, CombatIcon.DEFLECT))
                        ItemDegrading.handleItemDegrading(t2, DegradingItem.RING_OF_RECOIL)

                        /*if (t.getEquipment().contains(2550) && t.isHandleRecoil()) { //ring of recoil
						if (t.getRingOfRecoilCharges() == 1) {
							t.getEquipment().delete(2550, 1);
							t.getPacketSender().sendMessage("<img=10> @blu@Your Ring of Recoil has shattered.");
							t.setRingOfRecoilCharges(400);
							return;
						}
						t.setHandleRecoil(false);
						int returnDamage = Math.round((float) (damage * 0.1));
						if (returnDamage < 1) {
							returnDamage = 1;
						}
						if(attacker.getConstitution() < returnDamage)
							returnDamage = attacker.getConstitution();
						attacker.dealDamage(new Hit(returnDamage, Hitmask.RED, CombatIcon.DEFLECT));
						t.set(t.getRecoilCharges()-1);
						t.setHandleRecoil(true);
					}*/
                    }
                    /** PHOENIX NECK  */
                    if (t2.equipment.items[Equipment.AMULET_SLOT].id == 11090 && t2.location !== Locations.Location.DUEL_ARENA) {
                        val restore = (t2.skillManager.getMaxLevel(Skill.CONSTITUTION) * .3).toInt()
                        if (t2.skillManager.getCurrentLevel(Skill.CONSTITUTION) <= t2.skillManager.getMaxLevel(Skill.CONSTITUTION) * .2) {
                            t2.performGraphic(Graphic(1690))
                            t2.equipment.delete(t2.equipment.items[Equipment.AMULET_SLOT])
                            t2.skillManager.setCurrentLevel(
                                Skill.CONSTITUTION, t2.skillManager.getCurrentLevel(Skill.CONSTITUTION) + restore
                            )
                            t2.packetSender.sendMessage("Your Phoenix Necklace restored your Constitution, but was destroyed in the process.")
                            t2.updateFlag.flag(Flag.APPEARANCE)
                        }
                    } else if ((t2.equipment.items[Equipment.RING_SLOT].id == 2570 || t2.skillManager.skillCape(Skill.DEFENCE)) && t2.location !== Locations.Location.DUEL_ARENA && t2.location !== Locations.Location.WILDERNESS && t2.location !== Locations.Location.ZULRAH && t2.location !== Locations.Location.GRAVEYARD && t2.location !== Locations.Location.FREE_FOR_ALL_ARENA) {
                        if (t2.skillManager.getCurrentLevel(Skill.CONSTITUTION) <= t2.skillManager.getMaxLevel(Skill.CONSTITUTION) * .1) {
                            if (t2.equipment.items[Equipment.RING_SLOT].id == 2570) {
                                t2.packetSender.sendMessage("Your Ring of Life tried to teleport you away, and was destroyed in the process.")
                                t2.equipment.delete(t2.equipment.items[Equipment.RING_SLOT])
                            }
                            if (t2.skillManager.skillCape(Skill.DEFENCE)) {
                                t2.packetSender.sendMessage("Your Defence Cape effect activated, and tried to teleport you away.")
                            }
                            TeleportHandler.teleportPlayer(
                                t2, GameSettings.DEFAULT_POSITION.copy(), TeleportType.RING_TELE
                            )
                        }
                    }

                    /*
					need loop for enum - .forid()?
				 */

                    //WeaponPoison.handleWeaponPoison(((Player)attacker), t2);
                }
            }

            // 25% chance of these barrows armor effects happening.
            if (Misc.exclusiveRandom(4) == 0) {

                // The guthans effect is here.
                if (fullGuthans(attacker)) {
                    target!!.performGraphic(Graphic(398))
                    attacker.heal(damage)
                    return
                }
                // The rest of the effects only apply to victims that are players.
                /* if (builder.getVictim().isPlayer()) {
                Player victim = (Player) builder.getVictim();

                // The torags effect is here.
                if (CombatFactory.fullTorags(builder.getEntity())) {
                    victim.decrementRunEnergy(Misc.inclusiveRandom(1, 100));
                    victim.performGraphic(new Graphic(399));
                    return;
                }

                // The ahrims effect is here.
                if (CombatFactory.fullAhrims(builder.getEntity()) && victim.getSkills()[Skills.STRENGTH].getLevel() >= victim.getSkills()[Skills.STRENGTH].getLevelForExperience()) {
                    victim.getSkills()[Skills.STRENGTH].decreaseLevel(Utility.inclusiveRandom(
                        1, 10));
                    Skills.refresh(victim, Skills.STRENGTH);
                    victim.performGraphic(new Graphic(400));
                    return;
                }

                // The karils effect is here.
                if (CombatFactory.fullKarils(builder.getEntity()) && victim.getSkills()[Skills.AGILITY].getLevel() >= victim.getSkills()[Skills.AGILITY].getLevelForExperience()) {
                    victim.performGraphic(new Graphic(401));
                    victim.getSkills()[Skills.AGILITY].decreaseLevel(Utility.inclusiveRandom(
                        1, 10));
                    Skills.refresh(victim, Skills.AGILITY);
                    return;
                }
            }*/
            }
        }

        /**
         * Handles various prayer effects for the attacker and victim.
         *
         * @param builder
         * the attacker's combat builder.
         * @param container
         * the attacker's combat container.
         * @param damage
         * the total amount of damage dealt.
         */
        protected fun handlePrayerEffects(
            attacker: CharacterEntity?, target: CharacterEntity?, damage: Int, combatType: CombatType
        ) {
            if (attacker == null || target == null) return
            // Prayer effects can only be done with victims that are players.
            if (target.isPlayer && damage > 0) {
                val victim = target as Player

                // The redemption prayer effect.
                if (PrayerHandler.isActivated(
                        victim, PrayerHandler.REDEMPTION
                    ) && victim.constitution <= victim.skillManager.getMaxLevel(Skill.CONSTITUTION) / 10
                ) {
                    val amountToHeal = (victim.skillManager.getMaxLevel(Skill.PRAYER) * .25).toInt()
                    victim.performGraphic(Graphic(436))
                    victim.skillManager.setCurrentLevel(Skill.PRAYER, 0)
                    victim.skillManager.updateSkill(Skill.PRAYER)
                    victim.skillManager.setCurrentLevel(
                        Skill.CONSTITUTION, victim.constitution + amountToHeal
                    )
                    victim.skillManager.updateSkill(Skill.CONSTITUTION)
                    victim.packetSender.sendMessage(
                        "You've run out of prayer points!"
                    )
                    PrayerHandler.deactivateAll(victim)
                    return
                }

                // These last prayers can only be done with player attackers.
                if (attacker.isPlayer) {
                    val p = attacker as Player
                    // The retribution prayer effect.
                    if (PrayerHandler.isActivated(victim, PrayerHandler.RETRIBUTION) && victim.constitution < 1) {
                        victim.performGraphic(Graphic(437))
                        if (p.position.isWithinDistance(victim.position, RETRIBUTION_RADIUS)) {
                            p.dealDamage(
                                Hit(
                                    target,
                                    Misc.inclusiveRandom(MAXIMUM_RETRIBUTION_DAMAGE),
                                    Hitmask.RED,
                                    CombatIcon.DEFLECT
                                )
                            )
                        }
                    } else if (CurseHandler.isActivated(victim, CurseHandler.WRATH) && victim.constitution < 1) {
                        victim.performGraphic(Graphic(2259))
                        victim.performAnimation(Animation(12583))
                        if (p.position.isWithinDistance(victim.position, RETRIBUTION_RADIUS)) {
                            p.performGraphic(Graphic(2260))
                            p.dealDamage(
                                Hit(
                                    target,
                                    Misc.inclusiveRandom(MAXIMUM_RETRIBUTION_DAMAGE),
                                    Hitmask.RED,
                                    CombatIcon.DEFLECT
                                )
                            )
                        }
                    }
                    if (PrayerHandler.isActivated(
                            attacker as Player?, PrayerHandler.SMITE
                        )
                    ) {
                        victim.skillManager.setCurrentLevel(
                            Skill.PRAYER, victim.skillManager.getCurrentLevel(Skill.PRAYER) - damage / 4
                        )
                        if (victim.skillManager.getCurrentLevel(Skill.PRAYER) < 0) victim.skillManager.setCurrentLevel(
                            Skill.PRAYER, 0
                        )
                        victim.skillManager.updateSkill(Skill.PRAYER)
                    }
                }
            }
            if (attacker.isPlayer) {
                val p = attacker as Player
                if (CurseHandler.isActivated(p, CurseHandler.TURMOIL)) {
                    if (Misc.getRandom(5) >= 3) {
                        val increase = Misc.getRandom(2)
                        if (p.leechedBonuses[increase] + 1 < 30) {
                            p.leechedBonuses[increase] += 1
                            sendCurseBonuses(p)
                        }
                    }
                }
                if (CurseHandler.isActivated(p, CurseHandler.SOUL_SPLIT) && damage > 0) {
                    val form = damage / 4
                    Projectile(attacker, target, 2263, 44, 3, 43, 31, 0).sendProjectile()
                    TaskManager.submit(object : Task(1, p, false) {
                        public override fun execute() {
                            if (!(attacker == null || target == null || attacker.getConstitution() <= 0)) {
                                target.performGraphic(Graphic(2264, GraphicHeight.LOW))
                                p.heal(form)
                                if (target.isPlayer) {
                                    val victim = target as Player
                                    victim.skillManager.setCurrentLevel(
                                        Skill.PRAYER, victim.skillManager.getCurrentLevel(Skill.PRAYER) - form
                                    )
                                    if (victim.skillManager.getCurrentLevel(Skill.PRAYER) < 0) {
                                        victim.skillManager.setCurrentLevel(Skill.PRAYER, 0)
                                        CurseHandler.deactivateCurses(victim)
                                        PrayerHandler.deactivatePrayers(victim)
                                    }
                                    victim.skillManager.updateSkill(Skill.PRAYER)
                                }
                            }
                            stop()
                        }
                    })
                }
                if (p.curseActive[CurseHandler.LEECH_ATTACK] || p.curseActive[CurseHandler.LEECH_DEFENCE] || p.curseActive[CurseHandler.LEECH_STRENGTH] || p.curseActive[CurseHandler.LEECH_MAGIC] || p.curseActive[CurseHandler.LEECH_RANGED] || p.curseActive[CurseHandler.LEECH_SPECIAL_ATTACK] || p.curseActive[CurseHandler.LEECH_ENERGY]) {
                    var i: Int
                    var gfx: Int
                    var projectileGfx: Int
                    projectileGfx = -1
                    gfx = projectileGfx
                    i = gfx
                    if (Misc.getRandom(10) >= 7 && p.curseActive[CurseHandler.LEECH_ATTACK]) {
                        i = 0
                        projectileGfx = 2252
                        gfx = 2253
                    } else if (Misc.getRandom(15) >= 11 && p.curseActive[CurseHandler.LEECH_DEFENCE]) {
                        i = 1
                        projectileGfx = 2248
                        gfx = 2250
                    } else if (Misc.getRandom(11) <= 3 && p.curseActive[CurseHandler.LEECH_STRENGTH]) {
                        i = 2
                        projectileGfx = 2236
                        gfx = 2238
                    } else if (Misc.getRandom(20) >= 16 && p.curseActive[CurseHandler.LEECH_RANGED]) {
                        i = 4
                        projectileGfx = 2236
                        gfx = 2238
                    } else if (Misc.getRandom(30) >= 24 && p.curseActive[CurseHandler.LEECH_MAGIC]) {
                        i = 6
                        projectileGfx = 2244
                        gfx = 2242
                    } else if (Misc.getRandom(30) <= 4 && p.curseActive[CurseHandler.LEECH_SPECIAL_ATTACK]) {
                        i = 7
                        projectileGfx = 2256
                        gfx = 2257
                    } else if (Misc.getRandom(30) <= 4 && p.curseActive[CurseHandler.LEECH_ENERGY]) {
                        i = 8
                        projectileGfx = 2256
                        gfx = 2257
                    }
                    if (i != -1) {
                        p.performAnimation(Animation(12575))
                        if (i != 7 && i != 8) {
                            if (p.leechedBonuses[i] < 2) p.leechedBonuses[i] += Misc.getRandom(2)
                            sendCurseBonuses(p)
                        }
                        if (target.isPlayer) {
                            val victim = target as Player
                            Projectile(attacker, target, projectileGfx, 44, 3, 43, 31, 0).sendProjectile()
                            victim.performGraphic(Graphic(gfx))
                            if (i != 7 && i != 8) {
                                CurseHandler.handleLeech(victim, i, 2, -25, true)
                                sendCurseBonuses(victim)
                            } else if (i == 7) {
                                //Leech spec
                                var leeched = false
                                if (victim.specialPercentage - 10 >= 0) {
                                    victim.specialPercentage = victim.specialPercentage - 10
                                    CombatSpecial.updateBar(victim)
                                    victim.packetSender.sendMessage("Your Special Attack has been leeched by an enemy curse!")
                                    leeched = true
                                }
                                if (leeched) {
                                    p.specialPercentage = p.specialPercentage + 10
                                    if (p.specialPercentage > 100) p.specialPercentage = 100
                                }
                            } else if (i == 8) {
                                //Leech energy
                                var leeched = false
                                if (victim.runEnergy - 30 >= 0) {
                                    victim.runEnergy = victim.runEnergy - 30
                                    victim.packetSender.sendMessage("Your energy has been leeched by an enemy curse!")
                                    leeched = true
                                }
                                if (leeched) {
                                    p.runEnergy = p.runEnergy + 30
                                    if (p.runEnergy > 100) p.runEnergy = 100
                                }
                            }
                        }
                        //p.getPacketSender().sendMessage("You manage to leech your target's "+(i == 8 ? ("energy") : i == 7 ? ("Special Attack") : Misc.formatText(Skill.forId(i).toString().toLowerCase()))+".");
                    }
                } else {
                    val sapWarrior = p.curseActive[CurseHandler.SAP_WARRIOR]
                    val sapRanger = p.curseActive[CurseHandler.SAP_RANGER]
                    val sapMage = p.curseActive[CurseHandler.SAP_MAGE]
                    if (sapWarrior || sapRanger || sapMage) {
                        if (sapWarrior && Misc.getRandom(8) <= 2) {
                            CurseHandler.handleLeech(target, 0, 1, -10, true)
                            CurseHandler.handleLeech(target, 1, 1, -10, true)
                            CurseHandler.handleLeech(target, 2, 1, -10, true)
                            p.performGraphic(Graphic(2214))
                            p.performAnimation(Animation(12575))
                            Projectile(p, target, 2215, 44, 3, 43, 31, 0).sendProjectile()
                            p.packetSender.sendMessage("You decrease the your Attack, Strength and Defence level..")
                        } else if (sapRanger && Misc.getRandom(16) >= 9) {
                            CurseHandler.handleLeech(target, 4, 1, -10, true)
                            CurseHandler.handleLeech(target, 1, 1, -10, true)
                            p.performGraphic(Graphic(2217))
                            p.performAnimation(Animation(12575))
                            Projectile(p, target, 2218, 44, 3, 43, 31, 0).sendProjectile()
                            p.packetSender.sendMessage("You decrease your target's Ranged and Defence level..")
                        } else if (sapMage && Misc.getRandom(15) >= 10) {
                            CurseHandler.handleLeech(target, 6, 1, -10, true)
                            CurseHandler.handleLeech(target, 1, 1, -10, true)
                            p.performGraphic(Graphic(2220))
                            p.performAnimation(Animation(12575))
                            Projectile(p, target, 2221, 44, 3, 43, 31, 0).sendProjectile()
                            p.packetSender.sendMessage("You decrease your target's Magic and Defence level..")
                        }
                    }
                }
            }
            if (target.isPlayer) {
                val victim = target as Player
                if (damage > 0 && Misc.getRandom(10) <= 4) {
                    var deflectDamage = -1
                    if (CurseHandler.isActivated(
                            victim, CurseHandler.DEFLECT_MAGIC
                        ) && combatType === CombatType.MAGIC
                    ) {
                        victim.performGraphic(Graphic(2228, GraphicHeight.MIDDLE))
                        victim.performAnimation(Animation(12573))
                        deflectDamage = (damage * 0.20).toInt()
                    } else if (CurseHandler.isActivated(
                            victim, CurseHandler.DEFLECT_MISSILES
                        ) && combatType === CombatType.RANGED
                    ) {
                        victim.performGraphic(Graphic(2229, GraphicHeight.MIDDLE))
                        victim.performAnimation(Animation(12573))
                        deflectDamage = (damage * 0.20).toInt()
                    } else if (CurseHandler.isActivated(
                            victim, CurseHandler.DEFLECT_MELEE
                        ) && combatType === CombatType.MELEE
                    ) {
                        victim.performGraphic(Graphic(2230, GraphicHeight.MIDDLE))
                        victim.performAnimation(Animation(12573))
                        deflectDamage = (damage * 0.20).toInt()
                    }
                    if (deflectDamage > 0) {
                        if (deflectDamage > attacker.constitution) deflectDamage = attacker.constitution
                        val toDeflect = deflectDamage
                        TaskManager.submit(object : Task(1, victim, false) {
                            public override fun execute() {
                                if (attacker == null || attacker.constitution <= 0) {
                                    stop()
                                } else attacker.dealDamage(Hit(victim, toDeflect, Hitmask.RED, CombatIcon.DEFLECT))
                                stop()
                            }
                        })
                    }
                }
            }
        }

        protected fun handleSpellEffects(
            attacker: CharacterEntity, target: CharacterEntity, damage: Int, combatType: CombatType?
        ) {
            if (damage <= 0) return
            if (target.isPlayer) {
                val t = target as Player
                if (t.hasVengeance()) {
                    t.setHasVengeance(false)
                    t.forceChat("Taste Vengeance!")
                    var returnDamage = (damage * 0.75).toInt()
                    if (attacker.constitution < returnDamage) returnDamage = attacker.constitution
                    attacker.dealDamage(Hit(target, returnDamage, Hitmask.RED, CombatIcon.MAGIC))
                }
            }
            if (target.isNpc && attacker.isPlayer) {
                val player = attacker as Player
                val npc = target as NPC
                if (npc.id == 2043) { //zulrah red form
                    player.minigameAttributes.zulrahAttributes.setRedFormDamage(damage, true)
                    //System.out.println("Added "+damage+" to player's zulrah attributes. Current total: "+player.getMinigameAttributes().getZulrahAttributes().getRedFormDamage());
                }
            }
        }

        fun chargeDragonFireShield(player: Player) {
            if (player.dfsCharges >= 20) {
                //player.getPacketSender().sendMessage("Your Dragonfire shield is fully charged and can be operated.");
                player.performGraphic(Graphic(1168))
                player.performAnimation(Animation(6700))
            } else {
                player.performAnimation(Animation(6695))
                player.performGraphic(Graphic(1164))
                player.incrementDfsCharges(1)
                player.packetSender.sendMessage("Your shield absorbs some of the dragon's fire, and now has " + player.dfsCharges + " " + (if (player.dfsCharges > 1) "charges" else "charge") + ".")
            }
            update(player)
        }

        fun sendFireMessage(player: Player) {
            player.packetSender.sendMessage("Your shield protects against some of the dragon's fire.")
        }

        @JvmStatic
        fun handleDragonFireShield(player: Player?, target: CharacterEntity?) {
            if (player == null || target == null || target.constitution <= 0 || player.constitution <= 0) return
            if (!player.lastDfsTimer.elapsed(120000)) {
                player.packetSender.sendMessage("Your shield is not ready yet.")
                return
            }
            player.combatBuilder.cooldown(false)
            player.setEntityInteraction(target)
            player.performAnimation(Animation(6696))
            player.performGraphic(Graphic(1165))
            TaskManager.submit(object : Task(1, player, false) {
                var ticks = 0
                public override fun execute() {
                    when (ticks) {
                        3 -> Projectile(player, target, 1166, 44, 3, 43, 31, 0).sendProjectile()
                        4 -> {
                            val h = Hit(player, 50 + Misc.getRandom(20) * 10, Hitmask.RED, CombatIcon.MAGIC)
                            target.dealDamage(h)
                            target.performGraphic(Graphic(1167, GraphicHeight.HIGH))
                            target.combatBuilder.addDamage(player, h.damage)
                            target.lastCombat.reset()
                            stop()
                        }
                    }
                    ticks++
                }
            })
            player.lastDfsTimer.reset()
            player.dfsCharges = player.dfsCharges - 1
            player.packetSender.sendMessage("Your shield has " + player.dfsCharges + "/20 charges remaining.")
            update(player)
        }

        fun properLocation(player: Player?, player2: Player?): Boolean {
            return player!!.location.canAttack(player, player2)
        }
    }
}