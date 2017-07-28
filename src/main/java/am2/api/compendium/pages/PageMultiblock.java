package am2.api.compendium.pages;

import static net.minecraft.client.renderer.texture.TextureMap.LOCATION_BLOCKS_TEXTURE;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.lwjgl.opengl.GL11;

import am2.api.blocks.MultiblockGroup;
import am2.api.blocks.MultiblockStructureDefinition;
import am2.api.blocks.TypedMultiblockGroup;
import am2.gui.AMGuiHelper;
import am2.gui.GuiBlockAccess;
import am2.gui.controls.GuiButtonCompendiumNext;
import am2.gui.controls.GuiButtonVariableDims;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;

@SuppressWarnings("deprecation")
public class PageMultiblock extends CompendiumPage<MultiblockStructureDefinition> {

	private int curLayer = -1;
	private int maxLayers = 0;
	private GuiBlockAccess blockAccess = new GuiBlockAccess();
	private GuiButtonCompendiumNext prevLayer;
	private GuiButtonCompendiumNext nextLayer;
	private GuiButtonVariableDims pauseCycling;
	private ItemStack stackTip = null;
	private static final ResourceLocation red = new ResourceLocation("arsmagica2", "textures/blocks/red.png");

	public PageMultiblock(MultiblockStructureDefinition element) {
		super(element);
		maxLayers = element.getHeight();
	}
	
	@Override
	public GuiButton[] getButtons(int id, int posX, int posY) {
		prevLayer = new GuiButtonCompendiumNext(id++, posX, posY + 19, false);
		nextLayer = new GuiButtonCompendiumNext(id++, posX + 125, posY + 19, true);
		pauseCycling = new GuiButtonVariableDims(5, posX + 105, posY + 190, AMGuiHelper.instance.runCompendiumTicker ? I18n.translateToLocal("am2.gui.pause") : I18n.translateToLocal("am2.gui.cycle")).setDimensions(40, 20);
		prevLayer.visible = true;
		nextLayer.visible = true;
		pauseCycling.visible = true;
		return new GuiButton[] {prevLayer, nextLayer, pauseCycling};
	}
	
	@Override
	public void switchButtonDisplay(boolean shouldShow) {
		if (shouldShow) {
			prevLayer.visible = true;
			nextLayer.visible = true;
			pauseCycling.visible = true;
		} else {
			prevLayer.visible = false;
			nextLayer.visible = false;
			pauseCycling.visible = false;
		}
	}
	
	@Override
	public void actionPerformed(GuiButton button) throws IOException {
		if (button == nextLayer) {
			curLayer++;
			if (curLayer > maxLayers){
				curLayer = -1;
			}
		} else if (button == prevLayer) {
			curLayer--;
			if (curLayer < -1){
				curLayer = maxLayers;
			}
		} else if (button == pauseCycling) {
			AMGuiHelper.instance.runCompendiumTicker = !AMGuiHelper.instance.runCompendiumTicker;
			pauseCycling.displayString = AMGuiHelper.instance.runCompendiumTicker ? I18n.translateToLocal("am2.gui.pause") : I18n.translateToLocal("am2.gui.cycle");
		}
		super.actionPerformed(button);
	}
	
	@Override
	protected void renderPage(int posX, int posY, int mouseX, int mouseY) {
		stackTip = null;
		int cx = posX + 60;
		int cy = posY + 92;
		String label = String.format("%s: %s", I18n.translateToLocal("am2.gui.layer"), curLayer == -1 ? I18n.translateToLocal("am2.gui.all") : "" + curLayer);

		mc.fontRenderer.drawString(label, cx - mc.fontRenderer.getStringWidth(label) / 2, cy - 90, 0x000000);

		GlStateManager.pushMatrix();
		GlStateManager.pushAttrib();
		GlStateManager.disableCull();
		GlStateManager.disableLighting();

		/*ArrayList<IBlockState> blox = entryMultiblock.getAllowedBlocksAt(entryMultiblock.new BlockPos(0, 0, 0));
		if (blox != null){
			renderBlock(Block.blocksList[blox.get(0).getID()], blox.get(0).getMeta(), cx, cy);
		}*/
		BlockPos pickedBlock = getPickedBlock(cx, cy, mouseX, mouseY);
		if (curLayer == -1){
			for (int i = element.getMinY(); i <= element.getMaxY(); ++i){
				int y = (i - element.getMinY());
				GlStateManager.translate(0.0f, 0.0f, 20f * y);
				drawMultiblockLayer(cx, cy, i, pickedBlock, mouseX, mouseY);
			}
		}else{
			int i = element.getMinY() + curLayer;
			GlStateManager.translate(0.0f, 0.0f, 20f * curLayer);
			drawMultiblockLayer(cx, cy, i, pickedBlock, mouseX, mouseY);
		}
		if (stackTip != null)
			renderItemToolTip(stackTip, mouseX, mouseY);
		GlStateManager.popAttrib();
		GlStateManager.popMatrix();
	}
	
