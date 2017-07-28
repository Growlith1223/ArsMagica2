package am2.container;

import am2.container.slot.SlotSpellCustomization;
import am2.packet.AMDataWriter;
import am2.packet.AMNetHandler;
import am2.packet.AMPacketIDs;
import am2.utils.SpellUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerSpellCustomization extends Container{

	private final InventoryPlayer inventoryPlayer;
	private int iconIndex = -1;
	private String name = "";

	public ContainerSpellCustomization(EntityPlayer player){
		this.inventoryPlayer = player.inventory;
		iconIndex = -1;

		//edit item
		addSlotToContainer(new SlotSpellCustomization(inventoryPlayer, inventoryPlayer.currentItem, 80, 30));

	}

	public void setNameAndIndex(String name, int index){
		this.name = name;
		this.iconIndex = index;

		((Slot)this.inventorySlots.get(0)).getStack().setItemDamage(this.iconIndex);
		((Slot)this.inventorySlots.get(0)).getStack().setStackDisplayName("\247b" + this.name);

		if (inventoryPlayer.player.world.isRemote){
			sendPacketToServer();
		}
	}

	@Override
	public void onContainerClosed(EntityPlayer par1EntityPlayer){
		SpellUtils.changeEnchantmentsForShapeGroup(inventoryPlayer.getCurrentItem());
		super.onContainerClosed(par1EntityPlayer);
	}

	public boolean sendPacketToServer(){
		if (!name.equals("") && iconIndex > -1){
			AMDataWriter writer = new AMDataWriter();
			writer.add(iconIndex);
			writer.add(name);
			AMNetHandler.INSTANCE.sendPacketToServer(AMPacketIDs.SPELL_CUSTOMIZE, writer.generate());
			return true;
		}
		return false;
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer){
		return true;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2){
		Slot slot = (Slot)this.inventorySlots.get(par2);

		if (slot != null && slot.getHasStack()){
			ItemStack itemstack = slot.getStack();
			return itemstack;
		}
		return ItemStack.EMPTY;
	}

	public String getInitialSuggestedName(){
		Slot slot = (Slot)this.inventorySlots.get(0);
		if (slot == null || !slot.getHasStack()) return "";
		ItemStack stack = slot.getStack();
		if (!stack.hasTagCompound()) return "";
		if (!stack.getTagCompound().hasKey("suggestedName")) return "";
		return stack.getTagCompound().getString("suggestedName");

	}

}
