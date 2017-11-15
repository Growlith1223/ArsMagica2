package am2.entity;

import java.util.List;

import am2.spell.component.Dig;
import am2.spell.modifier.Piercing;
import com.google.common.base.Optional;

import am2.api.affinity.Affinity;
import am2.api.spell.SpellModifiers;
import am2.defs.ItemDefs;
import am2.utils.AffinityShiftUtils;
import am2.utils.NBTUtils;
import am2.utils.SpellUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntitySpellProjectile extends Entity {
	
	
	private static final DataParameter<Integer> DW_BOUNCE_COUNTER = EntityDataManager.createKey(EntitySpellProjectile.class, DataSerializers.VARINT);
	private static final DataParameter<Float> DW_GRAVITY = EntityDataManager.createKey(EntitySpellProjectile.class, DataSerializers.FLOAT);
	private static final DataParameter<Optional<ItemStack>> DW_EFFECT = EntityDataManager.createKey(EntitySpellProjectile.class, DataSerializers.OPTIONAL_ITEM_STACK);
	private static final DataParameter<String> DW_ICON_NAME = EntityDataManager.createKey(EntitySpellProjectile.class, DataSerializers.STRING);
	private static final DataParameter<Integer> DW_PIERCE_COUNT = EntityDataManager.createKey(EntitySpellProjectile.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> DW_COLOR = EntityDataManager.createKey(EntitySpellProjectile.class, DataSerializers.VARINT);
	private static final DataParameter<Integer> DW_SHOOTER = EntityDataManager.createKey(EntitySpellProjectile.class, DataSerializers.VARINT);
	private static final DataParameter<Boolean> DW_TARGETGRASS = EntityDataManager.createKey(EntitySpellProjectile.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> DW_HOMING = EntityDataManager.createKey(EntitySpellProjectile.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> DW_HOMING_TARGET = EntityDataManager.createKey(EntitySpellProjectile.class, DataSerializers.VARINT);

	private static int currentPierces;
	public EntitySpellProjectile(World worldIn) {
		super(worldIn);
		setSize(0.5F, 0.5F);
		//noClip = true;
	}
	
	public void setTargetWater(){
		if (!this.worldObj.isRemote)
			this.getDataManager().set(DW_TARGETGRASS, true);
	}
	
	public boolean targetWater() {
		return this.getDataManager().get(DW_TARGETGRASS);
	}
	
	@Override
	protected void entityInit() {
		this.getDataManager().register(DW_BOUNCE_COUNTER, 0);
		this.getDataManager().register(DW_GRAVITY, 0.f);
		this.getDataManager().register(DW_EFFECT, Optional.<ItemStack>absent());
		this.getDataManager().register(DW_ICON_NAME, "arcane");
		this.getDataManager().register(DW_PIERCE_COUNT, 0);
		this.getDataManager().register(DW_COLOR, 0xFFFFFF);
		this.getDataManager().register(DW_SHOOTER, 0);
		this.getDataManager().register(DW_TARGETGRASS, false);
		this.getDataManager().register(DW_HOMING, false);
		this.getDataManager().register(DW_HOMING_TARGET, -1);
	}
	
	public void setShooter (EntityLivingBase living) {
		this.getDataManager().set(DW_SHOOTER, living.getEntityId());
	}
	
	public void decreaseBounces () {
		setBounces(getBounces() -1);
	}
	
	public int getBounces () {
		return this.getDataManager().get(DW_BOUNCE_COUNTER);
	}

	public int getPierces () { return this.getDataManager().get(DW_PIERCE_COUNT) - this.currentPierces; }
	
	public ItemStack getSpell () {
		return this.getDataManager().get(DW_EFFECT).orNull();
	}
	
	public void bounce(EnumFacing facing) {
		if (facing == null) {
			motionX = -motionX;
			motionY = -motionY;
			motionZ = -motionZ;			
		}
		else {
			double projectileSpeed = SpellUtils.getModifiedDouble_Mul(1, getSpell(), getShooter(), null, worldObj, SpellModifiers.VELOCITY_ADDED);
			double newMotionX = motionX / projectileSpeed;
			double newMotionY = motionY / projectileSpeed;
			double newMotionZ = motionZ / projectileSpeed;
			if (facing.equals(EnumFacing.UP) || facing.equals(EnumFacing.DOWN)) {
				newMotionY = -newMotionY;
			}
			else if (facing.equals(EnumFacing.NORTH) || facing.equals(EnumFacing.SOUTH)) {
				newMotionZ = -newMotionZ;
			}
			else if (facing.equals(EnumFacing.EAST) || facing.equals(EnumFacing.WEST)) {
				newMotionX = -newMotionX;
			}
			motionX = newMotionX * projectileSpeed;
			motionY = newMotionY * projectileSpeed;
			motionZ = newMotionZ * projectileSpeed;
		}
		decreaseBounces();
	}
	
	@Override
	public void onUpdate() {
		try {
			if (ticksExisted > 200)
				this.setDead();
			RayTraceResult mop = worldObj.rayTraceBlocks(new Vec3d(posX, posY, posZ),new Vec3d(posX + motionX, posY + motionY, posZ + motionZ));
			if (mop != null && mop.typeOfHit.equals(RayTraceResult.Type.BLOCK)) {
				if (worldObj.getBlockState(mop.getBlockPos()).getBlock().isBlockSolid(worldObj, mop.getBlockPos(), mop.sideHit) || targetWater()) {
					worldObj.getBlockState(mop.getBlockPos()).getBlock().onEntityCollidedWithBlock(worldObj, mop.getBlockPos(), worldObj.getBlockState(mop.getBlockPos()), this);
					if (getBounces() > 0) {
						bounce(mop.sideHit);
					} else {
						SpellUtils.applyStageToGround(getSpell(), getShooter(), worldObj, mop.getBlockPos(), mop.sideHit, posX, posY, posZ, true);
						SpellUtils.applyStackStage(getSpell(), getShooter(), null, mop.hitVec.xCoord + motionX, mop.hitVec.yCoord + motionY, mop.hitVec.zCoord + motionZ, mop.sideHit, worldObj, false, true, 0);
						if (this.getPierces() == 1 || !SpellUtils.modifierIsPresent(SpellModifiers.PIERCING, this.getSpell()))
							this.setDead();
						else
							this.currentPierces++;
					}
				}
			} else {
				List<Entity> list = worldObj.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox().addCoord(motionX, motionY, motionZ).expand(0.25D, 0.25D, 0.25D));
				int effSize = list.size();
				for (Entity entity : list) {
					if (entity instanceof EntityDragonPart && ((EntityDragonPart)entity).entityDragonObj instanceof EntityLivingBase)
						entity = (EntityLivingBase)((EntityDragonPart)entity).entityDragonObj;
					if (entity instanceof EntityLivingBase) {
						if (entity.equals(getShooter())) {
							effSize--;
							continue;
						}
						SpellUtils.applyStageToEntity(getSpell(), getShooter(), worldObj, entity, true);
						SpellUtils.applyStackStage(getSpell(), getShooter(), (EntityLivingBase) entity, entity.posX, entity.posY, entity.posZ, null, worldObj, false, true, 0);
						break;
					} else {
						effSize--;
					}
				}
				if (effSize != 0) {
					if (this.getPierces() == 1 || !SpellUtils.modifierIsPresent(SpellModifiers.PIERCING, this.getSpell()))
						this.setDead();
					else
						this.currentPierces++;
				}
			}
			motionY += this.getDataManager().get(DW_GRAVITY);
			setPosition(posX + motionX, posY + motionY, posZ + motionZ);
		} catch (NullPointerException e) {
			this.setDead();
		}
	}
	
	public EntityLivingBase getShooter() {
		try {
			return (EntityLivingBase) worldObj.getEntityByID(this.getDataManager().get(DW_SHOOTER));
		} catch (RuntimeException e) {
			this.setDead();
			return null;
		}
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound tagCompund) {
		NBTTagCompound am2Tag = NBTUtils.getAM2Tag(tagCompund);
		dataManager.set(DW_BOUNCE_COUNTER, am2Tag.getInteger("BounceCount"));
		dataManager.set(DW_GRAVITY, am2Tag.getFloat("Gravity"));
		dataManager.set(DW_EFFECT, Optional.of(ItemStack.loadItemStackFromNBT(am2Tag.getCompoundTag("Effect"))));
		dataManager.set(DW_ICON_NAME, am2Tag.getString("IconName"));
		dataManager.set(DW_PIERCE_COUNT, am2Tag.getInteger("PierceCount"));
		dataManager.set(DW_COLOR, am2Tag.getInteger("Color"));
		dataManager.set(DW_SHOOTER, am2Tag.getInteger("Shooter"));
		dataManager.set(DW_TARGETGRASS, am2Tag.getBoolean("TargetGrass"));
		dataManager.set(DW_HOMING, am2Tag.getBoolean("Homing"));
		dataManager.set(DW_HOMING_TARGET, am2Tag.getInteger("HomingTarget"));
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound tagCompound) {
		NBTTagCompound am2Tag = NBTUtils.getAM2Tag(tagCompound);
		am2Tag.setInteger("BounceCount", dataManager.get(DW_BOUNCE_COUNTER));
		am2Tag.setFloat("Gravity", dataManager.get(DW_GRAVITY));
		NBTTagCompound tmp = new NBTTagCompound();
		dataManager.get(DW_EFFECT).or(new ItemStack(ItemDefs.spell)).writeToNBT(tmp);
		am2Tag.setTag("Effect", tmp);
		am2Tag.setString("IconName", dataManager.get(DW_ICON_NAME));
		am2Tag.setInteger("PierceCount", dataManager.get(DW_PIERCE_COUNT));

		am2Tag.setInteger("Color", dataManager.get(DW_COLOR));
		am2Tag.setInteger("Shooter", dataManager.get(DW_SHOOTER));
		am2Tag.setBoolean("TargetGrass", dataManager.get(DW_TARGETGRASS));
		am2Tag.setBoolean("Homing", dataManager.get(DW_HOMING));
		am2Tag.setInteger("HomingTarget", dataManager.get(DW_HOMING_TARGET));
	}
	
	public void selectHomingTarget () {
		List<Entity> entities = worldObj.getEntitiesWithinAABBExcludingEntity(this, this.getCollisionBoundingBox().expand(10.0F, 10.0F, 10.0F));
		Vec3d pos = new Vec3d(posX, posY, posZ);
		EntityLivingBase target = null;
		double dist = 900;
		for (Entity entity : entities) {
			if (entity instanceof EntityLivingBase && !entity.equals(getShooter())) {
				Vec3d ePos = new Vec3d(entity.posX, entity.posY, entity.posZ);
				double eDist = pos.distanceTo(ePos);
				if (eDist < dist) {
					dist = eDist;
					target = (EntityLivingBase)entity;
				}
			}
		}
		
		if (target != null) {
			this.getDataManager().set(DW_HOMING_TARGET, target.getEntityId());
		}
	}
	
	public EntityLivingBase getHomingTarget() {
		return (EntityLivingBase) worldObj.getEntityByID(this.getDataManager().get(DW_HOMING_TARGET));
	}
	
	public void setGravity(float projectileGravity) {
		this.getDataManager().set(DW_GRAVITY, projectileGravity);
	}
	
	public void setSpell (ItemStack stack) {
		this.getDataManager().set(DW_EFFECT, Optional.fromNullable(stack));
		Affinity mainAff = AffinityShiftUtils.getMainShiftForStack(stack);
		if (mainAff.equals(Affinity.ENDER)) this.getDataManager().set(DW_COLOR, 0x550055);
		else if (mainAff.equals(Affinity.ICE)) this.getDataManager().set(DW_COLOR, 0x2299FF);
		else if (mainAff.equals(Affinity.LIFE)) this.getDataManager().set(DW_COLOR, 0x22FF44);
	}
	
	public void setBounces(int projectileBounce) {
		this.getDataManager().set(DW_BOUNCE_COUNTER, projectileBounce);
	}

	public void setNumPierces(int pierces) {
		this.getDataManager().set(DW_PIERCE_COUNT, pierces);
		this.currentPierces = 0;
	}

	public void setHoming(boolean homing) {
		this.getDataManager().set(DW_HOMING, homing);		
	}
	
	public void setIcon(String icon) {
		this.getDataManager().set(DW_ICON_NAME, icon);
	}
	
	public String getIcon() {
		return this.getDataManager().get(DW_ICON_NAME);
	}
	
	public int getColor() {
		return this.getDataManager().get(DW_COLOR);
	}
	
}
