package am2.container;

import am2.api.items.ISpellFocus;
import am2.api.items.ItemFilterFocus;
import am2.blocks.tileentity.TileEntitySeerStone;
import am2.container.slot.AM2Container;
import am2.container.slot.SlotFilterFociOnly;
import am2.container.slot.SlotGhostRune;
import am2.container.slot.SlotSpellFocusOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerSeerStone extends AM2Container{
	private TileEntitySeerStone stone;

	private static final int PLAYER_INVENTORY_START = 5;
	private static final int PLAYER_ACTION_BAR_START = 32;
	private static final int PLAYER_ACTION_BAR_END = 41;

	public ContainerSeerStone(InventoryPlayer inventoryplayer, TileEntitySeerStone stone){
		this.stone = stone;
		addSlotToContainer(new SlotSpellFocusOnly(stone, 0, 44, 48)); //inventory, index, x, y
		addSlotToContainer(new SlotFilterFociOnly(stone, 1, 116, 48));

		addSlotToContainer(new SlotGhostRune(stone, 2, 62, 8));
		addSlotToContainer(new SlotGhostRune(stone, 3, 80, 8));
		addSlotToContainer(new SlotGhostRune(stone, 4, 98, 8));

		//display player inventory
		for (int i = 0; i < 3; i++){
			for (int k = 0; k < 9; k++){
				addSlotToContainer(new Slot(inventoryplayer, k + i * 9 + 9, 8 + k * 18, 98 + i * 18));
			}
		}

		//display player action bar
		for (int j1 = 0; j1 < 9; j1++){
			addSlotToContainer(new Slot(inventoryplayer, j1, 8 + j1 * 18, 156));
		}

	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer){
		return stone.isUsableByPlayer(entityplayer);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int i){
		ItemStack itemstack = ItemStack.EMPTY;

		Slot slot = (Slot)inventorySlots.get(i);
		if (slot != null && slot.getHasStack()){
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (i < PLAYER_INVENTORY_START){
				if (!mergeItemStack(itemstack1, PLAYER_INVENTORY_START, PLAYER_ACTION_BAR_END, true)){
					return ItemStack.EMPTY;
				}
			}else if (i >= PLAYER_INVENTORY_START && i < PLAYER_ACTION_BAR_START) //from player inventory
			{
				if (!mergeSpecialItems(itemstack1, slot)){
					if (!mergeItemStack(itemstack1, PLAYER_ACTION_BAR_START, PLAYER_ACTION_BAR_END, false)){
						return ItemStack.EMPTY;
					}
				}else{
					return ItemStack.EMPTY;
				}
			}else if (i >= PLAYER_ACTION_BAR_START && i < PLAYER_ACTION_BAR_END){
				if (!mergeSpecialItems(itemstack1, slot)){
					if (!mergeItemStack(itemstack1, PLAYER_INVENTORY_START, PLAYER_ACTION_BAR_START - 1, false)){
						return ItemStack.EMPTY;
					}
				}else{
					return ItemStack.EMPTY;
				}
			}else if (!mergeItemStack(itemstack1, PLAYER_INVENTORY_START, PLAYER_ACTION_BAR_END, false)){
				return ItemStack.EMPTY;
			}

			if (itemstack1.getCount() == 0){
				slot.putStack(ItemStack.EMPTY);
			}else{
				slot.onSlotChanged();
			}

			if (itemstack1.getCount() != itemstack.getCount()){
				slot.onSlotChange(itemstack1, itemstack);
			}else{
				return ItemStack.EMPTY;
			}
		}
		return itemstack;
	}

	private boolean mergeSpecialItems(ItemStack stack, Slot slot){
		if (stack.getItem() instanceof ItemFilterFocus){
			Slot filterSlot = (Slot)inventorySlots.get(1);
			if (!filterSlot.getHasStack()){
				filterSlot.putStack(new ItemStack(stack.getItem(), 1, stack.getItemDamage()));
				filterSlot.onSlotChanged();
				stack.shrink(1);
				if (stack.getCount() == 0){
					slot.putStack(null);
					slot.onSlotChanged();
				}
				return true;
			}
		}else if (stack.getItem() instanceof ISpellFocus){
			Slot focusSlot = (Slot)inventorySlots.get(0);
			if (!focusSlot.getHasStack()){
				focusSlot.putStack(new ItemStack(stack.getItem(), 1, stack.getItemDamage()));
				focusSlot.onSlotChanged();
				stack.shrink(1);
				if (stack.getCount() == 0){
					slot.putStack(null);
					slot.onSlotChanged();
				}
				return true;
			}
		}
		return false;
	}
}
