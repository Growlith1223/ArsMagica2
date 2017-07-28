package am2.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import am2.ArsMagica2;
import am2.defs.ItemDefs;
import am2.defs.PotionEffectsDefs;
import am2.entity.EntityBroom;
import am2.particles.AMParticle;
import am2.particles.ParticleFadeOut;
import am2.particles.ParticleFloatUpward;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * This is my invisible utility block.  I use it for illumination (meta 0-2), as well as invisible walls (meta 3-10).
 * Meta 0: low illuminated
 * Meta 1: med illuminated
 * Meta 2: high illuminated
 * Meta 3: only collidable from +x
 * Meta 4: only collidable from -x
 * Meta 5: only collidable from +z
 * Meta 6: only collidable from -z
 * Meta 7: only collidable from +/- x
 * Meta 8: only collidable from +/- z
 * Meta 9: fully collidable
 * Meta 10: special illuminated (light sigil)
 *
 * @author Mithion
 */
public class BlockInvisibleUtility extends BlockAM{
	
	public static final PropertyEnum<EnumInvisibleType> TYPE = PropertyEnum.create("type", EnumInvisibleType.class);

	public BlockInvisibleUtility(){
		super(Material.GLASS);
		this.setBlockBounds(0, 0, 0, 0.01f, 0.01f, 0.01f);
		this.setTickRandomly(true);
		this.setDefaultState(blockState.getBaseState().withProperty(TYPE, EnumInvisibleType.LOW_ILLUMINATED));
	}

