package am2.items;

import am2.blocks.BlockCrystalMarker;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;

@SuppressWarnings("deprecation")
public class ItemBlockCrystalMarker extends ItemBlockSubtypes {

	public ItemBlockCrystalMarker(Block block) {
		super(block);
	}
	
	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		return I18n.translateToLocal("tile.arsmagica2:" + BlockCrystalMarker.crystalMarkerTypes[MathHelper.clamp(stack.getItemDamage(), 0, BlockCrystalMarker.crystalMarkerTypes.length - 1)].toLowerCase() + ".name");
	}
}
