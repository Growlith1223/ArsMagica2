package am2.world;

import java.util.Random;

import am2.blocks.BlockAMFlower;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class AM2FlowerGen extends WorldGenerator{

	private IBlockState plantBlock;

	public AM2FlowerGen(BlockAMFlower block){
		this.plantBlock = block.getDefaultState();
	}

    public boolean generate(World worldIn, Random rand, BlockPos position)
    {
        for (int i = 0; i < 64; ++i)
        {
            BlockPos blockpos = position.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));

            if (worldIn.isAirBlock(blockpos) && (!worldIn.provider.hasNoSky() || blockpos.getY() < 255) && ((BlockAMFlower)this.plantBlock.getBlock()).canBlockStay(worldIn, blockpos, this.plantBlock))
            {
                worldIn.setBlockState(blockpos, this.plantBlock, 2);
            }
        }

        return true;
    }
}
