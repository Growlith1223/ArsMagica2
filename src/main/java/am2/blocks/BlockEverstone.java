package am2.blocks;

import java.util.ArrayList;
import java.util.List;

import am2.ArsMagica2;
import am2.blocks.tileentity.TileEntityEverstone;
import am2.defs.ItemDefs;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockEverstone extends BlockAMPowered{
	
	public static final PropertyBool HAS_FACADE = PropertyBool.create("has_facade");
	public static final PropertyBool IS_SOLID = PropertyBool.create("is_solid");

	public BlockEverstone(){
		super(Material.ROCK);
		setHardness(3.0f);
		setResistance(3.0f);
		setDefaultState(blockState.getBaseState().withProperty(HAS_FACADE, false).withProperty(IS_SOLID, true));
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, HAS_FACADE, IS_SOLID);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState();
	}

	@Override
	public TileEntity createNewTileEntity(World world, int i){
		return new TileEntityEverstone();
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player,
			boolean willHarvest) {
		TileEntityEverstone everstone = getTE(world, pos);
		if (everstone == null){
			if (player.capabilities.isCreativeMode){
				world.setTileEntity(pos, null);
				world.setBlockToAir(pos);
				return true;
			}
			return false;
		}
		if (!player.capabilities.isCreativeMode)
			everstone.onBreak();
		if (player.capabilities.isCreativeMode){
			world.setTileEntity(pos, null);
			world.setBlockToAir(pos);
			return true;
		}
		return false;
	}
	
	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		return new ArrayList<>();
	}
	
	@Override
	public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
		TileEntityEverstone everstone = getTE(world, pos);
		if (everstone != null){
			everstone.onBreak();
		}
		return 10000f;
	}

	private TileEntityEverstone getTE(IBlockAccess world, BlockPos pos){
		if (world == null)
			return null;

		TileEntity te = world.getTileEntity(pos);
		if (te == null)
			return null;

		return (TileEntityEverstone)te;
	}	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
			EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!(player.getHeldItemMainhand() == ItemStack.EMPTY)){
			IBlockState block = null;
			TileEntityEverstone everstone = getTE(world, pos);
			if (everstone == null) return false;

			if (player.getHeldItemMainhand().getItem() == ItemDefs.crystalWrench){
				if (!world.isRemote){
					if (everstone.getFacade() != null){
						everstone.setFacade(null);
						return true;
					}else{
						world.setBlockToAir(pos);
						this.dropBlockAsItem(world, pos, state, 0);
						return true;
					}
				}
			
			}else if (player.getHeldItemMainhand().getItem() == Item.getItemFromBlock(this)){
				return true;
			}
			else if (player.getHeldItemMainhand().getItem() instanceof ItemBlock){
				ItemBlock itemblock = (ItemBlock)player.getHeldItemMainhand().getItem();
				block = itemblock.getBlock().getStateFromMeta(player.getHeldItemMainhand().getItemDamage());
			}
			if (everstone.getFacade() == null && block != null){
				everstone.setFacade(block);
				world.updateObservingBlocksAt(pos, this);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state){
		return false;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state){
		if (state.getValue(IS_SOLID) && !state.getValue(HAS_FACADE))
			return EnumBlockRenderType.MODEL;
		return EnumBlockRenderType.INVISIBLE;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos) {
		TileEntityEverstone everstone = getTE(worldIn, pos);
		if (everstone == null || everstone.isSolid())
			return super.getCollisionBoundingBox(blockState, worldIn, pos);
		return NULL_AABB;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
		TileEntityEverstone everstone = getTE(worldIn, pos);
		if (everstone == null || everstone.isSolid())
			return super.getSelectedBoundingBox(state, worldIn, pos);
		return new AxisAlignedBB(0, 0, 0, 0, 0, 0);
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		TileEntityEverstone everstone = getTE(source, pos);
		if (everstone == null || everstone.isSolid())
			return super.getBoundingBox(state, source, pos);
		return new AxisAlignedBB(0, 0, 0, 0, 0, 0);
	}
	
	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntityEverstone everstone = getTE(world, pos);
		if (everstone == null) return true;
		return everstone.isSolid();
	}
	
	@Override
	public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
		TileEntityEverstone everstone = getTE(worldIn, pos);
		if (everstone == null) return this.blockHardness;
		IBlockState block = everstone.getFacade();
		if (block == null || block == this) return this.blockHardness;
		return block.getBlockHardness(worldIn, pos);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {

		TileEntityEverstone everstone = getTE(world, pos);

		for (int i = 0; i < 5 * ArsMagica2.config.getGFXLevel(); ++i){
			IBlockState block;
			if (everstone == null || everstone.getFacade() == null){
				block = this.getDefaultState();
			}else{
				block = everstone.getFacade();
				if (block == null) block = this.getDefaultState();
			}
			
			manager.addEffect(new ParticleDigging.Factory().createParticle(0, world,
					pos.getX() + world.rand.nextDouble(),
					pos.getY() + world.rand.nextDouble(),
					pos.getZ() + world.rand.nextDouble(), 0, 0, 0, Block.getStateId(block)));
		}

		return true;
	}
	
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean addHitEffects(IBlockState state, World world, RayTraceResult target, ParticleManager manager){
		TileEntityEverstone everstone = getTE(world, target.getBlockPos());
		IBlockState block;
		if (everstone == null || everstone.getFacade() == null){
			block = this.getDefaultState();
		}else{
			block = everstone.getFacade();
			if (block == null) block = this.getDefaultState();
		}
		
		
		manager.addEffect(new ParticleDigging.Factory().createParticle(0, world,
				target.getBlockPos().getX() + world.rand.nextDouble(),
				target.getBlockPos().getY() + world.rand.nextDouble(),
				target.getBlockPos().getZ() + world.rand.nextDouble(), 0, 0, 0, Block.getStateId(block)));

		return true;
	}
}
