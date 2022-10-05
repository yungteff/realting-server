package com.realting.world.content.player.skill.dungeoneering

import com.realting.world.content.dialogue.DialogueManager
import com.realting.GameSettings
import com.realting.model.*
import com.realting.world.World
import com.realting.model.entity.character.GroundItemManager
import java.util.concurrent.CopyOnWriteArrayList
import com.realting.world.content.dialogue.impl.DungPartyInvitation
import com.realting.world.content.PlayerPanel
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc

/**
 * @author Gabriel Hannason
 */
class DungeoneeringParty(val ownerPlayer: Player) {
    var dungeoneeringFloor: DungeoneeringFloor? = null
    var complexity = -1
    val players: CopyOnWriteArrayList<Player>
    val npcs = CopyOnWriteArrayList<NPC?>()
    val groundItems = CopyOnWriteArrayList<GroundItem>()
    var gatestonePosition: Position? = null
    private var hasEnteredDungeon = false
    var kills = 0
    var deaths = 0
    private var killedBoss = false

    init {
        players = CopyOnWriteArrayList()
        players.add(ownerPlayer)
    }

    fun invite(p: Player) {
        if (getOwner() == null || p === getOwner()) return
        if (hasEnteredDungeon) {
            getOwner()!!.packetSender.sendMessage("You cannot invite anyone right now.")
            return
        }
        if (players.size >= 5) {
            getOwner()!!.packetSender.sendMessage("Your party is full.")
            return
        }
        if (p.location !== Locations.Location.DUNGEONEERING || p.isTeleporting) {
            getOwner()!!.packetSender.sendMessage("That player is not in Deamonheim.")
            return
        }
        if (players.contains(p)) {
            getOwner()!!.packetSender.sendMessage("That player is already in your party.")
            return
        }
        if (p.minigameAttributes.dungeoneeringAttributes.party != null) {
            getOwner()!!.packetSender.sendMessage("That player is currently in another party.")
            return
        }
        if (p.rights != PlayerRights.DEVELOPER && System.currentTimeMillis() - getOwner()!!.minigameAttributes.dungeoneeringAttributes.lastInvitation < 2000) {
            getOwner()!!.packetSender.sendMessage("You must wait 2 seconds between each party invitation.")
            return
        }
        if (p.busy()) {
            getOwner()!!.packetSender.sendMessage("That player is currently busy.")
            return
        }
        getOwner()!!.minigameAttributes.dungeoneeringAttributes.lastInvitation = System.currentTimeMillis()
        DialogueManager.start(p, DungPartyInvitation(getOwner(), p))
        getOwner()!!.packetSender.sendMessage("An invitation has been sent to " + p.username + ".")
    }

    fun add(p: Player) {
        if (players.size >= 5) {
            p.packetSender.sendMessage("That party is already full.")
            return
        }
        if (hasEnteredDungeon) {
            p.packetSender.sendMessage("This party has already entered a dungeon.")
            return
        }
        if (p.location !== Locations.Location.DUNGEONEERING || p.isTeleporting) {
            return
        }
        sendMessage("" + p.username + " has joined the party.")
        p.packetSender.sendMessage("You've joined " + getOwner()!!.username + "'s party.")
        players.add(p)
        p.minigameAttributes.dungeoneeringAttributes.party =
            ownerPlayer.minigameAttributes.dungeoneeringAttributes.party
        p.packetSender.sendTabInterface(GameSettings.QUESTS_TAB, Dungeoneering.PARTY_INTERFACE)
        p.packetSender.sendDungeoneeringTabIcon(true)
        p.packetSender.sendTab(GameSettings.QUESTS_TAB)
        refreshInterface()
    }

