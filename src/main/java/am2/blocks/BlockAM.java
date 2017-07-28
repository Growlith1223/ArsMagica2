package am2.blocks;

import am2.defs.CreativeTabsDefs;
import am2.items.ItemBlockSubtypes;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockAM extends Block {

	public BlockAM(Material materialIn) {
		super(materialIn);
		setCreativeTab(CreativeTabsDefs.tabAM2Blocks);
	}

	public BlockAM(Material blockMaterialIn, MapColor blockMapColorIn) {
		super(blockMaterialIn, blockMapColorIn);
	}
	
	public BlockAM registerAndName(ResourceLocation rl) {
		this.setUnlocalizedName(rl.toString());
		GameRegistry.register(this, rl);
		GameRegistry.register(new ItemBlockSubtypes(this), rl);
		return this;
	}
	
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return new ItemStack(this, 1, getMetaFromState(state));
	}
	
	protected AxisAlignedBB boundingBox = new AxisAlignedBB(0, 0, 0, 1, 1, 1);
	
	public void setBlockBounds(float xStart, float yStart, float zStart, float xEnd, float yEnd, float zEnd) {
		boundingBox = new AxisAlignedBB(xStart, yStart, zStart, xEnd, yEnd, zEnd);
	}
	
	/*public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos) {
		return getBoundingBox(blockState, worldIn, pos);
	}*/
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return boundingBox;
	}
}
