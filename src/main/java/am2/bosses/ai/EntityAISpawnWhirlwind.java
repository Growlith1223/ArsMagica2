package am2.bosses.ai;

import am2.bosses.BossActions;
import am2.bosses.IArsMagicaBoss;
import am2.entity.EntityWhirlwind;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.SoundCategory;

public class EntityAISpawnWhirlwind extends EntityAIBase{
	private final EntityLiving host;
	private int cooldownTicks = 0;
	private boolean hasCasted = false;
	private int windTicks = 0;
	private float movespeed = 0;

	public EntityAISpawnWhirlwind(IArsMagicaBoss host, float movespeed){
		this.host = (EntityLiving)host;
		this.setMutexBits(1);
		this.movespeed = movespeed;
	}

	@Override
	public boolean shouldExecute(){
		cooldownTicks--;
		boolean execute = ((IArsMagicaBoss)host).getCurrentAction() == BossActions.IDLE && host.getAttackTarget() != null && cooldownTicks <= 0;
		if (execute) hasCasted = false;
		return execute;
	}

	@Override
	public boolean continueExecuting(){
		return !hasCasted && host.getAttackTarget() != null && !host.getAttackTarget().isDead;
	}

	@Override
	public void resetTask(){
		((IArsMagicaBoss)host).setCurrentAction(BossActions.IDLE);
		cooldownTicks = 10;
		hasCasted = true;
		windTicks = 0;
	}

	@Override
	public void updateTask(){
		host.getLookHelper().setLookPositionWithEntity(host.getAttackTarget(), 30, 30);
		if (host.getDistanceSqToEntity(host.getAttackTarget()) > 64){

			double deltaZ = host.getAttackTarget().posZ - host.posZ;
			double deltaX = host.getAttackTarget().posX - host.posX;

			double angle = -Math.atan2(deltaZ, deltaX);

			double newX = host.getAttackTarget().posX + (Math.cos(angle) * 6);
			double newZ = host.getAttackTarget().posZ + (Math.sin(angle) * 6);

			host.getNavigator().tryMoveToXYZ(newX, host.getAttackTarget().posY, newZ, 0.5f);
		}else if (!host.canEntityBeSeen(host.getAttackTarget())){
			host.getNavigator().tryMoveToEntityLiving(host.getAttackTarget(), 0.5f);
		}else{
			if (((IArsMagicaBoss)host).getCurrentAction() != BossActions.CASTING)
				((IArsMagicaBoss)host).setCurrentAction(BossActions.CASTING);

			windTicks++;
			if (windTicks == 12 && !host.world.isRemote){
				if (!host.world.isRemote)
					host.world.playSound(host.posX, host.posY, host.posZ, ((IArsMagicaBoss)host).getAttackSound(), SoundCategory.HOSTILE, 1.0f, 1.0f, false);
				EntityWhirlwind whirlwind = new EntityWhirlwind(host.world);
				whirlwind.setPosition(host.posX, host.posY + host.getEyeHeight(), host.posZ);
				host.world.spawnEntity(whirlwind);
			}
		}
		if (windTicks >= 23){
			resetTask();
		}
	}

}
