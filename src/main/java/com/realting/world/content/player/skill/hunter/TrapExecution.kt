package com.realting.world.content.player.skill.hunter

import com.realting.model.Skill
import com.realting.model.entity.character.npc.NPC
import com.realting.util.Misc

/**
 *
 * @author Rene
 */
object TrapExecution {
    /**
     * Handles Trap's with a state of 'set'
     *
     * @param trap
     */
    @JvmStatic
    fun setTrapProcess(trap: Trap) {
        for (npc in Hunter.HUNTER_NPC_LIST) {
            if (npc == null || !npc.isVisible) {
                continue
            }
            if (trap is BoxTrap && npc.id != 5079 && npc.id != 5080) continue
            if (trap is SnareTrap && (npc.id == 5079 || npc.id == 5080)) continue
            if (npc.entityPosition.isWithinDistance(trap.gameObject.entityPosition, 1)) {
                if (Misc.getRandom(100) < successFormula(trap, npc)) {
                    Hunter.catchNPC(trap, npc)
                    return
                }
            }
        }
    }

    fun successFormula(trap: Trap, npc: NPC?): Int {
        if (trap.owner!! == null) return 0
        var chance = 70
        if (Hunter.hasLarupia(trap.owner!!)) chance = chance + 10
        chance = (chance + (trap.owner!!.skillManager.getCurrentLevel(Skill.HUNTER) / 1.5).toInt() + 10)
        if (trap.owner!!.skillManager.getCurrentLevel(Skill.HUNTER) < 25) chance = (chance * 1.5).toInt() + 8
        if (trap.owner!!.skillManager.getCurrentLevel(Skill.HUNTER) < 40) chance = (chance * 1.4).toInt() + 3
        if (trap.owner!!.skillManager.getCurrentLevel(Skill.HUNTER) < 50) chance = (chance * 1.3).toInt() + 1
        if (trap.owner!!.skillManager.getCurrentLevel(Skill.HUNTER) < 55) chance = (chance * 1.2).toInt()
        if (trap.owner!!.skillManager.getCurrentLevel(Skill.HUNTER) < 60) chance = (chance * 1.1).toInt()
        if (trap.owner!!.skillManager.getCurrentLevel(Skill.HUNTER) < 65) chance = (chance * 1.05).toInt() + 3
        return chance
    }

    /**
     * Handles the cycle management of each traps timer
     *
     * @param trap
     * is the given trap we are managing
     * @return false if the trap is too new to have caught
     */
    @JvmStatic
    fun trapTimerManagement(trap: Trap): Boolean {
        if (trap.ticks > 0) trap.ticks = trap.ticks - 1
        if (trap.ticks <= 0) {
            Hunter.deregister(trap)
            trap.owner!!.packetSender.sendMessage(
                "You left your trap for too long, and it collapsed."
            )
        }
        return true
    }
}