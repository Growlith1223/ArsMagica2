package am2.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import am2.ArsMagica2;
import am2.api.ArsMagicaAPI;
import am2.api.SkillPointRegistry;
import am2.api.SkillRegistry;
import am2.api.SkillTreeRegistry;
import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.api.extensions.ISkillData;
import am2.api.skill.Skill;
import am2.api.skill.SkillPoint;
import am2.api.skill.SkillTree;
import am2.defs.ItemDefs;
import am2.defs.SkillDefs;
import am2.extensions.AffinityData;
import am2.extensions.SkillData;
import am2.gui.controls.GuiButtonSkillTree;
import am2.packet.AMDataWriter;
import am2.packet.AMNetHandler;
import am2.packet.AMPacketIDs;
import am2.texture.SpellIconManager;
import am2.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;

@SuppressWarnings("deprecation")
public class GuiOcculus extends GuiScreen {
	int xSize = 210;
	int ySize = 210;
	SkillTree currentTree = SkillDefs.TREE_OFFENSE;
	EntityPlayer player;
	int currentTabId = 0;
	
	private boolean isDragging = false;
	private int lastMouseX = 0;
	private int lastMouseY = 0;
	private int page = 0;
	private int maxPage = 0;
	private int offsetX = 568 / 2 - 82 + 8;
	private int offsetY = 0;
	private Skill hoverItem = null;
	
	private GuiButton nextPage;
	private GuiButton prevPage;
	
	public GuiOcculus(EntityPlayer player) {
		this.player = player;
	}
	
