package am2.items.rendering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import am2.api.affinity.Affinity;
import am2.items.ItemSpellBase;
import am2.items.ItemSpellBook;
import am2.particles.AMParticleIcons;
import am2.utils.AffinityShiftUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SpellParticleRender extends ItemOverrideList{
	private final Minecraft mc;
	private final Map<Affinity, TextureAtlasSprite> icons;
	private boolean setupIcons = false;
	private static final ResourceLocation rLoc = TextureMap.LOCATION_BLOCKS_TEXTURE;

	public SpellParticleRender(List<ItemOverride> overridesIn){
		super (overridesIn);
		new ModelBiped(0.0F);
		mc = Minecraft.getMinecraft();
		icons = new HashMap<Affinity, TextureAtlasSprite>();
	}
	
	@Override
	public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
		if (world == null || entity == null)
			return super.handleItemState(originalModel, stack, world, entity);
		if (!Minecraft.getMinecraft().inGameHasFocus)
			return super.handleItemState(originalModel, stack, world, entity);
		renderItem(stack, entity);
		return new BakedBlank();//model != null ? model : super.handleItemState(originalModel, stack, world, entity);
	}
	
	private class BakedBlank implements IBakedModel {

		@Override
		public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
			return Lists.newArrayList();
		}

		@Override
		public boolean isAmbientOcclusion() {
			return false;
		}

		@Override
		public boolean isGui3d() {
			return false;
		}

		@Override
		public boolean isBuiltInRenderer() {
			return false;
		}

		@Override
		public TextureAtlasSprite getParticleTexture() {
			return Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
		}

		@Override
		public ItemCameraTransforms getItemCameraTransforms() {
			return ItemCameraTransforms.DEFAULT;
		}

		@Override
		public ItemOverrideList getOverrides() {
			return new ItemOverrideList(new ArrayList<>());
		}
	}
	
	public void renderItem(ItemStack item, EntityLivingBase entity){

		if (mc.player.isPotionActive(Potion.getPotionFromResourceLocation("invisibility"))) return;

		ItemStack scrollStack = null;
		if (item.getItem() instanceof ItemSpellBase){
			scrollStack = item;
		}else if (item.getItem() instanceof ItemSpellBook){
			scrollStack = ((ItemSpellBook)item.getItem()).getActiveScrollInventory(item).get(((ItemSpellBook)item.getItem()).GetActiveSlot(item));
		}


		if (scrollStack == null) return;

		Affinity affinity = AffinityShiftUtils.getMainShiftForStack(scrollStack);

		renderEffect(affinity, true, entity);
	}

	public void renderEffect(Affinity affinity, boolean includeArm, EntityLivingBase entity){

		if (!setupIcons){
			setupAffinityIcons();
			setupIcons = true;
		}
		
		GL11.glPushMatrix();
		GL11.glEnable(32826 /*GL_RESCALE_NORMAL_EXT*/);
		GL11.glEnable(3042);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		float scale = 3f;
		if (entity == mc.player && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0){

			GL11.glPushMatrix();

//			if (((EntityPlayer)entity).getItemInUseCount() > 0){
//				GL11.glRotatef(120, 1, 0, 1);
//				GL11.glRotatef(-10, 0, 1, 0);
//				GL11.glTranslatef(2f, 0f, 0.8f);
//			}else{
//				GL11.glTranslatef(3f, -1.2f, -2.5f);
//				GL11.glScalef(scale, scale, scale);
//				GL11.glRotatef(-45, 0, 1, 0);
//			}
//			GlStateManager.rotate(90, 0, 1, 0);
			RenderHelper.disableStandardItemLighting();
			GL11.glTranslatef(0.0f, 0.0f, -2.5f);
			RenderByAffinity(affinity);
			RenderHelper.enableStandardItemLighting();
			GL11.glPopMatrix();

			if (includeArm){
				Minecraft.getMinecraft().renderEngine.bindTexture(mc.player.getLocationSkin());
				renderFirstPersonArm(mc.player);
			}
		}
		else{
			GL11.glTranslatef(-0.25f, 0.0f, 0.0f);
			scale = 0.5f;
			GL11.glScalef(scale, scale, scale);
			//GL11.glRotatef(45, 0, 0, 0);
			RenderByAffinity(affinity);
		}

		GL11.glDisable(32826 /*GL_RESCALE_NORMAL_EXT*/);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(3042);

		GL11.glPopMatrix();
	}

	private void setupAffinityIcons(){
		icons.put(Affinity.AIR, AMParticleIcons.instance.getIconByName("air_hand"));
		icons.put(Affinity.ARCANE, AMParticleIcons.instance.getIconByName("arcane_hand"));
		icons.put(Affinity.EARTH, AMParticleIcons.instance.getIconByName("earth_hand"));
		icons.put(Affinity.ENDER, AMParticleIcons.instance.getIconByName("ender_hand"));
		icons.put(Affinity.FIRE, AMParticleIcons.instance.getIconByName("fire_hand"));
		icons.put(Affinity.ICE, AMParticleIcons.instance.getIconByName("ice_hand"));
		icons.put(Affinity.LIFE, AMParticleIcons.instance.getIconByName("life_hand"));
		icons.put(Affinity.LIGHTNING, AMParticleIcons.instance.getIconByName("lightning_hand"));
		icons.put(Affinity.NATURE, AMParticleIcons.instance.getIconByName("nature_hand"));
		icons.put(Affinity.NONE, AMParticleIcons.instance.getIconByName("none_hand"));
		icons.put(Affinity.WATER, AMParticleIcons.instance.getIconByName("water_hand"));
	}

	public void RenderByAffinity(Affinity affinity){

		Minecraft.getMinecraft().renderEngine.bindTexture(rLoc);

		TextureAtlasSprite icon = icons.get(affinity);
		if (icon == null) return;

		float TLX = icon.getMinU();
		float BRX = icon.getMaxU();
		float TLY = icon.getMinV();
		float BRY = icon.getMaxV();

		doRender(TLX, TLY, BRX, BRY);
	}


	private void doRender(float TLX, float TLY, float BRX, float BRY){
		//ItemRenderer.renderItemIn2D(Tessellator.instance, TLX, TLY, BRX, BRY, 1, 1, 0.0625F);
		Tessellator t = Tessellator.getInstance();
		t.getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
		t.getBuffer().pos(0.0D, 0.0D, 0.0D).tex(TLX, BRY).normal(0.0F, 0.0F, 1.0F).endVertex();
		t.getBuffer().pos(1.0D, 0.0D, 0.0D).tex(BRX, BRY).normal(0.0F, 0.0F, 1.0F).endVertex();
		t.getBuffer().pos(1.0D, 1.0D, 0.0D).tex(BRX, TLY).normal(0.0F, 0.0F, 1.0F).endVertex();
		t.getBuffer().pos(0.0D, 1.0D, 0.0D).tex(TLX, TLY).normal(0.0F, 0.0F, 1.0F).endVertex();
		t.draw();
	}

	private void renderFirstPersonArm(EntityPlayerSP player) {
		EnumHandSide hand = player.getPrimaryHand();
		boolean flag = hand != EnumHandSide.LEFT;
		float f = flag ? 1.0F : -1.0F;
		float f1 = MathHelper.sqrt(0);
		GlStateManager.rotate(f * 45.0F, 0.0F, 1.0F, 0.0F);
		float f5 = MathHelper.sin(0 * 0 * (float) Math.PI);
		float f6 = MathHelper.sin(f1 * (float) Math.PI);
		GlStateManager.rotate(f * f6 * 70.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(f * f5 * -20.0F, 0.0F, 0.0F, 1.0F);
		AbstractClientPlayer abstractclientplayer = this.mc.player;
		this.mc.getTextureManager().bindTexture(abstractclientplayer.getLocationSkin());
		GlStateManager.translate(f * -1.0F, 3.6F, 3.5F);
		GlStateManager.rotate(f * 120.0F, 0.0F, 0.0F, 1.0F);
		GlStateManager.rotate(200.0F, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(f * -135.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.translate(f * 5.6F, 0.0F, 0.0F);
		RenderPlayer renderplayer = (RenderPlayer) Minecraft.getMinecraft().getRenderManager()
				.getEntityRenderObject(abstractclientplayer);
		GlStateManager.disableCull();
		if (flag) {
			renderplayer.renderRightArm(abstractclientplayer);
		} else {
			renderplayer.renderLeftArm(abstractclientplayer);
		}

		GlStateManager.enableCull();

		// GL11.glMatrixMode(GL11.GL_MODELVIEW);
		// float par1 = 0.5f;
		//
		// float f1 = 1.0F;
		// EntityPlayerSP entityclientplayermp = this.mc.player;
		// if (entityclientplayermp == null) return;
		// float f2 = entityclientplayermp.prevRotationPitch +
		// (entityclientplayermp.rotationPitch -
		// entityclientplayermp.prevRotationPitch) * par1;
		// GL11.glPushMatrix();
		// GL11.glRotatef(f2, 1.0F, 0.0F, 0.0F);
		// GL11.glRotatef(entityclientplayermp.prevRotationYaw +
		// (entityclientplayermp.rotationYaw -
		// entityclientplayermp.prevRotationYaw) * par1, 0.0F, 1.0F, 0.0F);
		// RenderHelper.enableStandardItemLighting();
		// GL11.glPopMatrix();
		// EntityPlayerSP entityplayersp = entityclientplayermp;
		// float f3 = entityplayersp.prevRenderArmPitch +
		// (entityplayersp.renderArmPitch - entityplayersp.prevRenderArmPitch) *
		// par1;
		// float f4 = entityplayersp.prevRenderArmYaw +
		// (entityplayersp.renderArmYaw - entityplayersp.prevRenderArmYaw) *
		// par1;
		// GL11.glRotatef((entityclientplayermp.rotationPitch - f3) * 0.1F,
		// 1.0F, 0.0F, 0.0F);
		// GL11.glRotatef((entityclientplayermp.rotationYaw - f4) * 0.1F, 0.0F,
		// 1.0F, 0.0F);
		// int i = mc.world.isBlockLoaded(entityclientplayermp.getPosition())
		// ? mc.world.getCombinedLight(entityclientplayermp.getPosition(), 0)
		// : 0;
		// int j = i % 65536;
		// int k = i / 65536;
		// OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j
		// / 1.0F, k / 1.0F);
		// GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		// float f6;
		// float f7;
		// float f8;
		//
		// GL11.glPushMatrix();
		// float f12 = 0.8F;
		// f7 = entityclientplayermp.getSwingProgress(par1);
		// f8 = MathHelper.sin(f7 * (float)Math.PI);
		// f6 = MathHelper.sin(MathHelper.sqrt(f7) * (float)Math.PI);
		// GL11.glTranslatef(-f6 * 0.3F,
		// MathHelper.sin(MathHelper.sqrt(f7) * (float)Math.PI * 2.0F) *
		// 0.4F, -f8 * 0.4F);
		// GL11.glTranslatef(0.8F * f12, -0.75F * f12 - (1.0F - f1) * 0.6F,
		// -0.9F * f12);
		// GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
		// GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		// f7 = entityclientplayermp.getSwingProgress(par1);
		// f8 = MathHelper.sin(f7 * f7 * (float)Math.PI);
		// f6 = MathHelper.sin(MathHelper.sqrt(f7) * (float)Math.PI);
		// GL11.glRotatef(f6 * 70.0F, 0.0F, 1.0F, 0.0F);
		// GL11.glRotatef(-f8 * 20.0F, 0.0F, 0.0F, 1.0F);
		// this.mc.getTextureManager().bindTexture(rLoc);//entityclientplayermp.getLocationSkin());
		// GL11.glTranslatef(-1.0F, 3.6F, 3.5F);
		// GL11.glRotatef(120.0F, 0.0F, 0.0F, 1.0F);
		// GL11.glRotatef(200.0F, 1.0F, 0.0F, 0.0F);
		// GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);
		// GL11.glScalef(1.0F, 1.0F, 1.0F);
		// GL11.glTranslatef(5.6F, 0.0F, 0.0F);
		//
		// float f = 1.0F;
		// GL11.glColor3f(f, f, f);
		//// this.modelBipedMain.onGround = 0.0F;
		// this.modelBipedMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F,
		// 0.0625F, player);
		// this.modelBipedMain.bipedRightArm.render(0.0625F);
		// GL11.glPopMatrix();

	}

}
