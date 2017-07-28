package am2.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class InventorySpellBook implements IInventory{
	public static int inventorySize = 40;
	public static int activeInventorySize = 8;
	private NonNullList<ItemStack> inventoryItems = NonNullList.<ItemStack>withSize(inventorySize, ItemStack.EMPTY);

	public InventorySpellBook(){
	}

	public void SetInventoryContents(NonNullList<ItemStack> nonNullList){
		int loops = (int)Math.min(inventorySize, nonNullList.size());
		for (int i = 0; i < loops; ++i){
			inventoryItems.set(i, nonNullList.get(i));
		}
	}

	@Override
	public int getSizeInventory(){
		return inventorySize;
	}

	@Override
	public ItemStack getStackInSlot(int i){
		if (i < 0 || i > inventoryItems.size() - 1){
			return ItemStack.EMPTY;
		}
		return inventoryItems.get(i);
	}

	@Override
	public ItemStack decrStackSize(int i, int j){

		if (!inventoryItems.get(i).isEmpty()){
			if (inventoryItems.get(i).getCount() <= j){
				ItemStack itemstack = inventoryItems.get(i);
				inventoryItems.set(i, ItemStack.EMPTY);
				return itemstack;
			}
			ItemStack itemstack1 = inventoryItems.get(i).splitStack(j);
			if (inventoryItems.get(i).getCount() == 0){
				inventoryItems.set(i, ItemStack.EMPTY);
			}
			return itemstack1;
		}else{
			return ItemStack.EMPTY;
		}
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack){
		inventoryItems.set(i, itemstack);
	}

	@Override
	public String getName(){
		return "Spell Book";
	}

	@Override
	public int getInventoryStackLimit(){
		return 1;
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

	public NonNullList<ItemStack> GetInventoryContents(){
		return inventoryItems;
	}

	@Override
	public ItemStack removeStackFromSlot(int i){
		if (!inventoryItems.get(i).isEmpty()){
			ItemStack itemstack = inventoryItems.get(i);
			inventoryItems.set(i, ItemStack.EMPTY);
			return itemstack;
		}else{
			return ItemStack.EMPTY;
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
		return new TextComponentString(getName());
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
		for(ItemStack item : this.inventoryItems){
			if (!item.isEmpty()) return false;
		}
		return true;
	}

}