	@Override
	public int tickRate(World par1World){
		return 5;
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		if (getType(state).type == EnumType.LIGHT)
			return new AxisAlignedBB(0, 0, 0, 0, 0, 0);
		return new AxisAlignedBB(pos);
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, TYPE);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(TYPE).ordinal();
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(TYPE, EnumInvisibleType.values()[meta]);
	}
	
	@SuppressWarnings("incomplete-switch")
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entity){

		if (entity == null || world == null || entity instanceof EntityPlayer || entity instanceof EntityBroom)
			return;

		EnumInvisibleType meta = getType(state);
		double distanceThreshold = 1.1;
		double shortDistanceThreshold = 0.1;

		boolean isCollided = false;

		if (entity.width < 0.5 || entity.height < 0.5){
			distanceThreshold = 0.5f;
			shortDistanceThreshold = -0.2f;
		}

		if (meta.type == EnumType.COLLISION){
			switch (meta){
			case COLLISION_POSITIVE_X: //+x
				if (entity.posX > pos.getX() + distanceThreshold){
					collidingBoxes.add(new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.25, pos.getY() + 1.6, pos.getZ() + 1.25));
					isCollided = true;
				}
				break;
			case COLLISION_NEGATIVE_X: //-x
				if (entity.posX < pos.getX() - shortDistanceThreshold){
					collidingBoxes.add(new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.25, pos.getY() + 1.6, pos.getZ() + 1.25));
					isCollided = true;
				}
				break;
			case COLLISION_POSITIVE_Z: //+z
				if (entity.posZ > pos.getZ() + distanceThreshold){
					collidingBoxes.add(new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.25, pos.getY() + 1.6, pos.getZ() + 1.25));
					isCollided = true;
				}
				break;
			case COLLISION_NEGATIVE_Z: //-z
				if (entity.posZ < pos.getZ() - shortDistanceThreshold){
					collidingBoxes.add(new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.25, pos.getY() + 1.6, pos.getZ() + 1.25));
					isCollided = true;
				}
				break;
			case COLLISION_ALL_X: //+/- x
				if (entity.posX > pos.getX() + distanceThreshold || entity.posX < pos.getX() - shortDistanceThreshold){
					collidingBoxes.add(new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.25, pos.getY() + 1.6, pos.getZ() + 1.25));
					isCollided = true;
				}
				break;
			case COLLISION_ALL_Z: //+/- z
				if (entity.posZ > pos.getZ() + distanceThreshold || entity.posZ < pos.getZ() - shortDistanceThreshold){
					collidingBoxes.add(new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.25, pos.getY() + 1.6, pos.getZ() + 1.25));
					isCollided = true;
				}
				break;
			case COLLISION_ALL: //all
				collidingBoxes.add(new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.25, pos.getY() + 1.6, pos.getZ() + 1.25));
				isCollided = true;
				break;
			}

			if (world.isRemote && isCollided)
				spawnBlockParticles(world, pos);
		}
	}
	
	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
		if (worldIn.isRemote){
			if (getType(state).type == EnumType.COLLISION)
				spawnBlockParticles(worldIn, pos);
		}
	}
	
	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	private void spawnBlockParticles(World world, BlockPos pos){
		AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "symbols", pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
		if (particle != null){
			particle.addRandomOffset(1, 1.6, 1);
			particle.setParticleScale(0.1f);
			particle.AddParticleController(new ParticleFloatUpward(particle, 0, 0.02f, 1, false));
			particle.AddParticleController(new ParticleFadeOut(particle, 1, false).setFadeSpeed(0.05f));
			particle.setMaxAge(20);
			if (world.rand.nextBoolean()){
				particle.setRGBColorI(0x481bc8);
			}else{
				particle.setRGBColorI(0x891bc8);
			}
		}
	}
	
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return null;
	}

	@Override
	public boolean canDropFromExplosion(Explosion par1Explosion){
		return false;
	}
	
	@Override
	public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
		return false;
	}
	
	@Override
	public boolean isAir(IBlockState state, IBlockAccess world, BlockPos pos) {
		return true;
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}
	
	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		switch (getType(state)){
		case LOW_ILLUMINATED:
			return 8;
		case MED_ILLUMINATED:
			return 12;
		case HIGH_ILLUMINATED:
		case SPECIAL_ILLUMINATED:
			return 15;
		default:
			return 0;
		}
	}
	
	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		if (!worldIn.isRemote){
			worldIn.scheduleBlockUpdate(pos, this, this.tickRate(worldIn), 0);
		}
	}

	@Override
	public boolean isOpaqueCube(IBlockState state){
		return false;
	}
	
	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if (getType(state).type == EnumType.LIGHT){
			float r = 1.5f;
			List<EntityLivingBase> ents = world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(pos).expandXyz(r));
			boolean buffNearby = false;
			for (EntityLivingBase ent : ents){
				buffNearby |= ent.isPotionActive(PotionEffectsDefs.illumination) ||
						(ent instanceof EntityPlayer &&
								((EntityPlayer)ent).inventory.getCurrentItem() != null &&
								((EntityPlayer)ent).inventory.getCurrentItem().getItem() == ItemDefs.wardingCandle);
			}
			if (!buffNearby && world.getBlockState(pos).getBlock() == this)
				world.setBlockToAir(pos);

			world.scheduleBlockUpdate(pos, this, this.tickRate(world), 0);
		}
	}
	
	@Override
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		if (worldIn.rand.nextInt(10) < 3 && getType(stateIn).type == EnumType.COLLISION){
			List<Entity> ents = worldIn.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos).expandXyz(0.2F));
			if (ents.size() > 0){
				spawnBlockParticles(worldIn, pos);
			}
		}
	}
		
	@Override
	public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
		return getType(world.getBlockState(pos)).type == EnumType.COLLISION ? 100f : 0f;
	}
	
	public static EnumInvisibleType getType(IBlockState state) {
		return state.getValue(TYPE);
	}
	
	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		return new ArrayList<>();
	}
	
	public static enum EnumInvisibleType implements IStringSerializable {
		LOW_ILLUMINATED(EnumType.LIGHT),
		MED_ILLUMINATED(EnumType.LIGHT),
		HIGH_ILLUMINATED(EnumType.LIGHT),
		COLLISION_POSITIVE_X(EnumType.COLLISION),
		COLLISION_NEGATIVE_X(EnumType.COLLISION),
		COLLISION_POSITIVE_Z(EnumType.COLLISION),
		COLLISION_NEGATIVE_Z(EnumType.COLLISION),
		COLLISION_ALL_X(EnumType.COLLISION),
		COLLISION_ALL_Z(EnumType.COLLISION),
		COLLISION_ALL(EnumType.COLLISION),
		SPECIAL_ILLUMINATED(EnumType.LIGHT);
		
		private final EnumType type;
		
		private EnumInvisibleType(EnumType type) {
			this.type = type;
		}
		
		@Override
		public String getName() {
			return this.name().toLowerCase();
		}
		
	}
	
	@Override
	public BlockAM registerAndName(ResourceLocation rl) {
		this.setUnlocalizedName(rl.toString());
		GameRegistry.register(this, rl);
		return this;
	}
	
	private enum EnumType {
		LIGHT,
		COLLISION
	}
}
