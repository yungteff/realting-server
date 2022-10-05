package com.realting.model.movement

/**
 * Represents a player's movement status, whether they are standing still,
 * moving, frozen or stunned.
 *
 * @author relex lawl
 */
enum class MovementStatus {
    NONE, MOVING, FROZEN, STUNNED, CANNOT_MOVE
}