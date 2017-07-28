package am2.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import am2.api.compendium.CompendiumCategory;
import am2.api.compendium.CompendiumEntry;
import am2.api.extensions.ISkillData;
import am2.extensions.SkillData;
import am2.gui.controls.GuiButtonCompendiumLink;
import am2.gui.controls.GuiButtonCompendiumNext;
import am2.gui.controls.GuiButtonCompendiumTab;
import am2.gui.controls.GuiSpellImageButton;
import am2.lore.ArcaneCompendium;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class GuiCompendiumIndex extends GuiScreen{
	private CompendiumCategory currentCategory;
	private final ImmutableList<CompendiumCategory> categories;

	int xSize = 360;
	int ySize = 256;

	int page = 0;
	int numPages = 0;
	
	ArrayList<String> lines;
	int lineWidth = 140;
	int maxLines = 22;

	GuiButtonCompendiumNext nextPage;
	GuiButtonCompendiumNext prevPage;
	
	GuiSpellImageButton updateButton;

	ISkillData sk;

	private static final ResourceLocation background = new ResourceLocation("arsmagica2", "textures/gui/arcane_compendium_gui_index.png");

	public GuiCompendiumIndex(){
		categories = CompendiumCategory.getCategories();
		currentCategory = categories.iterator().next();
		lines = new ArrayList<String>();
		String path = am2.lore.ArcaneCompendium.For(Minecraft.getMinecraft().player).getPath();
		if (path != null) {
			CompendiumCategory category = CompendiumCategory.getCategoryFromID(path);
			if (category != null)
				currentCategory = category;
		}
		
		
		sk = SkillData.For(Minecraft.getMinecraft().player);
	}

	@Override
	public void initGui(){
		int idCount = 0;
		int posX = (width - xSize) / 2;
		int posY = (height - ySize) / 2;
		int tabY = posY + 40;
		int tabWidth = 1;
		
		nextPage = new GuiButtonCompendiumNext(idCount++, posX + 320, posY + 13, true);
		prevPage = new GuiButtonCompendiumNext(idCount++, posX + 33, posY + 13, false);
		for (CompendiumCategory category : categories) {
			int locPage = 0;
			if (!category.hasParents()) {
				GuiButtonCompendiumTab tab = new GuiButtonCompendiumTab(idCount++, posX + 10, tabY, category);
				if (category == currentCategory)
					tab.setActive(true);
				buttonList.add(tab);
				if (tabWidth < tab.getWidth())
					tabWidth = tab.getWidth();
				tabY += 18;
			}
			int buttonY = posY + 35;
			int buttonX = posX + 40;
			ArrayList<CompendiumCategory> sortedCategories = Lists.newArrayList(categories);
			sortedCategories.sort(new Comparator<CompendiumCategory>() {public int compare(CompendiumCategory o1, CompendiumCategory o2) { return o1.getCategoryName().compareTo(o2.getCategoryName());}});
			for (CompendiumCategory sub : sortedCategories) {
				if (sub.getParentsString().equals(category.getID())) {
					boolean hasSubItems = false;
					for (CompendiumEntry entry : category.getEntries()) {
						if (entry.getRenderObject() == null || ArcaneCompendium.For(mc.player).isUnlocked(entry.getID())) {
							hasSubItems = true;
							break;
						}
					}
					if (!hasSubItems) continue;
					GuiButtonCompendiumLink tab = new GuiButtonCompendiumLink(idCount++, buttonX, buttonY, fontRenderer, locPage, null, sub);
					tab.visible = sub.getParentsString().equals(currentCategory.getID()) && page == locPage;
					buttonY += 12;
					if (buttonY > posY + (ySize) - 25){
						if (buttonX > posX + 40){
							buttonX = posX + 40;
							locPage++;
						}else{
							buttonX += 155;
						}
						buttonY = posY + 30;
					}
					buttonList.add(tab);
				}
			}
			ArrayList<CompendiumEntry> sortedEntries = Lists.newArrayList(category.getEntries());
			sortedEntries.sort(new Comparator<CompendiumEntry>() {public int compare(CompendiumEntry o1, CompendiumEntry o2) { return o1.getName().compareTo(o2.getName());}});
			for (CompendiumEntry entry : sortedEntries) {
				if (!mc.player.capabilities.isCreativeMode && entry.getRenderObject() != null && !ArcaneCompendium.For(mc.player).isUnlocked(entry.getID()))
					continue;
				GuiButtonCompendiumLink link = new GuiButtonCompendiumLink(idCount++, buttonX, buttonY, fontRenderer, locPage, entry, null);
				link.visible = entry.canBeDisplayed(category.getID()) && page == locPage;
				buttonY += 12;
				if (buttonY > posY + (ySize) - 25){
					if (buttonX > posX + 40){
						buttonX = posX + 40;
						locPage++;
					}else{
						buttonX += 155;
					}
					buttonY = posY + 30;
				}
				buttonList.add(link);
			}
		}
		for (GuiButton button : buttonList) {
			if (button instanceof GuiButtonCompendiumTab)
				((GuiButtonCompendiumTab)button).setDimensions(tabWidth, 16);
		}
		nextPage.visible = numPages > 0;
		prevPage.visible = false;
		switchCategory(currentCategory);
		buttonList.add(nextPage);
		buttonList.add(prevPage);
		super.initGui();
	}


	@Override
	protected void mouseClicked(int par1, int par2, int par3) throws IOException{
		for (int l = 0; l < this.buttonList.size(); ++l){
			GuiButton guibutton = (GuiButton)this.buttonList.get(l);

			if (guibutton.mousePressed(Minecraft.getMinecraft(), par1, par2)){
//				if (guibutton.id == updateButton.id){
//					this.storeBreadcrumb();
//					if (par3 == 0){ //left click
//						this.mc.displayGuiScreen(new GuiConfirmOpenLink(this, ArcaneCompendium.instance.getModDownloadLink(), 0, false));
//					}else if (par3 == 1){ //right click
//						this.mc.displayGuiScreen(new GuiConfirmOpenLink(this, ArcaneCompendium.instance.getPatchNotesLink(), 1, false));
//					}
//				}
				if (par3 == 0){
					this.actionPerformed(guibutton);
					return;
				}
			}
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button == nextPage) {
			page++;
			if (page > numPages)
				page = numPages;
			switchCategory(currentCategory);
		} else if (button == prevPage) {
			page--;
			if (page < 0)
				page = 0;
			switchCategory(currentCategory);
		} else if (button instanceof GuiButtonCompendiumTab) {
			switchCategory(((GuiButtonCompendiumTab)button).category);
		} else if (button instanceof GuiButtonCompendiumLink) {
			GuiButtonCompendiumLink target = (GuiButtonCompendiumLink)button;
			if (target.getEntry() != null)
				mc.displayGuiScreen(new GuiArcaneCompendium(target.getEntry()));
			else if (target.getCategory() != null) {
				switchCategory(target.getCategory());
			}
		}
		super.actionPerformed(button);
	}
	
	private void switchCategory(CompendiumCategory category) {
		if (category != currentCategory) {
			page = 0;
			nextPage.visible = numPages > 0;
			prevPage.visible = false;
		}
		int numButton = 0;
		for (GuiButton button : buttonList) {
			if (button instanceof GuiButtonCompendiumLink) {
				GuiButtonCompendiumLink link = (GuiButtonCompendiumLink)button;
				if (link.getEntry() != null)
					link.visible = link.getEntry().canBeDisplayed(category.getID());
				else if (link.getCategory() != null)
					link.visible = link.getCategory().getParentsString().equals(category.getID());
				if (link.visible)
					numButton++;
				link.visible &= page == link.getPage();
			} else if (button instanceof GuiButtonCompendiumTab) {
				GuiButtonCompendiumTab tab = (GuiButtonCompendiumTab)button;
				tab.setActive(tab.category == category && page == numPages);
			}
		}
		numPages = (int) Math.floor((double)numButton / 34D);
		nextPage.visible = page < numPages;
		prevPage.visible = page > 0;
		this.currentCategory = category;
		am2.lore.ArcaneCompendium.For(Minecraft.getMinecraft().player).setPath(category.getID());
	}
	
	@Override
	public boolean doesGuiPauseGame(){
		return false;
	}

	@Override
	protected void keyTyped(char par1, int par2) throws IOException{
		if (par2 == 1){
			onGuiClosed();
		}
		super.keyTyped(par1, par2);
	}

	@Override
	public void drawScreen(int par1, int par2, float par3){

		this.drawDefaultBackground();

		RenderHelper.enableGUIStandardItemLighting();

		int l = (width - xSize) / 2;
		int i1 = (height - ySize) / 2;

		mc.renderEngine.bindTexture(background);
		GL11.glColor3f(1.0f, 1.0f, 1.0f);
		this.drawTexturedModalRect_Classic(l, i1, 0, 0, xSize, ySize, 256, 240);

		String compendiumTitle = "\247nArcane Compendium";

		int y_start_title = i1 + 20;
		int x_start_title = l + 100 - (fontRenderer.getStringWidth(compendiumTitle) / 2);

		if (page == 0)
			fontRenderer.drawString(compendiumTitle, x_start_title, y_start_title, 0);

		int x_start_line = l + 35;
		int y_start_line = i1 + 35;

		if (lines != null && lines.size() > page){
			AMGuiHelper.drawCompendiumText(lines.get(page), x_start_line, y_start_line, lineWidth, 0x000000, fontRenderer);
		}

		super.drawScreen(par1, par2, par3);
	}

	public void drawTexturedModalRect_Classic(int dst_x, int dst_y, int src_x, int src_y, int dst_width, int dst_height, int src_width, int src_height){
		float var7 = 0.00390625F;
		float var8 = 0.00390625F;

		Tessellator var9 = Tessellator.getInstance();
		var9.getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX);
		var9.getBuffer().pos(dst_x + 0, dst_y + dst_height, this.zLevel).tex((src_x + 0) * var7, (src_y + src_height) * var8).endVertex();;
		var9.getBuffer().pos(dst_x + dst_width, dst_y + dst_height, this.zLevel).tex((src_x + src_width) * var7, (src_y + src_height) * var8).endVertex();;
		var9.getBuffer().pos(dst_x + dst_width, dst_y + 0, this.zLevel).tex((src_x + src_width) * var7, (src_y + 0) * var8).endVertex();;
		var9.getBuffer().pos(dst_x + 0, dst_y + 0, this.zLevel).tex((src_x + 0) * var7, (src_y + 0) * var8).endVertex();;
		var9.draw();
	}
}
