package am2.bosses.ai;

import am2.bosses.BossActions;
import am2.bosses.EntityEnderGuardian;
import am2.utils.NPCSpells;
import am2.utils.SpellUtils;
import net.minecraft.entity.EntityLivingBase;
import thehippomaster.AnimationAPI.AIAnimation;
import thehippomaster.AnimationAPI.IAnimatedEntity;

public class EntityAIProtect extends AIAnimation{

	private int cooldownTicks = 0;

	public EntityAIProtect(IAnimatedEntity entity){
		super(entity);
	}

	@Override
	public int getAnimID(){
		return BossActions.SHIELD_BASH.ordinal();
	}

	@Override
	public boolean isAutomatic(){
		return false;
	}

	@Override
	public int getDuration(){
		return 35;
	}

	@Override
	public boolean shouldAnimate(){
		//accessor method in AIAnimation that gives access to the entity
		EntityEnderGuardian living = getEntity();

		//must have an attack target
		if (living.getAttackTarget() == null || living.getTicksSinceLastAttack() > 40)
			return false;

		return cooldownTicks-- <= 0;
	}

	@Override
	public void resetTask(){
		cooldownTicks = 20;
		EntityLivingBase ent = getEntity();
		ent.extinguish();
		SpellUtils.applyStackStage(NPCSpells.instance.dispel, getEntity(), null, ent.posX, ent.posY, ent.posZ, null, ent.world, false, false, 0);
		super.resetTask();
	}

	@Override
	public void updateTask(){
		EntityEnderGuardian guardian = getEntity();
		if (guardian.getAttackTarget() != null){
			guardian.getLookHelper().setLookPositionWithEntity(guardian.getAttackTarget(), 30, 30);
		}
	}

}
