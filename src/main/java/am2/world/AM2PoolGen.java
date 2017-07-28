package am2.world;

import java.util.Random;
import java.util.Set;

import am2.defs.BlockDefs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class AM2PoolGen{
	public void generate(World world, Random rand, BlockPos pos){
		pos = correctYCoord(world, pos);
		if (pos.getY() == -1) return;
		if (validPoolLocation(world, pos)){
			world.setBlockState(pos, BlockDefs.liquid_essence.getBlock().getDefaultState());
			world.setBlockState(pos.add(-1, 0, 0), BlockDefs.liquid_essence.getBlock().getDefaultState());
			world.setBlockState(pos.add(0, 0, -1), BlockDefs.liquid_essence.getBlock().getDefaultState());
			world.setBlockState(pos.add(-1, 0, -1), BlockDefs.liquid_essence.getBlock().getDefaultState());

			world.setBlockToAir(pos.up());
			world.setBlockToAir(pos.add(-1, 0, 0).up());
			world.setBlockToAir(pos.add(0, 0, -1).up());
			world.setBlockToAir(pos.add(-1, 0, -1).up());

			//AMCore.log.info("Generated pool at: %d %d %d", x, y ,z);
		}
	}

	private BlockPos correctYCoord(World world, BlockPos pos){
		while (pos.getY() > 0 && world.isAirBlock(pos)){
			pos = pos.down();
		}
		if (pos.getY() <= 0) return new BlockPos (0, -1, 0);

		while (pos.getY() < world.provider.getActualHeight() && !world.isAirBlock(pos.up())){
			pos = pos.up();
		}
		if (pos.getY() > world.provider.getActualHeight()) return new BlockPos (0, -1, 0);

		return pos;
	}

	private boolean validPoolLocation(World world, BlockPos pos){

		if (!biomeIsValid(world, pos)) return false;

		int radius = 2;

		IBlockState requiredBlock = world.getBiome(pos).topBlock;
		IBlockState alternateBlock = world.getBiome(pos).fillerBlock;

		if (requiredBlock == null || alternateBlock == null) return false;

		for (int i = -radius; i < radius; ++i){
			for (int k = -radius; k < radius; ++k){
				IBlockState blockBelow = world.getBlockState(pos.add(i, -1, k));
				IBlockState block = world.getBlockState(pos.add(i, 0, k));

				if (blockBelow == null || block == null) return false;
				if (!blockBelow.isOpaqueCube()) return false;
				if (block != requiredBlock && block != alternateBlock) return false;

				IBlockState blockAbove = world.getBlockState(pos.up());
				if (blockAbove == null) continue;
				if (blockAbove.isOpaqueCube()) return false;
				if (blockAbove.getMaterial().blocksMovement()) return false;
			}
		}

		return true;
	}

	private boolean biomeIsValid(World world, BlockPos pos){
		Biome biome = world.getBiome(pos);
		Set<BiomeDictionary.Type> types = BiomeDictionary.getTypes(biome);

		for (Type type : types){
			if (type == Type.END || type == Type.MUSHROOM || type == Type.NETHER || type == Type.WATER || type == Type.WASTELAND){
				return false;
			}
		}

		return true;
	}
}
