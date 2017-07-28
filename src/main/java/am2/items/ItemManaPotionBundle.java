package am2.items;

import java.util.List;

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
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("deprecation")
public class ItemManaPotionBundle extends ItemArsMagica{
	public ItemManaPotionBundle(){
		super();
		this.setMaxDamage(0);
		this.setMaxStackSize(1);
	}

	private Item getPotion(int damage){
		int id = damage >> 8;
		switch (id){
		case 0:
			return ItemDefs.lesserManaPotion;
		case 1:
			return ItemDefs.standardManaPotion;
		case 2:
			return ItemDefs.greaterManaPotion;
		case 3:
			return ItemDefs.epicManaPotion;
		case 4:
			return ItemDefs.legendaryManaPotion;
		}
		return ItemDefs.lesserManaPotion;
	}

	private int getUses(int damage){
		return (damage & 0x0F);
	}

	@Override
	public int getMaxItemUseDuration(ItemStack par1ItemStack){
		return 32;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack par1ItemStack){
		return EnumAction.DRINK;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World par2World, EntityPlayer par3EntityPlayer, EnumHand hand){
		ItemStack par1ItemStack = par3EntityPlayer.getHeldItem(hand);
		EntityExtension props = EntityExtension.For(par3EntityPlayer);
		if (props.getCurrentMana() < props.getMaxMana()){
			par3EntityPlayer.setActiveHand(hand);
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, par1ItemStack);
		}
		return new ActionResult<ItemStack>(EnumActionResult.FAIL, par1ItemStack);
	}


	@Override
	public ItemStack onItemUseFinish(ItemStack par1ItemStack, World par2World, EntityLivingBase ent){
		if (!(ent instanceof EntityPlayer)) return super.onItemUseFinish(par1ItemStack, par2World, ent);
		EntityPlayer par3EntityPlayer = (EntityPlayer)ent;
		Item potion = getPotion(par1ItemStack.getItemDamage());
		if (potion == ItemDefs.lesserManaPotion){
			ItemDefs.lesserManaPotion.onItemUseFinish(par1ItemStack, par2World, par3EntityPlayer);
		}else if (potion == ItemDefs.standardManaPotion){
			ItemDefs.standardManaPotion.onItemUseFinish(par1ItemStack, par2World, par3EntityPlayer);
		}else if (potion == ItemDefs.greaterManaPotion){
			ItemDefs.greaterManaPotion.onItemUseFinish(par1ItemStack, par2World, par3EntityPlayer);
		}else if (potion == ItemDefs.epicManaPotion){
			ItemDefs.epicManaPotion.onItemUseFinish(par1ItemStack, par2World, par3EntityPlayer);
		}else if (potion == ItemDefs.legendaryManaPotion){
			ItemDefs.legendaryManaPotion.onItemUseFinish(par1ItemStack, par2World, par3EntityPlayer);
		}

		par1ItemStack.setItemDamage(((par1ItemStack.getItemDamage() >> 8) << 8) + getUses(par1ItemStack.getItemDamage()) - 1);

		if (getUses(par1ItemStack.getItemDamage()) == 0){
			giveOrDropItem(par3EntityPlayer, new ItemStack(Items.STRING));
			par1ItemStack.shrink(1);
			if (par1ItemStack.isEmpty()){
				par1ItemStack = null;
			}
		}

		giveOrDropItem(par3EntityPlayer, new ItemStack(Items.GLASS_BOTTLE));

		return par1ItemStack;
	}

	private void giveOrDropItem(EntityPlayer player, ItemStack stack){
		if (!player.inventory.addItemStackToInventory(stack))
			player.dropItem(stack, true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List<String> par3List, boolean par4){
		Item potion = getPotion(par1ItemStack.getItemDamage());
		if (potion == ItemDefs.lesserManaPotion){
			par3List.add("Lesser Mana Restoration");
		}else if (potion == ItemDefs.standardManaPotion){
			par3List.add("Standard Mana Restoration");
		}else if (potion == ItemDefs.greaterManaPotion){
			par3List.add("Greater Mana Restoration");
		}else if (potion == ItemDefs.epicManaPotion){
			par3List.add("Epic Mana Restoration");
		}else if (potion == ItemDefs.legendaryManaPotion){
			par3List.add("Legendary Mana Restoration");
		}
		par3List.add("" + getUses(par1ItemStack.getItemDamage()) + " " + I18n.translateToLocal("am2.tooltip.uses") + ".");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List){
		par3List.add(new ItemStack(ItemDefs.manaPotionBundle, 1, (0 << 8) + 3));
		par3List.add(new ItemStack(ItemDefs.manaPotionBundle, 1, (1 << 8) + 3));
		par3List.add(new ItemStack(ItemDefs.manaPotionBundle, 1, (2 << 8) + 3));
		par3List.add(new ItemStack(ItemDefs.manaPotionBundle, 1, (3 << 8) + 3));
		par3List.add(new ItemStack(ItemDefs.manaPotionBundle, 1, (4 << 8) + 3));
	}

	@Override
	public String getItemStackDisplayName(ItemStack par1ItemStack){
		Item potion = getPotion(par1ItemStack.getItemDamage());
		if (potion == ItemDefs.lesserManaPotion){
			return String.format("%s %s", I18n.translateToLocal("item.arsmagica2:lesser_mana_potion.name"), I18n.translateToLocal("item.arsmagica2:potion_bundle.name"));
		}else if (potion == ItemDefs.standardManaPotion){
			return String.format("%s %s", I18n.translateToLocal("item.arsmagica2:standard_mana_potion.name"), I18n.translateToLocal("item.arsmagica2:potion_bundle.name"));
		}else if (potion == ItemDefs.greaterManaPotion){
			return String.format("%s %s", I18n.translateToLocal("item.arsmagica2:greater_mana_potion.name"), I18n.translateToLocal("item.arsmagica2:potion_bundle.name"));
		}else if (potion == ItemDefs.epicManaPotion){
			return String.format("%s %s", I18n.translateToLocal("item.arsmagica2:epic_mana_potion.name"), I18n.translateToLocal("item.arsmagica2:potion_bundle.name"));
		}else if (potion == ItemDefs.legendaryManaPotion){
			return String.format("%s %s", I18n.translateToLocal("item.arsmagica2:legendary_mana_potion.name"), I18n.translateToLocal("item.arsmagica2:potion_bundle.name"));
		}
		return "? " + I18n.translateToLocal("am2.items.bundle");
	}

	@Override
	public boolean getHasSubtypes(){
		return true;
	}

}
