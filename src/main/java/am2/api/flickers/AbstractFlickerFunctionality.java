package am2.api.flickers;

import am2.api.affinity.Affinity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;

public abstract class AbstractFlickerFunctionality extends IForgeRegistryEntry.Impl<AbstractFlickerFunctionality> {
	
	public abstract boolean RequiresPower();

	public abstract int PowerPerOperation();

	public abstract boolean DoOperation(World world, IFlickerController<?> controller, boolean powered);

	public abstract boolean DoOperation(World world, IFlickerController<?> controller, boolean powered, Affinity[] flickers);
	
	public abstract void RemoveOperator(World world, IFlickerController<?> controller, boolean powered);

	public abstract int TimeBetweenOperation(boolean powered, Affinity[] flickers);

	public abstract void RemoveOperator(World world, IFlickerController<?> controller, boolean powered, Affinity[] flickers);

	public abstract Object[] getRecipe();
	
	public abstract ResourceLocation getTexture();
	
	public abstract Affinity[] getMask();
}
