package am2.items;

import java.util.List;

import am2.api.ArsMagicaAPI;
import am2.api.flickers.AbstractFlickerFunctionality;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.translation.I18n;

@SuppressWarnings("deprecation")
public class ItemFlickerFocus extends ItemArsMagica{

	public ItemFlickerFocus(){
		super();
		setHasSubtypes(true);
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack){
		int meta = stack.getItemDamage();
		AbstractFlickerFunctionality operator = ArsMagicaAPI.getFlickerFocusRegistry().getObjectById(meta);
		if (operator == null)
			return "Trash";
		return String.format(I18n.translateToLocal("item.arsmagica2:FlickerFocusPrefix"), I18n.translateToLocal("item.arsmagica2:" + operator.getClass().getSimpleName() + ".name"));
	}

	@Override
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List){
		for (AbstractFlickerFunctionality func : ArsMagicaAPI.getFlickerFocusRegistry().getValues()){
			par3List.add(new ItemStack(this, 1, ArsMagicaAPI.getFlickerFocusRegistry().getId(func)));
		}
	}
}
