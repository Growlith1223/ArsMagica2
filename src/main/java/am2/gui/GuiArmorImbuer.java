package am2.gui;

import java.io.IOException;
import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import am2.api.items.armor.ArmorImbuement;
import am2.api.items.armor.ImbuementTiers;
import am2.armor.ArmorHelper;
import am2.armor.infusions.ImbuementRegistry;
import am2.blocks.tileentity.TileEntityArmorImbuer;
import am2.container.ContainerArmorInfuser;
import am2.packet.AMNetHandler;
import am2.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;

@SuppressWarnings("deprecation")
public class GuiArmorImbuer extends GuiContainer{

	private TileEntityArmorImbuer tileEntity;
	private EntityPlayer player;

	private static final ResourceLocation foreground = new ResourceLocation("arsmagica2", "textures/gui/armor_upgrade_gui.png");

	int spriteHeight = 24;
	int spriteWidth = 43;
	ResourceLocation hoveredID;
	int tier1Offset = 0;
	int tier2Offset = 0;
	int tier3Offset = 0;
	int tier4Offset = 0;
	GuiButton tier1Next;
	GuiButton tier1Prev;
	GuiButton tier2Next;
	GuiButton tier2Prev;
	GuiButton tier3Next;
	GuiButton tier3Prev;
	GuiButton tier4Next;
	GuiButton tier4Prev;

	public GuiArmorImbuer(EntityPlayer player, TileEntityArmorImbuer infuser){
		super(new ContainerArmorInfuser(player, infuser));
		this.tileEntity = infuser;
		this.player = player;
		xSize = 247;
		ySize = 250;
	}
	
	@Override
	public void initGui() {
		super.initGui();
		int l = (width - xSize) / 2;
		int i1 = (height - ySize) / 2;
		tier1Next = new GuiButton(0, l + 15 + 208, i1 + 26 + 0, 20, 20, ">");
		tier1Prev = new GuiButton(1, l + 1, i1 + 26 + 0, 20, 20, "<");
		tier2Next = new GuiButton(2, l + 15 + 208, i1 + 26 + 45, 20, 20, ">");
		tier2Prev = new GuiButton(3, l + 1, i1 + 26 + 45, 20, 20, "<");
		tier3Next = new GuiButton(4, l + 15 + 208, i1 + 26 + 90, 20, 20, ">");
		tier3Prev = new GuiButton(5, l + 1, i1 + 26 + 90, 20, 20, "<");
		tier4Next = new GuiButton(6, l + 15 + 208, i1 + 26 + 135, 20, 20, ">");
		tier4Prev = new GuiButton(7, l + 1, i1 + 26 + 135, 20, 20, "<");
		tier1Next.visible = false;
		tier1Prev.visible = false;
		tier2Next.visible = false;
		tier2Prev.visible = false;
		tier3Next.visible = false;
		tier3Prev.visible = false;
		tier4Next.visible = false;
		tier4Prev.visible = false;
		this.buttonList.add(tier1Next);
		this.buttonList.add(tier1Prev);
		this.buttonList.add(tier2Next);
		this.buttonList.add(tier2Prev);
		this.buttonList.add(tier3Next);
		this.buttonList.add(tier3Prev);
		this.buttonList.add(tier4Next);
		this.buttonList.add(tier4Prev);
	}

