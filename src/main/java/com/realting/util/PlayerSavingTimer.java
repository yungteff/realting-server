package com.realting.util;

import com.realting.GameSettings;
import com.realting.world.World;

public class PlayerSavingTimer {
	
	public static long massSaveTimer = System.currentTimeMillis();

	public static void massSaving() {
		if (System.currentTimeMillis() - massSaveTimer > GameSettings.charcterSavingInterval) {
			World.savePlayers();
			massSaveTimer = System.currentTimeMillis();
		}
	}
	
}
