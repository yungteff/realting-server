package com.realting.world.content.minigames

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.Graphic
import com.realting.model.Position
import com.realting.model.RegionInstance
import com.realting.model.RegionInstance.RegionInstanceType
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.world.World
import com.realting.world.content.PlayerPanel
import com.realting.world.content.dialogue.DialogueManager

/**
 * @author Gabriel Hannason
 */
object Nomad {
    @JvmStatic
    fun startFight(p: Player) {
        if (p.minigameAttributes.nomadAttributes.hasFinishedPart(1)) return
        p.packetSender.sendInterfaceRemoval()
        p.moveTo(Position(3361, 5856, p.index * 4))
        p.regionInstance = RegionInstance(p, RegionInstanceType.NOMAD)
        TaskManager.submit(object : Task(1, p, false) {
            var tick = 0
            public override fun execute() {
                if (tick >= 4) {
                    val n = NPC(8528, Position(p.entityPosition.x, p.entityPosition.y - 2, p.entityPosition.z)).setSpawnedFor(p)
                    World.register(n)
                    p.regionInstance.npcsList.add(n)
                    n.combatBuilder.attack(p)
                    n.forceChat("You want to throw hands, brah?!")
                    n.performGraphic(Graphic(1295))
                    stop()
                }
                tick++
            }
        })
    }

    @JvmStatic
    fun endFight(p: Player, killed: Boolean) {
        if (p.regionInstance != null) p.regionInstance.destruct()
        p.moveTo(Position(1889, 3177))
        if (killed) {
            p.restart()
            p.minigameAttributes.nomadAttributes.setPartFinished(1, true)
            DialogueManager.start(p, 53)
            PlayerPanel.refreshPanel(p)
        }
    }

    @JvmStatic
    fun openQuestLog(p: Player) {
        for (i in 8145..8195) p.packetSender.sendString(i, "")
        p.packetSender.sendInterface(8134)
        p.packetSender.sendString(8136, "Close window")
        p.packetSender.sendString(8144, "" + questTitle)
        p.packetSender.sendString(8145, "")
        var questIntroIndex = 0
        for (i in 8147 until 8147 + questIntro.size) {
            p.packetSender.sendString(i, "@dre@" + questIntro[questIntroIndex])
            questIntroIndex++
        }
        var questGuideIndex = 0
        for (i in 8147 + questIntro.size until 8147 + questIntro.size + questGuide.size) {
            if (!p.minigameAttributes.nomadAttributes.hasFinishedPart(questGuideIndex)) p.packetSender.sendString(
                i, "" + questGuide[questGuideIndex]
            ) else p.packetSender.sendString(i, "@str@" + questGuide[questGuideIndex] + "")
            questGuideIndex++
        }
        if (p.minigameAttributes.nomadAttributes.hasFinishedPart(1)) p.packetSender.sendString(
            8147 + questIntro.size + questGuide.size, "@dre@Quest complete!"
        )
    }

    fun getQuestTabPrefix(player: Player): String {
        if (player.minigameAttributes.nomadAttributes.hasFinishedPart(0) && !player.minigameAttributes.nomadAttributes.hasFinishedPart(
                1
            )
        ) {
            return "@yel@"
        } else if (player.minigameAttributes.nomadAttributes.hasFinishedPart(1)) {
            return "@gre@"
        }
        return "@red@"
    }

    private const val questTitle = "Nomad's Requiem"
    private val questIntro = arrayOf(
        "Nomad is searching for a worthy opponent.", "Are you eligible for the job?", ""
    )
    private val questGuide = arrayOf(
        "Talk to Nomad and accept his challenge to a fight.", "Defeat Nomad."
    )
}