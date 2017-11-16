package am2.gui;

import java.io.IOException;

import am2.ArsMagica2;
import am2.gui.controls.GuiButtonVariableDims;
import am2.gui.controls.GuiSlideControl;
import am2.packet.AMDataWriter;
import am2.packet.AMNetHandler;
import am2.packet.AMPacketIDs;
import am2.particles.AMParticle;
import am2.particles.ParticleController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("deprecation")
@SideOnly(Side.CLIENT)
public class AuraCustomizationMenu extends GuiScreen{

	/**
	 * The title string that is displayed in the top-center of the screen.
	 */
	protected String screenTitle = "Beta Particle Customization";

	private GuiButtonVariableDims btnParticleType;
	private GuiButtonVariableDims btnParticleBehaviour;
	private GuiButtonVariableDims btnParticleColorMode;
	private GuiSlideControl sliParticleRed;
	private GuiSlideControl sliParticleGreen;
	private GuiSlideControl sliParticleBlue;
	private GuiSlideControl sliParticleAlpha;
	private GuiSlideControl sliParticleScale;
	private GuiSlideControl sliParticleQuantity;
	private GuiSlideControl sliParticleDelay;
	private GuiSlideControl sliParticleSpeed;

	private GuiButton activeButton;

	private GuiScreen parent;

	public AuraCustomizationMenu(){
		this.mc = Minecraft.getMinecraft();
		this.parent = this.mc.currentScreen;
		this.fontRendererObj = Minecraft.getMinecraft().fontRendererObj;
		ScaledResolution scaledresolution = new ScaledResolution(mc);
		this.width = scaledresolution.getScaledWidth();
		this.height = scaledresolution.getScaledHeight();
	}

	@Override
	public boolean doesGuiPauseGame(){
		return false;
	}

	@Override
	public void initGui(){
		btnParticleType = new GuiButtonVariableDims(10, 50, 40, AMParticle.particleTypes[ArsMagica2.config.getAuraIndex()]);
		btnParticleBehaviour = new GuiButtonVariableDims(11, 50, 60, ParticleController.AuraControllerOptions[ArsMagica2.config.getAuraBehaviour()]);
		btnParticleColorMode = new GuiButtonVariableDims(12, 50, 80, ArsMagica2.config.getAuraColorDefault() ? I18n.translateToLocal("am2.gui.default") : ArsMagica2.config.getAuraColorRandom() ? I18n.translateToLocal("am2.gui.random") : I18n.translateToLocal("am2.gui.custom"));

		btnParticleType.setDimensions(80, 20);
		btnParticleBehaviour.setDimensions(80, 20);
		btnParticleColorMode.setDimensions(80, 20);

		sliParticleScale = new GuiSlideControl(14, width - 110, 40, 100, I18n.translateToLocal("am2.gui.scale"), ArsMagica2.config.getAuraScale() * 10, 1f, 200f);
		sliParticleAlpha = new GuiSlideControl(15, width - 110, 60, 100, I18n.translateToLocal("am2.gui.alpha"), ArsMagica2.config.getAuraAlpha() * 100, 1f, 100f);
		sliParticleRed = new GuiSlideControl(16, width - 110, 80, 100, I18n.translateToLocal("am2.gui.red"), (ArsMagica2.config.getAuraColor() >> 16) & 0xFF, 0f, 255f);
		sliParticleRed.setInteger(true);
		sliParticleGreen = new GuiSlideControl(17, width - 110, 100, 100, I18n.translateToLocal("am2.gui.green"), (ArsMagica2.config.getAuraColor() >> 8) & 0xFF, 0f, 255f);
		sliParticleGreen.setInteger(true);
		sliParticleBlue = new GuiSlideControl(18, width - 110, 120, 100, I18n.translateToLocal("am2.gui.blue"), ArsMagica2.config.getAuraColor() & 0xFF, 0f, 255f);
		sliParticleBlue.setInteger(true);

		sliParticleQuantity = new GuiSlideControl(20, width - 110, 140, 100, I18n.translateToLocal("am2.gui.qty"), ArsMagica2.config.getAuraQuantity(), 1, 5);
		sliParticleQuantity.setInteger(true);

		sliParticleDelay = new GuiSlideControl(21, width - 110, 160, 100, I18n.translateToLocal("am2.gui.delay"), ArsMagica2.config.getAuraDelay(), 1, 100);
		sliParticleDelay.setInteger(true);

		sliParticleSpeed = new GuiSlideControl(22, width - 110, 180, 100, I18n.translateToLocal("am2.gui.speed"), ArsMagica2.config.getAuraSpeed(), 0.05f, 10.0f);

		if (ArsMagica2.config.getAuraColorDefault() || ArsMagica2.config.getAuraColorRandom()){
			sliParticleRed.enabled = false;
			sliParticleBlue.enabled = false;
			sliParticleGreen.enabled = false;
		}else{
			sliParticleRed.enabled = true;
			sliParticleBlue.enabled = true;
			sliParticleGreen.enabled = true;
		}


		this.buttonList.add(new GuiButton(200, this.width / 2 - 100, this.height - 28, I18n.translateToLocal("am2.gui.done")));

		this.buttonList.add(btnParticleType);
		this.buttonList.add(btnParticleBehaviour);
		this.buttonList.add(btnParticleColorMode);
		this.buttonList.add(sliParticleScale);
		this.buttonList.add(sliParticleAlpha);
		this.buttonList.add(sliParticleRed);
		this.buttonList.add(sliParticleGreen);
		this.buttonList.add(sliParticleBlue);
		this.buttonList.add(sliParticleDelay);
		this.buttonList.add(sliParticleQuantity);
		this.buttonList.add(sliParticleSpeed);
	}

