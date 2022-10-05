package com.realting.world.content.player.skill.dungeoneering

import com.realting.model.Skill
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc

object ItemBinding {
    private val BINDABLE_ITEMS = arrayOf(
        intArrayOf(15753, 16207),
        intArrayOf(16273, 15936),
        intArrayOf(16339, 16262),
        intArrayOf(16383, 16024),
        intArrayOf(16405, 16174),
        intArrayOf(16647, 16196),
        intArrayOf(16669, 15925),
        intArrayOf(16691, 15914),
        intArrayOf(16713, 16080),
        intArrayOf(16889, 16127),
        intArrayOf(16935, 16035),
        intArrayOf(17019, 16116),
        intArrayOf(17239, 16013),
        intArrayOf(17341, 15808),
        intArrayOf(15755, 16208),
        intArrayOf(16275, 15937),
        intArrayOf(16341, 16263),
        intArrayOf(16385, 16025),
        intArrayOf(16407, 16175),
        intArrayOf(16649, 16197),
        intArrayOf(16671, 15926),
        intArrayOf(16693, 15915),
        intArrayOf(16715, 16081),
        intArrayOf(16891, 16128),
        intArrayOf(16937, 16036),
        intArrayOf(17021, 16117),
        intArrayOf(17241, 16014),
        intArrayOf(17343, 15809),
        intArrayOf(15757, 16209),
        intArrayOf(16277, 15938),
        intArrayOf(16343, 16264),
        intArrayOf(16387, 16026),
        intArrayOf(16409, 16176),
        intArrayOf(16651, 16198),
        intArrayOf(16673, 15927),
        intArrayOf(16695, 15916),
        intArrayOf(16717, 16082),
        intArrayOf(16893, 16129),
        intArrayOf(16939, 16037),
        intArrayOf(17023, 16118),
        intArrayOf(17243, 16015),
        intArrayOf(17345, 15810),
        intArrayOf(15759, 16210),
        intArrayOf(16279, 15939),
        intArrayOf(16345, 16265),
        intArrayOf(16389, 16027),
        intArrayOf(16411, 16177),
        intArrayOf(16653, 16199),
        intArrayOf(16675, 15928),
        intArrayOf(16697, 15917),
        intArrayOf(16719, 16083),
        intArrayOf(16895, 16130),
        intArrayOf(16941, 16038),
        intArrayOf(17025, 16119),
        intArrayOf(17245, 16016),
        intArrayOf(17347, 15811),
        intArrayOf(15761, 16211),
        intArrayOf(16281, 15940),
        intArrayOf(16347, 16266),
        intArrayOf(16391, 16028),
        intArrayOf(16413, 16178),
        intArrayOf(16655, 16200),
        intArrayOf(16677, 15929),
        intArrayOf(16699, 15918),
        intArrayOf(16721, 16084),
        intArrayOf(16897, 16131),
        intArrayOf(16943, 16039),
        intArrayOf(17027, 16120),
        intArrayOf(17247, 16017),
        intArrayOf(17349, 15812),
        intArrayOf(15763, 16212),
        intArrayOf(16283, 15941),
        intArrayOf(16349, 16267),
        intArrayOf(16393, 16029),
        intArrayOf(16415, 16179),
        intArrayOf(16657, 16201),
        intArrayOf(16679, 15930),
        intArrayOf(16701, 15919),
        intArrayOf(16723, 16085),
        intArrayOf(16899, 16132),
        intArrayOf(16945, 16040),
        intArrayOf(17029, 16121),
        intArrayOf(17249, 16018),
        intArrayOf(17351, 15813),
        intArrayOf(15765, 16213),
        intArrayOf(16285, 15942),
        intArrayOf(16351, 16268),
        intArrayOf(16395, 16030),
        intArrayOf(16417, 16180),
        intArrayOf(16659, 16202),
        intArrayOf(16681, 15931),
        intArrayOf(16703, 15920),
        intArrayOf(16725, 16086),
        intArrayOf(16901, 16133),
        intArrayOf(16947, 16041),
        intArrayOf(17031, 16122),
        intArrayOf(17251, 16019),
        intArrayOf(17353, 15814),
        intArrayOf(15767, 16214),
        intArrayOf(16287, 15943),
        intArrayOf(16353, 16269),
        intArrayOf(16397, 16031),
        intArrayOf(16419, 16181),
        intArrayOf(16661, 16203),
        intArrayOf(16683, 15932),
        intArrayOf(16705, 15921),
        intArrayOf(16727, 16087),
        intArrayOf(16903, 16134),
        intArrayOf(16949, 16042),
        intArrayOf(17033, 16123),
        intArrayOf(17253, 16020),
        intArrayOf(17355, 15815),
        intArrayOf(15769, 16215),
        intArrayOf(16289, 15944),
        intArrayOf(16355, 16270),
        intArrayOf(16399, 16032),
        intArrayOf(16421, 16182),
        intArrayOf(16663, 16204),
        intArrayOf(16685, 15933),
        intArrayOf(16707, 15922),
        intArrayOf(16729, 16088),
        intArrayOf(16905, 16135),
        intArrayOf(16951, 16043),
        intArrayOf(17035, 16124),
        intArrayOf(17255, 16021),
        intArrayOf(17357, 15816),
        intArrayOf(15771, 16216),
        intArrayOf(16291, 15945),
        intArrayOf(16357, 16271),
        intArrayOf(16401, 16033),
        intArrayOf(16423, 16183),
        intArrayOf(16665, 16205),
        intArrayOf(16687, 15934),
        intArrayOf(16709, 15923),
        intArrayOf(16731, 16089),
        intArrayOf(16907, 16136),
        intArrayOf(16953, 16044),
        intArrayOf(17037, 16125),
        intArrayOf(17257, 16022),
        intArrayOf(17359, 15817),
        intArrayOf(15773, 16217),
        intArrayOf(16293, 15946),
        intArrayOf(16359, 16272),
        intArrayOf(16403, 16034),
        intArrayOf(16425, 16184),
        intArrayOf(16667, 16206),
        intArrayOf(16689, 15935),
        intArrayOf(16711, 15924),
        intArrayOf(16733, 16090),
        intArrayOf(16909, 16137),
        intArrayOf(16955, 16045),
        intArrayOf(17039, 16126),
        intArrayOf(17259, 16023),
        intArrayOf(17361, 15818),
        intArrayOf(16755, 15902),
        intArrayOf(17237, 15847),
        intArrayOf(16865, 15807),
        intArrayOf(16931, 15796),
        intArrayOf(17171, 16195),
        intArrayOf(17061, 16056),
        intArrayOf(17193, 16078),
        intArrayOf(17339, 16067),
        intArrayOf(17317, 16012),
        intArrayOf(17215, 16115),
        intArrayOf(17293, 15835)
    )

