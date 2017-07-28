package am2.spell.modifier;

import java.util.EnumSet;

import am2.api.affinity.Affinity;
import am2.api.spell.SpellModifier;
import am2.api.spell.SpellModifiers;
import am2.defs.ItemDefs;
import am2.items.ItemOre;
import am2.utils.AffinityShiftUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class Lunar extends SpellModifier{

	@Override
	public EnumSet<SpellModifiers> getAspectsModified(){
		return EnumSet.of(SpellModifiers.RANGE, SpellModifiers.RADIUS, SpellModifiers.DAMAGE, SpellModifiers.DURATION, SpellModifiers.HEALING);
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public float getModifier(SpellModifiers type, EntityLivingBase caster, Entity target, World world, NBTTagCompound metadata){
		switch (type){
		case RANGE:
			return modifyValueOnLunarCycle(world, 3f);
		case RADIUS:
			return modifyValueOnLunarCycle(world, 3);
		case DAMAGE:
			return modifyValueOnTime(world, 2.4f);
		case DURATION:
			return modifyValueOnTime(world, 5f);
		case HEALING:
			return modifyValueOnTime(world, 2f);
		}
		return 1.0f;
	}

	private float modifyValueOnTime(World world, float value){
		long x = world.provider.getWorldTime() % 24000;
		float multiplierFromTime = (float)(Math.sin(((x / 4600f) * (x / 21000f) - 900f) * (180f / Math.PI)) * 3f) + 1;
		if (multiplierFromTime < 0)
			multiplierFromTime *= -0.5f;
		return value * multiplierFromTime;
	}

	private float modifyValueOnLunarCycle(World world, float value){
		long boundedTime = world.provider.getWorldTime() % 24000;
		int phase = 8 - world.provider.getMoonPhase(world.getWorldInfo().getWorldTime());
		if (boundedTime > 12500 && boundedTime < 23500){
			return value + (phase / 2);
		}
		return Math.abs(value - 1);
	}

	@Override
	public Object[] getRecipe(){
		return new Object[]{
				AffinityShiftUtils.getEssenceForAffinity(Affinity.NATURE),
				new ItemStack(ItemDefs.itemOre, 1, ItemOre.META_MOONSTONE),
				Items.CLOCK
		};
	}

	@Override
	public float getManaCostMultiplier(ItemStack spellStack, int stage, int quantity){
		return 4.0f * quantity;
	}

	@Override
	public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {

		
	}
}
