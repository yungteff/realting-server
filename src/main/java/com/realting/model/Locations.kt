package com.realting.model

import com.realting.GameSettings
import com.realting.model.RegionInstance.RegionInstanceType
import com.realting.model.entity.Entity
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import com.realting.world.World
import com.realting.world.content.PlayerLogs
import com.realting.world.content.PlayerPunishment.Jail
import com.realting.world.content.Zulrah
import com.realting.world.content.combat.CombatFactory.Companion.combatLevelDifference
import com.realting.world.content.combat.pvp.BountyHunter
import com.realting.world.content.combat.strategy.impl.bosses.Scorpia.Companion.killedBaby
import com.realting.world.content.dialogue.DialogueManager
import com.realting.world.content.minigames.Barrows.killBarrowsNpc
import com.realting.world.content.minigames.FightCave.handleJadDeath
import com.realting.world.content.minigames.FightCave.leaveCave
import com.realting.world.content.minigames.FightPit.getState
import com.realting.world.content.minigames.FightPit.inFightPits
import com.realting.world.content.minigames.FightPit.removePlayer
import com.realting.world.content.minigames.FightPit.updateGame
import com.realting.world.content.minigames.FightPit.updateWaitingRoom
import com.realting.world.content.minigames.Graveyard
import com.realting.world.content.minigames.Graveyard.handleDeath
import com.realting.world.content.minigames.Nomad.endFight
import com.realting.world.content.minigames.PestControl
import com.realting.world.content.minigames.RecipeForDisaster.handleNPCDeath
import com.realting.world.content.minigames.TheSix
import com.realting.world.content.minigames.TheSix.allKilled
import com.realting.world.content.minigames.TheSix.spawn
import com.realting.world.content.player.skill.dungeoneering.Dungeoneering.doingDungeoneering
import com.realting.world.content.player.skill.dungeoneering.Dungeoneering.handleNpcDeath
import com.realting.world.content.player.skill.dungeoneering.Dungeoneering.handlePlayerDeath
import com.realting.world.content.player.skill.dungeoneering.Dungeoneering.leave

object Locations {
    @JvmStatic
    fun login(player: Player) {
        player.location = Location.getLocation(player)
        player.location.login(player)
        player.location.enter(player)
    }

    @JvmStatic
    fun logout(player: Player) {
        player.location.logout(player)
        if (player.regionInstance != null) player.regionInstance.destruct()
        if (player.location !== Location.GODWARS_DUNGEON) {
            player.location.leave(player)
        }
    }

    @JvmField
    var PLAYERS_IN_WILD = 0

    @JvmField
    var PLAYERS_IN_DUEL_ARENA = 0

    @JvmStatic
    fun inMulti(gc: CharacterEntity): Boolean {
        val x = gc.entityPosition.x
        val y = gc.entityPosition.y
        if (gc.location === Location.WILDERNESS) {
            if (x >= 3250 && x <= 3302 && y >= 3905 && y <= 3925 || x >= 3020 && x <= 3055 && y >= 3684 && y <= 3711 || x >= 3150 && x <= 3195 && y >= 2958 && y <= 3003 || x >= 3645 && x <= 3715 && y >= 3454 && y <= 3550 || x >= 3150 && x <= 3199 && y >= 3796 && y <= 3869 || x >= 2994 && x <= 3041 && y >= 3733 && y <= 3790) return true
            if (x >= 3336 && x <= 3371 && y >= 3792 && y <= 3819) //zulrah pinnensula
                return true
            //wyrm multi handler
            if (x >= 3052 && x <= 3083 && y >= 3929 && y <= 3963 || x >= 3294 && x <= 3315 && y >= 3919 && y <= 3961 || x >= 3214 && x <= 3253 && y >= 3594 && y <= 3639 || x >= 3266 && x <= 3306 && y >= 3868 && y <= 3903 || x >= 3169 && x <= 3221 && y >= 3651 && y <= 3700 || x >= 3152 && x <= 3190 && y >= 3776 && y <= 3817) return true
            //z x1: 3336, x2: 3371, y1: 3819, y2: 3792
        } else {
            // Crash site cavern (demonic gorillas)
            if (x >= 2048 && x <= 2175 && y >= 5632 && y <= 5695) {
                return true
            }
        }
        return gc.location.multi
    }

    fun process(gc: CharacterEntity) {
        val newLocation = Location.getLocation(gc)
        if (gc.location === newLocation) {
            if (gc.isPlayer) {
                val player = gc as Player
                gc.getLocation().process(player)
                if (inMulti(player)) {
                    if (player.multiIcon != 1) player.packetSender.sendMultiIcon(1)
                } else if (player.multiIcon == 1) player.packetSender.sendMultiIcon(0)
            }
        } else {
            val prev = gc.location
            if (gc.isPlayer) {
                val player = gc as Player
                if (player.multiIcon > 0) player.packetSender.sendMultiIcon(0)
                if (player.walkableInterfaceId > 0 && player.walkableInterfaceId != 37400 && player.walkableInterfaceId != 50000) player.packetSender.sendWalkableInterface(
                    -1
                )
                if (player.playerInteractingOption != PlayerInteractingOption.NONE) player.packetSender.sendInteractionOption(
                    "null", 2, true
                )
            }
            gc.location = newLocation
            if (gc.isPlayer) {
                prev.leave(gc as Player)
                gc.getLocation().enter(gc)
            }
        }
    }

    fun goodDistance(
        objectX: Int, objectY: Int, playerX: Int, playerY: Int, distance: Int
    ): Boolean {
        if (playerX == objectX && playerY == objectY) return true
        for (i in 0..distance) {
            for (j in 0..distance) {
                if (objectX + i == playerX && (objectY + j == playerY || objectY - j == playerY || objectY == playerY)) {
                    return true
                } else if (objectX - i == playerX && (objectY + j == playerY || objectY - j == playerY || objectY == playerY)) {
                    return true
                } else if (objectX == playerX && (objectY + j == playerY || objectY - j == playerY || objectY == playerY)) {
                    return true
                }
            }
        }
        return false
    }

