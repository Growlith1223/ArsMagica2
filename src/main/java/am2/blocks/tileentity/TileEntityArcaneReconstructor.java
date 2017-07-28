package am2.blocks.tileentity;

import am2.ArsMagica2;
import am2.api.blocks.IKeystoneLockable;
import am2.api.event.ReconstructorRepairEvent;
import am2.api.math.AMVector3;
import am2.defs.AMSounds;
import am2.entity.EntityDummyCaster;
import am2.items.ItemFocusCharge;
import am2.items.ItemFocusMana;
import am2.particles.AMParticle;
import am2.particles.ParticleFadeOut;
import am2.particles.ParticleFloatUpward;
import am2.power.PowerNodeRegistry;
import am2.power.PowerTypes;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;

public class TileEntityArcaneReconstructor extends TileEntityAMPower implements IInventory, ISidedInventory, IKeystoneLockable<TileEntityArcaneReconstructor>{

	private NonNullList<ItemStack> inventory;
	private boolean active;
	private int repairCounter;
	private final static float repairCostPerDamagePoint = 250;
	private float ringOffset;
	private final AMVector3 outerRingRotation;
	private final AMVector3 middleRingRotation;
	private final AMVector3 innerRingRotation;
	private float rotateOffset = 0;
	private int deactivationDelayTicks = 0;

	private EntityLiving dummyEntity;

	private AMVector3 outerRingRotationSpeeds;
	private AMVector3 middleRingRotationSpeeds;
	private AMVector3 innerRingRotationSpeeds;

	private static final int SLOT_ACTIVE = 3;
	private boolean isFirstTick = true;

	public TileEntityArcaneReconstructor(){
		super(500);
		inventory = NonNullList.<ItemStack>withSize(getSizeInventory(), ItemStack.EMPTY);
		active = false;
		repairCounter = 0;

		outerRingRotation = new AMVector3(0, 0, 0);
		middleRingRotation = new AMVector3(0, 0, 0);
		innerRingRotation = new AMVector3(0, 0, 0);

	}

	@Override
	public float particleOffset(int axis){
		if (axis == 1)
			return 0.25f;
		return 0.5f;
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket(){
		NBTTagCompound compound = new NBTTagCompound();
		writeToNBT(compound);
		SPacketUpdateTileEntity packet = new SPacketUpdateTileEntity(pos, 0, compound);
		return packet;
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
		this.readFromNBT(pkt.getNbtCompound());
	}

	@Override
	public void update(){
		if (isFirstTick) {
			outerRingRotationSpeeds = new AMVector3(world.rand.nextDouble() * 4 - 2, world.rand.nextDouble() * 4 - 2, world.rand.nextDouble() * 4 - 2);
			middleRingRotationSpeeds = new AMVector3(world.rand.nextDouble() * 4 - 2, world.rand.nextDouble() * 4 - 2, world.rand.nextDouble() * 4 - 2);
			innerRingRotationSpeeds = new AMVector3(world.rand.nextDouble() * 4 - 2, world.rand.nextDouble() * 4 - 2, world.rand.nextDouble() * 4 - 2);
			isFirstTick = false;
		}

		if (PowerNodeRegistry.For(this.world).checkPower(this, this.getRepairCost())) {// has enough power
			if ((repairCounter++ % getRepairRate() == 0) && (!queueRepairableItem())) {// has ticked and already has item queued
				if (performRepair()){// something to repair
					if (!world.isRemote){
						PowerNodeRegistry.For(this.world).consumePower(this, PowerNodeRegistry.For(world).getHighestPowerType(this), this.getRepairCost());
					}
				}
			}
			deactivationDelayTicks = 0;
		} else if (!world.isRemote && active){// out of power, on server and active
			if (deactivationDelayTicks++ > 100) {// 5 seconds
				deactivationDelayTicks = 0;
				this.active = false;
				if (!world.isRemote)
					world.markAndNotifyBlock(pos, world.getChunkFromBlockCoords(pos), world.getBlockState(pos), world.getBlockState(pos), 2);
			}
		}
		if (world.isRemote){
			updateRotations();
			if (shouldRenderItemStack()){
				AMParticle p = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "sparkle2", pos.getX() + 0.2 + (world.rand.nextDouble() * 0.6), pos.getY() + 0.4, pos.getZ() + 0.2 + (world.rand.nextDouble() * 0.6));
				if (p != null){
					p.AddParticleController(new ParticleFloatUpward(p, 0.0f, 0.02f, 1, false));
					p.setIgnoreMaxAge(true);
					p.setParticleScale(0.1f);
					p.AddParticleController(new ParticleFadeOut(p, 1, false).setFadeSpeed(0.035f).setKillParticleOnFinish(true));
					p.setRGBColorF(1, 0, 1);
				}
			}
		}

		super.update();
	}

