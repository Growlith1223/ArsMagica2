package am2.blocks;

import am2.defs.BlockDefs;
import am2.defs.CreativeTabsDefs;
import am2.items.ItemBlockSubtypes;
import am2.world.AM2FlowerGen;
import am2.world.WitchwoodTreeHuge;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.List;
import java.util.Random;

public class BlockWitchwoodSapling extends BlockBush implements IGrowable{
	
	public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 15);
	
	public BlockWitchwoodSapling(){
		super();
		setTickRandomly(true);
		setCreativeTab(CreativeTabsDefs.tabAM2Blocks);
		setDefaultState(blockState.getBaseState().withProperty(AGE, 0));
		setSoundType(SoundType.PLANT);
	}
	
	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand){
		if (!worldIn.isRemote){
			super.updateTick(worldIn, pos, state, rand);

			int nearbyEssence = countNearbyEssencePools(worldIn, pos, rand);
			updateOrGrowTree(worldIn, pos, rand, nearbyEssence);
		}
	}

	private void updateOrGrowTree(World world, BlockPos pos, Random rand, int numNearbyPools){
		if(!(world.getBlockState(pos).getBlock() instanceof BlockWitchwoodSapling))
			return;
		if (rand.nextInt(7) == 0){
			int meta = world.getBlockState(pos).getValue(AGE) + numNearbyPools;
			world.setBlockState(pos, world.getBlockState(pos).withProperty(AGE, Math.min(meta, 15)));
			if (canGrow(world, pos, world.getBlockState(pos), world.isRemote))
				this.grow(world, rand, pos, world.getBlockState(pos));
		}
	}

	private int countNearbyEssencePools(World world, BlockPos pos, Random rand){
		int essenceNearby = 0;

		for (int i = -1; i <= 1; i++){
			for (int j = -1; j <= 1; j++){
				IBlockState block = world.getBlockState(pos.add(i, -1, j));
				if (block == BlockDefs.liquid_essence.getBlock().getDefaultState())
					essenceNearby++;
			}
		}

		return essenceNearby;
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list){
		list.add(new ItemStack(this));
	}

	@Override
	public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
		return state.getValue(AGE) == 15;
	}

	@Override
	public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
		return false;
	}

	@Override
	public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
		WitchwoodTreeHuge generator = new WitchwoodTreeHuge(true);

		worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 4);
		if (!generator.generate(worldIn, rand, pos))
			worldIn.setBlockState(pos, getDefaultState().withProperty(AGE, 15));
		else
			new AM2FlowerGen(BlockDefs.aum).generate(worldIn, rand, pos);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(AGE);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(AGE, meta);
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, AGE);
	}
	
	public BlockWitchwoodSapling registerAndName(ResourceLocation rl) {
		this.setUnlocalizedName(rl.toString());
		GameRegistry.register(this, rl);
		GameRegistry.register(new ItemBlockSubtypes(this), rl);
		return this;
	}
}
