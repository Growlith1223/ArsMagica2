package am2.world;

import static net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.LAKE;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import am2.ArsMagica2;
import am2.blocks.BlockArsMagicaOre;
import am2.blocks.BlockArsMagicaOre.EnumOreType;
import am2.defs.BlockDefs;
import am2.entity.SpawnBlacklists;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.fml.common.IWorldGenerator;

public class AM2WorldDecorator implements IWorldGenerator{

	//ores
	private final WorldGenMinable vinteum;
	private final WorldGenMinable blueTopaz;
	private final WorldGenMinable chimerite;
	private final WorldGenMinable sunstone;

	//flowers
	private final AM2FlowerGen blueOrchid;
	private final AM2FlowerGen desertNova;
	private final AM2FlowerGen wakebloom;
	private final AM2FlowerGen aum;
	private final AM2FlowerGen tarmaRoot;

	private ArrayList<Integer> dimensionBlacklist = new ArrayList<Integer>();


	//trees
	private final WitchwoodTreeHuge witchwoodTree;

	//pools
	private final AM2PoolGen pools;
	public AM2WorldDecorator(){

		for (int i : ArsMagica2.config.getWorldgenBlacklist()){
			if (i == -1) continue;
			dimensionBlacklist.add(i);
		}

		vinteum = new WorldGenMinable(BlockDefs.ores.getDefaultState().withProperty(BlockArsMagicaOre.ORE_TYPE, EnumOreType.VINTEUM), 4, BlockMatcher.forBlock(Blocks.STONE));
		chimerite = new WorldGenMinable(BlockDefs.ores.getDefaultState().withProperty(BlockArsMagicaOre.ORE_TYPE, EnumOreType.CHIMERITE), 6, BlockMatcher.forBlock(Blocks.STONE));
		blueTopaz = new WorldGenMinable(BlockDefs.ores.getDefaultState().withProperty(BlockArsMagicaOre.ORE_TYPE, EnumOreType.BLUETOPAZ), 6, BlockMatcher.forBlock(Blocks.STONE));
		sunstone = new WorldGenMinable(BlockDefs.ores.getDefaultState().withProperty(BlockArsMagicaOre.ORE_TYPE, EnumOreType.SUNSTONE), 3, BlockMatcher.forBlock(Blocks.LAVA));

		blueOrchid = new AM2FlowerGen(BlockDefs.cerublossom);
		desertNova = new AM2FlowerGen(BlockDefs.desertNova);
		wakebloom = new AM2FlowerGen(BlockDefs.wakebloom);
		aum = new AM2FlowerGen(BlockDefs.aum);
		tarmaRoot = new AM2FlowerGen(BlockDefs.tarmaRoot);

		witchwoodTree = new WitchwoodTreeHuge(true);

		pools = new AM2PoolGen();

		new WorldGenEssenceLakes(BlockDefs.liquid_essence.getBlock());
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
			IChunkProvider chunkProvider){

		if (!SpawnBlacklists.worldgenCanHappenInDimension(world.provider.getDimension()))
			return;

		if (world.getWorldInfo().getTerrainType() == WorldType.FLAT) return;
		if (dimensionBlacklist.contains(world.provider.getDimension())) return;
		switch (world.provider.getDimension()){
		case -1:
			generateNether(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
			break;
		case 1:
			break;
		default:
			generateOverworld(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
		}
	}

	public void generateNether(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
			IChunkProvider chunkProvider){
		generateOre(sunstone, 20, world, random, 5, 120, chunkX, chunkZ);
	}

	public void generateOverworld(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
			IChunkProvider chunkProvider){
		generateOre(vinteum, 6, world, random, 10, 45, chunkX, chunkZ);
		generateOre(chimerite, 8, world, random, 10, 80, chunkX, chunkZ);
		generateOre(blueTopaz, 8, world, random, 10, 80, chunkX, chunkZ);
		generateOre(sunstone, 20, world, random, 5, 120, chunkX, chunkZ);

		generateFlowers(blueOrchid, world, random, chunkX, chunkZ);
		generateFlowers(desertNova, world, random, chunkX, chunkZ);
		generateFlowers(tarmaRoot, world, random, chunkX, chunkZ);

		Biome biome = world.getBiome(new BlockPos (chunkX << 4, 0, chunkZ << 4));
		Set<BiomeDictionary.Type> biomeTypes = BiomeDictionary.getTypes(biome);
		boolean typeValid = false;
		for (Type type : biomeTypes){
			if (type == Type.BEACH || type == Type.SWAMP || type == Type.JUNGLE || type == Type.PLAINS || type == Type.WATER){
				typeValid = true;
			}else if (type == Type.SNOWY){
				typeValid = false;
				break;
			}
		}

		if (biome != Biome.REGISTRY.getObject(new ResourceLocation("minecraft:ocean")) && typeValid && random.nextInt(10) < 7){
			generateFlowers(wakebloom, world, random, chunkX, chunkZ);
		}

		if (random.nextInt(35) == 0){
			generateTree(witchwoodTree, world, random, chunkX, chunkZ);
		}

		if (random.nextInt(25) == 0){
			generatePools(world, random, chunkX, chunkZ);
		}

		if ((BiomeDictionary.hasType(biome, Type.MAGICAL) || BiomeDictionary.hasType(biome, Type.FOREST)) && random.nextInt(4) == 0 && TerrainGen.populate(chunkGenerator, world, random, chunkX, chunkZ, true, LAKE)){
			int lakeGenX = (chunkX * 16) + random.nextInt(16) + 8;
			int lakeGenY = random.nextInt(128);
			int lakeGenZ = (chunkZ * 16) + random.nextInt(16) + 8;
			(new WorldGenEssenceLakes(BlockDefs.liquid_essence.getBlock())).generate(world, random, new BlockPos (lakeGenX, lakeGenY, lakeGenZ));
		}
	}

	private void generateFlowers(AM2FlowerGen flowers, World world, Random random, int chunkX, int chunkZ){
		int x = (chunkX << 4) + random.nextInt(16) + 8;
		int y = random.nextInt(128);
		int z = (chunkZ << 4) + random.nextInt(16) + 8;

		flowers.generate(world, random, new BlockPos (x, y, z));
	}

	private void generateOre(WorldGenMinable mineable, int amount, World world, Random random, int minY, int maxY, int chunkX, int chunkZ){
		for (int i = 0; i < amount; ++i){
			int x = (chunkX << 4) + random.nextInt(16);
			int y = random.nextInt(maxY - minY) + minY;
			int z = (chunkZ << 4) + random.nextInt(16);

			mineable.generate(world, random, new BlockPos (x, y, z));
		}
	}

	private void generateTree(WorldGenerator trees, World world, Random random, int chunkX, int chunkZ){
		int x = (chunkX * 16) + random.nextInt(16);
		int z = (chunkZ * 16) + random.nextInt(16);
		BlockPos y = world.getHeight(new BlockPos(x, 0, z));

		if (new WitchwoodTreeHuge(true).generate(world, random, y)){
			aum.generate(world, random, y);
		}
	}

	private void generatePools(World world, Random random, int chunkX, int chunkZ){
		int x = (chunkX * 16) + random.nextInt(16);
		int z = (chunkZ * 16) + random.nextInt(16);
		BlockPos y = world.getHeight(new BlockPos(x, 0, z));

		pools.generate(world, random, y);
	}
}
