package com.realting.world.clip.region

import com.realting.GameServer
import com.realting.model.GameObject
import com.realting.model.Locations
import com.realting.model.Locations.Location.Companion.getLocation
import com.realting.model.Locations.Location.Companion.inLocation
import com.realting.model.Position
import com.realting.model.definitions.GameObjectDefinition
import com.realting.model.entity.character.CharacterEntity
import com.realting.util.Misc
import com.realting.world.clip.stream.ByteStream
import lombok.extern.java.Log
import org.apache.commons.lang3.ArrayUtils
import java.io.DataInputStream
import java.io.EOFException
import java.io.File
import java.io.FileInputStream
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * A highly modified version of the released clipping.
 *
 * @author Relex lawl and Palidino: Gave me (Gabbe) the base.
 * @editor Gabbe: Rewrote the system, now loads regions when they're actually
 * needed etc.
 */
private val log = Logger.getLogger(RegionClipping::class.java.name)

@Log
class RegionClipping(val id: Int, map: Int, mapObj: Int, private val osrs: Boolean) {
    private inner class RegionData(val mapGround: Int, val mapObject: Int)

    private val regionData: RegionData
    private val clips = arrayOfNulls<Array<IntArray>?>(4)
    var gameObjects: Array<Array<Array<GameObject?>?>?>? = arrayOfNulls<Array<Array<GameObject?>?>?>(4)

    val regionAbsX = (id shr 8) * 64
    val regionAbsY = (id and 0xff) * 64

    fun removeClip(x: Int, y: Int, height: Int, shift: Int) {
        loadRegion(x, y)
        if (clips[height] == null) {
            clips[height] = Array(64) { IntArray(64) }
        }
        clips[height]!![x - regionAbsX][y - regionAbsY] = 16777215 - shift
    }

    fun addClip(x: Int, y: Int, height: Int, shift: Int) {
        loadRegion(x, y)
        if (clips[height] == null) {
            clips[height] = Array(64) { IntArray(64) }
        }
        clips[height]!![x - regionAbsX][y - regionAbsY] = clips[height]!![x - regionAbsX][y - regionAbsY] or shift
    }

    private fun getClip(x: Int, y: Int, height: Int): Int {
        loadRegion(x, y)
        if (clips[height] == null) {
            clips[height] = Array(64) { IntArray(64) }
        }
        return clips[height]!![x - regionAbsX][y - regionAbsY]
    }

    init {
        regionData = RegionData(map, mapObj)
    }

