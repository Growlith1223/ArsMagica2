package am2.blocks;

import java.util.ArrayList;
import java.util.List;

import am2.defs.BlockDefs;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;

public abstract class BlockWitchwoodSlabs extends BlockSlab{
	
	public static final PropertyEnum<EnumSlabType> SLAB_TYPE = PropertyEnum.create("slab_type", EnumSlabType.class);

	public BlockWitchwoodSlabs(){
		super(Material.WOOD);
		this.setHardness(2.0f);
		this.setResistance(2.0f);
		this.setHarvestLevel("axe", 2);
		setDefaultState(isDouble() ? blockState.getBaseState().withProperty(SLAB_TYPE, EnumSlabType.WITCHWOOD) : blockState.getBaseState().withProperty(SLAB_TYPE, EnumSlabType.WITCHWOOD).withProperty(HALF, EnumBlockHalf.BOTTOM));
	}
	
	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
		drops.add(new ItemStack(BlockDefs.witchwoodSingleSlab, isDouble() ? 2 : 1, state.getValue(SLAB_TYPE).ordinal()));
		return drops;
	}
	
	@Override
	public String getUnlocalizedName(int meta) {
		return "tile.arsmagica2:witchwood_slab";
	}

	@Override
	public abstract boolean isDouble();

	@Override
	public IProperty<?> getVariantProperty() {
		return SLAB_TYPE;
	}

	@Override
	public Comparable<?> getTypeForItem(ItemStack stack) {
		return EnumSlabType.byMeta(stack.getItemDamage());
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return isDouble() ? new BlockStateContainer(this, SLAB_TYPE) : new BlockStateContainer(this, SLAB_TYPE, HALF);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		int meta = state.getValue(SLAB_TYPE).ordinal();
		if (!isDouble() && state.getValue(HALF) == EnumBlockHalf.TOP)
			meta |= 0x8;
		return meta;
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumSlabType slabType = EnumSlabType.byMeta(meta);
		IBlockState state = getDefaultState().withProperty(SLAB_TYPE, slabType);
		if (!isDouble())
			state = state.withProperty(HALF, (meta & 0x8) == 0x8 ? EnumBlockHalf.TOP : EnumBlockHalf.BOTTOM);
		return state;
	}
	
	public static enum EnumSlabType implements IStringSerializable {
		WITCHWOOD;

		@Override
		public String getName() {
			return name().toLowerCase();
		}
		
		public static EnumSlabType byMeta(int meta) {
			meta &= 0x7;
			meta = MathHelper.clamp(meta, 0, values().length - 1);
			return values()[meta];
		}
	}
}
