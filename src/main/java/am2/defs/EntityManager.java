package am2.defs;

import java.util.Set;

import am2.ArsMagica2;
import am2.LogHelper;
import am2.bosses.EntityAirGuardian;
import am2.bosses.EntityArcaneGuardian;
import am2.bosses.EntityEarthGuardian;
import am2.bosses.EntityEnderGuardian;
import am2.bosses.EntityFireGuardian;
import am2.bosses.EntityLifeGuardian;
import am2.bosses.EntityLightningGuardian;
import am2.bosses.EntityNatureGuardian;
import am2.bosses.EntityWaterGuardian;
import am2.bosses.EntityWinterGuardian;
import am2.bosses.renderers.RenderAirGuardian;
import am2.bosses.renderers.RenderArcaneGuardian;
import am2.bosses.renderers.RenderEarthGuardian;
import am2.bosses.renderers.RenderEnderGuardian;
import am2.bosses.renderers.RenderFireGuardian;
import am2.bosses.renderers.RenderIceGuardian;
import am2.bosses.renderers.RenderLifeGuardian;
import am2.bosses.renderers.RenderLightningGuardian;
import am2.bosses.renderers.RenderPlantGuardian;
import am2.bosses.renderers.RenderThrownRock;
import am2.bosses.renderers.RenderThrownSickle;
import am2.bosses.renderers.RenderWaterGuardian;
import am2.bosses.renderers.RenderWinterGuardianArm;
import am2.entity.EntityAirSled;
import am2.entity.EntityBoundArrow;
import am2.entity.EntityBroom;
import am2.entity.EntityDarkMage;
import am2.entity.EntityDarkling;
import am2.entity.EntityDryad;
import am2.entity.EntityEarthElemental;
import am2.entity.EntityFireElemental;
import am2.entity.EntityFlicker;
import am2.entity.EntityHecate;
import am2.entity.EntityHellCow;
import am2.entity.EntityLightMage;
import am2.entity.EntityManaCreeper;
import am2.entity.EntityManaElemental;
import am2.entity.EntityManaVortex;
import am2.entity.EntityRiftStorage;
import am2.entity.EntityShadowHelper;
import am2.entity.EntityShockwave;
import am2.entity.EntitySpellEffect;
import am2.entity.EntitySpellProjectile;
import am2.entity.EntityThrownRock;
import am2.entity.EntityThrownSickle;
import am2.entity.EntityWaterElemental;
import am2.entity.EntityWhirlwind;
import am2.entity.EntityWinterGuardianArm;
import am2.entity.render.*;
import am2.utils.RenderFactory;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.BiomeManager.BiomeType;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public class EntityManager {
	
	public static final EntityManager instance = new EntityManager();
	
	private EntityManager() {
		
	}
	
	public static final ResourceLocation SPELL_PROJECTILE = makeName("SpellProjectile");
	public static final ResourceLocation RIFT_STORAGE = makeName("RiftStorage");
	public static final ResourceLocation SPELL_EFFECT = makeName("SpellEffect");
	public static final ResourceLocation THROWN_ROCK = makeName("ThrownRock");
	public static final ResourceLocation BOUND_ARROW = makeName("BoundArrow");
	public static final ResourceLocation DARKLING = makeName("Darkling");
	public static final ResourceLocation DARK_MAGE = makeName("DarkMage");
	public static final ResourceLocation DRYAD = makeName("Dryad");
	public static final ResourceLocation EARTH_ELEMENTAL = makeName("EarthElemental");
	public static final ResourceLocation FIRE_ELEMENTAL = makeName("FireElemental");
	public static final ResourceLocation MANA_ELEMENTAL = makeName("ManaElemental");
	public static final ResourceLocation LIGHT_MAGE = makeName("LightMage");
	public static final ResourceLocation WATER_ELEMENTAL = makeName("WaterElemental");
	public static final ResourceLocation MANA_CREEPER = makeName("ManaCreeper");
	public static final ResourceLocation HECATE = makeName("Hecate");
	public static final ResourceLocation HELL_COW = makeName("HellCow");
	public static final ResourceLocation MANA_VORTEX = makeName("ManaVortex");
	public static final ResourceLocation SHOCKWAVE = makeName("Shockwave");
	public static final ResourceLocation THROWN_SICKLE = makeName("ThrownSickle");
	public static final ResourceLocation WHIRLWIND = makeName("Whirlwind");
	public static final ResourceLocation WINTER_GUARDIAN_ARM = makeName("WinterGuardianArm");
	public static final ResourceLocation SHADOW_HELPER = makeName("ShadowHelper");
	
	public static final ResourceLocation AIR_GUARDIAN = makeName("AirGuardian");
	public static final ResourceLocation ARCANE_GUARDIAN = makeName("ArcaneGuardian");
	public static final ResourceLocation EARTH_GUARDIAN = makeName("EarthGuardian");
	public static final ResourceLocation ENDER_GUARDIAN = makeName("EnderGuardian");
	public static final ResourceLocation FIRE_GUARDIAN = makeName("FireGuardian");
	public static final ResourceLocation LIFE_GUARDIAN = makeName("LifeGuardian");
	public static final ResourceLocation LIGHTNING_GUARDIAN = makeName("LightningGuardian");
	public static final ResourceLocation NATURE_GUARDIAN = makeName("NatureGuardian");
	public static final ResourceLocation WATER_GUARDIAN = makeName("WaterGuardian");
	public static final ResourceLocation WINTER_GUARDIAN = makeName("WinterGuardian");
	
	public static final ResourceLocation AIR_SLED = makeName("AirSled");
	public static final ResourceLocation BROOM = makeName("DaBroom");
	public static final ResourceLocation FLICKER = makeName("Flicker");
	
	private static ResourceLocation makeName(String s)
	{
		return new ResourceLocation(ArsMagica2.MODID + ":" + s);
	}
	
	public void registerEntities() {
		LogHelper.info("Initializing entities. Apparently this will appear twice.");
		//Mobs
		EntityRegistry.registerModEntity(DARKLING, EntityDarkling.class, "Darkling", 5, ArsMagica2.instance, 64, 2, true);
		EntityRegistry.registerModEntity(DARK_MAGE, EntityDarkMage.class, "DarkMage", 6, ArsMagica2.instance, 64, 2, true, 0xaa00ff, 0x660066);
		EntityRegistry.registerModEntity(DRYAD, EntityDryad.class, "Dryad", 7, ArsMagica2.instance, 64, 2, true, 0x00ff00, 0x34e122);
		EntityRegistry.registerModEntity(EARTH_ELEMENTAL, EntityEarthElemental.class, "EarthElemental", 8, ArsMagica2.instance, 64, 2, true, 0x61330b, 0x00ff00);
		EntityRegistry.registerModEntity(FIRE_ELEMENTAL, EntityFireElemental.class, "FireElemental", 9, ArsMagica2.instance, 64, 2, true, 0xef260b, 0xff0000);
		EntityRegistry.registerModEntity(LIGHT_MAGE, EntityLightMage.class, "LightMage", 10, ArsMagica2.instance, 64, 2, true, 0xaa00ff, 0xff00ff);
		EntityRegistry.registerModEntity(MANA_ELEMENTAL, EntityManaElemental.class, "ManaElemental", 11, ArsMagica2.instance, 64, 2, true, 0xcccccc, 0xb935cd);
		EntityRegistry.registerModEntity(WATER_ELEMENTAL, EntityWaterElemental.class, "WaterElemental", 29, ArsMagica2.instance, 64, 2, true, 0x0b5cef, 0x0000ff);
		EntityRegistry.registerModEntity(MANA_CREEPER, EntityManaCreeper.class, "ManaCreeper", 30, ArsMagica2.instance, 64, 2, true, 0x0b5cef, 0xb935cd);
		EntityRegistry.registerModEntity(HECATE, EntityHecate.class, "Hecate", 31, ArsMagica2.instance, 64, 2, true, 0xef260b, 0x3f043d);
		EntityRegistry.registerModEntity(HELL_COW, EntityHellCow.class, "HellCow", 33, ArsMagica2.instance, 64, 2, true);
		//Technical Entities
		EntityRegistry.registerModEntity(MANA_VORTEX, EntityManaVortex.class, "ManaVortex", 12, ArsMagica2.instance, 64, 2, true);
		EntityRegistry.registerModEntity(SHOCKWAVE, EntityShockwave.class, "Shockwave", 13, ArsMagica2.instance, 64, 2, true);
		EntityRegistry.registerModEntity(THROWN_SICKLE, EntityThrownSickle.class, "ThrownSickle", 14, ArsMagica2.instance, 64, 2, true);
		EntityRegistry.registerModEntity(WHIRLWIND, EntityWhirlwind.class, "Whirlwind", 15, ArsMagica2.instance, 64, 2, true);
		EntityRegistry.registerModEntity(WINTER_GUARDIAN_ARM, EntityWinterGuardianArm.class, "WinterGuardianArm", 16, ArsMagica2.instance, 64, 2, true);
		EntityRegistry.registerModEntity(SHADOW_HELPER, EntityShadowHelper.class, "ShadowHelper", 34, ArsMagica2.instance, 64, 2, true);
		EntityRegistry.registerModEntity(SPELL_PROJECTILE,EntitySpellProjectile.class, "SpellProjectile", 0, ArsMagica2.instance, 64, 2, true);
		EntityRegistry.registerModEntity(RIFT_STORAGE, EntityRiftStorage.class, "RiftStorage", 1, ArsMagica2.instance, 64, 2, false);
		EntityRegistry.registerModEntity(SPELL_EFFECT, EntitySpellEffect.class, "SpellEffect", 2, ArsMagica2.instance, 64, 2, true);
		EntityRegistry.registerModEntity(THROWN_ROCK, EntityThrownRock.class, "ThrownRock", 3, ArsMagica2.instance, 64, 2, true);
		EntityRegistry.registerModEntity(BOUND_ARROW, EntityBoundArrow.class, "BoundArrow", 4, ArsMagica2.instance, 64, 2, true);
		//Bosses
		EntityRegistry.registerModEntity(AIR_GUARDIAN, EntityAirGuardian.class, "AirGuardian", 17, ArsMagica2.instance, 64, 2, true, 0xFFFFFF, 0xFFCC00);
		EntityRegistry.registerModEntity(ARCANE_GUARDIAN, EntityArcaneGuardian.class, "ArcaneGuardian", 18, ArsMagica2.instance, 64, 2, true, 0x999999, 0xcc00cc);
		EntityRegistry.registerModEntity(EARTH_GUARDIAN, EntityEarthGuardian.class, "EarthGuardian", 19, ArsMagica2.instance, 64, 2, true, 0x663300, 0x339900);
		EntityRegistry.registerModEntity(ENDER_GUARDIAN, EntityEnderGuardian.class, "EnderGuardian", 20, ArsMagica2.instance, 64, 2, true, 0x000000, 0x6633);
		EntityRegistry.registerModEntity(FIRE_GUARDIAN, EntityFireGuardian.class, "FireGuardian", 21, ArsMagica2.instance, 64, 2, true, 0xFFFFFF, 0xFF0000);
		EntityRegistry.registerModEntity(LIFE_GUARDIAN, EntityLifeGuardian.class, "LifeGuardian", 22, ArsMagica2.instance, 64, 2, true, 0x00E6FF, 0xFFE600);
		EntityRegistry.registerModEntity(LIGHTNING_GUARDIAN, EntityLightningGuardian.class, "LightningGuardian", 23, ArsMagica2.instance, 64, 2, true, 0xFFE600, 0x00C4FF);
		EntityRegistry.registerModEntity(NATURE_GUARDIAN, EntityNatureGuardian.class, "NatureGuardian", 24, ArsMagica2.instance, 64, 2, true, 0x44FF00, 0x307D0F);
		EntityRegistry.registerModEntity(WATER_GUARDIAN, EntityWaterGuardian.class, "WaterGuardian", 25, ArsMagica2.instance, 64, 2, true, 0x0F387D, 0x0097CE);
		EntityRegistry.registerModEntity(WINTER_GUARDIAN, EntityWinterGuardian.class, "WinterGuardian", 26, ArsMagica2.instance, 64, 2, true, 0x00CEBA, 0x104742);
		//Utility Entities
		EntityRegistry.registerModEntity(AIR_SLED, EntityAirSled.class, "AirSled", 27, ArsMagica2.instance, 64, 2, true);
		EntityRegistry.registerModEntity(BROOM, EntityBroom.class, "DaBroom", 28, ArsMagica2.instance, 64, 2, true);
		EntityRegistry.registerModEntity(FLICKER, EntityFlicker.class, "Flicker", 32, ArsMagica2.instance, 64, 2, true);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void registerRenderers() {
		RenderingRegistry.registerEntityRenderingHandler(EntityRiftStorage.class, new RenderFactory(RenderRiftStorage.class));
		RenderingRegistry.registerEntityRenderingHandler(EntitySpellProjectile.class, new RenderFactory(RenderSpellProjectile.class));
		RenderingRegistry.registerEntityRenderingHandler(EntitySpellEffect.class, new RenderFactory(RenderHidden.class));
		RenderingRegistry.registerEntityRenderingHandler(EntityThrownRock.class, new RenderFactory(RenderThrownRock.class));
		RenderingRegistry.registerEntityRenderingHandler(EntityBoundArrow.class, new RenderFactory(RenderBoundArrow.class));
		
		RenderingRegistry.registerEntityRenderingHandler(EntityThrownSickle.class, new RenderFactory(RenderThrownSickle.class));
		RenderingRegistry.registerEntityRenderingHandler(EntityWinterGuardianArm.class, new RenderFactory(RenderWinterGuardianArm.class));
		RenderingRegistry.registerEntityRenderingHandler(EntityAirSled.class, new RenderFactory(RenderAirSled.class));
		//Bosses
		RenderingRegistry.registerEntityRenderingHandler(EntityAirGuardian.class, new RenderFactory(RenderAirGuardian.class));
		RenderingRegistry.registerEntityRenderingHandler(EntityArcaneGuardian.class, new RenderFactory(RenderArcaneGuardian.class));
		RenderingRegistry.registerEntityRenderingHandler(EntityEarthGuardian.class, new RenderFactory(RenderEarthGuardian.class));
		RenderingRegistry.registerEntityRenderingHandler(EntityFireGuardian.class, new RenderFactory(RenderFireGuardian.class));
		RenderingRegistry.registerEntityRenderingHandler(EntityEnderGuardian.class, new RenderFactory(RenderEnderGuardian.class));
		RenderingRegistry.registerEntityRenderingHandler(EntityFireGuardian.class, new RenderFactory(RenderFireGuardian.class));
		RenderingRegistry.registerEntityRenderingHandler(EntityLifeGuardian.class, new RenderFactory(RenderLifeGuardian.class));
		RenderingRegistry.registerEntityRenderingHandler(EntityLightningGuardian.class, new RenderFactory(RenderLightningGuardian.class));
		RenderingRegistry.registerEntityRenderingHandler(EntityNatureGuardian.class, new RenderFactory(RenderPlantGuardian.class));
		RenderingRegistry.registerEntityRenderingHandler(EntityWaterGuardian.class, new RenderFactory(RenderWaterGuardian.class));
		RenderingRegistry.registerEntityRenderingHandler(EntityWinterGuardian.class, new RenderFactory(RenderIceGuardian.class));

		RenderingRegistry.registerEntityRenderingHandler(EntityManaElemental.class, new RenderFactory(RenderManaElemental.class));
		RenderingRegistry.registerEntityRenderingHandler(EntityWaterElemental.class, new RenderFactory(RenderWaterElemental.class));
		RenderingRegistry.registerEntityRenderingHandler(EntityFireElemental.class, new RenderFactory(RenderFireElemental.class));
		RenderingRegistry.registerEntityRenderingHandler(EntityEarthElemental.class, new RenderFactory(RenderEarthElemental.class));
		RenderingRegistry.registerEntityRenderingHandler(EntityManaCreeper.class, new RenderFactory(RenderManaCreeper.class));
		RenderingRegistry.registerEntityRenderingHandler(EntityLightMage.class, new RenderFactory(RenderLightMage.class));
		RenderingRegistry.registerEntityRenderingHandler(EntityDarkMage.class, new RenderFactory(RenderDarkMage.class));
		RenderingRegistry.registerEntityRenderingHandler(EntityManaVortex.class, new RenderFactory(RenderManaVortex.class));
		RenderingRegistry.registerEntityRenderingHandler(EntityHecate.class, new RenderFactory(RenderHecate.class));
		RenderingRegistry.registerEntityRenderingHandler(EntityDryad.class, new RenderFactory(RenderDryad.class));
		RenderingRegistry.registerEntityRenderingHandler(EntityFlicker.class, new RenderFactory(RenderFlicker.class));
		RenderingRegistry.registerEntityRenderingHandler(EntityHellCow.class, new RenderFactory(RenderHellCow.class));
		RenderingRegistry.registerEntityRenderingHandler(EntityDarkling.class, new RenderFactory(RenderDarkling.class));
		RenderingRegistry.registerEntityRenderingHandler(EntityShadowHelper.class, new RenderFactory(RenderShadowHelper.class));
		RenderingRegistry.registerEntityRenderingHandler(EntityBroom.class, new RenderFactory(RenderBroom.class));
		RenderingRegistry.registerEntityRenderingHandler(EntityShockwave.class, new RenderFactory(RenderShockwave.class));
	}
	
	public void initializeSpawns(){
	
		//SpawnListEntry wisps = new SpawnListEntry(EntityWisp.class, 1, 1, 1);
		SpawnListEntry manaElementals = new SpawnListEntry(EntityManaElemental.class, ArsMagica2.config.GetManaElementalSpawnRate(), 1, 1);
		SpawnListEntry dryads = new SpawnListEntry(EntityDryad.class, ArsMagica2.config.GetDryadSpawnRate(), 1, 2);
		SpawnListEntry hecates_nonHell = new SpawnListEntry(EntityHecate.class, ArsMagica2.config.GetHecateSpawnRate(), 1, 1);
		SpawnListEntry hecates_hell = new SpawnListEntry(EntityHecate.class, ArsMagica2.config.GetHecateSpawnRate() * 2, 1, 2);
		SpawnListEntry manaCreepers = new SpawnListEntry(EntityManaCreeper.class, ArsMagica2.config.GetManaCreeperSpawnRate(), 1, 1);
		SpawnListEntry lightMages = new SpawnListEntry(EntityLightMage.class, ArsMagica2.config.GetMageSpawnRate(), 1, 3);
		SpawnListEntry darkMages = new SpawnListEntry(EntityDarkMage.class, ArsMagica2.config.GetMageSpawnRate(), 1, 3);
		SpawnListEntry waterElementals = new SpawnListEntry(EntityWaterElemental.class, ArsMagica2.config.GetWaterElementalSpawnRate(), 1, 3);
		SpawnListEntry darklings = new SpawnListEntry(EntityDarkling.class, ArsMagica2.config.GetDarklingSpawnRate(), 4, 8);
		SpawnListEntry earthElementals = new SpawnListEntry(EntityEarthElemental.class, ArsMagica2.config.GetEarthElementalSpawnRate(), 1, 2);
		SpawnListEntry fireElementals = new SpawnListEntry(EntityFireElemental.class, ArsMagica2.config.GetFireElementalSpawnRate(), 1, 1);
		SpawnListEntry flickers = new SpawnListEntry(EntityFlicker.class, ArsMagica2.config.GetFlickerSpawnRate(), 1, 1);

		initSpawnsForBiomeTypes(manaElementals, EnumCreatureType.MONSTER, new Type[]{Type.BEACH, Type.DRY, Type.FOREST, Type.COLD, Type.HILLS, Type.JUNGLE, Type.MAGICAL, Type.MOUNTAIN, Type.PLAINS, Type.SWAMP, Type.WASTELAND}, new Type[]{Type.END, Type.NETHER, Type.MUSHROOM});

		initSpawnsForBiomeTypes(dryads, EnumCreatureType.CREATURE, new Type[]{Type.BEACH, Type.FOREST, Type.MAGICAL, Type.HILLS, Type.JUNGLE, Type.MOUNTAIN, Type.PLAINS}, new Type[]{Type.END, Type.COLD, Type.MUSHROOM, Type.NETHER, Type.WASTELAND, Type.SWAMP, Type.DRY});

		initSpawnsForBiomeTypes(hecates_nonHell, EnumCreatureType.MONSTER, new Type[]{Type.BEACH, Type.DRY, Type.FOREST, Type.COLD, Type.HILLS, Type.JUNGLE, Type.MAGICAL, Type.MOUNTAIN, Type.PLAINS, Type.SWAMP, Type.WASTELAND}, new Type[]{Type.END, Type.NETHER, Type.MUSHROOM});

		initSpawnsForBiomeTypes(hecates_hell, EnumCreatureType.MONSTER, new Type[]{Type.NETHER}, new Type[]{Type.MUSHROOM});

		initSpawnsForBiomeTypes(darklings, EnumCreatureType.MONSTER, new Type[]{Type.NETHER}, new Type[]{Type.MUSHROOM});

		initSpawnsForBiomeTypes(manaCreepers, EnumCreatureType.MONSTER, new Type[]{Type.BEACH, Type.DRY, Type.FOREST, Type.COLD, Type.HILLS, Type.JUNGLE, Type.MAGICAL, Type.MOUNTAIN, Type.PLAINS, Type.SWAMP, Type.WASTELAND}, new Type[]{Type.END, Type.NETHER, Type.MUSHROOM});

		initSpawnsForBiomeTypes(lightMages, EnumCreatureType.MONSTER, new Type[]{Type.BEACH, Type.DRY, Type.FOREST, Type.COLD, Type.HILLS, Type.JUNGLE, Type.MAGICAL, Type.MOUNTAIN, Type.PLAINS, Type.SWAMP, Type.WASTELAND}, new Type[]{Type.END, Type.NETHER, Type.MUSHROOM});

		initSpawnsForBiomeTypes(darkMages, EnumCreatureType.MONSTER, new Type[]{Type.BEACH, Type.DRY, Type.FOREST, Type.COLD, Type.HILLS, Type.JUNGLE, Type.MAGICAL, Type.MOUNTAIN, Type.PLAINS, Type.SWAMP, Type.WASTELAND}, new Type[]{Type.END, Type.NETHER, Type.MUSHROOM});

		initSpawnsForBiomeTypes(waterElementals, EnumCreatureType.MONSTER, new Type[]{Type.WATER}, new Type[]{Type.END, Type.NETHER, Type.MUSHROOM});
		initSpawnsForBiomeTypes(waterElementals, EnumCreatureType.WATER_CREATURE, new Type[]{Type.WATER}, new Type[]{Type.END, Type.NETHER, Type.MUSHROOM});

		initSpawnsForBiomeTypes(earthElementals, EnumCreatureType.MONSTER, new Type[]{Type.HILLS, Type.MOUNTAIN}, new Type[]{Type.MUSHROOM});
		initSpawnsForBiomeTypes(fireElementals, EnumCreatureType.MONSTER, new Type[]{Type.NETHER}, new Type[]{Type.MUSHROOM});
		
		initSpawnsForBiomeTypes(flickers, EnumCreatureType.AMBIENT, new Type[]{Type.WATER, Type.COLD, Type.HOT, Type.JUNGLE, Type.HILLS, Type.LUSH, Type.MAGICAL, Type.PLAINS, Type.SPARSE, Type.END, Type.NETHER, Type.DRY}, new Type[0]);

	}

	private void initSpawnsForBiomeTypes(SpawnListEntry spawnListEntry, EnumCreatureType creatureType, Type[] types, Type[] exclusions){
		if (spawnListEntry.itemWeight == 0){
			LogHelper.info("Skipping spawn list entry for %s (as type %s), as the weight is set to 0.  This can be changed in config.", spawnListEntry.entityClass.getName(), creatureType.toString());
			return;
		}
		for (Type type : types){
			initSpawnsForBiomes(BiomeDictionary.getBiomes(type), spawnListEntry, creatureType, exclusions);
		}
	}

	private void initSpawnsForBiomes(Set<Biome> biomes, SpawnListEntry spawnListEntry, EnumCreatureType creatureType, Type[] exclusions){
		if (biomes == null) return;
		for (Biome biome : biomes){
			if (biomeIsExcluded(biome, exclusions)) continue;
			if (!biome.getSpawnableList(creatureType).contains(spawnListEntry))
				biome.getSpawnableList(creatureType).add(spawnListEntry);
		}
	}

	private boolean biomeIsExcluded(Biome biome, Type[] exclusions){

		Set<Type> biomeTypes = BiomeDictionary.getTypes(biome);

		for (Type exclusion : exclusions){
			for (Type biomeType : biomeTypes){
				if (biomeType == exclusion) return true;
			}
		}
		return false;
	}}
