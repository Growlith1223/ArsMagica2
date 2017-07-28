package am2.blocks.tileentity;

import java.util.ArrayList;
import java.util.Iterator;

import am2.ArsMagica2;
import am2.api.math.AMVector3;
import am2.packet.AMNetHandler;
import am2.particles.AMParticle;
import am2.particles.ParticleFloatUpward;
import am2.power.PowerNodeRegistry;
import am2.power.PowerTypes;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class TileEntitySlipstreamGenerator extends TileEntityAMPower{

	private ArrayList<EntityPlayer> levitatingEntities;
	private int updateTicks = 1;

	private static final int EFFECT_HEIGHT = 50;

	public TileEntitySlipstreamGenerator(){
		super(100);
		levitatingEntities = new ArrayList<EntityPlayer>();
	}

	@Override
	public boolean canProvidePower(PowerTypes type){
		return false;
	}

	@Override
	public void update(){
		super.update();
		updateTicks++;
		if (updateTicks > 10){
			refreshPlayerList();
			updateTicks = 0;
			if (world.isRemote && levitatingEntities.size() > 0)
				AMNetHandler.INSTANCE.sendPowerRequestToServer(new AMVector3(this).toVec3D());
		}

		if (levitatingEntities.isEmpty())
			return;

		Iterator<EntityPlayer> it = levitatingEntities.iterator();
		while (it.hasNext()){
			EntityPlayer player = it.next();
			if (!playerIsValid(player)){
				it.remove();
				continue;
			}

			if (PowerNodeRegistry.For(this.world).getHighestPower(this) >= 0.25f){

				player.motionY *= 0.5999999;
				if (Math.abs(player.motionY) < 0.2){
					player.addVelocity(0, -player.motionY, 0);
					player.fallDistance = 0f;
				}else{
					player.fallDistance--;
				}
				if (!player.isSneaking()){
                    float pitch = player.rotationPitch;
                    float factor = (pitch > 0 ? (pitch - 10) : (pitch + 10)) / -180.0f;
					if (Math.abs(pitch) > 10f){
						player.move(MoverType.PISTON,0, factor, 0);
					}
				}

				if (world.isRemote)
					spawnParticles(player);
				PowerNodeRegistry.For(this.world).consumePower(this, PowerNodeRegistry.For(this.world).getHighestPowerType(this), 0.25f);
			}
		}
	}

	private void spawnParticles(EntityPlayer player){
		AMParticle wind = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "wind", player.posX, player.posY - player.height, player.posZ);
		float pitch = player.rotationPitch;
		float factor = (pitch > 0 ? (pitch - 10) : (pitch + 10)) / -180.0f;
		if (player.isSneaking())
			factor = 0.01f;
		if (wind != null){
			wind.setMaxAge(10);
			wind.addRandomOffset(1, 1, 1);
			wind.setParticleScale(0.1f);
			wind.AddParticleController(new ParticleFloatUpward(wind, 0, Math.abs(factor) * 2, 1, false));
		}
	}

	private boolean playerIsValid(EntityPlayer player){
		if (player == null || player.isDead)
			return false;
		float tolerance = 0.2f;
		AxisAlignedBB bb = new AxisAlignedBB(pos.getX() - tolerance, pos.getY() + 1, pos.getZ() - tolerance, pos.getX() + 1 + tolerance, pos.getY() + 1 + EFFECT_HEIGHT, pos.getZ() + 1 + tolerance);
		Vec3d myLoc = new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
		Vec3d playerLoc = new Vec3d(player.posX, player.posY, player.posZ);
		return bb.intersectsWith(player.getEntityBoundingBox()) && world.rayTraceBlocks(myLoc, playerLoc, true) == null;
	}

	private void refreshPlayerList(){
		levitatingEntities.clear();

		for (int i = 0; i < world.playerEntities.size(); ++i){
			EntityPlayer player = (EntityPlayer)world.playerEntities.get(i);
			if (playerIsValid(player) && !levitatingEntities.contains(player))
				levitatingEntities.add(player);
		}
	}

	@Override
	public boolean canRequestPower(){
		return true;
	}

	@Override
	public int getChargeRate(){
		return 12;
	}

	@Override
	public boolean canRelayPower(PowerTypes type){
		return false;
	}
}
