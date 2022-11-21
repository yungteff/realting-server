package com.realting.world.content.combat.strategy.impl.bosses

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.*
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import com.realting.world.content.combat.CombatContainer
import com.realting.world.content.combat.CombatType
import com.realting.world.content.combat.HitQueue.CombatHit
import com.realting.world.content.combat.strategy.CombatStrategy

class CorporealBeast : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return victim!!.isPlayer
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        return null
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        val cB = entity as NPC?
        if (cB!!.isChargingAttack || cB.constitution <= 0) {
            return true
        }
        val target = victim as Player?
        var stomp = false
        for (t in Misc.getCombinedPlayerList(target)) {
            if (t == null || t.location !== Locations.Location.CORPOREAL_BEAST) continue
            if (Locations.goodDistance(t.entityPosition, cB.entityPosition, 1)) {
                stomp = true
                cB.combatBuilder.victim = t
                CombatHit(cB.combatBuilder, CombatContainer(cB, t, 1, CombatType.MAGIC, true)).handleAttack()
            }
        }
        if (stomp) {
            cB.performAnimation(attack_anim)
            cB.performGraphic(attack_graphic)
        }
        var attackStyle = Misc.getRandom(4)
        if (attackStyle == 0 || attackStyle == 1) { // melee
            val distanceX = target!!.entityPosition.x - cB.entityPosition.x
            val distanceY = target.entityPosition.y - cB.entityPosition.y
            if (distanceX > 4 || distanceX < -1 || distanceY > 4 || distanceY < -1) attackStyle = 4 else {
                cB.performAnimation(Animation(if (attackStyle == 0) 10057 else 10058))
                if (target.location === Locations.Location.CORPOREAL_BEAST) cB.combatBuilder.container =
                    CombatContainer(
                        cB, target, 1, 1, CombatType.MELEE, true
                    )
                return true
            }
        } else if (attackStyle == 2) { // powerfull mage spiky ball
            cB.performAnimation(attack_anim2)
            cB.combatBuilder.container = CombatContainer(cB, target!!, 1, 2, CombatType.MAGIC, true)
            Projectile(cB, target, 1825, 44, 3, 43, 43, 0).sendProjectile()
        } else if (attackStyle == 3) { // translucent ball of energy
            cB.performAnimation(attack_anim2)
            if (target!!.location === Locations.Location.CORPOREAL_BEAST) cB.combatBuilder.container = CombatContainer(
                cB, target!!, 1, 2, CombatType.MAGIC, true
            )
            Projectile(cB, target, 1823, 44, 3, 43, 43, 0).sendProjectile()
            TaskManager.submit(object : Task(1, target, false) {
                public override fun execute() {
                    val skill = Misc.getRandom(4)
                    val skillT = Skill.forId(skill)
                    val player = target
                    var lvl = player!!.skillManager.getCurrentLevel(skillT)
                    lvl -= 1 + Misc.getRandom(4)
                    player.skillManager.setCurrentLevel(
                        skillT,
                        if (player.skillManager.getCurrentLevel(skillT) - lvl <= 0) 1 else lvl
                    )
                    target.packetSender.sendMessage("Your " + skillT.formatName + " has been slighly drained!")
                    stop()
                }
            })
        }
        if (attackStyle == 4) {
            cB.performAnimation(attack_anim2)
            for (t in Misc.getCombinedPlayerList(target)) {
                if (t == null || t.location !== Locations.Location.CORPOREAL_BEAST) continue
                Projectile(cB, target, 1824, 44, 3, 43, 43, 0).sendProjectile()
            }
            TaskManager.submit(object : Task(1, target, false) {
                public override fun execute() {
                    for (t in Misc.getCombinedPlayerList(target)) {
                        if (t == null || t.location !== Locations.Location.CORPOREAL_BEAST) continue
                        cB.combatBuilder.victim = t
                        CombatHit(cB.combatBuilder, CombatContainer(cB, t, 1, CombatType.RANGED, true)).handleAttack()
                    }
                    stop()
                }
            })
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
        private val attack_anim = Animation(10496)
        private val attack_anim2 = Animation(10410)
        private val attack_graphic = Graphic(1834)
    }
}