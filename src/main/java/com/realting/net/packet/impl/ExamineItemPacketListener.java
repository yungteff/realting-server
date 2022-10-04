package com.realting.net.packet.impl;

import com.realting.model.SkillLevel;
import com.realting.model.definitions.ItemDefinition;
import com.realting.net.packet.Packet;
import com.realting.net.packet.PacketListener;
import com.realting.util.Misc;
import com.realting.world.content.skill.herblore.FinishedPotions;
import com.realting.model.entity.character.player.Player;

public class ExamineItemPacketListener implements PacketListener {

	@Override
	public void handleMessage(Player player, Packet packet) {
		int item = packet.readUnsignedShort();
		if(item == 995 || item == 18201) {
			player.getPacketSender().sendMessage("Mhmm... Shining coins...");
			return;
		}
		
		if (ItemDefinition.forId(item) != null && ItemDefinition.forId(item).getName() != null && ItemDefinition.forId(item).getName().toLowerCase().contains("(unf)")) {
			for (int i = 0; i < FinishedPotions.values().length; i++) {
				if (item == FinishedPotions.values()[i].getUnfinishedPotion()) {
					player.getPacketSender().sendMessage("Finish this potion with a "+ItemDefinition.forId(FinishedPotions.values()[i].getItemNeeded()).getName()+".");
					return;
				}
			}
		}
		if (item == 12926 || item == 12934) {
			ItemDefinition itemDef = ItemDefinition.forId(item);
			if(itemDef != null) {
				player.getPacketSender().sendMessage("@gre@<shad=0>You currently have "+Misc.format(player.getBlowpipeCharges())+" Zulrah scales stored.");
			}
		}
		ItemDefinition itemDef = ItemDefinition.forId(item);
		if(itemDef != null) {
			player.getPacketSender().sendMessage(itemDef.getDescription());
			for (SkillLevel level : itemDef.getRequirements()) {
				if (level.getLevel() > player.getSkillManager().getMaxLevel(level.getSkill())) {
					String skillName = Misc.formatText(level.getSkill().getName());
					player.getPacketSender().sendMessage("@red@Attention: You need " +
							Misc.anOrA(skillName) + " " + skillName + " level of at least " + level.getLevel() + " to wear this.");
				}
			}
		}
	}

}
