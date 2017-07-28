package am2.blocks;

import am2.ArsMagica2;
import am2.blocks.tileentity.TileEntityParticleEmitter;
import am2.defs.ItemDefs;
import am2.items.ItemCrystalWrench;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockParticleEmitter extends BlockAMContainer{
	
	public static final PropertyEnum<EnumFacing> FACING = PropertyEnum.create("facing", EnumFacing.class, EnumFacing.HORIZONTALS);
	public static final PropertyBool HIDDEN = PropertyBool.create("hidden");
	
	
	public BlockParticleEmitter(){
		super(Material.GLASS);
		setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(HIDDEN, false));
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING, HIDDEN);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		int meta = state.getValue(FACING).getHorizontalIndex();
		if (state.getValue(HIDDEN)) meta |= 0x8;
		return meta;
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta & 0x3)).withProperty(HIDDEN, (meta & 0x8) == 0x8);
	}	

	@Override
	public boolean isOpaqueCube(IBlockState state){
		return false;
	}
	
	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player,
			boolean willHarvest) {
		TileEntityParticleEmitter tile = (TileEntityParticleEmitter)world.getTileEntity(pos);
		if(tile != null && !tile.getShow())
			return false;
		
		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}
	
	@Override
	public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
		TileEntity te = world.getTileEntity(pos);
		TileEntityParticleEmitter te2 = null;
		if (te instanceof TileEntityParticleEmitter)
			te2 = (TileEntityParticleEmitter) te;
		if (te2 == null)
			super.onBlockExploded(world, pos, explosion);
		if (te2 != null && te2.getShow())
			super.onBlockExploded(world, pos, explosion);
	}
	
	
	
	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public TileEntity createNewTileEntity(World world, int i){
		return new TileEntityParticleEmitter();
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
	
	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return state.getValue(HIDDEN) ? new AxisAlignedBB(0, 0, 0, 0, 0, 0) : super.getBoundingBox(state, source, pos);
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos) {
		return blockState.getValue(HIDDEN) ? null : super.getCollisionBoundingBox(blockState, worldIn, pos);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		ItemStack heldItem = playerIn.getHeldItem(hand);
		if (worldIn.isRemote) {
			TileEntity te = worldIn.getTileEntity(pos);
			if (te != null && te instanceof TileEntityParticleEmitter) {
				if (heldItem != null
						&& heldItem.getItem() == ItemDefs.crystalWrench) {
					if (ItemCrystalWrench.getMode(heldItem) == 0) {
						ArsMagica2.proxy.openParticleBlockGUI(worldIn, playerIn, (TileEntityParticleEmitter) te);
					} else {
						if (ArsMagica2.proxy.cwCopyLoc == null) {
							playerIn.sendMessage(new TextComponentString("Settings Copied."));
							ArsMagica2.proxy.cwCopyLoc = new NBTTagCompound();
							((TileEntityParticleEmitter) te).writeSettingsToNBT(ArsMagica2.proxy.cwCopyLoc);
						} else {
							playerIn.sendMessage(new TextComponentString("Settings Applied."));
							((TileEntityParticleEmitter) te).readSettingsFromNBT(ArsMagica2.proxy.cwCopyLoc);
							((TileEntityParticleEmitter) te).syncWithServer();
							ArsMagica2.proxy.cwCopyLoc = null;
						}
					}
					return true;
				} else {
					ArsMagica2.proxy.openParticleBlockGUI(worldIn, playerIn, (TileEntityParticleEmitter) te);
					return true;
				}
			}
		}
		return true;
	}
}
