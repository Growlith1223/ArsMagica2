package am2.enchantments;

import java.util.Map;

import am2.ArsMagica2;
import am2.api.enchantment.IAMEnchantmentHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;

public class AMEnchantmentHelper implements IAMEnchantmentHelper{
	@Override
	public Enchantment getSoulbound(){
		return AMEnchantments.soulbound;
	}

	@Override
	public Enchantment getMagicResist(){
		return AMEnchantments.magicResist;
	}

	public static ItemStack soulbindStack(ItemStack stack){
		if (!ArsMagica2.config.getAllowSoulbound())
			return stack;
		stack.addEnchantment(AMEnchantments.soulbound, 1);
		return stack;
	}

	public static ItemStack silkTouchStack(ItemStack stack, int level){
		Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
		if (level > 0){
			map.put(Enchantments.SILK_TOUCH, level);
		}else{
			map.remove(Enchantments.SILK_TOUCH);
		}
		EnchantmentHelper.setEnchantments(map, stack);
		return stack;
	}

	public static ItemStack fortuneStack(ItemStack stack, int level){
		Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
		if (level > 0){
			map.put(Enchantments.FORTUNE, level);
		}else{
			map.remove(Enchantments.SILK_TOUCH);
		}
		EnchantmentHelper.setEnchantments(map, stack);
		return stack;
	}

	public static ItemStack lootingStack(ItemStack stack, int level){
		Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
		if (level > 0){
			map.put(Enchantments.LOOTING, level);
		}else{
			map.remove(Enchantments.SILK_TOUCH);
		}
		EnchantmentHelper.setEnchantments(map, stack);
		return stack;
	}

	public static void copyEnchantments(ItemStack source, ItemStack dest){
		Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(source);
		if (map != null){
			EnchantmentHelper.setEnchantments(map, dest);
		}
	}
}
