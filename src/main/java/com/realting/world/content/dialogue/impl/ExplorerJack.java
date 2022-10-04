package com.realting.world.content.dialogue.impl;

import com.realting.GameSettings;
import com.realting.model.input.impl.ItemSearch;
import com.realting.util.Misc;
import com.realting.world.content.dialogue.Dialogue;
import com.realting.world.content.dialogue.DialogueExpression;
import com.realting.world.content.dialogue.DialogueType;
import com.realting.model.entity.character.player.Player;

public class ExplorerJack {

	public static Dialogue getDialogue(Player player) {
		return new Dialogue() {

			@Override
			public DialogueType type() {
				return DialogueType.NPC_STATEMENT;
			}

			@Override
			public DialogueExpression animation() {
				return DialogueExpression.NORMAL;
			}

			@Override
			public int npcId() {
				return 7969;
			}
			
			@Override
			public String[] dialogue() {
				return new String[]{"Hello adventurer.", "I have travelled all around "+GameSettings.RSPS_NAME, "and can tell about the items I've encountered.", "Which item would you like to know about?"};
			}
			
			public Dialogue nextDialogue() {
				return new Dialogue() {

					@Override
					public DialogueType type() {
						return DialogueType.NPC_STATEMENT;
					}

					@Override
					public DialogueExpression animation() {
						return DialogueExpression.NORMAL;
					}

					@Override
					public int npcId() {
						return 7969;
					}
					
					@Override
					public String[] dialogue() {
						return new String[]{"Hello adventurer.", "I have traveled all around "+GameSettings.RSPS_NAME, "and can tell about the items I've encountered.", "Which item would you like to know about?"};
					}
				
					@Override
					public void specialAction() {
						player.getPacketSender().sendInterfaceRemoval();
						player.setInputHandling(new ItemSearch());
						player.getPacketSender().sendEnterInputPrompt("Enter an item's full name below...");
					}
				};
				
			}
		};
	}
	
	public static Dialogue foundDrop(String itemName, String npcName) {
		return new Dialogue() {
			
			@Override
			public DialogueType type() {
				return DialogueType.NPC_STATEMENT;
			}

			@Override
			public DialogueExpression animation() {
				return DialogueExpression.NORMAL;
			}

			@Override
			public int npcId() {
				return 7969;
			}
			
			@Override
			public String[] dialogue() {
				return new String[]{"Ah, yes! The "+Misc.formatText(itemName)+".", "I killed "+Misc.anOrA(npcName)+" "+npcName+" once", "and received it as a drop.", "Perhaps you should try doing the same?"};
			}
		};
	}
}
