package am2.items;

import am2.buffs.BuffMaxManaIncrease;
import am2.defs.PotionEffectsDefs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemLiquidEssenceBottle extends ItemArsMagica{

	public ItemLiquidEssenceBottle(){
		super();
		this.setMaxStackSize(1);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
		if (!playerIn.isPotionActive(PotionEffectsDefs.manaBoost))
			playerIn.setActiveHand(hand);
		return super.onItemRightClick(worldIn, playerIn, hand);
	}

	@Override
	public EnumAction getItemUseAction(ItemStack par1ItemStack){
		return EnumAction.DRINK;
	}
	
	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving){
		stack = new ItemStack(Items.GLASS_BOTTLE);
		entityLiving.addPotionEffect(new BuffMaxManaIncrease(6000, 1)); //5 mins
		return stack;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack par1ItemStack){
		return 32;
	}
}
