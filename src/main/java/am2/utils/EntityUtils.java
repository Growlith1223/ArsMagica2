package am2.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import am2.ArsMagica2;
import am2.api.math.AMVector3;
import am2.blocks.tileentity.TileEntitySummoner;
import am2.entity.ai.EntityAIGuardSpawnLocation;
import am2.entity.ai.EntityAISummonFollowOwner;
import am2.entity.ai.selectors.SummonEntitySelector;
import am2.extensions.EntityExtension;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class EntityUtils {
	
	private static final HashMap<Integer, ArrayList<EntityAITasks.EntityAITaskEntry>> storedTasks = new HashMap<>();
	private static final HashMap<Integer, ArrayList<EntityAITasks.EntityAITaskEntry>> storedAITasks = new HashMap<>();
	private static final String isSummonKey = "AM2_Entity_Is_Made_Summon";
//	private static final String summonEntityIDs = "AM2_Summon_Entity_IDs";
	private static final String summonDurationKey = "AM2_Summon_Duration";
	private static final String summonOwnerKey = "AM2_Summon_Owner";
	private static Method ptrSetSize = null;
	private static final String summonTileXKey = "AM2_Summon_Tile_X";
	private static final String summonTileYKey = "AM2_Summon_Tile_Y";
	private static final String summonTileZKey = "AM2_Summon_Tile_Z";
	
	
	public static int getLevelFromXP(float totalXP){
		int level = 0;
		int xp = (int)Math.floor(totalXP);
		do{
			int cap = xpBarCap(level);
			xp -= cap;
			if (xp < 0)
				break;
			level++;
		}while (true);

		return level;
	}
	
	public static Entity getPointedEntity(World world, EntityLivingBase entityplayer, double range, double collideRadius, boolean nonCollide, boolean targetWater){
		Entity pointedEntity = null;
		double d = range;
		Vec3d vec3d = new Vec3d(entityplayer.posX, entityplayer.posY + entityplayer.getEyeHeight(), entityplayer.posZ);
		Vec3d vec3d1 = entityplayer.getLookVec();
		Vec3d vec3d2 = vec3d.addVector(vec3d1.xCoord * d, vec3d1.yCoord * d, vec3d1.zCoord * d);
		double f1 = collideRadius;
		List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(entityplayer, entityplayer.getEntityBoundingBox().addCoord(vec3d1.xCoord * d, vec3d1.yCoord * d, vec3d1.zCoord * d).expand(f1, f1, f1));

		double d2 = 0.0D;
		for (int i = 0; i < list.size(); i++){
			Entity entity = (Entity)list.get(i);
			RayTraceResult mop = world.rayTraceBlocks(
					new Vec3d(entityplayer.posX, entityplayer.posY + entityplayer.getEyeHeight(), entityplayer.posZ),
					new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ),
					targetWater, !targetWater, false);
			if (((entity.canBeCollidedWith()) || (nonCollide)) && mop == null){
				float f2 = Math.max(0.8F, entity.getCollisionBorderSize());
				AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox().expand(f2, f2, f2);
				RayTraceResult movingobjectposition = axisalignedbb.calculateIntercept(vec3d, vec3d2);
				if (axisalignedbb.isVecInside(vec3d)){
					if ((0.0D < d2) || (d2 == 0.0D)){
						pointedEntity = entity;
						d2 = 0.0D;
					}

				}else if (movingobjectposition != null){
					double d3 = vec3d.distanceTo(movingobjectposition.hitVec);
					if ((d3 < d2) || (d2 == 0.0D)){
						pointedEntity = entity;
						d2 = d3;
					}
				}
			}
		}
		return pointedEntity;
	}
	
	public static int getXPFromLevel(int level){
		int totalXP = 0;
		for(int i = 0; i < level; i++){
			totalXP += xpBarCap(i);
		}
		return totalXP;
	}
	
	public static int xpBarCap(int experienceLevel){
		return experienceLevel >= 30 ? 112 + (experienceLevel - 30) * 9 : (experienceLevel >= 15 ? 37 + (experienceLevel - 15) * 5 : 7 + experienceLevel * 2);//experienceLevel >= 30 ? 62 + (experienceLevel - 30) * 7 : (experienceLevel >= 15 ? 17 + (experienceLevel - 15) * 3 : 17);
	}
	
	public static boolean isAIEnabled(EntityCreature ent){
		return !ent.isAIDisabled();
	}
	
	public static void makeSummon_PlayerFaction(EntityCreature entityliving, EntityPlayer player, boolean storeForRevert){
		if (isAIEnabled(entityliving) && EntityExtension.For(player).getCurrentSummons() < EntityExtension.For(player).getMaxSummons()){
			if (storeForRevert)
				storedTasks.put(entityliving.getEntityId(), new ArrayList<EntityAITasks.EntityAITaskEntry>(entityliving.targetTasks.taskEntries));

			boolean addMeleeAttack = false;
			ArrayList<EntityAITaskEntry> toRemove = new ArrayList<EntityAITaskEntry>();
			for (Object task : entityliving.tasks.taskEntries){
				EntityAITaskEntry base = (EntityAITaskEntry)task;
				if (base.action instanceof EntityAIAttackMelee){
					toRemove.add(base);
					addMeleeAttack = true;
				}
			}

			entityliving.tasks.taskEntries.removeAll(toRemove);

			if (storeForRevert)
				storedAITasks.put(entityliving.getEntityId(), toRemove);

			if (addMeleeAttack){
				float speed = entityliving.getAIMoveSpeed();
				if (speed <= 0) speed = 1.0f;
				entityliving.tasks.addTask(3, new EntityAIAttackMelee(entityliving, speed, true));
			}
			
			entityliving.targetTasks.taskEntries.clear();

			entityliving.targetTasks.addTask(1, new EntityAIHurtByTarget(entityliving, true));
			entityliving.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityMob>(entityliving, EntityMob.class, 0, true, false, SummonEntitySelector.instance));
			entityliving.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntitySlime>(entityliving, EntitySlime.class, true));
			entityliving.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityGhast>(entityliving, EntityGhast.class, true));
			entityliving.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityShulker>(entityliving, EntityShulker.class, true));

			if (!entityliving.world.isRemote && entityliving.getAttackTarget() != null && entityliving.getAttackTarget() instanceof EntityPlayer)
				ArsMagica2.proxy.addDeferredTargetSet(entityliving, null);

			if (entityliving instanceof EntityTameable){
				((EntityTameable)entityliving).setTamed(true);
				((EntityTameable)entityliving).setOwnerId(player.getPersistentID());
			}

			entityliving.getEntityData().setBoolean(isSummonKey, true);
			EntityExtension.For(player).addSummon(entityliving);
		}
	}
	
	public static boolean isSummon(EntityLivingBase entityliving){
		return entityliving.getEntityData().getBoolean(isSummonKey);
	}
	
	public static void makeSummon_MonsterFaction(EntityCreature entityliving, boolean storeForRevert){
		if (isAIEnabled(entityliving)){
			if (storeForRevert)
				storedTasks.put(entityliving.getEntityId(), new ArrayList<EntityAITasks.EntityAITaskEntry>(entityliving.targetTasks.taskEntries));
			entityliving.targetTasks.taskEntries.clear();
			entityliving.targetTasks.addTask(1, new EntityAIHurtByTarget(entityliving, true));
			entityliving.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>(entityliving, EntityPlayer.class, true));
			if (!entityliving.world.isRemote && entityliving.getAttackTarget() != null && entityliving.getAttackTarget() instanceof EntityMob)
				ArsMagica2.proxy.addDeferredTargetSet(entityliving, null);

			entityliving.getEntityData().setBoolean(isSummonKey, true);
		}
	}
	
	public static void setOwner(EntityLivingBase entityliving, EntityLivingBase owner){
		if (owner == null){
			entityliving.getEntityData().removeTag(summonOwnerKey);
			return;
		}

		entityliving.getEntityData().setInteger(summonOwnerKey, owner.getEntityId());
		if (entityliving instanceof EntityCreature){
			float speed = entityliving.getAIMoveSpeed();
			if (speed <= 0) speed = 1.0f;
			((EntityCreature)entityliving).tasks.addTask(1, new EntityAISummonFollowOwner((EntityCreature)entityliving, speed, 10, 20));
		}
	}
	
	public static void setSummonDuration(EntityLivingBase entity, int duration){
		entity.getEntityData().setInteger(summonDurationKey, duration);
	}
	
	public static int getOwner(EntityLivingBase entityliving){
		if (!isSummon(entityliving)) return -1;
		Integer ownerID = entityliving.getEntityData().getInteger(summonOwnerKey);
		return ownerID == null ? -1 : ownerID;
	}

	public static boolean revertAI(EntityCreature entityliving){

		int ownerID = getOwner(entityliving);
		Entity owner = entityliving.world.getEntityByID(ownerID);
		if (owner != null && owner instanceof EntityLivingBase){
			EntityExtension.For((EntityLivingBase)owner).removeSummon();
			if (EntityExtension.For((EntityLivingBase)owner).isManaLinkedTo(entityliving)){
				EntityExtension.For((EntityLivingBase)owner).updateManaLink(entityliving);
			}
		}

		entityliving.getEntityData().setBoolean(isSummonKey, false);
		setOwner(entityliving, null);

		if (storedTasks.containsKey(entityliving.getEntityId())){
			entityliving.targetTasks.taskEntries.clear();
			entityliving.targetTasks.taskEntries.addAll(storedTasks.get(entityliving.getEntityId()));
			storedTasks.remove(entityliving.getEntityId());

			if (storedAITasks.get(entityliving.getEntityId()) != null){
				ArrayList<EntityAITaskEntry> toRemove = new ArrayList<EntityAITaskEntry>();
				for (Object task : entityliving.tasks.taskEntries){
					EntityAITaskEntry base = (EntityAITaskEntry)task;
					if (base.action instanceof EntityAIAttackMelee || base.action instanceof EntityAISummonFollowOwner){
						toRemove.add(base);
					}
				}

				entityliving.tasks.taskEntries.removeAll(toRemove);

				entityliving.tasks.taskEntries.addAll(storedAITasks.get(entityliving.getEntityId()));
				storedAITasks.remove(entityliving.getEntityId());
			}
			if (!entityliving.world.isRemote && entityliving.getAttackTarget() != null)
				ArsMagica2.proxy.addDeferredTargetSet(entityliving, null);
			if (entityliving instanceof EntityTameable){
				((EntityTameable)entityliving).setTamed(false);
			}
			return true;
		}

		return false;
	}

	public static boolean canBlockDamageSource(EntityLivingBase living, DamageSource damageSourceIn) {
		if (!damageSourceIn.isUnblockable()) {
			Vec3d vec3d = damageSourceIn.getDamageLocation();

			if (vec3d != null) {
				Vec3d vec3d1 = living.getLook(1.0F);
				Vec3d vec3d2 = vec3d.subtractReverse(new Vec3d(living.posX, living.posY, living.posZ)).normalize();
				vec3d2 = new Vec3d(vec3d2.xCoord, 0.0D, vec3d2.zCoord);

				if (vec3d2.dotProduct(vec3d1) < 0.0D) {
					return true;
				}
			}
		}
		return false;
	}

	public static void setSize(EntityLivingBase entityliving, float width, float height){
		if (entityliving.width == width && entityliving.height == height)
			return;
		if (ptrSetSize == null){
			try{
				ptrSetSize = ReflectionHelper.findMethod(Entity.class, entityliving, new String[]{"func_70105_a", "setSize"}, Float.TYPE, Float.TYPE);
			}catch (Throwable t){
				t.printStackTrace();
				return;
			}
		}
		if (ptrSetSize != null){
			try{
				ptrSetSize.setAccessible(true);
				ptrSetSize.invoke(entityliving, width, height);
				//entityliving.yOffset = entityliving.height * 0.8f;
			}catch (Throwable t){
				t.printStackTrace();
				return;
			}
		}
	}
	
	public static Vec3d correctLook(Vec3d vecIn, Entity entityIn) {
		if (entityIn instanceof EntityLivingBase && EntityExtension.For((EntityLivingBase) entityIn).isInverted()) {
			return new Vec3d(-vecIn.xCoord, -vecIn.yCoord, vecIn.zCoord);
		}
		return vecIn;
	}
	
	public static float correctEyePos(float floatIn, Entity entityIn) {
		float curHeight = floatIn;
		if (entityIn instanceof EntityPlayer && EntityExtension.For((EntityLivingBase) entityIn).isInverted()) {
			EntityPlayer player = (EntityPlayer) entityIn;
			curHeight = player.height - floatIn - 0.1f;
		}
		if (entityIn instanceof EntityLivingBase && EntityExtension.For((EntityLivingBase) entityIn).shrinkAmount != 0)
			curHeight *= EntityExtension.For((EntityLivingBase) entityIn).shrinkAmount;
		return curHeight;
	}
	
	public static boolean correctMouvement(float strafe, float forward, float friction, Entity entityIn) {
		if (entityIn instanceof EntityPlayer && EntityExtension.For((EntityLivingBase) entityIn).isInverted()) {
	        float f = strafe * strafe + forward * forward;
	        if (f >= 1.0E-4F)
	        {
	            f = MathHelper.sqrt(f);

	            if (f < 1.0F)
	            {
	                f = 1.0F;
	            }

	            f = friction / f;
	            strafe = strafe * f;
	            forward = forward * f;
	            float f1 = MathHelper.sin(-entityIn.rotationYaw * 0.017453292F);
	            float f2 = MathHelper.cos(-entityIn.rotationYaw * 0.017453292F);
	            entityIn.motionX += (double)(strafe * f2 - forward * f1);
	            entityIn.motionZ += (double)(forward * f2 + strafe * f1);
	        }
			return true;
		}
		return false;
	}
	
	public static void setTileSpawned(EntityLivingBase entityliving, TileEntitySummoner summoner){
		entityliving.getEntityData().setInteger(summonTileXKey, summoner.getPos().getX());
		entityliving.getEntityData().setInteger(summonTileYKey, summoner.getPos().getY());
		entityliving.getEntityData().setInteger(summonTileZKey, summoner.getPos().getZ());
	}
	
	public static void setGuardSpawnLocation(EntityCreature entity, double x, double y, double z){
		float speed = entity.getAIMoveSpeed();
		if (speed <= 0) speed = 1.0f;
		entity.tasks.addTask(1, new EntityAIGuardSpawnLocation(entity, speed, 3, 16, new AMVector3(x, y, z)));
	}
	
	public static int deductXP(int amount, EntityPlayer player) {
		int i = player.experienceTotal;

		if (amount > i) {
			amount = i;
		}
		player.experienceTotal -= amount;
		player.experience = 0;
		player.experienceLevel = 0;
		int addedXP = 0;
		while (addedXP < player.experienceTotal) {
			int toAdd = player.experienceTotal - addedXP;
			toAdd = Math.min(toAdd, player.xpBarCap());
			player.experience = toAdd / (float)player.xpBarCap();
			if (player.experience == 1f) {
				player.experienceLevel++;
				player.experience = 0;
			}
			addedXP += toAdd;
		}
		return amount;
	}

}
