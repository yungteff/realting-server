package com.realting.world.content.player.skill.dungeoneering

import com.realting.model.*
import com.realting.model.entity.character.npc.NPC

/**
 * I couldn't be arsed to put all npc spawns in the enum.
 * @author Gabriel Hannason
 */
enum class DungeoneeringFloor(
    val entrance: Position, val smugglerPosition: Position, val objects: Array<GameObject>, val npcs: Array<Array<NPC>>
) {
    FIRST_FLOOR(
        Position(2451, 4935), Position(2448, 4939), arrayOf(GameObject(-1, Position(2461, 4931))), arrayOf(
            arrayOf(
                NPC(491, Position(2440, 4958)),
                NPC(688, Position(2443, 4954)),
                NPC(13, Position(2444, 4958)),
                NPC(5664, Position(2460, 4965)),
                NPC(90, Position(2460, 4961)),
                NPC(90, Position(2462, 4965)),
                NPC(1624, Position(2474, 4958)),
                NPC(174, Position(2477, 4954)),
                NPC(2060, Position(2473, 4940)),
                NPC(688, Position(2471, 4937)),
                NPC(688, Position(2474, 4943))
            ), arrayOf(
                NPC(124, Position(2441, 4958)),
                NPC(108, Position(2441, 4954)),
                NPC(688, Position(2443, 4956)),
                NPC(111, Position(2460, 4965)),
                NPC(52, Position(2457, 4961)),
                NPC(1643, Position(2477, 4954)),
                NPC(13, Position(2477, 4958)),
                NPC(13, Position(2474, 4958)),
                NPC(8549, Position(2473, 4940))
            ), arrayOf(
                NPC(13, Position(2441, 4958)),
                NPC(13, Position(2441, 4955)),
                NPC(13, Position(2443, 4954)),
                NPC(1643, Position(2445, 4956)),
                NPC(13, Position(2443, 4958)),
                NPC(13, Position(2445, 4958)),
                NPC(13, Position(2445, 4954)),
                NPC(2019, Position(2461, 4965)),
                NPC(27, Position(2458, 4966)),
                NPC(27, Position(2458, 4961)),
                NPC(27, Position(2458, 4967)),
                NPC(5361, Position(2476, 4957)),
                NPC(3495, Position(2475, 4954)),
                NPC(491, Position(2472, 4957)),
                NPC(1382, Position(2473, 4940))
            ), arrayOf(
                NPC(8162, Position(2441, 4954)),
                NPC(8162, Position(2441, 4957)),
                NPC(90, Position(2443, 4958)),
                NPC(90, Position(2443, 4954)),
                NPC(90, Position(2440, 4956)),
                NPC(2896, Position(2458, 4967)),
                NPC(2896, Position(2462, 4967)),
                NPC(2896, Position(2462, 4960)),
                NPC(2896, Position(2457, 4960)),
                NPC(2896, Position(2459, 4964)),
                NPC(1880, Position(2456, 4964)),
                NPC(110, Position(2472, 4955)),
                NPC(688, Position(2477, 4954)),
                NPC(84, Position(2477, 4957)),
                NPC(9939, Position(2472, 4940))
            )
        )
    );

    companion object {
        fun forId(id: Int): DungeoneeringFloor? {
            for (floors in values()) {
                if (floors != null && floors.ordinal == id) {
                    return floors
                }
            }
            return null
        }
    }
}