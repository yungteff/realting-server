package com.realting.model.container;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.realting.model.GroundItem;
import com.realting.model.Item;
import com.realting.model.PlayerRights;
import com.realting.model.container.impl.Bank;
import com.realting.model.container.impl.Shop;
import com.realting.model.definitions.ItemDefinition;
import com.realting.model.entity.character.GroundItemManager;
import com.realting.model.entity.character.player.Player;

/**
 * Represents a container which contains items.
 * 
 * @author relex lawl
 */

public abstract class ItemContainer {

	/**
	 * The amount of items the container can hold, such as 28 for inventory.
	 */
	public abstract int capacity();

	/**
	 * The container's type enum, see enum for information.
	 */
	public abstract StackType stackType();

	/**
	 * The refresh method to send the container's interface on addition or deletion of an item.
	 */
	public abstract ItemContainer refreshItems();

	/**
	 * The full method which contains the content a player will receive upon container being full,
	 * such as a message when inventory is full.
	 */
	public abstract ItemContainer full();

	/**
	 * ItemContainer constructor to create a new instance and to define the player.
	 * @param player	Player who owns the item container.
	 */
	public ItemContainer(Player player) {
		this.player = player;
		for (int i = 0; i < capacity(); i++) {
			items[i] = new Item(-1, 0);
		}
	}

	/**
	 * ItemContainer constructor to create a new instance and to define the player.
	 * @param player	Player who owns the item container.
	 */
	public ItemContainer(Player player, int capacity) {
		this.player = player;
		items = new Item[capacity];
		for (int i = 0; i < capacity; i++) {
			items[i] = new Item(-1, 0);
		}
	}

	/**
	 * Player who owns the item container.
	 */
	private Player player;

	/**
	 * Gets the owner's player instance.
	 * @return	player.
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * Sets the player viewing the container, used
	 * for containers such as Shops.
	 */
	public ItemContainer setPlayer(Player player) {
		this.player = player;
		return this;
	}

	/**
	 * The items located in the container.
	 */
	private Item[] items = new Item[capacity()];

	/**
	 * Gets the items in the container.
	 * @return	items.
	 */
	public Item[] getItems() {
		return items;
	}

	public Item[] getCopiedItems() {
		Item[] it = new Item[items.length];
		for(int i = 0; i < it.length; i++) {
			it[i] = items[i].copy();
		}
		return it;
	}

	/**
	 * Gets the valid items in the container,
	 * @return items in a list format.
	 */
	public List<Item> getValidItems() {
		List<Item> items = new ArrayList<Item>();
		for (Item item : this.items) {
			if (item != null && item.getId() > 0 && item.getAmount() > 0) {
				items.add(item);
			}
		}
		return items;
	}

	public Item[] getValidItemsArray() {
		List<Item> items = getValidItems();
		Item[] array = new Item[items.size()];
		for(int i = 0; i < items.size(); i++) {
			array[i] = items.get(i);
		}
		return array;
	}

	/**
	 * Sets all the items in the container.
	 * @param items		The item array to which set the container to hold.
	 */
	public ItemContainer setItems(Item[] items) {
		
		if(!(this instanceof Shop)) { //Fixes an exploit
			if(items.length != capacity()) {
				return this;
			}
		}
		
		this.items = items;
		return this;
	}

	/**
	 * Sets the item in said slot.
	 * @param slot	Slot to set item for.
	 * @param item	Item that will occupy the slot.
	 */
	public ItemContainer setItem(int slot, Item item) {
		items[slot] = item;
		return this;
	}

	/**
	 * Checks if the slot contains an item.
	 * @param slot	The container slot to check.
	 * @return		items[slot] != null.
	 */
	public boolean isSlotOccupied(int slot) {
		return items[slot] != null && items[slot].getId() > 0 && items[slot].getAmount() > 0;
	}

