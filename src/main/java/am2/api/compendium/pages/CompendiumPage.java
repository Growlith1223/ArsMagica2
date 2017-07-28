package am2.api.compendium.pages;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import am2.api.ArsMagicaAPI;
import am2.api.blocks.MultiblockStructureDefinition;
import am2.api.compendium.wrapper.StackMapWrapper;
import am2.api.rituals.IRitualInteraction;
import am2.api.skill.Skill;
import am2.api.spell.AbstractSpellPart;
import am2.defs.ItemDefs;
import am2.power.PowerTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("deprecation")
@SideOnly(Side.CLIENT)
public abstract class CompendiumPage<E> {
	
	private static final HashMap<Class<?>, Class<? extends CompendiumPage<?>>> HANDLERS = new HashMap<>();
	
	static {
		registerPageType(PageItemStack.class, ItemStack.class);
		registerPageType(PageText.class, String.class);
		registerPageType(PageSpellComponent.class, AbstractSpellPart.class);
		registerPageType(PageStackMap.class, StackMapWrapper.class);
		registerPageType(PageMultiblock.class, MultiblockStructureDefinition.class);
		registerPageType(PageRitual.class, IRitualInteraction.Wrapper.class);
		registerPageType(PageSkill.class, Skill.class);
		registerPageType(PageEntity.class, Entity.class);
	}
	
	public static final <E> void registerPageType(Class<? extends CompendiumPage<E>> page, Class<E> clazz) {
		HANDLERS.put(clazz, page);
	}
	
	protected E element;
	protected Minecraft mc;
	protected float zLevel;
	
	public CompendiumPage(E element) {
		this.element = element;
		this.mc = Minecraft.getMinecraft();
	}
	
	protected abstract void renderPage(int posX, int posY, int mouseX, int mouseY);
	
	public final void render(int posX, int posY, int mouseX, int mouseY) {
		GlStateManager.pushMatrix();
		try {
			renderPage(posX, posY, mouseX, mouseY);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		GlStateManager.enableAlpha();
		GlStateManager.popMatrix();
	}
	
	public GuiButton[] getButtons(int id, int posX, int posY) {
		return new GuiButton[0];
	}
	
	public void switchButtonDisplay(boolean shouldShow) {}
	
	public void actionPerformed(GuiButton button) throws IOException {}
	
	public void dragMouse(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {}
	
	protected void drawHoveringText(List<String> par1List, int par2, int par3, FontRenderer font){
		if (!par1List.isEmpty()){
			GlStateManager.disableRescaleNormal();
			RenderHelper.disableStandardItemLighting();
			GlStateManager.disableLighting();
			GlStateManager.disableDepth();
			int k = 0;
			Iterator<String> iterator = par1List.iterator();

			while (iterator.hasNext()){
				String s = (String)iterator.next();
				int l = font.getStringWidth(s);

				if (l > k){
					k = l;
				}
			}

			int i1 = par2 + 12;
			int j1 = par3 - 12;
			int k1 = 8;

			if (par1List.size() > 1){
				k1 += 2 + (par1List.size() - 1) * 10;
			}

//			if (i1 + k > this.width){
//				i1 -= 28 + k;
//			}
//
//			if (j1 + k1 + 6 > this.height){
//				j1 = this.height - k1 - 6;
//			}

			this.zLevel = 300.0F;
			Minecraft.getMinecraft().getRenderItem().zLevel = 300.0F;
			int l1 = -267386864;
			this.drawGradientRect(i1 - 3, j1 - 4, i1 + k + 3, j1 - 3, l1, l1);
			this.drawGradientRect(i1 - 3, j1 + k1 + 3, i1 + k + 3, j1 + k1 + 4, l1, l1);
			this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 + k1 + 3, l1, l1);
			this.drawGradientRect(i1 - 4, j1 - 3, i1 - 3, j1 + k1 + 3, l1, l1);
			this.drawGradientRect(i1 + k + 3, j1 - 3, i1 + k + 4, j1 + k1 + 3, l1, l1);
			int i2 = 1347420415;
			int j2 = (i2 & 16711422) >> 1 | i2 & -16777216;
			this.drawGradientRect(i1 - 3, j1 - 3 + 1, i1 - 3 + 1, j1 + k1 + 3 - 1, i2, j2);
			this.drawGradientRect(i1 + k + 2, j1 - 3 + 1, i1 + k + 3, j1 + k1 + 3 - 1, i2, j2);
			this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 - 3 + 1, i2, i2);
			this.drawGradientRect(i1 - 3, j1 + k1 + 2, i1 + k + 3, j1 + k1 + 3, j2, j2);

			for (int k2 = 0; k2 < par1List.size(); ++k2){
				String s1 = (String)par1List.get(k2);
				font.drawStringWithShadow(s1, i1, j1, -1);

				if (k2 == 0){
					j1 += 2;
				}

				j1 += 10;
			}

			this.zLevel = 0.0F;
			Minecraft.getMinecraft().getRenderItem().zLevel = 0.0F;
			GlStateManager.enableLighting();
			GlStateManager.enableDepth();
			RenderHelper.enableStandardItemLighting();
			GlStateManager.enableRescaleNormal();
		}
	}
	
