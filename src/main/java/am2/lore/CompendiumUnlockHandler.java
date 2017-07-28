package am2.lore;

import am2.ArsMagica2;
import am2.api.SkillRegistry;
import am2.api.event.PlayerMagicLevelChangeEvent;
import am2.api.event.SkillLearnedEvent;
import am2.api.event.SpellCastEvent;
import am2.api.extensions.IArcaneCompendium;
import am2.api.skill.SkillPoint;
import am2.extensions.EntityExtension;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry.EntityRegistration;

/**
 * This class should handle compendium unlocks wherever possible through events.
 * If it is not possible, then all calls should use the static utility methods here.
 *
 * @author Mithion
 */
public class CompendiumUnlockHandler{
	/**
	 * This is a catch all method - it's genericized to attempt to unlock a compendium entry for anything AM2 based that the player picks up
	 *
	 * @param event
	 */
	@SubscribeEvent
	public void onPlayerPickupItem(EntityItemPickupEvent event){
		IArcaneCompendium instance = ArcaneCompendium.For(event.getEntityPlayer());
		instance.unlockRelatedItems(event.getItem().getEntityItem());
	}

	/**
	 * Any magic level based unlocks should go in here
	 *
	 * @param event
	 */
	@SubscribeEvent
	public void onPlayerMagicLevelChange(PlayerMagicLevelChangeEvent event){
		if (event.getEntity().world.isRemote && event.getEntity() instanceof EntityPlayer){
			IArcaneCompendium instance = ArcaneCompendium.For(event.getEntityPlayer());
			if (event.getLevel() >= 5){
				//ArcaneCompendium.instance.unlockEntry("dungeonsAndExploring");
				instance.unlockEntry("enchantments");
			}
			if (event.getLevel() >= 10){
				instance.unlockEntry("armorMage");
				instance.unlockEntry("playerjournal");
			}
			if (event.getLevel() >= 15){
				instance.unlockEntry("BossWaterGuardian");
				instance.unlockEntry("BossEarthGuardian");
				instance.unlockEntry("rituals");
				instance.unlockEntry("inlays");
				instance.unlockEntry("inlays_structure");
			}
			if (event.getLevel() >= 20){
				instance.unlockEntry("armorBattlemage");
			}
			if (event.getLevel() >= 25){
				instance.unlockEntry("BossAirGuardian");
				instance.unlockEntry("BossArcaneGuardian");
				instance.unlockEntry("BossLifeGuardian");
			}
			if (event.getLevel() >= 35){
				instance.unlockEntry("BossNatureGuardian");
				instance.unlockEntry("BossWinterGuardian");
				instance.unlockEntry("BossFireGuardian");
				instance.unlockEntry("BossLightningGuardian");
				instance.unlockEntry("BossEnderGuardian");
			}
		}
	}

	/**
	 * This should handle all mobs and the Astral Barrier
	 *
	 * @param event
	 */
	@SubscribeEvent
	public void onEntityDeath(LivingDeathEvent event){
		if (event.getEntityLiving().world.isRemote && event.getSource().getEntity() instanceof EntityPlayer){
			if (event.getEntity() instanceof EntityEnderman){
				ArcaneCompendium.For((EntityPlayer)event.getSource().getEntity()).unlockEntry("blockastralbarrier");
			}else{
				EntityRegistration reg = EntityRegistry.instance().lookupModSpawn(event.getEntityLiving().getClass(), true);
				if (reg != null && reg.getContainer().matches(ArsMagica2.instance)){
					String id = reg.getEntityName();
					ArcaneCompendium.For((EntityPlayer)event.getSource().getEntity()).unlockEntry(id);
				}
			}
		}
	}


	/**
	 * Any skill-based unlocks should go in here
	 *
	 * @param event
	 */
	@SubscribeEvent
	public void onSkillLearned(SkillLearnedEvent event){
		IArcaneCompendium instance = ArcaneCompendium.For(event.getEntityPlayer());
		if (event.getSkill().equals(SkillRegistry.getSkillFromName("summon"))){
			instance.unlockEntry("crystal_phylactery");
			instance.unlockEntry("summoner");
		} else if (event.getSkill().equals(SkillRegistry.getSkillFromName("true_sight"))){
			instance.unlockEntry("illusionBlocks");
		} else if (event.getSkill().getPoint().equals(SkillPoint.SILVER_POINT)){
			instance.unlockEntry("silver_skills");
		}
	}

	/**
	 * Any spell-based unlocks should go here (eg, low mana based unlocks, affinity, etc.)
	 *
	 * @param event
	 */
	@SubscribeEvent
	public void onSpellCast(SpellCastEvent.Pre event){
		if (event.entityLiving instanceof EntityPlayer){
			IArcaneCompendium instance = ArcaneCompendium.For((EntityPlayer) event.entityLiving);
			instance.unlockEntry("unlockingPowers");
			instance.unlockEntry("affinity");
			if (EntityExtension.For(event.entityLiving).getCurrentMana() < EntityExtension.For(event.entityLiving).getMaxMana() / 2)
				instance.unlockEntry("mana_potion");
		}
	}

	/**
	 * This is another genericized method, which attempts to unlock any entry for something the player crafts
	 */
	@SubscribeEvent
	public void onCrafting(ItemCraftedEvent event){
		if (event.player.world.isRemote){
			IArcaneCompendium instance = ArcaneCompendium.For(event.player);
			instance.unlockRelatedItems(event.crafting);
		}
	}
}
