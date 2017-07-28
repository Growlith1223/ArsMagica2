/**
 *
 */
package am2.container;

import am2.blocks.tileentity.TileEntityFlickerHabitat;
import am2.container.slot.AM2Container;
import am2.container.slot.SlotSpecifiedItemsOnly;
import am2.defs.ItemDefs;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author Zero
 */
public class ContainerFlickerHabitat extends AM2Container{

	private TileEntityFlickerHabitat habitat;

	public ContainerFlickerHabitat(EntityPlayer player, TileEntityFlickerHabitat habitat){
		this.habitat = habitat;
		SlotSpecifiedItemsOnly slot;

		if (habitat.isUpgrade()){
			slot = new SlotSpecifiedItemsOnly(habitat, 0, 79, 47, ItemDefs.flickerJar);
		}else{
			slot = new SlotSpecifiedItemsOnly(habitat, 0, 79, 47, ItemDefs.flickerJar, ItemDefs.flickerFocus);
		}

		slot.setMaxStackSize(1);
		addSlotToContainer(slot);

		this.addPlayerInventory(player, 8, 84);
		this.addPlayerActionBar(player, 8, 142);

	}

	/* (non-Javadoc)
	 * @see net.minecraft.inventory.Container#canInteractWith(net.minecraft.entity.player.EntityPlayer)
	 */
	@Override
	public boolean canInteractWith(EntityPlayer entityplayer){
		return this.habitat.isUsableByPlayer(entityplayer);
	}

	@Override
	public void onContainerClosed(EntityPlayer par1EntityPlayer){
		super.onContainerClosed(par1EntityPlayer);
		this.habitat.closeInventory(par1EntityPlayer);
		this.habitat.updateOperator(habitat.getStackInSlot(0));
	}

}
