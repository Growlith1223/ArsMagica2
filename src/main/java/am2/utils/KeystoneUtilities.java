package am2.utils;

import java.util.ArrayList;

import am2.api.blocks.IKeystoneLockable;
import am2.api.items.KeystoneAccessType;
import am2.defs.ItemDefs;
import am2.extensions.EntityExtension;
import am2.items.ItemKeystone;
import am2.items.ItemRune;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;

@SuppressWarnings("deprecation")
public class KeystoneUtilities {
  
	// constants to pass to the canPlayerAccess() function to say what you're trying to do
	// please use these by reference instead of hard-coded numbers
	public static final int MODE_NONE = 0;
	public static final int MODE_USE = 1;
	public static final int MODE_BREAK = 2;

	public static final KeystoneUtilities instance = new KeystoneUtilities();

	public static boolean HandleKeystoneRecovery(EntityPlayer player, IKeystoneLockable<?> lock){
		if (EntityExtension.For(player).isRecoveringKeystone){
			if (KeystoneUtilities.instance.getKeyFromRunes(lock.getRunesInKey()) != 0){
				String combo = "";
				for (ItemStack rune : lock.getRunesInKey()){
					if (rune == null)
						combo += "empty ";
					else
						combo += rune.getDisplayName() + " ";
				}
				player.sendMessage(new TextComponentString(combo));
			}else{
				player.sendMessage(new TextComponentString(I18n.translateToLocal("am2.tooltip.noKeyPresent")));
			}
			EntityExtension.For(player).isRecoveringKeystone = false;
			return true;
		}else{
			return false;
		}
	}

	public ArrayList<Long> GetKeysInInvenory(EntityLivingBase ent){
		ArrayList<Long> toReturn = new ArrayList<Long>();
		toReturn.add((long)0); //any inventory has the "0", or "unlocked" key

		if (ent instanceof EntityPlayer){
			EntityPlayer p = (EntityPlayer)ent;
			for (ItemStack is : p.inventory.mainInventory){
				if (is == null || !(is.getItem() instanceof ItemKeystone)) continue;

				ItemKeystone keystone = (ItemKeystone)is.getItem();
				long key = keystone.getKey(is);
				if (!toReturn.contains(key)){
					toReturn.add(key);
				}
			}
		}
		return toReturn;
	}

	public long getKeyFromRunes(ItemStack[] runes){
		long key = 0;

		int index = 0;
		for (ItemStack stack : runes){
			if (stack == null || stack.getItem() != ItemDefs.rune) continue;
			long keyIndex = ((ItemRune)stack.getItem()).getKeyIndex(stack);
			key |= (keyIndex << (index * 16));
			index += 1;
		}

		return key;
	}


	public boolean canPlayerAccess(IKeystoneLockable<?> inventory, EntityPlayer player, KeystoneAccessType accessMode){
		ItemStack[] runes = inventory.getRunesInKey();
		long key = getKeyFromRunes(runes);

		if (key == 0) //no key combo set?  No lock!  Access granted!
			return true;

		if (inventory.keystoneMustBeHeld()){
			if (player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() == ItemDefs.keystone){
				return ((ItemKeystone)player.getHeldItemMainhand().getItem()).getKey(player.getHeldItemMainhand()) == key;
			}
		}else if (inventory.keystoneMustBeInActionBar()){
			for (int i = 0; i < 9; ++i){
				ItemStack stack = player.inventory.getStackInSlot(i);
				if (stack == null || stack.getItem() != ItemDefs.keystone) continue;
				if (((ItemKeystone)stack.getItem()).getKey(stack) == key){
					return true;
				}
			}
		}else{
			for (int i = 0; i < player.inventory.mainInventory.size() - 1; ++i){
				ItemStack stack = player.inventory.getStackInSlot(i);
				if (stack == null || stack.getItem() != ItemDefs.keystone) continue;
				if (((ItemKeystone)stack.getItem()).getKey(stack) == key){
					return true;
				}
			}
		}

		if (accessMode == KeystoneAccessType.USE && !player.world.isRemote){
			player.sendMessage(new TextComponentString(I18n.translateToLocal("am2.tooltip.wrongKeystoneUse")));
		}
		else if (accessMode == KeystoneAccessType.BREAK && !player.world.isRemote){
			player.sendMessage(new TextComponentString(I18n.translateToLocal("am2.tooltip.wrongKeystoneBreak")));
		}
		return false;
	}

}
