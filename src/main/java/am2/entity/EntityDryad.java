package am2.entity;

import am2.ArsMagica2;
import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import am2.bosses.BossSpawnHelper;
import am2.defs.ItemDefs;
import am2.particles.AMParticle;
import am2.particles.ParticleOrbitEntity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityDryad extends EntityCreature{

	public EntityDryad(World par1World){
		super(par1World);
		this.setPathPriority(PathNodeType.WATER, -1.0F);
		tasks.addTask(0, new EntityAISwimming(this));
		tasks.addTask(1, new EntityAIPanic(this, 0.68F));
		tasks.addTask(1, new EntityAITempt(this, getAIMoveSpeed(), Item.getItemFromBlock(Blocks.SAPLING), false));
		tasks.addTask(5, new EntityAIWander(this, 0.5F));
		tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6F));
		tasks.addTask(7, new EntityAILookIdle(this));
	}
	@Override
	public boolean canTriggerWalking(){
		return false;
	}


	@Override
	public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound){
		super.writeEntityToNBT(par1NBTTagCompound);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound){
		super.readEntityFromNBT(par1NBTTagCompound);
	}

	@Override
	protected Item getDropItem(){
		return null;
	}
	
	@Override
	public void onUpdate(){
		World world = this.world;
		super.onUpdate();
		if (!world.isRemote || world == null){
			return;
		}
		if (world.rand.nextInt(100) == 3){
			AMParticle effect = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "hr_sparkles_1", this.posX, this.posY + 2, this.posZ);
			if (effect != null){
				effect.AddParticleController(new ParticleOrbitEntity(effect, this, world.rand.nextDouble() * 0.2 + 0.2, 1, false));
				effect.setIgnoreMaxAge(false);
				effect.setRGBColorF(0.1f, 0.8f, 0.1f);
			}
		}
	}

	@Override
	protected boolean canDespawn(){
		return ArsMagica2.config.canDraydsDespawn();
	}

	@Override
	protected void dropFewItems(boolean par1, int par2){
		int i = rand.nextInt(1);

		for (int j = 0; j < i; j++){
			this.entityDropItem(new ItemStack(ItemDefs.essence, 1, ArsMagicaAPI.getAffinityRegistry().getId(Affinity.EARTH)), 0.0f);
		}

		i = rand.nextInt(10);

		if (i == 3){
			this.entityDropItem(new ItemStack(ItemDefs.essence, 1, ArsMagicaAPI.getAffinityRegistry().getId(Affinity.LIFE)), 0.0f);
		}
	}

	@Override
	public void onDeath(DamageSource par1DamageSource){
		if (par1DamageSource.getSourceOfDamage() instanceof EntityPlayer){
			BossSpawnHelper.instance.onDryadKilled(this);
		}
		super.onDeath(par1DamageSource);
	}

	@Override
	public boolean getCanSpawnHere(){
		if (!SpawnBlacklists.entityCanSpawnHere(this.getPosition(), world, this))
			return false;
		return super.getCanSpawnHere();
	}

	;
}
