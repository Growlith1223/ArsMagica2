package am2.blocks;


import am2.ArsMagica2;
import am2.api.blocks.IKeystoneLockable;
import am2.api.items.KeystoneAccessType;
import am2.blocks.tileentity.TileEntityEssenceRefiner;
import am2.defs.IDDefs;
import am2.utils.KeystoneUtilities;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;

public class BlockEssenceRefiner extends BlockAMPowered{

	public static final PropertyBool ACTIVE = PropertyBool.create("active");
	public static final PropertyEnum<EnumFacing> FACING = PropertyEnum.create("facing", EnumFacing.class, EnumFacing.HORIZONTALS);

	private static boolean keepRefinerInventory = false;

	public BlockEssenceRefiner(){
		super(Material.WOOD);
		setHardness(2.0f);
		setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(ACTIVE, Boolean.valueOf(false)));
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING, ACTIVE);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		int meta = state.getValue(FACING).getHorizontalIndex();
		if (state.getValue(ACTIVE).booleanValue())
			meta |= 0x4;
		return meta;
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta & 0x3)).withProperty(ACTIVE, (meta & 0x4) == 0x4);
	}

	@Override
	public boolean onBlockActivated(World par1World, BlockPos pos, IBlockState state, EntityPlayer par5EntityPlayer, EnumHand hand, EnumFacing par6, float par7, float par8, float par9){
		if (this.HandleSpecialItems(par1World, par5EntityPlayer, pos)){
			return true;
		}
		if (!par1World.isRemote){
			if (KeystoneUtilities.HandleKeystoneRecovery(par5EntityPlayer, ((IKeystoneLockable<?>)par1World.getTileEntity(pos))))
				return true;
			if (KeystoneUtilities.instance.canPlayerAccess((IKeystoneLockable<?>)par1World.getTileEntity(pos), par5EntityPlayer, KeystoneAccessType.USE)){
				super.onBlockActivated(par1World, pos, state, par5EntityPlayer, hand, par6, par7, par8, par9);
				FMLNetworkHandler.openGui(par5EntityPlayer, ArsMagica2.instance, IDDefs.GUI_ESSENCE_REFINER, par1World, pos.getX(), pos.getY(), pos.getZ());
			}
		}
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state){
		if (!keepRefinerInventory){
			TileEntityEssenceRefiner refiner = (TileEntityEssenceRefiner)world.getTileEntity(pos);
			if (refiner == null) return;
			for (int l = 0; l < refiner.getSizeInventory() - 3; l++){
				ItemStack itemstack = refiner.getStackInSlot(l);
				if (itemstack == null){
					continue;
				}
				float f = world.rand.nextFloat() * 0.8F + 0.1F;
				float f1 = world.rand.nextFloat() * 0.8F + 0.1F;
				float f2 = world.rand.nextFloat() * 0.8F + 0.1F;
				do{
					if (itemstack.getCount() <= 0){
						break;
					}
					int i1 = world.rand.nextInt(21) + 10;
					if (i1 > itemstack.getCount()){
						i1 = itemstack.getCount();
					}
					itemstack.shrink(i1);
					ItemStack newItem = new ItemStack(itemstack.getItem(), i1, itemstack.getItemDamage());
					newItem.setTagCompound(itemstack.getTagCompound());
					EntityItem entityitem = new EntityItem(world, pos.getX() + f, pos.getY() + f1, pos.getZ() + f2, newItem);
					float f3 = 0.05F;
					entityitem.motionX = (float)world.rand.nextGaussian() * f3;
					entityitem.motionY = (float)world.rand.nextGaussian() * f3 + 0.2F;
					entityitem.motionZ = (float)world.rand.nextGaussian() * f3;
					world.spawnEntity(entityitem);
				}while (true);
			}

		}
		super.breakBlock(world, pos, state);
	}
	
	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player,
			boolean willHarvest){
		IKeystoneLockable<?> lockable = (IKeystoneLockable<?>)world.getTileEntity(pos);
		if (!KeystoneUtilities.instance.canPlayerAccess(lockable, player, KeystoneAccessType.BREAK)) return false;

		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}

	@Override
	public TileEntity createNewTileEntity(World par1World, int i){
		return new TileEntityEssenceRefiner();
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
	
	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}
}
