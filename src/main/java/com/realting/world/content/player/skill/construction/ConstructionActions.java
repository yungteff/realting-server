package com.realting.world.content.player.skill.construction;

import com.realting.model.GameObject;
import com.realting.model.entity.character.npc.NPC;
import com.realting.model.entity.character.player.Player;

public class ConstructionActions {

	/*
	 * DIALOGUE ACTION IDS WOODEN LARDER: 700 (2) OAK LOARDER: 701 (4) TEAK
	 * LARDER: 702 (4) 703 (4) 704 (2)
	 */

	public static boolean handleFirstObjectClick(Player p, GameObject obj) {
		switch(obj.getId()) {
		/*case 15478:
			p.setDialogueActionId(87);
			DialogueManager.start(p, 140);
			return true;*/
		}
		return false;
	}
	
	public static boolean handleFirstClickNpc(Player p, NPC npc) {
		switch(npc.getId()) {
		/*case 4247:
			p.setDialogueActionId(86);
			DialogueManager.start(p, 139);
			return true;*/
		}
		return false;
	}

	public static boolean handleItemOnObject(Player p, int objectId, int item) {

		return false;
	}
	
}