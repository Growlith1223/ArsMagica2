package am2.blocks;

import java.util.List;

import am2.ArsMagica2;
import am2.api.math.AMVector3;
import am2.blocks.tileentity.TileEntityCrystalMarker;
import am2.blocks.tileentity.TileEntityCrystalMarkerSpellExport;
import am2.blocks.tileentity.TileEntityFlickerHabitat;
import am2.defs.IDDefs;
import am2.defs.ItemDefs;
import am2.items.ItemBlockCrystalMarker;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;

@SuppressWarnings("deprecation")
public class BlockCrystalMarker extends BlockAMContainer{

	public static final int META_IN = 0;
	public static final int META_OUT = 1;
	public static final int META_LIKE_EXPORT = 2;
	public static final int META_SET_EXPORT = 3;
	public static final int META_REGULATE_EXPORT = 4;
	public static final int META_REGULATE_MULTI = 5;
	public static final int META_SET_IMPORT = 6;
	public static final int META_FINAL_DEST = 7;
	public static final int META_SPELL_EXPORT = 8;
	
	public static final PropertyInteger TYPE = PropertyInteger.create("type", 0, 8);
	public static final PropertyDirection FACING = PropertyDirection.create("facing");

	public static final String[] crystalMarkerTypes = {
			"cm_import",
			"cm_export",
			"cm_likeExport",
			"cm_setExport",
			"cm_regulateExport",
			"cm_regulateMulti",
			"cm_setImport",
			"cm_final",
			"cm_spellExport"
	};

	public BlockCrystalMarker(){
		super(Material.GLASS);
		setDefaultState(blockState.getBaseState().withProperty(TYPE, 0).withProperty(FACING, EnumFacing.UP));
	}

	private int getCrystalTier(int meta){
		switch (meta){
		case META_IN:
		case META_OUT:
		case META_FINAL_DEST:
			return 0;
		case META_LIKE_EXPORT:
		case META_SET_EXPORT:
		case META_SET_IMPORT:
			return 1;
		case META_REGULATE_EXPORT:
		case META_REGULATE_MULTI:
			return 2;
		case META_SPELL_EXPORT:
			return 3;
		}
		return 0;
	}
	
	@Override
	public int getLightValue(IBlockState state) {
		return 3 + getCrystalTier(getMetaFromState(state));
	}

