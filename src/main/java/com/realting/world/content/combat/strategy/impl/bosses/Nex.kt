package com.realting.world.content.combat.strategy.impl.bosses

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.*
import com.realting.model.definitions.NpcDefinition
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.npc.NPCMovementCoordinator
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import com.realting.world.World
import com.realting.world.content.CustomObjects
import com.realting.world.content.combat.CombatContainer
import com.realting.world.content.combat.CombatType
import com.realting.world.content.combat.HitQueue.CombatHit
import com.realting.world.content.combat.strategy.CombatStrategy

class Nex : CombatStrategy {
    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return victim!!.isPlayer && (victim as Player?)!!.minigameAttributes.godwarsDungeonAttributes.hasEnteredRoom()
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        return null
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        val p = victim as Player?
        if (p!!.constitution <= 0 || NEX!!.constitution <= 0) {
            return true
        }
        if (NEX!!.isChargingAttack || p.constitution <= 0) {
            return true
        }
        if (phase == 0) {
            val rnd = Misc.getRandom(10)
            if (rnd <= 2) {
                NEX!!.forceChat("Let the virus flow through you!")
                cough(p)
                NEX!!.performAnimation(Animation(6986))
                CombatHit(NEX!!.combatBuilder, CombatContainer(NEX!!, p, 1, CombatType.MAGIC, true)).handleAttack()
                return true
            }
            return if (p.entityPosition.distanceToPoint(NEX!!.entityPosition.x, NEX!!.entityPosition.y) <= 2 && Misc.getRandom(1) == 0) {
                NEX!!.performAnimation(Animation(6354))
                TaskManager.submit(object : Task(1, NEX, false) {
                    public override fun execute() {
                        CombatHit(
                            NEX!!.combatBuilder,
                            CombatContainer(NEX!!, p, 1, CombatType.MELEE, true)
                        ).handleAttack()
                        stop()
                    }
                })
                true
            } else {
                NEX!!.performAnimation(Animation(6326))
                p.performGraphic(Graphic(383))
                CombatHit(NEX!!.combatBuilder, CombatContainer(NEX!!, p, 1, CombatType.MAGIC, true)).handleAttack()
                true
            }
        }
        if (phase == 1) {
            val rnd = Misc.getRandom(20)
            if (rnd < 2 && !attacks[3]) {
                NEX!!.forceChat("Fear the shadow!")
                attacks[3] = true
                NEX!!.isChargingAttack = true
                for (p_ in Misc.getCombinedPlayerList(p)) {
                    if (p_ == null || p_.location !== Locations.Location.GODWARS_DUNGEON) continue
                    TaskManager.submit(object : Task(1, NEX, false) {
                        var origX = 0
                        var origY = 0
                        var ticks = 0
                        public override fun execute() {
                            if (ticks == 0) {
                                origX = p_.entityPosition.x
                                origY = p_.entityPosition.y
                            }
                            if (ticks == 5) {
                                if (origX == p_.entityPosition.x && origY == p_.entityPosition.y) {
                                    p_.dealDamage(Hit(NEX, 100 + Misc.getRandom(100), Hitmask.RED, CombatIcon.NONE))
                                    p_.packetSender.sendMessage("The shadows begin to damage you!")
                                    stop()
                                }
                            }
                            if (ticks == 10) {
                                stop()
                            }
                            ticks++
                        }

                        override fun stop() {
                            setEventRunning(false)
                            attacks[3] = false
                            NEX!!.isChargingAttack = false
                        }
                    })
                }
            } else if (rnd >= 5 && rnd <= 7 && !attacks[4]) {
                NEX!!.forceChat("Embrace darkness!")
                attacks[4] = true
                NEX!!.isChargingAttack = true
                for (p_ in Misc.getCombinedPlayerList(p)) {
                    if (p_ == null || p_.location !== Locations.Location.GODWARS_DUNGEON) continue
                    TaskManager.submit(object : Task(1, NEX, false) {
                        var ticks = 0
                        public override fun execute() {
                            if (ticks == 10) setShadow(p_, 250) else {
                                val dist = p_.entityPosition.distanceToPoint(NEX!!.entityPosition.x, NEX!!.entityPosition.y)
                                if (dist < 3) {
                                    p_.packetSender.sendMessage("The shadows begin to consume you!")
                                    p_.dealDamage(Hit(NEX, 10, Hitmask.RED, CombatIcon.NONE))
                                    setShadow(p_, 20)
                                }
                                if (dist >= 3 && dist < 5) setShadow(p_, 40)
                                if (dist > 5) setShadow(p_, 90)
                            }
                            if (ticks >= 10) {
                                stop()
                            }
                            ticks++
                        }

                        override fun stop() {
                            setEventRunning(false)
                            attacks[4] = false
                            NEX!!.isChargingAttack = false
                        }
                    })
                }
                NEX!!.performAnimation(Animation(6984))
                CombatHit(NEX!!.combatBuilder, CombatContainer(NEX!!, p, 1, CombatType.MAGIC, true)).handleAttack()
                return true
            } else {
                return if (p.entityPosition.distanceToPoint(
                        NEX!!.entityPosition.x,
                        NEX!!.entityPosition.y
                    ) <= 2 && Misc.getRandom(1) == 0
                ) {
                    NEX!!.performAnimation(Animation(6354))
                    TaskManager.submit(object : Task(1, NEX, false) {
                        public override fun execute() {
                            CombatHit(
                                NEX!!.combatBuilder,
                                CombatContainer(NEX!!, p, 1, CombatType.MELEE, true)
                            ).handleAttack()
                            stop()
                        }
                    })
                    true
                } else {
                    NEX!!.performAnimation(Animation(6326))
                    NEX!!.performGraphic(Graphic(378))
                    CombatHit(NEX!!.combatBuilder, CombatContainer(NEX!!, p, 1, CombatType.MAGIC, true)).handleAttack()
                    true
                }
            }
        }
        if (phase == 2) {
            if (p.entityPosition.distanceToPoint(NEX!!.entityPosition.x, NEX!!.entityPosition.y) <= 2 && Misc.getRandom(1) == 0) {
                NEX!!.performAnimation(Animation(6354))
                TaskManager.submit(object : Task(1, NEX, false) {
                    public override fun execute() {
                        CombatHit(
                            NEX!!.combatBuilder,
                            CombatContainer(NEX!!, p, 1, CombatType.MELEE, true)
                        ).handleAttack()
                        stop()
                    }
                })
                return true
            }
            return true
        }
        if (phase == 4) {
            prayerTimer++
            if (prayerTimer == 4) {
                if (prayerType == 0) {
                    prayerType = 1
                    NEX!!.transform(13448)
                } else {
                    prayerType = 0
                    NEX!!.transform(13450)
                }
                prayerTimer = 0
            }
            return if (p.entityPosition.distanceToPoint(NEX!!.entityPosition.x, NEX!!.entityPosition.y) <= 2 && Misc.getRandom(1) == 0) {
                NEX!!.performAnimation(Animation(6354))
                TaskManager.submit(object : Task(1, NEX, false) {
                    public override fun execute() {
                        CombatHit(
                            NEX!!.combatBuilder,
                            CombatContainer(NEX!!, p, 1, CombatType.MELEE, true)
                        ).handleAttack()
                        stop()
                    }
                })
                true
            } else {
                NEX!!.performAnimation(Animation(6326))
                NEX!!.performGraphic(Graphic(373))
                CombatHit(NEX!!.combatBuilder, CombatContainer(NEX!!, p, 1, CombatType.MAGIC, true)).handleAttack()
                true
            }
        }
        if (phase == 3) {
            val rnd = Misc.getRandom(15)
            if (rnd >= 0 && rnd <= 3 && !attacks[0]) {
                attacks[0] = true
                NEX!!.forceChat("Die now, in a prison of ice!")
                NEX!!.isChargingAttack = true
                val origX = p.entityPosition.x
                val origY = p.entityPosition.y
                p.movementQueue.reset()
                for (x in origX - 1 until origX + 1) {
                    for (y in origY - 1 until origY + 1) {
                        CustomObjects.globalObjectRemovalTask(GameObject(57263, Position(x, y)), 5)
                    }
                }
                TaskManager.submit(object : Task(10, NEX, false) {
                    public override fun execute() {
                        if (p.entityPosition.x == origX && p.entityPosition.y == origY) {
                            p.dealDamage(Hit(NEX, 250 + Misc.getRandom(150), Hitmask.RED, CombatIcon.NONE))
                        }
                        for (x in origX - 1 until origX + 1) {
                            for (y in origY - 1 until origY + 1) {
                                CustomObjects.globalObjectRemovalTask(GameObject(6951, Position(x, y)), 5)
                            }
                        }
                        stop()
                    }

                    override fun stop() {
                        setEventRunning(false)
                        attacks[0] = false
                        NEX!!.isChargingAttack = false
                    }
                })
            } else if (rnd > 3 && rnd <= 5 && !attacks[1]) {
                NEX!!.forceChat("Contain this!")
                NEX!!.isChargingAttack = true
                attacks[1] = true
                val origX = NEX!!.entityPosition.x
                val origY = NEX!!.entityPosition.y
                for (x in origX - 2 until origX + 2) {
                    for (y in origY - 2 until origY + 2) {
                        if (x == origX - 2 || x == origX + 2 || y == origY - 2 || y == origY + 2) {
                            CustomObjects.globalObjectRemovalTask(GameObject(57262, Position(x, y)), 5)
                        }
                    }
                }
                TaskManager.submit(object : Task(1, NEX, false) {
                    var ticks = 0
                    public override fun execute() {
                        for (x in origX - ticks until origX + ticks) {
                            for (y in origY - ticks until origY + ticks) {
                                if (x == origX - ticks || y == origY - ticks || x == origX + ticks || y == origY + ticks) {
                                    p.packetSender.sendGraphic(Graphic(366), Position(x, y))
                                    for (p_ in Misc.getCombinedPlayerList(p)) {
                                        if (p_ == null || p_.location !== Locations.Location.GODWARS_DUNGEON) continue
                                        if (p_.entityPosition.x == x && p_.entityPosition.y == y) p_.dealDamage(
                                            Hit(
                                                NEX,
                                                150 + Misc.getRandom(110),
                                                Hitmask.RED,
                                                CombatIcon.NONE
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        if (ticks == 6) {
                            attacks[1] = false
                            NEX!!.isChargingAttack = false
                            stop()
                        }
                        ticks++
                    }
                })
            } else {
                if (p.entityPosition.distanceToPoint(NEX!!.entityPosition.x, NEX!!.entityPosition.y) <= 2 && Misc.getRandom(1) == 0) {
                    NEX!!.performAnimation(Animation(6354))
                    TaskManager.submit(object : Task(1, NEX, false) {
                        public override fun execute() {
                            CombatHit(
                                NEX!!.combatBuilder,
                                CombatContainer(NEX!!, p, 1, CombatType.MELEE, true)
                            ).handleAttack()
                            stop()
                        }
                    })
                } else {
                    p.performGraphic(Graphic(366))
                    p.movementQueue.freeze(5)
                    NEX!!.performAnimation(Animation(6326))
                    CombatHit(NEX!!.combatBuilder, CombatContainer(NEX!!, p, 1, CombatType.MAGIC, true)).handleAttack()
                }
            }
        }
        return true
    }

    override fun attackDelay(entity: CharacterEntity?): Int {
        return entity!!.attackSpeed
    }

    override fun attackDistance(entity: CharacterEntity): Int {
        return if (phase == 2) 2 else 8
    }

    override fun getCombatType(entity: CharacterEntity): CombatType? {
        return CombatType.MIXED
    }

    companion object {
        var NEX: NPC? = null
        var FUMUS: NPC? = null
        var UMBRA: NPC? = null
        var CRUOR: NPC? = null
        var GLACIES: NPC? = null
        private val magesKilled = BooleanArray(4)
        private val magesAttackable = BooleanArray(4)
        private val attacks = BooleanArray(18)
        private var zarosStage = false
        private var phase = 0
        private var prayerType = 0
        private var prayerTimer = 0
        @JvmStatic
		fun spawn() {
            prayerTimer = 0
            prayerType = prayerTimer
            phase = prayerType
            zarosStage = false
            for (i in 0..3) {
                magesAttackable[i] = false
                magesKilled[i] = magesAttackable[i]
            }
            for (i in 0..17) {
                attacks[i] = false
            }
            despawn(true)
            NEX = NPC(13447, Position(2925, 5203))
            NEX!!.movementCoordinator.coordinator = NPCMovementCoordinator.Coordinator(true, 3)
            FUMUS = NPC(13451, Position(2916, 5213))
            UMBRA = NPC(13452, Position(2934, 5213))
            CRUOR = NPC(13453, Position(2915, 5193))
            GLACIES = NPC(13454, Position(2935, 5193))
            FUMUS!!.movementCoordinator.coordinator = NPCMovementCoordinator.Coordinator(false, -1)
            UMBRA!!.movementCoordinator.coordinator = NPCMovementCoordinator.Coordinator(false, -1)
            CRUOR!!.movementCoordinator.coordinator = NPCMovementCoordinator.Coordinator(false, -1)
            GLACIES!!.movementCoordinator.coordinator = NPCMovementCoordinator.Coordinator(false, -1)
            World.register(NEX)
            World.register(FUMUS)
            World.register(UMBRA)
            World.register(CRUOR)
            World.register(GLACIES)
        }

        fun despawn(nex: Boolean) {
            if (nex) {
                if (NEX != null && NEX!!.isRegistered) World.deregister(NEX)
            }
            if (FUMUS != null && FUMUS!!.isRegistered) World.deregister(FUMUS)
            if (UMBRA != null && UMBRA!!.isRegistered) World.deregister(UMBRA)
            if (CRUOR != null && CRUOR!!.isRegistered) World.deregister(CRUOR)
            if (GLACIES != null && GLACIES!!.isRegistered) World.deregister(GLACIES)
        }

        @JvmStatic
		fun death(id: Int) {
            if (nexMinion(id)) {
                val index = id - 13451
                if (index >= 0) {
                    magesKilled[index] = true
                }
                return
            } else {
                TaskManager.submit(object : Task(65) {
                    override fun execute() {
                        spawn()
                        stop()
                    }
                })
            }
        }

        /** MISC  */
        fun dealtDamage(p: Player, damage: Int) {
            if (phase == 4) {
                if (prayerType == 0 && damage != 0) {
                    Projectile(NEX, p, 2263, 44, 3, 43, 43, 0).sendProjectile()
                    TaskManager.submit(object : Task(2, NEX, false) {
                        public override fun execute() {
                            NEX!!.constitution = NEX!!.constitution + damage / 5
                            p.skillManager.setCurrentLevel(
                                Skill.PRAYER,
                                p.skillManager.getCurrentLevel(Skill.PRAYER) - damage / 85
                            )
                            if (p.skillManager.getCurrentLevel(Skill.PRAYER) < 0) p.skillManager.setCurrentLevel(
                                Skill.PRAYER,
                                0
                            )
                            p.performGraphic(Graphic(2264))
                            Projectile(NEX, p, 2263, 44, 3, 43, 43, 0).sendProjectile()
                            stop()
                        }
                    })
                }
            }
        }

        fun takeDamage(from: Player, damage: Int) {
            if (phase == 4 && damage > 0) {
                if (prayerType == 0) {
                    NEX!!.constitution = NEX!!.constitution + damage / 2
                }
            }
            if (phase == 3) {
                if (NEX!!.constitution <= 4000) {
                    if (magesKilled[3]) {
                        phase = 4
                        NEX!!.forceChat("NOW, THE POWER OF ZAROS!")
                        zarosStage = true
                    }
                    if (!magesAttackable[3]) {
                        NEX!!.forceChat("Don't fail me, Glacies!")
                        sendGlobalMsg(from, "@red@Glacies is now attackable! You need to defeat him to weaken Nex!")
                        NEX!!.constitution = 4000
                        magesAttackable[3] = true
                    }
                    if (magesAttackable[3] && !magesKilled[3]) {
                        NEX!!.constitution = 4000
                        from.packetSender.sendMessage("You need to kill Glacies before being able to damage Nex further!")
                        from.combatBuilder.reset(true)
                    }
                }
            }
            if (phase == 2) {
                if (NEX!!.constitution <= 8000) {
                    if (magesKilled[2]) phase = 3
                    if (!magesAttackable[2]) {
                        NEX!!.forceChat("Don't fail me, Cruor!")
                        sendGlobalMsg(from, "@red@Cruor is now attackable! You need to defeat him to weaken Nex!")
                        NEX!!.constitution = 8000
                        magesAttackable[2] = true
                    }
                    if (magesAttackable[2] && !magesKilled[2]) {
                        NEX!!.constitution = 8000
                        from.packetSender.sendMessage("You need to kill Cruor before being able to damage Nex further!")
                        from.combatBuilder.reset(true)
                    }
                }
            }
            if (phase == 1) {
                if (NEX!!.constitution <= 12000) {
                    if (magesKilled[1]) phase = 2
                    if (!magesAttackable[1]) {
                        NEX!!.forceChat("Don't fail me, Umbra!")
                        sendGlobalMsg(from, "@red@Umbra is now attackable! You need to defeat him to weaken Nex!")
                        magesAttackable[1] = true
                    }
                    if (magesAttackable[1] && !magesKilled[1]) {
                        NEX!!.constitution = 12000
                        from.packetSender.sendMessage("You need to kill Umbra before being able to damage Nex further!")
                        from.combatBuilder.reset(true)
                    }
                }
            }
            if (phase == 0) {
                if (NEX!!.constitution <= 16000) {
                    if (magesKilled[0]) phase = 1
                    if (!magesAttackable[0]) {
                        NEX!!.forceChat("Don't fail me, Fumus!")
                        sendGlobalMsg(from, "@red@Fumus is now attackable! You need to defeat her to weaken Nex!")
                        magesAttackable[0] = true
                    }
                    if (magesAttackable[0] && !magesKilled[0]) {
                        NEX!!.constitution = 16000
                        from.packetSender.sendMessage("You need to kill Fumus before being able to damage Nex further!")
                        from.combatBuilder.reset(true)
                    }
                }
            }
        }

        @JvmStatic
		fun handleDeath() {
            phase = 0
            despawn(false)
            NEX!!.forceChat("Taste my wrath!")
            val x = NEX!!.entityPosition.x
            val y = NEX!!.entityPosition.y
            TaskManager.submit(object : Task(4) {
                public override fun execute() {
                    for (p in World.getPlayers()) {
                        if (p == null) continue
                        if (p.entityPosition.distanceToPoint(x, y) <= 3) {
                            p.dealDamage(Hit(NEX, 150, Hitmask.RED, CombatIcon.NONE))
                        }
                        if (p.entityPosition.distanceToPoint(x, y) <= 20) {
                            for (x_ in x - 2 until x + 2) {
                                for (y_ in y - 2 until y + 2) {
                                    p.packetSender.sendGraphic(Graphic(2259), Position(x_, y))
                                }
                            }
                        }
                    }
                    stop()
                }
            })
        }

        fun sendGlobalMsg(original: Player?, message: String?) {
            for (p in Misc.getCombinedPlayerList(original)) {
                p?.packetSender?.sendMessage(message)
            }
        }

        fun zarosStage(): Boolean {
            return zarosStage
        }

        @JvmStatic
		fun nexMob(id: Int): Boolean {
            return id == 13447 || nexMinion(id)
        }

        @JvmStatic
		fun nexMinion(id: Int): Boolean {
            return id >= 13451 && id <= 13454
        }

        fun checkAttack(p: Player, npc: Int): Boolean {
            if (npc == 13447) {
                for (i in magesAttackable.indices) {
                    if (magesAttackable[i] && !magesKilled[i]) {
                        val index = 13451 + i
                        p.packetSender.sendMessage("You need to kill " + NpcDefinition.forId(index).name + " before being able to damage Nex further!")
                        return false
                    }
                }
                return true
            }
            val index = npc - 13451
            if (!magesAttackable[index] && !magesKilled[index]) {
                p.packetSender.sendMessage("" + NpcDefinition.forId(npc).name + " is currently being protected by Nex!")
                return false
            }
            return true
        }

        fun cough(p: Player?) {
            if (p!!.isCoughing) return
            p.packetSender.sendMessage("You've been infected with a virus!")
            p.isCoughing = true
            TaskManager.submit(object : Task(1, p, false) {
                var ticks = 0
                public override fun execute() {
                    if (ticks >= 5 || p.constitution <= 0 || p.location !== Locations.Location.GODWARS_DUNGEON) {
                        stop()
                        return
                    }
                    p.forceChat("Cough..")
                    for (skill in Skill.values()) {
                        if (skill != Skill.CONSTITUTION && skill != Skill.PRAYER) {
                            p.skillManager.setCurrentLevel(skill, p.skillManager.getCurrentLevel(skill) - 1)
                            if (p.skillManager.getCurrentLevel(skill) < 1) p.skillManager.setCurrentLevel(skill, 1)
                        }
                    }
                    for (p2 in p.localPlayers) {
                        if (p2 == null || p2 === p) continue
                        if (p2.entityPosition.distanceToPoint(
                                p.entityPosition.x,
                                p.entityPosition.y
                            ) == 1.0 && p2.constitution > 0 && p2.location === Locations.Location.GODWARS_DUNGEON
                        ) {
                            cough(p2)
                        }
                    }
                    ticks++
                }

                override fun stop() {
                    setEventRunning(false)
                    p.isCoughing = false
                }
            })
        }

        fun setShadow(p: Player, shadow: Int) {
            if (p.shadowState == shadow) return
            p.shadowState = shadow
            p.packetSender.sendShadow().sendMessage("@whi@Nex calls upon shadows to endarken your vision!")
        }
    }
}