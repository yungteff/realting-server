package com.realting.model.entity;

import com.realting.engine.task.TaskManager;
import com.realting.model.GameObject;
import com.realting.net.PlayerSession;
import com.realting.net.SessionState;
import com.realting.world.World;
import com.realting.world.clip.region.RegionClipping;
import com.realting.world.content.CustomObjects;
import com.realting.model.entity.character.npc.NPC;
import com.realting.model.entity.character.player.Player;

public class EntityHandler {

	public static void register(Entity entity) {
		if(entity.isPlayer()) {
			Player player = (Player) entity;
			PlayerSession session = player.getSession();
			if (session.getState() == SessionState.LOGGING_IN && !World.getLoginQueue().contains(player)) {
				World.getLoginQueue().add(player);
			}
		} if(entity.isNpc()) {
			NPC npc = (NPC) entity;
			World.getNpcs().add(npc);
		} else if(entity.isGameObject()) {
			GameObject gameObject = (GameObject) entity;
			RegionClipping.addObject(gameObject);
			CustomObjects.spawnGlobalObjectWithinDistance(gameObject);
		}
	}

	public static void deregister(Entity entity) {
		if(entity.isPlayer()) {
			Player player = (Player)entity;
			World.getPlayers().remove(player);
		} else if(entity.isNpc()) {
			NPC npc = (NPC)entity;
			TaskManager.cancelTasks(npc.getCombatBuilder());
			TaskManager.cancelTasks(entity);
			World.getNpcs().remove(npc);
		} else if(entity.isGameObject()) {
			GameObject gameObject = (GameObject) entity;
			RegionClipping.removeObject(gameObject);
			CustomObjects.deleteGlobalObjectWithinDistance(gameObject);
		}
	}
}
