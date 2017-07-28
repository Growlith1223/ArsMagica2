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
import am2.buffs.BuffEffectCharmed;
import am2.defs.ItemDefs;
import am2.defs.PotionEffectsDefs;
import am2.extensions.EntityExtension;
import am2.items.ItemCrystalPhylactery;
import am2.particles.AMParticle;
import am2.particles.ParticleFloatUpward;
import am2.utils.AffinityShiftUtils;
import am2.utils.EntityUtils;
import am2.utils.SpellUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Charm extends SpellComponent implements IRitualInteraction{

	@Override
	public Object[] getRecipe(){
		return new Object[]{
				new ItemStack(ItemDefs.rune, 1, EnumDyeColor.RED.getDyeDamage()),
				AffinityShiftUtils.getEssenceForAffinity(Affinity.LIFE),
				new ItemStack(ItemDefs.crystalPhylactery, 1, ItemCrystalPhylactery.META_EMPTY)
		};
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target){
		if (!(target instanceof EntityCreature) || ((EntityCreature)target).isPotionActive(PotionEffectsDefs.charme) || EntityUtils.isSummon((EntityCreature)target)){
			return false;
		}

		int duration = SpellUtils.getModifiedInt_Mul(PotionEffectsDefs.default_buff_duration, stack, caster, target, world, SpellModifiers.DURATION);
		//duration = SpellUtils.modifyDurationBasedOnArmor(caster, duration);

		if (RitualShapeHelper.instance.matchesRitual(this, world, target.getPosition())){
			duration += (3600 * (SpellUtils.countModifiers(SpellModifiers.BUFF_POWER, stack) + 1));
			RitualShapeHelper.instance.consumeReagents(this, world, target.getPosition());
		}

		if (target instanceof EntityAnimal){
			((EntityAnimal)target).setInLove(null);
			return true;
		}

		if (EntityExtension.For(caster).getCanHaveMoreSummons()){
			if (caster instanceof EntityPlayer){
				if (target instanceof EntityCreature){
					BuffEffectCharmed charmBuff = new BuffEffectCharmed(duration, BuffEffectCharmed.CHARM_TO_PLAYER);
					charmBuff.setCharmer(caster);
					((EntityCreature)target).addPotionEffect(charmBuff);
				}
				return true;
			}else if (caster instanceof EntityLiving){
				if (target instanceof EntityCreature){
					BuffEffectCharmed charmBuff = new BuffEffectCharmed(duration, BuffEffectCharmed.CHARM_TO_MONSTER);
					charmBuff.setCharmer(caster);
					((EntityCreature)target).addPotionEffect(charmBuff);
				}
				return true;
			}
		}else{
			if (caster instanceof EntityPlayer){
				((EntityPlayer)caster).sendMessage(new TextComponentString("You cannot have any more summons."));
			}
			return true;
		}
		return false;
	}
	
	@Override
	public EnumSet<SpellModifiers> getModifiers() {
		return EnumSet.of(SpellModifiers.DURATION);
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
		for (int i = 0; i < 10; ++i){
			AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "heart", x, y, z);
			if (particle != null){
				particle.addRandomOffset(1, 2, 1);
				particle.AddParticleController(new ParticleFloatUpward(particle, 0, 0.05f + rand.nextFloat() * 0.1f, 1, false));
				particle.setMaxAge(20);
				if (colorModifier > -1){
					particle.setRGBColorF(((colorModifier >> 16) & 0xFF) / 255.0f, ((colorModifier >> 8) & 0xFF) / 255.0f, (colorModifier & 0xFF) / 255.0f);
				}
			}
		}
	}

	@Override
	public Set<Affinity> getAffinity(){
		return Sets.newHashSet(Affinity.LIFE);
	}

	@Override
	public float getAffinityShift(Affinity affinity){
		return 0.1f;
	}

	@Override
	public MultiblockStructureDefinition getRitualShape(){
		return RitualShapeHelper.instance.hourglass;
	}

	@Override
	public ItemStack[] getReagents(){
		return new ItemStack[]{
				new ItemStack(Items.WHEAT),
				new ItemStack(Items.WHEAT_SEEDS),
				new ItemStack(Items.CARROT)
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
