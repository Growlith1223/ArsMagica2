package am2.spell.shape;

import java.util.EnumSet;

import am2.api.spell.SpellModifiers;
import am2.api.spell.SpellShape;
import am2.defs.BlockDefs;
import am2.defs.ItemDefs;
import am2.entity.EntitySpellEffect;
import am2.items.ItemOre;
import am2.items.ItemSpellBase;
import am2.spell.SpellCastResult;
import am2.utils.SpellUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class Zone extends SpellShape{

	@Override
	public SpellCastResult beginStackStage(ItemSpellBase item, ItemStack stack, EntityLivingBase caster, EntityLivingBase target, World world, double x, double y, double z, EnumFacing side, boolean giveXP, int useCount){
		if (world.isRemote) return SpellCastResult.SUCCESS;
		int radius = SpellUtils.getModifiedInt_Add(2, stack, caster, target, world, SpellModifiers.RADIUS);
		double gravity = SpellUtils.getModifiedDouble_Add(0, stack, caster, target, world, SpellModifiers.GRAVITY);
		int duration = SpellUtils.getModifiedInt_Mul(100, stack, caster, target, world, SpellModifiers.DURATION);
		EntitySpellEffect zone = new EntitySpellEffect(world);
		zone.setRadius(radius);
		zone.setTicksToExist(duration);
		zone.setGravity(gravity);
		zone.SetCasterAndStack(caster, stack);
		zone.setPosition(x, y, z);
		world.spawnEntity(zone);
		return SpellCastResult.SUCCESS;
	}

	@Override
	public boolean isChanneled(){
		return false;
	}
	
	@Override
	public EnumSet<SpellModifiers> getModifiers() {
		return EnumSet.of(SpellModifiers.RADIUS, SpellModifiers.GRAVITY, SpellModifiers.DURATION, SpellModifiers.COLOR, SpellModifiers.TARGET_NONSOLID_BLOCKS);
	}

	@Override
	public Object[] getRecipe(){
		return new Object[]{
				BlockDefs.tarmaRoot,
				new ItemStack(ItemDefs.itemOre, 1, ItemOre.META_MOONSTONE),
				new ItemStack(ItemDefs.itemOre, 1, ItemOre.META_SUNSTONE),
				Items.DIAMOND
		};
	}

	@Override
	public float manaCostMultiplier(ItemStack spellStack){
		return 4.5f;
	}

	@Override
	public boolean isTerminusShape(){
		return false;
	}

	@Override
	public boolean isPrincipumShape(){
		return true;
	}

//	@Override
//	public String getSoundForAffinity(Affinity affinity, ItemStack stack, World world){
//		switch (affinity){
//		case AIR:
//			return "arsmagica2:spell.cast.air";
//		case ARCANE:
//			return "arsmagica2:spell.cast.arcane";
//		case EARTH:
//			return "arsmagica2:spell.cast.earth";
//		case ENDER:
//			return "arsmagica2:spell.cast.ender";
//		case FIRE:
//			return "arsmagica2:spell.cast.fire";
//		case ICE:
//			return "arsmagica2:spell.cast.ice";
//		case LIFE:
//			return "arsmagica2:spell.cast.life";
//		case LIGHTNING:
//			return "arsmagica2:spell.cast.lightning";
//		case NATURE:
//			return "arsmagica2:spell.cast.nature";
//		case WATER:
//			return "arsmagica2:spell.cast.water";
//		case NONE:
//		default:
//			return "arsmagica2:spell.cast.none";
//		}
//	}

	@Override
	public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {}
}
