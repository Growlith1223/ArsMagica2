package am2.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class InventoryEssenceBag implements IInventory{
	public static int inventorySize = 12;
	private ItemStack[] inventoryItems;

	public InventoryEssenceBag(){
		inventoryItems = new ItemStack[inventorySize];
	}

	public void SetInventoryContents(ItemStack[] inventoryContents){
		int loops = Math.min(inventorySize, inventoryContents.length);
		for (int i = 0; i < loops; ++i){
			inventoryItems[i] = inventoryContents[i];
		}
	}

	@Override
	public int getSizeInventory(){
		return inventorySize;
	}

	@Override
	public ItemStack getStackInSlot(int i){
		if (i < 0 || i > inventoryItems.length - 1){
			return null;
		}
		return inventoryItems[i];
	}

	@Override
	public ItemStack decrStackSize(int i, int j){

		if (inventoryItems[i] != null){
			if (inventoryItems[i].getCount() <= j){
				ItemStack itemstack = inventoryItems[i];
				inventoryItems[i] = null;
				return itemstack;
			}
			ItemStack itemstack1 = inventoryItems[i].splitStack(j);
			if (inventoryItems[i].getCount() == 0){
				inventoryItems[i] = null;
			}
			return itemstack1;
		}else{
			return null;
		}
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack){
		inventoryItems[i] = itemstack;
	}

	@Override
	public String getName(){
		return "Essence Bag";
	}

	@Override
	public int getInventoryStackLimit(){
		return 64;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer entityplayer){
		return true;
	}

	@Override
	public void openInventory(EntityPlayer player){
	}

	@Override
	public void closeInventory(EntityPlayer player){
	}

	public ItemStack[] GetInventoryContents(){
		return inventoryItems;
	}

	@Override
	public ItemStack removeStackFromSlot(int i){
		if (inventoryItems[i] != null){
			ItemStack itemstack = inventoryItems[i];
			inventoryItems[i] = null;
			return itemstack;
		}else{
			return null;
		}
	}

	@Override
	public boolean hasCustomName(){
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack){
		return false;
	}

	@Override
	public void markDirty(){
	}

	@Override
	public ITextComponent getDisplayName() {

		return null;
	}

	@Override
	public int getField(int id) {

		return 0;
	}

	@Override
	public void setField(int id, int value) {

		
	}

	@Override
	public int getFieldCount() {

		return 0;
	}

	@Override
	public void clear() {

		
	}
	
	@Override
	public boolean isEmpty(){
		for (ItemStack item : this.inventoryItems){
			if (!item.isEmpty()) return false;
		}
		return true;
	}
	


}
