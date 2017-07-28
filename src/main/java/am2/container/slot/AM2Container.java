package am2.container.slot;

import am2.ArsMagica2;
import am2.utils.InventoryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public abstract class AM2Container extends Container{

	public AM2Container(){
	}
	
	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player){
		Slot slot = slotId < 0 ? null : (Slot)this.inventorySlots.get(slotId);
		if (slot instanceof SlotGhostItem){
			return slotClickGhost(slot, dragType, clickTypeIn, player);
		}
		return super.slotClick(slotId, dragType, clickTypeIn, player);
	}

	public ItemStack slotClickGhost(Slot slot, int mouseButton, ClickType clickTypeIn, EntityPlayer player){
		ItemStack stack = null;

		if (mouseButton == 2){
			if (((IGhostSlot)slot).canAdjust()){
				slot.putStack(null);
			}
		}else if (mouseButton == 0 || mouseButton == 1){
			InventoryPlayer playerInv = player.inventory;
			slot.onSlotChanged();
			ItemStack stackSlot = slot.getStack();
			ItemStack stackHeld = playerInv.getItemStack();

			if (stackSlot != null){
				stack = stackSlot.copy();
			}

			if (stackSlot == null){
				if (stackHeld != null && slot.isItemValid(stackHeld)){
					fillGhostSlot(slot, stackHeld, mouseButton, clickTypeIn);
				}
			}else if (stackHeld == null){
				adjustGhostSlot(slot, mouseButton, clickTypeIn);
				slot.onTake(player, playerInv.getItemStack());
			}else if (slot.isItemValid(stackHeld)){
				if (InventoryUtilities.canStacksMerge(stackSlot, stackHeld)){
					adjustGhostSlot(slot, mouseButton, clickTypeIn);
				}else{
					fillGhostSlot(slot, stackHeld, mouseButton, clickTypeIn);
				}
			}
		}

		return stack;
	}

	private void adjustGhostSlot(Slot slot, int mouseButton, ClickType clickTypeIn){
		if (!((IGhostSlot)slot).canAdjust()){
			return;
		}

		ItemStack stackSlot = slot.getStack();
		int stackSize;
		if (clickTypeIn == ClickType.PICKUP){
			stackSize = mouseButton == 0 ? (stackSlot.getCount() + 1) / 2 : stackSlot.getCount() + 2;
		}else{
			stackSize = mouseButton == 0 ? stackSlot.getCount() - 1 : stackSlot.getCount() + 1;
		}

		if (stackSize > slot.getSlotStackLimit()){
			stackSize = slot.getSlotStackLimit();
		}

		stackSlot.setCount(stackSize);

		if (stackSlot.getCount() <= 0){
			slot.putStack((ItemStack)null);
		}
	}

	private void fillGhostSlot(Slot slot, ItemStack stackHeld, int mouseButton, ClickType clickTypeIn){
		if (!((IGhostSlot)slot).canAdjust()){
			return;
		}

		int stackSize = mouseButton == 0 ? stackHeld.getCount() : 1;
		if (stackSize > slot.getSlotStackLimit()){
			stackSize = slot.getSlotStackLimit();
		}
		ItemStack ghostStack = stackHeld.copy();
		ghostStack.setCount(stackSize);

		slot.putStack(ghostStack);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex){
		ItemStack originalStack = null;
		Slot slot = (Slot)inventorySlots.get(slotIndex);
		int numSlots = inventorySlots.size();
		if (slot != null && slot.getHasStack()){
			ItemStack stackInSlot = slot.getStack();
			originalStack = stackInSlot.copy();
			if (slotIndex >= numSlots - 9 * 4 && tryShiftItem(stackInSlot, numSlots)){

			}else if (slotIndex >= numSlots - 9 * 4 && slotIndex < numSlots - 9){
				if (!shiftItemStack(stackInSlot, numSlots - 9, numSlots)){
					return null;
				}
			}else if (slotIndex >= numSlots - 9 && slotIndex < numSlots){
				if (!shiftItemStack(stackInSlot, numSlots - 9 * 4, numSlots - 9)){
					return null;
				}
			}else if (!shiftItemStack(stackInSlot, numSlots - 9 * 4, numSlots)){
				return null;
			}
			slot.onSlotChange(stackInSlot, originalStack);
			if (stackInSlot.getCount() <= 0){
				slot.putStack(null);
			}else{
				slot.onSlotChanged();
			}

			if (stackInSlot.getCount() == originalStack.getCount()){
				return null;
			}

			slot.onTake(player, stackInSlot);
		}
		return originalStack;
	}

	private boolean shiftItemStack(ItemStack stackToShift, int start, int end){
		boolean changed = false;
		if (stackToShift.isStackable()){
			for (int slotIndex = start; stackToShift.getCount() > 0 && slotIndex < end; slotIndex++){
				Slot slot = (Slot)inventorySlots.get(slotIndex);
				ItemStack stackInSlot = slot.getStack();
				if (stackInSlot != null && InventoryUtilities.canStacksMerge(stackInSlot, stackToShift)){
					int resultingStackSize = stackInSlot.getCount() + stackToShift.getCount();
					int max = Math.min(stackToShift.getMaxStackSize(), slot.getSlotStackLimit());
					if (resultingStackSize <= max){
						stackToShift.setCount(0);;
						stackInSlot.setCount(resultingStackSize);
						slot.onSlotChanged();
						changed = true;
					}else if (stackInSlot.getCount() < max){
						stackToShift.shrink(max - stackInSlot.getCount());
						stackInSlot.setCount(max);
						slot.onSlotChanged();
						changed = true;
					}
				}
			}
		}

		if (stackToShift.getCount() > 0){
			for (int slotIndex = start; stackToShift.getCount() > 0 && slotIndex < end; slotIndex++){
				Slot slot = (Slot)inventorySlots.get(slotIndex);
				ItemStack stackInSlot = slot.getStack();
				if (stackInSlot == null){
					int max = Math.min(stackToShift.getMaxStackSize(), slot.getSlotStackLimit());
					stackInSlot = stackToShift.copy();
					stackInSlot.setCount(Math.min(stackToShift.getCount(), max));
					stackToShift.shrink(stackInSlot.getCount());
					slot.putStack(stackInSlot);
					slot.onSlotChanged();
					changed = true;
				}
			}
		}

		return changed;
	}

	private boolean tryShiftItem(ItemStack stackToShift, int numSlots){
		for (int machineIndex = 0; machineIndex < numSlots - 9 * 4; machineIndex++){
			Slot slot = (Slot)inventorySlots.get(machineIndex);
			if (slot instanceof IGhostSlot){
				continue;
			}
			if (shiftItemStack(stackToShift, machineIndex, machineIndex + 1)){
				return true;
			}
		}

		return false;
	}

	protected void addPlayerInventory(EntityPlayer player, int left, int top){
		//display player inventory
		for (int i = 0; i < 3; i++){
			for (int k = 0; k < 9; k++){
				addSlotToContainer(new Slot(player.inventory, k + i * 9 + 9, left + k * 18, top + i * 18));
			}
		}
	}

	protected void addPlayerActionBar(EntityPlayer player, int left, int top){
		//display player action bar
		for (int j1 = 0; j1 < 9; j1++){
			addSlotToContainer(new Slot(player.inventory, j1, left + j1 * 18, top));
		}
	}
	
	@Override
	public void detectAndSendChanges(){
		for (int i = 0; i < this.inventorySlots.size(); ++i)
        {
            ItemStack itemstack = (((Slot)this.inventorySlots.get(i)).getStack() == null) ? ItemStack.EMPTY :((Slot)this.inventorySlots.get(i)).getStack();
            ItemStack itemstack1 = (ItemStack)this.inventoryItemStacks.get(i);

            if (!ItemStack.areItemStacksEqual(itemstack1, itemstack))
            {
                itemstack1 = itemstack.isEmpty() ? ItemStack.EMPTY : itemstack.copy();
                this.inventoryItemStacks.set(i, itemstack1);

                for (int j = 0; j < this.listeners.size(); ++j)
                {
                    ((IContainerListener)this.listeners.get(j)).sendSlotContents(this, i, itemstack1);
                }
            }
        }
	}
}
