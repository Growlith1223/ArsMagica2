package am2.entity.ai;

import am2.api.math.AMVector3;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityAIGuardSpawnLocation extends EntityAIBase{

	private final EntityCreature theGuard;
	World theWorld;
	private final float moveSpeed;
	private final PathNavigate guardPathfinder;
	private int field_48310_h;
	float maxDist;
	float minDist;
	private final AMVector3 spawnLocation;

	public EntityAIGuardSpawnLocation(EntityCreature par1EntityMob, float moveSpeed, float minDist, float maxDist, AMVector3 spawn){
		theGuard = par1EntityMob;
		theWorld = par1EntityMob.world;
		this.moveSpeed = moveSpeed;
		guardPathfinder = par1EntityMob.getNavigator();
		this.minDist = minDist;
		this.maxDist = maxDist;
		this.spawnLocation = spawn;
		setMutexBits(3);
	}

	public double getDistanceSqToSpawnXZ(){
		double d = theGuard.posX - spawnLocation.x;
		double d2 = theGuard.posZ - spawnLocation.z;
		return d * d + d2 * d2;
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	@Override
	public boolean shouldExecute(){
		if (getDistanceSqToSpawnXZ() < minDist * minDist){
			return false;
		}else{
			return true;
		}
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	@Override
	public boolean continueExecuting(){
		return !guardPathfinder.noPath() && getDistanceSqToSpawnXZ() > maxDist * maxDist;
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	@Override
	public void startExecuting(){
		field_48310_h = 0;
		theGuard.setPathPriority(PathNodeType.WATER, -1.0F);
	}

	/**
	 * Resets the task
	 */
	@Override
	public void resetTask(){
		guardPathfinder.clearPathEntity();
		theGuard.setPathPriority(PathNodeType.WATER, -1.0F);
	}

	/**
	 * Updates the task
	 */
	@Override
	public void updateTask(){
		theGuard.getLookHelper().setLookPosition(spawnLocation.x, spawnLocation.y, spawnLocation.z, 10F, theGuard.getVerticalFaceSpeed());


		if (--field_48310_h > 0){
			return;
		}

		field_48310_h = 10;


		if (guardPathfinder.tryMoveToXYZ(spawnLocation.x, spawnLocation.y, spawnLocation.z, moveSpeed)){
			return;
		}

		if (getDistanceSqToSpawnXZ() < 144D){
			return;
		}

		BlockPos pos = new BlockPos(spawnLocation.x - 2, spawnLocation.y, spawnLocation.z);

		for (int l = 0; l <= 4; l++){
			for (int i1 = 0; i1 <= 4; i1++){
				IBlockState otherBlock = theWorld.getBlockState(pos.add(l, 1, i1));
				if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && this.theWorld.isSideSolid(pos, EnumFacing.UP) && !otherBlock.isBlockNormalCube()){
					this.theGuard.setLocationAndAngles((double)((float)(pos.getX() + l) + 0.5F), (double)pos.getY(), (double)((float)(pos.getZ() + i1) + 0.5F), this.theGuard.rotationYaw, this.theGuard.rotationPitch);
					this.guardPathfinder.clearPathEntity();
					return;
				}
			}
		}
	}

}