    fun remove(p: Player, resetTab: Boolean, fromParty: Boolean) {
        if (fromParty) {
            players.remove(p)
            if (resetTab) {
                p.packetSender.sendTabInterface(GameSettings.QUESTS_TAB, if (p.isKillsTrackerOpen) 55000 else 639)
                p.packetSender.sendDungeoneeringTabIcon(false)
                p.packetSender.sendTab(GameSettings.QUESTS_TAB)
            } else {
                p.packetSender.sendTabInterface(GameSettings.QUESTS_TAB, Dungeoneering.FORM_PARTY_INTERFACE)
                p.packetSender.sendDungeoneeringTabIcon(true)
                p.packetSender.sendTab(GameSettings.QUESTS_TAB)
            }
        }
        p.packetSender.sendInterfaceRemoval()
        if (p === ownerPlayer) {
            for (member in players) {
                if (member != null && member.minigameAttributes.dungeoneeringAttributes.party != null && member.minigameAttributes.dungeoneeringAttributes.party === this) {
                    if (member === ownerPlayer) continue
                    if (fromParty) {
                        member.packetSender.sendMessage("Your party has been deleted by the party's leader.")
                        remove(member, false, true)
                        member.isInDung = false
                    } else {
                        remove(member, false, false)
                        member.isInDung = false
                    }
                }
            }
            if (hasEnteredDungeon) {
                for (npc in p.minigameAttributes.dungeoneeringAttributes.party.npcs) {
                    if (npc != null && npc.position.z == p.position.z) World.deregister(npc)
                }
                for (groundItem in p.minigameAttributes.dungeoneeringAttributes.party.groundItems) {
                    if (groundItem != null) GroundItemManager.remove(groundItem, true)
                }
            }
        } else {
            if (fromParty) {
                sendMessage(p.username + " has left the party.")
                if (hasEnteredDungeon) {
                    if (p.inventory.contains(Dungeoneering.DUNGEONEERING_GATESTONE_ID)) {
                        p.inventory.delete(Dungeoneering.DUNGEONEERING_GATESTONE_ID, 1)
                        getOwner()!!.inventory.add(Dungeoneering.DUNGEONEERING_GATESTONE_ID, 1)
                    }
                }
            }
        }
        if (hasEnteredDungeon) {
            p.equipment.resetItems().refreshItems()
            p.inventory.resetItems().refreshItems()
            p.restart()
            p.updateFlag.flag(Flag.APPEARANCE)
            p.moveTo(Position(3450, 3715))
            val damage = p.minigameAttributes.dungeoneeringAttributes.damageDealt
            var deaths = p.minigameAttributes.dungeoneeringAttributes.deaths
            var exp = damage / 100 * p.skillManager.getCurrentLevel(Skill.DUNGEONEERING) * complexity
            if (killedBoss) {
                exp = damage / 90 * p.skillManager.getCurrentLevel(Skill.DUNGEONEERING) * (complexity + 2)
            }
            if (p.skillManager.getCurrentLevel(Skill.DUNGEONEERING) < 15) {
                exp *= 25
            }
            if (deaths > 1) {
                exp *= (1 - 0.05 * deaths).toInt()
            }
            val tokens = exp / 10
            //World.sendMessage("YOU'VE DONE "+damage+" DMG");
            if (exp > 0 && tokens > 0) {
                /* if(player_members.size() == 1) {
					exp = (int) (exp * 0.7);
					tokens = (int) (tokens * 0.7);
				} */
                if (damage > 2000 || p.skillManager.getCurrentLevel(Skill.DUNGEONEERING) < 15) {
                    var count = exp * 2
                    p.packetSender.sendMessage(
                        "<img=10> <col=660000>You've recieved " + Misc.format(count) + " floor experience, and " + Misc.format(
                            tokens
                        ) + " tokens."
                    )
                    if (p.minutesBonusExp != -1) {
                        count *= 1.3.toInt()
                        p.packetSender.sendMessage(
                            "<col=660000>Your bonus EXP INCREASED the amount to " + Misc.format(
                                count
                            ) + "."
                        )
                    }
                    if (Misc.isWeekend()) {
                        count *= 2
                        p.packetSender.sendMessage(
                            "<col=660000>The bonus EXP weekend DOUBLED the amount to " + Misc.format(
                                count
                            ) + "."
                        )
                    }
                    p.packetSender.sendMessage(
                        "<col=660000>You " + (if (killedBoss) "killed the boss" else "didn't kill the boss") + ", and dealt " + Misc.format(
                            damage
                        ) + " damage."
                    )
                    p.skillManager.addExperience(Skill.DUNGEONEERING, exp)
                    p.pointsHandler.setDungeoneeringTokens(tokens, true)
                } else {
                    p.packetSender.sendMessage("You must do more damage to recieve experience and tokens.")
                }
                //p.getPacketSender().sendMessage("DMG: "+damage);
                PlayerPanel.refreshPanel(p)
            }
            if (p === ownerPlayer) {
                killedBoss = false
                hasEnteredDungeon = killedBoss
                deaths = 0
                kills = deaths
                gatestonePosition = null
            }
        }
        if (fromParty) {
            p.minigameAttributes.dungeoneeringAttributes.party = null
            refreshInterface()
        }
        p.packetSender.sendInterfaceRemoval()
    }

