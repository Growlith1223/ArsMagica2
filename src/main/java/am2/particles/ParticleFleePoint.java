package am2.particles;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ParticleFleePoint extends ParticleController{

	private Vec3d target;
	private double fleeSpeed;
	private double targetDistance;

	public ParticleFleePoint(AMParticle particleEffect, Vec3d fleePoint, double fleeSpeed, double targetDistance, int priority, boolean exclusive){
		super(particleEffect, priority, exclusive);
		this.target = fleePoint;
		this.fleeSpeed = fleeSpeed;
		this.targetDistance = targetDistance;
	}

	@Override
	public void doUpdate(){

		double posX;
		double posZ;
		double posY = particle.getPosY();
		double angle;

		double distanceToTarget = new Vec3d(particle.getPosX(), particle.getPosY(), particle.getPosZ()).distanceTo(target);
		double deltaZ = particle.getPosZ() - target.zCoord;
		double deltaX = particle.getPosX() - target.xCoord;
		angle = Math.atan2(deltaZ, deltaX);

		double radians = angle;

		posX = particle.getPosX() + (fleeSpeed * Math.cos(radians));
		posZ = particle.getPosZ() + (fleeSpeed * Math.sin(radians));
		double deltaY = target.yCoord - posY;
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
		return new ParticleFleePoint(particle, target, fleeSpeed, targetDistance, priority, exclusive);
	}

}
