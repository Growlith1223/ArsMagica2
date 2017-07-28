/**
 *
 */
package am2.gui;

import org.lwjgl.opengl.GL11;

import am2.blocks.tileentity.TileEntityCrystalMarker;
import am2.container.ContainerCrystalMarker;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

/**
 * @author Zero
 */
public class GuiCrystalMarker extends GuiContainer{

	private static final ResourceLocation background = new ResourceLocation("arsmagica2", "textures/gui/crystal_marker.png");
	
	public GuiCrystalMarker(EntityPlayer player, TileEntityCrystalMarker tileEntityCrystalMarker){
		super(new ContainerCrystalMarker(player, tileEntityCrystalMarker));
		xSize = 176;
		ySize = 165;
	}


	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j){
		mc.renderEngine.bindTexture(background);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int l = (width - xSize) / 2;
		int i1 = (height - ySize) / 2;
		drawTexturedModalRect(l, i1, 0, 0, xSize, ySize);
	}
}
