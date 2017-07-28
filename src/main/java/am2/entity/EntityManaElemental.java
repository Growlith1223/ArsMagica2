package am2.entity;

import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import am2.defs.AMSounds;
import am2.defs.ItemDefs;
import am2.entity.ai.EntityAIManaDrainBolt;
import am2.extensions.EntityExtension;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class EntityManaElemental extends EntityMob{

	private final float hostileSpeed;
	
	public EntityManaElemental(World par1World){
		super(par1World);
		this.setAIMoveSpeed(0.2f);
		this.hostileSpeed = 0.25F;
		//this.attackStrength = 4;
		this.setSize(0.8f, 2.5f);
		EntityExtension.For(this).setMagicLevelWithMana(15);
		initAI();
	}

	public void setOnGroudFloat(float onGround){
	}


	@Override
	public boolean isAIDisabled(){
		return false;
	}

	@Override
	public int getTotalArmorValue(){
		return 12;
	}

	@Override
	public void onUpdate(){
		if (this.world != null){
			if (this.world.isRemote){
			}else{
				if (EntityExtension.For(this).getCurrentMana() <= 0){
					this.attackEntityFrom(DamageSource.GENERIC, 500);
				}
			}
		}
		super.onUpdate();
	}
	
	@Override
	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
		if (getRNG().nextInt(10) == 0)
			this.entityDropItem(new ItemStack(ItemDefs.essence, 1, ArsMagicaAPI.getAffinityRegistry().getId(Affinity.ARCANE)), 0.0f);
		super.dropFewItems(wasRecentlyHit, lootingModifier);
	}
	
	@Override
	protected Item getDropItem(){
		return ItemDefs.manaCake;
	}

	@Override
	protected SoundEvent getAmbientSound(){
		return AMSounds.MANA_ELEMENTAL_IDLE;
	}

	@Override
	protected SoundEvent getHurtSound(){
		return AMSounds.MANA_ELEMENTAL_DEATH;
	}

	@Override
	protected SoundEvent getDeathSound(){
		return AMSounds.MANA_ELEMENTAL_DEATH;
	}

	private void initAI(){
		this.setPathPriority(PathNodeType.WATER, -1F);
		this.tasks.addTask(3, new EntityAIManaDrainBolt(this, this.hostileSpeed, 35, 1, 10));
		this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, this.getAIMoveSpeed()));
		this.tasks.addTask(7, new EntityAIWander(this, this.getAIMoveSpeed()));
		this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(8, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, 0, true, false, null));
	}

	@Override
	public boolean getCanSpawnHere(){
		if (!SpawnBlacklists.entityCanSpawnHere(this.getPosition(), world, this))
			return false;
		return super.getCanSpawnHere();
	}
}
