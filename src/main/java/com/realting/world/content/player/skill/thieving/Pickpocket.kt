package com.realting.world.content.player.skill.thieving

import com.realting.model.*
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc

object Pickpocket {
    /**
     * Use this method when an NPC is pickpocketed.
     * @author Crimson
     * @since Aug 12, 2017
     * @param player - the person trying to steal
     * @param npc - the npc being stolen from
     */
    @JvmStatic
    fun handleNpc(player: Player, npc: NPC) {
        val data = PickpocketData.Companion.forNpc(npc.id)
        player.positionToFace = npc.entityPosition
        if (player.isFrozen || player.isStunned) {
            return
        }
        if (player.combatBuilder.isAttacking || player.combatBuilder.isBeingAttacked) {
            return
        }
        if (player.skillManager.getMaxLevel(Skill.THIEVING) < data?.requirement!!) {
            player.packetSender.sendMessage("You need a thieving level of " + data.requirement + " to steal from there.")
            return
        }
        if (player.inventory.isFull) {
            player.packetSender.sendMessage("You need some inventory space to hold anything more.")
            return
        }
        player.performAnimation(Animation(881))
        if (shouldFail(player, data.requirement)) {
            player.movementQueue.stun(5)
            npc.forceChat(data.failMessage.random())
            npc.positionToFace = player.entityPosition
            if (npc.definition.attackAnimation > 0) {
                npc.performAnimation(Animation(npc.definition.attackAnimation))
            } else {
                npc.performAnimation(Animation(422)) //punch anim
            }
            player.performGraphic(Graphic(254))
            player.dealDamage(Hit(data.damage.random()))
            player.combatBuilder.addDamage(player, data.damage.random())
            player.updateFlag.isUpdateRequired
            player.updateFlag.flag(Flag.SINGLE_HIT)
            return
        }
        player.inventory.add(data.reward)
        player.skillManager.addExperience(Skill.THIEVING, data.experience)
        player.packetSender.sendMessage("You steal from the " + npc.definition.name + "'s pocket.")
    }

    fun shouldFail(player: Player, levelReq: Int): Boolean {
        return player.skillManager.getCurrentLevel(Skill.THIEVING) - levelReq < Misc.getRandom(levelReq)
    }
}