    companion object {
        /**
         * Osrs region ids.
         */
        @kotlin.jvm.JvmField
        val OSRS_REGIONS: IntArray = Arrays.stream(
            intArrayOf( // Motherlode mine
                14935,
                14936,
                14937,
                15191,
                15192,
                15193,
                14678,
                14679,
                14680,
                14681,
                14682,
                14934,
                14938,
                15190,
                15194,
                15446,
                15447,
                15448,
                15449,
                15450,  // Lizardman canyon
                5689,
                5690,
                5945,
                5946,
                5432,
                5433,
                5434,
                5435,
                5688,
                5691,
                5944,
                5947,
                6200,
                6201,
                6202,
                6203,  // Crash site cavern (demonic gorillas)
                8279,
                8280,
                8535,
                8536,
                8791,
                8792,
                8023,
                8024,
                8025,
                8279,
                8280,
                8281,
                8535,
                8536,
                8537,
                8280,
                8281,
                8536,
                8537,
                8792,
                8793
            )
        ).distinct().toArray()

        private val regions: MutableMap<Int, RegionClipping> = HashMap()
        private val loadedRegions = HashSet<Int>()

        @kotlin.jvm.JvmStatic
        fun init() {
            try {
                if (!GameServer.getConfiguration().isDebug) {
                    log.level = Level.OFF
                }
                GameObjectDefinition.init()
                var file = File("./data/clipping/map_index")
                var buffer = ByteArray(file.length().toInt())
                var input = DataInputStream(FileInputStream(file))
                input.readFully(buffer)
                input.close()
                var stream = ByteStream(buffer)
                var size = stream.uShort
                for (i in 0 until size) {
                    val regionId = stream.uShort
                    regions[regionId] = RegionClipping(regionId, stream.uShort, stream.uShort, false)
                }
                file = File("./data/clipping/map_index_osrs")
                buffer = ByteArray(file.length().toInt())
                input = DataInputStream(FileInputStream(file))
                input.readFully(buffer)
                input.close()
                stream = ByteStream(buffer)
                size = stream.uShort
                for (i in 0 until size) {
                    val regionId = stream.uShort
                    val file1 = stream.uShort
                    val file2 = stream.uShort
                    if (ArrayUtils.contains(OSRS_REGIONS, regionId)) {
                        regions[regionId] = RegionClipping(regionId, file1, file2, true)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        operator fun get(regionId: Int): RegionClipping? {
            return regions[regionId]
        }

        @kotlin.jvm.JvmStatic
        fun loadRegion(x: Int, y: Int) {
            val regionX = x shr 3
            val regionY = y shr 3
            val regionId = (regionX / 8 shl 8) + regionY / 8
            val r = Companion[regionId] ?: return
            if (loadedRegions.contains(regionId)) {
                return
            }
            val mapGround = r.regionData.mapGround
            val mapObjects = r.regionData.mapObject
            val directory = if (r.osrs) "maps_osrs" else "maps_standard"
            val objectFile = "./data/clipping/$directory/$mapObjects.gz"
            val groundFile = "./data/clipping/$directory/$mapGround.gz"
            try {
                val objectData = Misc.getBuffer(File(objectFile))
                val groundData = Misc.getBuffer(File(groundFile))
                if (objectData == null) {
                    log.severe(String.format("File missing %s", objectFile))
                    loadedRegions.add(regionId)
                    return
                }
                loadedRegions.add(regionId)
                loadMaps(regionId, ByteStream(objectData), ByteStream(groundData), r.osrs)
                //log.info(String.format("Loaded map regionId=%d, objectFile=%d, groundFile=%d, osrs=%s", regionId, mapObjects, mapGround, r.osrs));
            } catch (e: Exception) {
                if (e.javaClass == EOFException::class.java) {
                    log.severe(String.format("End of file exception %s %s", objectFile, groundFile))
                } else {
                    log.severe(String.format("Error loading files %s %s", objectFile, groundFile))
                    e.printStackTrace()
                }
                loadedRegions.add(regionId)
            }
        }

        private fun loadMaps(regionId: Int, objectStream: ByteStream, groundStream: ByteStream, osrs: Boolean) {
            val absX = (regionId shr 8) * 64
            val absY = (regionId and 0xff) * 64
            val heightMap = Array(4) { Array(64) { ByteArray(64) } }
            for (z in 0..3) {
                for (tileX in 0..63) {
                    for (tileY in 0..63) {
                        while (true) {
                            val tileType = groundStream.uByte
                            if (tileType == 0) {
                                break
                            } else if (tileType == 1) {
                                groundStream.uByte
                                break
                            } else if (tileType <= 49) {
                                groundStream.uByte
                            } else if (tileType <= 81) {
                                heightMap[z][tileX][tileY] = (tileType - 49).toByte()
                            }
                        }
                    }
                }
            }
            for (i in 0..3) {
                for (i2 in 0..63) {
                    for (i3 in 0..63) {
                        if (heightMap[i][i2][i3].toInt() and 1 == 1) {
                            var height = i
                            if (heightMap[1][i2][i3].toInt() and 2 == 2) {
                                height--
                            }
                            if (height >= 0 && height <= 3) {
                                addClipping(absX + i2, absY + i3, height, 0x200000)
                            }
                        }
                    }
                }
            }
            var objectId = -1
            var incr: Int
            while (objectStream.uSmart.also { incr = it } != 0) {
                objectId += incr
                var location = 0
                var incr2: Int
                while (objectStream.uSmart.also { incr2 = it } != 0) {
                    location += incr2 - 1
                    val localX = location shr 6 and 0x3f
                    val localY = location and 0x3f
                    var height = location shr 12
                    val objectData = objectStream.uByte
                    val type = objectData shr 2
                    val direction = objectData and 0x3
                    if (localX < 0 || localX >= 64 || localY < 0 || localY >= 64) {
                        continue
                    }
                    if (heightMap[1][localX][localY].toInt() and 2 == 2) {
                        height--
                    }
                    if (height >= 0 && height <= 3) {
                        addObject(
                            objectId + if (osrs) 70000 else 0, absX + localX, absY + localY, height, type, direction
                        ) // Add object to clipping
                    }
                }
            }
        }

        fun addClipping(x: Int, y: Int, height: Int, shift: Int) {
            loadRegion(x, y)
            val regionX = x shr 3
            val regionY = y shr 3
            val regionId = (regionX / 8 shl 8) + regionY / 8
            val r = Companion[regionId]
            r?.addClip(x, y, height, shift)
        }

        fun removeClipping(x: Int, y: Int, height: Int, shift: Int) {
            loadRegion(x, y)
            val regionX = x shr 3
            val regionY = y shr 3
            val regionId = (regionX / 8 shl 8) + regionY / 8
            val r = Companion[regionId]
            r?.removeClip(x, y, height, shift)
        }

        fun forPosition(position: Position): RegionClipping? {
            val regionX = position.x shr 3
            val regionY = position.y shr 3
            val regionId = (regionX / 8 shl 8) + regionY / 8
            loadRegion(position.x, position.y)
            return Companion[regionId]
        }

        fun getObjectInformation(position: Position): IntArray? {
            val clipping = forPosition(position)
            return if (clipping != null) {
                val x = position.x
                val y = position.y
                var height = position.z
                val regionAbsX = (clipping.id shr 8) * 64
                val regionAbsY = (clipping.id and 0xff) * 64
                if (height < 0 || height >= 4) height = 0
                loadRegion(x, y)
                if (clipping.gameObjects == null || clipping.gameObjects!![height] == null || clipping.gameObjects!![height]!![x - regionAbsX] == null || clipping.gameObjects!![height]!![x - regionAbsX]!![y - regionAbsY] == null) {
                    null
                } else intArrayOf(
                    clipping.gameObjects!![height]!![x - regionAbsX]!![y - regionAbsY]!!.face,
                    clipping.gameObjects!![height]!![x - regionAbsX]!![y - regionAbsY]!!.type,
                    clipping.gameObjects!![height]!![x - regionAbsX]!![y - regionAbsY]!!.id
                )
            } else {
                null
            }
        }

        @kotlin.jvm.JvmStatic
        fun objectExists(`object`: GameObject): Boolean {
            val loc = getLocation(`object`)
            val pos = `object`.entityPosition
            val id = `object`.id
            val barrows = (pos.z == -1 && `object`.definition != null && (`object`.definition.getName()
                .lowercase(Locale.getDefault()).contains("sarcophagus") || `object`.definition.getName()
                .lowercase(Locale.getDefault())
                .contains("staircase")) || loc != null && loc === Locations.Location.BARROWS)
            val catherbyAquariums = id == 10091 && pos.x >= 2829 && pos.x <= 2832 && pos.y >= 3441 && pos.y <= 3447
            val freeForAllPortal = id == 38700 && pos.x == 2814 && pos.y == 5509
            val warriorsGuild =
                id == 15653 && pos.x == 2877 && pos.y == 3546 || loc === Locations.Location.WARRIORS_GUILD
            val fightPit = id == 9369 && pos.x == 2399 && pos.y == 5176 || id == 9368 && pos.x == 2399 && pos.y == 5168
            val barbCourseRopeswing = id == 2282 && pos.x == 2551 && pos.y == 3550
            val lumbridgeCastle =
                id == 12348 && pos.x == 3207 && pos.y == 3217 || id == 1738 && pos.x == 3204 && pos.y == 3207 || id == 1739 && pos.x == 3204 && pos.y == 3207 && pos.z == 1 || id == 1739 && pos.x == 3204 && pos.y == 3229 && pos.z == 1
            val rfd =
                (id == 12356 && (pos.x == 1900 && pos.y == 5345 || pos.x == 1899 && pos.y == 5366 || pos.x == 1910 && pos.y == 5356 || pos.x == 1889 && pos.y == 5355))
            val lunar = id == 29944 && pos.x == 2111 && pos.y == 3917
            val chaosTunnels = id == 28779 // It checks player coords anyway
            val trees =
                (id == 1306 && pos.x == 2696 && pos.y == 3423 || id == 1307 && (pos.x == 2727 && pos.y == 3501 || pos.x == 2729 && pos.y == 3481))
            val godwars = pos.z == 2
            val lawAltar = id == 2485 && pos.x == 2463 && pos.y == 4831
            val mageBankLever = id == 5959 && pos.x == 3090 && pos.y == 3956
            val well = id == 884 && pos.x == 3084 && pos.y == 3502
            val waterRcAltar = id == 2480 && pos.x == 3483 && pos.y == 4835
            val crystalChest = id == 172 && pos.x == 3077 && pos.y == 3497
            val draynor = id == 135 && pos.x == 3109 && pos.y == 3353 || id == 134 && pos.x == 3108 && pos.y == 3353
            if (well || mageBankLever || lawAltar || trees || chaosTunnels || lunar || barrows || rfd || lumbridgeCastle || barbCourseRopeswing || catherbyAquariums || freeForAllPortal || warriorsGuild || fightPit || godwars || barrows || waterRcAltar || crystalChest || draynor) return true
            val info = getObjectInformation(`object`.entityPosition)
            return if (info != null) {
                info[2] == `object`.id
            } else false
        }

        fun getGameObject(position: Position): GameObject? {
            val clipping = forPosition(position)
            return if (clipping != null) {
                val x = position.x
                val y = position.y
                var height = position.z
                val regionAbsX = (clipping.id shr 8) * 64
                val regionAbsY = (clipping.id and 0xff) * 64
                if (height < 0 || height >= 4) height = 0
                if (clipping.gameObjects!![height] == null) {
                    null
                } else clipping.gameObjects!![height]!![x - regionAbsX]!![y - regionAbsY]
            } else {
                null
            }
        }

        private fun addClippingForVariableObject(
            x: Int, y: Int, height: Int, type: Int, direction: Int, flag: Boolean
        ) {
            if (type == 0) {
                if (direction == 0) {
                    addClipping(x, y, height, 128)
                    addClipping(x - 1, y, height, 8)
                } else if (direction == 1) {
                    addClipping(x, y, height, 2)
                    addClipping(x, y + 1, height, 32)
                } else if (direction == 2) {
                    addClipping(x, y, height, 8)
                    addClipping(x + 1, y, height, 128)
                } else if (direction == 3) {
                    addClipping(x, y, height, 32)
                    addClipping(x, y - 1, height, 2)
                }
            } else if (type == 1 || type == 3) {
                if (direction == 0) {
                    addClipping(x, y, height, 1)
                    addClipping(x - 1, y, height, 16)
                } else if (direction == 1) {
                    addClipping(x, y, height, 4)
                    addClipping(x + 1, y + 1, height, 64)
                } else if (direction == 2) {
                    addClipping(x, y, height, 16)
                    addClipping(x + 1, y - 1, height, 1)
                } else if (direction == 3) {
                    addClipping(x, y, height, 64)
                    addClipping(x - 1, y - 1, height, 4)
                }
            } else if (type == 2) {
                if (direction == 0) {
                    addClipping(x, y, height, 130)
                    addClipping(x - 1, y, height, 8)
                    addClipping(x, y + 1, height, 32)
                } else if (direction == 1) {
                    addClipping(x, y, height, 10)
                    addClipping(x, y + 1, height, 32)
                    addClipping(x + 1, y, height, 128)
                } else if (direction == 2) {
                    addClipping(x, y, height, 40)
                    addClipping(x + 1, y, height, 128)
                    addClipping(x, y - 1, height, 2)
                } else if (direction == 3) {
                    addClipping(x, y, height, 160)
                    addClipping(x, y - 1, height, 2)
                    addClipping(x - 1, y, height, 8)
                }
            }
            if (flag) {
                if (type == 0) {
                    if (direction == 0) {
                        addClipping(x, y, height, 65536)
                        addClipping(x - 1, y, height, 4096)
                    } else if (direction == 1) {
                        addClipping(x, y, height, 1024)
                        addClipping(x, y + 1, height, 16384)
                    } else if (direction == 2) {
                        addClipping(x, y, height, 4096)
                        addClipping(x + 1, y, height, 65536)
                    } else if (direction == 3) {
                        addClipping(x, y, height, 16384)
                        addClipping(x, y - 1, height, 1024)
                    }
                }
                if (type == 1 || type == 3) {
                    if (direction == 0) {
                        addClipping(x, y, height, 512)
                        addClipping(x - 1, y + 1, height, 8192)
                    } else if (direction == 1) {
                        addClipping(x, y, height, 2048)
                        addClipping(x + 1, y + 1, height, 32768)
                    } else if (direction == 2) {
                        addClipping(x, y, height, 8192)
                        addClipping(x + 1, y + 1, height, 512)
                    } else if (direction == 3) {
                        addClipping(x, y, height, 32768)
                        addClipping(x - 1, y - 1, height, 2048)
                    }
                } else if (type == 2) {
                    if (direction == 0) {
                        addClipping(x, y, height, 66560)
                        addClipping(x - 1, y, height, 4096)
                        addClipping(x, y + 1, height, 16384)
                    } else if (direction == 1) {
                        addClipping(x, y, height, 5120)
                        addClipping(x, y + 1, height, 16384)
                        addClipping(x + 1, y, height, 65536)
                    } else if (direction == 2) {
                        addClipping(x, y, height, 20480)
                        addClipping(x + 1, y, height, 65536)
                        addClipping(x, y - 1, height, 1024)
                    } else if (direction == 3) {
                        addClipping(x, y, height, 81920)
                        addClipping(x, y - 1, height, 1024)
                        addClipping(x - 1, y, height, 4096)
                    }
                }
            }
        }

        private fun addClippingForSolidObject(
            x: Int, y: Int, height: Int, xLength: Int, yLength: Int, flag: Boolean
        ) {
            var clipping = 256
            if (flag) {
                clipping += 0x20000
            }
            for (i in x until x + xLength) {
                for (i2 in y until y + yLength) {
                    addClipping(i, i2, height, clipping)
                }
            }
        }

        fun getLocalPosition(position: Position): IntArray {
            val clipping = forPosition(position)
            val absX = position.x
            val absY = position.y
            val regionAbsX = (clipping!!.id shr 8) * 64
            val regionAbsY = (clipping.id and 0xff) * 64
            val localX = absX - regionAbsX
            val localY = absY - regionAbsY
            return intArrayOf(localX, localY)
        }

        fun addObject(objectId: Int, x: Int, y: Int, height: Int, type: Int, direction: Int) {
            if (GameObjectDefinition.remove667(objectId)) {
                return
            }
            val def = GameObjectDefinition.forId(objectId) ?: return
            loadRegion(x, y)
            when (objectId) {
                14233, 14235 -> return
            }
            val position = Position(x, y, height)
            val clipping = forPosition(position)
            if (clipping != null) {
                if (clipping.gameObjects!![height % 4] == null) {
                    clipping.gameObjects!![height % 4] = Array(64) { arrayOfNulls(64) }
                }
                val local = getLocalPosition(position)
                clipping.gameObjects!![height % 4]!![local[0]]!![local[1]] = GameObject(
                    objectId, Position(x, y, height), type, direction
                )
            }
            if (objectId == -1) {
                removeClipping(x, y, height, 0x000000)
                return
            }
            val xLength: Int
            val yLength: Int
            if (direction != 1 && direction != 3) {
                xLength = def.xLength()
                yLength = def.yLength()
            } else {
                yLength = def.xLength()
                xLength = def.yLength()
            }
            if (type == 22) {
                if (def.hasActions() && def.unwalkable) {
                    addClipping(x, y, height, 0x200000)
                }
            } else if (type >= 9) {
                if (def.unwalkable) {
                    addClippingForSolidObject(
                        x, y, height, xLength, yLength, def.aBoolean779
                    )
                }
            } else if (type >= 0 && type <= 3) {
                if (def.unwalkable) {
                    addClippingForVariableObject(
                        x, y, height, type, direction, def.aBoolean779
                    )
                }
            }
        }

        fun addObject(gameObject: GameObject) {
            if (gameObject.id != 65535) addObject(
                gameObject.id,
                gameObject.entityPosition.x,
                gameObject.entityPosition.y,
                gameObject.entityPosition.z,
                gameObject.type,
                gameObject.face
            )
        }

        fun removeObject(gameObject: GameObject) {
            addObject(
                -1,
                gameObject.entityPosition.x,
                gameObject.entityPosition.y,
                gameObject.entityPosition.z,
                gameObject.type,
                gameObject.face
            )
        }

        fun getClipping(x: Int, y: Int, height: Int): Int {
            var height = height
            loadRegion(x, y)
            val regionX = x shr 3
            val regionY = y shr 3
            val regionId = (regionX / 8 shl 8) + regionY / 8
            if (height >= 4) height = 0 else if (height == -1 || inLocation(
                    x, y, Locations.Location.PURO_PURO
                )
            ) return 0
            val r = Companion[regionId]
            return r?.getClip(x, y, height) ?: 0
        }

        fun canMove(
            startX: Int, startY: Int, endX: Int, endY: Int, height: Int, xLength: Int, yLength: Int
        ): Boolean {
            var diffX = endX - startX
            var diffY = endY - startY
            val max = Math.max(Math.abs(diffX), Math.abs(diffY))
            for (ii in 0 until max) {
                val currentX = endX - diffX
                val currentY = endY - diffY
                for (i in 0 until xLength) {
                    for (i2 in 0 until yLength) if (diffX < 0 && diffY < 0) {
                        if (getClipping(
                                currentX + i - 1, currentY + i2 - 1, height
                            ) and 0x128010e != 0 || getClipping(
                                currentX + i - 1, currentY + i2, height
                            ) and 0x1280108 != 0 || getClipping(
                                currentX + i, currentY + i2 - 1, height
                            ) and 0x1280102 != 0
                        ) return false
                    } else if (diffX > 0 && diffY > 0) {
                        if (getClipping(
                                currentX + i + 1, currentY + i2 + 1, height
                            ) and 0x12801e0 != 0 || getClipping(
                                currentX + i + 1, currentY + i2, height
                            ) and 0x1280180 != 0 || getClipping(
                                currentX + i, currentY + i2 + 1, height
                            ) and 0x1280120 != 0
                        ) return false
                    } else if (diffX < 0 && diffY > 0) {
                        if (getClipping(
                                currentX + i - 1, currentY + i2 + 1, height
                            ) and 0x1280138 != 0 || getClipping(
                                currentX + i - 1, currentY + i2, height
                            ) and 0x1280108 != 0 || getClipping(
                                currentX + i, currentY + i2 + 1, height
                            ) and 0x1280120 != 0
                        ) return false
                    } else if (diffX > 0 && diffY < 0) {
                        if (getClipping(
                                currentX + i + 1, currentY + i2 - 1, height
                            ) and 0x1280183 != 0 || getClipping(
                                currentX + i + 1, currentY + i2, height
                            ) and 0x1280180 != 0 || getClipping(
                                currentX + i, currentY + i2 - 1, height
                            ) and 0x1280102 != 0
                        ) return false
                    } else if (diffX > 0 && diffY == 0) {
                        if (getClipping(
                                currentX + i + 1, currentY + i2, height
                            ) and 0x1280180 != 0
                        ) return false
                    } else if (diffX < 0 && diffY == 0) {
                        if (getClipping(
                                currentX + i - 1, currentY + i2, height
                            ) and 0x1280108 != 0
                        ) return false
                    } else if (diffX == 0 && diffY > 0) {
                        if (getClipping(
                                currentX + i, currentY + i2 + 1, height
                            ) and 0x1280120 != 0
                        ) return false
                    } else if (diffX == 0 && diffY < 0 && getClipping(
                            currentX + i, currentY + i2 - 1, height
                        ) and 0x1280102 != 0
                    ) return false
                }
                if (diffX < 0) diffX++ else if (diffX > 0) diffX--
                if (diffY < 0) diffY++ else if (diffY > 0) diffY--
            }
            return true
        }

        fun canMove(
            start: Position, end: Position, xLength: Int, yLength: Int
        ): Boolean {
            return canMove(
                start.x, start.y, end.x, end.y, start.z, xLength, yLength
            )
        }

        fun blockedProjectile(position: Position): Boolean {
            return getClipping(position.x, position.y, position.z) and 0x20000 == 0
        }

        fun blocked(pos: Position): Boolean {
            return getClipping(pos.x, pos.y, pos.z) and 0x1280120 != 0
        }

        @kotlin.jvm.JvmStatic
        fun blockedNorth(pos: Position): Boolean {
            return getClipping(pos.x, pos.y + 1, pos.z) and 0x1280120 != 0
        }

        @kotlin.jvm.JvmStatic
        fun blockedEast(pos: Position): Boolean {
            return getClipping(pos.x + 1, pos.y, pos.z) and 0x1280180 != 0
        }

        @kotlin.jvm.JvmStatic
        fun blockedSouth(pos: Position): Boolean {
            return getClipping(pos.x, pos.y - 1, pos.z) and 0x1280102 != 0
        }

        @kotlin.jvm.JvmStatic
        fun blockedWest(pos: Position): Boolean {
            return getClipping(pos.x - 1, pos.y, pos.z) and 0x1280108 != 0
        }

        fun blockedNorthEast(pos: Position): Boolean {
            return getClipping(pos.x + 1, pos.y + 1, pos.z) and 0x12801e0 != 0
        }

        fun blockedNorthWest(pos: Position): Boolean {
            return getClipping(pos.x - 1, pos.y + 1, pos.z) and 0x1280138 != 0
        }

        fun blockedSouthEast(pos: Position): Boolean {
            return getClipping(pos.x + 1, pos.y - 1, pos.z) and 0x1280183 != 0
        }

        fun blockedSouthWest(pos: Position): Boolean {
            return getClipping(pos.x - 1, pos.y - 1, pos.z) and 0x128010e != 0
        }

        fun canProjectileAttack(a: CharacterEntity, b: CharacterEntity): Boolean {
            if (!a.isPlayer) {
                if (b.isPlayer) {
                    return canProjectileMove(
                        b.entityPosition.x,
                        b.entityPosition.y,
                        a.entityPosition.x,
                        a.entityPosition.y,
                        a.entityPosition.z,
                        1,
                        1
                    )
                }
            }
            return canProjectileMove(
                a.entityPosition.x, a.entityPosition.y, b.entityPosition.x, b.entityPosition.y, a.entityPosition.z, 1, 1
            )
        }

        fun canProjectileMove(
            startX: Int, startY: Int, endX: Int, endY: Int, height: Int, xLength: Int, yLength: Int
        ): Boolean {
            var diffX = endX - startX
            var diffY = endY - startY
            // height %= 4;
            val max = Math.max(Math.abs(diffX), Math.abs(diffY))
            for (ii in 0 until max) {
                val currentX = endX - diffX
                val currentY = endY - diffY
                for (i in 0 until xLength) {
                    for (i2 in 0 until yLength) {
                        if (diffX < 0 && diffY < 0) {
                            if (getClipping(
                                    currentX + i - 1, currentY + i2 - 1, height
                                ) and (UNLOADED_TILE or  /* BLOCKED_TILE | */UNKNOWN or PROJECTILE_TILE_BLOCKED or PROJECTILE_EAST_BLOCKED or PROJECTILE_NORTH_EAST_BLOCKED or PROJECTILE_NORTH_BLOCKED) != 0 || getClipping(
                                    currentX + i - 1, currentY + i2, height
                                ) and (UNLOADED_TILE or  /* BLOCKED_TILE | */UNKNOWN or PROJECTILE_TILE_BLOCKED or PROJECTILE_EAST_BLOCKED) != 0 || getClipping(
                                    currentX + i, currentY + i2 - 1, height
                                ) and (UNLOADED_TILE or  /* BLOCKED_TILE | */UNKNOWN or PROJECTILE_TILE_BLOCKED or PROJECTILE_NORTH_BLOCKED) != 0
                            ) {
                                return false
                            }
                        } else if (diffX > 0 && diffY > 0) {
                            if (getClipping(
                                    currentX + i + 1, currentY + i2 + 1, height
                                ) and (UNLOADED_TILE or  /* BLOCKED_TILE | */UNKNOWN or PROJECTILE_TILE_BLOCKED or PROJECTILE_WEST_BLOCKED or PROJECTILE_SOUTH_WEST_BLOCKED or PROJECTILE_SOUTH_BLOCKED) != 0 || getClipping(
                                    currentX + i + 1, currentY + i2, height
                                ) and (UNLOADED_TILE or  /* BLOCKED_TILE | */UNKNOWN or PROJECTILE_TILE_BLOCKED or PROJECTILE_WEST_BLOCKED) != 0 || getClipping(
                                    currentX + i, currentY + i2 + 1, height
                                ) and (UNLOADED_TILE or  /* BLOCKED_TILE | */UNKNOWN or PROJECTILE_TILE_BLOCKED or PROJECTILE_SOUTH_BLOCKED) != 0
                            ) {
                                return false
                            }
                        } else if (diffX < 0 && diffY > 0) {
                            if (getClipping(
                                    currentX + i - 1, currentY + i2 + 1, height
                                ) and (UNLOADED_TILE or  /* BLOCKED_TILE | */UNKNOWN or PROJECTILE_TILE_BLOCKED or PROJECTILE_SOUTH_BLOCKED or PROJECTILE_SOUTH_EAST_BLOCKED or PROJECTILE_EAST_BLOCKED) != 0 || getClipping(
                                    currentX + i - 1, currentY + i2, height
                                ) and (UNLOADED_TILE or  /* BLOCKED_TILE | */UNKNOWN or PROJECTILE_TILE_BLOCKED or PROJECTILE_EAST_BLOCKED) != 0 || getClipping(
                                    currentX + i, currentY + i2 + 1, height
                                ) and (UNLOADED_TILE or  /* BLOCKED_TILE | */UNKNOWN or PROJECTILE_TILE_BLOCKED or PROJECTILE_SOUTH_BLOCKED) != 0
                            ) {
                                return false
                            }
                        } else if (diffX > 0 && diffY < 0) {
                            if (getClipping(
                                    currentX + i + 1, currentY + i2 - 1, height
                                ) and (UNLOADED_TILE or  /* BLOCKED_TILE | */UNKNOWN or PROJECTILE_TILE_BLOCKED or PROJECTILE_WEST_BLOCKED or PROJECTILE_NORTH_BLOCKED or PROJECTILE_NORTH_WEST_BLOCKED) != 0 || getClipping(
                                    currentX + i + 1, currentY + i2, height
                                ) and (UNLOADED_TILE or  /* BLOCKED_TILE | */UNKNOWN or PROJECTILE_TILE_BLOCKED or PROJECTILE_WEST_BLOCKED) != 0 || getClipping(
                                    currentX + i, currentY + i2 - 1, height
                                ) and (UNLOADED_TILE or  /* BLOCKED_TILE | */UNKNOWN or PROJECTILE_TILE_BLOCKED or PROJECTILE_NORTH_BLOCKED) != 0
                            ) {
                                return false
                            }
                        } else if (diffX > 0 && diffY == 0) {
                            if (getClipping(
                                    currentX + i + 1, currentY + i2, height
                                ) and (UNLOADED_TILE or  /* BLOCKED_TILE | */UNKNOWN or PROJECTILE_TILE_BLOCKED or PROJECTILE_WEST_BLOCKED) != 0
                            ) {
                                return false
                            }
                        } else if (diffX < 0 && diffY == 0) {
                            if (getClipping(
                                    currentX + i - 1, currentY + i2, height
                                ) and (UNLOADED_TILE or  /* BLOCKED_TILE | */UNKNOWN or PROJECTILE_TILE_BLOCKED or PROJECTILE_EAST_BLOCKED) != 0
                            ) {
                                return false
                            }
                        } else if (diffX == 0 && diffY > 0) {
                            if (getClipping(
                                    currentX + i, currentY + i2 + 1, height
                                ) and (UNLOADED_TILE or  /*
																	 * BLOCKED_TILE
																	 * |
																	 */UNKNOWN or PROJECTILE_TILE_BLOCKED or PROJECTILE_SOUTH_BLOCKED) != 0
                            ) {
                                return false
                            }
                        } else if (diffX == 0 && diffY < 0) {
                            if (getClipping(
                                    currentX + i, currentY + i2 - 1, height
                                ) and (UNLOADED_TILE or  /*
																	 * BLOCKED_TILE
																	 * |
																	 */UNKNOWN or PROJECTILE_TILE_BLOCKED or PROJECTILE_NORTH_BLOCKED) != 0
                            ) {
                                return false
                            }
                        }
                    }
                }
                if (diffX < 0) {
                    diffX++
                } else if (diffX > 0) {
                    diffX--
                }
                if (diffY < 0) {
                    diffY++ // change
                } else if (diffY > 0) {
                    diffY--
                }
            }
            return true
        }

        fun isInDiagonalBlock(
            attacked: CharacterEntity, attacker: CharacterEntity
        ): Boolean {
            return (attacked.entityPosition.x - 1 == attacker.entityPosition.x && attacked.entityPosition.y + 1 == attacker.entityPosition.y) || (attacker.entityPosition.x - 1 == attacked.entityPosition.x && attacker.entityPosition.y + 1 == attacked.entityPosition.y) || (attacked.entityPosition.x + 1 == attacker.entityPosition.x && attacked.entityPosition.y - 1 == attacker.entityPosition.y) || (attacker.entityPosition.x + 1 == attacked.entityPosition.x && attacker.entityPosition.y - 1 == attacked.entityPosition.y) || (attacked.entityPosition.x + 1 == attacker.entityPosition.x && attacked.entityPosition.y + 1 == attacker.entityPosition.y) || (attacker.entityPosition.x + 1 == attacked.entityPosition.x && attacker.entityPosition.y + 1 == attacked.entityPosition.y)
        }

        const val PROJECTILE_NORTH_WEST_BLOCKED = 0x200
        const val PROJECTILE_NORTH_BLOCKED = 0x400
        const val PROJECTILE_NORTH_EAST_BLOCKED = 0x800
        const val PROJECTILE_EAST_BLOCKED = 0x1000
        const val PROJECTILE_SOUTH_EAST_BLOCKED = 0x2000
        const val PROJECTILE_SOUTH_BLOCKED = 0x4000
        const val PROJECTILE_SOUTH_WEST_BLOCKED = 0x8000
        const val PROJECTILE_WEST_BLOCKED = 0x10000
        const val PROJECTILE_TILE_BLOCKED = 0x20000
        const val UNKNOWN = 0x80000
        const val BLOCKED_TILE = 0x200000
        const val UNLOADED_TILE = 0x1000000
        const val OCEAN_TILE = 2097152
    }
}