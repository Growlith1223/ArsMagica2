package am2.blocks;

import am2.ArsMagica2;
import am2.blocks.tileentity.TileEntityLectern;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockLectern extends BlockAMSpecialRenderContainer{

	public static final PropertyEnum<EnumFacing> FACING = PropertyEnum.create("facing", EnumFacing.class, EnumFacing.HORIZONTALS);

	public BlockLectern(){
		super(Material.WOOD);
		setHardness(2.0f);
		setResistance(2.0f);
		this.setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
	}

	@Override
	public TileEntity createNewTileEntity(World world, int i){
		return new TileEntityLectern();
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntityLectern te = getTileEntity(world, pos);
		if (te == null){
			return true;
		}
		if (te.hasStack()){
			if (player.isSneaking()){
				if (!world.isRemote && ((player instanceof EntityPlayerMP) && ((EntityPlayerMP)player).interactionManager.getGameType() != GameType.ADVENTURE)){
					float f = world.rand.nextFloat() * 0.8F + 0.1F;
					float f1 = world.rand.nextFloat() * 0.8F + 0.1F;
					float f2 = world.rand.nextFloat() * 0.8F + 0.1F;
					ItemStack newItem = te.getStack().copy();
					EntityItem entityitem = new EntityItem(world, pos.getX() + f, pos.getY() + f1, pos.getZ() + f2, newItem);
					float f3 = 0.05F;
					entityitem.motionX = (float)world.rand.nextGaussian() * f3;
					entityitem.motionY = (float)world.rand.nextGaussian() * f3 + 0.2F;
					entityitem.motionZ = (float)world.rand.nextGaussian() * f3;
					world.spawnEntity(entityitem);
					te.setStack(null);
				}
			}else{
				if (te.getStack().getItem() == Items.WRITTEN_BOOK && world.isRemote && player == ArsMagica2.proxy.getLocalPlayer())
					openBook(player, te);
				else
					te.getStack().getItem().onItemRightClick(world, player, hand);
				return true;
			}
		}else{
			if (player.getHeldItem(hand) != null){
				if (te.setStack(player.getHeldItem(hand).copy())){
					player.getHeldItem(hand).shrink(1);;
					if (player.getHeldItem(hand).getCount() <= 0){
						player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
					}
				}
			}
		}
		
		return true;
	}
	
	@SideOnly(Side.CLIENT)
	private void openBook(EntityPlayer player, TileEntityLectern te) {
		Minecraft.getMinecraft().displayGuiScreen(new GuiScreenBook(player, te.getStack(), false));
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TileEntityLectern te = getTileEntity(world, pos);
		if (te == null){
			return;
		}
		if (!world.isRemote){
			if (te.hasStack()){
				float f = world.rand.nextFloat() * 0.8F + 0.1F;
				float f1 = world.rand.nextFloat() * 0.8F + 0.1F;
				float f2 = world.rand.nextFloat() * 0.8F + 0.1F;
				ItemStack newItem = new ItemStack(te.getStack().getItem(), 1, te.getStack().getItemDamage());
				if (te.getStack().getTagCompound() != null)
					newItem.setTagCompound((NBTTagCompound)te.getStack().getTagCompound().copy());
				EntityItem entityitem = new EntityItem(world, pos.getX() + f, pos.getY() + f1, pos.getZ() + f2, newItem);
				float f3 = 0.05F;
				entityitem.motionX = (float)world.rand.nextGaussian() * f3;
				entityitem.motionY = (float)world.rand.nextGaussian() * f3 + 0.2F;
				entityitem.motionZ = (float)world.rand.nextGaussian() * f3;
				world.spawnEntity(entityitem);
			}
		}		
		super.breakBlock(world, pos, state);
	}

	private TileEntityLectern getTileEntity(World world, BlockPos pos){
		TileEntity te = world.getTileEntity(pos);
		if (te != null && te instanceof TileEntityLectern){
			return (TileEntityLectern)te;
		}
		return null;
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).ordinal() - 2;
	}
	
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(FACING, EnumFacing.values()[meta + 2]);
	}
	
	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

}
