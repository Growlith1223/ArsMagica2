package am2.entity;

import java.util.List;

import am2.ArsMagica2;
import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import am2.defs.AMSounds;
import am2.defs.ItemDefs;
import am2.extensions.EntityExtension;
import am2.packet.AMNetHandler;
import am2.particles.AMParticle;
import am2.particles.ParticleFadeOut;
import am2.particles.ParticleFloatUpward;
import am2.particles.ParticleMoveOnHeading;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIBreakDoor;
import net.minecraft.entity.ai.EntityAIFleeSun;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class EntityHecate extends EntityZombie{

	private double leftArmAnimTicks;
	private double rightArmAnimTicks;

	private double leftArmRotationOffset;
	private double rightArmRotationOffset;

	private final float hostileSpeed;
	private static final float forwardThreshold = 1.22f;
	private float currentForwardRotation = 0f;

	private int invisibilityCooldown = 0;
	private int invisibilityCounter = 0;
	private boolean hasSpawnedInvisParticles = false;
	public EntityHecate(World par1World){
		super(par1World);
		leftArmAnimTicks = 0;
		rightArmAnimTicks = 12;
		leftArmRotationOffset = 0;
		rightArmRotationOffset = 0;
		this.hostileSpeed = 1.7F;
		this.setSize(0.6f, 1.5f);

		EntityExtension.For(this).setCurrentLevel(7);
		EntityExtension.For(this).setCurrentMana(600);

		this.tasks.taskEntries.clear();
		this.targetTasks.taskEntries.clear();
		initAI();
		this.stepHeight = 1.02f;
	}

	@Override
	protected void applyEntityAttributes(){
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(12D);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5D);
	}

	@Override
	protected void entityInit(){
		super.entityInit();
	}

	private void initAI(){
		((PathNavigateGround)this.getNavigator()).setBreakDoors(true);
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(2, new EntityAIBreakDoor(this));
		this.tasks.addTask(3, new EntityAIAttackMelee(this, this.hostileSpeed, false));
		this.tasks.addTask(1, new EntityAIFleeSun(this, this.hostileSpeed));
		this.tasks.addTask(7, new EntityAIWander(this, 0.5f));
		this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(8, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityGolem>(this, EntityGolem.class, 0, false, false, null));
		this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, 0, true, false, null));
		this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<EntityVillager>(this, EntityVillager.class, 0, false, false, null));
	}

	@Override
	public int getTotalArmorValue(){
		return 5;
	}

	public float getHorizontalAverageVelocity(){
		return (float)((this.motionX + this.motionZ) / 2);
	}

	@Override
	public void onDeath(DamageSource par1DamageSource){
		super.onDeath(par1DamageSource);
	}

	private boolean isMoving(){
		return (this.prevPosX != this.posX) || (this.prevPosZ != this.posZ);
	}

	private void updateForwardRotation(){
		if (isMoving() && currentForwardRotation < forwardThreshold){
			currentForwardRotation += 0.12f;
		}else if (!isMoving() && currentForwardRotation > 0){
			currentForwardRotation -= 0.12f;
		}
	}

	public float getForwardRotation(){
		return currentForwardRotation;
	}

	@Override
	public void onUpdate(){

		if (invisibilityCooldown > 0){
			invisibilityCooldown--;
		}
		if (invisibilityCooldown == 0) hasSpawnedInvisParticles = false;

		if (this.motionY < 0)
			this.motionY *= 0.79999f;

		if (this.world != null){
			if (this.world.isRemote){
				if (!this.getFlag(5) && this.ticksExisted % 3 == 0){
					spawnLivingParticles();
				}else if (!hasSpawnedInvisParticles){
					spawnInvisibilityParticles();
				}

				if (invisibilityCounter > 0) invisibilityCounter--;

				updateArmRotations();
				updateForwardRotation();
			}
			if (this.world.getDifficulty() == EnumDifficulty.HARD && this.getAttackTarget() != null && this.invisibilityCooldown == 0){
				this.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("invisibility"), 60, 2));
				this.invisibilityCooldown = 600;
			}
		}
		super.onUpdate();
	}

	@Override
	public void onLivingUpdate(){
		if (this.world.isDaytime() && !this.world.isRemote && !this.isDead){
			float f = this.getBrightness(1.0F);

			if (f > 0.5F && this.rand.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && this.world.canBlockSeeSky(getPosition())){
				AMNetHandler.INSTANCE.sendHecateDeathToAllAround(this);
				this.attackEntityFrom(DamageSource.ON_FIRE, 5000);
			}
		}
		super.onLivingUpdate();
	}

	private void spawnInvisibilityParticles(){
		/*for (int i = 0; i < 50; ++i){
			ArsMagicaParticle effect = ParticleManager.spawn(this.world, "hr_smoke", this.posX + rand.nextDouble(), this.posY + 1, this.posZ);
			if (effect != null){
				effect.setMaxAge(20);
				effect.setIgnoreMaxAge(false);
				effect.AddParticleController(new ParticleFleeEntity(effect, this, 0.1, 3, 1, false));
			}
		}*/
		hasSpawnedInvisParticles = true;
		this.invisibilityCooldown = 600;
	}

	public double getLeftArmOffset(){
		return this.leftArmRotationOffset;
	}

	public double getRightArmOffset(){
		return this.rightArmRotationOffset;
	}

	private void spawnLivingParticles(){

		if (rand.nextInt(3) == 0){
			double yPos = this.posY + 1.1;
			if (this.currentForwardRotation >= 0.24){
				yPos += 0.3;
			}

			AMParticle effect = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "smoke",
					this.posX + ((rand.nextFloat() * 0.2) - 0.1f),
					yPos,
					this.posZ + ((rand.nextFloat() * 0.4) - 0.2f));
			if (effect != null){
				if (this.currentForwardRotation < 0.24){
					effect.AddParticleController(new ParticleFloatUpward(effect, 0.1f, -0.06f, 1, false));
				}else{
					effect.AddParticleController(new ParticleMoveOnHeading(effect, this.rotationYaw - 90, this.rotationPitch, 0.01f, 1, false));
				}
				effect.AddParticleController(new ParticleFadeOut(effect, 2, false).setFadeSpeed(0.04f));
				effect.setMaxAge(25);
				effect.setIgnoreMaxAge(false);
				effect.setRGBColorF(0.3f, 0.3f, 0.3f);
			}
		}
	}

	private void updateArmRotations(){
		leftArmAnimTicks += 0.05;
		leftArmAnimTicks %= 90;
		rightArmAnimTicks += 0.05;
		rightArmAnimTicks %= 90;

		//double lpct = ((double)leftArmAnimTicks - 90) / 180.0d;
		//double rpct = ((double)rightArmAnimTicks - 90) / 180.0d;

		leftArmRotationOffset = Math.sin(leftArmAnimTicks) * .3;
		rightArmRotationOffset = Math.cos(rightArmAnimTicks) * .3;
	}
	
	@Override
	protected void dropFewItems(boolean bool, int looting){
		if (getRNG().nextInt(10) == 5)
			this.entityDropItem(new ItemStack(ItemDefs.essence, 1, ArsMagicaAPI.getAffinityRegistry().getId(Affinity.ENDER)), 0.0f);
	}

	@Override
	protected Item getDropItem(){
		return null;
	}

	@Override
	protected SoundEvent getHurtSound(){
		return AMSounds.HECATE_HIT;
	}

	@Override
	protected SoundEvent getDeathSound(){
		return AMSounds.HECATE_DEATH;
	}

	@Override
	protected SoundEvent getAmbientSound(){
		return AMSounds.HECATE_IDLE;
	}

	private int getAverageNearbyPlayerMagicLevel(){
		if (this.world == null) return 0;
		List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().expand(250, 250, 250));
		if (players.size() == 0) return 0;
		int avgLvl = 0;
		for (EntityPlayer player : players){
			avgLvl += EntityExtension.For(player).getCurrentLevel();
		}
		return (int)Math.ceil(avgLvl / players.size());
	}

	@Override
	public boolean getCanSpawnHere(){
		if (!SpawnBlacklists.entityCanSpawnHere(this.getPosition(), world, this))
			return false;
		if (getAverageNearbyPlayerMagicLevel() < 20){
			return false;
		}
		return super.getCanSpawnHere();
	}
}
