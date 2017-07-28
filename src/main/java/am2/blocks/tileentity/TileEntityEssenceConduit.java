package am2.blocks.tileentity;

import am2.blocks.BlockEssenceConduit;
import am2.power.PowerTypes;
import net.minecraft.util.EnumFacing;

public class TileEntityEssenceConduit extends TileEntityAMPower{

	private float rotationX;
	private float rotationY;
	private float rotationZ;

	private boolean redstonePowered;

	private float rotationIncrementX;
	private float rotationIncrementY;
	private float rotationIncrementZ;
	
	private boolean isFirstTick = true;

	public TileEntityEssenceConduit(){
		super(1);

		redstonePowered = false;
	}

	@Override
	public void update(){
		if (isFirstTick) {
			rotationX = world.rand.nextInt(360);
			rotationY = world.rand.nextInt(360);
			rotationZ = world.rand.nextInt(360);
			rotationIncrementX = world.rand.nextFloat() * 0.002f + 0.005f;
			rotationIncrementY = world.rand.nextFloat() * 0.002f + 0.005f;
			rotationIncrementZ = world.rand.nextFloat() * 0.002f + 0.005f;
			isFirstTick = false;
		}
		if (world != null && world.isBlockIndirectlyGettingPowered(pos) > 0){
			redstonePowered = true;
		}else{
			redstonePowered = false;
		}
		incrementRotations();
		super.update();
	}

	@Override
	public float particleOffset(int axis){
		EnumFacing meta = world.getBlockState(pos).getValue(BlockEssenceConduit.FACING);

		if (axis == 0){
			switch (meta){
			case WEST:
				return 0.8f;
			case EAST:
				return 0.2f;
			default:
				return 0.5f;
			}
		}else if (axis == 1){
			switch (meta){
			case UP:
				return 0.2f;
			case DOWN:
				return 0.8f;
			default:
				return 0.5f;
			}
		}else if (axis == 2){
			switch (meta){
			case NORTH:
				return 0.8f;
			case SOUTH:
				return 0.2f;
			default:
				return 0.5f;
			}
		}

		return 0.5f;
	}

	public float getRotationX(){
		return this.rotationX;
	}

	public float getRotationY(){
		return this.rotationY;
	}

	public float getRotationZ(){
		return this.rotationZ;
	}

	public void incrementRotations(){
		rotationX += rotationIncrementX;
		rotationY += rotationIncrementY;
		rotationZ += rotationIncrementZ;

		if (rotationX >= 360){
			rotationX = 0;
		}

		if (rotationY >= 360){
			rotationY = 0;
		}

		if (rotationZ >= 360){
			rotationZ = 0;
		}

		if (rotationX < 0){
			rotationX = 359;
		}

		if (rotationY < 0){
			rotationY = 359;
		}

		if (rotationZ < 0){
			rotationZ = 359;
		}
	}

	@Override
	public int getChargeRate(){
		return 1;
	}

	@Override
	public boolean canRequestPower(){
		return !this.redstonePowered;
	}

	@Override
	public boolean canProvidePower(PowerTypes type){
		return false;
	}

	@Override
	public boolean canRelayPower(PowerTypes type){
		return true;
	}
}
