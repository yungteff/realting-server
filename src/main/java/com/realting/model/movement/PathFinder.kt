package com.realting.model.movement

import com.realting.model.Position
import com.realting.model.entity.character.CharacterEntity
import com.realting.world.clip.region.RegionClipping
import java.util.*

object PathFinder {
    @JvmStatic
    fun findPath(
        character: CharacterEntity, destX: Int, destY: Int, moveNear: Boolean, xLength: Int, yLength: Int
    ) {
        var destXInline = destX
        var destYInline = destY
        try {
            if (destXInline == character.position.localX && destYInline == character.position.localY && !moveNear) {
                return
            }
            val height = character.position.z % 4
            destXInline -= 8 * character.position.regionX
            destYInline -= 8 * character.position.regionY
            val via = Array(104) { IntArray(104) }
            val cost = Array(104) { IntArray(104) }
            val tileQueueX = LinkedList<Int>()
            val tileQueueY = LinkedList<Int>()
            for (xx in 0..103) for (yy in 0..103) cost[xx][yy] = 99999999
            var curX = character.position.localX
            var curY = character.position.localY
            if (curX > via.size - 1 || curY > via[curX].size - 1) return
            if (curY < via[0].size) via[curX][curY] = 99
            if (curX < cost.size && curY < cost[0].size) cost[curX][curY] = 0
            val head = 0
            var tail = 0
            //TODO:: player vs player combat clipping needs work
            //val curAbsX = character.position.regionX * 8 + curX seemed to make it better

            tileQueueX.add(curX)
            tileQueueY.add(curY)
            var foundPath = false
            val pathLength = 4000
            while (tail != tileQueueX.size && tileQueueX.size < pathLength) {
                curX = tileQueueX[tail]
                curY = tileQueueY[tail]
                val curAbsX = character.position.regionX * 8 + curX
                val curAbsY = character.position.regionY * 8 + curY
                if (curX == destXInline && curY == destYInline) {
                    println("found path: $foundPath")
                    foundPath = true
                    break
                }
                tail = (tail + 1) % pathLength
                if (cost.size < curX || cost[curX].size < curY) return
                val thisCost = cost[curX][curY] + 1
                if (curY > 0 && via[curX][curY - 1] == 0 && RegionClipping.getClipping(
                        curAbsX, curAbsY - 1, height
                    ) and 0x1280102 == 0
                ) {
                    tileQueueX.add(curX)
                    tileQueueY.add(curY - 1)
                    via[curX][curY - 1] = 1
                    cost[curX][curY - 1] = thisCost
                }
                if (curX > 0 && via[curX - 1][curY] == 0 && RegionClipping.getClipping(
                        curAbsX - 1, curAbsY, height
                    ) and 0x1280108 == 0
                ) {
                    tileQueueX.add(curX - 1)
                    tileQueueY.add(curY)
                    via[curX - 1][curY] = 2
                    cost[curX - 1][curY] = thisCost
                }
                if (curY < 104 - 1 && via[curX][curY + 1] == 0 && RegionClipping.getClipping(
                        curAbsX, curAbsY + 1, height
                    ) and 0x1280120 == 0
                ) {
                    tileQueueX.add(curX)
                    tileQueueY.add(curY + 1)
                    via[curX][curY + 1] = 4
                    cost[curX][curY + 1] = thisCost
                }
                if (curX < 104 - 1 && via[curX + 1][curY] == 0 && RegionClipping.getClipping(
                        curAbsX + 1, curAbsY, height
                    ) and 0x1280180 == 0
                ) {
                    tileQueueX.add(curX + 1)
                    tileQueueY.add(curY)
                    via[curX + 1][curY] = 8
                    cost[curX + 1][curY] = thisCost
                }
                if (curX > 0 && curY > 0 && via[curX - 1][curY - 1] == 0 && RegionClipping.getClipping(
                        curAbsX - 1, curAbsY - 1, height
                    ) and 0x128010e == 0 && RegionClipping.getClipping(
                        curAbsX - 1, curAbsY, height
                    ) and 0x1280108 == 0 && RegionClipping.getClipping(curAbsX, curAbsY - 1, height) and 0x1280102 == 0
                ) {
                    tileQueueX.add(curX - 1)
                    tileQueueY.add(curY - 1)
                    via[curX - 1][curY - 1] = 3
                    cost[curX - 1][curY - 1] = thisCost
                }
                if (curX > 0 && curY < 104 - 1 && via[curX - 1][curY + 1] == 0 && RegionClipping.getClipping(
                        curAbsX - 1, curAbsY + 1, height
                    ) and 0x1280138 == 0 && RegionClipping.getClipping(
                        curAbsX - 1, curAbsY, height
                    ) and 0x1280108 == 0 && RegionClipping.getClipping(curAbsX, curAbsY + 1, height) and 0x1280120 == 0
                ) {
                    tileQueueX.add(curX - 1)
                    tileQueueY.add(curY + 1)
                    via[curX - 1][curY + 1] = 6
                    cost[curX - 1][curY + 1] = thisCost
                }
                if (curX < 104 - 1 && curY > 0 && via[curX + 1][curY - 1] == 0 && RegionClipping.getClipping(
                        curAbsX + 1, curAbsY - 1, height
                    ) and 0x1280183 == 0 && RegionClipping.getClipping(
                        curAbsX + 1, curAbsY, height
                    ) and 0x1280180 == 0 && RegionClipping.getClipping(curAbsX, curAbsY - 1, height) and 0x1280102 == 0
                ) {
                    tileQueueX.add(curX + 1)
                    tileQueueY.add(curY - 1)
                    via[curX + 1][curY - 1] = 9
                    cost[curX + 1][curY - 1] = thisCost
                }
                if (curX < 104 - 1 && curY < 104 - 1 && via[curX + 1][curY + 1] == 0 && RegionClipping.getClipping(
                        curAbsX + 1, curAbsY + 1, height
                    ) and 0x12801e0 == 0 && RegionClipping.getClipping(
                        curAbsX + 1, curAbsY, height
                    ) and 0x1280180 == 0 && RegionClipping.getClipping(curAbsX, curAbsY + 1, height) and 0x1280120 == 0
                ) {
                    tileQueueX.add(curX + 1)
                    tileQueueY.add(curY + 1)
                    via[curX + 1][curY + 1] = 12
                    cost[curX + 1][curY + 1] = thisCost
                }
            }
            if (!foundPath) if (moveNear) {
                var i_223_ = 1000
                var thisCost = 100
                val i_225_ = 10
                for (x in destXInline - i_225_..destXInline + i_225_) for (y in destYInline - i_225_..destYInline + i_225_) if (x >= 0 && y >= 0 && x < 104 && y < 104 && cost[x][y] < 100) {
                    var i_228_ = 0
                    if (x < destXInline) i_228_ = destXInline - x else if (x > destXInline + xLength - 1) i_228_ =
                        x - (destXInline + xLength - 1)
                    var i_229_ = 0
                    if (y < destYInline) i_229_ = destYInline - y else if (y > destYInline + yLength - 1) i_229_ =
                        y - (destYInline + yLength - 1)
                    val i_230_ = i_228_ * i_228_ + i_229_ * i_229_
                    if (i_230_ < i_223_ || i_230_ == i_223_ && cost[x][y] < thisCost) {
                        i_223_ = i_230_
                        thisCost = cost[x][y]
                        curX = x
                        curY = y
                    }
                }
                if (i_223_ == 1000) return
            } else return
            tail = 0
            tileQueueX[tail] = curX
            tileQueueY[tail++] = curY
            var l5: Int
            var j5 = via[curX][curY].also { l5 = it }
            while (curX != character.position.localX || curY != character.position.localY) {
                if (j5 != l5) {
                    l5 = j5
                    tileQueueX[tail] = curX
                    tileQueueY[tail++] = curY
                }
                if (j5 and 2 != 0) curX++ else if (j5 and 8 != 0) curX--
                if (j5 and 1 != 0) curY++ else if (j5 and 4 != 0) curY--
                j5 = via[curX][curY]
            }
            val size = tail--
            var pathX = character.position.regionX * 8 + tileQueueX[tail]
            var pathY = character.position.regionY * 8 + tileQueueY[tail]
            character.movementQueue.addFirstStep(Position(pathX, pathY, character.position.z))
            for (i in 1 until size) {
                tail--
                pathX = character.position.regionX * 8 + tileQueueX[tail]
                pathY = character.position.regionY * 8 + tileQueueY[tail]
                println("pathX: $pathX pathY: $pathX")
                character.movementQueue.addStep(Position(pathX, pathY, character.position.z))
            }
        } catch (e: Exception) {
            println("Error finding route, destx: $destXInline, destY: $destYInline. Reseted queue.")
            character.movementQueue.reset()
        }
    }
}