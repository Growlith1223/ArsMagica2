package am2.bosses.ai;

import java.util.List;

import am2.api.DamageSources;
import am2.bosses.BossActions;
import am2.bosses.IArsMagicaBoss;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.SoundCategory;

public class EntityAIStrikeAttack extends EntityAIBase{
	private final EntityLiving host;
	private final float moveSpeed;
	private EntityLivingBase target;
	private int cooldownTicks = 0;
	private final float damage;
	private final DamageSources.DamageSourceTypes damageType;

	public EntityAIStrikeAttack(IArsMagicaBoss host, float moveSpeed, float damage, DamageSources.DamageSourceTypes damageType){
		this.host = ((EntityLiving)host);
		this.moveSpeed = moveSpeed;
		this.setMutexBits(1);
		this.damage = damage;
		this.damageType = damageType;
	}

	@Override
	public boolean shouldExecute(){
		if (cooldownTicks-- > 0 || ((IArsMagicaBoss)host).getCurrentAction() != BossActions.IDLE || !((IArsMagicaBoss)host).isActionValid(BossActions.STRIKE))
			return false;
		EntityLivingBase AITarget = host.getAttackTarget();
		if (AITarget == null || AITarget.isDead) return false;
		if (AITarget != null && host.getDistanceSqToEntity(AITarget) > 4D){
			if (!host.getNavigator().tryMoveToEntityLiving(AITarget, moveSpeed))
				return false;
		}
		this.target = AITarget;
		return true;
	}

	@Override
	public boolean continueExecuting(){
		EntityLivingBase AITarget = host.getAttackTarget();
		if (AITarget == null || AITarget.isDead || (((IArsMagicaBoss)host).getCurrentAction() == BossActions.STRIKE && ((IArsMagicaBoss)host).getTicksInCurrentAction() > ((IArsMagicaBoss)host).getCurrentAction().getMaxActionTime())){
			((IArsMagicaBoss)host).setCurrentAction(BossActions.IDLE);
			cooldownTicks = 5;
			return false;
		}
		return true;
	}

	@Override
	public void updateTask(){
		host.getLookHelper().setLookPositionWithEntity(target, 30, 30);
		host.getNavigator().tryMoveToEntityLiving(target, moveSpeed);
		if (host.getDistanceSqToEntity(target) < 16)
			if (((IArsMagicaBoss)host).getCurrentAction() != BossActions.STRIKE)
				((IArsMagicaBoss)host).setCurrentAction(BossActions.STRIKE);

		if (((IArsMagicaBoss)host).getCurrentAction() == BossActions.STRIKE && ((IArsMagicaBoss)host).getTicksInCurrentAction() > 12){

			if (!host.world.isRemote)
				host.world.playSound(host.posX, host.posY, host.posZ, ((IArsMagicaBoss)host).getAttackSound(), SoundCategory.HOSTILE, 1.0f, 1.0f, false);

			double offsetX = Math.cos(host.rotationYaw) * 2;
			double offsetZ = Math.sin(host.rotationYaw) * 2;
			List<EntityLivingBase> aoeEntities = host.world.getEntitiesWithinAABB(EntityLivingBase.class, host.getEntityBoundingBox().offset(offsetX, 0, offsetZ).expand(2.5, 2, 2.5));
			for (EntityLivingBase ent : aoeEntities){
				if (ent == host) continue;
				ent.attackEntityFrom(DamageSources.causeDamage(damageType, host, true), damage);
			}
		}
	}
}
