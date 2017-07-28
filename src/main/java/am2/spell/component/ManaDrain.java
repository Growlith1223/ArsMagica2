package am2.spell.component;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;

import am2.ArsMagica2;
import am2.api.affinity.Affinity;
import am2.api.extensions.IEntityExtension;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellModifiers;
import am2.defs.ItemDefs;
import am2.extensions.EntityExtension;
import am2.items.ItemOre;
import am2.particles.AMParticle;
import am2.particles.ParticleArcToEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ManaDrain extends SpellComponent{

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target){
		if (!(target instanceof EntityLivingBase)) return false;

		double manaStolen = 250;
		IEntityExtension targetProperties = EntityExtension.For((EntityLivingBase)target);
		if (manaStolen > targetProperties.getCurrentMana()){
			manaStolen = targetProperties.getCurrentMana();
		}
		targetProperties.setCurrentMana((float)(targetProperties.getCurrentMana() - manaStolen));
		IEntityExtension casterProperties = EntityExtension.For(caster);
		casterProperties.setCurrentMana((float)(casterProperties.getCurrentMana() + manaStolen));
		return true;
	}
	
	@Override
	public EnumSet<SpellModifiers> getModifiers() {
		return EnumSet.noneOf(SpellModifiers.class);
	}


	@Override
	public float manaCost(EntityLivingBase caster){
		return 20;
	}

	@Override
	public ItemStack[] reagents(EntityLivingBase caster){
		return null;
	}

	@Override
	public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier){
		for (int i = 0; i < 15; ++i){
			AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "sparkle2", x, y, z);
			if (particle != null){
				particle.addRandomOffset(1, 1, 1);
				particle.setIgnoreMaxAge(true);
				particle.AddParticleController(new ParticleArcToEntity(particle, 1, caster, false).SetSpeed(0.03f).generateControlPoints());
				particle.setRGBColorF(0, 0.4f, 1);
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
				new ItemStack(ItemDefs.rune, 1, EnumDyeColor.BLACK.getDyeDamage()),
				new ItemStack(ItemDefs.itemOre, 1, ItemOre.META_MOONSTONE),
				new ItemStack(ItemDefs.itemOre, 1, ItemOre.META_VINTEUM),
		};
	}

	@Override
	public float getAffinityShift(Affinity affinity){
		return 0.01f;
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
