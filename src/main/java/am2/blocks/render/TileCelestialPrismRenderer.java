package am2.blocks.render;

import org.lwjgl.opengl.GL11;

import am2.blocks.BlockEssenceGenerator;
import am2.blocks.tileentity.TileEntityCelestialPrism;
import am2.defs.BlockDefs;
import net.minecraft.block.state.IBlockState;
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
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.TRSRTransformation;

public class TileCelestialPrismRenderer extends TileEntitySpecialRenderer<TileEntityCelestialPrism> {
	
	IModel model;
	IBakedModel bakedModel;
	
	private IBakedModel getBakedModel() {
		try {
			model = ModelLoaderRegistry.getModel(new ResourceLocation("arsmagica2", "block/celestial_prism.obj"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		bakedModel = model.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM,
				location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
		return bakedModel;
	}
	
	@Override
	public void renderTileEntityAt(TileEntityCelestialPrism te, double x, double y, double z, float partialTicks, int destroyStage) {
		GlStateManager.pushAttrib();
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.disableRescaleNormal();
		GlStateManager.pushMatrix();
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_CULL_FACE);
		RenderHelper.disableStandardItemLighting();
		if (x != 0 || y != 0 || z != 0) {
			GlStateManager.translate(-1f, 0, 1F);
			GL11.glTranslated(1.5, 0, -0.5);
			GL11.glScalef(2, 2, 2);
			IBlockState state = te.getWorld().getBlockState(te.getPos());
			EnumFacing facing = state.getValue(BlockEssenceGenerator.FACING);
			GlStateManager.rotate(180 - facing.getHorizontalAngle(), 0, 1, 0);
			GlStateManager.translate(-te.getPos().getX(), -te.getPos().getY(), -te.getPos().getZ());
			GlStateManager.translate(-0.5, 0, -0.5);
		}
		if (Minecraft.isAmbientOcclusionEnabled())
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
		else
			GlStateManager.shadeModel(GL11.GL_FLAT);
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		Tessellator tessellator = Tessellator.getInstance();
		tessellator.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		if (x != 0 || y != 0 || z != 0) {
			Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(te.getWorld(), getBakedModel(), te.getWorld().getBlockState(te.getPos()), te.getPos(), tessellator.getBuffer(), false);
		} else {
			Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(Minecraft.getMinecraft().world, getBakedModel(), BlockDefs.celestialPrism.getDefaultState(), BlockPos.ORIGIN, tessellator.getBuffer(), false);
		}
		tessellator.draw();
		RenderHelper.enableStandardItemLighting();
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_BLEND);
		GlStateManager.popMatrix();
		GlStateManager.popMatrix();
		GlStateManager.popAttrib();
	}

}
