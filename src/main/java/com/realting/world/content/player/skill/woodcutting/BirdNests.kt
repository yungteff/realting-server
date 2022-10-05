package com.realting.world.content.player.skill.woodcutting

import com.realting.model.entity.character.GroundItemManager
import com.realting.model.GroundItem
import com.realting.model.Item
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc

/**
 * @author Optimum
 * I do not give permission to
 * release this anywhere else
 */
object BirdNests {
    /**
     * Ints.
     */
    val BIRD_NEST_IDS = intArrayOf(5070, 5071, 5072, 5073, 5074)
    val SEED_REWARDS = intArrayOf(5312, 5313, 5314, 5315, 5316, 5283, 5284, 5285, 5286, 5287, 5288, 5289, 5290, 5317)
    val RING_REWARDS = intArrayOf(1635, 1637, 1639, 1641, 1643)
    const val EMPTY = 5075
    const val RED = 5076
    const val BLUE = 5077
    const val GREEN = 5078
    const val AMOUNT = 1

    /**
     * Check if the item is a nest
     *
     */
    @JvmStatic
    fun isNest(itemId: Int): Boolean {
        for (nest in BIRD_NEST_IDS) {
            if (nest == itemId) {
                return true
            }
        }
        return false
    }

    /**
     * Generates the random drop and creates a ground item
     * where the player is standing
     */
    @JvmStatic
    fun dropNest(p: Player) {
        if (p.position.z > 0) {
            return
        }
        if (Misc.getRandom(60) == 1) {
            var nest: Item? = null
            val r = Misc.getRandom(1000)
            if (r >= 0 && r <= 640) {
                nest = Item(5073)
            } else if (r >= 641 && r <= 960) {
                nest = Item(5074)
            } else if (r >= 961) {
                val random = Misc.getRandom(2)
                nest = if (random == 1) {
                    Item(5072)
                } else if (random == 2) {
                    Item(5071)
                } else {
                    Item(5070)
                }
            }
            if (nest != null) {
                nest.amount = 1
                GroundItemManager.spawnGroundItem(
                    p,
                    GroundItem(nest, p.position.copy(), p.username, false, 80, true, 80)
                )
                p.packetSender.sendMessage("A bird's nest falls out of the tree!")
            }
        }
    }

    /**
     *
     * Searches the nest.
     *
     */
    @JvmStatic
    fun searchNest(p: Player, itemId: Int) {
        if (p.inventory.freeSlots <= 0) {
            p.packetSender.sendMessage("You do not have enough free inventory slots to do this.")
            return
        }
        p.inventory.delete(itemId, 1)
        eggNest(p, itemId)
        seedNest(p, itemId)
        ringNest(p, itemId)
        p.inventory.add(EMPTY, AMOUNT)
    }

    /**
     *
     * Determines what loot you get
     * from ring bird nests
     *
     */
    fun ringNest(p: Player, itemId: Int) {
        if (itemId == 5074) {
            val random = Misc.getRandom(1000)
            if (random >= 0 && random <= 340) {
                p.inventory.add(RING_REWARDS[0], AMOUNT)
            } else if (random >= 341 && random <= 750) {
                p.inventory.add(RING_REWARDS[1], AMOUNT)
            } else if (random >= 751 && random <= 910) {
                p.inventory.add(RING_REWARDS[2], AMOUNT)
            } else if (random >= 911 && random <= 989) {
                p.inventory.add(RING_REWARDS[3], AMOUNT)
            } else if (random >= 990) {
                p.inventory.add(RING_REWARDS[4], AMOUNT)
            }
        }
    }

    /**
     *
     * Determines what loot you get
     * from seed bird nests
     *
     */
    private fun seedNest(p: Player, itemId: Int) {
        if (itemId == 5073) {
            val random = Misc.getRandom(1000)
            if (random >= 0 && random <= 220) {
                p.inventory.add(SEED_REWARDS[0], AMOUNT)
            } else if (random >= 221 && random <= 350) {
                p.inventory.add(SEED_REWARDS[1], AMOUNT)
            } else if (random >= 351 && random <= 400) {
                p.inventory.add(SEED_REWARDS[2], AMOUNT)
            } else if (random >= 401 && random <= 430) {
                p.inventory.add(SEED_REWARDS[3], AMOUNT)
            } else if (random >= 431 && random <= 440) {
                p.inventory.add(SEED_REWARDS[4], AMOUNT)
            } else if (random >= 441 && random <= 600) {
                p.inventory.add(SEED_REWARDS[5], AMOUNT)
            } else if (random >= 601 && random <= 700) {
                p.inventory.add(SEED_REWARDS[6], AMOUNT)
            } else if (random >= 701 && random <= 790) {
                p.inventory.add(SEED_REWARDS[7], AMOUNT)
            } else if (random >= 791 && random <= 850) {
                p.inventory.add(SEED_REWARDS[8], AMOUNT)
            } else if (random >= 851 && random <= 900) {
                p.inventory.add(SEED_REWARDS[9], AMOUNT)
            } else if (random >= 901 && random <= 930) {
                p.inventory.add(SEED_REWARDS[10], AMOUNT)
            } else if (random >= 931 && random <= 950) {
                p.inventory.add(SEED_REWARDS[11], AMOUNT)
            } else if (random >= 951 && random <= 970) {
                p.inventory.add(SEED_REWARDS[12], AMOUNT)
            } else if (random >= 971 && random <= 980) {
                p.inventory.add(SEED_REWARDS[13], AMOUNT)
            } else {
                p.inventory.add(SEED_REWARDS[0], AMOUNT)
            }
        }
    }

    /**
     *
     * Egg nests
     *
     */
    fun eggNest(p: Player, itemId: Int) {
        if (itemId == 5070) {
            p.inventory.add(RED, AMOUNT)
        }
        if (itemId == 5071) {
            p.inventory.add(GREEN, AMOUNT)
        }
        if (itemId == 5072) {
            p.inventory.add(BLUE, AMOUNT)
        }
    }
}