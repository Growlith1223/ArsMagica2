package am2.blocks.render;

import am2.blocks.BlockLectern;
import am2.blocks.tileentity.TileEntityLectern;
import am2.models.ModelArchmagePodium;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBook;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class TileLecternRenderer extends TileEntitySpecialRenderer<TileEntityLectern>{
	RenderEntityItem renderItem = new RenderEntityItem(Minecraft.getMinecraft().getRenderManager(), Minecraft.getMinecraft().getRenderItem());
	private final ModelBook enchantmentBook = new ModelBook();
	private final ModelArchmagePodium podium = new ModelArchmagePodium();
	private static int curDye = 1;
	private static int curTick = 0;
	public TileLecternRenderer() {
		
	}

	public void renderTileEntityArchmagePodiumAt(TileEntityLectern podium, double x, double y, double z, float f1) throws Exception{
		Minecraft.getMinecraft().mcProfiler.startSection("Lectern-Render");
		Minecraft.getMinecraft().mcProfiler.startSection("model");
		RenderHelper.disableStandardItemLighting();
		EnumFacing facing = EnumFacing.NORTH;
		if (podium.hasWorld()) {
			facing = podium.getWorld().getBlockState(podium.getPos()).getValue(BlockLectern.FACING);
		}
		Minecraft.getMinecraft().mcProfiler.startSection("rendering");
		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("arsmagica2", "textures/blocks/custom/archmage_podium.png"));
		GlStateManager.pushMatrix();
		GlStateManager.translate((float)x + 0.5F, (float)y + 0.9F, (float)z + 0.5F);
		GlStateManager.rotate(180 - facing.getHorizontalAngle(), 0, 1, 0);
		GlStateManager.scale(1.0F, -1F, -1F);
		GlStateManager.scale(1.0F, 0.6F, 1.0F);
		this.podium.renderModel(0.0625F);
		GlStateManager.popMatrix();
		Minecraft.getMinecraft().mcProfiler.endSection();
		Minecraft.getMinecraft().mcProfiler.endStartSection("book-model");
		if (podium.hasStack()){
			if (podium.getOverpowered())
				GlStateManager.color(0.7f, 0.2f, 0.2f, 1.0f);
			else
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			RenderBook(podium, x, y, z, f1, 0);
		}else if (podium.getNeedsBook()){
			GlStateManager.color(0.7f, 0.2f, 0.2f, 0.2f);
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			RenderBook(podium, x, y, z, f1, 0);
			GlStateManager.disableBlend();
		}
		renderHelperIcon(podium, x, y, z, f1);
		GlStateManager.disableBlend();
		Minecraft.getMinecraft().mcProfiler.endSection();
		Minecraft.getMinecraft().mcProfiler.endSection();
	}

	private void renderHelperIcon(TileEntityLectern podium, double x, double y, double z, float f){
		if (podium.getTooltipStack() == null){
			podium.resetParticleAge();
			return;
		}

		float deg = (curTick / 0.25f % 360F);



		ItemStack stack = podium.getTooltipStack().copy();
		stack.setCount(1);;
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.translate(x +0.5f, y+1.4f, z+0.5f);
		GlStateManager.rotate(deg, 0, f, 0);
		if (curTick++ >= 100) {
			curTick = 0;
			curDye++;
			if (curDye >= 16)
				curDye = 1;

		}
		if(stack.getItem() == Items.DYE){
			stack = new ItemStack(Items.DYE, 1, curDye);
		}
		Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.GROUND);
		//try {
		//renderItem.doRender(AMGuiHelper.instance.dummyItem, x + 0.5f, y + 1.4f, z + 0.5f, AMGuiHelper.instance.dummyItem.rotationYaw, f);
		//} catch (NullPointerException e) {
		//	renderItem = new RenderEntityItem(Minecraft.getMinecraft().getRenderManager(), Minecraft.getMinecraft().getRenderItem());
		//}
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5f, y + 1f, z + 0.5f);
		float scale = 0.2f;
		GlStateManager.scale(scale, scale, scale);
		renderRadiant(Tessellator.getInstance(), f, podium);
		GlStateManager.popMatrix();
	}

	private void renderRadiant(Tessellator tessellator, float partialFrame, TileEntityLectern podium){
//		RenderHelper.disableStandardItemLighting();
//		float var4 = (podium.particleAge + partialFrame) / podium.particleMaxAge;
//		float var5 = 0.0F;
//
//		if (var4 > 0.8F){
//			var5 = (var4 - 0.8F) / 0.2F;
//		}
//
//		Random var6 = new Random(432L);
//		GL11.glDisable(GL11.GL_TEXTURE_2D);
//		GL11.glShadeModel(GL11.GL_SMOOTH);
//		GL11.glEnable(GL11.GL_BLEND);
//		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
//		GL11.glDisable(GL11.GL_ALPHA_TEST);
//		GL11.glEnable(GL11.GL_CULL_FACE);
//		GL11.glDepthMask(false);
//		GL11.glPushMatrix();
//
//		for (int var7 = 0; var7 < 20.0F; ++var7){
//			float rH = 90f;
//			float oH = 0f;
//			GL11.glPushMatrix();
//			GL11.glRotatef(podium.field_145926_a % 360, 0.0F, 1.0F, 0.0F);
//			GL11.glRotatef(var6.nextFloat() * 180, 1.0F, 0.0F, 0.0F);
//			GL11.glRotatef(var6.nextFloat() * -180, 1.0F, 0.0F, 0.0F);
//			tessellator.startDrawing(6);
//			float var8 = var6.nextFloat() * 2.0F + 2.0F + var5 * 0.5F;
//			float var9 = var6.nextFloat() * 2.0F + 1.0F + var5 * 2.0F;
//			if (!podium.getOverpowered())
//				tessellator.setColorRGBA_F(0.2f, 0.2f, 1.0f, 0.2f);
//			else
//				tessellator.setColorRGBA_F(1.0f, 0.2f, 0.2f, 0.2f);
//			tessellator.addVertex(0.0D, 0.0D, 0.0D);
//			if (!podium.getOverpowered())
//				tessellator.setColorRGBA_F(0.2f, 0.2f, 1.0f, 0.0f);
//			else
//				tessellator.setColorRGBA_F(1.0f, 0.2f, 0.2f, 0.0f);
//			tessellator.addVertex(-0.866D * var9, var8, -0.5F * var9);
//			tessellator.addVertex(0.866D * var9, var8, -0.5F * var9);
//			tessellator.addVertex(0.0D, var8, 1.0F * var9);
//			tessellator.addVertex(-0.866D * var9, var8, -0.5F * var9);
//			tessellator.draw();
//			GL11.glPopMatrix();
//		}
//
//		GL11.glPopMatrix();
//		GL11.glDepthMask(true);
//		GL11.glDisable(GL11.GL_CULL_FACE);
//		GL11.glDisable(GL11.GL_BLEND);
//		GL11.glShadeModel(GL11.GL_FLAT);
//		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
//		GL11.glEnable(GL11.GL_TEXTURE_2D);
//		GL11.glEnable(GL11.GL_ALPHA_TEST);
//		RenderHelper.enableStandardItemLighting();
	}

	private void RenderBook(TileEntityLectern podium, double x, double y, double z, float partialTicks, int meta){
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5F, y + 0.75F, z + 0.5F);
		float var9 = (float)podium.tickCount + partialTicks;
		GlStateManager.translate(0.0F, 0.1F + MathHelper.sin(var9 * 0.01F) * 0.01F, 0.0F);
		float f2;

		for (f2 = podium.tRot - podium.bookRotation; f2 >= (float)Math.PI; f2 -= ((float)Math.PI * 2F));

		while (f2 < -(float)Math.PI){
			f2 += ((float)Math.PI * 2F);
		}

