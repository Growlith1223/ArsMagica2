package am2.blocks.render;

import org.lwjgl.opengl.GL11;

import am2.blocks.tileentity.TileEntitySummoner;
import am2.models.ModelSummoner;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;

public class TileSummonerRenderer extends TileEntitySpecialRenderer<TileEntitySummoner>{
	private ResourceLocation rLoc;
	private ResourceLocation powered;
	private ModelSummoner model;

	public TileSummonerRenderer(){
		model = new ModelSummoner();
		rLoc = new ResourceLocation("arsmagica2", "textures/blocks/custom/block_summoner.png");
		powered = new ResourceLocation("arsmagica2", "textures/blocks/custom/essence_conduit.png");
	}
	
	@Override
	public void renderTileEntityAt(TileEntitySummoner tile, double x, double y, double z, float partialTicks, int destroyStage) {
		int i = 2;
		int y2 = 0;

		if (destroyStage != -10){
			i = tile.getBlockMetadata() & 3;
			y2 = (tile.getBlockMetadata() & 12) >> 2;
		}
		int j = (i + 1) * 90;
		int n = 0;
		if (y2 == 1){
			n = 90;
		}else if (y2 == 2){
			n = -90;
		}

		bindTexture(rLoc);
		GL11.glPushMatrix(); //start
		GL11.glTranslatef((float)x + 0.5f, (float)y + 1.5f, (float)z + 0.5f); //size
		GL11.glRotatef(j, 0.0F, 1.0F, 0.0F); //rotate based on metadata
		GL11.glTranslatef(0, -1f, 0);
		GL11.glRotatef(n, 0.0f, 0.0f, 1.0f);
		GL11.glTranslatef(0, 1f, 0);
		GL11.glScalef(1.0F, -1F, -1F);
		model.renderModel(0.0625F);
		bindTexture(powered);
		model.renderCrystal(tile, 0.0625f);
		GL11.glPopMatrix(); //end
	}

}
