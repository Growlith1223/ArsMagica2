package am2.entity;

import java.util.List;

import am2.ArsMagica2;
import am2.entity.ai.EntityAIFireballAttack;
import am2.particles.AMParticle;
import am2.particles.ParticleApproachPoint;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIBreakDoor;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class EntityFireElemental extends EntityMob{

	private static final ItemStack defaultHeldItem;
	private static final int cookRadius = 10;
//	private int burnTimer;
	
	private static final DataParameter<Integer> COOK_TARGET_ID = EntityDataManager.createKey(EntityFireElemental.class, DataSerializers.VARINT);

	public EntityFireElemental(World world){
		super(world);
		setSize(0.6F, 1.8F);
		isImmuneToFire = true;

		initAI();
	}

	private void initAI(){
		//this.getNavigator().setBreakDoors(true);
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(2, new EntityAIBreakDoor(this));
		this.tasks.addTask(2, new EntityAIFireballAttack(this, 0.5f, 1, 20));
		this.tasks.addTask(3, new EntityAIAttackMelee(this, 0.5f, false));
		this.tasks.addTask(7, new EntityAIWander(this, 0.5f));
		this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(8, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, 0, true, false, null));
	}

	@Override
	protected void applyEntityAttributes(){
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(15D);
	}

	@Override
	protected void entityInit(){
		super.entityInit();
		dataManager.register(COOK_TARGET_ID, 0);
	}

	@Override
	public int getTotalArmorValue(){
		return 5;
	}

	@Override
	public boolean isBurning(){
		return this.getAttackTarget() != null;
	}

	@Override
	protected boolean isValidLightLevel(){
		return true;
	}

//	@Override
//	protected String getLivingSound(){
//		return "fire_elem_living";
//	}
//
//	@Override
//	protected String getHurtSound(){
//		return "fire_elem_hurt";
//	}
//
//	@Override
//	protected String getDeathSound(){
//		return "fire_elem_death";
//	}

	public int getEntityBrightnessForRender(float f){
		return 0xf000f0;
	}

	public float getEntityBrightness(float f){
		return 1.0F;
	}

	@Override
	public void onLivingUpdate(){
		if (isWet()){
			this.attackEntityFrom(DamageSource.DROWN, 1);
		}
//		if (!this.world.isRemote){
//			if (this.getAttackTarget() != null && !this.getAttackTarget().isDead){
//				if (this.dataManager.getWatchableObjectByte(0) == (byte)0){
//					this.dataManager.updateObject(0, (byte)1);
//					burnTimer = 20;
//				}
//			}else{
//				if (burnTimer > 0){
//					burnTimer--;
//				}else if (this.dataManager.getWatchableObjectByte(0) == (byte)1){
//					this.dataManager.updateObject(0, (byte)0);
//				}
//			}
//		}
		super.onLivingUpdate();
	}

	@Override
	public void onUpdate(){
		int cookTargetID = dataManager.get(COOK_TARGET_ID);
		if (cookTargetID != 0){
			List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, this.getEntityBoundingBox().expand(cookRadius, cookRadius, cookRadius));
			EntityItem inanimate = null;
			for (EntityItem item : items){
				if (item.getEntityId() == cookTargetID){
					inanimate = item;
				}
			}

			if (inanimate != null && world.isRemote){
				AMParticle effect = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "fire", posX, posY + getEyeHeight(), posZ);
				if (effect != null){
					effect.setIgnoreMaxAge(true);
					effect.AddParticleController(new ParticleApproachPoint(effect, inanimate.posX + (rand.nextFloat() - 0.5), inanimate.posY + (rand.nextFloat() - 0.5), inanimate.posZ + (rand.nextFloat() - 0.5), 0.1f, 0.1f, 1, false).setKillParticleOnFinish(true));
				}
			}
		}

		if (world.isRemote && rand.nextInt(100) > 75 && !isBurning())
			for (int i = 0; i < ArsMagica2.config.getGFXLevel(); i++)
				world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, posX + (rand.nextDouble() - 0.5D) * width, posY + rand.nextDouble() * height, posZ + (rand.nextDouble() - 0.5D) * width, 0.0D, 0.0D, 0.0D);
		super.onUpdate();
	}

	@Override
	public ItemStack getHeldItemMainhand(){
		return defaultHeldItem.copy();
	}

	static{
		defaultHeldItem = new ItemStack(Items.FIRE_CHARGE, 1, 0);
	}

	@Override
	public boolean getCanSpawnHere(){
		if (!SpawnBlacklists.entityCanSpawnHere(getPosition(), world, this))
			return false;
		return super.getCanSpawnHere();
	}
}
