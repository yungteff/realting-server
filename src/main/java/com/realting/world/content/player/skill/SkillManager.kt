package com.realting.world.content.player.skill

import com.realting.DiscordBot.JavaCord
import com.realting.engine.task.Task
import com.realting.engine.task.TaskManager
import com.realting.model.*
import com.realting.model.container.impl.Equipment
import com.realting.model.definitions.WeaponAnimations
import com.realting.model.definitions.WeaponInterfaces
import com.realting.model.entity.character.player.Player
import com.realting.util.Misc
import com.realting.webhooks.discord.DiscordMessager
import com.realting.world.World
import com.realting.world.content.*
import com.realting.world.content.combat.prayer.CurseHandler
import com.realting.world.content.combat.prayer.PrayerHandler
import com.realting.world.content.player.events.Achievements
import com.realting.world.content.player.events.Achievements.AchievementData
import com.realting.world.content.player.events.BonusManager
import com.realting.world.content.player.events.BrawlingGloves
import com.realting.world.content.player.skill.dungeoneering.UltimateIronmanHandler
import org.javacord.api.entity.message.embed.EmbedBuilder
import java.awt.Color
import java.util.*

/**
 * Represents a player's skills in the game, also manages
 * calculations such as combat level and total level.
 *
 * @author relex lawl
 * @editor Gabbe
 */
