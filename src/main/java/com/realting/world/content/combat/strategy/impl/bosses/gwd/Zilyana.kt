package com.realting.world.content.combat.strategy.impl.bosses.gwd

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

class Zilyana : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return victim!!.isPlayer && (victim as Player?)!!.minigameAttributes.godwarsDungeonAttributes.hasEnteredRoom()
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        return null
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        val zilyana = entity as NPC?
        if (victim!!.constitution <= 0) {
            return true
        }
        if (Locations.goodDistance(zilyana!!.position.copy(), victim.position.copy(), 1) && Misc.getRandom(5) <= 3) {
            zilyana.performAnimation(Animation(zilyana.definition.attackAnimation))
            zilyana.combatBuilder.container = CombatContainer(zilyana, victim, 1, 1, CombatType.MELEE, true)
        } else {
            zilyana.performAnimation(attack_anim)
            zilyana.performGraphic(Graphic(1220))
            zilyana.combatBuilder.container = CombatContainer(zilyana, victim, 2, 3, CombatType.MAGIC, true)
            zilyana.combatBuilder.attackTimer = 7
        }
        return true
    }

    override fun attackDelay(entity: CharacterEntity?): Int {
        return entity!!.attackSpeed
    }

    override fun attackDistance(entity: CharacterEntity): Int {
        return 1
    }

    override fun getCombatType(entity: CharacterEntity): CombatType? {
        return CombatType.MIXED
    }

    companion object {
        private val attack_anim = Animation(6967)
    }
}