	public AMVector3 getOuterRingRotationSpeed(){
		return this.outerRingRotationSpeeds;
	}

	public AMVector3 getMiddleRingRotationSpeed(){
		return this.middleRingRotationSpeeds;
	}

	public AMVector3 getInnerRingRotationSpeed(){
		return this.innerRingRotationSpeeds;
	}

	public float getRotateOffset(){
		return this.rotateOffset;
	}

	public boolean shouldRenderRotateOffset(){
		return this.ringOffset >= 0.5 && this.rotateOffset > 0;
	}

	private void updateRotations(){
		if (active){
			if (ringOffset < 0.5f){
				ringOffset += 0.015f;
			}else{
				ringOffset = 0.51f;
				outerRingRotation.add(outerRingRotationSpeeds);
				middleRingRotation.add(middleRingRotationSpeeds);
				innerRingRotation.add(innerRingRotationSpeeds);
			}

			if (rotateOffset < 10){
				rotateOffset += 0.0625f;
			}
		}else{
			if (rotateOffset > 0){
				rotateOffset -= 0.025f;
			}
			if (!outerRingRotation.isZero() || !middleRingRotation.isZero() || !innerRingRotation.isZero()){
				outerRingRotation.x = easeCoordinate(outerRingRotation.x, outerRingRotationSpeeds.x);
				outerRingRotation.y = easeCoordinate(outerRingRotation.y, outerRingRotationSpeeds.y);
				outerRingRotation.z = easeCoordinate(outerRingRotation.z, outerRingRotationSpeeds.z);

				middleRingRotation.x = easeCoordinate(middleRingRotation.x, middleRingRotationSpeeds.x);
				middleRingRotation.y = easeCoordinate(middleRingRotation.y, middleRingRotationSpeeds.y);
				middleRingRotation.z = easeCoordinate(middleRingRotation.z, middleRingRotationSpeeds.z);

				innerRingRotation.x = easeCoordinate(innerRingRotation.x, innerRingRotationSpeeds.x);
				innerRingRotation.y = easeCoordinate(innerRingRotation.y, innerRingRotationSpeeds.y);
				innerRingRotation.z = easeCoordinate(innerRingRotation.z, innerRingRotationSpeeds.z);
			}else{
				if (ringOffset > 0f){
					ringOffset -= 0.03f;
				} else {
					ringOffset = 0f;
				}
			}
		}
	}

	private float easeCoordinate(float coord, float step){
		step = Math.abs(step);
		step = 4;
		float calc = coord;
		if (Math.abs(coord % 360) > -4 && Math.abs(coord % 360) < 4){
			return 0;
		}
		if (calc < 0){
			calc %= -360;
			if (calc <= -180){
				coord -= step;
			}else{
				coord += step;
			}
		}else{
			calc %= 360;
			if (calc >= 180){
				coord += step;
			}else{
				coord -= step;
			}
		}
		return coord;
	}

	public float getOffset(){
		return this.ringOffset;
	}

	public boolean shouldRenderItemStack(){
		return active && ringOffset >= 0.5;
	}

	public ItemStack getCurrentItem(){
		return inventory.get(SLOT_ACTIVE);
	}

	public AMVector3 getInnerRingRotation(){
		return innerRingRotation;
	}

	public AMVector3 getMiddleRingRotation(){
		return middleRingRotation;
	}

