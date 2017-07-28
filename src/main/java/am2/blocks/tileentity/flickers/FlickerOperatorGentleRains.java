package am2.blocks.tileentity.flickers;

import am2.ArsMagica2;
import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import am2.api.flickers.IFlickerController;
import am2.api.flickers.AbstractFlickerFunctionality;
import am2.defs.BlockDefs;
import am2.defs.ItemDefs;
import am2.particles.AMParticle;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FlickerOperatorGentleRains extends AbstractFlickerFunctionality{

	public final static FlickerOperatorGentleRains instance = new FlickerOperatorGentleRains();
	
	@Override
	public boolean RequiresPower(){
		return false;
	}

	@Override
	public int PowerPerOperation(){
		return 0;
	}

	@Override
	public boolean DoOperation(World world, IFlickerController<?> habitat, boolean powered){
		int radius = 6;
		int diameter = radius * 2 + 1;
		if (!world.isRemote){
			int effectX = ((TileEntity)habitat).getPos().getX() - radius + (world.rand.nextInt(diameter));
			int effectZ = ((TileEntity)habitat).getPos().getZ() - radius + (world.rand.nextInt(diameter));
			int effectY = ((TileEntity)habitat).getPos().getY() - 1;
			
			BlockPos effectPos = new BlockPos(effectX, effectY, effectZ);

			while (world.isAirBlock(effectPos) && effectY > 0){
				effectY--;
			}

			while (!world.isAirBlock(effectPos) && world.getBlockState(effectPos).getBlock() != Blocks.FARMLAND && effectY > 0){
				effectY++;
			}

			effectY--;

			IBlockState block = world.getBlockState(effectPos);
			if (block.getBlock() == Blocks.FARMLAND && block.getValue(BlockFarmland.MOISTURE) < 7){
				world.setBlockState(effectPos, block.withProperty(BlockFarmland.MOISTURE, 7));
				return true;
			}
		}else{
			for (int i = 0; i < ArsMagica2.config.getGFXLevel() * 2; ++i){
				AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "water_ball", ((TileEntity)habitat).getPos().getX() + 0.5, ((TileEntity)habitat).getPos().getX() + 3, ((TileEntity)habitat).getPos().getX() + 0.5);
				if (particle != null){
					particle.setAffectedByGravity();
					particle.setMaxAge(10);
					particle.setDontRequireControllers();
					particle.setParticleScale(0.03f);
					particle.addRandomOffset(diameter, 0, diameter);
				}
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
	public int TimeBetweenOperation(boolean powered, Affinity[] flickers){
		return 1;
	}

	@Override
	public void RemoveOperator(World world, IFlickerController<?> habitat, boolean powered, Affinity[] flickers){
	}

	@Override
	public Object[] getRecipe(){
		return new Object[]{
				" B ",
				"CWT",
				" B ",
				Character.valueOf('C'), BlockDefs.essenceConduit,
				Character.valueOf('T'), BlockDefs.tarmaRoot,
				Character.valueOf('W'), new ItemStack(ItemDefs.flickerJar, 1, ArsMagicaAPI.getAffinityRegistry().getId(Affinity.WATER)),
				Character.valueOf('B'), new ItemStack(ItemDefs.rune, 1, EnumDyeColor.BLUE.getDyeDamage())
		};
	}
	
	@Override
	public ResourceLocation getTexture() {
		return new ResourceLocation("arsmagica2", "flicker_operator_gentle_rains");
	}

	@Override
	public Affinity[] getMask() {
		return new Affinity[]{Affinity.WATER};
	}


}
