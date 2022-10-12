package com.realting.net.packet.impl

import com.realting.world.content.player.skill.woodcutting.Woodcutting.cutWood
import com.realting.world.content.player.skill.mining.MiningData.forRock
import com.realting.world.content.player.skill.mining.Mining.startMining
import com.realting.world.content.player.skill.runecrafting.Runecrafting.runecraftingAltar
import com.realting.world.content.player.skill.runecrafting.Runecrafting.craftRunes
import com.realting.world.content.player.events.WildernessObelisks.handleObelisk
import com.realting.world.content.player.skill.dungeoneering.Dungeoneering.doingDungeoneering
import com.realting.world.content.player.skill.fishing.Fishing.setupFishing
import com.realting.world.content.player.skill.woodcutting.WoodcuttingData.getHatchet
import com.realting.world.content.player.skill.hunter.Hunter.dismantle
import com.realting.world.content.player.skill.hunter.PuroPuro.goThroughWheat
import com.realting.world.content.minigames.Nomad.endFight
import com.realting.world.content.minigames.RecipeForDisaster.openRFDShop
import com.realting.world.content.minigames.RecipeForDisaster.leave
import com.realting.world.content.minigames.RecipeForDisaster.enter
import com.realting.world.content.minigames.FightPit.addPlayer
import com.realting.world.content.minigames.FightPit.removePlayer
import com.realting.world.content.minigames.FightCave.leaveCave
import com.realting.world.content.minigames.FightCave.enterCave
import com.realting.world.content.minigames.Dueling.Companion.checkRule
import com.realting.world.content.minigames.PestControl.Companion.boardBoat
import com.realting.world.content.minigames.WarriorsGuild.warriorsGuildDialogue
import com.realting.world.content.minigames.WarriorsGuild.handleTokenRemoval
import com.realting.world.content.minigames.WarriorsGuild.resetCyclopsCombat
import com.realting.world.content.player.skill.hunter.Hunter.lootTrap
import com.realting.world.content.player.skill.thieving.Stalls.stealFromStall
import com.realting.world.content.player.skill.fishing.Fishing.forSpot
import com.realting.world.content.player.skill.smithing.EquipmentMaking.handleAnvil
import com.realting.world.content.combat.weapon.CombatSpecial.Companion.updateBar
import com.realting.world.content.player.skill.smithing.Smelting.openInterface
import com.realting.world.content.player.skill.mining.Prospecting.prospectOre
import com.realting.world.content.player.skill.crafting.Flax.showSpinInterface
import com.realting.world.content.player.skill.crafting.Jewelry.jewelryInterface
import com.realting.net.packet.PacketListener
import com.realting.world.clip.region.RegionClipping
import com.realting.model.definitions.GameObjectDefinition
import com.realting.engine.task.impl.WalkToTask
import com.realting.engine.task.impl.WalkToTask.FinalizedMovementTask
import com.realting.world.content.player.skill.woodcutting.WoodcuttingData
import com.realting.world.content.randomevents.EvilTree.EvilTreeDef
import com.realting.world.content.player.skill.runecrafting.RunecraftingData.RuneData
import com.realting.world.content.player.skill.agility.Agility
import com.realting.world.content.minigames.Barrows
import com.realting.world.content.player.skill.construction.ConstructionActions
import com.realting.world.content.holidayevents.easter2017data
import com.realting.engine.task.TaskManager
import com.realting.world.content.holidayevents.christmas2016
import com.realting.GameSettings
import com.realting.engine.task.Task
import com.realting.model.*
import com.realting.world.content.transportation.TeleportHandler
import com.realting.world.content.transportation.TeleportType
import com.realting.world.content.dialogue.DialogueManager
import com.realting.world.content.player.skill.fishing.Fishing
import com.realting.world.content.portal.portal
import com.realting.world.content.transportation.TeleportLocations
import com.realting.model.container.impl.Equipment
import com.realting.world.content.player.skill.woodcutting.WoodcuttingData.Hatchet
import com.realting.world.content.minigames.Dueling.DuelRule
import com.realting.world.World
import com.realting.world.content.combat.range.DwarfMultiCannon
import com.realting.model.input.impl.EnterAmountOfLogsToAdd
import com.realting.world.content.combat.prayer.PrayerHandler
import com.realting.world.content.combat.prayer.CurseHandler
import com.realting.model.entity.character.player.Player
import com.realting.world.content.combat.magic.Autocasting
import com.realting.world.content.CrystalChest
import com.realting.model.input.impl.DonateToWell
import com.realting.net.packet.Packet
import com.realting.util.Misc
import com.realting.world.content.CustomObjects
import com.realting.world.content.grandexchange.GrandExchange
import com.realting.world.content.player.skill.construction.Construction

/**
 * This packet listener is called when a player clicked
 * on a game object.
 *
 * @author relex lawl
 */
class ObjectActionPacketListener : PacketListener {
    override fun handleMessage(player: Player, packet: Packet) {
        if (player.isTeleporting || player.isPlayerLocked || player.movementQueue.isLockedMovement) return
        when (packet.opcode) {
            FIRST_CLICK -> {
                firstClick(player, packet)
                if (player.rights.OwnerDeveloperOnly()) {
                    player.packetSender.sendMessage("1st click obj")
                }
            }
            SECOND_CLICK -> {
                secondClick(player, packet)
                if (player.rights.OwnerDeveloperOnly()) {
                    player.packetSender.sendMessage("2nd click obj")
                }
            }
            THIRD_CLICK -> if (player.rights.OwnerDeveloperOnly()) {
                player.packetSender.sendMessage("3rd click obj. no handler.")
            }
            FOURTH_CLICK -> if (player.rights.OwnerDeveloperOnly()) {
                player.packetSender.sendMessage("4th click obj. no handler")
            }
            FIFTH_CLICK -> {
                fifthClick(player, packet)
                if (player.rights.OwnerDeveloperOnly()) {
                    player.packetSender.sendMessage("5th click obj")
                }
            }
        }
    }

