package am2.bosses.ai;

import java.util.Iterator;
import java.util.List;

import am2.api.DamageSources;
import am2.api.math.AMVector3;
import am2.bosses.BossActions;
import am2.bosses.EntityAirGuardian;
import am2.bosses.IArsMagicaBoss;
import am2.entity.EntityWhirlwind;
import am2.packet.AMNetHandler;
import am2.utils.MathUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

public class EntityAIHurricane extends EntityAIBase{

	private final EntityAirGuardian host;
	private EntityLivingBase target;
	private int cooldownTicks = 0;

	public EntityAIHurricane(EntityAirGuardian host, float moveSpeed){
		this.host = host;
		this.setMutexBits(1);
	}

	@Override
	public boolean shouldExecute(){
		if (cooldownTicks-- > 0 || ((IArsMagicaBoss)host).getCurrentAction() != BossActions.IDLE || !((IArsMagicaBoss)host).isActionValid(BossActions.SPINNING))
			return false;
		EntityLivingBase AITarget = host.getAttackTarget();
		if (AITarget == null || AITarget.isDead || AITarget.getDistanceSqToEntity(host) > 25) return false;
		this.target = AITarget;
		((IArsMagicaBoss)host).setCurrentAction(BossActions.SPINNING);
		return true;
	}

	@Override
	public boolean continueExecuting(){
		EntityLivingBase AITarget = ((EntityLiving)host).getAttackTarget();
		if (host.hitCount >= 10){
			((IArsMagicaBoss)host).setCurrentAction(BossActions.IDLE);
			cooldownTicks = 20;
			return false;
		}
		if (AITarget == null || AITarget.isDead || ((IArsMagicaBoss)host).getTicksInCurrentAction() > BossActions.SPINNING.getMaxActionTime()){

			if (!host.world.isRemote){
				int y = (int)host.posY + 1;
				for (int x = -1; x <= 1; ++x){
					for (int z = -1; z <= 1; ++z){
						BlockPos pos = new BlockPos(host.posX + x, y, host.posZ + z);
						while (!host.world.canBlockSeeSky(pos) && host.world.getBlockState(pos).getBlock() != Blocks.BEDROCK){
							if (Math.abs(y - host.posY) > 10) break;
							host.world.destroyBlock(new BlockPos ((int)host.posX + x, y++, (int)host.posZ + z), true);
						}
						y = (int)host.posY + 2;
					}
				}
			}

			List<EntityLivingBase> nearbyEntities = host.world.getEntitiesWithinAABB(EntityLivingBase.class, host.getEntityBoundingBox().expand(2, 2, 2));
			for (Iterator<EntityLivingBase> enti = nearbyEntities.iterator();enti.hasNext();){
				EntityLivingBase ent = enti.next();
				if (ent == host) continue;
				AMVector3 movement = MathUtilities.GetMovementVectorBetweenPoints(new AMVector3(host), new AMVector3(ent));
				float factor = 2.15f;

				double x = -(movement.x * factor);
				double y = 2.5f;
				double z = -(movement.z * factor);

				ent.attackEntityFrom(DamageSources.causeWindDamage(host), 12);

				ent.addVelocity(x, y, z);

				if (ent instanceof EntityPlayer){
					AMNetHandler.INSTANCE.sendVelocityAddPacket(host.world, ent, x, y, z);
				}
				ent.fallDistance = 0f;
			}
			((IArsMagicaBoss)host).setCurrentAction(BossActions.IDLE);
			cooldownTicks = 20;
			return false;
		}
		return true;
	}

	@Override
	public void updateTask(){
		host.getLookHelper().setLookPositionWithEntity(target, 30, 30);
		//host.getNavigator().tryMoveToEntityLiving(target, moveSpeed);
		List<EntityLivingBase> nearbyEntities = host.world.getEntitiesWithinAABB(EntityLivingBase.class, host.getEntityBoundingBox().expand(6, 3, 6));
		for (EntityLivingBase ent : nearbyEntities){
			if (ent == host) continue;

			if (!host.world.isRemote && ent instanceof EntityWhirlwind){
				ent.setDead();
				continue;
			}
			AMVector3 movement = MathUtilities.GetMovementVectorBetweenPoints(new AMVector3(host).add(new AMVector3(0, host.getEyeHeight(), 0)), new AMVector3(ent));
			float factor = 0.18f;

			double x = (movement.x * factor);
			double y = (movement.y * factor);
			double z = (movement.z * factor);

			double oX = ent.motionX, oY = ent.motionY, oZ = ent.motionZ;
			ent.addVelocity(x, y, z);

			if (Math.abs(ent.motionX) > Math.abs(x * 2)){
				ent.motionX = x * (ent.motionX / ent.motionX);
			}
			if (Math.abs(ent.motionY) > Math.abs(y * 2)){
				ent.motionY = y * (ent.motionY / ent.motionY);
			}
			if (Math.abs(ent.motionZ) > Math.abs(z * 2)){
				ent.motionZ = z * (ent.motionZ / ent.motionZ);
			}

			if (ent instanceof EntityPlayer){
				AMNetHandler.INSTANCE.sendVelocityAddPacket(host.world, ent, -(oX - ent.motionX), -(oY - ent.motionY), -(oZ - ent.motionZ));
			}
			ent.fallDistance = 0f;

		}
	}
}
