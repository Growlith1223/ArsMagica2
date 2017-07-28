package am2.items.colorizers;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class SpellBookColorizer implements IItemColor {

	@Override
	public int getColorFromItemstack(ItemStack stack, int tintIndex) {
		if (tintIndex == 0) {
			int meta = MathHelper.clamp(stack.getItemDamage(), 0, 15);
			if (meta == 0) return EnumDyeColor.BROWN.getMapColor().colorValue;
			if (meta == 1) return EnumDyeColor.CYAN.getMapColor().colorValue;
			if (meta == 2) return EnumDyeColor.GRAY.getMapColor().colorValue;
			if (meta == 3) return EnumDyeColor.LIGHT_BLUE.getMapColor().colorValue;
			if (meta == 4) return EnumDyeColor.WHITE.getMapColor().colorValue;
			if (meta == 5) return EnumDyeColor.BLACK.getMapColor().colorValue;
			if (meta == 6) return EnumDyeColor.ORANGE.getMapColor().colorValue;
			if (meta == 7) return EnumDyeColor.PURPLE.getMapColor().colorValue;
			if (meta == 8) return EnumDyeColor.BLUE.getMapColor().colorValue;
			if (meta == 9) return EnumDyeColor.GREEN.getMapColor().colorValue;
			if (meta == 10) return EnumDyeColor.YELLOW.getMapColor().colorValue;
			if (meta == 11) return EnumDyeColor.RED.getMapColor().colorValue;
			if (meta == 12) return EnumDyeColor.LIME.getMapColor().colorValue;
			if (meta == 13) return EnumDyeColor.PINK.getMapColor().colorValue;
			if (meta == 14) return EnumDyeColor.MAGENTA.getMapColor().colorValue;
			if (meta == 15) return EnumDyeColor.SILVER.getMapColor().colorValue;
		}
		return 0xffffff;
	}

}
