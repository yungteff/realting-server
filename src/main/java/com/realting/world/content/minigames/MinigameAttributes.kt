package com.realting.world.content.minigames

import com.realting.world.content.player.skill.dungeoneering.DungeoneeringParty

/**
 * Holds different minigame attributes for a player
 * @author Gabriel Hannason
 */
class MinigameAttributes {
    val barrowsMinigameAttributes = BarrowsMinigameAttributes()
    val warriorsGuildAttributes = WarriorsGuildAttributes()
    val pestControlAttributes = PestControlAttributes()
    val recipeForDisasterAttributes = RecipeForDisasterAttributes()
    val nomadAttributes = NomadAttributes()

    //private final SoulWarsAttributes soulWarsAttributes = new SoulWarsAttributes();
    val godwarsDungeonAttributes = GodwarsDungeonAttributes()
    val graveyardAttributes = GraveyardAttributes()
    val dungeoneeringAttributes = DungeoneeringAttributes()
    val trioAttuibutes = trioAttributes()
    val zulrahAttributes = ZulrahAttributes()

    inner class GraveyardAttributes {
        var wave = 0
            private set
        var requiredKills = 0
        var level = 0
        private var entered = false
        fun setWave(wave: Int): GraveyardAttributes {
            this.wave = wave
            return this
        }

        fun incrementAndGetWave(): Int {
            return wave++
        }

        fun incrementLevel() {
            level++
        }

        fun decrementAndGetRequiredKills(): Int {
            return requiredKills--
        }

        fun hasEntered(): Boolean {
            return entered
        }

        fun setEntered(entered: Boolean): GraveyardAttributes {
            this.entered = entered
            return this
        }
    }

    inner class PestControlAttributes {
        var damageDealt = 0
        fun incrementDamageDealt(damageDealt: Int) {
            this.damageDealt += damageDealt
        }
    }

    inner class ZulrahAttributes {
        var redFormDamage = 0
            private set

        fun setRedFormDamage(value: Int, add: Boolean) {
            if (add) {
                redFormDamage += value
            } else {
                redFormDamage = value
            }
        }
    }

    inner class WarriorsGuildAttributes {
        private var hasSpawnedArmour = false
        private var enteredTokenRoom = false
        fun hasSpawnedArmour(): Boolean {
            return hasSpawnedArmour
        }

        fun setSpawnedArmour(hasSpawnedArmour: Boolean) {
            this.hasSpawnedArmour = hasSpawnedArmour
        }

        fun enteredTokenRoom(): Boolean {
            return enteredTokenRoom
        }

        fun setEnteredTokenRoom(enteredTokenRoom: Boolean) {
            this.enteredTokenRoom = enteredTokenRoom
        }
    }

    inner class trioAttributes {
        private var joinedBossRoom = false
        fun joinedBossRoom(): Boolean {
            return joinedBossRoom
        }

        fun setJoinedBossRoom(joinedBossRoom: Boolean) {
            this.joinedBossRoom = joinedBossRoom
        }
    }

    inner class BarrowsMinigameAttributes {
        var killcount = 0
        var randomCoffin = 0
        var riddleAnswer = -1
        var barrowsData = arrayOf(
            intArrayOf(2030, 0),
            intArrayOf(2029, 0),
            intArrayOf(2028, 0),
            intArrayOf(2027, 0),
            intArrayOf(2026, 0),
            intArrayOf(2025, 0)
        )
    }

    inner class RecipeForDisasterAttributes {
        var wavesCompleted = 0
        var questParts = BooleanArray(9)
        fun hasFinishedPart(index: Int): Boolean {
            return questParts[index]
        }

        fun setPartFinished(index: Int, finished: Boolean) {
            questParts[index] = finished
        }

        fun reset() {
            questParts = BooleanArray(9)
            wavesCompleted = 0
        }
    }

    inner class NomadAttributes {
        var questParts = BooleanArray(2)
        fun hasFinishedPart(index: Int): Boolean {
            return questParts[index]
        }

        fun setPartFinished(index: Int, finished: Boolean) {
            questParts[index] = finished
        }

        fun reset() {
            questParts = BooleanArray(2)
        }
    }

    /*public class SoulWarsAttributes {
		private int activity = 30;
		private int productChosen = -1;
		private int team = -1;

		public int getActivity() {
			return activity;
		}

		public void setActivity(int activity) {
			this.activity = activity;
		}

		public int getProductChosen() {
			return productChosen;
		}

		public void setProductChosen(int prodouctChosen) {
			this.productChosen = prodouctChosen;
		}

		public int getTeam() {
			return team;
		}

		public void setTeam(int team) {
			this.team = team;
		}
	}*/
    inner class GodwarsDungeonAttributes {
        var killcount = IntArray(4) // 0 = armadyl, 1 = bandos, 2 = saradomin, 3 = zamorak
        private var enteredRoom = false
        var altarDelay: Long = 0
            private set

        fun hasEnteredRoom(): Boolean {
            return enteredRoom
        }

        fun setHasEnteredRoom(enteredRoom: Boolean) {
            this.enteredRoom = enteredRoom
        }

        fun setAltarDelay(altarDelay: Long): GodwarsDungeonAttributes {
            this.altarDelay = altarDelay
            return this
        }
    }

    inner class DungeoneeringAttributes {
        lateinit var party: DungeoneeringParty
        lateinit var partyInvitation: DungeoneeringParty
        var lastInvitation: Long = 0
        var boundItems = IntArray(5)
        var damageDealt = 0
        var deaths = 0
        fun incrementDamageDealt(damage: Int) {
            damageDealt += damage
        }

        fun incrementDeaths() {
            deaths++
        }
    }
}