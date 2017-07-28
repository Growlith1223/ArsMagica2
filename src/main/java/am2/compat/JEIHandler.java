package am2.compat;

import am2.api.recipes.RecipesEssenceRefiner;
import am2.compat.jei.EssenceRefinerRecipeCategory;
import am2.compat.jei.EssenceRefinerRecipeHandler;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;

@JEIPlugin
public class JEIHandler implements IModPlugin{

	public void register(IModRegistry registry) {
		registry.addRecipeCategories(new EssenceRefinerRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeHandlers(new EssenceRefinerRecipeHandler());
		registry.addRecipes(RecipesEssenceRefiner.essenceRefinement().getAllRecipes());
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void registerIngredients(IModIngredientRegistration registry) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {

	}

}
