package am2.entity;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;

import am2.ArsMagica2;
import am2.api.DamageSources;
import am2.api.spell.SpellModifier;
import am2.api.spell.SpellModifiers;
import am2.blocks.BlockArsMagicaOre;
import am2.blocks.BlockArsMagicaOre.EnumOreType;
import am2.defs.BlockDefs;
import am2.defs.ItemDefs;
import am2.packet.AMNetHandler;
import am2.particles.AMParticle;
import am2.particles.ParticleChangeSize;
import am2.particles.ParticleColorShift;
import am2.particles.ParticleHoldPosition;
import am2.spell.modifier.Colour;
import am2.utils.MathUtilities;
import am2.utils.SpellUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityThrownRock extends EntityLiving{

	private EntityLivingBase throwingEntity;
	private int maxTicksToExist;
	private Vec3d target = null;
	private float damage;

	private static final DataParameter<Boolean> IS_MOONSTONE_METEOR = EntityDataManager.createKey(EntityThrownRock.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> IS_SHOOTING_STAR = EntityDataManager.createKey(EntityThrownRock.class, DataSerializers.BOOLEAN);
	private static final DataParameter<ItemStack> SPELL_STACK = EntityDataManager.createKey(EntityThrownRock.class, DataSerializers.OPTIONAL_ITEM_STACK);

	public EntityThrownRock(World par1World){
		super(par1World);
		ticksExisted = 0;
		maxTicksToExist = 120;
		this.noClip = true;
	}

	public void setMoonstoneMeteor(){
		this.dataManager.set(IS_MOONSTONE_METEOR, true);
	}

	public void setShootingStar(float damage){
		this.dataManager.set(IS_SHOOTING_STAR, true);
		this.damage = damage;
	}

	public void setMoonstoneMeteorTarget(Vec3d target){
		this.target = target;
	}

	public boolean getIsMoonstoneMeteor(){
		return dataManager.get(IS_MOONSTONE_METEOR);
	}

	public boolean getIsShootingStar(){
		return dataManager.get(IS_SHOOTING_STAR) ;
	}

	public EntityThrownRock(World world, EntityLivingBase entityLiving, double projectileSpeed){
		super(world);
		this.noClip = true;
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
		maxTicksToExist = 100;
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

	public void setThrowingEntity(EntityLivingBase thrower){
		this.throwingEntity = thrower;
	}

	@Override
	protected void entityInit(){
		super.entityInit();
		this.dataManager.register(IS_MOONSTONE_METEOR, false);
		this.dataManager.register(IS_SHOOTING_STAR, false);
		this.dataManager.register(SPELL_STACK, (new ItemStack(ItemDefs.spell)));
	}

	private ItemStack getSpellStack(){
		return this.dataManager.get(SPELL_STACK);
	}

	public void setSpellStack(ItemStack spellStack){
		this.dataManager.set(SPELL_STACK, (spellStack));
	}

	@Override
	protected boolean canDespawn(){
		return !getIsMoonstoneMeteor() && !getIsShootingStar();
	}

	@Override
	public void onUpdate(){
		super.onUpdate();
		if (this.target != null && this.posY > this.target.yCoord){
			double deltaX = this.posX - target.xCoord;
			double deltaY = this.posY - target.yCoord;
			double deltaZ = this.posZ - target.zCoord;

			double angle = Math.atan2(deltaZ, deltaX);

			double hDist = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

			double vAngle = Math.atan2(deltaY, hDist);

			motionX = -Math.cos(angle) * 0.2;
			motionZ = -Math.sin(angle) * 0.2;
			motionY = -Math.sin(vAngle) * 2.5;
		}

		if (!getIsMoonstoneMeteor() && !getIsShootingStar()){
			if (!world.isRemote && (throwingEntity == null || throwingEntity.isDead)){
				setDead();
			}else{
				ticksExisted++;
				int maxTicksToLive = maxTicksToExist > -1 ? maxTicksToExist : 100;
				if (ticksExisted >= maxTicksToLive && !world.isRemote){
					setDead();
					return;
				}
			}
		}

		if (getIsShootingStar()){
			motionY -= 0.1f;
			if (motionY < -2f)
				motionY = -2f;
		}

		if (world.isRemote){
			if (getIsMoonstoneMeteor()){
				AMParticle fire = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "explosion_2", posX, posY, posZ);
				if (fire != null){
					fire.setMaxAge(20);
					fire.setRGBColorF(1, 1, 1);
					fire.setParticleScale(2.0f);
					fire.AddParticleController(new ParticleHoldPosition(fire, 20, 1, false));
					fire.AddParticleController(new ParticleColorShift(fire, 1, false).SetShiftSpeed(0.1f).SetColorTarget(0.01f, 0.01f, 0.01f).SetEndOnReachingTargetColor().setKillParticleOnFinish(false));
				}
			}else if (getIsShootingStar()){

				int color = -1;
				if (getSpellStack() != null){
					if (SpellUtils.modifierIsPresent(SpellModifiers.COLOR, getSpellStack())){
						ArrayList<SpellModifier> mods = SpellUtils.getModifiersForStage(getSpellStack(), -1);
						for (SpellModifier mod : mods){
							if (mod instanceof Colour){
								color = (int)mod.getModifier(SpellModifiers.COLOR, null, null, null, getSpellStack().getTagCompound());
							}
						}
					}
				}

				for (float i = 0; i < Math.abs(motionY); i += 0.1f){
					AMParticle star = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "ember", posX + motionX * i, posY + motionY * i, posZ + motionZ * i);
					if (star != null){
						star.setMaxAge(22);
						float clrMod = Minecraft.getMinecraft().world.rand.nextFloat();
						int finalColor = -1;
						if (color == -1)
							finalColor = MathUtilities.colorFloatsToInt(0.24f * clrMod, 0.58f * clrMod, 0.71f * clrMod);
						else{
							float[] colors = MathUtilities.colorIntToFloats(color);
							for (int c = 0; c < colors.length; ++c)
								colors[c] = colors[c] * clrMod;
							finalColor = MathUtilities.colorFloatsToInt(colors[0], colors[1], colors[2]);
						}
						star.setRGBColorI(finalColor);
						star.AddParticleController(new ParticleHoldPosition(star, 20, 1, false));
						star.AddParticleController(new ParticleChangeSize(star, 0.5f, 0.05f, 20, 1, false));
					}
				}
			}
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
			Entity entity1 = (Entity)list.get(j);
			if (!entity1.canBeCollidedWith() || entity1.isEntityEqual(throwingEntity) && ticksExisted < 25){
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
		}
		if (movingobjectposition != null){
			HitObject(movingobjectposition);
		}

		posX += motionX;
		posY += motionY;
		posZ += motionZ;
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
		setPosition(posX, posY, posZ);
	}
	
	
	
	protected void HitObject(RayTraceResult movingobjectposition){
		if (world.isRemote){
			return;
		}
		
		
		if (getIsShootingStar()){
			AMNetHandler.INSTANCE.sendStarImpactToClients(posX, posY + ((movingobjectposition.typeOfHit == RayTraceResult.Type.ENTITY) ? -movingobjectposition.entityHit.getEyeHeight() : 1.5f), posZ, world, this.getSpellStack());
			List<EntityLivingBase> ents = world.getEntitiesWithinAABB(EntityLivingBase.class, getEntityBoundingBox().expand(12, 5, 12));
			this.posY++;
			for (EntityLivingBase e : ents){
				if (e == throwingEntity) continue;
				if (this.getDistanceToEntity(e) < 12 && this.canEntityBeSeen(e))
					SpellUtils.attackTargetSpecial(null, e, DamageSources.causeMagicDamage(throwingEntity), damage);
			}
		}else{

			if (movingobjectposition.entityHit != null && movingobjectposition.entityHit instanceof EntityLivingBase){
				if (movingobjectposition.entityHit == throwingEntity || throwingEntity == null) return;
				if (throwingEntity != null){
					movingobjectposition.entityHit.attackEntityFrom(DamageSource.causeMobDamage(throwingEntity), 10);
				}
			}else if (movingobjectposition.typeOfHit == RayTraceResult.Type.BLOCK){
				if (this.getIsMoonstoneMeteor()){

					if (this.target == null){
						this.target = movingobjectposition.hitVec;
					}
					this.world.newExplosion(this, this.target.xCoord, this.target.yCoord, this.target.zCoord, 0.8f, false, ArsMagica2.config.moonstoneMeteorsDestroyTerrain());

					int numOres = rand.nextInt(4) + 1;

					for (int i = 0; i < numOres; ++i){
						generateSurfaceOreAtOffset(world, new BlockPos(target), i == 0);
					}

//					if (this.world.isRemote){
//						for (Object player : world.playerEntities)
//							if (((EntityPlayer)player).getDistanceSqToEntity(this) < 4096)
//								CompendiumUnlockHandler.unlockEntry("moonstone_meteors");
//					}
				}
			}
		}
		this.setDead();
	}

	private void generateSurfaceOreAtOffset(World world, BlockPos pos, boolean force){
		pos = pos.east(rand.nextInt(4) - 2);
		pos = pos.south(rand.nextInt(4) - 2);

		while (!world.isAirBlock(pos) && pos.getY() < world.getActualHeight())
			pos = pos.up();

		if (rand.nextInt(4) < 2 || force)
			world.setBlockState(pos, BlockDefs.ores.getDefaultState().withProperty(BlockArsMagicaOre.ORE_TYPE, EnumOreType.MOONSTONE));
		else
			world.setBlockState(pos, Blocks.STONE.getDefaultState());
	}

	@Override
	public boolean attackEntityFrom(DamageSource par1DamageSource, float par2){
		return false;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound par1nbtTagCompound){
		super.readEntityFromNBT(par1nbtTagCompound);

		this.damage = par1nbtTagCompound.getFloat("star_damage");
		if (par1nbtTagCompound.getBoolean("MoonstoneMeteor"))
			this.setMoonstoneMeteor();
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound par1nbtTagCompound){
		super.writeEntityToNBT(par1nbtTagCompound);

		par1nbtTagCompound.setFloat("star_damage", damage);
		par1nbtTagCompound.setBoolean("MoonstoneMeteor", getIsMoonstoneMeteor());
	}
}
