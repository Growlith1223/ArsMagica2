package am2.blocks.render;

import am2.defs.BlockDefs;
import net.minecraft.init.Blocks;
import org.lwjgl.opengl.GL11;

import am2.blocks.BlockEssenceConduit;
import am2.blocks.tileentity.TileEntityEssenceConduit;
import am2.models.ModelEssenceConduit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.TRSRTransformation;


public class TileEssenceConduitRenderer extends TileEntitySpecialRenderer<TileEntityEssenceConduit>{

	private ResourceLocation powered;
	//private ResourceLocation unPowered;
	
	IBakedModel bakedModel;
	IModel modelCrystal;
	
	public TileEssenceConduitRenderer(){
		model = new ModelEssenceConduit();

		powered = new ResourceLocation("arsmagica2", "textures/blocks/custom/essence_conduit.png");
		//unPowered = new ResourceLocation("arsmagica2", "textures/blocks/custom/essenceConduitUnpowered.png");
	}
	
	private void createModel() {
		try {
			modelCrystal = ModelLoaderRegistry.getModel(new ResourceLocation("arsmagica2", "block/crystal.obj"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		bakedModel = modelCrystal.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM,
				location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
	}
	
	public void renderTileEntityAt(TileEntityEssenceConduit tile, double d, double d1, double d2, float f, int destroyStage){
		if (tile.getWorld() == null) return;
		if (tile.getWorld().getBlockState(tile.getPos()).getBlock() != BlockDefs.essenceConduit) return;
		createModel();
		EnumFacing i = EnumFacing.DOWN;
		if (destroyStage == -10) {
			i = EnumFacing.DOWN;
		} else if (tile.getWorld() != null){
			i = tile.getWorld().getBlockState(tile.getPos()).getValue(BlockEssenceConduit.FACING);
		}

		GlStateManager.pushMatrix(); //start


		switch (i){
		case UP:
			GL11.glTranslatef((float)d + 0.5f, (float)d1 - 0.5F, (float)d2 + 0.5F); //size
			GL11.glRotatef(180, 1.0F, 0.0F, 0.0F); //rotate based on metadata
			break;
		//case UP:
		case DOWN:
			GL11.glTranslatef((float)d + 0.5f, (float)d1 + 1.5F, (float)d2 + 0.5F); //size
			break;
		case SOUTH:
			GL11.glTranslatef((float)d + 0.5f, (float)d1 + 0.5F, (float)d2 - 0.5F); //size
			GL11.glRotatef(270, 1.0F, 0.0F, 0.0F); //rotate based on metadata
			break;
		case NORTH:
			GL11.glTranslatef((float)d + 0.5f, (float)d1 + 0.5F, (float)d2 + 1.5F); //size
			GL11.glRotatef(90, 1.0F, 0.0F, 0.0F); //rotate based on metadata
			break;
		case EAST:
			GL11.glTranslatef((float)d - 0.5f, (float)d1 + 0.5F, (float)d2 + 0.5F); //size
			GL11.glRotatef(90, 0.0F, 0.0F, 1.0F); //rotate based on metadata
			break;
		case WEST:
			GL11.glTranslatef((float)d + 1.5F, (float)d1 + 0.5F, (float)d2 + 0.5F); //size
			GL11.glRotatef(270, 0.0F, 0.0F, 1.0F); //rotate based on metadata
			break;
		}


		GL11.glScalef(1.0F, -1F, -1F); //if you read this, comment out this line and you can see what happens
		//if (tile.hasNexusPath()){
		bindTexture(powered);
		//}else{
		//bindTexture(unPowered);
		//}

		tile.incrementRotations();
		model.renderModel(tile.getRotationX(), tile.getRotationY(), tile.getRotationZ(), 0, 0.0625F); //renders and yes 0.0625 is a random number
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		RenderHelper.disableStandardItemLighting();
		GL11.glTranslatef(0f, 1f, 0f);
		GL11.glScalef(5f, -5f, 5f);
		GL11.glRotatef(tile.getRotationZ(), 0, 1, 0);
		GlStateManager.pushMatrix();
		GlStateManager.translate(-tile.getPos().getX(), -tile.getPos().getY(), -tile.getPos().getZ());
		bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		Tessellator tesselator = Tessellator.getInstance();
		tesselator.getBuffer().begin(7, DefaultVertexFormats.BLOCK);
		Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(getWorld(), bakedModel, getWorld().getBlockState(tile.getPos()), tile.getPos(), tesselator.getBuffer(), false);
		tesselator.draw();
		GlStateManager.popMatrix();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.popMatrix();
		GlStateManager.popMatrix(); //end
	}

	private ModelEssenceConduit model;

}
