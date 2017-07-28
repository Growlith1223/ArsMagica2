package am2.blocks;

import am2.defs.CreativeTabsDefs;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockCaster extends BlockAMPowered{
	//TODO: Caster.
	public BlockCaster() {
		super(Material.ROCK);
		setCreativeTab(CreativeTabsDefs.tabAM2Blocks);
		this.setHardness(1.5f);
		this.setResistance(10.0f);
		this.setHarvestLevel("pickaxe", 1);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		//TODO Caster Tile.
		return null;
	}

}
