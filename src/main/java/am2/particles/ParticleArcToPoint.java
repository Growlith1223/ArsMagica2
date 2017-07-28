package am2.particles;

import am2.utils.MathUtilities;
import net.minecraft.util.math.Vec3d;

public class ParticleArcToPoint extends ParticleController{

	private final Vec3d start;
	private final Vec3d target;
	private Vec3d firstControl;
	private Vec3d secondControl;
	private float percent;
	private float speed;
	private final float offsetFactor;
	private final float halfOffsetFactor;

	public ParticleArcToPoint(AMParticle particleEffect, int priority, double startX, double startY, double startZ, double endX, double endY, double endZ, boolean exclusive){
		super(particleEffect, priority, exclusive);
		start = new Vec3d(startX, startY, startZ);
		target = new Vec3d(endX, endY, endZ);
		percent = 0.0f;
		speed = 0.03f;
		offsetFactor = 10;
		halfOffsetFactor = offsetFactor / 2;
		generateControlPoints();
	}

	public ParticleArcToPoint(AMParticle particleEffect, int priority, double endX, double endY, double endZ, boolean exclusive){
		this(particleEffect, priority, particleEffect.getPosX(), particleEffect.getPosY(), particleEffect.getPosZ(), endX, endY, endZ, exclusive);
	}

	public ParticleArcToPoint generateControlPoints(){
		firstControl = new Vec3d(
				start.xCoord + ((target.xCoord - start.xCoord) / 3),
				start.yCoord + ((target.yCoord - start.yCoord) / 3),
				start.zCoord + ((target.zCoord - start.zCoord) / 3));

		secondControl = new Vec3d(
				start.xCoord + ((target.xCoord - start.xCoord) / 3 * 2),
				start.yCoord + ((target.yCoord - start.yCoord) / 3 * 2),
				start.zCoord + ((target.zCoord - start.zCoord) / 3 * 2));

		double offsetX = (particle.getworld().rand.nextFloat() * offsetFactor) - halfOffsetFactor;
		double offsetZ = (particle.getworld().rand.nextFloat() * offsetFactor) - halfOffsetFactor;
		double offsetY = (particle.getworld().rand.nextFloat() * offsetFactor) - halfOffsetFactor;

		Vec3d offset = new Vec3d(offsetX, offsetY, offsetZ);

		firstControl = firstControl.add(offset);
		secondControl = secondControl.add(offset);

		return this;
	}

	public ParticleArcToPoint specifyControlPoints(Vec3d first, Vec3d second){
		this.firstControl = first;
		this.secondControl = second;
		return this;
	}

	public ParticleArcToPoint SetSpeed(float speed){
		this.speed = speed;
		return this;
	}

	@Override
	public void doUpdate(){
		percent += speed;
		if (percent >= 1.0f){
			this.finish();
			return;
		}
		Vec3d bez = MathUtilities.bezier(start, firstControl, secondControl, target, percent);
		particle.setPosition(bez.xCoord, bez.yCoord, bez.zCoord);
	}

	@Override
	public ParticleController clone(){
		return new ParticleArcToPoint(particle, priority, target.xCoord, target.yCoord, target.zCoord, exclusive).SetSpeed(speed).specifyControlPoints(firstControl, secondControl);
	}

}
