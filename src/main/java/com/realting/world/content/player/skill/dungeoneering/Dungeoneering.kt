package com.realting.world.content.player.skill.dungeoneering

import com.realting.GameSettings
import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.*
import com.realting.model.entity.character.GroundItemManager
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import com.realting.world.World
import com.realting.world.content.CustomObjects
import com.realting.world.content.combat.prayer.CurseHandler
import com.realting.world.content.combat.prayer.PrayerHandler
import com.realting.world.content.dialogue.DialogueManager

/**
 * yeye
 * @author Gabriel Hannason
 */
object Dungeoneering {
    @JvmStatic
    fun start(p: Player) {
        p.packetSender.sendInterfaceRemoval()
        /*if(p.getRights() != PlayerRights.DEVELOPER) {
			p.getPacketSender().sendMessage("Dungeoneering isn't out yet.");
			return;
		}*/if (p.minigameAttributes.dungeoneeringAttributes.party == null) {
            DialogueManager.start(p, 111)
            return
        }
        val party = p.minigameAttributes.dungeoneeringAttributes.party!!
        if (party.hasEnteredDungeon()) return
        if (party.dungeoneeringFloor == null) {
            DialogueManager.start(p, 112)
            return
        } else if (party.complexity == -1) {
            DialogueManager.start(p, 113)
            return
        }
        if (party.ownerPlayer !== p) {
            p.packetSender.sendMessage("Only the party leader can start the dungeon.")
            return
        }
        var plrCannotEnter: String? = null
        for (member in party.players) {
            if (member != null) {
                member.packetSender.sendInterfaceRemoval()
                if (member.summoning.familiar != null) {
                    member.packetSender.sendMessage("You must dismiss your familiar before being allowed to enter a dungeon.")
                    p.packetSender.sendMessage("" + p.username + " has to dismiss their familiar before you can enter the dungeon.")
                    return
                }
                for (t in member.equipment.items) {
                    if (t != null && t.id > 0 && t.id != 15707) {
                        plrCannotEnter = member.username
                    }
                }
                for (t in member.inventory.items) {
                    if (t != null && t.id > 0 && t.id != 15707) {
                        plrCannotEnter = member.username
                    }
                }
                if (plrCannotEnter != null) {
                    p.packetSender.sendMessage("Your team cannot enter the dungeon because $plrCannotEnter hasn't banked")
                        .sendMessage("all of their items.")
                    return
                }
            }
        }
        party.enteredDungeon(true)
        val height = p.index * 4
        val amt = if (party.players.size >= 2) 35000 else 45000
        for (member in party.players) {
            if (member != null) {
                member.isInDung = true
                member.packetSender.sendInterfaceRemoval()
                member.minigameAttributes.dungeoneeringAttributes.damageDealt = 0
                member.minigameAttributes.dungeoneeringAttributes.deaths = 0
                member.regionInstance = null
                member.movementQueue.reset()
                member.clickDelay.reset()
                member.moveTo(
                    Position(
                        party.dungeoneeringFloor!!.entrance.x, party.dungeoneeringFloor!!.entrance.y, height
                    )
                )
                member.equipment.resetItems().refreshItems()
                member.inventory.resetItems().refreshItems()
                member.inventory.add(18201, amt)
                if (member.rights.isMember) {
                    party.sendMessage("@gre@<shad=0>Because " + member.username + " is a member, they get better starting items.")
                    member.packetSender.sendMessage("<img=10> @blu@You get your member starting items, and 8 sharks.")
                    member.inventory.add(10499, 1).add(4151, 1).add(892, 100).add(861, 1).add(385, 8)
                } else {
                    member.packetSender.sendMessage("<img=10> @blu@Members get better starting items! ::shop if you're interested!")
                }
                ItemBinding.onDungeonEntrance(member)
                PrayerHandler.deactivateAll(member)
                CurseHandler.deactivateAll(member)
                for (skill in Skill.values()) member.skillManager.setCurrentLevel(
                    skill, member.skillManager.getMaxLevel(
                        skill
                    )
                )
                member.skillManager.stopSkilling()
                member.packetSender.sendClientRightClickRemoval()
            }
        }
        party.deaths = 0
        party.kills = 0
        party.sendMessage("Welcome to Dungeoneering floor " + (party.dungeoneeringFloor!!.ordinal + 1) + ", complexity level " + party.complexity + ".")
        party.sendFrame(37508, "Party deaths: 0")
        party.sendFrame(37509, "Party kills: 0")
        TaskManager.submit(object : Task(1) {
            public override fun execute() {
                setupFloor(party, height)
                stop()
            }
        })
        p.inventory.add(Item(17489))
    }

