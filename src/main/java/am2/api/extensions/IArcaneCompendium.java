package am2.api.extensions;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import am2.api.compendium.CompendiumCategory;
import am2.api.compendium.CompendiumEntry;
import am2.lore.ArcaneCompendium;
import am2.utils.NBTUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public interface IArcaneCompendium {
	
	/**
	 * Unlocks a compendium entry
	 * @param entry : id of the entry
	 */
	public void unlockEntry(String entry);
	
	/**
	 * Unlock related entries to this one
	 * @param crafting
	 */
	public void unlockRelatedItems(ItemStack crafting);
	
	/**
	 * 
	 * @param string
	 * @return true if the entry/category is unlocked
	 */
	public boolean isUnlocked(String string);
	
	public String getPath();
	
	public void setPath(String str);
	
	public boolean shouldUpdate();
	public byte[] generateUpdatePacket();
	public void handleUpdatePacket(byte[] bytes);
	public void forceUpdate();

	
	public static class Storage implements IStorage<IArcaneCompendium> {

		@Override
		public NBTBase writeNBT(Capability<IArcaneCompendium> capability, IArcaneCompendium instance, EnumFacing side) {
			NBTTagCompound compound = new NBTTagCompound();
			NBTTagCompound am2tag = NBTUtils.getAM2Tag(compound);
			NBTTagList unlocks = NBTUtils.addCompoundList(am2tag, "Unlocks");
			for (CompendiumCategory categroy : CompendiumCategory.getCategories()) {
				for (CompendiumEntry entry : categroy.getEntries()) {
					NBTTagCompound tmp = new NBTTagCompound();
					tmp.setString("ID", entry.getID());
					tmp.setBoolean("Unlocked", instance.isUnlocked(entry.getID()));
					unlocks.appendTag(tmp);
				}
			}
			am2tag.setString("Path", instance.getPath());
			return compound;
		}

		@Override
		public void readNBT(Capability<IArcaneCompendium> capability, IArcaneCompendium instance, EnumFacing side, NBTBase nbt) {
			NBTTagCompound am2tag = NBTUtils.getAM2Tag((NBTTagCompound) nbt);			
			NBTTagList unlocks = NBTUtils.addCompoundList(am2tag, "Unlocks");
			for (int i = 0; i < unlocks.tagCount(); i++) {
				NBTTagCompound tmp = unlocks.getCompoundTagAt(i);
				if (tmp.getBoolean("Unlocked")) {
					instance.unlockEntry(tmp.getString("ID"));
				}
			}
			instance.setPath(am2tag.getString("Path"));
		}
	}
	
	public static class Factory implements Callable<IArcaneCompendium> {

		@Override
		public IArcaneCompendium call() throws Exception {
			return new ArcaneCompendium();
		}
		
	}

	public ArrayList<CompendiumEntry> getEntriesForCategory(String categoryName);

	public boolean isNew(String id);

}
