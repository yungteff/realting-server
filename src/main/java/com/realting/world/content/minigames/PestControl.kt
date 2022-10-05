package com.realting.world.content.minigames

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.world.World
import com.realting.world.content.dialogue.DialogueManager
import com.realting.world.content.PlayerPanel
import com.realting.model.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.Locale
import java.util.HashMap
import com.realting.model.movement.MovementQueue

import com.realting.model.definitions.ItemDefinition
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.model.input.impl.EnterAmountToSellToShop
import com.realting.model.input.impl.EnterAmountToBuyFromShop
import com.realting.model.movement.PathFinder
import com.realting.util.Misc

/**
 * Pest control minigame
 * @author Gabriel Hannason
 */
class PestControl(val id: Int) {

    enum class PestControlNPC(//BRAWLER(3772, 3776);
        val lowestNPCID: Int, val highestNPCID: Int
    ) {
        SPINNER(3747, 3751),  //SPLATTER(3727, 3731),
        SHIFTER(3732, 3741), TORCHER(3752, 3761), DEFILER(3762, 3771);

        var tries = 0
    }

    companion object {
        @JvmField
        var TOTAL_PLAYERS = 0
        private var PLAYERS_IN_BOAT = 0

        /**
         * @note Stores player and State
         */
        private val playerMap: MutableMap<Player, String> = HashMap()

        /*
	 * Stores npcs
	 */
        private val npcList = CopyOnWriteArrayList<NPC?>()

        /**
         * @return HashMap Value
         */
        fun getState(player: Player): String? {
            return playerMap[player]
        }

        /**
         * @note States of minigames
         */
        const val PLAYING = "PLAYING"
        const val WAITING = "WAITING"

        /**
         * Is a game running?
         */
        private var gameRunning = false

        /**
         * Moves a player in to the boat (waiting area)
         * and adds the player to the map.
         * @param p            The player entering
         */
        @JvmStatic
        fun boardBoat(p: Player) {
            if (p.summoning.familiar != null) {
                p.packetSender.sendMessage("Familiars are not allowed on the boat.")
                return
            }
            if (p.skillManager.combatLevel < 30) {
                p.packetSender.sendMessage("You must have a combat level of at least 30 to play this minigame.")
                return
            }
            if (getState(p) == null) {
                playerMap[p] = WAITING
                TOTAL_PLAYERS++
                PLAYERS_IN_BOAT++
            }
            p.session.clearMessages()
            p.moveTo(Position(2661, 2639, 0))
            p.packetSender.sendString(21117, "")
            p.packetSender.sendString(21118, "")
            p.packetSender.sendString(21008, "(Need 2 to 25 players)")
            p.movementQueue.setLockMovement(false).reset()
        }

        /**
         * Moves the player out of the boat (waiting area)
         * and removes the player from the map.
         * @param p            The player leaving
         */
        @JvmStatic
        fun leave(p: Player, fromList: Boolean) {
            val state = getState(p)
            if (state != null) {
                if (fromList) {
                    playerMap.remove(p)
                }
                TOTAL_PLAYERS--
                if (state === WAITING) {
                    PLAYERS_IN_BOAT--
                }
            }
            p.packetSender.sendInterfaceRemoval()
            p.session.clearMessages()
            p.moveTo(Position(2657, 2639, 0))
            p.movementQueue.setLockMovement(false).reset()
        }

        /**
         * Handles the static process required.
         */
        @JvmStatic
        fun sequence() {
            if (TOTAL_PLAYERS == 0 && !gameRunning) return
            updateBoatInterface()
            if (waitTimer > 0) waitTimer--
            if (waitTimer <= 0) {
                if (!gameRunning) startGame() else {
                    for (p in playerMap.keys) {
                        if (p == null) continue
                        val state = getState(p)
                        if (state != null && state == WAITING) {
                            p.packetSender.sendMessage("A new Pest control game will be started once the current one has finished.")
                        }
                    }
                }
                waitTimer = WAIT_TIMER
            }
            if (gameRunning) {
                updateIngameInterface()
                if (Math.random() < 0.1) spawnRandomNPC()
                processNPCs()
                if (knight == null || knight != null && knight!!.constitution <= 0) {
                    endGame(false)
                    waitTimer = WAIT_TIMER
                } else if (allPortalsDead()) {
                    endGame(true)
                    waitTimer = WAIT_TIMER
                }
            }
        }

        var KNIGHT_CHAT = arrayOf(
            "We must not fail!",
            "Take down the portals",
            "The Void Knights will not fall!",
            "Hail the Void Knights!",
            "We are beating these scum!"
        )

        /**
         * Updates the boat (waiting area) interface for every player in it.
         */
        private fun updateBoatInterface() {
            for (p in playerMap.keys) {
                if (p == null) continue
                val state = getState(p)
                if (state != null && state == WAITING) {
                    p.packetSender.sendString(21006, "Next Departure: " + waitTimer + "")
                    p.packetSender.sendString(21007, "Players Ready: " + PLAYERS_IN_BOAT + "")
                    p.packetSender.sendString(21009, "Commendations: " + p.pointsHandler.commendations)
                }
            }
        }

        /**
         * Updates the game interface for every player.
         */
        private fun updateIngameInterface() {
            for (p in playerMap.keys) {
                if (p == null) continue
                val state = getState(p)
                if (state != null && state == PLAYING) {
                    p.packetSender.sendString(21111, getPortalText(0))
                    p.packetSender.sendString(21112, getPortalText(1))
                    p.packetSender.sendString(21113, getPortalText(2))
                    p.packetSender.sendString(21114, getPortalText(3))
                    var prefix =
                        if (knight!!.constitution < 500) "@red@" else if (knight!!.constitution < 800) "@yel@" else "@gre@"
                    p.packetSender.sendString(
                        21115,
                        if (knight != null && knight!!.constitution > 0) prefix + "Knight's health: " + knight!!.constitution else "Dead"
                    )
                    prefix =
                        if (p.minigameAttributes.pestControlAttributes.damageDealt == 0) "@red@" else if (p.minigameAttributes.pestControlAttributes.damageDealt < 100) "@yel@" else "@gre@"
                    p.packetSender.sendString(
                        21116,
                        prefix + "Your damage : " + p.minigameAttributes.pestControlAttributes.damageDealt + "/100"
                    )
                }
            }
        }

        /**
         * Starts a game and moves players in to the game.
         */
        private fun startGame() {
            val startGame = !gameRunning && PLAYERS_IN_BOAT >= 2
            if (startGame) {
                gameRunning = true
                spawnMainNPCs()
            }
            for (player in playerMap.keys) {
                if (player != null) {
                    val state = getState(player)
                    if (state != null && state == WAITING) {
                        if (startGame) {
                            movePlayerToIsland(player)
                            playerMap[player] = PLAYING
                        } else player.packetSender.sendMessage("There must be at least 3 players in the boat before a game can start.")
                    }
                }
            }
        }

        /**
         * Teleports the player in to the game
         */
        private fun movePlayerToIsland(p: Player) {
            p.packetSender.sendInterfaceRemoval()
            p.session.clearMessages()
            p.moveTo(Position(2658, 2611, 0))
            p.movementQueue.setLockMovement(false).reset()
            DialogueManager.start(p, 26)
            PLAYERS_IN_BOAT--
        }

        /**
         * Ends a game and rewards players.
         * @param won    Did the players manage to win the game?
         */
        private fun endGame(won: Boolean) {
            val it = playerMap.keys.iterator()
            while (it.hasNext()) {
                val p = it.next() ?: continue
                val state = getState(p)
                if (state != null && state == PLAYING) {
                    leave(p, false)
                    if (won && p.minigameAttributes.pestControlAttributes.damageDealt >= 50) {
                        p.packetSender.sendMessage("The portals were successfully closed. You've been rewarded for your effort.")
                        p.packetSender.sendMessage("You've received 15 Commendations and " + p.skillManager.combatLevel * 50 + " coins.")
                        p.pointsHandler.setCommendations(15, true)
                        PlayerPanel.refreshPanel(p)
                        p.inventory.add(995, p.skillManager.combatLevel * 80)
                        p.restart()
                    } else if (won) p.packetSender.sendMessage("You didn't participate enough to receive a reward.") else {
                        p.packetSender.sendMessage("You failed to kill all the portals in time.")
                        DialogueManager.start(p, 356)
                    }
                    p.minigameAttributes.pestControlAttributes.damageDealt = 0
                }
                it.remove()
            }
            playerMap.clear()
            PLAYERS_IN_BOAT = 0
            for (p in World.getPlayers()) {
                if (p != null && p.location === Locations.Location.PEST_CONTROL_BOAT) {
                    playerMap[p] = WAITING
                    PLAYERS_IN_BOAT++
                }
            }
            for (n in npcList) {
                if (n == null || !n.isRegistered) continue
                if (n.location === Locations.Location.PEST_CONTROL_GAME) {
                    World.deregister(n)
                    //TODO:: whats this?
//                    n = null
                }
            }
            npcList.clear()
            for (i in portals.indices) portals[i] = null
            knight = null
            gameRunning = false
        }
        /*==========================================================================================================*/ /*NPC STUFF*/
        /**
         * Spawns the game's key/main NPC's on to the map
         */
        private fun spawnMainNPCs() {
            val knightHealth = 3000 - PLAYERS_IN_BOAT * 14
            val portalHealth = defaultPortalConstitution
            knight = spawnPCNPC(3782, Position(2656, 2592), knightHealth) //knight
            portals[0] = spawnPCNPC(6142, Position(2628, 2591), portalHealth) //purple
            portals[1] = spawnPCNPC(6143, Position(2680, 2588), portalHealth) //red
            portals[2] = spawnPCNPC(6144, Position(2669, 2570), portalHealth) //blue
            portals[3] = spawnPCNPC(6145, Position(2645, 2569), portalHealth) //yellow
            npcList.add(knight)
            for (n in portals) {
                npcList.add(n)
            }
        }

        val defaultPortalConstitution: Int
            get() = 1600 + PLAYERS_IN_BOAT * 190

        /**
         * Gets the text which shall be sent on to a player's interface
         * @param i        The portal index to get information about
         * @return        Information about the portal with the index specified
         */
        private fun getPortalText(i: Int): String {
            return if (portals[i] != null && portals[i]!!.constitution > 0 && portals[i]!!.constitution > 0) Integer.toString(
                portals[i]!!.constitution
            ) else "Dead"
        }

        /**
         * Checks if all portals are dead (if true, the game will end and the players will win)
         * @return        true if all portals are dead, otherwise false
         */
        private fun allPortalsDead(): Boolean {
            var count = 0
            for (i in portals.indices) {
                if (portals[i] != null) {
                    if (portals[i]!!.constitution <= 0 || portals[i]!!.constitution <= 0) {
                        count++
                    }
                }
            }
            return count >= 4
        }

        /**
         * Processes all NPC's within Pest control
         */
        private fun processNPCs() {
            for (npc in npcList) {
                if (npc == null) continue
                if (npc.location === Locations.Location.PEST_CONTROL_GAME && npc.constitution > 0) {
                    for (PCNPC in PestControlNPC.values()) {
                        if (npc.id >= PCNPC.lowestNPCID && npc.id <= PCNPC.highestNPCID) {
                            processPCNPC(npc, PCNPC)
                            break
                        }
                    }
                }
            }
            if (knight != null && knight!!.constitution > 0 && Misc.getRandom(10) == 4) {
                knight!!.forceChat(KNIGHT_CHAT[Misc.getRandom(KNIGHT_CHAT.size - 1)])
            }
        }

        /**
         * Spawns a random NPC onto the map
         */
        private fun spawnRandomNPC() {
            for (i in portals.indices) {
                if (portals[i] != null && Math.random() > 0.5) {
                    val luckiest = PestControlNPC.values()[(Math.random() * PestControlNPC.values().size).toInt()]
                    if (luckiest != null) {
                        npcList.add(
                            spawnPCNPC(
                                luckiest.lowestNPCID + (Math.random() * (luckiest.highestNPCID - luckiest.lowestNPCID)).toInt(),
                                Position(
                                    portals[i]!!.position.x, portals[i]!!.position.y - 1, 0
                                ),
                                400
                            )
                        )
                    }
                }
            }
        }

        private fun processPCNPC(npc: NPC?, _npc: PestControlNPC?) {
            if (knight == null || npc == null || _npc == null) return
            when (_npc) {
                PestControlNPC.SPINNER -> processSpinner(npc)
                PestControlNPC.SHIFTER -> processShifter(npc, _npc)
                PestControlNPC.TORCHER -> processDefiler(npc, _npc)
                PestControlNPC.DEFILER -> processDefiler(npc, _npc)
            }
        }

        /**
         * Processes the spinner NPC
         * Finds the closest portal, walks to it and heals it if injured.
         * @param npc    The Spinner NPC
         */
        private fun processSpinner(npc: NPC) {
            var closestPortal: NPC? = null
            var distance = Int.MAX_VALUE
            for (i in portals.indices) {
                if (portals[i] != null && portals[i]!!.constitution > 0 && portals[i]!!.constitution > 0) {
                    val distanceCandidate = distance(
                        npc.position.x, npc.position.y, portals[i]!!.position.x, portals[i]!!.position.y
                    )
                    if (distanceCandidate < distance) {
                        closestPortal = portals[i]
                        distance = distanceCandidate
                    }
                }
            }
            if (closestPortal == null) return
            npc.setEntityInteraction(closestPortal)
            if (distance <= 3 && closestPortal.constitution < defaultPortalConstitution) {
                npc.performAnimation(Animation(3911))
                closestPortal.constitution = closestPortal.constitution + 2
                if (closestPortal.constitution > defaultPortalConstitution) closestPortal.constitution =
                    defaultPortalConstitution
            } else if (closestPortal != null) {
                PathFinder.findPath(npc, closestPortal.position.x, closestPortal.position.y - 1, true, 1, 1)
                return
            }
        }

        private fun processShifter(npc: NPC?, npc_: PestControlNPC) {
            if (npc != null && knight != null) {
                if (isFree(npc, npc_)) {
                    if (distance(npc.position.x, npc.position.y, knight!!.position.x, knight!!.position.y) > 5) {
                        val npcId = npc.id
                        val pos = Position(
                            knight!!.position.x + Misc.getRandom(3),
                            knight!!.position.y + Misc.getRandom(2),
                            npc.position.z
                        )
                        World.deregister(npc)
                        npcList.remove(npc)
                        npcList.add(spawnPCNPC(npcId, pos, 200))
                    } else {
                        if (distance(npc.position.x, npc.position.y, knight!!.position.x, knight!!.position.y) > 1) {
                            PathFinder.findPath(npc, knight!!.position.x, knight!!.position.y - 1, true, 1, 1)
                        } else {
                            npc.combatBuilder.reset(true)
                            val max = 5 + npc.definition.combatLevel / 9
                            attack(npc, knight, 3901, max, CombatIcon.MELEE)
                        }
                    }
                }
                if (npc.position.copy() == knight!!.position.copy()) MovementQueue.stepAway(npc)
            }
        }

        private fun processDefiler(npc: NPC?, npc_: PestControlNPC) {
            if (npc != null) {
                if (isFree(npc, npc_)) {
                    if (distance(npc.position.x, npc.position.y, knight!!.position.x, knight!!.position.y) > 5) {
                        PathFinder.findPath(npc, knight!!.position.x, knight!!.position.y - 1, true, 1, 1)
                    } else {
                        if (Math.random() <= 0.04) for (p in playerMap.keys) {
                            if (p != null) {
                                val state = getState(p)
                                if (state == PLAYING) Projectile(npc, knight, 1508, 80, 3, 43, 31, 0).sendProjectile()
                            }
                        }
                        TaskManager.submit(object : Task(1) {
                            public override fun execute() {
                                val max = 7 + npc.definition.combatLevel / 9
                                attack(
                                    npc,
                                    knight,
                                    if (npc_ == PestControlNPC.DEFILER) 3920 else 3882,
                                    max,
                                    if (npc_ == PestControlNPC.DEFILER) CombatIcon.RANGED else CombatIcon.MAGIC
                                )
                                stop()
                            }
                        })
                    }
                }
            }
        }

        private fun attack(npc: NPC?, knight: NPC?, anim: Int, maxhit: Int, icon: CombatIcon): Boolean {
            if (knight == null || npc == null) return false
            npc.setEntityInteraction(knight)
            npc.positionToFace = knight.position
            if (npc.combatBuilder.attackTimer == 0) {
                val damage = (Math.random() * maxhit).toInt()
                npc.performAnimation(Animation(anim))
                knight.dealDamage(Hit(npc, damage, Hitmask.RED, icon))
                knight.lastCombat.reset()
                npc.combatBuilder.attackTimer = 3 + Misc.getRandom(3)
                npc.lastCombat.reset()
                return true
            }
            return false
        }

        private fun distance(x: Int, y: Int, dx: Int, dy: Int): Int {
            val xdiff = x - dx
            val ydiff = y - dy
            return Math.sqrt((xdiff * xdiff + ydiff * ydiff).toDouble()).toInt()
        }

        private fun isFree(npc: NPC, npc_: PestControlNPC): Boolean {
            return if (!npc.combatBuilder.isAttacking) {
                true
            } else {
                if (npc_.tries++ >= 12) {
                    npc_.tries = 0
                    npc.combatBuilder.reset(true)
                    true
                } else {
                    false
                }
            }
        }

        var runningGames = arrayOfNulls<PestControl>(1)
        const val WAIT_TIMER = 20
        var waitTimer = WAIT_TIMER
        private val portals = arrayOfNulls<NPC>(4)
        var knight: NPC? = null

        /**
         * Handles the shop
         * @param p            The player buying something from the shop
         * @param item        The item which the player is buying
         * @param id        The id of the item/skill which the player is buying
         * @param amount    The amount of the item/skill xp which the player is buying
         * @param cost        The amount it costs to buy this item
         */
        fun buyFromShop(p: Player, item: Boolean, id: Int, amount: Int, cost: Int) {
            if (p.pointsHandler.commendations < cost && p.rights != PlayerRights.DEVELOPER) {
                p.packetSender.sendMessage("You don't have enough Commendations to purchase this.")
                return
            }
            if (!p.clickDelay.elapsed(500)) return
            var name = ItemDefinition.forId(id).name
            val comm = if (cost > 1) "Commendations" else "Commendation"
            if (!item) {
                p.pointsHandler.setCommendations(p.pointsHandler.commendations - cost, false)
                val skill = Skill.forId(id)
                val xp = amount * cost
                p.skillManager.addExperience(skill, xp)
                p.packetSender.sendMessage(
                    "You have purchased " + xp + " " + Misc.formatText(
                        skill.toString().lowercase(Locale.getDefault())
                    ) + " XP."
                )
            } else {
                if (p.inventory.freeSlots == 0) {
                    p.inventory.full()
                    return
                }
                var id2 = 0
                if (id > 19784 && id < 19787) {
                    if (id == 19785) id2 = 8839 else if (id == 19786) id2 = 8840
                    if (p.inventory.contains(id2)) {
                        p.inventory.delete(id2, 1)
                    } else {
                        name = ItemDefinition.forId(id2).name
                        p.packetSender.sendMessage("You need to have " + Misc.anOrA(name) + " " + name + " to purchase this uppgrade.")
                        return
                    }
                }
                p.pointsHandler.setCommendations(p.pointsHandler.commendations - cost, false)
                p.inventory.add(id, amount)
                PlayerPanel.refreshPanel(p)
                p.packetSender.sendMessage("You have purchased " + Misc.anOrA(name) + " " + name + " for " + cost + " " + comm + ".")
            }
            p.packetSender.sendString(18729, "Commendations: " + Integer.toString(p.pointsHandler.commendations))
            p.clickDelay.reset()
        }

        @JvmStatic
        fun handleInterface(player: Player, id: Int): Boolean {
            if (player.interfaceId == 18730 || player.interfaceId == 18746) {
                when (id) {
                    18733 -> {
                        buyFromShop(player, true, 11665, 1, 200)
                        return true //melee helm
                    }
                    18735 -> {
                        buyFromShop(player, true, 11664, 1, 200)
                        return true //ranger helm
                    }
                    18741 -> {
                        buyFromShop(player, true, 11663, 1, 200)
                        return true //mage helm
                    }
                    18734 -> {
                        buyFromShop(player, true, 8839, 1, 250)
                        return true //top
                    }
                    18737 -> {
                        buyFromShop(player, true, 8840, 1, 250)
                        return true //robes
                    }
                    18742 -> {
                        buyFromShop(player, true, 8842, 1, 150)
                        return true //gloves
                    }
                    18740 -> {
                        buyFromShop(player, true, 19712, 1, 350)
                        return true //deflector
                    }
                    18745 -> {
                        buyFromShop(player, true, 19780, 1, 2000)
                        return true //korasi
                    }
                    18749 -> {
                        buyFromShop(player, true, 19785, 1, 125)
                        return true //elite top
                    }
                    18750 -> {
                        buyFromShop(player, true, 19786, 1, 125)
                        return true //elite legs
                    }
                    18743 -> {
                        player.packetSender.sendInterface(18746)
                        return true
                    }
                    18748 -> {
                        player.packetSender.sendInterface(18730)
                        return true
                    }
                    18728 -> {
                        player.packetSender.sendInterfaceRemoval()
                        return true
                    }
                }
            }
            return false
        }

        fun spawnPCNPC(id: Int, pos: Position?, constitution: Int): NPC {
            val np = NPC(id, pos)
            np.constitution = constitution
            np.defaultConstitution = constitution
            World.register(np)
            return np
        }
    }
}