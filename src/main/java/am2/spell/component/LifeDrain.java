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
import am2.items.ItemOre;
import am2.particles.AMParticle;
import am2.particles.ParticleArcToEntity;
import am2.utils.SpellUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LifeDrain extends SpellComponent{

	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, BlockPos pos, EnumFacing blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster){
		return false;
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target){
		if (world.isRemote || !(target instanceof EntityLivingBase) || ((EntityLivingBase)target).getCreatureAttribute() == EnumCreatureAttribute.UNDEAD){
			return true;
		}
		int magnitude = SpellUtils.getModifiedInt_Add(4, stack, caster, target, world, SpellModifiers.DAMAGE);

		boolean success = SpellUtils.attackTargetSpecial(stack, target, DamageSource.causeIndirectMagicDamage(caster, caster), SpellUtils.modifyDamage(caster, magnitude));

		if (success){
			caster.heal((int)Math.ceil(magnitude / 4));
			return true;
		}

		return false;
	}

	@Override
	public float manaCost(EntityLivingBase caster){
		return 300;
	}

	@Override
	public ItemStack[] reagents(EntityLivingBase caster){
		return null;
	}

	@Override
	public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier){
		for (int i = 0; i < 15; ++i){
			AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "ember", x, y, z);
			if (particle != null){
				particle.addRandomOffset(1, 1, 1);
				particle.setIgnoreMaxAge(true);
				particle.AddParticleController(new ParticleArcToEntity(particle, 1, caster, false).SetSpeed(0.03f).generateControlPoints());
				particle.setRGBColorF(1, 0.2f, 0.2f);
				particle.SetParticleAlpha(0.5f);
				if (colorModifier > -1){
					particle.setRGBColorF(((colorModifier >> 16) & 0xFF) / 255.0f, ((colorModifier >> 8) & 0xFF) / 255.0f, (colorModifier & 0xFF) / 255.0f);
				}
			}
		}
	}
	
	@Override
	public EnumSet<SpellModifiers> getModifiers() {
		return EnumSet.of(SpellModifiers.DAMAGE);
	}


	@Override
	public Set<Affinity> getAffinity(){
		return Sets.newHashSet(Affinity.LIFE);
	}

	@Override
	public Object[] getRecipe(){
		return new Object[]{
				new ItemStack(ItemDefs.rune, 1, EnumDyeColor.BLACK.getDyeDamage()),
				new ItemStack(ItemDefs.itemOre, 1, ItemOre.META_SUNSTONE),
				//TODO BlocksCommonProxy.aum
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
