package am2.compat.jei;

import am2.api.recipes.RecipeArsMagica;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class EssenceRefinerRecipeHandler implements IRecipeHandler<RecipeArsMagica> {

	public Class<RecipeArsMagica> getRecipeClass() {
		return RecipeArsMagica.class;
	}

	public String getRecipeCategoryUid() {
		return "am2.essence_refiner";
	}

	public String getRecipeCategoryUid(RecipeArsMagica recipe) {
		return getRecipeCategoryUid();
	}

	
	public IRecipeWrapper getRecipeWrapper(RecipeArsMagica recipe) {
		return new EssenceRefinerRecipeWrapper(recipe);
	}

	public boolean isRecipeValid(RecipeArsMagica recipe) {
		return true;
	}

}