class SkillManager(
    /**
     * The player associated with this Skills instance.
     */
    private val player: Player
) {
    /**
     * Creates a new skillmanager for the player
     * Sets current and max appropriate levels.
     */
    fun newSkillManager() {
        skills = Skills()
        for (i in 0 until MAX_SKILLS) {
            skills!!.maxLevel[i] = 1
            skills!!.level[i] = skills!!.maxLevel[i]
            skills!!.experience[i] = 0
        }
        skills!!.maxLevel[Skill.CONSTITUTION.ordinal] = 100
        skills!!.level[Skill.CONSTITUTION.ordinal] = skills!!.maxLevel[Skill.CONSTITUTION.ordinal]
        skills!!.experience[Skill.CONSTITUTION.ordinal] = 1184
        skills!!.maxLevel[Skill.PRAYER.ordinal] = 10
        skills!!.level[Skill.PRAYER.ordinal] = skills!!.maxLevel[Skill.PRAYER.ordinal]
    }

    /**
     * Roll for successful skilling actions
     * @param skill
     * the skill id
     * @param levelRequired
     * the level required
     * @return
     * if the success roll was true
     */
    fun isSuccess(skill: Skill, levelRequired: Int): Boolean {
        val level = getMaxLevel(skill).toDouble()
        val req = levelRequired.toDouble()
        val successChance = Math.ceil((level * 25 - req * 14) / req / 3 * 4)
        val roll = Misc.rand(99)
        return roll <= successChance
    }

    /**
     * Roll for successful skilling actions
     * @param skill
     * the skill id
     * @param levelRequired
     * the level required
     * @param toolLevelRequired
     * the tool level required
     * @return
     * if the success roll was true
     */
    fun isSuccess(skill: Skill, levelRequired: Int, toolLevelRequired: Int): Boolean {
        var level = getMaxLevel(skill) + (toolLevelRequired / 3).toDouble()
        if (level < levelRequired) {
            level = levelRequired.toDouble()
        }
        val req = levelRequired.toDouble()
        val successChance = Math.ceil((level * 25 - req * 14) / req / 3 * 4)
        val roll = Misc.rand(99)
        return roll <= successChance
    }

    /**
     * Adds experience to `skill` by the `experience` amount.
     * @param skill            The skill to add experience to.
     * @param experience    The amount of experience to add to the skill.
     * @return                The Skills instance.
     */
    @JvmOverloads
    fun addExperience(skill: Skill, experience: Int, multiply: Boolean = true): SkillManager {
        var experience = experience
        try {
            var v: Int
            val sk = skill.ordinal //0=att
            val p = skill.petId
            if (p != -1) {
                v = -1 // set it to -1
                if (sk == 8 || sk == 10 || sk == 14 || sk == 17 || sk == 19 || sk == 20 || sk == 22) { //gathering skills
                    v = 50000
                } else {
                    if (sk == 5 || sk == 7 || sk == 9 || sk == 11 || sk == 12 || sk == 13 || sk == 15 || sk == 23 || sk == 16) { //processing skills
                        v = 50000
                    } else {
                        if (sk == 18) { //longer skill -  slayer
                            v = 5000
                        } else {
                            if (sk == 24) { //dungeoneering, longest
                                v = 500
                            }
                        }
                    }
                }
                if (v != -1) {
                    val q = Misc.getRandom(v)
                    /*if (player.getClanChatName().equalsIgnoreCase("debug")) {
						player.getPacketSender().sendMessage(q+" is what you rolled, with a max of "+v+ " and the number to hit is 1, and 2 for mems..");
						player.getPacketSender().sendMessage("Skilling "+sk+", skill's pet: "+p);
					}*/if (q == 1 || player.rights.isMember && q == 2) { //if you roll the lucky number
                        World.sendMessage("<img=101> <shad=0><col=F300FF>" + player.username + " has just earned a Skilling Pet while training " + skill.formatName + "! @red@CONGRATULATIONS!")
                        player.packetSender.sendMessage("<img=10> <shad=0><col=F300FF>You've found a friend while training!")
                        PlayerLogs.log(player.username, "just earned a " + skill.formatName + " pet!")
                        PlayerLogs.log("1 - pet drops", player.username + " got a " + skill.getName() + " pet drop.")
                        if (player.inventory.freeSlots > 0) {
                            player.inventory.add(p, 1)
                        } else if (!player.getBank(0).isFull) {
                            player.packetSender.sendMessage("Your inventory was full, so we sent your " + skill.formatName + " pet to your bank!")
                            player.getBank(0).add(p, 1)
                        } else {
                            PlayerLogs.log(
                                player.username,
                                player.username + " got a skilling pet, but had a full bank/inv." + skill.petId + ", " + skill.formatName
                            )
                            DiscordMessager.sendStaffMessage(player.username + " got a skilling pet, but had a full bank/inv. ID: " + skill.petId + ", " + skill.formatName)
                            player.packetSender.sendMessage("<img=10>@red@<shad=0> Your inventory, and bank were full, so your pet had no where to go. Contact Crimson for more help.")
                            World.sendMessage("<img=100> <shad=0><col=F300FF>" + player.username + "'s bank is full, so their " + skill.formatName + " pet was lost. Most unfortunate.")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            player.packetSender.sendMessage("An error occured.")
            println(e)
        }
        if (player.experienceLocked()) return this
        /*
		 * If the experience in the skill is already greater or equal to
		 * {@code MAX_EXPERIENCE} then stop.
		 */if (skills!!.experience[skill.ordinal] >= MAX_EXPERIENCE) {
            return this
        }
        if (UltimateIronmanHandler.hasItemsStored(player) && player.location !== Locations.Location.DUNGEONEERING) {
            player.packetSender.sendMessage("<shad=0>@red@You will gain NO EXP until you claim your stored Dungeoneering items.")
            return this
        }
        if (multiply) {
            experience *= player.rights.getExperienceGainModifier(skill).toInt()
        }
        if (WellOfGoodwill.isActive()) {
            experience *= 1.3.toInt()
        }
        if (player.minutesBonusExp != -1) {
            experience *= if (player.gameMode != GameMode.NORMAL) {
                1.30.toInt()
            } else {
                1.30.toInt()
            }
        }
        experience = BrawlingGloves.getExperienceIncrease(player, skill.ordinal, experience)
        if (Misc.isWeekend()) {
            experience *= 2
        }

        /*
		 * The skill's level before adding experience.
		 */
        val startingLevel =
            if (isNewSkill(skill)) (skills!!.maxLevel[skill.ordinal] / 10) else skills!!.maxLevel[skill.ordinal]
        /*
		 * Adds the experience to the skill's experience.
		 */skills!!.experience[skill.ordinal] =
            if (skills!!.experience[skill.ordinal] + experience > MAX_EXPERIENCE) MAX_EXPERIENCE else skills!!.experience[skill.ordinal] + experience
        if (skills!!.experience[skill.ordinal] >= MAX_EXPERIENCE) {
            Achievements.finishAchievement(player, AchievementData.REACH_MAX_EXP_IN_A_SKILL)
        }

        /*
		 * The skill's level after adding the experience.
		 */
        val newLevel = getLevelForExperience(skills!!.experience[skill.ordinal])
        /*
		 * If the starting level less than the new level, level up.
		 */if (newLevel > startingLevel) {
            val level = newLevel - startingLevel
            val skillName = Misc.formatText(skill.toString().lowercase(Locale.getDefault()))
            skills!!.maxLevel[skill.ordinal] += if (isNewSkill(skill)) level * 10 else level
            var Link: String? = null
            /*
			 * If the skill is not constitution, prayer or summoning, then set the current level
			 * to the max level.
			 */setCurrentLevel(skill, getCurrentLevel(skill) + level * if (isNewSkill(skill)) 10 else 1)
            player.dialogue = null
            player.packetSender.sendString(4268, "Congratulations! You have achieved a $skillName level!")
            player.packetSender.sendString(4269, "Well done. You are now level $newLevel.")
            player.packetSender.sendString(358, "Click here to continue.")
            player.packetSender.sendChatboxInterface(skill.chatboxInterface)
            player.performGraphic(Graphic(312))
            player.packetSender.sendMessage("You've just advanced $skillName level! You have reached level $newLevel.")
            Sounds.sendSound(player, Sounds.Sound.LEVELUP)
            if (skills!!.maxLevel[skill.ordinal] == getMaxAchievingLevel(skill)) {
                when (skill.ordinal) {
                    1 -> Link = "https://zenyte.com/img/adventure-icons/defence.png"
                    0 -> Link = "https://zenyte.com/img/adventure-icons/attack.png"
                    3 -> Link = "https://zenyte.com/img/adventure-icons/hitpoints.png"
                    2 -> Link = "https://zenyte.com/img/adventure-icons/strength.png"
                    4 -> {}
                    5 -> {}
                    6 -> {}
                    7 -> {}
                    8 -> {}
                    9 -> {}
                    10 -> {}
                    11 -> {}
                    12 -> {}
                    13 -> {}
                    14 -> {}
                    15 -> {}
                    16 -> {}
                    17 -> {}
                    18 -> {}
                    19 -> {}
                    20 -> {}
                    21 -> {}
                    22 -> {}
                    23 -> {}
                    24 -> {}
                    else -> {}
                }
                player.packetSender.sendMessage("Well done! You've achieved the highest possible level in this skill!")
                World.sendMessage("<shad=15536940><img=10> " + player.username + " has just achieved the highest possible level in " + skillName + "!")
                JavaCord.sendEmbed(
                    "ingame-announcements", EmbedBuilder()
                        .setTitle("New 99! Congratulations adventurer!")
                        .setDescription(player.username + " just achieved level 99 in " + skillName + "!")
                        .setColor(Color.GREEN)
                        .setTimestampToNow()
                        .setThumbnail(Link)
                        .setFooter("Powered by JavaCord")
                )
                if (maxed(player)) {
                    //Achievements.finishAchievement(player, AchievementData.REACH_LEVEL_99_IN_ALL_SKILLS);
                    World.sendMessage("<shad=15536940><img=10> " + player.username + " has just achieved the highest possible level in all skills!")
                    JavaCord.sendEmbed(
                        "ingame-announcements", EmbedBuilder()
                            .setTitle("New maxed adventurer!")
                            .setDescription(player.username + " just achieved level 99 in all skills! Congratulations!")
                            .setColor(Color.MAGENTA)
                            .setTimestampToNow()
                            .setThumbnail("https://zenyte.com/img/adventure-icons/overall.png")
                            .setFooter("Powered by JavaCord")
                    )
                }
                TaskManager.submit(object : Task(2, player, true) {
                    var localGFX = 1634
                    public override fun execute() {
                        player.performGraphic(Graphic(localGFX))
                        if (localGFX == 1637) {
                            stop()
                            return
                        }
                        localGFX++
                        player.performGraphic(Graphic(localGFX))
                    }
                })
            } else {
                TaskManager.submit(object : Task(2, player, false) {
                    public override fun execute() {
                        player.performGraphic(Graphic(199))
                        stop()
                    }
                })
            }
            player.updateFlag.flag(Flag.APPEARANCE)
        }
        updateSkill(skill)
        totalGainedExp += experience.toLong()
        return this
    }

    fun skillCape(skill: Skill): Boolean {
        val c = skill.skillCapeId
        val ct = skill.skillCapeTrimmedId
        return if (player.checkItem(Equipment.CAPE_SLOT, c) || player.checkItem(Equipment.CAPE_SLOT, ct) ||
            player.checkItem(Equipment.CAPE_SLOT, 14019) || player.checkItem(
                Equipment.CAPE_SLOT,
                14022
            ) || player.checkItem(Equipment.CAPE_SLOT, 20081) || player.checkItem(
                Equipment.CAPE_SLOT,
                22052
            ) && player.skillManager.getMaxLevel(skill) >= 99 && player.rights.isMember
        ) {
            true
        } else {
            false
        }
    }

    fun stopSkilling(): SkillManager {
        if (player.currentTask != null) {
            player.currentTask.stop()
            player.currentTask = null
        }
        player.resetPosition = null
        player.inputHandling = null
        return this
    }

    /**
     * Updates the skill strings, for skill tab and orb updating.
     * @param skill    The skill who's strings to update.
     * @return        The Skills instance.
     */
    fun updateSkill(skill: Skill): SkillManager {
        var maxLevel = getMaxLevel(skill)
        var currentLevel = getCurrentLevel(skill)
        if (skill == Skill.PRAYER) player.packetSender.sendString(687, "$currentLevel/$maxLevel")
        if (isNewSkill(skill)) {
            maxLevel = maxLevel / 10
            currentLevel = currentLevel / 10
        }
        player.packetSender.sendString(31200, Integer.toString(totalLevel))
        player.packetSender.sendString(19000, "Combat level: $combatLevel")
        player.packetSender.sendSkill(skill)
        return this
    }

    fun resetSkill(skill: Skill, prestige: Boolean): SkillManager {
        if (player.equipment.freeSlots != player.equipment.capacity()) {
            player.packetSender.sendMessage("Please unequip all your items first.")
            return this
        }
        if (player.location === Locations.Location.WILDERNESS || player.combatBuilder.isBeingAttacked) {
            player.packetSender.sendMessage("You cannot do this at the moment")
            return this
        }
        if (prestige && player.skillManager.getMaxLevel(skill) < getMaxAchievingLevel(skill)) {
            player.packetSender.sendMessage("You must have reached the maximum level in a skill to prestige in it.")
            return this
        }
        if (prestige) {
            val pts = getPrestigePoints(player, skill)
            player.pointsHandler.setPrestigePoints(pts, true)
            player.packetSender.sendMessage("You've received $pts Prestige points!")
            PlayerPanel.refreshPanel(player)
        } else {
            player.inventory.delete(13663, 1)
        }
        setCurrentLevel(
            skill,
            if (skill == Skill.PRAYER) 10 else if (skill == Skill.CONSTITUTION) 100 else 1
        ).setMaxLevel(skill, if (skill == Skill.PRAYER) 10 else if (skill == Skill.CONSTITUTION) 100 else 1)
            .setExperience(skill, getExperienceForLevel(if (skill == Skill.CONSTITUTION) 10 else 1))
        PrayerHandler.deactivateAll(player)
        CurseHandler.deactivateAll(player)
        BonusManager.update(player)
        WeaponInterfaces.assign(player, player.equipment[Equipment.WEAPON_SLOT])
        WeaponAnimations.update(player)
        player.packetSender.sendMessage("You have reset your " + skill.formatName + " level.")
        return this
    }

    /**
     * Calculates the player's combat level.
     * @return    The average of the player's combat skills.
     */
    val combatLevel: Int
        get() {
            val attack = skills!!.maxLevel[Skill.ATTACK.ordinal]
            val defence = skills!!.maxLevel[Skill.DEFENCE.ordinal]
            val strength = skills!!.maxLevel[Skill.STRENGTH.ordinal]
            val ranged = skills!!.maxLevel[Skill.RANGED.ordinal]
            val magic = skills!!.maxLevel[Skill.MAGIC.ordinal]
            val summoning = skills!!.maxLevel[Skill.SUMMONING.ordinal]
            var combatLevel = 3
            combatLevel =
                ((defence + (skills!!.maxLevel[Skill.CONSTITUTION.ordinal] / 10) + Math.floor(((skills!!.maxLevel[Skill.PRAYER.ordinal] / 10) / 2).toDouble())) * 0.2535).toInt() + 1
            val melee = (attack + strength) * 0.325
            val ranger = Math.floor(ranged * 1.5) * 0.325
            val mage = Math.floor(magic * 1.5) * 0.325
            if (melee >= ranger && melee >= mage) {
                combatLevel += melee.toInt()
            } else if (ranger >= melee && ranger >= mage) {
                combatLevel += ranger.toInt()
            } else if (mage >= melee && mage >= ranger) {
                combatLevel += mage.toInt()
            }
            if (player.location !== Locations.Location.WILDERNESS) {
                combatLevel += (summoning * 0.125).toInt()
            } else {
                if (combatLevel > 126) {
                    return 126
                }
            }
            if (combatLevel > 138) {
                return 138
            } else if (combatLevel < 3) {
                return 3
            }
            return combatLevel
        }/*
				 * Other-wise add the maxLevel / 10, used for 'constitution' and prayer * 10.
				 *//*
			 * If the skill is not equal to constitution or prayer, total can 
			 * be summed up with the maxLevel.
			 */

    /**
     * Gets the player's total level.
     * @return    The value of every skill summed up.
     */
    val totalLevel: Int
        get() {
            var total = 0
            for (skill in Skill.values()) {
                /*
			 * If the skill is not equal to constitution or prayer, total can 
			 * be summed up with the maxLevel.
			 */
                total += if (!isNewSkill(skill)) {
                    skills!!.maxLevel[skill.ordinal]
                    /*
				 * Other-wise add the maxLevel / 10, used for 'constitution' and prayer * 10.
				 */
                } else {
                    skills!!.maxLevel[skill.ordinal] / 10
                }
            }
            return total
        }

    /**
     * Gets the player's total experience.
     * @return    The experience value from the player's every skill summed up.
     */
    val totalExp: Long
        get() {
            var xp: Long = 0
            for (skill in Skill.values()) xp += player.skillManager.getExperience(skill).toLong()
            return xp
        }

    /**
     * Gets the current level for said skill.
     * @param skill        The skill to get current/temporary level for.
     * @return            The skill's level.
     */
    fun getCurrentLevel(skill: Skill): Int {
        return skills!!.level[skill.ordinal]
    }

    /**
     * Gets the max level for said skill.
     * @param skill        The skill to get max level for.
     * @return            The skill's maximum level.
     */
    fun getMaxLevel(skill: Skill): Int {
        return skills!!.maxLevel[skill.ordinal]
    }

    /**
     * Gets the max level for said skill.
     * @param skill        The skill to get max level for.
     * @return            The skill's maximum level.
     */
    fun getMaxLevel(skill: Int): Int {
        return skills!!.maxLevel[skill]
    }

    /**
     * Gets the experience for said skill.
     * @param skill        The skill to get experience for.
     * @return            The experience in said skill.
     */
    fun getExperience(skill: Skill): Int {
        return skills!!.experience[skill.ordinal]
    }

    /**
     * Sets the current level of said skill.
     * @param skill        The skill to set current/temporary level for.
     * @param level        The level to set the skill to.
     * @param refresh    If `true`, the skill's strings will be updated.
     * @return            The Skills instance.
     */
    fun setCurrentLevel(skill: Skill, level: Int, refresh: Boolean): SkillManager {
        skills!!.level[skill.ordinal] = if (level < 0) 0 else level
        if (refresh) updateSkill(skill)
        return this
    }

    /**
     * Sets the maximum level of said skill.
     * @param skill        The skill to set maximum level for.
     * @param level        The level to set skill to.
     * @param refresh    If `true`, the skill's strings will be updated.
     * @return            The Skills instance.
     */
    fun setMaxLevel(skill: Skill, level: Int, refresh: Boolean): SkillManager {
        skills!!.maxLevel[skill.ordinal] = level
        if (refresh) updateSkill(skill)
        return this
    }

    /**
     * Sets the experience of said skill.
     * @param skill            The skill to set experience for.
     * @param experience    The amount of experience to set said skill to.
     * @param refresh        If `true`, the skill's strings will be updated.
     * @return                The Skills instance.
     */
    fun setExperience(skill: Skill, experience: Int, refresh: Boolean): SkillManager {
        skills!!.experience[skill.ordinal] = if (experience < 0) 0 else experience
        if (refresh) updateSkill(skill)
        return this
    }

    /**
     * Sets the current level of said skill.
     * @param skill        The skill to set current/temporary level for.
     * @param level        The level to set the skill to.
     * @return            The Skills instance.
     */
    fun setCurrentLevel(skill: Skill, level: Int): SkillManager {
        setCurrentLevel(skill, level, true)
        return this
    }

    /**
     * Sets the maximum level of said skill.
     * @param skill        The skill to set maximum level for.
     * @param level        The level to set skill to.
     * @return            The Skills instance.
     */
    fun setMaxLevel(skill: Skill, level: Int): SkillManager {
        setMaxLevel(skill, level, true)
        return this
    }

    /**
     * Sets the experience of said skill.
     * @param skill            The skill to set experience for.
     * @param experience    The amount of experience to set said skill to.
     * @return                The Skills instance.
     */
    fun setExperience(skill: Skill, experience: Int): SkillManager {
        setExperience(skill, experience, true)
        return this
    }

    var skills: Skills? = null
    var totalGainedExp: Long = 0

    inner class Skills {
        val level: IntArray
        val maxLevel: IntArray
        val experience: IntArray

        init {
            level = IntArray(MAX_SKILLS)
            maxLevel = IntArray(MAX_SKILLS)
            experience = IntArray(MAX_SKILLS)
        }
    }

    /**
     * The skillmanager's constructor
     * @param player    The player's who skill set is being represented.
     */
    init {
        newSkillManager()
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun maxed(p: Player): Boolean {
            for (i in Skill.values().indices) {
                if (i == 21) continue
                if (p.skillManager.getMaxLevel(i) < (if (i == 3 || i == 5) 990 else 99)) {
                    return false
                }
            }
            return true
        }

        @kotlin.jvm.JvmStatic
        fun getPrestigePoints(player: Player, skill: Skill): Int {
            val MAX_EXP = MAX_EXPERIENCE.toFloat()
            val experience = player.skillManager.getExperience(skill).toFloat()
            val basePoints = skill.prestigePoints
            var bonusPointsModifier: Double =
                if (player.gameMode == GameMode.IRONMAN) 1.3 else (if (player.gameMode == GameMode.ULTIMATE_IRONMAN) 1.6 else 1) as Double
            bonusPointsModifier += (experience / MAX_EXP * 5).toDouble()
            return (basePoints * bonusPointsModifier).toInt()
        }

        /**
         * Gets the minimum experience in said level.
         * @param level        The level to get minimum experience for.
         * @return            The least amount of experience needed to achieve said level.
         */
        @kotlin.jvm.JvmStatic
        fun getExperienceForLevel(level: Int): Int {
            var level = level
            if (level <= 99) {
                return EXP_ARRAY[if (--level > 98) 98 else level]
            } else {
                var points = 0
                var output = 0
                for (lvl in 1..level) {
                    points += Math.floor(lvl + 300.0 * Math.pow(2.0, lvl / 7.0)).toInt()
                    if (lvl >= level) {
                        return output
                    }
                    output = Math.floor((points / 4).toDouble()).toInt()
                }
            }
            return 0
        }

        /**
         * Gets the level from said experience.
         * @param experience    The experience to get level for.
         * @return                The level you obtain when you have specified experience.
         */
        @kotlin.jvm.JvmStatic
        fun getLevelForExperience(experience: Int): Int {
            if (experience <= EXPERIENCE_FOR_99) {
                for (j in 98 downTo 0) {
                    if (EXP_ARRAY[j] <= experience) {
                        return j + 1
                    }
                }
            } else {
                var points = 0
                var output = 0
                for (lvl in 1..99) {
                    points += Math.floor(lvl + 300.0 * Math.pow(2.0, lvl / 7.0)).toInt()
                    output = Math.floor((points / 4).toDouble()).toInt()
                    if (output >= experience) {
                        return lvl
                    }
                }
            }
            return 99
        }

        /**
         * Checks if the skill is a x10 skill.
         * @param skill        The skill to check.
         * @return            The skill is a x10 skill.
         */
        fun isNewSkill(skill: Skill): Boolean {
            return skill == Skill.CONSTITUTION || skill == Skill.PRAYER
        }

        /**
         * Gets the max level for `skill`
         * @param skill        The skill to get max level for.
         * @return            The max level that can be achieved in said skill.
         */
        @kotlin.jvm.JvmStatic
        fun getMaxAchievingLevel(skill: Skill): Int {
            var level = 99
            if (isNewSkill(skill)) {
                level = 990
            }
            /*if (skill == Skill.DUNGEONEERING) {
			level = 120;
		}*/return level
        }

        /**
         * The maximum amount of skills in the game.
         */
        const val MAX_SKILLS = 25

        /**
         * The maximum amount of experience you can
         * achieve in a skill.
         */
        private const val MAX_EXPERIENCE = 2000000000
        private const val EXPERIENCE_FOR_99 = 13034431
        private val EXP_ARRAY = intArrayOf(
            0, 83, 174, 276, 388, 512, 650, 801, 969, 1154, 1358, 1584, 1833, 2107, 2411, 2746, 3115, 3523,
            3973, 4470, 5018, 5624, 6291, 7028, 7842, 8740, 9730, 10824, 12031, 13363, 14833, 16456, 18247,
            20224, 22406, 24815, 27473, 30408, 33648, 37224, 41171, 45529, 50339, 55649, 61512, 67983, 75127,
            83014, 91721, 101333, 111945, 123660, 136594, 150872, 166636, 184040, 203254, 224466, 247886,
            273742, 302288, 333804, 368599, 407015, 449428, 496254, 547953, 605032, 668051, 737627, 814445,
            899257, 992895, 1096278, 1210421, 1336443, 1475581, 1629200, 1798808, 1986068, 2192818, 2421087,
            2673114, 2951373, 3258594, 3597792, 3972294, 4385776, 4842295, 5346332, 5902831, 6517253, 7195629,
            7944614, 8771558, 9684577, 10692629, 11805606, 13034431
        )
    }
}