    companion object {
        /**
         * The PacketListener logger to debug information and print out errors.
         */
        //private final static Logger logger = Logger.getLogger(ObjectActionPacketListener.class);
        private fun firstClick(player: Player, packet: Packet) {
            val x = packet.readLEShortA().toInt()
            val id = packet.readInt()
            val y = packet.readUnsignedShortA()
            val position = Position(x, y, player.position.z)
            val gameObject = GameObject(id, position)
            if (id > 0 && id != 6 && !RegionClipping.objectExists(gameObject)) {
                if (player.rights.OwnerDeveloperOnly()) {
                    player.packetSender.sendMessage("A interaction error occured. Error code: $id")
                } else {
                    player.packetSender.sendMessage("Nothing interesting happens.")
                }
                return
            }
            var distanceX = player.position.x - position.x
            var distanceY = player.position.y - position.y
            if (distanceX < 0) distanceX = -distanceX
            if (distanceY < 0) distanceY = -distanceY
            var size =
                if (distanceX > distanceY) GameObjectDefinition.forId(id).sizeX else GameObjectDefinition.forId(id).sizeY
            if (size <= 0) size = 1
            gameObject.setSize(size)
            if (player.movementQueue.isLockedMovement) return
            if (player.rights == PlayerRights.DEVELOPER) player.packetSender.sendMessage("First click object id; [id, position] : [$id, $position]")
            player.setInteractingObject(gameObject).walkToTask =
                WalkToTask(player, position, gameObject.getSize(), object : FinalizedMovementTask {
                    override fun execute() {
                        player.positionToFace = gameObject.position
                        if (WoodcuttingData.Trees.forId(id) != null) {
                            cutWood(player, gameObject, false)
                            return
                        }
                        if (EvilTreeDef.forId(id) != null) {
                            cutWood(player, gameObject, false)
                            return
                        }
                        if (forRock(gameObject.id) != null) {
                            startMining(player, gameObject)
                            return
                        }
                        if (player.farming.click(player, x, y, 1)) return
                        if (runecraftingAltar(player, gameObject.id)) {
                            val rune = RuneData.forId(gameObject.id) ?: return
                            craftRunes(player, rune)
                            return
                        }
                        if (Agility.handleObject(player, gameObject)) {
                            return
                        }
                        if (Barrows.handleObject(player, gameObject)) {
                            return
                        }
                        if (player.location != null && player.location === Locations.Location.WILDERNESS && handleObelisk(
                                gameObject.id
                            )
                        ) {
                            return
                        }
                        if (ConstructionActions.handleFirstObjectClick(player, gameObject)) {
                            return
                        }
                        if (gameObject.definition != null && gameObject.definition.getName() != null && gameObject.definition.name.equals(
                                "door",
                                ignoreCase = true
                            ) && player.rights.OwnerDeveloperOnly()
                        ) {
                            player.packetSender.sendMessage("You just clicked a door. ID: $id")
                            //CustomObjects.deleteGlobalObject(gameObject);

                            /*Door door = Door.create(gameObject.getId(), gameObject.getPosition().getX(), gameObject.getPosition().getY());
                                //GameObject obj = Region.loadRegion(gameObject.getPosition().getX(), gameObject.getPosition().getY()).getObject(gameObject.getId(), gameObject.getPosition().getX(), gameObject.getPosition().getY());
                                //player.createObject(gameObject.getPosition().getX(), gameObject.getPosition().getY(), gameObject.getId(), door.isOpen() ? obj.getFace() : obj.getFace() + 1, 0);
                                player.getPacketSender().sendObject(new GameObject(gameObject.getId(), new Position(gameObject.getPosition().getX(), gameObject.getPosition().getY()), 10, (door.isOpen() ? gameObject.getFace() : gameObject.getFace() + 1)));
                                door.setOpen(!door.isOpen());*/
                        }
                        when (id) {
                            2305 -> if (player.location != null && player.location === Locations.Location.WILDERNESS) {
                                player.moveTo(Position(3003, 10354, player.position.z))
                                player.packetSender.sendMessage("You escape from the spikes.")
                            }
                            589 -> if (Misc.easter(2017)) {
                                if (player.inventory.isFull) {
                                    player.packetSender.sendMessage("My inventory is too full, I should make room first.")
                                    return
                                }
                                if (player.easter2017 == easter2017data.forObjectId(id).requiredProgress) {
                                    player.packetSender.sendMessage(easter2017data.forObjectId(id).searchMessage)
                                    player.easter2017 = easter2017data.forObjectId(id).requiredProgress + 1
                                    player.inventory.add(1961, 1)
                                }
                            } else {
                                player.packetSender.sendMessage("Just a wise old woman's ball.")
                            }
                            11678 -> if (Misc.easter(2017)) {
                                if (player.inventory.isFull) {
                                    player.packetSender.sendMessage("My inventory is too full, I should make room first.")
                                    return
                                }
                                if (player.easter2017 == easter2017data.forObjectId(id).requiredProgress) {
                                    player.packetSender.sendMessage(easter2017data.forObjectId(id).searchMessage)
                                    player.easter2017 = easter2017data.forObjectId(id).requiredProgress + 1
                                    player.inventory.add(1961, 1)
                                }
                            } else {
                                player.packetSender.sendMessage("Nope. Nothing special to it.")
                            }
                            5595 -> if (Misc.easter(2017)) {
                                if (player.inventory.isFull) {
                                    player.packetSender.sendMessage("My inventory is too full, I should make room first.")
                                    return
                                }
                                if (player.easter2017 == easter2017data.forObjectId(id).requiredProgress) {
                                    player.packetSender.sendMessage(easter2017data.forObjectId(id).searchMessage)
                                    player.easter2017 = easter2017data.forObjectId(id).requiredProgress + 1
                                    player.inventory.add(1961, 1)
                                }
                            } else {
                                player.packetSender.sendMessage("Just some toys.")
                            }
                            2725 -> if (Misc.easter(2017)) {
                                if (player.inventory.isFull) {
                                    player.packetSender.sendMessage("My inventory is too full, I should make room first.")
                                    return
                                }
                                if (player.easter2017 == easter2017data.forObjectId(id).requiredProgress) {
                                    player.packetSender.sendMessage(easter2017data.forObjectId(id).searchMessage)
                                    player.easter2017 = easter2017data.forObjectId(id).requiredProgress + 1
                                    player.inventory.add(1961, 1)
                                }
                            } else {
                                player.packetSender.sendMessage("Just regular fireplace things.")
                            }
                            423 -> if (Misc.easter(2017)) {
                                if (player.inventory.isFull) {
                                    player.packetSender.sendMessage("My inventory is too full, I should make room first.")
                                    return
                                }
                                if (player.easter2017 == easter2017data.forObjectId(id).requiredProgress) {
                                    player.packetSender.sendMessage(easter2017data.forObjectId(id).searchMessage)
                                    player.easter2017 = easter2017data.forObjectId(id).requiredProgress + 1
                                    player.inventory.add(1961, 1)
                                }
                            } else {
                                player.packetSender.sendMessage("I don't want to mess around with someone's bed.")
                            }
                            11339 -> if (Misc.easter(2017)) {
                                if (player.inventory.isFull) {
                                    player.packetSender.sendMessage("My inventory is too full, I should make room first.")
                                    return
                                }
                                if (player.easter2017 == easter2017data.forObjectId(id).requiredProgress) {
                                    player.packetSender.sendMessage(easter2017data.forObjectId(id).searchMessage)
                                    player.easter2017 = easter2017data.forObjectId(id).requiredProgress + 1
                                    player.inventory.add(1961, 1)
                                }
                            } else {
                                player.packetSender.sendMessage("Just some gold, I can get enough on my own.")
                            }
                            17953 -> if (player.location === Locations.Location.ZULRAH_WAITING) {
                                player.packetSender.sendMessage("You push the boat into the swamp...")
                                //player.setPositionToFace(gameObject.getPosition());
                                player.performAnimation(Animation(923))
                                TaskManager.submit(object : Task(1, player, true) {
                                    var tick = 0
                                    public override fun execute() {
                                        if (tick >= 2) {
                                            //player.moveTo(new Position(player.getPosition().getX()-1, player.getPosition().getY()));
                                            player.moveTo(Position(3420, 2777, (player.index + 1) * 4))
                                            player.packetSender.sendMessage("...And arrive in Zulrah's territory.")
                                            stop()
                                        }
                                        tick++
                                    }
                                })
                            } else if (player.location === Locations.Location.ZULRAH) {
                                if (!player.rights.isMember && player.skillManager.getCurrentLevel(Skill.AGILITY) < 85) {
                                    player.packetSender.sendMessage("You need 85 Agility to navigate the boat back to camp!")
                                    return
                                }
                                if (player.rights.isMember && player.skillManager.getCurrentLevel(Skill.AGILITY) < 85) {
                                    player.packetSender.sendMessage("As a member you can navigate the swamp without 85 Agility.")
                                }
                                player.packetSender.sendMessage("You push the boat into the swamp...")
                                //player.setPositionToFace(gameObject.getPosition());
                                player.performAnimation(Animation(923))
                                TaskManager.submit(object : Task(1, player, true) {
                                    var tick = 0
                                    public override fun execute() {
                                        if (tick >= 2) {
                                            //player.moveTo(new Position(player.getPosition().getX()-1, player.getPosition().getY()));
                                            player.moveTo(Position(3406, 2794, 0))
                                            player.packetSender.sendMessage("...And return to the pillar santuary.")
                                            stop()
                                        }
                                        tick++
                                    }
                                })
                            }
                            28295 -> if (christmas2016.isChristmas()) {
                                player.packetSender.sendMessage("Welcome to the Christmas 2016 event!")
                                player.moveTo(christmas2016.eventStart)
                            }
                            28296 -> {
                                if (!player.clickDelay.elapsed(1250)) {
                                    //player.getPacketSender().sendMessage("Your hands are getting cold, slow down!");
                                    return
                                }
                                player.clickDelay.reset()
                                if (!player.inventory.isFull || player.inventory.freeSlots == 0 && player.inventory.contains(
                                        10501
                                    )
                                ) {
                                    player.performAnimation(Animation(827))
                                    player.inventory.add(10501, Misc.getRandom(20))
                                    player.packetSender.sendMessage("You pack some of the snow together...")
                                } else {
                                    player.packetSender.sendMessage("You'll need some inventory space first!")
                                }
                            }
                            134, 135 -> {
                                if (player.position.y < 3354 && GameSettings.Halloween) {
                                    TeleportHandler.teleportPlayer(
                                        player,
                                        Position(3109, 3354, 404),
                                        TeleportType.NORMAL
                                    )
                                }
                                val move = player.position.y < 3354
                                if (!move) {
                                    player.packetSender.sendMessage("Nope, it's not going to move.")
                                }
                            }
                            2112 -> {
                                if (!player.rights.isMember) {
                                    player.packetSender.sendMessage("You must be a member to access this area.")
                                    return
                                }
                                TaskManager.submit(object : Task(0, player, true) {
                                    var tick = 0
                                    public override fun execute() {
                                        tick++
                                        if (player.position.x == 3046 && player.position.y == 9757) {
                                            player.movementQueue.walkStep(0, -1)
                                            player.packetSender.sendMessage("As a member, you can pass through the door.")
                                        } else if (player.position.x == 3046 && player.position.y == 9756) {
                                            player.movementQueue.walkStep(0, 1)
                                            player.packetSender.sendMessage("As a member, you can pass through the door.")
                                        } else {
                                            player.packetSender.sendMessage("You must be in front of the door first.")
                                        }
                                        if (tick == 1) stop()
                                    }

                                    override fun stop() {
                                        setEventRunning(false)
                                        //player.setCrossingObstacle(false);
                                    }
                                })
                            }
                            2882, 2883 -> TaskManager.submit(object : Task(0, player, true) {
                                var tick = 0
                                public override fun execute() {
                                    tick++
                                    if (player.position.x == 3268 && player.position.y > 3226 && player.position.y < 3229) {
                                        player.movementQueue.walkStep(-1, 0)
                                        player.packetSender.sendMessage("You pass through the gate.")
                                    } else if (player.position.x == 3267 && player.position.y > 3226 && player.position.y < 3229) {
                                        player.movementQueue.walkStep(1, 0)
                                        player.packetSender.sendMessage("You pass through the gate.")
                                    } else {
                                        player.packetSender.sendMessage("You must be in front of the gate first.")
                                    }
                                    if (tick == 1) stop()
                                }

                                override fun stop() {
                                    setEventRunning(false)
                                    //player.setCrossingObstacle(false);
                                }
                            })
                            5262 -> if (player.location === Locations.Location.KRAKEN) {
                                player.packetSender.sendMessage("You leave the cave and end up at home.")
                                player.moveTo(GameSettings.DEFAULT_POSITION.copy())
                            }
                            2273 -> {
                                player.moveTo(Position(3563, 3313, 0))
                                Locations.Location.THE_SIX.leave(player)
                            }
                            5259 -> if (player.position.x >= 3653) { // :)
                                if (player.position.y != 3485 && player.position.y != 3486) {
                                    player.packetSender.sendMessage("You need to stand infront of the barrier to pass through.")
                                    return
                                }
                                player.moveTo(Position(3651, player.position.y))
                            } else {
                                player.dialogueActionId = 73
                                DialogueManager.start(player, 115)
                            }
                            10805, 10806 -> GrandExchange.open(player)
                            38700 -> if (gameObject.position.x == 3668 && gameObject.position.y == 2976) {
                                player.packetSender.sendMessage("<img=10> @blu@Welcome to the free-for-all arena! You will not lose any items on death here.")
                                player.moveTo(Position(2815, 5511))
                            } else if (player.location === Locations.Location.FREE_FOR_ALL_WAIT) {
                                player.moveTo(GameSettings.DEFAULT_POSITION.copy())
                            } else if (gameObject.position.x == 2849 && gameObject.position.y == 3353) {
                                player.packetSender.sendMessage("<img=10> @blu@Welcome to the free-for-all arena! You will not lose any items on death here.")
                                player.moveTo(Position(2815, 5511))
                            }
                            2465 -> if (player.location === Locations.Location.EDGEVILLE) {
                                player.packetSender.sendMessage("<img=10> @blu@Welcome to the free-for-all arena! You will not lose any items on death here.")
                                player.moveTo(Position(2815, 5511))
                            } else {
                                player.packetSender.sendMessage("The portal does not seem to be functioning properly.")
                            }
                            45803, 1767 -> {
                                DialogueManager.start(player, 114)
                                player.dialogueActionId = 72
                            }
                            7352 -> if (doingDungeoneering(player) && player.minigameAttributes.dungeoneeringAttributes.party!!.gatestonePosition != null) {
                                player.moveTo(player.minigameAttributes.dungeoneeringAttributes.party!!.gatestonePosition)
                                player.setEntityInteraction(null)
                                player.packetSender.sendMessage("You are teleported to your party's gatestone.")
                                player.performGraphic(Graphic(1310))
                            } else player.packetSender.sendMessage("Your party must drop a Gatestone somewhere in the dungeon to use this portal.")
                            7353 -> player.moveTo(Position(2439, 4956, player.position.z))
                            7321 -> player.moveTo(Position(2452, 4944, player.position.z))
                            7322 -> player.moveTo(Position(2455, 4964, player.position.z))
                            7315 -> player.moveTo(Position(2447, 4956, player.position.z))
                            7316 -> player.moveTo(Position(2471, 4956, player.position.z))
                            7318 -> player.moveTo(Position(2464, 4963, player.position.z))
                            7324 -> player.moveTo(Position(2481, 4956, player.position.z))
                            7319 -> if (gameObject.position.x == 2481 && gameObject.position.y == 4956) player.moveTo(
                                Position(2467, 4940, player.position.z)
                            )
                            4388 -> {}
                            11356 -> {
                                player.moveTo(Position(2860, 9741))
                                player.packetSender.sendMessage("You step through the portal..")
                            }
                            47180 -> {
                                if (!player.rights.isMember) {
                                    player.packetSender.sendMessage("You must be a Member to use this.")
                                    return
                                }
                                player.packetSender.sendMessage("You activate the device..")
                                player.moveTo(Position(2586, 3912))
                            }
                            10091, 8702 -> {
                                if (gameObject.id == 8702) {
                                    if (!player.rights.isMember) {
                                        player.packetSender.sendMessage("You must be a Member to use this.")
                                        return
                                    }
                                }
                                setupFishing(player, Fishing.Spot.ROCKTAIL)
                            }
                            9319 -> {
                                if (player.skillManager.getCurrentLevel(Skill.AGILITY) < 61) {
                                    player.packetSender.sendMessage("You need an Agility level of at least 61 or higher to climb this")
                                    return
                                }
                                if (player.position.z == 0) player.moveTo(
                                    Position(
                                        3422,
                                        3549,
                                        1
                                    )
                                ) else if (player.position.z == 1) {
                                    if (gameObject.position.x == 3447) player.moveTo(
                                        Position(
                                            3447,
                                            3575,
                                            2
                                        )
                                    ) else player.moveTo(
                                        Position(3447, 3575, 0)
                                    )
                                }
                            }
                            9320 -> {
                                if (player.skillManager.getCurrentLevel(Skill.AGILITY) < 61) {
                                    player.packetSender.sendMessage("You need an Agility level of at least 61 or higher to climb this")
                                    return
                                }
                                if (player.position.z == 1) player.moveTo(
                                    Position(
                                        3422,
                                        3549,
                                        0
                                    )
                                ) else if (player.position.z == 0) player.moveTo(
                                    Position(3447, 3575, 1)
                                ) else if (player.position.z == 2) player.moveTo(Position(3447, 3575, 1))
                                player.performAnimation(Animation(828))
                            }
                            2470 -> {
                                if (player.teleblockTimer > 0) {
                                    player.packetSender.sendMessage(
                                        "You are teleblocked, don't die, noob."
                                    )
                                    return
                                }
                                if (gameObject.position.x == 2464 && gameObject.position.y == 4782) {
                                    player.moveTo(GameSettings.DEFAULT_POSITION.copy())
                                    player.packetSender.sendMessage("The portal teleports you home.")
                                    return
                                }
                                if (gameObject.position.x == 3674 && gameObject.position.y == 2981 && GameSettings.FridayThe13th) {
                                    player.moveTo(Position(2463, 4782))
                                    player.packetSender.sendMessage("Enjoy the Friday the 13th mini-event.")
                                }
                                if (gameObject.position.x == 3674 && gameObject.position.y == 2981 && GameSettings.Halloween) {
                                    player.moveTo(Position(3108, 3352, 4))
                                    player.packetSender.sendMessage("<img=10> You teleport to the event!")
                                    return
                                }
                            }
                            2274 -> {
                                if (player.teleblockTimer > 0) {
                                    player.packetSender.sendMessage(
                                        "You are teleblocked, don't die, noob."
                                    )
                                    return
                                }
                                if (gameObject.position.x == 2912 && gameObject.position.y == 5300) {
                                    player.moveTo(Position(2914, 5300, 1))
                                } else if (gameObject.position.x == 2914 && gameObject.position.y == 5300) {
                                    player.moveTo(Position(2912, 5300, 2))
                                } else if (gameObject.position.x == 3553 && gameObject.position.y == 9695) {
                                    player.moveTo(Position(3565, 3313, 0))
                                } else if (gameObject.position.x == 2919 && gameObject.position.y == 5276) {
                                    player.moveTo(Position(2918, 5274))
                                } else if (gameObject.position.x == 2918 && gameObject.position.y == 5274) {
                                    player.moveTo(Position(2919, 5276, 1))
                                } else if (gameObject.position.x == 3001 && gameObject.position.y == 3931 || gameObject.position.x == 3652 && gameObject.position.y == 3488) {
                                    player.moveTo(GameSettings.DEFAULT_POSITION.copy())
                                    player.packetSender.sendMessage("The portal teleports you home.")
                                    //} else if(gameObject.getPosition().getX() == 2914 && gameObject.getPosition().getY() == 5300 && (player.getAmountDonated() >= 5 || player.getSkillManager().getCurrentLevel(Skill.AGILITY) == 99)) {
                                    //	player.getPacketSender().sendMessage("You would have access to the shortcut.");
                                }
                            }
                            28779 -> {
                                if (player.teleblockTimer > 0) {
                                    player.packetSender.sendMessage(
                                        "You are teleblocked, and cannot navigate the chaos tunnels."
                                    )
                                    return
                                }
                                var des = Position(-1, -1)
                                var i = 0
                                while (i < portal.values().size) {
                                    if (portal.values()[i].location.x == gameObject.position.x && portal.values()[i].location.y == gameObject.position.y) {
                                        des = Position(
                                            portal.values()[i].destination.x,
                                            portal.values()[i].destination.y,
                                            player.position.z
                                        )
                                        //System.out.println("Matched on portal index "+i);
                                        break
                                    }
                                    i++
                                }
                                if (des.x != -1 && des.y != -1) {
                                    player.moveTo(des)
                                } else {
                                    player.packetSender.sendMessage("ERROR 13754, no internals. Report on forums!")
                                }
                            }
                            7836, 7808 -> {
                                val amt = player.inventory.getAmount(6055)
                                if (amt > 0) {
                                    player.inventory.delete(6055, amt)
                                    player.packetSender.sendMessage("You put the weed in the compost bin.")
                                    player.skillManager.addExperience(Skill.FARMING, 1 * amt)
                                } else {
                                    player.packetSender.sendMessage("You do not have any weeds in your inventory.")
                                }
                            }
                            5960, 5959 -> if (player.location === Locations.Location.MAGEBANK_SAFE) {
                                TeleportHandler.teleportPlayer(
                                    player,
                                    TeleportLocations.MAGEBANK_WILDY.pos,
                                    TeleportType.LEVER
                                )
                            } else if (player.wildernessLevel >= 53 && player.location === Locations.Location.WILDERNESS) {
                                TeleportHandler.teleportPlayer(
                                    player,
                                    TeleportLocations.MAGEBANK_SAFE.pos,
                                    TeleportType.LEVER
                                )
                            } else {
                                player.packetSender.sendMessage("ERROR: 00512, P: [" + player.position.x + "," + player.position.y + "," + player.position.z + "] - please report this bug!")
                            }
                            5096 -> if (gameObject.position.x == 2644 && gameObject.position.y == 9593) player.moveTo(
                                Position(2649, 9591)
                            )
                            5094 -> if (gameObject.position.x == 2648 && gameObject.position.y == 9592) player.moveTo(
                                Position(2643, 9594, 2)
                            )
                            5098 -> if (gameObject.position.x == 2635 && gameObject.position.y == 9511) player.moveTo(
                                Position(2637, 9517)
                            )
                            5097 -> if (gameObject.position.x == 2635 && gameObject.position.y == 9514) player.moveTo(
                                Position(2636, 9510, 2)
                            )
                            26428, 26426, 26425, 26427 -> {
                                var bossRoom = "Armadyl"
                                var leaveRoom = player.position.y > 5295
                                var index = 0
                                var movePos = Position(2839, if (!leaveRoom) 5296 else 5295, 2)
                                if (id == 26425) {
                                    bossRoom = "Bandos"
                                    leaveRoom = player.position.x > 2863
                                    index = 1
                                    movePos = Position(if (!leaveRoom) 2864 else 2863, 5354, 2)
                                } else if (id == 26427) {
                                    bossRoom = "Saradomin"
                                    leaveRoom = player.position.x < 2908
                                    index = 2
                                    movePos = Position(if (leaveRoom) 2908 else 2907, 5265)
                                } else if (id == 26428) {
                                    bossRoom = "Zamorak"
                                    leaveRoom = player.position.y <= 5331
                                    index = 3
                                    movePos = Position(2925, if (leaveRoom) 5332 else 5331, 2)
                                }
                                if (!leaveRoom && !player.rights.isMember && player.minigameAttributes.godwarsDungeonAttributes.killcount[index] < 20) {
                                    if (player.inventory.contains(22053)) {
                                        player.inventory.delete(22053, 1)
                                        player.packetSender.sendMessage("Your ecumenical key is consumed, and you pass through the door.")
                                    } else {
                                        player.packetSender.sendMessage("You need " + Misc.anOrA(bossRoom) + " " + bossRoom + " killcount of at least 20 to enter this room.")
                                        return
                                    }
                                }
                                if (player.rights.isMember) {
                                    player.packetSender.sendMessage("@red@As a member, you don't need to worry about kill count.")
                                    player.performGraphic(Graphic(6, GraphicHeight.LOW))
                                }
                                player.moveTo(movePos)
                                player.minigameAttributes.godwarsDungeonAttributes.setHasEnteredRoom(!leaveRoom)
                                player.minigameAttributes.godwarsDungeonAttributes.killcount[index] = 0
                                player.packetSender.sendString(16216 + index, "0")
                            }
                            26289, 26286, 26288, 26287 -> {
                                if (System.currentTimeMillis() - player.minigameAttributes.godwarsDungeonAttributes.altarDelay < 600000) {
                                    player.packetSender.sendMessage("")
                                    player.packetSender.sendMessage("You can only pray at a God's altar once every 10 minutes.")
                                    player.packetSender.sendMessage("You must wait another " + (600 - (System.currentTimeMillis() - player.minigameAttributes.godwarsDungeonAttributes.altarDelay) * 0.001).toInt() + " seconds before being able to do this again.")
                                    return
                                }
                                val itemCount = when (id) {
                                    26289 -> Equipment.getItemCount(
                                        player,
                                        "Bandos",
                                        false
                                    )
                                    26286 -> Equipment.getItemCount(
                                        player,
                                        "Zamorak",
                                        false
                                    )
                                    26288 -> Equipment.getItemCount(
                                        player,
                                        "Armadyl",
                                        false
                                    )
                                    26287 -> Equipment.getItemCount(player, "Saradomin", false)
                                    else -> 0
                                }
                                val toRestore = player.skillManager.getMaxLevel(Skill.PRAYER) + itemCount * 10
                                if (player.skillManager.getCurrentLevel(Skill.PRAYER) >= toRestore) {
                                    player.packetSender.sendMessage("You do not need to recharge your Prayer points at the moment.")
                                    return
                                }
                                player.performAnimation(Animation(645))
                                player.skillManager.setCurrentLevel(Skill.PRAYER, toRestore)
                                player.minigameAttributes.godwarsDungeonAttributes.setAltarDelay(System.currentTimeMillis())
                            }
                            23093 -> {
                                if (player.skillManager.getCurrentLevel(Skill.AGILITY) < 70) {
                                    player.packetSender.sendMessage("You need an Agility level of at least 70 to go through this portal.")
                                    return
                                }
                                if (!player.clickDelay.elapsed(2000)) return
                                val plrHeight = player.position.z
                                if (plrHeight == 2) player.moveTo(Position(2914, 5300, 1)) else if (plrHeight == 1) {
                                    if (gameObject.position.x == 2914 && gameObject.position.y == 5300) player.moveTo(
                                        Position(
                                            2912,
                                            5299,
                                            2
                                        )
                                    ) else if (gameObject.position.x == 2920 && gameObject.position.y == 5276) player.moveTo(
                                        Position(2920, 5274, 0)
                                    )
                                } else if (plrHeight == 0) player.moveTo(Position(2920, 5276, 1))
                                player.clickDelay.reset()
                            }
                            26439 -> {
                                if (player.skillManager.getMaxLevel(Skill.CONSTITUTION) <= 700 && !player.rights.isMember) {
                                    player.packetSender.sendMessage("You need a Constitution level of at least 70 to swim across, or be a member.")
                                    return
                                }
                                if (player.skillManager.getMaxLevel(Skill.CONSTITUTION) <= 700) {
                                    player.performGraphic(Graphic(6, GraphicHeight.LOW))
                                    player.packetSender.sendMessage("@red@You don't have 70 Constitution, but as a member you can cross anyway.")
                                }
                                if (!player.clickDelay.elapsed(1000)) return
                                if (player.isCrossingObstacle) return
                                val startMessage = "You jump into the icy cold water.."
                                val endMessage = "You climb out of the water safely."
                                val jumpGFX = 68
                                val jumpAnimation = 772
                                player.skillAnimation = 773
                                player.isCrossingObstacle = true
                                player.updateFlag.flag(Flag.APPEARANCE)
                                player.performAnimation(Animation(3067))
                                val goBack2 = player.position.y >= 5344
                                player.packetSender.sendMessage(startMessage)
                                player.moveTo(Position(2885, if (!goBack2) 5335 else 5342, 2))
                                player.direction = if (goBack2) Direction.SOUTH else Direction.NORTH
                                player.performGraphic(Graphic(jumpGFX))
                                player.performAnimation(Animation(jumpAnimation))
                                TaskManager.submit(object : Task(1, player, false) {
                                    var ticks = 0
                                    public override fun execute() {
                                        ticks++
                                        player.movementQueue.walkStep(0, if (goBack2) -1 else 1)
                                        if (ticks >= 10) stop()
                                    }

                                    override fun stop() {
                                        player.skillAnimation = -1
                                        player.isCrossingObstacle = false
                                        player.updateFlag.flag(Flag.APPEARANCE)
                                        player.packetSender.sendMessage(endMessage)
                                        player.moveTo(Position(2885, if (player.position.y < 5340) 5333 else 5345, 2))
                                        setEventRunning(false)
                                    }
                                })
                                player.clickDelay.reset(System.currentTimeMillis() + 9000)
                            }
                            26384 -> {
                                if (player.isCrossingObstacle) return
                                if (!player.inventory.contains(2347) && !player.rights.isMember) {
                                    player.packetSender.sendMessage("You need to have a hammer to bang on the door with.")
                                    return
                                }
                                if (!player.inventory.contains(2347) && player.rights.isMember) {
                                    player.packetSender.sendMessage("@red@You don't have a hammer, but as a member you can enter anyway.")
                                    player.performGraphic(Graphic(6, GraphicHeight.LOW))
                                }
                                if (player.rights.isMember) player.isCrossingObstacle = true
                                val goBack = player.position.x <= 2850
                                player.performAnimation(Animation(377))
                                TaskManager.submit(object : Task(2, player, false) {
                                    public override fun execute() {
                                        player.moveTo(Position(if (goBack) 2851 else 2850, 5333, 2))
                                        player.isCrossingObstacle = false
                                        stop()
                                    }
                                })
                            }
                            57211 -> player.packetSender.sendMessage("@red@Nobody is home. Please use the teleport under Modern Bosses to get to Nex.")
                            26303 -> {
                                if (!player.clickDelay.elapsed(1200)) return
                                if (player.skillManager.getCurrentLevel(Skill.RANGED) < 70 && !player.rights.isMember) player.packetSender.sendMessage(
                                    "You need a Ranged level of at least 70 to swing across here."
                                )
                                    .sendMessage("Or, you can get membership for $10 and pass without the requirement.") else if (!player.inventory.contains(
                                        9418
                                    ) && !player.rights.isMember
                                ) {
                                    player.packetSender.sendMessage("You need a Mithril grapple to swing across here. Explorer Jack might have one.")
                                        .sendMessage("Or, you can get membership for $10 and pass without the requirement.")
                                    return
                                } else {
                                    if (player.skillManager.getCurrentLevel(Skill.RANGED) < 70) {
                                        player.packetSender.sendMessage("@red@You don't have 70 Ranged, but as a member you can enter anyway.")
                                        player.performGraphic(Graphic(6, GraphicHeight.LOW))
                                    }
                                    if (!player.inventory.contains(9418)) {
                                        player.performGraphic(Graphic(6, GraphicHeight.LOW))
                                        player.packetSender.sendMessage("@red@You don't have a Mith grapple, but as a member you can enter anyway.")
                                    }
                                    player.performAnimation(Animation(789))
                                    TaskManager.submit(object : Task(2, player, false) {
                                        public override fun execute() {
                                            player.packetSender.sendMessage("You throw your Mithril grapple over the pillar and move across.")
                                            player.moveTo(
                                                Position(
                                                    2871,
                                                    if (player.position.y <= 5270) 5279 else 5269,
                                                    2
                                                )
                                            )
                                            stop()
                                        }
                                    })
                                    player.clickDelay.reset()
                                }
                            }
                            4493 -> if (player.position.x >= 3432) {
                                player.moveTo(Position(3433, 3538, 1))
                            }
                            4494 -> player.moveTo(Position(3438, 3538, 0))
                            4495 -> player.moveTo(Position(3417, 3541, 2))
                            4496 -> player.moveTo(Position(3412, 3541, 1))
                            2491 -> {
                                player.dialogueActionId = 48
                                DialogueManager.start(player, 87)
                            }
                            25339, 25340 -> player.moveTo(Position(1778, 5346, if (player.position.z == 0) 1 else 0))
                            10229, 10230 -> {
                                val up = id == 10229
                                player.performAnimation(Animation(if (up) 828 else 827))
                                player.packetSender.sendMessage("You climb " + (if (up) "up" else "down") + " the ladder..")
                                TaskManager.submit(object : Task(1, player, false) {
                                    override fun execute() {
                                        player.moveTo(if (up) Position(1912, 4367) else Position(2900, 4449))
                                        stop()
                                    }
                                })
                            }
                            1568 -> player.moveTo(Position(3097, 9868))
                            5103, 5104, 5105, 5106, 5107 -> {
                                if (!player.clickDelay.elapsed(4000)) return
                                if (player.skillManager.getCurrentLevel(Skill.WOODCUTTING) < 30) {
                                    player.packetSender.sendMessage("You need a Woodcutting level of at least 30 to do this.")
                                    return
                                }
                                if (getHatchet(player) < 0) {
                                    player.packetSender.sendMessage("You do not have a hatchet which you have the required Woodcutting level to use.")
                                    return
                                }
                                val axe = Hatchet.forId(getHatchet(player))
                                player.performAnimation(Animation(axe!!.anim))
                                gameObject.face = -1
                                TaskManager.submit(object : Task(3 + Misc.getRandom(4), player, false) {
                                    override fun execute() {
                                        if (player.movementQueue.isMoving) {
                                            stop()
                                            return
                                        }
                                        var xMovement = 0
                                        var yMovement = 0
                                        if (player.position.x == 2689 && player.position.y == 9564) {
                                            xMovement = 2
                                            yMovement = 0
                                        } else if (player.position.x == 2691 && player.position.y == 9564) {
                                            xMovement = -2
                                            yMovement = 0
                                        } else if (player.position.x == 2683 && player.position.y == 9568) {
                                            xMovement = 0
                                            yMovement = 2
                                        } else if (player.position.x == 2683 && player.position.y == 9570) {
                                            xMovement = 0
                                            yMovement = -2
                                        } else if (player.position.x == 2674 && player.position.y == 9479) {
                                            xMovement = 2
                                            yMovement = 0
                                        } else if (player.position.x == 2676 && player.position.y == 9479) {
                                            xMovement = -2
                                            yMovement = 0
                                        } else if (player.position.x == 2693 && player.position.y == 9482) {
                                            xMovement = 2
                                            yMovement = 0
                                        } else if (player.position.x == 2672 && player.position.y == 9499) {
                                            xMovement = 2
                                            yMovement = 0
                                        } else if (player.position.x == 2674 && player.position.y == 9499) {
                                            xMovement = -2
                                            yMovement = 0
                                        }
                                        CustomObjects.objectRespawnTask(
                                            player,
                                            GameObject(-1, gameObject.position.copy()),
                                            gameObject,
                                            10
                                        )
                                        player.packetSender.sendMessage("You chop down the vines..")
                                        player.skillManager.addExperience(Skill.WOODCUTTING, 45)
                                        player.performAnimation(Animation(65535))
                                        player.movementQueue.walkStep(xMovement, yMovement)
                                        stop()
                                    }
                                })
                                player.clickDelay.reset()
                            }
                            29942 -> {
                                if (player.skillManager.getCurrentLevel(Skill.SUMMONING) == player.skillManager.getMaxLevel(
                                        Skill.SUMMONING
                                    )
                                ) {
                                    player.packetSender.sendMessage("You do not need to recharge your Summoning points right now.")
                                    return
                                }
                                player.performGraphic(Graphic(1517))
                                player.skillManager.setCurrentLevel(
                                    Skill.SUMMONING,
                                    player.skillManager.getMaxLevel(Skill.SUMMONING),
                                    true
                                )
                                player.packetSender.sendString(
                                    18045,
                                    " " + player.skillManager.getCurrentLevel(Skill.SUMMONING) + "/" + player.skillManager.getMaxLevel(
                                        Skill.SUMMONING
                                    )
                                )
                                player.packetSender.sendMessage("You recharge your Summoning points.")
                            }
                            57225 -> if (!player.minigameAttributes.godwarsDungeonAttributes.hasEnteredRoom()) {
                                player.dialogueActionId = 44
                                DialogueManager.start(player, 79)
                            } else {
                                player.moveTo(Position(2906, 5204))
                                player.minigameAttributes.godwarsDungeonAttributes.setHasEnteredRoom(false)
                            }
                            26945 -> {
                                player.dialogueActionId = 41
                                DialogueManager.start(player, 75)
                            }
                            9294 -> {
                                if (player.skillManager.getCurrentLevel(Skill.AGILITY) < 80) {
                                    player.packetSender.sendMessage("You need an Agility level of at least 80 to use this shortcut.")
                                    return
                                }
                                player.performAnimation(Animation(769))
                                TaskManager.submit(object : Task(1, player, false) {
                                    override fun execute() {
                                        player.moveTo(Position(if (player.position.x >= 2880) 2878 else 2880, 9813))
                                        stop()
                                    }
                                })
                            }
                            9293 -> {
                                if (!player.rights.isMember && player.skillManager.getCurrentLevel(Skill.AGILITY) < 70) {
                                    player.packetSender.sendMessage("You must have at least 70 Agility to use this shortcut.")
                                    return
                                }
                                if (player.rights.isMember && player.skillManager.getCurrentLevel(Skill.AGILITY) < 70) {
                                    player.packetSender.sendMessage("You do not have 70 Agility, but as a member you can pass anyway.")
                                }
                                val back = player.position.x > 2888
                                player.moveTo(if (back) Position(2886, 9799) else Position(2891, 9799))
                            }
                            2320 -> {
                                val back = player.position.y == 9969 || player.position.y == 9970
                                player.moveTo(if (back) Position(3120, 9963) else Position(3120, 9969))
                            }
                            1755 -> {
                                player.performAnimation(Animation(828))
                                player.packetSender.sendMessage("You climb the ladder..")
                                TaskManager.submit(object : Task(1, player, false) {
                                    override fun execute() {
                                        if (gameObject.position.x == 2547 && gameObject.position.y == 9951) {
                                            player.moveTo(Position(2548, 3551))
                                        } else if (gameObject.position.x == 3005 && gameObject.position.y == 10363) {
                                            player.moveTo(Position(3005, 3962))
                                        } else if (gameObject.position.x == 3084 && gameObject.position.y == 9672) {
                                            player.moveTo(Position(3117, 3244))
                                        } else if (gameObject.position.x == 3097 && gameObject.position.y == 9867) {
                                            player.moveTo(Position(3096, 3468))
                                        } else if (gameObject.position.x == 3209 && gameObject.position.y == 9616) {
                                            player.moveTo(Position(3210, 3216))
                                        }
                                        stop()
                                    }
                                })
                            }
                            28742 -> {
                                player.performAnimation(Animation(827))
                                player.packetSender.sendMessage("You enter the trapdoor..")
                                TaskManager.submit(object : Task(1, player, false) {
                                    override fun execute() {
                                        player.moveTo(Position(3209, 9617))
                                        stop()
                                    }
                                })
                            }
                            5110 -> {
                                player.moveTo(Position(2647, 9557))
                                player.packetSender.sendMessage("You pass the stones..")
                            }
                            5111 -> {
                                player.moveTo(Position(2649, 9562))
                                player.packetSender.sendMessage("You pass the stones..")
                            }
                            6434 -> {
                                player.performAnimation(Animation(827))
                                player.packetSender.sendMessage("You enter the trapdoor..")
                                TaskManager.submit(object : Task(1, player, false) {
                                    override fun execute() {
                                        player.moveTo(Position(3085, 9672))
                                        stop()
                                    }
                                })
                            }
                            19187, 19175 -> dismantle(player, gameObject)
                            25029 -> goThroughWheat(player, gameObject)
                            47976 -> endFight(player, false)
                            2182 -> {
                                if (!player.minigameAttributes.recipeForDisasterAttributes.hasFinishedPart(0)) {
                                    player.packetSender.sendMessage("You have no business with this chest. Talk to the Gypsy first!")
                                    return
                                }
                                openRFDShop(player)
                            }
                            12356 -> {
                                if (!player.minigameAttributes.recipeForDisasterAttributes.hasFinishedPart(0)) {
                                    player.packetSender.sendMessage("You have no business with this portal. Talk to the Gypsy first!")
                                    return
                                }
                                if (player.position.z > 0) {
                                    leave(player)
                                } else {
                                    player.minigameAttributes.recipeForDisasterAttributes.setPartFinished(1, true)
                                    enter(player)
                                }
                            }
                            9369 -> if (player.position.y > 5175) {
                                addPlayer(player)
                            } else {
                                removePlayer(player, "leave room")
                            }
                            9368 -> if (player.position.y < 5169) {
                                removePlayer(player, "leave game")
                            }
                            9357 -> leaveCave(player, false)
                            9356 -> enterCave(player)
                            6704 -> player.moveTo(Position(3577, 3282, 0))
                            6706 -> player.moveTo(Position(3554, 3283, 0))
                            6705 -> player.moveTo(Position(3566, 3275, 0))
                            6702 -> player.moveTo(Position(3564, 3289, 0))
                            6703 -> player.moveTo(Position(3574, 3298, 0))
                            6707 -> player.moveTo(Position(3556, 3298, 0))
                            3203 -> if (player.location === Locations.Location.DUEL_ARENA && player.dueling.duelingStatus == 5) {
                                if (checkRule(player, DuelRule.NO_FORFEIT)) {
                                    player.packetSender.sendMessage("Forfeiting has been disabled in this duel.")
                                    return
                                }
                                player.combatBuilder.reset(true)
                                if (player.dueling.duelingWith > -1) {
                                    val duelEnemy =
                                        World.getPlayers()[player.dueling.duelingWith] ?: return
                                    duelEnemy.combatBuilder.reset(true)
                                    duelEnemy.movementQueue.reset()
                                    duelEnemy.dueling.duelVictory()
                                }
                                player.moveTo(Position(3368 + Misc.getRandom(5), 3267 + Misc.getRandom(3), 0))
                                player.dueling.reset()
                                player.combatBuilder.reset(true)
                                player.restart()
                            }
                            14315 -> boardBoat(player)
                            14314 -> if (player.location === Locations.Location.PEST_CONTROL_BOAT) {
                                player.location.leave(player)
                            }
                            2145 -> player.packetSender.sendMessage("There's no good reason to disturb that.")
                            1738 -> if (gameObject.position.x == 3204 && gameObject.position.y == 3207 && player.position.z == 0) {
                                player.moveTo(Position(player.position.x, player.position.y, 1))
                            } else if (player.location === Locations.Location.WARRIORS_GUILD) {
                                player.moveTo(Position(2840, 3539, 2))
                            }
                            1739 -> {
                                if (player.location === Locations.Location.LUMBRIDGE) {
                                    //player.moveTo(teleportTarget)
                                    //player.setDialogueActionId(154);
                                    //DialogueManager.start(player, 154);
                                    player.moveTo(Position(player.position.x, player.position.y, 2))
                                }
                                if (player.location === Locations.Location.WARRIORS_GUILD) {
                                    player.moveTo(Position(2840, 3539, 0))
                                }
                            }
                            15638 -> if (player.location === Locations.Location.WARRIORS_GUILD) {
                                player.moveTo(Position(2840, 3539, 0))
                            }
                            1740 -> if (player.location === Locations.Location.LUMBRIDGE) {
                                player.moveTo(Position(player.position.x, player.position.y, 1))
                            }
                            15644, 15641 -> when (player.position.z) {
                                0 -> player.moveTo(Position(2855, if (player.position.y >= 3546) 3545 else 3546))
                                2 -> if (player.position.x == 2846) {
                                    if (player.inventory.getAmount(8851) < 70) {
                                        player.packetSender.sendMessage("You need at least 70 tokens to enter this area.")
                                        return
                                    }
                                    DialogueManager.start(player, warriorsGuildDialogue(player))
                                    player.moveTo(Position(2847, player.position.y, 2))
                                    handleTokenRemoval(player)
                                } else if (player.position.x == 2847) {
                                    resetCyclopsCombat(player)
                                    player.moveTo(Position(2846, player.position.y, 2))
                                    player.minigameAttributes.warriorsGuildAttributes.setEnteredTokenRoom(false)
                                }
                            }
                            28714 -> {
                                player.performAnimation(Animation(828))
                                player.delayedMoveTo(Position(3088, 3495), 2)
                            }
                            26933 -> {
                                player.performAnimation(Animation(827))
                                player.delayedMoveTo(Position(3096, 9867), 2)
                            }
                            1746 -> {
                                player.performAnimation(Animation(827))
                                player.delayedMoveTo(Position(2209, 5348), 2)
                            }
                            19191, 19189, 19180, 19184, 19182, 19178 -> lootTrap(player, gameObject)
                            13493 -> {
                                if (!player.rights.isMember) {
                                    player.packetSender.sendMessage("You must be a Member to use this.")
                                    return
                                }
                                val c = Math.random() * 100
                                val reward =
                                    if (c >= 70) 13003 else if (c >= 45) 4131 else if (c >= 35) 1113 else if (c >= 25) 1147 else if (c >= 18) 1163 else if (c >= 12) 1079 else if (c >= 5) 1201 else 1127
                                stealFromStall(player, 95, 121, reward, "You stole some rune equipment.")
                            }
                            30205 -> {
                                player.dialogueActionId = 11
                                DialogueManager.start(player, 20)
                            }
                            28716 -> if (!player.busy()) {
                                player.skillManager.updateSkill(Skill.SUMMONING)
                                player.packetSender.sendInterface(63471)
                            } else player.packetSender.sendMessage("Please finish what you're doing before opening this.")
                            6 -> {
                                val cannon = player.cannon
                                if (cannon == null || cannon.ownerIndex != player.index) {
                                    player.packetSender.sendMessage("This is not your cannon!")
                                } else {
                                    DwarfMultiCannon.startFiringCannon(player, cannon)
                                }
                            }
                            2 -> {
                                player.moveTo(Position(if (player.position.x > 2690) 2687 else 2694, 3714))
                                player.packetSender.sendMessage("You walk through the entrance..")
                            }
                            2026, 2028, 2029, 2030, 2031 -> {
                                player.setEntityInteraction(gameObject)
                                setupFishing(player, forSpot(gameObject.id, false))
                                return
                            }
                            12692, 2783, 4306 -> {
                                player.interactingObject = gameObject
                                handleAnvil(player)
                            }
                            2732, 11404, 11406, 11405, 20000, 20001 -> EnterAmountOfLogsToAdd.openInterface(player)
                            409, 27661, 2640, 36972 -> {
                                player.performAnimation(Animation(645))
                                if (player.skillManager.getCurrentLevel(Skill.PRAYER) < player.skillManager.getMaxLevel(
                                        Skill.PRAYER
                                    )
                                ) {
                                    player.skillManager.setCurrentLevel(
                                        Skill.PRAYER,
                                        player.skillManager.getMaxLevel(Skill.PRAYER),
                                        true
                                    )
                                    player.packetSender.sendMessage("You recharge your Prayer points.")
                                }
                            }
                            8749 -> {
                                val restore = player.specialPercentage < 100
                                if (restore) {
                                    player.specialPercentage = 100
                                    updateBar(player)
                                    player.packetSender.sendMessage("Your special attack energy has been restored.")
                                }
                                for (skill in Skill.values()) {
                                    val increase =
                                        if (skill != Skill.PRAYER && skill != Skill.CONSTITUTION && skill != Skill.SUMMONING) 19 else 0
                                    if (player.skillManager.getCurrentLevel(skill) < player.skillManager.getMaxLevel(
                                            skill
                                        ) + increase
                                    ) player.skillManager.setCurrentLevel(
                                        skill,
                                        player.skillManager.getMaxLevel(skill) + increase
                                    )
                                }
                                player.performGraphic(Graphic(1302))
                                player.packetSender.sendMessage("Your stats have received a major buff.")
                            }
                            4859 -> {
                                player.performAnimation(Animation(645))
                                if (player.skillManager.getCurrentLevel(Skill.PRAYER) < player.skillManager.getMaxLevel(
                                        Skill.PRAYER
                                    )
                                ) {
                                    player.skillManager.setCurrentLevel(
                                        Skill.PRAYER,
                                        player.skillManager.getMaxLevel(Skill.PRAYER),
                                        true
                                    )
                                    player.packetSender.sendMessage("You recharge your Prayer points.")
                                }
                            }
                            411 -> {
                                if (player.skillManager.getMaxLevel(Skill.DEFENCE) < 30) {
                                    player.packetSender.sendMessage("You need a Defence level of at least 30 to use this altar.")
                                    return
                                }
                                player.performAnimation(Animation(645))
                                if (player.prayerbook == Prayerbook.NORMAL) {
                                    player.packetSender.sendMessage("You sense a surge of power flow through your body!")
                                    player.prayerbook = Prayerbook.CURSES
                                } else {
                                    player.packetSender.sendMessage("You sense a surge of purity flow through your body!")
                                    player.prayerbook = Prayerbook.NORMAL
                                }
                                player.packetSender.sendTabInterface(
                                    GameSettings.PRAYER_TAB,
                                    player.prayerbook.interfaceId
                                )
                                PrayerHandler.deactivateAll(player)
                                CurseHandler.deactivateAll(player)
                            }
                            6552 -> {
                                player.performAnimation(Animation(645))
                                player.spellbook =
                                    if (player.spellbook == MagicSpellbook.ANCIENT) MagicSpellbook.NORMAL else MagicSpellbook.ANCIENT
                                player.packetSender.sendTabInterface(
                                    GameSettings.MAGIC_TAB,
                                    player.spellbook.interfaceId
                                )
                                    .sendMessage("Your magic spellbook is changed..")
                                Autocasting.resetAutocast(player, true)
                            }
                            410 -> {
                                if (player.skillManager.getMaxLevel(Skill.DEFENCE) < 40) {
                                    player.packetSender.sendMessage("You need a Defence level of at least 40 to use this altar.")
                                    return
                                }
                                player.performAnimation(Animation(645))
                                player.spellbook =
                                    if (player.spellbook == MagicSpellbook.LUNAR) MagicSpellbook.NORMAL else MagicSpellbook.LUNAR
                                player.packetSender.sendTabInterface(
                                    GameSettings.MAGIC_TAB,
                                    player.spellbook.interfaceId
                                )
                                    .sendMessage("Your magic spellbook is changed..")
                                Autocasting.resetAutocast(player, true)
                            }
                            452 -> player.packetSender.sendMessage("There's no ore in that rock.")
                            172 -> CrystalChest.handleChest(player, gameObject, false)
                            6910, 4483, 3193, 2213, 11758, 14367, 42192, 75, 26972, 11338, 19230 -> player.getBank(
                                player.currentBankTab
                            )
                                .open()
                            11666 -> openInterface(player)
                        }
                    }
                })
        }

        private fun secondClick(player: Player, packet: Packet) {
            val id = packet.readInt()
            val y = packet.readLEShort().toInt()
            val x = packet.readUnsignedShortA()
            val position = Position(x, y, player.position.z)
            val gameObject = GameObject(id, position)
            if (id > 0 && id != 6 && !RegionClipping.objectExists(gameObject)) {
                //player.getPacketSender().sendMessage("An error occured. Error code: "+id).sendMessage("Please report the error to a staff member.");
                return
            }
            player.positionToFace = gameObject.position
            var distanceX = player.position.x - position.x
            var distanceY = player.position.y - position.y
            if (distanceX < 0) distanceX = -distanceX
            if (distanceY < 0) distanceY = -distanceY
            val size = if (distanceX > distanceY) distanceX else distanceY
            gameObject.setSize(size)
            if (player.rights == PlayerRights.DEVELOPER) player.packetSender.sendMessage("Second click object id; [id, position] : [$id, $position]")
            player.setInteractingObject(gameObject).walkToTask =
                WalkToTask(player, position, gameObject.getSize(), object : FinalizedMovementTask {
                    override fun execute() {
                        if (forRock(gameObject.id) != null) {
                            prospectOre(player, id)
                            return
                        }
                        if (player.farming.click(player, x, y, 1)) return
                        when (gameObject.id) {
                            2145 -> player.packetSender.sendMessage("Eww. That's a terrible idea!")
                            1739 -> if (player.location === Locations.Location.LUMBRIDGE) {
                                player.moveTo(Position(player.position.x, player.position.y, 0))
                            }
                            6910, 4483, 3193, 2213, 11758, 14367, 42192, 75, 26972, 11338, 19230 -> player.getBank(
                                player.currentBankTab
                            )
                                .open()
                            26945 -> {
                                player.dialogueActionId = 41
                                player.inputHandling = DonateToWell()
                                player.packetSender.sendInterfaceRemoval()
                                    .sendEnterAmountPrompt("How much money would you like to contribute with?")
                            }
                            2646, 312 -> {
                                if (!player.clickDelay.elapsed(1200)) return
                                if (player.inventory.isFull) {
                                    player.packetSender.sendMessage("You don't have enough free inventory space.")
                                    return
                                }
                                val type = if (gameObject.id == 312) "Potato" else "Flax"
                                player.performAnimation(Animation(827))
                                player.inventory.add(if (gameObject.id == 312) 1942 else 1779, 1)
                                player.packetSender.sendMessage("You pick some $type..")
                                gameObject.pickAmount = gameObject.pickAmount + 1
                                if (Misc.getRandom(3) == 1 && gameObject.pickAmount >= 1 || gameObject.pickAmount >= 6) {
                                    player.packetSender.sendClientRightClickRemoval()
                                    gameObject.pickAmount = 0
                                    CustomObjects.globalObjectRespawnTask(
                                        GameObject(
                                            -1,
                                            gameObject.position
                                        ), gameObject, 10
                                    )
                                }
                                player.clickDelay.reset()
                            }
                            2644 -> showSpinInterface(player)
                            6 -> {
                                val cannon = player.cannon
                                if (cannon == null || cannon.ownerIndex != player.index) {
                                    player.packetSender.sendMessage("This is not your cannon!")
                                } else {
                                    DwarfMultiCannon.pickupCannon(player, cannon, false)
                                }
                            }
                            5917 -> stealFromStall(
                                player,
                                1,
                                0,
                                13150,
                                "You search the Plasma Vent... and find a Spooky Box!"
                            )
                            4875 -> stealFromStall(player, 1, 13, 18199, "You steal a banana.")
                            4874 -> stealFromStall(player, 30, 34, 15009, "You steal a golden ring.")
                            4876 -> stealFromStall(player, 60, 57, 17401, "You steal a damaged hammer.")
                            4877 -> stealFromStall(player, 65, 80, 1389, "You steal a staff.")
                            4878 -> stealFromStall(player, 80, 101, 11998, "You steal a scimitar.")
                            3044, 6189, 26814, 11666 -> jewelryInterface(player)
                            2152 -> {
                                player.performAnimation(Animation(8502))
                                player.performGraphic(Graphic(1308))
                                player.skillManager.setCurrentLevel(
                                    Skill.SUMMONING,
                                    player.skillManager.getMaxLevel(Skill.SUMMONING)
                                )
                                player.packetSender.sendMessage("You renew your Summoning points.")
                            }
                        }
                    }
                })
        }

        private fun thirdClick(player: Player, packet: Packet) {}
        private fun fourthClick(player: Player, packet: Packet) {}
        private fun fifthClick(player: Player, packet: Packet) {
            val id = packet.readInt()
            val y = packet.readUnsignedShortA()
            val x = packet.readShort().toInt()
            val position = Position(x, y, player.position.z)
            val gameObject = GameObject(id, position)
            if (!Construction.buildingHouse(player)) {
                if (id > 0 && !RegionClipping.objectExists(gameObject)) {
                    //player.getPacketSender().sendMessage("An error occured. Error code: "+id).sendMessage("Please report the error to a staff member.");
                    return
                }
            }
            player.positionToFace = gameObject.position
            var distanceX = player.position.x - position.x
            var distanceY = player.position.y - position.y
            if (distanceX < 0) distanceX = -distanceX
            if (distanceY < 0) distanceY = -distanceY
            val size = if (distanceX > distanceY) distanceX else distanceY
            gameObject.setSize(size)
            if (player.rights == PlayerRights.DEVELOPER) {
                player.packetSender.sendMessage("Third click object id; [id, position] : [$id, $position]")
            }
            player.interactingObject = gameObject
//            player.walkToTask = WalkToTask(player, position, gameObject.getSize()) {
//                Construction.handleFifthObjectClick(x, y, id, player)
//            }
        }

        const val FIRST_CLICK = 132
        const val SECOND_CLICK = 252
        const val THIRD_CLICK = 70
        const val FOURTH_CLICK = 234
        const val FIFTH_CLICK = 228
    }
}