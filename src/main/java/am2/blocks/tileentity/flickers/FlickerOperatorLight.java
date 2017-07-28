package am2.blocks.tileentity.flickers;

import am2.ArsMagica2;
import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import am2.api.flickers.IFlickerController;
import am2.api.flickers.AbstractFlickerFunctionality;
import am2.blocks.BlockInvisibleUtility;
import am2.blocks.BlockInvisibleUtility.EnumInvisibleType;
import am2.defs.BlockDefs;
import am2.defs.ItemDefs;
import am2.items.ItemOre;
import am2.packet.AMDataReader;
import am2.packet.AMDataWriter;
import am2.particles.AMParticle;
import am2.particles.ParticleFadeOut;
import am2.particles.ParticleFloatUpward;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

public class FlickerOperatorLight extends AbstractFlickerFunctionality{
	
	public final static FlickerOperatorLight instance = new FlickerOperatorLight();

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
		if (!world.isRemote){
			int radius = 16;
			int yRadius = radius / 4;
			int checksPerOperation = 8;
			
			BlockPos checkPos = ((TileEntity)habitat).getPos().add(-radius, -yRadius, -radius);

			byte[] meta = habitat.getMetadata(this);

			if (meta.length != 0){
				AMDataReader rdr = new AMDataReader(meta, false);
				checkPos = new BlockPos(rdr.getInt(), rdr.getInt(), rdr.getInt());
			}

			for (int i = 0; i < checksPerOperation; ++i){

				int light = world.getLightFor(EnumSkyBlock.BLOCK, checkPos);

				if (light < 10 && world.isAirBlock(checkPos)){
					world.setBlockState(checkPos, BlockDefs.invisibleUtility.getDefaultState().withProperty(BlockInvisibleUtility.TYPE, EnumInvisibleType.SPECIAL_ILLUMINATED), 2);
				}

				checkPos.east();
				if (checkPos.getX() > ((TileEntity)habitat).getPos().getX() + radius){
					checkPos = new BlockPos(((TileEntity)habitat).getPos().getX() - radius, checkPos.getY(), checkPos.getZ());
					checkPos = checkPos.up();
					if (checkPos.getY() > ((TileEntity)habitat).getPos().getY() + yRadius){
						checkPos = new BlockPos(checkPos.getX(), ((TileEntity)habitat).getPos().getY() - yRadius, checkPos.getZ());
						checkPos = checkPos.south();
						if (checkPos.getZ() > ((TileEntity)habitat).getPos().getZ() + yRadius){
							checkPos = new BlockPos(checkPos.getX(), checkPos.getY(), ((TileEntity)habitat).getPos().getZ() - radius);
						}
					}
				}
			}

			AMDataWriter writer = new AMDataWriter();
			writer.add(checkPos.getX()).add(checkPos.getY()).add(checkPos.getZ());

			habitat.setMetadata(this, writer.generate());
		}else{
			AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "sparkle", ((TileEntity)habitat).getPos().getX() + 0.5, ((TileEntity)habitat).getPos().getY() + 1, ((TileEntity)habitat).getPos().getZ() + 0.5);
			if (particle != null){
				particle.addRandomOffset(0.5, 0.4, 0.5);
				particle.AddParticleController(new ParticleFloatUpward(particle, 0, 0.02f, 1, false));
				particle.AddParticleController(new ParticleFadeOut(particle, 1, false).setFadeSpeed(0.05f));
				particle.setMaxAge(20);
			}
		}

		return true;
	}

	@Override
	public boolean DoOperation(World world, IFlickerController<?> habitat, boolean powered, Affinity[] flickers){
		return DoOperation(world, habitat, powered);
	}

	@Override
	public void RemoveOperator(World world, IFlickerController<?> habitat, boolean powered){
		habitat.removeMetadata(this);

		if (!world.isRemote){
			int radius = 28;
			int yRadius = radius / 4;

			for (int i = ((TileEntity)habitat).getPos().getX() - radius; i <= ((TileEntity)habitat).getPos().getX() + radius; ++i){
				for (int j = ((TileEntity)habitat).getPos().getY() - yRadius; j <= ((TileEntity)habitat).getPos().getY() + yRadius; ++j){
					for (int k = ((TileEntity)habitat).getPos().getY() - radius; k <= ((TileEntity)habitat).getPos().getY() + radius; ++k){
						BlockPos removePos = new BlockPos(i, j, k);
						Block block = world.getBlockState(removePos).getBlock();
						if (block == BlockDefs.invisibleUtility){
							if (BlockInvisibleUtility.getType(world.getBlockState(removePos)) == EnumInvisibleType.SPECIAL_ILLUMINATED){
								world.setBlockToAir(removePos);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public int TimeBetweenOperation(boolean powered, Affinity[] flickers){
		return 10;
	}

	@Override
	public void RemoveOperator(World world, IFlickerController<?> habitat, boolean powered, Affinity[] flickers){
		RemoveOperator(world, habitat, powered);
	}


	@Override
	public Object[] getRecipe(){
		return new Object[]{
				"ISI",
				"F L",
				"ISI",
				Character.valueOf('F'), new ItemStack(ItemDefs.flickerJar, 1, ArsMagicaAPI.getAffinityRegistry().getId(Affinity.FIRE)),
				Character.valueOf('S'), new ItemStack(ItemDefs.itemOre, 1, ItemOre.META_MOONSTONE),
				Character.valueOf('L'), new ItemStack(ItemDefs.flickerJar, 1, ArsMagicaAPI.getAffinityRegistry().getId(Affinity.LIGHTNING)),
				Character.valueOf('I'), ItemDefs.liquidEssenceBottle

		};
	}
	
	@Override
	public ResourceLocation getTexture() {
		return new ResourceLocation("arsmagica2", "flicker_operator_light");
	}

	@Override
	public Affinity[] getMask() {
		return new Affinity[]{Affinity.FIRE, Affinity.LIGHTNING};
	}

}
