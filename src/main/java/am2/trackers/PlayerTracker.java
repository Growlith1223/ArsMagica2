package am2.trackers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

import am2.ArsMagica2;
import am2.armor.ArmorHelper;
import am2.armor.infusions.GenericImbuement;
import am2.enchantments.AMEnchantments;
import am2.extensions.EntityExtension;
import am2.packet.AMDataWriter;
import am2.packet.AMNetHandler;
import am2.proxy.tick.ServerTickHandler;
import am2.utils.EntityUtils;
import am2.utils.WebRequestUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;

import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import scala.actors.threadpool.Arrays;

public class PlayerTracker{

	public static HashMap<UUID, HashMap<Integer, ItemStack>> soulbound_Storage;

	private TreeMap<String, Integer> aals;
	private TreeMap<String, String> clls;
	private TreeMap<String, Integer> cldm;

	public PlayerTracker(){
		soulbound_Storage = new HashMap<UUID, HashMap<Integer, ItemStack>>();

		aals = new TreeMap<String, Integer>();
		clls = new TreeMap<String, String>();
		cldm = new TreeMap<String, Integer>();
	}

	public void postInit(){
		populateAALList();
	}

	@SubscribeEvent
	public void onPlayerLogin(PlayerLoggedInEvent event){
		if (hasAA(event.player)){
			AMNetHandler.INSTANCE.requestClientAuras((EntityPlayerMP)event.player);
		}
		
		ArsMagica2.disabledSkills.getDisabledSkills(true);
		int[] disabledSkills = ArsMagica2.disabledSkills.getDisabledSkillIDs();

		AMDataWriter writer = new AMDataWriter();
		writer.add(ArsMagica2.config.getSkillTreeSecondaryTierCap()).add(disabledSkills);
		writer.add(ArsMagica2.config.getManaCap());
		byte[] data = writer.generate();

		AMNetHandler.INSTANCE.syncLoginData((EntityPlayerMP)event.player, data);
		if (ServerTickHandler.lastWorldName != null)
			AMNetHandler.INSTANCE.syncWorldName((EntityPlayerMP)event.player, ServerTickHandler.lastWorldName);
	}

