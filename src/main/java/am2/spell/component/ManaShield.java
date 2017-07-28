package am2.spell.component;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;

import am2.api.affinity.Affinity;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellModifiers;
import am2.defs.ItemDefs;
import am2.extensions.EntityExtension;
import am2.utils.SpellUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ManaShield extends SpellComponent{

	@Override
	public Object[] getRecipe(){
		return new Object[]{
				ItemDefs.manaFocus,
				ItemDefs.battlemageArmor,
				ItemDefs.mageArmor
		};
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target) {
		float consumed = EntityExtension.For(caster).getCurrentMana();
		EntityExtension.For(caster).deductMana(consumed);
		EntityExtension.For((EntityLivingBase) target).addMagicShielding((consumed / 250) * SpellUtils.getModifiedInt_Add(1, stack, caster, target, world, SpellModifiers.BUFF_POWER));
		return true;
	}
	
	@Override
	public EnumSet<SpellModifiers> getModifiers() {
		return EnumSet.of(SpellModifiers.BUFF_POWER);
	}

	
	@Override
	public float manaCost(EntityLivingBase caster){
		return 0;
	}
	
	@Override
	public float burnout(EntityLivingBase caster) {
		return 0;
	}
	
	@Override
	public ItemStack[] reagents(EntityLivingBase caster){
		return null;
	}

	@Override
	public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier){
	}

	@Override
	public Set<Affinity> getAffinity(){
		return Sets.newHashSet(Affinity.ARCANE);
	}

	@Override
	public float getAffinityShift(Affinity affinity){
		return 0.01f;
	}
	
	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, BlockPos blockPos, EnumFacing blockFace,
			double impactX, double impactY, double impactZ, EntityLivingBase caster) {

		return false;
	}
	
	@Override
	public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {

		
	}
}
