package am2.lore;

import java.util.ArrayList;

import am2.api.compendium.CompendiumCategory;
import am2.api.compendium.CompendiumEntry;
import am2.api.extensions.IArcaneCompendium;
import am2.defs.ItemDefs;
import am2.packet.AMDataReader;
import am2.packet.AMDataWriter;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.stats.Achievement;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class ArcaneCompendium implements IArcaneCompendium, ICapabilityProvider, ICapabilitySerializable<NBTBase> {
	
	@CapabilityInject(IArcaneCompendium.class)
	public static Capability<IArcaneCompendium> INSTANCE = null;
	
	public static Achievement compendiumData = (new Achievement("am2_ach_data", "compendiumData", 0, 0, ItemDefs.arcaneCompendium, null));
	public static Achievement componentUnlock = (new Achievement("am2_ach_unlock", "componentUnlock", 0, 0, ItemDefs.spellParchment, null));
	
	public static final int SYNC_COMPENDIUM = 0x1;
	
	private EntityPlayer player;
	private String path = "";
	private int syncCode = 0;
	
	private ArrayList<String> compendium;
	
	public ArcaneCompendium() {
		compendium = new ArrayList<>();
	}
	
	public void unlockEntry(String name) {
		compendium.add(name);
		syncCode |= SYNC_COMPENDIUM;
	}
	
	public boolean isUnlocked(String name) {
		for (String str : compendium) {
			if (str.equalsIgnoreCase(name))
				return true;
		}
		return false;
	}
	
	public void init(EntityPlayer player) {
		this.player = player;
	}

	public static IArcaneCompendium For(EntityPlayer entityPlayer) {
		return entityPlayer.getCapability(INSTANCE, null);
	}

	@Override
	public boolean isNew(String id) {
		return false;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == INSTANCE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == INSTANCE)
			return (T) this;
		return null;
	}

	@Override
	public NBTBase serializeNBT() {
		return new IArcaneCompendium.Storage().writeNBT(INSTANCE, this, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt) {
		new IArcaneCompendium.Storage().readNBT(INSTANCE, this, null, nbt);
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public void setPath(String str) {
		this.path = str;
	}

	@Override
	public void unlockRelatedItems(ItemStack crafting) {
		for (CompendiumEntry entry : CompendiumCategory.getAllEntries()) {
			Object obj = entry.getRenderObject();
			if (obj == null)
				continue;
			else if (obj instanceof ItemStack && ((ItemStack)obj).isItemEqual(crafting))
				unlockEntry(entry.getID());
			else if (obj instanceof Item && crafting.getItem() == obj)
				unlockEntry(entry.getID());
			else if (obj instanceof Block && crafting.getItem() instanceof ItemBlock && ((ItemBlock)crafting.getItem()).block == obj)
				unlockEntry(entry.getID());
		}
	}

	@Override
	public ArrayList<CompendiumEntry> getEntriesForCategory(String categoryName) {
		return null;
	}

	@Override
	public boolean shouldUpdate() {
		return syncCode != 0;
	}

	@Override
	public byte[] generateUpdatePacket() {
		AMDataWriter writer = new AMDataWriter();
		writer.add(syncCode);
		if ((syncCode & SYNC_COMPENDIUM) == SYNC_COMPENDIUM) {
			writer.add(compendium.size());
			for (String entry : compendium)
				writer.add(entry);
		}
		syncCode = 0;
		return writer.generate();
	}

	@Override
	public void handleUpdatePacket(byte[] bytes) {
		AMDataReader reader = new AMDataReader(bytes, false);
		int syncCode = reader.getInt();
		if ((syncCode & SYNC_COMPENDIUM) == SYNC_COMPENDIUM) {
			compendium.clear();
			int size = reader.getInt();
			for (int i = 0; i < size; i++) {
				compendium.add(reader.getString());
			}
		}
	}

	@Override
	public void forceUpdate() {
		this.syncCode = 0xFFFFFFFF;
	}
}
