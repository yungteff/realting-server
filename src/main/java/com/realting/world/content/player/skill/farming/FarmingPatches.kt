package com.realting.world.content.player.skill.farming

enum class FarmingPatches(
    val x: Int,
    val y: Int,
    val x2: Int,
    val y2: Int,
    val mod: Int,
    val config: Int,
    val planter: Int,
    val harvestAnimation: Int,
    val harvestItem: Int,
    val seedType: SeedType
) {
    /*
	CATHERBY_ALLOTMENT_NORTH(2805, 3465, 2815, 3469, 65536, 504, 5343, 2275, 5329, SeedType.ALLOTMENT), 
	CATHERBY_ALLOTMENT_SOUTH(2805, 3458, 2815, 3461, 16777216, 504, 5343, 2275, 5329, SeedType.ALLOTMENT), 
	CATHERBY_HERB(2813, 3462, 2815, 3464, 256, 515, 5343, 2275, 5329, SeedType.HERB), 
	CATHERBY_FLOWER(2808, 3462, 2811, 3465, 256, 508, 5343, 2275, 5329, SeedType.FLOWER),
	
	*/
    SOUTH_FALADOR_HERB(3058, 3310, 3060, 3313, 1, 515, 5343, 2275, 5329, SeedType.HERB), SOUTH_FALADOR_FLOWER(
        3054, 3306, 3056, 3307, 1, 508, 5343, 2275, 5329, SeedType.FLOWER
    ),
    SOUTH_FALADOR_ALLOTMENT_WEST(
        3050, 3306, 3055, 3312, 1, 504, 5343, 2275, 5329, SeedType.ALLOTMENT
    ),
    SOUTH_FALADOR_ALLOTMENT_SOUTH(3055, 3302, 3059, 3309, 1, 504, 5343, 2275, 5329, SeedType.ALLOTMENT);
}