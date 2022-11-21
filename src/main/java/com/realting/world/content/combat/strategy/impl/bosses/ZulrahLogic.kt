package com.realting.world.content.combat.strategy.impl.bosses

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.*
import com.realting.model.RegionInstance.RegionInstanceType
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import com.realting.world.World
import com.realting.world.content.combat.CombatContainer
import com.realting.world.content.combat.CombatType
import com.realting.world.content.combat.strategy.CombatStrategy
import org.apache.commons.lang3.StringUtils

class ZulrahLogic : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return true
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        return null
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        if (entity!!.entityPosition.z == 0) {
            World.deregister(entity)
        }
        if (victim!!.constitution <= 0) {
            World.deregister(entity)
        }
        val zulrah = entity as NPC?
        val player = victim as Player?
        if (zulrah!!.isChargingAttack) {
            return true
        }
        if (zulrah.id == phase[0]) { //do green phase
            if (Misc.getRandom(1) == 0) {
                TaskManager.submit(object : Task(zulrah.attackSpeed, zulrah, false) {
                    var tick = 0
                    public override fun execute() {
                        if (entity == null || victim == null || zulrah.constitution <= 0 || player!!.constitution <= 0 || zulrah.location !== player.location) {
                            stop()
                            player!!.minigameAttributes.zulrahAttributes.setRedFormDamage(0, false)
                            zulrah.isChargingAttack = false
                            if (player.regionInstance != null && player.regionInstance.type == RegionInstanceType.ZULRAH) {
                                World.deregister(zulrah)
                                player.regionInstance.destruct()
                            }
                            return
                        }
                        if (tick >= ticksPerPhase) {
                            stop()
                            player.minigameAttributes.zulrahAttributes.setRedFormDamage(0, false)
                            zulrah.isChargingAttack = false
                            switchPhase(entity, victim)
                            return
                        }
                        if (Misc.isEven(tick)) {
                            zulrah.isChargingAttack = true
                            zulrah.combatBuilder.container =
                                CombatContainer(zulrah, victim, 1, 1, CombatType.MAGIC, true)
                            zulrah.performAnimation(shoot)
                            Projectile(zulrah, victim, 2733, 44, 3, 43, 31, 0).sendProjectile() //fire blast
                        } else { //do range attack
                            zulrah.isChargingAttack = true
                            zulrah.combatBuilder.container =
                                CombatContainer(zulrah, victim, 1, 1, CombatType.RANGED, true)
                            zulrah.performAnimation(shoot)
                            Projectile(zulrah, victim, 551, 44, 3, 43, 31, 0).sendProjectile() //chaos elemental green
                        }
                        tick++
                    }
                })
            } else {
                TaskManager.submit(object : Task(zulrah.attackSpeed, zulrah, false) {
                    var tick = 0
                    public override fun execute() {
                        if (zulrah.constitution <= 0 || player!!.constitution <= 0 || zulrah.location !== player.location) {
                            stop()
                            player!!.minigameAttributes.zulrahAttributes.setRedFormDamage(0, false)
                            zulrah.isChargingAttack = false
                            if (player.regionInstance != null && player.regionInstance.type == RegionInstanceType.ZULRAH) {
                                World.deregister(zulrah)
                                player.regionInstance.destruct()
                            }
                            return
                        }
                        if (tick >= ticksPerPhase) {
                            stop()
                            player.minigameAttributes.zulrahAttributes.setRedFormDamage(0, false)
                            zulrah.isChargingAttack = false
                            switchPhase(entity, victim)
                            return
                        }
                        zulrah.isChargingAttack = true
                        zulrah.combatBuilder.container = CombatContainer(zulrah, victim, 1, 1, CombatType.RANGED, true)
                        zulrah.performAnimation(shoot)
                        Projectile(zulrah, victim, 551, 44, 3, 43, 31, 0).sendProjectile() //chaos elemental green
                        tick++
                    }
                })
            }
        }
        if (zulrah.id == phase[1]) { //do blue phase
            if (Misc.getRandom(1) == 0) {
                TaskManager.submit(object : Task(zulrah.attackSpeed, zulrah, false) {
                    var tick = 0
                    public override fun execute() {
                        if (entity == null || victim == null || zulrah.constitution <= 0 || player!!.constitution <= 0 || zulrah.location !== player.location) {
                            stop()
                            player!!.minigameAttributes.zulrahAttributes.setRedFormDamage(0, false)
                            zulrah.isChargingAttack = false
                            if (player.regionInstance != null && player.regionInstance.type == RegionInstanceType.ZULRAH) {
                                World.deregister(zulrah)
                                player.regionInstance.destruct()
                            }
                            return
                        }
                        if (tick >= ticksPerPhase) {
                            //zulrah.forceChat("phase done");
                            stop()
                            player.minigameAttributes.zulrahAttributes.setRedFormDamage(0, false)
                            zulrah.isChargingAttack = false
                            switchPhase(entity, victim)
                            return
                        }
                        zulrah.isChargingAttack = true
                        if (Misc.inclusiveRandom(1, 3) < 3) {
                            zulrah.combatBuilder.container =
                                CombatContainer(zulrah, victim, 1, 1, CombatType.MAGIC, true)
                            zulrah.performAnimation(shoot)
                            Projectile(zulrah, victim, 2733, 44, 3, 43, 31, 0).sendProjectile() //fire blast
                        } else { //do range attack
                            zulrah.combatBuilder.container =
                                CombatContainer(zulrah, victim, 1, 1, CombatType.RANGED, true)
                            zulrah.performAnimation(shoot)
                            Projectile(zulrah, victim, 551, 44, 3, 43, 31, 0).sendProjectile() //chaos elemental green
                        }
                        tick++
                    }
                })
            } else {
                TaskManager.submit(object : Task(zulrah.attackSpeed, zulrah, false) {
                    var tick = 0
                    public override fun execute() {
                        if (entity == null || victim == null || zulrah.constitution <= 0 || player!!.constitution <= 0 || zulrah.location !== player.location) {
                            stop()
                            player!!.minigameAttributes.zulrahAttributes.setRedFormDamage(0, false)
                            zulrah.isChargingAttack = false
                            if (player.regionInstance != null && player.regionInstance.type == RegionInstanceType.ZULRAH) {
                                World.deregister(zulrah)
                                player.regionInstance.destruct()
                            }
                            return
                        }
                        if (tick >= ticksPerPhase) {
                            stop()
                            player.minigameAttributes.zulrahAttributes.setRedFormDamage(0, false)
                            zulrah.isChargingAttack = false
                            switchPhase(entity, victim)
                            return
                        }
                        zulrah.isChargingAttack = true
                        zulrah.combatBuilder.container = CombatContainer(zulrah, victim, 1, 1, CombatType.MAGIC, true)
                        zulrah.performAnimation(shoot)
                        Projectile(zulrah, victim, 2733, 44, 3, 43, 31, 0).sendProjectile() //fire blast
                        tick++
                    }
                })
            }
        }
        if (zulrah.id == phase[2]) { //do red phase
            //zulrah.forceChat("Current position: "+zulrah.getPosition().getX()+", "+zulrah.getPosition().getY()+", "+zulrah.getPosition().getZ());
            TaskManager.submit(object : Task(zulrah.attackSpeed, zulrah, false) {
                var tick = 0
                public override fun execute() {
                    if (entity == null || victim == null || zulrah.constitution <= 0 || player!!.constitution <= 0 || zulrah.location !== player.location) {
                        player!!.minigameAttributes.zulrahAttributes.setRedFormDamage(0, false)
                        stop()
                        zulrah.isChargingAttack = false
                        if (player.regionInstance != null && player.regionInstance.type == RegionInstanceType.ZULRAH) {
                            World.deregister(zulrah)
                            player.regionInstance.destruct()
                        }
                        return
                    }
                    zulrah.isChargingAttack = true
                    if (player.minigameAttributes.zulrahAttributes.redFormDamage >= player.skillManager.getCurrentLevel(
                            Skill.CONSTITUTION
                        )
                    ) {
                        var hiss = "Hi"
                        hiss = hiss + StringUtils.repeat("s", Misc.inclusiveRandom(2, 8))
                        hiss = hiss + StringUtils.repeat("!", Misc.inclusiveRandom(1, 4))
                        zulrah.forceChat(hiss)
                    }
                    if (tick < ticksPerPhase - 5) {
                        zulrah.combatBuilder.container = CombatContainer(zulrah, victim, 1, 1, CombatType.MAGIC, true)
                        zulrah.performAnimation(shoot)
                        Projectile(zulrah, victim, 2215, 44, 3, 43, 31, 0).sendProjectile() //fire blast
                    }
                    if (tick == ticksPerPhase - 5) {
                        zulrah.performAnimation(melee)
                    }
                    if (tick >= ticksPerPhase) {
                        player.dealDoubleDamage(
                            Hit(
                                zulrah,
                                player.minigameAttributes.zulrahAttributes.redFormDamage / 2,
                                Hitmask.LIGHT_YELLOW,
                                CombatIcon.DEFLECT
                            ),
                            Hit(
                                zulrah,
                                player.minigameAttributes.zulrahAttributes.redFormDamage / 2,
                                Hitmask.LIGHT_YELLOW,
                                CombatIcon.DEFLECT
                            )
                        )
                        stop()
                        player.minigameAttributes.zulrahAttributes.setRedFormDamage(0, false)
                        zulrah.isChargingAttack = false
                        switchPhase(entity, victim)
                        return
                    }
                    tick++
                }
            })
        }
        return true
    }

    override fun attackDelay(entity: CharacterEntity?): Int {
        return entity!!.attackSpeed
    }

    override fun attackDistance(entity: CharacterEntity): Int {
        return 20
    }

    override fun getCombatType(entity: CharacterEntity): CombatType? {
        return CombatType.MIXED
    }

    companion object {
        var ticksPerPhase = 20
        @JvmField
		var phase = intArrayOf(2042, 2044, 2043)
        @JvmField
		var move = arrayOf(
            Position(3431, 2781), Position(3421, 2771), Position(3423, 2781),
            Position(3415, 2773)
        ) //new Position(3415, 2780), 
        private val shoot = Animation(5069)
        private val charge = Animation(5806)
        private val melee = Animation(5807)
        private val dive = Animation(5072)
        private val rise = Animation(5073)
        private val toxic_cloud = Graphic(310)
        private val fire = Graphic(78)
        private val snakeling_summon = Graphic(281)
        private fun switchPhase(entity: CharacterEntity, victim: CharacterEntity?) {
            //System.out.println("Switching phase...");
            val zulrah = entity as NPC
            val player = victim as Player?
            zulrah.performAnimation(dive)
            val currenthealth = zulrah.constitution
            if (zulrah != null && zulrah.constitution > 0 && zulrah.isRegistered) {
                World.deregister(zulrah)
                TaskManager.submit(object : Task(1, player, false) {
                    var tick = 0
                    public override fun execute() {
                        if (tick == 5 && !zulrah.isRegistered) {
                            if (victim == null || player!!.constitution <= 0 || player.location !== Locations.Location.ZULRAH || zulrah.constitution <= 0) {
                                stop()
                                player!!.minigameAttributes.zulrahAttributes.setRedFormDamage(0, false)
                                zulrah.isChargingAttack = false
                                if (player.regionInstance != null && player.regionInstance.type == RegionInstanceType.ZULRAH) {
                                    World.deregister(zulrah)
                                    player.regionInstance.destruct()
                                }
                                return
                            }
                            val rand = Misc.randomMinusOne(move.size)
                            val zulrah = NPC(
                                phase[Misc.randomMinusOne(phase.size)], Position(
                                    move[rand].x, move[rand].y, player.entityPosition.z
                                )
                            )
                            World.register(zulrah)
                            zulrah.positionToFace = player.entityPosition
                            zulrah.performAnimation(rise)
                            zulrah.constitution = currenthealth
                            zulrah.combatBuilder.attack(player)
                            stop()
                        }
                        tick++
                    }
                })
            }
        }
    }
}