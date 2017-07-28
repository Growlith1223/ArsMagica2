package am2.blocks.render;


import am2.blocks.tileentity.TileEntityOtherworldAura;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class TileOtherworldAuraRenderer extends TileEntitySpecialRenderer<TileEntityOtherworldAura>{

	private ResourceLocation rLoc_aura;

	public TileOtherworldAuraRenderer(){
		rLoc_aura = new ResourceLocation("arsmagica2", "textures/blocks/custom/sc_auto.png");
	}

	public void renderTileEntityAt(TileEntityOtherworldAura tile, double d1, double d2, double d3, float f, int destroyStage){
		GlStateManager.pushMatrix();
		GlStateManager.pushAttrib();

		GlStateManager.translate((float)d1 + 0.5f, (float)d2 + 0.5f, (float)d3 + 0.5f);
		GlStateManager.depthMask(false);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);


		RenderHelper.disableStandardItemLighting();

		Tessellator tessellator = Tessellator.getInstance();

		float n = 1.25f;
		float offset = 1;
		while (n > 0){
			renderArsMagicaEffect(tessellator, offset, n, f, destroyStage != -10);
			n -= 0.25f;
			offset *= -1;
		}

		RenderHelper.enableStandardItemLighting();
		GlStateManager.disableBlend();
		GlStateManager.depthMask(true);
		GlStateManager.popAttrib();
		GlStateManager.popMatrix();
	}


	private void renderArsMagicaEffect(Tessellator tessellator, float offset, float scale, float smooth, boolean rotate){
		GlStateManager.pushMatrix();
		if (rotate){
			GlStateManager.rotate(180F - Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(-Minecraft.getMinecraft().getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
		}else{
			GlStateManager.translate(0, -0.25f, 0);
			GlStateManager.rotate(90, 0, 1, 0);
		}

		Minecraft.getMinecraft().renderEngine.bindTexture(rLoc_aura);
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1, 0, 0.75f, 1);
		GlStateManager.translate(0.0f, 0.25f, 0.0f);
		GlStateManager.rotate((Minecraft.getMinecraft().player.ticksExisted + smooth) * (scale * 2) * offset, 0, 0, 1);
		float scalefactor = (float)Math.abs(Math.sin(System.currentTimeMillis() / 1000.0 * scale)) + 0.01f;
		GlStateManager.scale(scale * scalefactor, scale * scalefactor, scale * scalefactor);
		GlStateManager.translate(0.0f, -0.25f, 0.0f);
		renderSprite(tessellator);
		GlStateManager.popMatrix();

	}

	private void renderSprite(Tessellator tessellator){

		float TLX = 0;
		float BRX = 1;
		float TLY = 0;
		float BRY = 1;

		float f4 = 1.0F;
		float f5 = 0.5F;
		float f6 = 0.25F;

		try{
			tessellator.getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX);
			tessellator.getBuffer().pos(0.0F - f5, 0.0F - f6, 0.0D).tex(TLX, BRY).endVertex();
			tessellator.getBuffer().pos(f4 - f5, 0.0F - f6, 0.0D).tex(BRX, BRY).endVertex();
			tessellator.getBuffer().pos(f4 - f5, f4 - f6, 0.0D).tex(BRX, TLY).endVertex();
			tessellator.getBuffer().pos(0.0F - f5, f4 - f6, 0.0D).tex(TLX, TLY).endVertex();;
			tessellator.draw();
		}catch (Throwable t){
		}
	}
}
