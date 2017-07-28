package am2.items;

import java.util.List;

import am2.buffs.BuffEffectManaRegen;
import am2.defs.ItemDefs;
import am2.extensions.EntityExtension;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemManaPotion extends ItemArsMagica{

	public ItemManaPotion(){
		super();
		this.setMaxStackSize(1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack par1ItemStack){
		return true;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World par2World, EntityPlayer par3EntityPlayer, EnumHand hand){
		EntityExtension props = EntityExtension.For(par3EntityPlayer);
		ItemStack par1ItemStack = par3EntityPlayer.getHeldItem(hand);
		if (props.getCurrentMana() < props.getMaxMana()){
			par3EntityPlayer.setActiveHand(hand);
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, par1ItemStack);
		}
		return new ActionResult<ItemStack>(EnumActionResult.FAIL, par1ItemStack);
	}


	private float getManaRestored(){
		float manaRestored = 0;
		if (this == ItemDefs.lesserManaPotion){
			manaRestored = 100;
		}else if (this == ItemDefs.standardManaPotion){
			manaRestored = 250;
		}else if (this == ItemDefs.greaterManaPotion){
			manaRestored = 2000;
		}else if (this == ItemDefs.epicManaPotion){
			manaRestored = 5000;
		}else if (this == ItemDefs.legendaryManaPotion){
			manaRestored = 10000;
		}

		return manaRestored;
	}

	private int getManaRegenLevel(){
		if (this == ItemDefs.lesserManaPotion){
			return 0;
		}else if (this == ItemDefs.standardManaPotion){
			return 0;
		}else if (this == ItemDefs.greaterManaPotion){
			return 1;
		}else if (this == ItemDefs.epicManaPotion){
			return 1;
		}else if (this == ItemDefs.legendaryManaPotion){
			return 2;
		}
		return 0;
	}

	private int getManaRegenDuration(){
		if (this == ItemDefs.lesserManaPotion){
			return 600;
		}else if (this == ItemDefs.standardManaPotion){
			return 1200;
		}else if (this == ItemDefs.greaterManaPotion){
			return 1800;
		}else if (this == ItemDefs.epicManaPotion){
			return 2400;
		}else if (this == ItemDefs.legendaryManaPotion){
			return 3000;
		}
		return 600;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack par1ItemStack){
		return EnumAction.DRINK;
	}
	
	@Override
	public ItemStack onItemUseFinish(ItemStack par1ItemStack, World par2World, EntityLivingBase par3EntityPlayer){
		par1ItemStack = new ItemStack(Items.GLASS_BOTTLE);
		EntityExtension.For(par3EntityPlayer).setCurrentMana(EntityExtension.For(par3EntityPlayer).getCurrentMana() + getManaRestored());

		if (!par2World.isRemote){
			par3EntityPlayer.addPotionEffect(new BuffEffectManaRegen(getManaRegenDuration(), getManaRegenLevel()));
		}

		return par1ItemStack;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack par1ItemStack){
		return 32;
	}

	@Override
	public boolean getHasSubtypes(){
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List){
		super.getSubItems(par1, par2CreativeTabs, par3List);
	}

}
