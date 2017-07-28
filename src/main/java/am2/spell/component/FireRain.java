package am2.spell.component;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;

import am2.api.affinity.Affinity;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellModifiers;
import am2.defs.ItemDefs;
import am2.entity.EntitySpellEffect;
import am2.items.ItemOre;
import am2.utils.AffinityShiftUtils;
import am2.utils.SpellUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FireRain extends SpellComponent{

	@Override
	public Object[] getRecipe(){
		return new Object[]{
				AffinityShiftUtils.getEssenceForAffinity(Affinity.FIRE),
				new ItemStack(ItemDefs.itemOre, 1, ItemOre.META_ARCANEASH),
				Blocks.NETHERRACK,
				AffinityShiftUtils.getEssenceForAffinity(Affinity.FIRE),
				Items.LAVA_BUCKET
		};
	}

	private boolean spawnFireRain(ItemStack stack, World world, EntityLivingBase caster, Entity target, double x, double y, double z){

		List<EntitySpellEffect> zones = world.getEntitiesWithinAABB(EntitySpellEffect.class, new AxisAlignedBB(x - 10, y - 10, z - 10, x + 10, y + 10, z + 10));

		for (EntitySpellEffect zone : zones){
			if (zone.isRainOfFire())
				return false;
		}

		if (!world.isRemote){
			int radius = SpellUtils.getModifiedInt_Add(2, stack, caster, target, world, SpellModifiers.RADIUS) / 2 + 1;
			double damage = SpellUtils.getModifiedDouble_Mul(1, stack, caster, target, world, SpellModifiers.DAMAGE);
			int duration = SpellUtils.getModifiedInt_Mul(100, stack, caster, target, world, SpellModifiers.DURATION);

			EntitySpellEffect fire = new EntitySpellEffect(world);
			fire.setPosition(x, y, z);
			fire.setRainOfFire(false);
			fire.setRadius(radius);
			fire.setDamageBonus((float)damage);
			fire.setTicksToExist(duration);
			fire.SetCasterAndStack(caster, stack);
			world.spawnEntity(fire);
		}
		return true;
	}
	
	@Override
	public EnumSet<SpellModifiers> getModifiers() {
		return EnumSet.of(SpellModifiers.RADIUS, SpellModifiers.DAMAGE, SpellModifiers.DURATION, SpellModifiers.COLOR);
	}
	
	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, BlockPos pos, EnumFacing blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster){
		return spawnFireRain(stack, world, caster, caster, impactX, impactY, impactZ);
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target){
		return spawnFireRain(stack, world, caster, target, target.posX, target.posY, target.posZ);
	}

	@Override
	public float manaCost(EntityLivingBase caster){
		return 3000;
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
		return Sets.newHashSet(Affinity.FIRE);
	}

	@Override
	public float getAffinityShift(Affinity affinity){
		return 0.1f;
	}
	
	@Override
	public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {

		
	}

}