	@Override
	public void drawScreen(int par1, int par2, float par3){
		this.drawDefaultBackground();
		drawCenteredString(fontRendererObj, screenTitle, width / 2, 4, 0xffffff);
		drawString(fontRendererObj, I18n.translateToLocal("am2.gui.type"), 10, 45, 0xffffff);
		drawString(fontRendererObj, I18n.translateToLocal("am2.gui.action"), 10, 65, 0xffffff);
		drawString(fontRendererObj, I18n.translateToLocal("am2.gui.color"), 10, 85, 0xffffff);
		drawString(fontRendererObj, I18n.translateToLocal("am2.gui.border"), 10, 105, 0xffffff);
		super.drawScreen(par1, par2, par3);
	}

	@Override
	protected void actionPerformed(GuiButton par1GuiButton){

		int index = 0;
		activeButton = par1GuiButton;

		switch (par1GuiButton.id){
		case 10: //particle type
			index = ArsMagica2.config.getAuraIndex();
			index++;
			if (index >= AMParticle.particleTypes.length) index = 0;

			ArsMagica2.config.setAuraIndex(index);
			btnParticleType.displayString = AMParticle.particleTypes[index];
			break;
		case 11: //particle behaviour
			index = ArsMagica2.config.getAuraBehaviour();
			index++;
			if (index >= ParticleController.AuraControllerOptions.length) index = 0;
			ArsMagica2.config.setAuraBehaviour(index);
			btnParticleBehaviour.displayString = ParticleController.AuraControllerOptions[index];
			break;
		case 12: //default color
		case 13: //random color
			if (ArsMagica2.config.getAuraColorDefault()){
				ArsMagica2.config.setAuraColorDefault(false);
				ArsMagica2.config.setAuraColorRandom(true);
				sliParticleRed.enabled = false;
				sliParticleBlue.enabled = false;
				sliParticleGreen.enabled = false;
			}else if (ArsMagica2.config.getAuraColorRandom()){
				ArsMagica2.config.setAuraColorDefault(false);
				ArsMagica2.config.setAuraColorRandom(false);
				sliParticleRed.enabled = true;
				sliParticleBlue.enabled = true;
				sliParticleGreen.enabled = true;
			}else{
				ArsMagica2.config.setAuraColorDefault(true);
				ArsMagica2.config.setAuraColorRandom(false);
				sliParticleRed.enabled = false;
				sliParticleBlue.enabled = false;
				sliParticleGreen.enabled = false;
			}
			btnParticleColorMode.displayString = ArsMagica2.config.getAuraColorDefault() ? "Default" : ArsMagica2.config.getAuraColorRandom() ? I18n.translateToLocal("am2.gui.random") : I18n.translateToLocal("am2.gui.custom");
			break;
		case 14: //scale
			ArsMagica2.config.setAuraScale(((GuiSlideControl)par1GuiButton).getShiftedValue() / 10f);
			break;
		case 15: //alpha
			ArsMagica2.config.setAuraAlpha(((GuiSlideControl)par1GuiButton).getShiftedValue() / 100f);
			break;
		case 16: //red
		case 17: //green
		case 18: //blue
			int color = ((int)sliParticleRed.getShiftedValue() & 0xFF) << 16 | ((int)sliParticleGreen.getShiftedValue() & 0xFF) << 8 | (int)sliParticleBlue.getShiftedValue() & 0xFF;
			ArsMagica2.config.setAuraColor(color);
			break;
		case 20: //quantity
			ArsMagica2.config.setAuraQuantity((int)sliParticleQuantity.getShiftedValue());
			break;
		case 21: //delay
			ArsMagica2.config.setAuraDelay((int)sliParticleDelay.getShiftedValue());
			break;
		case 22: //speed
			ArsMagica2.config.setAuraSpeed(sliParticleSpeed.getShiftedValue());
			break;
		case 200: //close
			this.mc.displayGuiScreen(this.parent);
			break;
		}
	}

