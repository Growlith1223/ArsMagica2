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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class Wave extends SpellShape{

	@Override
	public Object[] getRecipe(){
		return new Object[]{
				new ItemStack(ItemDefs.itemOre, 1, ItemOre.META_VINTEUM),
				BlockDefs.magicWall,
				"E:*", 25000
		};
	}

	@Override
	public SpellCastResult beginStackStage(ItemSpellBase item, ItemStack stack, EntityLivingBase caster, EntityLivingBase target, World world, double x, double y, double z, EnumFacing side, boolean giveXP, int useCount){
		if (world.isRemote) return SpellCastResult.SUCCESS;
		double radius = SpellUtils.getModifiedDouble_Add(1, stack, caster, target, world, SpellModifiers.RADIUS);
		int duration = SpellUtils.getModifiedInt_Mul(20, stack, caster, target, world, SpellModifiers.DURATION);
		double speed = SpellUtils.getModifiedDouble_Add(1f, stack, caster, target, world, SpellModifiers.SPEED) * 0.5f;
		int gravityModifiers = SpellUtils.countModifiers(SpellModifiers.GRAVITY, stack);
		boolean hasPiercing = SpellUtils.modifierIsPresent(SpellModifiers.PIERCING, stack);

		EntitySpellEffect wave = new EntitySpellEffect(world);
		wave.setRadius((float)radius);
		wave.setTicksToExist(duration);
		wave.SetCasterAndStack(caster, stack);
		wave.setPosition(x, y + 1, z);
		wave.setWave(caster.rotationYaw, (float)speed);
		wave.noClip = hasPiercing;
		wave.setGravity(gravityModifiers * 0.5f);
		world.spawnEntity(wave);
		return SpellCastResult.SUCCESS;
	}
	
	@Override
	public EnumSet<SpellModifiers> getModifiers() {
		return EnumSet.of(SpellModifiers.RADIUS, SpellModifiers.GRAVITY, SpellModifiers.DURATION, SpellModifiers.COLOR, SpellModifiers.SPEED, SpellModifiers.PIERCING, SpellModifiers.TARGET_NONSOLID_BLOCKS);
	}

	@Override
	public boolean isChanneled(){
		return false;
	}

	@Override
	public float manaCostMultiplier(ItemStack spellStack){
		return 3f;
	}

	@Override
	public boolean isTerminusShape(){
		return false;
	}

	@Override
	public boolean isPrincipumShape(){
		return true;
	}

	@Override
	public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {}

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
}
