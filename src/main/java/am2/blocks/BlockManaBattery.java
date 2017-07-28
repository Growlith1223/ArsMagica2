package am2.blocks;

import java.util.ArrayList;
import java.util.List;
import am2.ArsMagica2;
import am2.blocks.tileentity.TileEntityManaBattery;
import am2.power.PowerNodeRegistry;
import am2.power.PowerTypes;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockManaBattery extends BlockAMPowered{

	public BlockManaBattery(){
		super(Material.IRON);
		this.setHardness(2.0f);
		this.setResistance(2.0f);
	}
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ){
		if (!super.onBlockActivated(worldIn, pos, state, playerIn, hand, side, hitX, hitY, hitZ))
			return true;

		if (!worldIn.isRemote){
			TileEntityManaBattery te = getTileEntity(worldIn, pos);
			if (te != null){
				if (ArsMagica2.config.colourblindMode()){
					playerIn.sendMessage(new TextComponentString(String.format("Charge Level: %.2f %% [%s]", PowerNodeRegistry.For(worldIn).getPower(te, te.getPowerType()) / te.getCapacity() * 100, getColorNameFromPowerType(te.getPowerType()))));
				}else{
					playerIn.sendMessage(new TextComponentString(String.format("Charge Level: %s%.2f \u00A7f%%", te.getPowerType().getChatColor(), PowerNodeRegistry.For(worldIn).getPower(te, te.getPowerType()) / te.getCapacity() * 100)));
				}
			}
		}

		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int i){
		return new TileEntityManaBattery();
	}

	private TileEntityManaBattery getTileEntity(World world, BlockPos pos){
		TileEntity te = world.getTileEntity(pos);
		if (te != null && te instanceof TileEntityManaBattery){
			return (TileEntityManaBattery)te;
		}
		return null;
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		if (stack != null){
			TileEntityManaBattery te = getTileEntity(worldIn, pos);
			if (stack.getTagCompound() != null){
				if (stack.getTagCompound().hasKey("mana_battery_charge") && stack.getTagCompound().hasKey("mana_battery_powertype"))
					PowerNodeRegistry.For(worldIn).setPower(te, PowerTypes.getByID(stack.getTagCompound().getInteger("mana_battery_powertype")), stack.getTagCompound().getFloat("mana_battery_charge"));
				else
					te.setPowerType(PowerTypes.NONE, false);
			}

		}
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
	}


	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		List<ItemStack> drops = new ArrayList<ItemStack>();
		ItemStack stack = new ItemStack(this, 1);
		drops.add(stack);
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityManaBattery && stack != null){
			if (PowerNodeRegistry.For((World)world).getPower((TileEntityManaBattery)te, ((TileEntityManaBattery) te).getPowerType()) != 0.0F){
				stack.setTagCompound(new NBTTagCompound());
				stack.getTagCompound().setFloat("mana_battery_charge", PowerNodeRegistry.For((World)world).getPower((TileEntityManaBattery)te, ((TileEntityManaBattery) te).getPowerType()));
				stack.getTagCompound().setInteger("mana_battery_powertype", ((TileEntityManaBattery) te).getPowerType().ID());
			}
		}
		return drops;
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
		this.onBlockDestroyedByPlayer(world, pos,state);
		if (willHarvest){
			this.harvestBlock(world, player, pos, state, world.getTileEntity(pos), player.getHeldItemMainhand());
		}
		world.setBlockToAir(pos);

		return false;
	}

	@Override
	public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
		TileEntityManaBattery batt = getTileEntity(worldIn, pos);
		if (batt == null)
			return 0;

		//can simply use getHighest, as batteries can only have *one* type. 
		//the only time they have more, is when they are at zero, but then it doesn't matter
		//as all power types are zero.
		//Once they get power a single time, they lock to that power type.
		float pct = PowerNodeRegistry.For(worldIn).getHighestPower(batt) / batt.getCapacity();

		return (int)Math.floor(15.0f * pct);
	}
	
	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}

	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List){
		ItemStack stack = new ItemStack(this);

		par3List.add(stack);
		for (PowerTypes type : PowerTypes.all()){
			stack = new ItemStack(this, 1, type.ID());
			stack.setTagCompound(new NBTTagCompound());
			stack.getTagCompound().setFloat("mana_battery_charge", new TileEntityManaBattery().getCapacity());
			stack.getTagCompound().setInteger("mana_battery_powertype", type.ID());
			par3List.add(stack);
		}
	}

//	@Override
//	public int colorMultiplier(IBlockAccess blockAccess, int x, int y, int z){
//		TileEntity te = blockAccess.getTileEntity(x, y, z);
//		if (te instanceof TileEntityManaBattery){
//			TileEntityManaBattery battery = (TileEntityManaBattery)te;
//			if (battery.getPowerType() == PowerTypes.DARK)
//				return 0x850e0e;
//			else if (battery.getPowerType() == PowerTypes.LIGHT)
//				return 0x61cfc3;
//			else if (battery.getPowerType() == PowerTypes.NEUTRAL)
//				return 0x2683d2;
//			else
//				return 0xFFFFFF;
//		}
//		return 0xFFFFFF;
//	}
	
	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}
	
	@Override
	public boolean shouldSideBeRendered(IBlockState blockState,
			IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		return true;
	}
	
	@Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }
	
	@Override
	public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return true;
	}
	
	@Override
	public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return true;
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
	public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
		return 0;
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
}