	@Override
	public void onGuiClosed(){

		AMDataWriter writer = new AMDataWriter();

		writer.add(ArsMagica2.config.getAuraIndex());
		writer.add(ArsMagica2.config.getAuraBehaviour());
		writer.add(ArsMagica2.config.getAuraScale());
		writer.add(ArsMagica2.config.getAuraAlpha());
		writer.add(ArsMagica2.config.getAuraColorRandom());
		writer.add(ArsMagica2.config.getAuraColorDefault());
		writer.add(ArsMagica2.config.getAuraColor());
		writer.add(ArsMagica2.config.getAuraDelay());
		writer.add(ArsMagica2.config.getAuraQuantity());
		writer.add(ArsMagica2.config.getAuraSpeed());

		byte[] data = writer.generate();

//		AMNetHandler.INSTANCE.sendPacketToServer(AMPacketIDs.SYNC_BETA_PARTICLES, data);

		ArsMagica2.config.save();

		super.onGuiClosed();
	}
	
	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {

		if (activeButton != null && activeButton instanceof GuiSlideControl){
			actionPerformed(activeButton);
		}
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
	}

	@Override
	protected void mouseClicked(int x, int y, int button) throws IOException{
		GuiButton clickedBtn = getControlByXY(x, y);
		if (clickedBtn != null && button == 1){
			if (clickedBtn.id == 10){
				int index = ArsMagica2.config.getAuraIndex();
				index--;
				if (index < 0) index = AMParticle.particleTypes.length - 1;

				while (AMParticle.particleTypes[index].startsWith("lightning_bolt") && ArsMagica2.proxy.playerTracker.getAAL(Minecraft.getMinecraft().thePlayer) < 3){
					index--;
					if (index < 0) index = AMParticle.particleTypes.length - 1;
				}

				ArsMagica2.config.setAuraIndex(index);
				btnParticleType.displayString = AMParticle.particleTypes[index];
			}else if (clickedBtn.id == 11){
				int index = ArsMagica2.config.getAuraBehaviour();
				index--;
				if (index < 0) index = ParticleController.AuraControllerOptions.length - 1;
				ArsMagica2.config.setAuraBehaviour(index);
				btnParticleBehaviour.displayString = ParticleController.AuraControllerOptions[index];
			}
		}
		super.mouseClicked(x, y, button);
	}

	private GuiButton getControlByXY(int x, int y){
		for (Object btn : this.buttonList){
			if (btn instanceof GuiButton){
				GuiButton button = (GuiButton)btn;
				if (button.mousePressed(mc, x, y))
					return button;
			}
		}
		return null;
	}
}


