package am2.entity;

import am2.ArsMagica2;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityShockwave extends Entity{

	private float movingSpeed;
	private float moveAngle;

	public EntityShockwave(World par1World){
		super(par1World);
		this.setSize(3.0f, 0.2f);
	}

	public void setMoveSpeedAndAngle(float moveSpeed, float angle){
		this.movingSpeed = moveSpeed;
		this.moveAngle = (float)Math.toRadians(angle);
	}

	@Override
	public void onUpdate(){

		this.ticksExisted++;

		if (this.ticksExisted >= 60)
			this.setDead();

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		int j = MathHelper.floor(this.posX);
		int i = MathHelper.floor(this.posY - 0.20000000298023224D);
		int k = MathHelper.floor(this.posZ);
		IBlockState l = this.world.getBlockState(new BlockPos (j, i, k));
		if (l.getBlock() != Blocks.AIR) {
			for (int h = 0; h < (5 * ArsMagica2.config.getGFXLevel()); ++h) {
				if (this.getEntityWorld().isRemote)
					this.world.spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX + (this.rand.nextFloat() - 0.5D) * this.width, this.getEntityBoundingBox().minY + 0.1D, this.posZ + (this.rand.nextFloat() - 0.5D) * this.width, -this.motionX * 4.0D, 1.5D, -this.motionZ * 4.0D, new int[]{Block.getStateId(l)});
			}
		}

		double deltaX = Math.cos(moveAngle) * movingSpeed;
		double deltaZ = Math.sin(moveAngle) * movingSpeed;

		this.move(MoverType.PISTON,deltaX, 0, deltaZ);
	}

	@Override
	public void applyEntityCollision(Entity par1Entity){
		par1Entity.attackEntityFrom(DamageSource.MAGIC, 2);
		super.applyEntityCollision(par1Entity);
	}

	@Override
	public void onCollideWithPlayer(EntityPlayer par1EntityPlayer){
		par1EntityPlayer.attackEntityFrom(DamageSource.GENERIC, 2);
	}

	@Override
	public boolean canBePushed(){
		return false;
	}

	@Override
	public boolean canBeCollidedWith(){
		return true;
	}

	@Override
	protected void entityInit(){
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound){
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound){
	}


}
