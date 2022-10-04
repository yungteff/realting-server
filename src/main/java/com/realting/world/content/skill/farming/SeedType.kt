package com.realting.world.content.skill.farming;

public enum SeedType {
	HERB, ALLOTMENT, FLOWER;

	public static SeedType forId(int id) {
		for(SeedType type : SeedType.values()) {
			if(type != null && type.ordinal() == id)
				return type;
		}
		return HERB;
	}
}