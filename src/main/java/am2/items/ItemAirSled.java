package am2.items;

import java.util.List;

import am2.defs.ItemDefs;
import am2.entity.EntityAirSled;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

@SuppressWarnings("deprecation")
public class ItemAirSled extends ItemArsMagica{

	public ItemAirSled(){
		super();
		setMaxStackSize(1);
	}

	@Override
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List<String> par3List, boolean par4){
		par3List.add(I18n.translateToLocal("am2.tooltip.air_sled"));
		super.addInformation(par1ItemStack, par2EntityPlayer, par3List, par4);
	}

	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand){
		if (!world.isRemote){
			EntityAirSled sled = new EntityAirSled(world);
			sled.setPosition(pos.getX() + hitX, pos.getY() + hitY + 0.5, pos.getZ() + hitZ);
			world.spawnEntity(sled);
			player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
			return EnumActionResult.PASS;
		}
		return EnumActionResult.PASS;
	}

	@Override
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List){
		par3List.add(ItemDefs.airSledEnchanted.copy());
	}

}