	public AMVector3 getOuterRingRotation(){
		return outerRingRotation;
	}

	private boolean queueRepairableItem(){
		if (!inventory.get(SLOT_ACTIVE).isEmpty()) {
			if (!active) {
				this.active = true;
				if (!world.isRemote)
					world.markAndNotifyBlock(pos, world.getChunkFromBlockCoords(pos), world.getBlockState(pos), world.getBlockState(pos), 2);
			}
			return false;
		}
		for (int i = 4; i < 10; ++i){
			if (itemStackIsValid(inventory.get(i))){
				inventory.set(SLOT_ACTIVE, inventory.get(i).copy());
				inventory.set(i, ItemStack.EMPTY);
				this.active = true;
				if (!world.isRemote)
					world.markAndNotifyBlock(pos, world.getChunkFromBlockCoords(pos), world.getBlockState(pos), world.getBlockState(pos), 2);
				return true;
			}
		}
		this.active = false;
		if (!world.isRemote)
			world.markAndNotifyBlock(pos, world.getChunkFromBlockCoords(pos), world.getBlockState(pos), world.getBlockState(pos), 2);
		return true;
	}

	private boolean itemStackIsValid(ItemStack stack){
		return stack != null && !(stack.getItem() instanceof ItemBlock) && stack.getItem().isRepairable();
	}

	private EntityLiving getDummyEntity(){
		if (dummyEntity == null)
			dummyEntity = new EntityDummyCaster(this.world);
		return dummyEntity;
	}

	private boolean performRepair(){
		if (inventory.get(SLOT_ACTIVE).isEmpty()) return false;

		ReconstructorRepairEvent event = new ReconstructorRepairEvent(inventory.get(SLOT_ACTIVE));
		if (MinecraftForge.EVENT_BUS.post(event)){
			return true;
		}

		if (inventory.get(SLOT_ACTIVE).isItemDamaged()){
			if (!world.isRemote)
				inventory.get(SLOT_ACTIVE).damageItem(-1, getDummyEntity());
			return true;
		}else{
			boolean did_copy = false;
			for (int i = 10; i < 16; ++i){
				if (inventory.get(i).isEmpty()){
					if (!world.isRemote){
						inventory.set(i, inventory.get(SLOT_ACTIVE).copy());
						inventory.set(SLOT_ACTIVE, ItemStack.EMPTY);
					}
					did_copy = true;
					break;
				}
			}
			world.playSound(pos.getX(), pos.getY(), pos.getZ(), AMSounds.RECONSTRUCTOR_COMPLETE, SoundCategory.BLOCKS, 1.0f, 1.0f, true);

			return did_copy;
		}
	}

	@Override
	public int getSizeInventory(){
		return 19;
	}

	@Override
	public ItemStack getStackInSlot(int var1){
		if (var1 >= inventory.size()){
			return null;
		}
		return inventory.get(var1);
	}

	@Override
	public ItemStack decrStackSize(int i, int j){
		if (!inventory.get(i).isEmpty()){
			if (inventory.get(i).getCount() <= j){
				ItemStack itemstack = inventory.get(i);
				inventory.set(i, ItemStack.EMPTY);
				return itemstack;
			}
			ItemStack itemstack1 = inventory.get(i).splitStack(j);
			if (inventory.get(i).getCount() == 0){
				inventory.set(i, ItemStack.EMPTY);
			}
			return itemstack1;
		}else{
			return ItemStack.EMPTY;
		}
	}

	@Override
	public ItemStack removeStackFromSlot(int i){
		if (!inventory.get(i).isEmpty()){
			ItemStack itemstack = inventory.get(i);
			inventory.set(i, ItemStack.EMPTY);
			return itemstack;
		}else{
			return ItemStack.EMPTY;
		}
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack){
		inventory.set(i, itemstack);
		if (!itemstack.isEmpty() && itemstack.getCount() > getInventoryStackLimit()){
			itemstack.setCount(getInventoryStackLimit());
		}
	}

	@Override
	public String getName(){
		return "ArcaneReconstructor";
	}

	@Override
	public int getInventoryStackLimit(){
		return 1;
	}

