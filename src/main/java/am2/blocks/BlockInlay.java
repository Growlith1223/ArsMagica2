package am2.blocks;

import java.util.List;
import java.util.Random;

import am2.ArsMagica2;
import am2.api.DamageSources;
import am2.api.math.AMVector3;
import am2.blocks.BlockArsMagicaBlock.EnumBlockType;
import am2.bosses.BossSpawnHelper;
import am2.defs.BlockDefs;
import am2.defs.CreativeTabsDefs;
import am2.items.ItemBlockSubtypes;
import am2.particles.AMParticle;
import am2.particles.ParticleFloatUpward;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockInlay extends BlockRailBase {

	public static final PropertyEnum<EnumRailDirection> SHAPE = PropertyEnum.create("shape", EnumRailDirection.class);
	
	private final int material;

	public static final int TYPE_REDSTONE = 0;
	public static final int TYPE_IRON = 1;
	public static final int TYPE_GOLD = 2;
	
	private static final String minecartGoldInlayKey = "AM2GoldInlayTimer";
	
	public BlockInlay(int type) {
		super(false);
		setTickRandomly(true);
		setCreativeTab(CreativeTabsDefs.tabAM2Blocks);
		this.setSoundType(SoundType.METAL);
		this.setHarvestLevel("pickaxe", -1);
		this.setHardness(1);
		this.setResistance(1);
		this.setLightOpacity(0);
		this.setDefaultState(this.blockState.getBaseState().withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_SOUTH));
		material = type;
	}

	protected void updateState(IBlockState state, World world, BlockPos pos, Block block) {
		if (block.getDefaultState().canProvidePower() && countAdjacentRails(world, pos) == 3) {
			this.updateDir(world, pos, state, false);
		}
	}

	protected int countAdjacentRails(World world, BlockPos pos) {
		int i = 0;

		for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
			if (BlockRailBase.isRailBlock(world, pos.offset(enumfacing))
					|| BlockRailBase.isRailBlock(world, pos.offset(enumfacing).up())
					|| BlockRailBase.isRailBlock(world, pos.offset(enumfacing).down())) {
				i++;
			}
		}

		return i;
	}

	public IProperty<BlockRailBase.EnumRailDirection> getShapeProperty() {
		return SHAPE;
	}
	
	@Override
	public void randomDisplayTick(IBlockState stateIn, World world, BlockPos pos, Random rand) {
		if (world.isRemote && world.getBlockState(pos.down()).getBlock().isAir(world.getBlockState(pos.down()), world, pos.down()) && ArsMagica2.config.FullGFX() && rand.nextInt(10) < 8){
			AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, ArsMagica2.config.FullGFX() ? "radiant" : "sparkle2", pos.getX() + rand.nextFloat(), pos.getY(), pos.getZ() + rand.nextFloat());
			if (particle != null){
				particle.setMaxAge(20);
				particle.setParticleScale(ArsMagica2.config.FullGFX() ? 0.015f : 0.15f);
				particle.AddParticleController(new ParticleFloatUpward(particle, 0.01f, -0.025f, 1, false));
				if (this == BlockDefs.redstoneInlay)
					particle.setRGBColorF(1.0f, 0.4f, 0.4f);
				else if (this == BlockDefs.ironInlay)
					particle.setRGBColorF(1.0f, 1.0f, 1.0f);
				else if (this == BlockDefs.goldInlay)
					particle.setRGBColorF(1.0f, 1.0f, 0.2f);
			}
		}
	}
	
	@Override
	public boolean canMakeSlopes(IBlockAccess world, BlockPos pos) {
		return false;
	}
	
	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		if (!worldIn.isRemote)
			this.updateState(state, worldIn, pos, blockIn);
	}
	
	
	@Override
	public int tickRate(World par1World){
		return 60 + par1World.rand.nextInt(80);
	}
	
	@Override
	public float getRailMaxSpeed(World world, EntityMinecart cart, BlockPos pos) {
		if (this.material == TYPE_REDSTONE)
			return 0.7f;
		return 0.4F;
	}
	
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(SHAPE, BlockRailBase.EnumRailDirection.byMetadata(meta));
	}

	public int getMetaFromState(IBlockState state) {
		return ((BlockRailBase.EnumRailDirection) state.getValue(SHAPE)).getMetadata();
	}

	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { SHAPE });
	}
	
	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		if (!worldIn.isRemote){
			if (state.getValue(SHAPE) == EnumRailDirection.SOUTH_EAST)
				checkPattern(worldIn, pos);
			worldIn.scheduleBlockUpdate(pos, this, tickRate(worldIn), 0);
		}
	}

	private void checkPattern(World world, BlockPos pos){
		boolean allGood = true;
		for (int i = 0; i <= 2; ++i){
			for (int j = 0; j <= 2; ++j){
				if (i == 1 && j == 1) continue;
				allGood &= world.getBlockState(pos.add(i, 0, j)).getBlock() == this;
			}
		}

		if (allGood){
			if (!world.isRemote){
				if (!checkForIceEffigy(world, pos))
					checkForLightningEffigy(world, pos);
			}
		}
	}

	private boolean checkForIceEffigy(World world, BlockPos pos){
		if (world.getBlockState(pos.add(1, 0, 1)).getBlock() == BlockDefs.blocks.getDefaultState().withProperty(BlockArsMagicaBlock.BLOCK_TYPE, EnumBlockType.BLUETOPAZ).getBlock()) {
			if (world.getBlockState(pos.add(1, 1, 1)).getBlock() == BlockDefs.blocks.getDefaultState().withProperty(BlockArsMagicaBlock.BLOCK_TYPE, EnumBlockType.BLUETOPAZ).getBlock()){
				if (world.getBlockState(pos.add(1, 2, 1)).getBlock() == BlockDefs.iceEffigy){
					IBlockState state = world.getBlockState(pos.add(1, 2, 1));
					int iceMeta = world.getBlockState(pos.add(1, 2, 1)).getValue(BlockEffigy.PROGRESS);
					if (iceMeta > 0){
						ArsMagica2.proxy.particleManager.RibbonFromPointToPoint(world, pos.getX() + 1.5, pos.getY() + 2, pos.getZ() + 1.5, pos.getX() + 2, pos.getY() + 3, pos.getZ() + 2);
					}
					if (iceMeta >= 3){
						BossSpawnHelper.instance.onIceEffigyBuilt(world, pos.add(1, 0, 1));
					}else{
						List<EntitySnowman> snowmen = world.getEntitiesWithinAABB(EntitySnowman.class, new AxisAlignedBB(pos.getX() - 9, pos.getY() - 2, pos.getZ() - 9, pos.getX() + 10, pos.getY() + 4, pos.getZ() + 10));
						if (snowmen.size() > 0){
							snowmen.get(0).attackEntityFrom(DamageSources.unsummon, 5000);
							iceMeta++;
							world.setBlockState(pos.add(1, 2, 1), state.withProperty(BlockEffigy.PROGRESS, iceMeta));
							ArsMagica2.proxy.particleManager.BeamFromEntityToPoint(world, snowmen.get(0), pos.getX() + 1.5, pos.getY() + 2.5, pos.getZ() + 1.5);
						}
					}
					return true;
				}
			}
		}
		return false;
	}

	private boolean checkForLightningEffigy(World world, BlockPos pos){

		if (!world.isRaining())
			return false;

		if (world.getBlockState(pos.add(1, 0, 1)).getBlock() == BlockDefs.manaBattery){
			if (world.getBlockState(pos.add(1, 1, 1)).getBlock() == Blocks.IRON_BARS){
				if (world.getBlockState(pos.add(1, 2, 1)).getBlock() == BlockDefs.lightningEffigy){
					IBlockState state = world.getBlockState(pos.add(1, 2, 1));
					int fenceMeta = world.getBlockState(pos.add(1, 2, 1)).getValue(BlockEffigy.PROGRESS);
					if (fenceMeta > 0){
						ArsMagica2.proxy.particleManager.RibbonFromPointToPoint(world, pos.getX() + 1.5, pos.getY() + 2, pos.getZ() + 1.5, pos.getX() + 2, pos.getY() + 3, pos.getZ() + 2);
					}
					fenceMeta++;
					world.setBlockState(pos.add(1, 2, 1), state.withProperty(BlockEffigy.PROGRESS, fenceMeta));
					if (!world.isRemote){
						for (int i = 0; i < 5; ++i)
							ArsMagica2.proxy.particleManager.BoltFromPointToPoint(world, pos.getX() + 1.5, pos.getY() + 30, pos.getZ() + 1.5, pos.getX() + 1.5, pos.getY() + 2, pos.getZ() + 1.5);
						for (int i = 0; i < fenceMeta; ++i)
							world.playSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.AMBIENT, 1.0f, world.rand.nextFloat() + 0.5f, false);
					}
					if (fenceMeta >= 3){
						BossSpawnHelper.instance.onLightningEffigyBuilt(world, pos.add(1, 0, 1));
					}
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public void onMinecartPass(World world, EntityMinecart cart, BlockPos pos) {
		if (cart == null) return;

		long millisSinceLastTeleport = cart.getEntityData().getLong(minecartGoldInlayKey);

		EnumRailDirection meta = world.getBlockState(pos).getValue(SHAPE);
		if (this.material == TYPE_REDSTONE){
			float limit = 2f;
			if (meta == EnumRailDirection.EAST_WEST){
				if (cart.motionX > 0 && cart.motionX < limit)
					cart.motionX *= 1.1f;
				else if (cart.motionX < 0 && cart.motionX > -limit)
					cart.motionX *= 1.1f;
			}else if (meta == EnumRailDirection.NORTH_SOUTH){
				if (cart.motionZ > 0 && cart.motionZ < limit)
					cart.motionZ *= 1.1f;
				else if (cart.motionZ < 0 && cart.motionZ > -limit)
					cart.motionZ *= 1.1f;
			}
		}else if (this.material == TYPE_IRON){
			if (meta == EnumRailDirection.EAST_WEST){
				cart.motionX = -cart.motionX * 0.5f;
				if (cart.motionX < 0)
					cart.setPosition(pos.getX() - 0.02, cart.posY, cart.posZ);
				else if (cart.motionX > 0)
					cart.setPosition(pos.getX() + 1.02, cart.posY, cart.posZ);
			}else if (meta == EnumRailDirection.NORTH_SOUTH){
				cart.motionZ = -cart.motionZ * 0.5f;
				if (cart.motionZ < 0)
					cart.setPosition(cart.posX, cart.posY, pos.getZ() - 0.02);
				else if (cart.motionZ > 0)
					cart.setPosition(cart.posX, cart.posY, pos.getZ() + 1.02);
			}
		}else if (this.material == TYPE_GOLD){
			AMVector3 teleportLocation = null;
			if (meta == EnumRailDirection.EAST_WEST){
				if (cart.motionX > 0){
					for (int i = 1; i <= 8; ++i){
						if (world.getBlockState(pos.add(i, 0, 0)).getBlock() == BlockDefs.goldInlay){
							teleportLocation = new AMVector3(pos.add(i, 0, 0));
							break;
						}
					}
				}else if (cart.motionX < 0){
					for (int i = 1; i <= 8; ++i){
						if (world.getBlockState(pos.add(-i, 0, 0)).getBlock() == BlockDefs.goldInlay){
							teleportLocation = new AMVector3(pos.add(-i, 0, 0));
							break;
						}
					}
				}
			}else if (meta == EnumRailDirection.NORTH_SOUTH){
				if (cart.motionZ > 0){
					for (int i = 1; i <= 8; ++i){
						if (world.getBlockState(pos.add(0, 0, i)).getBlock() == BlockDefs.goldInlay){
							teleportLocation = new AMVector3(pos.add(0, 0, i));
							break;
						}
					}
				}else if (cart.motionZ < 0){
					for (int i = 1; i <= 8; ++i){
						if (world.getBlockState(pos.add(0, 0, -i)).getBlock() == BlockDefs.goldInlay){
							teleportLocation = new AMVector3(pos.add(0, 0, -i));
							break;
						}
					}
				}
			}
			EnumRailDirection teleportMeta = teleportLocation != null ? world.getBlockState(teleportLocation.toBlockPos()).getValue(SHAPE) : null;
			long time = System.currentTimeMillis();
			boolean cooldownPassed = (time - millisSinceLastTeleport) > 5000;
			if (teleportLocation != null && (teleportMeta == EnumRailDirection.EAST_WEST || teleportMeta == EnumRailDirection.NORTH_SOUTH) && cooldownPassed){
				world.playSound(teleportLocation.x, teleportLocation.y, teleportLocation.z, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
				world.playSound(cart.posX, cart.posY, cart.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
				cart.setPosition(teleportLocation.x, teleportLocation.y, teleportLocation.z);
				cart.getEntityData().setLong(minecartGoldInlayKey, time);
			}
		}
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return new AxisAlignedBB(0f, 0f, 0f, 1f, 0.01f, 1f);
	}
	
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos) {
		super.getCollisionBoundingBox(blockState, worldIn, pos);
		return blockState.getBoundingBox(worldIn, pos);
	}
	
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn) {
		if (entityIn instanceof EntityMinecart) return;
		addCollisionBoxToList(pos, entityBox, collidingBoxes, state.getCollisionBoundingBox(worldIn, pos));
	}
	
	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		super.onBlockAdded(worldIn, pos, state);
		worldIn.scheduleBlockUpdate(pos, this, tickRate(worldIn), 0);
	}
	
	public BlockInlay registerAndName(ResourceLocation rl) {
		this.setUnlocalizedName(rl.toString());
		GameRegistry.register(this, rl);
		GameRegistry.register(new ItemBlockSubtypes(this), rl);
		return this;
	}
}
