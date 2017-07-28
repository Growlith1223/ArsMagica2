package am2.api.extensions;

import java.util.concurrent.Callable;

import am2.extensions.RiftStorage;
import am2.utils.NBTUtils;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public interface IRiftStorage extends IInventory {
	
	
	public static class Storage implements IStorage<IRiftStorage> {
		
		@Override
		public NBTBase writeNBT(Capability<IRiftStorage> capability, IRiftStorage instance, EnumFacing side) {
			NBTTagCompound nbt = new NBTTagCompound();
			NBTTagCompound am2Tag = NBTUtils.getAM2Tag(nbt);
			NBTTagList list = NBTUtils.addCompoundList(am2Tag, "RiftInventory");
			for (int i = 0; i < instance.getSizeInventory(); i++) {
				if (instance.getStackInSlot(i) == null)
					continue;
				NBTTagCompound tmp = new NBTTagCompound();
				instance.getStackInSlot(i).writeToNBT(tmp);
				tmp.setInteger("Slot", i);
				list.appendTag(tmp);
			}
			return nbt;
		}

		@Override
		public void readNBT(Capability<IRiftStorage> capability, IRiftStorage instance, EnumFacing side, NBTBase nbt) {
			NBTTagCompound am2Tag = NBTUtils.getAM2Tag((NBTTagCompound) nbt);
			NBTTagList list = NBTUtils.addCompoundList(am2Tag, "RiftInventory");
			for (int i = 0; i < list.tagCount(); i++) {
				//LogHelper.info("Found a tag ");
				NBTTagCompound compound = list.getCompoundTagAt(i);
				instance.setInventorySlotContents(compound.getInteger("Slot"),new ItemStack(compound));
			}
		}
	}
	
	public static class Factory implements Callable<IRiftStorage> {
		@Override
		public IRiftStorage call() throws Exception {
			return new RiftStorage();
		}
	}

	int getAccessLevel();

	void setAccessLevel(int accessLevel);
	
}
