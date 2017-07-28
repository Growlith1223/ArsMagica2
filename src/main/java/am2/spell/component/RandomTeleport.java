package am2.spell.component;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;

import am2.api.affinity.Affinity;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellModifiers;
import am2.defs.ItemDefs;
import am2.utils.SpellUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;

public class RandomTeleport extends SpellComponent{

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target){
		Vec3d rLoc = getRandomTeleportLocation(world, stack, caster, target);
		return teleportTo(rLoc.xCoord, rLoc.yCoord, rLoc.zCoord, target);
	}

	private Vec3d getRandomTeleportLocation(World world, ItemStack stack, EntityLivingBase caster, Entity target){
		Vec3d origin = new Vec3d(target.posX, target.posY, target.posZ);
		float maxDist = 9;
		maxDist = (float)SpellUtils.getModifiedDouble_Mul(maxDist, stack, caster, target, world, SpellModifiers.RANGE);
		origin = origin.add(new Vec3d((world.rand.nextDouble() - 0.5) * maxDist, (world.rand.nextDouble() - 0.5) * maxDist, (world.rand.nextDouble() - 0.5) * maxDist));
		return origin;
	}
	
	@Override
	public EnumSet<SpellModifiers> getModifiers() {
		return EnumSet.of(SpellModifiers.RANGE);
	}

	protected boolean teleportTo(double par1, double par3, double par5, Entity target){
		if (target instanceof EntityLivingBase){
			EnderTeleportEvent event = new EnderTeleportEvent((EntityLivingBase)target, par1, par3, par5, 0);
			if (MinecraftForge.EVENT_BUS.post(event)){
				return false;
			}
			par1 = event.getTargetX();
			par3 = event.getTargetY();
			par5 = event.getTargetZ();
		}

		double d3 = target.posX;
		double d4 = target.posY;
		double d5 = target.posZ;
		target.posX = par1;
		target.posY = par3;
		target.posZ = par5;
		boolean locationValid = false;
		BlockPos pos = target.getPosition();
		Block l;

		if (target.world.getBlockState(pos) != null){
			boolean targetBlockIsSolid = false;

			while (!targetBlockIsSolid && pos.getY() > 0){
				l = target.world.getBlockState(pos.down()).getBlock();

				if (l != Blocks.AIR && l.isPassable(target.world, pos)){
					targetBlockIsSolid = true;
				}else{
					--target.posY;
					pos = pos.down();
				}
			}

			if (targetBlockIsSolid){
				target.setPosition(target.posX, target.posY, target.posZ);
				if (target.world.getCollisionBoxes(target, target.getEntityBoundingBox()).isEmpty()){
					locationValid = true;
				}
			}
		}

		if (!locationValid){
			target.setPosition(d3, d4, d5);
			return false;
		}else{
			return true;
		}
	}

	@Override
	public float manaCost(EntityLivingBase caster){
		return 52.5f;
	}

	@Override
	public ItemStack[] reagents(EntityLivingBase caster){
		return null;
	}

	@Override
	public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier){
		world.spawnParticle(EnumParticleTypes.PORTAL, target.posX + (rand.nextDouble() - 0.5D) * target.width, target.posY + rand.nextDouble() * target.height - 0.25D, target.posZ + (rand.nextDouble() - 0.5D) * target.width, (rand.nextDouble() - 0.5D) * 2.0D, -rand.nextDouble(), (rand.nextDouble() - 0.5D) * 2.0D);
	}

	@Override
	public Set<Affinity> getAffinity(){
		return Sets.newHashSet(Affinity.ENDER);
	}

	@Override
	public Object[] getRecipe(){
		return new Object[]{
				new ItemStack(ItemDefs.rune, 1, EnumDyeColor.BLACK.getDyeDamage()),
				Items.ENDER_PEARL
		};
	}

	@Override
	public float getAffinityShift(Affinity affinity){
		return 0.01f;
	}

	@Override
	public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {}

	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, BlockPos blockPos, EnumFacing blockFace,
			double impactX, double impactY, double impactZ, EntityLivingBase caster) {
		return false;
	}
}
