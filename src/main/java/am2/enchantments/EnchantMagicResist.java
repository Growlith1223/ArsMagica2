package am2.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public class EnchantMagicResist extends Enchantment{

	public EnchantMagicResist(Rarity rarity){
		super(rarity, EnumEnchantmentType.ARMOR, new EntityEquipmentSlot[] {EntityEquipmentSlot.FEET, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.HEAD});
		setName("magicresist");
	}

	@Override
	public int getMaxLevel(){
		return 5;
	}

	public static float ApplyEnchantment(EntityLivingBase entity, float damage){
		ItemStack[] armorList = null;
		float newDamage = damage;
		int totalMRLevel = 0;
		if (!(entity instanceof EntityPlayer)) {
			return damage;
		}else{
			armorList = ((EntityPlayer)entity).inventory.armorInventory;
		}
		if (armorList.length > 0){
			for (ItemStack armor : armorList){
				totalMRLevel += EnchantmentHelper.getEnchantmentLevel(AMEnchantments.magicResist, armor);
			}
		}
		
		if (totalMRLevel > 0){
			//ewDamage -= damage * totalMRLevel * 0.05f;
			//log scale, it is kind of ridiculous, as 1/4 of the resources reaps more than half the max benefit
			newDamage = (float) (damage - damage * 0.32845873875f * Math.log1p(totalMRLevel));
		}
		
		return newDamage;
	}

}
