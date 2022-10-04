package com.realting.model.input.impl;

import com.realting.GameServer;
import com.realting.GameSettings;
import com.realting.model.input.Input;
import com.realting.util.NameUtils;
import com.realting.model.entity.character.player.Player;
import org.mindrot.jbcrypt.BCrypt;

public class ChangePassword extends Input {

	@Override
	public void handleSyntax(Player player, String syntax) {
		player.getPacketSender().sendInterfaceRemoval();

		if(syntax == null || syntax.length() <= 2 || syntax.length() > 15 || !NameUtils.isValidName(syntax)) {
			player.getPacketSender().sendMessage("That password is invalid. Please try another password.");
			return;
		}
		if(syntax.contains("_")) {
			player.getPacketSender().sendMessage("Your password can not contain underscores.");
			return;
		}
		if(player.getBankPinAttributes().hasBankPin() && !player.getBankPinAttributes().hasEnteredBankPin()) {
			player.getPacketSender().sendMessage("Please visit the nearest bank and enter your pin before doing this.");
			return;
		}
		
		if (true) {
			player.setPassword(syntax);
			if (GameServer.getConfiguration().isEncryptPasswords()) {
				player.setSalt(BCrypt.gensalt(GameSettings.BCRYPT_ROUNDS));
			}
			player.getPacketSender().sendMessage("Your password has been updated.");
		}
		
	}
	
}
