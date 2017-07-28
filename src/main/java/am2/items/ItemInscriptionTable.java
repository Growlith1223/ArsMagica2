package am2.items;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemInscriptionTable extends ItemBlock {

	public ItemInscriptionTable(Block block) {
		super(block);
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!block.isReplaceable(worldIn, pos))
        {
            pos = pos.offset(facing);
        }
		BlockPos placePos = pos.offset(playerIn.getHorizontalFacing().rotateY());
		if (worldIn.isAirBlock(placePos) || worldIn.getBlockState(placePos).getBlock().isReplaceable(worldIn, placePos))
			return super.onItemUse(playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ);
		return EnumActionResult.FAIL;
	}

}
