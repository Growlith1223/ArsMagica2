package am2.blocks;

import am2.ArsMagica2;
import am2.api.blocks.IKeystoneLockable;
import am2.api.items.KeystoneAccessType;
import am2.blocks.tileentity.TileEntityKeystoneDoor;
import am2.defs.CreativeTabsDefs;
import am2.defs.IDDefs;
import am2.items.ItemBlockSubtypes;
import am2.utils.KeystoneUtilities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockKeystoneTrapdoor extends BlockTrapDoor implements ITileEntityProvider{

	public BlockKeystoneTrapdoor(){
		super(Material.WOOD);
		this.setHardness(2.5f);
		this.setResistance(2.0f);
		this.setCreativeTab(CreativeTabsDefs.tabAM2Blocks);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int i){
		return new TileEntityKeystoneDoor();
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ){
		TileEntity te = worldIn.getTileEntity(pos);

		playerIn.swingArm(hand);

		if (KeystoneUtilities.HandleKeystoneRecovery(playerIn, (IKeystoneLockable<?>)te))
			return true;
		if (KeystoneUtilities.instance.canPlayerAccess((IKeystoneLockable<?>)te, playerIn, KeystoneAccessType.USE)){
			if (playerIn.isSneaking()){
				FMLNetworkHandler.openGui(playerIn, ArsMagica2.instance, IDDefs.GUI_KEYSTONE_LOCKABLE, worldIn, pos.getX(), pos.getY(), pos.getZ());
			}else{
				//worldIn.playSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_WOODEN_DOOR_OPEN, SoundCategory.BLOCKS, 1.0f, 1.0f, true);
				return super.onBlockActivated(worldIn, pos, state, playerIn, hand, side, hitX, hitY, hitZ);
			}
		}

		return false;
	}
	
	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player,
			boolean willHarvest){
		IKeystoneLockable<?> lockable = (IKeystoneLockable<?>)world.getTileEntity(pos);
		if (!KeystoneUtilities.instance.canPlayerAccess(lockable, player, KeystoneAccessType.BREAK)) return false;

		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}
	
	
	public Block registerAndName(ResourceLocation rl) {
		this.setUnlocalizedName(rl.toString());
		GameRegistry.register(this, rl);
		GameRegistry.register(new ItemBlockSubtypes(this), rl);
		return this;
	}
	
	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}

}
