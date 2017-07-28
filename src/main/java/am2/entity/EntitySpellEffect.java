package am2.entity;

import java.util.ArrayList;
import java.util.List;

import am2.particles.*;
import com.google.common.base.Optional;

import am2.ArsMagica2;
import am2.api.DamageSources;
import am2.api.spell.SpellModifier;
import am2.api.spell.SpellModifiers;
import am2.buffs.BuffEffectFrostSlowed;
import am2.spell.modifier.Colour;
import am2.utils.AMLineSegment;
import am2.utils.AffinityShiftUtils;
import am2.utils.DummyEntityPlayer;
import am2.utils.MathUtilities;
import am2.utils.SpellUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntitySpellEffect extends Entity{

	private float rotation;
	private final float rotationSpeed;

	private int ticksToEffect = 20;
	private int maxTicksToEffect = 20;
	private int maxTicksToEffect_wall = 5;

	private int ticksToExist = 100;

	private ItemStack spellStack;
	private EntityPlayer dummycaster;
	private int casterEntityID;
	private float moveSpeed;    //used by waves only

	private static final DataParameter<ItemStack> WATCHER_STACK = EntityDataManager.createKey(EntitySpellEffect.class, DataSerializers.OPTIONAL_ITEM_STACK);
	private static final DataParameter<Float> WATCHER_RADIUS = EntityDataManager.createKey(EntitySpellEffect.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> WATCHER_GRAVITY = EntityDataManager.createKey(EntitySpellEffect.class, DataSerializers.FLOAT);
	private static final DataParameter<Integer> WATCHER_TYPE = EntityDataManager.createKey(EntitySpellEffect.class, DataSerializers.VARINT); //0 == zone, 1 == rain of fire, 2 == blizzard, 3 == wall, 4 == wave
	private static final DataParameter<Boolean> WATCHER_ROF_IGNITE = EntityDataManager.createKey(EntitySpellEffect.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Float> WATCHER_DAMAGEBONUS = EntityDataManager.createKey(EntitySpellEffect.class, DataSerializers.FLOAT);

	private static final int TYPE_ZONE = 0;
	private static final int TYPE_ROF = 1;
	private static final int TYPE_BLIZ = 2;
	private static final int TYPE_WALL = 3;
	private static final int TYPE_WAVE = 4;

	private boolean firstApply = true;

	public EntitySpellEffect(World par1World){
		super(par1World);
		this.rotation = 0;
		this.rotationSpeed = 10f;
		this.setSize(0.25f, 0.25f);
	}

	public void SetCasterAndStack(EntityLivingBase caster, ItemStack spellScroll){
		this.spellStack = spellScroll;
		this.dummycaster = DummyEntityPlayer.fromEntityLiving(caster);
		casterEntityID = caster.getEntityId();
		if (spellStack != null)
			this.dataManager.set(WATCHER_STACK, (spellStack));
	}
	
	public void setRadius(float newRadius){
		this.dataManager.set(WATCHER_RADIUS, newRadius);
	}

	public void setTickRate(int newTickRate){
		this.maxTicksToEffect = newTickRate;
	}

	public void setTicksToExist(int ticks){
		this.ticksToExist = ticks;
	}

	public void setGravity(double gravity){
		dataManager.set(WATCHER_GRAVITY, (float)gravity);
	}

	public void setDamageBonus(float damageBonus){
		this.dataManager.set(WATCHER_DAMAGEBONUS, damageBonus);
	}

	public float getRotation(){
		return this.rotation;
	}

	public void setWall(float rotation){
		this.setRotation(rotation, 0);
		this.dataManager.set(WATCHER_TYPE, TYPE_WALL);
	}

	public void setWave(float rotation, float speed){
		this.setRotation(rotation, 0);
		this.dataManager.set(WATCHER_TYPE, TYPE_WAVE);
		this.moveSpeed = speed;
		this.stepHeight = 0.6f;
		maxTicksToEffect_wall = 1;
	}

	@Override
	protected void entityInit(){
		this.dataManager.register(WATCHER_RADIUS, 3f);
		this.dataManager.register(WATCHER_STACK, (new ItemStack(Items.GOLDEN_APPLE)));
		this.dataManager.register(WATCHER_GRAVITY, 0F);
		this.dataManager.register(WATCHER_TYPE, 0);
		this.dataManager.register(WATCHER_ROF_IGNITE, false);
		this.dataManager.register(WATCHER_DAMAGEBONUS, 1.0f);
	}

	@Override
	public void onUpdate(){
		
		if (dummycaster != null && dummycaster instanceof DummyEntityPlayer)
			dummycaster.onUpdate();

		switch (this.dataManager.get(WATCHER_TYPE)){
		case TYPE_ZONE:
			zoneUpdate();
			break;
		case TYPE_ROF:
			rainOfFireUpdate();
			break;
		case TYPE_BLIZ:
			blizzardUpdate();
			break;
		case TYPE_WALL:
			wallUpdate();
			break;
		case TYPE_WAVE:
			waveUpdate();
			break;
		}

		if (!world.isRemote && this.ticksExisted >= this.ticksToExist){
			this.setDead();
		}
	}

	@Override
	public void setDead(){
		if (dummycaster instanceof DummyEntityPlayer)
			dummycaster.setDead();
		super.setDead();
	}

	private void zoneUpdate(){
		if (this.world.isRemote){
			if (!ArsMagica2.config.NoGFX()){
				this.rotation += this.rotationSpeed;
				this.rotation %= 360;

				double dist = getRadius();
				double _rotation = rotation;

				if (spellStack == null){
					spellStack = getEffectStack();
					if (spellStack == null){
						return;
					}
				}
				spellStack = spellStack.copy();

				int color = 0xFFFFFF;
				if (SpellUtils.modifierIsPresent(SpellModifiers.COLOR, spellStack)){
					ArrayList<SpellModifier> mods = SpellUtils.getModifiersForStage(spellStack, -1);
					for (SpellModifier mod : mods){
						if (mod instanceof Colour){
							color = (int)mod.getModifier(SpellModifiers.COLOR, null, null, null, spellStack.getTagCompound());
						}
					}
				}

				if ((ArsMagica2.config.FullGFX() && this.ticksExisted % 2 == 0) || this.ticksExisted % 8 == 0){
					for (int i = 0; i < 4; ++i){
						_rotation = (rotation + (90 * i)) % 360;
						double x = this.posX - Math.cos(3.141 / 180 * (_rotation)) * dist;
						double z = this.posZ - Math.sin(3.141 / 180 * (_rotation)) * dist;
						AMParticle effect = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, AMParticleDefs.getParticleForAffinity(AffinityShiftUtils.getMainShiftForStack(spellStack)), x, posY, z);
						if (effect != null){
							effect.setIgnoreMaxAge(false);
							effect.setMaxAge(20);
							effect.setParticleScale(0.15f);
							effect.setRGBColorI(color);
							effect.AddParticleController(new ParticleFloatUpward(effect, 0, 0.07f, 1, false));
							if (ArsMagica2.config.LowGFX()){
								effect.AddParticleController(new ParticleOrbitPoint(effect, posX, posY, posZ, 2, false).setIgnoreYCoordinate(true).SetOrbitSpeed(0.05f).SetTargetDistance(dist).setRotateDirection(true));
							}
						}
					}
				}
			}
		}
		
		this.move(MoverType.SHULKER, 0, (float)this.dataManager.get(WATCHER_GRAVITY), 0);

		ticksToEffect--;
		if (spellStack == null){
			if (!world.isRemote){
				this.setDead();
			}
			return;
		}
		if (dummycaster == null){
			dummycaster = DummyEntityPlayer.fromEntityLiving(new EntityDummyCaster(world));
		}
		if (ticksToEffect <= 0){
			ticksToEffect = maxTicksToEffect;
			float radius = this.dataManager.get(WATCHER_RADIUS);
			List<Entity> possibleTargets = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(posX - radius, posY - 3, posZ - radius, posX + radius, posY + 3, posZ + radius));
			for (Entity e : possibleTargets){
				if (e instanceof EntityDragonPart && ((EntityDragonPart)e).entityDragonObj instanceof EntityLivingBase)
					e = (EntityLivingBase)((EntityDragonPart)e).entityDragonObj;

				if (e instanceof EntityLivingBase)
					//SpellUtils.applyStageToEntity(spellStack, dummycaster, world, e, false);
					SpellUtils.applyStackStage(spellStack.copy(), dummycaster, (EntityLivingBase) e, e.posX, e.posY - 1, e.posZ, null, world, false, false, this.ticksExisted);
			}
			if (this.dataManager.get(WATCHER_GRAVITY) < 0 && !firstApply)
				SpellUtils.applyStackStage(spellStack.copy(), dummycaster, null, posX, posY - 1, posZ, null, world, false, false, this.ticksExisted);
			else
				SpellUtils.applyStackStage(spellStack.copy(), dummycaster, null, posX, posY, posZ, null, world, false, false, this.ticksExisted);
			firstApply = false;
			for (float i = -radius; i <= radius; i++) {
				for (int j = -3; j <= 3; j++) {
					Vec3d[] blocks = getAllBlockLocationsBetween(new Vec3d(posX + i, posY + j, posZ - radius), new Vec3d(posX + i, posY + j, posZ + radius));
					for (Vec3d vec : blocks) {
						SpellUtils.applyStageToGround(spellStack.copy(), dummycaster, world, new BlockPos(vec), EnumFacing.UP, vec.xCoord + 0.5, vec.yCoord + 0.5, vec.zCoord + 0.5, false);
					}
				}
			}
		}
	}

	private void rainOfFireUpdate(){
		float radius = this.dataManager.get(WATCHER_RADIUS);
		if (world.isRemote){

			if (spellStack == null){
				spellStack = getEffectStack();
				if (spellStack == null){
					return;
				}
			}
			spellStack = spellStack.copy();

			int color = 0xFFFFFF;
			if (SpellUtils.modifierIsPresent(SpellModifiers.COLOR, spellStack)){
				ArrayList<SpellModifier> mods = SpellUtils.getModifiersForStage(spellStack, -1);
				for (SpellModifier mod : mods){
					if (mod instanceof Colour){
						color = (int)mod.getModifier(SpellModifiers.COLOR, null, null, null, spellStack.getTagCompound());
					}
				}
			}

			for (int i = 0; i < 10; ++i){
				double x = this.posX - radius + (rand.nextDouble() * radius * 2);
				double z = this.posZ - radius + (rand.nextDouble() * radius * 2);
				double y = this.posY + 10;

				AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "explosion_2", x, y, z);
				if (particle != null){
					particle.setMaxAge(20);
					particle.addVelocity(rand.nextDouble() * 0.2f, 0, rand.nextDouble() * 0.2f);
					particle.setAffectedByGravity();
					particle.setDontRequireControllers();
					particle.setRGBColorI(color);
				}
			}

			//TODO: SoundHelper.instance.loopSound(world, (float)posX, (float)posY, (float)posZ, "arsmagica2:spell.loop.fire", 1.0f);
		}else{
			List<Entity> possibleTargets = world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(posX - radius, posY - 1, posZ - radius, posX + radius, posY + 3, posZ + radius));
			for (Entity e : possibleTargets){
				if (e != dummycaster){
					if (e instanceof EntityDragonPart && ((EntityDragonPart)e).entityDragonObj instanceof EntityLivingBase)
						e = (EntityLivingBase)((EntityDragonPart)e).entityDragonObj;

					double lastVelX = e.motionX;
					double lastVelY = e.motionY;
					double lastVelZ = e.motionZ;

					float damage = 0.75f * this.dataManager.get(WATCHER_DAMAGEBONUS);

					if (SpellUtils.attackTargetSpecial(null, e, DamageSources.causeFireDamage(dummycaster), damage) && !(e instanceof EntityPlayer))
						e.hurtResistantTime = 10;
					e.addVelocity(-(e.motionX - lastVelX), -(e.motionY - lastVelY), -(e.motionZ - lastVelZ));
				}
			}
			if (canRoFIgnite() && rand.nextInt(10) < 2){
				int pX = (int)(posX - radius + rand.nextInt((int)Math.ceil(radius) * 2));
				int pY = (int)posY;
				int pZ = (int)(posZ - radius + rand.nextInt((int)Math.ceil(radius) * 2));
				if (world.isAirBlock(new BlockPos(pX, pY, pZ)))
					world.setBlockState(new BlockPos(pX, pY, pZ), Blocks.FIRE.getDefaultState());
			}
			
		}
	}

	private void blizzardUpdate(){
		float radius = this.dataManager.get(WATCHER_RADIUS);
		if (world.isRemote){

			if (spellStack == null){
				spellStack = getEffectStack();
				if (spellStack == null){
					return;
				}
			}
			spellStack = spellStack.copy();

			int color = 0xFFFFFF;
			if (SpellUtils.modifierIsPresent(SpellModifiers.COLOR, spellStack)){
				ArrayList<SpellModifier> mods = SpellUtils.getModifiersForStage(spellStack, -1);
				for (SpellModifier mod : mods){
					if (mod instanceof Colour){
						color = (int)mod.getModifier(SpellModifiers.COLOR, null, null, null, spellStack.getTagCompound());
					}
				}
			}

			for (int i = 0; i < 20; ++i){
				double x = this.posX - radius + (rand.nextDouble() * radius * 2);
				double z = this.posZ - radius + (rand.nextDouble() * radius * 2);
				double y = this.posY + 10;

				AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "snowflakes", x, y, z);
				if (particle != null){
					particle.setMaxAge(20);
					particle.setParticleScale(0.1f);
					particle.addVelocity(rand.nextDouble() * 0.2f - 0.1f, 0, rand.nextDouble() * 0.2f - 0.1f);
					particle.setAffectedByGravity();
					particle.setRGBColorI(color);
					particle.setDontRequireControllers();
				}
			}

			double x = this.posX - radius + (rand.nextDouble() * radius * 2);
			double z = this.posZ - radius + (rand.nextDouble() * radius * 2);
			double y = this.posY + rand.nextDouble();
			AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "smoke", x, y, z);
			if (particle != null){
				particle.setParticleScale(2.0f);
				particle.setMaxAge(20);
				//particle.setRGBColorF(0.5f, 0.92f, 0.92f);
				particle.setRGBColorF(0.5098f, 0.7843f, 0.7843f);
				particle.SetParticleAlpha(0.6f);
				particle.AddParticleController(new ParticleFleePoint(particle, new Vec3d(x, y, z), 0.1f, 3f, 1, false));
			}

			//TODO: SoundHelper.instance.loopSound(world, (float)posX, (float)posY, (float)posZ, "arsmagica2:spell.loop.air", 1.0f);
		}else{
			List<Entity> possibleTargets = world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(posX - radius, posY - 1, posZ - radius, posX + radius, posY + 3, posZ + radius));
			for (Entity e : possibleTargets){
				if (e != dummycaster){
					if (e instanceof EntityDragonPart && ((EntityDragonPart)e).entityDragonObj instanceof EntityLivingBase)
						e = (EntityLivingBase)((EntityDragonPart)e).entityDragonObj;

					if (e instanceof EntityLivingBase)
						((EntityLivingBase)e).addPotionEffect(new BuffEffectFrostSlowed(80, 3));

					float damage = 1 * this.dataManager.get(WATCHER_DAMAGEBONUS);

					double lastVelX = e.motionX;
					double lastVelY = e.motionY;
					double lastVelZ = e.motionZ;
					if (SpellUtils.attackTargetSpecial(null, e, DamageSources.causeFrostDamage(dummycaster), damage) && !(e instanceof EntityPlayer))
						e.hurtResistantTime = 15;
					e.addVelocity(-(e.motionX - lastVelX), -(e.motionY - lastVelY), -(e.motionZ - lastVelZ));
				}
			}

			if (rand.nextInt(10) < 2){
				int pX = (int)(posX - radius + rand.nextInt((int)Math.ceil(radius) * 2));
				int pY = (int)posY + rand.nextInt(2);
				int pZ = (int)(posZ - radius + rand.nextInt((int)Math.ceil(radius) * 2));
				BlockPos pos = new BlockPos(pX, pY, pZ);
				if (world.isAirBlock(pos) && !world.isAirBlock(pos.down()) && world.getBlockState(pos).isOpaqueCube())
					world.setBlockState(pos, Blocks.SNOW.getDefaultState());
			}
		}
	}

	private void wallUpdate(){
		if (world.isRemote){
			if (spellStack == null){
				spellStack = getEffectStack();
				if (spellStack == null){
					return;
				}
			}
			spellStack = spellStack.copy();

			double dist = getRadius();

			int color = 0xFFFFFF;
			if (SpellUtils.modifierIsPresent(SpellModifiers.COLOR, spellStack)){
				ArrayList<SpellModifier> mods = SpellUtils.getModifiersForStage(spellStack, -1);
				for (SpellModifier mod : mods){
					if (mod instanceof Colour){
						color = (int)mod.getModifier(SpellModifiers.COLOR, null, null, null, spellStack.getTagCompound());
					}
				}
			}

			double px = Math.cos(3.141 / 180 * (rotationYaw + 90)) * 0.1f;
			double pz = Math.sin(3.141 / 180 * (rotationYaw + 90)) * 0.1f;
			double py = 0.1f;

			for (float i = 0; i < dist; i += 0.5f){
				double x = this.posX - Math.cos(3.141 / 180 * (rotationYaw)) * i;
				double z = this.posZ - Math.sin(3.141 / 180 * (rotationYaw)) * i;

				AMParticle effect = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, AMParticleDefs.getParticleForAffinity(AffinityShiftUtils.getMainShiftForStack(spellStack)), x, posY, z);
				if (effect != null){
					effect.setIgnoreMaxAge(false);
					effect.setMaxAge(20);
					effect.addRandomOffset(1, 1, 1);
					effect.setParticleScale(0.15f);
					effect.setRGBColorI(color);
					if (dataManager.get(WATCHER_TYPE) == TYPE_WALL){
						effect.AddParticleController(new ParticleFloatUpward(effect, 0, 0.07f, 1, false));
					}else{
						effect.setAffectedByGravity();
						effect.setDontRequireControllers();
						effect.addVelocity(px, py, pz);
					}
				}

				x = this.posX - Math.cos(Math.toRadians(rotationYaw)) * -i;
				z = this.posZ - Math.sin(Math.toRadians(rotationYaw)) * -i;

				effect = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, AMParticleDefs.getParticleForAffinity(AffinityShiftUtils.getMainShiftForStack(spellStack)), x, posY, z);
				if (effect != null){
					effect.setIgnoreMaxAge(false);
					effect.addRandomOffset(1, 1, 1);
					effect.setMaxAge(20);
					effect.setParticleScale(0.15f);
					effect.setRGBColorI(color);
					if (dataManager.get(WATCHER_TYPE) == TYPE_WALL){
						effect.AddParticleController(new ParticleFloatUpward(effect, 0, 0.07f, 1, false));
					}else{
						effect.setAffectedByGravity();
						effect.setDontRequireControllers();
						effect.addVelocity(px, py, pz);
					}
				}
			}

		}else{

			ticksToEffect--;
			if (spellStack == null){
				if (!world.isRemote){
					this.setDead();
				}
				return;
			}

			if (dummycaster == null){
				dummycaster = DummyEntityPlayer.fromEntityLiving(new EntityDummyCaster(world));
			}
			if (ticksToEffect <= 0){
				ticksToEffect = maxTicksToEffect_wall;
				float radius = this.dataManager.get(WATCHER_RADIUS);
				List<Entity> possibleTargets = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(posX - radius, posY - 1, posZ - radius, posX + radius, posY + 3, posZ + radius));
				for (Entity e : possibleTargets){
					if (e == this || e == dummycaster || e.getEntityId() == casterEntityID) continue;

					if (e instanceof EntityDragonPart && ((EntityDragonPart)e).entityDragonObj instanceof EntityLivingBase)
						e = (EntityLivingBase)((EntityDragonPart)e).entityDragonObj;

					Vec3d target = new Vec3d(e.posX, e.posY, e.posZ);

					double dirX = Math.cos(3.141 / 180 * (rotationYaw));
					double dirZ = Math.sin(3.141 / 180 * (rotationYaw));

					Vec3d a = new Vec3d(this.posX - dirX * radius, this.posY, this.posZ - dirZ * radius);
					Vec3d b = new Vec3d(this.posX - dirX * -radius, this.posY, this.posZ - dirZ * -radius);

					Vec3d closest = new AMLineSegment(a, b).closestPointOnLine(target);

					closest = new Vec3d(closest.xCoord, 0, closest.zCoord);
					target = new Vec3d(target.xCoord, 0, target.zCoord);

					double hDistance = closest.distanceTo(target);
					double vDistance = Math.abs(this.posY - e.posY);
					
					if (e instanceof EntityLivingBase && hDistance < 0.75f && vDistance < 2){
						//commented out in favor of line below so as to apply subsequent shapes as well
						//uncomment and comment out below line to revert to direct target only, but mark wave/wall as terminus
						//SpellUtils.applyStageToEntity(spellStack, dummycaster, world, e, false);
						SpellUtils.applyStackStage(spellStack.copy(), dummycaster, (EntityLivingBase)e, this.posX, this.posY, this.posZ, null, world, false, false, 0);
					}
				}
			}
		}
	}

	private void waveUpdate(){
		ticksToEffect = 0;
		wallUpdate();
		double dx = Math.cos(Math.toRadians(this.rotationYaw + 90));
		double dz = Math.sin(Math.toRadians(this.rotationYaw + 90));

		this.move(MoverType.SHULKER, dx * moveSpeed, 0, dz * moveSpeed);

		double dxH = Math.cos(Math.toRadians(this.rotationYaw));
		double dzH = Math.sin(Math.toRadians(this.rotationYaw));

		float radius = this.dataManager.get(WATCHER_RADIUS);

		for (int j = -1; j <= 1; j++) {
			Vec3d a = new Vec3d((this.posX + dx) - dxH * radius, this.posY + j, (this.posZ + dz) - dzH * radius);
			Vec3d b = new Vec3d((this.posX + dx) - dxH * -radius, this.posY + j, (this.posZ + dz) - dzH * -radius);
	
			if (dummycaster == null){
				dummycaster = DummyEntityPlayer.fromEntityLiving(new EntityDummyCaster(world));
			}
	
			Vec3d[] vecs = getAllBlockLocationsBetween(a, b);
			for (Vec3d vec : vecs){
				SpellUtils.applyStageToGround(SpellUtils.popStackStage(spellStack.copy()), dummycaster, world, new BlockPos(vec), EnumFacing.UP, vec.xCoord + 0.5, vec.yCoord + 0.5, vec.zCoord + 0.5, false);
			}
		}

	}

	private Vec3d[] getAllBlockLocationsBetween(Vec3d a, Vec3d b){
		a = MathUtilities.floorToI(a);
		b = MathUtilities.floorToI(b);

		double stepX = a.xCoord < b.xCoord ? 0.2f : -0.2f;
		double stepZ = a.zCoord < b.zCoord ? 0.2f : -0.2f;
		ArrayList<Vec3d> vecList = new ArrayList<Vec3d>();
		Vec3d curPos = new Vec3d(a.xCoord, a.yCoord, a.zCoord);
		for (int i = 0; i < this.height; ++i){
			vecList.add(new Vec3d(curPos.xCoord, curPos.yCoord + i, curPos.zCoord));
		}

		while (stepX != 0 || stepZ != 0){
			if ((stepX < 0 && curPos.xCoord <= b.xCoord) || (stepX > 0 && curPos.xCoord >= b.xCoord))
				stepX = 0;
			if ((stepZ < 0 && curPos.zCoord <= b.zCoord) || (stepZ > 0 && curPos.zCoord >= b.zCoord))
				stepZ = 0;
			curPos = new Vec3d(curPos.xCoord + stepX, curPos.yCoord, curPos.zCoord + stepZ);
			Vec3d tempPos = curPos.add(Vec3d.ZERO);
			tempPos = MathUtilities.roundToI(tempPos);
			if (!vecList.contains(tempPos)){
				for (int i = 0; i < this.height; ++i){
					vecList.add(new Vec3d(tempPos.xCoord, tempPos.yCoord + i, tempPos.zCoord));
				}
			}
		}

		return vecList.toArray(new Vec3d[vecList.size()]);
	}

	@Override
	public void onEntityUpdate(){
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound var1){
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound var1){
	}

	public void setRainOfFire(boolean ignite){
		this.dataManager.set(WATCHER_TYPE, TYPE_ROF);
		if (ignite)
			this.dataManager.set(WATCHER_ROF_IGNITE, true);
	}

	public void setBlizzard(){
		this.dataManager.set(WATCHER_TYPE, TYPE_BLIZ);
	}

	public boolean isBlizzard(){
		return this.dataManager.get(WATCHER_TYPE) == TYPE_BLIZ;
	}

	public boolean isRainOfFire(){
		return this.dataManager.get(WATCHER_TYPE) == TYPE_ROF;
	}

	@Override
	public boolean canBePushed(){
		return false;
	}

	@Override
	public boolean canBeCollidedWith(){
		return false;
	}
	
	public double getRadius() {
		return (double) dataManager.get(WATCHER_RADIUS);
	}
	
	public ItemStack getEffectStack() {
		return dataManager.get(WATCHER_STACK);
	}
	
	public int getType() {
		return dataManager.get(WATCHER_TYPE);
	}
	
	public double getGravity() {
		return (double) dataManager.get(WATCHER_GRAVITY);
	}
	
	public float getBonusDamage() {
		return dataManager.get(WATCHER_DAMAGEBONUS);
	}
	
	public boolean canRoFIgnite() {
		return dataManager.get(WATCHER_ROF_IGNITE);
	}
}
