package am2.entity;

import java.util.List;

import am2.ArsMagica2;
import am2.api.DamageSources;
import am2.extensions.EntityExtension;
import am2.particles.AMParticle;
import am2.particles.ParticleArcToEntity;
import am2.particles.ParticleFadeOut;
import am2.particles.ParticleLeaveParticleTrail;
import am2.particles.ParticleMoveOnHeading;
import am2.utils.MathUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityManaVortex extends Entity{

	private float rotation;
	private float scale = 0.0f;

	public static final DataParameter<Integer> TICKS_TO_EXIST = EntityDataManager.createKey(EntityManaVortex.class, DataSerializers.VARINT);
	public static final DataParameter<Float> MANA_STOLEN = EntityDataManager.createKey(EntityManaVortex.class, DataSerializers.FLOAT);
	private boolean hasGoneBoom = false;

	public EntityManaVortex(World par1World){
		super(par1World);
	}

	@Override
	protected void entityInit(){
		this.dataManager.register(TICKS_TO_EXIST, 50 + rand.nextInt(250));
		this.dataManager.register(MANA_STOLEN, 0.0f);
	}

	public int getTicksToExist(){
		try{
			return this.dataManager.get(TICKS_TO_EXIST);
		}catch (Throwable t){
			return -1;
		}
	}

	@Override
	public void onUpdate(){
		this.ticksExisted++;
		this.rotation += 5;
		if (!this.world.isRemote && (this.isDead || this.ticksExisted >= getTicksToExist())){
			this.setDead();
			return;
		}

		if (this.getTicksToExist() - this.ticksExisted <= 20){
			this.scale -= 1f / 20f;
		}else if (this.scale < 0.99f){
			this.scale = (float)(Math.sin((float)this.ticksExisted / 50));
		}

		if (getTicksToExist() - this.ticksExisted <= 5 && !hasGoneBoom){
			hasGoneBoom = true;
			if (!world.isRemote){
				List<EntityLivingBase> players = world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().expand(3 + Math.floor(this.ticksExisted / 50), 2, 3 + Math.floor(this.ticksExisted / 50)));
				float damage = this.dataManager.get(MANA_STOLEN) * 0.005f;
				if (damage > 100)
					damage = 100;

				Object[] playerArray = players.toArray();
				for (Object o : playerArray){
					EntityLivingBase e = (EntityLivingBase)o;
					RayTraceResult mop = this.world.rayTraceBlocks(new Vec3d(this.posX, this.posY, this.posZ), new Vec3d(e.posX, e.posY + e.getEyeHeight(), e.posZ), false);
					if (mop == null)
						e.attackEntityFrom(DamageSources.causePhysicalDamage(this), damage);
				}
			}else{
				for (int i = 0; i < 360; i += ArsMagica2.config.FullGFX() ? 5 : ArsMagica2.config.LowGFX() ? 10 : 20){
					AMParticle effect = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "ember", this.posX, this.posY, this.posZ);
					if (effect != null){
						effect.setIgnoreMaxAge(true);
						effect.AddParticleController(new ParticleMoveOnHeading(effect, i, 0, 0.7f, 1, false));
						effect.setRGBColorF(0.24f, 0.24f, 0.8f);
						effect.AddParticleController(new ParticleFadeOut(effect, 1, false).setFadeSpeed(0.05f).setKillParticleOnFinish(true));
						effect.AddParticleController(
								new ParticleLeaveParticleTrail(effect, "ember", false, 5, 1, false)
										.addControllerToParticleList(new ParticleMoveOnHeading(effect, i, 0, 0.1f, 1, false))
										.addControllerToParticleList(new ParticleFadeOut(effect, 1, false).setFadeSpeed(0.1f).setKillParticleOnFinish(true))
										.setParticleRGB_F(0.24f, 0.24f, 0.8f)
										.addRandomOffset(0.2f, 0.2f, 0.2f)
						);
					}
				}
			}
		}

		if (getTicksToExist() - this.ticksExisted > 30){
			//get all players within 5 blocks
			List<EntityLivingBase> players = world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().expand(3 + Math.floor(this.ticksExisted / 50), 2, 3 + Math.floor(this.ticksExisted / 50)));
			Object[] playerArray = players.toArray();

			for (Object o : playerArray){
				EntityLivingBase e = (EntityLivingBase)o;

				RayTraceResult mop = this.world.rayTraceBlocks(new Vec3d(this.posX, this.posY, this.posZ), new Vec3d(e.posX, e.posY + e.getEyeHeight(), e.posZ), false);
				if (mop != null)
					continue;

				if (world.isRemote){
					if (ArsMagica2.config.NoGFX()){
						break;
					}
					if (ArsMagica2.config.LowGFX() && (this.ticksExisted % 4) != 0){
						break;
					}
					AMParticle effect = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "ember", e.posX, e.posY + (e.getEyeHeight() / 2), e.posZ);
					if (effect != null){
						effect.setRGBColorF(0.24f, 0.24f, 0.8f);
						effect.AddParticleController(new ParticleArcToEntity(effect, 1, this, false).generateControlPoints().setKillParticleOnFinish(true));
						effect.setIgnoreMaxAge(true);
					}
				}
				float manaStolen = EntityExtension.For(e).getMaxMana() * 0.01f;
				float curMana = EntityExtension.For(e).getCurrentMana();

				if (manaStolen > curMana)
					manaStolen = curMana;

				this.dataManager.set(MANA_STOLEN, this.dataManager.get(MANA_STOLEN) + manaStolen);
				EntityExtension.For(e).setCurrentMana(EntityExtension.For(e).getCurrentMana() - manaStolen);

				Vec3d movement = MathUtilities.GetMovementVectorBetweenEntities(e, this);
				float speed = -0.075f;

				e.addVelocity(movement.xCoord * speed, movement.yCoord * speed, movement.zCoord * speed);
			}
		}
	}

	public float getManaStolenPercent(){
		float damage = this.dataManager.get(MANA_STOLEN) * 0.005f;
		if (damage > 100)
			damage = 100;
		return damage / 100f;
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound var1){
		dataManager.set(TICKS_TO_EXIST, var1.getInteger("ticksToExist"));
		dataManager.set(MANA_STOLEN, var1.getFloat("manaStolen"));
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound var1){
		var1.setInteger("ticksToExist", this.getTicksToExist());
		var1.setFloat("manaStolen", this.dataManager.get(MANA_STOLEN));
	}

	public float getRotation(){
		return this.rotation;
	}

	public float getScale(){
		return this.scale;
	}

	public float getManaStolen(){
		return this.dataManager.get(MANA_STOLEN);
	}
}
