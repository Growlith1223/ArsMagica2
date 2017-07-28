package am2.spell.component;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;

import am2.ArsMagica2;
import am2.api.affinity.Affinity;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellModifiers;
import am2.defs.ItemDefs;
import am2.particles.AMParticle;
import am2.particles.ParticleHoldPosition;
import am2.utils.EntityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Forge extends SpellComponent{

	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, BlockPos pos, EnumFacing blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster){
		if (!CanApplyFurnaceToBlockAtCoords(caster, world, pos)) return false;
		ApplyFurnaceToBlockAtCoords(caster, world, pos);
		return true;
	}

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target){
		if (target instanceof EntityVillager && ArsMagica2.config.forgeSmeltsVillagers()){
			if (!world.isRemote && !EntityUtils.isSummon((EntityLivingBase)target))
				target.dropItem(Items.EMERALD, 1);
			if (caster instanceof EntityPlayer)
				target.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer)caster), 5000);
			else
				target.attackEntityFrom(DamageSource.causeMobDamage(caster), 5000);
			return true;
		}
		return false;
	}
	
	@Override
	public EnumSet<SpellModifiers> getModifiers() {
		return EnumSet.noneOf(SpellModifiers.class);
	}

	
	@Override
	public float manaCost(EntityLivingBase caster){
		return 55;
	}

	@Override
	public ItemStack[] reagents(EntityLivingBase caster){
		return null;
	}

	@Override
	public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier){
		AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "radiant", x + 0.5, y + 0.5, z + 0.5);
		if (particle != null){
			particle.AddParticleController(new ParticleHoldPosition(particle, 20, 1, false));
			particle.setMaxAge(20);
			particle.setParticleScale(0.3f);
			particle.setRGBColorF(0.7f, 0.4f, 0.2f);
			particle.SetParticleAlpha(0.1f);
			if (colorModifier > -1){
				particle.setRGBColorF(((colorModifier >> 16) & 0xFF) / 255.0f, ((colorModifier >> 8) & 0xFF) / 255.0f, (colorModifier & 0xFF) / 255.0f);
			}
		}
	}

	@Override
	public Set<Affinity> getAffinity(){
		return Sets.newHashSet(Affinity.FIRE);
	}

	@SuppressWarnings("deprecation")
	private boolean ApplyFurnaceToBlockAtCoords(EntityLivingBase entity, World world, BlockPos pos){

		Block block = world.getBlockState(pos).getBlock();

		if (block.equals(Blocks.AIR)){
			return false;
		}

		if (block.equals(Blocks.ICE)){
			if (!world.isRemote){
				world.setBlockState(pos, Blocks.WATER.getDefaultState());
			}
			return true;
		}

		int meta = world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos));
		ItemStack smelted = FurnaceRecipes.instance().getSmeltingResult(new ItemStack(block, 1, meta));
		if (smelted == null){
			return false;
		}
		if (!world.isRemote){
			if (ItemIsBlock(smelted.getItem())){
				world.setBlockState(pos, ((ItemBlock)smelted.getItem()).block.getStateFromMeta(smelted.getItemDamage()));
			}else{
				EntityItem item = new EntityItem(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5F, smelted.copy());
				float f3 = 0.05F;
				item.motionX = (float)world.rand.nextGaussian() * f3;
				item.motionY = (float)world.rand.nextGaussian() * f3 + 0.2F;
				item.motionZ = (float)world.rand.nextGaussian() * f3;
				world.spawnEntity(item);
				world.setBlockState(pos, Blocks.AIR.getDefaultState());
			}
		}
		return true;
	}

	private boolean CanApplyFurnaceToBlockAtCoords(EntityLivingBase entity, World world, BlockPos pos){

		IBlockState block = world.getBlockState(pos);

		if (block.getBlock().equals(Blocks.AIR)){
			return false;
		}

		if (block.getBlock().equals(Blocks.ICE)){
			return true;
		}

		int meta = block.getBlock().getMetaFromState(block);
		ItemStack smelted = FurnaceRecipes.instance().getSmeltingResult(new ItemStack(block.getBlock(), 1, meta));
		if (smelted == null){
			return false;
		}
		return true;
	}

	public boolean ItemIsBlock(Item smelted){
		return smelted instanceof ItemBlock;
	}

	@Override
	public Object[] getRecipe(){
		return new Object[]{
				new ItemStack(ItemDefs.rune, 1, EnumDyeColor.RED.getDyeDamage()),
				Blocks.FURNACE
		};
	}

	@Override
	public float getAffinityShift(Affinity affinity){
		return 0.01f;
	}
	
	@Override
	public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {}
}
