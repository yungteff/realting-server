package com.realting.world.content.combat.strategy.impl.bosses

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.*
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.util.Misc
import com.realting.world.content.combat.CombatContainer
import com.realting.world.content.combat.CombatType
import com.realting.world.content.combat.strategy.CombatStrategy

class Nomad : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return true
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        return null
    }

    override fun customContainerAttack(attacker: CharacterEntity?, target: CharacterEntity?): Boolean {
        val randomNomad = Misc.getRandom(30)
        val nomad = attacker as NPC?
        if (target!!.constitution <= 0) {
            nomad!!.forceChat("Muhahaha, easy!")
            return true
        }
        if (nomad!!.isChargingAttack) return true
        if (randomNomad >= 0 && randomNomad <= 15) {
            val meleeDistance = Locations.goodDistance(nomad.entityPosition, target.entityPosition, 2)
            val heal = !nomad.hasHealed() && nomad.constitution < 4000
            if (meleeDistance) {
                if (nomad.constitution > 0 && !heal) {
                    nomad.performAnimation(anim2)
                    nomad.combatBuilder.container = CombatContainer(nomad, target, 1, 1, CombatType.MELEE, true)
                } else {
                    nomad.setHealed(true)
                    nomad.performGraphic(gfx2)
                    nomad.performAnimation(anim3)
                    nomad.movementQueue.setLockMovement(true)
                    nomad.forceChat("Zamorak.. Aid me..")
                    nomad.isChargingAttack = true
                    TaskManager.submit(object : Task(1, nomad, false) {
                        var ticks = 0
                        public override fun execute() {
                            nomad.constitution = nomad.constitution + 600
                            ticks++
                            if (ticks >= 5) {
                                nomad.forceChat("Zamorak, I am in your favor.")
                                nomad.movementQueue.setLockMovement(false)
                                nomad.isChargingAttack = false
                                stop()
                            }
                        }
                    })
                }
            } else if (randomNomad >= 23 && randomNomad <= 29) {
                nomad.isChargingAttack = true
                nomad.movementQueue.setLockMovement(true)
                TaskManager.submit(object : Task(1, nomad, false) {
                    var ticks = 0
                    public override fun execute() {
                        if (ticks == 0 || ticks == 4) {
                            nomad.performGraphic(gfx2)
                            nomad.performAnimation(anim3)
                        }
                        if (ticks == 7) nomad.forceChat("Almost.. Almost there..")
                        if (ticks == 9 || ticks == 11 || ticks == 13) {
                            nomad.forceChat("Die!")
                            nomad.performAnimation(Animation(12697))
                            nomad.performGraphic(Graphic(65565))
                        }
                        if (ticks == 10 || ticks == 12 || ticks == 14) {
                            nomad.performAnimation(Animation(12697))
                            Projectile(nomad, target, 2283, 44, 3, 43, 31, 0).sendProjectile()
                            nomad.combatBuilder.container = CombatContainer(nomad, target, 1, 1, CombatType.MAGIC, true)
                        } else if (ticks == 16) {
                            nomad.movementQueue.setLockMovement(false)
                            nomad.isChargingAttack = false
                            stop()
                        }
                        ticks++
                    }
                })
            } else if (randomNomad >= 16 && randomNomad <= 19) {
                nomad.isChargingAttack = true
                nomad.movementQueue.reset().setLockMovement(true)
                TaskManager.submit(object : Task(1, nomad, false) {
                    var ticks = 0
                    public override fun execute() {
                        if (ticks == 0) {
                            target.movementQueue.freeze(15)
                            target.performGraphic(gfx3)
                            nomad.forceChat("Freeze!")
                            nomad.performAnimation(Animation(12697))
                            nomad.combatBuilder.container = CombatContainer(nomad, target, 1, CombatType.MAGIC, true)
                        } else if (ticks == 1 || ticks == 4 || ticks == 5) {
                            nomad.performGraphic(gfx2)
                            nomad.performAnimation(anim3)
                        }
                        if (ticks == 5) nomad.forceChat("Zamorak, please! Allow me to me channel your power!")
                        if (ticks == 10) nomad.forceChat("Adventurer, prepare to be blown away!")
                        if (ticks == 18) nomad.forceChat("I call upon you, Zamorak!")
                        if (ticks == 20) nomad.performAnimation(Animation(12697))
                        if (ticks == 23) Projectile(nomad, target, 2001, 44, 3, 43, 31, 0).sendProjectile()
                        if (ticks == 24) target.performGraphic(Graphic(2004))
                        if (ticks == 25) {
                            nomad.combatBuilder.container =
                                CombatContainer(nomad, target, 1, 1, CombatType.MAGIC, false)
                            target.movementQueue.freeze(0)
                            nomad.movementQueue.setLockMovement(false)
                            nomad.isChargingAttack = false
                            stop()
                        }
                        ticks++
                    }
                })
            } else {
                if (meleeDistance) {
                    nomad.performAnimation(anim2)
                    nomad.forceChat("You shall fall!")
                    nomad.combatBuilder.container = CombatContainer(nomad, target, 1, 1, CombatType.MELEE, false)
                } else {
                    target.movementQueue.freeze(15)
                    target.performGraphic(gfx3)
                    nomad.forceChat("Freeze!")
                    nomad.performAnimation(Animation(12697))
                    nomad.combatBuilder.container = CombatContainer(nomad, target, 1, 1, CombatType.MAGIC, true)
                }
            }
        }
        return true
    }

    override fun attackDelay(entity: CharacterEntity?): Int {
        return entity!!.attackSpeed
    }

    override fun attackDistance(entity: CharacterEntity): Int {
        return 8
    }

    override fun getCombatType(entity: CharacterEntity): CombatType? {
        return CombatType.MIXED
    }

    companion object {
        private val anim2 = Animation(12696)
        private val anim3 = Animation(12698)
        private val gfx2 = Graphic(2281, GraphicHeight.LOW)
        private val gfx3 = Graphic(369, GraphicHeight.LOW)
    }
}