	@SubscribeEvent
	public void onPlayerLogout(PlayerLoggedOutEvent event){
		//kill any summoned creatures
		if (!event.player.worldObj.isRemote){
			List<Entity> list = event.player.worldObj.loadedEntityList;
			for (Object o : list){
				if (o instanceof EntityLivingBase && EntityUtils.isSummon((EntityLivingBase)o) && EntityUtils.getOwner((EntityLivingBase)o) == event.player.getEntityId()){
					((EntityLivingBase)o).setDead();
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerWake(PlayerWakeUpEvent event){
		if (ArsMagica2.config.getWakingRegen())
		EntityExtension.For(event.getEntityPlayer()).setCurrentMana(EntityExtension.For(event.getEntityPlayer()).getMaxMana());
	}

	@SubscribeEvent
	public void onPlayerRespawn(PlayerRespawnEvent event){
		//================================================================================
		//soulbound items
		//================================================================================
		if (soulbound_Storage.containsKey(event.player.getUniqueID())){
			HashMap<Integer, ItemStack> soulboundItems = soulbound_Storage.get(event.player.getUniqueID());
			for (Integer i : soulboundItems.keySet()){
				if (i < event.player.inventory.getSizeInventory())
					event.player.inventory.setInventorySlotContents(i, soulboundItems.get(i));
				else
					event.player.entityDropItem(soulboundItems.get(i), 0);
			}
		}
		//================================================================================
		//Syncing data.
		ArsMagica2.disabledSkills.getDisabledSkills(true);
		int[] disabledSkills = ArsMagica2.disabledSkills.getDisabledSkillIDs();
		AMDataWriter writer = new AMDataWriter();
		writer.add(ArsMagica2.config.getSkillTreeSecondaryTierCap()).add(disabledSkills);
		writer.add(ArsMagica2.config.getManaCap());
		byte[] data = writer.generate();
		AMNetHandler.INSTANCE.syncLoginData((EntityPlayerMP)event.player, data);
		if (ServerTickHandler.lastWorldName != null)
			AMNetHandler.INSTANCE.syncWorldName((EntityPlayerMP)event.player, ServerTickHandler.lastWorldName);
	}
	
	public void onPlayerDeath(EntityPlayer player){
		storeSoulboundItemsForRespawn(player);
	}

	public static void storeSoulboundItemsForRespawn(EntityPlayer player){
		if (soulbound_Storage.containsKey(player.getUniqueID()))
			soulbound_Storage.remove(player.getUniqueID());

		HashMap<Integer, ItemStack> soulboundItems = new HashMap<Integer, ItemStack>();

		int slotCount = 0;
		for (ItemStack stack : player.inventory.mainInventory){
			int soulbound_level = EnchantmentHelper.getEnchantmentLevel(AMEnchantments.soulbound, stack);
			if (soulbound_level > 0){
				soulboundItems.put(slotCount, stack.copy());
				player.inventory.setInventorySlotContents(slotCount, null);
			}
			slotCount++;
		}
		slotCount = 0;
		for (ItemStack stack : player.inventory.armorInventory){
			int soulbound_level = EnchantmentHelper.getEnchantmentLevel(AMEnchantments.soulbound, stack);
			if (soulbound_level > 0 || ArmorHelper.isInfusionPreset(stack, GenericImbuement.soulbound)){
				soulboundItems.put(slotCount + player.inventory.mainInventory.length, stack.copy());
				player.inventory.setInventorySlotContents(slotCount + player.inventory.mainInventory.length, null);
			}
			slotCount++;
		}

		soulbound_Storage.put(player.getUniqueID(), soulboundItems);
	}

	public static void storeSoulboundItemForRespawn(EntityPlayer player, ItemStack stack){
		if (!soulbound_Storage.containsKey(player.getUniqueID()))
			return;

		HashMap<Integer, ItemStack> soulboundItems = soulbound_Storage.get(player.getUniqueID());

		int slotTest = 0;
		while (soulboundItems.containsKey(slotTest)){
			slotTest++;
			if (slotTest == player.inventory.mainInventory.length)
				slotTest += player.inventory.armorInventory.length;
		}

		soulboundItems.put(slotTest, stack);
	}

	public boolean hasAA(EntityPlayer entity){
		return getAAL(entity) > 0;
	}

	public int getAAL(EntityPlayer thePlayer){
		try{
			thePlayer.getDisplayName();
		}catch (Throwable t){
			return 0;
		}

		if (aals == null || clls == null)
			populateAALList();
		if (aals.containsKey(thePlayer.getDisplayName().getUnformattedText().toLowerCase()))
			return aals.get(thePlayer.getDisplayName().getUnformattedText().toLowerCase());
		return 0;
	}
	
	private ArrayList<String> addContributors(ArrayList<String> lines){
		//Growlith
		lines.add("95ca1edb-b0d6-46a8-825d-2299763f03f0, :AL,3, :CL,http://i.imgur.com/QBCa5O0.png,6,growlith1223");
		//JJT
		lines.add("6b93546d-100a-403d-b352-f2bf75fd3b0c, :AL,3 :CL,http://i.imgur.com/QBCa5O0.png,6,jjtparadox");
		return lines;
	}

	private void populateAALList(){

		aals = new TreeMap<String, Integer>();
		clls = new TreeMap<String, String>();
		cldm = new TreeMap<String, Integer>();

		String dls = "http://qorconcept.com/mc/AREW0152.txt";
		char[] dl = dls.toCharArray();
		
		
		try{
			String s = WebRequestUtils.sendPost(new String(dl), new HashMap<String, String>());
			@SuppressWarnings("unchecked")
			ArrayList<String> lines = new ArrayList<String>(Arrays.asList(s.replace("\r\n", "\n").split("\n")));
			addContributors(lines);
			for (String line : lines){
				
				String[] split = line.split(",");
				for (int i = 1; i < split.length; ++i){
					if (split[i].equals(":AL")){
						try{
							aals.put(split[0].toLowerCase(), Integer.parseInt(split[i+1]));
						}catch(Throwable t){
							
						}
					}else if (split[i].equals(":CL")){
						try{
							clls.put(split[0].toLowerCase(), split[i+1]);
							cldm.put(split[0].toLowerCase(), Integer.parseInt(split[i+2]));
						}catch(Throwable t){
							
						}
					}
				}
			}
		}catch (Throwable t){
			//well, we tried.
		}
	}

	public String getCLF(String uuid){
		return clls.get(uuid.toLowerCase());
	}

	public boolean hasCLS(String uuid){
		return clls.containsKey(uuid.toLowerCase());
	}

	public boolean hasCLDM(String uuid){
		return cldm.containsKey(uuid.toLowerCase());
	}

	public int getCLDM(String uuid){
		return cldm.get(uuid.toLowerCase());
	}
}
