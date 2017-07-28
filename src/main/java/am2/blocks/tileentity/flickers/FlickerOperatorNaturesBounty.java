package am2.blocks.tileentity.flickers;

import am2.ArsMagica2;
import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import am2.api.flickers.IFlickerController;
import am2.api.flickers.AbstractFlickerFunctionality;
import am2.defs.ItemDefs;
import am2.particles.AMParticle;
import am2.particles.ParticleFloatUpward;
import am2.utils.AffinityShiftUtils;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

public class FlickerOperatorNaturesBounty extends AbstractFlickerFunctionality{
	
	public final static FlickerOperatorNaturesBounty instance = new FlickerOperatorNaturesBounty();

	@Override
	public boolean RequiresPower(){
		return false;
	}

	@Override
	public int PowerPerOperation(){
		return 5;
	}

	@Override
	public boolean DoOperation(World world, IFlickerController<?> habitat, boolean powered){
		return DoOperation(world, habitat, powered, new Affinity[0]);
	}

	@Override
	public boolean DoOperation(World world, IFlickerController<?> habitat, boolean powered, Affinity[] flickers){
		int radius = 6;
		int diameter = radius * 2 + 1;
		boolean updatedOnce = false;
		if (!world.isRemote){
			for (int i = 0; i < (powered ? 5 : 1); ++i){
				BlockPos effectPos = ((TileEntity)habitat).getPos().add(- radius + (world.rand.nextInt(diameter)), 0, - radius + (world.rand.nextInt(diameter)));

				while (world.isAirBlock(effectPos) && effectPos.getY() > 0){
					effectPos = effectPos.down();
				}

				while (!world.isAirBlock(effectPos) && effectPos.getY() > 0){
					effectPos = effectPos.up();
				}

				effectPos.down();


				Block block = world.getBlockState(effectPos).getBlock();
				if (block instanceof IPlantable || block instanceof IGrowable){
					block.updateTick(world, effectPos, world.getBlockState(effectPos), world.rand);
					updatedOnce = true;
				}
			}
		}else{
			int posY = ((TileEntity)habitat).getPos().getY();
			while (!world.isAirBlock(new BlockPos(((TileEntity)habitat).getPos().getX(), posY, ((TileEntity)habitat).getPos().getZ()))){
				posY++;
			}
			posY--;
			for (int i = 0; i < ArsMagica2.config.getGFXLevel() * 2; ++i){
				AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "plant", ((TileEntity)habitat).getPos().getX() + 0.5, posY + 0.5f, ((TileEntity)habitat).getPos().getZ() + 0.5);
				if (particle != null){

					particle.addRandomOffset(diameter, 0, diameter);
					particle.AddParticleController(new ParticleFloatUpward(particle, 0.01f, 0.04f, 1, false));
					particle.setMaxAge(16);
					particle.setParticleScale(0.08f);
				}
			}
		}

		if (powered){
			for (Affinity aff : flickers){
				if (aff == Affinity.WATER)
					FlickerOperatorGentleRains.instance.DoOperation(world, habitat, powered);
			}
		}

		return updatedOnce;
	}

	@Override
	public void RemoveOperator(World world, IFlickerController<?> controller, boolean powered){
	}

	@Override
	public int TimeBetweenOperation(boolean powered, Affinity[] flickers){
		return powered ? 1 : 100;
	}

	@Override
	public void RemoveOperator(World world, IFlickerController<?> controller, boolean powered, Affinity[] flickers){
	}

	@Override
	public Object[] getRecipe(){
		return new Object[]{
				"BAB",
				"LNW",
				"BGB",
				Character.valueOf('B'), new ItemStack(Items.DYE, 1, 15),
				Character.valueOf('G'), new ItemStack(ItemDefs.rune, 1, EnumDyeColor.GREEN.getDyeDamage()),
				Character.valueOf('N'), AffinityShiftUtils.getEssenceForAffinity(Affinity.NATURE),
				Character.valueOf('L'), new ItemStack(ItemDefs.flickerJar, 1, ArsMagicaAPI.getAffinityRegistry().getId(Affinity.LIFE)),
				Character.valueOf('A'), new ItemStack(ItemDefs.flickerJar, 1, ArsMagicaAPI.getAffinityRegistry().getId(Affinity.NATURE)),
				Character.valueOf('W'), new ItemStack(ItemDefs.flickerJar, 1, ArsMagicaAPI.getAffinityRegistry().getId(Affinity.WATER))

		};
	}
	
	@Override
	public ResourceLocation getTexture() {
		return new ResourceLocation("arsmagica2", "flicker_operator_natures_bounty");
	}

	@Override
	public Affinity[] getMask() {
		return new Affinity[]{Affinity.NATURE, Affinity.WATER, Affinity.LIFE};
	}


}
