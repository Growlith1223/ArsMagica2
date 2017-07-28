package am2.affinity.abilities;

import am2.api.affinity.AbstractToggledAffinityAbility;
import am2.api.affinity.Affinity;
import am2.extensions.AffinityData;
import am2.utils.WorldUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class AbilityWaterFreeze extends AbstractToggledAffinityAbility {

	public AbilityWaterFreeze() {
		super(new ResourceLocation("arsmagica2", "waterfreeze"));
	}

	@Override
	public float getMinimumDepth() {
		return 0.5F;
	}

	@Override
	public Affinity getAffinity() {
		return Affinity.ICE;
	}

	@Override
	public void applyTick(EntityPlayer player) {
		if (player.world.isRemote) return;
		BlockPos startPos = new BlockPos(player.posX, Math.floor(player.posY), player.posZ);
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				BlockPos newPos = startPos.add(x, -1, z);
				if (player.world.getBlockState(newPos).getBlock() == Blocks.WATER || player.world.getBlockState(newPos).getBlock() == Blocks.FLOWING_WATER)
					WorldUtils.freeze(newPos, player.world);
			}
		}
	}

	@Override
	protected boolean isEnabled(EntityPlayer player) {
		return AffinityData.For(player).getAbilityBoolean(AffinityData.ICE_BRIDGE_STATE);
	}

}
