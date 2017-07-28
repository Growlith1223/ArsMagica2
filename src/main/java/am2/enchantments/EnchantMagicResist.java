package am2.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.math.MathHelper;

public class EnchantMagicResist extends Enchantment{

	public EnchantMagicResist(Rarity rarity){
		super(rarity, EnumEnchantmentType.ARMOR, new EntityEquipmentSlot[] {EntityEquipmentSlot.FEET, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.HEAD});
		setName("magicresist");
	}

	@Override
	public int getMaxLevel(){
		return 5;
	}

	public static int ApplyEnchantment(EntityLivingBase inventory, int damage){
		int maxEnchantLevel = EnchantmentHelper.getMaxEnchantmentLevel(AMEnchantments.magicResist, inventory);

		if (maxEnchantLevel > 0){
			damage -= MathHelper.floor((float)damage * (float)maxEnchantLevel * 0.15F);
		}

		return damage;
	}

}