	public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List){
		par3List.add(new ItemStack(this, 1, META_IN));
		par3List.add(new ItemStack(this, 1, META_OUT));
		par3List.add(new ItemStack(this, 1, META_LIKE_EXPORT));
		par3List.add(new ItemStack(this, 1, META_SET_EXPORT));
		par3List.add(new ItemStack(this, 1, META_REGULATE_EXPORT));
		par3List.add(new ItemStack(this, 1, META_REGULATE_MULTI));
		par3List.add(new ItemStack(this, 1, META_SET_IMPORT));
		par3List.add(new ItemStack(this, 1, META_FINAL_DEST));
		par3List.add(new ItemStack(this, 1, META_SPELL_EXPORT));
	}

	@Override
	public TileEntity createNewTileEntity(World world, int i){
		if (i == META_SPELL_EXPORT)
			return new TileEntityCrystalMarkerSpellExport(i);
		return new TileEntityCrystalMarker(i);
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
			EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ){
		ItemStack heldItem = player.getHeldItem(hand);
		int operandType = state.getValue(TYPE);

		if (player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() == ItemDefs.crystalWrench){
			player.swingArm(EnumHand.MAIN_HAND);

			if (world.isRemote){
				return true;
			}

			return false;
		}else if (heldItem != null && heldItem.getItem() == ItemDefs.spellStaffMagitech){
			//if we're here, we are changing the crystal's priority level
			//swing the item, first off.
			player.swingArm(hand);
			//do nothing on client worlds other than this.
			if (world.isRemote){
				return true;
			}

			//input node?  Nothing more to do here than notify the player.
			if (operandType == META_IN){
				player.sendMessage(new TextComponentString(I18n.translateToLocal("am2.tooltip.noPriIn")));
				return false;
			}

			//resolve the crystal marker's Tile Entity
			TileEntityCrystalMarker crystalMarkerTE = GetTileEntity(world, pos);
			TileEntityFlickerHabitat elementalAttunerTE = null;

			if (crystalMarkerTE != null){
				//store the old priority for use later
				int oldPriority = crystalMarkerTE.getPriority();
				//cycle the priority (increment with limits)
				crystalMarkerTE.cyclePriority();
				//resolve the attached attuner for this crystal
				AMVector3 elementalAttunerVector = crystalMarkerTE.getElementalAttuner();

				if (elementalAttunerVector != null){
					//if we are here, there is an attached attuner to this marker.  Resolve the location into the tile entity.
					elementalAttunerTE = GetElementalAttunerTileEntity(world, elementalAttunerVector.toBlockPos());
					if (elementalAttunerTE != null){
						//Notify the attuner that this crystal now has a new priority
						elementalAttunerTE.switchMarkerPriority(new AMVector3(pos), oldPriority, crystalMarkerTE.getPriority());
					}
				}

				//finally, notify the player of the new priority
				player.sendMessage(
						new TextComponentString(String.format(
								I18n.translateToLocal("am2.tooltip.priSet"),
								String.format("%d", crystalMarkerTE.getPriority()) //need to put this as a string, because for some reason %d doesn't work when used in a localized string, but %s does
						)));
			}

			return false;
		}else if (operandType == META_SET_EXPORT || operandType == META_REGULATE_EXPORT || operandType == META_REGULATE_MULTI || operandType == META_SET_IMPORT){
			FMLNetworkHandler.openGui(player, ArsMagica2.instance, IDDefs.GUI_CRYSTAL_MARKER, world, pos.getX(), pos.getY(), pos.getZ());
			return true;
		}

		return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(FACING, facing.getOpposite());
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack){
		TileEntity te = world.getTileEntity(pos);
		if (te != null && te instanceof TileEntityCrystalMarker){
			((TileEntityCrystalMarker)te).setFacing(state.getValue(FACING));

			IBlockState attachedTo = null;
			double minx = 0;
			double miny = 0;
			double minz = 0;
			double maxx = 1;
			double maxy = 1;
			double maxz = 1;
			
			attachedTo = world.getBlockState(pos.offset(state.getValue(FACING)));

			if (attachedTo != null){
				minx = attachedTo.getBoundingBox(world, pos).minX;
				miny = attachedTo.getBoundingBox(world, pos).minY;
				minz = attachedTo.getBoundingBox(world, pos).minZ;
				maxx = attachedTo.getBoundingBox(world, pos).maxX;
				maxy = attachedTo.getBoundingBox(world, pos).maxY;
				maxz = attachedTo.getBoundingBox(world, pos).maxZ;
				((TileEntityCrystalMarker)te).SetConnectedBoundingBox(minx, miny, minz, maxx, maxy, maxz);
			}
		}
		super.onBlockPlacedBy(world, pos, state, placer, stack);
	}

	private TileEntityCrystalMarker GetTileEntity(World world, BlockPos pos){
		TileEntity te = world.getTileEntity(pos);

		if (te != null && te instanceof TileEntityCrystalMarker){
			return (TileEntityCrystalMarker)te;
		}else{
			return null;
		}
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TileEntityCrystalMarker crystalMarkerTE = GetTileEntity(world, pos);
		TileEntityFlickerHabitat elementalAttunerTE = null;

		if (crystalMarkerTE != null){
			AMVector3 elementalAttunerVector = crystalMarkerTE.getElementalAttuner();

			if (elementalAttunerVector != null){
				elementalAttunerTE = GetElementalAttunerTileEntity(world,
						elementalAttunerVector.toBlockPos());
				if (elementalAttunerTE != null){
					int operandType = state.getValue(TYPE);

					if (isInputMarker(operandType)){
						elementalAttunerTE.removeInMarkerLocation(pos);
					}else if (isOutputMarker(operandType)){
						elementalAttunerTE.removeOutMarkerLocation(pos);
					}
				}
			}
		}

		super.breakBlock(world, pos, state);
	}
	
	private TileEntityFlickerHabitat GetElementalAttunerTileEntity(
			World world, BlockPos pos){
		TileEntity te = world.getTileEntity(pos);

		if (te != null && te instanceof TileEntityFlickerHabitat){
			return (TileEntityFlickerHabitat)te;
		}else{
			return null;
		}
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state){
		return false;
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		TileEntity te = source.getTileEntity(pos);
		TileEntityCrystalMarker cm = (TileEntityCrystalMarker)te;
		if (cm == null)
			return new AxisAlignedBB(0, 0, 0, 0, 0, 0);
		EnumFacing facing = state.getValue(FACING);


		switch (facing){
		case UP: //Bottom, Inventory is above
			return new AxisAlignedBB(0.35f, (float)((1 + cm.GetConnectedBoundingBox().minY)) - 0.1f, 0.32f, 0.65f, (float)((1 + cm.GetConnectedBoundingBox().minY)), 0.68f);
		case DOWN: //Top, Inventory is below
			return new AxisAlignedBB(0.35f, (float)(-1 * (1 - cm.GetConnectedBoundingBox().maxY)), 0.3f, 0.65f, (float)(-1 * (1 - cm.GetConnectedBoundingBox().maxY)) + 0.1f, 0.7f);
		case SOUTH: //North, Inventory is to the south
			return new AxisAlignedBB(0.35f, 0.32f, (float)(1 + (1 - cm.GetConnectedBoundingBox().maxZ)) - 0.1f, 0.65f, 0.68f, (float)(1 + (1 - cm.GetConnectedBoundingBox().maxZ)));
		case NORTH: //South, Inventory is to the north
			return new AxisAlignedBB(0.35f, 0.32f, (float)(0 - cm.GetConnectedBoundingBox().minZ), 0.65f, 0.68f, (float)(0 - cm.GetConnectedBoundingBox().minZ) + 0.1f);
		case EAST: //West, Inventory is to the east
			return new AxisAlignedBB((float)(1 + cm.GetConnectedBoundingBox().minX) - 0.1f, 0.32f, 0.35f, (float)(1 + cm.GetConnectedBoundingBox().minX), 0.68f, 0.65f);
		case WEST: //East, Inventory is to the west
			return new AxisAlignedBB((float)(-1 * (1 - cm.GetConnectedBoundingBox().maxX)), 0.32f, 0.35f, (float)(-1 * (1 - cm.GetConnectedBoundingBox().maxX)) + 0.1f, 0.68f, 0.65f);
		}
		return null;
	}
	
	@Override
	public void onNeighborChange(IBlockAccess iblockaccess, BlockPos pos, BlockPos neighbor){
		if (!(iblockaccess instanceof World)) return;
		World world = (World)iblockaccess;
		TileEntity te = world.getTileEntity(pos);
		TileEntityCrystalMarker cm = null;
		boolean mustDrop = false;

		if (te != null && te instanceof TileEntityCrystalMarker){
			cm = (TileEntityCrystalMarker)te;
		}else{
			return;
		}

		mustDrop = world.isAirBlock(pos.offset(cm.getFacing()));

		if (mustDrop && !world.isRemote){
			IBlockState block = world.getBlockState(pos);
			ItemStack itemStack = new ItemStack(block.getBlock(), 1, block.getBlock().getMetaFromState(block));
			EntityItem entityItem = new EntityItem((World) world, pos.getX(), pos.getY(), pos.getZ(), itemStack);
			((World) world).spawnEntity(entityItem);
			((World) world).setBlockToAir(pos);
		}
	}

	public static boolean isOutputMarker(int operandType){
		return
				operandType == META_OUT ||
						operandType == META_LIKE_EXPORT ||
						operandType == META_SET_EXPORT ||
						operandType == META_REGULATE_EXPORT ||
						operandType == META_REGULATE_MULTI ||
						operandType == META_FINAL_DEST ||
						operandType == META_SPELL_EXPORT;
	}

	public static boolean isInputMarker(int operandType){
		return
				operandType == META_IN ||
						operandType == META_REGULATE_MULTI ||
						operandType == META_SET_IMPORT ||
						operandType == META_SPELL_EXPORT;
	}
	
	@Override
	public int damageDropped(IBlockState state) {
		return getMetaFromState(state);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(TYPE);
	}
	
	@Override
	public int getLightOpacity(IBlockState state) {
		return 0;
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(TYPE, meta);
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, TYPE, FACING);
	}
	
	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}
	
	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isNormalCube(IBlockState state) {
		return false;
	}
	
	@Override
	public BlockAMContainer registerAndName(ResourceLocation rl) {
		this.setUnlocalizedName(rl.toString());
		GameRegistry.register(this, rl);
		GameRegistry.register(new ItemBlockCrystalMarker(this), rl);
		return this;
	}
}

