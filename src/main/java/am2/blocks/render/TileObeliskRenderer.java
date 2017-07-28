package am2.blocks.render;

import net.minecraft.init.Blocks;
import org.lwjgl.opengl.GL11;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

import am2.blocks.BlockEssenceGenerator;
import am2.blocks.tileentity.TileEntityObelisk;
import am2.defs.BlockDefs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.common.model.TRSRTransformation;

public class TileObeliskRenderer extends TileEntitySpecialRenderer<TileEntityObelisk> {
	
	IModel defaultModel;
	IModel activeModel;
	IModel highPowerModel;
	IModel runesModel;
	IBakedModel defaultBakedModel;
	IBakedModel activeBakedModel;
	IBakedModel highPowerBakedModel;
	IBakedModel runesBakedModel;
	
	private void bake() {
		try {
			defaultModel = ModelLoaderRegistry.getModel(new ResourceLocation("arsmagica2", "block/obelisk.obj"));
			activeModel = ((OBJModel) defaultModel)
					.retexture(ImmutableMap.of("#Material", "arsmagica2:blocks/custom/obelisk_active"));
			highPowerModel = ((OBJModel) defaultModel)
					.retexture(ImmutableMap.of("#Material", "arsmagica2:blocks/custom/obelisk_active_highpower"));
			runesModel = ((OBJModel) defaultModel)
					.retexture(ImmutableMap.of("#Material", "arsmagica2:blocks/custom/obelisk_runes"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		Function<ResourceLocation, TextureAtlasSprite> getter = location -> Minecraft.getMinecraft()
				.getTextureMapBlocks().getAtlasSprite(location.toString());
		defaultBakedModel = defaultModel.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM, getter);
		activeBakedModel = activeModel.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM, getter);
		highPowerBakedModel = highPowerModel.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM, getter);
		runesBakedModel = runesModel.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM, getter);
    }
	
	@Override
	public void renderTileEntityAt(TileEntityObelisk te, double x, double y, double z, float partialTicks, int destroyStage) {
        if (!te.getWorld().isBlockLoaded(te.getPos(), false) || te.getWorld().getBlockState(te.getPos()).getBlock() != BlockDefs.obelisk)
            return;
        GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableRescaleNormal();
        GlStateManager.pushMatrix();
        RenderHelper.disableStandardItemLighting();
        EnumFacing facing = EnumFacing.NORTH;
        if (te.hasWorld()) {
            IBlockState state = te.getWorld().getBlockState(te.getPos());
            facing = state.getValue(BlockEssenceGenerator.FACING);
        }
        if (facing == EnumFacing.WEST || facing == EnumFacing.SOUTH)
            GlStateManager.translate(0, 0, 1);
        if (facing == EnumFacing.SOUTH || facing == EnumFacing.EAST)
            GlStateManager.translate(1, 0, 0);
        GlStateManager.rotate(180 - facing.getHorizontalAngle(), 0, 1, 0);
        GlStateManager.translate(-te.getPos().getX(), -te.getPos().getY(), -te.getPos().getZ());
        if (Minecraft.isAmbientOcclusionEnabled())
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
        else
            GlStateManager.shadeModel(GL11.GL_FLAT);
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        Tessellator tessellator = Tessellator.getInstance();
        tessellator.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        if (te.hasWorld())
            Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(te.getWorld(), getBakedModel(te), te.getWorld().getBlockState(te.getPos()), te.getPos(), tessellator.getBuffer(), false);
        else
            Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(Minecraft.getMinecraft().world, getBakedModel(te), BlockDefs.obelisk.getDefaultState(), new BlockPos(0, 0, 0), tessellator.getBuffer(), false);
        tessellator.draw();
        if (te.isActive()) {
            GL11.glMatrixMode(GL11.GL_TEXTURE);
            GL11.glPushMatrix();
            GL11.glLoadIdentity();
            GlStateManager.enableBlend();
            TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("arsmagica2:blocks/custom/obelisk_runes");
            GlStateManager.scale(1/(sprite.getMaxU() - sprite.getMinU()), 1/(sprite.getMaxV() - sprite.getMinV()), 1);
            Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("arsmagica2:textures/blocks/custom/obelisk_runes.png"));
            tessellator.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            float normx = (System.currentTimeMillis() % 32000) / 32000.0f;
            float normy = (System.currentTimeMillis() % 28000) / 28000.0f;
            GL11.glTranslatef(normx, normy, 0);
            float transp = (float)Math.abs(Math.sin(System.currentTimeMillis() / 1000.0));
            GL11.glColor4f(1, 1, 1, transp);
            Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(te.getWorld(), runesBakedModel, te.getWorld().getBlockState(te.getPos()), te.getPos(), tessellator.getBuffer(), false);
            tessellator.draw();
            GL11.glPopMatrix();
            GlStateManager.disableBlend();
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
        }

        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
        GlStateManager.enableRescaleNormal();
        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
	}

	private IBakedModel getBakedModel(TileEntityObelisk obelisk) {
		bake();
		if (obelisk.isHighPowerActive())
			return highPowerBakedModel;
		else if (obelisk.isActive())
			return activeBakedModel;
		return defaultBakedModel;
	}

}
