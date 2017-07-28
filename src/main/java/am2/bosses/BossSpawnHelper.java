package am2.bosses;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import am2.blocks.tileentity.TileEntityLectern;
import am2.defs.BlockDefs;
import am2.defs.ItemDefs;
import am2.entity.EntityDryad;
import am2.items.ItemOre;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BossSpawnHelper{
	public int dryadsKilled;
	public int ticksSinceLastDryadDeath;

	public static final BossSpawnHelper instance = new BossSpawnHelper();

	private final HashMap<EntityLivingBase, World> queuedBosses;

	private BossSpawnHelper(){
		queuedBosses = new HashMap<EntityLivingBase, World>();
		MinecraftForge.EVENT_BUS.register(this);
	}

	public void onDryadKilled(EntityDryad dryad){
		ticksSinceLastDryadDeath = 0;
		dryadsKilled++;
		if (dryadsKilled >= 5){
			spawnNatureGuardian(dryad.world, dryad.posX, dryad.posY, dryad.posZ);
			dryadsKilled = 0;
		}
	}

	@SubscribeEvent
	public void onVillagerChildKilled(LivingDeathEvent event){
		if (event.getEntity().getEntityWorld().isRemote || !(event.getEntity() instanceof EntityVillager))
			return;
		
		EntityVillager villager = (EntityVillager) event.getEntity();
		if (!villager.isChild())
			return;
		
		BlockPos pos = villager.getPosition();

		World world = villager.world;

		long time = world.getWorldTime() % 24000;
		if (time < 12500 || time > 23500) //night time
			return;

		int phase = getMoonPhaseProxiedProperly(world.provider.getWorldTime());
		if (phase != 0) // full moon
			return;

		//initial validation
		if (!world.isAirBlock(pos))
			return;

		//shift to center of circle
		if (world.getBlockState(pos.west()).getBlock() == BlockDefs.wizardChalk){
			pos = pos.west();
		}
		if (world.getBlockState(pos.east()).getBlock() == BlockDefs.wizardChalk){
			pos = pos.east();
		}
		if (world.getBlockState(pos.north()).getBlock() == BlockDefs.wizardChalk){
			pos = pos.north();
		}
		if (world.getBlockState(pos.south()).getBlock() == BlockDefs.wizardChalk){
			pos = pos.south();
		}

		if (!chalkCircleIsValid(world, pos))
			return;

		if (!world.isRemote){
			EntityLifeGuardian guardian = new EntityLifeGuardian(world);
			guardian.setPosition(pos.getX(), pos.getY(), pos.getZ());
			queuedBosses.put(guardian, world);
		}
	}

	private int getMoonPhaseProxiedProperly(long worldTime){
		return (int)(worldTime / 24000L) % 8;
	}

	private boolean chalkCircleIsValid(World world, BlockPos pos){
		//check for candles
		if (world.getBlockState(pos.east(3)).getBlock() != BlockDefs.wardingCandle)
			return false;
		if (world.getBlockState(pos.west(3)).getBlock() != BlockDefs.wardingCandle)
			return false;
		if (world.getBlockState(pos.north(3)).getBlock() != BlockDefs.wardingCandle)
			return false;
		if (world.getBlockState(pos.south(3)).getBlock() != BlockDefs.wardingCandle)
			return false;

		//check for chalk circle
		int xOff = -2;
		int zOff = -2;
		while (xOff <= 2)
			if (world.getBlockState(pos.add(xOff++, 0, zOff)).getBlock() != BlockDefs.wizardChalk)
				return false;
		xOff--;
		while (zOff <= 2)
			if (world.getBlockState(pos.add(xOff, 0, zOff++)).getBlock() != BlockDefs.wizardChalk)
				return false;
		zOff--;
		while (xOff >= -2)
			if (world.getBlockState(pos.add(xOff--, 0, zOff)).getBlock() != BlockDefs.wizardChalk)
				return false;
		xOff++;
		while (zOff >= -2)
			if (world.getBlockState(pos.add(xOff, 0, zOff--)).getBlock() != BlockDefs.wizardChalk)
				return false;

		return true;
	}

	private void spawnNatureGuardian(World world, double x, double y, double z){
		if (!world.isRemote){
			EntityNatureGuardian eng = new EntityNatureGuardian(world);
			eng.setPosition(x, y, z);
			queuedBosses.put(eng, world);
		}
	}

	public void tick(){
		ticksSinceLastDryadDeath++;
		if (ticksSinceLastDryadDeath >= 400){
			ticksSinceLastDryadDeath = 0;
			dryadsKilled = 0;
		}

		for (EntityLivingBase ent : queuedBosses.keySet()){
			World world = queuedBosses.get(ent);
			if (!world.isRemote){
				world.spawnEntity(ent);
				onBossSpawn(ent, world, ent.getPosition());
			}
		}
		queuedBosses.clear();
	}

	public void onItemInRing(EntityItem item, Block ringID){
		if (ringID == BlockDefs.redstoneInlay){
			checkForWaterGuardianSpawn(item.world, item.getPosition());
		}else if (ringID == BlockDefs.ironInlay){
			checkForArcaneGuardianSpawn(item.world, item.getPosition());
			checkForEarthGuardianSpawn(item.world, item.getPosition());
		}else if (ringID == BlockDefs.goldInlay){
			checkForAirGuardianSpawn(item.world, item.getPosition());
			checkForFireGuardianSpawn(item, item.world, item.getPosition());
			checkForEnderGuardianSpawn(item.world, item.getPosition());
		}
	}

	private void checkForWaterGuardianSpawn(World world, BlockPos pos){

		if (!world.isRaining()) return;

		Biome biome = world.getBiome(pos);
		Set<Type> types = BiomeDictionary.getTypes(biome);

		boolean containsWaterType = false;

		for (Type type : types){
			if (type == Type.WATER || type == Type.SWAMP || type == Type.BEACH || type == Type.OCEAN || type == Type.RIVER || type == Type.WET){
				containsWaterType = true;
				break;
			}
		}

		if (!containsWaterType) return;

		for (int i = -1; i <= 1; ++i)
			for (int j = -1; j <= 1; ++j)
				if (!world.canBlockSeeSky(pos.add(i, 1, j)))
					return;

		List<EntityItem> itemsInRange = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos, pos).expandXyz(1D));
		if (itemsInRange.size() != 2) return;
		boolean hasBucket = false;
		boolean hasBoat = false;

		for (EntityItem item : itemsInRange){
			if (item.isDead) continue;
			if (item.getEntityItem().getItem() == Items.BOAT)
				hasBoat = true;
			else if (item.getEntityItem().getItem() == Items.WATER_BUCKET)
				hasBucket = true;
		}

		if (hasBoat && hasBucket && !world.isRemote){
			for (EntityItem item : itemsInRange){
				item.setDead();
			}
			EntityWaterGuardian guardian = new EntityWaterGuardian(world);
			guardian.setPosition(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
			queuedBosses.put(guardian, world);
		}
	}

	private void checkForAirGuardianSpawn(World world, BlockPos pos){
		if (pos.getY() < 150) return;
		List<EntityItem> itemsInRange = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos, pos).expandXyz(1D));
		if (itemsInRange.size() != 1) return;
		if (itemsInRange.get(0).getEntityItem().getItem() != ItemDefs.essence || itemsInRange.get(0).getEntityItem().getItemDamage() != ArsMagicaAPI.getAffinityRegistry().getId(Affinity.AIR))
			return;

		itemsInRange.get(0).setDead();
		EntityAirGuardian guardian = new EntityAirGuardian(world);
		guardian.setPosition(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
		queuedBosses.put(guardian, world);
	}

	private void checkForArcaneGuardianSpawn(World world, BlockPos pos){
		List<EntityItem> itemsInRange = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos, pos).expandXyz(1D));
		if (itemsInRange.size() != 1) return;
		if (itemsInRange.get(0).getEntityItem().getItem() != ItemDefs.essence || itemsInRange.get(0).getEntityItem().getItemDamage() != ArsMagicaAPI.getAffinityRegistry().getId(Affinity.ARCANE))
			return;
		boolean hasStructure = false;
		TileEntityLectern lectern = null;
		//+z check
		if (world.getBlockState(pos.add(-1, 0, 2)).getBlock() == Blocks.BOOKSHELF &&
				world.getBlockState(pos.add(-1, 1, 2)).getBlock() == Blocks.BOOKSHELF &&
				world.getBlockState(pos.add(-1, 2, 2)).getBlock() == Blocks.BOOKSHELF &&
				world.getBlockState(pos.add(1, 0, 2)).getBlock() == Blocks.BOOKSHELF &&
				world.getBlockState(pos.add(1, 1, 2)).getBlock() == Blocks.BOOKSHELF &&
				world.getBlockState(pos.add(1, 2, 2)).getBlock() == Blocks.BOOKSHELF){
			if (world.getBlockState(pos.add(0, 0, 2)).getBlock() == BlockDefs.lectern){
				lectern = (TileEntityLectern)world.getTileEntity(pos.add(0, 0, 2));
				hasStructure = true;
			}
		}
		//-z check
		if (world.getBlockState(pos.add(-1, 0, -2)).getBlock() == Blocks.BOOKSHELF &&
				world.getBlockState(pos.add(-1, 1, -2)).getBlock() == Blocks.BOOKSHELF &&
				world.getBlockState(pos.add(-1, 2, -2)).getBlock() == Blocks.BOOKSHELF &&
				world.getBlockState(pos.add(1, 0, -2)).getBlock() == Blocks.BOOKSHELF &&
				world.getBlockState(pos.add(1, 1, -2)).getBlock() == Blocks.BOOKSHELF &&
				world.getBlockState(pos.add(1, 2, -2)).getBlock() == Blocks.BOOKSHELF){
			if (world.getBlockState(pos.add(0, 0, -2)).getBlock() == BlockDefs.lectern){
				lectern = (TileEntityLectern)world.getTileEntity(pos.add(0, 0, -2));
				hasStructure = true;
			}
		}
		//+x check
		if (world.getBlockState(pos.add(2, 0, -1)).getBlock() == Blocks.BOOKSHELF &&
				world.getBlockState(pos.add(2, 1, -1)).getBlock() == Blocks.BOOKSHELF &&
				world.getBlockState(pos.add(2, 2, -1)).getBlock() == Blocks.BOOKSHELF &&
				world.getBlockState(pos.add(2, 0, 1)).getBlock() == Blocks.BOOKSHELF &&
				world.getBlockState(pos.add(2, 1, 1)).getBlock() == Blocks.BOOKSHELF &&
				world.getBlockState(pos.add(2, 2, 1)).getBlock() == Blocks.BOOKSHELF){
			if (world.getBlockState(pos.add(2, 0, 0)).getBlock() == BlockDefs.lectern){
				lectern = (TileEntityLectern)world.getTileEntity(pos.add(2, 0, 0));
				hasStructure = true;
			}
		}
		//-x check
		if (world.getBlockState(pos.add(-2, 0, -1)).getBlock() == Blocks.BOOKSHELF &&
				world.getBlockState(pos.add(-2, 1, -1)).getBlock() == Blocks.BOOKSHELF &&
				world.getBlockState(pos.add(-2, 2, -1)).getBlock() == Blocks.BOOKSHELF &&
				world.getBlockState(pos.add(-2, 0, 1)).getBlock() == Blocks.BOOKSHELF &&
				world.getBlockState(pos.add(-2, 1, 1)).getBlock() == Blocks.BOOKSHELF &&
				world.getBlockState(pos.add(-2, 2, 1)).getBlock() == Blocks.BOOKSHELF){
			if (world.getBlockState(pos.add(-2, 0, 0)).getBlock() == BlockDefs.lectern){
				lectern = (TileEntityLectern)world.getTileEntity(pos.add(-2, 0, 0));
				hasStructure = true;
			}
		}

		if (hasStructure && lectern != null && lectern.hasStack() && lectern.getStack().getItem() == ItemDefs.arcaneCompendium){
			itemsInRange.get(0).setDead();
			EntityArcaneGuardian guardian = new EntityArcaneGuardian(world);
			guardian.setPosition(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
			queuedBosses.put(guardian, world);
		}
	}

	private void checkForEarthGuardianSpawn(World world, BlockPos pos){
		List<EntityItem> itemsInRange = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos, pos).expandXyz(1D));
		if (itemsInRange.size() != 3) return;
		boolean hasEmerald = false;
		boolean hasTopaz = false;
		boolean hasChimerite = false;
		boolean hasStructure = false;

		for (EntityItem item : itemsInRange){
			if (item.isDead) continue;
			if (item.getEntityItem().getItem() == Items.EMERALD)
				hasEmerald = true;
			else if (item.getEntityItem().getItem() == ItemDefs.itemOre && item.getEntityItem().getItemDamage() == ItemOre.META_BLUE_TOPAZ)
				hasTopaz = true;
			else if (item.getEntityItem().getItem() == ItemDefs.itemOre && item.getEntityItem().getItemDamage() == ItemOre.META_CHIMERITE)
				hasChimerite = true;
		}

		hasStructure = world.getBlockState(pos.down()) == Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.CHISELED);

		if (!hasStructure) return;

		for (int i = -1; i <= 1; ++i){
			for (int j = -1; j <= 1; ++j){
				if (i == 0 && j == 0) continue;
				hasStructure &= world.getBlockState(pos.add(i, -1, j)).getBlock() == Blocks.OBSIDIAN;
			}
		}
		
		hasStructure &= world.getBlockState(pos.add(-2, 0, 0)).getBlock() == BlockDefs.vinteumTorch;
		hasStructure &= world.getBlockState(pos.add(2, 0, 0)).getBlock() == BlockDefs.vinteumTorch;
		hasStructure &= world.getBlockState(pos.add(0, 0, -2)).getBlock() == BlockDefs.vinteumTorch;
		hasStructure &= world.getBlockState(pos.add(0, 0, 2)).getBlock() == BlockDefs.vinteumTorch;

		if (!world.isRemote && hasEmerald && hasTopaz && hasChimerite && hasStructure){
			for (EntityItem item : itemsInRange){
				item.setDead();
			}

			EntityEarthGuardian guardian = new EntityEarthGuardian(world);
			guardian.setPosition(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
			queuedBosses.put(guardian, world);
		}
	}

	private void checkForFireGuardianSpawn(EntityItem item, World world, BlockPos pos){
		if (item.getEntityItem().getItem() != ItemDefs.essence || item.getEntityItem().getItemDamage() != ArsMagicaAPI.getAffinityRegistry().getId(Affinity.WATER))
			return;
		boolean hasStructure = false;
		boolean hasDimension = world.provider.getDimension() == -1;

		hasStructure = world.getBlockState(pos.down()).getBlock() == Blocks.COAL_BLOCK;

		if (!hasStructure) return;

		for (int i = -1; i <= 1; ++i){
			for (int j = -1; j <= 1; ++j){
				if (i == 0 && j == 0) continue;
				hasStructure &= world.getBlockState(pos.add(i, -1, j)).getBlock() == Blocks.OBSIDIAN;
			}
		}

		if (!world.isRemote && hasStructure && hasDimension){
			item.setDead();

			EntityFireGuardian guardian = new EntityFireGuardian(world);
			guardian.setPosition(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
			queuedBosses.put(guardian, world);
		}
	}

	private void onBossSpawn(EntityLivingBase boss, World world, BlockPos pos){
		if (boss instanceof EntityEarthGuardian){
			if (world.getGameRules().getBoolean("mobGriefing")){
				for (int i = -10; i <= 10; ++i){
					for (int j = 0; j <= 4; ++j){
						for (int k = -10; k <= 10; ++k){
							if (world.getBlockState(pos.add(i, j, k)) != Blocks.BEDROCK)
								world.destroyBlock(pos, true);
						}
					}
				}
			}
		}else if (boss instanceof EntityFireGuardian){
			for (int i = -20; i <= 20; ++i){
				for (int k = -20; k <= 20; ++k){
					Block block = world.getBlockState(pos.add(i, -1, k)).getBlock();
					if (block == Blocks.LAVA || block == Blocks.FLOWING_LAVA)
						world.setBlockState(pos.add(i, -1, k), Blocks.NETHERRACK.getDefaultState(), 2);
				}
			}
		}
	}

	public void onIceEffigyBuilt(World world, BlockPos pos){
		Biome biome = world.getBiome(pos);
		Set<Type> types = BiomeDictionary.getTypes(biome);

		boolean containsIceType = false;

		for (Type type : types){
			if (type == Type.COLD){
				containsIceType = true;
				break;
			}
		}

		if (!containsIceType) return;

		for (int i = -1; i <= 1; ++i)
			for (int j = -1; j <= 1; ++j)
				if (i == 0 && j == 0) continue;
				else if (!world.canBlockSeeSky(pos.add(i, 1, j)))
					return;

		world.setBlockToAir(pos);
		world.setBlockToAir(pos.up());
		world.setBlockToAir(pos.up(2));

		EntityWinterGuardian guardian = new EntityWinterGuardian(world);
		guardian.setPosition(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
		world.spawnEntity(guardian);
	}

	public void onLightningEffigyBuilt(World world, BlockPos pos){
		for (int i = -1; i <= 1; ++i)
			for (int j = -1; j <= 1; ++j)
				if (i == 0 && j == 0) continue;
				else if (!world.canBlockSeeSky(pos.add(i, 1, j)))
					return;

		world.setBlockToAir(pos);
		world.setBlockToAir(pos.up());
		world.setBlockToAir(pos.up(2));

		EntityLightningGuardian guardian = new EntityLightningGuardian(world);
		guardian.setPosition(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
		world.spawnEntity(guardian);

		world.thunderingStrength = 1.0f;
	}

	public void checkForEnderGuardianSpawn(World world, BlockPos pos){
		if (world.provider.getDimension() != 1) return;

		for (int i = -1; i <= 1; ++i)
			for (int j = -1; j <= 1; ++j)
				if (i == 0 && j == 0) continue;
				else if (!world.canBlockSeeSky(pos.add(i, 1, j)))
					return;

		List<EntityItem> itemsInRange = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos, pos).expandXyz(1D));
		if (itemsInRange.size() != 2) return;

		boolean hasEnderEssence = false;
		boolean hasEyeofEnder = false;
		boolean hasStructure = false;

		for (EntityItem item : itemsInRange){
			if (item.isDead) continue;
			if (item.getEntityItem().getItem() == Items.ENDER_EYE)
				hasEyeofEnder = true;
			else if (item.getEntityItem().getItem() == ItemDefs.essence && item.getEntityItem().getItemDamage() == ArsMagicaAPI.getAffinityRegistry().getId(Affinity.ENDER))
				hasEnderEssence = true;
		}

		hasStructure = true;

		for (int i = -1; i <= 1; ++i){
			for (int j = -1; j <= 1; ++j){
				hasStructure &= world.getBlockState(pos.add(i, -1, j)).getBlock() == Blocks.COAL_BLOCK;
			}
		}

		hasStructure &= world.getBlockState(pos.east(2)).getBlock() == Blocks.FIRE;
		hasStructure &= world.getBlockState(pos.west(2)).getBlock() == Blocks.FIRE;
		hasStructure &= world.getBlockState(pos.south(2)).getBlock() == Blocks.FIRE;
		hasStructure &= world.getBlockState(pos.north(2)).getBlock() == Blocks.FIRE;
		hasStructure &= world.getBlockState(pos).getBlock() == BlockDefs.blackAurem;

		if (!hasStructure || !hasEnderEssence || !hasEyeofEnder)
			return;

		if (!world.isRemote){
			world.setBlockToAir(pos.east(2));
			world.setBlockToAir(pos.west(2));
			world.setBlockToAir(pos.south(2));
			world.setBlockToAir(pos.north(2));
			world.setBlockToAir(pos);

			for (EntityItem item : itemsInRange){
				item.setDead();
			}

			EntityEnderGuardian guardian = new EntityEnderGuardian(world);
			guardian.setPosition(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
			world.spawnEntity(guardian);
		}
	}

}
