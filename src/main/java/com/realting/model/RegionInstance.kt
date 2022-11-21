package com.realting.model

import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.world.World
import com.realting.world.content.minigames.Barrows.killBarrowsNpc
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Handles a custom region instance for a player
 * @author Gabriel
 */
open class RegionInstance(open var owner: Player, val type: RegionInstanceType) {
    enum class RegionInstanceType {
        BARROWS, THE_SIX, GRAVEYARD, FIGHT_CAVE, WARRIORS_GUILD, NOMAD, RECIPE_FOR_DISASTER, CONSTRUCTION_HOUSE, CONSTRUCTION_DUNGEON, TRIO, KRAKEN, ZULRAH
    }

    val npcsList: CopyOnWriteArrayList<NPC> = CopyOnWriteArrayList()
    var playersList: CopyOnWriteArrayList<Player>? = null

    init {
        if (type == RegionInstanceType.CONSTRUCTION_HOUSE || type == RegionInstanceType.THE_SIX) {
            playersList = CopyOnWriteArrayList()
        }
    }

    open fun destruct() {
        for (n in npcsList) {
            if (n != null && n.constitution > 0 && World.getNpcs()[n.index] != null && !n.isDying) {
                if (type == RegionInstanceType.WARRIORS_GUILD) {
                    if (n.id >= 4278 && n.id <= 4284) {
                        owner.minigameAttributes.warriorsGuildAttributes.setSpawnedArmour(false)
                    }
                } else if (type == RegionInstanceType.BARROWS) {
                    if (n.id >= 2024 && n.id <= 2034) {
                        killBarrowsNpc(owner, n, false)
                    }
                }
                World.deregister(n)
                //System.out.println("Is this running?");
            }
        }
        npcsList.clear()
        owner.regionInstance = null
        //System.out.println("Is this ru222nning?");
    }

    open fun add(c: CharacterEntity) {
        if (type == RegionInstanceType.CONSTRUCTION_HOUSE) {
            if (c.isPlayer) {
                playersList!!.add(c as Player)
            } else if (c.isNpc) {
                npcsList.add(c as NPC)
            }
            if (c.regionInstance == null || c.regionInstance !== this) {
                c.regionInstance = this
            }
        }
    }

    open fun remove(c: CharacterEntity) {
        if (type == RegionInstanceType.CONSTRUCTION_HOUSE) {
            if (c.isPlayer) {
                playersList!!.remove(c as Player)
                if (owner === c) {
                    destruct()
                }
            } else if (c.isNpc) {
                npcsList.remove(c as NPC)
            }
            if (c.regionInstance != null && c.regionInstance === this) {
                c.regionInstance = null
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        return other as RegionInstanceType? == type
    }
}