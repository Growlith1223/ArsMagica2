package am2.blocks;

import am2.ArsMagica2;
import am2.api.items.KeystoneAccessType;
import am2.blocks.tileentity.TileEntityKeystoneChest;
import am2.defs.IDDefs;
import am2.utils.KeystoneUtilities;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;


public class BlockKeystoneChest extends BlockAMSpecialRenderContainer{

	public static final PropertyEnum<EnumFacing> FACING = PropertyEnum.create("facing", EnumFacing.class, EnumFacing.HORIZONTALS);
	
	public BlockKeystoneChest(){
		super(Material.WOOD);
		this.setResistance(900000);
		this.setHardness(3.0f);
		this.setBlockBounds(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.875F, 0.9375F);
		setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getHorizontalIndex();
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta));
	}
	
	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,int meta, EntityLivingBase placer) {
		return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public TileEntity createNewTileEntity(World par1World, int i){
		return new TileEntityKeystoneChest();
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ){

		super.onBlockActivated(worldIn, pos, state, playerIn, hand, side, hitX, hitY, hitZ);

		if (!worldIn.isRemote){

			TileEntity myTE = worldIn.getTileEntity(pos);

			if (!(myTE instanceof TileEntityKeystoneChest)) return false;

			TileEntityKeystoneChest te = (TileEntityKeystoneChest)myTE;

			if (KeystoneUtilities.HandleKeystoneRecovery(playerIn, te)){
				return true;
			}

			if (!KeystoneUtilities.instance.canPlayerAccess(te, playerIn, KeystoneAccessType.USE)){
				return true;
			}

			/*PacketDispatcher.sendPacketToPlayer(AMCore.instance.proxy.packetSender.createArsMagicaClientPacket(AMPacketIDs.KEYSTONE_CHEST_GUI_OPEN,
					new AMDataWriter().add(par2).add(par3).add(par4).generate()), (Player)entityplayer);*/

			FMLNetworkHandler.openGui(playerIn, ArsMagica2.instance, IDDefs.GUI_KEYSTONE_CHEST, worldIn, pos.getX(), pos.getY(), pos.getZ());

			return true;
		}else{
			return true;
		}
	}
	
	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player){
		TileEntityKeystoneChest receptacle = (TileEntityKeystoneChest)worldIn.getTileEntity(pos);
		if (receptacle == null)
			return;
		if (KeystoneUtilities.instance.canPlayerAccess(receptacle, player, KeystoneAccessType.BREAK)){
			for (int i = receptacle.getSizeInventory() - 3; i < receptacle.getSizeInventory(); i++){
				receptacle.decrStackSize(i, 9001);
				// arbitrary number, just in case rune stack sizes increase in the future
				// yes, it's hard-coded; yes, it's also less computationally intensive than a stack size lookup
			}
		}
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state){
		if (world.isRemote){
			super.breakBlock(world, pos, state);
			return;
		}
		TileEntityKeystoneChest receptacle = (TileEntityKeystoneChest)world.getTileEntity(pos);

		if (receptacle == null) return;

		if (KeystoneUtilities.instance.getKeyFromRunes(receptacle.getRunesInKey()) == 0){

			for (int l = 0; l < receptacle.getSizeInventory() - 3; l++){
				ItemStack itemstack = receptacle.getStackInSlot(l);
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
			super.breakBlock(world, pos, state);
		}
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest){
		TileEntityKeystoneChest chest = (TileEntityKeystoneChest)world.getTileEntity(pos);
		if (!KeystoneUtilities.instance.canPlayerAccess(chest, player, KeystoneAccessType.BREAK)) return false;

		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}

	@Override
	public boolean canPlaceBlockAt(World par1World, BlockPos pos){
		int var5 = 0;

		if (par1World.getBlockState(pos.east()).getBlock() == this){
			++var5;
		}

		if (par1World.getBlockState(pos.west()).getBlock() == this){
			++var5;
		}

		if (par1World.getBlockState(pos.north()).getBlock() == this){
			++var5;
		}

		if (par1World.getBlockState(pos.south()).getBlock() == this){
			++var5;
		}

		return !(var5 > 0);
	}
}
