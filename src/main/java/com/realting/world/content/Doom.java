package com.realting.world.content;

import com.realting.model.Position;
import com.realting.util.Misc;
import com.realting.world.World;
import com.realting.model.entity.character.npc.NPC;
import com.realting.model.entity.character.player.Player;

public class Doom {
	
	private static int[] Monsters = {667, 1613, 1614, 934, 998, 2919, 3064, 3593, 998, 999, 1000, 3062, 5835, 5841, 5840, 5839, 190, 2780, 1002, 746, 935, 502, 503, 1575, 3065, 2717, 3851, 504, 505, 5397, 5399, 4973, 2782, 3665, 49, 3586, 269};
	
	public static void spawnMonsters(Player player) {
		for (int i=0; i < Monsters.length; i++) {
			Position DoomSpawn = new Position(2318+Misc.getRandom(6), 5224+Misc.getRandom(6));
			NPC monster = new NPC(Monsters[i], DoomSpawn);
			World.register(monster);
			monster.getCombatBuilder().attack(player);
			System.out.println("Spawned: "+Monsters[i]);
		}
		World.sendMessage("SHIT'S DONE FAM");
	}
	
}
