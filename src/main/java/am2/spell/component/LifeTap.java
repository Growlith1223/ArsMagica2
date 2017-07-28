package am2.spell.component;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;

import am2.ArsMagica2;
import am2.api.affinity.Affinity;
import am2.api.blocks.MultiblockStructureDefinition;
import am2.api.extensions.IEntityExtension;
import am2.api.rituals.IRitualInteraction;
import am2.api.rituals.RitualShapeHelper;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellModifiers;
import am2.defs.BlockDefs;
import am2.defs.ItemDefs;
import am2.extensions.EntityExtension;
import am2.particles.AMParticle;
import am2.particles.ParticleApproachEntity;
import am2.utils.AffinityShiftUtils;
import am2.utils.SpellUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LifeTap extends SpellComponent implements IRitualInteraction{

	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, BlockPos pos, EnumFacing blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster){

		if (world.getBlockState(pos).getBlock().equals(Blocks.MOB_SPAWNER)){
			boolean hasMatch = RitualShapeHelper.instance.matchesRitual(this, world, pos);
			if (hasMatch){
				if (!world.isRemote){
					world.setBlockToAir(pos);
					RitualShapeHelper.instance.consumeReagents(this, world, pos);
					RitualShapeHelper.instance.consumeShape(this, world, pos);
					EntityItem item = new EntityItem(world);
					item.setPosition(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
					item.setEntityItemStack(new ItemStack(BlockDefs.inertSpawner));
					world.spawnEntity(item);
				}else{

				}

				return true;
			}
		}

		return false;
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target){
		if (!(target instanceof EntityLivingBase)) return false;
		if (!world.isRemote){
			double damage = SpellUtils.getModifiedDouble_Mul(2, stack, caster, target, world, SpellModifiers.DAMAGE);
			IEntityExtension casterProperties = EntityExtension.For(caster);
			float manaRefunded = (float)(((damage * 0.01)) * casterProperties.getMaxMana());

			if ((caster).attackEntityFrom(DamageSource.OUT_OF_WORLD, (int)Math.floor(damage))){
				casterProperties.setCurrentMana(casterProperties.getCurrentMana() + manaRefunded);
			}else{
				return false;
			}
		}
		return true;
	}
	
	@Override
	public EnumSet<SpellModifiers> getModifiers() {
		return EnumSet.of(SpellModifiers.DAMAGE);
	}


	@Override
	public float manaCost(EntityLivingBase caster){
		return 0;
	}

	@Override
	public ItemStack[] reagents(EntityLivingBase caster){
		return null;
	}

	@Override
	public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier){
		for (int i = 0; i < 25; ++i){
			AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "sparkle2", x, y, z);
			if (particle != null){
				particle.addRandomOffset(2, 2, 2);
				particle.setMaxAge(15);
				particle.setParticleScale(0.1f);
				particle.AddParticleController(new ParticleApproachEntity(particle, target, 0.1, 0.1, 1, false));
				if (rand.nextBoolean())
					particle.setRGBColorF(0.4f, 0.1f, 0.5f);
				else
					particle.setRGBColorF(0.1f, 0.5f, 0.1f);
				if (colorModifier > -1){
					particle.setRGBColorF(((colorModifier >> 16) & 0xFF) / 255.0f, ((colorModifier >> 8) & 0xFF) / 255.0f, (colorModifier & 0xFF) / 255.0f);
				}
			}
		}
	}

	@Override
	public Set<Affinity> getAffinity(){
		return Sets.newHashSet(Affinity.LIFE, Affinity.ENDER);
	}

	@Override
	public Object[] getRecipe(){
		return new Object[]{
				new ItemStack(ItemDefs.rune, 1, EnumDyeColor.BLACK.getDyeDamage()),
				BlockDefs.aum
		};
	}

	@Override
	public float getAffinityShift(Affinity affinity){
		return 0.01f;
	}

	@Override
	public MultiblockStructureDefinition getRitualShape(){
		return RitualShapeHelper.instance.corruption;
	}

	@Override
	public ItemStack[] getReagents(){
		return new ItemStack[]{
				new ItemStack(ItemDefs.mobFocus),
				AffinityShiftUtils.getEssenceForAffinity(Affinity.ENDER)
		};
	}

	@Override
	public int getReagentSearchRadius(){
		return 3;
	}

	@Override
	public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {}
	
	@Override
	@SideOnly(Side.CLIENT)
	public ItemStack getResult() {
		return new ItemStack(BlockDefs.inertSpawner);
	}
}
