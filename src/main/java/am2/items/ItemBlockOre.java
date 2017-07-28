package am2.items;

import am2.blocks.BlockArsMagicaOre.EnumOreType;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;

@SuppressWarnings("deprecation")
public class ItemBlockOre extends ItemBlockSubtypes {

	public ItemBlockOre(Block block) {
		super(block);
	}
	
	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		return I18n.translateToLocal("tile.arsmagica2:ore_" + EnumOreType.values()[MathHelper.clamp(stack.getItemDamage(), 0, EnumOreType.values().length - 1)].getName().toLowerCase() + ".name");
	}
}
