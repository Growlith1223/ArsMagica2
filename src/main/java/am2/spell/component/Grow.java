package am2.spell.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;

import am2.ArsMagica2;
import am2.api.affinity.Affinity;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellModifiers;
import am2.blocks.BlockAMFlower;
import am2.defs.BlockDefs;
import am2.defs.ItemDefs;
import am2.particles.AMParticle;
import am2.particles.ParticleFadeOut;
import am2.particles.ParticleFloatUpward;
import am2.particles.ParticleOrbitPoint;
import am2.utils.DummyEntityPlayer;
import net.minecraft.block.BlockMushroom;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;


public class Grow extends SpellComponent{

	private final static ArrayList<BlockAMFlower> growableAMflowers = new ArrayList<BlockAMFlower>(Arrays.asList(BlockDefs.cerublossom, BlockDefs.desertNova, BlockDefs.wakebloom, BlockDefs.aum, BlockDefs.tarmaRoot));

	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, BlockPos pos, EnumFacing blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster){

		IBlockState block = world.getBlockState(pos);

		BonemealEvent event = new BonemealEvent(DummyEntityPlayer.fromEntityLiving(caster), world, pos, world.getBlockState(pos));
		if (MinecraftForge.EVENT_BUS.post(event)){
			return false;
		}
		if (event.getResult() == Result.ALLOW){
			return true;
		}

		//EoD: Spawn AM2 flowers with 3% chance. This has to be the first one in the list to override all others
		if (world.rand.nextInt(100) < 3 && block.isNormalCube()){
			// shuffle the flower list every time we want to try to find one.
			Collections.shuffle(growableAMflowers);

			for (BlockAMFlower flower : growableAMflowers){
				if (flower.canGrowOn(world, pos.up())){
					if (!world.isRemote){
						world.setBlockState(pos.up(), flower.getDefaultState());
					}
					return true;
				}
			}
			// We did not find a flower or we have been executed on the wrong block. Either way, we continue
		}

		//Grow huge mushrooms 10% of the time.
		if (block.getBlock() instanceof BlockMushroom){
			if (!world.isRemote && world.rand.nextInt(10) < 1){
				((BlockMushroom)block.getBlock()).grow(world, world.rand, pos, block);
			}
			return true;
		}


		//If the spell is executed in water, check if we have space for a wakebloom above and create one 3% of the time.
		if (block.getBlock() == Blocks.WATER){
			if (world.getBlockState(pos.up()).getBlock() == Blocks.AIR){
				if (!world.isRemote && world.rand.nextInt(100) < 3){
					world.setBlockState(pos.up(), BlockDefs.wakebloom.getDefaultState());
				}
				return true;
			}
		}

		//EoD: If there is already tallgrass present, let's grow it further 20% of the time.
		if (block.getBlock() == Blocks.TALLGRASS){
			if (Blocks.TALLGRASS.canBlockStay(world, pos, Blocks.TALLGRASS.getDefaultState())){
				if (!world.isRemote && world.rand.nextInt(10) < 2){
					world.setBlockState(pos, Blocks.TALLGRASS.getDefaultState());
				}
				return true;
			}
		}

		//EoD: If there is already deadbush present, let's revitalize it 20% of the time.
		//     This works only on podzol in vanilla MC.
		if (block.getBlock() == Blocks.DEADBUSH){
			if (Blocks.DEADBUSH.canBlockStay(world, pos, Blocks.DEADBUSH.getDefaultState())){
				if (!world.isRemote && world.rand.nextInt(10) < 2){
					world.setBlockState(pos, Blocks.DEADBUSH.getDefaultState());
				}
				return true;
			}
		}

		// EoD: Apply vanilla bonemeal effect to growables 30% of the time. This is the generic grow section.
		//      See ItemDye.applyBonemeal().
		if (block instanceof IGrowable){
			IGrowable igrowable = (IGrowable)block;
			//AMCore.log.getLogger().info("Grow component found IGrowable");

			if (igrowable.canGrow(world, pos, block, world.isRemote)){
				if (!world.isRemote && world.rand.nextInt(10) < 3){
					if (igrowable.canUseBonemeal(world, world.rand, pos, block)){
						igrowable.grow(world, world.rand, pos, block);
					}
				}
				return true;
			}
		}

		return true;
	}
	
	@Override
	public EnumSet<SpellModifiers> getModifiers() {
		return EnumSet.noneOf(SpellModifiers.class);
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target){
		return false;
	}

	@Override
	public float manaCost(EntityLivingBase caster){
		return 17.4f;
	}

	@Override
	public ItemStack[] reagents(EntityLivingBase caster){
		return null;
	}

	@Override
	public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier){
		for (int i = 0; i < 25; ++i){
			AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "plant", x + 0.5, y + 1, z + 0.5);
			if (particle != null){
				particle.addRandomOffset(1, 1, 1);
				particle.AddParticleController(new ParticleFloatUpward(particle, 0, 0.1f, 1, false));
				particle.AddParticleController(new ParticleOrbitPoint(particle, x + 0.5, y + 0.5, z + 0.5, 2, false).setIgnoreYCoordinate(true).SetOrbitSpeed(0.1f).SetTargetDistance(0.3f + rand.nextDouble() * 0.3));
				particle.AddParticleController(new ParticleFadeOut(particle, 1, false).setFadeSpeed(0.05f).setKillParticleOnFinish(true));
				particle.setMaxAge(20);
				particle.setParticleScale(0.1f);
				if (colorModifier > -1){
					particle.setRGBColorF(((colorModifier >> 16) & 0xFF) / 255.0f, ((colorModifier >> 8) & 0xFF) / 255.0f, (colorModifier & 0xFF) / 255.0f);
				}
			}
		}
	}

	@Override
	public Set<Affinity> getAffinity(){
		return Sets.newHashSet(Affinity.NATURE);
	}

	@Override
	public Object[] getRecipe(){
		return new Object[]{
				new ItemStack(ItemDefs.rune, 1, EnumDyeColor.GREEN.getDyeDamage()),
				new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage()),
				//TODO BlocksCommonProxy.witchwoodLog
		};
	}

	@Override
	public float getAffinityShift(Affinity affinity){
		return 0.02f;
	}

	@Override
	public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {

		
	}
}
