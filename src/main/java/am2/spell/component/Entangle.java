package am2.spell.component;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;

import am2.ArsMagica2;
import am2.api.affinity.Affinity;
import am2.api.blocks.MultiblockStructureDefinition;
import am2.api.rituals.IRitualInteraction;
import am2.api.rituals.RitualShapeHelper;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellModifiers;
import am2.buffs.BuffEffectEntangled;
import am2.defs.ItemDefs;
import am2.defs.PotionEffectsDefs;
import am2.particles.AMParticle;
import am2.particles.ParticleApproachEntity;
import am2.utils.SpellUtils;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Entangle extends SpellComponent implements IRitualInteraction{

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target){
		if (target instanceof EntityLivingBase){
			int duration = 140 + SpellUtils.getModifiedInt_Mul(PotionEffectsDefs.default_buff_duration / 10, stack, caster, target, world, SpellModifiers.DURATION);
			//duration = SpellUtils.modifyDurationBasedOnArmor(caster, duration);

			if (RitualShapeHelper.instance.matchesRitual(this, world, target.getPosition())){
				duration += (3600 * (SpellUtils.countModifiers(SpellModifiers.BUFF_POWER, stack) + 1));
				RitualShapeHelper.instance.consumeReagents(this, world, target.getPosition());
			}

			if (!world.isRemote)
				((EntityLivingBase)target).addPotionEffect(new BuffEffectEntangled(duration, SpellUtils.countModifiers(SpellModifiers.BUFF_POWER, stack)));
			return true;
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
	public EnumSet<SpellModifiers> getModifiers() {
		return EnumSet.of(SpellModifiers.DURATION, SpellModifiers.BUFF_POWER);
	}


	@Override
	public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier){
		for (int i = 0; i < 25; ++i){
			AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "plant", x, y, z);
			if (particle != null){
				particle.addRandomOffset(1, 2, 1);
				particle.AddParticleController(new ParticleApproachEntity(particle, target, 0.15f, 0.4f, 1, false));
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
				Blocks.VINE,
				Items.SLIME_BALL
		};
	}

	@Override
	public float getAffinityShift(Affinity affinity){
		return 0.05f;
	}

	@Override
	public MultiblockStructureDefinition getRitualShape(){
		return RitualShapeHelper.instance.hourglass;
	}

	@Override
	public ItemStack[] getReagents(){
		return new ItemStack[]{
				new ItemStack(Items.SLIME_BALL),
				new ItemStack(Blocks.WEB)
		};
	}

	@Override
	public int getReagentSearchRadius(){
		return 3;
	}
	
	@Override
	public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {

		
	}
	
	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, BlockPos blockPos, EnumFacing blockFace,
			double impactX, double impactY, double impactZ, EntityLivingBase caster) {

		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ItemStack getResult() {
		return null;
	}
}
