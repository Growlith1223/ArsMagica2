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
import am2.buffs.BuffEffectWateryGrave;
import am2.defs.ItemDefs;
import am2.defs.PotionEffectsDefs;
import am2.particles.AMParticle;
import am2.particles.ParticleFadeOut;
import am2.particles.ParticleFloatUpward;
import am2.particles.ParticleLeaveParticleTrail;
import am2.particles.ParticleOrbitEntity;
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

public class WateryGrave extends SpellComponent implements IRitualInteraction{

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target){
		if (target instanceof EntityLivingBase){
			int duration = SpellUtils.getModifiedInt_Mul(PotionEffectsDefs.default_buff_duration, stack, caster, target, world, SpellModifiers.DURATION);
			//duration = SpellUtils.instance.modifyDurationBasedOnArmor(caster, duration);
			if (RitualShapeHelper.instance.matchesRitual(this, world, target.getPosition())){
				duration += (3600 * (SpellUtils.countModifiers(SpellModifiers.BUFF_POWER, stack) + 1));
				RitualShapeHelper.instance.consumeReagents(this, world, target.getPosition());
			}

			if (!world.isRemote)
				((EntityLivingBase)target).addPotionEffect(new BuffEffectWateryGrave(duration, SpellUtils.countModifiers(SpellModifiers.BUFF_POWER, stack)));
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
	public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier){
		for (int i = 0; i < 25; ++i){
			AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "water_ball", x, y, z);
			if (particle != null){
				particle.addRandomOffset(1, 2, 1);
				particle.AddParticleController(new ParticleOrbitEntity(particle, target, 0.2f, 1, false));
				particle.AddParticleController(new ParticleFadeOut(particle, 1, false).setFadeSpeed(0.05f).setKillParticleOnFinish(true));
				particle.AddParticleController(new ParticleLeaveParticleTrail(particle, "water_ball", false, 5, 2, false)
						.addControllerToParticleList(new ParticleFloatUpward(particle, 0, -0.3f, 1, false))
						.setParticleRGB_F(1, 1, 1)
						.setTicksBetweenSpawns(2));
				particle.setMaxAge(20);
				particle.setParticleScale(0.01f);
				if (colorModifier > -1){
					particle.setRGBColorF(((colorModifier >> 16) & 0xFF) / 255.0f, ((colorModifier >> 8) & 0xFF) / 255.0f, (colorModifier & 0xFF) / 255.0f);
				}
			}
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
				Blocks.STONE,
				Items.LEATHER_BOOTS
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
				new ItemStack(Blocks.STONE)
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
	public EnumSet<SpellModifiers> getModifiers() {
		return EnumSet.of(SpellModifiers.BUFF_POWER, SpellModifiers.DURATION);
	}

	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, BlockPos blockPos, EnumFacing blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster) {

		return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public ItemStack getResult() {
		return null;
	}
}
