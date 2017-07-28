package am2.blocks;

import java.util.List;
import java.util.Random;

import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BlockGroundRune extends BlockAMContainer{
	
	protected BlockGroundRune(){
		super(Material.WOOD);
		setTickRandomly(true);
		float f = 0.0625F;
		setBlockBounds(f, 0.0F, f, 1.0F - f, 0.03125F, 1.0F - f);
		setHardness(10);
		setResistance(0.5f);
		//setBlockBounds(f, 0.0F, f, 1.0F - f, 0.0625F, 1.0F - f);
	}

	protected boolean triggerOnCaster(){
		return false;
	}

	@Override
	public abstract TileEntity createNewTileEntity(World var1, int i);

	@Override
	public int quantityDropped(Random random){
		return 0;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		return side == EnumFacing.DOWN || side == EnumFacing.UP;
	}
	
	@Override
	public int tickRate(World world){
		return 20;
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos) {
		return NULL_AABB;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos){
		return worldIn.getBlockState(pos.down()).isNormalCube() && worldIn.isAirBlock(pos);
	}

	public boolean placeAt(World world, BlockPos pos, IBlockState state){
		if (!canPlaceBlockAt(world, pos)) return false;
		world.setBlockState(pos, state);
		return true;
	}
	
	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand){
		if (worldIn.isRemote){
			return;
		}
		setStateIfMobInteractsWithPlate(worldIn, pos);
	}
	
	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn){
		if (worldIn.isRemote){
			return;
		}
		setStateIfMobInteractsWithPlate(worldIn, pos);
		return;
	}

	private void setStateIfMobInteractsWithPlate(World world, BlockPos pos){
		float f = 0.125F;
		List<Entity> list = null;
		list = world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB((float)pos.getX() - f, pos.getY(), (float)pos.getZ() - f, (float)(pos.getX() + 1 + f), (double)pos.getY() + 2D, (float)(pos.getZ() + 1 + f)));
		if (list.size() > 0){
			if (ActivateRune(world, list, pos)){
				IBlockState state = world.getBlockState(pos);
				if (!isPermanent(world, pos, state)){
					int numTriggers = getNumTriggers(world, pos, state);
					if (--numTriggers <= 0)
						world.setBlockToAir(pos);
					else
						setNumTriggers(world, pos, state, numTriggers);
				}

			}
		}
	}

	protected abstract boolean ActivateRune(World world, List<Entity> entitiesInRange, BlockPos pos);
	
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state){
		worldIn.updateObservingBlocksAt(pos, state.getBlock());
		worldIn.updateObservingBlocksAt(pos, worldIn.getBlockState(pos.down()).getBlock());
		super.breakBlock(worldIn, pos, state);
	}
	
	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state){
		return EnumPushReaction.DESTROY;
	}

	protected abstract boolean isPermanent(World world, BlockPos pos, IBlockState state);

	protected abstract int getNumTriggers(World world, BlockPos pos, IBlockState state);

	public abstract void setNumTriggers(World world, BlockPos pos, IBlockState state, int numTriggers);
	
	@Override
	public boolean isOpaqueCube(IBlockState state){
		return false;
	}
	
	@Override
	public int getLightOpacity(IBlockState state) {
		return 0;
	}
	
	@Override
	public boolean isTranslucent(IBlockState state) {
		return true;
	}
	
	@Override
	public BlockAMContainer registerAndName(ResourceLocation rl) {
		this.setUnlocalizedName(rl.toString());
		GameRegistry.register(this, rl);
		return this;
	}
}
