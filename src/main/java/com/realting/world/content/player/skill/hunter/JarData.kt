package com.realting.world.content.player.skill.hunter

import com.realting.model.ItemRarity
import com.realting.model.Item
import com.realting.util.Misc

enum class JarData(var jarId: Int, vararg loot: Item?) {
    BABY_IMPLING_JAR(
        11238,
        Item(1755).setRarity(ItemRarity.COMMON),
        Item(1734).setRarity(ItemRarity.COMMON),
        Item(1733).setRarity(ItemRarity.COMMON),
        Item(946).setRarity(ItemRarity.COMMON),
        Item(1985).setRarity(ItemRarity.COMMON),
        Item(2347).setRarity(ItemRarity.COMMON),
        Item(1759).setRarity(ItemRarity.COMMON),
        Item(1927).setRarity(ItemRarity.UNCOMMON),
        Item(319).setRarity(ItemRarity.UNCOMMON),
        Item(2007).setRarity(ItemRarity.UNCOMMON),
        Item(1779).setRarity(ItemRarity.UNCOMMON),
        Item(7170).setRarity(ItemRarity.UNCOMMON),
        Item(401).setRarity(ItemRarity.UNCOMMON),
        Item(1438).setRarity(ItemRarity.UNCOMMON),
        Item(2355).setRarity(ItemRarity.RARE),
        Item(1607).setRarity(ItemRarity.RARE),
        Item(1743).setRarity(ItemRarity.RARE),
        Item(379).setRarity(ItemRarity.RARE),
        Item(1761).setRarity(ItemRarity.RARE)
    ),
    YOUNG_IMPLING_JAR(
        11240,
        Item(1353).setRarity(ItemRarity.COMMON),
        Item(1097).setRarity(ItemRarity.UNCOMMON),
        Item(1157).setRarity(ItemRarity.COMMON),
        Item(1539).setRarity(ItemRarity.COMMON).setAmount(5),
        Item(8778).setRarity(ItemRarity.COMMON),
        Item(2293).setRarity(ItemRarity.COMMON),
        Item(1783).setRarity(ItemRarity.COMMON).setAmount(4),
        Item(133).setRarity(ItemRarity.UNCOMMON).setAmount(3),
        Item(453).setRarity(ItemRarity.COMMON),
        Item(7936).setRarity(ItemRarity.COMMON),
        Item(7936).setRarity(ItemRarity.COMMON),
        Item(2359).setRarity(ItemRarity.RARE),
        Item(1777).setRarity(ItemRarity.UNCOMMON),
        Item(361).setRarity(ItemRarity.UNCOMMON),
        Item(361).setRarity(ItemRarity.UNCOMMON),
        Item(231).setRarity(ItemRarity.COMMON),
        Item(1761).setRarity(ItemRarity.COMMON)
    ),
    GOURMET_IMPLING_JAR(
        11242,
        Item(365).setRarity(ItemRarity.COMMON),
        Item(361).setRarity(ItemRarity.COMMON),
        Item(2011).setRarity(ItemRarity.COMMON),
        Item(2327).setRarity(ItemRarity.COMMON),
        Item(1897).setRarity(ItemRarity.COMMON),
        Item(2293).setRarity(ItemRarity.COMMON),
        Item(5004).setRarity(ItemRarity.COMMON),
        Item(1883).setRarity(ItemRarity.UNCOMMON),
        Item(247).setRarity(ItemRarity.UNCOMMON),
        Item(380).setRarity(ItemRarity.UNCOMMON).setAmount(4),
        Item(386).setRarity(ItemRarity.UNCOMMON).setAmount(4),
        Item(7170).setRarity(ItemRarity.UNCOMMON),
        Item(7754).setRarity(ItemRarity.UNCOMMON),
        Item(7178).setRarity(ItemRarity.UNCOMMON),
        Item(7188).setRarity(ItemRarity.UNCOMMON),
        Item(10137).setRarity(ItemRarity.RARE),
        Item(7179).setRarity(ItemRarity.RARE).setAmount(6),
        Item(374).setRarity(ItemRarity.RARE).setAmount(3),
        Item(10136).setRarity(ItemRarity.RARE),
        Item(5406).setRarity(ItemRarity.RARE),
        Item(2007).setRarity(ItemRarity.COMMON),
        Item(5970).setRarity(ItemRarity.COMMON)
    ),
    EARTH_IMPLING_JAR(
        11244,
        Item(6032).setRarity(ItemRarity.COMMON),
        Item(557).setRarity(ItemRarity.COMMON).setAmount(32),
        Item(6032).setRarity(ItemRarity.COMMON),
        Item(5535).setRarity(ItemRarity.COMMON),
        Item(1440).setRarity(ItemRarity.COMMON),
        Item(1442).setRarity(ItemRarity.COMMON),
        Item(444).setRarity(ItemRarity.COMMON),
        Item(5104).setRarity(ItemRarity.COMMON),
        Item(2353).setRarity(ItemRarity.COMMON),
        Item(1784).setRarity(ItemRarity.UNCOMMON).setAmount(4),
        Item(454).setRarity(ItemRarity.UNCOMMON).setAmount(6),
        Item(5294).setRarity(ItemRarity.UNCOMMON).setAmount(2),
        Item(447).setRarity(ItemRarity.UNCOMMON),
        Item(1273).setRarity(ItemRarity.UNCOMMON),
        Item(1487).setRarity(ItemRarity.UNCOMMON),
        Item(5311).setRarity(ItemRarity.UNCOMMON).setAmount(2),
        Item(1606).setRarity(ItemRarity.RARE).setAmount(2),
        Item(448).setRarity(ItemRarity.RARE).setAmount(3),
        Item(6035).setRarity(ItemRarity.RARE).setAmount(2),
        Item(5303).setRarity(ItemRarity.VERY_RARE),
        Item(1603).setRarity(ItemRarity.VERY_RARE)
    ),
    ESSENCE_IMPLING_JAR(
        11246,
        Item(562).setRarity(ItemRarity.COMMON).setAmount(4),
        Item(555).setRarity(ItemRarity.COMMON).setAmount(22),
        Item(558).setRarity(ItemRarity.COMMON).setAmount(25),
        Item(556).setRarity(ItemRarity.COMMON).setAmount(30),
        Item(559).setRarity(ItemRarity.COMMON).setAmount(28),
        Item(554).setRarity(ItemRarity.COMMON).setAmount(50),
        Item(1448).setRarity(ItemRarity.COMMON),
        Item(7937).setRarity(ItemRarity.COMMON).setAmount(35),
        Item(1437).setRarity(ItemRarity.COMMON).setAmount(20),
        Item(564).setRarity(ItemRarity.UNCOMMON).setAmount(4),
        Item(4695).setRarity(ItemRarity.UNCOMMON).setAmount(4),
        Item(4696).setRarity(ItemRarity.UNCOMMON).setAmount(4),
        Item(4698).setRarity(ItemRarity.UNCOMMON).setAmount(4),
        Item(4694).setRarity(ItemRarity.UNCOMMON).setAmount(4),
        Item(4699).setRarity(ItemRarity.RARE).setAmount(4),
        Item(4697).setRarity(ItemRarity.RARE).setAmount(4),
        Item(565).setRarity(ItemRarity.RARE).setAmount(7),
        Item(566).setRarity(ItemRarity.RARE).setAmount(11),
        Item(563).setRarity(ItemRarity.RARE).setAmount(13),
        Item(563).setRarity(ItemRarity.RARE).setAmount(13),
        Item(560).setRarity(ItemRarity.RARE).setAmount(13),
        Item(1442).setRarity(ItemRarity.RARE).setAmount(4)
    ),
    ECLECTIC_IMPLING_JAR(
        11248,
        Item(1391).setRarity(ItemRarity.VERY_RARE),
        Item(1273).setRarity(ItemRarity.COMMON),
        Item(2493).setRarity(ItemRarity.RARE),
        Item(10083).setRarity(ItemRarity.RARE),
        Item(562).setRarity(ItemRarity.COMMON),
        Item(1199).setRarity(ItemRarity.UNCOMMON),
        Item(1213).setRarity(ItemRarity.RARE),
        Item(5970).setRarity(ItemRarity.COMMON),
        Item(231).setRarity(ItemRarity.COMMON),
        Item(4527).setRarity(ItemRarity.UNCOMMON),
        Item(444).setRarity(ItemRarity.UNCOMMON),
        Item(450).setRarity(ItemRarity.RARE).setAmount(10),
        Item(556).setRarity(ItemRarity.COMMON).setAmount(43),
        Item(2358).setRarity(ItemRarity.RARE).setAmount(5),
        Item(7937).setRarity(ItemRarity.UNCOMMON).setAmount(30),
        Item(237).setRarity(ItemRarity.UNCOMMON),
        Item(1601).setRarity(ItemRarity.VERY_RARE),
        Item(5759).setRarity(ItemRarity.RARE),
        Item(7208).setRarity(ItemRarity.RARE),
        Item(8779).setRarity(ItemRarity.COMMON).setAmount(4),
        Item(5321).setRarity(ItemRarity.RARE).setAmount(3)
    ),
    NATURE_IMPLING_JAR(
        11250,
        Item(5100).setRarity(ItemRarity.COMMON),
        Item(5104).setRarity(ItemRarity.COMMON),
        Item(5281).setRarity(ItemRarity.COMMON),
        Item(5294).setRarity(ItemRarity.COMMON),
        Item(5295).setRarity(ItemRarity.RARE),
        Item(5297).setRarity(ItemRarity.UNCOMMON),
        Item(5299).setRarity(ItemRarity.UNCOMMON),
        Item(5298).setRarity(ItemRarity.UNCOMMON),
        Item(5303).setRarity(ItemRarity.VERY_RARE),
        Item(5304).setRarity(ItemRarity.VERY_RARE),
        Item(5313).setRarity(ItemRarity.UNCOMMON),
        Item(5286).setRarity(ItemRarity.UNCOMMON),
        Item(3051).setRarity(ItemRarity.RARE),
        Item(3000).setRarity(ItemRarity.RARE),
        Item(219).setRarity(ItemRarity.VERY_RARE),
        Item(5974).setRarity(ItemRarity.UNCOMMON),
        Item(6016).setRarity(ItemRarity.COMMON),
        Item(1513).setRarity(ItemRarity.COMMON),
        Item(253).setRarity(ItemRarity.COMMON),
        Item(269).setRarity(ItemRarity.VERY_RARE)
    ),
    MAGPIE_IMPLING_JAR(
        11252,
        Item(1681).setRarity(ItemRarity.COMMON),
        Item(1682).setRarity(ItemRarity.UNCOMMON).setAmount(3),
        Item(1731).setRarity(ItemRarity.COMMON),
        Item(1732).setRarity(ItemRarity.UNCOMMON).setAmount(3),
        Item(2568).setRarity(ItemRarity.COMMON),
        Item(2569).setRarity(ItemRarity.UNCOMMON).setAmount(3),
        Item(3391).setRarity(ItemRarity.UNCOMMON),
        Item(2570).setRarity(ItemRarity.UNCOMMON),
        Item(4097).setRarity(ItemRarity.UNCOMMON),
        Item(4095).setRarity(ItemRarity.UNCOMMON),
        Item(1215).setRarity(ItemRarity.RARE),
        Item(1185).setRarity(ItemRarity.RARE),
        Item(5541).setRarity(ItemRarity.COMMON),
        Item(1747).setRarity(ItemRarity.COMMON),
        Item(2363).setRarity(ItemRarity.UNCOMMON),
        Item(1603).setRarity(ItemRarity.UNCOMMON),
        Item(1755).setRarity(ItemRarity.COMMON),
        Item(1734).setRarity(ItemRarity.COMMON),
        Item(1733).setRarity(ItemRarity.COMMON),
        Item(946).setRarity(ItemRarity.COMMON),
        Item(1985).setRarity(ItemRarity.COMMON),
        Item(2347).setRarity(ItemRarity.COMMON),
        Item(1759).setRarity(ItemRarity.COMMON),
        Item(1927).setRarity(ItemRarity.UNCOMMON),
        Item(319).setRarity(ItemRarity.UNCOMMON),
        Item(2007).setRarity(ItemRarity.UNCOMMON),
        Item(1779).setRarity(ItemRarity.UNCOMMON),
        Item(7170).setRarity(ItemRarity.UNCOMMON),
        Item(401).setRarity(ItemRarity.UNCOMMON),
        Item(1438).setRarity(ItemRarity.UNCOMMON),
        Item(2355).setRarity(ItemRarity.RARE),
        Item(1607).setRarity(ItemRarity.RARE),
        Item(1743).setRarity(ItemRarity.RARE),
        Item(379).setRarity(ItemRarity.RARE),
        Item(1601).setRarity(ItemRarity.RARE),
        Item(985).setRarity(ItemRarity.RARE),
        Item(987).setRarity(ItemRarity.RARE),
        Item(993).setRarity(ItemRarity.VERY_RARE),
        Item(5300).setRarity(ItemRarity.VERY_RARE),
        Item(12121).setRarity(ItemRarity.RARE)
    ),
    NINJA_IMPLING_JAR(
        11254,
        Item(6328).setRarity(ItemRarity.COMMON),
        Item(6328).setRarity(ItemRarity.COMMON),
        Item(10606).setRarity(ItemRarity.COMMON),
        Item(6328).setRarity(ItemRarity.COMMON),
        Item(3391).setRarity(ItemRarity.COMMON),
        Item(4097).setRarity(ItemRarity.COMMON),
        Item(4095).setRarity(ItemRarity.COMMON),
        Item(1333).setRarity(ItemRarity.UNCOMMON),
        Item(1347).setRarity(ItemRarity.UNCOMMON),
        Item(1215).setRarity(ItemRarity.UNCOMMON),
        Item(6313).setRarity(ItemRarity.COMMON),
        Item(892).setRarity(ItemRarity.COMMON).setAmount(40),
        Item(811).setRarity(ItemRarity.COMMON).setAmount(40),
        Item(868).setRarity(ItemRarity.COMMON).setAmount(20),
        Item(805).setRarity(ItemRarity.COMMON).setAmount(25),
        Item(9342).setRarity(ItemRarity.UNCOMMON).setAmount(2),
        Item(9194).setRarity(ItemRarity.COMMON).setAmount(4),
        Item(5100).setRarity(ItemRarity.COMMON),
        Item(5104).setRarity(ItemRarity.COMMON),
        Item(5281).setRarity(ItemRarity.COMMON),
        Item(5294).setRarity(ItemRarity.COMMON),
        Item(5295).setRarity(ItemRarity.RARE),
        Item(5297).setRarity(ItemRarity.UNCOMMON),
        Item(5299).setRarity(ItemRarity.UNCOMMON),
        Item(5298).setRarity(ItemRarity.UNCOMMON),
        Item(5303).setRarity(ItemRarity.VERY_RARE),
        Item(5304).setRarity(ItemRarity.VERY_RARE),
        Item(5313).setRarity(ItemRarity.UNCOMMON),
        Item(5286).setRarity(ItemRarity.UNCOMMON),
        Item(3051).setRarity(ItemRarity.RARE),
        Item(3000).setRarity(ItemRarity.RARE),
        Item(219).setRarity(ItemRarity.VERY_RARE),
        Item(5974).setRarity(ItemRarity.UNCOMMON),
        Item(1755).setRarity(ItemRarity.COMMON),
        Item(1734).setRarity(ItemRarity.COMMON),
        Item(1733).setRarity(ItemRarity.COMMON),
        Item(946).setRarity(ItemRarity.COMMON),
        Item(1985).setRarity(ItemRarity.COMMON),
        Item(2347).setRarity(ItemRarity.COMMON),
        Item(1759).setRarity(ItemRarity.COMMON),
        Item(1927).setRarity(ItemRarity.UNCOMMON),
        Item(319).setRarity(ItemRarity.UNCOMMON),
        Item(2007).setRarity(ItemRarity.UNCOMMON),
        Item(1779).setRarity(ItemRarity.UNCOMMON),
        Item(7170).setRarity(ItemRarity.UNCOMMON),
        Item(401).setRarity(ItemRarity.UNCOMMON),
        Item(1438).setRarity(ItemRarity.UNCOMMON),
        Item(2355).setRarity(ItemRarity.RARE),
        Item(1607).setRarity(ItemRarity.RARE),
        Item(1743).setRarity(ItemRarity.RARE),
        Item(379).setRarity(ItemRarity.RARE),
        Item(6016).setRarity(ItemRarity.COMMON),
        Item(1513).setRarity(ItemRarity.COMMON),
        Item(253).setRarity(ItemRarity.COMMON)
    ),
    DRAGON_IMPLING_JAR(
        11256,
        Item(1704).setRarity(ItemRarity.RARE),
        Item(4093).setRarity(ItemRarity.RARE),
        Item(5547).setRarity(ItemRarity.VERY_RARE),
        Item(1704).setRarity(ItemRarity.RARE),
        Item(1683).setRarity(ItemRarity.RARE),
        Item(11212).setRarity(ItemRarity.COMMON).setAmount(2),
        Item(9341).setRarity(ItemRarity.COMMON).setAmount(2),
        Item(1215).setRarity(ItemRarity.UNCOMMON),
        Item(11230).setRarity(ItemRarity.UNCOMMON).setAmount(10),
        Item(11232).setRarity(ItemRarity.UNCOMMON).setAmount(5),
        Item(11237).setRarity(ItemRarity.COMMON).setAmount(5),
        Item(9193).setRarity(ItemRarity.COMMON).setAmount(2),
        Item(535).setRarity(ItemRarity.COMMON).setAmount(5),
        Item(5316).setRarity(ItemRarity.RARE),
        Item(537).setRarity(ItemRarity.UNCOMMON).setAmount(10),
        Item(1615).setRarity(ItemRarity.COMMON),
        Item(5300).setRarity(ItemRarity.COMMON),
        Item(7219).setRarity(ItemRarity.UNCOMMON).setAmount(6),
        Item(562).setRarity(ItemRarity.COMMON).setAmount(3),
        Item(555).setRarity(ItemRarity.COMMON).setAmount(12),
        Item(558).setRarity(ItemRarity.COMMON).setAmount(6),
        Item(556).setRarity(ItemRarity.COMMON).setAmount(10),
        Item(559).setRarity(ItemRarity.COMMON).setAmount(18),
        Item(554).setRarity(ItemRarity.COMMON).setAmount(50),
        Item(1448).setRarity(ItemRarity.COMMON),
        Item(7937).setRarity(ItemRarity.COMMON).setAmount(25),
        Item(1437).setRarity(ItemRarity.COMMON).setAmount(5),
        Item(564).setRarity(ItemRarity.UNCOMMON).setAmount(4),
        Item(4695).setRarity(ItemRarity.UNCOMMON).setAmount(3),
        Item(4696).setRarity(ItemRarity.UNCOMMON).setAmount(4),
        Item(4698).setRarity(ItemRarity.UNCOMMON).setAmount(2),
        Item(4694).setRarity(ItemRarity.UNCOMMON).setAmount(4),
        Item(4699).setRarity(ItemRarity.RARE).setAmount(3),
        Item(4697).setRarity(ItemRarity.RARE).setAmount(5),
        Item(565).setRarity(ItemRarity.RARE).setAmount(3),
        Item(1755).setRarity(ItemRarity.COMMON),
        Item(1734).setRarity(ItemRarity.COMMON),
        Item(1733).setRarity(ItemRarity.COMMON),
        Item(946).setRarity(ItemRarity.COMMON),
        Item(1985).setRarity(ItemRarity.COMMON),
        Item(2347).setRarity(ItemRarity.COMMON),
        Item(1759).setRarity(ItemRarity.COMMON),
        Item(1927).setRarity(ItemRarity.UNCOMMON),
        Item(319).setRarity(ItemRarity.UNCOMMON),
        Item(2007).setRarity(ItemRarity.UNCOMMON),
        Item(1779).setRarity(ItemRarity.UNCOMMON),
        Item(7170).setRarity(ItemRarity.UNCOMMON),
        Item(401).setRarity(ItemRarity.UNCOMMON),
        Item(1438).setRarity(ItemRarity.UNCOMMON),
        Item(2355).setRarity(ItemRarity.RARE),
        Item(1607).setRarity(ItemRarity.RARE),
        Item(1743).setRarity(ItemRarity.RARE),
        Item(379).setRarity(ItemRarity.RARE),
        Item(566).setRarity(ItemRarity.RARE).setAmount(6),
        Item(563).setRarity(ItemRarity.RARE).setAmount(11),
        Item(563).setRarity(ItemRarity.RARE).setAmount(13),
        Item(560).setRarity(ItemRarity.RARE).setAmount(17),
        Item(1442).setRarity(ItemRarity.RARE).setAmount(7)
    ),
    KINGLY_IMPLING_JAR(
        15517,
        Item(15511).setRarity(ItemRarity.VERY_RARE),
        Item(15509).setRarity(ItemRarity.VERY_RARE),
        Item(15507).setRarity(ItemRarity.VERY_RARE),
        Item(15505).setRarity(ItemRarity.VERY_RARE),
        Item(15503).setRarity(ItemRarity.VERY_RARE),
        Item(1305).setRarity(ItemRarity.UNCOMMON),
        Item(1250).setRarity(ItemRarity.UNCOMMON),
        Item(7158).setRarity(ItemRarity.VERY_RARE),
        Item(2366).setRarity(ItemRarity.RARE),
        Item(2366).setRarity(ItemRarity.COMMON),
        Item(1617).setRarity(ItemRarity.COMMON),
        Item(1618).setRarity(ItemRarity.COMMON).setAmount(1),
        Item(1705).setRarity(ItemRarity.COMMON).setAmount(1),
        Item(1683).setRarity(ItemRarity.COMMON),
        Item(1684).setRarity(ItemRarity.COMMON).setAmount(1),
        Item(989).setRarity(ItemRarity.COMMON),
        Item(1615).setRarity(ItemRarity.UNCOMMON),
        Item(1616).setRarity(ItemRarity.UNCOMMON).setAmount(1),
        Item(1631).setRarity(ItemRarity.UNCOMMON),
        Item(1632).setRarity(ItemRarity.UNCOMMON).setAmount(2),
        Item(9341).setRarity(ItemRarity.UNCOMMON).setAmount(20),
        Item(9342).setRarity(ItemRarity.UNCOMMON).setAmount(10),
        Item(2364).setRarity(ItemRarity.UNCOMMON).setAmount(2),
        Item(9194).setRarity(ItemRarity.RARE).setAmount(5),
        Item(1615).setRarity(ItemRarity.UNCOMMON),
        Item(6571).setRarity(ItemRarity.VERY_RARE),
        Item(365).setRarity(ItemRarity.COMMON),
        Item(361).setRarity(ItemRarity.COMMON),
        Item(2011).setRarity(ItemRarity.COMMON),
        Item(2327).setRarity(ItemRarity.COMMON),
        Item(1897).setRarity(ItemRarity.COMMON),
        Item(2293).setRarity(ItemRarity.COMMON),
        Item(5004).setRarity(ItemRarity.COMMON),
        Item(1883).setRarity(ItemRarity.UNCOMMON),
        Item(247).setRarity(ItemRarity.UNCOMMON),
        Item(380).setRarity(ItemRarity.UNCOMMON).setAmount(2),
        Item(386).setRarity(ItemRarity.UNCOMMON).setAmount(2),
        Item(7170).setRarity(ItemRarity.UNCOMMON),
        Item(7754).setRarity(ItemRarity.UNCOMMON),
        Item(1755).setRarity(ItemRarity.COMMON),
        Item(1734).setRarity(ItemRarity.COMMON),
        Item(1733).setRarity(ItemRarity.COMMON),
        Item(946).setRarity(ItemRarity.COMMON),
        Item(1985).setRarity(ItemRarity.COMMON),
        Item(2347).setRarity(ItemRarity.COMMON),
        Item(1759).setRarity(ItemRarity.COMMON),
        Item(1927).setRarity(ItemRarity.UNCOMMON),
        Item(319).setRarity(ItemRarity.UNCOMMON),
        Item(2007).setRarity(ItemRarity.UNCOMMON),
        Item(1779).setRarity(ItemRarity.UNCOMMON),
        Item(7170).setRarity(ItemRarity.UNCOMMON),
        Item(401).setRarity(ItemRarity.UNCOMMON),
        Item(1438).setRarity(ItemRarity.UNCOMMON),
        Item(2355).setRarity(ItemRarity.RARE),
        Item(1607).setRarity(ItemRarity.RARE),
        Item(1743).setRarity(ItemRarity.RARE),
        Item(379).setRarity(ItemRarity.RARE),
        Item(7178).setRarity(ItemRarity.UNCOMMON),
        Item(7188).setRarity(ItemRarity.UNCOMMON),
        Item(10137).setRarity(ItemRarity.RARE),
        Item(7179).setRarity(ItemRarity.RARE).setAmount(6),
        Item(374).setRarity(ItemRarity.RARE).setAmount(3),
        Item(10136).setRarity(ItemRarity.RARE),
        Item(5406).setRarity(ItemRarity.RARE),
        Item(2007).setRarity(ItemRarity.COMMON)
    );

    var loot: Array<Item?>

    init {
        this.loot = loot as Array<Item?>
    }

    companion object {
        @JvmStatic
        fun forJar(jar: Int): JarData? {
            for (jars in values()) {
                if (jars.jarId == jar) {
                    return jars
                }
            }
            return null
        }

        fun getLootRarity(data: JarData, rarity: Int): Int {
            var k = 0
            for (items in data.loot) {
                if (items!!.rarity.rarity == rarity) {
                    k++
                }
            }
            return k
        }

        val rar: Int
            get() {
                if (Misc.getRandom(20) >= 16) return 1 else if (Misc.getRandom(50) >= 47) return 2 else if (Misc.getRandom(
                        60
                    ) >= 58
                ) return 3
                return 0
            }
    }
}