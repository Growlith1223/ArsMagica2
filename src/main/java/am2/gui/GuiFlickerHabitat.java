package am2.gui;

import org.lwjgl.opengl.GL11;

import am2.api.ArsMagicaAPI;
import am2.api.flickers.AbstractFlickerFunctionality;
import am2.blocks.tileentity.TileEntityFlickerHabitat;
import am2.container.ContainerFlickerHabitat;
import am2.power.PowerNodeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;

/**
 * @author Zero, Mithion
 */
@SuppressWarnings("deprecation")
public class GuiFlickerHabitat extends GuiContainer{

	private static final ResourceLocation background = new ResourceLocation("arsmagica2", "textures/gui/flicker_habitat.png");
	private final TileEntityFlickerHabitat flickerHabitat;

	/**
	 * @param par1Container
	 */
	public GuiFlickerHabitat(EntityPlayer player, TileEntityFlickerHabitat tileEntityFlickerHabitat){
		super(new ContainerFlickerHabitat(player, tileEntityFlickerHabitat));
		flickerHabitat = tileEntityFlickerHabitat;
		xSize = 176;
		ySize = 166;
	}

	/* (non-Javadoc)
	 * @see net.minecraft.client.gui.inventory.GuiContainer#drawGuiContainerBackgroundLayer(float, int, int)
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j){
		mc.renderEngine.bindTexture(background);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int l = (width - xSize) / 2;
		int i1 = (height - ySize) / 2;
		drawTexturedModalRect(l, i1, 0, 0, xSize, ySize);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_){
		super.drawGuiContainerForegroundLayer(p_146979_1_, p_146979_2_);

		GlStateManager.enableBlend();
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		ItemStack stack = flickerHabitat.getStackInSlot(0);

		if (stack == null) return;

		AbstractFlickerFunctionality func = ArsMagicaAPI.getFlickerFocusRegistry().getObjectById(stack.getItemDamage());

		if (func == null)
			return;

		String colorCode = Minecraft.getMinecraft().world.isBlockIndirectlyGettingPowered(flickerHabitat.getPos()) > 0 ? "\2474" : "\2472";

		int yPos = 5;
		String curLine = "";

		if (func.RequiresPower()){
			curLine = I18n.translateToLocal("am2.gui.flicker_needspower");
		}else{
			curLine = I18n.translateToLocal("am2.gui.flicker_doesntneedpower");
		}

		drawCenteredString(curLine, yPos);
		yPos += 12 * (int)Math.ceil(this.fontRenderer.getStringWidth(curLine) / 170.0f);

		drawCenteredString(
				String.format(
						I18n.translateToLocal("am2.gui.flicker_powerperop"),
						String.format("%s%d\2470", colorCode, func.PowerPerOperation())),
				yPos);

		yPos += 12;

		boolean powered = PowerNodeRegistry.For(flickerHabitat.getWorld()).checkPower(flickerHabitat, func.PowerPerOperation());

		if (yPos > 40)
			yPos += 27;

		drawCenteredString(
				String.format(
						I18n.translateToLocal("am2.gui.flicker_optime"),
						String.format("%s%.2f\2470", colorCode,
								func.TimeBetweenOperation(powered, flickerHabitat.getNearbyUpgrades()) / 20.0f
						)), yPos);
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.disableBlend();
	}

	private void drawCenteredString(String s, int yCoord){
		int w = this.fontRenderer.getStringWidth(s);
		int xPos = this.xSize / 2 - w / 2;
		if (w > 170){
			xPos = 3;
		}
		this.fontRenderer.drawSplitString(s, xPos, yCoord, 170, 0);
	}

}
