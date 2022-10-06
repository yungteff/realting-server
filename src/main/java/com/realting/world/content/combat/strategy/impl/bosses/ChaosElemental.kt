package com.realting.world.content.combat.strategy.impl.bosses

import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.*
import com.realting.model.entity.character.CharacterEntity
import com.realting.model.entity.character.npc.NPC
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import com.realting.world.clip.region.RegionClipping
import com.realting.world.content.combat.CombatContainer
import com.realting.world.content.combat.CombatType
import com.realting.world.content.combat.strategy.CombatStrategy

class ChaosElemental : CombatStrategy {
    private enum class elementalData(startGfx: Graphic, projectile: Graphic?, endGraphic: Graphic?) {
        MELEE(Graphic(553, GraphicHeight.HIGH), Graphic(554, GraphicHeight.MIDDLE), null), RANGED(
            Graphic(
                665,
                GraphicHeight.HIGH
            ), null, Graphic(552, GraphicHeight.HIGH)
        ),
        MAGIC(Graphic(550, GraphicHeight.HIGH), Graphic(551, GraphicHeight.MIDDLE), Graphic(555, GraphicHeight.HIGH));

        var startGraphic: Graphic?
        var projectileGraphic: Graphic?
        var endGraphic: Graphic?

        init {
            startGraphic = startGfx
            projectileGraphic = projectile
            this.endGraphic = endGraphic
        }

        val combatType: CombatType
            get() {
                return when (this) {
                    MAGIC -> CombatType.MAGIC
                    MELEE -> CombatType.MELEE
                    RANGED -> CombatType.RANGED
                }
                return CombatType.MELEE
            }

        companion object {
            fun forId(id: Int): elementalData? {
                for (data in values()) {
                    if (data.ordinal == id) return data
                }
                return null
            }
        }
    }

    override fun canAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        return true
    }

    override fun attack(entity: CharacterEntity?, victim: CharacterEntity?): CombatContainer? {
        return null
    }

    override fun customContainerAttack(entity: CharacterEntity?, victim: CharacterEntity?): Boolean {
        val cE = entity as NPC?
        if (cE!!.isChargingAttack || victim!!.constitution <= 0) {
            return true
        }
        cE.movementQueue.reset()
        cE.setEntityInteraction(victim)
        val attackStyle = Misc.getRandom(2) //0 = melee, 1 = range, 2 =mage
        val data = elementalData.forId(attackStyle)
        if (data!!.startGraphic != null) data.startGraphic?.let { cE.performGraphic(it) }
        cE.performAnimation(Animation(cE.definition.attackAnimation))
        if (data.projectileGraphic != null) Projectile(
            cE,
            victim,
            data.projectileGraphic!!.id,
            44,
            3,
            43,
            31,
            0
        ).sendProjectile()
        cE.isChargingAttack = true
        TaskManager.submit(object : Task(1, cE, false) {
            public override fun execute() {
                cE.combatBuilder.container = CombatContainer(cE, victim!!, 1, 2, data.combatType, true)
                if (data.endGraphic != null) victim.performGraphic(data.endGraphic!!)
                cE.isChargingAttack = false
                if (Misc.getRandom(50) <= 2) {
                    cE.performGraphic(teleGraphic)
                    victim.performGraphic(teleGraphic)
                    val randomX = victim.position.x - Misc.getRandom(30)
                    val randomY = victim.position.y
                    if (RegionClipping.getClipping(randomX, randomY, 0) == 0) {
                        victim.moveTo(Position(randomX, randomY))
                        (victim as Player?)!!.packetSender.sendMessage("The Chaos elemental has teleported you away!")
                    }
                }
                stop()
            }
        })
        return true
    }

    override fun attackDelay(entity: CharacterEntity?): Int {
        return entity!!.attackSpeed
    }

    override fun attackDistance(entity: CharacterEntity): Int {
        return 8
    }

    override fun getCombatType(entity: CharacterEntity): CombatType? {
        return CombatType.MIXED
    }

    companion object {
        private val teleGraphic = Graphic(661)
    }
}