package am2.defs;

import java.util.ArrayList;
import java.util.HashMap;

import am2.api.blocks.IKeystoneLockable;
import am2.api.math.AMVector3;
import am2.blocks.BlockAM;
import am2.blocks.BlockAMFlower;
import am2.blocks.BlockArcaneDeconstructor;
import am2.blocks.BlockArcaneReconstructor;
import am2.blocks.BlockArmorInfuser;
import am2.blocks.BlockArsMagicaBlock;
import am2.blocks.BlockArsMagicaOre;
import am2.blocks.BlockAstralBarrier;
import am2.blocks.BlockBrokenPowerLink;
import am2.blocks.BlockCalefactor;
import am2.blocks.BlockCandle;
import am2.blocks.BlockCraftingAltar;
import am2.blocks.BlockCrystalMarker;
import am2.blocks.BlockDesertNova;
import am2.blocks.BlockEffigy;
import am2.blocks.BlockEssenceConduit;
import am2.blocks.BlockEssenceGenerator;
import am2.blocks.BlockEssenceRefiner;
import am2.blocks.BlockEverstone;
import am2.blocks.BlockFlickerHabitat;
import am2.blocks.BlockFlickerLure;
import am2.blocks.BlockFrost;
import am2.blocks.BlockGroundRuneSpell;
import am2.blocks.BlockIllusionBlock;
import am2.blocks.BlockInertSpawner;
import am2.blocks.BlockInlay;
import am2.blocks.BlockInscriptionTable;
import am2.blocks.BlockInvisibleUtility;
import am2.blocks.BlockKeystoneChest;
import am2.blocks.BlockKeystoneDoor;
import am2.blocks.BlockKeystoneReceptacle;
import am2.blocks.BlockKeystoneTrapdoor;
import am2.blocks.BlockLectern;
import am2.blocks.BlockLightDecay;
import am2.blocks.BlockMageLight;
import am2.blocks.BlockMagicWall;
import am2.blocks.BlockMagiciansWorkbench;
import am2.blocks.BlockManaBattery;
import am2.blocks.BlockManaDrain;
import am2.blocks.BlockOcculus;
import am2.blocks.BlockOtherworldAura;
import am2.blocks.BlockParticleEmitter;
import am2.blocks.BlockSeerStone;
import am2.blocks.BlockSlipstreamGenerator;
import am2.blocks.BlockSpellSealedDoor;
import am2.blocks.BlockSummoner;
import am2.blocks.BlockTarmaRoot;
import am2.blocks.BlockVinteumTorch;
import am2.blocks.BlockWakebloom;
import am2.blocks.BlockWitchwoodLeaves;
import am2.blocks.BlockWitchwoodLog;
import am2.blocks.BlockWitchwoodPlanks;
import am2.blocks.BlockWitchwoodSapling;
import am2.blocks.BlockWitchwoodSlabsDouble;
import am2.blocks.BlockWitchwoodSlabsSimple;
import am2.blocks.BlockWitchwoodStairs;
import am2.blocks.BlockWizardsChalk;
import am2.blocks.colorizers.CrystalMarkerColorizer;
import am2.blocks.colorizers.FlickerHabitatColorizer;
import am2.blocks.colorizers.ManaBatteryColorizer;
import am2.blocks.colorizers.MonoColorizer;
import am2.blocks.tileentity.TileEntityKeystoneRecepticle;
import am2.items.rendering.IgnoreMetadataRenderer;
import am2.utils.KeystoneUtilities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSlab;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockDefs {
	
	public static final Block manaBattery = new BlockManaBattery().registerAndName(new ResourceLocation("arsmagica2:mana_battery"));
	public static final BlockFrost frost = new BlockFrost().registerAndName(new ResourceLocation("arsmagica2:frost"));
	public static final BlockOcculus occulus = new BlockOcculus().registerAndName(new ResourceLocation("arsmagica2:occulus"));
	public static final BlockAM magicWall = new BlockMagicWall().registerAndName(new ResourceLocation("arsmagica2:magic_wall"));
	public static final BlockAM invisibleLight = new BlockLightDecay().registerAndName(new ResourceLocation("arsmagica2:invisible_light"));
	public static final BlockAM invisibleUtility = new BlockInvisibleUtility().registerAndName(new ResourceLocation("arsmagica2:invisible_utility"));
	public static final BlockAM ores = new BlockArsMagicaOre().registerAndName(new ResourceLocation("arsmagica2:ore"));
	public static final BlockAM blocks = new BlockArsMagicaBlock().registerAndName(new ResourceLocation("arsmagica2:block"));
	public static final BlockAM blockMageLight = new BlockMageLight().registerAndName(new ResourceLocation("arsmagica2:block_mage_light"));
	public static final BlockAMFlower desertNova = new BlockDesertNova().registerAndName(new ResourceLocation("arsmagica2:desert_nova"));
	public static final BlockAMFlower cerublossom = new BlockAMFlower().registerAndName(new ResourceLocation("arsmagica2:cerublossom"));
	public static final BlockAMFlower wakebloom = new BlockWakebloom().registerAndName(new ResourceLocation("arsmagica2:wakebloom"));
	public static final BlockAMFlower aum = new BlockAMFlower().registerAndName(new ResourceLocation("arsmagica2:aum"));
	public static final BlockAMFlower tarmaRoot = new BlockTarmaRoot().registerAndName(new ResourceLocation("arsmagica2:tarma_root"));
	public static final BlockCraftingAltar craftingAltar = new BlockCraftingAltar().registerAndName(new ResourceLocation("arsmagica2:crafting_altar"));
	public static final Block wizardChalk = new BlockWizardsChalk().registerAndName(new ResourceLocation("arsmagica2:wizard_chalk_block"));
	public static final Block obelisk = new BlockEssenceGenerator(BlockEssenceGenerator.NEXUS_STANDARD).registerAndName(new ResourceLocation("arsmagica2:obelisk"));
	public static final Block blackAurem = new BlockEssenceGenerator(BlockEssenceGenerator.NEXUS_DARK).registerAndName(new ResourceLocation("arsmagica2:black_aurem"));
	public static final Block celestialPrism = new BlockEssenceGenerator(BlockEssenceGenerator.NEXUS_LIGHT).registerAndName(new ResourceLocation("arsmagica2:celestial_prism"));
	public static final Block crystalMarker = new BlockCrystalMarker().registerAndName(new ResourceLocation("arsmagica2:crystal_marker"));
	public static final Block wardingCandle = new BlockCandle().registerAndName(new ResourceLocation("arsmagica2:warding_candle"));
	public static final Block lectern = new BlockLectern().registerAndName(new ResourceLocation("arsmagica2:lectern"));
	public static final Block inscriptionTable = new BlockInscriptionTable().registerAndName(new ResourceLocation("arsmagica2:inscription_table"));
	public static final Block armorImbuer = new BlockArmorInfuser().registerAndName(new ResourceLocation("arsmagica2:armor_imbuer"));
	public static final Block slipstreamGenerator = new BlockSlipstreamGenerator().registerAndName(new ResourceLocation("arsmagica2:slipstream_generator"));
	public static final Block witchwoodLog = new BlockWitchwoodLog().registerAndName(new ResourceLocation("arsmagica2:witchwood_log"));
	public static final Block essenceConduit = new BlockEssenceConduit().registerAndName(new ResourceLocation("arsmagica2:essence_conduit"));
	public static final Block redstoneInlay = new BlockInlay(BlockInlay.TYPE_REDSTONE).registerAndName(new ResourceLocation("arsmagica2:redstone_inlay"));
	public static final Block ironInlay = new BlockInlay(BlockInlay.TYPE_IRON).registerAndName(new ResourceLocation("arsmagica2:iron_inlay"));;
	public static final Block goldInlay = new BlockInlay(BlockInlay.TYPE_GOLD).registerAndName(new ResourceLocation("arsmagica2:gold_inlay"));;
	public static final Block vinteumTorch = new BlockVinteumTorch().registerAndName(new ResourceLocation("arsmagica2:vinteum_torch"));
	public static final Block keystoneRecepticle = new BlockKeystoneReceptacle().registerAndName(new ResourceLocation("arsmagica2:keystone_recepticle"));
	public static final Block keystoneDoor = new BlockKeystoneDoor().registerAndName(new ResourceLocation("arsmagica2:keystone_door"));
	public static final Block keystoneTrapdoor = new BlockKeystoneTrapdoor().registerAndName(new ResourceLocation("arsmagica2:keystone_trapdoor"));
	public static final Block keystoneChest = new BlockKeystoneChest().registerAndName(new ResourceLocation("arsmagica2:keystone_chest"));
	public static final Block flickerLure = new BlockFlickerLure().registerAndName(new ResourceLocation("arsmagica2:flicker_lure"));
	public static final Block elementalAttuner = new BlockFlickerHabitat().registerAndName(new ResourceLocation("arsmagica2:flicker_habitat"));
	public static final BlockSpellSealedDoor spellSealedDoor = (BlockSpellSealedDoor) new BlockSpellSealedDoor().registerAndName(new ResourceLocation("arsmagica2:spell_sealed_door"));
	public static final Block witchwoodLeaves = new BlockWitchwoodLeaves().registerAndName(new ResourceLocation("arsmagica2:witchwood_leaves"));
	public static final Block witchwoodSapling = new BlockWitchwoodSapling().registerAndName(new ResourceLocation("arsmagica2:witchwood_sapling"));
	public static final Block everstone = new BlockEverstone().registerAndName(new ResourceLocation("arsmagica2:everstone"));
	public static final BlockGroundRuneSpell spellRune = (BlockGroundRuneSpell) new BlockGroundRuneSpell().registerAndName(new ResourceLocation("arsmagica2:spell_rune"));
	public static final Block arcaneDeconstructor = new BlockArcaneDeconstructor().registerAndName(new ResourceLocation("arsmagica2:arcane_deconstructor"));
	public static final Block arcaneReconstructor = new BlockArcaneReconstructor().registerAndName(new ResourceLocation("arsmagica2:arcane_reconstructor"));
	public static final Block astralBarrier = new BlockAstralBarrier().registerAndName(new ResourceLocation("arsmagica2:astral_barrier"));
	public static final Block essenceRefiner = new BlockEssenceRefiner().registerAndName(new ResourceLocation("arsmagica2:essence_refiner"));
	public static final BlockIllusionBlock illusionBlock = (BlockIllusionBlock) new BlockIllusionBlock().registerAndName(new ResourceLocation("arsmagica2:illusion_block"));
	public static final Block seerStone = new BlockSeerStone().registerAndName(new ResourceLocation("arsmagica2:seer_stone"));
	public static final Block brokenPowerLink = new BlockBrokenPowerLink().registerAndName(new ResourceLocation("arsmagica2:broken_power_link"));
	public static final Block calefactor = new BlockCalefactor().registerAndName(new ResourceLocation("arsmagica2:calefactor"));
	public static final Block inertSpawner = new BlockInertSpawner().registerAndName(new ResourceLocation("arsmagica2:inert_spawner"));
	public static final Block magiciansWorkbench = new BlockMagiciansWorkbench().registerAndName(new ResourceLocation("arsmagica2:magicians_workbench"));
	public static final Block otherworldAura = new BlockOtherworldAura().registerAndName(new ResourceLocation("arsmagica2:otherworld_aura"));
	public static final Block particleEmitter = new BlockParticleEmitter().registerAndName(new ResourceLocation("arsmagica2:particle_emitter"));
	public static final Block summoner = new BlockSummoner().registerAndName(new ResourceLocation("arsmagica2:summoner"));
	public static final Block iceEffigy = new BlockEffigy(Material.ICE).registerAndName(new ResourceLocation("arsmagica2:ice_effigy"));
	public static final Block lightningEffigy = new BlockEffigy(Material.IRON).registerAndName(new ResourceLocation("arsmagica2:lightning_effigy"));
	public static final Block witchwoodPlanks = new BlockWitchwoodPlanks().registerAndName(new ResourceLocation("arsmagica2:witchwood_planks"));
	public static final Block witchwoodStairs = new BlockWitchwoodStairs(witchwoodPlanks.getDefaultState()).registerAndName(new ResourceLocation("arsmagica2:witchwood_stairs"));
	public static final Block manaDrain = new BlockManaDrain().registerAndName(new ResourceLocation("arsmagica2:mana_drain"));
	public static final BlockSlab witchwoodSingleSlab = new BlockWitchwoodSlabsSimple();
	public static final BlockSlab witchwoodDoubleSlab = new BlockWitchwoodSlabsDouble();
	
	public static HashMap<Integer, ArrayList<AMVector3>> KeystonePortalLocations = new HashMap<>();
	public static Fluid liquid_essence = new Fluid("liquid_essence", new ResourceLocation("arsmagica2", "blocks/liquidEssenceStill"), new ResourceLocation("arsmagica2", "blocks/liquidEssenceFlowing")).setRarity(EnumRarity.RARE).setLuminosity(7);
	
	
	public void preInit () {
		FluidRegistry.registerFluid(liquid_essence);
		FluidRegistry.addBucketForFluid(liquid_essence);
		liquid_essence = FluidRegistry.getFluid(BlockDefs.liquid_essence.getName());
		Block blockliquid_essence = new BlockFluidClassic(liquid_essence, Material.WATER).setUnlocalizedName("arsmagica2:fluid_block_liquid_essence");
		Item itemliquid_essence = new ItemBlock(blockliquid_essence);
		GameRegistry.register(blockliquid_essence, new ResourceLocation("arsmagica2:liquid_essence"));
		GameRegistry.register(itemliquid_essence, new ResourceLocation("arsmagica2:liquid_essence"));
		
		GameRegistry.register(witchwoodSingleSlab, new ResourceLocation("arsmagica2:witchwood_slab"));
		GameRegistry.register(witchwoodDoubleSlab, new ResourceLocation("arsmagica2:witchwood_slab_double"));
		GameRegistry.register(new ItemSlab(witchwoodSingleSlab, witchwoodSingleSlab, witchwoodDoubleSlab), new ResourceLocation("arsmagica2:witchwood_slab"));
	}
	
	@SideOnly(Side.CLIENT)
	public void preInitClient() {
		Block blockliquid_essence = GameRegistry.findRegistry(Block.class).getValue(new ResourceLocation("arsmagica2:liquid_essence"));
		Item itemliquid_essence = GameRegistry.findRegistry(Item.class).getValue(new ResourceLocation("arsmagica2:liquid_essence"));
		ModelBakery.registerItemVariants(itemliquid_essence, new ModelResourceLocation(new ResourceLocation("arsmagica2:liquid_essence"), liquid_essence.getName()));
		ModelLoader.setCustomMeshDefinition(itemliquid_essence, new ItemMeshDefinition() {
			
			@Override
			public ModelResourceLocation getModelLocation(ItemStack stack) {
				return new ModelResourceLocation(new ResourceLocation("arsmagica2:liquid_essence"), liquid_essence.getName());
			}
		});
		
		ModelLoader.setCustomStateMapper(iceEffigy, new StateMap.Builder().ignore(BlockEffigy.PROGRESS).build());
		ModelLoader.setCustomStateMapper(lightningEffigy, new StateMap.Builder().ignore(BlockEffigy.PROGRESS).build());
		
		ModelLoader.setCustomStateMapper(blockliquid_essence, new StateMapperBase() {
			
			@Override
			protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
				return new ModelResourceLocation(new ResourceLocation("arsmagica2:liquid_essence"), liquid_essence.getName());
			}
		});
	}
	
	
	@SideOnly(Side.CLIENT)
	public static void initClient () {
		IForgeRegistry<Item> items = GameRegistry.findRegistry(Item.class);
		RenderItem renderer = Minecraft.getMinecraft().getRenderItem();
		
		//Utility Blocks
		registerTexture(frost);
		registerTexture(invisibleLight);
		registerTexture(invisibleUtility);
		registerTexture(blockMageLight);
		
		//Building Blocks
		registerTexture(magicWall);
		
		//Power Blocks
		registerTexture(obelisk);
		registerTexture(celestialPrism);
		registerTexture(blackAurem);
		registerTexture(manaBattery);
		registerTexture(armorImbuer);
		registerTexture(slipstreamGenerator);
		registerTexture(manaDrain);
		
		//Flickers
		registerTexture(crystalMarker);
		registerTexture(elementalAttuner);
		registerTexture(flickerLure);
		
		//Ritual Blocks
		registerTexture(wardingCandle);
		registerTexture(wizardChalk);
		
		//Spell Blocks
		registerTexture(occulus);
		registerTexture(lectern);
		registerTexture(craftingAltar);
		registerTexture(inscriptionTable);
		
		//Flowers
		registerTexture(aum);
		registerTexture(cerublossom);
		registerTexture(wakebloom);
		registerTexture(tarmaRoot);
		registerTexture(desertNova);
		
		registerTexture(keystoneRecepticle);
		registerTexture(keystoneDoor);
		registerTexture(keystoneChest);
		registerTexture(keystoneTrapdoor);
		
		registerTexture(witchwoodLeaves);
		registerTexture(witchwoodLog);
		registerTexture(witchwoodSapling);
		registerTexture(essenceConduit);
		
		registerTexture(arcaneDeconstructor);
		registerTexture(arcaneReconstructor);
		registerTexture(essenceRefiner);
		registerTexture(everstone);
		
		registerTexture(illusionBlock);
		registerTexture(seerStone);
		registerTexture(brokenPowerLink);
		registerTexture(calefactor);
		registerTexture(inertSpawner);
		registerTexture(magiciansWorkbench);
		registerTexture(otherworldAura);
		registerTexture(particleEmitter);
		registerTexture(summoner);
		registerTexture(iceEffigy);
		registerTexture(lightningEffigy);
		registerTexture(witchwoodStairs);
		registerTexture(witchwoodPlanks);
		registerTexture(witchwoodSingleSlab);
		registerTexture(redstoneInlay);
		registerTexture(goldInlay);
		registerTexture(ironInlay);
		registerTexture(astralBarrier);
		registerTexture(vinteumTorch);
		
		Item ore = items.getValue(new ResourceLocation("arsmagica2:ore"));
		Item block = items.getValue(new ResourceLocation("arsmagica2:block"));
		
		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(new ManaBatteryColorizer(), manaBattery);
		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(new CrystalMarkerColorizer(), crystalMarker);
		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(new FlickerHabitatColorizer(), elementalAttuner);
		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(new MonoColorizer(0x00ffff), essenceConduit);
		for (int i = 0; i < BlockArsMagicaOre.EnumOreType.values().length; i++) {
			ModelResourceLocation blockLoc = new ModelResourceLocation("arsmagica2:block_" + BlockArsMagicaOre.EnumOreType.values()[i].getName(), "inventory");
			ModelResourceLocation oreLoc = new ModelResourceLocation("arsmagica2:ore_" + BlockArsMagicaOre.EnumOreType.values()[i].getName(), "inventory");
			ModelBakery.registerItemVariants(ore, oreLoc);
			ModelBakery.registerItemVariants(block, blockLoc);
			renderer.getItemModelMesher().register(ore, i, oreLoc);
			renderer.getItemModelMesher().register(block, i, blockLoc);
		}
	}
	
	@SideOnly(Side.CLIENT)
	private static void registerTexture(Block block) {
		ResourceLocation loc = block.getRegistryName();
		Item item = GameRegistry.findRegistry(Item.class).getValue(loc);
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, new IgnoreMetadataRenderer(new ModelResourceLocation(loc, "inventory")));
	}
	
	public void registerKeystonePortal(BlockPos pos, int dimension){
		AMVector3 location = new AMVector3(pos);
		if (!KeystonePortalLocations.containsKey(dimension))
			KeystonePortalLocations.put(dimension, new ArrayList<AMVector3>());

		ArrayList<AMVector3> dimensionList = KeystonePortalLocations.get(dimension);

		if (!dimensionList.contains(location))
			dimensionList.add(location);
	}

	public void removeKeystonePortal(BlockPos pos, int dimension){
		AMVector3 location = new AMVector3(pos);
		if (KeystonePortalLocations.containsKey(dimension)){
			ArrayList<AMVector3> dimensionList = KeystonePortalLocations.get(dimension);

			if (dimensionList.contains(location))
				dimensionList.remove(location);
		}
	}

	public AMVector3 getNextKeystonePortalLocation(World world, BlockPos pos, boolean multidimensional, long key){
		AMVector3 current = new AMVector3(pos);
		if (!multidimensional){
			AMVector3 next = getNextKeystoneLocationInWorld(world, pos, key);
			if (next == null)
				next = current;
			return next;
		}else{
			return current;
		}
	}

	public AMVector3 getNextKeystoneLocationInWorld(World world, BlockPos pos, long key){
		AMVector3 location = new AMVector3(pos);
		ArrayList<AMVector3> dimensionList = KeystonePortalLocations.get(world.provider.getDimension());
		if (dimensionList == null || dimensionList.size() < 1){
			return null;
		}
		
		int index = dimensionList.indexOf(location);
		index++;
		if (index >= dimensionList.size()) index = 0;
		AMVector3 newLocation = dimensionList.get(index);
		for (int i = 0; i < dimensionList.size(); i++){
			TileEntity te = world.getTileEntity(newLocation.toBlockPos());
			if (te != null && te instanceof TileEntityKeystoneRecepticle){
				if (KeystoneUtilities.instance.getKeyFromRunes(((IKeystoneLockable<?>)te).getRunesInKey()) == key){
					return newLocation;
				}
			}
			index++;
			if (index >= dimensionList.size()) index = 0;
			newLocation = dimensionList.get(index);
		}

		return location;
	}

	public void resetKnownPortalLocations(){
		KeystonePortalLocations.clear();
	}
}
