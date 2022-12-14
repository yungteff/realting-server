package com.realting.world.content.player.skill.construction;

/**
 * 
 * @author Owner Blade
 *
 */
public class HouseFurniture {

	private int roomX, roomY, roomZ, hotSpotId, furnitureId, standardXOff, standardYOff;
	public HouseFurniture(int roomX, int roomY, int roomZ, int hotSpotId, int furnitureId, int standardXOff, int standardYOff)
	{
		this.roomX = roomX;
		this.roomY = roomY;
		this.setRoomZ(roomZ);
		this.hotSpotId = hotSpotId;
		this.furnitureId = furnitureId;
		this.standardXOff = standardXOff;
		this.standardYOff = standardYOff;
	}
	public ConstructionData.HotSpots getHotSpot(int roomRot)
	{
		return ConstructionData.HotSpots.forObjectIdAndCoords(furnitureId, standardXOff, standardYOff);
	}
	public int getRoomY() {
		return roomY;
	}
	public void setRoomY(int roomY) {
		this.roomY = roomY;
	}
	public int getHotSpotId() {
		return hotSpotId;
	}
	public void setHotSpotId(int hotSpotId) {
		this.hotSpotId = hotSpotId;
	}
	public int getFurnitureId() {
		return furnitureId;
	}
	public void setFurnitureId(int furnitureId) {
		this.furnitureId = furnitureId;
	}
	public int getRoomX() {
		return roomX;
	}
	public void setRoomX(int roomX) {
		this.roomX = roomX;
	}
	public int getStandardXOff() {
		return standardXOff;
	}
	public void setStandardXOff(int standardXOff) {
		this.standardXOff = standardXOff;
	}
	public int getStandardYOff() {
		return standardYOff;
	}
	public void setStandardYOff(int standardYOff) {
		this.standardYOff = standardYOff;
	}
	public int getRoomZ() {
		return roomZ;
	}
	public void setRoomZ(int roomZ) {
		this.roomZ = roomZ;
	}
	
}
