package com.realting.world.content.combat.strategy.impl.bosses

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.Animation
import com.realting.model.Graphic
import com.realting.model.Position
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.npc.NPCMovementCoordinator
import com.realting.util.Misc
import com.realting.util.Stopwatch
import com.realting.world.World
import com.realting.world.content.combat.CombatContainer
import com.realting.world.content.combat.CombatType
import com.realting.world.content.combat.strategy.CombatStrategy

class OLDNewZulrah : CombatStrategy {
    //private static TilePointer tile;
    //private static ArrayList<TilePointer> poisonedTiles = new ArrayList<TilePointer>();
    private val shoot = Animation(5069)
    private val charge = Animation(5806)
    private val melee_attack = Animation(5807)
    private val toxic_cloud = Graphic(310)
    private val fire = Graphic(78)
    private val snakeling_summon = Graphic(281)
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        //System.out.println("canAttack called");
        return true
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        // TODO Auto-generated method stub
        //System.out.println("attack called");
        return null
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        // TODO 
        return true
    }

    override fun attackDelay(entity: CharacterEntity?): Int {
        //System.out.println("attackDelay called");
        return entity!!.attackSpeed
    }

    override fun attackDistance(entity: CharacterEntity): Int {
        //System.out.println("attackDistance called");
        return 15
    }

    override fun getCombatType(entity: CharacterEntity): CombatType? {
        //System.out.println("combatType called");
        return CombatType.MIXED
    }

    companion object {
        var ZULRAH: NPC? = null
        private var zulrahId = 0
        private var zulrahHp = 0
        private var firstForm = false
        private var isDiving = false
        private const val firstCall = false
        private const val venom = false
        private val venomTime = Stopwatch()
        private val snakelingsKilled: BooleanArray = TODO()
        private var form = 0
        private const val defaultConstitution = 10000
        private var phase = 0
        private var prayerType = 0
        private var prayerTimer = 0
        private val moveX = intArrayOf(3366, 3360, 3370, 3363, 3356, 3369)
        private val moveY = intArrayOf(3800, 3801, 3805, 3818, 3812, 3809)
        private const val randomCoord = 0
        private var dir = -1
        private var zulrahPosition: Position? = null
        private val venomPosition: Position? = null
        private val snakelingPosition: Position? = null
        private val TILE: NPC? = null
        private val TILE2: NPC? = null
        private val SNAKELING: NPC? = null
        private val dive = Animation(5072)
        private val rise = Animation(5073)

        /**
         * Handles the spawning of [OLDZulrah].
         *
         * @param firstSpawn
         * Determines whether this is the first time the method is being called.
         */
        fun spawn() {
            if (ZULRAH != null && ZULRAH!!.isRegistered) {
                println("Could not spawn another, as ZULRAH is registered.")
            } else {
                getDir()
                zulrahId = 2042
                zulrahHp = 2000
                zulrahPosition = Position(moveX[dir], moveY[dir])
                firstForm = true
                ZULRAH = NPC(zulrahId, zulrahPosition)
                ZULRAH!!.movementCoordinator.coordinator = NPCMovementCoordinator.Coordinator(true, 3)
                World.register(ZULRAH)
                ZULRAH!!.performAnimation(rise)
                ZULRAH!!.defaultConstitution = defaultConstitution
                ZULRAH!!.constitution = defaultConstitution
                form = 1
                println("Spawned completed")
            }
        }

        fun despawn() {
            println("Despawn called")
            ZULRAH!!.performAnimation(dive)
            TaskManager.submit(object : Task(1, ZULRAH, false) {
                var tick = 0
                public override fun execute() {
                    if (tick == 3) {
                        zulrahHp = ZULRAH!!.constitution
                        phase = 2
                        if (ZULRAH != null && ZULRAH!!.isRegistered) World.deregister(ZULRAH)
                        dir = -1
                        form = -1
                        stop()
                    }
                    tick++
                }
            })
            println("Despawn completed")
        }

        fun move() {
            if (ZULRAH != null && ZULRAH!!.isRegistered) {
                zulrahHp = ZULRAH!!.constitution
                World.deregister(ZULRAH)
            }
            println("deregistered zulrah, zulrahHp = " + zulrahHp)
            TaskManager.submit(object : Task(4, ZULRAH, false) {
                public override fun execute() {
                    val newpos = newDir()
                    zulrahPosition = Position(moveX[newpos], moveY[newpos])
                    zulrahId = newForm()
                    prayerTimer = 0
                    prayerType = prayerTimer
                    phase = prayerType
                    println("Zulrah: " + zulrahId + ", phase = " + phase + ", Moved to newpos: " + newpos + ", x: " + moveX[newpos] + ", y: " + moveY[newpos])
                    isDiving = false
                    println("IsDiving false")
                    ZULRAH = NPC(zulrahId, zulrahPosition)
                    println("Declared new NPC")
                    ZULRAH!!.movementCoordinator.coordinator = NPCMovementCoordinator.Coordinator(true, 10)
                    println("Set ZULRAH movement")
                    World.register(ZULRAH)
                    println("Registered zulrah")
                    ZULRAH!!.performAnimation(rise)
                    println("zulrahHp = " + zulrahHp + ", Zulrah constitution " + ZULRAH!!.constitution)
                    ZULRAH!!.defaultConstitution = zulrahHp
                    ZULRAH!!.constitution = zulrahHp
                    println("done moving " + ZULRAH!!.constitution)
                    ZULRAH!!.forcedChat = "HISSSS!"
                    stop()
                }
            })
        }

        private fun getForm(): Int {
            if (ZULRAH == null) {
                return -1
            }
            if (!ZULRAH!!.isRegistered) {
                return -1
            }
            return if (ZULRAH!!.id == 2042) {
                1 //green
            } else if (ZULRAH!!.id == 2043) {
                2 //red
            } else if (ZULRAH!!.id == 2044) {
                3 //blue
            } else {
                -404 //ERROR
            }
        }

        private fun newForm(): Int {
            val current = getForm()
            val aNewForm = 2042 + Misc.getRandom(2)
            return if (current == aNewForm) {
                newForm() //TODO FIX
            } else aNewForm
        }

        private fun getDir(): Int {
            if (dir == -1) {
                val newdir = Misc.getRandom(moveX.size - 1)
                dir = newdir
            }
            if (ZULRAH != null && ZULRAH!!.isRegistered && ZULRAH!!.entityPosition.x != moveX[dir]) {
                println("Error. Dir = " + dir + ", Zulrah's pos = " + ZULRAH!!.entityPosition.x + ", moveX[" + dir + "] = " + moveX[dir])
            }
            return dir
        }

        private fun newDir(): Int {
            val current = getDir()
            val newdir = Misc.getRandom(moveX.size - 1)
            if (ZULRAH != null && ZULRAH!!.isRegistered && ZULRAH!!.entityPosition.x != moveX[current]) {
                println("[ERROR 666] Zulrah's X = " + ZULRAH!!.entityPosition.x + ", dir = " + current + ", moveX[" + current + "] = " + moveX[current])
                return 666
            }
            if (newdir == current) {
                println("[ERROR 2] Newdir == current ($current). Running again.")
                return newDir() //TODO FIX
            }
            return newdir
        }
    }
}