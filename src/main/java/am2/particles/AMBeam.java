package am2.particles;

import org.lwjgl.opengl.GL11;

import am2.ArsMagica2;
import am2.api.particles.IBeamParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AMBeam extends Particle implements IBeamParticle{

	int type;
	double dX, dY, dZ;

	double updateX, updateY, updateZ;

	private float yaw, pitch;
	private float prevYaw, prevPitch;

	private float rotateSpeed = 5f;
//	private final double offset = 0d;

	private float length;
	private final float endMod = 1.0F;
	private int maxLengthAge = 10;

	private boolean positionChanged = false;

	private boolean fppc = false; //first person player cast

	public AMBeam(World world, double x, double y, double z, double destX, double destY, double destZ){
		this(world, x, y, z, destX, destY, destZ, 0);
	}

	public AMBeam(World world, double x, double y, double z, double destX, double destY, double destZ, int color){
		super(world, x, y, z);
		this.type = 0;
		this.dX = destX;
		this.dY = destY;
		this.dZ = destZ;

		this.motionX = 0D;
		this.motionY = 0D;
		this.motionZ = 0D;

		setSize(0.2f, 0.2f);

		calculateLengthAndRotation();
		this.prevYaw = this.yaw;
		this.prevPitch = this.pitch;

		rotateSpeed = 30;
		maxLengthAge = 10;

		this.particleMaxAge = 10;
	}

	/**
	 * Sets the beam to be instantly full size instead of playing the grow
	 * animation.
	 *
	 * @return
	 */
	public AMBeam setInstantSpawn(){
		this.maxLengthAge = 1;
		return this;
	}

	public void setMaxAge(int maxAge){
		this.particleMaxAge = maxAge;
	}

	private void calculateLengthAndRotation(){
		float deltaX = (float)(this.posX - this.dX);
		float deltaY = (float)(this.posY - this.dY);
		float deltaZ = (float)(this.posZ - this.dZ);

		this.length = MathHelper.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
		double hDist = MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ);
		this.yaw = ((float)(Math.atan2(deltaX, deltaZ) * 180.0D / 3.141592653589793D));
		this.pitch = ((float)(Math.atan2(deltaY, hDist) * 180.0D / 3.141592653589793D));

	}

	public void setBeamLocationAndTarget(double posX, double posY, double posZ, double targetX, double targetY, double targetZ){
		this.updateX = posX;
		this.updateY = posY;
		this.updateZ = posZ;
		this.dX = targetX;
		this.dY = targetY;
		this.dZ = targetZ;

		if (this.particleAge > this.particleMaxAge - 5){
			this.particleMaxAge = this.particleAge + 5;
		}

		positionChanged = true;
	}

	private void storePrevInformation(){
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if (this.positionChanged){
			this.posX = this.updateX;
			this.posY = this.updateY;
			this.posZ = this.updateZ;
			if (this.fppc){
				EntityPlayer player = Minecraft.getMinecraft().player;
				if (player != null){
					float yaw = player.rotationYaw;
					float rotationYaw = (float)(yaw * Math.PI / 180);
					float offsetX = (float)Math.cos(rotationYaw) * 0.06f;
					float offsetZ = (float)Math.sin(rotationYaw) * 0.06f;
					this.posX -= offsetX;
					this.posZ -= offsetZ;
					this.posY += 0.06f;
					this.prevPosX = this.posX;
					this.prevPosY = this.posY;
					this.prevPosZ = this.posZ;
				}
			}
			this.positionChanged = false;
		}

		this.prevYaw = this.yaw;
		this.prevPitch = this.pitch;
	}

	@Override
	public void setFirstPersonPlayerCast(){
		this.fppc = true;
	}

	private void handleAging(){
		this.particleAge++;
		if (this.particleAge >= this.particleMaxAge){
			this.setExpired();
		}
	}

	@Override
	public void setType(int type){
		this.type = type;
	}

	@Override
	public void onUpdate(){
		storePrevInformation();
		calculateLengthAndRotation();
		handleAging();
	}

	@Override
	public int getFXLayer(){
		return 2;
	}

	@Override
	public void renderParticle(VertexBuffer tessellator, Entity ent, float par2, float par3, float par4, float par5, float par6, float par7){
		GL11.glPushMatrix();
		//GlStateManager.disableBlend();
		//GlStateManager.disableAlpha();
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.003921569F);
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		float scaleFactor = 1.0F;
//		float slide = this.world.getTotalWorldTime();
		float rot = this.world.provider.getWorldTime() % (360 / this.rotateSpeed) * this.rotateSpeed + this.rotateSpeed * par2;

		float size = (float)this.particleAge / (float)this.maxLengthAge;
		if (size > 1) size = 1;

		float op = 0.4F;
		float widthMod = 1.0f;

		TextureAtlasSprite beamIcon = null;

		switch (this.type){
		default:
			beamIcon = AMParticleIcons.instance.getHiddenIconByName("beam");
			break;
		case 1:
			beamIcon = AMParticleIcons.instance.getHiddenIconByName("beam1");
			break;
		case 2:
			beamIcon = AMParticleIcons.instance.getHiddenIconByName("beam2");
		}

		GL11.glTexParameterf(3553, 10242, 10497.0F);
		GL11.glTexParameterf(3553, 10243, 10497.0F);
