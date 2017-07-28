package am2.spell.component;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;

import am2.ArsMagica2;
import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import am2.api.blocks.MultiblockStructureDefinition;
import am2.api.rituals.IRitualInteraction;
import am2.api.rituals.RitualShapeHelper;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellModifiers;
import am2.buffs.BuffEffectAstralDistortion;
import am2.defs.BlockDefs;
import am2.defs.ItemDefs;
import am2.defs.PotionEffectsDefs;
import am2.particles.AMParticle;
import am2.particles.ParticleFloatUpward;
import am2.utils.SpellUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AstralDistortion extends SpellComponent implements IRitualInteraction{

	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, BlockPos pos, EnumFacing facing, double impactX, double impactY, double impactZ, EntityLivingBase caster){

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
		if (target instanceof EntityLivingBase){
			int duration = (int) SpellUtils.getModifiedInt_Mul(PotionEffectsDefs.default_buff_duration, stack, caster, target, world, SpellModifiers.DURATION);
			//duration = SpellUtils.modifyDurationBasedOnArmor(caster, duration);

			if (!world.isRemote)
				((EntityLivingBase)target).addPotionEffect(new BuffEffectAstralDistortion(duration, SpellUtils.countModifiers(SpellModifiers.BUFF_POWER, stack)));
			return true;
		}
		return false;
	}
	
	@Override
	public EnumSet<SpellModifiers> getModifiers() {
		return EnumSet.of(SpellModifiers.BUFF_POWER, SpellModifiers.DURATION);
	}


	@Override
	public float manaCost(EntityLivingBase caster){
		return 80;
	}

	@Override
	public ItemStack[] reagents(EntityLivingBase caster){
		return null;
	}

	@Override
	public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier){
		for (int i = 0; i < 10; ++i){
			AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "pulse", x, y, z);
			if (particle != null){
				particle.addRandomOffset(5, 4, 5);
				particle.AddParticleController(new ParticleFloatUpward(particle, 0.2f, 0, 1, false));
				particle.setMaxAge(25 + rand.nextInt(10));
				particle.setRGBColorF(0.7f, 0.2f, 0.9f);
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
				Items.ENDER_EYE
		};
	}

	@Override
	public float getAffinityShift(Affinity affinity){
		return 0.05f;
	}

	@Override
	public MultiblockStructureDefinition getRitualShape(){
		return RitualShapeHelper.instance.corruption;
	}

	@Override
	public ItemStack[] getReagents(){
		int enderMeta = 0;
		for (Affinity aff : ArsMagicaAPI.getAffinityRegistry().getValues()) {
			if (aff.equals(Affinity.NONE))
				continue;				
			if (aff.equals(Affinity.ENDER))
				break;
			enderMeta++;
		}
		return new ItemStack[]{
				new ItemStack(ItemDefs.mobFocus),
				new ItemStack(ItemDefs.essence, 1, enderMeta)
		};
	}

	@Override
	public int getReagentSearchRadius(){
		return 3;
	}

	@Override
	public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ItemStack getResult() {
		return new ItemStack(BlockDefs.inertSpawner);
	}
}
