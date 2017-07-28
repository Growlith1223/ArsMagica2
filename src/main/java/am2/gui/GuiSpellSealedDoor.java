package am2.gui;

import org.lwjgl.opengl.GL11;

import am2.blocks.tileentity.TileEntitySpellSealedDoor;
import am2.container.ContainerSpellSealedDoor;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiSpellSealedDoor extends GuiContainer{

	GuiButton radiusButton;

	private static final ResourceLocation background = new ResourceLocation("arsmagica2", "textures/gui/astral_barrier_gui.png");

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j){
		mc.renderEngine.bindTexture(background);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int l = (width - xSize) / 2;
		int i1 = (height - ySize) / 2;
		drawTexturedModalRect(l, i1, 0, 0, xSize, ySize);
	}

	public GuiSpellSealedDoor(InventoryPlayer inventoryplayer, TileEntitySpellSealedDoor door){
		super(new ContainerSpellSealedDoor(inventoryplayer, door));
		xSize = 176;
		ySize = 180;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2){
	}
}
