package am2.blocks;

import java.util.List;
import java.util.Random;

import am2.ArsMagica2;
import am2.items.ItemInscriptionTable;
import am2.particles.AMParticle;
import am2.particles.ParticleFloatUpward;
import am2.particles.ParticleGrow;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

public class BlockMageLight extends BlockAMSpecialRender {
	
	public static final PropertyEnum<EnumDyeColor> COLOR = PropertyEnum.<EnumDyeColor>create("color", EnumDyeColor.class);
	private int color = 0xFFFFFF;

	public BlockMageLight(){
		super(Material.CIRCUITS);
		this.setDefaultState(blockState.getBaseState().withProperty(COLOR, EnumDyeColor.WHITE));
		//setBlockBounds(0.35f, 0.35f, 0.35f, 0.65f, 0.65f, 0.65f);
		this.setTickRandomly(true);
	}

	@Override
	public int tickRate(World par1World){
		return 20 - 5 * ArsMagica2.config.getGFXLevel();
	}
	
	private void getRGBcolor(EnumDyeColor dye) {
		switch(dye) {
			case BLACK: color = 0x191919;break;
			case RED: color = 0x993333;break;
			case GREEN: color = 0x667F33;break;
			case BROWN: color = 0x664C33;break;
			case BLUE: color = 0x334CB2;break;
			case PURPLE: color = 0x7F3FB2;break;
			case CYAN: color = 0x4C7F99;break;
			case SILVER: color = 0x999999;break;
			case GRAY: color = 0x4C4C4C;break;
			case PINK: color = 0xF27FA5;break;
			case LIME: color = 0x7FCC19;break;
			case YELLOW: color = 0xE5E533;break;
			case LIGHT_BLUE: color = 0x6699D8;break;
			case MAGENTA: color = 0xB24CD8;break;
			case ORANGE: color = 0xD87F33;break;
			default: color = 0xFFFFFF;break;
		}
	}
	
	@Override
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(worldIn, "sparkle", pos.getX() + 0.5 + (rand.nextDouble() * 0.2f - 0.1f), pos.getY() + 0.5, pos.getZ() + 0.5 + (rand.nextDouble() * 0.2f - 0.1f));
		if (particle != null){
			particle.setIgnoreMaxAge(false);
			particle.setMaxAge(10 + rand.nextInt(20));
			particle.AddParticleController(new ParticleFloatUpward(particle, 0f, -0.01f, 1, false));
			particle.AddParticleController(new ParticleGrow(particle, -0.005f, 1, false));
			getRGBcolor(stateIn.getValue(COLOR));
			particle.setRGBColorI(color);
		}
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}
	
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
			EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ){

		if (!world.isRemote && heldItem != null){

			int[] ids = OreDictionary.getOreIDs(heldItem);
			for (int id : ids){
				List<ItemStack> ores = OreDictionary.getOres(OreDictionary.getOreName(id));
				for (ItemStack stack : ores){
					if (stack.getItem() == Items.DYE){
						//world.setBlockMetadataWithNotify(pos, heldItem.getItemDamage() % 15, 2);
						break;
					}
				}
			}
		}

		return super.onBlockActivated(world, pos, state, player, hand, heldItem, side, hitX, hitY, hitZ);
	}
	
	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		return 15;
	}

	@Override
	public int quantityDropped(Random random){
		return 0;
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos) {
		// TODO Auto-generated method stub
		return new AxisAlignedBB(-0.2, -0.2, -0.2, 0.2, 0.2, 0.2);
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return new AxisAlignedBB(0.35f, 0.35f, 0.35f, 0.65f, 0.65f, 0.65f);
	}
	
	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn) {
	}
	
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return null;
	}
	
	@Override
	public BlockAM registerAndName(ResourceLocation rl) {
		this.setUnlocalizedName(rl.toString());
		GameRegistry.register(this, rl);
		return this;
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, COLOR);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(COLOR).getMetadata();
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(COLOR, EnumDyeColor.byMetadata(meta));
	}
}
