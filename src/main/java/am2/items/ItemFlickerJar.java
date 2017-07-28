package am2.items;

import java.util.List;

import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import am2.entity.EntityFlicker;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.translation.I18n;

@SuppressWarnings("deprecation")
public class ItemFlickerJar extends ItemArsMagica{

	public ItemFlickerJar(){
		super();
		this.setMaxDamage(0);
		setHasSubtypes(true);
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack){
		int meta = stack.getItemDamage();
		String baseName = I18n.translateToLocal("am2.item.flickerJar");
		if (meta == ArsMagicaAPI.getAffinityRegistry().getId(Affinity.NONE))
			return String.format(I18n.translateToLocal("item.arsmagica2:flickerJar.name"), I18n.translateToLocal("am2.tooltip.empty"));

		Affinity aff = ArsMagicaAPI.getAffinityRegistry().getObjectById(meta);
		baseName = String.format(I18n.translateToLocal("item.arsmagica2:flickerJar.name"), aff.getLocalizedName());

		return baseName;
	}

	public void setFlickerJarTypeFromFlicker(ItemStack stack, EntityFlicker flick){
		stack.setItemDamage(ArsMagicaAPI.getAffinityRegistry().getId(flick.getFlickerAffinity()));
	}

	@Override
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List){
		for (Affinity aff : ArsMagicaAPI.getAffinityRegistry()){
			par3List.add(new ItemStack(this, 1, ArsMagicaAPI.getAffinityRegistry().getId(aff)));
		}
	}
}
