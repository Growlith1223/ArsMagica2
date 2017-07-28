package am2.items;

import am2.api.math.AMVector3;
import am2.entity.EntityBroom;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class ItemMagicBroom extends ItemArsMagica{

	public ItemMagicBroom(){
		super();
	}

	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand){
		ItemStack stack = player.getHeldItem(hand);
		if (!world.isRemote){
			RayTraceResult mop = this.rayTrace(world, player, true);
			if (mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK){
				TileEntity te = world.getTileEntity(mop.getBlockPos());
				if (te instanceof IInventory){
					EntityBroom broom = new EntityBroom(world);
					broom.setPosition(player.posX, player.posY, player.posZ);
					broom.setChestLocation(new AMVector3(mop.getBlockPos()));
					world.spawnEntity(broom);

					stack.shrink(1);

					if (stack.getCount() == 0){
						player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
					}
					return EnumActionResult.SUCCESS;
				}
			}
		}
		return EnumActionResult.PASS;
	}
}
