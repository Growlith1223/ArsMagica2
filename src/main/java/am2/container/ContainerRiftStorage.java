package am2.container;

import am2.api.extensions.IRiftStorage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerRiftStorage extends Container{

	private final IRiftStorage inventory;
	private int PLAYER_INVENTORY_START = 0;
	private int PLAYER_ACTIONBAR_START = 0;
	private int PLAYER_ACTIONBAR_END = 0;

	public ContainerRiftStorage(EntityPlayer player, IRiftStorage iRiftStorage){
		this.inventory = iRiftStorage;
		int rows = 1;
		switch (iRiftStorage.getAccessLevel()){
		case 1:
			rows = 1;
			break;
		case 2:
			rows = 3;
			break;
		case 3:
			rows = 6;
			break;
		}

		PLAYER_INVENTORY_START = rows * 9;
		PLAYER_ACTIONBAR_START = PLAYER_INVENTORY_START + 27;
		PLAYER_ACTIONBAR_END = PLAYER_ACTIONBAR_START + 9;


		//chest inventory slots
		for (int j = 0; j < rows; ++j){
			for (int i = 0; i < 9; ++i){
				addSlotToContainer(new Slot(inventory, i + j * 9, 8 + (18 * i), 13 + (j * 18)));
			}
		}

		//display player inventory
		for (int i = 0; i < 3; i++){
			for (int k = 0; k < 9; k++){
				addSlotToContainer(new Slot(player.inventory, k + i * 9 + 9, 8 + k * 18, 130 + i * 18));
			}
		}

		//display player action bar
		for (int j1 = 0; j1 < 9; j1++){
			addSlotToContainer(new Slot(player.inventory, j1, 8 + j1 * 18, 188));
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int i){
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = (Slot)inventorySlots.get(i);
		if (slot != null && slot.getHasStack()){
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (i < PLAYER_INVENTORY_START){
				if (!mergeItemStack(itemstack1, PLAYER_INVENTORY_START, PLAYER_ACTIONBAR_END, true)){
					return ItemStack.EMPTY;
				}
			}else if (i >= PLAYER_INVENTORY_START && i < PLAYER_ACTIONBAR_START){
				if (!mergeItemStack(itemstack1, 0, PLAYER_INVENTORY_START - 1, false)){
					return ItemStack.EMPTY;
				}
			}else if (i >= PLAYER_ACTIONBAR_START && i < PLAYER_ACTIONBAR_END){
				if (!mergeItemStack(itemstack1, 0, PLAYER_INVENTORY_START - 1, false)){
					return ItemStack.EMPTY;
				}
			}else if (!mergeItemStack(itemstack1, PLAYER_INVENTORY_START, PLAYER_ACTIONBAR_END, false)){
				return ItemStack.EMPTY;
			}
			if (itemstack1.getCount() == 0){
				slot.putStack(ItemStack.EMPTY);
				slot.onSlotChange(itemstack, itemstack1);
			}else{
				slot.onSlotChanged();
			}
			if (itemstack1.getCount() == itemstack.getCount()){
				return ItemStack.EMPTY;
			}
		}
		return itemstack;
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer){
		return inventory.isUsableByPlayer(entityplayer);
	}

}