//		float var11 = podium.bookRotationPrev + f2 * partialTicks;
		EnumFacing facing = EnumFacing.NORTH;
		if (podium.hasWorld())
			facing = podium.getWorld().getBlockState(podium.getPos()).getValue(BlockLectern.FACING);
		GlStateManager.rotate(270 - facing.getHorizontalAngle(), 0, 1, 0);
//		if (facing == EnumFacing.EAST || facing == EnumFacing.SOUTH)
//			GlStateManager.translate(0, 0, -1);
//		if (facing == EnumFacing.WEST || facing == EnumFacing.SOUTH)
//			GlStateManager.translate(-1, 0, 0);
		GlStateManager.rotate(80.0F, 0.0F, 0.0F, 1.0F);
		bindTexture(new ResourceLocation("textures/entity/enchanting_table_book.png"));
		float var12 = podium.pageFlipPrev + (podium.pageFlip - podium.pageFlipPrev) * partialTicks + 0.25F;
		float var13 = podium.pageFlipPrev + (podium.pageFlip - podium.pageFlipPrev) * partialTicks + 0.75F;
		var12 = (var12 - MathHelper.floor(var12)) * 1.6F - 0.3F;
		var13 = (var13 - MathHelper.floor(var13)) * 1.6F - 0.3F;
		
		var12 = MathHelper.clamp(var12, 0, 1);
		var13 = MathHelper.clamp(var13, 0, 1);

