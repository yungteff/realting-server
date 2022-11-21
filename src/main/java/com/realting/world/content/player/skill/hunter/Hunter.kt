package com.realting.world.content.player.skill.hunter

import com.realting.engine.task.impl.HunterTrapsTask
import com.realting.model.*
import com.realting.model.container.impl.Equipment
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.model.movement.MovementQueue
import com.realting.util.Misc
import com.realting.world.content.CustomObjects
import com.realting.world.content.player.skill.hunter.Trap.TrapState
import java.util.concurrent.CopyOnWriteArrayList

object Hunter {
    /**
     * Registers a new Trap
     *
     * @param trap
     */
    fun register(trap: Trap) {
        CustomObjects.spawnGlobalObject(trap.gameObject)
        traps.add(trap)
        if (trap.owner != null) trap.owner!!.trapsLaid = trap.owner!!.trapsLaid + 1
    }

    /**
     * Unregisters a trap
     *
     * @param trap
     */
    @JvmStatic
    fun deregister(trap: Trap) {
        CustomObjects.deleteGlobalObject(trap.gameObject)
        traps.remove(trap) // Remove the Trap
        if (trap.owner != null) trap.owner!!.trapsLaid = trap.owner!!.trapsLaid - 1
    }

    /**
     * The list which contains all Traps
     */
    @JvmField
    var traps: MutableList<Trap> = CopyOnWriteArrayList()

    /**
     * The Hash map which contains all Hunting NPCS
     */
    @JvmField
    var HUNTER_NPC_LIST: MutableList<NPC> = CopyOnWriteArrayList()
    private val exps = intArrayOf(34, 47, 61, 65, 96, 100, 199, 265)

    /**
     * Can this client lay a trap here?
     *
     * @param client
     */
    fun canLay(client: Player): Boolean {
        if (!goodArea(client)) {
            client.packetSender.sendMessage(
                "You need to be in a hunting area to lay a trap."
            )
            return false
        }
        if (!client.clickDelay.elapsed(2000)) return false
        for (trap in traps) {
            if (trap == null) continue
            if (trap.gameObject.entityPosition.x == client.entityPosition.x && trap.gameObject.entityPosition.y == client.entityPosition.y) {
                client.packetSender.sendMessage(
                    "There is already a trap here, please place yours somewhere else."
                )
                return false
            }
        }
        val x = client.entityPosition.x
        val y = client.entityPosition.y
        for (npc in HUNTER_NPC_LIST) {
            if (npc == null || !npc.isVisible) continue
            if (x == npc.entityPosition.x && y == npc.entityPosition.y || x == npc.defaultPosition.x && y == npc.defaultPosition.y) {
                client.packetSender.sendMessage(
                    "You cannot place your trap right here, try placing it somewhere else."
                )
                return false
            }
        }
        if (client.trapsLaid >= getMaximumTraps(client)) {
            client.packetSender.sendMessage(
                "You can only have a max of " + getMaximumTraps(client) + " traps setup at once."
            )
            return false
        }
        return true
    }

    @JvmStatic
    fun handleRegionChange(client: Player) {
        if (client.trapsLaid > 0) {
            for (trap in traps) {
                if (trap.owner != null && trap.owner!!.username == client.username && !Locations.goodDistance(
                        trap.gameObject.entityPosition, client.entityPosition, 50
                    )
                ) {
                    deregister(trap)
                    client.packetSender.sendMessage("You didn't watch over your trap well enough, it has collapsed.")
                }
            }
        }
    }

    /**
     * Checks if the user is in the area where you can lay boxes.
     *
     * @param client
     * @return
     */
    fun goodArea(client: Player): Boolean {
        val x = client.entityPosition.x
        val y = client.entityPosition.y
        return x in 2758..2965 && y >= 2880 && y <= 2954
    }

    /**
     * Returns the maximum amount of traps this player can have
     *
     * @param client
     * @return
     */
    fun getMaximumTraps(client: Player): Int {
        return client.skillManager.getCurrentLevel(Skill.HUNTER) / 20 + 1
    }

    /**
     * Gets the ObjectID required by NPC ID
     *
     * @param npcId
     */
    fun getObjectIDByNPCID(npcId: Int): Int {
        when (npcId) {
            5073 -> return 19180
            5079 -> return 19191
            5080 -> return 19189
            5075 -> return 19184
            5076 -> return 19186
            5074 -> return 19182
            5072 -> return 19178
        }
        return 0
    }

    /**
     * Searches the specific Trap that belongs to this WorldObject
     *
     * @param object
     */
    fun getTrapForGameObject(`object`: GameObject): Trap? {
        for (trap in traps) {
            if (trap.gameObject.entityPosition == `object`.entityPosition) return trap
        }
        return null
    }