    @JvmStatic
    fun goodDistance(pos1: Position, pos2: Position, distanceReq: Int): Boolean {
        return if (pos1.z != pos2.z) false else goodDistance(
            pos1.x, pos1.y, pos2.x, pos2.y, distanceReq
        )
    }

    fun distanceTo(
        position: Position, destination: Position, size: Int
    ): Int {
        val x = position.x
        val y = position.y
        val otherX = destination.x
        val otherY = destination.y
        val distX: Int
        val distY: Int
        distX = if (x < otherX) otherX - x else if (x > otherX + size) x - (otherX + size) else 0
        distY = if (y < otherY) otherY - y else if (y > otherY + size) y - (otherY + size) else 0
        if (distX == distY) return distX + 1
        return if (distX > distY) distX else distY
    }

    enum class Location(
        val x: IntArray?,
        val y: IntArray?,
        val multi: Boolean,
        val isSummoningAllowed: Boolean,
        val isFollowingAllowed: Boolean,
        val isCannonAllowed: Boolean,
        val isFiremakingAllowed: Boolean,
        val isAidingAllowed: Boolean
    ) {
        //Location(int[] x, int[] y, boolean multi, boolean summonAllowed, boolean followingAllowed, boolean cannonAllowed, boolean firemakingAllowed, boolean aidingAllowed) {
        MAGEBANK_SAFE(intArrayOf(2525, 2550), intArrayOf(4707, 4727), true, true, true, false, false, false), ZULRAH(
            intArrayOf(3395, 3453), intArrayOf(2751, 2785), false, false, true, false, false, false
        ) {
            override fun leave(player: Player) {
                if ((player.regionInstance != null) && (player.regionInstance.type == RegionInstanceType.ZULRAH)) {
                    player.regionInstance.destruct()
                }
                player.packetSender.sendCameraNeutrality()
                player.minigameAttributes.zulrahAttributes.setRedFormDamage(0, false)
            }

            override fun enter(player: Player) {
                Zulrah.enter(player)
                player.minigameAttributes.zulrahAttributes.setRedFormDamage(0, false)
            }

            override fun onDeath(player: Player) {
                if ((player.regionInstance != null) && (player.regionInstance.type == RegionInstanceType.ZULRAH)) {
                    player.regionInstance.destruct()
                }
                player.minigameAttributes.zulrahAttributes.setRedFormDamage(0, false)
            }

            override fun handleKilledNPC(killer: Player, npc: NPC): Boolean {
                killer.minigameAttributes.zulrahAttributes.setRedFormDamage(0, false)
                return false
            }

            override fun logout(player: Player) {
                if (player.regionInstance != null && player.regionInstance.type == RegionInstanceType.ZULRAH) {
                    player.regionInstance.destruct()
                }
                player.minigameAttributes.zulrahAttributes.setRedFormDamage(0, false)
                player.moveTo(GameSettings.DEFAULT_POSITION)
            }

            override fun login(player: Player) {
                if (player.regionInstance != null && player.regionInstance.type == RegionInstanceType.ZULRAH) {
                    player.regionInstance.destruct()
                }
                player.minigameAttributes.zulrahAttributes.setRedFormDamage(0, false)
                player.moveTo(GameSettings.DEFAULT_POSITION)
            }
        },
        DOOM(intArrayOf(2302, 2369), intArrayOf(5182, 5250), true, true, true, false, false, false), XMASEVENT2016(
            intArrayOf(2747, 2821), intArrayOf(3707, 3877), false, true, true, false, true, true
        ) {
            override fun process(player: Player) {
                if (player.walkableInterfaceId != 11877) {
                    player.packetSender.sendWalkableInterface(11877)
                }
            }
        },
        DUNGEONEERING(
            intArrayOf(3433, 3459, 2421, 2499),
            intArrayOf(3694, 3729, 4915, 4990),
            true,
            false,
            true,
            false,
            true,
            false
        ) {
            override fun login(player: Player) {
                player.packetSender.sendDungeoneeringTabIcon(true).sendTabInterface(GameSettings.QUESTS_TAB, 27224)
                    .sendTab(GameSettings.QUESTS_TAB)
            }

            override fun leave(player: Player) {
                leave(player, true, true)
            }

            override fun enter(player: Player) {
                player.packetSender.sendDungeoneeringTabIcon(true).sendTabInterface(GameSettings.QUESTS_TAB, 27224)
                    .sendTab(GameSettings.QUESTS_TAB)
                if (player.isInDung == false) {
                    DialogueManager.start(player, 104)
                }
            }

            override fun onDeath(player: Player) {
                if (doingDungeoneering(player)) {
                    handlePlayerDeath(player)
                }
            }

            override fun handleKilledNPC(killer: Player, npc: NPC): Boolean {
                if (doingDungeoneering(killer)) {
                    handleNpcDeath(killer, npc)
                    return true
                }
                return false
            }

            override fun process(player: Player) {
                if (doingDungeoneering(player)) {
                    if (player.walkableInterfaceId != 37500) {
                        player.packetSender.sendWalkableInterface(37500)
                    }
                } else if (player.walkableInterfaceId == 37500) {
                    player.packetSender.sendWalkableInterface(-1)
                }
            }
        },  //Location(int[] x, int[] y, boolean multi, boolean summonAllowed, boolean followingAllowed, boolean cannonAllowed, boolean firemakingAllowed, boolean aidingAllowed) {
        ZULRAH_WAITING(intArrayOf(3401, 3414), intArrayOf(2789, 2801), false, true, true, false, true, true) {
            override fun enter(player: Player) {
                if (player.skillManager.getCurrentLevel(Skill.CONSTITUTION) < player.skillManager.getMaxLevel(Skill.CONSTITUTION)) {
                    player.skillManager.setCurrentLevel(
                        Skill.CONSTITUTION, player.skillManager.getMaxLevel(Skill.CONSTITUTION)
                    )
                    player.packetSender.sendMessage("The astounding power of the old pillars heals you.")
                }
                if (player.skillManager.getCurrentLevel(Skill.PRAYER) < player.skillManager.getMaxLevel(Skill.PRAYER)) {
                    player.skillManager.setCurrentLevel(Skill.PRAYER, player.skillManager.getMaxLevel(Skill.PRAYER))
                    player.packetSender.sendMessage("The mystique aura of the pillars restores your prayer.")
                }
            }

            override fun leave(player: Player) {
                if (player.skillManager.getCurrentLevel(Skill.CONSTITUTION) < player.skillManager.getMaxLevel(Skill.CONSTITUTION)) {
                    player.skillManager.setCurrentLevel(
                        Skill.CONSTITUTION, player.skillManager.getMaxLevel(Skill.CONSTITUTION)
                    )
                    player.packetSender.sendMessage("The astounding power of the old pillars heals you.")
                }
                if (player.skillManager.getCurrentLevel(Skill.PRAYER) < player.skillManager.getMaxLevel(Skill.PRAYER)) {
                    player.skillManager.setCurrentLevel(Skill.PRAYER, player.skillManager.getMaxLevel(Skill.PRAYER))
                    player.packetSender.sendMessage("The mystique aura of the pillars restores your prayer.")
                }
            }
        },
        JAIL(intArrayOf(2505, 2535), intArrayOf(9310, 9330), false, false, false, false, false, false) {
            override fun canTeleport(player: Player): Boolean {
                return if (player.rights.isStaff) {
                    player.packetSender.sendMessage("Staff can leave at any time.")
                    true
                } else {
                    player.packetSender.sendMessage("That'd be convenient, wouldn't it?")
                    false
                }
            } /*@Override
			public void process(Player player) {
				//player.getPacketSender().sendInteractionOption(null, 4, false);
				if(player.getTrading().inTrade()){
					player.getTrading().declineTrade(true);
					player.getPacketSender().sendMessage("You can't trade in jail!");
				}
			}*/
        },
        MEMBER_ZONE(intArrayOf(3415, 3435), intArrayOf(2900, 2926), false, true, true, false, false, true), HOME_BANK(
            intArrayOf(3661, 3674), intArrayOf(2975, 2985), false, true, true, false, true, true
        ) {
            override fun enter(player: Player) {
                if (player.skillManager.getCurrentLevel(Skill.CONSTITUTION) < player.skillManager.getMaxLevel(Skill.CONSTITUTION)) {
                    player.skillManager.setCurrentLevel(
                        Skill.CONSTITUTION, player.skillManager.getMaxLevel(Skill.CONSTITUTION)
                    )
                    player.packetSender.sendMessage("As you enter the home bank, your health regenerates to full.")
                }
                if (player.skillManager.getCurrentLevel(Skill.PRAYER) < player.skillManager.getMaxLevel(Skill.PRAYER)) {
                    player.skillManager.setCurrentLevel(Skill.PRAYER, player.skillManager.getMaxLevel(Skill.PRAYER))
                    player.packetSender.sendMessage("As you enter the home bank, the gods restore your prayer.")
                }
            }

            override fun leave(player: Player) {
                if (player.skillManager.getCurrentLevel(Skill.CONSTITUTION) < player.skillManager.getMaxLevel(Skill.CONSTITUTION)) {
                    player.skillManager.setCurrentLevel(
                        Skill.CONSTITUTION, player.skillManager.getMaxLevel(Skill.CONSTITUTION)
                    )
                    player.packetSender.sendMessage("As you leave the home bank, your health regenerates to full.")
                }
                if (player.skillManager.getCurrentLevel(Skill.PRAYER) < player.skillManager.getMaxLevel(Skill.PRAYER)) {
                    player.skillManager.setCurrentLevel(Skill.PRAYER, player.skillManager.getMaxLevel(Skill.PRAYER))
                    player.packetSender.sendMessage("As you leave the home bank, the gods restore your prayer.")
                }
            }
        },
        NEW_MEMBER_ZONE(intArrayOf(2792, 2877), intArrayOf(3319, 3396), false, true, true, false, true, true) {
            override fun process(player: Player) {
                if (!player.rights.isMember && !player.newPlayer()) {
                    player.packetSender.sendMessage("This area is for Members only.")
                    player.moveTo(GameSettings.HOME_CORDS)
                }
            }

            override fun enter(player: Player) {
                if (player.skillManager.getCurrentLevel(Skill.CONSTITUTION) < player.skillManager.getMaxLevel(Skill.CONSTITUTION)) {
                    player.skillManager.setCurrentLevel(
                        Skill.CONSTITUTION, player.skillManager.getMaxLevel(Skill.CONSTITUTION)
                    )
                    player.packetSender.sendMessage("As you enter the Member Zone, your health regenerates to full.")
                }
                if (player.skillManager.getCurrentLevel(Skill.PRAYER) < player.skillManager.getMaxLevel(Skill.PRAYER)) {
                    player.skillManager.setCurrentLevel(Skill.PRAYER, player.skillManager.getMaxLevel(Skill.PRAYER))
                    player.packetSender.sendMessage("As you enter the Member Zone, the gods restore your prayer.")
                }
            }
        },
        TRIO_ZONE(intArrayOf(3008, 3039), intArrayOf(5216, 5247), false, false, false, false, false, false), VARROCK(
            intArrayOf(3167, 3272), intArrayOf(3263, 3504), false, true, true, true, true, true
        ),  /*BANK(new int[]{3090, 3099, 3089, 3090, 3248, 3258, 3179, 3191, 2944, 2948, 2942, 2948, 2944, 2950, 3008, 3019, 3017, 3022, 3203, 3213, 3212, 3215, 3215, 3220, 3220, 3227, 3227, 3230, 3226, 3228, 3227, 3229}, new int[]{3487, 3500, 3492, 3498, 3413, 3428, 3432, 3448, 3365, 3374, 3367, 3374, 3365, 3370, 3352, 3359, 3352, 3357, 3200, 3237, 3200, 3235, 3202, 3235, 3202, 3229, 3208, 3226, 3230, 3211, 3208, 3226}, false, true, true, false, false, true) {
		},*/
        EDGEVILLE(intArrayOf(3073, 3134), intArrayOf(3457, 3518), false, false, true, false, false, true), LUMBRIDGE(
            intArrayOf(3175, 3238), intArrayOf(3179, 3302), false, true, true, true, true, true
        ),
        KING_BLACK_DRAGON(intArrayOf(2251, 2292), intArrayOf(4673, 4717), true, true, true, true, true, true), SCORPIA(
            intArrayOf(2845, 2864), intArrayOf(9621, 9649), true, true, true, true, true, true
        ) {
            override fun handleKilledNPC(killer: Player, npc: NPC): Boolean {
                if (npc.id == 109) {
                    killedBaby()
                    return true
                }
                return false
            }
        },
        KRAKEN(intArrayOf(3672, 3690), intArrayOf(9875, 9899), true, true, true, true, true, true) {
            override fun leave(player: Player) {
                if (player.regionInstance != null && player.regionInstance.type == RegionInstanceType.KRAKEN) {
                    player.regionInstance.destruct()
                }
                player.packetSender.sendCameraNeutrality()
            }
        },
        SLASH_BASH(intArrayOf(2504, 2561), intArrayOf(9401, 9473), true, true, true, true, true, true), BANDOS_AVATAR(
            intArrayOf(2340, 2396), intArrayOf(4929, 4985), true, true, true, true, true, true
        ),
        KALPHITE_QUEEN(intArrayOf(3464, 3500), intArrayOf(9478, 9523), true, true, true, true, true, true), PHOENIX(
            intArrayOf(2824, 2862), intArrayOf(9545, 9594), true, true, true, true, true, true
        ),  //BANDIT_CAMP(new int[]{3020, 3150, 3055, 3195}, new int[]{3684, 3711, 2958, 3003}, true, true, true, true, true, true) {

        //},
        ROCK_CRABS(
            intArrayOf(2689, 2727), intArrayOf(3691, 3730), true, true, true, true, true, true
        ),
        ARMOURED_ZOMBIES(
            intArrayOf(3077, 3132), intArrayOf(9657, 9680), true, true, true, true, true, true
        ),
        CORPOREAL_BEAST(intArrayOf(2879, 2962), intArrayOf(4368, 4413), true, true, true, false, true, true) {
            override fun process(player: Player) {
                val x1 = 2889
                val x2 = 2908
                val y1 = 4381
                val y2 = 4403
                val currentx = player.entityPosition.x
                val currenty = player.entityPosition.y
                val safe = currentx >= x1 && currentx <= x2 && currenty >= y1 && currenty <= y2
                if (safe) {
                    //player.getPacketSender().sendMessage("You are safe");
                    player.packetSender.sendWalkableInterface(-1) //.sendMessage("sendwalkint-1"); 
                    /*player.setWalkableInterfaceId(-1); 
					player.getPacketSender().sendMessage("setwalkint-1");
					player.getPacketSender().sendInterfaceRemoval().sendMessage("sendintremoval");
					player.getPacketSender().sendInterfaceReset().sendMessage("sendintreset");
					*/
                } else {
                    //player.getPacketSender().sendMessage("Get out of the gas!");
                    player.dealDamage(Hit(null, Misc.getRandom(15) * 10, Hitmask.DARK_PURPLE, CombatIcon.CANNON))
                    if (player.walkableInterfaceId != 16152) {
                        player.packetSender.sendWalkableInterface(16152)
                    }
                    //player.setWalkableInterfaceId(16152);
                }
            }
        },
        DAGANNOTH_DUNGEON(
            intArrayOf(2886, 2938), intArrayOf(4431, 4477), true, true, true, false, true, true
        ),
        WILDERNESS(
            intArrayOf(2940, 3392, 2986, 3012, 3653, 3720, 3650, 3653, 3150, 3199, 2994, 3041),
            intArrayOf(3523, 3968, 10338, 10366, 3441, 3538, 3457, 3472, 3796, 3869, 3733, 3790),
            false,
            true,
            true,
            true,
            true,
            true
        ) {
            override fun process(player: Player) {
                val x = player.entityPosition.x
                val y = player.entityPosition.y
                val ghostTown = x >= 3650 && y <= 3538
                //boolean magebank = (x >= 3090 && x <= 3092 && y >= 3955 && y <= 3958);
                //System.out.println("magebank "+magebank+", "+player.getPosition());
                if (player.isFlying) {
                    player.packetSender.sendMessage("You cannot fly in the Wilderness.")
                    player.isFlying = false
                    player.newStance()
                }
                if (player.isGhostWalking) {
                    player.packetSender.sendMessage("You cannot ghost walk in the Wilderness.")
                    player.isGhostWalking = false
                    player.newStance()
                }
                /*boolean banditCampA = x >= 3020 && x <= 3150 && y >= 3684 && y <= 3711;
				boolean banditCampB = x >= 3055 && x <= 3195 && y >= 2958 && y <= 3003;
				if(banditCampA || banditCampB) {
				}*/if (ghostTown) {
                    player.wildernessLevel = 60
                } else {
                    player.wildernessLevel = ((if (y > 6400) y - 6400 else y) - 3520) / 8 + 1
                }
                player.packetSender.sendString(42023, "" + player.wildernessLevel)
                //player.getPacketSender().sendString(25355, "Levels: "+CombatFactory.getLevelDifference(player, false) +" - "+CombatFactory.getLevelDifference(player, true));
                BountyHunter.process(player)
            }

            override fun leave(player: Player) {
                if (player.location !== this) {
                    player.packetSender.sendString(19000, "Combat level: " + player.skillManager.combatLevel)
                    player.updateFlag.flag(Flag.APPEARANCE)
                }
                PLAYERS_IN_WILD--
            }

            override fun enter(player: Player) {
                player.packetSender.sendInteractionOption("Attack", 2, true)
                player.packetSender.sendWalkableInterface(42020)
                player.packetSender.sendString(19000, "Combat level: " + player.skillManager.combatLevel)
                player.updateFlag.flag(Flag.APPEARANCE)
                PLAYERS_IN_WILD++
            }

            override fun canTeleport(player: Player): Boolean {
                if (Jail.isJailed(player)) {
                    player.packetSender.sendMessage("That'd be convenient.")
                    return false
                }
                if (player.wildernessLevel > 20) {
                    if (player.rights == PlayerRights.MODERATOR || player.rights == PlayerRights.ADMINISTRATOR || player.rights == PlayerRights.OWNER || player.rights == PlayerRights.DEVELOPER) {
                        player.packetSender.sendMessage("@red@You've teleported out of deep Wilderness, logs have been written.")
                        PlayerLogs.log(
                            player.username,
                            " teleported out of level " + player.wildernessLevel + " wildy. Were in combat? " + player.combatBuilder.isBeingAttacked
                        )
                        return true
                    }
                    player.packetSender.sendMessage("Teleport spells are blocked in this level of Wilderness.")
                    player.packetSender.sendMessage("You must be below level 20 of Wilderness to use teleportation spells.")
                    return false
                }
                return true
            }

            override fun login(player: Player) {
                player.performGraphic(Graphic(2000, 8))
            }

            override fun canAttack(player: Player, target: Player): Boolean {
                val combatDifference =
                    combatLevelDifference(player.skillManager.combatLevel, target.skillManager.combatLevel)
                if (combatDifference > player.wildernessLevel + 5 || combatDifference > target.wildernessLevel + 5) {
                    player.packetSender.sendMessage("Your combat level difference is too great to attack that player here.")
                    player.movementQueue.reset()
                    return false
                }
                if (target.location !== WILDERNESS) {
                    player.packetSender.sendMessage("That player cannot be attacked, because they are not in the Wilderness.")
                    player.movementQueue.reset()
                    return false
                }
                if (Jail.isJailed(player)) {
                    player.packetSender.sendMessage("You cannot do that right now.")
                    return false
                }
                if (Jail.isJailed(target)) {
                    player.packetSender.sendMessage("That player cannot be attacked right now.")
                    return false
                }
                /*if(Misc.getMinutesPlayed(player) < 20) {
					player.getPacketSender().sendMessage("You must have played for at least 20 minutes in order to attack someone.");
					return false;
				}
				if(Misc.getMinutesPlayed(target) < 20) {
					player.getPacketSender().sendMessage("This player is a new player and can therefore not be attacked yet.");
					return false;
				}*/return true
            }
        },
        BARROWS(
            intArrayOf(3520, 3598, 3543, 3584, 3543, 3560),
            intArrayOf(9653, 9750, 3265, 3314, 9685, 9702),
            false,
            true,
            true,
            true,
            true,
            true
        ) {
            override fun process(player: Player) {
                if (player.walkableInterfaceId != 37200) player.packetSender.sendWalkableInterface(37200)
            }

            override fun canTeleport(player: Player): Boolean {
                return true
            }

            override fun logout(player: Player) {}
            override fun handleKilledNPC(killer: Player, npc: NPC): Boolean {
                killBarrowsNpc(killer, npc, true)
                return true
            }
        },
        THE_SIX(intArrayOf(2376, 2395), intArrayOf(4711, 4731), true, true, true, true, true, true) {
            override fun process(player: Player) {
                if (player.walkableInterfaceId != 37200) player.packetSender.sendWalkableInterface(37200)
            }

            override fun canTeleport(player: Player): Boolean {
                return true
            }

            override fun leave(player: Player) {
                if (!player.doingClanBarrows()) {
                    if (player.regionInstance != null) {
                        player.regionInstance.destruct()
                    }
                    TheSix.leave(player, false)
                } else if (player.currentClanChat != null && player.currentClanChat.doingClanBarrows()) {
                    TheSix.leave(player, false)
                }
            }

            override fun handleKilledNPC(killer: Player, npc: NPC): Boolean {
                var respawn = false
                if (!killer.doingClanBarrows()) {
                    killBarrowsNpc(killer, npc, true)
                    if (allKilled(killer)) {
                        respawn = true
                    }
                } else if (killer.currentClanChat != null && killer.currentClanChat.doingClanBarrows()) {
                    for (p in killer.currentClanChat.members) {
                        if (p == null || !p.doingClanBarrows()) {
                            continue
                        }
                        killBarrowsNpc(p, npc, true)
                        if (allKilled(p)) {
                            respawn = true
                        }
                    }
                }
                if (respawn) {
                    spawn(killer, killer.doingClanBarrows())
                }
                return true
            }
        },
        INVADING_GAME(intArrayOf(2216, 2223), intArrayOf(4936, 4943), true, true, true, false, true, true) {
            override fun process(player: Player) {
                if (player.walkableInterfaceId != 21005) player.packetSender.sendWalkableInterface(21005)
            }
        },
        PEST_CONTROL_GAME(intArrayOf(2624, 2690), intArrayOf(2550, 2619), true, true, true, false, true, true) {
            override fun process(player: Player) {
                if (player.walkableInterfaceId != 21100) player.packetSender.sendWalkableInterface(21100)
            }

            override fun canTeleport(player: Player): Boolean {
                player.packetSender.sendMessage("Teleport spells are blocked on this island. Wait for the game to finish!")
                return false
            }

            override fun leave(player: Player) {
                PestControl.leave(player, true)
            }

            override fun logout(player: Player) {
                PestControl.leave(player, true)
            }

            override fun handleKilledNPC(killer: Player, n: NPC): Boolean {
                return true
            }

            override fun onDeath(player: Player) {
                player.moveTo(Position(2657, 2612, 0))
            }
        },
        PEST_CONTROL_BOAT(intArrayOf(2660, 2663), intArrayOf(2638, 2643), false, false, false, false, false, true) {
            override fun process(player: Player) {
                if (player.walkableInterfaceId != 21005) player.packetSender.sendWalkableInterface(21005)
            }

            override fun canTeleport(player: Player): Boolean {
                player.packetSender.sendMessage("You must leave the boat before teleporting.")
                return false
            }

            override fun leave(player: Player) {
                if (player.location !== PEST_CONTROL_GAME) {
                    PestControl.leave(player, true)
                }
            }

            override fun logout(player: Player) {
                PestControl.leave(player, true)
            }
        },
        SOULWARS(intArrayOf(-1, -1), intArrayOf(-1, -1), true, true, true, false, true, true) {
            override fun process(player: Player) {}
            override fun canTeleport(player: Player): Boolean {
                player.packetSender.sendMessage("If you wish to leave, you must use the portal in your team's lobby.")
                return false
            }

            override fun logout(player: Player) {}
            override fun onDeath(player: Player) {}
            override fun handleKilledNPC(killer: Player, npc: NPC): Boolean {
                return false
            }
        },
        SOULWARS_WAIT(intArrayOf(-1, -1), intArrayOf(-1, -1), false, false, false, false, false, true) {
            override fun process(player: Player) {}
            override fun canTeleport(player: Player): Boolean {
                player.packetSender.sendMessage("You must leave the waiting room before being able to teleport.")
                return false
            }

            override fun logout(player: Player) {}
        },
        FIGHT_CAVES(intArrayOf(2360, 2445), intArrayOf(5045, 5125), true, true, false, false, false, false) {
            override fun process(player: Player) {}
            override fun canTeleport(player: Player): Boolean {
                player.packetSender.sendMessage("Teleport spells are blocked here. If you'd like to leave, use the north-east exit.")
                return false
            }

            override fun login(player: Player) {}
            override fun leave(player: Player) {
                player.combatBuilder.reset(true)
                if (player.regionInstance != null) {
                    player.regionInstance.destruct()
                }
                player.moveTo(Position(2439, 5169))
            }

            override fun onDeath(player: Player) {
                leaveCave(player, true)
            }

            override fun handleKilledNPC(killer: Player, npc: NPC): Boolean {
                handleJadDeath(killer, npc)
                return true
            }
        },
        GRAVEYARD(intArrayOf(3485, 3517), intArrayOf(3559, 3580), true, true, false, true, false, false) {
            override fun process(player: Player) {}
            override fun canTeleport(player: Player): Boolean {
                if (player.minigameAttributes.graveyardAttributes.hasEntered()) {
                    player.packetSender.sendInterfaceRemoval()
                        .sendMessage("A spell teleports you out of the graveyard.")
                    Graveyard.leave(player)
                    return false
                }
                return true
            }

            override fun logout(player: Player) {
                if (player.minigameAttributes.graveyardAttributes.hasEntered()) {
                    Graveyard.leave(player)
                }
            }

            override fun handleKilledNPC(killer: Player, npc: NPC): Boolean {
                return killer.minigameAttributes.graveyardAttributes.hasEntered() && handleDeath(killer, npc)
            }

            override fun onDeath(player: Player) {
                Graveyard.leave(player)
            }
        },
        FIGHT_PITS(intArrayOf(2370, 2425), intArrayOf(5133, 5167), true, true, true, false, false, true) {
            override fun process(player: Player) {
                if (inFightPits(player)) {
                    updateGame(player)
                    if (player.playerInteractingOption != PlayerInteractingOption.ATTACK) player.packetSender.sendInteractionOption(
                        "Attack", 2, true
                    )
                }
            }

            override fun canTeleport(player: Player): Boolean {
                player.packetSender.sendMessage("Teleport spells are blocked here. If you'd like to leave, use the northern exit.")
                return false
            }

            override fun logout(player: Player) {
                removePlayer(player, "leave game")
            }

            override fun leave(player: Player) {
                onDeath(player)
            }

            override fun onDeath(player: Player) {
                if (getState(player) != null) {
                    removePlayer(player, "death")
                }
            }

            override fun canAttack(player: Player, target: Player): Boolean {
                val state1 = getState(player)
                val state2 = getState(target)
                return state1 != null && state1 == "PLAYING" && state2 != null && state2 == "PLAYING"
            }
        },
        FIGHT_PITS_WAIT_ROOM(intArrayOf(2393, 2404), intArrayOf(5168, 5176), false, false, false, false, false, true) {
            override fun process(player: Player) {
                updateWaitingRoom(player)
            }

            override fun canTeleport(player: Player): Boolean {
                player.packetSender.sendMessage("Teleport spells are blocked here. If you'd like to leave, use the northern exit.")
                return false
            }

            override fun logout(player: Player) {
                removePlayer(player, "leave room")
            }

            override fun leave(player: Player) {
                if (player.location !== FIGHT_PITS) {
                    removePlayer(player, "leave room")
                }
            }
        },
        DUEL_ARENA(
            intArrayOf(3322, 3394, 3311, 3323, 3331, 3391),
            intArrayOf(3195, 3291, 3223, 3248, 3242, 3260),
            false,
            false,
            false,
            false,
            false,
            false
        ) {
            override fun process(player: Player) {
                if (player.walkableInterfaceId != 201) player.packetSender.sendWalkableInterface(201)
                if (player.dueling.duelingStatus == 0) {
                    if (player.playerInteractingOption != PlayerInteractingOption.CHALLENGE) player.packetSender.sendInteractionOption(
                        "Challenge", 2, false
                    )
                } else if (player.playerInteractingOption != PlayerInteractingOption.ATTACK) player.packetSender.sendInteractionOption(
                    "Attack", 2, true
                )
            }

            override fun enter(player: Player) {
                PLAYERS_IN_DUEL_ARENA++
                player.packetSender.sendMessage("<img=10> <col=996633>Warning! Do not stake items which you are not willing to lose.")
            }

            override fun canTeleport(player: Player): Boolean {
                if (player.dueling.duelingStatus == 5) {
                    player.packetSender.sendMessage("To forfiet a duel, run to the west and use the trapdoor.")
                    return false
                }
                return true
            }

            override fun logout(player: Player) {
                var dc = false
                if (player.dueling.inDuelScreen && player.dueling.duelingStatus != 5) {
                    player.dueling.declineDuel(player.dueling.duelingWith > 0)
                } else if (player.dueling.duelingStatus == 5) {
                    if (player.dueling.duelingWith > -1) {
                        val duelEnemy = World.getPlayers()[player.dueling.duelingWith]
                        if (duelEnemy != null) {
                            duelEnemy.dueling.duelVictory()
                        } else {
                            dc = true
                        }
                    }
                }
                player.moveTo(Position(3368, 3268))
                if (dc) {
                    World.getPlayers().remove(player)
                }
            }

            override fun leave(player: Player) {
                if (player.dueling.duelingStatus == 5) {
                    onDeath(player)
                }
                PLAYERS_IN_DUEL_ARENA--
            }

            override fun onDeath(player: Player) {
                if (player.dueling.duelingStatus == 5) {
                    if (player.dueling.duelingWith > -1) {
                        val duelEnemy = World.getPlayers()[player.dueling.duelingWith]
                        if (duelEnemy != null) {
                            duelEnemy.dueling.duelVictory()
                            duelEnemy.packetSender.sendMessage("You won the duel! Congratulations!")
                        }
                    }
                    PlayerLogs.log(player.username, "Has lost their duel.")
                    player.packetSender.sendMessage("You've lost the duel.")
                    player.dueling.arenaStats[1]++
                    player.dueling.reset()
                }
                player.moveTo(Position(3368 + Misc.getRandom(5), 3267 + Misc.getRandom(3)))
                player.dueling.reset()
            }

            override fun canAttack(player: Player, target: Player): Boolean {
                if (target.index != player.dueling.duelingWith) {
                    player.packetSender.sendMessage("That player is not your opponent!")
                    return false
                }
                if (player.dueling.timer != -1) {
                    player.packetSender.sendMessage("You cannot attack yet!")
                    return false
                }
                return player.dueling.duelingStatus == 5 && target.dueling.duelingStatus == 5
            }
        },
        GODWARS_DUNGEON(
            intArrayOf(2800, 2950, 2858, 2943), intArrayOf(5200, 5400, 5180, 5230), true, true, true, false, true, true
        ) {
            override fun process(player: Player) {
                if (player.entityPosition.x == 2842 && player.entityPosition.y == 5308 //ARMADYL
                    || player.entityPosition.x == 2876 && player.entityPosition.y == 5369 // BANDOS
                    || player.entityPosition.x == 2936 && player.entityPosition.y == 5331 // ZAMMY
                    || player.entityPosition.x == 2907 && player.entityPosition.y == 5272
                ) { //NORTH EAST, saradomin
                    player.moveTo(
                        Position(
                            player.entityPosition.x - 1, player.entityPosition.y - 1, player.entityPosition.z
                        )
                    )
                    player.movementQueue.reset()
                }
                if (player.entityPosition.x == 2842 && player.entityPosition.y == 5296 //ARMADYL
                    || player.entityPosition.x == 2876 && player.entityPosition.y == 5351 //BANDOS
                    || player.entityPosition.x == 2936 && player.entityPosition.y == 5318 //ZAMMY
                    || player.entityPosition.x == 2907 && player.entityPosition.y == 5258
                ) { // saradomin, SOUTH EAST
                    player.moveTo(
                        Position(
                            player.entityPosition.x - 1, player.entityPosition.y + 1, player.entityPosition.z
                        )
                    )
                    player.movementQueue.reset()
                }
                if (player.entityPosition.x == 2824 && player.entityPosition.y == 5296 //ARMADYL
                    || player.entityPosition.x == 2864 && player.entityPosition.y == 5351 //BANDOS
                    || player.entityPosition.x == 2918 && player.entityPosition.y == 5318 //ZAMMY
                    || player.entityPosition.x == 2895 && player.entityPosition.y == 5258
                ) { // saradomin, SOUTH WEST
                    player.moveTo(
                        Position(
                            player.entityPosition.x + 1, player.entityPosition.y + 1, player.entityPosition.z
                        )
                    )
                    player.movementQueue.reset()
                }
                if (player.entityPosition.x == 2824 && player.entityPosition.y == 5308 //ARMADYL
                    || player.entityPosition.x == 2864 && player.entityPosition.y == 5369 //BANDOS
                    || player.entityPosition.x == 2918 && player.entityPosition.y == 5331 //ZAMMY
                    || player.entityPosition.x == 2895 && player.entityPosition.y == 5272
                ) { // saradomin, NORTH WEST
                    player.moveTo(
                        Position(
                            player.entityPosition.x + 1, player.entityPosition.y - 1, player.entityPosition.z
                        )
                    )
                    player.movementQueue.reset()
                }
                if (player.walkableInterfaceId != 16210) player.packetSender.sendWalkableInterface(16210)
            }

            override fun canTeleport(player: Player): Boolean {
                return true
            }

            override fun onDeath(player: Player) {
                leave(player)
            }

            override fun leave(p: Player) {
                for (i in p.minigameAttributes.godwarsDungeonAttributes.killcount.indices) {
                    p.minigameAttributes.godwarsDungeonAttributes.killcount[i] = 0
                    p.packetSender.sendString(16216 + i, "0")
                }
                p.minigameAttributes.godwarsDungeonAttributes.setAltarDelay(0).setHasEnteredRoom(false)
                p.packetSender.sendMessage("Your Godwars dungeon progress has been reset.")
            }

            override fun handleKilledNPC(killer: Player, n: NPC): Boolean {
                var index = -1
                val npc = n.id
                if (npc == 6246 || npc == 6229 || npc == 6230 || npc == 6231) //Armadyl
                    index =
                        0 else if (npc == 102 || npc == 3583 || npc == 115 || npc == 113 || npc == 6273 || npc == 6276 || npc == 6277 || npc == 6288) //Bandos
                    index =
                        1 else if (npc == 6258 || npc == 6259 || npc == 6254 || npc == 6255 || npc == 6257 || npc == 6256) //Saradomin
                    index =
                        2 else if (npc == 10216 || npc == 6216 || npc == 1220 || npc == 6007 || npc == 6219 || npc == 6220 || npc == 6221 || npc == 49 || npc == 4418) //Zamorak
                    index = 3
                if (index != -1) {
                    killer.minigameAttributes.godwarsDungeonAttributes.killcount[index]++
                    killer.packetSender.sendString(
                        16216 + index, "" + killer.minigameAttributes.godwarsDungeonAttributes.killcount[index]
                    )
                }
                return false
            }
        },
        NOMAD(intArrayOf(3342, 3377), intArrayOf(5839, 5877), true, true, false, true, false, true) {
            override fun canTeleport(player: Player): Boolean {
                player.packetSender.sendMessage("Teleport spells are blocked here. If you'd like to leave, use the southern exit.")
                return false
            }

            override fun leave(player: Player) {
                if (player.regionInstance != null) player.regionInstance.destruct()
                player.moveTo(Position(1890, 3177))
                player.restart()
            }

            override fun handleKilledNPC(killer: Player, npc: NPC): Boolean {
                if (npc.id == 8528) {
                    endFight(killer, true)
                    return true
                }
                return false
            }
        },
        RECIPE_FOR_DISASTER(intArrayOf(1885, 1913), intArrayOf(5340, 5369), true, true, false, false, false, false) {
            override fun canTeleport(player: Player): Boolean {
                player.packetSender.sendMessage("Teleport spells are blocked here. If you'd like to leave, use a portal.")
                return false
            }

            override fun handleKilledNPC(killer: Player, npc: NPC): Boolean {
                handleNPCDeath(killer, npc)
                return true
            }

            override fun leave(player: Player) {
                if (player.regionInstance != null) player.regionInstance.destruct()
                player.moveTo(Position(3081, 3500))
            }

            override fun onDeath(player: Player) {
                leave(player)
            }
        },
        FREE_FOR_ALL_ARENA(intArrayOf(2755, 2876), intArrayOf(5512, 5627), true, true, true, false, false, true) {
            override fun canTeleport(player: Player): Boolean {
                player.packetSender.sendMessage("Teleport spells are blocked here, if you wish to teleport, use the portal.")
                return false
            }

            override fun onDeath(player: Player) {
                player.moveTo(Position(2815, 5511))
            }

            override fun canAttack(player: Player, target: Player): Boolean {
                if (target.location !== FREE_FOR_ALL_ARENA) {
                    player.packetSender.sendMessage("That player has not entered the dangerous zone yet.")
                    player.movementQueue.reset()
                    return false
                }
                return true
            }

            override fun enter(player: Player) {
                if (player.playerInteractingOption != PlayerInteractingOption.ATTACK) {
                    player.packetSender.sendInteractionOption("Attack", 2, true)
                }
            }
        },
        FREE_FOR_ALL_WAIT(intArrayOf(2755, 2876), intArrayOf(5507, 5627), false, false, true, false, false, true) {
            override fun canTeleport(player: Player): Boolean {
                player.packetSender.sendMessage("Teleport spells are blocked here, if you wish to teleport, use the portal.")
                return false
            }

            override fun onDeath(player: Player) {
                player.moveTo(Position(2815, 5511))
            }
        },
        WARRIORS_GUILD(intArrayOf(2833, 2879), intArrayOf(3531, 3559), false, true, true, false, false, true) {
            override fun logout(player: Player) {
                if (player.minigameAttributes.warriorsGuildAttributes.enteredTokenRoom()) {
                    player.moveTo(Position(2844, 3540, 2))
                }
            }

            override fun leave(player: Player) {
                player.minigameAttributes.warriorsGuildAttributes.setEnteredTokenRoom(false)
            }
        },
        PURO_PURO(
            intArrayOf(2556, 2630), intArrayOf(4281, 4354), false, true, true, false, false, true
        ),
        FLESH_CRAWLERS(intArrayOf(2033, 2049), intArrayOf(5178, 5197), false, true, true, false, true, true), RUNESPAN(
            intArrayOf(2122, 2159), intArrayOf(5517, 5556), false, false, true, true, true, false
        ),
        DEFAULT(null, null, false, true, true, true, true, true);

        open fun process(player: Player) {}
        open fun canTeleport(player: Player): Boolean {
            return true
        }

        open fun login(player: Player) {}
        open fun enter(player: Player) {}
        open fun leave(player: Player) {}
        open fun logout(player: Player) {}
        open fun onDeath(player: Player) {}
        open fun handleKilledNPC(killer: Player, npc: NPC): Boolean {
            return false
        }

        open fun canAttack(player: Player, target: Player): Boolean {
            return false
        }

        companion object {
            @JvmStatic
            fun getLocation(gc: Entity): Location {
                for (location in values()) {
                    if (location !== DEFAULT) if (inLocation(gc, location)) return location
                }
                return DEFAULT
            }

            fun inLocation(gc: Entity, location: Location): Boolean {
                return if (location === DEFAULT) {
                    getLocation(gc) === DEFAULT
                } else inLocation(
                    gc.entityPosition.x, gc.entityPosition.y, location
                )
                /*if(gc instanceof Player) {
				Player p = (Player)gc;
				if(location == Location.TRAWLER_GAME) {
					String state = FishingTrawler.getState(p);
					return (state != null && state.equals("PLAYING"));
				} else if(location == FIGHT_PITS_WAIT_ROOM || location == FIGHT_PITS) {
					String state = FightPit.getState(p), needed = (location == FIGHT_PITS_WAIT_ROOM) ? "WAITING" : "PLAYING";
					return (state != null && state.equals(needed));
				} else if(location == Location.SOULWARS) {
					return (SoulWars.redTeam.contains(p) || SoulWars.blueTeam.contains(p) && SoulWars.gameRunning);
				} else if(location == Location.SOULWARS_WAIT) {
					return SoulWars.isWithin(SoulWars.BLUE_LOBBY, p) || SoulWars.isWithin(SoulWars.RED_LOBBY, p);
				}
			}
			 */
            }

            @JvmStatic
            fun inLocation(absX: Int, absY: Int, location: Location): Boolean {
                val checks = location.x!!.size - 1
                var i = 0
                while (i <= checks) {
                    if (absX >= location.x[i] && absX <= location.x[i + 1]) {
                        if (absY >= location.y!![i] && absY <= location.y[i + 1]) {
                            return true
                        }
                    }
                    i += 2
                }
                return false
            }

            /** SHOULD AN ENTITY FOLLOW ANOTHER ENTITY NO MATTER THE DISTANCE BETWEEN THEM?  */
            @JvmStatic
            fun ignoreFollowDistance(character: CharacterEntity): Boolean {
                val location = character.location
                return location === FIGHT_CAVES || location === RECIPE_FOR_DISASTER || location === NOMAD
            }
        }
    }
}