//		float var14 = podium.bookSpreadPrev + (podium.bookSpread - podium.bookSpreadPrev) * partialTicks;
		this.enchantmentBook.setRotationAngles(var9, var12, var13, 1f, 0.0F, 0.0625F, (Entity)null);
		this.enchantmentBook.coverRight.render(0.0625F);
		this.enchantmentBook.coverLeft.render(0.0625F);
		this.enchantmentBook.bookSpine.render(0.0625F);
		this.enchantmentBook.pagesRight.render(0.0625F);
		this.enchantmentBook.pagesLeft.render(0.0625F);
		this.enchantmentBook.flippingPageRight.render(0.0625F);
		this.enchantmentBook.flippingPageLeft.render(0.0625F);
		GlStateManager.popMatrix();
	}

	@Override
	public void renderTileEntityAt(TileEntityLectern te, double x, double y, double z, float partialTicks, int destroyStage) {
		GlStateManager.pushMatrix();
		try {
			renderTileEntityArchmagePodiumAt(te, x, y, z, partialTicks);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		GlStateManager.popMatrix();
	}

	/*
	 * GL11.glPushMatrix();
        GL11.glTranslatef((float)x + 0.5F, (float)y + 0.75F, (float)z + 0.5F);
        float f1 = (float)podium.field_145926_a + partialTicks;
        GL11.glTranslatef(0.0F, 0.1F + MathHelper.sin(f1 * 0.1F) * 0.01F, 0.0F);
        float f2;

        for (f2 = podium.field_145928_o - podium.field_145925_p; f2 >= (float)Math.PI; f2 -= ((float)Math.PI * 2F))
        {
            ;
        }

        while (f2 < -(float)Math.PI)
        {
            f2 += ((float)Math.PI * 2F);
        }

        float f3 = podium.field_145925_p + f2 * partialTicks;
        GL11.glRotatef(-f3 * 180.0F / (float)Math.PI, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(80.0F, 0.0F, 0.0F, 1.0F);
        this.bindTexture(book);
        float f4 = podium.field_145931_j + (podium.field_145933_i - podium.field_145931_j) * partialTicks + 0.25F;
        float f5 = podium.field_145931_j + (podium.field_145933_i - podium.field_145931_j) * partialTicks + 0.75F;
        f4 = (f4 - (float)MathHelper.floor((double)f4)) * 1.6F - 0.3F;
        f5 = (f5 - (float)MathHelper.floor((double)f5)) * 1.6F - 0.3F;

        if (f4 < 0.0F)
        {
            f4 = 0.0F;
        }

        if (f5 < 0.0F)
        {
            f5 = 0.0F;
        }

        if (f4 > 1.0F)
        {
            f4 = 1.0F;
        }

        if (f5 > 1.0F)
        {
            f5 = 1.0F;
        }

        float f6 = podium.field_145927_n + (podium.field_145930_m - podium.field_145927_n) * partialTicks;
        GL11.glEnable(GL11.GL_CULL_FACE);
        this.enchantmentBook.render((Entity)null, f1, f4, f5, f6, 0.0F, 0.0625F);
        GL11.glPopMatrix();
	 */
}
