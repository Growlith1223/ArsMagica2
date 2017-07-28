package am2.items;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ItemRune extends ItemArsMagica {

	public ItemRune() {
	}
	
	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> subItems) {
		for (int i = 0; i < 16; i++) {
			subItems.add(new ItemStack(itemIn, 1, i));
		}
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return "item.arsmagica2:rune." + EnumDyeColor.byDyeDamage(stack.getItemDamage()).getUnlocalizedName();
	}
	
	public int getKeyIndex(ItemStack stack) {
		return getKeyIndex(stack.getItemDamage());
	}
	public int getKeyIndex(int meta){
		if (meta == EnumDyeColor.BLACK.getDyeDamage())
			return 0x1;
		if (meta == EnumDyeColor.BLUE.getDyeDamage())
			return 0x2;
		if (meta == EnumDyeColor.BROWN.getDyeDamage())
			return 0x4;
		if (meta == EnumDyeColor.CYAN.getDyeDamage())
			return 0x8;
		if (meta == EnumDyeColor.GRAY.getDyeDamage())
			return 0x10;
		if (meta == EnumDyeColor.GREEN.getDyeDamage())
			return 0x20;
		if (meta == EnumDyeColor.LIGHT_BLUE.getDyeDamage())
			return 0x40;
		if (meta == EnumDyeColor.SILVER.getDyeDamage())
			return 0x80;
		if (meta == EnumDyeColor.LIME.getDyeDamage())
			return 0x100;
		if (meta == EnumDyeColor.MAGENTA.getDyeDamage())
			return 0x200;
		if (meta == EnumDyeColor.ORANGE.getDyeDamage())
			return 0x400;
		if (meta == EnumDyeColor.PINK.getDyeDamage())
			return 0x800;
		if (meta == EnumDyeColor.PURPLE.getDyeDamage())
			return 0x1000;
		if (meta == EnumDyeColor.RED.getDyeDamage())
			return 0x2000;
		if (meta == EnumDyeColor.WHITE.getDyeDamage())
			return 0x4000;
		if (meta == EnumDyeColor.YELLOW.getDyeDamage())
			return 0x8000;
		return 0;
	}
}
