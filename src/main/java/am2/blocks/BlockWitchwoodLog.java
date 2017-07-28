package am2.blocks;

import java.util.List;
import java.util.Random;

import am2.defs.CreativeTabsDefs;
import am2.items.ItemBlockSubtypes;
import net.minecraft.block.BlockLog;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockWitchwoodLog extends BlockLog{

	public BlockWitchwoodLog(){
		super();
		setHardness(3.0f);
		setResistance(3.0f);
		setHarvestLevel("axe", 2);
		setCreativeTab(CreativeTabsDefs.tabAM2Blocks);
		setDefaultState(blockState.getBaseState().withProperty(LOG_AXIS, EnumAxis.Y));
	}

	/**
	 * Returns the quantity of items to drop on block destruction.
	 */
	public int quantityDropped(Random par1Random){
		return 1;
	}

	/**
	 * returns a number between 0 and 3
	 */
	public static int limitToValidMetadata(int par0){
		return par0 & 3;
	}
	
	@Override
	public int damageDropped(IBlockState state) {
		return 0;
	}
	
	@Override
	public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List){
		par3List.add(new ItemStack(this));
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, LOG_AXIS);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(LOG_AXIS).ordinal();
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(LOG_AXIS, EnumAxis.values()[MathHelper.clamp(meta, 0, 3)]);
	}
	
	public BlockWitchwoodLog registerAndName(ResourceLocation rl) {
		this.setUnlocalizedName(rl.toString());
		GameRegistry.register(this, rl);
		GameRegistry.register(new ItemBlockSubtypes(this), rl);
		return this;
	}
}
