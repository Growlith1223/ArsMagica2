package am2.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.translation.I18n;

@SuppressWarnings("deprecation")
public class ItemCore extends ItemArsMagica {
	
	public static final int META_BASE_CORE = 0;
	public static final int META_HIGH_CORE = 1;
	public static final int META_PURE = 2;
	
	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> subItems) {
		subItems.add(new ItemStack(this, 1, META_BASE_CORE));
		subItems.add(new ItemStack(this, 1, META_HIGH_CORE));
		subItems.add(new ItemStack(this, 1, META_PURE));
	}
	
	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		String name = I18n.translateToLocal("item.arsmagica2:core." + (stack.getItemDamage() == META_BASE_CORE ? "base" : (stack.getItemDamage() == META_HIGH_CORE ? "high" : "pure")) + ".name");
		return name;
	}
	
}
