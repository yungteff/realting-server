package com.realting.world.content.combat.strategy.impl.bosses.gwd

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

class Tsutsuroth : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return victim!!.isPlayer && (victim as Player?)!!.minigameAttributes.godwarsDungeonAttributes.hasEnteredRoom()
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        return null
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        val tsutsuroth = entity as NPC?
        if (victim!!.constitution <= 0) {
            return true
        }
        if (tsutsuroth!!.isChargingAttack) {
            return true
        }
        val target = victim as Player?
        val style = if (Misc.getRandom(8) >= 6 && Locations.goodDistance(
                tsutsuroth.entityPosition, victim.entityPosition, 2
            )
        ) CombatType.MELEE else CombatType.MAGIC
        if (style === CombatType.MELEE) {
            tsutsuroth.performAnimation(Animation(6945))
            tsutsuroth.combatBuilder.container = CombatContainer(tsutsuroth, victim, 1, 1, CombatType.MELEE, true)
            val specialAttack = Misc.getRandom(4)
            if (specialAttack == 2) {
                var amountToDrain = Misc.getRandom(400)
                target!!.packetSender.sendMessage("K'ril Tsutsaroth slams through your defence and steals some Prayer points..")
                if (amountToDrain > target.skillManager.getCurrentLevel(Skill.PRAYER)) {
                    amountToDrain = target.skillManager.getCurrentLevel(Skill.PRAYER)
                }
                target.skillManager.setCurrentLevel(
                    Skill.PRAYER, target.skillManager.getCurrentLevel(Skill.PRAYER) - amountToDrain
                )
                if (target.skillManager.getCurrentLevel(Skill.PRAYER) <= 0) {
                    target.packetSender.sendMessage("You have run out of Prayer points!")
                }
            }
        } else {
            tsutsuroth.performAnimation(anim1)
            tsutsuroth.isChargingAttack = true
            TaskManager.submit(object : Task(2, target, false) {
                var tick = 0
                public override fun execute() {
                    when (tick) {
                        0 -> for (t in Misc.getCombinedPlayerList(target)) {
                            if (t == null || t.location !== Locations.Location.GODWARS_DUNGEON || t.isTeleporting) continue
                            if (t.entityPosition.distanceToPoint(tsutsuroth.entityPosition.x, tsutsuroth.entityPosition.y) > 20) continue
                            Projectile(tsutsuroth, target, graphic1.id, 44, 3, 43, 43, 0).sendProjectile()
                        }
                        2 -> {
                            for (t in Misc.getCombinedPlayerList(target)) {
                                if (t == null || t.location !== Locations.Location.GODWARS_DUNGEON) continue
                                target!!.performGraphic(graphic2)
                                tsutsuroth.combatBuilder.victim = t
                                CombatHit(
                                    tsutsuroth.combatBuilder, CombatContainer(tsutsuroth, t, 1, CombatType.MAGIC, true)
                                ).handleAttack()
                            }
                            tsutsuroth.isChargingAttack = false
                            stop()
                        }
                    }
                    tick++
                }
            })
        }
        return true
    }

    override fun attackDelay(entity: CharacterEntity?): Int {
        return entity!!.attackSpeed
    }

    override fun attackDistance(entity: CharacterEntity): Int {
        return 3
    }

    override fun getCombatType(entity: CharacterEntity): CombatType? {
        return CombatType.MIXED
    }

    companion object {
        private val anim1 = Animation(6947)
        private val graphic1 = Graphic(1211, GraphicHeight.MIDDLE)
        private val graphic2 = Graphic(390)
    }
}