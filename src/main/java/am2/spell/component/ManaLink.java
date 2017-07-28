package am2.spell.component;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;

import am2.ArsMagica2;
import am2.api.affinity.Affinity;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellModifiers;
import am2.defs.BlockDefs;
import am2.defs.ItemDefs;
import am2.extensions.EntityExtension;
import am2.particles.AMLineArc;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ManaLink extends SpellComponent{

	@Override
	public Object[] getRecipe(){
		return new Object[]{
				BlockDefs.manaBattery,
				BlockDefs.essenceConduit,
				ItemDefs.crystalWrench,
				ItemDefs.manaFocus
		};
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target){
		if (target instanceof EntityLivingBase){
			EntityExtension.For((EntityLivingBase)target).updateManaLink(caster);
			return true;
		}
		return false;
	}
	
	@Override
	public EnumSet<SpellModifiers> getModifiers() {
		return EnumSet.noneOf(SpellModifiers.class);
	}

	
	@Override
	public float manaCost(EntityLivingBase caster){
		return 0;
	}

	@Override
	public ItemStack[] reagents(EntityLivingBase caster){
		return null;
	}

	@Override
	public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier){
		AMLineArc arc = (AMLineArc)ArsMagica2.proxy.particleManager.spawn(world, "textures/blocks/wipblock2.png", caster, target);
		if (arc != null){
			arc.setExtendToTarget();
			arc.setIgnoreAge(false);
			arc.setRBGColorF(0.17f, 0.88f, 0.88f);
		}
	}

	@Override
	public Set<Affinity> getAffinity(){
		return Sets.newHashSet(Affinity.LIGHTNING, Affinity.ENDER, Affinity.ARCANE);
	}

	@Override
	public float getAffinityShift(Affinity affinity){
		return 0.25f;
	}

	@Override
	public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {

		
	}

	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, BlockPos blockPos, EnumFacing blockFace,
			double impactX, double impactY, double impactZ, EntityLivingBase caster) {

		return false;
	}

}
