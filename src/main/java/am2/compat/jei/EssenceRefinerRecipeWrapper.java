package am2.compat.jei;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import am2.api.recipes.RecipeArsMagica;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class EssenceRefinerRecipeWrapper extends BlankRecipeWrapper {
	
	RecipeArsMagica recipe;
	
	public EssenceRefinerRecipeWrapper(RecipeArsMagica recipe) {
		this.recipe = recipe;
	}
	
	@Override
	public void getIngredients(IIngredients ingredients) {
		List<ItemStack> inputs = new ArrayList<ItemStack>();
		for (ItemStack item : recipe.getRecipeItems()){
			inputs.add(item);
		}
		ingredients.setInputs(ItemStack.class, inputs);
		ingredients.setOutput(ItemStack.class, recipe.getOutput());
	}

}
