package com.realting.world.content.player.events

import com.realting.model.*
import com.realting.world.content.clan.ClanChatManager
import com.realting.world.content.PlayerLogs
import com.realting.model.entity.character.player.Player
import com.realting.world.content.dialogue.DialogueManager
import com.realting.model.movement.MovementQueue
import com.realting.util.Misc
import com.realting.world.content.CustomObjects

object Gambling {
    @JvmStatic
    fun rollDice(player: Player) {
        if (!player.rights.isMember) {
            player.packetSender.sendMessage("You need to be a member to use this item.")
            return
        }
        if (player.location !== Locations.Location.VARROCK) {
            player.packetSender.sendMessage("").sendMessage("This dice can only be used in the gambling area!")
                .sendMessage("To get there, type :;gamble.")
            return
        }
        if (player.clanChatName == null) {
            player.packetSender.sendMessage("You need to be in a clanchat channel to roll a dice.")
            return
        } else if (player.clanChatName.equals("help", ignoreCase = true)) {
            player.packetSender.sendMessage("You can't roll a dice in this clanchat channel!")
            return
        } else if (player.clanChatName.equals("kandarin", ignoreCase = true)) {
            player.packetSender.sendMessage("You can't roll a dice in this clanchat channel!")
            return
        } else if (!player.currentClanChat.ownerName.equals(player.username, ignoreCase = true)) {
            player.packetSender.sendMessage("You must be the Owner of the clanchat to roll the dice.")
            return
        }
        if (!player.clickDelay.elapsed(5000)) {
            player.packetSender.sendMessage("You must wait 5 seconds between each dice cast.")
            return
        }
        val roll = Misc.getRandom(100)
        player.movementQueue.reset()
        player.performAnimation(Animation(11900))
        player.performGraphic(Graphic(2075))
        ClanChatManager.sendMessage(
            player.currentClanChat,
            "@bla@[ClanChat] @whi@" + player.username + " just rolled @bla@" + roll + "@whi@ on the percentile dice."
        )
        PlayerLogs.log(
            player.username, "[ClanChat]" + player.username + " just rolled" + roll + "on the percentile dice."
        )
        player.clickDelay.reset()
    }

    @JvmStatic
    fun plantSeed(player: Player) {
        if (player.rights == PlayerRights.PLAYER) {
            player.packetSender.sendMessage("You need to be a member to use this item.")
            return
        }
        if (player.location !== Locations.Location.VARROCK) {
            player.packetSender.sendMessage("").sendMessage("This seed can only be planted in the gambling area")
                .sendMessage("To get there, talk to the gambler.")
            return
        }
        if (!player.clickDelay.elapsed(3000)) return
        for (npc in player.localNpcs) {
            if (npc != null && npc.position == player.position) {
                player.packetSender.sendMessage("You cannot plant a seed right here.")
                return
            }
        }
        if (CustomObjects.objectExists(player.position.copy())) {
            player.packetSender.sendMessage("You cannot plant a seed right here.")
            return
        }
        val flowers = FlowersData.generate()
        val flower = GameObject(flowers.objectId, player.position.copy())
        player.movementQueue.reset()
        player.inventory.delete(299, 1)
        player.performAnimation(Animation(827))
        player.packetSender.sendMessage("You plant the seed..")
        player.movementQueue.reset()
        player.dialogueActionId = 42
        player.interactingObject = flower
        DialogueManager.start(player, 78)
        MovementQueue.stepAway(player)
        CustomObjects.globalObjectRemovalTask(flower, 90)
        player.positionToFace = flower.position
        player.clickDelay.reset()
    }

    enum class FlowersData(var objectId: Int, var itemId: Int) {
        PASTEL_FLOWERS(2980, 2460), RED_FLOWERS(2981, 2462), BLUE_FLOWERS(2982, 2464), YELLOW_FLOWERS(
            2983, 2466
        ),
        PURPLE_FLOWERS(2984, 2468), ORANGE_FLOWERS(2985, 2470), RAINBOW_FLOWERS(2986, 2472), WHITE_FLOWERS(
            2987, 2474
        ),
        BLACK_FLOWERS(2988, 2476);

        companion object {
            @JvmStatic
            fun forObject(`object`: Int): FlowersData? {
                for (data in values()) {
                    if (data.objectId == `object`) return data
                }
                return null
            }

            fun generate(): FlowersData {
                val RANDOM = Math.random() * 100
                return if (RANDOM >= 1) {
                    values()[Misc.getRandom(6)]
                } else {
                    if (Misc.getRandom(3) == 1) WHITE_FLOWERS else BLACK_FLOWERS
                }
            }
        }
    }
}