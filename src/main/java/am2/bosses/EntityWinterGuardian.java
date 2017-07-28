package am2.bosses;

import java.util.List;

import am2.ArsMagica2;
import am2.api.ArsMagicaAPI;
import am2.api.DamageSources;
import am2.api.affinity.Affinity;
import am2.api.sources.DamageSourceFire;
import am2.api.sources.DamageSourceFrost;
import am2.bosses.ai.EntityAICastSpell;
import am2.bosses.ai.EntityAISmash;
import am2.bosses.ai.EntityAIStrikeAttack;
import am2.bosses.ai.EntityWinterGuardianLaunchArm;
import am2.bosses.ai.ISpellCastCallback;
import am2.buffs.BuffEffectFrostSlowed;
import am2.defs.AMSounds;
import am2.defs.ItemDefs;
import am2.packet.AMNetHandler;
import am2.particles.AMParticle;
import am2.particles.ParticleApproachEntity;
import am2.particles.ParticleFleeEntity;
import am2.particles.ParticleFloatUpward;
import am2.particles.ParticleOrbitEntity;
import am2.utils.NPCSpells;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.BossInfo.Color;
import net.minecraft.world.World;

public class EntityWinterGuardian extends AM2Boss{

	private boolean hasRightArm;
	private boolean hasLeftArm;
	private float orbitRotation;

	public EntityWinterGuardian(World par1World){
		super(par1World);
		this.setSize(1.25f, 3.25f);
		hasRightArm = true;
		hasLeftArm = true;
	}

	@Override
	protected void initSpecificAI(){
		this.tasks.addTask(1, new EntityAICastSpell<EntityWinterGuardian>(this, NPCSpells.instance.dispel, 16, 23, 50, BossActions.CASTING, new ISpellCastCallback<EntityWinterGuardian>(){
			@Override
			public boolean shouldCast(EntityWinterGuardian host, ItemStack spell){
				return host.getActivePotionEffects().size() > 0;
			}
		}));
		this.tasks.addTask(2, new EntityAISmash(this, 0.5f, DamageSources.DamageSourceTypes.FROST));
		this.tasks.addTask(3, new EntityAIStrikeAttack(this, 0.5f, 6f, DamageSources.DamageSourceTypes.FROST));
		this.tasks.addTask(4, new EntityWinterGuardianLaunchArm(this, 0.5f));
	}

	public void returnOneArm(){
		if (!hasLeftArm) hasLeftArm = true;
		else if (!hasRightArm) hasRightArm = true;
	}

	public void launchOneArm(){
		if (hasLeftArm) hasLeftArm = false;
		else if (hasRightArm) hasRightArm = false;
	}

	public boolean hasLeftArm(){
		return hasLeftArm;
	}

	public boolean hasRightArm(){
		return hasRightArm;
	}

	@Override
	protected void applyEntityAttributes(){
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(290D);
	}

	@Override
	public int getTotalArmorValue(){
		return 23;
	}

	@Override
	public void onUpdate(){
		if (world.getBiome(getPosition()).getEnableSnow() && world.getWorldInfo().isRaining()){
			if (world.isRemote){
				AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "ember", posX + (rand.nextFloat() * 6 - 3), posY + 2 + (rand.nextFloat() * 2 - 1), posZ + (rand.nextFloat() * 6 - 3));
				if (particle != null){
					particle.AddParticleController(new ParticleApproachEntity(particle, this, 0.15f, 0.1, 1, false));
					particle.setIgnoreMaxAge(false);
					particle.setMaxAge(30);
					particle.setParticleScale(0.35f);
					particle.setRGBColorF(0.7843f, 0.5098f, 0.5098f);
				}
			}else{
				this.heal(0.1f);
			}
		}

		if (world.isRemote){
			updateRotations();
			spawnParticles();
		}else{
			if (this.ticksExisted % 100 == 0){
				List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().expand(2, 2, 2));
				for (EntityLivingBase entity : entities){
					if (entity == this)
						continue;
					entity.addPotionEffect(new BuffEffectFrostSlowed(220, 1));
					entity.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("mining_fatigue"), 220, 3));
				}
			}
		}

		super.onUpdate();
	}

	@Override
	public void setCurrentAction(BossActions action){
		super.setCurrentAction(action);

		if (!world.isRemote){
			AMNetHandler.INSTANCE.sendActionUpdateToAllAround(this);
		}
	}

	private void updateRotations(){
		this.orbitRotation += 2f;
		this.orbitRotation %= 360;
	}

	private void spawnParticles(){
		for (int i = 0; i < ArsMagica2.config.getGFXLevel() * 4; ++i){
			int rnd = rand.nextInt(10);
			AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, rnd < 5 ? "snowflakes" : "ember", posX + (rand.nextFloat() * 0.4 - 0.2), posY + 2, posZ + (rand.nextFloat() * 0.4 - 0.2));
			if (particle != null){
				if (rnd < 2 || rnd > 8){
					particle.AddParticleController(new ParticleOrbitEntity(particle, this, 0.2f, 1, false));
				}else{
					particle.AddParticleController(new ParticleFloatUpward(particle, 0.5f, -0.2f, 1, false));
					particle.AddParticleController(new ParticleFleeEntity(particle, this, 0.06f, 2, 2, false).setKillParticleOnFinish(true));
				}
				particle.setIgnoreMaxAge(false);
				particle.setMaxAge(30);
				particle.setParticleScale(rnd < 5 ? 0.15f : 0.35f);
				particle.setRGBColorF(0.5098f, 0.7843f, 0.7843f);
			}
		}
	}

	public float getOrbitRotation(){
		return this.orbitRotation;
	}

	@Override
	protected void dropFewItems(boolean par1, int par2){
		if (par1)
			this.entityDropItem(new ItemStack(ItemDefs.infinityOrb, 1, 2), 0.0f);

		int i = rand.nextInt(4);

		for (int j = 0; j < i; j++){
			this.entityDropItem(new ItemStack(ItemDefs.essence, 1, ArsMagicaAPI.getAffinityRegistry().getId(Affinity.ICE)), 0.0f);
		}
		i = rand.nextInt(10);

		if (i < 3){
			this.entityDropItem(ItemDefs.winterArmEnchanted.copy(), 0.0f);
		}
	}

	@Override
	protected float modifyDamageAmount(DamageSource source, float damageAmt){
		if (source instanceof DamageSourceFrost)
			damageAmt = 0;
		if (source.isFireDamage() || source instanceof DamageSourceFire)
			damageAmt *= 2;
		return damageAmt;
	}

	@Override
	protected SoundEvent getHurtSound(){
		return AMSounds.WINTER_GUARDIAN_HIT;
	}

	@Override
	protected SoundEvent getDeathSound(){
		return AMSounds.WINTER_GUARDIAN_DEATH;
	}

	@Override
	protected SoundEvent getAmbientSound(){
		return AMSounds.WINTER_GUARDIAN_IDLE;
	}

	@Override
	public SoundEvent getAttackSound(){
		return AMSounds.WINTER_GUARDIAN_ATTACK;
	}

	@Override
	protected Color getBarColor() {
		return Color.RED;
	}
}
