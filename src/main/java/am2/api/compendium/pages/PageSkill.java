package am2.api.compendium.pages;

import static net.minecraft.client.renderer.texture.TextureMap.LOCATION_BLOCKS_TEXTURE;

import am2.api.ArsMagicaAPI;
import am2.api.skill.Skill;
import am2.defs.ItemDefs;
import am2.gui.AMGuiHelper;
import am2.texture.SpellIconManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class PageSkill extends CompendiumPage<Skill> {

	public PageSkill(Skill element) {
		super(element);
	}

	@Override
	protected void renderPage(int posX, int posY, int mouseX, int mouseY) {	
		int cx = posX + 60;
		int cy = posY + 92;
		RenderHelper.disableStandardItemLighting();
		TextureAtlasSprite icon = SpellIconManager.INSTANCE.getSprite(element.getRegistryName().toString());
		mc.renderEngine.bindTexture(LOCATION_BLOCKS_TEXTURE);
		GlStateManager.color(1, 1, 1, 1);
		if (icon != null)
			AMGuiHelper.DrawIconAtXY(icon, cx, cy, zLevel, 16, 16, false);
		if (mouseX > cx && mouseX < cx + 16){
			if (mouseY > cy && mouseY < cy + 16){
				renderItemToolTip(new ItemStack(ItemDefs.spell_component, 1, ArsMagicaAPI.getSkillRegistry().getId(element.getRegistryName())), mouseX, mouseY);
			}
		}
		mc.renderEngine.bindTexture(new ResourceLocation("arsmagica2", "textures/gui/arcane_compendium_gui_extras.png"));
		zLevel++;
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		this.drawTexturedModalRect_Classic(posX + 125, posY + 15, 112, 145, 60, 40, 40, 40);
		this.drawTexturedModalRect_Classic(posX , posY + 200, 112, 175, 60, 40, 40, 40);
		drawExtra(cx, cy);
		GlStateManager.enableAlpha();
		mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		RenderHelper.enableStandardItemLighting();
	}
	
	private void drawExtra(int cx, int cy) {
		GlStateManager.disableBlend();
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		this.drawTexturedModalRect_Classic(cx - 77, cy - 68, 0, 101, 150, 150, 100, 147);
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();	
	}
	
}
