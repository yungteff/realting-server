package com.realting.world.content.combat.strategy.impl

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.*
import com.realting.model.container.impl.Equipment
import com.realting.model.definitions.WeaponAnimations
import com.realting.model.definitions.WeaponInterfaces
import com.realting.model.definitions.WeaponInterfaces.WeaponInterface
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.GroundItemManager
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import com.realting.world.content.ItemDegrading
import com.realting.world.content.combat.CombatContainer
import com.realting.world.content.combat.CombatFactory.Companion.crystalBow
import com.realting.world.content.combat.CombatFactory.Companion.darkBow
import com.realting.world.content.combat.CombatFactory.Companion.poisonEntity
import com.realting.world.content.combat.CombatFactory.Companion.toxicblowpipe
import com.realting.world.content.combat.CombatType
import com.realting.world.content.combat.effect.CombatPoisonEffect.PoisonType
import com.realting.world.content.combat.range.CombatRangedAmmo.*
import com.realting.world.content.combat.strategy.CombatStrategy
import com.realting.world.content.combat.weapon.CombatSpecial
import com.realting.world.content.combat.weapon.CombatSpecial.Companion.updateBar
import com.realting.world.content.minigames.Dueling.Companion.checkRule
import com.realting.world.content.minigames.Dueling.DuelRule
import java.util.*

/**
 * The default combat strategy assigned to an [CharacterEntity] during a ranged
 * based combat session.
 *
 * @author lare96
 */
class DefaultRangedCombatStrategy : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {

        // We do not need to check npcs.
        if (entity!!.isNpc) {
            return true
        }

        // Create the player instance.
        val player = entity as Player?

        // If we are using a crystal bow then we don't need to check for ammo.
        if (crystalBow(player!!)) {
            return true
        }
        if (checkRule(player, DuelRule.NO_RANGED)) {
            player.packetSender.sendMessage("Ranged-attacks have been turned off in this duel!")
            player.combatBuilder.reset(true)
            return false
        }

