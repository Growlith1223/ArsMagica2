package am2.gui;

import static net.minecraft.client.renderer.texture.TextureMap.LOCATION_BLOCKS_TEXTURE;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import am2.LogHelper;
import am2.api.ArsMagicaAPI;
import am2.defs.ItemDefs;
import am2.defs.PotionEffectsDefs;
import am2.extensions.EntityExtension;
import am2.items.ItemSpellComponent;
import am2.particles.AMParticleIcons;
import am2.power.PowerTypes;
import am2.texture.SpellIconManager;
import am2.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AMGuiHelper{

	protected static RenderItem itemRenderer = Minecraft.getMinecraft().getRenderItem();

	private AMGuiHelper(){
	}

	public static final AMGuiHelper instance = new AMGuiHelper();

	private long millis;
	private long lastmillis;
	private long accumulatedMillis;
	private static float zLevel = 300.0f;

	private static int fractalLineDetail = 2;

	// Buff flashing variables (for the UI)
	//=========================================
	public float slowFlashAlpha = 1.0f;
	public float fastFlashAlpha = 1.0f;
	private int flashCounter = 0;
	public final short flashDuration = 20;
	private int magicXPBarShowTimer = 0;
	private float magicXPBarAlpha = 0;
	public float playerRunesAlpha = 0;
	//=========================================
	//Blackout variables (for the UI)
	//=========================================
	private final short[] flashTimers = new short[5];
	private final int[] blackoutTimers = new int[5];
	private final int[] blackoutTimersMax = new int[5];
	//=========================================
	// Compendium Variables
	//=========================================
	private int slowUITicker = 0;
	private static int fastUITicker = 0;
	public boolean runCompendiumTicker = true;
	//=========================================

	public EntityItem dummyItem;
	
	private static final Random rand = new Random();

	public void blackoutArmorPiece(int index, int duration){
		flashTimers[index] = flashDuration;
		if (duration > blackoutTimers[index]) {
			blackoutTimers[index] = duration;
			blackoutTimersMax[index] = duration;
		}
	}

	public void flashArmorPiece(int index){
		flashTimers[index] = flashDuration;
	}

	public void flashManaBar(){
		if (flashTimers[4] <= 1)
			flashTimers[4] = flashDuration;
	}

	public short getFlashTimer(int index){
		return flashTimers[index];
	}

	public int getBlackoutTimer(int index){
		return blackoutTimers[index];
	}

	public int getBlackoutTimerMax(int index){
		return blackoutTimersMax[index];
	}

	public int getSlowTicker(){
		return slowUITicker;
	}

	public int getFastTicker(){
		return fastUITicker;
	}

	public float getMagicXPBarAlpha(){
		return this.magicXPBarAlpha;
	}

	public void showMagicXPBar(){
		this.magicXPBarAlpha = 1.0f;
		this.magicXPBarShowTimer = 100;
	}

	public void tick(){

		if (dummyItem == null){
			dummyItem = new EntityItem(Minecraft.getMinecraft().world);
		}else{
			dummyItem.rotationYaw += 0.1f;
			ReflectionHelper.setPrivateValue(EntityItem.class, dummyItem, (Integer)ReflectionHelper.getPrivateValue(EntityItem.class, dummyItem, "age", "field_70292_b", "d") + 1, "age", "field_70292_b", "d");
		}

		for (int i = 0; i < this.flashTimers.length; ++i){
			if (this.flashTimers[i] > 0)
				this.flashTimers[i]--;
		}

		for (int i = 0; i < this.blackoutTimers.length; ++i){
			if (this.blackoutTimers[i] > 0){
				this.blackoutTimers[i]--;
				if (this.blackoutTimers[i] == 0){
					flashArmorPiece(i);
				}
			}else{
				this.blackoutTimersMax[i] = 0;
			}
		}

		flashCounter++;
		if (flashCounter > 20) flashCounter = 0;

		if (magicXPBarShowTimer > 0){
			magicXPBarShowTimer--;
			if (magicXPBarShowTimer < 20)
				magicXPBarAlpha -= 0.05f;
		}

		if (runCompendiumTicker){
			fastUITicker++;
			if (fastUITicker > 40){
				fastUITicker = 0;
				slowUITicker++;
			}
		}

		slowFlashAlpha = Math.abs((flashCounter / 20f) - 0.5f) * 2;
		fastFlashAlpha = Math.abs((flashCounter % 5 / 5f) - 0.5f) * 2;

		lastmillis = millis;
		millis = System.currentTimeMillis();
	}

	public void guiTick(){
		lastmillis = millis;
		millis = System.currentTimeMillis();
		accumulatedMillis += (millis - lastmillis);
		if (accumulatedMillis >= 50){
			tick();
			accumulatedMillis = 0;
		}
	}

	public static void OpenBookGUI(ItemStack stack){
		
	}

	public static void OpenCompendiumGui(ItemStack stack){
		Minecraft.getMinecraft().displayGuiScreen(new GuiCompendiumIndex());
	}

	public static void DrawIconAtXY(TextureAtlasSprite IIcon, float x, float y, float zLevel, int w, int h, boolean semitransparent){

		if (IIcon == null)
			return;

		GlStateManager.matrixMode(GL11.GL_TEXTURE);
		GlStateManager.pushMatrix();
		if (semitransparent){
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		}
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

		Tessellator tessellator = Tessellator.getInstance();

		boolean drawing = false;
		try{
			tessellator.getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX);
		} catch (IllegalStateException e) {
			drawing = true;
			tessellator.draw();
			tessellator.getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX);
		}

		tessellator.getBuffer().pos(x, y + h, zLevel).tex( IIcon.getMinU(), IIcon.getMaxV()).endVertex();
		tessellator.getBuffer().pos(x + w, y + h, zLevel).tex( IIcon.getMaxU(), IIcon.getMaxV()).endVertex();
		tessellator.getBuffer().pos(x + w, y, zLevel).tex( IIcon.getMaxU(), IIcon.getMinV()).endVertex();
		tessellator.getBuffer().pos(x, y, zLevel).tex( IIcon.getMinU(), IIcon.getMinV()).endVertex();
		
		tessellator.draw();

		if (semitransparent){
			GlStateManager.disableBlend();
		}
		GlStateManager.popMatrix();
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);

		if (drawing)
			tessellator.getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX);
	}
	
	public static void DrawIconAtXY(TextureAtlasSprite IIcon, float x, float y, float zLevel, int w, int h, int color){

		if (IIcon == null)
			return;
		//GlStateManager.matrixMode(GL11.GL_TEXTURE);
		//GlStateManager.pushMatrix();
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

		Tessellator tessellator = Tessellator.getInstance();

		boolean drawing = ReflectionHelper.getPrivateValue(VertexBuffer.class, tessellator.getBuffer(), "isDrawing", "field_179010_r");
		if (drawing)
			tessellator.draw();

		tessellator.getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		
		//LogHelper.info(color);
		
		tessellator.getBuffer().pos(x, y + h, zLevel).tex( IIcon.getMinU(), IIcon.getMaxV())
		.color(RenderUtils.getRed(color), RenderUtils.getGreen(color), RenderUtils.getBlue(color), 1.0f)
		.endVertex();
		tessellator.getBuffer().pos(x + w, y + h, zLevel).tex( IIcon.getMaxU(), IIcon.getMaxV())
		.color(RenderUtils.getRed(color), RenderUtils.getGreen(color), RenderUtils.getBlue(color), 1.0f)
		.endVertex();
		tessellator.getBuffer().pos(x + w, y, zLevel).tex( IIcon.getMaxU(), IIcon.getMinV())
		.color(RenderUtils.getRed(color), RenderUtils.getGreen(color), RenderUtils.getBlue(color), 1.0f)
		.endVertex();
		tessellator.getBuffer().pos(x, y, zLevel).tex( IIcon.getMinU(), IIcon.getMinV())
		.color(RenderUtils.getRed(color), RenderUtils.getGreen(color), RenderUtils.getBlue(color), 1.0f)
		.endVertex();
		
		tessellator.draw();
		//GlStateManager.popMatrix();
		//GlStateManager.matrixMode(GL11.GL_MODELVIEW);

		if (drawing)
			tessellator.getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX);
	}

	public static void DrawItemAtXY(ItemStack stack, float x, float y, float zLevel){
		DrawItemAtXY(stack, x, y, zLevel, 1.0f);
	}

	public static void DrawItemAtXY(ItemStack stack, float x, float y, float zLevel, float scale){
		Minecraft.getMinecraft().renderEngine.bindTexture(LOCATION_BLOCKS_TEXTURE);
		if (stack == null)
			return;
		GlStateManager.pushAttrib();
		GlStateManager.color(1, 1, 1, 1);

		RenderHelper.disableStandardItemLighting();
		RenderHelper.enableGUIStandardItemLighting();
		//GL11.glDisable(GL11.GL_BLEND);
		//GL11.glDisable(GL11.GL_LIGHTING);

		if (scale != 1.0f){
			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y, zLevel);
			GlStateManager.scale(scale, scale, 1);
			itemRenderer.renderItemIntoGUI(stack, 0, 0);
			GlStateManager.popMatrix();
		}else {
			if (stack.getItem() instanceof ItemSpellComponent) {
				Minecraft.getMinecraft().renderEngine.bindTexture(LOCATION_BLOCKS_TEXTURE);
				TextureAtlasSprite icon = SpellIconManager.INSTANCE.getSprite(ArsMagicaAPI.getSpellRegistry().getValue(ArsMagicaAPI.getSkillRegistry().getObjectById(stack.getItemDamage()).getRegistryName()).getRegistryName().toString());
				GlStateManager.color(1, 1, 1, 1);
				if (icon != null)
					DrawIconAtXY(icon, x, y, zLevel + 1, 16, 16, false);
			} 
			else if (stack.getItem().equals(ItemDefs.etherium)) {
				Minecraft.getMinecraft().renderEngine.bindTexture(LOCATION_BLOCKS_TEXTURE);
				TextureAtlasSprite icon = AMParticleIcons.instance.getIconByName("lights");
				int color = 0;
				for (PowerTypes type : PowerTypes.all()) {
					if ((stack.getItemDamage() & type.ID()) == type.ID()) {
						color |= type.getColor();
					}
				}
				if (icon != null);
					DrawIconAtXY(icon, x, y, zLevel, 16, 16, color);
			} 
			else {
				RenderHelper.enableGUIStandardItemLighting();
				itemRenderer.renderItemIntoGUI(stack, (int)x, (int)y);
			}
		}
		RenderHelper.enableStandardItemLighting();
		//GL11.glDisable(GL11.GL_ALPHA_TEST);
		GlStateManager.popAttrib();
	}

	public static void drawCompendiumText(String text, int x_start, int y_start, int max_width, int start_color, FontRenderer fontRenderer){
		int cur_color = start_color;
		text = text.replaceAll("!d", "\n\n").replace("!l", "\n").replaceAll("-", "- ");
		String[] words = text.split(" ");
		int lineLength = 0;
		int posX = x_start;
		int posY = y_start;

		for (String word : words){
			if (word.equals("")) continue;
			int linesBefore = 0;
			int linesAfter = 0;

			int wordLength = fontRenderer.getStringWidth(word.replaceAll("#.", "") + " ");
			if (lineLength + wordLength > max_width){
				posY += fontRenderer.FONT_HEIGHT;
				posX = x_start;
				lineLength = 0;
			}

			while (word.startsWith("\n")){
				linesBefore++;
				word = word.substring(1);
			}
			while (word.endsWith("\n")){
				linesAfter++;
				word = word.substring(0, word.length() - 1);
			}
			//word = word.replaceAll("\n", "");
			
			if (linesBefore > 0){
				posY += fontRenderer.FONT_HEIGHT * linesBefore;
				posX = x_start - 13;
				lineLength = 0;
			}

			cur_color = parseColorAndDraw(word, posX, posY, cur_color, fontRenderer);

			posX += wordLength;
			lineLength += wordLength;

			if (linesAfter > 0){
				posY += fontRenderer.FONT_HEIGHT * linesAfter;
				posX = x_start;
				lineLength = 0;
			}
		}
	}

	protected static void drawHoveringText(List<String> par1List, int par2, int par3, FontRenderer font, int width, int height){
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

			if (i1 + k > width){
				i1 -= 28 + k;
			}

			if (j1 + k1 + 6 > height){
				j1 = height - k1 - 6;
			}

			int l1 = -267386864;
			drawGradientRect(i1 - 3, j1 - 4, i1 + k + 3, j1 - 3, l1, l1);
			drawGradientRect(i1 - 3, j1 + k1 + 3, i1 + k + 3, j1 + k1 + 4, l1, l1);
			drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 + k1 + 3, l1, l1);
			drawGradientRect(i1 - 4, j1 - 3, i1 - 3, j1 + k1 + 3, l1, l1);
			drawGradientRect(i1 + k + 3, j1 - 3, i1 + k + 4, j1 + k1 + 3, l1, l1);
			int i2 = 1347420415;
			int j2 = (i2 & 16711422) >> 1 | i2 & -16777216;
			drawGradientRect(i1 - 3, j1 - 3 + 1, i1 - 3 + 1, j1 + k1 + 3 - 1, i2, j2);
			drawGradientRect(i1 + k + 2, j1 - 3 + 1, i1 + k + 3, j1 + k1 + 3 - 1, i2, j2);
			drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 - 3 + 1, i2, i2);
			drawGradientRect(i1 - 3, j1 + k1 + 2, i1 + k + 3, j1 + k1 + 3, j2, j2);

			for (int k2 = 0; k2 < par1List.size(); ++k2){
				String s1 = (String)par1List.get(k2);
				font.drawStringWithShadow(s1, i1, j1, -1);

				if (k2 == 0){
					j1 += 2;
				}

				j1 += 10;
			}
			GlStateManager.enableLighting();
			GlStateManager.enableDepth();
			RenderHelper.enableStandardItemLighting();
			GlStateManager.enableRescaleNormal();
		}
	}

	protected static void drawGradientRect(int par1, int par2, int par3, int par4, int par5, int par6){
		float f = (par5 >> 24 & 255) / 255.0F;
		float f1 = (par5 >> 16 & 255) / 255.0F;
		float f2 = (par5 >> 8 & 255) / 255.0F;
		float f3 = (par5 & 255) / 255.0F;
		float f4 = (par6 >> 24 & 255) / 255.0F;
		float f5 = (par6 >> 16 & 255) / 255.0F;
		float f6 = (par6 >> 8 & 255) / 255.0F;
		float f7 = (par6 & 255) / 255.0F;
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Tessellator tessellator = Tessellator.getInstance();
		tessellator.getBuffer().begin(7, DefaultVertexFormats.POSITION_COLOR);
		tessellator.getBuffer().pos(par3, par2, zLevel).color(f1, f2, f3, f).endVertex();
		tessellator.getBuffer().pos(par1, par2, zLevel).color(f1, f2, f3, f).endVertex();;
		tessellator.getBuffer().pos(par1, par4, zLevel).color(f5, f6, f7, f4).endVertex();
		tessellator.getBuffer().pos(par3, par4, zLevel).color(f5, f6, f7, f4).endVertex();
		tessellator.draw();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();
	}

	public static void line2d(float src_x, float src_y, float dst_x, float dst_y, float zLevel, int color){
		GlStateManager.disableTexture2D();
		GlStateManager.glLineWidth(1f);
		GlStateManager.color(((color & 0xFF0000) >> 16) / 255.0f, ((color & 0x00FF00) >> 8) / 255.0f, (color & 0x0000FF) / 255.0f);
		GlStateManager.glBegin(GL11.GL_LINES);
		GlStateManager.glVertex3f(src_x, src_y, zLevel);
		GlStateManager.glVertex3f(dst_x, dst_y, zLevel);
		GlStateManager.glEnd();
		GlStateManager.color(1.0f, 1.0f, 1.0f);
		GlStateManager.enableTexture2D();
	}

	public static void line2d(float src_x, float src_y, float dst_x, float dst_y, float zLevel, float weight, int color){
		GlStateManager.disableTexture2D();
		GlStateManager.glLineWidth(weight);
		GlStateManager.color(((color & 0xFF0000) >> 16) / 255.0f, ((color & 0x00FF00) >> 8) / 255.0f, (color & 0x0000FF) / 255.0f);
		GlStateManager.glBegin(GL11.GL_LINES);
		GlStateManager.glVertex3f(src_x, src_y, zLevel);
		GlStateManager.glVertex3f(dst_x, dst_y, zLevel);
		GlStateManager.glEnd();
		GlStateManager.color(1.0f, 1.0f, 1.0f);
		GlStateManager.enableTexture2D();
	}

	public static void gradientline2d(float src_x, float src_y, float dst_x, float dst_y, float zLevel, int color1, int color2){
		GlStateManager.disableTexture2D();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.glLineWidth(1f);
		GlStateManager.glBegin(GL11.GL_LINES);
		GlStateManager.color((color1 & 0xFF0000) >> 16, (color1 & 0x00FF00) >> 8, color1 & 0x0000FF);
		GlStateManager.glVertex3f(src_x, src_y, zLevel);
		GlStateManager.color((color2 & 0xFF0000) >> 16, (color2 & 0x00FF00) >> 8, color2 & 0x0000FF);
		GlStateManager.glVertex3f(dst_x, dst_y, zLevel);
		GlStateManager.glEnd();
		GlStateManager.color(1.0f, 1.0f, 1.0f);
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.enableTexture2D();
	}

	public static void fractalLine2d(int src_x, int src_y, int dst_x, int dst_y, float zLevel, int color, float displace){
		fractalLine2d(src_x, src_y, dst_x, dst_y, zLevel, color, displace, fractalLineDetail);
	}

	public static void fractalLine2d(int src_x, int src_y, int dst_x, int dst_y, float zLevel, int color, float displace, float fractalDetail){
		if (displace < fractalDetail){
			line2d(src_x, src_y, dst_x, dst_y, zLevel, color);
		}else{
			int mid_x = (dst_x + src_x) / 2;
			int mid_y = (dst_y + src_y) / 2;
			mid_x += (rand.nextFloat() - 0.5) * displace;
			mid_y += (rand.nextFloat() - 0.5) * displace;
			fractalLine2d(src_x, src_y, mid_x, mid_y, zLevel, color, displace / 2f, fractalDetail);
			fractalLine2d(dst_x, dst_y, mid_x, mid_y, zLevel, color, displace / 2f, fractalDetail);
		}
	}

	private static int parseColorAndDraw(String word, int posX, int posY, int cur_color, FontRenderer fontRenderer){
		int index = word.indexOf("#");
		int color = cur_color;
		while (index > -1 && index < word.length() - 1){

			String toRender = word.substring(0, index);
			fontRenderer.drawString(toRender, posX, posY, color);
			posX += fontRenderer.getStringWidth(toRender);

			char nextChar = word.charAt(index + 1);
			switch (nextChar){
			case '0':
				color = 0x000000;
				break;
			case '1':
				color = 0x0000BF;
				break;
			case '2':
				color = 0x00BF00;
				break;
			case '3':
				color = 0x00BFBF;
				break;
			case '4':
				color = 0xBF0000;
				break;
			case '5':
				color = 0xBF00BF;
				break;
			case '6':
				color = 0xBFBF00;
				break;
			case '7':
				color = 0xBFBFBF;
				break;
			case '8':
				color = 0x404040;
				break;
			case '9':
				color = 0x4040FF;
				break;
			case 'a':
				color = 0x40FF40;
				break;
			case 'b':
				color = 0x40FFFF;
				break;
			case 'c':
				color = 0xFF4040;
				break;
			case 'd':
				color = 0xFF40FF;
				break;
			case 'e':
				color = 0xFFFF40;
				break;
			case 'f':
				color = 0xFFFFFF;
				break;
			}

			word = word.substring(index + 2);
			index = word.indexOf("#");
		}

		fontRenderer.drawString(word, posX, posY, color);

		return color;
	}

	public static int createRenderTexture(){
		int colorTextureID = GL11.glGenTextures();
		GlStateManager.bindTexture(colorTextureID);                                    // Bind the colorbuffer texture
		GlStateManager.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);                // make it linear filterd
		GlStateManager.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, 512, 512, 0, GL11.GL_RGBA, GL11.GL_INT, null);    // Create the texture data

		return colorTextureID;
	}

	public static int createFBO(int textureID, int w, int h, boolean depthBuffer){
		boolean FBOEnabled = GLContext.getCapabilities().GL_EXT_framebuffer_object;
		if (!FBOEnabled)
			return -1;
		IntBuffer buffer = ByteBuffer.allocateDirect(1 * 4).order(ByteOrder.nativeOrder()).asIntBuffer(); // allocate a 1 int byte buffer
		EXTFramebufferObject.glGenFramebuffersEXT(buffer); // generate
		int myFBOId = buffer.get();
		EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, myFBOId);
		EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT, GL11.GL_TEXTURE_2D, textureID, 0);
		int framebuffer = EXTFramebufferObject.glCheckFramebufferStatusEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT);
		switch (framebuffer){
		case EXTFramebufferObject.GL_FRAMEBUFFER_COMPLETE_EXT:
			break;
		case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT:
			throw new RuntimeException("FrameBuffer: " + myFBOId + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT exception");
		case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT:
			throw new RuntimeException("FrameBuffer: " + myFBOId + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT exception");
		case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT:
			throw new RuntimeException("FrameBuffer: " + myFBOId + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT exception");
		case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT:
			throw new RuntimeException("FrameBuffer: " + myFBOId + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT exception");
		case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT:
			throw new RuntimeException("FrameBuffer: " + myFBOId + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT exception");
		case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT:
			throw new RuntimeException("FrameBuffer: " + myFBOId + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT exception");
		default:
			throw new RuntimeException("Unexpected reply from glCheckFramebufferStatusEXT: " + framebuffer);
		}

		return myFBOId;
	}

	public static boolean bindFBOTexture(int FBOId, int w, int h){
		try{
			EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, FBOId);
			GlStateManager.pushAttrib();
			GlStateManager.viewport(0, 0, w, h);
			GlStateManager.popAttrib();

			return true;
		}catch (Throwable t){
			return false;
		}
	}

	public static boolean unbindFBOTexture(){
		try{
			EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);
			GlStateManager.popAttrib();

			return true;
		}catch (Throwable t){
			return false;
		}
	}

	public static void flipView(float f){
		float flip = EntityExtension.For(Minecraft.getMinecraft().player).getFlipRotation();
		float lastFlip = EntityExtension.For(Minecraft.getMinecraft().player).getPrevFlipRotation();
		GlStateManager.rotate(lastFlip + (flip - lastFlip) * f, 0, 0, 1);
	}

	public static void shiftView(float f){
		EntityPlayer entity = Minecraft.getMinecraft().player;
//		int viewSet = Minecraft.getMinecraft().gameSettings.thirdPersonView;
//		if (viewSet == 0){
//			EntityExtension exProps = EntityExtension.For(entity);
//			if (exProps.getShrinkPct() > 0f){
//				float amt = exProps.getPrevShrinkPct() + (exProps.getShrinkPct() - exProps.getPrevShrinkPct()) * f;
//				GlStateManager.translate(0, 1 * amt, 0);
//			}
//		}

		float flip = EntityExtension.For(entity).getFlipRotation();
		float lastFlip = EntityExtension.For(entity).getPrevFlipRotation();
		if (flip > 0){
			//float smoothedFlip = lastFlip + ((flip - lastFlip) * f);
			//GlStateManager.translate(0, (entity.eyeHeight * (smoothedFlip / 180f) - 0.1f), 0);
			GlStateManager.rotate(lastFlip + (flip - lastFlip) * f, 0, 0, 1);
			//flipView(f);
		}
	}

	public static void overrideKeyboardInput(){
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.player != null && mc.world != null && EntityExtension.For(mc.player).shouldReverseInput()){
			EntityPlayerSP player = Minecraft.getMinecraft().player;
			if (mc.gameSettings.keyBindLeft.isKeyDown()){
				LogHelper.debug("Override Left");
				player.movementInput.moveStrafe -= 2;
			}

			if (mc.gameSettings.keyBindRight.isKeyDown()){
				LogHelper.debug("Override Rights");
				player.movementInput.moveStrafe += 2;
			}

			if (mc.player.isPotionActive(PotionEffectsDefs.scrambleSynapses)){
				if (mc.gameSettings.keyBindForward.isKeyDown()){
					player.movementInput.moveForward -= 2;
				}
				if (mc.gameSettings.keyBindBack.isKeyDown()){
					player.movementInput.moveForward += 2;
				}
			}
		}
	}

	public static boolean overrideMouseInput(EntityRenderer renderer, float f, boolean b){
		Minecraft mc = Minecraft.getMinecraft();

		if (!mc.inGameHasFocus || mc.player == null || mc.world == null)
			return true;

		if (!mc.player.isPotionActive(PotionEffectsDefs.scrambleSynapses)){
			return true;
		}

		mc.mouseHelper.mouseXYChange();
		float f1 = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
		float f2 = f1 * f1 * f1 * 8.0F;
		float f3 = (float)mc.mouseHelper.deltaX * f2;
		float f4 = (float)mc.mouseHelper.deltaY * f2;
		byte b0 = -1;

		if (mc.gameSettings.invertMouse){
			b0 = 1;
		}

		if (mc.gameSettings.smoothCamera){
			String[] scy = {"field_78496_H", "smoothCamYaw"};
			String[] scp = {"field_78521_m", "smoothCamPitch"};
			String[] scpt = {"field_78533_p", "smoothCamPartialTicks"};
			String[] scfx = {"field_78518_n", "smoothCamFilterX"};
			String[] scfy = {"field_78499_K", "smoothCamFilterY"};

			//renderer.smoothCamYaw += f3;
			ReflectionHelper.setPrivateValue(EntityRenderer.class, renderer, (Float)ReflectionHelper.getPrivateValue(EntityRenderer.class, renderer, scy) - f3, scy);
			//renderer.smoothCamPitch += f4;
			ReflectionHelper.setPrivateValue(EntityRenderer.class, renderer, (Float)ReflectionHelper.getPrivateValue(EntityRenderer.class, renderer, scp) - f4, scp);
			//float f5 = f - renderer.smoothCamPartialTicks;
			float f5 = f - (Float)ReflectionHelper.getPrivateValue(EntityRenderer.class, renderer, scpt);
			//renderer.smoothCamPartialTicks = f;
			ReflectionHelper.setPrivateValue(EntityRenderer.class, renderer, f, scpt);
			//f3 = renderer.smoothCamFilterX * f5;
			f3 = (Float)ReflectionHelper.getPrivateValue(EntityRenderer.class, renderer, scfx) * f5;
			//f4 = renderer.smoothCamFilterY * f5;
			f4 = (Float)ReflectionHelper.getPrivateValue(EntityRenderer.class, renderer, scfy) * f5;
		}
			mc.player.setLocationAndAngles(mc.player.posX, mc.player.posY, mc.player.posZ, -f3, f4 * (float)b0);
		return false;
	}
	

}
