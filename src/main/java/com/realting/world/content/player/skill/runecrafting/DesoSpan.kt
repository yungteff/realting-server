package com.realting.world.content.player.skill.runecrafting

import com.realting.engine.task.Task
import com.realting.model.container.impl.Equipment
import com.realting.world.World
import com.realting.model.movement.MovementQueue
import com.realting.engine.task.TaskManager
import com.realting.model.*
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc

object DesoSpan {
    private val SIPHONING_ANIMATION = Animation(9368)
    private const val ENERGY_FRAGMENT = 13653
    private fun gearboost(player: Player): Int {
        var boost = 0
        if (player.equipment[Equipment.HEAD_SLOT].definition != null && player.equipment[Equipment.HEAD_SLOT].definition.name.equals(
                "runecrafter hat",
                ignoreCase = true
            )
        ) {
            boost += 10
        }
        if (player.equipment[Equipment.BODY_SLOT].definition != null && player.equipment[Equipment.BODY_SLOT].definition.name.equals(
                "runecrafter robe",
                ignoreCase = true
            )
        ) {
            boost += 10
        }
        if (player.equipment[Equipment.LEG_SLOT].definition != null && player.equipment[Equipment.LEG_SLOT].definition.name.equals(
                "runecrafter skirt",
                ignoreCase = true
            )
        ) {
            boost += 10
        }
        if (player.equipment[Equipment.HANDS_SLOT].definition != null && player.equipment[Equipment.HANDS_SLOT].definition.name.equals(
                "runecrafter gloves",
                ignoreCase = true
            )
        ) {
            boost += 10
        }
        //System.out.println("boost = "+boost);
        return boost
    }

    @JvmStatic
    fun spawn() {
        var lastX = 0
        for (i in 0..5) {
            var randomX = 2595 + Misc.getRandom(12)
            if (randomX == lastX || randomX == lastX + 1 || randomX == lastX - 1) randomX++
            val randomY = 4772 + Misc.getRandom(8)
            lastX = randomX
            World.register(NPC(if (i <= 3) 8028 else 8022, Position(randomX, randomY)))
        }
    }

    @JvmStatic
    fun siphon(player: Player, n: NPC) {
        val energyType = Energy.forId(n.id)
        if (energyType != null) {
            player.skillManager.stopSkilling()
            if (player.position == n.position) MovementQueue.stepAway(player)
            player.setEntityInteraction(n)
            if (player.skillManager.getCurrentLevel(Skill.RUNECRAFTING) < energyType.levelReq) {
                player.packetSender.sendMessage("You need a Runecrafting level of at least " + energyType.levelReq + " to siphon this energy source.")
                return
            }
            if (!player.inventory.contains(ENERGY_FRAGMENT) && player.inventory.freeSlots == 0) {
                player.packetSender.sendMessage("You need some free inventory space to do this.")
                return
            }
            player.performAnimation(SIPHONING_ANIMATION)
            Projectile(player, n, energyType.projectileGraphic, 15, 44, 43, 31, 0).sendProjectile()
            val cycle = 2 + Misc.getRandom(2)
            player.currentTask = object : Task(cycle, player, false) {
                public override fun execute() {
                    if (n.constitution <= 0) {
                        player.packetSender.sendMessage("This energy source has died out.")
                        stop()
                        return
                    }
                    player.skillManager.addExperience(Skill.RUNECRAFTING, energyType.experience)
                    player.performGraphic(Graphic(energyType.playerGraphic, GraphicHeight.HIGH))
                    n.performGraphic(Graphic(energyType.npcGraphic, GraphicHeight.HIGH))
                    n.dealDamage(Hit(null, Misc.getRandom(12), Hitmask.RED, CombatIcon.MAGIC))
                    if (Misc.getRandom(30 + gearboost(player)) <= 10) {
                        player.dealDamage(
                            Hit(
                                null,
                                1 + Misc.getRandom(48),
                                Hitmask.RED,
                                CombatIcon.DEFLECT
                            )
                        )
                        player.packetSender.sendMessage("You accidently attempt to siphon too much energy, and get hurt.")
                    } else {
                        player.packetSender.sendMessage("You siphon some energy.")
                        player.inventory.add(ENERGY_FRAGMENT, 1)
                    }
                    if (n.constitution > 0 && player.constitution > 0) siphon(player, n)
                    stop()
                }
            }
            TaskManager.submit(player.currentTask)
        }
    }

    internal enum class Energy(
        var npcId: Int,
        var levelReq: Int,
        var experience: Int,
        var playerGraphic: Int,
        var projectileGraphic: Int,
        var npcGraphic: Int
    ) {
        GREEN_ENERGY(8028, 40, 18, 912, 551, 999), YELLOW_ENERGY(8022, 72, 42, 913, 554, 1006);

        companion object {
            fun forId(npc: Int): Energy? {
                for (e in values()) {
                    if (e.npcId == npc) return e
                }
                return null
            }
        }
    }
}