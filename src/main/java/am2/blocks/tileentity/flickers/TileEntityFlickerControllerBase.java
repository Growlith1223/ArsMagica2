package am2.blocks.tileentity.flickers;

import java.util.HashMap;
import java.util.List;

import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import am2.api.flickers.IFlickerController;
import am2.api.flickers.AbstractFlickerFunctionality;
import am2.blocks.tileentity.TileEntityAMPower;
import am2.blocks.tileentity.TileEntityFlickerHabitat;
import am2.defs.ItemDefs;
import am2.power.PowerNodeRegistry;
import am2.power.PowerTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.Constants;

public class TileEntityFlickerControllerBase extends TileEntityAMPower implements IFlickerController<TileEntityFlickerControllerBase>{
	private HashMap<Integer, byte[]> sigilMetadata;
	private AbstractFlickerFunctionality operator;
	private int tickCounter;
	Affinity[] nearbyList = new Affinity[6];
	private boolean lastOpWasPowered = false;
	private boolean firstOp = true;

	public TileEntityFlickerControllerBase(){
		super(500);
		sigilMetadata = new HashMap<Integer, byte[]>();
	}

	protected void setOperator(AbstractFlickerFunctionality operator){
		if (this.operator != null){
			this.operator.RemoveOperator(world, this, PowerNodeRegistry.For(world).checkPower(this, this.operator.PowerPerOperation()), nearbyList);
		}
		this.operator = operator;
		tickCounter = 0;
	}

	public void updateOperator(ItemStack stack){
		if (stack == null || stack.getItem() != ItemDefs.flickerFocus)
			return;
		operator = ArsMagicaAPI.getFlickerFocusRegistry().getObjectById(stack.getItemDamage());
	}

	public void scanForNearbyUpgrades(){
		for (EnumFacing direction : EnumFacing.values()){
			TileEntity te = world.getTileEntity(pos.offset(direction));
			if (te != null && te instanceof TileEntityFlickerHabitat){
				nearbyList[direction.ordinal()] = ((TileEntityFlickerHabitat)te).getSelectedAffinity();
			}
		}
	}

	public void notifyOfNearbyUpgradeChange(TileEntity neighbor){
		if (neighbor instanceof TileEntityFlickerHabitat){
			EnumFacing direction = getNeighboringEnumFacing(neighbor);
			if (direction != null){
				nearbyList[direction.ordinal()] = ((TileEntityFlickerHabitat)neighbor).getSelectedAffinity();
			}
		}
	}

	private EnumFacing getNeighboringEnumFacing(TileEntity neighbor){
		if (neighbor.getPos().getX() == this.pos.getX() && neighbor.getPos().getY() == this.pos.getY() && neighbor.getPos().getZ() == this.pos.getZ() + 1)
			return EnumFacing.SOUTH;
		else if (neighbor.getPos().getX() == this.pos.getX() && neighbor.getPos().getY() == this.pos.getY() && neighbor.getPos().getZ() == this.pos.getZ() - 1)
			return EnumFacing.NORTH;
		else if (neighbor.getPos().getX() == this.pos.getX() + 1 && neighbor.getPos().getY() == this.pos.getY() && neighbor.getPos().getZ() == this.pos.getZ())
			return EnumFacing.EAST;
		else if (neighbor.getPos().getX() == this.pos.getX() - 1 && neighbor.getPos().getY() == this.pos.getY() && neighbor.getPos().getZ() == this.pos.getZ())
			return EnumFacing.WEST;
		else if (neighbor.getPos().getX() == this.pos.getX() && neighbor.getPos().getY() == this.pos.getY() + 1 && neighbor.getPos().getZ() == this.pos.getZ())
			return EnumFacing.UP;
		else if (neighbor.getPos().getX() == this.pos.getX() && neighbor.getPos().getY() == this.pos.getY() - 1 && neighbor.getPos().getZ() == this.pos.getZ())
			return EnumFacing.DOWN;

		return null;
	}

