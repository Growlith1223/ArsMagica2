package am2.entity;

import java.util.ArrayList;
import java.util.List;

import am2.api.DamageSources;
import am2.bosses.EntityNatureGuardian;
import am2.defs.ItemDefs;
import am2.enchantments.AMEnchantmentHelper;
import am2.items.ItemNatureGuardianSickle;
import am2.trackers.PlayerTracker;
import am2.utils.DummyEntityPlayer;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class EntityThrownSickle extends EntityLiving{

	private final int maxTicksToExist;
	private EntityLivingBase throwingEntity;
	private final ArrayList<Integer> entityHits;
	private static final DataParameter<Integer> THROWING_ENTITY = EntityDataManager.createKey(EntityThrownSickle.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> PROJECTILE_SPEED = EntityDataManager.createKey(EntityThrownSickle.class, DataSerializers.VARINT);
    private ItemStack itemNBT;
	public EntityThrownSickle(World par1World){
		super(par1World);
		ticksExisted = 0;
		maxTicksToExist = 120;
		this.noClip = true;
		entityHits = new ArrayList<Integer>();
		this.setSize(0.5f, 2);
        itemNBT = null;
	}

	public EntityThrownSickle(World world, EntityLivingBase entityLiving, double projectileSpeed){
		this(world);
		throwingEntity = entityLiving;
		setSize(0.25F, 0.25F);
		setLocationAndAngles(entityLiving.posX, entityLiving.posY + entityLiving.getEyeHeight(), entityLiving.posZ, entityLiving.rotationYaw, entityLiving.rotationPitch);
		posX -= MathHelper.cos((rotationYaw / 180F) * 3.141593F) * 0.16F;
		posY -= 0.10000000149011612D;
		posZ -= MathHelper.sin((rotationYaw / 180F) * 3.141593F) * 0.16F;
		setPosition(posX, posY, posZ);
		float f = 0.05F;
		motionX = -MathHelper.sin((rotationYaw / 180F) * 3.141593F) * MathHelper.cos((rotationPitch / 180F) * 3.141593F) * f;
		motionZ = MathHelper.cos((rotationYaw / 180F) * 3.141593F) * MathHelper.cos((rotationPitch / 180F) * 3.141593F) * f;
		motionY = -MathHelper.sin((rotationPitch / 180F) * 3.141593F) * f;
		setHeading(motionX, motionY, motionZ, projectileSpeed, projectileSpeed);	
	}

	public void setSickleNBT(ItemStack val){
        itemNBT = val;
    }
	
	public void setInMotion(double projectileSpeed) {
		float f = 0.05F;
		motionX = -MathHelper.sin((rotationYaw / 180F) * 3.141593F) * MathHelper.cos((rotationPitch / 180F) * 3.141593F) * f;
		motionZ = MathHelper.cos((rotationYaw / 180F) * 3.141593F) * MathHelper.cos((rotationPitch / 180F) * 3.141593F) * f;
		motionY = -MathHelper.sin((rotationPitch / 180F) * 3.141593F) * f;
		setHeading(motionX, motionY, motionZ, projectileSpeed, projectileSpeed);	
	}

	public void setHeading(double movementX, double movementY, double movementZ, double projectileSpeed, double projectileSpeed2){
		float f = MathHelper.sqrt(movementX * movementX + movementY * movementY + movementZ * movementZ);
		movementX /= f;
		movementY /= f;
		movementZ /= f;
		movementX += rand.nextGaussian() * 0.0074999998323619366D * projectileSpeed2;
		movementY += rand.nextGaussian() * 0.0074999998323619366D * projectileSpeed2;
		movementZ += rand.nextGaussian() * 0.0074999998323619366D * projectileSpeed2;
		movementX *= projectileSpeed;
		movementY *= projectileSpeed;
		movementZ *= projectileSpeed;
		motionX = movementX;
		motionY = movementY;
		motionZ = movementZ;
		float f1 = MathHelper.sqrt(movementX * movementX + movementZ * movementZ);
		prevRotationYaw = rotationYaw = (float)((Math.atan2(movementX, movementZ) * 180D) / Math.PI);
		prevRotationPitch = rotationPitch = (float)((Math.atan2(movementY, f1) * 180D) / Math.PI);
	}

	@Override
	public void setDead(){
		if (getThrowingEntity() != null && getThrowingEntity() instanceof EntityNatureGuardian){
			((EntityNatureGuardian)getThrowingEntity()).hasSickle = true;
		}else if (getThrowingEntity() != null && getThrowingEntity() instanceof EntityPlayer){
			if (!world.isRemote) {
                ItemStack res = itemNBT == null ? ItemDefs.natureScytheEnchanted.copy() : itemNBT;
                if (getThrowingEntity().getHealth() <= 0) {
                    PlayerTracker.storeSoulboundItemForRespawn((EntityPlayer) getThrowingEntity(), res);
                } else {
                    if (!((EntityPlayer) getThrowingEntity()).inventory.addItemStackToInventory(res)) {
                        EntityItem item = new EntityItem(world);
                        item.setPosition(posX, posY, posZ);
                        if (itemNBT != null)
                            item.setEntityItemStack(res);
                        else
                            item.setEntityItemStack(res);
                        world.spawnEntity(item);
                    }
                }
            }
		}
		super.setDead();
	}

	@Override
	public void onUpdate(){
		if (!world.isRemote && (getThrowingEntity() == null || getThrowingEntity().isDead)){
			setDead();
			return;
		}else{
			ticksExisted++;
			if (ticksExisted >= maxTicksToExist && !world.isRemote){
				setDead();
				return;
			}
		}
		if (getThrowingEntity() != null && getThrowingEntity() instanceof EntityNatureGuardian){
			((EntityNatureGuardian)getThrowingEntity()).hasSickle = false;
		}
		
		Vec3d vec3d = new Vec3d(posX, posY, posZ);
		Vec3d vec3d1 = new Vec3d(posX + motionX, posY + motionY, posZ + motionZ);
		RayTraceResult movingobjectposition = world.rayTraceBlocks(vec3d, vec3d1);
		vec3d = new Vec3d(posX, posY, posZ);
		vec3d1 = new Vec3d(posX + motionX, posY + motionY, posZ + motionZ);
		if (movingobjectposition != null){
			vec3d1 = new Vec3d(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
		}
		Entity entity = null;
		List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox().addCoord(motionX, motionY, motionZ).expand(1.0D, 1.0D, 1.0D));
		double d = 0.0D;
		for (int j = 0; j < list.size(); j++){
			Entity entity1 = list.get(j);
			if (!entity1.canBeCollidedWith() || entity1.isEntityEqual(getThrowingEntity()) && ticksExisted < 25){
				continue;
			}
			float f2 = 0.3F;
			AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand(f2, f2, f2);
			RayTraceResult movingobjectposition1 = axisalignedbb.calculateIntercept(vec3d, vec3d1);
			if (movingobjectposition1 == null){
				continue;
			}
			double d1 = vec3d.distanceTo(movingobjectposition1.hitVec);
			if (d1 < d || d == 0.0D){
				entity = entity1;
				d = d1;
			}
		}

		if (entity != null){
			movingobjectposition = new RayTraceResult(entity);
		}else{
			movingobjectposition = new RayTraceResult(new Vec3d(posX, posY, posZ), null, getPosition());
		}
		if (movingobjectposition != null){
			HitObject(movingobjectposition);
		}
		posX += motionX;
		posY += motionY;
		posZ += motionZ;
		setPosition(posX, posY, posZ);
		float f = MathHelper.sqrt(motionX * motionX + motionZ * motionZ);
		rotationYaw = (float)((Math.atan2(motionX, motionZ) * 180D) / 3.1415927410125732D);
		for (rotationPitch = (float)((Math.atan2(motionY, f) * 180D) / 3.1415927410125732D); rotationPitch - prevRotationPitch < -180F; prevRotationPitch -= 360F){
		}
		for (; rotationPitch - prevRotationPitch >= 180F; prevRotationPitch += 360F){
		}
		for (; rotationYaw - prevRotationYaw < -180F; prevRotationYaw -= 360F){
		}
		for (; rotationYaw - prevRotationYaw >= 180F; prevRotationYaw += 360F){
		}
		rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
		rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;
		if (isInWater()){
			for (int k = 0; k < 4; k++){
				float f3 = 0.25F;
				world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, posX - motionX * f3, posY - motionY * f3, posZ - motionZ * f3, motionX, motionY, motionZ);
			}
		}

		if (this.ticksExisted > 30 && this.ticksExisted < 40){
			this.motionX *= 0.8f;
			this.motionY *= 0.8f;
			this.motionZ *= 0.8f;
		}else if (this.ticksExisted > 40 && getThrowingEntity() != null){
			double deltaX = this.posX - getThrowingEntity().posX;
			double deltaZ = this.posZ - getThrowingEntity().posZ;
			double deltaY = this.posY - getThrowingEntity().posY;
			double angle = Math.atan2(deltaZ, deltaX);
			double speed = Math.min((this.ticksExisted - 40f) / 10f, this.getProjectileSpeed());

			double horizontalDistance = MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ);
			float pitchRotation = (float)(-Math.atan2(deltaY, horizontalDistance));

			this.motionX = -Math.cos(angle) * speed;
			this.motionZ = -Math.sin(angle) * speed;
			this.motionY = Math.sin(pitchRotation) * speed;

			if (this.getDistanceSqToEntity(getThrowingEntity()) < 1.2 && !world.isRemote){
				this.setDead();
			}
		}
		
	}

	protected void HitObject(RayTraceResult movingobjectposition){
		if (movingobjectposition.entityHit != null && movingobjectposition.entityHit instanceof EntityLivingBase){
			if (movingobjectposition.entityHit == getThrowingEntity() || getThrowingEntity() == null) return;
			if (getThrowingEntity() != null && !this.entityHits.contains(movingobjectposition.entityHit.getEntityId())){
				this.entityHits.add(movingobjectposition.entityHit.getEntityId());
				if (getThrowingEntity() instanceof EntityPlayer){
					if (movingobjectposition.entityHit instanceof EntityPlayer && (getThrowingEntity().world.isRemote || !FMLCommonHandler.instance().getMinecraftServerInstance().isPVPEnabled()))
						return;
					movingobjectposition.entityHit.attackEntityFrom(DamageSources.causeCactusDamage(getThrowingEntity(), true), 10);
				}else{
					movingobjectposition.entityHit.attackEntityFrom(DamageSources.causeCactusDamage(getThrowingEntity(), true), 12);
				}
			}
		}else if (movingobjectposition.typeOfHit == RayTraceResult.Type.BLOCK && this.getThrowingEntity() != null && this.getThrowingEntity() instanceof EntityPlayer){
			int radius = 1;
			for (int i = -radius; i <= radius; ++i){
				for (int j = -radius; j <= radius; ++j){
					for (int k = -radius; k <= radius; ++k){
						IBlockState nextBlock = world.getBlockState(movingobjectposition.getBlockPos().add(i, j, k));
						if (nextBlock == null) continue;
						if (nextBlock.getBlock() instanceof BlockLeaves || nextBlock.getBlock() instanceof BlockBush || nextBlock.getBlock() instanceof BlockCrops){
							if (ForgeEventFactory.doPlayerHarvestCheck(DummyEntityPlayer.fromEntityLiving(getThrowingEntity()), nextBlock, true))
								if (!world.isRemote)
									world.destroyBlock(movingobjectposition.getBlockPos().add(i, j, k), true);
						}
					}
				}
			}
		}
	}

	@Override
	protected void entityInit(){
		super.entityInit();
		this.dataManager.register(THROWING_ENTITY, 0);
		this.dataManager.register(PROJECTILE_SPEED, 20);
	}

	public void setThrowingEntity(EntityLivingBase entity){
		this.throwingEntity = entity;
		this.dataManager.set(THROWING_ENTITY, entity.getEntityId());
	}

	public void setProjectileSpeed(double speed){
		this.dataManager.set(PROJECTILE_SPEED, (int)(speed * 10));
	}

	private double getProjectileSpeed(){
		return this.dataManager.get(PROJECTILE_SPEED) / 10;
	}

	private EntityLivingBase getThrowingEntity(){
		if (throwingEntity == null){
			Entity e = this.world.getEntityByID(this.dataManager.get(THROWING_ENTITY));
			if (e instanceof EntityLivingBase)
				throwingEntity = (EntityLivingBase)e;
		}
		return throwingEntity;
	}

	@Override
	public ItemStack getHeldItem(EnumHand hand){
		return null;
	}
	
	@Override
	public void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack) {
	}

	@Override
	public boolean attackEntityFrom(DamageSource par1DamageSource, float par2){
		return false;
	}

	@Override
	protected boolean canDespawn(){
		return false;
	}
}
