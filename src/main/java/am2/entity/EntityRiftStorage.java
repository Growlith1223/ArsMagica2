package am2.entity;

import am2.ArsMagica2;
import am2.defs.IDDefs;
import am2.extensions.RiftStorage;
import am2.utils.NBTUtils;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityRiftStorage extends EntityLiving {
	
	private static final DataParameter<Integer> STORAGE_LEVEL = EntityDataManager.createKey(EntityRiftStorage.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> LIVE_TICKS = EntityDataManager.createKey(EntityRiftStorage.class, DataSerializers.VARINT);
	
	private float rotation = 0f;
	private float scale = 0.0f;
	private float scale2 = 1.0f;
	
	public EntityRiftStorage(World worldIn) {
		super(worldIn);
		setSize(1.5F, 1.5F);
		//LogHelper.info(worldIn.isRemote);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(STORAGE_LEVEL, 3);
		this.dataManager.register(LIVE_TICKS, 1200);
	}
	
	@Override
	public void onUpdate() {
		if (this.ticksExisted++ >= getTicksToLive()) this.setDead();
		this.rotation += (Math.sin((float)this.ticksExisted / 100) + 0.5f);

		if (getTicksToLive() - this.ticksExisted <= 20){
			this.scale -= 1f / 20f;
		}else if (this.scale < 0.99f){
			this.scale = (float)(Math.sin((float)this.ticksExisted / 50));
		}
		//LogHelper.info(world.isRemote);

		this.motionX = 0;
		this.motionY = 0;
		this.motionZ = 0;
		super.onUpdate();
	}
	
	@Override
	protected boolean processInteract(EntityPlayer player, EnumHand hand) {
		if (player.isSneaking()){
			this.setTicksToLive(this.ticksExisted + 20);
			return super.processInteract(player, hand);
		}
		RiftStorage.For(player).setAccessLevel(getStorageLevel());
		player.openGui(ArsMagica2.instance, IDDefs.GUI_RIFT, world, (int)posX, (int)posY, (int)posZ);
		return super.processInteract(player, hand);
	}
	
	@Override
	public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand) {
		return super.applyPlayerInteraction(player, vec, hand);
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		this.setStorageLevel(NBTUtils.getAM2Tag(compound).getInteger("StorageLevel"));
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		NBTUtils.getAM2Tag(compound).setInteger("StorageLevel", getStorageLevel());
	}
	
	
	private int getTicksToLive(){
		return dataManager.get(LIVE_TICKS);
	}

	private void setTicksToLive(int ticks){
		dataManager.set(LIVE_TICKS, ticks);
	}
	
	public int getStorageLevel() {
		return dataManager.get(STORAGE_LEVEL);
	}
	
	public void setStorageLevel(int storageLevel) {
		dataManager.set(STORAGE_LEVEL, storageLevel);
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox() {
		return null;
	}

	public float getRotation() {
		return rotation;
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		return false;
	}
	
	public float getScale(int type){
		switch (type){
		case 0:
			return this.scale;
		case 1:
		default:
			return this.scale2;
		}
	}
	
}
