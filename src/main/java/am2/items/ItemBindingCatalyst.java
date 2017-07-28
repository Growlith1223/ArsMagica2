package am2.items;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("deprecation")
public class ItemBindingCatalyst extends ItemArsMagica{

	public static final int META_PICK = 0;
	public static final int META_AXE = 1;
	public static final int META_SWORD = 2;
	public static final int META_SHOVEL = 3;
	public static final int META_HOE = 4;
	public static final int META_BOW = 5;
	public static final int META_SHIELD = 6;
	public static final int META_BLANK = 7;
	public static final String[] NAMES = {"pick", "axe", "sword", "shovel", "hoe", "bow", "shield"};

	public ItemBindingCatalyst(){
		super();
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
	}
	
	@Override
	public String getItemStackDisplayName(ItemStack stack){
		int meta = stack.getItemDamage();

		String baseName = I18n.translateToLocal("item.arsmagica2:bindingCatalyst.name");

		switch (meta){
		case META_PICK:
			return baseName + I18n.translateToLocal("item.arsmagica2:bindingCatalystPick.name");
		case META_AXE:
			return baseName + I18n.translateToLocal("item.arsmagica2:bindingCatalystAxe.name");
		case META_SWORD:
			return baseName + I18n.translateToLocal("item.arsmagica2:bindingCatalystSword.name");
		case META_SHOVEL:
			return baseName + I18n.translateToLocal("item.arsmagica2:bindingCatalystShovel.name");
		case META_HOE:
			return baseName + I18n.translateToLocal("item.arsmagica2:bindingCatalystHoe.name");
		case META_BOW:
			return baseName + I18n.translateToLocal("item.arsmagica2:bindingCatalystBow.name");
		case META_SHIELD:
			return baseName + I18n.translateToLocal("item.arsmagica2:bindingCatalystShield.name");
		}
		return baseName;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List){
		par3List.add(new ItemStack(par1, 1, META_PICK));
		par3List.add(new ItemStack(par1, 1, META_AXE));
		par3List.add(new ItemStack(par1, 1, META_SHOVEL));
		par3List.add(new ItemStack(par1, 1, META_SWORD));
		par3List.add(new ItemStack(par1, 1, META_HOE));
		par3List.add(new ItemStack(par1, 1, META_BOW));
		par3List.add(new ItemStack(par1, 1, META_SHIELD));
		par3List.add(new ItemStack(par1, 1, 7));
	}

}
