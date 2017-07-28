package am2.proxy.gui;

import java.util.ArrayList;

import org.lwjgl.util.vector.Quaternion;

import com.google.common.base.Optional;

import am2.api.affinity.Affinity;
import am2.api.event.RenderingItemEvent;
import am2.bosses.models.ModelPlantGuardianSickle;
import am2.defs.ItemDefs;
import am2.items.rendering.SpellParticleRender;
import am2.utils.ModelUtils;
import am2.utils.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ItemRenderer {
	private static final ResourceLocation sickleLocation = new ResourceLocation("arsmagica2", "textures/mobs/bosses/plant_guardian.png");
	private static final ResourceLocation arcaneBookLocation = new ResourceLocation("arsmagica2", "textures/mobs/bosses/arcane_guardian.png");
	private static final ResourceLocation winterArmLocation = new ResourceLocation("arsmagica2", "textures/mobs/bosses/ice_guardian.png");
	private static final ResourceLocation airLocation = new ResourceLocation("arsmagica2", "textures/mobs/bosses/air_guardian.png");
	private static final ResourceLocation waterLocation = new ResourceLocation("arsmagica2", "textures/mobs/bosses/water_guardian.png");
	private static final ResourceLocation fireLocation = new ResourceLocation("arsmagica2", "textures/mobs/bosses/fire_guardian.png");
	private static final ResourceLocation earthLocation = new ResourceLocation("arsmagica2", "textures/mobs/bosses/earth_guardian.png");
	private static final ResourceLocation broomLocation = new ResourceLocation("arsmagica2", "textures/mobs/broom.png");
	private static final ResourceLocation candleLocation = new ResourceLocation("arsmagica2", "textures/blocks/custom/candle.png");

	protected ModelPlantGuardianSickle modelSickle;

	public static final ItemRenderer instance = new ItemRenderer();

	private ItemRenderer(){
		Minecraft.getMinecraft();
		modelSickle = new ModelPlantGuardianSickle();
		modelSickle.setNoSpin();
	}
	
	@SubscribeEvent
	public void renderItemEvent(RenderingItemEvent event) {
		renderItem(event.getCameraTransformType(), event.getStack(), event.getEntity());
		if (event.getStack().getItem() == ItemDefs.BoundShield) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("arsmagica2:textures/models/bound_shield.png"));
			GlStateManager.pushMatrix();
			IItemPropertyGetter getter = ItemDefs.BoundShield.getPropertyGetter(new ResourceLocation("blocking"));
			ModelUtils.renderShield(event.getStack(), getter.apply(event.getStack(), Minecraft.getMinecraft().world, event.getEntity()) == 1.0f, event.getCameraTransformType(), event.getEntity());
			GlStateManager.popMatrix();
			Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		}
		if (event.getStack() == null || event.getStack().getItem() == null || Block.getBlockFromItem(event.getStack().getItem()) == null) return;
		Block block = Block.getBlockFromItem(event.getStack().getItem());
		if (!(block instanceof ITileEntityProvider)) return;
		TileEntity te = ((ITileEntityProvider)block).createNewTileEntity(Minecraft.getMinecraft().world, event.getStack().getItemDamage());
		if (te == null) return;
		TileEntitySpecialRenderer<TileEntity> tesr = TileEntityRendererDispatcher.instance.getSpecialRenderer(te);
		if (tesr == null) return;
		GlStateManager.pushMatrix();
		GlStateManager.disableBlend();
		GlStateManager.color(1f, 1f, 1f);
		GlStateManager.translate(-0.5, -0.5, -0.5);
		TRSRTransformation transform = ModelUtils.DEFAULT_BLOCK_STATE.apply(Optional.fromNullable(event.getCameraTransformType())).orNull();
		if (transform != null) {
			GlStateManager.translate(transform.getTranslation().x, transform.getTranslation().y, transform.getTranslation().z);
			GlStateManager.scale(transform.getScale().x, transform.getScale().y, transform.getScale().z);
			GlStateManager.rotate(new Quaternion(transform.getLeftRot().x, transform.getLeftRot().y, transform.getLeftRot().z, transform.getLeftRot().w));
		}
