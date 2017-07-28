package am2.items;

import am2.defs.BlockDefs;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;

public class ItemWakebloom extends ItemBlock{

	public ItemWakebloom(Block block) {
		super(block);
	}
	
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand){
		ItemStack itemStackIn = playerIn.getHeldItem(hand);
		RayTraceResult mop = this.rayTrace(worldIn, playerIn, true);

		if (mop == null){
			return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStackIn);
		}else{
			if (mop.typeOfHit == Type.BLOCK){

				if (!worldIn.canMineBlockBody(playerIn, mop.getBlockPos())){
					return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStackIn);
				}

				if (!playerIn.canPlayerEdit(mop.getBlockPos(), mop.sideHit, itemStackIn)){
					return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStackIn);
				}

				if (worldIn.getBlockState(mop.getBlockPos()) == Blocks.FLOWING_WATER.getDefaultState() || worldIn.getBlockState(mop.getBlockPos()) == Blocks.WATER.getDefaultState()){
					worldIn.setBlockState(mop.getBlockPos().up(), BlockDefs.wakebloom.getDefaultState());

					if (!playerIn.capabilities.isCreativeMode){
						itemStackIn.shrink(1);
					}
				}
			}

			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
		}
	}

}
