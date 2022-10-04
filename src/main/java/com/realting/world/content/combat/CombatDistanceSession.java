package com.realting.world.content.combat;

import com.realting.model.Locations;
import com.realting.model.Locations.Location;
import com.realting.model.entity.character.CharacterEntity;

public class CombatDistanceSession {

	/** The combat builder. */
	private CombatBuilder builder;

	/** The victim being hunted. */
	private CharacterEntity victim;

	/**
	 * Create a new {@link CombatDistanceSession}.
	 * 
	 * @param builder
	 *            the combat builder.
	 * @param victim
	 *            the victim being hunted.
	 */
	public CombatDistanceSession(CombatBuilder builder, CharacterEntity victim) {
		this.builder = builder;
		this.victim = victim;
	}

	public void process() {
		builder.determineStrategy();
		builder.attackTimer = 0;

		if(builder.getVictim() != null && !builder.getVictim().equals(victim)) {
			builder.reset(true);
			stop();
			return;
		}
				
		if(!Location.ignoreFollowDistance(builder.getCharacter())) {
			if (!Locations.goodDistance(builder.getCharacter().getPosition(), victim.getPosition(), 40)) {
				builder.reset(true);
				stop();
				return;
			}
		}
		
		if(Locations.goodDistance(builder.getCharacter().getPosition(), victim.getPosition(), builder.getStrategy().attackDistance(builder.getCharacter()))) {
			sucessFul();
			stop();
			return;
		}
	}
	
	public void stop() {
		builder.setDistanceSession(null);
	}
	
	private void sucessFul() {
		builder.getCharacter().getMovementQueue().reset();
		builder.setVictim(victim);
        builder.setCombatSession(new CombatSession(builder));
	}
}
