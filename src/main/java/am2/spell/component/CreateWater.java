package am2.spell.component;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;

import am2.api.affinity.Affinity;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellModifiers;
import am2.defs.ItemDefs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.BlockFlower;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CreateWater extends SpellComponent{

	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, BlockPos pos, EnumFacing blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster){
		if (world.provider.doesWaterVaporize()) return false;
		
		Block block = world.getBlockState(pos).getBlock();

		if (block == Blocks.CAULDRON){
			world.setBlockState(pos, Blocks.CAULDRON.getDefaultState().withProperty(BlockCauldron.LEVEL, 3));
			return true;
		}
		
		pos = pos.offset(blockFace);

		block = world.getBlockState(pos).getBlock();
		if (world.isAirBlock(pos) || block == Blocks.SNOW_LAYER || block == Blocks.WATER || block == Blocks.FLOWING_WATER || block instanceof BlockFlower){
			world.setBlockState(pos, Blocks.WATER.getDefaultState());
			return true;
		}
		return false;
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target){
		return false;
	}

	@Override
	public float manaCost(EntityLivingBase caster){
		return 25;
	}

	@Override
	public ItemStack[] reagents(EntityLivingBase caster){
		return null;
	}

	@Override
	public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier){
		for (int i = 0; i < 15; ++i){
			world.spawnParticle(EnumParticleTypes.WATER_SPLASH, x - 0.5 + rand.nextDouble(), y, z - 0.5 + rand.nextDouble(), 0.5 - rand.nextDouble(), 0.1, 0.5 - rand.nextDouble());
		}
	}

	@Override
	public Set<Affinity> getAffinity(){
		return Sets.newHashSet(Affinity.WATER);
	}

	@Override
	public Object[] getRecipe(){
		return new Object[]{
				new ItemStack(ItemDefs.rune, 1, EnumDyeColor.BLUE.getDyeDamage()),
				Items.WATER_BUCKET
		};
	}
	
	@Override
	public EnumSet<SpellModifiers> getModifiers() {
		return EnumSet.noneOf(SpellModifiers.class);
	}

	@Override
	public float getAffinityShift(Affinity affinity){
		return 0.001f;
	}

	@Override
	public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {}
}
