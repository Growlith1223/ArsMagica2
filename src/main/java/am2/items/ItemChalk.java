package am2.items;

import am2.blocks.BlockWizardsChalk;
import am2.defs.BlockDefs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemChalk extends ItemArsMagica{

	public ItemChalk(){
		setMaxDamage(50);
		setMaxStackSize(1);
	}
	
	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
		if (side != EnumFacing.UP || !canBeUsed(world, pos.up())){
			return EnumActionResult.FAIL;
		}
		if (!world.isRemote){
			world.setBlockState(pos.up(), BlockDefs.wizardChalk.getDefaultState().withProperty(BlockWizardsChalk.VARIANT, world.rand.nextInt(16)));
			player.getHeldItem(hand).damageItem(1, player);
		}
		return EnumActionResult.PASS;
	}

	public boolean canBeUsed(World world, BlockPos pos){
		if (world.getBlockState(pos.down()).getBlock() == BlockDefs.wizardChalk){
			return false;
		}
		if (!world.isAirBlock(pos)){
			return false;
		}
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldRotateAroundWhenRendering(){
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isFull3D(){
		return true;
	}
}