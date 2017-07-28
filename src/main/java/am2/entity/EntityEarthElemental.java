package am2.entity;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIBreakDoor;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class EntityEarthElemental extends EntityMob{

	public EntityEarthElemental(World world){
		super(world);
		setSize(0.6F, 1.8F);
		initAI();
	}

	private void initAI(){
		//this.getNavigator().setBreakDoors(true);
		//this.getNavigator().setAvoidSun(true);
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(2, new EntityAIBreakDoor(this));
		this.tasks.addTask(3, new EntityAIAttackMelee(this, 0.5f, false));
		this.tasks.addTask(7, new EntityAIWander(this, 0.5f));
		this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(8, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, 0, true, false, null));
	}

	@Override
	public boolean isAIDisabled(){
		return false;
	}

	@Override
	protected void applyEntityAttributes(){
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(12);
	}

	@Override
	public int getTotalArmorValue(){
		return 14;
	}

	protected float MovementSpeed(){
		return 0.4f;
	}

	@Override
	protected SoundEvent getAmbientSound(){
		return SoundEvents.ENTITY_IRONGOLEM_STEP;
	}

	@Override
	protected SoundEvent getHurtSound(){
		return SoundEvents.ENTITY_IRONGOLEM_HURT;
	}

	@Override
	protected SoundEvent getDeathSound(){
		return SoundEvents.ENTITY_IRONGOLEM_DEATH;
	}
	
//	@Override
//	public boolean attackEntityFrom(DamageSource source, float amount) {
//		if (amount < 1.5f && entity.getEntityBoundingBox().maxY >= getEntityBoundingBox().minY && entity.getEntityBoundingBox().minY <= getEntityBoundingBox().maxY){
//			if (onGround){
//				entity.attackEntityFrom(DamageSource.causeMobDamage(this), 1);
//			}
//		}		
//	}

	@Override
	public boolean getCanSpawnHere(){
		if (!SpawnBlacklists.entityCanSpawnHere(getPosition(), world, this))
			return false;
		return super.getCanSpawnHere();
	}
}
