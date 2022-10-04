package com.realting.model.input.impl;

import com.realting.model.container.impl.Bank.BankSearchAttributes;
import com.realting.model.input.Input;
import com.realting.model.entity.character.player.Player;

public class EnterSyntaxToBankSearchFor extends Input {

	@Override
	public void handleSyntax(Player player, String syntax) {
		boolean searchingBank = player.isBanking() && player.getBankSearchingAttribtues().isSearchingBank();
		if(searchingBank)
			BankSearchAttributes.beginSearch(player, syntax);
	}
}