	@Override
	public void initGui() {
		int tabId = 0;
		int posX = width/2 - xSize/2;
		int posY = height/2 - ySize/2;
		ImmutableList<SkillTree> testTab = SkillTreeRegistry.getSkillTreeMap();
//		ArrayList<SkillTree> test = new ArrayList<>(testTab);
//		for (int i = 0; i < 16; i++) {
//			test.add(new SkillTree("Tree" + i, new ResourceLocation("textures/blocks/dirt.png"), new ResourceLocation("textures/blocks/stone.png")));
//		}
		for (SkillTree entry : testTab) {
			if (tabId % 16 < 8)
				buttonList.add(new GuiButtonSkillTree(tabId, posX + 7 + ((tabId % 16) * 24), posY - 22, entry, (int)Math.floor((float)tabId / 16F), false));
			else 
				buttonList.add(new GuiButtonSkillTree(tabId, posX + 7 + (((tabId % 16) - 8) * 24), posY + 210, entry, (int)Math.floor((float)tabId / 16F), true));
				
			tabId++;
		}
		maxPage = (int)Math.floor((float)(tabId - 1) / 16F);
		nextPage = new GuiButton(1000, posX + 212, posY - 21, 20, 20, ">");
		prevPage = new GuiButton(1001, posX - 15, posY - 21, 20, 20, "<");
		nextPage.enabled = page < maxPage;
		prevPage.enabled = page > 0;
		for (GuiButton button : buttonList) {
			if (button instanceof GuiButtonSkillTree) {
				button.visible = (int)Math.floor((float)button.id / 16F) == page;
			}
		}
		buttonList.add(nextPage);
		buttonList.add(prevPage);
		super.initGui();
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if (mouseButton == 0) {
			if (hoverItem != null && !SkillData.For(player).hasSkill(hoverItem.getID())) {
				ISkillData data = SkillData.For(player);
				if (data.canLearn(hoverItem.getID())) {
					AMNetHandler.INSTANCE.sendPacketToServer(AMPacketIDs.UNLOCK_OCCULUS_ENTRY, new AMDataWriter().add(hoverItem.getID()).generate());
				}
			}
			else if (this.currentTree != SkillDefs.TREE_AFFINITY)
				isDragging = true;
		}
	}
	
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		isDragging = false;
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		if (button instanceof GuiButtonSkillTree) {
			currentTree = ((GuiButtonSkillTree)button).getTree();
			currentTabId = button.id;
			offsetX = 568 / 2 - 82 + 8;
			offsetY = 0;
		}else if (button == nextPage) {
			page++;
			if (page > maxPage)
				page = maxPage;
			nextPage.enabled = page < maxPage;
			prevPage.enabled = page > 0;
			for (GuiButton button_ : buttonList) {
				if (button_ instanceof GuiButtonSkillTree) {
					button_.visible = (int)Math.floor((float)button_.id / 16F) == page;
				}
			}
		}else if (button == prevPage) {
			page--;
			if (page < 0)
				page = 0;
			nextPage.enabled = page < maxPage;
			prevPage.enabled = page > 0;
			for (GuiButton button_ : buttonList) {
				if (button_ instanceof GuiButtonSkillTree) {
					button_.visible = (int)Math.floor((float)button_.id / 16F) == page;
				}
			}
		}
	}
	
	private int calcXOffset(int posX, Skill s) {
		return (int) (posX - this.offsetX + s.getPosX());
	}
	
	private int calcYOffset(int posY, Skill s) {
		return (int) (posY - this.offsetY + s.getPosY());
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		int posX = width/2 - xSize/2;
		int posY = height/2 - ySize/2;
		float renderSize = 32F;
		float renderRatio = 0.29F;
		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("arsmagica2", "textures/occulus/overlay.png"));
		//Overlay
		drawTexturedModalRect(posX, posY, 0, 0, 210, 210);
		//Tab Under
		if ((int)Math.floor((float)currentTabId / 16F) == page) {
			if ((currentTabId % 16) < 8)
				drawTexturedModalRect(posX + 7 + ((currentTabId % 16) * 24), posY, 22, 210, 22, 7);
			else 
				drawTexturedModalRect(posX + 7 + (((currentTabId % 16) - 8) * 24), posY + 203, 22, 210, 22, 7);
		}
		zLevel = -18F;
		if (isDragging){
			int dx = lastMouseX - mouseX;
			int dy = lastMouseY - mouseY;

			this.offsetX += dx;
			this.offsetY += dy;

			if (this.offsetX < 0) this.offsetX = 0;
			if (this.offsetX > 568) this.offsetX = 568;

			if (this.offsetY < 0) this.offsetY = 0;
			if (this.offsetY > 568) this.offsetY = 568;
		}
		lastMouseX = mouseX;
		lastMouseY = mouseY;
		float calcYOffest = ((float)offsetY / 568) * (1 - renderRatio);
		float calcXOffest = ((float)offsetX / 568) * (1 - renderRatio);
		{
			int maxSize = 0;
			for (SkillPoint point : SkillPointRegistry.getSkillPointMap().values()) {
				if (!point.canRender()) continue;
				maxSize = Math.max(maxSize, fontRenderer.getStringWidth(point.getName() + " : " + SkillData.For(player).getSkillPoint(point)));
			}
			zLevel = 0F;
			Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("arsmagica2", "textures/occulus/skill_points.png"));
			drawSkillPointBackground(posX, posY, maxSize + 10, 210);		
			int pointOffsetX = 5;
			for (SkillPoint point : SkillPointRegistry.getSkillPointMap().values()) {
				if (!point.canRender()) continue;
				fontRenderer.drawString(point.getName() + " : " + SkillData.For(player).getSkillPoint(point), posX + 215, posY + pointOffsetX, point.getColor());
				pointOffsetX+=10;
			}
			GlStateManager.color(1f, 1f, 1f);
		}
		Minecraft.getMinecraft().renderEngine.bindTexture(currentTree.getBackground());
		if (currentTree != SkillDefs.TREE_AFFINITY) {
			RenderUtils.drawBox(posX + 7, posY + 7, 196, 196, zLevel, calcXOffest, calcYOffest, renderRatio + calcXOffest, renderRatio + calcYOffest);
			ArrayList<Skill> skills = SkillRegistry.getSkillsForTree(currentTree);
			zLevel = 1F;
			ISkillData data = SkillData.For(player);
			for (Skill s : skills) {
				if (!s.getPoint().canRender() && !data.hasSkill(s.getID()))
					continue;
				for (String p : s.getParents()) {
					if (p == null)
						continue;
		        	Skill parent = SkillRegistry.getSkillFromName(p);
		        	if (parent == null || !skills.contains(parent)) continue;
					if (!parent.getPoint().canRender() && !data.hasSkill(parent.getID()))
						continue;
					int offsetX = calcXOffset(posX, s) + 16;
					int offsetY = calcYOffset(posY, s) + 16;
					int offsetX2 = calcXOffset(posX, parent) + 16;
					int offsetY2 = calcYOffset(posY, parent) + 16;
			        offsetX = MathHelper.clamp(offsetX, posX + 7, posX + 203);
					offsetY = MathHelper.clamp(offsetY, posY + 7, posY + 203);
					offsetX2 = MathHelper.clamp(offsetX2, posX + 7, posX + 203);
					offsetY2 = MathHelper.clamp(offsetY2, posY + 7, posY + 203);
					boolean hasPrereq = data.canLearn(s.getID()) || data.hasSkill(s.getID());
					int color = (!SkillData.For(player).hasSkill(s.getID()) ? s.getPoint().getColor() & 0x999999 : 0x00ff00);
					if (!hasPrereq) color = 0x000000;
					if (!(offsetX == posX + 7 || offsetX == posX + 203))
						RenderUtils.lineThick2d(offsetX, offsetY, offsetX, offsetY2, hasPrereq ? 0 : -1, color);
					if (!(offsetY2 == posY + 7 || offsetY2 == posY + 203))
						RenderUtils.lineThick2d(offsetX, offsetY2, offsetX2, offsetY2, hasPrereq ? 0 : -1, color);
				}
			}
			Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			for (Skill s : skills) {
				if (!s.getPoint().canRender() && !data.hasSkill(s.getID()))
					continue;
				GlStateManager.color(1, 1, 1, 1.0F);
				ISkillData skillData = SkillData.For(player);
				boolean hasPrereq = skillData.canLearn(s.getID()) || data.hasSkill(s.getID());
				int offsetX = calcXOffset(posX, s);
				int offsetY = calcYOffset(posY, s);
				int tick = (player.ticksExisted % 80) >= 40 ? (player.ticksExisted % 40) - 20 : -(player.ticksExisted % 40) + 20;
				float multiplier = 0.75F + tick / 80F;
				TextureAtlasSprite sprite = SpellIconManager.INSTANCE.getSprite(s.getID());
				if (offsetX + renderSize < posX + 7 || offsetX > posX + 203 || offsetY + renderSize < posY + 7 || offsetY > posY + 203 || sprite == null) {
					continue;
				}
				float spriteXSize = sprite.getMaxU() - sprite.getMinU();
				float spriteYSize = sprite.getMaxV() - sprite.getMinV();
				float xStartMod = 0;
				float yStartMod = 0;
				float xEndMod = 0;
				float yEndMod = 0;
				if (offsetX < posX + 7) {
					float mod = (posX + 7 - offsetX);
					xStartMod = mod;
				}
				else if (offsetX + renderSize > posX + 203) {
					float mod = renderSize - (posX + 203 - offsetX);
					xEndMod = mod;
				}
				if (offsetY < posY + 7) {
					float mod = (posY + 7 - offsetY);
					yStartMod = mod;
				}
				else if (offsetY + renderSize > posY + 203) {
					float mod = renderSize - (posY + 203 - offsetY);
					yEndMod = mod;
				}
				if (!hasPrereq)
					GlStateManager.color(0.1F, 0.1F, 0.1F);
				else if (!skillData.hasSkill(s.getID()))
					GlStateManager.color(Math.max(RenderUtils.getRed(s.getPoint().getColor()), 0.6F) * multiplier, Math.max(RenderUtils.getGreen(s.getPoint().getColor()), 0.6F) * multiplier, Math.max(RenderUtils.getBlue(s.getPoint().getColor()), 0.6F) * multiplier);
				
				if (ArsMagica2.disabledSkills.isSkillDisabled(s.getID()))
					GlStateManager.color(0.3f, 0.3f, 0.3f);
				RenderUtils.drawBox(offsetX + xStartMod,
						offsetY + yStartMod,
						renderSize - xStartMod - xEndMod,
						renderSize - yStartMod - yEndMod,
						0,
						sprite.getMinU() + (xStartMod / renderSize * spriteXSize),
						sprite.getMinV() + (yStartMod / renderSize * spriteYSize),
						sprite.getMaxU() - (xEndMod / renderSize * spriteXSize),
						sprite.getMaxV() - (yEndMod / renderSize * spriteYSize));
				GlStateManager.color(1, 1, 1, 1.0F);
				if (ArsMagica2.disabledSkills.isSkillDisabled(s.getID())){
					sprite = AMGuiIcons.padlock;
					spriteXSize = sprite.getMaxU() - sprite.getMinU();
					spriteYSize = sprite.getMaxV() - sprite.getMinV();
					xStartMod = 0;
					yStartMod = 0;
					xEndMod = 0;
					yEndMod = 0;
					if (offsetX + 8 < posX + 7) {
						float mod = (posX + 7 - offsetX - 8);
						xStartMod = mod;
					}
					else if (offsetX + 24 > posX + 203) {
						float mod = 16 - (posX + 203 - offsetX - 8);
						xEndMod = mod;
					}
					if (offsetY + 8 < posY + 7) {
						float mod = (posY + 7 - offsetY - 8);
						yStartMod = mod;
					}
					else if (offsetY + 24 > posY + 203) {
						float mod = 16 - (posY + 203 - offsetY - 8);
						yEndMod = mod;
					}

					RenderUtils.drawBox(offsetX + xStartMod + 8,
							offsetY + yStartMod + 8,
							16 - xStartMod - xEndMod,
							16 - yStartMod - yEndMod,
							0,
							sprite.getMinU() + (xStartMod / 16 * spriteXSize),
							sprite.getMinV() + (yStartMod / 16 * spriteYSize),
							sprite.getMaxU() - (xEndMod / 16 * spriteXSize),
							sprite.getMaxV() - (yEndMod / 16 * spriteYSize));
					GlStateManager.color(1, 1, 1, 1.0F);
				}
				
			}
			
			//Get the skill
			
			if (mouseX > posX && mouseX < posX + 210 && mouseY > posY && mouseY < posY + 210) {
				boolean flag = false;
				zLevel = 0F;
				for (Skill s : skills) {
					if (!s.getPoint().canRender() && !data.hasSkill(s.getID()))
						continue;
					int offsetX = calcXOffset(posX, s);
					int offsetY = calcYOffset(posY, s);
					if (offsetX > mouseX || offsetX < mouseX - renderSize|| offsetY > mouseY || offsetY < mouseY - renderSize)
						continue;
					boolean hasPrereq = true;
					for (String subParent : s.getParents()) {
						hasPrereq &= data.hasSkill(subParent);
					}
					ArrayList<String> list = new ArrayList<String>();
					list.add(s.getPoint().getChatColor().toString() + s.getName());
					if (ArsMagica2.disabledSkills.isSkillDisabled(s.getID()))
						list.add(TextFormatting.DARK_RED.toString() + I18n.translateToLocal("am2.gui.occulus.disabled"));
					else if (hasPrereq)
						list.add(TextFormatting.DARK_GRAY.toString() + s.getOcculusDesc()); 
					else
						list.add(TextFormatting.DARK_RED.toString() + I18n.translateToLocal("am2.gui.occulus.missingrequirements"));
					
					drawHoveringText(list, mouseX, mouseY, Minecraft.getMinecraft().fontRenderer);
					flag = true;
					hoverItem = s;
		            RenderHelper.disableStandardItemLighting();
		            GlStateManager.color(1.0F, 1.0F, 1.0F);
				}
				if (!flag)
					hoverItem = null;
			}
			
		} else {
			boolean isShiftDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
			RenderUtils.drawBox(posX + 7, posY + 7, 196, 196, zLevel, 0, 0, 1, 1);
			int affNum = ArsMagicaAPI.getAffinityRegistry().getValues().size() - 1;
			int portion = 360 / affNum;
			int currentID = 0;
			int cX = posX + xSize/2;
			int cY = posY + ySize/2;
			//float finalPercentage = AffinityData.For(player).getAffinityDepth(SkillDefs.NONE) * 100;
			ArrayList<String> drawString = new ArrayList<>();
			for (Affinity aff : ArsMagicaAPI.getAffinityRegistry().getValues()) {
				if (aff == Affinity.NONE)
					continue;
				double depth = AffinityData.For(player).getAffinityDepth(aff);
				double affEndX = Math.cos(Math.toRadians(portion*currentID)) * 10F + Math.cos(Math.toRadians(portion*currentID)) * depth * 60F;
				double affEndY = Math.sin(Math.toRadians(portion*currentID)) * 10F + (Math.sin(Math.toRadians(portion*currentID))) * depth * 60F;
				double affStartX1 = Math.cos(Math.toRadians(portion*currentID - portion/2)) * 10F;
				double affStartY1 = Math.sin(Math.toRadians(portion*currentID - portion/2)) * 10F;
				double affStartX2 = Math.cos(Math.toRadians(portion*currentID + portion/2)) * 10F;
				double affStartY2 = Math.sin(Math.toRadians(portion*currentID + portion/2)) * 10F;
				double affDrawTextX =  Math.cos(Math.toRadians(portion*currentID)) * 80F - 7;
				double affDrawTextY =  Math.sin(Math.toRadians(portion*currentID)) * 80F - 7;
				currentID++;
				
				int displace = (int)((Math.max(affStartX1, affStartX2) - Math.min(affStartX1, affStartX2) + Math.max(affStartY1, affStartY2) - Math.min(affStartY1, affStartY2)) / 2);
				if (depth > 0.01F) {
					RenderUtils.fractalLine2dd(affStartX1 + cX, affStartY1 + cY, affEndX + cX, affEndY + cY, zLevel, aff.getColor(), displace, 0.8F);
					RenderUtils.fractalLine2dd(affStartX2 + cX, affStartY2 + cY, affEndX + cX, affEndY + cY, zLevel, aff.getColor(), displace, 0.8F);
				
					RenderUtils.fractalLine2dd(affStartX1 + cX, affStartY1 + cY, affEndX + cX, affEndY + cY, zLevel, aff.getColor(), displace, 1.1F);
					RenderUtils.fractalLine2dd(affStartX2 + cX, affStartY2 + cY, affEndX + cX, affEndY + cY, zLevel, aff.getColor(), displace, 1.1F);
				} else {
					RenderUtils.line2d((float)affStartX1 + cX, (float)affStartY1 + cY, (float)affEndX + cX, (float)affEndY + cY, zLevel, aff.getColor());
					RenderUtils.line2d((float)affStartX2 + cX, (float)affStartY2 + cY, (float)affEndX + cX, (float)affEndY + cY, zLevel, aff.getColor());
				}
				
				Minecraft.getMinecraft().fontRenderer.drawString("" + (float)Math.round(depth * 10000) / 100F, (int)((affDrawTextX *0.9) + cX), (int)((affDrawTextY*0.9) + cY), aff.getColor());
				//Minecraft.getMinecraft().fontRenderer.drawString("" + (float)Math.round(depth * 10000) / 100F, , aff.getColor());
				int xMovement = affDrawTextX > 0 ? 5 : -5;
				xMovement = affDrawTextX == 0 ? 0 : xMovement;
				int yMovement = affDrawTextY > 0 ? 5 : -5;
				yMovement = affDrawTextY == 0 ? 0 : yMovement;
				int drawX = (int)((affDrawTextX * 1.1) + cX + xMovement);
				int drawY = (int)((affDrawTextY * 1.1) + cY + yMovement);
				this.itemRender.renderItemAndEffectIntoGUI(new ItemStack(ItemDefs.essence, 1, ArsMagicaAPI.getAffinityRegistry().getId(aff)) , drawX, drawY);
				if (mouseX > drawX && mouseX < drawX + 16 && mouseY > drawY && mouseY < drawY + 16) {
					drawString.add(TextFormatting.RESET.toString() + aff.getLocalizedName());
					ArrayList<AbstractAffinityAbility> abilites = Lists.newArrayList(ArsMagicaAPI.getAffinityAbilityRegistry().getValues());
					abilites.sort(new Comparator<AbstractAffinityAbility>() {

						@Override
						public int compare(AbstractAffinityAbility o1, AbstractAffinityAbility o2) {
							return (int) ((o1.getMinimumDepth() * 100) - (o2.getMinimumDepth() * 100));
						}
					});
					
					
					for (AbstractAffinityAbility ability : abilites) {
						if (ability.getAffinity() == aff) {
							String advancedTooltip = "";
							if (isShiftDown) {
								advancedTooltip = " (Min. : " + Math.round(ability.getMinimumDepth() * 100) + "%" + (ability.hasMax() ?(", Max. : " + Math.round(ability.getMaximumDepth() * 100) + "%")  : "") + ")";
							}
							drawString.add(TextFormatting.RESET.toString()
									+ (ability.isEligible(player) ? TextFormatting.GREEN.toString()
											: TextFormatting.DARK_RED.toString())
									+ I18n.translateToLocal("affinityability."
											+ ability.getRegistryName().toString().replaceAll("arsmagica2:", "")
											+ ".name") + advancedTooltip);
						}
					}
				}
			}
			if (!drawString.isEmpty()) {
				if (!isShiftDown)
					drawString.add(TextFormatting.GRAY.toString() + I18n.translateToLocal("am2.tooltip.shiftForDetails"));
				drawHoveringText(drawString, mouseX, mouseY);
			}
			GlStateManager.color(1, 1, 1);
			RenderHelper.disableStandardItemLighting();
		}
		
		//Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("missingno"));
