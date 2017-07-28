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
import am2.defs.PotionEffectsDefs;
import am2.particles.AMParticle;
import am2.particles.ParticleFloatUpward;
import am2.utils.AffinityShiftUtils;
import am2.utils.DimensionUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class EnderIntervention extends SpellComponent{

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target){
		if (world.isRemote || !(target instanceof EntityLivingBase)) return true;

		if (((EntityLivingBase)target).isPotionActive(PotionEffectsDefs.astralDistortion)){
			if (target instanceof EntityPlayer)
				((EntityPlayer)target).sendMessage(new TextComponentString("The distortion around you prevents you from teleporting"));
			return true;
		}

		if (target.dimension == 1){
			if (target instanceof EntityPlayer)
				((EntityPlayer)target).sendMessage(new TextComponentString("Nothing happens..."));
			return true;
		}else if (target.dimension == -1){
			if (target instanceof EntityPlayer)
				((EntityPlayer)target).sendMessage(new TextComponentString("You are already in the nether."));
			return false;
		}else{
			DimensionUtilities.doDimensionTransfer((EntityLivingBase)target, -1);
			ArsMagica2.proxy.addDeferredDimensionTransfer((EntityLivingBase)target, -1);
		}

		return true;
	}
	
	@Override
	public EnumSet<SpellModifiers> getModifiers() {
		return EnumSet.noneOf(SpellModifiers.class);
	}
	
	@Override
	public float manaCost(EntityLivingBase caster){
		return 400;
	}

	@Override
	public ItemStack[] reagents(EntityLivingBase caster){
		return new ItemStack[]{AffinityShiftUtils.getEssenceForAffinity(Affinity.ENDER)};
	}

	@Override
	public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier){
		for (int i = 0; i < 100; ++i){
			AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "ghost", x, y - 1, z);
			if (particle != null){
				particle.addRandomOffset(1, 2, 1);
				particle.AddParticleController(new ParticleFloatUpward(particle, 0, 0.1f, 1, false));
				particle.setMaxAge(25 + rand.nextInt(10));
				particle.setRGBColorF(0.7f, 0.2f, 0.2f);
				if (colorModifier > -1){
					particle.setRGBColorF(((colorModifier >> 16) & 0xFF) / 255.0f, ((colorModifier >> 8) & 0xFF) / 255.0f, (colorModifier & 0xFF) / 255.0f);
				}
			}
		}
	}

	@Override
	public Set<Affinity> getAffinity(){
		return Sets.newHashSet(Affinity.ENDER);
	}

	@Override
	public Object[] getRecipe(){
		return new Object[]{
				new ItemStack(ItemDefs.rune, 1, EnumDyeColor.PURPLE.getDyeDamage()),
				Blocks.OBSIDIAN,
				Blocks.OBSIDIAN,
				Items.FLINT_AND_STEEL,
				Items.ENDER_PEARL
		};
	}

	@Override
	public float getAffinityShift(Affinity affinity){
		return 0.4f;
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
