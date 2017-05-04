package am2.spell.component;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;

import am2.ArsMagica2;
import am2.api.DamageSources;
import am2.api.affinity.Affinity;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellModifiers;
import am2.defs.ItemDefs;
import am2.particles.AMParticle;
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

public class MagicDamage extends SpellComponent{

	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, BlockPos pos, EnumFacing blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster){
		return false;
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target){
		if (!(target instanceof EntityLivingBase)) return false;
		float baseDamage = 6;
		double damage = SpellUtils.getModifiedDouble_Add(baseDamage, stack, caster, target, world, SpellModifiers.DAMAGE);
		return SpellUtils.attackTargetSpecial(stack, target, DamageSources.causeMagicDamage(caster), SpellUtils.modifyDamage(caster, (float)damage));
	}

	@Override
	public float manaCost(EntityLivingBase caster){
		return 80;
	}

	@Override
	public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier){
		for (int i = 0; i < 5; ++i){
			AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "arcane", x, y, z);
			if (particle != null){
				particle.addRandomOffset(1, 0.5, 1);
				particle.addVelocity(rand.nextDouble() * 0.2 - 0.1, rand.nextDouble() * 0.2, rand.nextDouble() * 0.2 - 0.1);
				particle.setAffectedByGravity();
				particle.setDontRequireControllers();
				particle.setMaxAge(5);
				particle.setParticleScale(0.1f);
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
		return Sets.newHashSet(Affinity.ARCANE, Affinity.ENDER);
	}

	@Override
	public Object[] getRecipe(){
		return new Object[]{
				new ItemStack(ItemDefs.rune, 1, EnumDyeColor.PURPLE.getDyeDamage()),
				new ItemStack(Items.DYE, 1, 4),
				Items.BOOK,
				Items.STONE_SWORD
		};
	}

	@Override
	public float getAffinityShift(Affinity affinity){
		if (affinity == Affinity.ENDER)
			return 0.005f;
		return 0.01f;
	}

	@Override
	public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {}

	@Override
	public ItemStack[] reagents(EntityLivingBase caster) {
		return null;
	}
}
