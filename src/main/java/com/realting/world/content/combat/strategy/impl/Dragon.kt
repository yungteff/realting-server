package com.realting.world.content.combat.strategy.impl

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.Animation
import com.realting.model.Graphic
import com.realting.model.Locations
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import com.realting.world.content.combat.CombatContainer
import com.realting.world.content.combat.CombatType
import com.realting.world.content.combat.strategy.CombatStrategy

class Dragon : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return true
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        return null
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        val dragon = entity as NPC?
        val player = victim as Player?
        if (dragon!!.isChargingAttack || dragon.constitution <= 0) {
            dragon.combatBuilder.attackTimer = 4
            return true
        }
        if (victim == null || player == null || victim.getConstitution() <= 0 || entity == null || entity.getConstitution() <= 0 || dragon == null) {
            return true
        }
        if (!victim.getCombatBuilder().lastAttack.elapsed(2000)
            && victim.getCombatBuilder().victim != null && victim.getCombatBuilder().victim !== entity
        ) { //stop dragons from PJing
            //entity.forceChat("i just tried to pj but was stopped");
            return true
        }
        if (Locations.goodDistance(dragon.entityPosition.copy(), victim.entityPosition.copy(), 1) && Misc.getRandom(5) <= 3) {
            dragon.performAnimation(Animation(dragon.definition.attackAnimation))
            dragon.combatBuilder.container = CombatContainer(dragon, victim, 1, 1, CombatType.MELEE, true)
        } else {
            dragon.isChargingAttack = true
            dragon.performAnimation(Animation(getAnimation(dragon.id)))
            dragon.combatBuilder.container = CombatContainer(dragon, victim, 1, 3, CombatType.DRAGON_FIRE, true)
            TaskManager.submit(object : Task(1, dragon, false) {
                var tick = 0
                public override fun execute() {
                    /*if(tick == 1 && dragon.getId() == 50) {
						new Projectile(dragon, victim, 393 + Misc.getRandom(3), 44, 3, 43, 43, 0).sendProjectile();
					} else*/
                    if (tick == 2) {
                        victim.performGraphic(Graphic(5))
                    } else if (tick == 3) {
                        victim.performGraphic(Graphic(5))
                        dragon.setChargingAttack(false).combatBuilder.attackTimer = 6
                        stop()
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
        return 5
    }

    override fun getCombatType(entity: CharacterEntity): CombatType? {
        return CombatType.MIXED
    }

    companion object {
        fun getAnimation(npc: Int): Int {
            var anim = 12259
            if (npc == 50) anim = 81 else if (npc == 5363 || npc == 1590 || npc == 1591 || npc == 1592) anim =
                14246 else if (npc == 51) anim = 13152
            return anim
        }
    }
}