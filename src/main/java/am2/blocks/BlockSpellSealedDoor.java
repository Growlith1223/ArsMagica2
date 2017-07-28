package am2.blocks;

import java.util.Random;

import am2.ArsMagica2;
import am2.api.blocks.IKeystoneLockable;
import am2.api.items.KeystoneAccessType;
import am2.api.spell.SpellComponent;
import am2.blocks.tileentity.TileEntitySpellSealedDoor;
import am2.defs.BlockDefs;
import am2.defs.IDDefs;
import am2.defs.ItemDefs;
import am2.items.ItemKeystoneDoor;
import am2.utils.KeystoneUtilities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;


public class BlockSpellSealedDoor extends BlockDoor implements ITileEntityProvider{

	public BlockSpellSealedDoor(){
		super(Material.WOOD);
		this.setHardness(2.5f);
		this.setResistance(2.0f);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ){

		if (worldIn.getBlockState(pos.down()).getBlock() == BlockDefs.spellSealedDoor)
			pos = pos.down();

		TileEntity te = worldIn.getTileEntity(pos);

		playerIn.swingArm(hand);

		if (!worldIn.isRemote){

			if (KeystoneUtilities.HandleKeystoneRecovery(playerIn, (IKeystoneLockable<?>)te))
				return true;

			if (KeystoneUtilities.instance.canPlayerAccess((IKeystoneLockable<?>)te, playerIn, KeystoneAccessType.USE)){
				if (playerIn.isSneaking()){
					FMLNetworkHandler.openGui(playerIn, ArsMagica2.instance, IDDefs.GUI_SPELL_SEALED_DOOR, worldIn, pos.getX(), pos.getY(), pos.getZ());
				}else{
					return false;
				}
			}
		}

		return false;
	}
//
//	@Override
//	public boolean canBlockStay(World p_149718_1_, int p_149718_2_, int p_149718_3_, int p_149718_4_){
//		return true;
//	}
	
	@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
		//Blank
	}
	
	
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state){
		if (worldIn.isRemote){
			super.breakBlock(worldIn, pos, state);
			return;
		}

		if (worldIn.getBlockState(pos.down()).getBlock() == BlockDefs.spellSealedDoor)
			pos = pos.down();

		TileEntitySpellSealedDoor door = (TileEntitySpellSealedDoor)worldIn.getTileEntity(pos);
		if (door == null) return;
		ItemStack itemstack = door.getStackInSlot(3);
		if (itemstack == null){
			return;
		}
		float f = worldIn.rand.nextFloat() * 0.8F + 0.1F;
		float f1 = worldIn.rand.nextFloat() * 0.8F + 0.1F;
		float f2 = worldIn.rand.nextFloat() * 0.8F + 0.1F;
		do{
			if (itemstack.getCount() <= 0){
				break;
			}
			int i1 = worldIn.rand.nextInt(21) + 10;
			if (i1 > itemstack.getCount()){
				i1 = itemstack.getCount();
			}
			itemstack.shrink(i1);
			ItemStack newItem = new ItemStack(itemstack.getItem(), i1, itemstack.getItemDamage());
			newItem.setTagCompound(itemstack.getTagCompound());
			EntityItem entityitem = new EntityItem(worldIn, pos.getX() + f, pos.getY() + f1, pos.getZ() + f2, newItem);
			float f3 = 0.05F;
			entityitem.motionX = (float)worldIn.rand.nextGaussian() * f3;
			entityitem.motionY = (float)worldIn.rand.nextGaussian() * f3 + 0.2F;
			entityitem.motionZ = (float)worldIn.rand.nextGaussian() * f3;
			worldIn.spawnEntity(entityitem);
		}while (true);

		worldIn.setBlockToAir(pos.up());

		super.breakBlock(worldIn, pos, state);
	}
	
	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player,
			boolean willHarvest){
		if (world.isRemote)
			return false;

		if (world.getBlockState(pos.down()).getBlock() == BlockDefs.spellSealedDoor)
			pos = pos.down();

		IKeystoneLockable<?> lockable = (IKeystoneLockable<?>)world.getTileEntity(pos);

		if (lockable == null)
			return false;

		if (!KeystoneUtilities.instance.canPlayerAccess(lockable, player, KeystoneAccessType.BREAK)) return false;

		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}
	
	@Override
	public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
		if (worldIn.isRemote)
			return;

		if (worldIn.getBlockState(pos.down()).getBlock() == BlockDefs.spellSealedDoor)
			pos = pos.down();

		IKeystoneLockable<?> lockable = (IKeystoneLockable<?>)worldIn.getTileEntity(pos);

		if (lockable == null)
			return;

		if (!KeystoneUtilities.instance.canPlayerAccess(lockable, player, KeystoneAccessType.BREAK))
			return;		super.onBlockHarvested(worldIn, pos, state, player);
	}
	
	
	@Override
	public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
		return new ItemStack(ItemDefs.itemKeystoneDoor, 1, ItemKeystoneDoor.SPELL_SEALED_DOOR);
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return ItemDefs.itemKeystoneDoor;
	}
	
	@Override
	public int damageDropped(IBlockState state) {
		return ItemKeystoneDoor.SPELL_SEALED_DOOR;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int i){
		return new TileEntitySpellSealedDoor();
	}

	public boolean applyComponentToDoor(IBlockAccess access, SpellComponent component, BlockPos pos){

		if (access.getBlockState(pos.down()).getBlock() == BlockDefs.spellSealedDoor)
			pos = pos.down();


		TileEntity te = access.getTileEntity(pos);
		if (te == null || te instanceof TileEntitySpellSealedDoor == false){
			return false;
		}

		((TileEntitySpellSealedDoor)te).addPartToCurrentKey(component);

		return true;
	}
	
	public Block registerAndName(ResourceLocation rl) {
		this.setUnlocalizedName(rl.getResourcePath());
		GameRegistry.register(this, rl);
		return this;
	}
	
	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}

}
