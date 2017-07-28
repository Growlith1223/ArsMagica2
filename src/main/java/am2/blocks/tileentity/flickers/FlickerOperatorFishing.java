package am2.blocks.tileentity.flickers;

import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import am2.api.flickers.IFlickerController;
import am2.api.flickers.AbstractFlickerFunctionality;
import am2.defs.ItemDefs;
import am2.utils.InventoryUtilities;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTableList;

public class FlickerOperatorFishing extends AbstractFlickerFunctionality{

//	private static final List common_items = LootTableList.//Arrays.asList(new WeightedRandomFishable[]{(new WeightedRandomFishable(new ItemStack(Items.leather_boots), 10)).func_150709_a(0.9F), new WeightedRandomFishable(new ItemStack(Items.leather), 10), new WeightedRandomFishable(new ItemStack(Items.bone), 10), new WeightedRandomFishable(new ItemStack(Items.potionitem), 10), new WeightedRandomFishable(new ItemStack(Items.string), 5), (new WeightedRandomFishable(new ItemStack(Items.fishing_rod), 2)).func_150709_a(0.9F), new WeightedRandomFishable(new ItemStack(Items.bowl), 10), new WeightedRandomFishable(new ItemStack(Items.stick), 5), new WeightedRandomFishable(new ItemStack(Items.dye, 10, 0), 1), new WeightedRandomFishable(new ItemStack(Blocks.tripwire_hook), 10), new WeightedRandomFishable(new ItemStack(Items.rotten_flesh), 10)});
//	private static final List rare_items = Arrays.asList(new WeightedRandomFishable[]{new WeightedRandomFishable(new ItemStack(Blocks.waterlily), 1), new WeightedRandomFishable(new ItemStack(Items.name_tag), 1), new WeightedRandomFishable(new ItemStack(Items.saddle), 1), (new WeightedRandomFishable(new ItemStack(Items.bow), 1)).func_150709_a(0.25F).func_150707_a(), (new WeightedRandomFishable(new ItemStack(Items.fishing_rod), 1)).func_150709_a(0.25F).func_150707_a(), (new WeightedRandomFishable(new ItemStack(Items.book), 1)).func_150707_a()});
//	private static final List uncommon_items = Arrays.asList(new WeightedRandomFishable[]{new WeightedRandomFishable(new ItemStack(Items.fish, 1, ItemFishFood.FishType.COD.func_150976_a()), 60), new WeightedRandomFishable(new ItemStack(Items.fish, 1, ItemFishFood.FishType.SALMON.func_150976_a()), 25), new WeightedRandomFishable(new ItemStack(Items.fish, 1, ItemFishFood.FishType.CLOWNFISH.func_150976_a()), 2), new WeightedRandomFishable(new ItemStack(Items.fish, 1, ItemFishFood.FishType.PUFFERFISH.func_150976_a()), 13)});

	public final static FlickerOperatorFishing instance = new FlickerOperatorFishing();
	
	@Override
	public boolean RequiresPower(){
		return true;
	}

	@Override
	public int PowerPerOperation(){
		return 500;
	}

	@Override
	public boolean DoOperation(World world, IFlickerController<?> controller, boolean powered){
		return DoOperation(world, controller, powered, new Affinity[0]);
	}

	@Override
	public boolean DoOperation(World world, IFlickerController<?> controller, boolean powered, Affinity[] flickers){
		TileEntity te = (TileEntity)controller;
		if (!powered || !checkSurroundings(world, te.getPos()) || world.isBlockIndirectlyGettingPowered(te.getPos()) == 0)
			return false;
        LootContext.Builder lootcontext$builder = new LootContext.Builder((WorldServer)world);
		
		for (ItemStack itemstack : world.getLootTableManager().getLootTableFromLocation(LootTableList.GAMEPLAY_FISHING).generateLootForPools(world.rand, lootcontext$builder.build())) {
			transferOrEjectItem(world, itemstack, te.getPos());
		}

		return true;
	}

	private boolean checkSurroundings(World world, BlockPos pos){
		for (int i = -1; i <= 1; ++i){
			for (int j = -1; j >= -2; --j){
				for (int k = -1; k <= 1; ++k){
					Block block = world.getBlockState(pos.add(i, j, k)).getBlock();
					if (block != Blocks.WATER && block != Blocks.FLOWING_WATER)
						return false;
				}
			}
		}
		return true;
	}

	private void transferOrEjectItem(World world, ItemStack stack, BlockPos pos){
		if (world.isRemote)
			return;

		for (int i = -1; i <= 1; ++i){
			for (int j = -1; j <= 1; ++j){
				for (int k = -1; k <= 1; ++k){
					if (i == 0 && j == 0 && k == 0)
						continue;
					TileEntity te = world.getTileEntity(pos.add(i, j, k));
					if (te != null && te instanceof IInventory){
						for (EnumFacing facing : EnumFacing.values()){
							if (InventoryUtilities.mergeIntoInventory((IInventory)te, stack, stack.getCount(), facing))
								return;
						}
					}
				}
			}
		}

		//eject the remainder
		EntityItem item = new EntityItem(world);
		item.setPosition(pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5);
		item.setEntityItemStack(stack);
		world.spawnEntity(item);
	}

	@Override
	public void RemoveOperator(World world, IFlickerController<?> controller, boolean powered){
	}

	@Override
	public int TimeBetweenOperation(boolean powered, Affinity[] flickers){
		int time = 2000;
		for (Affinity aff : flickers){
			if (aff == Affinity.LIGHTNING)
				time *= 0.8;
		}
		return time;
	}

	@Override
	public void RemoveOperator(World world, IFlickerController<?> controller, boolean powered, Affinity[] flickers){
	}

	@Override
	public Object[] getRecipe(){
		return new Object[]{
				" F ",
				"N W",
				" R ",
				Character.valueOf('F'), Items.FISH,
				Character.valueOf('W'), new ItemStack(ItemDefs.flickerJar, 1, ArsMagicaAPI.getAffinityRegistry().getId(Affinity.WATER)),
				Character.valueOf('N'), new ItemStack(ItemDefs.flickerJar, 1, ArsMagicaAPI.getAffinityRegistry().getId(Affinity.NATURE)),
				Character.valueOf('R'), Items.FISHING_ROD
		};
	}
	
	@Override
	public ResourceLocation getTexture() {
		return new ResourceLocation("arsmagica2", "flicker_operator_fishing");
	}

	@Override
	public Affinity[] getMask() {
		return new Affinity[]{Affinity.WATER, Affinity.NATURE};
	}

}
