package am2;

import java.util.HashMap;

import am2.api.power.IObeliskFuelHelper;
import am2.utils.NBTUtils;
import net.minecraft.item.ItemStack;

public class ObeliskFuelHelper implements IObeliskFuelHelper{
	private HashMap<ItemStack, Integer> validFuels;

	public static final ObeliskFuelHelper instance = new ObeliskFuelHelper();

	private ObeliskFuelHelper(){
		validFuels = new HashMap<ItemStack, Integer>();
	}

	@Override
	public void registerFuelType(ItemStack stack, int burnTime){
		stack.setCount(0);
		validFuels.put(stack.copy(), burnTime);
	}

	@Override
	public int getFuelBurnTime(ItemStack stack){
		if (stack == null)
			return 0;

		for (ItemStack possibleFuel : validFuels.keySet()){
			boolean itemCheck = stack.getItem() == possibleFuel.getItem();
			boolean metaCheck = possibleFuel.getItemDamage() == Short.MAX_VALUE || possibleFuel.getItemDamage() == stack.getItemDamage();
			boolean tagCheck = possibleFuel.getTagCompound() == null ? true : (stack.getTagCompound() == null ? false : NBTUtils.contains(possibleFuel.getTagCompound(), stack.getTagCompound()));
			if (itemCheck && metaCheck && tagCheck)
				return validFuels.get(possibleFuel);
		}
		return 0;
	}
}
