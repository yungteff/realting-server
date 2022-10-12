package com.realting.world.content.player.events

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.GameObject
import com.realting.model.Locations
import com.realting.model.Position
import com.realting.util.Misc
import com.realting.world.World
import com.realting.world.content.CustomObjects

/**
 * Handles the Wilderness teleport obelisks.
 * @author Gabriel Hannason
 */
object WildernessObelisks {
    /**
     * Activates the Wilderness obelisks.
     * @param objectId        The object id
     * @return                true if the object is an obelisk
     */
    @JvmStatic
    fun handleObelisk(objectId: Int): Boolean {
        val index = getObeliskIndex(objectId)
        if (index >= 0) {
            if (!OBELISK_ACTIVATED[index]) {
                OBELISK_ACTIVATED[index] = true
                obelisks[0] = GameObject(
                    14825, Position(
                        OBELISK_COORDS[index][0], OBELISK_COORDS[index][1]
                    )
                )
                obelisks[1] = GameObject(
                    14825, Position(
                        OBELISK_COORDS[index][0] + 4, OBELISK_COORDS[index][1]
                    )
                )
                obelisks[2] = GameObject(
                    14825, Position(
                        OBELISK_COORDS[index][0], OBELISK_COORDS[index][1] + 4
                    )
                )
                obelisks[3] = GameObject(
                    14825, Position(
                        OBELISK_COORDS[index][0] + 4, OBELISK_COORDS[index][1] + 4
                    )
                )
                var obeliskX: Int
                var obeliskY: Int
                for (i in obelisks.indices) {
                    obeliskX = if (i == 1 || i == 3) OBELISK_COORDS[index][0] + 4 else OBELISK_COORDS[index][0]
                    obeliskY = if (i >= 2) OBELISK_COORDS[index][1] + 4 else OBELISK_COORDS[index][1]
                    CustomObjects.globalObjectRespawnTask(
                        obelisks[i], GameObject(
                            OBELISK_IDS[index], Position(obeliskX, obeliskY)
                        ), 8
                    )
                }
                TaskManager.submit(object : Task(8, false) {
                    public override fun execute() {
                        handleTeleport(index)
                        stop()
                    }

                    override fun stop() {
                        setEventRunning(false)
                        OBELISK_ACTIVATED[index] = false
                    }
                })
            }
            return true
        }
        return false
    }

    fun handleTeleport(index: Int) {
        var random = Misc.getRandom(5)
        while (random == index) random = Misc.getRandom(5)
        for (player in World.getPlayers()) {
            if (player == null || player.location == null || player.location !== Locations.Location.WILDERNESS) continue
            if (Locations.goodDistance(
                    player.position.copy(), Position(
                        OBELISK_COORDS[index][0] + 2, OBELISK_COORDS[index][1] + 2
                    ), 1
                )
            ) player.moveTo(
                Position(
                    OBELISK_COORDS[random][0] + 2, OBELISK_COORDS[random][1] + 2
                )
            )
        }
    }

    /*
	 * Gets the array index for an obelisk
	 */
    fun getObeliskIndex(id: Int): Int {
        for (j in OBELISK_IDS.indices) {
            if (OBELISK_IDS[j] == id) return j
        }
        return -1
    }

    /*
	 * Obelisk ids
	 */
    private val OBELISK_IDS = intArrayOf(
        14829, 14830, 14827, 14828, 14826, 14831
    )

    /*
	 * The obelisks
	 */
    val obelisks = arrayOf<GameObject>()

    /*
	 * Are the obelisks activated?
	 */
    private val OBELISK_ACTIVATED = BooleanArray(OBELISK_IDS.size)

    /*
	 * Obelisk coords
	 */
    private val OBELISK_COORDS = arrayOf(
        intArrayOf(3154, 3618),
        intArrayOf(3225, 3665),
        intArrayOf(3033, 3730),
        intArrayOf(3104, 3792),
        intArrayOf(2978, 3864),
        intArrayOf(3305, 3914)
    )
}