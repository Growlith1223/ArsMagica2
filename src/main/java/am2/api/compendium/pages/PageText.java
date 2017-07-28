package am2.api.compendium.pages;

import am2.gui.AMGuiHelper;
import net.minecraft.util.text.translation.I18n;

@SuppressWarnings("deprecation")
public class PageText extends CompendiumPage<String> {

	public PageText(String element) {
		super(element);
	}

	@Override
	protected void renderPage(int posX, int posY, int mouseX, int mouseY) {
//		int y_start_title = posY + 50;
//		int x_start_title = posX + 100 - (mc.fontRenderer.getStringWidth(element) / 2);
//		int x_start_line = posX + 35;
//		int y_start_line = posY + 50;
//		if (page > numPages) page = numPages;
//
//		if (page == 0)
//			fontRenderer.drawString(entrySkill != null ? entrySkill.getName() : entry.getName(), x_start_title, y_start_title, 0x000000);
		AMGuiHelper.drawCompendiumText(I18n.translateToLocal(element), posX, posY, 140, 0x000000, mc.fontRenderer);
	}

}
