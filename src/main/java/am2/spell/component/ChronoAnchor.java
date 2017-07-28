package am2.spell.component;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;

import am2.ArsMagica2;
import am2.api.affinity.Affinity;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellModifiers;
import am2.buffs.BuffEffectTemporalAnchor;
import am2.defs.ItemDefs;
import am2.defs.PotionEffectsDefs;
import am2.particles.AMParticle;
import am2.particles.ParticleFadeOut;
import am2.particles.ParticleOrbitEntity;
import am2.utils.SpellUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ChronoAnchor extends SpellComponent{

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target){
		if (target instanceof EntityLivingBase){
			int duration = SpellUtils.getModifiedInt_Mul(PotionEffectsDefs.default_buff_duration, stack, caster, target, world, SpellModifiers.DURATION);
			//duration = SpellUtils.modifyDurationBasedOnArmor(caster, duration);
			if (!world.isRemote) {
				if (((EntityLivingBase)target).getActivePotionEffects().contains(BuffEffectTemporalAnchor.class))
					((EntityLivingBase)target).getActivePotionEffects().remove(BuffEffectTemporalAnchor.class);
				((EntityLivingBase) target).addPotionEffect(new BuffEffectTemporalAnchor(duration, SpellUtils.countModifiers(SpellModifiers.BUFF_POWER, stack)));
				return true;
			}
		}
		return false;
	}

	@Override
	public float manaCost(EntityLivingBase caster){
		return 80;
	}

	@Override
	public ItemStack[] reagents(EntityLivingBase caster){
		return null;
	}

	@Override
	public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier){
		for (int i = 0; i < 25; ++i){
			AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "clock", x, y, z);
			if (particle != null){
				particle.addRandomOffset(1, 2, 1);
				particle.setParticleScale(0.1f);
				particle.AddParticleController(new ParticleOrbitEntity(particle, target, 0.1f + rand.nextFloat() * 0.1f, 1, false));
				particle.AddParticleController(new ParticleFadeOut(particle, 1, false).setFadeSpeed(0.05f));
				particle.setMaxAge(40);
				if (colorModifier > -1){
					particle.setRGBColorF(((colorModifier >> 16) & 0xFF) / 255.0f, ((colorModifier >> 8) & 0xFF) / 255.0f, (colorModifier & 0xFF) / 255.0f);
				}
			}
		}
	}

	@Override
	public Set<Affinity> getAffinity(){
		return Sets.newHashSet(Affinity.ARCANE);
	}
	
	@Override
	public Object[] getRecipe(){
		return new Object[]{
				new ItemStack(ItemDefs.rune, 1, EnumDyeColor.WHITE.getDyeDamage()),
				Items.CLOCK,
				Items.NETHER_STAR
		};
	}
	
	@Override
	public EnumSet<SpellModifiers> getModifiers() {
		return EnumSet.of(SpellModifiers.DURATION, SpellModifiers.BUFF_POWER);
	}
	
	@Override
	public float getAffinityShift(Affinity affinity){
		return 0.15f;
	}

	@Override
	public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {}

	@Override
	public boolean applyEffectBlock(ItemStack stack, World world,
			BlockPos blockPos, EnumFacing blockFace, double impactX,
			double impactY, double impactZ, EntityLivingBase caster) {

		return false;
	}
}
