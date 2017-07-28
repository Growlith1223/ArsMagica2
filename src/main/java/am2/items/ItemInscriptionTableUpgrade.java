package am2.items;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.translation.I18n;

@SuppressWarnings("deprecation")
public class ItemInscriptionTableUpgrade extends ItemArsMagica{

	public ItemInscriptionTableUpgrade(){
		super();
		setMaxDamage(0);
		setMaxStackSize(1);
		this.setHasSubtypes(true);
	}
	@Override
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List){
		for (int i = 0; i < 3; ++i){
			par3List.add(new ItemStack(this, 1, i));
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack){
		int meta = stack.getItemDamage();
		switch (meta){
		case 2:
			return I18n.translateToLocal("item.arsmagica2:inscup_3.name");
		case 1:
			return I18n.translateToLocal("item.arsmagica2:inscup_2.name");
		case 0:
		default:
			return I18n.translateToLocal("item.arsmagica2:inscup_1.name");
		}
	}
}
