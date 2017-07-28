package am2.blocks.tileentity.flickers;

import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import am2.api.flickers.IFlickerController;
import am2.api.flickers.AbstractFlickerFunctionality;
import am2.defs.ItemDefs;
import am2.utils.AffinityShiftUtils;
import am2.utils.InventoryUtilities;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FlickerOperatorPackedEarth extends AbstractFlickerFunctionality{
	
	public final static FlickerOperatorPackedEarth instance = new FlickerOperatorPackedEarth();

	@Override
	public boolean RequiresPower(){
		return false;
	}

	@Override
	public int PowerPerOperation(){
		return 10;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean DoOperation(World world, IFlickerController<?> habitat, boolean powered){
		int searchesPerLoop = 12;

		int radius = 6;
		int diameter = radius * 2 + 1;

		if (!world.isRemote){

			boolean actionPerformed = false;
			for (int i = 0; i < searchesPerLoop && !actionPerformed; ++i){
				TileEntity te = world.getTileEntity(((TileEntity)habitat).getPos().down());
				if (te == null || !(te instanceof IInventory)){
					return false;
				}
				
				BlockPos effectPos = ((TileEntity)habitat).getPos().add(-radius, -1, -radius).add(world.rand.nextInt(diameter), world.rand.nextInt(diameter), world.rand.nextInt(radius));
				
				if (effectPos.getY() < 3)
					effectPos = new BlockPos(effectPos.getX(), 3, effectPos.getY());

				Block block = world.getBlockState(effectPos).getBlock();

				if (world.isAirBlock(effectPos) || block.isReplaceable(world, effectPos)){
					int inventoryIndex = InventoryUtilities.getFirstBlockInInventory((IInventory)te);
					if (inventoryIndex > -1){
						ItemStack stack = ((IInventory)te).getStackInSlot(inventoryIndex);
						InventoryUtilities.decrementStackQuantity((IInventory)te, inventoryIndex, 1);
						world.setBlockState(effectPos, Block.getBlockFromItem(stack.getItem()).getStateFromMeta(stack.getItemDamage()));
						actionPerformed = true;
					}
				}
			}
		}

		return true;
	}

	@Override
	public boolean DoOperation(World world, IFlickerController<?> controller, boolean powered, Affinity[] flickers){
		return DoOperation(world, controller, powered);
	}

	@Override
	public void RemoveOperator(World world, IFlickerController<?> controller, boolean powered){
	}

	@Override
	public int TimeBetweenOperation(boolean powered, Affinity[] flickers){
		return powered ? 1 : 20;
	}

	@Override
	public void RemoveOperator(World world, IFlickerController<?> controller, boolean powered, Affinity[] flickers){
	}

	@Override
	public Object[] getRecipe(){
		return new Object[]{
				"DDD",
				"RFR",
				" E ",
				Character.valueOf('D'), Blocks.DIRT,
				Character.valueOf('R'), new ItemStack(ItemDefs.rune, 1, EnumDyeColor.BLACK.getDyeDamage()),
				Character.valueOf('E'), AffinityShiftUtils.getEssenceForAffinity(Affinity.EARTH),
				Character.valueOf('F'), new ItemStack(ItemDefs.flickerJar, 1, ArsMagicaAPI.getAffinityRegistry().getId(Affinity.EARTH))

		};
	}
	
	@Override
	public ResourceLocation getTexture() {
		return new ResourceLocation("arsmagica2", "flicker_operator_packed_earth");
	}

	@Override
	public Affinity[] getMask() {
		return new Affinity[]{Affinity.EARTH};
	}


}
