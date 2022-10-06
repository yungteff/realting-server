package com.realting.world.content.combat

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.Animation
import com.realting.model.Graphic
import com.realting.model.GraphicHeight
import com.realting.model.Locations
import com.realting.model.container.impl.Equipment
import com.realting.model.definitions.WeaponAnimations
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.npc.NPCMovementCoordinator.CoordinateState
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import com.realting.world.content.Kraken
import com.realting.world.content.Sounds
import com.realting.world.content.combat.CombatContainer.ContainerHit
import com.realting.world.content.combat.strategy.impl.bosses.Nex
import com.realting.world.content.player.events.Achievements.AchievementData
import com.realting.world.content.player.events.Achievements.doProgress
import com.realting.world.content.player.events.Achievements.finishAchievement
import java.util.concurrent.CopyOnWriteArrayList

class HitQueue {
    val combatHits = CopyOnWriteArrayList<CombatHit?>()
    fun append(c: CombatHit?) {
        if (c == null) {
            return
        }
        if (c.initialRun()) {
            c.handleAttack()
        } else {
            combatHits.add(c)
        }
    }

    fun process() {
        for (c in combatHits) {
            if (c == null) {
                combatHits.remove(c)
                continue
            }
            if (c.delay > 0) {
                c.delay--
            } else {
                c.handleAttack()
                combatHits.remove(c)
            }
        }
    }

    class CombatHit {
        /** The attacker instance.  */
        private var attacker: CharacterEntity

        /** The victim instance.  */
        private var victim: CharacterEntity?

        /** The attacker's combat builder attached to this task.  */
        private var builder: CombatBuilder

        /** The attacker's combat container that will be used.  */
        private var container: CombatContainer

        /** The total damage dealt during this hit.  */
        private var damage = 0
        private var initialDelay = 0
        var delay = 0

        constructor(builder: CombatBuilder, container: CombatContainer) {
            this.builder = builder
            this.container = container
            attacker = builder.character
            victim = builder.victim
        }

        constructor(builder: CombatBuilder, container: CombatContainer, delay: Int) {
            this.builder = builder
            this.container = container
            attacker = builder.character
            victim = builder.victim
            initialDelay = delay
            this.delay = initialDelay
        }

