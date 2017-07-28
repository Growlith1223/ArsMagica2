package am2.blocks.tileentity;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.common.collect.Lists;

import am2.api.math.AMVector3;
import am2.defs.ItemDefs;
import am2.entity.EntityShadowHelper;
import am2.power.PowerNodeRegistry;
import am2.power.PowerTypes;
import am2.utils.InventoryUtilities;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class TileEntityOtherworldAura extends TileEntityAMPower{

	private ArrayList<PowerTypes> valid = Lists.newArrayList(
			PowerTypes.DARK,
			PowerTypes.NEUTRAL
	);

	private ArrayList<AMVector3> nearbyInventories;
	private boolean active;
	private TileEntityCraftingAltar watchTarget;
	private int delayCounter; //used as a delay when an item isn't found (prevents oversearching)
	private String placedByUsername = ""; //username of the player who placed the block (used to get the shadow's texture)
	private EntityShadowHelper helper; //the helper entity that actually moves items around

	public TileEntityOtherworldAura(){
		super(200);
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
	public ArrayList<PowerTypes> getValidPowerTypes(){
		return valid;
	}

	public void setActive(boolean active, TileEntityCraftingAltar watchMe){
		if (active && this.active) return;
		this.active = active;
		if (active){
			refreshNearbyInventories();
			watchTarget = watchMe;
		}else{
			nearbyInventories = null;
			watchTarget = null;
			if (this.helper != null){
				this.helper.unSummon();
				this.helper = null;
			}
		}
	}

	private void refreshNearbyInventories(){
		nearbyInventories = new ArrayList<AMVector3>();
		int radius = 6;
		for (int i = -radius; i <= radius; ++i){
			for (int j = -radius; j <= radius; ++j){
				for (int k = -radius; k <= radius; ++k){
					if (i == 0 && j == 0 && k == 0) continue;
					TileEntity te = world.getTileEntity(pos.add(i, j, k));
					if (te == null) continue;
					if (!(te instanceof IInventory)) continue;
					nearbyInventories.add(new AMVector3(te));
				}
			}
		}
	}

	private AMVector3 findInNearbyInventories(ItemStack search){
		Iterator<AMVector3> it = this.nearbyInventories.iterator();
		while (it.hasNext()){
			AMVector3 vec = it.next();
			TileEntity te = world.getTileEntity(vec.toBlockPos());
			if (te == null || !(te instanceof IInventory)){
				//not found, prune list and move on
				it.remove();
				continue;
			}
			IInventory inventory = ((IInventory)te);
			//search from all 6 sides
			for (EnumFacing facing : EnumFacing.values()){
				if (InventoryUtilities.inventoryHasItem(inventory, search, 1, facing)){
					return vec;
				}
			}
		}
		return null;
	}

	private void spawnHelper(){
		if (helper != null || world.isRemote)
			return;

		this.helper = new EntityShadowHelper(world);
		this.helper.setPosition(pos.getX(), pos.getY() + 1, pos.getZ());
		if (this.watchTarget != null)
			this.helper.setAltarTarget(watchTarget);
		this.world.spawnEntity(helper);

		if (this.watchTarget != null){
			this.helper.setDropoffLocation(new AMVector3(watchTarget.getPos().down(2)));
			if (placedByUsername != null && !placedByUsername.isEmpty())
				this.helper.setMimicUser(placedByUsername);
		}
	}

	@Override
	public void update(){
		super.update();

		//sanity check
		if (nearbyInventories == null) return;
		if (watchTarget == null || !watchTarget.isCrafting()){
			setActive(false, null);
			return;
		}

		if (helper != null && helper.isDead){
			return;
		}

		if (!world.isRemote){
			if (PowerNodeRegistry.For(world).checkPower(this, 1.25f)){
				if (delayCounter-- <= 0){
					if (helper == null){
						spawnHelper();
					}

					ItemStack next = watchTarget.getNextPlannedItem();
					if (next.isEmpty()){
						setActive(false, null);
						return;
					}

					if (next.getItem() == ItemDefs.etherium){
						if (!this.helper.hasSearchLocation())
							this.helper.setSearchLocationAndItem(new AMVector3(1, 1, 1), next);
						delayCounter = 100;
					}else{
						AMVector3 location = findInNearbyInventories(next);
						if (location == null){
							//can't find it nearby - don't search again for 5 seconds (gives player time to add)
							delayCounter = 20;
							return;
						}else{
							if (!this.helper.hasSearchLocation())
								this.helper.setSearchLocationAndItem(location, next);
							delayCounter = 20; //delay for 5s (give the helper time to get the item and toss it in)
						}
					}
				}
				PowerNodeRegistry.For(world).consumePower(this, PowerNodeRegistry.For(world).getHighestPowerType(this), 1.25f);
			}else{
				if (this.helper != null)
					this.helper.unSummon();
			}
		}
	}

	public void setPlacedByUsername(String userName){
		this.placedByUsername = userName;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound){
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setString("placedBy", placedByUsername);
		return nbttagcompound;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound){
		super.readFromNBT(nbttagcompound);
		this.placedByUsername = nbttagcompound.getString("placedBy");
	}
}
