package am2.entity.render;

import org.lwjgl.opengl.GL11;

import am2.entity.EntityRiftStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class RenderRiftStorage extends Render<EntityRiftStorage> {
	
	private static final ResourceLocation TEXTURE_MAIN = new ResourceLocation("arsmagica2", "textures/mobs/rift.png");
	private static final ResourceLocation TEXTURE_SECONDARY = new ResourceLocation("arsmagica2", "textures/mobs/rift2.png");
	
	public RenderRiftStorage(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityRiftStorage entity) {
		return TEXTURE_MAIN;
	}

	public void renderRift(EntityRiftStorage entityRiftStorage, double d, double d1, double d2, float f, float f1){

		GL11.glPushMatrix();
		GL11.glTranslated(d, d1, d2);
		GL11.glEnable(32826 /*GL_RESCALE_NORMAL_EXT*/);
		GL11.glScalef(entityRiftStorage.getScale(0) * 2, entityRiftStorage.getScale(1) * 2, entityRiftStorage.getScale(0) * 2);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_SRC_ALPHA);
		//GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		Tessellator tessellator = Tessellator.getInstance();

		GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.8f);

		GL11.glDisable(GL11.GL_LIGHTING);

		GL11.glRotatef(180F - renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(-renderManager.playerViewX, 1.0F, 0.0F, 0.0F);

		GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.8f);

		Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE_MAIN);
		GL11.glPushMatrix();
		GL11.glTranslatef(0.0f, 0.25f, 0.0f);
		GL11.glRotatef(entityRiftStorage.getRotation(), 0.0f, 0.0f, 1.0f);
		GL11.glTranslatef(0.0f, -0.25f, 0.0f);
		renderSprite(tessellator);
		GL11.glPopMatrix();

		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

		Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE_SECONDARY);
		GL11.glPushMatrix();
		GL11.glTranslatef(0.0f, 0.25f, 0.0f);
		GL11.glRotatef(-entityRiftStorage.getRotation(), 0.0f, 0.0f, 1.0f);
		GL11.glTranslatef(0.0f, -0.25f, 0.1f);
		renderSprite(tessellator);
		GL11.glPopMatrix();


		GL11.glDisable(32826 /*GL_RESCALE_NORMAL_EXT*/);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glPopMatrix();
	}

	private void renderSprite(Tessellator tessellator){

		float TLX = 0;
		float BRX = 1;
		float TLY = 0;
		float BRY = 1;

		float f4 = 1.0F;
		float f5 = 0.5F;
		float f6 = 0.25F;

		tessellator.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		tessellator.getBuffer().pos(0.0F - f5, 0.0F - f6, 0.0D).tex( TLX, BRY).endVertex();
		tessellator.getBuffer().pos(f4 - f5, 0.0F - f6, 0.0D).tex( BRX, BRY).endVertex();
		tessellator.getBuffer().pos(f4 - f5, f4 - f6, 0.0D).tex( BRX, TLY).endVertex();
		tessellator.getBuffer().pos(0.0F - f5, f4 - f6, 0.0D).tex( TLX, TLY).endVertex();
		tessellator.draw();
	}

	public void doRender(EntityRiftStorage entity, double d, double d1, double d2, float f, float f1){
		try {
			renderRift(entity, d, d1, d2, f, f1);
		} catch (Exception e) {
			e.printStackTrace();
			Tessellator.getInstance().draw();
		}
	}

}