    /**
     * Dismantles a Trap
     *
     * @param client
     */
    @JvmStatic
    fun dismantle(client: Player, trap: GameObject?) {
        if (trap == null) return
        val theTrap = getTrapForGameObject(trap)
        if (theTrap != null && theTrap.owner === client) {
            deregister(theTrap)
            if (theTrap is SnareTrap) client.inventory.add(10006, 1) else if (theTrap is BoxTrap) {
                client.inventory.add(10008, 1)
                client.performAnimation(Animation(827))
            }
            client.packetSender.sendMessage("You dismantle the trap..")
        } else client.packetSender.sendMessage(
            "You cannot dismantle someone else's trap."
        )
    }

    /**
     * Sets up a trap
     *
     * @param client
     * @param trap
     */
    @JvmStatic
    fun layTrap(client: Player, trap: Trap) {
        var id = 10006
        if (trap is BoxTrap) {
            id = 10008
            if (client.skillManager.getCurrentLevel(Skill.HUNTER) < 60) {
                client.packetSender.sendMessage("You need a Hunter level of at least 60 to lay this trap.")
                return
            }
        }
        if (!client.inventory.contains(id)) return
        if (canLay(client)) {
            register(trap)
            client.clickDelay.reset()
            client.movementQueue.reset()
            MovementQueue.stepAway(client)
            client.positionToFace = trap.gameObject.entityPosition
            client.performAnimation(Animation(827))
            if (trap is SnareTrap) {
                client.packetSender.sendMessage("You set up a bird snare..")
                client.inventory.delete(10006, 1)
            } else if (trap is BoxTrap) {
                if (client.skillManager.getCurrentLevel(Skill.HUNTER) < 27) {
                    client.packetSender.sendMessage("You need a Hunter level of at least 27 to do this.")
                    return
                }
                client.packetSender.sendMessage("You set up a box trap..")
                client.inventory.delete(10008, 1)
            }
            HunterTrapsTask.fireTask()
        }
    }

    /**
     * Gets the required level for the NPC.
     *
     * @param npcType
     */
    fun requiredLevel(npcType: Int): Int {
        var levelToReturn = 1
        if (npcType == 5072) levelToReturn = 19 else if (npcType == 5072) levelToReturn =
            1 else if (npcType == 5074) levelToReturn = 11 else if (npcType == 5075) levelToReturn =
            5 else if (npcType == 5076) levelToReturn = 9 else if (npcType == 5079) levelToReturn =
            53 else if (npcType == 5080) levelToReturn = 63
        return levelToReturn
    }

    fun isHunterNPC(npc: Int): Boolean {
        return npc >= 5072 && npc <= 5080
    }