	private BlockPos getPickedBlock(int cx, int cy, int mousex, int mousey){
		BlockPos block = null;

		float step_x = 14f;
		float step_y = -16.0f;
		float step_z = 7f;

		cy -= step_y * element.getMinY() / 2;
		cy -= step_y * element.getMaxY() / 2;

		int start = curLayer == -1 ? element.getMinY() : element.getMinY() + curLayer;
		int end = curLayer == -1 ? element.getMaxY() : element.getMinY() + curLayer;

		for (int i = start; i <= end; ++i){
			TreeMap<BlockPos, List<IBlockState>> layerBlocksSorted = getMultiblockLayer(i);

			float px = cx - (step_x * (element.getWidth() / 2));
			float py = cy - (step_z * (element.getLength() / 2));
						
			for (BlockPos bc : layerBlocksSorted.keySet()){
				float x = px + ((bc.getX() - bc.getZ()) * step_x);
				float y = py + ((bc.getZ() + bc.getX()) * step_z) + (step_y * i);

				x += 20;
				y -= 10;

				if (mousex > x && mousex < x + 32){
					if (mousey > y && mousey < y + 32){
						block = bc;
					}
				}
			}
		}
		return block;
	}
	
	private void drawMultiblockLayer(int cx, int cy, int layer, BlockPos pickedBlock, int mousex, int mousey){
		TreeMap<BlockPos, List<IBlockState>> layerBlocksSorted = getMultiblockLayer(layer);
		float step_x = 14f;
		float step_y = -16.0f;
		float step_z = 7f;
		cy -= step_y * element.getMinX() / 2;
		cy -= step_y * element.getMaxY() / 2;

		float px = cx - (step_x * (element.getWidth() / 2));
		float py = cy - (step_z * (element.getLength() / 2));

		for (BlockPos bc : layerBlocksSorted.keySet()){
			//if (bc.getX() == 0 && bc.getY() == 0 && bc.getZ() == 0) continue;
			IBlockState bd = layerBlocksSorted.get(bc).get(AMGuiHelper.instance.getSlowTicker() % layerBlocksSorted.get(bc).size());
			float x = px + ((bc.getX() - bc.getZ()) * step_x);
			float y = py + ((bc.getZ() + bc.getX()) * step_z) + (step_y * layer);
			GL11.glPushMatrix();
			GL11.glTranslatef(0, 0, 15 * bc.getX());
			boolean picked = pickedBlock != null && bc.equals(pickedBlock);
			renderBlock(bd, x, y, bc.getX(), bc.getY(), bc.getZ(), picked);
			GL11.glPopMatrix();

			if (picked){
				ItemStack stack = new ItemStack(bd.getBlock(), 1, bd.getBlock().getMetaFromState(bd));
				if (stack.getItem() != null){
					stackTip  = stack;
				}
			}
		}
	}
	
	private TreeMap<BlockPos, List<IBlockState>> getMultiblockLayer(int layer){
		TreeMap<BlockPos, List<IBlockState>> layerBlocksSorted = new TreeMap<>();

		for (MultiblockGroup mutex : element.getGroups()){
			HashMap<BlockPos, List<IBlockState>> layerBlocks = element.getStructureLayer(mutex, layer);
			for (BlockPos bc : layerBlocks.keySet()){
				if (mutex instanceof TypedMultiblockGroup) {
					TypedMultiblockGroup newGroup = (TypedMultiblockGroup) mutex;
					layerBlocksSorted.put(bc, newGroup.getState(bc));
				} else {
					layerBlocksSorted.put(bc, layerBlocks.get(bc));
				}
			}
		}

		return layerBlocksSorted;
	}
	
	private void renderBlock(IBlockState state, float x, float y, int offsetX, int offsetY, int offsetZ, boolean picked){

		RenderHelper.disableStandardItemLighting();

		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 15, y + 3, 12.0F * offsetZ);
		GlStateManager.translate(0, 0, 40);
		GlStateManager.scale(20.0F, 20.0F, 20.0F);
		GlStateManager.translate(1.0F, 0.5F, 1.0F);
		GlStateManager.scale(1.0F, 1.0F, -1.0F);
		GlStateManager.rotate(210.0F, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
		this.blockAccess.setFakeBlockAndMeta(state);
		if (picked)
			mc.renderEngine.bindTexture(red);
		else
			mc.renderEngine.bindTexture(LOCATION_BLOCKS_TEXTURE);
		GlStateManager.enableLighting();
		if (state.getBlock() instanceof ITileEntityProvider)
			TileEntityRendererDispatcher.instance.renderTileEntityAt(((ITileEntityProvider)state.getBlock()).createNewTileEntity(Minecraft.getMinecraft().world, state.getBlock().getMetaFromState(state)), 0, 0, 0, 0, 0);
		GlStateManager.disableLighting();
		Tessellator.getInstance().getBuffer().begin(7, DefaultVertexFormats.BLOCK);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlock(state, new BlockPos(0, 0, 0), blockAccess , Tessellator.getInstance().getBuffer());
		Tessellator.getInstance().draw();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}
}
