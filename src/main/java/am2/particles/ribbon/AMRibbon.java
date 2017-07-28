package am2.particles.ribbon;

import java.util.LinkedList;
import java.util.Vector;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AMRibbon extends Particle{

	int NUMCURVES = 5;    //number of ribbonCurves per ribbon
	int CURVERESOLUTION = 25; //lower -> faster
	float RIBBONWIDTH = 2.25f;
	float NOISESTEP = 0.005f;
	float MAXSEPARATION = 2f;

	PerlinNoise noise;

	float ribbonSeparation, noisePosn;
	Vector<Vec3d> pts;
	LinkedList<RibbonCurve> curves;
	float ribbonColor;
	float ribbonWidth;
	RibbonCurve currentCurve; //current RibbonCurve
	int stepId;

	int movement = 2;

	Vec3d ribbonTarget;

	public AMRibbon(World world, float pcolor, float width, double x, double y, double z){
		super(world, x, y, z, 0.0, 0.0, 0.0);
		this.setPosition(x, y, z);
		motionX = 0;
		motionY = 0;
		motionZ = 0;
		//this.setSize(5, 5);
		noise = new PerlinNoise();
		curves = new LinkedList<>();
		pts = new Vector<>();

		ribbonColor = pcolor;
		ribbonWidth = width;
		stepId = 0;

		ribbonTarget = new Vec3d(random(-movement, movement), random(-movement, movement), random(-movement, movement));
		ribbonSeparation = lerp(-MAXSEPARATION, MAXSEPARATION, noise.noise1(noisePosn += NOISESTEP));

		pts.addElement(getRandPt());
		pts.addElement(getRandPt());
		pts.addElement(getRandPt());

		this.particleAge = 0;
		this.particleMaxAge = 200;

		addRibbonCurve();
	}
	
	
	@Override
	public void renderParticle(VertexBuffer par1Tessellator, Entity ent, float partialframe, float cosyaw, float cospitch, float sinyaw, float sinsinpitch, float cossinpitch){


		GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

		GL11.glPushMatrix();
		GL11.glEnable(32826 /*GL_RESCALE_NORMAL_EXT*/);

		double d = (this.prevPosX + (this.posX - this.prevPosX) * partialframe) - Minecraft.getMinecraft().getRenderManager().viewerPosX;
		double d1 = (this.prevPosY + (this.posY - this.prevPosY) * partialframe) - Minecraft.getMinecraft().getRenderManager().viewerPosY;
		double d2 = (this.prevPosZ + (this.posZ - this.prevPosZ) * partialframe) - Minecraft.getMinecraft().getRenderManager().viewerPosZ;

		GL11.glTranslatef((float)d, (float)d1, (float)d2);
		GL11.glColor4f(1, 1, 1, 1f);
		//GL11.glScalef(0.1f, 0.1f, 0.1f);

		draw();
		GL11.glPopMatrix();
		GL11.glPopAttrib();

	}

	@Override
	public int getFXLayer(){
		return 2;
	}

	void draw(){
		ribbonTarget = new Vec3d(random(-movement, movement), random(-movement, movement), random(-movement, movement));
		ribbonSeparation = lerp(-MAXSEPARATION, MAXSEPARATION, noise.noise1(noisePosn += NOISESTEP));
		currentCurve.addSegment();
		int size = curves.size();
		if (size > NUMCURVES - 1){
			RibbonCurve c = (RibbonCurve)curves.get(0);
			c.removeSegment();
		}
		stepId++;

		if (stepId > CURVERESOLUTION) addRibbonCurve();

		//draw curves
		for (int i = 0; i < size; i++){
			RibbonCurve c = (RibbonCurve)curves.get(i);
			c.draw();
		}
	}

	private float random(float min, float max){
		return (float)((Math.random() * max) - min);
	}

	private float lerp(float start, float stop, float amount){
		return start + ((stop - start) * amount);
	}

	void addRibbonCurve(){
		//add new point
		pts.addElement(getRandPt());

		Vec3d nextPt = (Vec3d)pts.elementAt(pts.size() - 1);
		Vec3d curPt = (Vec3d)pts.elementAt(pts.size() - 2);
		Vec3d lastPt = (Vec3d)pts.elementAt(pts.size() - 3);

		Vec3d lastMidPt = new Vec3d((curPt.xCoord + lastPt.xCoord) / 2,
				(curPt.yCoord + lastPt.yCoord) / 2,
				(curPt.zCoord + lastPt.zCoord) / 2);

		Vec3d midPt = new Vec3d((curPt.xCoord + nextPt.xCoord) / 2,
				(curPt.yCoord + nextPt.yCoord) / 2,
				(curPt.zCoord + nextPt.zCoord) / 2);

		/*float width = 0.00003F * (getRelativeViewVector(midPt).length()) * 1.5f;
		if (width > 0.2f)
			width = 0.2f;
		if (width < 0.1f)
			width = 0.1f;*/

		float width = 0.2f;

		currentCurve = new RibbonCurve(lastMidPt, midPt, curPt, width, CURVERESOLUTION, ribbonColor);
		curves.add(currentCurve);

		//remove old curves
		if (curves.size() > NUMCURVES){
			curves.removeFirst();
		}

		stepId = 0;

	}

//	private static Vec3d getRelativeViewVector(Vec3d pos){
//		EntityPlayer renderentity = Minecraft.getMinecraft().player;
//		return new Vec3d((float)renderentity.posX - pos.xCoord, (float)renderentity.posY - pos.yCoord, (float)renderentity.posZ - pos.zCoord);
//	}

	Vec3d getRandPt(){
		return new Vec3d(ribbonTarget.xCoord + random(-ribbonSeparation, ribbonSeparation),
				ribbonTarget.yCoord + random(-ribbonSeparation, ribbonSeparation),
				ribbonTarget.zCoord + random(-ribbonSeparation, ribbonSeparation));
	}

	@Override
	public void onUpdate(){
		super.onUpdate();
	}
}