    @JvmStatic
    fun lootTrap(client: Player, trap: GameObject?) {
        if (trap != null) {
            client.positionToFace = trap.entityPosition
            val theTrap = getTrapForGameObject(trap)
            if (theTrap != null) {
                if (theTrap.owner != null) if (theTrap.owner === client) {
                    if (theTrap is SnareTrap) {
                        client.inventory.add(10006, 1)
                        client.inventory.add(526, 1)
                        client.inventory.add(9978, 1)
                        if (theTrap.gameObject.id == 19180) {
                            if (client.skillManager.skillCape(Skill.HUNTER)) {
                                client.inventory.add(10088, 40 + Misc.getRandom(30) + Misc.getRandom(30))
                            } else {
                                client.inventory.add(10088, 20 + Misc.getRandom(30))
                            }
                            client.packetSender.sendMessage("You've succesfully caught a Crimson Swift.")
                            client.skillManager.addExperience(Skill.HUNTER, exps[0])
                        } else if (theTrap.gameObject.id == 19184) {
                            if (client.skillManager.skillCape(Skill.HUNTER)) {
                                client.inventory.add(10090, 40 + Misc.getRandom(30) + Misc.getRandom(30))
                            } else {
                                client.inventory.add(10090, 20 + Misc.getRandom(30))
                            }
                            client.packetSender.sendMessage(
                                "You've succesfully caught a Golden Warbler."
                            )
                            client.skillManager.addExperience(Skill.HUNTER, exps[1])
                        } else if (theTrap.gameObject.id == 19186) {
                            if (client.skillManager.skillCape(Skill.HUNTER)) {
                                client.inventory.add(10091, 40 + Misc.getRandom(50) + Misc.getRandom(50))
                            } else {
                                client.inventory.add(
                                    10091, 20 + Misc.getRandom(50)
                                )
                            }
                            client.packetSender.sendMessage(
                                "You've succesfully caught a Copper Longtail."
                            )
                            client.skillManager.addExperience(Skill.HUNTER, exps[2])
                        } else if (theTrap.gameObject.id == 19182) {
                            if (client.skillManager.skillCape(Skill.HUNTER)) {
                                client.inventory.add(10089, 40 + Misc.getRandom(30) + Misc.getRandom(30))
                            } else {
                                client.inventory.add(
                                    10089, 20 + Misc.getRandom(30)
                                )
                            }
                            client.packetSender.sendMessage(
                                "You've succesfully caught a Cerulean Twitch."
                            )
                            client.skillManager.addExperience(Skill.HUNTER, exps[3])
                        } else if (theTrap.gameObject.id == 19178) {
                            if (client.skillManager.skillCape(Skill.HUNTER)) {
                                client.inventory.add(10087, 40 + Misc.getRandom(30) + Misc.getRandom(30))
                            } else {
                                client.inventory.add(
                                    10087, 20 + Misc.getRandom(30)
                                )
                            }
                            client.packetSender.sendMessage(
                                "You've succesfully caught a Tropical Wagtail."
                            )
                            client.skillManager.addExperience(Skill.HUNTER, exps[4])
                        }
                        if (client.skillManager.skillCape(Skill.HUNTER)) {
                            client.packetSender.sendMessage("Your cape gives you double the loot!")
                        }
                    } else if (theTrap is BoxTrap) {
                        client.inventory.add(10008, 1)
                        if (theTrap.gameObject.id == 19191) {
                            if (client.skillManager.skillCape(Skill.HUNTER)) {
                                client.inventory.add(10033, 2)
                            } else {
                                client.inventory.add(10033, 1)
                            }
                            client.skillManager.addExperience(Skill.HUNTER, exps[6])
                            client.packetSender.sendMessage(
                                "You've succesfully caught a Chinchompa!"
                            )
                        } else if (theTrap.gameObject.id == 19189) {
                            if (client.skillManager.skillCape(Skill.HUNTER)) {
                                client.inventory.add(10034, 2)
                            } else {
                                client.inventory.add(10034, 1)
                            }
                            client.skillManager.addExperience(Skill.HUNTER, exps[7])
                            client.packetSender.sendMessage(
                                "You've succesfully caught a Red Chinchompa!"
                            )
                        }
                    }
                    if (client.skillManager.skillCape(Skill.HUNTER)) {
                        client.packetSender.sendMessage("Your cape gives you double the loot!")
                    }
                    deregister(theTrap)
                    client.performAnimation(Animation(827))
                } else client.packetSender.sendMessage(
                    "This is not your trap."
                )
            }
        }
    }

    /**
     * Try to catch an NPC
     *
     * @param trap
     * @param npc
     */
    fun catchNPC(trap: Trap, npc: NPC) {
        if (trap.trapState == TrapState.CAUGHT) return
        if (trap.owner != null) {
            if (trap.owner!!.skillManager.getCurrentLevel(Skill.HUNTER) < requiredLevel(npc.id)) {
                trap.owner!!.packetSender.sendMessage(
                    "You failed to catch the animal because your Hunter level is too low."
                )
                trap.owner!!.packetSender.sendMessage(
                    "You need atleast " + requiredLevel(npc.id) + " Hunter to catch this animal"
                )
                return
            }
            deregister(trap)
            if (trap is SnareTrap) register(
                SnareTrap(
                    GameObject(
                        getObjectIDByNPCID(npc.id), Position(trap.gameObject.entityPosition.x, trap.gameObject.entityPosition.y)
                    ), TrapState.CAUGHT, 100, trap.owner
                )
            ) else register(
                BoxTrap(
                    GameObject(
                        getObjectIDByNPCID(npc.id), Position(trap.gameObject.entityPosition.x, trap.gameObject.entityPosition.y)
                    ), TrapState.CAUGHT, 100, trap.owner
                )
            )
            HUNTER_NPC_LIST.remove(npc)
            npc.isVisible = false
            npc.appendDeath()
        }
    }

    fun hasLarupia(client: Player?): Boolean {
        return client!!.equipment.items[Equipment.HEAD_SLOT].id == 10045 && client.equipment.items[Equipment.BODY_SLOT].id == 10043 && client.equipment.items[Equipment.LEG_SLOT].id == 10041
    }

    @JvmStatic
    fun handleLogout(p: Player) {
        if (p.trapsLaid > 0) {
            for (trap in traps) {
                if (trap != null) {
                    if (trap.owner != null && trap.owner!!.username == p.username) {
                        deregister(trap)
                        if (trap is SnareTrap) p.inventory.add(10006, 1) else if (trap is BoxTrap) {
                            p.inventory.add(10008, 1)
                            p.performAnimation(Animation(827))
                        }
                    }
                }
            }
        }
    }
}