	@Override
	protected void mouseClicked(int par1, int par2, int par3) throws IOException{
		if (hoveredID != null){
			AMNetHandler.INSTANCE.sendImbueToServer(tileEntity, hoveredID.toString());
			tileEntity.imbueCurrentArmor(hoveredID);
		}
		super.mouseClicked(par1, par2, par3);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j){
		int l = (width - xSize) / 2;
		int i1 = (height - ySize) / 2;

		ItemStack stack = tileEntity.getStackInSlot(0);

		int startX = l + 22;
		int stepX = 52;
		int startY = i1 + 23;
		int stepY = 45;

		int drawX = startX;
		int drawY = startY;

		ArrayList<String> hoverLines = new ArrayList<String>();

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GL11.glColor3f(0, 0, 0);
		drawTexturedModalRect(l, i1, 0, 0, xSize, ySize);

		GL11.glColor3f(1, 1, 1);
		hoveredID = null;
		if (stack != null){
			EntityEquipmentSlot armorType = null;
			if ( stack.getItem() instanceof ItemArmor) {
				armorType = ((ItemArmor)stack.getItem()).armorType;
			}

			for (ImbuementTiers tier : ImbuementTiers.values()){
				int offset = tier == ImbuementTiers.FIRST ? tier1Offset : tier == ImbuementTiers.SECOND ? tier2Offset : tier == ImbuementTiers.THIRD ? tier3Offset : tier4Offset;
				int num = 0;
				ArrayList<ArmorImbuement> infusions = new ArrayList<>();
				for (ArmorImbuement imbuement : ImbuementRegistry.instance.getImbuementsForTier(tier, armorType)) {
					if (imbuement.canApplyToArmor(stack, player)) infusions.add(imbuement);
				}
				GuiButton buttonNext = tier == ImbuementTiers.FIRST ? tier1Next : (tier == ImbuementTiers.SECOND ? tier2Next : (tier == ImbuementTiers.THIRD ? tier3Next : tier4Next));
				GuiButton buttonPrev = tier == ImbuementTiers.FIRST ? tier1Prev : (tier == ImbuementTiers.SECOND ? tier2Prev :( tier == ImbuementTiers.THIRD ? tier3Prev : tier4Prev));
				if (infusions.size() > 4) {
					buttonNext.visible = true;
					buttonPrev.visible = true;
				} else {
					buttonNext.visible = false;
					buttonPrev.visible = false;
				}
				if (offset <= 0) 
					buttonPrev.enabled = false;
				else
					buttonPrev.enabled = true;
				if (offset >= infusions.size() - 4)
					buttonNext.enabled = false;
				else
					buttonNext.enabled = true;
				for (ArmorImbuement infusion : infusions){
					if (num < offset || num >= offset + 4) {
						num++;
						continue;
					}
					if (num == offset + 3) drawX--;
					mc.renderEngine.bindTexture(new ResourceLocation(infusion.getRegistryName().getResourceDomain(), "textures/armorinfusions/" + infusion.getRegistryName().getResourcePath() + ".png"));
					drawInfusionIconAt(drawX, drawY, false);
					
					if (i >= drawX && i <= drawX + spriteWidth){
						if (j >= drawY && j <= drawY + spriteHeight){
							hoverLines.add(I18n.translateToLocal("am2.tooltip." + infusion.getID()));
						}
					}
					num++;
					drawX += stepX;
				}
				drawY += stepY;
				drawX = startX;
			}

			drawX = startX;
			drawY = startY;

			int highestSelectedTier = 0;
			for (ImbuementTiers tier : ImbuementTiers.values()){
				ArrayList<ArmorImbuement> infusions = new ArrayList<>();
				for (ArmorImbuement imbuement : ImbuementRegistry.instance.getImbuementsForTier(tier, armorType)) {
					if (imbuement.canApplyToArmor(stack, player)) infusions.add(imbuement);
				}
				ArmorImbuement[] existingInfusions = ArmorHelper.getInfusionsOnArmor(stack);
				ArmorImbuement tierInfusion = null;
				int offset = tier == ImbuementTiers.FIRST ? tier1Offset : tier == ImbuementTiers.SECOND ? tier2Offset : tier == ImbuementTiers.THIRD ? tier3Offset : tier4Offset;
				int num = 0;
				for (ArmorImbuement infusion : existingInfusions){
					if (infusion == null) continue;
					if (infusion.getTier() == tier){
						tierInfusion = infusion;
						if (tier.ordinal() >= highestSelectedTier)
							highestSelectedTier = tier.ordinal() + 1;
						break;
					}
				}
				for (ArmorImbuement infusion : infusions){
					if (num < offset || num >= offset + 4) {
						num++;
						continue;
					}
					if (num == offset + 3) drawX--;
					mc.renderEngine.bindTexture(new ResourceLocation(infusion.getRegistryName().getResourceDomain(), "textures/armorinfusions/" + infusion.getRegistryName().getResourcePath() + ".png"));
					if ((tierInfusion == null && infusion.getTier().ordinal() <= highestSelectedTier)){
						if (tileEntity.isCreativeAllowed() || ArmorHelper.getArmorLevel(stack) >= ArmorHelper.getImbueCost(tier)){
							drawInfusionIconAt(drawX, drawY, true);
							if (i >= drawX && i <= drawX + spriteWidth){
								if (j >= drawY && j <= drawY + spriteHeight){
									hoveredID = infusion.getRegistryName();
								}
							}
						}
					}else if (tierInfusion == infusion){
						mc.renderEngine.bindTexture(new ResourceLocation(infusion.getRegistryName().getResourceDomain(), "textures/armorinfusions/" + infusion.getRegistryName().getResourcePath() + ".png"));
						drawInfusionIconAt(drawX, drawY, true);
						mc.renderEngine.bindTexture(new ResourceLocation("textures/misc/enchanted_item_glint.png"));
						//SETUP
						GlStateManager.enableBlend();
				        GlStateManager.depthMask(false);
				        //GlStateManager.depthFunc(514);
				        GlStateManager.disableLighting();
				        RenderUtils.color(-8372020);
				        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
						GlStateManager.matrixMode(GL11.GL_TEXTURE);
						GlStateManager.pushMatrix();
						
				        float move = (float)(Minecraft.getSystemTime() % 3000L) / 3000.0F;
				        move %= 1;
				        GlStateManager.translate(move, 0.0F, 0.0F);
				        GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);
						RenderUtils.drawBox(drawX, drawY, spriteWidth, spriteHeight, l, 0, 0, 0.125f, 0.125f);
						GlStateManager.popMatrix();
						
						GlStateManager.pushMatrix();
				        float move1 = (float)(Minecraft.getSystemTime() % 4873L) / 4873.0F;
				        move1 %= 1;
				        GlStateManager.translate(move1, 0.0F, 0.0F);
				        GlStateManager.rotate(10.0F, 0.0F, 0.0F, 1.0F);
						RenderUtils.drawBox(drawX, drawY, spriteWidth, spriteHeight, l, 0, 0, 0.125f, 0.125f);
						GlStateManager.popMatrix();
						
						GlStateManager.matrixMode(GL11.GL_MODELVIEW);
				        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
				        //GlStateManager.depthFunc(515);
				        GlStateManager.depthMask(true);
				        RenderUtils.color(0xffffff);
				        GlStateManager.disableBlend();
					}

					num++;
					drawX += stepX;
				}
				drawY += stepY;
				drawX = startX;
			}
		} else {
			for (GuiButton button : buttonList) {
				button.visible = false;
			}
			tier1Offset = 0;
			tier2Offset = 0;
			tier3Offset = 0;
			tier4Offset = 0;
		}

		mc.renderEngine.bindTexture(foreground);
		drawTexturedModalRect(l, i1, 0, 0, xSize, ySize);

		if (hoverLines.size() > 0)
			AMGuiHelper.drawHoveringText(hoverLines, i, j, fontRenderer, width, height);
	}

	private void drawInfusionIconAt(int x, int y, boolean active){
		drawTexturedModalRect(x, y, 0, active ? 0 : spriteHeight, spriteWidth, spriteHeight);
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 0) {
			tier1Offset++;
		} else if (button.id == 1) {
			tier1Offset--;
		} else if (button.id == 2) {
			tier2Offset++;
		} else if (button.id == 3) {
			tier2Offset--;
		} else if (button.id == 4) {
			tier3Offset++;
		} else if (button.id == 5) {
			tier3Offset--;
		} else if (button.id == 6) {
			tier4Offset++;			
		} else if (button.id == 7) {
			tier4Offset--;
		}
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2){

	}

}
