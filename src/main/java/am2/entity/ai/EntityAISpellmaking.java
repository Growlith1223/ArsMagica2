package am2.entity.ai;

import am2.api.math.AMVector3;
import am2.blocks.tileentity.TileEntityCraftingAltar;
import am2.defs.ItemDefs;
import am2.entity.EntityShadowHelper;
import am2.utils.DummyEntityPlayer;
import am2.utils.InventoryUtilities;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class EntityAISpellmaking extends EntityAIBase{

	private EntityShadowHelper host; //the host for this AI task

	private int action_state = 0;
	private int wait_counter = 0;

	private static final int DISTANCE_THRESHOLD = 4;
	private static final float MOVE_SPEED = 0.5f;
	private static final float WAIT_TIME = 20;

	public EntityAISpellmaking(EntityShadowHelper host){
		this.host = host;
		this.setMutexBits(1);
	}

	@Override
	public boolean shouldExecute(){
		return host.hasSearchLocation();
	}

	@Override
	public void resetTask(){
		action_state = 0;
		wait_counter = 0;
		host.setSearchLocationAndItem(AMVector3.zero(), new ItemStack(Items.PAPER));
	}

	@Override
	public void updateTask(){
		AMVector3 targetLocation = host.getSearchLocation();
		AMVector3 dropLocation = host.getDropLocation();
		AMVector3 hostLocation = new AMVector3(host);

		ItemStack searchItem = host.getSearchItem();
		if (searchItem.getItem() == ItemDefs.etherium){
			TileEntityCraftingAltar altar = host.getAltarTarget();
			if (altar.switchIsOn())
				return;
			BlockPos bc = altar.getSwitchLocation();
			if (action_state == 0 && host.getNavigator().noPath()){
				host.getNavigator().tryMoveToXYZ(altar.getPos().getX() + bc.getX(), altar.getPos().getY() + bc.getY(), altar.getPos().getZ() + bc.getZ(), MOVE_SPEED);
			}else if (action_state == 0 && hostLocation.distanceSqTo(new AMVector3(altar.getPos().getX() + bc.getX(), altar.getPos().getY() + bc.getY(), altar.getPos().getZ() + bc.getZ())) < DISTANCE_THRESHOLD){
				action_state = 1;
				altar.flipSwitch();
				wait_counter = 0;
			}else if (action_state == 1 && wait_counter < WAIT_TIME){
				wait_counter++;
			}else if (action_state == 1 && wait_counter >= WAIT_TIME){
				resetTask();
			}
		}else{
			if (action_state == 0 && host.getNavigator().noPath()){ //no item and too far away to grab			
				host.getNavigator().tryMoveToXYZ(targetLocation.x, targetLocation.y, targetLocation.z, MOVE_SPEED);
			}else if (action_state == 0 && hostLocation.distanceSqTo(targetLocation) < DISTANCE_THRESHOLD){
				host.getNavigator().clearPathEntity();
				TileEntity te = host.world.getTileEntity(targetLocation.toBlockPos());
				if (te == null || !(te instanceof IInventory)){
					resetTask();
					return;
				}
				((IInventory)te).openInventory(new DummyEntityPlayer(host.world));
				if (!host.world.isRemote)
					InventoryUtilities.deductFromInventory(((IInventory)te), host.getSearchItem(), 1);
				host.setHeldItem(host.getSearchItem());
				action_state = 1;
			}else if (action_state == 1 && host.getNavigator().noPath() && wait_counter < WAIT_TIME){
				wait_counter++;
			}else if (action_state == 1 && host.getNavigator().noPath() && wait_counter >= WAIT_TIME){
				wait_counter = 0;
				TileEntity te = host.world.getTileEntity(targetLocation.toBlockPos());
				if (te == null || !(te instanceof IInventory)){
					resetTask();
					return;
				}
				((IInventory)te).closeInventory(new DummyEntityPlayer(host.world));
				host.getNavigator().tryMoveToXYZ(dropLocation.x, dropLocation.y, dropLocation.z, MOVE_SPEED);
			}else if (action_state == 1 && hostLocation.distanceSqTo(dropLocation) < DISTANCE_THRESHOLD){
				host.getNavigator().clearPathEntity();
				if (!host.world.isRemote){
					host.entityDropItem(host.getSearchItem(), 0f);
					host.setHeldItem(new ItemStack(Items.PAPER));
				}
				action_state = 2;
			}else if (action_state == 2 && wait_counter < WAIT_TIME){
				wait_counter++;
			}else if (action_state == 2 && wait_counter >= WAIT_TIME){
				resetTask();
			}
		}
	}

}