	/**
	 * Swaps two item slots.
	 * @param fromSlot 	From slot.
	 * @param toSlot 	To slot.
	 */
	public ItemContainer swap(int fromSlot, int toSlot) {
		Item temporaryItem = getItems()[fromSlot];
		if(temporaryItem == null || temporaryItem.getId() <= 0)
			return this;
		setItem(fromSlot, getItems()[toSlot]);
		setItem(toSlot, temporaryItem);
		return this;
	}

	public ItemContainer insert(int fromSlot, int toSlot) {
		Item temporaryItem = getItems()[fromSlot];
		if(temporaryItem == null || temporaryItem.getId() <= 0)
			return this;

		if (fromSlot == toSlot)
			return this;
		if (get(fromSlot) == null)
			return this;

		Item item = get(fromSlot);
		items[fromSlot] = new Item(-1, 0);

		if (fromSlot < toSlot) {
			for (int slot = fromSlot + 1; slot <= toSlot; slot++) {
				items[slot - 1] = items[slot];
				items[slot] = new Item(-1, 0);
			}
		} else {
			for (int slot = fromSlot - 1; slot >= toSlot; slot--) {
				items[slot + 1] = items[slot];
				items[slot] = new Item(-1, 0);
			}
		}

		items[toSlot] = item;

		return this;
	}

	/**
	 * Gets the amount of free slots the container has.
	 * @return	Total amount of free slots in container.
	 */
	public int getFreeSlot() {
		for (int index = 0; index < items.length; index++) {
			if (items[index].getId() == -1) {
				return index;
			}
		}
		return -1;
	}

	/**
	 * Gets the amount of free slots the container has.
	 * @return	Total amount of free slots in container.
	 */
	public int getFreeSlots() {
		int space = 0;
		for (Item item : items) {
			if (item.getId() == -1) {
				space++;
			}
		}
		return space;
	}

	/**
	 * Checks if the container is out of available slots.
	 * @return	No free slot available.
	 */
	public boolean isFull() {
		return getEmptySlot() == -1;
	}