	@Override
	public void update(){
		//handle any power update ticks
		super.update();

		//if redstone powered, increment the tick counter (so that operator time still continues), but do nothing else.
		//this allows a redstone signal to effectively turn off any flicker habitat.
		if (world.isBlockIndirectlyGettingPowered(pos) > 0){
			tickCounter++;
			return;
		}

		//tick operator, if it exists
		if (operator != null){
			boolean powered = PowerNodeRegistry.For(world).checkPower(this, operator.PowerPerOperation());

			//check which neighbors are not receiving power
			//this allows individual upgrades to be turned off by providing them with a redstone signal.
			Affinity unpoweredNeighbors[] = getUnpoweredNeighbors();

			if (tickCounter++ >= operator.TimeBetweenOperation(powered, unpoweredNeighbors)){
				tickCounter = 0;
				if ((powered && operator.RequiresPower()) || !operator.RequiresPower()){
					if (firstOp){
						scanForNearbyUpgrades();
						firstOp = false;
					}
					boolean success = operator.DoOperation(world, this, powered, unpoweredNeighbors);
					if (success || operator.RequiresPower())
						PowerNodeRegistry.For(world).consumePower(this, PowerNodeRegistry.For(world).getHighestPowerType(this), operator.PowerPerOperation());
					lastOpWasPowered = true;
				}else if (lastOpWasPowered && operator.RequiresPower() && !powered){
					operator.RemoveOperator(world, this, powered, unpoweredNeighbors);
					lastOpWasPowered = false;
				}
			}
		}
	}

	private Affinity[] getUnpoweredNeighbors(){
		Affinity[] aff = new Affinity[EnumFacing.values().length];
		for (int i = 0; i < nearbyList.length; ++i){
			EnumFacing dir = EnumFacing.values()[i];
			if (nearbyList[i] == null || world.isBlockIndirectlyGettingPowered(pos.offset(dir)) > 0){
				aff[i] = null;
			}else{
				aff[i] = nearbyList[i];
			}
		}
		return aff;
	}

	private Integer getFlagForOperator(AbstractFlickerFunctionality operator){
		return ArsMagicaAPI.getFlickerFocusRegistry().getId(operator);
	}

	public void setMetadata(AbstractFlickerFunctionality operator, byte[] meta){
		sigilMetadata.put(getFlagForOperator(operator), meta);
	}

	public byte[] getMetadata(AbstractFlickerFunctionality operator){
		byte[] arr = sigilMetadata.get(getFlagForOperator(operator));
		return arr != null ? arr : new byte[0];
	}

	public void removeMetadata(AbstractFlickerFunctionality operator){
		sigilMetadata.remove(getFlagForOperator(operator));
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound par1nbtTagCompound){
		super.writeToNBT(par1nbtTagCompound);

		NBTTagList sigilMetaStore = new NBTTagList();
		for (Integer i : sigilMetadata.keySet()){
			NBTTagCompound sigilMetaEntry = new NBTTagCompound();
			sigilMetaEntry.setInteger("sigil_mask", i);
			sigilMetaEntry.setByteArray("sigil_meta", sigilMetadata.get(i));
			sigilMetaStore.appendTag(sigilMetaEntry);
		}

		par1nbtTagCompound.setTag("sigil_metadata_collection", sigilMetaStore);
		return par1nbtTagCompound;
	}

	@Override
	public void readFromNBT(NBTTagCompound par1nbtTagCompound){
		super.readFromNBT(par1nbtTagCompound);

		sigilMetadata = new HashMap<Integer, byte[]>();

		NBTTagList sigilMetaStore = par1nbtTagCompound.getTagList("sigil_metadata_collection", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < sigilMetaStore.tagCount(); ++i){
			NBTTagCompound sigilMetaEntry = (NBTTagCompound)sigilMetaStore.getCompoundTagAt(i);
			Integer mask = sigilMetaEntry.getInteger("sigil_mask");
			byte[] meta = sigilMetaEntry.getByteArray("sigil_meta");
			sigilMetadata.put(mask, meta);
		}
	}

	@Override
	public boolean canProvidePower(PowerTypes type){
		return false;
	}

	@Override
	public boolean canRelayPower(PowerTypes type){
		return false;
	}

	@Override
	public boolean canRequestPower(){
		return true;
	}

	@Override
	public boolean isSource(){
		return false;
	}

	@Override
	public int getChargeRate(){
		return 100;
	}

	@Override
	public List<PowerTypes> getValidPowerTypes(){
		return PowerTypes.all();
	}

	@Override
	public float particleOffset(int axis){
		return 0.5f;
	}

	public Affinity[] getNearbyUpgrades(){
		return this.getUnpoweredNeighbors();
	}
}
