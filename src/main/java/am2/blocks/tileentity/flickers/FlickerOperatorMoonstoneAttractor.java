package am2.blocks.tileentity.flickers;

import java.util.ArrayList;

import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import am2.api.flickers.IFlickerController;
import am2.api.flickers.AbstractFlickerFunctionality;
import am2.api.math.AMVector3;
import am2.blocks.BlockArsMagicaOre;
import am2.defs.BlockDefs;
import am2.defs.ItemDefs;
import am2.utils.AffinityShiftUtils;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class FlickerOperatorMoonstoneAttractor extends AbstractFlickerFunctionality{
	
	public final static FlickerOperatorMoonstoneAttractor instance = new FlickerOperatorMoonstoneAttractor();

	private static final ArrayList<AMVector3> attractors = new ArrayList<AMVector3>();

	public static AMVector3 getMeteorAttractor(AMVector3 target){
		for (AMVector3 attractor : attractors.toArray(new AMVector3[attractors.size()])){
			if (attractor.distanceSqTo(target) <= 16384)
				return attractor.copy();
		}
		return null;
	}

	@Override
	public boolean RequiresPower(){
		return true;
	}

	@Override
	public int PowerPerOperation(){
		return 10;
	}

	@Override
	public boolean DoOperation(World world, IFlickerController<?> habitat, boolean powered){
		AMVector3 vec = new AMVector3((TileEntity)habitat);
		if (powered){
			if (!attractors.contains(vec)){
				attractors.add(vec);
			}
			return true;
		}else{
			attractors.remove(vec);
		}
		return false;
	}

	@Override
	public boolean DoOperation(World world, IFlickerController<?> habitat, boolean powered, Affinity[] flickers){
		return DoOperation(world, habitat, powered);
	}

	@Override
	public void RemoveOperator(World world, IFlickerController<?> habitat, boolean powered){
		AMVector3 vec = new AMVector3((TileEntity)habitat);
		attractors.remove(vec);
	}

	@Override
	public int TimeBetweenOperation(boolean powered, Affinity[] flickers){
		return 100;
	}

	@Override
	public void RemoveOperator(World world, IFlickerController<?> habitat, boolean powered, Affinity[] flickers){
		RemoveOperator(world, habitat, powered);
	}

	@Override
	public Object[] getRecipe(){
		return new Object[]{
				"RLR",
				"AME",
				"I T",
				Character.valueOf('R'), new ItemStack(ItemDefs.rune, 1, EnumDyeColor.ORANGE.getDyeDamage()),
				Character.valueOf('L'), new ItemStack(ItemDefs.flickerJar, 1, ArsMagicaAPI.getAffinityRegistry().getId(Affinity.LIGHTNING)),
				Character.valueOf('A'), new ItemStack(ItemDefs.flickerJar, 1, ArsMagicaAPI.getAffinityRegistry().getId(Affinity.ARCANE)),
				Character.valueOf('E'), new ItemStack(ItemDefs.flickerJar, 1, ArsMagicaAPI.getAffinityRegistry().getId(Affinity.EARTH)),
				Character.valueOf('M'), new ItemStack(BlockDefs.ores, 1, BlockArsMagicaOre.EnumOreType.MOONSTONE.ordinal()),
				Character.valueOf('I'), AffinityShiftUtils.getEssenceForAffinity(Affinity.AIR),
				Character.valueOf('T'), AffinityShiftUtils.getEssenceForAffinity(Affinity.EARTH)
		};
	}
	
	@Override
	public ResourceLocation getTexture() {
		return new ResourceLocation("arsmagica2", "flicker_operator_moonstone_attractor");
	}

	@Override
	public Affinity[] getMask() {
		return new Affinity[] {Affinity.LIGHTNING, Affinity.ARCANE, Affinity.EARTH};
	}


}
