package am2.blocks.tileentity;

import am2.ArsMagica2;
import am2.api.math.AMVector3;
import am2.blocks.BlockParticleEmitter;
import am2.defs.ItemDefs;
import am2.packet.AMDataWriter;
import am2.packet.AMNetHandler;
import am2.packet.AMPacketIDs;
import am2.particles.AMParticle;
import am2.particles.AMParticleIcons;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityParticleEmitter extends TileEntity implements ITickable{

	private int particleType;
	private int particleQuantity;
	private int spawnRate;
	private int particleBehaviour;
	private int particleColor;
	private float particleScale;
	private float particleAlpha;
	private boolean defaultColor;
	private boolean randomColor;
	private boolean show;
	private float speed;

	private int spawnTicks = 0;
	private int showTicks = 0;
	boolean forceShow;

	public TileEntityParticleEmitter(){
		particleType = 0;
		particleQuantity = 1;
		spawnRate = 5;
		particleBehaviour = 0;
		particleColor = 0;
		particleScale = 0.5f;
		particleAlpha = 1.0f;
		defaultColor = true;
		randomColor = false;
		show = true;
		forceShow = false;
	}

	@Override
	public void update(){
		if (world.isRemote && spawnTicks++ >= spawnRate){
			for (int i = 0; i < particleQuantity; ++i)
				doSpawn();
			spawnTicks = 0;
		}
		if (!show && !world.isRemote && ((forceShow && showTicks++ > 100) || !forceShow)){
			showTicks = 0;
			forceShow = false;
			for (EntityPlayer player : world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(pos).expandXyz(8D))) {
				if (player != null && player.inventory.getCurrentItem() != null && player.inventory.getCurrentItem().getItem() == ItemDefs.crystalWrench){
					AMVector3 myLoc = new AMVector3(pos);
					AMVector3 playerLoc = new AMVector3(player);
					if (myLoc.distanceSqTo(playerLoc) < 64D){
						forceShow = true;
					}
				}
			}
			world.setBlockState(getPos(), world.getBlockState(pos).withProperty(BlockParticleEmitter.HIDDEN, !forceShow), 3);
		}
		world.markAndNotifyBlock(pos, world.getChunkFromBlockCoords(pos), world.getBlockState(pos), world.getBlockState(pos), 3);
	}

	private void doSpawn(){
		//if (!hasReceivedFullUpdate) return;
		double x = randomizeCoord(pos.getX() + 0.5);
		double y = randomizeCoord(pos.getY() + 0.5);
		double z = randomizeCoord(pos.getZ() + 0.5);
		AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, AMParticle.particleTypes[particleType], x, y, z);
		if (particle != null){
			particle.AddParticleController(ArsMagica2.proxy.particleManager.createDefaultParticleController(particleBehaviour, particle, new AMVector3(x, y, z).toVec3D(), speed, getBlockMetadata()));
			particle.setParticleAge(Math.min(Math.max(spawnRate, 10), 40));
			particle.setIgnoreMaxAge(false);
			particle.setParticleScale(particleScale);
			particle.SetParticleAlpha(particleAlpha);
			if (!defaultColor){
				if (!randomColor)
					particle.setRGBColorF(((particleColor >> 16) & 0xFF) / 255f, ((particleColor >> 8) & 0xFF) / 255f, (particleColor & 0xFF) / 255f);
				else
					particle.setRGBColorF(world.rand.nextFloat(), world.rand.nextFloat(), world.rand.nextFloat());
			}
		}
	}

	private double randomizeCoord(double base){
		return base + world.rand.nextDouble() - 0.5;
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket(){
		NBTTagCompound compound = new NBTTagCompound();
		this.writeToNBT(compound);
		SPacketUpdateTileEntity packet = new SPacketUpdateTileEntity(pos, getBlockMetadata(), compound);
		return packet;
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
		this.readFromNBT(pkt.getNbtCompound());
		applyParamConstraints();
	}

	private void applyParamConstraints(){
		if (spawnRate < 1) spawnRate = 1;
		if (particleQuantity < 1) particleQuantity = 1;
		if (particleQuantity > 5) particleQuantity = 5;
		if (particleType < 0) particleType = 0;
		if (particleType > AMParticleIcons.instance.numParticles())
			particleType = AMParticleIcons.instance.numParticles() - 1;
		if (particleBehaviour < 0) particleBehaviour = 0;
		if (particleBehaviour > 6) particleBehaviour = 6;
		if (particleScale < 0) particleScale = 0;
		if (particleScale > 1) particleScale = 1;
		if (particleAlpha < 0) particleAlpha = 0;
		if (particleAlpha > 1) particleAlpha = 1;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound){
		super.readFromNBT(compound);
		readSettingsFromNBT(compound);
	}

	public void readSettingsFromNBT(NBTTagCompound compound){
		particleType = compound.getInteger("particleType");
		particleQuantity = compound.getInteger("particleQuantity");
		spawnRate = compound.getInteger("spawnRate");
		particleBehaviour = compound.getInteger("particleBehaviour");
		particleColor = compound.getInteger("particleColor");
		particleScale = compound.getFloat("particleScale");
		particleAlpha = compound.getFloat("particleAlpha");
		defaultColor = compound.getBoolean("defaultColor");
		randomColor = compound.getBoolean("randomColor");
		show = compound.getBoolean("show");
		speed = compound.getFloat("speed");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound){
		super.writeToNBT(compound);
		writeSettingsToNBT(compound);
		return compound;
	}

	public void writeSettingsToNBT(NBTTagCompound compound){
		compound.setInteger("particleType", particleType);
		compound.setInteger("particleQuantity", particleQuantity);
		compound.setInteger("spawnRate", spawnRate);
		compound.setInteger("particleBehaviour", particleBehaviour);
		compound.setInteger("particleColor", particleColor);
		compound.setFloat("particleScale", particleScale);
		compound.setFloat("particleAlpha", particleAlpha);
		compound.setBoolean("defaultColor", defaultColor);
		compound.setBoolean("randomColor", randomColor);
		compound.setBoolean("show", show);
		compound.setFloat("speed", speed);
	}

	public void setParticleType(int particleType){
		this.particleType = particleType;
	}

	public void setParticleBehaviour(int particleBehaviour){
		this.particleBehaviour = particleBehaviour;
	}

	public void setColorDefault(boolean def){
		this.defaultColor = def;
	}

	public void setColorRandom(boolean rand){
		this.randomColor = rand;
	}

	public void setColor(int color){
		this.particleColor = color;
	}

	public void setScale(float scale){
		this.particleScale = scale;
	}

	public void setAlpha(float alpha){
		this.particleAlpha = alpha;
	}

	public void setShow(boolean show){
		this.show = show;
		if (world.isRemote && show){
			forceShow = false;
			showTicks = 0;
			EntityPlayer localPlayer = ArsMagica2.proxy.getLocalPlayer();
			if (localPlayer != null && localPlayer.inventory.getCurrentItem() != null && localPlayer.inventory.getCurrentItem().getItem() == ItemDefs.crystalWrench){
				AMVector3 myLoc = new AMVector3(pos);
				AMVector3 playerLoc = new AMVector3(localPlayer);
				if (myLoc.distanceSqTo(playerLoc) < 64D){
					forceShow = true;
				}
			}
		}

//		int oldMeta = getBlockMetadata();

		//world.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, show ? oldMeta & ~0x8 : oldMeta | 0x8, 2);
	}
	
	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
		return oldState.getBlock() != newSate.getBlock();
	}
	
	public int getParticleType(){
		return this.particleType;
	}

	public int getParticleBehaviour(){
		return this.particleBehaviour;
	}

	public boolean getColorDefault(){
		return this.defaultColor;
	}

	public boolean getColorRandom(){
		return this.randomColor;
	}

	public int getColor(){
		return this.particleColor;
	}

	public float getScale(){
		return this.particleScale;
	}

	public float getAlpha(){
		return this.particleAlpha;
	}

	public boolean getShow(){
		return this.show;
	}

	public void setQuantity(int quantity){
		this.particleQuantity = quantity;
	}

	public int getQuantity(){
		return this.particleQuantity;
	}

	public void setDelay(int delay){
		this.spawnRate = delay;
		this.spawnTicks = 0;
	}

	public int getDelay(){
		return this.spawnRate;
	}

	public void setSpeed(float speed){
		this.speed = speed;
	}

	public float getSpeed(){
		return speed;
	}

	public void syncWithServer(){
		if (this.world.isRemote){
			AMDataWriter writer = new AMDataWriter();
			writer.add(this.pos.getX());
			writer.add(this.pos.getY());
			writer.add(this.pos.getZ());
			NBTTagCompound compound = new NBTTagCompound();
			this.writeToNBT(compound);
			writer.add(compound);

			byte[] data = writer.generate();

			AMNetHandler.INSTANCE.sendPacketToServer(AMPacketIDs.DECO_BLOCK_UPDATE, data);
		}
	}
}
