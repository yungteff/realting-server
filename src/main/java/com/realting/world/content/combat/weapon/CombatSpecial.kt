package com.realting.world.content.combat.weapon

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.engine.task.impl.PlayerSpecialAmountTask
import com.realting.engine.task.impl.StaffOfLightSpecialAttackTask
import com.realting.model.*
import com.realting.model.container.impl.Equipment
import com.realting.model.definitions.WeaponInterfaces.WeaponInterface
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import com.realting.world.content.combat.CombatContainer
import com.realting.world.content.combat.CombatType
import com.realting.world.content.combat.HitQueue.CombatHit
import com.realting.world.content.combat.magic.Autocasting
import com.realting.world.content.minigames.Dueling.Companion.checkRule
import com.realting.world.content.minigames.Dueling.DuelRule
import com.realting.world.content.player.events.Achievements.AchievementData
import com.realting.world.content.player.events.Achievements.finishAchievement
import com.realting.world.content.player.events.Consumables.drinkStatPotion
import java.util.*

/**
 * Holds constants that hold data for all of the special attacks that can be
 * used.
 *
 * @author lare96
 */
enum class CombatSpecial
/**
 * Create a new [CombatSpecial].
 *
 * @param identifers
 * the weapon ID's that perform this special when activated.
 * @param drainAmount
 * the amount of special energy this attack will drain.
 * @param strengthBonus
 * the strength bonus when performing this special attack.
 * @param accuracyBonus
 * the accuracy bonus when performing this special attack.
 * @param combatType
 * the combat type used when performing this special attack.
 * @param weaponType
 * the weapon interface used by the identifiers.
 */(
    /** The weapon ID's that perform this special when activated.  */
    val identifiers: IntArray,
    /** The amount of special energy this attack will drain.  */
    val drainAmount: Int,
    /** The strength bonus when performing this special attack.  */
    val strengthBonus: Double,
    /** The accuracy bonus when performing this special attack.  */
    val accuracyBonus: Double,
    /** The combat type used when performing this special attack.  */
    val combatType: CombatType,
    /** The weapon interface used by the identifiers.  */
    val weaponType: WeaponInterface
) {
    /*
	 private CombatSpecial(int[] identifiers, int drainAmount,
			double strengthBonus, double accuracyBonus, CombatType combatType,
			WeaponInterface weaponType) {
		this.identifiers = identifiers;
		this.drainAmount = drainAmount;
		this.strengthBonus = strengthBonus;
		this.accuracyBonus = accuracyBonus;
		this.combatType = combatType;
		this.weaponType = weaponType;
	 */
    DRAGON_DAGGER(intArrayOf(1215, 1231, 5680, 5698, 22039), 25, 1.16, 1.20, CombatType.MELEE, WeaponInterface.DAGGER) {
        override fun container(player: Player, target: CharacterEntity?): CombatContainer {
            player.performAnimation(Animation(1062))
            player.performGraphic(Graphic(252, GraphicHeight.HIGH))
            return CombatContainer(
                player, target!!, 2, CombatType.MELEE, true
            )
        }
    },
    KORASIS_SWORD(intArrayOf(19780), 60, 1.55, 8.0, CombatType.MELEE, WeaponInterface.SWORD) {
        override fun container(player: Player, target: CharacterEntity?): CombatContainer {
            player.performAnimation(Animation(14788))
            player.performGraphic(Graphic(1729))
            return object : CombatContainer(player, target!!, 1, 1, CombatType.MAGIC, true) {
                override fun onHit(damage: Int, accurate: Boolean) {
                    target!!.performGraphic(Graphic(1730))
                }
            }
        }
    },
    ARMADYL_CROSSBOW(intArrayOf(22034), 40, 1.01, 2.01, CombatType.RANGED, WeaponInterface.ARMADYLXBOW) {
        //arma spec
        override fun container(player: Player, target: CharacterEntity?): CombatContainer {
            player.performAnimation(Animation(4230))
            player.performGraphic(Graphic(28, GraphicHeight.HIGH))
            TaskManager.submit(object : Task(1, player, false) {
                public override fun execute() {
                    Projectile(player, target, 72, 44, 3, 0, 0, 0).sendProjectile()
                    stop()
                }
            })
            return CombatContainer(player, target!!, 1, CombatType.RANGED, true)
        }
    },
    MORRIGANS_JAVELIN(intArrayOf(13879), 50, 1.40, 1.30, CombatType.RANGED, WeaponInterface.JAVELIN) {
        override fun container(player: Player, target: CharacterEntity?): CombatContainer {
            player.performAnimation(Animation(10501))
            player.performGraphic(Graphic(1836))
            return CombatContainer(player, target!!, 1, CombatType.RANGED, true)
        }
    },
    MORRIGANS_THROWNAXE(intArrayOf(13883), 50, 1.38, 1.30, CombatType.RANGED, WeaponInterface.THROWNAXE) {
        override fun container(player: Player, target: CharacterEntity?): CombatContainer {
            player.performAnimation(Animation(10504))
            player.performGraphic(Graphic(1838))
            return CombatContainer(player, target!!, 1, CombatType.RANGED, true)
        }
    },
    GRANITE_MAUL(intArrayOf(4153, 20084), 50, 1.21, 1.0, CombatType.MELEE, WeaponInterface.WARHAMMER) {
        override fun container(player: Player, target: CharacterEntity?): CombatContainer {
            player.performAnimation(Animation(1667))
            player.performGraphic(Graphic(337, GraphicHeight.HIGH))
            player.combatBuilder.attackTimer = 1
            return CombatContainer(
                player, target!!, 1, CombatType.MELEE, true
            )
        }
    },
    SCYTHE(intArrayOf(1419), 50, 1.0, 1.0, CombatType.MELEE, WeaponInterface.HALBERD) {
        override fun container(player: Player, target: CharacterEntity?): CombatContainer {
            player.performAnimation(Animation(2066))
            player.performGraphic(Graphic(2959)) //2114
            return CombatContainer(player, target!!, 1, CombatType.MELEE, true)
        }
    },
    ABYSSAL_WHIP(
        intArrayOf(4151, 21371, 15441, 15442, 15443, 15444, 22008), 50, 1.0, 1.0, CombatType.MELEE, WeaponInterface.WHIP
    ) {
        override fun container(player: Player, target: CharacterEntity?): CombatContainer {
            player.performAnimation(Animation(1658))
            target!!.performGraphic(Graphic(341, GraphicHeight.HIGH))
            if (target.isPlayer) {
                val p = target as Player?
                var totalRunEnergy = p!!.runEnergy - 25
                if (totalRunEnergy < 0) totalRunEnergy = 0
                p.runEnergy = totalRunEnergy
                p.isRunning = false
                p.packetSender.sendRunStatus()
            }
            return CombatContainer(
                player, target, 1, CombatType.MELEE, false
            )
        }
    },
    DRAGON_LONGSWORD(intArrayOf(1305), 25, 1.15, 1.20, CombatType.MELEE, WeaponInterface.LONGSWORD) {
        override fun container(player: Player, target: CharacterEntity?): CombatContainer {
            player.performAnimation(Animation(1058))
            player.performGraphic(Graphic(248, GraphicHeight.HIGH))
            return CombatContainer(
                player, target!!, 1, CombatType.MELEE, true
            )
        }
    },
    STEEL_TEMPEST(intArrayOf(14018), 60, 1.62, 1.83, CombatType.MELEE, WeaponInterface.SCIMITAR) {
        override fun container(player: Player, target: CharacterEntity?): CombatContainer {
            player.performAnimation(Animation(2876))
            target!!.performGraphic(Graphic(1333, GraphicHeight.LOW))
            return CombatContainer(
                player, target, 1, CombatType.MELEE, true
            )
        }
    },
    SKULL_SCEPTRE(intArrayOf(9013), 100, 2.0, 2.0, CombatType.MELEE, WeaponInterface.BATTLEAXE) {
        override fun container(player: Player, target: CharacterEntity?): CombatContainer {
            //player.performAnimation(new Animation(1058));
            //player.performGraphic(new Graphic(248, GraphicHeight.HIGH));
            player.performAnimation(Animation(1058))
            player.performGraphic(Graphic(726, GraphicHeight.HIGH))
            player.setHasVengeance(true)
            player.packetSender.sendMessage("You cast @red@Vengeance@bla@.")
            target!!.forceChat("Spooky!")
            return CombatContainer(
                player, target, 1, CombatType.MELEE, true
            )
        }
    },
    BARRELSCHEST_ANCHOR(intArrayOf(10887), 50, 1.21, 1.30, CombatType.MELEE, WeaponInterface.WARHAMMER) {
        override fun container(player: Player, target: CharacterEntity?): CombatContainer {
            player.performAnimation(Animation(5870))
            player.performGraphic(Graphic(1027, GraphicHeight.MIDDLE))
            return CombatContainer(
                player, target!!, 1, CombatType.MELEE, true
            )
        }
    },
    SARADOMIN_SWORD(intArrayOf(11730), 100, 1.35, 1.2, CombatType.MELEE, WeaponInterface.TWO_HANDED_SWORD) {
        override fun container(player: Player, target: CharacterEntity?): CombatContainer {
            player.performAnimation(Animation(11993))
            player.setEntityInteraction(target)
            return object : CombatContainer(player, target!!, 2, CombatType.MAGIC, true) {
                override fun onHit(damage: Int, accurate: Boolean) {
                    target!!.performGraphic(Graphic(1194))
                }
            }
        }
    },
    VESTAS_LONGSWORD(intArrayOf(13899, 13901), 25, 1.28, 1.25, CombatType.MELEE, WeaponInterface.LONGSWORD) {
        override fun container(player: Player, target: CharacterEntity?): CombatContainer {
            player.performAnimation(Animation(10502))
            return CombatContainer(player, target!!, 1, CombatType.MELEE, true)
        }
    },
    VESTAS_SPEAR(intArrayOf(13905, 13907), 50, 1.26, 1.0, CombatType.MELEE, WeaponInterface.SPEAR) {
        override fun container(player: Player, target: CharacterEntity?): CombatContainer {
            player.performAnimation(Animation(10499))
            player.performGraphic(Graphic(1835))
            player.combatBuilder.attackTimer = 1
            return CombatContainer(
                player, target!!, 1, CombatType.MELEE, true
            )
        }
    },
    STATIUS_WARHAMMER(intArrayOf(13902, 13904), 30, 1.25, 1.23, CombatType.MELEE, WeaponInterface.WARHAMMER) {
        override fun container(player: Player, target: CharacterEntity?): CombatContainer {
            player.performAnimation(Animation(10505))
            player.performGraphic(Graphic(1840))
            return object : CombatContainer(player, target!!, 1, CombatType.MELEE, true) {
                override fun onHit(damage: Int, accurate: Boolean) {
                    if (target!!.isPlayer && accurate) {
                        val t = target as Player?
                        val currentDef = t!!.skillManager.getCurrentLevel(Skill.DEFENCE)
                        val defDecrease = (currentDef * 0.11).toInt()
                        if (currentDef - defDecrease <= 0 || currentDef <= 0) return
                        t.skillManager.setCurrentLevel(Skill.DEFENCE, defDecrease)
                        t.packetSender.sendMessage("Your opponent has reduced your Defence level.")
                        player.packetSender.sendMessage("Your hammer forces some of your opponent's defences to break.")
                    }
                }
            }
        }
    },
    BARB_AXE(intArrayOf(22062), 75, 1.25, 1.23, CombatType.MELEE, WeaponInterface.BATTLEAXE) {
        override fun container(player: Player, target: CharacterEntity?): CombatContainer {
            player.performAnimation(Animation(10505))
            player.performGraphic(Graphic(1840))
            return object : CombatContainer(player, target!!, 1, CombatType.MELEE, true) {
                override fun onHit(damage: Int, accurate: Boolean) {
                    if (target!!.isPlayer) {
                        val t = target as Player?
                        val currentHelth = t!!.skillManager.getCurrentLevel(Skill.CONSTITUTION) / 2
                        player.dealDamage(Hit(player, currentHelth, Hitmask.DARK_PURPLE, CombatIcon.DEFLECT))
                        //	t.getPacketSender().sendMessage("Your opponent has reduced your Defence level.");
                        player.packetSender.sendMessage("You take recoil damage.")
                    } else {
                        val t = target as NPC?
                        val currentHealth = t!!.constitution / 100
                        player.dealDamage(Hit(player, currentHealth, Hitmask.DARK_PURPLE, CombatIcon.DEFLECT))
                        player.packetSender.sendMessage("You take recoil damage.") //temp messages? possible think of better ones.
                    }
                }
            }
        }
    },
    MAGIC_SHORTBOW(intArrayOf(861), 55, 1.0, 1.2, CombatType.RANGED, WeaponInterface.SHORTBOW) {
        override fun container(player: Player, target: CharacterEntity?): CombatContainer {
            player.performAnimation(Animation(1074))
            player.performGraphic(Graphic(250, GraphicHeight.HIGH))
            TaskManager.submit(object : Task(1, player, false) {
                public override fun execute() {
                    Projectile(player, target, 249, 44, 3, 43, 31, 0).sendProjectile()
                    stop()
                }
            })
            return CombatContainer(
                player, target!!, 2, CombatType.RANGED, true
            )
        }
    },
    MAGIC_LONGBOW(intArrayOf(859), 35, 1.0, 5.0, CombatType.RANGED, WeaponInterface.LONGBOW) {
        override fun container(player: Player, target: CharacterEntity?): CombatContainer {
            player.performAnimation(Animation(426))
            player.performGraphic(Graphic(250, GraphicHeight.HIGH))
            Projectile(player, target, 249, 44, 3, 43, 31, 0).sendProjectile()
            return CombatContainer(
                player, target!!, 1, CombatType.RANGED, true
            )
        }
    },
    DARK_BOW(intArrayOf(11235), 55, 1.45, 1.22, CombatType.RANGED, WeaponInterface.LONGBOW) {
        override fun container(player: Player, target: CharacterEntity?): CombatContainer {
            player.performAnimation(Animation(426))
            TaskManager.submit(object : Task(1, player, false) {
                var tick = 0
                public override fun execute() {
                    if (tick == 0) {
                        Projectile(player, target, 1099, 44, 3, 43, 31, 0).sendProjectile()
                        Projectile(player, target, 1099, 60, 3, 43, 31, 0).sendProjectile()
                    } else if (tick >= 1) {
                        target!!.performGraphic(Graphic(1100, GraphicHeight.HIGH))
                        stop()
                    }
                    tick++
                }
            })
            return CombatContainer(
                player, target!!, 2, CombatType.RANGED, true
            )
        }
    },
    HAND_CANNON(intArrayOf(15241), 45, 1.45, 1.15, CombatType.RANGED, WeaponInterface.SHORTBOW) {
        override fun container(player: Player, target: CharacterEntity?): CombatContainer {
            player.performAnimation(Animation(12175))
            player.combatBuilder.attackTimer = 8
            TaskManager.submit(object : Task(1, player, false) {
                public override fun execute() {
                    player.performGraphic(Graphic(2141))
                    Projectile(player, target, 2143, 44, 3, 43, 31, 0).sendProjectile()
                    CombatHit(
                        player.combatBuilder, CombatContainer(player, target!!, CombatType.RANGED, true)
                    ).handleAttack()
                    player.combatBuilder.attackTimer = 2
                    stop()
                }
            })
            return CombatContainer(
                player, target!!, 1, 1, CombatType.RANGED, true
            )
        }
    },
    DRAGON_BATTLEAXE(intArrayOf(1377), 100, 1.0, 1.0, CombatType.MELEE, WeaponInterface.BATTLEAXE) {
        override fun onActivation(player: Player, target: CharacterEntity?) {
            player.performGraphic(Graphic(246, GraphicHeight.LOW))
            player.performAnimation(Animation(1056))
            player.forceChat("Raarrrrrgggggghhhhhhh!")
            drain(player, drainAmount)
            drinkStatPotion(player, -1, -1, -1, Skill.STRENGTH.ordinal, true)
            player.skillManager.setCurrentLevel(Skill.ATTACK, player.skillManager.getCurrentLevel(Skill.ATTACK) - 7)
            player.combatBuilder.cooldown(true)
        }

        override fun container(player: Player, target: CharacterEntity?): CombatContainer {
            throw UnsupportedOperationException(
                "Dragon battleaxe does not have a special attack!"
            )
        }
    },
    STAFF_OF_LIGHT(
        intArrayOf(14004, 14005, 14006, 14007, 15486), 100, 1.0, 1.0, CombatType.MELEE, WeaponInterface.LONGSWORD
    ) {
        override fun onActivation(player: Player, target: CharacterEntity?) {
            player.performGraphic(Graphic(1958))
            player.performAnimation(Animation(10516))
            drain(player, drainAmount)
            player.staffOfLightEffect = 200
            TaskManager.submit(StaffOfLightSpecialAttackTask(player))
            player.packetSender.sendMessage("You are shielded by the spirits of the Staff of light!")
            player.combatBuilder.cooldown(true)
        }

        override fun container(player: Player, target: CharacterEntity?): CombatContainer {
            throw UnsupportedOperationException(
                "Dragon battleaxe does not have a special attack!"
            )
        }
    },
    DRAGON_SPEAR(intArrayOf(1249, 1263, 5716, 5730, 11716), 25, 1.0, 1.0, CombatType.MELEE, WeaponInterface.SPEAR) {
        override fun container(player: Player, target: CharacterEntity?): CombatContainer {
            player.performAnimation(Animation(1064))
            player.performGraphic(Graphic(253))
            return object : CombatContainer(
                player, target!!, 1, CombatType.MELEE, true
            ) {
                override fun onHit(damage: Int, accurate: Boolean) {
                    if (target!!.isPlayer) {
                        var moveX = target.position.x - player.position.x
                        var moveY = target.position.y - player.position.y
                        if (moveX > 0) moveX = 1 else if (moveX < 0) moveX = -1
                        if (moveY > 0) moveY = 1 else if (moveY < 0) moveY = -1
                        if (target.movementQueue.canWalk(moveX, moveY)) {
                            target.setEntityInteraction(player)
                            target.movementQueue.reset()
                            target.movementQueue.walkStep(moveX, moveY)
                        }
                    }
                    target.performGraphic(Graphic(254, GraphicHeight.HIGH))
                    TaskManager.submit(object : Task(1, false) {
                        public override fun execute() {
                            target.movementQueue.freeze(6)
                            stop()
                        }
                    })
                }
            }
        }
    },
    DRAGON_MACE(intArrayOf(1434), 25, 1.29, 1.25, CombatType.MELEE, WeaponInterface.MACE) {
        override fun container(player: Player, target: CharacterEntity?): CombatContainer {
            player.performAnimation(Animation(1060))
            player.performGraphic(Graphic(251, GraphicHeight.HIGH))
            return CombatContainer(
                player, target!!, 1, CombatType.MELEE, true
            )
        }
    },
    DRAGON_SCIMITAR(intArrayOf(4587), 55, 1.1, 1.1, CombatType.MELEE, WeaponInterface.SCIMITAR) {
        override fun container(player: Player, target: CharacterEntity?): CombatContainer {
            player.performAnimation(Animation(1872))
            player.performGraphic(Graphic(347, GraphicHeight.HIGH))
            return CombatContainer(
                player, target!!, 1, CombatType.MELEE, true
            )
        }
    },
    DRAGON_2H_SWORD(intArrayOf(7158), 60, 1.0, 1.0, CombatType.MELEE, WeaponInterface.TWO_HANDED_SWORD) {
        override fun container(player: Player, target: CharacterEntity?): CombatContainer {
            player.performAnimation(Animation(3157))
            player.performGraphic(Graphic(559))
            return object : CombatContainer(
                player, target!!, 1, CombatType.MELEE, false
            ) {
                override fun onHit(damage: Int, accurate: Boolean) {
                    /*if (Location.inMulti(player)) {
						List<GameCharacter> localEntities;

						if (target.isPlayer()) {
							localEntities = Optional.of(player.getLocalPlayers());
						} else if (target.isNpc()) {
							localEntities = Optional.of(player.getLocalNpcs());
						}

						for (GameCharacter e : localEntities.get()) {
							if (e == null) {
								continue;
							}

							if (e.getPosition().isWithinDistance(
									target.getPosition(), 1) && !e.equals(target) && !e.equals(player) && e.getConstitution() > 0 && !e.isDead()) {
								Hit hit = CombatFactory.getHit(player, target,
										CombatType.MELEE);
								e.dealDamage(hit);
								e.getCombatBuilder().addDamage(player,
										hit.getDamage());
							}
						}
					}*/
                }
            }
        }
    },
    DRAGON_HALBERD(intArrayOf(3204), 30, 1.07, 1.08, CombatType.MELEE, WeaponInterface.HALBERD) {
        override fun container(player: Player, target: CharacterEntity?): CombatContainer {
            player.performAnimation(Animation(1203))
            player.performGraphic(Graphic(282, GraphicHeight.HIGH))
            return CombatContainer(
                player, target!!, 2, CombatType.MELEE, true
            )
        }
    },
    ARMADYL_GODSWORD(intArrayOf(11694), 50, 1.43, 1.63, CombatType.MELEE, WeaponInterface.TWO_HANDED_SWORD) {
        override fun container(player: Player, target: CharacterEntity?): CombatContainer {
            player.performAnimation(Animation(11989))
            player.performGraphic(Graphic(2113))
            return CombatContainer(player, target!!, 1, CombatType.MELEE, true)
        }
    },
    ZAMORAK_GODSWORD(intArrayOf(11700), 50, 1.25, 1.4, CombatType.MELEE, WeaponInterface.TWO_HANDED_SWORD) {
        override fun container(player: Player, target: CharacterEntity?): CombatContainer {
            player.performAnimation(Animation(7070))
            return object : CombatContainer(player, target!!, 1, CombatType.MELEE, true) {
                override fun onHit(damage: Int, accurate: Boolean) {
                    if (target != null && target.isPlayer && accurate) {
                        val p = target as Player
                        val dmgDrain = damage * 0.75
                        val prayerDrain = dmgDrain.toInt()
                        player.performGraphic(Graphic(1221))
                        if (prayerDrain <= 0) return
                        // player.getSkillManager().setCurrentLevel(Skill.PRAYER,
                        // (p.getSkillManager().getCurrentLevel(Skill.PRAYER) +
                        // prayerDrain));
                        player.packetSender.sendMessage(
                            "@bla@You have stolen @red@$prayerDrain @bla@prayer points from your target."
                        )
                        p.skillManager.setCurrentLevel(
                            Skill.PRAYER, p.skillManager.getCurrentLevel(Skill.PRAYER) - prayerDrain
                        )
                        p.packetSender.sendMessage(
                            "@bla@Your opponent has stolen @red@$prayerDrain @bla@prayer points from you."
                        )
                        // if
                        // (player.getSkillManager().getCurrentLevel(Skill.PRAYER)
                        // > player.getSkillManager().getMaxLevel(Skill.PRAYER))
                        // {
                        // player.getSkillManager().setCurrentLevel(Skill.PRAYER,
                        // player.getSkillManager().getMaxLevel(Skill.PRAYER));
                        // player.getPacketSender().sendMessage("You absorbed
                        // more prayer points than you could hold!");
                        // }
                        if (p.skillManager.getCurrentLevel(Skill.PRAYER) == 0) {
                            p.packetSender.sendMessage(
                                "@red@Zamorak's wicked thoughts infect your mind and drop your prayer."
                            )
                            player.forceChat("...HAHAHAHA! Strength through Chaos!")
                            player.packetSender.sendMessage(
                                "@red@Zamorak's spiteful laughter indicates " + p.username + "'s prayer dropped."
                            )
                        }
                    }
                }
            }
        }
    },
    BANDOS_GODSWORD(intArrayOf(11696), 100, 1.25, 1.4, CombatType.MELEE, WeaponInterface.TWO_HANDED_SWORD) {
        override fun container(player: Player, target: CharacterEntity?): CombatContainer {
            player.performAnimation(Animation(11991))
            player.performGraphic(Graphic(2114))
            return object : CombatContainer(
                player, target!!, 1, CombatType.MELEE, false
            ) {
                override fun onHit(damage: Int, accurate: Boolean) {
                    if (target != null && target.isPlayer && accurate) {
                        val skillDrain = 1
                        val damageDrain = (damage * 0.1).toInt()
                        if (damageDrain < 0) return
                        (target as Player).skillManager.setCurrentLevel(
                            Skill.forId(skillDrain),
                            player.skillManager.getCurrentLevel(Skill.forId(skillDrain)) - damageDrain
                        )
                        if (target.skillManager.getCurrentLevel(Skill.forId(skillDrain)) < 1) target.skillManager.setCurrentLevel(
                            Skill.forId(skillDrain), 1
                        )
                        player.packetSender.sendMessage(
                            "You've drained " + target.username + "'s " + Misc.formatText(
                                Skill.forId(skillDrain).toString().lowercase(Locale.getDefault())
                            ) + " level by " + damageDrain + "."
                        )
                        target.packetSender.sendMessage(
                            "Your " + Misc.formatText(
                                Skill.forId(skillDrain).toString().lowercase(Locale.getDefault())
                            ) + " level has been drained."
                        )
                    }
                }
            }
        }
    },
    SARADOMIN_GODSWORD(intArrayOf(11698), 50, 1.25, 1.5, CombatType.MELEE, WeaponInterface.TWO_HANDED_SWORD) {
        override fun container(player: Player, target: CharacterEntity?): CombatContainer {
            player.performAnimation(Animation(7071))
            player.performGraphic(Graphic(1220))
            return object : CombatContainer(player, target!!, 1, CombatType.MELEE, false) {
                override fun onHit(dmg: Int, accurate: Boolean) {
                    if (accurate) {
                        val damageHeal = (dmg * 0.5).toInt()
                        val damagePrayerHeal = (dmg * 0.25).toInt()
                        player.heal(damageHeal)
                        if (player.skillManager.getCurrentLevel(Skill.PRAYER) < player.skillManager.getMaxLevel(Skill.PRAYER)) {
                            val level =
                                if (player.skillManager.getCurrentLevel(Skill.PRAYER) + damagePrayerHeal > player.skillManager.getMaxLevel(
                                        Skill.PRAYER
                                    )
                                ) player.skillManager.getMaxLevel(Skill.PRAYER) else player.skillManager.getCurrentLevel(
                                    Skill.PRAYER
                                ) + damagePrayerHeal
                            player.skillManager.setCurrentLevel(Skill.PRAYER, level)
                        }
                    }
                }
            }
        }
    },
    DRAGON_CLAWS(intArrayOf(14484, 13999), 50, 2.0, 1.8, CombatType.MELEE, WeaponInterface.CLAWS) {
        override fun container(player: Player, target: CharacterEntity?): CombatContainer {
            player.performAnimation(Animation(10961))
            player.performGraphic(Graphic(1950))
            return CombatContainer(player, target!!, 4, CombatType.MELEE, true)
        }
    };
    /**
     * Gets the weapon ID's that perform this special when activated.
     *
     * @return the weapon ID's that perform this special when activated.
     */
    /**
     * Gets the amount of special energy this attack will drain.
     *
     * @return the amount of special energy this attack will drain.
     */
    /**
     * Gets the strength bonus when performing this special attack.
     *
     * @return the strength bonus when performing this special attack.
     */
    /**
     * Gets the accuracy bonus when performing this special attack.
     *
     * @return the accuracy bonus when performing this special attack.
     */
    /**
     * Gets the combat type used when performing this special attack.
     *
     * @return the combat type used when performing this special attack.
     */
    /**
     * Gets the weapon interface used by the identifiers.
     *
     * @return the weapon interface used by the identifiers.
     */

    /**
     * Fired when the argued [Player] activates the special attack bar.
     *
     * @param player
     * the player activating the special attack bar.
     * @param target
     * the target when activating the special attack bar, will be
     * `null` if the player is not in combat while
     * activating the special bar.
     */
    open fun onActivation(player: Player, target: CharacterEntity?) {}

    /**
     * Fired when the argued [Player] is about to attack the argued
     * target.
     *
     * @param player
     * the player about to attack the target.
     * @param target
     * the entity being attacked by the player.
     * @return the combat container for this combat hook.
     */
    abstract fun container(player: Player, target: CharacterEntity?): CombatContainer

    companion object {
        /**
         * Drains the special bar for the argued [Player].
         *
         * @param player
         * the player who's special bar will be drained.
         * @param amount
         * the amount of energy to drain from the special bar.
         */
        fun drain(player: Player, amount: Int) {
            player.decrementSpecialPercentage(amount)
            player.isSpecialActivated = false
            updateBar(player)
            if (!player.isRecoveringSpecialAttack) TaskManager.submit(PlayerSpecialAmountTask(player))
            finishAchievement(player, AchievementData.PERFORM_A_SPECIAL_ATTACK)
        }

        /**
         * Restores the special bar for the argued [Player].
         *
         * @param player
         * the player who's special bar will be restored.
         * @param amount
         * the amount of energy to restore to the special bar.
         */
        fun restore(player: Player, amount: Int) {
            player.incrementSpecialPercentage(amount)
            updateBar(player)
        }

        /**
         * Updates the special bar with the amount of special energy the argued
         * [Player] has.
         *
         * @param player
         * the player who's special bar will be updated.
         */
        @JvmStatic
        fun updateBar(player: Player) {
            if (player.weapon.specialBar == -1 || player.weapon.specialMeter == -1) {
                return
            }
            var specialCheck = 10
            var specialBar = player.weapon.specialMeter
            val specialAmount = player.specialPercentage / 10
            for (i in 0..9) {
                player.packetSender.sendInterfaceComponentMoval(
                    if (specialAmount >= specialCheck) 500 else 0, 0, --specialBar
                )
                specialCheck--
            }
            player.packetSender.updateSpecialAttackOrb().sendString(
                player.weapon.specialMeter,
                if (player.isSpecialActivated) "@yel@ Special Attack (" + player.specialPercentage + "%)" else "@bla@ Special Attack (" + player.specialPercentage + "%"
            )
        }

        /**
         * Assigns special bars to the attack style interface if needed.
         *
         * @param player
         * the player to assign the special bar for.
         */
        @JvmStatic
        fun assign(player: Player) {
            if (player.weapon.specialBar == -1) {
                //if(!player.isPerformingSpecialAttack()) {
                player.isSpecialActivated = false
                player.combatSpecial = null
                updateBar(player)
                //}
                return
            }
            for (c in values()) {
                if (player.weapon == c.weaponType) {
                    if (Arrays.stream(c.identifiers)
                            .anyMatch { id: Int -> player.equipment[Equipment.WEAPON_SLOT].id == id }
                    ) {
                        player.packetSender.sendInterfaceDisplayState(player.weapon.specialBar, false)
                        player.combatSpecial = c
                        return
                    }
                }
            }
            player.packetSender.sendInterfaceDisplayState(player.weapon.specialBar, true)
            player.combatSpecial = null
        }

        @JvmStatic
        fun activate(player: Player) {
            if (checkRule(player, DuelRule.NO_SPECIAL_ATTACKS)) {
                player.packetSender.sendMessage("Special Attacks have been turned off in this duel.")
                return
            }
            if (player.combatSpecial == null) {
                return
            }
            if (player.isSpecialActivated) {
                player.isSpecialActivated = false
                updateBar(player)
            } else {
                if (player.specialPercentage < player.combatSpecial.drainAmount) {
                    player.packetSender.sendMessage(
                        "You do not have enough special attack energy left!"
                    )
                    return
                }
                val spec = player.combatSpecial
                val instantSpecial = spec === GRANITE_MAUL || spec === DRAGON_BATTLEAXE || spec === STAFF_OF_LIGHT
                if (spec !== STAFF_OF_LIGHT && player.isAutocast) {
                    Autocasting.resetAutocast(player, true)
                } else if (spec === STAFF_OF_LIGHT && player.hasStaffOfLightEffect()) {
                    player.packetSender.sendMessage("You are already being protected by the Staff of Light!")
                    return
                }
                player.isSpecialActivated = true
                if (instantSpecial) {
                    spec.onActivation(player, player.combatBuilder.victim)
                    if (spec === GRANITE_MAUL && player.combatBuilder.isAttacking && !player.combatBuilder.isCooldown) {
                        player.combatBuilder.attackTimer = 0
                        player.combatBuilder.attack(player.combatBuilder.victim)
                        player.combatBuilder.instant()
                    } else updateBar(player)
                } else {
                    updateBar(player)
                    TaskManager.submit(object : Task(1, false) {
                        public override fun execute() {
                            if (!player.isSpecialActivated) {
                                stop()
                                return
                            }
                            spec.onActivation(player, player.combatBuilder.victim)
                            stop()
                        }
                    }.bind(player))
                }
            }
        }
    }
}