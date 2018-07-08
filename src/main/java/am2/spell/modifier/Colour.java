package am2.spell.modifier;

import java.util.EnumSet;

import am2.api.spell.SpellModifier;
import am2.api.spell.SpellModifiers;
import am2.defs.ItemDefs;
import am2.items.ItemOre;
import am2.utils.NBTUtils;
import am2.utils.SpellUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public class Colour extends SpellModifier{
	
	@Override
	public Object[] getRecipe() {
		return new Object[]{
				new ItemStack(ItemDefs.itemOre, 1, ItemOre.META_CHIMERITE),
				new ItemStack(Items.DYE, 1, OreDictionary.WILDCARD_VALUE)
		};
	}

	@Override
	public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {
		for (Object obj : recipe) {
			if (obj instanceof ItemStack) {
				ItemStack is = (ItemStack) obj;
				if (is.getItem().equals(Items.DYE)) {
					NBTUtils.addTag(tag, SpellUtils.SPELL_DATA).setInteger("Color", is.getMetadata());
				}
			}
		}
	}
	@Override
	public EnumSet<SpellModifiers> getAspectsModified() {
		return EnumSet.of(SpellModifiers.COLOR);
	}

	@Override
	public float getModifier(SpellModifiers type, EntityLivingBase caster, Entity target, World world, NBTTagCompound nbt) {
		if (type == SpellModifiers.COLOR) {
			return NBTUtils.addTag(nbt, SpellUtils.SPELL_DATA).getInteger("Color");
		}
		return 0;
	}

	@Override
	public float getManaCostMultiplier(ItemStack spellStack, int stage, int quantity) {
		return 1F;
	}

}
