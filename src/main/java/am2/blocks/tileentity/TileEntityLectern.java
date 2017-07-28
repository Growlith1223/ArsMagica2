package am2.blocks.tileentity;

import java.util.ArrayList;

import am2.ArsMagica2;
import am2.defs.ItemDefs;
import am2.packet.AMDataWriter;
import am2.packet.AMNetHandler;
import am2.packet.AMPacketIDs;
import am2.particles.AMParticle;
import am2.particles.ParticleFadeOut;
import am2.particles.ParticleMoveOnHeading;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityEnchantmentTable;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.MathHelper;

public class TileEntityLectern extends TileEntityEnchantmentTable implements ITickable{
	private ItemStack stack;
	private ItemStack tooltipStack;
	private boolean needsBook;
	private boolean overPowered;
	public int particleAge;
	public int particleMaxAge = 150;
	private boolean increasing = true;
	public TileEntityLectern(){
	}

	public void resetParticleAge(){
		particleAge = 0;
		increasing = true;
	}

	public ItemStack getTooltipStack(){
		return tooltipStack;
	}

	public void setTooltipStack(ItemStack stack){
		this.tooltipStack = stack;
	}

	@Override
	public void update(){
		if (world.isRemote){
			updateBookRender();
			if (tooltipStack != null && tickCount % 2 == 0){
				AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "sparkle", pos.getX() + 0.5 + ((world.rand.nextDouble() * 0.2) - 0.1), pos.getY() + 1, pos.getZ() + 0.5 + ((world.rand.nextDouble() * 0.2) - 0.1));
				if (particle != null){
					particle.AddParticleController(new ParticleMoveOnHeading(particle, world.rand.nextDouble() * 360, -45 - world.rand.nextInt(90), 0.05f, 1, false));
					particle.AddParticleController(new ParticleFadeOut(particle, 2, false).setFadeSpeed(0.05f).setKillParticleOnFinish(true));
					particle.setIgnoreMaxAge(true);
					if (getOverpowered()){
						particle.setRGBColorF(1.0f, 0.2f, 0.2f);
					}
				}
			}
		}
		
		world.markAndNotifyBlock(pos, world.getChunkFromBlockCoords(pos), world.getBlockState(pos), world.getBlockState(pos), 3);
	}

	private void updateBookRender() {
		particleAge++;
		if (increasing){
			particleMaxAge += 2;
			if (particleMaxAge - particleAge > 120)
				increasing = false;
		}else{
			if (particleMaxAge - particleAge < 5)
				increasing = true;
		}
		this.bookSpreadPrev = this.bookSpread;
		this.bookRotationPrev = this.bookRotation;

		this.bookSpread += 0.1F;

		if (this.bookSpread < 0.5F || world.rand.nextInt(40) == 0){
			float f1 = this.flipT;

			do{
				this.flipT += (float)(world.rand.nextInt(4) - world.rand.nextInt(4));
			}
			while (f1 == this.flipT);
		}

		while (this.bookRotation >= (float)Math.PI){
			this.bookRotation -= ((float)Math.PI * 2F);
		}

		while (this.bookRotation < -(float)Math.PI){
			this.bookRotation += ((float)Math.PI * 2F);
		}

		while (this.tRot >= (float)Math.PI){
			this.tRot -= ((float)Math.PI * 2F);
		}

		while (this.tRot < -(float)Math.PI){
			this.tRot += ((float)Math.PI * 2F);
		}

		float f2;

		for (f2 = this.tRot - this.bookRotation; f2 >= (float)Math.PI; f2 -= ((float)Math.PI * 2F));

		while (f2 < -(float)Math.PI){
			f2 += ((float)Math.PI * 2F);
		}

		this.bookRotation += f2 * 0.4F;

		if (this.bookSpread < 0.0F){
			this.bookSpread = 0.0F;
		}

		if (this.bookSpread > 1.0F){
			this.bookSpread = 1.0F;
		}

		++this.tickCount;
        this.pageFlipPrev = this.pageFlip;
        float f = (this.flipT - this.pageFlip) * 0.4F;
		float f3 = 0.2F;
		f = MathHelper.clamp(f, -f3, f3);
        this.flipA += (f - this.flipA) * 0.9F;
        this.pageFlip += this.flipA;
	}
	
	public ItemStack getStack(){
		return stack;
	}

	public boolean setStack(ItemStack stack){
		if (stack == null || getValidItems().contains(stack.getItem())){
			if (stack != null)
				stack.setCount(1);
			this.stack = stack;
			if (!this.world.isRemote){
				AMDataWriter writer = new AMDataWriter();
				writer.add(pos.getX());
				writer.add(pos.getY());
				writer.add(pos.getZ());
				if (stack == null){
					writer.add(false);
				}else{
					writer.add(true);
					writer.add(stack);
				}
				AMNetHandler.INSTANCE.sendPacketToAllClientsNear(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 32, AMPacketIDs.LECTERN_DATA, writer.generate());
			}
			return true;
		}
		return false;
	}

	public boolean hasStack(){
		return stack != null;
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket(){
		NBTTagCompound compound = new NBTTagCompound();
		writeToNBT(compound);
		SPacketUpdateTileEntity packet = new SPacketUpdateTileEntity(pos, getBlockMetadata(), compound);
		return packet;
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
		this.readFromNBT(pkt.getNbtCompound());
	}

	private ArrayList<Item> getValidItems(){
		ArrayList<Item> validItems = new ArrayList<Item>();

		validItems.add(Items.WRITTEN_BOOK);
		validItems.add(ItemDefs.arcaneCompendium);

//		if (Loader.isModLoaded("Thaumcraft")){
//			ItemStack item = thaumcraft.api.ItemApi.getItem("itemThaumonomicon", 0);
//			if (item != null){
//				validItems.add(item.getItem());
//			}
//		}

		return validItems;
	}

	@Override
	public void readFromNBT(NBTTagCompound comp){
		super.readFromNBT(comp);
		if (comp.hasKey("placedBook")){
			NBTTagCompound bewk = comp.getCompoundTag("placedBook");
			stack = new ItemStack(bewk);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound comp){
		super.writeToNBT(comp);
		if (stack != null){
			NBTTagCompound bewk = new NBTTagCompound();
			stack.writeToNBT(bewk);
			comp.setTag("placedBook", bewk);
		}
		return comp;
	}

	public void setNeedsBook(boolean b){
		this.needsBook = b;
	}

	public boolean getNeedsBook(){
		return this.needsBook;
	}

	public void setOverpowered(boolean b){
		this.overPowered = b;
	}

	public boolean getOverpowered(){
		return this.overPowered;
	}

}