    @JvmStatic
    fun leave(p: Player, resetTab: Boolean, leaveParty: Boolean) {
        if (p.minigameAttributes.dungeoneeringAttributes.party != null) {
            p.minigameAttributes.dungeoneeringAttributes.party!!.remove(p, resetTab, leaveParty)
            p.isInDung = false
        } else if (resetTab) {
            p.packetSender.sendTabInterface(GameSettings.QUESTS_TAB, if (p.isKillsTrackerOpen) 55000 else 639)
            p.packetSender.sendDungeoneeringTabIcon(false)
            p.packetSender.sendTab(GameSettings.QUESTS_TAB)
        }
    }

    fun setupFloor(party: DungeoneeringParty, height: Int) {
        /*
		 * Spawning npcs
		 */
        val smuggler = NPC(
            11226,
            Position(party.dungeoneeringFloor!!.smugglerPosition.x, party.dungeoneeringFloor!!.smugglerPosition.y, height)
        )
        World.register(smuggler)
        party.npcs.add(smuggler)
        for (n in party.dungeoneeringFloor!!.npcs[party.complexity - 1]) {
            val npc = NPC(n.id, n.position.copy().setZ(height))
            World.register(npc)
            party.npcs.add(npc)
        }
        /*
		 * Spawning objects
		 */for (obj in party.dungeoneeringFloor!!.objects) {
            CustomObjects.spawnGlobalObjectWithinDistance(GameObject(obj.id, obj.position.copy().setZ(height)))
        }
    }

    @JvmStatic
    fun doingDungeoneering(p: Player): Boolean {
        return p.minigameAttributes.dungeoneeringAttributes.party != null && p.minigameAttributes.dungeoneeringAttributes.party!!.hasEnteredDungeon()
    }

    @JvmStatic
    fun handlePlayerDeath(player: Player) {
        player.minigameAttributes.dungeoneeringAttributes.incrementDeaths()
        val party = player.minigameAttributes.dungeoneeringAttributes.party!!
        val pos = party.dungeoneeringFloor!!.entrance
        player.moveTo(Position(pos.x, pos.y, player.position.z))
        party.sendMessage("@red@" + player.username + " has died and been moved to the starting room.")
        if (player.skillManager.getMaxLevel(Skill.DUNGEONEERING) < 10) {
            party.sendMessage("@or2@However, because " + player.username + " has less than 10 dungeoneering,")
            party.sendMessage("@or2@their death has been ignored.")
        } else {
            party.deaths = party.deaths + 1
            party.sendFrame(37508, "Party deaths: " + party.deaths)
        }
    }

    private val misc = arrayOf(
        Item(555, 121),
        Item(557, 87),
        Item(554, 81),
        Item(565, 63),
        Item(5678),
        Item(560, 97),
        Item(861, 1),
        Item(892, 127),
        Item(18161, 2),
        Item(18159, 2),
        Item(139, 1)
    )

    @JvmStatic
    fun handleNpcDeath(p: Player, n: NPC) {
        if (n.position.z == p.position.z) {
            val party = p.minigameAttributes.dungeoneeringAttributes.party!!
            if (!party.npcs.contains(n)) return
            party.npcs.remove(n)
            party.kills = party.kills + 1
            val boss = n.id == 2060 || n.id == 8549 || n.id == 1382 || n.id == 9939
            if (boss) {
                party.setKilledBoss(true)
            }
            party.sendFrame(37509, "Party kills: " + party.kills)
            val random = Misc.getRandom(11)
            if (random >= 3 || boss) {
                GroundItemManager.spawnGroundItem(
                    p, GroundItem(
                        Item(ItemBinding.randomBindableItem),
                        n.position.copy(),
                        "Dungeoneering",
                        false,
                        -1,
                        false,
                        -1
                    )
                )
                if (boss) {
                    party.sendMessage("@red@The boss has been slain! Exit at the ladder to the East!")
                }
            } else if (random >= 100 && random <= 150) {
                val amt = 3000 + Misc.getRandom(10000)
                GroundItemManager.spawnGroundItem(
                    p, GroundItem(Item(18201, amt), n.position.copy(), "Dungeoneering", false, -1, false, -1)
                )
            } else if (random > 150 && random < 250) GroundItemManager.spawnGroundItem(
                p, GroundItem(
                    misc[Misc.getRandom(
                        misc.size - 1
                    )], n.position.copy(), "Dungeoneering", false, -1, false, -1
                )
            )
        }
    }

    const val FORM_PARTY_INTERFACE = 27224
    const val PARTY_INTERFACE = 26224
    const val DUNGEONEERING_GATESTONE_ID = 17489
}