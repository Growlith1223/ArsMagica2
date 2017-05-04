package am2.enchantments;

import am2.ArsMagica2;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public class EnchantmentSoulbound extends Enchantment{

	public EnchantmentSoulbound(Rarity rarityIn) {
		super(rarityIn, EnumEnchantmentType.ALL, new EntityEquipmentSlot[] {EntityEquipmentSlot.FEET, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.HEAD, EntityEquipmentSlot.MAINHAND, EntityEquipmentSlot.OFFHAND});
		setName("soulbound");
	}

	@Override
	public int getMinEnchantability(int par1){
		return 0;
	}

	@Override
	public int getMaxEnchantability(int par1){
		return 50;
	}

	@Override
	public int getMinLevel(){
		return 1;
	}

	@Override
	public int getMaxLevel(){
		return 1;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack){
		if (ArsMagica2.config.getAllowSoulbound())
		return super.canApplyAtEnchantingTable(stack);
		else return false;
	}
}
