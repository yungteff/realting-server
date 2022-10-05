package com.realting.model.movement

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.Direction
import com.realting.model.Locations
import com.realting.model.Position
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.model.movement.PathFinder.findPath
import com.realting.world.clip.region.RegionClipping
import com.realting.world.content.EnergyHandler
import com.realting.world.content.combat.CombatFactory.Companion.checkAttackDistance
import java.util.*

/**
 * A queue of [Direction]s which a [CharacterEntity] will follow.
 *
 * @author Graham Edgecombe
 * Edited by Gabbe
 */
class MovementQueue(
    /**
     * The character whose walking queue this is.
     */
    private val character: CharacterEntity
) {
    /**
     * Represents a single point in the queue.
     *
     * @author Graham Edgecombe
     */
    private class Point
    /**
     * Creates a point.
     *
     * @param position  The position.
     * @param direction The direction.
     */(
        /**
         * The point's position.
         */
        val position: Position,
        /**
         * The direction to walk to this point.
         */
        val direction: Direction
    ) {
        override fun toString(): String {
            return (Point::class.java.name + " [direction=" + direction
                    + ", position=" + position + "]")
        }
    }

    /**
     * The queue of directions.
     */
    private val points: Deque<Point> = ArrayDeque()

    /**
     * The following task
     */
    private var followTask: Task? = null
    internal var followCharacter: CharacterEntity? = null
    private val isPlayer: Boolean = character.isPlayer

    /**
     * Sets a character to follow
     */
    fun setFollowCharacter(followCharacter: CharacterEntity?) {
        this.followCharacter = followCharacter
        startFollow()
    }

    fun getFollowCharacter(): CharacterEntity? {
        return followCharacter
    }

    /**
     * Adds the first step to the queue, attempting to connect the server and
     * client position by looking at the previous queue.
     *
     * @param clientConnectionPosition The first step.
     * @return `true` if the queues could be connected correctly,
     * `false` if not.
     */
    fun addFirstStep(clientConnectionPosition: Position): Boolean {
        reset()
        addStep(clientConnectionPosition)
        return true
    }

    /**
     * Adds a step to walk to the queue.
     *
     * @param x       X to walk to
     * @param y       Y to walk to
     * //     * @param clipped Can the step walk through objects?
     */
    fun walkStep(x: Int, y: Int) {
        val position = character.position.copy()
        position.x = position.x + x
        position.y = position.y + y
        addStep(position)
    }

    /**
     * Adds a step.
     *
     * @param x           The x coordinate of this step.
     * @param y           The y coordinate of this step.
     * @param heightLevel
     * //     * @param flag
     */
    private fun addStep(x: Int, y: Int, heightLevel: Int) {
        if (character.movementQueue.isLockedMovement || character.isFrozen || character.isStunned) {
            return
        }
        if (points.size >= MAXIMUM_SIZE) return
        val last = last
        val deltaX = x - last.position.x
        val deltaY = y - last.position.y
        val direction = Direction.fromDeltas(deltaX, deltaY)
        if (direction != Direction.NONE) points.add(Point(Position(x, y, heightLevel), direction))
    }

    /**
     * Adds a step to the queue.
     *
     * @param step The step to add.
     * @oaram flag
     */
    fun addStep(step: Position) {
        if (character.isFrozen || isLockedMovement || character.isStunned) return
        val last = last
        val x = step.x
        val y = step.y
        var deltaX = x - last.position.x
        var deltaY = y - last.position.y
        val max = Math.max(Math.abs(deltaX), Math.abs(deltaY))
        for (i in 0 until max) {
            if (deltaX < 0) deltaX++ else if (deltaX > 0) deltaX--
            if (deltaY < 0) deltaY++ else if (deltaY > 0) deltaY--
            addStep(x - deltaX, y - deltaY, step.z)
        }
    }

    fun canWalk(deltaX: Int, deltaY: Int): Boolean {
        val to = Position(character.position.x + deltaX, character.position.y + deltaY, character.position.z)
        return if (character.position.z == -1 && to.z == -1 && character.isNpc && !(character as NPC).isSummoningNpc || character.location === Locations.Location.RECIPE_FOR_DISASTER) true else canWalk(
            character.position,
            to,
            character.size
        )
    }
    /*
     * public boolean checkBarricade(int x, int y) { Position position =
     * character.getPosition(); if(character.isPlayer()) {
     * if(Locations.inSoulWars((Player)character)) {
     * if(SoulWars.checkBarricade(position.getX() + x, position.getY()+ y,
     * position.getZ())) { ((Player)character).getPacketSender().sendMessage(
     * "The path is blocked by a Barricade."); reset(true); return true; } } }
     * return false; }
     */
    /**
     * Gets the last point.
     *
     * @return The last point.
     */
    private val last: Point
        get() = points.peekLast() ?: Point(character.position, Direction.NONE)

    /**
     * @return true if the character is moving.
     */
    val isMoving: Boolean
        get() = !points.isEmpty()

    /**
     * Called every 600ms, updates the queue.
     */
    fun sequence() {
        val movement = !isLockedMovement && !character.isFrozen && !character.isStunned
        if (movement) {
            var walkPoint: Point? = null
            var runPoint: Point? = null
            walkPoint = points.poll()
            if (isRunToggled) {
                runPoint = points.poll()
            }
            if (character.isNeedsPlacement) {
                reset()
                return
            }
            if (walkPoint != null && walkPoint.direction != Direction.NONE) {
                if (followCharacter != null) {
                    if (walkPoint.position == followCharacter!!.position) {
                        return
                    } else {
                        if (!followCharacter!!.movementQueue.isRunToggled) {
                            if (character.position.isWithinDistance(followCharacter!!.position, 2)) {
                                runPoint = null
                            }
                        }
                    }
                }
                if (!isPlayer && !character.combatBuilder.isAttacking) {
                    if ((character as NPC).isSummoningNpc && !character.summoningCombat()) {
                        if (!canWalk(character.getPosition(), walkPoint.position, character.getSize())) {
                            return
                        }
                    }
                }
                character.position = walkPoint.position
                character.primaryDirection = walkPoint.direction
                character.lastDirection = walkPoint.direction
            }
            if (runPoint != null && runPoint.direction != Direction.NONE) {
                if (followCharacter != null) {
                    if (walkPoint.position == followCharacter!!.position) {
                        return
                    }
                }
                character.position = runPoint.position
                character.secondaryDirection = runPoint.direction
                character.lastDirection = runPoint.direction
                if (isPlayer) {
                    handleRegionChange()
                }
            }
        }
        if (isPlayer) {
            Locations.process(character)
            EnergyHandler.processPlayerEnergy(character as Player)
        }
    }

    val isMovementDone: Boolean
        get() = points.size == 0

    fun handleRegionChange() {
        val diffX = (character.position.x
                - character.lastKnownRegion.regionX * 8)
        val diffY = (character.position.y
                - character.lastKnownRegion.regionY * 8)
        var regionChanged = false
        if (diffX < 16) regionChanged = true else if (diffX >= 88) regionChanged = true
        if (diffY < 16) regionChanged = true else if (diffY >= 88) regionChanged = true
        if (regionChanged) {
            (character as Player).packetSender.sendMapRegion()
        }
    }

    fun startFollow() {
        if (followCharacter == null && (followTask == null || !followTask!!.isRunning)) return
        if (followTask == null || !followTask!!.isRunning) {

            // Build the task that will be scheduled when following.
            followTask = object : Task(1, character, true) {
                public override fun execute() {

                    // Check if we can still follow the leader.
                    if (followCharacter == null || followCharacter!!.constitution <= 0 || !followCharacter!!.isRegistered || character.constitution <= 0 || !character.isRegistered) {
                        character.setEntityInteraction(null)
                        stop()
                        return
                    }
                    val combatFollowing = character.combatBuilder.isAttacking
                    if (!Locations.Location.ignoreFollowDistance(character)) {
                        val summNpc = followCharacter!!.isPlayer && character.isNpc && (character as NPC).isSummoningNpc
                        if (!character.position.isWithinDistance(
                                followCharacter!!.position,
                                if (summNpc) 10 else if (combatFollowing) 40 else 20
                            )
                        ) {
                            character.setEntityInteraction(null)
                            stop()
                            if (summNpc) (followCharacter as Player).summoning.moveFollower(true)
                            return
                        }
                    }

                    // Block if our movement is locked.
                    if (character.movementQueue.isLockedMovement || character.isFrozen || character.isStunned) {
                        return
                    }

                    // If we are on the same position as the leader then move
                    // away.

                    //If combat follow, let the combat factory handle it
                    if (character.position == followCharacter!!.position) {
                        character.movementQueue.reset()
                        if (followCharacter!!.movementQueue.isMovementDone) stepAway(
                            character
                        )
                        return
                    }

                    // Check if we are within distance to attack for combat.
                    if (combatFollowing) {
                        //if (character.isPlayer()) {
                        if (character.combatBuilder.strategy == null) {
                            character.combatBuilder.determineStrategy()
                        }
                        //TODO: notsure what it does
                        if (checkAttackDistance(character, followCharacter!!)) {
                            return
                        }
                    } else {
                        if (character.interactingEntity !== followCharacter) {
                            character.setEntityInteraction(followCharacter)
                        }
                    }

                    // If we are within 1 square we don't need to move.
                    if (Locations.goodDistance(character.position, followCharacter!!.position, 1)) {
                        return
                    }
                    if (character.isNpc && (character as NPC).isSummoningNpc && (followCharacter!!.location === Locations.Location.HOME_BANK || followCharacter!!.location === Locations.Location.EDGEVILLE || followCharacter!!.location === Locations.Location.VARROCK)) {
                        character.getMovementQueue().walkStep(
                            getMove(character.getPosition().x, followCharacter!!.position.x, 1), getMove(
                                character.getPosition().y - 1, followCharacter!!.position.y, 1
                            )
                        )
                    } else {
                        findPath(
                            character,
                            followCharacter!!.position.x,
                            followCharacter!!.position.y - character.size,
                            true,
                            character.size,
                            character.size
                        )
                    }
                }

                override fun stop() {
                    setEventRunning(false)
                    followTask = null
                }
            }

            // Then submit the actual task.
            TaskManager.submit(followTask)
        }
    }

    /**
     * Stops the movement.
     */
    fun reset(): MovementQueue {
        points.clear()
        return this
    }

    /**
     * Gets the size of the queue.
     *
     * @return The size of the queue.
     */
    fun size(): Int {
        return points.size
    }

    fun stun(delay: Int) {
        character.stunDelay = delay
        if (character.isPlayer) {
            (character as Player).packetSender.sendMessage("You have been stunned!")
        }
        reset()
        TaskManager.submit(object : Task(2, character, true) {
            override fun execute() {
                if (!character.isRegistered || character.constitution <= 0) {
                    stop()
                    return
                }
                if (character.decrementAndGetStunDelay() == 0) {
                    stop()
                }
            }
        })
    }

    fun freeze(delay: Int) {
        if (character.isFrozen) return
        character.freezeDelay = delay
        if (character.isPlayer) {
            (character as Player).packetSender.sendMessage("You have been frozen!")
        }
        reset()
        TaskManager.submit(object : Task(2, character, true) {
            override fun execute() {
                if (!character.isRegistered || character.constitution <= 0) {
                    stop()
                    return
                }
                if (character.decrementAndGetFreezeDelay() == 0) {
                    stop()
                }
            }
        })
    }
    /**
     * Gets whether or not this entity is 'frozen'.
     *
     * @return true if this entity cannot move.
     */
    /**
     * If this entity's movement is locked.
     */
    public var isLockedMovement = false

    /**
     * Sets if this entity can move or not.
     *
     * @param lockMovement true if this entity cannot move.
     */
    fun setLockMovement(lockMovement: Boolean): MovementQueue {
        isLockedMovement = lockMovement
        return this
    }

    val isRunToggled: Boolean
        get() = character.isPlayer && (character as Player).isRunning && !character.isCrossingObstacle

    companion object {
        /**
         * The maximum size of the queue. If any additional steps are added, they
         * are discarded.
         */
        private const val MAXIMUM_SIZE = 100

        @JvmStatic
        fun canWalk(from: Position?, to: Position?, size: Int): Boolean {
            return RegionClipping.canMove(from, to, size, size)
        }

        /**
         * The force movement array index values.
         */
        const val FIRST_MOVEMENT_X = 0
        const val FIRST_MOVEMENT_Y = 1
        const val SECOND_MOVEMENT_X = 2
        const val SECOND_MOVEMENT_Y = 3
        const val MOVEMENT_SPEED = 4
        const val MOVEMENT_REVERSE_SPEED = 5
        const val MOVEMENT_DIRECTION = 6

        /**
         * Steps away from a Gamecharacter
         *
         * @param character The gamecharacter to step away from
         */
        fun stepAway(character: CharacterEntity) {
            if (character.movementQueue.canWalk(-1, 0)) character.movementQueue.walkStep(
                -1,
                0
            ) else if (character.movementQueue.canWalk(1, 0)) character.movementQueue.walkStep(
                1,
                0
            ) else if (character.movementQueue.canWalk(0, -1)) character.movementQueue.walkStep(
                0,
                -1
            ) else if (character.movementQueue.canWalk(0, 1)) character.movementQueue.walkStep(0, 1)
        }

        fun getMove(x: Int, p2: Int, size: Int): Int {
            if (x - p2 == 0) {
                return 0
            } else if (x - p2 < 0) {
                return size
            } else if (x - p2 > 0) {
                return -size
            }
            return 0
        }
    }
}