package am2.entity.ai;

import am2.buffs.BuffEffectWateryGrave;
import am2.packet.AMNetHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityAIWaterElementalAttack extends EntityAIBase{
	World world;
	EntityCreature attacker;
	EntityLivingBase entityTarget;

	/**
	 * An amount of decrementing ticks that allows the entity to attack once the tick reaches 0.
	 */
	int attackTick;
	float field_75440_e;
	boolean field_75437_f;
	Path field_75438_g;
	Class<? extends Entity> classTarget;
	private int field_75445_i;
	public EntityAIWaterElementalAttack(EntityCreature par1EntityLiving, Class<? extends Entity> par2Class, float par3, boolean par4){
		this(par1EntityLiving, par3, par4);
		this.classTarget = par2Class;
	}

	public EntityAIWaterElementalAttack(EntityCreature par1EntityLiving, float par2, boolean par3){
		this.attackTick = 0;
		this.attacker = par1EntityLiving;
		this.world = par1EntityLiving.world;
		this.field_75440_e = par2;
		this.field_75437_f = par3;
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute(){
		EntityLivingBase var1 = this.attacker.getAttackTarget();

		if (var1 == null){
			return false;
		}else if (this.classTarget != null && !this.classTarget.isAssignableFrom(var1.getClass())){
			return false;
		}else{
			this.entityTarget = var1;
			this.field_75438_g = this.attacker.getNavigator().getPathToEntityLiving(this.entityTarget);
			return this.field_75438_g != null;
		}
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean continueExecuting(){
		EntityLivingBase var1 = this.attacker.getAttackTarget();
		return var1 == null ? false :
				(!this.entityTarget.isEntityAlive() ? false :
						(!this.field_75437_f ? !this.attacker.getNavigator().noPath() :
								this.attacker.isWithinHomeDistanceCurrentPosition()));
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting(){
		this.attacker.getNavigator().setPath(this.field_75438_g, this.field_75440_e);
		this.field_75445_i = 0;
	}

	/**
	 * Resets the task
	 */
	public void resetTask(){
		this.entityTarget = null;
		this.attacker.getNavigator().clearPathEntity();
	}

	/**
	 * Updates the task
	 */
	public void updateTask(){
		this.attacker.getLookHelper().setLookPositionWithEntity(this.entityTarget, 30.0F, 30.0F);

		if ((this.field_75437_f || this.attacker.getEntitySenses().canSee(this.entityTarget)) && --this.field_75445_i <= 0){
			this.field_75445_i = 4 + this.attacker.getRNG().nextInt(7);
			this.attacker.getNavigator().tryMoveToEntityLiving(this.entityTarget, this.field_75440_e);
		}

		this.attackTick = Math.max(this.attackTick - 1, 0);
		double var1 = (double)(this.attacker.width * 2.0F * this.attacker.width * 2.0F);

		if (this.attacker.getDistanceSq(this.entityTarget.posX, this.entityTarget.getEntityBoundingBox().minY, this.entityTarget.posZ) <= var1){
			if (this.attackTick <= 0){
				this.attackTick = 20;
				double var9 = attacker.posX - entityTarget.posX;
				double var7;

				for (var7 = attacker.posZ - entityTarget.posZ; var9 * var9 + var7 * var7 < 1.0E-4D; var7 = (Math.random() - Math.random()) * 0.01D){
					var9 = (Math.random() - Math.random()) * 0.01D;
				}

				double mX = entityTarget.motionX;
				double mY = entityTarget.motionY;
				double mZ = entityTarget.motionZ;

				entityTarget.isAirBorne = true;
				float var10 = MathHelper.sqrt(var9 * var9 + var7 * var7);
				float var8 = 0.4F;
				mX /= 2.0D;
				mY /= 2.0D;
				mZ /= 2.0D;
				mX -= var9 / (double)var10 * (double)var8;
				mY += (double)var8;
				mZ -= var7 / (double)var10 * (double)var8;

				if (mY > 0.4000000059604645D){
					mY = 0.4000000059604645D;
				}

				entityTarget.motionX = mX * 8;
				entityTarget.motionY = mY * 8;
				entityTarget.motionZ = mZ * 8;

				AMNetHandler.INSTANCE.sendVelocityAddPacket(world, entityTarget, mX, mY, mZ);

				entityTarget.addPotionEffect(new BuffEffectWateryGrave(100, 1));
			}
		}
	}
}
