package com.realting.model;

import com.google.common.base.Preconditions;
import com.realting.GameSettings;
import com.realting.ReducedSellPrice;
import com.realting.model.definitions.ItemDefinition;
import com.realting.world.content.Effigies;


/**
 * Represents an item which is owned by a player.
 * 
 * @author relex lawl
 */

public class Item {

	public static int getNoted(int id) {
		int noted = id + 1;
		if(id == 11283 || id == 11284) {
			noted = 11285;
		}
		if(ItemDefinition.forId(noted).getName().equals(ItemDefinition.forId(id).getName())) {
			//System.out.println("getNoted has returned "+noted);
			return noted;
		}
		//System.out.println("getNoted has not returned anything good.");
		return id;
	}

	public static int getUnNoted(int id) {
		int unNoted = id - 1;
		if(id == 11284 || id == 11285) {
			unNoted = 11283;
		}
		if(ItemDefinition.forId(unNoted).getName().equals(ItemDefinition.forId(id).getName())) {
			return unNoted;
		}
		return id;
	}

	public static Item getNoted(int id, int amount) {
		int notedItem = id+1;
		if(ItemDefinition.forId(notedItem).getName().equals(ItemDefinition.forId(id).getName())) {
			return new Item(notedItem, amount);
		}
		return new Item(id, amount);
	}

	public static boolean tradeable(int item) {
		return new Item(item).tradeable();
	}

	public static boolean sellable(int item) {
		return new Item(item).sellable();
	}

	/**
	 * The item id.
	 */
	private int id;

	/**
	 * Amount of the item.
	 */
	private int amount;

	/**
	 * The attributes.
	 */
	private ItemAttributes attributes;

	/**
	 * The item slot.
	 */
	private transient int slot;

	/**
	 * The item rarity.
	 */
	public ItemRarity rarity;

	/**
	 * The item name.
	 */
	private String name;

	/**
	 * An Item object constructor.
	 * @param id		Item id.
	 * @param amount	Item amount.
	 */
	public Item(int id, int amount) {
		this.id = id;
		this.amount = amount;
	}

	/**
	 * An Item object constructor.
	 * @param id		Item id.
	 */
	public Item(int id) {
		this(id, 1);
	}

	/**
	 * Gets item's definition.
	 */
	public ItemDefinition getDefinition() {
		return ItemDefinition.forId(id);
	}

	public boolean tradeable() {
		String name = getDefinition().getName().toLowerCase();
		if(name.contains("clue scroll"))
			return false;
		if(name.contains("overload") || name.contains("extreme"))
			return false;
		if(name.toLowerCase().contains("(deg)") || name.toLowerCase().contains("brawling"))
			return false;
		if(name.toLowerCase().contains("pet"))
			return false;
		for(int i : GameSettings.UNTRADEABLE_ITEMS){
			if(id == i)
				return false;
		}
		if(Effigies.isEffigy(id))
			return false;
		return !getAttributes().hasAttributes();
	}

	public boolean reducedPrice() {
		for (ReducedSellPrice r : ReducedSellPrice.values()) {
			if (r.getUnNotedId() == id || r.getNotedId() == id) {
				return true;
			}
		}
		return false;
	}

	public boolean sellable() {
		String name = getDefinition().getName().toLowerCase();
		if(name.contains("clue scroll"))
			return false;
		if(name.contains("overload") || name.contains("extreme"))
			return false;
		if(name.toLowerCase().contains("(deg)") || name.toLowerCase().contains("brawling"))
			return false;
		for(int i : GameSettings.UNTRADEABLE_ITEMS){
			if(id == i)
				return false;
		}
		for(int i : GameSettings.UNSELLABLE_ITEMS){
			if(id == i)
				return false;
		}
		if(Effigies.isEffigy(id))
			return false;
		return !getAttributes().hasAttributes();
	}

	/**
	 * Copying the item by making a new item with same values.
	 */
	public Item copy() {
		Item item = new Item(id, amount);
		item.getAttributes().copy(this.getAttributes());
		if (amount > 1 && getAttributes().hasAttributes())
			throw new IllegalStateException();
		return item;
	}

	/**
	 * Increment the amount by 1.
	 */
	public void incrementAmount() {
		if ((amount + 1) > Integer.MAX_VALUE) {
			return;
		}
		amount++;
	}

	/**
	 * Decrement the amount by 1.
	 */
	public void decrementAmount() {
		if ((amount - 1) < 0) {
			return;
		}
		amount--;
	}

	/**
	 * Increment the amount by the specified amount.
	 */
	public void incrementAmountBy(int amount) {
		if ((this.amount + amount) > Integer.MAX_VALUE) {
			this.amount = Integer.MAX_VALUE;
		} else {
			this.amount += amount;
		}
	}

	/**
	 * Decrement the amount by the specified amount.
	 */
	public void decrementAmountBy(int amount) {
		if ((this.amount - amount) < 1) {
			this.amount = 0;
		} else {
			this.amount -= amount;
		}
	}

	public Item setRarity(ItemRarity rarity) {
		this.rarity = rarity;
		return this;
	}

	/**
	 * Gets the item's id.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the item's id.
	 * @param id	New item id.
	 */
	public Item setId(int id) {
		this.id = id;
		return this;
	}

	/**
	 * Gets the amount of the item.
	 */
	public int getAmount() {
		return amount;
	}

	public int getSlot() {
		return this.slot;
	}

	public void setSlot(int slot) {
		this.slot = slot;
	}

	/**
	 * Sets the amount of the item.
	 */
	public Item setAmount(int amount) {
		Preconditions.checkState(attributes == null || !attributes.hasAttributes() || amount == 1, "Illegal attribute on stacked item.");
		this.amount = amount;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ItemAttributes getAttributes() {
		if (attributes == null)
			attributes = new ItemAttributes(this);
		return attributes;
	}
}