        fun handleAttack() {
            if (attacker.constitution <= 0 || !attacker.isRegistered) {
                return
            }
            if (victim == null) {
                return
            }
            // Do any hit modifications to the container here first.
            if (attacker.isPlayer && victim!!.isNpc) {
                val npc = victim as NPC
                if (Kraken.isWhirpool(npc)) {
                    Kraken.attackPool(attacker as Player, npc)
                    return
                }
            }
            if (container.modifiedDamage > 0) {
                container.allHits { context: ContainerHit? ->
                    context?.hit?.damage = container.modifiedDamage
                    context?.isAccurate = true
                }
            }

            // Now we send the hitsplats if needed! We can't send the hitsplats
            // there are none to send, or if we're using magic and it splashed.
            if (container.hits.isNotEmpty() && (container.combatType != CombatType.MAGIC || attacker.isNpc) || container.isAccurate) {
                /** PRAYERS  */
                CombatFactory.applyPrayerProtection(container, builder)
                damage = container.damage
                victim!!.combatBuilder.addDamage(attacker, damage)
                container.dealDamage()
                /** MISC  */
                if (attacker.isPlayer) {
                    val p = attacker as Player
                    if (damage > 0) {
                        if (p.location === Locations.Location.PEST_CONTROL_GAME) {
                            p.minigameAttributes.pestControlAttributes.incrementDamageDealt(damage)
                        } else if (p.location === Locations.Location.DUNGEONEERING) {
                            p.minigameAttributes.dungeoneeringAttributes.incrementDamageDealt(damage)
                        }
                        /** ACHIEVEMENTS  */
                        if (container.combatType == CombatType.MELEE) {
                            doProgress(p, AchievementData.DEAL_EASY_DAMAGE_USING_MELEE, damage)
                            doProgress(p, AchievementData.DEAL_MEDIUM_DAMAGE_USING_MELEE, damage)
                            doProgress(p, AchievementData.DEAL_HARD_DAMAGE_USING_MELEE, damage)
                        } else if (container.combatType == CombatType.RANGED) {
                            doProgress(p, AchievementData.DEAL_EASY_DAMAGE_USING_RANGED, damage)
                            doProgress(p, AchievementData.DEAL_MEDIUM_DAMAGE_USING_RANGED, damage)
                            doProgress(p, AchievementData.DEAL_HARD_DAMAGE_USING_RANGED, damage)
                        } else if (container.combatType == CombatType.MAGIC) {
                            doProgress(p, AchievementData.DEAL_EASY_DAMAGE_USING_MAGIC, damage)
                            doProgress(p, AchievementData.DEAL_MEDIUM_DAMAGE_USING_MAGIC, damage)
                            doProgress(p, AchievementData.DEAL_HARD_DAMAGE_USING_MAGIC, damage)
                        }
                        if (victim!!.isPlayer) {
                            finishAchievement(p, AchievementData.FIGHT_ANOTHER_PLAYER)
                        }
                    }
                } else {
                    if (victim!!.isPlayer && container.combatType == CombatType.DRAGON_FIRE) {
                        val p = victim as Player
                        if (Misc.getRandom(4) <= 3 && p.equipment.items[Equipment.SHIELD_SLOT].id == 11283) {
                            p.positionToFace = attacker.position.copy()
                            CombatFactory.chargeDragonFireShield(p)
                        }
                        if (p.equipment.items[Equipment.SHIELD_SLOT].id == 1540 || p.equipment.items[Equipment.SHIELD_SLOT].id == 13655) {
                            p.positionToFace = attacker.position.copy()
                            CombatFactory.sendFireMessage(p)
                        }
                        if (damage >= 160) {
                            (victim as Player).packetSender.sendMessage("You are badly burnt by the dragon's fire!")
                        }
                    }
                }
            }


            // Give experience based on the hits.
            CombatFactory.giveExperience(builder, container, damage)
            if (!container.isAccurate) {
                if (container.combatType == CombatType.MAGIC && attacker.currentlyCasting != null) {
                    victim!!.performGraphic(Graphic(85, GraphicHeight.MIDDLE))
                    attacker.currentlyCasting.finishCast(attacker, victim, false, 0)
                    attacker.currentlyCasting = null
                }
            } else {
                CombatFactory.handleArmorEffects(attacker, victim, damage, container.combatType)
                CombatFactory.handlePrayerEffects(attacker, victim, damage, container.combatType)
                CombatFactory.handleSpellEffects(attacker, victim, damage, container.combatType)
                attacker.poisonVictim(victim, container.combatType)

                // Finish the magic spell with the correct end graphic.
                if (container.combatType == CombatType.MAGIC && attacker.currentlyCasting != null) {
                    attacker.currentlyCasting.endGraphic()
                        .ifPresent { graphic: Graphic-> victim!!.performGraphic(graphic) }
                    attacker.currentlyCasting.finishCast(attacker, victim, true, damage)
                    attacker.currentlyCasting = null
                }
            }

            // Degrade items that need to be degraded
            if (victim!!.isPlayer) {
                CombatFactory.handleDegradingArmor(victim as Player?)
            }
            if (attacker.isPlayer) {
                CombatFactory.handleDegradingWeapons(attacker as Player)
            }

            // Send the defensive animations.
            if (victim!!.combatBuilder.getAttackTimer() <= 2) {
                if (victim!!.isPlayer) {
                    victim!!.performAnimation(Animation(WeaponAnimations.getBlockAnimation(victim as Player?)))
                    if ((victim as Player).interfaceId > 0) (victim as Player).packetSender.sendInterfaceRemoval()
                } else if (victim!!.isNpc) {
                    if ((victim as NPC).id !in 6142..6145) (victim as NPC).performAnimation(Animation((victim as NPC).definition.defenceAnimation))
                }
            }

            // Fire the container's dynamic hit method.
            container.onHit(damage, container.isAccurate)

            // And finally auto-retaliate if needed.
            if (!victim!!.combatBuilder.isAttacking || victim!!.combatBuilder.isCooldown || victim!!.isNpc && (victim as NPC).findNewTarget()) {
                if (shouldRetaliate()) {
                    if (initialDelay == 0) {
                        TaskManager.submit(object : Task(1, victim, false) {
                            override fun execute() {
                                if (shouldRetaliate()) {
                                    retaliate()
                                }
                                stop()
                            }
                        })
                    } else {
                        retaliate()
                    }
                }
            }
            if (attacker.isNpc && victim!!.isPlayer) {
                val npc = attacker as NPC
                val p = victim as Player
                if (npc.switchesVictim() && Misc.getRandom(6) <= 1) {
                    if (npc.definition.isAggressive) {
                        npc.setFindNewTarget(true)
                    } else {
                        if (p.localPlayers.size >= 1) {
                            val list = p.localPlayers
                            val c = list[Misc.getRandom(list.size - 1)]
                            npc.combatBuilder.attack(c)
                        }
                    }
                }
                Sounds.sendSound(p, Sounds.getPlayerBlockSounds(p.equipment[Equipment.WEAPON_SLOT].id))
                /** CUSTOM ON DAMAGE STUFF  */
                if ((victim as Player).isPlayer && npc.id == 13447) {
                    Nex.dealtDamage(victim as Player, damage)
                }
            } else if (attacker.isPlayer) {
                val player = attacker as Player
                player.packetSender.sendCombatBoxData(victim)
                /** SKULLS  */
                if (player.location === Locations.Location.WILDERNESS && victim!!.isPlayer) {
                    val didRetaliate = player.combatBuilder.didAutoRetaliate()
                    if (!didRetaliate) {
                        val soloRetaliate = !player.combatBuilder.isBeingAttacked
                        val multiRetaliate =
                            player.combatBuilder.isBeingAttacked && player.combatBuilder.lastAttacker !== victim && Locations.inMulti(
                                player
                            )
                        if (soloRetaliate || multiRetaliate) {
                            CombatFactory.skullPlayer(player)
                        }
                    }
                }
                player.lastCombatType = container.combatType
                Sounds.sendSound(player, Sounds.getPlayerAttackSound(player))
                /** CUSTOM ON DAMAGE STUFF  */
                if (victim!!.isNpc) {
                    if ((victim as NPC).id == 13447) {
                        Nex.takeDamage(player, damage)
                    }
                } else {
                    Sounds.sendSound(
                        victim as Player?, Sounds.getPlayerBlockSounds(
                            (victim as Player).equipment[Equipment.WEAPON_SLOT].id
                        )
                    )
                }
            }
        }

        fun shouldRetaliate(): Boolean {
            if (victim!!.isPlayer) {
                if (attacker.isNpc) {
                    if (!(attacker as NPC).definition.isAttackable) {
                        return false
                    }
                }
                return victim!!.isPlayer && (victim as Player?)!!.isAutoRetaliate && !victim!!.movementQueue.isMoving && (victim as Player?)!!.walkToTask == null
            } else if (!(attacker.isNpc && (attacker as NPC).isSummoningNpc)) {
                val npc = victim as NPC?
                return npc!!.movementCoordinator.coordinateState == CoordinateState.HOME && npc.location !== Locations.Location.PEST_CONTROL_GAME
            }
            return false
        }

        fun retaliate() {
            if (victim!!.isPlayer) {
                victim!!.combatBuilder.setDidAutoRetaliate(true)
                victim!!.combatBuilder.attack(attacker)
            } else if (victim!!.isNpc) {
                val npc = victim as NPC?
                npc!!.combatBuilder.attack(attacker)
                npc.setFindNewTarget(false)
            }
        }

        fun initialRun(): Boolean {
            return delay == 0
        }
    }
}