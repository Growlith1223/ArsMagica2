package am2.particles;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.math.MathHelper;

public class ParticleApproachEntity extends ParticleController{

	private final Entity target;
	private final double approachSpeed;
	private final double targetDistance;

	public ParticleApproachEntity(AMParticle particleEffect, Entity approachEntity, double approachSpeed, double targetDistance, int priority, boolean exclusive){
		super(particleEffect, priority, exclusive);
		this.target = approachEntity;
		this.approachSpeed = approachSpeed;
		this.targetDistance = targetDistance;
	}

	@Override
	public void doUpdate(){

		if (target == null){
			this.finish();
			return;
		}

		double posX;
		double posZ;
		double posY = particle.getPosY();
		double angle;

		double distanceToTarget = target.getDistanceSq(particle.getPosX(), particle.getPosY(), particle.getPosZ());
		double deltaZ = target.posZ - particle.getPosZ();
		double deltaX = target.posX - particle.getPosX();
		angle = Math.atan2(deltaZ, deltaX);

		double radians = angle;

		posX = particle.getPosX() + (approachSpeed * Math.cos(radians));
		posZ = particle.getPosZ() + (approachSpeed * Math.sin(radians));
		double deltaY;

		if (target instanceof EntityLiving){
			EntityLiving entityliving = (EntityLiving)target;
			deltaY = posY - (entityliving.posY + entityliving.getEyeHeight());
		}else if (target instanceof EntityItem){
			deltaY = posY - target.posY;
		}else{
			deltaY = (target.getEntityBoundingBox().minY + target.getEntityBoundingBox().maxY) / 2D - posY;
		}
		double horizontalDistance = MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ);
		float pitchRotation = (float)(-Math.atan2(deltaY, horizontalDistance));
		double pitchRadians = pitchRotation;

		posY = particle.getPosY() - (approachSpeed * Math.sin(pitchRadians));

		if (distanceToTarget <= (targetDistance * targetDistance)){
			this.finish();
		}else{
			particle.setPosition(posX, posY, posZ);
		}
	}

	@Override
	public ParticleController clone(){
		return new ParticleApproachEntity(particle, target, approachSpeed, targetDistance, priority, exclusive);
	}

}
