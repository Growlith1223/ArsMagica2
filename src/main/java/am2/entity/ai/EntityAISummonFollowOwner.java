package am2.entity.ai;

import am2.utils.EntityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityAISummonFollowOwner extends EntityAIBase{
	private EntityCreature theSummon;
	private EntityLivingBase theOwner;
	World theWorld;
	private double moveSpeed;
	private PathNavigate petPathfinder;
	private int field_75343_h;
	float maxDist;
	float minDist;
	public EntityAISummonFollowOwner(EntityCreature host, double moveSpeed, float minDist, float maxDist){
		this.theSummon = host;
		this.theWorld = host.world;
		this.moveSpeed = moveSpeed;
		this.petPathfinder = host.getNavigator();
		this.minDist = minDist;
		this.maxDist = maxDist;
		this.setMutexBits(3);
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	public boolean shouldExecute(){
		EntityLivingBase entitylivingbase = getOwner();

		if (entitylivingbase == null){
			return false;
		}else if (this.theSummon.getDistanceSqToEntity(entitylivingbase) < (double)(this.minDist * this.minDist)){
			return false;
		}else{
			this.theOwner = entitylivingbase;
			return true;
		}
	}

	private EntityLivingBase getOwner(){
		int entityID = EntityUtils.getOwner(theSummon);
		if (entityID == -1) return null;
		Entity e = theSummon.world.getEntityByID(entityID);
		if (e instanceof EntityLivingBase)
			return (EntityLivingBase)e;
		return null;
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean continueExecuting(){
		return !this.petPathfinder.noPath() && this.theSummon.getDistanceSqToEntity(this.theOwner) > (double)(this.maxDist * this.maxDist);
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void startExecuting(){
		this.field_75343_h = 0;
//		this.field_75344_i = this.theSummon.getNavigator().getAvoidsWater();
//		this.theSummon.getNavigator().setAvoidsWater(false);
	}

	/**
	 * Resets the task
	 */
	public void resetTask(){
		this.theOwner = null;
		this.petPathfinder.clearPathEntity();
//		this.theSummon.getNavigator().setAvoidsWater(this.field_75344_i);
	}
	
	private boolean isEmptyBlock(BlockPos pos) {
		IBlockState iblockstate = this.theWorld.getBlockState(pos);
		Block block = iblockstate.getBlock();
		return block == Blocks.AIR ? true : !iblockstate.isFullCube();
	}
	
	/**
	 * Updates the task
	 */
	@SuppressWarnings("deprecation")
	public void updateTask() {
		this.theSummon.getLookHelper().setLookPositionWithEntity(this.theOwner, 10.0F,(float) this.theSummon.getVerticalFaceSpeed());
		if (--this.field_75343_h <= 0) {
			this.field_75343_h = 10;

			if (!this.petPathfinder.tryMoveToEntityLiving(this.theOwner, this.moveSpeed * 3)) {
				if (!this.theSummon.getLeashed()) {
					if (this.theSummon.getDistanceSqToEntity(this.theOwner) >= 144.0D) {
						int i = MathHelper.floor(this.theOwner.posX) - 2;
						int j = MathHelper.floor(this.theOwner.posZ) - 2;
						int k = MathHelper.floor(this.theOwner.getEntityBoundingBox().minY);

						for (int l = 0; l <= 4; ++l) {
							for (int i1 = 0; i1 <= 4; ++i1) {
								if ((l < 1 || i1 < 1 || l > 3 || i1 > 3)
										&& this.theWorld.getBlockState(new BlockPos(i + l, k - 1, j + i1))
												.isFullyOpaque()
										&& this.isEmptyBlock(new BlockPos(i + l, k, j + i1))
										&& this.isEmptyBlock(new BlockPos(i + l, k + 1, j + i1))) {
									this.theSummon.setLocationAndAngles((double) ((float) (i + l) + 0.5F), (double) k,
											(double) ((float) (j + i1) + 0.5F), this.theOwner.rotationYaw,
											this.theOwner.rotationPitch);
									this.petPathfinder.clearPathEntity();
									return;
								}
							}
						}
					}
				}
			}
		}
	}
}
