package am2.spell.component;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;

import am2.api.affinity.Affinity;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellModifiers;
import am2.extensions.EntityExtension;
import am2.utils.SpellUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Dig extends SpellComponent {

	@Override
	public Object[] getRecipe() {
		return new Object[] {
			new ItemStack(Items.IRON_SHOVEL),
			new ItemStack(Items.IRON_AXE),
			new ItemStack(Items.IRON_PICKAXE)
		};
	}

	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, BlockPos blockPos, EnumFacing blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster) {
		if (!(caster instanceof EntityPlayer))
			return false;
		stack.getTagCompound().setBoolean("ArsMagica2.harvestByProjectile", true);
		if (world.isRemote)
			return true;
		IBlockState state = world.getBlockState(blockPos);
		float hardness = state.getBlockHardness(world, blockPos);
		if (hardness != -1 && state.getBlock().getHarvestLevel(state) <= SpellUtils.getModifiedInt_Add(2, stack, caster, null, world, SpellModifiers.MINING_POWER)) {
			if (SpellUtils.modifierIsPresent(SpellModifiers.SILKTOUCH_LEVEL, stack)) {
				ItemStack blockstack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
				EntityItem blockitem = new EntityItem(world, blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockstack);
	            blockitem.setDefaultPickupDelay();
	            //items were flying at insane speeds, so I'm toning it down a bit
	            blockitem.motionX = (double)((float)(Math.random() * 0.020000000298023224D - 0.01000000149011612D));
	            blockitem.motionY = 0.01;
	            blockitem.motionZ = (double)((float)(Math.random() * 0.02000000298023224D - 0.01000000149011612D));
	            world.spawnEntityInWorld(blockitem);
			}else{
				int xp = state.getBlock().getExpDrop(state, world, blockPos, SpellUtils.countModifiers(SpellModifiers.FORTUNE_LEVEL, stack));
				state.getBlock().dropBlockAsItem(world, blockPos, state, SpellUtils.countModifiers(SpellModifiers.FORTUNE_LEVEL, stack));
				state.getBlock().dropXpOnBlockBreak(world, blockPos, xp);
			}
			world.destroyBlock(blockPos, false);
			EntityExtension.For(caster).deductMana(hardness * 1.28f);
		}
		return true;
	}

	@Override
	public EnumSet<SpellModifiers> getModifiers() {
		return EnumSet.of(SpellModifiers.FORTUNE_LEVEL, SpellModifiers.MINING_POWER);
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world,
			EntityLivingBase caster, Entity target) {
		return false;
	}

	@Override
	public float manaCost(EntityLivingBase caster) {
		return 10;
	}

	@Override
	public ItemStack[] reagents(EntityLivingBase caster) {
		return null;
	}

	@Override
	public void spawnParticles(World world, double x, double y, double z,
			EntityLivingBase caster, Entity target, Random rand,
			int colorModifier) {
	}

	@Override
	public Set<Affinity> getAffinity() {
		return Sets.newHashSet(Affinity.EARTH);
	}

	@Override
	public float getAffinityShift(Affinity affinity) {
		return 0.001F;
	}

	@Override
	public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {
	}

}