	@Override
	public void openInventory(EntityPlayer player){
	}

	@Override
	public void closeInventory(EntityPlayer player){
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound){
		super.readFromNBT(nbttagcompound);
		NBTTagList nbttaglist = nbttagcompound.getTagList("ArcaneReconstructorInventory", Constants.NBT.TAG_COMPOUND);
		inventory = NonNullList.<ItemStack>withSize(getSizeInventory(), ItemStack.EMPTY);
		for (int i = 0; i < nbttaglist.tagCount(); i++){
			String tag = String.format("ArrayIndex", i);
			NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.getCompoundTagAt(i);
			byte byte0 = nbttagcompound1.getByte(tag);
			if (byte0 >= 0 && byte0 < inventory.size()){
				inventory.set(i, new ItemStack(nbttagcompound1));
			}
		}
		active = nbttagcompound.getBoolean("ArcaneReconstructorActive");
		repairCounter = nbttagcompound.getInteger("ArcaneReconstructorRepairCounter");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound){
		super.writeToNBT(nbttagcompound);
		NBTTagList nbttaglist = new NBTTagList();
		for (int i = 0; i < inventory.size(); i++){
			if (!inventory.get(i).isEmpty()){
				String tag = String.format("ArrayIndex", i);
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte(tag, (byte)i);
				ItemStack s = inventory.get(i);
				s.writeToNBT(nbttagcompound1);
				inventory.set(i, s);
				nbttaglist.appendTag(nbttagcompound1);
			}
		}

		nbttagcompound.setTag("ArcaneReconstructorInventory", nbttaglist);
		nbttagcompound.setBoolean("ArcaneReconstructorActive", active);
		nbttagcompound.setInteger("ArcaneReconstructorRepairCounter", repairCounter);
		return nbttagcompound;
	}

	private int numFociOfType(Class<?> type){
		int count = 0;
		for (int i = 0; i < 3; ++i){
			if (!inventory.get(i).isEmpty() && type.isInstance(inventory.get(i).getItem())){
				count++;
			}
		}
		return count;
	}

	private int getRepairRate(){
		int numFoci = numFociOfType(ItemFocusCharge.class);
		int base = 20;
		if (numFoci > 0){
			base -= 5 * numFoci;
		}

		return base;
	}

	private float getRepairCost(){
		int numFoci = numFociOfType(ItemFocusMana.class);
		float base = repairCostPerDamagePoint;
		float deduction = 100;
		for (int i = 0; i < numFoci; ++i){
			base -= deduction;
			deduction /= 2;
		}

		return base;
	}

	@Override
	public boolean hasCustomName(){
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack){
		return i >= 4 && i < 10 && itemStackIsValid(itemstack);
	}

	@Override
	public boolean canProvidePower(PowerTypes type){
		return false;
	}

	@Override
	public int[] getSlotsForFace(EnumFacing var1){
		return new int[]{4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, EnumFacing j){
		return i >= 4 && i < 10 && itemStackIsValid(itemstack);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, EnumFacing j){
		return i >= 10 && i < 16;
	}

	@Override
	public int getChargeRate(){
		return 250;
	}

	@Override
	public boolean canRelayPower(PowerTypes type){
		return false;
	}

	@Override
	public ItemStack[] getRunesInKey(){
		ItemStack[] runes = new ItemStack[3];
		runes[0] = inventory.get(16);
		runes[1] = inventory.get(17);
		runes[2] = inventory.get(18);

		return runes;
	}

	@Override
	public boolean keystoneMustBeHeld(){
		return false;
	}

	@Override
	public boolean keystoneMustBeInActionBar(){
		return false;
	}

	@Override
	public ITextComponent getDisplayName() {

		return null;
	}

	@Override
	public int getField(int id) {

		return 0;
	}

	@Override
	public void setField(int id, int value) {

		
	}

	@Override
	public int getFieldCount() {

		return 0;
	}

	@Override
	public void clear() {

		
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemstack : this.inventory)
        {
            if (!itemstack.isEmpty())
            {
                return false;
            }
        }

        return true;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return true;
	}
}
