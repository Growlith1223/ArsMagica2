package am2.bosses.ai;

import java.util.List;

import am2.api.DamageSources;
import am2.bosses.BossActions;
import am2.bosses.EntityLightningGuardian;
import am2.defs.AMSounds;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.SoundCategory;
import thehippomaster.AnimationAPI.AIAnimation;
import thehippomaster.AnimationAPI.IAnimatedEntity;

/*
*/

public class EntityAIStatic extends AIAnimation{

	private int cooldownTicks = 0;

	public EntityAIStatic(IAnimatedEntity entity){
		super(entity);
		this.setMutexBits(3);
	}

	@Override
	public boolean shouldAnimate(){
		//accessor method in AIAnimation that gives access to the entity
		EntityLiving living = getEntity();

		//must have an attack target
		if (living.getAttackTarget() == null || living.getDistanceSqToEntity(living.getAttackTarget()) > 64D || !living.getEntitySenses().canSee(living.getAttackTarget()))
			return false;

		return cooldownTicks-- <= 0;
	}

	@Override
	public int getAnimID(){
		return BossActions.CHARGE.ordinal();
	}

	@Override
	public boolean isAutomatic(){
		return false;
	}

	@Override
	public int getDuration(){
		return 107;
	}

	@Override
	public void resetTask(){
		cooldownTicks = 50;
		doStrike();
		super.resetTask();
	}

	@Override
	public void updateTask(){
		EntityLightningGuardian guardian = getEntity();
		if (guardian.getAttackTarget() != null){
			guardian.getLookHelper().setLookPositionWithEntity(guardian.getAttackTarget(), 10, 10);
			if (guardian.getTicksInCurrentAction() == 20){
				if (!guardian.world.isRemote)
					guardian.world.playSound(guardian.posX, guardian.posY, guardian.posZ, AMSounds.LIGHTNING_GUARDIAN_STATIC, SoundCategory.HOSTILE, 1.0f, guardian.getRNG().nextFloat() * 0.5f + 0.5f, false);
			}
			if (guardian.getTicksInCurrentAction() > 66 && guardian.getTicksInCurrentAction() % 15 == 0 && guardian.getEntitySenses().canSee(guardian.getAttackTarget())){
				doStrike();
			}
		}
	}

	private void doStrike(){
		EntityLightningGuardian guardian = getEntity();
		List<EntityLivingBase> entities = guardian.world.getEntitiesWithinAABB(EntityLivingBase.class, guardian.getEntityBoundingBox().expand(8, 3, 8));
		for (EntityLivingBase e : entities)
			if (e != guardian)
				e.attackEntityFrom(DamageSources.causeLightningDamage(guardian), 8);
	}
}
