package am2.entity;

import am2.ArsMagica2;
import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import am2.defs.ItemDefs;
import am2.entity.ai.EntityAIWaterElementalAttack;
import am2.extensions.EntityExtension;
import am2.particles.AMParticle;
import am2.particles.ParticleFadeOut;
import am2.particles.ParticleFloatUpward;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EntityWaterElemental extends EntityMob{

	private float hostileSpeed;

	public EntityWaterElemental(World par1World){
		super(par1World);
		this.hostileSpeed = 0.46F;
		initAI();
		EntityExtension.For(this).setCurrentLevel(5);
		EntityExtension.For(this).setCurrentMana(300);
	}

	private void initAI(){

		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(3, new EntityAIWaterElementalAttack(this, EntityPlayer.class, this.hostileSpeed, false));
		this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(8, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, 0, true, false, null));
	}

	@Override
	public void onUpdate(){
		if (this.world != null){
			if (this.world.isRemote){
				spawnLivingParticles();
			}
		}
		super.onUpdate();
	}
	
	@Override
	protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier) {
		this.entityDropItem(new ItemStack(ItemDefs.essence, 1, ArsMagicaAPI.getAffinityRegistry().getId(Affinity.WATER)), 0.0f);
	}

	private void spawnLivingParticles(){
		if (rand.nextBoolean()){
			double yPos = this.posY + 1.1;
			AMParticle effect = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "water_ball",
					this.posX + ((rand.nextFloat() * 0.2) - 0.1f),
					yPos,
					this.posZ + ((rand.nextFloat() * 0.4) - 0.2f));
			if (effect != null){
				effect.AddParticleController(new ParticleFloatUpward(effect, 0.1f, -0.06f, 1, false));
				effect.AddParticleController(new ParticleFadeOut(effect, 2, false).setFadeSpeed(0.04f));
				effect.setMaxAge(25);
				effect.setIgnoreMaxAge(false);
				effect.setParticleScale(0.1f);
			}
		}
	}

	/* Checks if this entity is inside water (if inWater field is true as a result of handleWaterMovement() returning
	 * true)
	 */
	@Override
	public boolean isInWater(){
		return this.world.handleMaterialAcceleration(this.getEntityBoundingBox().expand(0.0D, -0.6000000238418579D, 0.0D), Material.WATER, this);
	}

	@Override
	public boolean getCanSpawnHere(){
		if (!SpawnBlacklists.entityCanSpawnHere(this.getPosition(), world, this))
			return false;
		return super.getCanSpawnHere();
	}
}
