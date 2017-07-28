package am2.api.compendium.pages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import am2.api.blocks.MultiblockStructureDefinition;
import am2.api.compendium.CompendiumEntry;
import am2.api.rituals.IRitualInteraction;
import am2.api.spell.AbstractSpellPart;
import am2.gui.AMGuiHelper;
import am2.gui.GuiArcaneCompendium;
import am2.gui.controls.GuiButtonTextOnly;
import am2.gui.controls.GuiButtonVariableDims;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.oredict.OreDictionary;

@SuppressWarnings("deprecation")
public class PageRitual extends CompendiumPage<IRitualInteraction.Wrapper> {
	
	GuiButton shapeButton;
	
	public PageRitual(IRitualInteraction.Wrapper element) {
		super(element);
	}

	@Override
	protected void renderPage(int posX, int posY, int mouseX, int mouseY) {
		String ritualName;
		if (element.getRitualInteraction() instanceof AbstractSpellPart)
			ritualName = TextFormatting.UNDERLINE.toString() + TextFormatting.BOLD.toString() + I18n.translateToLocal("ritual." + ((AbstractSpellPart)element.getRitualInteraction()).getRegistryName().toString() + ".name");
		else
			ritualName = TextFormatting.UNDERLINE.toString() + TextFormatting.BOLD.toString() + I18n.translateToLocal("ritual." + element.getRitualInteraction().getClass().getSimpleName().toLowerCase() + ".name");
		mc.fontRenderer.drawString(ritualName, posX + 72 - (mc.fontRenderer.getStringWidth(ritualName) / 2), posY, 0);
		String categoryName = TextFormatting.UNDERLINE.toString() + I18n.translateToLocal("am2.gui.ritualshape");
		mc.fontRenderer.drawString(categoryName, posX + 72 - (mc.fontRenderer.getStringWidth(categoryName) / 2), posY + 20, 0);
//		String shapeName = I18n.translateToLocal("ritualshape." + element.getRitualInteraction().getRitualShape().getId() + ".name");
//		mc.fontRenderer.drawString(shapeName, posX + 72 - (mc.fontRenderer.getStringWidth(shapeName) / 2), posY + 30, 0);
		
		String reagentsName = TextFormatting.UNDERLINE.toString() + I18n.translateToLocal("am2.gui.ritualreagents");
		mc.fontRenderer.drawString(reagentsName, posX + 72 - (mc.fontRenderer.getStringWidth(reagentsName) / 2), posY + 50, 0);
		Random randomizer = new Random(new Random(AMGuiHelper.instance.getSlowTicker()).nextLong());
		
		ItemStack[] reagents = element.getRitualInteraction().getReagents();
		int lines = (int) Math.ceil(reagents.length / 4F);
		int yOffset = 0;
		ItemStack stackTip = null;
		RenderHelper.enableGUIStandardItemLighting();
		for (int l = 0; l < lines; l++) {
			int items = (reagents.length - (l*4)) % 4;
			int xOffset = 16;
			if (items == 3)
				xOffset += 16;
			if (items == 2)
				xOffset += 32;
			if (items == 1)
				xOffset += 48;
			for (int i = 0; i < items; i++) {
				ItemStack stack = reagents[l * 4 + i];
				if (stack != null) {
					/*if (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
						NonNullList<ItemStack> subItems = new NonNullList<ItemStack>();
						stack.getItem().getSubItems(stack.getItem(), stack.getItem().getCreativeTab(), subItems);
						stack = subItems.get(randomizer.nextInt(subItems.size()));
					}*/ // Temporarily disabled because I can't deal with NonNullLists.
					mc.getRenderItem().renderItemIntoGUI(stack, posX + xOffset, posY + yOffset + 60);
				}
				if (mouseX > posX + xOffset && mouseX < posX + xOffset + 16 && mouseY > posY + yOffset + 60 && mouseY < posY + yOffset + 76)
					stackTip = stack;
				xOffset += 32;
			}
			yOffset += 32;
		}
		
		if (element.getRitualInteraction().getResult() != null) {
			String resultName = TextFormatting.UNDERLINE.toString() + I18n.translateToLocal("am2.gui.ritualresult");
			mc.fontRenderer.drawString(resultName, posX + 72 - (mc.fontRenderer.getStringWidth(resultName) / 2), posY + 165, 0);
			mc.getRenderItem().renderItemIntoGUI(element.getRitualInteraction().getResult(), posX + 64, posY + 180);
			if (mouseX > posX + 64 && mouseX < posX + 80 && mouseY > posY + 180 && mouseY < posY + yOffset + 196)
				stackTip = element.getRitualInteraction().getResult();			
		}
		
		RenderHelper.disableStandardItemLighting();
		if (stackTip != null)
			renderItemToolTip(stackTip, mouseX, mouseY);
	}
	
	@Override
	public void actionPerformed(GuiButton button) throws IOException {
		if (button == shapeButton) {
			MultiblockStructureDefinition def = element.getRitualInteraction().getRitualShape();
			Minecraft.getMinecraft().displayGuiScreen(new GuiArcaneCompendium(new CompendiumEntry(null, "shape." + def.getId()).addObject("compendium.shape." + def.getId() + ".page1").addObject(def)));
		}
	}
	
	@Override
	public void switchButtonDisplay(boolean shouldShow) {
		shapeButton.visible = shouldShow;
	}
	
	@Override
	public GuiButton[] getButtons(int id, int posX, int posY) {
		String shapeName = I18n.translateToLocal("ritualshape." + element.getRitualInteraction().getRitualShape().getId() + ".name");
		shapeButton = new GuiButtonTextOnly(id++, 0, 0, shapeName);
		((GuiButtonVariableDims)shapeButton).setDimensions(mc.fontRenderer.getStringWidth(shapeName), 10);
		((GuiButtonVariableDims)shapeButton).setPosition(posX + 72 - (mc.fontRenderer.getStringWidth(shapeName) / 2), posY + 55);
		return new GuiButton[] {shapeButton};
	}
	
}