	/**
	 * Checks if container contains a certain item id.
	 * @param id	The item id to check for in container.
	 * @return		Container contains item with the specified id.
	 */
	public boolean contains(int id) {
		for (Item items : this.items) {
			if (items != null && items.getId() == id) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if this container has a set of certain items.
	 *
	 * @param item
	 * the item to check in this container for.
	 * @return true if this container has the item.
	 */
	public boolean contains(Item[] item) {
		if (item.length == 0) {
			return false;
		}

		for (Item nextItem : item) {
			if (nextItem == null) {
				continue;
			}

			if (!contains(nextItem.getId())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Gets the next empty slot for an item to equip.
	 * @return	The next empty slot index.
	 */
	public int getEmptySlot() {
		for (int i = 0; i < capacity(); i++) {
			if (items[i].getId() <= 0 || items[i].getAmount() <= 0) {
				return i;
			}
		}
		return -1;
	}

	public int getSlot(Item item) {
		for (int i = 0; i < capacity(); i++) {
			if (items[i].getId() > 0 && items[i].getId() == item.getId() && items[i].getAmount() > 0 && !items[i].getAttributes().hasAttributes()) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Gets the first slot found for an item with said id.
	 * @param id	The id to loop through items to find.
	 * @return		The slot index the item is located in.
	 */
	public int getSlot(int id) {
		for (int i = 0; i < capacity(); i++) {
			if (items[i].getId() > 0 && items[i].getId() == id && items[i].getAmount() > 0) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Gets the total amount of items in the container with the specified id.
	 * @param id	The id of the item to search for.
	 * @return 		The total amount of items in the container with said id.
	 */
	public int getAmount(int id) {
		int totalAmount = 0;
		for (Item item : items) {
			if (item.getId() == id) {
				totalAmount += item.getAmount();
			}
		}
		return totalAmount;
	}

	/**
	 * Gets the total amount of items in the container in the specified slot
	 * @param slot	The slot of the item to search for.
	 * @return 		The total amount of items in the container with said slot.
	 */
	public int getAmountForSlot(int slot) {
		return items[slot].getAmount();
	}

	/**
	 * Resets items in the container.
	 * @return The ItemContainer instance.
	 */
	public ItemContainer resetItems() {
		for (int i = 0; i < capacity(); i++) {
			items[i] = new Item(-1, 0);
		}
		return this;
	}

	/**
	 * Gets an item by their slot index.
	 * @param slot	Slot to check for item.
	 * @return		Item in said slot.
	 */
	public Item forSlot(int slot) {
		return items[slot];
	}

	/**
	 * Switches an item from one item container to another.
	 * @param to		The item container to put item on.
	 * @param item		The item to put from one container to another.
	 * @param fromSlot		The slot of the item to switch from one container to another.
	 * @param sort		This flag checks whether or not to sort items, such as for bank.
	 * @return			The ItemContainer instance.
	 */
	public ItemContainer switchItem(ItemContainer to, Item item, int fromSlot, boolean sort, boolean refresh) {
		if (getItems()[fromSlot].getId() != item.getId()) {
			return this;
		}
		if (to.getFreeSlots() <= 0 && !(to.contains(item.getId()) && item.getDefinition().isStackable())) {
			to.full();
			return this;
		}
		if(this instanceof Shop) {
			delete(item, fromSlot, refresh, to);
		}
		to.add(item, refresh);
		if (sort && getAmount(item.getId()) <= 0)
			sortItems();
		if (refresh) {
			refreshItems();
			to.refreshItems();
		}
		return this;
	}


	/**
	 * Switches an item from one item container to another.
	 * @param from		The item container to get item 
	 * @param to		The item container to put item on.
	 * @param item		The item to put from one container to another.
	 * @param slot		The slot of the item to switch from one container to another.
	 * @param sort		This flag checks whether or not to sort items, such as for bank.
	 * @return			The ItemContainer instance.
	 */
	public ItemContainer switchItem(ItemContainer from, ItemContainer to, Item item, int slot, boolean sort, boolean refresh) {
		if (from.getItems()[slot].getId() != item.getId())
			return this;
		if (to.getFreeSlots() <= 0 && !(to.contains(item.getId()) && item.getDefinition().isStackable())) {
			to.full();
			return this;
		}
		from.delete(item, slot, refresh, to);
		to.add(item);
		if (sort && getAmount(item.getId()) <= 0)
			sortItems();
		if (refresh) {
			from.refreshItems();
			to.refreshItems();
		}
		return this;
	}

	/**
	 * Switches an item from one item container to another.
	 * @param from		The item container to get item 
	 * @param to		The item container to put item on.
	 * @param item		The item to put from one container to another.
	 * @param sort		This flag checks whether or not to sort items, such as for bank.
	 * @return			The ItemContainer instance.
	 */
	public ItemContainer switchItem(ItemContainer from, ItemContainer to, Item item, boolean sort, boolean refresh) {
		if (to.getFreeSlots() <= 0 && !(to.contains(item.getId()) && item.getDefinition().isStackable())) {
			to.full();
			return this;
		}
		from.delete(item);
		to.add(item);
		if (sort && getAmount(item.getId()) <= 0)
			sortItems();
		if (refresh) {
			from.refreshItems();
			to.refreshItems();
		}
		return this;
	}

	/*
	 * Checks if container is full
	 */
	public boolean full(int itemId) {
		return this.getFreeSlots() <= 0 && !(this.contains(itemId) && new Item(itemId).getDefinition().isStackable());
	}

	/**
	 * Switches an item from one item container to another. THIS METHOD IS MAINLY USED FOR SELLING ITEMS TO SHOPS
	 * @param from		The item container to get item 
	 * @param to		The item container to put item on.
	 * @param itemId	The item's id to put from one container to another.
	 * @param amount	The amount of the item to put from one container to another.
	 * @return			The ItemContainer instance.
	 */

	public ItemContainer switchItem(ItemContainer from, ItemContainer to, int itemId, int amount) {
		if (full(itemId)) {
			to.full();
			return this;
		}
		Item item = new Item(getPlayer().getInventory().getItems()[from.getSlot(itemId)].getId());
		if(item == null || item.getId() <= 0 || item.getAmount() <= 0)
			return this;
		if(item.getDefinition().isStackable() && amount > 1) { //Item is stackable 
			item.setAmount(from.getAmount(item.getId()));
			from.delete(item.getId(), amount, false);
			to.add(item.getId(), amount);
		} else {
			from.delete(item.getId(), item.getAmount(), false);
			to.add(item);
		}
		return this;
	}

	public ItemContainer addItems(Item[] items, boolean refresh) {
		if(items == null)
			return this;
		for(Item item : items) {
			if(item.getId() > 0 && item.getAmount() > 0) {
				this.add(item, refresh);
			}
		}
		return this;
	}

	/**
	 * Sorts this item container's array of items to leave no empty spaces.
	 * @return	The ItemContainer instance.
	 */
	public ItemContainer sortItems() {
		for(int k = 0; k < capacity(); k++) {
			if(getItems()[k] == null)
				continue;
			for (int i = 0; i < (capacity() - 1); i++) {
				if (getItems()[i] == null || getItems()[i].getId() <= 0 || getItems()[i].getAmount() <= 0) {
					swap((i + 1), i);
				}
			}
		}
		return this;
	}

	/**
	 * Does the container have enough slots to hold these items
	 * @param items
	 * 			the items
	 * @return
	 * 		true if the container has enough slots to hold the items
	 */
	public boolean hasRoomFor(Item...items){
		int slots = 0;
		for (Item item : items) {
			if (item != null) {
				if (item.getDefinition().isStackable()) {
					if (contains(item.getId())) {
						if (!hasRoomFor(item)) {
							return false;
						}
					} else {
						slots++;
					}
				} else {
					slots++;
				}
			}
		}

		return getFreeSlots() >= slots;
	}

	/**
	 * Checks if there is room in the inventory for an item.
	 * @param item The item.
	 * @return <code>true</code> if so, <code>false</code> if not.
	 */
	public boolean hasRoomFor(Item item) {
		if(item.getDefinition().isStackable() || stackType() == StackType.STACKS) {
			for(int i = 0; i < items.length; i++) {
				if(items[i] != null && items[i].getId() == item.getId()) {
					long totalCount = (long) item.getAmount() + items[i].getAmount();
					if(totalCount > Integer.MAX_VALUE) {
						return false;
					}
					return true;
				}
			}
			return getFreeSlot() != -1;
		} else {
			return getFreeSlots() >= item.getAmount();
		}

	}

	/**
	 * Adds an item to the item container.
	 * @param item	The item to add.
	 * @return		The ItemContainer instance.
	 */
	public ItemContainer add(Item item) {
		return add(item, true);
	}

	/**
	 * Adds an item to the item container.
	 * @param id		The id of the item.
	 * @param amount	The amount of the item.
	 * @return			The ItemContainer instance.
	 */
	public ItemContainer add(int id, int amount) {
		return add(new Item(id, amount));
	}

	/**
	 * Adds an item to the item container.
	 * @param item		The item to add.
	 * @param refresh	If <code>true</code> the item container interface will be refreshed.
	 * @return			The ItemContainer instance.
	 */
	public ItemContainer add(Item item, boolean refresh) {
		if(item == null || item.getId() <= 0 || item.getAmount() <= 0)
			return this;
		Preconditions.checkArgument(ItemDefinition.forId(item.getId()) != null, "Item definition is null: " + item.getId());
		/*if(item.getId() == 995 && this instanceof Inventory)  {
			if(this instanceof Inventory) {
				if(getAmount(item.getId()) + item.getAmount() >= Integer.MAX_VALUE || getAmount(item.getId()) + item.getAmount() <= -1) {
					getPlayer().setMoneyInPouch(getPlayer().getMoneyInPouch() + item.getAmount());
					getPlayer().getPacketSender().sendString(8135, ""+player.getMoneyInPouch()+"");
					getPlayer().getPacketSender().sendMessage("The coins that you could not hold in your inventory have been placed in your pouch.");
					return this;
				}
			}
		}*/

		if (ItemDefinition.forId(item.getId()).isStackable() && !item.getAttributes().hasAttributes() || stackType() == StackType.STACKS && !item.getAttributes().hasAttributes()) {
			int slot = getSlot(item);
			if (slot == -1)
				slot = getEmptySlot();
			if (slot == -1) {
				if(getPlayer().getRights() != PlayerRights.OWNER && getPlayer().getRights() != PlayerRights.DEVELOPER) {
					GroundItemManager.spawnGroundItem(player, new GroundItem(item, player.getPosition().copy(), player.getUsername(), player.getHostAddress(), false, 120, player.getPosition().getZ() >= 0 && player.getPosition().getZ() < 4 ? true : false, 60));
					getPlayer().getPacketSender().sendMessage("The item which you couldn't hold has been placed beneath you.");
					if (refresh)
						refreshItems();
				}
				return this;
			}
			long totalAmount = items[slot].getAmount() + item.getAmount();
			if (totalAmount > Integer.MAX_VALUE) {
				int notAdded = (int) totalAmount - Integer.MAX_VALUE;
				totalAmount = Integer.MAX_VALUE;
				item.setAmount(notAdded);
				items[slot].setId(item.getId());
				items[slot].setAmount(Integer.MAX_VALUE);
			} else {
				items[slot].setId(item.getId());
				items[slot].setAmount(items[slot].getAmount() + item.getAmount());
			}
		} else {
			int amount = item.getAmount();
			while (amount > 0) {
				int slot = getEmptySlot();
				if (slot == -1) {
					if(getPlayer().getRights() != PlayerRights.OWNER && getPlayer().getRights() != PlayerRights.DEVELOPER) {
						if (!item.getAttributes().hasAttributes()) {
							GroundItemManager.spawnGroundItem(player, new GroundItem(Item.getNoted(item.getId(), amount), player.getPosition().copy(), player.getUsername(), false, 120, player.getPosition().getZ() >= 0 && player.getPosition().getZ() < 4 ? true : false, 60));
							getPlayer().getPacketSender().sendMessage("The item(s) which you couldn't hold have been placed beneath you.");
							if (refresh)
								refreshItems();
							return this;
						} else {
							throw new IllegalStateException("Item that has attributes cannot be a ground item.");
						}
					}
				} else {
					if (item.getAttributes().hasAttributes()) {
						if (item.getAmount() > 1) {
							throw new IllegalStateException("Item without attributes cannot be stacked.");
						} else {
							items[slot] = item;
						}
					} else {
						items[slot].setId(item.getId());
						items[slot].setAmount(1);
					}
				}
				amount--;
			}
		}
		if (refresh)
			refreshItems();
		return this;
	}

	/**
	 * Deletes an item from the item container.
	 * @param item	The item to delete.
	 * @return		The ItemContainer instance.
	 */
	public ItemContainer delete(Item item) {
		return delete(item.getId(), item.getAmount());
	}

	/**
	 * Deletes an item from the item container.
	 * @param item	The item to delete.
	 * @param slot	The slot of the item (used to delete the item from said slot, not the first one found).
	 * @return		The ItemContainer instance.
	 */
	public ItemContainer delete(Item item, int slot) {
		return delete(item, slot, true);
	}

	/**
	 * Deletes an item from the item container.
	 * @param id		The id of the item to delete.
	 * @param amount	The amount of the item to delete.
	 * @return			The ItemContainer instance.
	 */
	public ItemContainer delete(int id, int amount) {
		return delete(id, amount, true);
	}

	/**
	 * Deletes an item from the item container.
	 * @param id		The id of the item to delete.
	 * @param amount	The amount of the item to delete.
	 * @param refresh	If <code>true</code> the item container interface will refresh.
	 * @return			The ItemContainer instance.
	 */
	public ItemContainer delete(int id, int amount, boolean refresh) {
		return delete(new Item(id, amount), getSlot(id), refresh);
	}

	/**
	 * Deletes an item from the item container.
	 * @param item		The item to delete.
	 * @param slot		The slot of the item to delete.
	 * @param refresh	If <code>true</code> the item container interface will refresh.
	 * @return			The ItemContainer instance.
	 */
	public ItemContainer delete(Item item, int slot, boolean refresh) {
		return delete(item, slot, refresh, null);
	}

	/**
	 * Deletes an item from the item container.
	 * @param item			The item to delete.
	 * @param slot			The slot of the item to delete.
	 * @param refresh		If <code>true</code> the item container interface will refresh.
	 * @param toContainer	To check if other container has enough space to continue deleting said amount from this container.
	 * @return				The ItemContainer instance.
	 */
	public ItemContainer delete(Item item, int slot, boolean refresh, ItemContainer toContainer) {
		if (item == null || slot < 0)
			return this;
		if (item.getAmount() > getAmount(item.getId()))
			item.setAmount(getAmount(item.getId()));
		if (item.getDefinition().isStackable() || stackType() == StackType.STACKS) {
			if (toContainer != null && !item.getDefinition().isStackable() && item.getAmount() > toContainer.getFreeSlots() && !(this instanceof Bank))
				item.setAmount(toContainer.getFreeSlots());
			if (items[slot].getAmount() - item.getAmount() < 1) {
				items[slot] = new Item(-1, 0);
			} else {
				items[slot].setAmount(items[slot].getAmount() - item.getAmount());
			}
		} else {
			int amount = item.getAmount();
			while (amount > 0) {
				if (slot == -1 || (toContainer != null && toContainer.isFull()))
					break;
				items[slot] = new Item(-1, 0);
				slot = getSlot(item.getId());
				amount--;
			}
		}
		if (refresh)
			refreshItems();
		return this;
	}

	/**
	 * Gets an item id by its index.
	 *
	 * @param id the id
	 * @return the item id on this index.
	 */
	public Item getById(int id) {
		for (int i = 0; i < items.length; i++) {
			if (items[i] == null) {
				continue;
			}
			if (items[i].getId() == id) {
				return items[i];
			}
		}
		return null;
	}

	public boolean containsAll(int... ids) {
		return Arrays.stream(ids).allMatch(id -> contains(id));
	}

	public boolean containsAll(Item... items) {
		return Arrays.stream(items).filter(Objects::nonNull).allMatch(item -> contains(item.getId()));
	}


	public boolean containsAny(int... ids) {
		return Arrays.stream(ids).anyMatch(id -> contains(id));
	}

	public void set(int slot, Item item) {
		items[slot] = item;
	}

	public Item get(int slot) {
		return items[slot];
	}

	public boolean isSlotFree(int slot) {
		return items[slot] == null || items[slot].getId() == -1;
	}

	public Item[] toSafeArray() {
		return Iterables.toArray(Arrays.stream(items).filter(Objects::nonNull).collect(Collectors.toList()), Item.class);
	}

	public void moveItems(ItemContainer to, boolean refreshOrig, boolean refreshTo) {

		for (Item it : getValidItems()) {
			if(to.getFreeSlots() <= 0 && !(to.contains(it.getId()) && it.getDefinition().isStackable())) {
				break;
			}
			to.add(it, false);
			delete(it.getId(), it.getAmount(), false);
		}

		if(refreshOrig) {
			refreshItems();
		}
		if(refreshTo) {
			to.refreshItems();
		}
	}

	/**
	 * Adds a set of items into the inventory.
	 *
	 * @param item
	 * the set of items to add.
	 */
	public void addItemSet(Item[] item) {
		for (Item addItem : item) {
			if (addItem == null) {
				continue;
			}
			add(addItem);
		}
	}

	/**
	 * Deletes a set of items from the inventory.
	 *
	 * @param item
	 * the set of items to delete.
	 */
	public void deleteItemSet(Item[] item) {
		for (Item deleteItem : item) {
			if (deleteItem == null) {
				continue;
			}

			delete(deleteItem);
		}
	}
}

