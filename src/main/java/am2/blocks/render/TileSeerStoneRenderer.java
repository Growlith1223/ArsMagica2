package am2.blocks.render;

import am2.blocks.BlockSeerStone;
import am2.blocks.tileentity.TileEntitySeerStone;
import am2.models.ModelSeerStone;
import am2.power.PowerTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class TileSeerStoneRenderer extends TileEntitySpecialRenderer<TileEntitySeerStone>{

	private static final float TextureSize = 640;
	private static final float FrameSize = 64;

	private ResourceLocation block;
	private ResourceLocation eye;

	public TileSeerStoneRenderer(){
		model = new ModelSeerStone();
		block = new ResourceLocation("arsmagica2", "textures/blocks/custom/block_seer_stone.png");
		eye = new ResourceLocation("arsmagica2", "textures/blocks/custom/eye_bw.png");
	}

	public void renderTileEntityAt(TileEntitySeerStone tile, double d, double d1, double d2, float f, int destroyStage){
		EnumFacing i = EnumFacing.SOUTH;

		if (destroyStage != -10){
			i = tile.getWorld().getBlockState(tile.getPos()).getValue(BlockSeerStone.FACING);
		}

		GlStateManager.pushMatrix();

		EnumFacing meta = i;


		switch (meta){
		case UP:
			GlStateManager.translate((float)d + 0.5f, (float)d1 - 0.5F, (float)d2 + 0.5F); //size
			GlStateManager.rotate(180, 1.0F, 0.0F, 0.0F); //rotate based on metadata
			break;
		case DOWN:
			GlStateManager.translate((float)d + 0.5f, (float)d1 + 1.5F, (float)d2 + 0.5F); //size
			break;
		case SOUTH:
			GlStateManager.translate((float)d + 0.5f, (float)d1 + 0.5F, (float)d2 - 0.5F); //size
			GlStateManager.rotate(270, 1.0F, 0.0F, 0.0F); //rotate based on metadata
			break;
		case NORTH:
			GlStateManager.translate((float)d + 0.5f, (float)d1 + 0.5F, (float)d2 + 1.5F); //size
			GlStateManager.rotate(90, 1.0F, 0.0F, 0.0F); //rotate based on metadata
			break;
		case EAST:
			GlStateManager.translate((float)d - 0.5f, (float)d1 + 0.5F, (float)d2 + 0.5F); //size
			GlStateManager.rotate(90, 0.0F, 0.0F, 1.0F); //rotate based on metadata
			break;
		case WEST:
			GlStateManager.translate((float)d + 1.5F, (float)d1 + 0.5F, (float)d2 + 0.5F); //size
			GlStateManager.rotate(270, 0.0F, 0.0F, 1.0F); //rotate based on metadata
			break;
		}


		GlStateManager.scale(1.0F, -1F, -1F); //if you read this comment out this line and you can see what happens
		bindTexture(block);
		model.renderModel(0.0625F); //renders and yes 0.0625 is a random number
		GlStateManager.popMatrix();
		
		if (tile.ShouldAnimate())
			RenderEye(PowerTypes.NEUTRAL, tile.getAnimationIndex(), d, d1, d2, meta);
	}

	private void RenderEye(PowerTypes powerType, int index, double d, double d1, double d2, EnumFacing meta){
		bindTexture(eye);
		GlStateManager.pushMatrix();
		GlStateManager.translate(d + 0.5, d1 + 0.5, d2 + 0.5);
		GlStateManager.scale(0.5F, 0.5F, 0.5F);
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		Tessellator tessellator = Tessellator.getInstance();
		tessellator.getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);

		if (powerType == PowerTypes.DARK)
			GlStateManager.color(1.0f, 0.0f, 0.0f, 1.0f);
		else if (powerType == PowerTypes.LIGHT)
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		else if (powerType == PowerTypes.NEUTRAL)
			GlStateManager.color(0.0f, 0.2f, 1.0f, 1.0f);

		renderArsMagicaEffect(tessellator, index, meta);
		tessellator.draw();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
		//GL11.glDisable(32826 /*GL_RESCALE_NORMAL_EXT*/);
	}

	private void renderArsMagicaEffect(Tessellator tessellator, int i, EnumFacing meta){
		float iof = TextureSize / FrameSize;
		float foi = FrameSize / TextureSize;

		float TLX = (i % iof) * foi;
		float BRX = (i % iof) * foi + foi;
		float TLY = (float)(Math.floor(i / iof) * foi);
		float BRY = (float)(Math.floor(i / iof) * foi + foi);

		float f4 = 1.0F;
		float f5 = 0.5F;
		float f6 = 0.25F;

		//tessellator.getBuffer().putBrightness4(15728864, 15728864, 15728864, 15728864);

		//GL11.glRotatef(90, 0.0f, 1.0f, 0f);
		//GL11.glRotatef(90, 0.0f, 1.0f, 0f);


		switch (meta){
		case DOWN: break;
		case UP:
			GlStateManager.translate(0.0f, -0.4f, 0.0f);
			break;
		case SOUTH:
			GlStateManager.translate(0.0f, -0.2f, -0.4f);
			break;
		case NORTH:
			GlStateManager.translate(0.0f, -0.2f, 0.4f);
			break;
		case EAST:
			GlStateManager.translate(-0.4f, -0.2f, 0.0f);
			break;
		case WEST:
			GlStateManager.translate(0.4f, -0.2f, 0.0f);
			break;
		}

		GlStateManager.rotate(-Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(Minecraft.getMinecraft().getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);

		GlStateManager.rotate(180F, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);
		
		tessellator.getBuffer().pos(0.0F - f5, 0.0F - f6, 0.0D).tex(TLX, BRY).normal(0.0F, 1.0F, 0.0F).endVertex();
		tessellator.getBuffer().pos(f4 - f5, 0.0F - f6, 0.0D).tex(BRX, BRY).normal(0.0F, 1.0F, 0.0F).endVertex();
		tessellator.getBuffer().pos(f4 - f5, f4 - f6, 0.0D).tex(BRX, TLY).normal(0.0F, 1.0F, 0.0F).endVertex();
		tessellator.getBuffer().pos(0.0F - f5, f4 - f6, 0.0D).tex(TLX, TLY).normal(0.0F, 1.0F, 0.0F).endVertex();
	}

	private ModelSeerStone model;
}
