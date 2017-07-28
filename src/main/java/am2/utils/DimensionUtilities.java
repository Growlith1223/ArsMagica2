package am2.utils;

import java.util.ArrayList;

import am2.blocks.tileentity.TileEntityAstralBarrier;
import am2.defs.BlockDefs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class DimensionUtilities{
	public static void doDimensionTransfer(EntityLivingBase entity, int dimension){

		if (entity instanceof EntityPlayerMP){
			EntityPlayerMP player = (EntityPlayerMP)entity;
			new AMTeleporter(player.mcServer.worldServerForDimension(dimension)).teleport(entity);
		}else{
			entity.world.profiler.startSection("changeDimension");
			MinecraftServer minecraftserver = FMLCommonHandler.instance().getMinecraftServerInstance();
			int j = entity.dimension;
			WorldServer worldserver = minecraftserver.worldServerForDimension(j);
			WorldServer worldserver1 = minecraftserver.worldServerForDimension(dimension);
			entity.dimension = dimension;
			entity.world.removeEntity(entity);
			entity.isDead = false;
			entity.world.profiler.startSection("reposition");
			minecraftserver.getPlayerList().transferEntityToWorld(entity, j, worldserver, worldserver1, new AMTeleporter(worldserver1));
			entity.world.profiler.endStartSection("reloading");
			Entity e = EntityList.createEntityByID(EntityList.getID(entity.getClass()), worldserver1);

			if (e != null){
				e.readFromNBT(entity.writeToNBT(new NBTTagCompound()));
				worldserver1.spawnEntity(e);
			}

			entity.isDead = true;
			entity.world.profiler.endSection();
			worldserver.resetUpdateEntityTick();
			worldserver1.resetUpdateEntityTick();
			entity.world.profiler.endSection();
		}
	}

	public static TileEntityAstralBarrier GetBlockingAstralBarrier(World world, BlockPos pos, ArrayList<Long> keys){
		//check for Astral Barrier
		for (int i = -20; i <= 20; ++i){
			for (int j = -20; j <= 20; ++j){
				for (int k = -20; k <= 20; ++k){
					if (world.getBlockState(pos.add(i, j, k)).getBlock() == BlockDefs.astralBarrier){

						TileEntity te = world.getTileEntity(pos.add(i, j, k));
						if (te == null || !(te instanceof TileEntityAstralBarrier)){
							continue;
						}
						TileEntityAstralBarrier barrier = (TileEntityAstralBarrier)te;

						long barrierKey = KeystoneUtilities.instance.getKeyFromRunes(barrier.getRunesInKey());
						if ((barrierKey != 0 && keys.contains(barrierKey)) || !barrier.IsActive()) continue;

						int sqDist = (int) pos.distanceSq(barrier.getPos());

						if (sqDist < (barrier.getRadius() * barrier.getRadius())) return barrier;
					}
				}
			}
		}
		return null;
	}
}
