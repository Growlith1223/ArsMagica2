package am2.utils;

import com.mojang.authlib.GameProfile;

import am2.LogHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class SelectionUtils {
	
	public static int[] getRuneSet(EntityPlayer player) {
		long key = player.getUniqueID().getMostSignificantBits();
		return getRunesFromKey(key);
	}
	
	public static int[] getRunesFromKey(long key) {
		int size = 4;
		long checkKey = 0xffff;
		boolean[] used = new boolean[16];
		int[] ids = new int[size];
		for (int i = 0; i < size; i++) {
			long rune = key >> i*16;
			rune &= checkKey;
			if ((rune & 0x1L) == 0x1L && !used[0]) {
				ids[i] = EnumDyeColor.BLACK.getDyeDamage();
				used[0] = true;
			} else if ((rune & 0x2L) == 0x2L && !used[1]) {
				ids[i] = EnumDyeColor.BLUE.getDyeDamage();
				used[1] = true;
			} else if ((rune & 0x4L) == 0x4L && !used[2]) {
				ids[i] = EnumDyeColor.BROWN.getDyeDamage();
				used[2] = true;
			} else if ((rune & 0x8L) == 0x8L && !used[3]) {
				ids[i] = EnumDyeColor.CYAN.getDyeDamage();
				used[3] = true;
			} else if ((rune & 0x10L) == 0x10L && !used[4]) {
				ids[i] = EnumDyeColor.GRAY.getDyeDamage();
				used[4] = true;
			} else if ((rune & 0x20L) == 0x20L && !used[5]) {
				ids[i] = EnumDyeColor.GREEN.getDyeDamage();
				used[5] = true;
			} else if ((rune & 0x40L) == 0x40L && !used[6]) {
				ids[i] = EnumDyeColor.LIGHT_BLUE.getDyeDamage();
				used[6] = true;
			} else if ((rune & 0x80L) == 0x80L && !used[7]) {
				ids[i] = EnumDyeColor.SILVER.getDyeDamage();
				used[7] = true;
			} else if ((rune & 0x100L) == 0x100L && !used[8]) {
				ids[i] = EnumDyeColor.LIME.getDyeDamage();
				used[8] = true;
			} else if ((rune & 0x200L) == 0x200L && !used[9]) {
				ids[i] = EnumDyeColor.MAGENTA.getDyeDamage();
				used[9] = true;
			} else if ((rune & 0x400L) == 0x400L && !used[10]) {
				ids[i] = EnumDyeColor.ORANGE.getDyeDamage();
				used[10] = true;
			} else if ((rune & 0x800L) == 0x800L && !used[11]) {
				ids[i] = EnumDyeColor.PINK.getDyeDamage();
				used[11] = true;
			} else if ((rune & 0x1000L) == 0x1000L && !used[12]) {
				ids[i] = EnumDyeColor.PURPLE.getDyeDamage();
				used[12] = true;
			} else if ((rune & 0x2000L) == 0x2000L && !used[13]) {
				ids[i] = EnumDyeColor.RED.getDyeDamage();
				used[13] = true;
			} else if ((rune & 0x4000L) == 0x4000L && !used[14]) {
				ids[i] = EnumDyeColor.WHITE.getDyeDamage();
				used[14] = true;
			} else if ((rune & 0x8000L) == 0x8000L && !used[15]) {
				ids[i] = EnumDyeColor.YELLOW.getDyeDamage();
				used[15] = true;
			}
		}
		return ids;
	}
	
	public static EntityPlayer getPlayersForRuneSet (int[] runes) {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		GameProfile[] profiles = server.getPlayerList().getOnlinePlayerProfiles();
		if (runes.length != 4)
			return null;
		for (GameProfile profile : profiles) {
			EntityPlayer player = server.getEntityWorld().getPlayerEntityByUUID(profile.getId());
			boolean match = true;
			for (int i = 0; i < 4; i++) {
				if (getRuneSet(player)[i] != runes[i])
					match = false;
			}
			if (match)
				return player;
			if (player == null) {
				LogHelper.error("Missing player " + profile.getName());
				continue;
			}
			
		}
		return null;
	}
}
