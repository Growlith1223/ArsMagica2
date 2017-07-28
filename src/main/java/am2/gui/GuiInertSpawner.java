/**
 *
 */
package am2.gui;


import org.lwjgl.opengl.GL11;

import am2.blocks.tileentity.TileEntityInertSpawner;
import am2.container.ContainerInertSpawner;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiInertSpawner extends GuiContainer{

	private static final ResourceLocation background = new ResourceLocation("arsmagica2", "textures/gui/flicker_habitat.png");
	/**
	 * @param par1Container
	 */
	public GuiInertSpawner(EntityPlayer player, TileEntityInertSpawner tileEntityFlickerHabitat){
		super(new ContainerInertSpawner(player, tileEntityFlickerHabitat));
		xSize = 176;
		ySize = 166;
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
