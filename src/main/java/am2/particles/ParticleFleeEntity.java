package am2.particles;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.MathHelper;

public class ParticleFleeEntity extends ParticleController{

	private Entity target;
	private double fleeSpeed;
	private double targetDistance;

	public ParticleFleeEntity(AMParticle particleEffect, Entity fleeEntity, double fleeSpeed, double targetDistance, int priority, boolean exclusive){
		super(particleEffect, priority, exclusive);
		this.target = fleeEntity;
		this.fleeSpeed = fleeSpeed;
		this.targetDistance = targetDistance;
	}

	@Override
	public void doUpdate(){

		double posX;
		double posZ;
		double posY = particle.getPosY();
		double angle;

		double distanceToTarget = target.getDistance(particle.getPosX(), particle.getPosY(), particle.getPosZ());
		double deltaZ = particle.getPosZ() - target.posZ;
		double deltaX = particle.getPosX() - target.posX;
		angle = Math.atan2(deltaZ, deltaX);

		double radians = angle;

		posX = particle.getPosX() + (fleeSpeed * Math.cos(radians));
		posZ = particle.getPosZ() + (fleeSpeed * Math.sin(radians));
		double deltaY;

		if (target instanceof EntityLiving){
			EntityLiving entityliving = (EntityLiving)target;
			deltaY = posY - (entityliving.posY + (double)entityliving.getEyeHeight());
		}else{
			deltaY = (target.getEntityBoundingBox().minY + target.getEntityBoundingBox().maxY) / 2D - posY;
		}
		double horizontalDistance = MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ);
		float pitchRotation = (float)(-Math.atan2(deltaY, horizontalDistance));
		double pitchRadians = pitchRotation;

		posY = particle.getPosY() + (fleeSpeed * Math.sin(pitchRadians));

		if (distanceToTarget > targetDistance){
			this.finish();
		}else{
			particle.setPosition(posX, posY, posZ);
		}
	}

	@Override
	public ParticleController clone(){
		return new ParticleFleeEntity(particle, target, fleeSpeed, targetDistance, priority, exclusive);
	}

}
