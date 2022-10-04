package com.realting.engine.task.impl;

import com.realting.engine.task.Task;
import com.realting.engine.task.TaskManager;
import com.realting.model.entity.character.player.Player;

public class FireImmunityTask extends Task {

	public FireImmunityTask(final Player p) {
		super(2, p, false);
		this.p = p;
	}

	final Player p;

	@Override
	public void execute() {
		if(p == null || !p.isRegistered()) {
			stop();
			return;
		}
		if(p.getFireImmunity() > 0) {
			p.setFireImmunity(p.getFireImmunity()- 1);
			if(p.getFireImmunity() == 20) 
				p.getPacketSender().sendMessage("Your resistance to dragonfire is about to run out.");
		} else {
			p.setFireImmunity(0).setFireDamageModifier(0);
			p.getPacketSender().sendMessage("Your resistance to dragonfire has run out.");
			stop();
		}
	}

	public static void makeImmune(final Player p, int seconds, int fireDamageModifier) {
		boolean startEvent = p.getFireImmunity() == 0;
		p.setFireImmunity(seconds).setFireDamageModifier(fireDamageModifier);
		if(startEvent) {
			TaskManager.submit(new FireImmunityTask(p));
		}
	}
}
