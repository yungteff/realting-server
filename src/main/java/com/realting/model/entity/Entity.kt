package com.realting.model.entity

import com.realting.GameSettings
import com.realting.model.*
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.world.World

open class Entity(position: Position) {
    /**
     * Attributes.
     */
    val attributes = Attributes()
    /**
     * Gets the entity's unique index.
     * @return    The entity's index.
     */
    /**
     * The entity's unique index.
     */
    var index = 0
        private set
    /**
     * gets the entity's tile size.
     * @return    The size the entity occupies in the world.
     */
    /**
     * The entity's tile size.
     */
    open var size = 1
    /**
     * Gets the entity position.
     * @return the entity's world position
     */
    /**
     * The default position the entity is in.
     */
    var position = GameSettings.DEFAULT_POSITION.copy()
    /**
     * Gets this entity's first position upon entering their
     * current map region.
     * @return    The lastKnownRegion instance.
     */
    /**
     * The entity's first position in current map region.
     */
    var lastKnownRegion: Position
        private set

    /**
     * The Entity constructor.
     * @param position    The position the entity is currently in.
     */
    init {
        setPosition(position)
        lastKnownRegion = position
    }

    /**
     * Sets the entity's index.
     * @param index        The value the entity's index will contain.
     * @return            The Entity instance.
     */
    fun setIndex(index: Int): Entity {
        this.index = index
        return this
    }

    /**
     * Sets the entity's current region's position.
     * @param lastKnownRegion    The position in which the player first entered the current region.
     * @return                    The Entity instance.
     */
    fun setLastKnownRegion(lastKnownRegion: Position): Entity {
        this.lastKnownRegion = lastKnownRegion
        return this
    }

    /**
     * Sets the entity position
     * @param position the world position
     */
    fun setPosition(position: Position): Entity {
        this.position = position
        return this
    }

    /**
     * Performs an animation.
     * @param animation    The animation to perform.
     */
    open fun performAnimation(animation: Animation) {}

    /**
     * Performs a graphic.
     * @param graphic    The graphic to perform.
     */
    open fun performGraphic(graphic: Graphic) {}

    /**
     * Sets the entity's tile size
     * @return    The Entity instance.
     */
    fun setSize(size: Int): Entity {
        this.size = size
        return this
    }

    fun toNpc(): NPC {
        return World.getNpcs()[index]
    }

    open val isNpc: Boolean
        get() = this is NPC
    open val isPlayer: Boolean
        get() = this is Player
    val isGameObject: Boolean
        get() = this is GameObject
}