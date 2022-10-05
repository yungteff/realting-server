package com.realting.engine.task.impl;

import java.util.Iterator;

import com.realting.engine.task.Task;
import com.realting.engine.task.TaskManager;
import com.realting.world.content.player.skill.hunter.Hunter;
import com.realting.world.content.player.skill.hunter.Trap;
import com.realting.world.content.player.skill.hunter.TrapExecution;

public class HunterTrapsTask extends Task {
	
	public HunterTrapsTask() {
		super(1);
	}

	@Override
	protected void execute() {
		final Iterator<Trap> iterator = Hunter.traps.iterator();
		while (iterator.hasNext()) {
			final Trap trap = iterator.next();
			if (trap == null)
				continue;
			if (trap.getOwner() == null || !trap.getOwner().isRegistered())
				Hunter.deregister(trap);
			TrapExecution.setTrapProcess(trap);
			TrapExecution.trapTimerManagement(trap);
		}
		if(Hunter.traps.isEmpty())
			stop();
	}
	
	@Override
	public void stop() {
		setEventRunning(false);
		running = false;
	}
	
	public static void fireTask() {
		if(running)
			return;
		running = true;
		TaskManager.submit(new HunterTrapsTask());
	}
	
	private static boolean running;
}
