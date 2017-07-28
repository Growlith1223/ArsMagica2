package am2.items;

import am2.defs.CreativeTabsDefs;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;

public class ItemOre extends ItemArsMagica {
	
	public static final String[] names = {
			"vinteum",
			"purified_vinteum",
			"chimerite",
			"moonstone",
			"sunstone",
			"bluetopaz",
			"arcaneash",
			"arcane_compound",
			"animal_fat"};
	public static final int META_VINTEUM = 0;
	public static final int META_PURIFIED_VINTEUM = 1;
	public static final int META_CHIMERITE = 2;
	public static final int META_MOONSTONE = 3;
	public static final int META_SUNSTONE = 4;	
	public static final int META_BLUE_TOPAZ = 5;
	public static final int META_ARCANEASH = 6;
	public static final int META_ARCANECOMPOUND = 7;
	public static final int META_ANIMALFAT = 8;
	
	public ItemOre() {
		 setHasSubtypes(true);
		 setMaxDamage(0);
		 setCreativeTab(CreativeTabsDefs.tabAM2Items);
	}
	
	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> subItems) {
		for (int i = 0; i < names.length; i++) {
			subItems.add(new ItemStack(this, 1, i));
		}
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return "item.arsmagica2:ore." + names[MathHelper.clamp(stack.getItemDamage(), 0, names.length - 1)];
	}
}