    protected void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor)
    {
        float f = (float)(startColor >> 24 & 255) / 255.0F;
        float f1 = (float)(startColor >> 16 & 255) / 255.0F;
        float f2 = (float)(startColor >> 8 & 255) / 255.0F;
        float f3 = (float)(startColor & 255) / 255.0F;
        float f4 = (float)(endColor >> 24 & 255) / 255.0F;
        float f5 = (float)(endColor >> 16 & 255) / 255.0F;
        float f6 = (float)(endColor >> 8 & 255) / 255.0F;
        float f7 = (float)(endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vertexbuffer.pos((double)right, (double)top, (double)this.zLevel).color(f1, f2, f3, f).endVertex();
        vertexbuffer.pos((double)left, (double)top, (double)this.zLevel).color(f1, f2, f3, f).endVertex();
        vertexbuffer.pos((double)left, (double)bottom, (double)this.zLevel).color(f5, f6, f7, f4).endVertex();
        vertexbuffer.pos((double)right, (double)bottom, (double)this.zLevel).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }
    
	protected void renderItemToolTip(ItemStack stack, int x, int y){
		try{
			List<String> list = stack.getTooltip(this.mc.player, this.mc.gameSettings.advancedItemTooltips);

			if (stack.getItem() instanceof ItemBlock){
			}else{
				if (stack.getItem() == ItemDefs.spell_component){
					list.clear();
					Skill skill = ArsMagicaAPI.getSkillRegistry().getObjectById(stack.getItemDamage());
					if (skill == null)
						return;
					list.add(skill.getName());
				}else if (stack.getItem() == ItemDefs.etherium){
					list.clear();
					list.add(stack.getCount() + " " + I18n.translateToLocal("item.arsmagica2:etherium.name"));
					ArrayList<String> subList = new ArrayList<>();
					for (PowerTypes type : PowerTypes.all()) {
						if ((stack.getItemDamage() & type.ID()) == type.ID()) {
							subList.add(type.getChatColor() + I18n.translateToLocal("etherium." + type.name() + ".name"));
						}
					}
					if (subList.size() == PowerTypes.all().size()) {
						list.add(TextFormatting.GRAY.toString() + I18n.translateToLocal("etherium.any.name"));
					} else {
						list.addAll(subList);
					}
				}else{
				}
			}

			for (int k = 0; k < list.size(); ++k){
				if (k == 0){
					list.set(k, stack.getRarity().rarityColor.toString() + (String)list.get(k));
				}else{
					list.set(k, TextFormatting.GRAY.toString() + (String)list.get(k));
				}
			}

			//split out multiline entries (only entry 0 in this case)
			if (((String)list.get(0)).indexOf('\n') != -1){
				String s = ((String)list.get(0));
				String colorPrefix = "";
				list.remove(0);
				colorPrefix = stack.getRarity().rarityColor.toString();
				String[] split = s.split("\n");
				for (int i = split.length - 1; i >= 0; --i){
					list.add(0, colorPrefix + split[i]);
				}
			}

			FontRenderer font = stack.getItem().getFontRenderer(stack);
			drawHoveringText(list, x, y, (font == null ? this.mc.fontRenderer : font));
		}catch (Throwable t){
		}
	}
	
	@SuppressWarnings("unchecked")
	public static final <E> CompendiumPage<E> getCompendiumPage(Class<? extends E> clazz, E element) {
		for (Entry<Class<?>, Class<? extends CompendiumPage<?>>> entry : HANDLERS.entrySet()){
			if (entry.getKey().isAssignableFrom(clazz)) {
				try {
					Constructor<CompendiumPage<E>> constructor = (Constructor<CompendiumPage<E>>) entry.getValue().getConstructor(entry.getKey());
					return constructor.newInstance(element);
				} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("No handlers found for " + clazz.getSimpleName());
		return null;
	}
	
	public void drawTexturedModalRect_Classic(int dst_x, int dst_y, int src_x, int src_y, int dst_width, int dst_height, int src_width, int src_height){
		float var7 = 0.00390625F;
		float var8 = 0.00390625F;

		Tessellator var9 = Tessellator.getInstance();
		var9.getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX);
		var9.getBuffer().pos(dst_x + 0, dst_y + dst_height, this.zLevel).tex((src_x + 0) * var7, (src_y + src_height) * var8).endVertex();
		var9.getBuffer().pos(dst_x + dst_width, dst_y + dst_height, this.zLevel).tex((src_x + src_width) * var7, (src_y + src_height) * var8).endVertex();
		var9.getBuffer().pos(dst_x + dst_width, dst_y + 0, this.zLevel).tex((src_x + src_width) * var7, (src_y + 0) * var8).endVertex();
		var9.getBuffer().pos(dst_x + 0, dst_y + 0, this.zLevel).tex((src_x + 0) * var7, (src_y + 0) * var8).endVertex();
		var9.draw();
	}

	public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {}

	public void mouseReleased(int mouseX, int mouseY, int state) {}
}
