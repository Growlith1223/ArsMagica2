package am2.blocks;

import java.util.Random;

import am2.defs.ItemDefs;
import am2.items.ItemBlockOre;
import am2.items.ItemOre;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockArsMagicaOre extends BlockAM {
	
	public static final PropertyEnum<EnumOreType> ORE_TYPE = PropertyEnum.create("ore_type", EnumOreType.class);
	
	public BlockArsMagicaOre() {
		super(Material.ROCK);
		setDefaultState(blockState.getBaseState().withProperty(ORE_TYPE, EnumOreType.VINTEUM));
		setHardness(3.0F);
		setResistance(5.0F);
		setHarvestLevel("pickaxe", 2);
	}
	
	@Override
	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[]{BlockArsMagicaOre.ORE_TYPE});
	}
	
	@Override
	public int quantityDropped(IBlockState state, int fortune, Random random) {
		if (state.getValue(ORE_TYPE) == EnumOreType.VINTEUM) return 1;
		int fortuneBonus = fortune > 0 ? random.nextInt(fortune) : 0;
		boolean isChimerite = state.getValue(ORE_TYPE) == EnumOreType.CHIMERITE;
		int drop = 1 + (isChimerite ? random.nextInt(4) : random.nextInt(1));
		return drop + (isChimerite ? 1 : 2) * fortuneBonus;
	}
	
	/*@Override
	public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
		for (int i = 0; i < EnumOreType.values().length; i++) {
			list.add(new ItemStack(this, 1, i));
		}
	}*/
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(ORE_TYPE).ordinal();
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return state.getValue(ORE_TYPE) == EnumOreType.VINTEUM ? Item.getItemFromBlock(this) : ItemDefs.itemOre;
	}
	
	@Override
	public int damageDropped(IBlockState state) {
		EnumOreType type = state.getValue(ORE_TYPE);
		if (type == EnumOreType.VINTEUM)
			return EnumOreType.VINTEUM.ordinal();
		if (type == EnumOreType.CHIMERITE)
			return ItemOre.META_CHIMERITE;
		if (type == EnumOreType.BLUETOPAZ)
			return ItemOre.META_BLUE_TOPAZ;
		if (type == EnumOreType.MOONSTONE)
			return ItemOre.META_MOONSTONE;
		if (type == EnumOreType.SUNSTONE)
			return ItemOre.META_SUNSTONE;
		return super.damageDropped(state);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(ORE_TYPE, EnumOreType.values()[MathHelper.clamp(meta, 0, EnumOreType.values().length - 1)]);
	}
	
	@Override
	public BlockAM registerAndName(ResourceLocation rl) {
		this.setUnlocalizedName(rl.getResourcePath());
		GameRegistry.register(this, rl);
		GameRegistry.register(new ItemBlockOre(this), rl);
		return this;
	}
	
	public static enum EnumOreType implements IStringSerializable{
		VINTEUM,
		CHIMERITE,
		BLUETOPAZ,
		MOONSTONE,
		SUNSTONE;

		@Override
		public String getName() {
			return name().toLowerCase();
		}		
	}

}
