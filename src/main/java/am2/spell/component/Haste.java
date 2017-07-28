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
import am2.buffs.BuffEffectHaste;
import am2.defs.ItemDefs;
import am2.defs.PotionEffectsDefs;
import am2.particles.AMParticle;
import am2.particles.ParticleFadeOut;
import am2.particles.ParticleFloatUpward;
import am2.particles.ParticleOrbitPoint;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Haste extends SpellComponent implements IRitualInteraction{

	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, BlockPos pos, EnumFacing blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster){
		return false;
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target){
		if (target instanceof EntityLivingBase){
			int duration = SpellUtils.getModifiedInt_Mul(PotionEffectsDefs.default_buff_duration, stack, caster, target, world, SpellModifiers.DURATION);
			//duration = SpellUtils.modifyDurationBasedOnArmor(caster, duration);

			if (RitualShapeHelper.instance.matchesRitual(this, world, target.getPosition())){
				duration += (3600 * (SpellUtils.countModifiers(SpellModifiers.BUFF_POWER, stack) + 1));
				RitualShapeHelper.instance.consumeReagents(this, world, target.getPosition());
			}

			if (!world.isRemote)
				((EntityLivingBase)target).addPotionEffect(new BuffEffectHaste(duration, SpellUtils.countModifiers(SpellModifiers.BUFF_POWER, stack)));
			return true;
		}
		return false;
	}
	
	@Override
	public EnumSet<SpellModifiers> getModifiers() {
		return EnumSet.of(SpellModifiers.DURATION, SpellModifiers.BUFF_POWER);
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
			AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "mystic", x, y - 1, z);
			if (particle != null){
				particle.addRandomOffset(1, 1, 1);
				particle.AddParticleController(new ParticleFloatUpward(particle, 0, 0.1f, 1, false));
				particle.AddParticleController(new ParticleOrbitPoint(particle, x, y, z, 2, false).setIgnoreYCoordinate(true).SetOrbitSpeed(0.1f).SetTargetDistance(0.3f + rand.nextDouble() * 0.3));
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
		return Sets.newHashSet(Affinity.LIGHTNING);
	}

	@Override
	public Object[] getRecipe(){
		return new Object[]{
				new ItemStack(ItemDefs.rune, 1, EnumDyeColor.YELLOW.getDyeDamage()),
				Items.REDSTONE,
				Items.GLOWSTONE_DUST
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
				new ItemStack(Items.GLOWSTONE_DUST)
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
	@SideOnly(Side.CLIENT)
	public ItemStack getResult() {
		return null;
	}
}
