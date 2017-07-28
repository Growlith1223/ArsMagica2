package am2.blocks.tileentity.flickers;

import java.util.HashMap;
import java.util.List;

import am2.ArsMagica2;
import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import am2.api.flickers.IFlickerController;
import am2.api.flickers.AbstractFlickerFunctionality;
import am2.defs.ItemDefs;
import am2.entity.SpawnBlacklists;
import am2.particles.AMParticle;
import am2.particles.ParticleFloatUpward;
import am2.utils.AffinityShiftUtils;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class FlickerOperatorProgeny extends AbstractFlickerFunctionality{

	private static final int BASE_COOLDOWN = 2000;
	private static final float COOLDOWN_BONUS = 0.75f;
	private static final int COOLDOWN_MINIMUM = 600; //minimum 30 seconds
	
	public final static FlickerOperatorProgeny instance = new FlickerOperatorProgeny();

	@Override
	public boolean RequiresPower(){
		return true;
	}

	@Override
	public int PowerPerOperation(){
		return 500;
	}

	@Override
	public boolean DoOperation(World world, IFlickerController<?> habitat, boolean powered){
		HashMap<Class<? extends EntityAnimal>, Integer> entityCount = new HashMap<>();
		int radius = 8;
		List<EntityAnimal> creatures = world.getEntitiesWithinAABB(EntityAnimal.class, new AxisAlignedBB(((TileEntity)habitat).getPos()).expandXyz(radius));
		for (EntityAnimal creature : creatures){
			Class<? extends EntityAnimal> clazz = creature.getClass();
			if (!SpawnBlacklists.canProgenyAffect(clazz))
				continue;
			Integer count = entityCount.get(clazz);
			if (count == null)
				count = 0;
			if (!creature.isInLove() && creature.getGrowingAge() == 0)
				count++;
			entityCount.put(clazz, count);
			if (count == 2){
				if (world.isRemote){
					AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "heart", ((TileEntity)habitat).getPos().getX() + 0.5, ((TileEntity)habitat).getPos().getX() + 0.7, ((TileEntity)habitat).getPos().getX() + 0.5);
					if (particle != null){
						particle.setMaxAge(20);
						particle.AddParticleController(new ParticleFloatUpward(particle, 0, 0.05f, 1, false));
					}
				}else{
					creatures = world.getEntitiesWithinAABB(clazz, new AxisAlignedBB(((TileEntity)habitat).getPos()).expandXyz(radius));
					count = 0;
					for (EntityAnimal animal : creatures){
						if (!animal.isChild()){
							animal.setInLove(null);
							count++;
							if (count == 2)
								break;
						}
					}
				}
				return true;
			}
		}

		return false;
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
		if (powered){
			float base = BASE_COOLDOWN;
			for (Affinity aff : flickers)
				if (aff == Affinity.LIGHTNING)
					base *= COOLDOWN_BONUS;

			return (int)Math.max(base, COOLDOWN_MINIMUM);
		}else{
			return BASE_COOLDOWN;
		}
	}

	@Override
	public void RemoveOperator(World world, IFlickerController<?> controller, boolean powered, Affinity[] flickers){
	}

	@Override
	public Object[] getRecipe(){
		return new Object[]{
				"ELE",
				"EFE",
				"EWE",
				Character.valueOf('E'), Items.EGG,
				Character.valueOf('L'), AffinityShiftUtils.getEssenceForAffinity(Affinity.LIFE),
				Character.valueOf('F'), new ItemStack(ItemDefs.flickerJar, 1, ArsMagicaAPI.getAffinityRegistry().getId(Affinity.LIFE)),
				Character.valueOf('W'), new ItemStack(ItemDefs.rune, 1, EnumDyeColor.WHITE.getDyeDamage())

		};
	}
	
	@Override
	public ResourceLocation getTexture() {
		return new ResourceLocation("arsmagica2", "flicker_operator_progeny");
	}

	@Override
	public Affinity[] getMask() {
		return new Affinity[] {Affinity.LIFE};
	}


}