        // Check the ammo before proceeding.
        if (!checkAmmo(player)) {
            if (player.isSpecialActivated) {
                player.isSpecialActivated = false
                updateBar(player)
            }
            return false
        }
        return true
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        if (entity!!.isNpc) {
            val npc = entity as NPC?
            var ammo = AmmunitionData.ADAMANT_ARROW
            when (npc!!.id) {
                688 -> ammo = AmmunitionData.BRONZE_ARROW
                27 -> ammo = AmmunitionData.STEEL_ARROW
                2028 -> ammo = AmmunitionData.BOLT_RACK
                6220, 6256, 6276 -> ammo = AmmunitionData.RUNE_ARROW
                6225 -> ammo = AmmunitionData.STEEL_JAVELIN
                6252 -> ammo = AmmunitionData.RUNE_ARROW
            }
            entity.performAnimation(Animation(npc.definition.attackAnimation))
            entity.performGraphic(
                Graphic(
                    ammo.startGfxId,
                    if (ammo.startHeight >= 43) GraphicHeight.HIGH else GraphicHeight.MIDDLE
                )
            )
            fireProjectile(npc, victim, ammo, false)
            return CombatContainer(entity, victim!!, 1, CombatType.RANGED, true)
        }
        val player = entity as Player?
        val dBow = darkBow(player!!)
        player.fireAmmo = 0
        startAnimation(player)
        val ammo = RangedWeaponData.getAmmunitionData(player)
        if (!player.isSpecialActivated) {
            if (toxicblowpipe(player)) {
                if (Misc.inclusiveRandom(1, 3) > 1) {
                    ItemDegrading.handleItemDegrading(player, ItemDegrading.DegradingItem.TOXIC_BLOWPIPE)
                } /*else {
					player.getPacketSender().sendMessage("did not degrade bp because rng");
				}*/
            }
            if (!crystalBow(player)) {
                decrementAmmo(player, victim!!.entityPosition)
                if (dBow || player.rangedWeaponData == RangedWeaponData.MAGIC_SHORTBOW && player.isSpecialActivated && player.combatSpecial != null && player.combatSpecial === CombatSpecial.MAGIC_SHORTBOW) {
                    decrementAmmo(player, victim.entityPosition)
                }
            }
            player.performGraphic(
                Graphic(
                    ammo.startGfxId,
                    if (ammo.startGfxId == 2138) GraphicHeight.LOW else if (ammo.startHeight >= 43) GraphicHeight.HIGH else GraphicHeight.MIDDLE
                )
            )
            fireProjectile(player, victim, ammo, dBow)
        }
        val container = CombatContainer(entity, victim!!, if (dBow) 2 else 1, CombatType.RANGED, true)
        /** CROSSBOW BOLTS EFFECT  */
        if (player.equipment[Equipment.WEAPON_SLOT].definition != null && player.equipment[Equipment.WEAPON_SLOT].definition.name.lowercase(
                Locale.getDefault()
            ).contains("crossbow")
        ) {
            if (Misc.getRandom(12) >= 10) {
                container.modifiedDamage = getModifiedDamage(player, victim, container)
            }
        }
        return container
    }

    override fun attackDelay(entity: CharacterEntity?): Int {
        return entity!!.attackSpeed
    }

    override fun attackDistance(entity: CharacterEntity): Int {

        // The default distance for all npcs using ranged is 6.
        if (entity.isNpc) {
            return 6
        }

        // Create the player instance.
        val player = entity as Player
        var distance = 5

        //TODO fix this in the future the distance is wrong when ranging from the east got something to do with clipping
//        if (player.rangedWeaponData != null) {
//            distance = player.rangedWeaponData.type.distanceRequired
//        }
//        if (((player.regionInstance != null) && (player.regionInstance.type == RegionInstanceType.KRAKEN)) || (player.regionInstance.type == RegionInstanceType.ZULRAH)) {
//            println(player.rangedWeaponData.type.distanceRequired)
//            distance += 3
//        }

//        return distance + if (player.fightType.style === FightStyle.DEFENSIVE) 2 else 0
        return distance
    }

    /**
     * Starts the performAnimation for the argued [Player] in the current combat
     * hook.
     *
     * @param player
     * the player to start the performAnimation for.
     */
    private fun startAnimation(player: Player?) {
        if (player!!.equipment[Equipment.WEAPON_SLOT].definition.name.startsWith("Karils")) {
            player.performAnimation(Animation(2075))
        } else {
            player.performAnimation(Animation(WeaponAnimations.getAttackAnimation(player)))
        }
    }

    /**
     * Checks the ammo to make sure the argued [Player] has the right type
     * and amount before attacking.
     *
     * @param player
     * the player's ammo to check.
     * @return `true` if the player has the right ammo,
     * `false` otherwise.
     */
    private fun checkAmmo(player: Player?): Boolean {
        val data = player!!.rangedWeaponData
        if (data.type == RangedWeaponType.THROW) return true
        if (player.equipment[Equipment.WEAPON_SLOT].id == 22010) { //|| player.getEquipment().get(Equipment.WEAPON_SLOT).getId() == 20171) {
            return true
        }
        if (toxicblowpipe(player)) {
            val charges = player.blowpipeCharges
            //System.out.println(charges);
            if (charges <= 0) {
                player.packetSender.sendMessage("You have no charges in your blowpipe!")
                //player.getPacketSender().sendMessage("You have no charges!");
                return false
            }
        }
        val ammunition =
            player.equipment.items[if (data.type == RangedWeaponType.THROW) Equipment.WEAPON_SLOT else Equipment.AMMUNITION_SLOT]
        val darkBow =
            data.type == RangedWeaponType.DARK_BOW && ammunition.amount < 2 || data == RangedWeaponData.MAGIC_SHORTBOW && player.isSpecialActivated && player.combatSpecial != null && player.combatSpecial === CombatSpecial.MAGIC_SHORTBOW && ammunition.amount < 2
        if (ammunition.id == -1 || ammunition.amount < 1 || darkBow) {
            player.packetSender.sendMessage(if (darkBow) "You need at least 2 arrows to fire this bow." else "You don't have any ammunition to fire.")
            player.combatBuilder.reset(true)
            return false
        }
        var properEquipment = false
        for (ammo in data.ammunitionData) {
            for (i in ammo.itemIds) {
                if (i == ammunition.id) {
                    properEquipment = true
                    break
                }
            }
        }
        if (!properEquipment) {
            val ammoName = ammunition.definition.name
            val weaponName = player.equipment.items[Equipment.WEAPON_SLOT].definition.name
            val add = if (!ammoName.endsWith("s") && !ammoName.endsWith("(e)")) "s" else ""
            player.packetSender.sendMessage("You can not use " + ammoName + "" + add + " with " + Misc.anOrA(weaponName) + " " + weaponName + ".")
            player.combatBuilder.reset(true)
            return false
        }
        return true
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return false
    }

    override fun getCombatType(entity: CharacterEntity): CombatType? {
        return CombatType.RANGED
    }

    companion object {
        fun fireProjectile(e: CharacterEntity?, victim: CharacterEntity?, ammo: AmmunitionData, dBow: Boolean) {
            TaskManager.submit(object : Task(1, e!!.combatBuilder, false) {
                //TODO FIX THIS PROJECTILE, SHOULDNT BE A TASK
                override fun execute() {
                    Projectile(
                        e,
                        victim,
                        ammo.projectileId,
                        ammo.projectileDelay + 16,
                        ammo.projectileSpeed,
                        ammo.startHeight,
                        ammo.endHeight,
                        0
                    ).sendProjectile()
                    if (dBow) {
                        Projectile(
                            e,
                            victim,
                            ammo.projectileId,
                            ammo.projectileDelay + 32,
                            ammo.projectileSpeed,
                            ammo.startHeight - 2,
                            ammo.endHeight,
                            0
                        ).sendProjectile()
                    }
                    stop()
                }
            })
        }

        /**
         * Decrements the amount ammo the [Player] currently has equipped.
         *
         * @param player
         * the player to decrement ammo for.
         */
        fun decrementAmmo(player: Player?, pos: Position?) {

            // Determine which slot we are decrementing ammo from.
            val slot =
                if (player!!.weapon == WeaponInterface.SHORTBOW || player.weapon == WeaponInterface.LONGBOW || player.weapon == WeaponInterface.CROSSBOW || player.weapon == WeaponInterface.BLOWPIPE || player.weapon == WeaponInterface.BSOAT || player.weapon == WeaponInterface.ARMADYLXBOW) Equipment.AMMUNITION_SLOT else Equipment.WEAPON_SLOT

            // Set the ammo we are currently using.
            player.fireAmmo = player.equipment[slot].id
            val ardy = player.equipment[Equipment.CAPE_SLOT].id == 19748
            val avas = player.equipment[Equipment.CAPE_SLOT].id == 10499
            val skillcape = player.skillManager.skillCape(Skill.RANGED)
            val zaryte = player.equipment[Equipment.WEAPON_SLOT].id == 20171
            if ((avas || ardy || skillcape) && Misc.getRandom(11) <= 9) { //Avas
                return
            }
            if (zaryte) {
                return
            }


            // Decrement the ammo in the selected slot.
            player.equipment[slot].decrementAmount()
            if ((!avas || !ardy || !skillcape || !zaryte) && player.fireAmmo != 15243) {
                if (player.location === Locations.Location.ZULRAH || player.location === Locations.Location.KRAKEN) {
                    GroundItemManager.spawnGroundItem(
                        player,
                        GroundItem(Item(player.fireAmmo), player.entityPosition, player.username, false, 120, true, 120)
                    )
                } else {
                    GroundItemManager.spawnGroundItem(
                        player,
                        GroundItem(Item(player.fireAmmo), pos, player.username, false, 120, true, 120)
                    )
                }
            }

            // If we are at 0 ammo remove the item from the equipment completely.
            if (player.equipment[slot].amount == 0) {
                if (player.equipment[Equipment.WEAPON_SLOT].id == 22010) {
                    return
                } else {
                    player.packetSender.sendMessage("You have run out of ammunition!")
                    player.equipment[slot] = Item(-1)
                }
                if (slot == Equipment.WEAPON_SLOT) {
                    WeaponInterfaces.assign(player, Item(-1))
                }
                player.updateFlag.flag(Flag.APPEARANCE)
            }

            // Refresh the equipment interface.
            player.equipment.refreshItems()
        }

        private fun getModifiedDamage(player: Player?, target: CharacterEntity?, container: CombatContainer?): Int {
            if (container == null || container.getHits().size < 1) return 0
            val hit = container.getHits()[0]!!
            if (!hit.isAccurate) return 0
            var damage = container.getHits()[0]!!.hit.damage
            val ammo = player!!.fireAmmo
            if (ammo == -1) {
                return damage
            }
            var multiplier = 1.0
            val pTarget = if (target!!.isPlayer) target as Player? else null
            when (ammo) {
                9236 -> {
                    target.performGraphic(Graphic(749))
                    multiplier = 1.3
                }
                9237 -> {
                    target.performGraphic(Graphic(755))
                    multiplier = 1.05
                }
                9238 -> {
                    target.performGraphic(Graphic(750))
                    multiplier = 1.1
                }
                9239 -> {
                    target.performGraphic(Graphic(757))
                    if (pTarget != null) {
                        pTarget.skillManager.setCurrentLevel(
                            Skill.MAGIC,
                            pTarget.skillManager.getCurrentLevel(Skill.MAGIC) - 3
                        )
                        pTarget.packetSender.sendMessage("Your Magic level has been reduced.")
                    }
                }
                9240 -> {
                    target.performGraphic(Graphic(751))
                    if (pTarget != null) {
                        pTarget.skillManager.setCurrentLevel(
                            Skill.PRAYER,
                            pTarget.skillManager.getCurrentLevel(Skill.PRAYER) - 40
                        )
                        if (pTarget.skillManager.getCurrentLevel(Skill.PRAYER) < 0) {
                            pTarget.skillManager.setCurrentLevel(Skill.PRAYER, 0)
                        }
                        pTarget.packetSender.sendMessage("Your Prayer level has been leeched.")
                        player.skillManager.setCurrentLevel(
                            Skill.PRAYER,
                            pTarget.skillManager.getCurrentLevel(Skill.PRAYER) + 40
                        )
                        if (player.skillManager.getCurrentLevel(Skill.PRAYER) > player.skillManager.getMaxLevel(Skill.PRAYER)) {
                            player.skillManager.setCurrentLevel(
                                Skill.PRAYER,
                                player.skillManager.getMaxLevel(Skill.PRAYER)
                            )
                        } else {
                            player.packetSender.sendMessage("Your enchanced bolts leech some Prayer points from your opponent..")
                        }
                    }
                }
                9241 -> {
                    target.performGraphic(Graphic(752))
                    poisonEntity(target, PoisonType.MILD)
                }
                9242 -> {

                    //TODO:: default ranged combat??
//                    if (player.skillManager.getCurrentLevel(Skill.CONSTITUTION) - player.skillManager.getCurrentLevel(
//                            Skill.CONSTITUTION
//                        ) / 200 < 10
//                    ) {
//                        break
//                    }
                    val priceDamage = (player.skillManager.getCurrentLevel(Skill.CONSTITUTION) * 0.08).toInt()
                    if (priceDamage < 0) {
                        return damage
                    }
                    val dmg2 =
                        (if ((target.constitution * 0.065).toInt() > 1000) 650 + Misc.getRandom(50) else target.constitution * 0.065).toInt()
                    if (dmg2 <= 0) {
                        return damage
                    }
                    target.performGraphic(Graphic(754))
                    player.dealDamage(Hit(null, priceDamage, Hitmask.RED, CombatIcon.RANGED))
                    return dmg2
                }
                9243 -> {
                    target.performGraphic(Graphic(758, GraphicHeight.MIDDLE))
                    multiplier = 1.15
                }
                9244 -> {
                    target.performGraphic(Graphic(756))
                    if (pTarget != null && (pTarget.equipment.items[Equipment.SHIELD_SLOT].id == 1540 || pTarget.equipment.items[Equipment.SHIELD_SLOT].id == 13655 || pTarget.equipment.items[Equipment.SHIELD_SLOT].id == 11283 || pTarget.fireImmunity > 0)) {
                        return damage
                    }
                    if (damage < 300 && Misc.getRandom(3) <= 1) {
                        damage = 300 + Misc.getRandom(150)
                    }
                    multiplier = 1.25
                }
                9245 -> {
                    target.performGraphic(Graphic(753))
                    multiplier = 1.26
                    val heal = (damage * 0.25).toInt() + 10
                    player.heal(heal)
                    if (damage < 250 && Misc.getRandom(3) <= 1) {
                        damage += 150 + Misc.getRandom(80)
                    }
                }
            }
            return (damage * multiplier).toInt()
        }
    }
}