    fun refreshInterface() {
        for (member in players) {
            if (member != null) {
                for (s in 26236..26239) member.packetSender.sendString(s, "")
                member.packetSender.sendString(26235, ownerPlayer.username + "'s Party")
                member.packetSender.sendString(
                    26240, if (dungeoneeringFloor == null) "-" else "" + (dungeoneeringFloor!!.ordinal + 1)
                )
                member.packetSender.sendString(26241, if (complexity == -1) "-" else "" + complexity + "")
                for (i in players.indices) {
                    val p = players[i]
                    if (p != null) {
                        if (p === getOwner()) continue
                        member.packetSender.sendString(26235 + i, p.username)
                    }
                }
            }
        }
    }

    fun sendMessage(message: String) {
        for (member in players) {
            member?.packetSender?.sendMessage("<img=10> <col=660000>$message")
        }
    }

    fun sendFrame(frame: Int, string: String?) {
        for (member in players) {
            member?.packetSender?.sendString(frame, string)
        }
    }

    fun getOwner(): Player {
        return ownerPlayer
    }

    fun hasEnteredDungeon(): Boolean {
        return hasEnteredDungeon
    }

    fun enteredDungeon(hasEnteredDungeon: Boolean) {
        this.hasEnteredDungeon = hasEnteredDungeon
    }

    fun setKilledBoss(killedBoss: Boolean) {
        this.killedBoss = killedBoss
    }

    companion object {
        @JvmStatic
        fun create(p: Player) {
            if (p.location !== Locations.Location.DUNGEONEERING) {
                p.packetSender.sendMessage("You must be in Daemonheim to create a party.")
                return
            }
            if (p.minigameAttributes.dungeoneeringAttributes.party != null) {
                p.packetSender.sendMessage("You are already in a Dungeoneering party.")
                return
            }
            if (p.minigameAttributes.dungeoneeringAttributes.party == null) p.minigameAttributes.dungeoneeringAttributes.party =
                DungeoneeringParty(p)
            p.minigameAttributes.dungeoneeringAttributes.party.dungeoneeringFloor = DungeoneeringFloor.FIRST_FLOOR
            p.minigameAttributes.dungeoneeringAttributes.party.complexity = 1
            p.packetSender.sendMessage("<img=10> <col=660000>You've created a Dungeoneering party. Perhaps you should invite a few players?")
            p.minigameAttributes.dungeoneeringAttributes.party.refreshInterface()
            p.packetSender.sendTabInterface(GameSettings.QUESTS_TAB, Dungeoneering.PARTY_INTERFACE)
            p.packetSender.sendDungeoneeringTabIcon(true)
            p.packetSender.sendTab(GameSettings.QUESTS_TAB).sendInterfaceRemoval()
        }
    }
}