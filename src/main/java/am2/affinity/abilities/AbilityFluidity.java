package am2.affinity.abilities;

import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class AbilityFluidity extends AbstractAffinityAbility {

	public AbilityFluidity() {
		super(new ResourceLocation("arsmagica2", "fluidity"));
	}

	@Override
	public float getMinimumDepth() {
		return 0.5f;
	}

	@Override
	public Affinity getAffinity() {
		return Affinity.WATER;
	}

	@Override
	public void applyTick(EntityPlayer player) {
		if (player.isInWater()) {
			applyReverseWaterMovement(player);
		}
	}
	
	private void applyReverseWaterMovement(EntityPlayer entity){
		AxisAlignedBB par1AxisAlignedBB = entity.getEntityBoundingBox().expand(0.0D, -0.4000000059604645D, 0.0D).contract(0.001D);

		int i = MathHelper.floor(par1AxisAlignedBB.minX);
		int j = MathHelper.floor(par1AxisAlignedBB.maxX + 1.0D);
		int k = MathHelper.floor(par1AxisAlignedBB.minY);
		int l = MathHelper.floor(par1AxisAlignedBB.maxY + 1.0D);
		int i1 = MathHelper.floor(par1AxisAlignedBB.minZ);
		int j1 = MathHelper.floor(par1AxisAlignedBB.maxZ + 1.0D);
		
		Vec3d vec3 = new Vec3d(0.0D, 0.0D, 0.0D);

		for (int k1 = i; k1 < j; ++k1) {
			for (int l1 = k; l1 < l; ++l1) {
				for (int i2 = i1; i2 < j1; ++i2) {
					IBlockState blockState = entity.world.getBlockState(new BlockPos (k1, l1, i2));

					if (blockState != null && blockState.getMaterial() == Material.WATER) {
						double d0 = l1 + 1 - BlockLiquid.getLiquidHeightPercent(blockState.getBlock().getMetaFromState(blockState));

						if (l >= d0) {
							blockState.getBlock().modifyAcceleration(entity.world, new BlockPos (k1, l1, i2), entity, vec3);
						}
					}
				}
			}
		}

		if (vec3.lengthVector() > 0.0D && entity.isInWater()) {
			vec3 = vec3.normalize();
			double d1 = -0.014D;
			entity.motionX += vec3.xCoord * d1;
			entity.motionY += vec3.yCoord * d1;
			entity.motionZ += vec3.zCoord * d1;
		}
	}

}
