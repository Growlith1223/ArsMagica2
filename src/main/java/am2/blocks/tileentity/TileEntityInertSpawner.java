package am2.blocks.tileentity;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import am2.defs.ItemDefs;
import am2.items.ItemCrystalPhylactery;
import am2.power.PowerNodeRegistry;
import am2.power.PowerTypes;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class TileEntityInertSpawner extends TileEntityAMPower implements ISidedInventory{

	private ItemStack phylactery;
	private float powerConsumed = 0.0f;

	private static final ArrayList<PowerTypes> valid = Lists.newArrayList(PowerTypes.DARK);

	private static final float SUMMON_REQ = 6000;
	
	@Override
	public boolean isEmpty() {
	if (!phylactery.isEmpty())
            {
                return false;
            }
        return true;
	}

	public TileEntityInertSpawner(){
		super(500);
	}

	@Override
	public boolean canRelayPower(PowerTypes type){
		return false;
	}

	@Override
	public int getChargeRate(){
		return 100;
	}

	@Override
	public int getSizeInventory(){
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int i){
		if (i < getSizeInventory() && phylactery != null){
			return phylactery;
		}

		return null;
	}

	@Override
	public ItemStack decrStackSize(int i, int j){
		if (i < getSizeInventory() && phylactery != null){
			ItemStack jar = phylactery;
			phylactery = null;
			return jar;
		}
		return null;
	}

	@Override
	public ItemStack removeStackFromSlot(int i){
		if (i < getSizeInventory() && phylactery != null){
			ItemStack jar = phylactery;
			phylactery = null;
			return jar;
		}
		return null;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack){
		phylactery = itemstack;
		if (itemstack != null && itemstack.getCount() > getInventoryStackLimit()){
			itemstack.setCount(getInventoryStackLimit());
		}

	}

	@Override
	public String getName(){
		return "Inert Spawner";
	}

	@Override
	public int getInventoryStackLimit(){
		return 1;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer entityplayer){
		if (world.getTileEntity(pos) != this){
			return false;
		}

		return entityplayer.getDistanceSqToCenter(pos) <= 64D;
	}

	@Override
	public void openInventory(EntityPlayer player){
	}

	@Override
	public void closeInventory(EntityPlayer player){
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack){
		return i == 0 && stack != null && stack.getItem() == ItemDefs.crystalPhylactery;
	}

	@Override
	public boolean hasCustomName(){
		return false;
	}

	@Override
	public int[] getSlotsForFace(EnumFacing p_94128_1_){
		return new int[]{0};
	}

	@Override
	public boolean canInsertItem(int i, ItemStack stack, EnumFacing face){
		return
				i == 0 &&
						this.getStackInSlot(0) == null &&
						stack != null &&
						stack.getItem() == ItemDefs.crystalPhylactery &&
						stack.getCount() == 1 &&
						((ItemCrystalPhylactery)stack.getItem()).isFull(stack);
	}

	@Override
	public boolean canExtractItem(int p_102008_1_, ItemStack p_102008_2_, EnumFacing p_102008_3_){
		return true;
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket(){
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		return new SPacketUpdateTileEntity(pos, 1, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
		this.readFromNBT(pkt.getNbtCompound());
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound){
		super.writeToNBT(nbttagcompound);

		if (phylactery != null){
			NBTTagCompound phy = new NBTTagCompound();
			phylactery.writeToNBT(phy);
			nbttagcompound.setTag("phylactery", phy);
		}

		nbttagcompound.setFloat("powerConsumed", powerConsumed);
		return nbttagcompound;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound){
		super.readFromNBT(nbttagcompound);


		if (nbttagcompound.hasKey("phylactery")){
			NBTTagCompound phy = nbttagcompound.getCompoundTag("phylactery");
			phylactery = new ItemStack(phy);
		}

		this.powerConsumed = nbttagcompound.getFloat("powerConsumed");
	}

	public void update(){
		super.update();

		if (!world.isRemote && phylactery != null && phylactery.getItem() instanceof ItemCrystalPhylactery && ((ItemCrystalPhylactery)phylactery.getItem()).isFull(phylactery) && world.isBlockIndirectlyGettingPowered(pos) == 0){
			if (this.powerConsumed < TileEntityInertSpawner.SUMMON_REQ){
				this.powerConsumed += PowerNodeRegistry.For(world).consumePower(
						this,
						PowerTypes.DARK,
						Math.min(this.getCapacity(), TileEntityInertSpawner.SUMMON_REQ - this.powerConsumed)
				);
			}else{
				this.powerConsumed = 0;
				ItemCrystalPhylactery item = (ItemCrystalPhylactery)this.phylactery.getItem();
				if (item.isFull(phylactery)){
					Integer entityID = item.getSpawnClass(phylactery);
					if (entityID != null){
						Class<?> clazz = (Class<?>)EntityList.getClassFromID(entityID);
						if (clazz != null){
							EntityLiving entity = null;
							try{
								entity = (EntityLiving)clazz.getConstructor(World.class).newInstance(world);
							}catch (Throwable t){
								t.printStackTrace();
								return;
							}
							if (entity == null)
								return;
							setEntityPosition(entity);
							world.spawnEntity(entity);
						}
					}
				}
			}
		}
	}

	private void setEntityPosition(EntityLiving e){
		for (EnumFacing dir : EnumFacing.values()){
			if (world.isAirBlock(pos.offset(dir))){
				e.setPosition(pos.offset(dir).getX(), pos.offset(dir).getY(), pos.offset(dir).getZ());
				return;
			}
		}
		e.setPosition(pos.getX(), pos.getY(), pos.getZ());
	}

	@Override
	public List<PowerTypes> getValidPowerTypes(){
		return valid;
	}

	@Override
	public int getField(int id) {

		return 0;
	}

	@Override
	public void setField(int id, int value) {

		
	}

	@Override
	public int getFieldCount() {

		return 0;
	}

	@Override
	public void clear() {

		
	}

	@Override
	public ITextComponent getDisplayName() {

		return null;
	}
}