//		
//		int tier0 = SkillData.For(player).getSkillPoint(SkillPoint.SKILL_POINT_1);
//		int tier1 = SkillData.For(player).getSkillPoint(SkillPoint.SKILL_POINT_2);
//		int tier2 = SkillData.For(player).getSkillPoint(SkillPoint.SKILL_POINT_3);
//		int tier3 = SkillData.For(player).getSkillPoint(SkillPoint.SKILL_POINT_4);
//		int tier4 = SkillData.For(player).getSkillPoint(SkillPoint.SKILL_POINT_5);
//		int tier5 = SkillData.For(player).getSkillPoint(SkillPoint.SKILL_POINT_6);
//		GlStateManager.disableDepth();
//		fontRenderer.drawString("" + tier0, posX + 191, posY - 19, SkillPointRegistry.getPointForTier(0).getColor());
//		fontRenderer.drawString("" + tier1, posX + 203, posY - 19, SkillPointRegistry.getPointForTier(1).getColor());
//		fontRenderer.drawString("" + tier2, posX + 197, posY - 9, SkillPointRegistry.getPointForTier(2).getColor());
//		if (SkillPointRegistry.getPointForTier(3) != null)
//			fontRenderer.drawString("" + tier3, posX + 191, posY + 210 + 2, SkillPointRegistry.getPointForTier(3).getColor());
//		if (SkillPointRegistry.getPointForTier(4) != null)
//			fontRenderer.drawString("" + tier4, posX + 203, posY + 210 + 2, SkillPointRegistry.getPointForTier(4).getColor());
//		if (SkillPointRegistry.getPointForTier(5) != null)
//			fontRenderer.drawString("" + tier5, posX + 197, posY + 210 + 12, SkillPointRegistry.getPointForTier(5).getColor());
		
		GlStateManager.color(1, 1, 1);

		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	private void drawSkillPointBackground(int startX, int startY, int width, int height) {
		int posX = 210;
		int posY = 0;
		//drawTexturedModalRect(startX + posX, startY + posY, 0, 0, 4, 4);
		drawTexturedModalRect(startX + posX + width - 4, startY + posY, 252, 0, 4, 4);
		//drawTexturedModalRect(startX + posX, startY + posY + height - 4, 0, 252, 4, 4);
		drawTexturedModalRect(startX + posX + width - 4, startY + posY + height - 4, 252, 252, 4, 4);
		int w = width - 4;
		int h = height - 8;
		while (w > 0) {
			int x = 0;
			if (w > 252)
				x = 252;
			else
				x = w;
			while (h > 0) {
				int y = 0;
				if (h > 248)
					y = 248;
				else
					y = h;		
				drawTexturedModalRect(startX + posX + w - x, startY + posY + 4 + h - y, 4, 4, x, y);
				h -= y;
			}
			w -= x;
		}
		w = width - 4;
		h = height - 8;
		while (w > 0) {
			int x = 0;
			if (w > 252)
				x = 252;
			else
				x = w;
			drawTexturedModalRect(startX + posX + w - x, startY + posY, 4, 0, x, 4);
			drawTexturedModalRect(startX + posX + w - x, startY + posY + height - 4, 4, 252, x, 4);
			w -= x;
		}
		while (h > 0) {
			int y = 0;
			if (h > 248)
				y = 248;
			else
				y = h;
			//drawTexturedModalRect(startX + posX, startY + posY + 4 + h - y, 0, 4, 4, y);
			drawTexturedModalRect(startX + posX + width - 4, startY + posY + 4 + h - y, 252, 4, 4, y);
			h -= y;
		}
	}
	
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
}
