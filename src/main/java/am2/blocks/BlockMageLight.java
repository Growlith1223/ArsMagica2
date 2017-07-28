package am2.blocks;

import java.util.List;
import java.util.Random;

import am2.ArsMagica2;
import am2.particles.AMParticle;
import am2.particles.ParticleFloatUpward;
import am2.particles.ParticleGrow;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
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

	public BlockMageLight(){
		super(Material.CIRCUITS);
		//setBlockBounds(0.35f, 0.35f, 0.35f, 0.65f, 0.65f, 0.65f);
		this.setTickRandomly(true);
		this.setLightLevel(15);
	}

	@Override
	public int tickRate(World par1World){
		return 20 - 5 * ArsMagica2.config.getGFXLevel();
	}
	
	@Override
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(worldIn, "sparkle", pos.getX() + 0.5 + (rand.nextDouble() * 0.2f - 0.1f), pos.getY() + 0.5, pos.getZ() + 0.5 + (rand.nextDouble() * 0.2f - 0.1f));
		if (particle != null){
			particle.setIgnoreMaxAge(false);
			particle.setMaxAge(10 + rand.nextInt(20));
			particle.AddParticleController(new ParticleFloatUpward(particle, 0f, -0.01f, 1, false));
			particle.AddParticleController(new ParticleGrow(particle, -0.005f, 1, false));
			//particle.setRGBColorI(color);
		}
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}
	
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
			EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ){
		ItemStack heldItem = player.getHeldItem(hand);
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

		return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
	}
	
	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		return 15;
	}

	@Override
	public int quantityDropped(Random random){
		return 0;
	}
	
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos) {
		return new AxisAlignedBB(-0.2, -0.2, -0.2, 0.2, 0.2, 0.2);
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return new AxisAlignedBB(0.35f, 0.35f, 0.35f, 0.65f, 0.65f, 0.65f);
	}
	
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
}
