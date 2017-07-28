package am2.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class AMTeleporter extends Teleporter{

	private final WorldServer instance;

	public AMTeleporter(WorldServer par1WorldServer){
		super(par1WorldServer);
		instance = par1WorldServer;
	}

	public void teleport(EntityLivingBase entity){
		teleport(entity, instance);
	}

	// Move the Entity to the portal
	public void teleport(EntityLivingBase entity, World world){
		// Set Dimension
		if (entity.world.provider.getDimension() != world.provider.getDimension()){
			BlockPos teleportPos = clearTeleportPath(world, entity);

			entity.motionX = entity.motionY = entity.motionZ = 0.0D;
			entity.fallDistance = 0;
			entity.setPosition(teleportPos.getX() + 0.5F, teleportPos.getY(), teleportPos.getZ() + 0.5F);
			
			FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().transferPlayerToDimension((EntityPlayerMP)entity, world.provider.getDimension(), this);
		}

		//AMCore.log.info("Teleported to dim " + world.provider.dimensionId + ": " + teleportPos.x + "/" + teleportPos.y + "/" + teleportPos.z);
	}
	
	@Override
	public void placeInPortal(Entity entityIn, float rotationYaw) {}
	
	@Override
	public boolean placeInExistingPortal(Entity entityIn, float rotationYaw) {
		return false;
	}
	
	@Override
	public void removeStalePortalLocations(long p_85189_1_){
	}

	private BlockPos clearTeleportPath(World world, EntityLivingBase entity){
		BlockPos vec = new BlockPos(entity);
		vec = new BlockPos(vec.getX() / world.provider.getMovementFactor(),
		vec.getY() / world.provider.getMovementFactor(),
		vec.getZ() / world.provider.getMovementFactor());
		if (entity.dimension != -1){
			boolean canFindHigherGround = false;
			vec = new BlockPos(vec.getX(), entity.posY, vec.getZ());
			if (vec.getY() < 5 || vec.getY() >= world.getActualHeight() - 10)
				vec = new BlockPos(vec.getX(), 5, vec.getZ());

			while (true){
				if (world.getBlockState(vec).getBlock() == Blocks.AIR || vec.getY() >= world.getActualHeight()){
					canFindHigherGround = true;
					break;
				}
				vec = vec.up();
			}

			if (canFindHigherGround){
				while (world.getBlockState(vec.down()).getBlock() == Blocks.AIR && vec.getY() > 0){
					vec = vec.down();
				}
			}else{
				if (vec.getY() < 5)
					vec = new BlockPos(vec.getX(), 5, vec.getZ());
				if (vec.getY() > world.getActualHeight() - 10)
					vec = new BlockPos(vec.getX(), world.getActualHeight() - 10, vec.getZ());

				for (int q = (int)Math.floor(vec.getY()) - 2; q < vec.getY() + 1; ++q){
					for (int i = (int)Math.floor(vec.getX()) - 1; i < vec.getX() + 1; ++i){
						for (int k = (int)Math.floor(vec.getZ()) - 1; k < vec.getZ() + 1; ++k){
							if (q == (int)Math.floor(vec.getY() - 2)){
								world.setBlockState(new BlockPos(i, q, k), Blocks.AIR.getDefaultState());
							}
						}
					}
				}
			}
		}else if (entity.dimension == -1){
			boolean canFindHigherGround = false;
			while (true){
				if (world.getBlockState(vec).getBlock() == Blocks.AIR || vec.getY() >= 256){
					canFindHigherGround = true;
					break;
				}
				vec = vec.up();
			}

			if (!canFindHigherGround){
				for (int q = (int)Math.floor(vec.getY()) - 2; q < vec.getY() + 1; ++q){
					for (int i = (int)Math.floor(vec.getX()) - 1; i < vec.getX() + 1; ++i){
						for (int k = (int)Math.floor(vec.getZ()) - 1; k < vec.getZ() + 1; ++k){
							if (q == (int)Math.floor(vec.getY() - 2)){
								world.setBlockState(new BlockPos(i, q, k), Blocks.NETHERRACK.getDefaultState());
							}else{
								world.setBlockState(new BlockPos(i, q, k), Blocks.AIR.getDefaultState());
							}
						}
					}
				}
			}
		}
		return vec;
	}
}