//		ModelUtils.transform(ModelUtils.DEFAULT_BLOCK_STATE, event.getCameraTransformType(),
//				event.getCameraTransformType() == TransformType.FIRST_PERSON_LEFT_HAND
//						|| event.getCameraTransformType() == TransformType.THIRD_PERSON_LEFT_HAND);
		tesr.renderTileEntityAt(te, 0, 0, 0, event.getStack().getItemDamage(), -10);
		GlStateManager.enableBlend();
		GlStateManager.popMatrix();
	}

	public void renderItem(TransformType type, ItemStack item, Object... data){
		if (item == null) return;
		bindTextureByItem(item);
		GlStateManager.pushMatrix();
		//ModelUtils.transform(ModelUtils.DEFAULT_ITEM_STATE, type, type == TransformType.FIRST_PERSON_LEFT_HAND || type == TransformType.THIRD_PERSON_LEFT_HAND);
		setupItemRender(type, item);

		renderModelByItem(item);
		GlStateManager.popMatrix();
		
		GlStateManager.pushMatrix();
		if (item.hasEffect()) {
			setupItemRender(type, item);
			// SETUP
			GlStateManager.depthMask(false);
			GlStateManager.depthFunc(514);
			GlStateManager.disableLighting();
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
			Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("textures/misc/enchanted_item_glint.png"));
			GlStateManager.matrixMode(5890);
			GlStateManager.pushMatrix(); 
			{
				GlStateManager.scale(8.0F, 8.0F, 8.0F);
				float f = (float) (Minecraft.getSystemTime() % 3000L) / 3000.0F / 8.0F;
				GlStateManager.translate(f, 0.0F, 0.0F);
				GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);
				RenderUtils.color(-8372020);
				GlStateManager.matrixMode(5888);
				GlStateManager.pushMatrix();
				{
					renderModelByItem(item);// this.renderModel(model, -8372020);
				}
				GlStateManager.popMatrix();
				GlStateManager.matrixMode(5890);
			}
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			{
				GlStateManager.scale(8.0F, 8.0F, 8.0F);
				float f1 = (float) (Minecraft.getSystemTime() % 4873L) / 4873.0F / 8.0F;
				GlStateManager.translate(-f1, 0.0F, 0.0F);
				GlStateManager.rotate(10.0F, 0.0F, 0.0F, 1.0F);
				GlStateManager.matrixMode(5888);
				GlStateManager.pushMatrix();
				{
					renderModelByItem(item);// this.renderModel(model, -8372020);
				}
				GlStateManager.popMatrix();
				GlStateManager.matrixMode(5890);
			}
			GlStateManager.popMatrix();
			GlStateManager.matrixMode(5888);
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			GlStateManager.enableLighting();
			GlStateManager.depthFunc(515);
			GlStateManager.depthMask(true);
			GlStateManager.resetColor();
			Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		}
		GlStateManager.popMatrix();
		if (item.getItem() == ItemDefs.wardingCandle)
			GlStateManager.enableCull();
		if (item.getItem() == ItemDefs.wardingCandle && (type == TransformType.THIRD_PERSON_RIGHT_HAND || type == TransformType.FIRST_PERSON_RIGHT_HAND || type == TransformType.FIRST_PERSON_LEFT_HAND || type == TransformType.THIRD_PERSON_LEFT_HAND)){
			renderCandleFlame(type, item, data);
		}
		GlStateManager.resetColor();
	}

	private void renderCandleFlame(TransformType type, ItemStack item, Object... data){

		if (item.hasTagCompound()){
			GlStateManager.color(item.getTagCompound().getFloat("flame_red"),
					item.getTagCompound().getFloat("flame_green"),
					item.getTagCompound().getFloat("flame_blue"));
		}
		GlStateManager.pushMatrix();
		if (type == TransformType.THIRD_PERSON_RIGHT_HAND || type == TransformType.THIRD_PERSON_LEFT_HAND){
			GlStateManager.translate(0.0f, 0.5f, 0f);
			GlStateManager.scale(0.5f, 0.5f, 0.5f);
		}else if (type == TransformType.FIRST_PERSON_RIGHT_HAND || type == TransformType.FIRST_PERSON_LEFT_HAND){
			GlStateManager.translate(0, 0.5f, 0.3f);
			if (type == TransformType.FIRST_PERSON_LEFT_HAND)
				GlStateManager.translate(0, 0, 0.17);
			GlStateManager.scale(0.15f, 0.15f, 0.15f);
		}
		new SpellParticleRender(new ArrayList<>()).renderEffect(Affinity.FIRE, false, (EntityLivingBase) data[0]);
		GlStateManager.popMatrix();
	}

	private void setupItemRender(TransformType type, ItemStack stack){
//		float scale = 1.0F;
//		switch (type){
//		case GROUND:
//			if (stack.getItem() == ItemDefs.magicBroom){
//				GL11.glRotatef(90F, 1, 0, 0);
//				GL11.glTranslatef(0, 0, -3f);
//			}else if (stack.getItem() == ItemDefs.arcaneSpellbook){
//				GL11.glTranslatef(0, 0.6f, 0);
//			}else if (stack.getItem() == ItemDefs.earthArmor){
//				GL11.glRotatef(90, 1, 0, 0);
//				GL11.glTranslatef(0, 0, -3f);
//			}else if (stack.getItem() == ItemDefs.fireEars){
//				GL11.glRotatef(90, 1, 0, 0);
//				GL11.glTranslatef(0, 0, -1f);
//			}else if (stack.getItem() == ItemDefs.waterOrbs){
//				GL11.glRotatef(90, 1, 0, 0);
//				GL11.glTranslatef(0, 0, -2f);
//			}else if (stack.getItem() == ItemDefs.airSled){
//				GL11.glRotatef(90, 1, 0, 0);
//				GL11.glTranslatef(0, 0, -2f);
//			}else if (stack.getItem() == ItemDefs.wardingCandle){
//				GL11.glRotatef(90, 1, 0, 0);
//				GL11.glTranslatef(0, 0, -3f);
//			}
//			scale = 1.6f;
//			break;
//		case FIRST_PERSON_RIGHT_HAND:
//			if (stack.getItem() == ItemDefs.magicBroom){
//				GL11.glRotatef(135F, 0, 1, 0);
//				GL11.glRotatef(-90F, 1, 0, 0);
//				GL11.glTranslatef(-0.5f, 1f, 0f);
//			}else if (stack.getItem() == ItemDefs.arcaneSpellbook){
//				GL11.glRotatef(270F, 0.0F, 1.0F, 0.0F);
//				GL11.glTranslatef(0.5f, 1f, -0.5f);
//			}else if (stack.getItem() == ItemDefs.winterArm){
//				GL11.glRotatef(60, 1, -0.6f, 0);
//				GL11.glTranslatef(1f, 0.5f, -1);
//			}else if (stack.getItem() == ItemDefs.winterArm){
//				GL11.glRotatef(180, 1, 0, 0);
//				GL11.glTranslatef(0, 0, -1);
//			}else if (stack.getItem() == ItemDefs.earthArmor){
//				GL11.glRotatef(90, 1, 0, 0);
//				GL11.glTranslatef(0f, 0.7f, -2f);
//			}else if (stack.getItem() == ItemDefs.fireEars){
//				GL11.glRotatef(90, 0, 1, 0);
//				GL11.glTranslatef(-0.8f, 1.2f, -1f);
//			}else if (stack.getItem() == ItemDefs.waterOrbs){
//				GL11.glRotatef(45, 0, 1, 0);
//				GL11.glTranslatef(0f, 0.8f, -0.5f);
//			}else if (stack.getItem() == ItemDefs.airSled){
//				GL11.glTranslatef(0, 0.8f, -0.8f);
//			}else if (stack.getItem() == ItemDefs.wardingCandle){
//				GL11.glRotatef(90F, 1, 0, 0);
//				GL11.glTranslatef(-1f, 0.75f, -3.5f);
//			}else{
//				GL11.glRotatef(135F, 0, 1, 0);
//				GL11.glTranslatef(-0.55f, -0.35f, -1f);
//			}
//			break;
//		case THIRD_PERSON_RIGHT_HAND:
//			scale = 1.6f;
//			GL11.glRotatef(-135F, 0, 1, 0);
//			if (stack.getItem() == ItemDefs.magicBroom){
//				GL11.glRotatef(20F, 1, 0, 0);
//				GL11.glTranslatef(-0.3f, 0f, -1.5f);
//			}else if (stack.getItem() == ItemDefs.arcaneSpellbook){
//				GL11.glRotatef(180F, 0.0F, 1.0F, 0.0F);
//				GL11.glRotatef(120F, 1.0F, 0.0F, 0f);
//				GL11.glTranslatef(-0.05f, 0.1f, -1f);
//			}else if (stack.getItem() == ItemDefs.winterArm){
//				GL11.glRotatef(180, 1, 0, 0);
//				GL11.glTranslatef(0, 0, -1);
//			}else if (stack.getItem() == ItemDefs.earthArmor){
//				GL11.glRotatef(60F, 1, 0, 0);
//				GL11.glTranslatef(-1f, -1f, -1.2f);
//			}else if (stack.getItem() == ItemDefs.fireEars){
//				GL11.glTranslatef(0, 0.15f, -2f);
//			}else if (stack.getItem() == ItemDefs.waterOrbs){
//				GL11.glRotatef(45, 0, 0, 1);
//				GL11.glTranslatef(-0.7f, -0.7f, -3f);
//			}else if (stack.getItem() == ItemDefs.airSled){
//				GL11.glTranslatef(0, 0.2f, -2.5f);
//			}else if (stack.getItem() == ItemDefs.wardingCandle){
//				GL11.glRotatef(90F, 1, 0, 0);
//				GL11.glTranslatef(-0.3f, -0.75f, -3.5f);
//			}else if (stack.getItem() == ItemDefs.natureScythe) {
//				GlStateManager.rotate(-45, 0, 1, 0);
//				GlStateManager.translate(0.2, 0, -0.2);
//			}else{
//				GL11.glRotatef(60F, 1, 0, 0);
//				GL11.glTranslatef(-0.55f, -0.75f, -1f);
//			}
//			break;
//		case GUI:
//			scale = 0.5f;
//			GL11.glTranslatef(0.25f, 0, 0);
//			if (stack.getItem() == ItemDefs.magicBroom){
//				GL11.glRotatef(50F, 1, 0, 0);
//				GL11.glTranslatef(-0.5F, 0F, -1.5F);
//				scale = 1.0f;
//			}else if (stack.getItem() == ItemDefs.winterArm){
//				scale = 1.0f;
//				GL11.glTranslatef(0.5f, 0.5f, 0);
//			}else if (stack.getItem() == ItemDefs.arcaneSpellbook){
//				GL11.glRotatef(240f, 0.0F, 1.0F, 0.0F);
//				GL11.glRotatef(45f, 1.0F, 0.0F, 0.0F);
//				GL11.glTranslatef(0.2f, 0.1f, 0);
//			}else if (stack.getItem() == ItemDefs.earthArmor){
//				GL11.glRotatef(90, 1, 0, 0);
//				scale = 1.0f;
//				GL11.glTranslatef(0.2f, 0.8f, -1.6f);
//			}else if (stack.getItem() == ItemDefs.fireEars){
//				GL11.glRotatef(90, 1, 0, 0.7f);
//				scale = 1.7f;
//				GL11.glTranslatef(0.2f, 0.2f, -0.8f);
//			}else if (stack.getItem() == ItemDefs.waterOrbs){
//				GL11.glRotatef(90, 0, 1, 1);
//				scale = 1.2f;
//				GL11.glTranslatef(-0.4f, 0.3f, -0.4f);
//			}else if (stack.getItem() == ItemDefs.airSled){
//				scale = 2.4f;
//				GL11.glTranslatef(0f, 0.45f, -1.75f);
//			}else if (stack.getItem() == ItemDefs.wardingCandle){
//				scale = 2.4f;
//				GL11.glRotatef(90, 1, 0, 0);
//				GL11.glTranslatef(-0.5f, 0, -4);
//			}else if (stack.getItem() == ItemDefs.natureScythe) {
//				GL11.glTranslated(-1.2, -0.3, 0);
//				scale = 0.35f;
//				GL11.glRotatef(90, 0, 1, 0);
//				GL11.glRotatef(20, 1, 0, 1);
//			}
//		default:
//			break;
//		}
//
//		if (stack.getItem() == ItemDefs.arcaneSpellbook){
//			scale = 3.5f;
//		}else if (stack.getItem() == ItemDefs.winterArm){
//
//		}
//		else{
//			GL11.glRotatef(90F, 1.0F, 0.0F, 0.0F);
//			GL11.glTranslatef(0.3f, 0.75f, 0);
//		}
//		if (type == TransformType.FIRST_PERSON_LEFT_HAND || type == TransformType.FIRST_PERSON_RIGHT_HAND || type == TransformType.THIRD_PERSON_LEFT_HAND || type == TransformType.THIRD_PERSON_RIGHT_HAND) {
//			GL11.glRotated(180, 0, 0, 1);
//			GL11.glTranslated(-0.25, 0.55, 0);
//		}
//		GL11.glScalef(scale, scale, scale);

		if (stack.getItem() == ItemDefs.natureScythe) {
			if (type != TransformType.GUI) {
				GlStateManager.translate(0.05, 0, -0.3);
				GlStateManager.rotate(180, 0, 0, 1);
				GlStateManager.rotate(-90, 1, 0, 0);
			} else {
				GlStateManager.rotate(90, 1, 0, 0);
				GlStateManager.rotate(105, 0, 0, 1);
				GlStateManager.rotate(15, 0, 1, 0);
				GlStateManager.scale(0.35, 0.35, 0.35);
				GlStateManager.translate(0, 0, 1);
			}
		} else if (stack.getItem() == ItemDefs.winterArm) {
			if (type != TransformType.GUI) {
				GlStateManager.translate(0, -0.1, -0.85);
				//GlStateManager.rotate(180, 0, 0, 1);
				//GlStateManager.rotate(-90, 1, 0, 0);
			} else {
				GlStateManager.rotate(90, 0, 1, 0);
				GlStateManager.translate(0, -0.1, -0.4);
				GlStateManager.rotate(135, 0, 0, 1);
				GlStateManager.rotate(25, 0, 1, 0);
				GlStateManager.scale(0.60, 0.60, 0.60);
			}
		} else if (stack.getItem() == ItemDefs.airSled) {
			GlStateManager.translate(0, -0.65, 0);
		} else if (stack.getItem() == ItemDefs.earthArmor) {
			GlStateManager.rotate(180, 0, 0, 1);
			GlStateManager.translate(0, -0.5, 0);
			if (type != TransformType.GUI) {
				GlStateManager.scale(0.60, 0.60, 0.60);
			} else {
				//GlStateManager.rotate(90, 0, 1, 0);
				GlStateManager.translate(0, 0.5, 0);
				//GlStateManager.rotate(135, 0, 0, 1);
				GlStateManager.rotate(25, 0, 1, 0);
				GlStateManager.scale(0.60, 0.60, 0.60);
			}
		} else if (stack.getItem() == ItemDefs.arcaneSpellbook) {
			if (type == TransformType.GUI) {
				GlStateManager.rotate(135, 0, 0, 1);
				GlStateManager.rotate(25, 0, 1, 0);	
				GlStateManager.scale(2.2, 2.2, 2.2);
			}
		} else if (stack.getItem() == ItemDefs.fireEars) {
			if (type == TransformType.GUI) {
				GlStateManager.rotate(25, 0, 0, 1);
				GlStateManager.rotate(25, 0, 1, 0);	
			}		
		} else if (stack.getItem() == ItemDefs.waterOrbs) {
			GlStateManager.translate(0, -0.5, 0);
			GlStateManager.scale(0.8, 0.8, 0.8);
			if (type == TransformType.GUI) {
				GlStateManager.rotate(25, 1, 0, 0);
				//GlStateManager.rotate(25, 0, 1, 0);	
			}		
		} else if (stack.getItem() == ItemDefs.magicBroom) {
			if (type == TransformType.GUI) {
				GlStateManager.translate(0.25, 0, 0);
				GlStateManager.scale(0.5, 0.5, 0.5);
				GlStateManager.rotate(25, 1, 0, 0);
				GlStateManager.rotate(105, 0, 0, 1);	
			}		
		} else if (stack.getItem() == ItemDefs.wardingCandle) {
			GlStateManager.disableCull();
			GlStateManager.scale(1, -1, 1);
			GlStateManager.translate(0, -1.5, 0);
			if (type == TransformType.GUI) {
				GlStateManager.rotate(-25, 1, 0, 0);
				GlStateManager.rotate(-45, 0, 1, 0);
				GlStateManager.translate(0, 0.5, 0);
			}
		}
	}

	private void renderModelByItem(ItemStack stack){
		if (stack == null)
			return;

		if (stack.getItem() == ItemDefs.natureScythe)
			ModelLibrary.instance.sickle.render(null, 0, 0, 0, 0, 0, 0.0625F);
		else if (stack.getItem() == ItemDefs.magicBroom)
			ModelLibrary.instance.magicBroom.render(ModelLibrary.instance.dummyBroom, 0, 0, 0, 0, 0, 0.0625F);
		else if (stack.getItem() == ItemDefs.arcaneSpellbook)
			ModelLibrary.instance.dummyArcaneSpellbook.render(null, 0, 0, 0, 0, 0, 0.0625F);
		else if (stack.getItem() == ItemDefs.winterArm)
			ModelLibrary.instance.winterGuardianArm.render(null, 0, 0, 0, 0, 0, 0.0625F);
		else if (stack.getItem() == ItemDefs.airSled)
			ModelLibrary.instance.airSled.render(null, 0, 0, 0, 0, 0, 0.0625F);
		else if (stack.getItem() == ItemDefs.waterOrbs)
			ModelLibrary.instance.waterOrbs.render(null, 0, 0, 0, 0, 0, 0.0625F);
		else if (stack.getItem() == ItemDefs.fireEars)
			ModelLibrary.instance.fireEars.render(null, 0, 0, 0, 0, 0, 0.0625F);
		else if (stack.getItem() == ItemDefs.earthArmor)
			ModelLibrary.instance.earthArmor.render(null, 0, 0, 0, 0, 0, 0.0625F);
		else if (stack.getItem() == ItemDefs.wardingCandle){
			ModelLibrary.instance.wardingCandle.render(0.0625f);
		}
	}

	private void bindTextureByItem(ItemStack stack){
		if (stack == null)
			return;

		if (stack.getItem() == ItemDefs.natureScythe)
			Minecraft.getMinecraft().renderEngine.bindTexture(sickleLocation);
		else if (stack.getItem() == ItemDefs.magicBroom)
			Minecraft.getMinecraft().renderEngine.bindTexture(broomLocation);
		else if (stack.getItem() == ItemDefs.arcaneSpellbook)
			Minecraft.getMinecraft().renderEngine.bindTexture(arcaneBookLocation);
		else if (stack.getItem() == ItemDefs.winterArm)
			Minecraft.getMinecraft().renderEngine.bindTexture(winterArmLocation);
		else if (stack.getItem() == ItemDefs.airSled)
			Minecraft.getMinecraft().renderEngine.bindTexture(airLocation);
		else if (stack.getItem() == ItemDefs.waterOrbs)
			Minecraft.getMinecraft().renderEngine.bindTexture(waterLocation);
		else if (stack.getItem() == ItemDefs.fireEars)
			Minecraft.getMinecraft().renderEngine.bindTexture(fireLocation);
		else if (stack.getItem() == ItemDefs.earthArmor)
			Minecraft.getMinecraft().renderEngine.bindTexture(earthLocation);
		else if (stack.getItem() == ItemDefs.wardingCandle)
			Minecraft.getMinecraft().renderEngine.bindTexture(candleLocation);
	}
}
