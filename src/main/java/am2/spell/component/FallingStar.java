package am2.spell.component;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;

import am2.api.affinity.Affinity;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellModifiers;
import am2.defs.BlockDefs;
import am2.defs.ItemDefs;
import am2.entity.EntityThrownRock;
import am2.items.ItemOre;
import am2.utils.AffinityShiftUtils;
import am2.utils.SpellUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FallingStar extends SpellComponent{

	@Override
	public Object[] getRecipe(){
		return new Object[]{
				AffinityShiftUtils.getEssenceForAffinity(Affinity.ARCANE),
				new ItemStack(ItemDefs.itemOre, 1, ItemOre.META_ARCANEASH),
				AffinityShiftUtils.getEssenceForAffinity(Affinity.ARCANE),
				BlockDefs.manaBattery,
				Items.LAVA_BUCKET
		};
	}

	private boolean spawnStar(ItemStack spellStack, EntityLivingBase caster, Entity target, World world, double x, double y, double z){

		List<EntityThrownRock> rocks = world.getEntitiesWithinAABB(EntityThrownRock.class, new AxisAlignedBB(x - 10, y - 10, z - 10, x + 10, y + 10, z + 10));

		int damageMultitplier = SpellUtils.getModifiedInt_Mul(15, spellStack, caster, target, world, SpellModifiers.DAMAGE);
		for (EntityThrownRock rock : rocks){
			if (rock.getIsShootingStar())
				return false;
		}

		if (!world.isRemote){
			EntityThrownRock star = new EntityThrownRock(world);
			star.setPosition(x, world.getActualHeight(), z);
			star.setShootingStar(2 * damageMultitplier);
			star.setThrowingEntity(caster);
			star.setSpellStack(spellStack.copy());
			world.spawnEntity(star);
		}
		return true;
	}
	
	@Override
	public EnumSet<SpellModifiers> getModifiers() {
		return EnumSet.of(SpellModifiers.DAMAGE, SpellModifiers.COLOR);
	}


	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, BlockPos pos, EnumFacing blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster){
		return spawnStar(stack, caster, caster, world, impactX, impactY + 50, impactZ);
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target){
		return spawnStar(stack, caster, target, world, target.posX, target.posY + 50, target.posZ);
	}

	@Override
	public float manaCost(EntityLivingBase caster){
		return 400;
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
		return Sets.newHashSet(Affinity.ARCANE);
	}

	@Override
	public float getAffinityShift(Affinity affinity){
		return 0.05f;
	}
	
	@Override
	public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {

		
	}

}