    /*	if(ItemDefinition.forId(BINDABLE_ITEMS[index][0]).getName().toLowerCase().contains("body") || ItemDefinition.forId(BINDABLE_ITEMS[index][0]).getName().toLowerCase().contains("legs") || ItemDefinition.forId(BINDABLE_ITEMS[index][0]).getName().toLowerCase().contains("skirt")) {
			index = index + 1 >= BINDABLE_ITEMS[0].length ? index-1 : index + 1;
		} */
    val randomBindableItem: Int
        get() {
            val index = Misc.getRandom(BINDABLE_ITEMS.size - 1)
            /*	if(ItemDefinition.forId(BINDABLE_ITEMS[index][0]).getName().toLowerCase().contains("body") || ItemDefinition.forId(BINDABLE_ITEMS[index][0]).getName().toLowerCase().contains("legs") || ItemDefinition.forId(BINDABLE_ITEMS[index][0]).getName().toLowerCase().contains("skirt")) {
			index = index + 1 >= BINDABLE_ITEMS[0].length ? index-1 : index + 1;
		} */return BINDABLE_ITEMS[index][0]
        }

    @JvmStatic
    fun isBindable(item: Int): Boolean {
        for (i in BINDABLE_ITEMS.indices) {
            if (BINDABLE_ITEMS[i][0] == item) return true
        }
        return false
    }

    @JvmStatic
    fun isBoundItem(item: Int): Boolean {
        for (i in BINDABLE_ITEMS.indices) {
            if (BINDABLE_ITEMS[i][1] == item) return true
        }
        return false
    }

    fun getItem(currentId: Int): Int {
        for (i in BINDABLE_ITEMS.indices) {
            if (BINDABLE_ITEMS[i][0] == currentId) return BINDABLE_ITEMS[i][1]
        }
        return -1
    }

    @JvmStatic
    fun unbindItem(p: Player, item: Int) {
        if (Dungeoneering.doingDungeoneering(p)) {
            for (i in p.minigameAttributes.dungeoneeringAttributes.boundItems.indices) {
                if (p.minigameAttributes.dungeoneeringAttributes.boundItems[i] == item) {
                    p.minigameAttributes.dungeoneeringAttributes.boundItems[i] = 0
                    p.packetSender.sendMessage("You unbind the item..")
                    break
                }
            }
        }
    }

    @JvmStatic
    fun bindItem(p: Player, item: Int) {
        if (Dungeoneering.doingDungeoneering(p)) {
            if (!isBindable(item)) return
            var amountBound = 0
            for (i in p.minigameAttributes.dungeoneeringAttributes.boundItems.indices) {
                if (p.minigameAttributes.dungeoneeringAttributes.boundItems[i] != 0) amountBound++
            }
            if (amountBound >= 5) {
                p.packetSender.sendMessage("You have already bound four items, which is the maximum.")
                return
            } else if (amountBound == 4 && p.skillManager.getCurrentLevel(Skill.DUNGEONEERING) < 95) {
                p.packetSender.sendMessage("You need a Dungeoneering level of at least 95 to have 5 bound items.")
                return
            } else if (amountBound == 3 && p.skillManager.getCurrentLevel(Skill.DUNGEONEERING) < 80) {
                p.packetSender.sendMessage("You need a Dungeoneering level of at least 80 to have 4 bound items.")
                return
            } else if (amountBound == 2 && p.skillManager.getCurrentLevel(Skill.DUNGEONEERING) < 60) {
                p.packetSender.sendMessage("You need a Dungeoneering level of at least 60 to have 3 bound items.")
                return
            } else if (amountBound == 1 && p.skillManager.getCurrentLevel(Skill.DUNGEONEERING) < 40) {
                p.packetSender.sendMessage("You need a Dungeoneering level of at least 40 to have 2 bound items.")
                return
            }
            val bind = getItem(item)
            var index = -1
            for (i in p.minigameAttributes.dungeoneeringAttributes.boundItems.indices) {
                if (p.minigameAttributes.dungeoneeringAttributes.boundItems[i] != 0) continue
                index = i
                break
            }
            if (bind != -1 && index != -1) {
                p.minigameAttributes.dungeoneeringAttributes.boundItems[index] = bind
                p.inventory.delete(item, 1).add(bind, 1)
                p.packetSender.sendMessage("You bind the item..")
            }
        }
    }

    fun onDungeonEntrance(p: Player) {
        for (i in p.minigameAttributes.dungeoneeringAttributes.boundItems.indices) {
            if (p.minigameAttributes.dungeoneeringAttributes.boundItems[i] != 0) {
                p.inventory.add(p.minigameAttributes.dungeoneeringAttributes.boundItems[i], 1)
            }
        }
    }
}