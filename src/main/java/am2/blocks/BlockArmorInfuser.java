package am2.blocks;

import am2.ArsMagica2;
import am2.blocks.tileentity.TileEntityArmorImbuer;
import am2.defs.IDDefs;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;

public class BlockArmorInfuser extends BlockAMPowered{

	public BlockArmorInfuser(){
		super(Material.IRON);
		setHardness(4.0f);
		setResistance(4.0f);
		defaultRender = true;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int i){
		return new TileEntityArmorImbuer();
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		super.onBlockActivated(worldIn, pos, state, playerIn, hand, side, hitX, hitY, hitZ);
		if (HandleSpecialItems(worldIn, playerIn, pos))
			return true;
		if (!worldIn.isRemote){
			FMLNetworkHandler.openGui(playerIn, ArsMagica2.instance, IDDefs.GUI_ARMOR_INFUSION, worldIn, pos.getX(), pos.getY(), pos.getZ());
			/*
			if (KeystoneUtilities.HandleKeystoneRecovery(par5EntityPlayer, ((IKeystoneLockable)par1World.getTileEntity(par2, par3, par4))))
				return true;
			if (KeystoneUtilities.instance.canPlayerAccess((IKeystoneLockable)par1World.getTileEntity(par2, par3, par4), par5EntityPlayer, KeystoneAccessType.USE)){
				super.onBlockActivated(par1World, par2, par3, par4, par5EntityPlayer, par6, par7, par8, par9);
				FMLNetworkHandler.openGui(par5EntityPlayer, AMCore.instance, ArsMagicaGuiIdList.GUI_ARMOR_INFUSION, par1World, par2, par3, par4);
			}
			*/
		}
		return true;	
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state){
		if (world.isRemote){
			super.breakBlock(world, pos, state);
			return;
		}
		TileEntityArmorImbuer imbuer = (TileEntityArmorImbuer)world.getTileEntity(pos);
		if (imbuer == null) return;
		for (int l = 0; l < imbuer.getSizeInventory() - 3; l++){
			ItemStack itemstack = imbuer.getStackInSlot(l);
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
