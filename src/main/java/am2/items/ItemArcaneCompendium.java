package am2.items;

import am2.gui.AMGuiHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemArcaneCompendium extends ItemArsMagica{

	public ItemArcaneCompendium(){
		super();
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		ItemStack item = playerIn.getHeldItem(hand);
		if (worldIn.isRemote){
			AMGuiHelper.OpenCompendiumGui(item);
		}
		return ActionResult.newResult(EnumActionResult.SUCCESS, item);
	}
}
