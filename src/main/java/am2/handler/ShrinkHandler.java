package am2.handler;

import am2.ArsMagica2;
import am2.api.math.AMVector2;
import am2.defs.PotionEffectsDefs;
import am2.extensions.EntityExtension;
import am2.utils.EntityUtils;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ShrinkHandler{

	@SubscribeEvent
	public void onEntityLiving(LivingEvent event){
		if (!(event.getEntityLiving() instanceof EntityPlayer)) return;

		EntityPlayer player = (EntityPlayer) event.getEntityLiving();

		if (ArsMagica2.disabledSkills.isSkillDisabled("shrink"))
			return;


		EntityExtension exProps = null;

		try{
			exProps = EntityExtension.For(player);
		}catch (Throwable t){
			return;
		}

		if (exProps.originalSize == null){
			exProps.originalSize = new AMVector2(player.width, player.height);
		}

		boolean shrunk = exProps.isShrunk();

		if (!player.world.isRemote && shrunk && !player.isPotionActive(PotionEffectsDefs.shrink)){
			exProps.setShrunk(false);
			shrunk = false;
			//player.yOffset = (float)exProps.getOriginalSize().y * 0.9f;
		}else if (!player.world.isRemote && !shrunk && player.isPotionActive(PotionEffectsDefs.shrink)){
			exProps.setShrunk(true);
			shrunk = true;
			//player.yOffset = 0.0F;
		}

		float shrinkPct = exProps.getShrinkPct();
		if (shrunk && shrinkPct < 1f){
			shrinkPct = Math.min(1f, shrinkPct + 0.005f);
		}else if (!shrunk && shrinkPct > 0f){
			shrinkPct = Math.max(0f, shrinkPct - 0.005f);
		}
		exProps.setShrinkPct(shrinkPct);

		if (exProps.getShrinkPct() > 0f){
			if (exProps.shrinkAmount == 0f || //shrink hasn't yet been applied
					exProps.getOriginalSize().x * 0.5 != player.width || //width has changed through other means
					exProps.getOriginalSize().y * 0.5 != player.height){ //height has changed through other means
				exProps.setOriginalSize(new AMVector2(player.width, player.height));
				exProps.shrinkAmount = 0.5f;
				EntityUtils.setSize(player, player.width * exProps.shrinkAmount, player.height * exProps.shrinkAmount);
				//player.eyeHeight = player.getDefaultEyeHeight() * exProps.shrinkAmount;
				//player.yOffset = 0.0f;
			}
		}else{
			if (exProps.shrinkAmount != 0f){
				AMVector2 size = EntityExtension.For(player).getOriginalSize();
				EntityUtils.setSize(player, (float)(size.x), (float)(size.y));
				exProps.shrinkAmount = 0f;
				//player.eyeHeight = player.getDefaultEyeHeight();
				//player.yOffset = 0.0f;
				if (exProps.getIsFlipped()){
					event.getEntityLiving().move(MoverType.SELF, 0.0, -1.0, 0.0);
				}
			}
		}

		// update Y offset
		if (player.world.isRemote && exProps.getPrevShrinkPct() != exProps.getShrinkPct()){
			// Vanilla player is 1.8f height with 1.62f yOffset => 0.9f
			//player.yOffset = (float)exProps.getOriginalSize().y * 0.9f * (1f - 0.5f * exProps.getShrinkPct());
		}
	}
}