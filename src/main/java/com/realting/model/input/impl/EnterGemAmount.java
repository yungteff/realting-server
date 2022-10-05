package com.realting.model.input.impl;

import com.realting.model.input.EnterAmount;
import com.realting.world.content.player.skill.fletching.Fletching;
import com.realting.model.entity.character.player.Player;

/**
 * Created by brandon on 4/19/2017.
 */
public class EnterGemAmount extends EnterAmount {
    public void handleAmount(Player player, int amount) {
        if(player.getSelectedSkillingItem() > 0) {
            Fletching.crushGems(player, amount, player.getSelectedSkillingItem());
        }
    }
}