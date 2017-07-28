package am2.blocks;

import am2.ArsMagica2;
import am2.api.blocks.IKeystoneLockable;
import am2.api.items.KeystoneAccessType;
import am2.blocks.tileentity.TileEntityAstralBarrier;
import am2.defs.IDDefs;
import am2.utils.KeystoneUtilities;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;

public class BlockAstralBarrier extends BlockAMPowered{

	public BlockAstralBarrier(){
		super(Material.ROCK);
		setHardness(3.0f);
		setResistance(2.0f);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ){

		TileEntity te = worldIn.getTileEntity(pos);
		TileEntityAstralBarrier abte = null;
		if (te != null && te instanceof TileEntityAstralBarrier){
			abte = (TileEntityAstralBarrier)te;
		}else{
			return true;
		}

		if (HandleSpecialItems(worldIn, playerIn, pos)){
			return true;
		}
		if (!worldIn.isRemote)
			if (KeystoneUtilities.HandleKeystoneRecovery(playerIn, ((IKeystoneLockable<?>)worldIn.getTileEntity(pos))))
				return true;

		if (KeystoneUtilities.instance.canPlayerAccess((IKeystoneLockable<?>)worldIn.getTileEntity(pos), playerIn, KeystoneAccessType.USE)){
			if (playerIn.isSneaking()){
				if (worldIn.isRemote){
					abte.ToggleAuraDisplay();
					playerIn.sendMessage(new TextComponentString("Barrier Aura Toggled"));
				}
			}else{
				if (!worldIn.isRemote){
					super.onBlockActivated(worldIn, pos, state, playerIn, hand, side, hitX, hitY, hitZ);
					FMLNetworkHandler.openGui(playerIn, ArsMagica2.instance, IDDefs.GUI_ASTRAL_BARRIER, worldIn, pos.getX(), pos.getY(), pos.getZ());
				}
			}
		}
		return true;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state){
		if (world.isRemote){
			super.breakBlock(world, pos, state);
			return;
		}
		TileEntityAstralBarrier barrier = (TileEntityAstralBarrier)world.getTileEntity(pos);
		if (barrier == null) return;
		for (int l = 0; l < barrier.getSizeInventory() - 3; l++){
			ItemStack itemstack = barrier.getStackInSlot(l);
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
	
	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player,
			boolean willHarvest) {
		IKeystoneLockable<?> lockable = (IKeystoneLockable<?>)world.getTileEntity(pos);
		if (!KeystoneUtilities.instance.canPlayerAccess(lockable, player, KeystoneAccessType.BREAK)) return false;
		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}

	@Override
	public TileEntity createNewTileEntity(World par1World, int i){
		return new TileEntityAstralBarrier();
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
