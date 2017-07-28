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
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class FlickerOperatorButchery extends AbstractFlickerFunctionality{

	public final static FlickerOperatorButchery instance = new FlickerOperatorButchery();

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
		HashMap<Class<?>, Integer> entityCount = new HashMap<>();
		int radius = 6;
		List<EntityAnimal> creatures = world.getEntitiesWithinAABB(EntityAnimal.class, new AxisAlignedBB(((TileEntity)habitat).getPos()).expandXyz(radius));
		for (EntityAnimal creature : creatures){
			Class<? extends EntityAnimal> clazz = creature.getClass();
			if (!SpawnBlacklists.canButcheryAffect(clazz))
				continue;
			Integer count = entityCount.get(clazz);
			if (count == null)
				count = 0;
			if (!creature.isChild())
				count++;
			entityCount.put(clazz, count);
			if (count > 2){
				if (world.isRemote){
					AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "ghost", ((TileEntity)habitat).getPos().getX() + 0.5, ((TileEntity)habitat).getPos().getY() + 0.7, ((TileEntity)habitat).getPos().getZ() + 0.5);
					if (particle != null){
						particle.setMaxAge(20);
						particle.AddParticleController(new ParticleFloatUpward(particle, 0, 0.05f, 1, false));
					}
				}else{
					creature.attackEntityFrom(DamageSource.GENERIC, 500);
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean DoOperation(World world, IFlickerController<?> habitat, boolean powered, Affinity[] flickers){
		return DoOperation(world, habitat, powered);
	}

	@Override
	public void RemoveOperator(World world, IFlickerController<?> habitat, boolean powered){
	}

	@Override
	public void RemoveOperator(World world, IFlickerController<?> habitat, boolean powered, Affinity[] flickers){
	}

	@Override
	public int TimeBetweenOperation(boolean powered, Affinity[] flickers){
		return 600;
	}


	@Override
	public Object[] getRecipe(){
		return new Object[]{
				"PBC",
				"FGL",
				"RER",
				Character.valueOf('P'), new ItemStack(Items.PORKCHOP),
				Character.valueOf('B'), new ItemStack(Items.BEEF),
				Character.valueOf('C'), new ItemStack(Items.CHICKEN),
				Character.valueOf('F'), new ItemStack(ItemDefs.flickerJar, 1, ArsMagicaAPI.getAffinityRegistry().getId(Affinity.FIRE)),
				Character.valueOf('G'), new ItemStack(Items.GOLDEN_SWORD),
				Character.valueOf('L'), new ItemStack(ItemDefs.flickerJar, 1, ArsMagicaAPI.getAffinityRegistry().getId(Affinity.LIFE)),
				Character.valueOf('R'), new ItemStack(ItemDefs.rune, 1, EnumDyeColor.RED.getDyeDamage()),
				Character.valueOf('E'), new ItemStack(ItemDefs.evilBook)
		};
	}

	@Override
	public ResourceLocation getTexture() {
		return new ResourceLocation("arsmagica2", "flicker_operator_butchery");
	}

	@Override
	public Affinity[] getMask() {
		return new Affinity[]{Affinity.FIRE, Affinity.LIFE};
	}
}
