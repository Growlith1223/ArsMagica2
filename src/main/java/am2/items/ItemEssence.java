package am2.items;

import java.util.List;

import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.translation.I18n;

@SuppressWarnings("deprecation")
public class ItemEssence extends ItemArsMagica {
	
	public ItemEssence() {
		super();
		hasSubtypes = true;
		setMaxDamage(0);
	}
	
	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> subItems) {
		for (int i = 0; i < ArsMagicaAPI.getAffinityRegistry().getValues().size(); i++) {
			if (ArsMagicaAPI.getAffinityRegistry().getValues().get(i).equals(Affinity.NONE))
				continue;
			subItems.add(new ItemStack(this, 1, i));
		}
	}
	
	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		return String.format(I18n.translateToLocal("item.arsmagica2:essence.name"), ArsMagicaAPI.getAffinityRegistry().getObjectById(stack.getItemDamage()).getLocalizedName());
	}
}
