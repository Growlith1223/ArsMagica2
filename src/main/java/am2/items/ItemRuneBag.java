package am2.items;

import am2.ArsMagica2;
import am2.container.InventoryRuneBag;
import am2.defs.IDDefs;
import am2.defs.ItemDefs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;

public class ItemRuneBag extends ItemArsMagica{

	public ItemRuneBag(){
		super();
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer entityplayer, EnumHand hand){
		if (entityplayer.isSneaking()){
			FMLNetworkHandler.openGui(entityplayer, ArsMagica2.instance, IDDefs.GUI_RUNE_BAG, world, (int)entityplayer.posX, (int)entityplayer.posY, (int)entityplayer.posZ);
		}
		return super.onItemRightClick(world, entityplayer, hand);
	}

	private ItemStack[] getMyInventory(ItemStack itemStack){
		return ReadFromStackTagCompound(itemStack);
	}

	public void UpdateStackTagCompound(ItemStack itemStack, ItemStack[] values){
		if (itemStack.getTagCompound() == null){
			itemStack.setTagCompound(new NBTTagCompound());
		}
		for (int i = 0; i < values.length; ++i){
			ItemStack stack = values[i];
			if (stack == null){
				itemStack.getTagCompound().removeTag("runebagmeta" + i);
				continue;
			}else{
				itemStack.getTagCompound().setInteger("runebagmeta" + i, stack.getItemDamage());
			}
		}
	}

	@Override
	public boolean getShareTag(){
		return true;
	}

	public void UpdateStackTagCompound(ItemStack itemStack, InventoryRuneBag inventory){
		if (itemStack.getTagCompound() == null){
			itemStack.setTagCompound(new NBTTagCompound());
		}
		for (int i = 0; i < inventory.getSizeInventory(); ++i){
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack == null){
				continue;
			}else{
				itemStack.getTagCompound().setInteger("runebagmeta" + i, stack.getItemDamage());
			}
		}
	}

	public ItemStack[] ReadFromStackTagCompound(ItemStack itemStack){
		if (itemStack.getTagCompound() == null){
			return new ItemStack[InventoryRuneBag.inventorySize];
		}
		ItemStack[] items = new ItemStack[InventoryRuneBag.inventorySize];
		for (int i = 0; i < items.length; ++i){
			if (!itemStack.getTagCompound().hasKey("runebagmeta" + i) || itemStack.getTagCompound().getInteger("runebagmeta" + i) == -1){
				items[i] = null;
				continue;
			}
			int meta = 0;
			meta = itemStack.getTagCompound().getInteger("runebagmeta" + i);
			items[i] = new ItemStack(ItemDefs.rune, 1, meta);
		}
		return items;
	}

	public InventoryRuneBag ConvertToInventory(ItemStack runeBagStack){
		InventoryRuneBag irb = new InventoryRuneBag();
		irb.SetInventoryContents(getMyInventory(runeBagStack));
		return irb;
	}
}
