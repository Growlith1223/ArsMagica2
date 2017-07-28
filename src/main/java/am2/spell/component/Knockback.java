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
import am2.packet.AMNetHandler;
import am2.particles.AMParticle;
import am2.particles.ParticleFadeOut;
import am2.particles.ParticleMoveOnHeading;
import am2.utils.SpellUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Knockback extends SpellComponent{

	@Override
	public boolean applyEffectEntity(ItemStack stack, World world, EntityLivingBase caster, Entity target){

		if (target instanceof EntityLivingBase){
			double speed = 1.5;
			speed = SpellUtils.getModifiedDouble_Add(speed, stack, caster, target, world, SpellModifiers.VELOCITY_ADDED);
			double vertSpeed = 0.325;

			EntityLivingBase curEntity = (EntityLivingBase)target;

			double deltaZ = curEntity.posZ - caster.posZ;
			double deltaX = curEntity.posX - caster.posX;
			double angle = Math.atan2(deltaZ, deltaX);

			double radians = angle;

			if (curEntity instanceof EntityPlayer){
				AMNetHandler.INSTANCE.sendVelocityAddPacket(world, curEntity, speed * Math.cos(radians), vertSpeed, speed * Math.sin(radians));
			}else{
				curEntity.motionX += (speed * Math.cos(radians));
				curEntity.motionZ += (speed * Math.sin(radians));
				curEntity.motionY += vertSpeed;
			}
			return true;
		}
		return false;
	}
	
	@Override
	public EnumSet<SpellModifiers> getModifiers() {
		return EnumSet.of(SpellModifiers.VELOCITY_ADDED);
	}


	@Override
	public float manaCost(EntityLivingBase caster){
		return 60;
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
				double dx = caster.posX - target.posX;
				double dz = caster.posZ - target.posZ;
				double angle = Math.toDegrees(Math.atan2(-dz, -dx));
				particle.AddParticleController(new ParticleMoveOnHeading(particle, angle, 0, 0.1 + rand.nextDouble() * 0.5, 1, false));
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
		return Sets.newHashSet(Affinity.AIR, Affinity.WATER, Affinity.EARTH);
	}

	@Override
	public Object[] getRecipe(){
		return new Object[]{
				new ItemStack(ItemDefs.rune, 1, EnumDyeColor.YELLOW.getDyeDamage()),
				Blocks.PISTON
		};
	}

	@Override
	public float getAffinityShift(Affinity affinity){
		return 0.01f;
	}

	@Override
	public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {

		
	}

	@Override
	public boolean applyEffectBlock(ItemStack stack, World world, BlockPos blockPos, EnumFacing blockFace,
			double impactX, double impactY, double impactZ, EntityLivingBase caster) {

		return false;
	}
}
