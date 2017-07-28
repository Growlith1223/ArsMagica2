package am2.items;

import am2.blocks.BlockIllusionBlock.EnumIllusionType;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;

@SuppressWarnings("deprecation")
public class ItemBlockIllusion extends ItemBlockSubtypes {

	public ItemBlockIllusion(Block block) {
		super(block);
	}
	
	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		return I18n.translateToLocal("tile.arsmagica2:illusion_block_" + EnumIllusionType.values()[MathHelper.clamp(stack.getItemDamage(), 0, EnumIllusionType.values().length - 1)].getName().toLowerCase() + ".name");
	}
}
