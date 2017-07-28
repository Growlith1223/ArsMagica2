package am2.defs;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class CreativeTabsDefs {
	
	public static final CreativeTabs tabAM2Blocks = new CreativeTabs("am2.blocks") {public ItemStack getTabIconItem() {return new ItemStack(Item.getItemFromBlock(BlockDefs.occulus));}};
	public static final CreativeTabs tabAM2Items = new CreativeTabs("am2.items") {public ItemStack getTabIconItem() {return new ItemStack(ItemDefs.spellParchment);}};
}
