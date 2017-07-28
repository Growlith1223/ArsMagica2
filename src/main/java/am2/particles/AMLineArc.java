package am2.particles;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AMLineArc extends Particle{

	Vec3d targetPoint;
	Vec3d currentTargetPoint;
	Vec3d sourcePoint;
	Entity targetEntity;
	Entity sourceEntity;

	boolean hadSource = false;
	boolean hadTarget = false;
	boolean ignoreAge = true;

	double deviation;
	float speed;
	float width;
	boolean extendToTarget;
	float extensionProgress;
	ResourceLocation rl;
	float forwardFactor;

	public AMLineArc(World world, double x, double y, double z, double targetX, double targetY, double targetZ, String IIconName){
		super(world, x, y, z);
		targetPoint = new Vec3d(targetX, targetY, targetZ);
		currentTargetPoint = new Vec3d(targetPoint.xCoord, targetPoint.yCoord, targetPoint.zCoord);
		sourcePoint = new Vec3d(x, y, z);
		deviation = 1.0;
		speed = 0.01f;
		width = 0.05f;
		rl = new ResourceLocation("arsmagica2", IIconName);
		this.particleMaxAge = 100;
		forwardFactor = 0;
	}

	public AMLineArc(World world, double x, double y, double z, Entity targetEntity, String IIconName){
		this(world, x, y, z, targetEntity.posX, targetEntity.posY + targetEntity.getEyeHeight() - (targetEntity.height * 0.2f), targetEntity.posZ, IIconName);
		this.targetEntity = targetEntity;

		hadTarget = true;
	}

	public AMLineArc(World world, Entity sourceEntity, Entity targetEntity, String IIconName){
		this(world, sourceEntity.posX, sourceEntity.posY + sourceEntity.getEyeHeight() - (sourceEntity.height * 0.2f), sourceEntity.posZ, targetEntity.posX, targetEntity.posY + targetEntity.getEyeHeight() - (targetEntity.height * 0.2f), targetEntity.posZ, IIconName);
		this.targetEntity = targetEntity;
		this.sourceEntity = sourceEntity;

		hadSource = true;
		hadTarget = true;
	}

	public AMLineArc setExtendToTarget(){
		extendToTarget = true;
		return this;
	}

	public AMLineArc setIgnoreAge(boolean ignore){
		ignoreAge = ignore;
		return this;
	}

	@Override
	public void onUpdate(){

		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;

		if (this.particleAge >= this.particleMaxAge){
			this.setExpired();
			return;
		}

		if (targetEntity != null){
			if (ignoreAge)
				this.particleAge = 0;
			if (targetEntity.isDead){
				this.setExpired();
				return;
			}
			targetPoint = new Vec3d(targetEntity.posX, targetEntity.posY + targetEntity.getEyeHeight() - (targetEntity.height * 0.2f), targetEntity.posZ);
			currentTargetPoint = targetPoint.add(Vec3d.ZERO);
		}else if (hadTarget){
			this.setExpired();
			return;
		}

		if (sourceEntity != null){
			if (ignoreAge)
				this.particleAge = 0;
			if (sourceEntity.isDead){
				this.setExpired();
				return;
			}
			sourcePoint = new Vec3d(sourceEntity.posX, sourceEntity.posY + sourceEntity.getEyeHeight() - (sourceEntity.height * 0.2f), sourceEntity.posZ);
		}else if (hadSource){
			this.setExpired();
			return;
		}


		if (extendToTarget && extensionProgress < 1.0f){
			extensionProgress += 0.08;
			Vec3d delta = targetPoint.subtract(sourcePoint);
			delta.scale(extensionProgress);
			currentTargetPoint = delta.add(sourcePoint);
		}
		particleAge++;
	}

	@Override
	public void renderParticle(VertexBuffer par1Tessellator, Entity ent, float partialTicks, float rotationX, float rotationXZ, float rotationZ, float rotationYZ, float rotationXY){
		if (targetEntity != null && sourceEntity != null){
			drawArcingLine(
					sourceEntity.prevPosX + (sourceEntity.posX - sourceEntity.prevPosX) * partialTicks,
					(sourceEntity.prevPosY + (sourceEntity.posY - sourceEntity.prevPosY) * partialTicks) + (sourceEntity.getEyeHeight() - (sourceEntity.height * 0.2f)),
					sourceEntity.prevPosZ + (sourceEntity.posZ - sourceEntity.prevPosZ) * partialTicks,
					targetEntity.prevPosX + (targetEntity.posX - targetEntity.prevPosX) * partialTicks,
					(targetEntity.prevPosY + (targetEntity.posY - targetEntity.prevPosY) * partialTicks) + (targetEntity.getEyeHeight() - (targetEntity.height * 0.2f)),
					targetEntity.prevPosZ + (targetEntity.posZ - targetEntity.prevPosZ) * partialTicks,
					partialTicks, speed, deviation);
		}else if (targetEntity != null){
			drawArcingLine(
					prevPosX + (posX - prevPosX) * partialTicks,
					prevPosY + (posY - prevPosY) * partialTicks,
					prevPosZ + (posZ - prevPosZ) * partialTicks,
					targetEntity.prevPosX + (targetEntity.posX - targetEntity.prevPosX) * partialTicks,
					(targetEntity.prevPosY + (targetEntity.posY - targetEntity.prevPosY) * partialTicks) + (targetEntity.getEyeHeight() - (targetEntity.height * 0.2f)),
					targetEntity.prevPosZ + (targetEntity.posZ - targetEntity.prevPosZ) * partialTicks,
					partialTicks, speed, deviation);
		}else{
			drawArcingLine(prevPosX + (posX - prevPosX) * partialTicks, prevPosY + (posY - prevPosY) * partialTicks, prevPosZ + (posZ - prevPosZ) * partialTicks, currentTargetPoint.xCoord, currentTargetPoint.yCoord, currentTargetPoint.zCoord, partialTicks, speed, deviation);
		}
	}

	public void drawArcingLine(double srcX, double srcY, double srcZ, double dstX, double dstY, double dstZ, float partialTicks, float speed, double distance){

		GL11.glPushMatrix();

		Minecraft.getMinecraft().renderEngine.bindTexture(rl);

		int fxQuality = 8;//AMCore.config.getGFXLevel() * 8;

		EntityPlayer player = Minecraft.getMinecraft().player;
		double interpolatedX = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
		double interpolatedY = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
		double interpolatedZ = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;

		Tessellator tessellator = Tessellator.getInstance();

		double deltaX = srcX - dstX;
		double deltaY = srcY - dstY;
		double deltaZ = srcZ - dstZ;

		float time = System.nanoTime() / 10000000L;

		float dist = MathHelper.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
		float blocks = Math.round(dist);
		float length = blocks * (fxQuality / 2.0F);

//		float vMin = 0.0F;
//		float VMax = 1.0F;

		GL11.glTranslated(-interpolatedX + dstX, -interpolatedY + dstY, -interpolatedZ + dstZ);


		double wGain = (width * 3) / (length * distance);
		float curWidth = width * 3;
		tessellator.getBuffer().begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX);
		for (int i = 0; i <= length * distance; i++){
			float lengthFactor = i / length;
			float f3 = 1.0F - Math.abs(i - length / 2.0F) / (length / 2.0F);

			//ZXY
			double dx = deltaX + MathHelper.sin((float)((srcX % 16.0D + dist * (1.0F - lengthFactor) * fxQuality / 2.0F - time % 32767.0F / 5.0F) / 4.0D)) * 0.5F * f3;
			double dy = deltaY + MathHelper.sin((float)((srcY % 16.0D + dist * (1.0F - lengthFactor) * fxQuality / 2.0F - time % 32767.0F / 5.0F) / 3.0D)) * 0.5F * f3;
			double dz = deltaZ + MathHelper.sin((float)((srcZ % 16.0D + dist * (1.0F - lengthFactor) * fxQuality / 2.0F - time % 32767.0F / 5.0F) / 2.0D)) * 0.5F * f3;

			GL11.glColor4f(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha);

			float u = (1.0F - lengthFactor) * dist - time * speed;

			tessellator.getBuffer().pos(dx * lengthFactor - curWidth, dy * lengthFactor, dz * lengthFactor).tex( u, 1.0f).endVertex();
			tessellator.getBuffer().pos(dx * lengthFactor + curWidth, dy * lengthFactor, dz * lengthFactor).tex( u, 0.0f).endVertex();

			curWidth -= wGain;
		}
		tessellator.draw();
		forwardFactor = (forwardFactor + 0.01f) % 1.0f;
		GL11.glPopMatrix();
	}
}
