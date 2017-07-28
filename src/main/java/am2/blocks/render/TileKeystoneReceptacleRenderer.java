package am2.blocks.render;

import org.lwjgl.opengl.GL11;

import am2.blocks.tileentity.TileEntityKeystoneRecepticle;
import am2.entity.render.RenderManaVortex;
import am2.gui.AMGuiIcons;
import am2.models.ModelKeystoneRecepticle;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;

public class TileKeystoneReceptacleRenderer extends TileEntitySpecialRenderer<TileEntityKeystoneRecepticle>{

	private final ModelKeystoneRecepticle model;
	private static final ResourceLocation portal = TextureMap.LOCATION_BLOCKS_TEXTURE;
	private final ResourceLocation rLoc;

	public TileKeystoneReceptacleRenderer(){
		model = new ModelKeystoneRecepticle();

		rLoc = new ResourceLocation("arsmagica2", "textures/blocks/custom/keystone_receptacle.png");
	}

	public void renderTileEntityAt(TileEntityKeystoneRecepticle tile, double x, double y, double z, float partialTicks,int destroyStage){
		int i = 3;

		if (tile.getWorld() != null){
			i = tile.getBlockMetadata();
		}
		int j = 0;

		if (i == 0 || i == 4){
			j = 0;
		}

		if (i == 1){
			j = 270;
		}

		if (i == 2){
			j = 180;
		}

		if (i == 3){
			j = 90;
		}

		bindTexture(rLoc);
		GL11.glPushMatrix(); //start
		GL11.glTranslatef((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F); //size
		GL11.glRotatef(j, 0.0F, 1.0F, 0.0F); //rotate based on metadata
		GL11.glScalef(1.0F, -1F, -1F); //if you read this comment out this line and you can see what happens
		model.renderModel(0.0625F); //renders and yes 0.0625 is a random number
		GL11.glPopMatrix(); //end	

		if (tile.isActive()){
			bindTexture(portal);

			GL11.glPushMatrix();

			GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

			GL11.glTranslatef((float)x + 0.5f, (float)y - 2.5F, (float)z + 0.5f);
			GL11.glScaled(4, 4, 4);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.003921569F);
			GL11.glDepthMask(false);
			Tessellator tessellator = Tessellator.getInstance();

			GL11.glRotatef(j, 0.0F, 1.0F, 0.0F); //rotate based on metadata

			//apply portal colors here
			GL11.glColor4f(1, 1, 1, 1);

			renderArsMagicaEffect(tessellator);

			GL11.glDisable(GL11.GL_BLEND);

			GL11.glPopAttrib();
			GL11.glPopMatrix();
		}
	}

	private void renderArsMagicaEffect(Tessellator tessellator){
		GL11.glPushMatrix();
		GL11.glDisable(GL11.GL_LIGHTING);
		renderSprite(tessellator);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glPopMatrix();
	}

	private void renderSprite(Tessellator tessellator){

		TextureAtlasSprite IIcon = AMGuiIcons.gatewayPortal;

		float min_u = IIcon.getMinU();
		float max_u = IIcon.getMaxU();
		float min_v = IIcon.getMinV();
		float max_v = IIcon.getMaxV();

		float f4 = 1.0F;
		float f5 = 0.5F;
		float f6 = 0.25F;
		int i = 0xF00F0;
        int j = i >> 16 & 65535;
        int k = i & 65535;
		tessellator.getBuffer().begin(7, RenderManaVortex.POS_TEX_LIGHTMAP);
		tessellator.getBuffer().pos(0.0F - f5, 0.0F - f6, 0.0D).tex(min_u, max_v).lightmap(j, k).endVertex();
		tessellator.getBuffer().pos(f4 - f5, 0.0F - f6, 0.0D).tex(max_u, max_v).lightmap(j, k).endVertex();
		tessellator.getBuffer().pos(f4 - f5, f4 - f6, 0.0D).tex(max_u, min_v).lightmap(j, k).endVertex();
		tessellator.getBuffer().pos(0.0F - f5, f4 - f6, 0.0D).tex(min_u, min_v).lightmap(j, k).endVertex();
		tessellator.draw();

		GL11.glRotatef(180, 0, 1, 0);

		tessellator.getBuffer().begin(7, RenderManaVortex.POS_TEX_LIGHTMAP);
		tessellator.getBuffer().pos(0.0F - f5, 0.0F - f6, 0.0D).tex(min_u, max_v).lightmap(j, k).endVertex();
		tessellator.getBuffer().pos(f4 - f5, 0.0F - f6, 0.0D).tex(max_u, max_v).lightmap(j, k).endVertex();
		tessellator.getBuffer().pos(f4 - f5, f4 - f6, 0.0D).tex(max_u, min_v).lightmap(j, k).endVertex();
		tessellator.getBuffer().pos(0.0F - f5, f4 - f6, 0.0D).tex(min_u, min_v).lightmap(j, k).endVertex();
		tessellator.draw();
	}

}
