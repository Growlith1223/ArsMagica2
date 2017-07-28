package am2.container;

import am2.blocks.tileentity.TileEntityArcaneDeconstructor;
import am2.container.slot.AM2Container;
import am2.container.slot.SlotGhostRune;
import am2.container.slot.SlotOneItemTypeOnly;
import am2.container.slot.SlotPickupOnly;
import am2.defs.ItemDefs;
import am2.items.ItemFocusCharge;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerArcaneDeconstructor extends AM2Container{

	private TileEntityArcaneDeconstructor deconstructor;

	private static final int PLAYER_INVENTORY_START = 16;
	private static final int PLAYER_ACTION_BAR_START = 43;
	private static final int PLAYER_ACTION_BAR_END = 52;


	public ContainerArcaneDeconstructor(InventoryPlayer inventoryplayer, TileEntityArcaneDeconstructor deconstructor){
		this.deconstructor = deconstructor;
		int i = 0;
		int j = 0;

		//working slot
		this.addSlotToContainer(new Slot(deconstructor, 0, 80, 48));

		//internal inventory
		for (i = 0; i < 9; ++i){
			this.addSlotToContainer(new SlotPickupOnly(deconstructor, i + 1, 8 + i * 18, 84));
		}

		//foci
		this.addSlotToContainer(new SlotOneItemTypeOnly(deconstructor, 10, 62, 28, ItemDefs.chargeFocus));
		this.addSlotToContainer(new SlotOneItemTypeOnly(deconstructor, 11, 80, 28, ItemDefs.chargeFocus));
		this.addSlotToContainer(new SlotOneItemTypeOnly(deconstructor, 12, 98, 28, ItemDefs.chargeFocus));

		//ghost runes
		this.addSlotToContainer(new SlotGhostRune(deconstructor, 13, 62, 8));
		this.addSlotToContainer(new SlotGhostRune(deconstructor, 14, 80, 8));
		this.addSlotToContainer(new SlotGhostRune(deconstructor, 15, 98, 8));

		//display player inventory
		for (i = 0; i < 3; i++){
			for (j = 0; j < 9; j++){
				addSlotToContainer(new Slot(inventoryplayer, j + i * 9 + 9, 8 + j * 18, 116 + i * 18));
			}
		}

		//display player action bar
		for (j = 0; j < 9; j++){
			addSlotToContainer(new Slot(inventoryplayer, j, 8 + j * 18, 174));
		}

	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer){
		return deconstructor.isUsableByPlayer(entityplayer);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int i){
		ItemStack itemstack;
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
		return ItemStack.EMPTY;
	}

	private boolean mergeSpecialItems(ItemStack stack, Slot slot){
		if (stack.getItem() instanceof ItemFocusCharge){
			for (int b = 0; b < 3; ++b){
				Slot focusSlot = (Slot)inventorySlots.get(b);
				if (focusSlot.getHasStack()) continue;

				focusSlot.putStack(new ItemStack(stack.getItem(), 1, stack.getItemDamage()));
				focusSlot.onSlotChanged();
				stack.shrink(1);
				if (stack.getCount() == 0){
					slot.putStack(ItemStack.EMPTY);
					slot.onSlotChanged();
				}
				return true;
			}
		}
		return false;
	}

}
