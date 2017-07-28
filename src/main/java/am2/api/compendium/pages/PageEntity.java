package am2.api.compendium.pages;

import java.io.IOException;

import am2.bosses.AM2Boss;
import am2.entity.EntityFlicker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class PageEntity extends CompendiumPage<Entity> {
	
	private Render<Entity> renderer;
	private float curRotationH = 0;
	private int lastMouseX = 0;
	private boolean isDragging;

	@SuppressWarnings("unchecked")
	public PageEntity(Entity element) throws Throwable{
		super(element);
		renderer = (Render<Entity>) Minecraft.getMinecraft().getRenderManager().entityRenderMap.get(element.getClass());
		
	}

	@Override
	protected void renderPage(int posX, int posY, int mouseX, int mouseY) {
		if (renderer == null)
			return;
		int cx = posX + 60;
		int cy = posY + 92;
		NBTTagCompound compound = element.writeToNBT(new NBTTagCompound());
		
		GlStateManager.pushMatrix();
		try {
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.translate((float)(cx - 2), (float)(cy + 20), -3.0F + Minecraft.getMinecraft().getRenderItem().zLevel);
			GlStateManager.scale(10.0F, 10.0F, 10.0F);
			GlStateManager.translate(1.0F, 6.5F, 1.0F);
			GlStateManager.scale(6.0F, 6.0F, -1.0F);
			GlStateManager.rotate(210.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
	
			GlStateManager.pushMatrix();
			GlStateManager.pushAttrib();
			try {
				if (element instanceof AM2Boss){
					float scaleFactorX = (1 / element.width);
					float scaleFactorY = (2 / element.height);
					float scaleFactor = Math.min(scaleFactorX, scaleFactorY);
					GlStateManager.scale(scaleFactor, scaleFactor, scaleFactor);
				}else if (element instanceof EntityFlicker){
					GlStateManager.translate(0, 1.3f, 0);
				}
				GlStateManager.rotate(curRotationH, 0, 1, 0);
	
				//entity, x, y, z, yaw, partialtick
				Entity ent = element.getClass().getConstructor(World.class).newInstance(Minecraft.getMinecraft().world);
				ent.readFromNBT(compound);
				renderer.doRender(ent, 0.0, 0.0, 0.0, 90.0F, 0.0F);
				GlStateManager.disableRescaleNormal();
		        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		        GlStateManager.disableTexture2D();
		        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
			} catch(Exception e) {
				
			}
			GlStateManager.popMatrix();
			GlStateManager.popAttrib();
		} catch(Exception e) {
			
		}
		GlStateManager.popMatrix();

		String renderString = "Click and drag to rotate";
		mc.fontRenderer.drawString(renderString, posX + 72 - (mc.fontRenderer.getStringWidth(renderString) / 2), posY + 200, 0x000000);
	}
	
	@Override
	public void dragMouse(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		if (isDragging) {
			curRotationH -= (lastMouseX - mouseX);
			lastMouseX = mouseX;
		}
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		isDragging = true;
		lastMouseX = mouseX;
	}
	
	@Override
	public void mouseReleased(int mouseX, int mouseY, int state) {
		if (state == 1)
			isDragging = false;
	}
}
