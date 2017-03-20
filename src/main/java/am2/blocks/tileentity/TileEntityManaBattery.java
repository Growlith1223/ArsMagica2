package am2.blocks.tileentity;

import am2.power.PowerNodeRegistry;
import am2.power.PowerTypes;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;

import java.util.List;

public class TileEntityManaBattery extends TileEntityAMPower implements ITileEntityAMBase {

	private boolean active;
	public static int storageCapacity = 250000;
	private PowerTypes outputPowerType = PowerTypes.NONE;
	private int tickCounter = 0;
	boolean hasUpdated = false;
	int prevEnergy;

	public TileEntityManaBattery(){
		super(storageCapacity);
		active = false;
	}

	public PowerTypes getPowerType(){
		return outputPowerType;
	}

	public void setPowerType(PowerTypes type, boolean forceSubNodes){
		this.outputPowerType = type;
		if (worldObj != null && worldObj.isRemote) {
			markDirty();
		}
	}

	public void setActive(boolean active){
		this.active = active;
	}

	@Override
	public boolean canProvidePower(PowerTypes type){
		return true;
	}

	@Override
	public void update(){

		if (worldObj.isBlockIndirectlyGettingPowered(pos) > 0){
			this.setPowerRequests();
		}else{
			this.setNoPowerRequests();
		}

		if (!this.worldObj.isRemote) {
			PowerTypes highest = PowerNodeRegistry.For(worldObj).getHighestPowerType(this);
			float amt = PowerNodeRegistry.For(worldObj).getPower(this, highest);
			if (amt > 0) {
				if(this.outputPowerType != highest) {
					this.outputPowerType = highest;
					this.tickCounter = 0;
				}
			} else {
				if(this.outputPowerType != PowerTypes.NONE) {
					this.outputPowerType = PowerTypes.NONE;
					this.tickCounter = 0;
				}
			}
		}
		this.markDirty();
		this.getWorld().setBlockState(getPos(), this.getWorld().getBlockState(getPos()), 3);
		this.getWorld().notifyBlockOfStateChange(getPos(), getBlockType());
//		if(this.tickCounter == 10) {
//			this.tickCounter++;
//			getWorld().notifyBlockUpdate(getPos(), getWorld().getBlockState(getPos()), getWorld().getBlockState(getPos()), 3);
//		} else{
//			if(this.tickCounter < 10)
//				this.tickCounter++;
//		}
		super.update();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound){
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setBoolean("isActive", active);
		nbttagcompound.setInteger("outputType", outputPowerType.ID());
		return nbttagcompound;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound){
		super.readFromNBT(nbttagcompound);
		active = nbttagcompound.getBoolean("isActive");
		if (nbttagcompound.hasKey("outputType"))
			outputPowerType = PowerTypes.getByID(nbttagcompound.getInteger("outputType"));
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket(){
		return new SPacketUpdateTileEntity(getPos(), getBlockMetadata(), getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
		this.readFromNBT(pkt.getNbtCompound());
	}

	@Override
	public int getChargeRate(){
		return 1000;
	}

	@Override
	public List<PowerTypes> getValidPowerTypes(){
		if (this.outputPowerType == PowerTypes.NONE)
			return PowerTypes.all();
		return Lists.newArrayList(outputPowerType);
	}

	@Override
	public boolean canRelayPower(PowerTypes type){
		return false;
	}

	public boolean dirty = false;

	@Override
	public void markForUpdate() {
		this.dirty = true;
	}

	@Override
	public boolean needsUpdate() {
		return this.dirty;
	}

	@Override
	public void clean() {
		this.dirty = false;
	}

	@Override
	public void markDirty() {
		this.markForUpdate();
		super.markDirty();
	}
}
