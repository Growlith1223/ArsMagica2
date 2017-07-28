package am2.container.slot;

import am2.blocks.tileentity.TileEntityInscriptionTable;
import am2.defs.ItemDefs;
import am2.items.ItemSpellBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

@SuppressWarnings("deprecation")
public class SlotInscriptionTable extends Slot{

	public SlotInscriptionTable(TileEntityInscriptionTable par1iInventory, int par2, int par3, int par4){
		super(par1iInventory, par2, par3, par4);
	}

	@Override
	public boolean isItemValid(ItemStack par1ItemStack){
		if (par1ItemStack == null || par1ItemStack.getItem() == null){
			return false;
		}
		if (par1ItemStack.getItem() == Items.WRITTEN_BOOK && (par1ItemStack.getTagCompound() == null || !par1ItemStack.getTagCompound().getBoolean("spellFinalized")))
			return true;
		else if (par1ItemStack.getItem() == Items.WRITABLE_BOOK)
			return true;
		else if (par1ItemStack.getItem() == ItemDefs.spell)
			return true;
		return false;
	}

	@Override
	public ItemStack onTake(EntityPlayer par1EntityPlayer, ItemStack par2ItemStack){
		if (par2ItemStack.getItem() == Items.WRITTEN_BOOK)
			return ((TileEntityInscriptionTable)this.inventory).writeRecipeAndDataToBook(par2ItemStack, par1EntityPlayer, "Spell Recipe");
		else
			((TileEntityInscriptionTable)this.inventory).clearCurrentRecipe();
		return super.onTake(par1EntityPlayer, par2ItemStack);
	}

	@Override
	public void onSlotChanged(){
		if (this.getStack() != null){
			Class<? extends Item> clazz = this.getStack().getItem().getClass();
			if (ItemSpellBase.class.isAssignableFrom(clazz)){
				((TileEntityInscriptionTable)this.inventory).reverseEngineerSpell(this.getStack());
			}
		}
		super.onSlotChanged();
	}

	@Override
	public void putStack(ItemStack stack){
		if (stack != null && stack.getItem() == Items.WRITABLE_BOOK){
			stack = new ItemStack(Items.WRITTEN_BOOK);
			stack.setCount(1);
			stack.setStackDisplayName(I18n.translateToLocal("am2.tooltip.unfinishedSpellRecipe"));
		}
		super.putStack(stack);
	}
}
