package am2.entity.render;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import am2.entity.EntityManaVortex;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderManaVortex extends Render<EntityManaVortex>{

	public RenderManaVortex(RenderManager renderManager) {
		super(renderManager);
	}
	
	public static final VertexFormat POS_TEX_LIGHTMAP = new VertexFormat();
	static {
		POS_TEX_LIGHTMAP.addElement(DefaultVertexFormats.POSITION_3F);
		POS_TEX_LIGHTMAP.addElement(DefaultVertexFormats.TEX_2F);
		POS_TEX_LIGHTMAP.addElement(DefaultVertexFormats.TEX_2S);
	}

	private static final ResourceLocation vortex = new ResourceLocation("arsmagica2", "textures/blocks/custom/magic_fabricator_2.png");
	private static final ResourceLocation wisp = new ResourceLocation("arsmagica2", "textures/mobs/mob_wisp.png");

	@Override
	public void doRender(EntityManaVortex var1, double var2, double var4, double var6, float var8, float var9){
		if (var1 == null || !var1.world.isRemote){
			return;
		}
		GL11.glPushMatrix();
		GL11.glTranslatef((float)var2, (float)var4, (float)var6);
		GL11.glEnable(32826 /*GL_RESCALE_NORMAL_EXT*/);
		GL11.glDepthMask(false);
		GL11.glEnable(3042);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		if (this.renderManager.renderEngine == null){
			return;
		}
		Tessellator tessellator = Tessellator.getInstance();

		renderArsMagicaEffect(tessellator, var1.ticksExisted, var1.getTicksToExist(), var1.getRotation(), var1.getScale(), var1.getManaStolenPercent());

		GL11.glDisable(32826 /*GL_RESCALE_NORMAL_EXT*/);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(3042);
		GL11.glDepthMask(true);
		GL11.glPopMatrix();
	}

	private void renderArsMagicaEffect(Tessellator tessellator, int ticks, int life, float rotation, float scale, float percent){
		GL11.glPushMatrix();
		GL11.glRotatef(180F - renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(-renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

		GL11.glColor4f(0.2f, 0.5f, 1.0f, 1.0f);

		Minecraft.getMinecraft().renderEngine.bindTexture(vortex);
		GL11.glPushMatrix();
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glTranslatef(0.0f, 0.25f, 0.0f);
		GL11.glRotatef(rotation, 0.0f, 0.0f, 1.0f);
		GL11.glScalef(scale * 2, scale * 2, scale * 2);
		GL11.glTranslatef(0.0f, -0.25f, 0.0f);
		renderSprite(tessellator);
		GL11.glPopMatrix();

		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

		Minecraft.getMinecraft().renderEngine.bindTexture(wisp);
		GL11.glPushMatrix();
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glTranslatef(0.0f, 0.25f, 0.0f);
		GL11.glRotatef(rotation, 0.0f, 0.0f, 1.0f);
		GL11.glScalef(scale * 3, scale * 3, scale * 3);
		GL11.glTranslatef(0.0f, -0.25f, 0.0f);
		renderSprite(tessellator);
		GL11.glPopMatrix();

		GL11.glPopMatrix();

		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		RenderHelper.disableStandardItemLighting();
		float var4 = ((float)ticks) / life;
		float var5 = 0.0F;

		if (var4 > 0.8F){
			var5 = (var4 - 0.8F) / 0.2F;
		}

		Random var6 = new Random(432L);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDepthMask(false);
		GL11.glPushMatrix();
		GL11.glTranslatef(0.0F, 0.3F, 0.0F);

		for (int var7 = 0; var7 < (var4 + var4 * var4) / 2.0F * 60.0F; ++var7){
			GL11.glRotatef(var6.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(var6.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(var6.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
			GL11.glRotatef(var6.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(var6.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(var6.nextFloat() * 360.0F + var4 * 90.0F, 0.0F, 0.0F, 1.0F);
			tessellator.getBuffer().begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
			float var8 = var6.nextFloat() * 2.0F + 2.0F + var5 * 0.5F;
			float var9 = var6.nextFloat() * 2.0F + 1.0F + var5 * 2.0F;
			//tessellator.setColorRGBA_I(color, (int)(255.0F * (0.2F - (var5 * 0.2f))));
			tessellator.getBuffer().pos(0.0D, 0.0D, 0.0D).color((int)(percent * 255), (int)(255 - (percent * 255)), (int)(255 - (percent * 255)), (int)(255.0F * (0.2F - (var5 * 0.2f))));
			//tessellator.setColorRGBA_I(color, 0);
			tessellator.getBuffer().pos(-0.866D * var9, var8, -0.5F * var9).color((int)(percent * 255), (int)(255 - (percent * 255)), (int)(255 - (percent * 255)), 0).endVertex();
			tessellator.getBuffer().pos(0.866D * var9, var8, -0.5F * var9).color((int)(percent * 255), (int)(255 - (percent * 255)), (int)(255 - (percent * 255)), 0).endVertex();
			tessellator.getBuffer().pos(0.0D, var8, 1.0F * var9).color((int)(percent * 255), (int)(255 - (percent * 255)), (int)(255 - (percent * 255)), 0).endVertex();
			tessellator.getBuffer().pos(-0.866D * var9, var8, -0.5F * var9).color((int)(percent * 255), (int)(255 - (percent * 255)), (int)(255 - (percent * 255)), 0).endVertex();
			tessellator.draw();
		}

		GL11.glPopMatrix();
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		RenderHelper.enableStandardItemLighting();
	}

	private void renderSprite(Tessellator tessellator){

		float TLX = 0;
		float BRX = 1;
		float TLY = 0;
		float BRY = 1;

		float f4 = 1.0F;
		float f5 = 0.5F;
		float f6 = 0.25F;
		
		int i = 15728863;
        int j = i >> 16 & 65535;
        int k = i & 65535;
		tessellator.getBuffer().begin(7, POS_TEX_LIGHTMAP);
		tessellator.getBuffer().pos(0.0F - f5, 0.0F - f6, 0.0D).tex(TLX, BRY).lightmap(j, k).endVertex();
		tessellator.getBuffer().pos(f4 - f5, 0.0F - f6, 0.0D).tex(BRX, BRY).lightmap(j, k).endVertex();
		tessellator.getBuffer().pos(f4 - f5, f4 - f6, 0.0D).tex(BRX, TLY).lightmap(j, k).endVertex();
		tessellator.getBuffer().pos(0.0F - f5, f4 - f6, 0.0D).tex(TLX, TLY).lightmap(j, k).endVertex();
		tessellator.draw();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityManaVortex entity){
		return vortex;
	}
}