//		float var11 = slide + par2;
//		float var12 = -var11 * 0.2F - MathHelper.floor_float(-var11 * 0.1F);

		float xx = (float)(this.prevPosX + (this.posX - this.prevPosX) * par2 - interpPosX);
		float yy = (float)(this.prevPosY + (this.posY - this.prevPosY) * par2 - interpPosY);
		float zz = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * par2 - interpPosZ);
		GL11.glTranslated(xx, yy, zz);

		if (fppc){
			widthMod = 0.3f;
		}

		float deltaYaw = Math.abs(this.yaw) - Math.abs(this.prevYaw);

		float ry = this.prevYaw + (deltaYaw) * par2;
		float rp = this.prevPitch + (this.pitch - this.prevPitch) * par2;
		GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
		GL11.glRotatef(180.0F + ry, 0.0F, 0.0F, -1.0F);
		GL11.glRotatef(rp, 1.0F, 0.0F, 0.0F);

		double offset1 = (-0.15D * widthMod) * size;
		double offset2 = (0.15D * widthMod) * size;
		double offset3 = (-0.15D * widthMod) * size * this.endMod;
		double offset4 = (0.15D * widthMod) * size * this.endMod;

		GL11.glRotatef(rot, 0.0F, 1.0F, 0.0F);
		int i = 5;
		float inc = 36.0F;
		if (ArsMagica2.config.LowGFX()){
			i = 3;
			inc = 90;
		}else if (ArsMagica2.config.NoGFX()){
			i = 1;
			inc = 180;
		}
		
		for (int t = 0; t < i; t++){
			Tessellator.getInstance().draw();
			tessellator.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
			double l = this.length * size * scaleFactor;
			double tl = beamIcon.getMinU();
			double br = beamIcon.getMaxU();
			double mU = beamIcon.getMaxV(); //-1.0F + var12 + t / 3.0F;
			double mV = beamIcon.getMinV(); //this.length * size * scaleFactor + mU;

			GL11.glRotatef(inc, 0.0F, 1.0F, 0.0F);
			GlStateManager.resetColor();
//			if (t % 2 == 0){
//				GL11.glColor4f(this.particleRed, this.particleGreen, this.particleBlue, op);
//			}else{
//				GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.4f);
//			}
	        int b = this.getBrightnessForRender(par7);
	        int j = b >> 16 & 65535;
	        int k = b & 65535;
			tessellator.pos(offset3, l, 0.0D).tex( br, mV).color(this.particleRed, this.particleGreen, this.particleBlue, op).lightmap(j, k).endVertex();
			tessellator.pos(offset1, 0.0D, 0.0D).tex( br, mU).color(this.particleRed, this.particleGreen, this.particleBlue, op).lightmap(j, k).endVertex();
			tessellator.pos(offset2, 0.0D, 0.0D).tex( tl, mU).color(this.particleRed, this.particleGreen, this.particleBlue, op).lightmap(j, k).endVertex();
			tessellator.pos(offset4, l, 0.0D).tex( tl, mV).color(this.particleRed, this.particleGreen, this.particleBlue, op).lightmap(j, k).endVertex();
			Tessellator.getInstance().draw();
			tessellator.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
		}
		GL11.glPopMatrix();
	}

	@Override
	public void setRGBColorF(float r, float g, float b){
		this.particleGreen = g;
		this.particleRed = r;
		this.particleBlue = b;
	}

	@Override
	public void setRGBColor(int color){
		this.particleRed = ((color >> 16) & 0xFF) / 255.0F;
		this.particleGreen = ((color >> 8) & 0xFF) / 255.0F;
		this.particleBlue = (color & 0xFF) / 255.0F;
	}

	@Override
	public void setRGBColorI(int r, int g, int b){
		this.particleRed = r / 255.0f;
		this.particleGreen = g / 255.0f;
		this.particleBlue = b / 255.0f;
	}
	
	public double getPosX() {
		return posX;
	}
	
	public double getPosY() {
		return posY;
	}
	
	public double getPosZ() {
		return posZ;
	}
}
