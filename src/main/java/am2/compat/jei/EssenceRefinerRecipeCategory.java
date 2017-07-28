package am2.compat.jei;

import java.util.Collections;
import java.util.List;

import am2.ArsMagica2;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;

@SuppressWarnings("deprecation")
public class EssenceRefinerRecipeCategory implements IRecipeCategory<EssenceRefinerRecipeWrapper> {

	IDrawableStatic background;
	private final String name;
	private IGuiHelper helper;
	
	public EssenceRefinerRecipeCategory(IGuiHelper helpers) {
		this.helper = helpers;
		this.name = I18n.translateToLocal("am2.jei.recipe.refiner");
		this.background = helpers.createDrawable(new ResourceLocation("arsmagica2:textures/gui/essence_extractor_gui.png"), 3, 25, 170, 114);
	}

	@Override
	public String getTitle() {
		return name;
	}

	@Override
	public String getUid() {
		return "am2.essence_refiner";
	}
	
	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void drawExtras(Minecraft minecraft) {
		
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, EssenceRefinerRecipeWrapper recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup stacks = recipeLayout.getItemStacks();
		
		stacks.init(0, true, 76, 16);
		stacks.init(1, true, 44, 48);
		stacks.init(2, true, 76, 48);
		stacks.init(3, true, 108, 48);
		stacks.init(4, true, 76, 81);
		stacks.init(5, false, 139, 84);
		
		stacks.set(0, ingredients.getInputs(ItemStack.class).get(0));
		stacks.set(1, ingredients.getInputs(ItemStack.class).get(1));
		stacks.set(2, ingredients.getInputs(ItemStack.class).get(2));
		stacks.set(3, ingredients.getInputs(ItemStack.class).get(3));
		stacks.set(4, ingredients.getInputs(ItemStack.class).get(4));
		stacks.set(5, ingredients.getOutputs(ItemStack.class).get(0));
	}

	@Override
	public String getModName() {
		return ArsMagica2.MODID;
	}

	@Override
	public IDrawable getIcon() {
		return null;
	}

	@Override
	public List<String> getTooltipStrings(int mouseX, int mouseY) {
		return Collections.emptyList();
	}

}
