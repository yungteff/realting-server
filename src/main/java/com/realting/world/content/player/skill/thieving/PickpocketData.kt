package com.realting.world.content.player.skill.thieving

import com.realting.model.Item
import com.realting.util.Misc

enum class PickpocketData(//tzhaar-hur
    val npcs: IntArray,
    val requirement: Int,
    val experience: Int,
    val damage: IntArray,
    val failMessage: Array<String>,
    val loot: Array<Item>
) {
    MAN(
        intArrayOf(1, 2, 3, 4, 5, 6),
        1,
        8,
        intArrayOf(10, 10),
        arrayOf("Keep your hands to yourself!", "What are you doing?!", "Hey! Cut it out.", "Stop that!"),
        arrayOf(
            Item(995, 3)
        )
    ),
    FARMER(
        intArrayOf(7),
        10,
        15,
        intArrayOf(10, 20),
        arrayOf("Oi!", "What're you doing?", "Stop it.", "You're mad.", "Quit it."),
        arrayOf(
            Item(5318), Item(995, 9)
        )
    ),
    FEMALE_HAM(
        intArrayOf(1715),
        15,
        19,
        intArrayOf(10, 30),
        arrayOf("Thief!", "We do not take kindly to thieves!", "Get away from me!", "What's your problem?"),
        arrayOf(
            Item(4298),
            Item(4300),
            Item(4302),
            Item(4304),
            Item(4306),
            Item(4308),
            Item(4310),
            Item(1351),
            Item(1205),
            Item(1265),
            Item(1739),
            Item(1349),
            Item(1267),
            Item(1129),
            Item(1131),
            Item(1167),
            Item(1095),
            Item(1063),
            Item(1511),
            Item(1734),
            Item(321),
            Item(2138),
            Item(440),
            Item(453),
            Item(886),
            Item(1269),
            Item(1353),
            Item(995, 20),
            Item(199),
            Item(203),
            Item(211),
            Item(205),
            Item(207),
            Item(209),
            Item(946),
            Item(1733),
            Item(1207),
            Item(590),
            Item(10496),
            Item(1627),
            Item(1625),
            Item(995, 25)
        )
    ),
    MALE_HAM(
        intArrayOf(1714),
        20,
        23,
        intArrayOf(16, 36),
        arrayOf("Pickpocket!", "Stay out of my pockets!", "Caught you!", "What is this?"),
        arrayOf(
            Item(4298),
            Item(4300),
            Item(4302),
            Item(4304),
            Item(4306),
            Item(4308),
            Item(4310),
            Item(1351),
            Item(1205),
            Item(1265),
            Item(1739),
            Item(1349),
            Item(1267),
            Item(1129),
            Item(1131),
            Item(1167),
            Item(1095),
            Item(1063),
            Item(1511),
            Item(1734),
            Item(321),
            Item(2138),
            Item(440),
            Item(453),
            Item(886),
            Item(1269),
            Item(1353),
            Item(995, 20),
            Item(199),
            Item(203),
            Item(211),
            Item(205),
            Item(207),
            Item(209),
            Item(946),
            Item(1733),
            Item(1207),
            Item(590),
            Item(10496),
            Item(1627),
            Item(1625),
            Item(995, 45)
        )
    ),
    WARRIOR_WOMAN_ALKHARID(
        intArrayOf(18, 15),
        25,
        26,
        intArrayOf(18, 38),
        arrayOf("You there! Away from my pockets!", "Inconsiderate rat!", "Oi!", "Halt."),
        arrayOf(
            Item(995, 18)
        )
    ),
    ROGUE(
        intArrayOf(187), 32, 36, intArrayOf(20, 40), arrayOf(
            "Thief, stay away.",
            "I am not afraid of conflict.",
            "You best not try that again.",
            "Is this how you want to die?"
        ), arrayOf(
            Item(995, 40), Item(995, 25), Item(1523), Item(1219), Item(1993), Item(556, 8)
        )
    ),
    CAVE_GOBLIN(
        intArrayOf(5752, 5753, 5755, 5756, 5757, 5758, 5759),
        36,
        40,
        intArrayOf(22, 42),
        arrayOf("Human?", "Surface-dweller! Why?!", "Stay away human!", "No touch!"),
        arrayOf(
            Item(995, 50)
        )
    ),
    MASTER_FARMER(
        intArrayOf(3299, 2234, 2235),
        38,
        43,
        intArrayOf(30, 30),
        arrayOf("Let it go, mate.", "Cor blimey!", "Do you mind?", "Oi!", "Got'cha!"),
        arrayOf(
            Item(5291),
            Item(5292),
            Item(5293),
            Item(5294),
            Item(5291),
            Item(5292),
            Item(5293),
            Item(5294),
            Item(5295),
            Item(5296),
            Item(5297),
            Item(5298),
            Item(5299),
            Item(5300),
            Item(5301),
            Item(5302),
            Item(5303),
            Item(5304)
        )
    ),
    GUARD(
        intArrayOf(9), 40, 47, intArrayOf(25, 45), arrayOf(
            "Guard might get nervous...",
            "I used to be an adventurer like you.",
            "Disrespect the law, and you disrespect me.",
            "No lollygaggin'.",
            "Everything all right?",
            "Only burglars and vampires creep around after dark. So which are you?"
        ), arrayOf(
            Item(995, 30)
        )
    ),  //FREMENNIK
    DESERT_BANDIT(
        intArrayOf(1880, 1881), 53, 80, intArrayOf(50, 50), arrayOf(
            "Are you sure about that?",
            "We don't take kindly to strangers.",
            "You looking for a beating?",
            "You mix potions, right? Could I get a brew?"
        ), arrayOf(
            Item(995, 50)
        )
    ),
    KNIGHT(
        intArrayOf(23, 26),
        53,
        80,
        intArrayOf(50, 50),
        arrayOf("Stay out of trouble.", "What is it?", "I don't have time for this.", "Hey! Hands off."),
        arrayOf(
            Item(995, 50)
        )
    ),
    YANILLE_WATCHMAN(
        intArrayOf(34), 65, 138, intArrayOf(50, 60), arrayOf(
            "Not on my watch.",
            "Guard might get nervous...",
            "I used to be an adventurer like you.",
            "Disrespect the law, and you disrespect me.",
            "No lollygaggin'.",
            "Everything all right?",
            "Only burglars and vampires creep around after dark. So which are you?"
        ), arrayOf(
            Item(995, 60), Item(2309)
        )
    ),
    PALADIN(
        intArrayOf(20, 2256), 70, 152, intArrayOf(60, 60), arrayOf(
            "You dare dishonor me?",
            "My blade is ready - try that again.",
            "I would leave now, thief.",
            "Not the brightest thief."
        ), arrayOf(
            Item(995, 80), Item(562, 2)
        )
    ),
    GNOME(
        intArrayOf(66, 67, 68, 159, 160, 161, 168, 169),
        75,
        199,
        intArrayOf(50, 100),
        arrayOf("Stop!", "You're too brash, human.", "Keep your paws off me.", "Quit it."),
        arrayOf(
            Item(995, 300), Item(577), Item(444), Item(569), Item(2150), Item(2162)
        )
    ),
    HERO(
        intArrayOf(21), 80, 275, intArrayOf(100, 150), arrayOf(
            "Keep your distance!",
            "Don't you know who I am?",
            "Insignificant fool!",
            "I will be your death.",
            "Is this worth dying over?"
        ), arrayOf(
            Item(995, 200), Item(995, 300), Item(565), Item(1601), Item(1993), Item(560, 2), Item(569), Item(444)
        )
    ),
    ELF(
        intArrayOf(2363, 2364, 2365, 2366),
        85,
        353,
        intArrayOf(100, 200),
        arrayOf("Mortality is cruel.", "You might lose that finger!", "Keep a distance from me.", "Hahaha."),
        arrayOf(
            Item(995, 280), Item(995, 350), Item(561, 3), Item(1601), Item(1993), Item(560, 2), Item(569), Item(444)
        )
    );

    fun getFailMessage(): String {
        return Misc.randomElement(failMessage)
    }

    val reward: Item
        get() = Misc.randomElement(loot)

    fun getDamage(): Int {
        return if (damage[0] == damage[1]) {
            damage[0]
        } else Misc.inclusiveRandom(damage[0], damage[1])
    }

    companion object {
        @JvmStatic
        fun forNpc(npcId: Int): PickpocketData? {
            for (p in values()) {
                for (i in p.npcs) {
                    if (i == npcId) {
                        return p
                    }
                }
            }
            return null
        }
    }
}