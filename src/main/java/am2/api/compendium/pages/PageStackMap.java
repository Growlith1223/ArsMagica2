package am2.api.compendium.pages;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import am2.api.compendium.wrapper.StackMapWrapper;
import am2.gui.controls.GuiButtonCompendiumNext;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

public class PageStackMap extends CompendiumPage<StackMapWrapper> {
	
	private GuiButtonCompendiumNext nextPage = null;
	private GuiButtonCompendiumNext prevPage = null;
	private int page = 0;
	private int maxPages;

	public PageStackMap(StackMapWrapper element) {
		super(element);
		maxPages = (int) Math.floor(((double)element.getInput().size() - 1) / 24D);
	}
	
	@Override
	public GuiButton[] getButtons(int id, int posX, int posY) {
		if (nextPage == null)
			nextPage = new GuiButtonCompendiumNext(id++, posX + 125, posY + 20, true);
		if (prevPage == null)
			prevPage = new GuiButtonCompendiumNext(id++, posX + 5, posY + 20, false);
		nextPage.visible = page < maxPages;
		prevPage.visible = page > 0;
		return new GuiButton[] {nextPage, prevPage};
	}
	
	@Override
	public void actionPerformed(GuiButton button) throws IOException {
		if (button == nextPage) {
			page++;
			if (page > maxPages)
				page = maxPages;
			nextPage.visible = page < maxPages;
			prevPage.visible = page > 0;
		} else if (button == prevPage) {
			page--;
			if (page < 0)
				page = 0;
			nextPage.visible = page < maxPages;
			prevPage.visible = page > 0;
		}
	}
	
	@Override
	public void switchButtonDisplay(boolean shouldShow) {
		if (shouldShow) {
			nextPage.visible = page < maxPages;
			prevPage.visible = page > 0;			
		} else {
			nextPage.visible = false;
			prevPage.visible = false;
		}
	}
	
	@Override
	protected void renderPage(int posX, int posY, int mouseX, int mouseY) {
		String loc = element.getLocalizedName();
		mc.fontRenderer.drawString(loc, posX + 72 - (mc.fontRenderer.getStringWidth(loc) / 2), posY, 0x000000);
		int index = 0;
		int offsetX = 16;
		int offsetY = 24;
		RenderHelper.enableGUIStandardItemLighting();
		int max = -1;
		HashMap<ItemStack, Integer> map = new HashMap<>();
		map.putAll(element.getInput());
		for (Entry<ItemStack, Integer> entry : map.entrySet()) {
			if (entry.getValue() != null && entry.getValue().intValue() > max)
				max = entry.getValue().intValue();
		}
		ItemStack tip = null;
		for (int i = 0; i <= max; i++) {
			for (Entry<ItemStack, Integer> entry : map.entrySet()) {
				if (entry.getValue() != null && entry.getValue().intValue() == i) {
					int entryPage = (int) Math.floor(index / 24D);
					index++;
					if (entryPage == page) {
						mc.getRenderItem().renderItemIntoGUI(entry.getKey(), posX + offsetX, posY + offsetY);
						mc.fontRenderer.drawString(entry.getValue().toString(), posX + offsetX + 8 - (mc.fontRenderer.getStringWidth(entry.getValue().toString()) / 2), posY + offsetY + 18, 0xffffff);
					}
					if (mouseX > posX + offsetX && mouseX < posX + offsetX + 16 && mouseY > posY + offsetY && mouseY < posY + offsetY + 16)
						tip = entry.getKey();
					offsetX += 32;
					if (index % 4 == 0) {
						offsetX = 16;
						offsetY += 32;
					}
					if (index % 24 == 0) {
						offsetX = 16;
						offsetY = 24;
					}
				}
			}
		}
		if (tip != null)
			renderItemToolTip(tip, mouseX, mouseY);
	}

}
