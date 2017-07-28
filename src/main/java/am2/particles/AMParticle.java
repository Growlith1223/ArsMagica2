package am2.particles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AMParticle extends Particle {

	private boolean ignoreMaxAge;
	private final List<ParticleController> controllers;
	private final ControllerComparator comparer;

	private float particleRed;
	private float particleGreen;
	private float particleBlue;
	private float particleAlpha;

	private float particleScaleX = 0.2f;
	private float particleScaleY = 0.2f;
	private float particleScaleZ = 0.2f;

//	private int particleFrameCount;
//	private final int maxFrames;
//	private float uStep;
//	private float vStep;
//
//	private String particleName;
//
//	private boolean doRender;
	private boolean isRadiant = false;
	private boolean isBreak = false;
	private boolean isAffectedByGravity = false;
	private boolean ignoreNoControllers = false;
	private boolean doVelocityUpdates = true;

	public static String[] particleTypes;

	public void setParticleAge(int age){
		this.particleAge = age;
	}

	public AMParticle(World par1World, double par2, double par4, double par6){
		super(par1World, 0, 0, 0, 0, 0, 0);
		motionX = 0;
		motionY = 0;
		motionZ = 0;
		this.setPosition(par2, par4, par6);
		par2 += (rand.nextFloat() - rand.nextFloat()) * 0.05F;
		par4 += (rand.nextFloat() - rand.nextFloat()) * 0.05F;
		par6 += (rand.nextFloat() - rand.nextFloat()) * 0.05F;
		particleRed = particleGreen = particleBlue = particleAlpha = 1.0F;
		this.ignoreMaxAge = false;
		particleMaxAge = 20 + rand.nextInt(20);
		controllers = new ArrayList<ParticleController>();
		comparer = new ControllerComparator();
//		doRender = true;
//		maxFrames = 1;

		this.particleGravity = 1;

		this.setRandomScale(0.1f, 0.3f);
	}

//	public void setDoRender(boolean doRender){
//		this.doRender = doRender;
//	}

	public AMParticle setAffectedByGravity(){
		this.isAffectedByGravity = true;
		return this;
	}

	public AMParticle setDontRequireControllers(){
		this.ignoreNoControllers = true;
		return this;
	}

	public void setNoVelocityUpdates(){
		this.doVelocityUpdates = false;
	}

	public boolean isRadiant(){
		return isRadiant;
	}

	public boolean isBlockTexture(){
		return isBreak;
	}

	public void addRandomOffset(double maxX, double maxY, double maxZ){
		double newX = this.posX + (rand.nextDouble() * maxX) - (maxX / 2);
		double newY = this.posY + (rand.nextDouble() * maxY) - (maxY / 2);
		double newZ = this.posZ + (rand.nextDouble() * maxZ) - (maxZ / 2);

		this.setPosition(newX, newY, newZ);
	}

	public float getParticleScaleX(){
		return this.particleScaleX;
	}

	public float getParticleScaleY(){
		return this.particleScaleY;
	}

	public float getParticleScaleZ(){
		return this.particleScaleZ;
	}

	public AMParticle setRandomScale(float min, float max){
		this.setParticleScale((rand.nextFloat() * (max - min)) + min);
		return this;
	}

	public void setParticleScale(float scale){
		this.particleScaleX = scale;
		this.particleScaleY = scale;
		this.particleScaleZ = scale;
	}

	public void setParticleScale(float scaleX, float scaleY, float scaleZ){
		this.particleScaleX = scaleX;
		this.particleScaleY = scaleY;
		this.particleScaleZ = scaleZ;
	}

	public int GetParticleAge(){
		return this.particleAge;
	}

	public int GetParticleMaxAge(){
		return this.particleMaxAge;
	}

	public void setMaxAge(int age){
		this.particleMaxAge = age;
	}

	public void setIgnoreMaxAge(boolean ignore){
		this.ignoreMaxAge = ignore;
		this.particleAge = 0;
	}

	public void setRGBColorF(float r, float g, float b){
		this.particleRed = r;
		this.particleGreen = g;
		this.particleBlue = b;
	}

	public void setRGBColorI(int color){
		this.particleRed = ((color >> 16) & 0xFF) / 255.0f;
		this.particleGreen = ((color >> 8) & 0xFF) / 255.0f;
		this.particleBlue = (color & 0xFF) / 255.0f;
	}

	public void SetParticleAlpha(float alpha){
		this.particleAlpha = alpha;
	}

	public float GetParticleRed(){
		return this.particleRed;
	}

	public float GetParticleGreen(){
		return this.particleGreen;
	}

	public float GetParticleBlue(){
		return this.particleBlue;
	}

	public float GetParticleAlpha(){
		return this.particleAlpha;
	}

	public void AddParticleController(ParticleController controller){
		controllers.add(controller);
		Collections.sort(controllers, comparer);
	}

	public void RemoveParticleController(ParticleController controller){
		this.controllers.remove(controller);
	}

	public void ClearParticleControllers(){
		this.controllers.clear();
	}

	@Override
	public int getBrightnessForRender(float par1){
		float f = (particleAge + par1) / particleMaxAge;

		if (f < 0.0F){
			f = 0.0F;
		}

		if (f > 1.0F){
			f = 1.0F;
		}

		int i = super.getBrightnessForRender(par1);
		int j = i & 0xff;
		int k = i >> 16 & 0xff;
		j += (int)(f * 15F * 16F);

		if (j > 240){
			j = 240;
		}

		return j | k << 16;
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void onUpdate(){
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if (isAffectedByGravity)
			this.motionY -= 0.04D * this.particleGravity;
		if (doVelocityUpdates)
			this.move(this.motionX, this.motionY, this.motionZ);

		List<ParticleController> remove = new ArrayList<ParticleController>();

		for (ParticleController pmc : controllers){
			if (pmc.getFinished()){
				remove.add(pmc);
				continue;
			}
			pmc.onUpdate(this.world);
			if (pmc.getExclusive()){
				break;
			}
		}

		for (ParticleController pmc : remove){
			controllers.remove(pmc);
		}

		if ((particleAge++ > particleMaxAge && !this.ignoreMaxAge) || (!ignoreNoControllers && controllers.size() == 0)){
			this.setExpired();
		}
	}

	public class ControllerComparator implements Comparator<ParticleController>{
		@Override
		public int compare(ParticleController o1, ParticleController o2){
			return (o1.getPriority() > o2.getPriority() ? 1 : (o1 == o2 ? 0 : -1));
		}
	}

	@Override
	public int getFXLayer(){
		return 2;
	}
	
	@Override
	public void renderParticle(VertexBuffer tessellator, Entity ent, float partialframe, float cosyaw, float cospitch, float sinyaw, float sinsinpitch, float cossinpitch){
		if (!this.world.isRemote){
			return;
		}
		float f11 = (float)(this.prevPosX + (this.posX - this.prevPosX) * partialframe - interpPosX);
		float f12 = (float)(this.prevPosY + (this.posY - this.prevPosY) * partialframe - interpPosY);
		float f13 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * partialframe - interpPosZ);

		if (this.isRadiant){
			renderRadiant(Tessellator.getInstance(), partialframe);
		}else{
			if (this.particleTexture == null){ //|| this.renderManager.renderEngine == null){
				return;
			}
//			tessellator.setBrightness(0x0F0000F0);
//			tessellator.setColorRGBA_F(this.GetParticleRed(), this.GetParticleGreen(), this.GetParticleBlue(), this.GetParticleAlpha());

			float scaleFactorX = this.getParticleScaleX();
			float scaleFactorY = this.getParticleScaleY();
			float scaleFactorZ = this.getParticleScaleZ();

			float min_u = this.particleTexture.getMinU();
			float min_v = this.particleTexture.getMinV();
			float max_u = this.particleTexture.getMaxU();
			float max_v = this.particleTexture.getMaxV();

			tessellator.pos(f11 - cosyaw * scaleFactorX - sinsinpitch * scaleFactorX, f12 - cospitch * scaleFactorY, f13 - sinyaw * scaleFactorZ - cossinpitch * scaleFactorZ).tex( max_u, max_v).color(this.GetParticleRed(), this.GetParticleGreen(), this.GetParticleBlue(), this.GetParticleAlpha()).endVertex();
			tessellator.pos(f11 - cosyaw * scaleFactorX + sinsinpitch * scaleFactorX, f12 + cospitch * scaleFactorY, f13 - sinyaw * scaleFactorZ + cossinpitch * scaleFactorZ).tex( max_u, min_v).color(this.GetParticleRed(), this.GetParticleGreen(), this.GetParticleBlue(), this.GetParticleAlpha()).endVertex();
			tessellator.pos(f11 + cosyaw * scaleFactorX + sinsinpitch * scaleFactorX, f12 + cospitch * scaleFactorY, f13 + sinyaw * scaleFactorZ + cossinpitch * scaleFactorZ).tex( min_u, min_v).color(this.GetParticleRed(), this.GetParticleGreen(), this.GetParticleBlue(), this.GetParticleAlpha()).endVertex();
			tessellator.pos(f11 + cosyaw * scaleFactorX - sinsinpitch * scaleFactorX, f12 - cospitch * scaleFactorY, f13 + sinyaw * scaleFactorZ - cossinpitch * scaleFactorZ).tex( min_u, max_v).color(this.GetParticleRed(), this.GetParticleGreen(), this.GetParticleBlue(), this.GetParticleAlpha()).endVertex();
		}
	}

	private void renderRadiant(Tessellator tessellator, float partialFrame){
		RenderHelper.disableStandardItemLighting();
		float var4 = (this.GetParticleAge() + partialFrame) / this.GetParticleMaxAge();
		float var5 = 0.0F;

		if (var4 > 0.8F){
			var5 = (var4 - 0.8F) / 0.2F;
		}

		Random var6 = new Random(432L);
		float f11 = (float)(this.prevPosX + (this.posX - this.prevPosX) * partialFrame - Minecraft.getMinecraft().getRenderManager().viewerPosX);
		float f12 = (float)(this.prevPosY + (this.posY - this.prevPosY) * partialFrame - Minecraft.getMinecraft().getRenderManager().viewerPosY);
		float f13 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * partialFrame - Minecraft.getMinecraft().getRenderManager().viewerPosZ);

		GlStateManager.pushMatrix();
		GlStateManager.translate(f11, f12, f13);
		GlStateManager.scale(getParticleScaleX(), getParticleScaleY(), getParticleScaleZ());

		for (int var7 = 0; var7 < 50.0F; ++var7){
			GlStateManager.rotate(var6.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(var6.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(var6.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotate(var6.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(var6.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(var6.nextFloat() * 360.0F + var4 * 90.0F, 0.0F, 0.0F, 1.0F);
			tessellator.getBuffer().begin(6, DefaultVertexFormats.POSITION_COLOR);
//			int i = 0xF00F0;
//	        int j = i >> 16 & 65535;
//	        int k = i & 65535;
			float var8 = var6.nextFloat() * 2.0F + 2.0F + var5 * 0.5F;
			float var9 = var6.nextFloat() * 2.0F + 1.0F + var5 * 2.0F;
			tessellator.getBuffer().pos(0.0D, 0.0D, 0.0D).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).endVertex();
			tessellator.getBuffer().pos(-0.866D * var9, var8, -0.5F * var9).color(this.particleRed, this.particleGreen, this.particleBlue, 0).endVertex();
			tessellator.getBuffer().pos(0.866D * var9, var8, -0.5F * var9).color(this.particleRed, this.particleGreen, this.particleBlue, 0).endVertex();
			tessellator.getBuffer().pos(0.0D, var8, 1.0F * var9).color(this.particleRed, this.particleGreen, this.particleBlue, 0).endVertex();
			tessellator.getBuffer().pos(-0.866D * var9, var8, -0.5F * var9).color(this.particleRed, this.particleGreen, this.particleBlue, 0).endVertex();
			tessellator.draw();
		}

		GlStateManager.popMatrix();
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
	
	public double getPrevPosX() {
		return prevPosX;
	}
	
	public double getPrevPosY() {
		return prevPosY;
	}
	
	public double getPrevPosZ() {
		return prevPosZ;
	}
	
	public void setPrevPos(double prevPosX, double prevPosY, double prevPosZ) {
		this.prevPosX = prevPosX;
		this.prevPosY = prevPosY;
		this.prevPosZ = prevPosZ;
	}
	
	public void pushPos() {
		setPrevPos(posX, posY, posZ);
	}
	
	public World getworld() {
		return world;
	}

	public void SetParticleTextureByName(String name) {
		if (name.equalsIgnoreCase("radiant")) {
			this.isRadiant = true;
		}
		this.particleTexture = AMParticleIcons.instance.getIconByName(name);
	}

	public void setPosition(Entity entity) {
		setPosition(entity.posX, entity.posY, entity.posZ);
	}

	/*public boolean isCollided() {
		return isCollided;
	}*/

	public void addVelocity(double d, double e, double f) {
		this.motionX = d;
		this.motionY = e;
		this.motionZ = f;
	}
	
}
