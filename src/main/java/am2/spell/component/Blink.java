package am2.spell.component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;

import am2.ArsMagica2;
import am2.api.affinity.Affinity;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellModifiers;
import am2.blocks.tileentity.TileEntityAstralBarrier;
import am2.defs.ItemDefs;
import am2.defs.PotionEffectsDefs;
import am2.extensions.EntityExtension;
import am2.particles.AMParticle;
import am2.particles.ParticleFadeOut;
import am2.particles.ParticleMoveOnHeading;
import am2.utils.DimensionUtilities;
import am2.utils.KeystoneUtilities;
import am2.utils.SpellUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class Blink extends SpellComponent{

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target){
		if (!(target instanceof EntityLivingBase)) return false;

		if (world.isRemote){
			EntityExtension.For((EntityLivingBase)target).astralBarrierBlocked = false;
		}

		double distance = GetTeleportDistance(stack, caster, target);

		double motionX = -MathHelper.sin((target.rotationYaw / 180F) * 3.141593F) * MathHelper.cos((target.rotationPitch / 180F) * 3.141593F) * distance;
		double motionZ = MathHelper.cos((target.rotationYaw / 180F) * 3.141593F) * MathHelper.cos((target.rotationPitch / 180F) * 3.141593F) * distance;
		double motionY = -MathHelper.sin((target.rotationPitch / 180F) * 3.141593F) * distance;

		double d = motionX, d1 = motionY, d2 = motionZ;

		float f2 = MathHelper.sqrt(d * d + d1 * d1 + d2 * d2);
		d /= f2;
		d1 /= f2;
		d2 /= f2;
		d *= distance;
		d1 *= distance;
		d2 *= distance;
		motionX = d;
		motionY = d1;
		motionZ = d2;
		ArrayList<Long> keystoneKeys = KeystoneUtilities.instance.GetKeysInInvenory((EntityLivingBase)target);

		double newX = target.posX + motionX;
		double newZ = target.posZ + motionZ;
		double newY = target.posY + motionY;

		boolean coordsValid = false;
		boolean astralBarrierBlocked = false;

		TileEntityAstralBarrier finalBlocker = null;

		while (!coordsValid && distance > 0){

			if (caster.isPotionActive(PotionEffectsDefs.astralDistortion)){
				coordsValid = true;
				newX = caster.posX;
				newY = caster.posY;
				newZ = caster.posZ;
			}

			TileEntityAstralBarrier blocker = DimensionUtilities.GetBlockingAstralBarrier(world, new BlockPos(newX, newY, newZ), keystoneKeys);
			while (blocker != null){
				finalBlocker = blocker;
				astralBarrierBlocked = true;

				int dx = (int)newX - blocker.getPos().getX();
				int dy = (int)newY - blocker.getPos().getY();
				int dz = (int)newZ - blocker.getPos().getZ();

				int sqDist = (dx * dx + dy * dy + dz * dz);
				int delta = blocker.getRadius() - (int)Math.floor(Math.sqrt(sqDist));
				distance -= delta;
				if (distance < 0) break;

				motionX = -MathHelper.sin((target.rotationYaw / 180F) * 3.141593F) * MathHelper.cos((target.rotationPitch / 180F) * 3.141593F) * distance;
				motionZ = MathHelper.cos((target.rotationYaw / 180F) * 3.141593F) * MathHelper.cos((target.rotationPitch / 180F) * 3.141593F) * distance;
				motionY = -MathHelper.sin((target.rotationPitch / 180F) * 3.141593F) * distance;

				d = motionX;
				d1 = motionY;
				d2 = motionZ;

				f2 = MathHelper.sqrt(d * d + d1 * d1 + d2 * d2);
				d /= f2;
				d1 /= f2;
				d2 /= f2;
				d *= distance;
				d1 *= distance;
				d2 *= distance;
				motionX = d;
				motionY = d1;
				motionZ = d2;
				newX = target.posX + motionX;
				newZ = target.posZ + motionZ;
				newY = target.posY + motionY;

				blocker = DimensionUtilities.GetBlockingAstralBarrier(world, new BlockPos(newX, newY, newZ), keystoneKeys);
			}
			if (distance < 0){
				coordsValid = false;
				break;
			}

			//rounding combinations, normal y
			if (CheckCoords(world, (int)Math.floor(newX), (int)newY, (int)Math.floor(newZ))){
				newX = Math.floor(newX) + 0.5;
				newZ = Math.floor(newZ) + 0.5;
				coordsValid = true;
				break;
			}else if (CheckCoords(world, (int)Math.floor(newX), (int)newY, (int)Math.ceil(newZ))){
				newX = Math.floor(newX) + 0.5;
				newZ = Math.ceil(newZ) + 0.5;
				coordsValid = true;
				break;
			}else if (CheckCoords(world, (int)Math.ceil(newX), (int)newY, (int)Math.floor(newZ))){
				newX = Math.ceil(newX) + 0.5;
				newZ = Math.floor(newZ) + 0.5;
				coordsValid = true;
				break;
			}else if (CheckCoords(world, (int)Math.ceil(newX), (int)newY, (int)Math.ceil(newZ))){
				newX = Math.ceil(newX) + 0.5;
				newZ = Math.ceil(newZ) + 0.5;
				coordsValid = true;
				break;
			}
			//rounding combinations, y-1
			if (CheckCoords(world, (int)Math.floor(newX), (int)newY - 1, (int)Math.floor(newZ))){
				newX = Math.floor(newX) + 0.5;
				newZ = Math.floor(newZ) + 0.5;
				newY--;
				coordsValid = true;
				break;
			}else if (CheckCoords(world, (int)Math.floor(newX), (int)newY - 1, (int)Math.ceil(newZ))){
				newX = Math.floor(newX) + 0.5;
				newZ = Math.ceil(newZ) + 0.5;
				newY--;
				coordsValid = true;
				break;
			}else if (CheckCoords(world, (int)Math.ceil(newX), (int)newY - 1, (int)Math.floor(newZ))){
				newX = Math.ceil(newX) + 0.5;
				newZ = Math.floor(newZ) + 0.5;
				newY--;
				coordsValid = true;
				break;
			}else if (CheckCoords(world, (int)Math.ceil(newX), (int)newY - 1, (int)Math.ceil(newZ))){
				newX = Math.ceil(newX) + 0.5;
				newZ = Math.ceil(newZ) + 0.5;
				newY--;
				coordsValid = true;
				break;
			}
			//rounding combinations, y+1
			if (CheckCoords(world, (int)Math.floor(newX), (int)newY + 1, (int)Math.floor(newZ))){
				newX = Math.floor(newX) + 0.5;
				newZ = Math.floor(newZ) + 0.5;
				newY++;
				coordsValid = true;
				break;
			}else if (CheckCoords(world, (int)Math.floor(newX), (int)newY + 1, (int)Math.ceil(newZ))){
				newX = Math.floor(newX) + 0.5;
				newZ = Math.ceil(newZ) + 0.5;
				newY++;
				coordsValid = true;
				break;
			}else if (CheckCoords(world, (int)Math.ceil(newX), (int)newY + 1, (int)Math.floor(newZ))){
				newX = Math.ceil(newX) + 0.5;
				newZ = Math.floor(newZ) + 0.5;
				newY++;
				coordsValid = true;
				break;
			}else if (CheckCoords(world, (int)Math.ceil(newX), (int)newY + 1, (int)Math.ceil(newZ))){
				newX = Math.ceil(newX) + 0.5;
				newZ = Math.ceil(newZ) + 0.5;
				newY++;
				coordsValid = true;
				break;
			}

			distance--;

			motionX = -MathHelper.sin((target.rotationYaw / 180F) * 3.141593F) * MathHelper.cos((target.rotationPitch / 180F) * 3.141593F) * distance;
			motionZ = MathHelper.cos((target.rotationYaw / 180F) * 3.141593F) * MathHelper.cos((target.rotationPitch / 180F) * 3.141593F) * distance;
			motionY = -MathHelper.sin((target.rotationPitch / 180F) * 3.141593F) * distance;

			d = motionX;
			d1 = motionY;
			d2 = motionZ;

			f2 = MathHelper.sqrt(d * d + d1 * d1 + d2 * d2);
			d /= f2;
			d1 /= f2;
			d2 /= f2;
			d *= distance;
			d1 *= distance;
			d2 *= distance;
			motionX = d;
			motionY = d1;
			motionZ = d2;
//			f3 = MathHelper.sqrt(d * d + d2 * d2);

			newX = target.posX + motionX;
			newZ = target.posZ + motionZ;
			newY = target.posY + motionY;

		}

		if (world.isRemote && astralBarrierBlocked && coordsValid){
			EntityExtension.For((EntityLivingBase)target).astralBarrierBlocked = true;
			if (finalBlocker != null){
				finalBlocker.onEntityBlocked((EntityLivingBase)target);
			}
		}

		if (!world.isRemote){
			if (!coordsValid && target instanceof EntityPlayer){
				((EntityPlayer)target).sendMessage(new TextComponentString("Can't find a place to blink forward to."));
				return false;
			}
		}

		if (!world.isRemote){
			((EntityLivingBase)target).setPositionAndUpdate(newX, newY, newZ);
		}

		return true;
	}

	@Override
	public float manaCost(EntityLivingBase caster){
		return 160;
	}

	@Override
	public ItemStack[] reagents(EntityLivingBase caster){
		return null;
	}

	@Override
	public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier){
		for (int i = 0; i < 25; ++i){
			AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "sparkle", x, y, z);
			if (particle != null){
				particle.addRandomOffset(1, 2, 1);
				particle.AddParticleController(new ParticleMoveOnHeading(particle, MathHelper.wrapDegrees((target instanceof EntityLivingBase ? ((EntityLivingBase)target).rotationYawHead : target.rotationYaw) + 90), MathHelper.wrapDegrees(target.rotationPitch), 0.1 + rand.nextDouble() * 0.5, 1, false));
				particle.AddParticleController(new ParticleFadeOut(particle, 1, false).setFadeSpeed(0.05f));
				particle.setMaxAge(20);
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

	@SuppressWarnings("deprecation")
	private boolean CheckCoords(World world, int x, int y, int z){

		if (y < 0){
			return false;
		}

		IBlockState firstBlock = world.getBlockState(new BlockPos(x, y, z));
		IBlockState secondBlock = world.getBlockState(new BlockPos(x, y + 1, z));

		AxisAlignedBB firstBlockBB = null;
		AxisAlignedBB secondBlockBB = null;

		if (firstBlock != null){
			firstBlockBB = firstBlock.getBlock().getCollisionBoundingBox(firstBlock, world, new BlockPos(x, y, z));
		}
		if (secondBlock != null){
			secondBlockBB = secondBlock.getBlock().getCollisionBoundingBox(secondBlock, world, new BlockPos(x, y + 1, z));
		}

		if ((firstBlockBB == null && secondBlockBB == null)){
			return true;
		}
		return false;
	}

	protected double GetTeleportDistance(ItemStack stack, EntityLivingBase caster, Entity target){
		return SpellUtils.getModifiedInt_Add(12, stack, caster, target, caster.getEntityWorld(), SpellModifiers.RANGE);
	}
	
	@Override
	public EnumSet<SpellModifiers> getModifiers() {
		return EnumSet.of(SpellModifiers.RANGE);
	}

	@Override
	public Object[] getRecipe(){
		return new Object[]{
				new ItemStack(ItemDefs.rune, 1, EnumDyeColor.PURPLE.getDyeDamage()),
				Items.ENDER_PEARL
		};
	}

	@Override
	public float getAffinityShift(Affinity affinity){
		return 0.05f;
	}

	@Override
	public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {

		
	}

	@Override
	public boolean applyEffectBlock(ItemStack stack, World world,
			BlockPos blockPos, EnumFacing blockFace, double impactX,
			double impactY, double impactZ, EntityLivingBase caster) {

		return false;
	}
}
