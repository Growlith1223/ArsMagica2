package am2.entity;

import am2.ArsMagica2;
import am2.defs.ItemDefs;
import am2.particles.AMParticle;
import am2.particles.ParticleFadeOut;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityAirSled extends EntityLiving{

	private float rotation;

	public EntityAirSled(World par1World){
		super(par1World);
		this.setSize(0.5f, 1);
		this.stepHeight = 1.02f;
	}

	@Override
	public void onUpdate(){
		this.stepHeight = 1.02f;

		if (world.isRemote){
			rotation += 1f;
			if (this.world.isAirBlock(getPosition().down())){
				for (int i = 0; i < ArsMagica2.config.getGFXLevel(); ++i){
					AMParticle cloud = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "sparkle2", posX, posY + 0.5, posZ);
					if (cloud != null){
						cloud.addRandomOffset(1, 1, 1);
						cloud.AddParticleController(new ParticleFadeOut(cloud, 1, false).setFadeSpeed(0.01f));
					}
				}
			}
		}
		super.onUpdate();
	}

	public float getRotation(){
		return rotation;
	}

	@Override
	public boolean shouldRiderSit(){
		return false;
	}
	
	
	@Override
	public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand) {
		if (this.getControllingPassenger() != null && this.getControllingPassenger() instanceof EntityPlayer && this.getControllingPassenger() != player){
			return EnumActionResult.SUCCESS;
		}else{
			if (!this.world.isRemote){
				if (player.isSneaking()){
					this.setDead();
					EntityItem item = new EntityItem(world);
					item.setPosition(posX, posY, posZ);
					item.setEntityItemStack(ItemDefs.airSledEnchanted.copy());
					world.spawnEntity(item);
				}else{
					player.startRiding(this);
				}
			}

			return EnumActionResult.SUCCESS;
		}
	}
	
	@Override
	public void updateRidden() {
		if (this.isBeingRidden()){
			this.getControllingPassenger().setPosition(this.posX, this.posY + this.getMountedYOffset() + this.getControllingPassenger().getYOffset(), this.posZ);
		}
	}
	
	@Override
	public Entity getControllingPassenger() {
		return getPassengers().isEmpty() ? null : getPassengers().get(0);
	}

	@Override
	public void moveEntityWithHeading(float par1, float par2){
		if (this.getControllingPassenger() != null){
			this.prevRotationYaw = this.rotationYaw = this.getControllingPassenger().rotationYaw;
			this.rotationPitch = this.getControllingPassenger().rotationPitch * 0.5F;
			this.setRotation(this.rotationYaw, this.rotationPitch);
			this.rotationYawHead = this.renderYawOffset = this.rotationYaw;
			par1 = ((EntityLivingBase)this.getControllingPassenger()).moveStrafing * 0.5F;
			par2 = ((EntityLivingBase)this.getControllingPassenger()).moveForward;

			if (par2 <= 0.0F){
				par2 *= 0.25F;
			}

			this.stepHeight = 1.0F;
			this.jumpMovementFactor = this.getAIMoveSpeed() * 0.1F;

			if (!this.world.isRemote){
				par2 *= 0.06f;
				if (par1 != 0){
					float f4 = MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F);
					float f5 = MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F);
					this.motionX += (par1 * f5 - par2 * f4) * 0.06f;
					this.motionZ += (par2 * f5 + par1 * f4) * 0.06f;
				}

				this.motionX += -Math.sin(Math.toRadians(this.rotationYaw)) * par2;
				this.motionY += -Math.sin(Math.toRadians(this.rotationPitch)) * par2;
				this.motionZ += Math.cos(Math.toRadians(this.rotationYaw)) * par2;
			}
		}else{
			if (!this.onGround && !this.isInWater())
				this.motionY = -0.1f;
			else
				this.motionY = 0f;

			this.motionX *= 0.7f;
			this.motionZ *= 0.7f;

		}

		if (this.getControllingPassenger() != null)
			this.setSize(0.5f, 3);
		this.move(MoverType.PLAYER, this.motionX, this.motionY, this.motionZ);
		if (this.getControllingPassenger() != null)
			this.setSize(0.5f, 1);

		float f2 = 0.91F;

		this.motionY *= f2;//0.9800000190734863D;
		this.motionX *= f2;
		this.motionZ *= f2;
	}

	@Override
	public double getMountedYOffset(){
		return 1.6f;
	}

	@Override
	protected void entityInit(){
		super.entityInit();
	}
	
	@Override
	public void playSound(SoundEvent soundIn, float volume, float pitch) {
	}
	
	@Override
	public void fall(float distance, float damageMultiplier) {}

	@Override
	public boolean canBeCollidedWith(){
		return true;
	}

	@Override
	public AxisAlignedBB getCollisionBox(Entity par1Entity){
		return null;
	}

	@Override
	public boolean attackEntityFrom(DamageSource par1DamageSource, float par2){
		if (this.getControllingPassenger() != null && !this.getControllingPassenger().isEntityEqual(par1DamageSource.getEntity()))
			this.getControllingPassenger().dismountRidingEntity();
		return false;
	}
}
