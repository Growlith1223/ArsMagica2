package am2.spell.modifier;

import java.util.EnumSet;

import am2.api.spell.SpellModifier;
import am2.api.spell.SpellModifiers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class MissingModifier extends SpellModifier{

	@Override
	public EnumSet<SpellModifiers> getAspectsModified(){
		return EnumSet.noneOf(SpellModifiers.class);
	}

	@Override
	public float getModifier(SpellModifiers type, EntityLivingBase caster, Entity target, World world, NBTTagCompound metadata){
		return 0;
	}

	@Override
	public Object[] getRecipe(){
		return null;
	}

	@Override
	public float getManaCostMultiplier(ItemStack spellStack, int stage, int quantity){
		return 1;
	}

	@Override
	public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {

		
	}
}
