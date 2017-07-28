package am2.spell.component;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;

import am2.ArsMagica2;
import am2.api.affinity.Affinity;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellModifiers;
import am2.defs.ItemDefs;
import am2.particles.AMParticle;
import net.minecraft.block.Block;
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

public class Plow extends SpellComponent{

	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, BlockPos pos, EnumFacing blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster){
		Block block = world.getBlockState(pos).getBlock();
		//Block blockAbove = world.getBlockState(pos.up()).getBlock();
		if ((block == Blocks.DIRT || block == Blocks.GRASS)){
			/*if (!(blockAbove instanceof BlockFlower)){
				return false;
			}
			blockAbove.breakBlock(world, blockx, blocky+1, blockz, blockAbove, 0);
			world.setBlock(blockx, blocky+1, blockz, Blocks.air);*/
			if (!world.isRemote){
				world.setBlockState(pos, Blocks.FARMLAND.getDefaultState());
			}
			return true;
		}
		return false;
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
		return 75;
	}

	@Override
	public ItemStack[] reagents(EntityLivingBase caster){
		return null;
	}

	@Override
	public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier){
		for (int i = 0; i < 10; ++i){
			AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "rock", x, y + 1, z);
			if (particle != null){
				particle.addRandomOffset(1, 1, 1);
				particle.addVelocity(rand.nextDouble() * 0.2 - 0.1, 0.2f, rand.nextDouble() * 0.2 - 0.1);
				particle.setDontRequireControllers();
				particle.setAffectedByGravity();
				particle.setMaxAge(20);
				particle.setParticleScale(0.05f);
				if (colorModifier > -1){
					particle.setRGBColorF(((colorModifier >> 16) & 0xFF) / 255.0f, ((colorModifier >> 8) & 0xFF) / 255.0f, (colorModifier & 0xFF) / 255.0f);
				}
			}
		}
	}

	@Override
	public Set<Affinity> getAffinity(){
		return Sets.newHashSet(Affinity.EARTH);
	}

	@Override
	public Object[] getRecipe(){
		return new Object[]{
				new ItemStack(ItemDefs.rune, 1, EnumDyeColor.GREEN.getDyeDamage()),
				Items.STONE_HOE
		};
	}

	@Override
	public float getAffinityShift(Affinity affinity){
		return 0.01f;
	}

	@Override
	public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {

		
	}
}
