package am2.particles;

import java.util.ArrayList;
import java.util.Iterator;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ParticleRenderer{

	public static String name = "am2-particle";
	private final ArrayList<Particle> particles;
	private final ArrayList<Particle> blocks;
	private final ArrayList<Particle> radiants;
	private final ArrayList<AMLineArc> arcs;

	private final ArrayList<Particle> deferredParticles;
	private final ArrayList<Particle> deferredBlocks;
	private final ArrayList<Particle> deferredRadiants;
	private final ArrayList<AMLineArc> deferredArcs;

	public ParticleRenderer(){
		MinecraftForge.EVENT_BUS.register(this);
		particles = new ArrayList<Particle>();
		radiants = new ArrayList<Particle>();
		arcs = new ArrayList<AMLineArc>();
		blocks = new ArrayList<Particle>();

		deferredParticles = new ArrayList<Particle>();
		deferredRadiants = new ArrayList<Particle>();
		deferredArcs = new ArrayList<AMLineArc>();
		deferredBlocks = new ArrayList<Particle>();
	}

	public void addEffect(Particle effect){
		if (effect instanceof AMParticle){
			addAMParticle((AMParticle)effect);
		}else if (effect instanceof AMLineArc){
			addArcEffect((AMLineArc)effect);
		}else{
			deferredParticles.add(effect);
		}
	}

	public void addAMParticle(AMParticle particle){
		if (particle.isRadiant())
			deferredRadiants.add(particle);
		else if (particle.isBlockTexture())
			deferredBlocks.add(particle);
		else
			deferredParticles.add(particle);
	}

	public void addArcEffect(AMLineArc arc){
		deferredArcs.add(arc);
	}

	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event){
		render(event.getPartialTicks());
	}

	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event){
		particles.clear();
		radiants.clear();
		arcs.clear();
		blocks.clear();
		deferredParticles.clear();
		deferredRadiants.clear();
		deferredArcs.clear();
		deferredBlocks.clear();
	}

	@SubscribeEvent
	public void onTickEnd(TickEvent.ClientTickEvent event){
		if (event.phase == TickEvent.Phase.END){
			updateParticles();
		}
	}

	private void updateParticles(){
		Minecraft.getMinecraft().mcProfiler.startSection(name + "-update");

		particles.addAll(deferredParticles);
		deferredParticles.clear();

		radiants.addAll(deferredRadiants);
		deferredRadiants.clear();

		arcs.addAll(deferredArcs);
		deferredArcs.clear();

		blocks.addAll(deferredBlocks);
		deferredBlocks.clear();

		for (Iterator<Particle> it = particles.iterator(); it.hasNext(); ){
			Particle particle = it.next();

			particle.onUpdate();

			if (!particle.isAlive()){
				it.remove();
			}
		}

		for (Iterator<Particle> it = radiants.iterator(); it.hasNext(); ){
			Particle particle = it.next();

			particle.onUpdate();

			if (!particle.isAlive()){
				it.remove();
			}
		}

		for (Iterator<AMLineArc> it = arcs.iterator(); it.hasNext(); ){
			AMLineArc particle = it.next();

			particle.onUpdate();

			if (!particle.isAlive()){
				it.remove();
			}
		}

		for (Iterator<Particle> it = blocks.iterator(); it.hasNext(); ){
			Particle particle = it.next();

			particle.onUpdate();

			if (!particle.isAlive()){
				it.remove();
			}
		}

		Minecraft.getMinecraft().mcProfiler.endSection();
	}

	private void render(float partialTicks){
		GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		Minecraft.getMinecraft().mcProfiler.startSection(name + "-render");

		Entity renderer = Minecraft.getMinecraft().getRenderViewEntity();
		Particle.interpPosX = renderer.lastTickPosX + (renderer.posX - renderer.lastTickPosX) * partialTicks;
		Particle.interpPosY = renderer.lastTickPosY + (renderer.posY - renderer.lastTickPosY) * partialTicks;
		Particle.interpPosZ = renderer.lastTickPosZ + (renderer.posZ - renderer.lastTickPosZ) * partialTicks;

		// bind the texture
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

		//============================================================================================
		// Standard Particles Using the Block Atlas
		//============================================================================================
		renderBlockParticles(partialTicks);

		// bind the texture
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

		//============================================================================================
		// Standard Particles
		//============================================================================================
		renderStandardParticles(partialTicks);
		//============================================================================================
		// Radiant Particles
		//============================================================================================
		renderRadiants(partialTicks);
		//============================================================================================
		// Arc Particles
		//============================================================================================
		renderArcs(partialTicks);
		//============================================================================================
		// End
		//============================================================================================
		Minecraft.getMinecraft().mcProfiler.endSection();
		GL11.glPopAttrib();
	}

	private void renderStandardParticles(float partialTicks){
		float rotationX = ActiveRenderInfo.getRotationX();
		float rotationZ = ActiveRenderInfo.getRotationZ();
		float rotationYZ = ActiveRenderInfo.getRotationYZ();
		float rotationXY = ActiveRenderInfo.getRotationXY();
		float rotationXZ = ActiveRenderInfo.getRotationXZ();

		// save the old gl state
		GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		// gl states/settings for drawing
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDepthMask(false);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.003921569F);
		GL11.glDisable(32826 /*GL_RESCALE_NORMAL_EXT*/);

		//GL11.glRotatef(180F - RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
		//GL11.glRotatef(-RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);

		Tessellator tessellator = Tessellator.getInstance();
		tessellator.getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);

		for (Particle particle : particles){
			//tessellator.setBrightness(particle.getBrightnessForRender(partialTicks));
			//LogHelper.info("Rendering");
			particle.renderParticle(tessellator.getBuffer(), Minecraft.getMinecraft().getRenderViewEntity(), partialTicks, rotationX, rotationXZ, rotationZ, rotationYZ, rotationXY);
		}

		tessellator.draw();

		// restore previous gl state
		GL11.glPopAttrib();
	}

	private void renderBlockParticles(float partialTicks){
		float rotationX = ActiveRenderInfo.getRotationX();
		float rotationZ = ActiveRenderInfo.getRotationZ();
		float rotationYZ = ActiveRenderInfo.getRotationYZ();
		float rotationXY = ActiveRenderInfo.getRotationXY();
		float rotationXZ = ActiveRenderInfo.getRotationXZ();

		// save the old gl state
		GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		// gl states/settings for drawing
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDepthMask(false);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.003921569F);
		GL11.glDisable(32826 /*GL_RESCALE_NORMAL_EXT*/);

		//GL11.glRotatef(180F - RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
		//GL11.glRotatef(-RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);

		Tessellator tessellator = Tessellator.getInstance();
		//tessellator.getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX);

		for (Particle particle : blocks){
			//tessellator.setBrightness(particle.getBrightnessForRender(partialTicks));

			particle.renderParticle(tessellator.getBuffer(), Minecraft.getMinecraft().getRenderViewEntity(), partialTicks, rotationX, rotationXZ, rotationZ, rotationYZ, rotationXY);
		}

		//tessellator.draw();

		// restore previous gl state
		GL11.glPopAttrib();
	}

	private void renderRadiants(float partialTicks){
		float rotationX = ActiveRenderInfo.getRotationX();
		float rotationZ = ActiveRenderInfo.getRotationZ();
		float rotationYZ = ActiveRenderInfo.getRotationYZ();
		float rotationXY = ActiveRenderInfo.getRotationXY();
		float rotationXZ = ActiveRenderInfo.getRotationXZ();

		// save the old gl state
		GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		// gl states/settings for drawing
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDepthMask(false);
		Tessellator tessellator = Tessellator.getInstance();

		for (Particle particle : radiants){
			//tessellator.setBrightness(particle.getBrightnessForRender(partialTicks));

			particle.renderParticle(tessellator.getBuffer(), Minecraft.getMinecraft().getRenderViewEntity(), partialTicks, rotationX, rotationXZ, rotationZ, rotationYZ, rotationXY);
		}

		// restore previous gl state
		GL11.glPopAttrib();

		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
	}

	private void renderArcs(float partialTicks){
		float rotationX = ActiveRenderInfo.getRotationX();
		float rotationZ = ActiveRenderInfo.getRotationZ();
		float rotationYZ = ActiveRenderInfo.getRotationYZ();
		float rotationXY = ActiveRenderInfo.getRotationXY();
		float rotationXZ = ActiveRenderInfo.getRotationXZ();

		Tessellator tessellator = Tessellator.getInstance();
		//tessellator.getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX);

		GL11.glDepthMask(false);
		GL11.glEnable(3042);
		GL11.glBlendFunc(770, 1);
		GL11.glDisable(2884);//2884

		for (AMLineArc particle : arcs){
//			tessellator.setBrightness(15728864);

			particle.renderParticle(tessellator.getBuffer(), Minecraft.getMinecraft().getRenderViewEntity(), partialTicks, rotationX, rotationXZ, rotationZ, rotationYZ, rotationXY);
		}

		GL11.glEnable(2884);
		GL11.glDisable(3042);
		GL11.glDepthMask(true);
	}
}
