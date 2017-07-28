package am2.blocks.tileentity;

import am2.ObeliskFuelHelper;
import am2.api.IMultiblockStructureController;
import am2.api.blocks.MultiblockGroup;
import am2.api.blocks.MultiblockStructureDefinition;
import am2.api.blocks.TypedMultiblockGroup;
import am2.buffs.BuffEffectManaRegen;
import am2.defs.BlockDefs;
import am2.defs.PotionEffectsDefs;
import am2.packet.AMDataReader;
import am2.packet.AMDataWriter;
import am2.packet.AMNetHandler;
import am2.power.PowerNodeRegistry;
import am2.power.PowerTypes;
import am2.utils.InventoryUtilities;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.List;

public class TileEntityObelisk extends TileEntityAMPower implements IMultiblockStructureController, IInventory, ITileEntityAMBase {
	protected static int pillarBlockID = 98; //stone brick
	protected static int pillarBlockMeta = 3; //arcane texture
	protected int surroundingCheckTicks;
	private NonNullList<ItemStack> inventory = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);
	protected float powerMultiplier = 1f;
	protected float powerBase = 5.0f;

	public float offsetY = 0;
	public float lastOffsetY = 0;

	public int burnTimeRemaining = 0;
	public int maxBurnTime = 1;

	private static final byte PK_BURNTIME_CHANGE = 1;

	protected MultiblockStructureDefinition structure;
	protected MultiblockGroup wizardChalkCircle;
	protected MultiblockGroup pillars;
	protected HashMap<IBlockState, Float> caps;
	protected TypedMultiblockGroup capsGroup;

	// obelisk rituals
	protected MultiblockStructureDefinition[] rituals;
	protected MultiblockGroup ritual1Chalk;
	protected MultiblockGroup ritual1Candles;
	protected MultiblockGroup ritual2Chalk;
	protected MultiblockGroup ritual2Candles;
	
	protected HashMap<Integer, IBlockState> createMap(IBlockState state) {
		HashMap<Integer, IBlockState> states = new HashMap<>();
		states.put(0, state);
		return states;
	}
	
	public TileEntityObelisk(){
		this(5000);
		inventory = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);
	}

	protected void checkNearbyBlockState(){
		List<MultiblockGroup> groups = structure.getMatchingGroups(world, pos);

		float capsLevel = 1;
		boolean pillarsFound = false;
		boolean wizChalkFound = false;
		boolean capsFound = false;

		for (MultiblockGroup group : groups){
			if (group == pillars)
				pillarsFound = true;
			else if (group == wizardChalkCircle)
				wizChalkFound = true;
			else if (group == capsGroup)
				capsFound = true;
		}
		
		if (pillarsFound && capsFound) {
			IBlockState capState = world.getBlockState(pos.add(2, 2, 2));
			
			for (IBlockState cap : caps.keySet()){
				if (capState == cap){
					capsLevel = caps.get(cap);
					break;
				}
			}
		}

		powerMultiplier = 1;

		if (wizChalkFound)
			powerMultiplier = 1.25f;

		if (pillarsFound)
			powerMultiplier *= capsLevel;
	}

	@SuppressWarnings("unchecked")
	public TileEntityObelisk(int capacity){
		super(capacity);
		setNoPowerRequests();
		surroundingCheckTicks = 0;

		structure = new MultiblockStructureDefinition("obelisk_structure");
		pillars = new MultiblockGroup("pillars", Lists.newArrayList(Blocks.STONEBRICK.getDefaultState()), false);
		caps = new HashMap<>();
		capsGroup = new TypedMultiblockGroup("caps", Lists.newArrayList(createMap(Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.CHISELED))), false);
		caps.put(Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.CHISELED), 1.35f);
		
		MultiblockGroup obelisk = new MultiblockGroup("obelisk", Lists.newArrayList(BlockDefs.obelisk.getDefaultState()), true);
		
		obelisk.addBlock(BlockPos.ORIGIN);

		pillars.addBlock(new BlockPos (-2, 0, -2));
		pillars.addBlock(new BlockPos (-2, 1, -2));
		capsGroup.addBlock(new BlockPos (-2, 2, -2), 0);

		pillars.addBlock(new BlockPos (2, 0, -2));
		pillars.addBlock(new BlockPos (2, 1, -2));
		capsGroup.addBlock(new BlockPos (2, 2, -2), 0);

		pillars.addBlock(new BlockPos (-2, 0, 2));
		pillars.addBlock(new BlockPos (-2, 1, 2));
		capsGroup.addBlock(new BlockPos (-2, 2, 2), 0);

		pillars.addBlock(new BlockPos (2, 0, 2));
		pillars.addBlock(new BlockPos (2, 1, 2));
		capsGroup.addBlock(new BlockPos (2, 2, 2), 0);

		wizardChalkCircle = addWizChalkGroupToStructure(structure);
		structure.addGroup(pillars);
		structure.addGroup(capsGroup);
		structure.addGroup(wizardChalkCircle);
		structure.addGroup(obelisk);

		// Obelisk ritual - Light
		rituals = new MultiblockStructureDefinition[2];
		rituals[0] = new MultiblockStructureDefinition("obelisk_light");

		ritual1Candles = new MultiblockGroup("candles", Lists.newArrayList(BlockDefs.wardingCandle.getDefaultState()), false);

		ritual1Candles.addBlock(new BlockPos(-2, 0, -2));
		ritual1Candles.addBlock(new BlockPos(2, 0, 2));
		ritual1Candles.addBlock(new BlockPos(2, 0, -2));
		ritual1Candles.addBlock(new BlockPos(-2, 0, 2));
		//rituals[0].addGroup(obelisk);
		rituals[0].addGroup(ritual1Candles);
		// Obelisk ritual - Dark
		rituals[1] = new MultiblockStructureDefinition("obelisk_dark");
	}

	public boolean isActive(){
		return burnTimeRemaining > 0 || (!inventory.get(0).isEmpty() && ObeliskFuelHelper.instance.getFuelBurnTime(inventory.get(0)) > 0);
	}

	public boolean isHighPowerActive(){
		return burnTimeRemaining > 200 && !inventory.get(0).isEmpty();
	}

	public int getCookProgressScaled(int par1){
		return burnTimeRemaining * par1 / maxBurnTime;
	}

	protected MultiblockGroup addWizChalkGroupToStructure(MultiblockStructureDefinition def){
		MultiblockGroup group = new MultiblockGroup("wizardChalkCircle", Lists.newArrayList(BlockDefs.wizardChalk.getDefaultState()), true);

		for (int i = -1; i <= 1; ++i){
			for (int j = -1; j <= 1; ++j){
				if (i == 0 && j == 0) continue;
				group.addBlock(new BlockPos(i, 0, j));
			}
		}

		return group;
	}

	protected void callSuperUpdate(){
		super.update();
	}

	private void setMaxBurnTime(int burnTime){
		if (burnTime == 0)
			burnTime = 1;
		maxBurnTime = burnTime;
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket(){
		SPacketUpdateTileEntity packet = new SPacketUpdateTileEntity(pos, getBlockMetadata(), getUpdateTag());
		return packet;
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
		this.readFromNBT(pkt.getNbtCompound());
	}

	private void sendCookUpdateToClients(){
		if (!world.isRemote){
			AMNetHandler.INSTANCE.sendObeliskUpdate(this, new AMDataWriter().add(PK_BURNTIME_CHANGE).add(this.burnTimeRemaining).generate());
		}
	}

	public void handlePacket(byte[] data){
		AMDataReader rdr = new AMDataReader(data);
		if (rdr.ID == TileEntityObelisk.PK_BURNTIME_CHANGE)
			this.burnTimeRemaining = rdr.getInt();
	}

	@Override
	public void update(){
		surroundingCheckTicks++;

		if (isActive()){
			if (!world.isRemote && surroundingCheckTicks % 100 == 0){
				checkNearbyBlockState();
				surroundingCheckTicks = 1;
				if (PowerNodeRegistry.For(this.world).checkPower(this, this.capacity * 0.1f)){
					List<EntityPlayer> nearbyPlayers = world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.pos.add(-2, 0, -2), pos.add(2, 3, 2)));
					for (EntityPlayer p : nearbyPlayers){
						if (p.isPotionActive(PotionEffectsDefs.manaRegen)) continue;
						p.addPotionEffect(new BuffEffectManaRegen(600, 1));
					}
				}
			}

			float powerAmt = PowerNodeRegistry.For(world).getPower(this, PowerTypes.NEUTRAL);
			float powerAdded = !inventory.get(0).isEmpty() ? ObeliskFuelHelper.instance.getFuelBurnTime(inventory.get(0)) * (powerBase * powerMultiplier) : 0;

			float chargeThreshold = Math.max(this.getCapacity() - powerAdded, this.getCapacity() * 0.75f);

			if (burnTimeRemaining <= 0 && powerAmt < chargeThreshold){
				burnTimeRemaining = ObeliskFuelHelper.instance.getFuelBurnTime(inventory.get(0));
				if (burnTimeRemaining > 0){
					setMaxBurnTime(burnTimeRemaining);
					if (this.inventory.get(0).getItem() instanceof UniversalBucket)
						this.inventory.set(0, ((UniversalBucket)this.inventory.get(0).getItem()).getEmpty().copy());
					else if (this.inventory.get(0).getItem().hasContainerItem(this.inventory.get(0)))
						this.inventory.set(0, new ItemStack(this.inventory.get(0).getItem().getContainerItem()));
					else
						InventoryUtilities.decrementStackQuantity(this, 0, 1);
					sendCookUpdateToClients();
				}
			}

			if (burnTimeRemaining > 0){
				burnTimeRemaining--;
				PowerNodeRegistry.For(world).insertPower(this, PowerTypes.NEUTRAL, powerBase * powerMultiplier);

				if (burnTimeRemaining % 20 == 0)
					sendCookUpdateToClients();
			}
		}else{
			surroundingCheckTicks = 1;
		}

		if (world.isRemote){
			lastOffsetY = offsetY;
			offsetY = (float)Math.max(Math.sin(world.getTotalWorldTime() / 20f) / 5, 0.25f);
			if (burnTimeRemaining > 0)
				burnTimeRemaining--;
		}
		markDirty();
		super.update();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox(){
		return new AxisAlignedBB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 2, pos.getY() + 0.3, pos.getZ() + 2);
	}

	@Override
	public MultiblockStructureDefinition getDefinition(){
		return structure;
	}

	public MultiblockStructureDefinition getRitual(int index){
		return rituals[index];
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound){
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setInteger("burnTimeRemaining", burnTimeRemaining);
		nbttagcompound.setInteger("maxBurnTime", maxBurnTime);

		if (inventory != null){
			NBTTagList nbttaglist = new NBTTagList();
			for (int i = 0; i < inventory.size(); i++){
				if (!inventory.get(i).isEmpty()){
					String tag = String.format("ArrayIndex", i);
					NBTTagCompound nbttagcompound1 = new NBTTagCompound();
					nbttagcompound1.setByte(tag, (byte)i);
					inventory.get(i).writeToNBT(nbttagcompound1);
					nbttaglist.appendTag(nbttagcompound1);
				}
			}

			nbttagcompound.setTag("BurnInventory", nbttaglist);
		}
		return nbttagcompound;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound){
		super.readFromNBT(nbttagcompound);
		burnTimeRemaining = nbttagcompound.getInteger("burnTimeRemaining");
		setMaxBurnTime(nbttagcompound.getInteger("maxBurnTime"));

		if (nbttagcompound.hasKey("BurnInventory")){
			NBTTagList nbttaglist = nbttagcompound.getTagList("BurnInventory", Constants.NBT.TAG_COMPOUND);
			inventory = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);
			for (int i = 0; i < nbttaglist.tagCount(); i++){
				String tag = String.format("ArrayIndex", i);
				NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.getCompoundTagAt(i);
				byte byte0 = nbttagcompound1.getByte(tag);
				if (byte0 >= 0 && byte0 < inventory.size()){
					inventory.set(byte0, new ItemStack(nbttagcompound1));
				}
			}
		}
	}

	@Override
	public int getChargeRate(){
		return 0;
	}

	@Override
	public boolean canProvidePower(PowerTypes type){
		return type == PowerTypes.NEUTRAL;
	}

	@Override
	public List<PowerTypes> getValidPowerTypes(){
		return Lists.newArrayList(PowerTypes.NEUTRAL);
	}

	@Override
	public boolean canRequestPower(){
		return false;
	}

	@Override
	public int getSizeInventory(){
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int i){
		if (i < 0 || i >= this.getSizeInventory())
			return ItemStack.EMPTY;
		return inventory.get(i);
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
			return null;
		}
	}

	@Override
	public ItemStack removeStackFromSlot(int i){
		if (!inventory.get(i).isEmpty()){
			ItemStack itemstack = inventory.get(i);
			inventory.set(i, ItemStack.EMPTY);
			return itemstack;
		}else{
			return null;
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
		return "obelisk";
	}

	@Override
	public boolean hasCustomName(){
		return false;
	}

	@Override
	public int getInventoryStackLimit(){
		return 64;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer entityplayer){
		if (world.getTileEntity(pos) != this){
			return false;
		}
		return entityplayer.getDistanceSqToCenter(pos) <= 64D;
	}

	@Override
	public void openInventory(EntityPlayer player){
	}

	@Override
	public void closeInventory(EntityPlayer player){
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack){
		return ObeliskFuelHelper.instance.getFuelBurnTime(itemstack) > 0;
	}

	@Override
	public boolean canRelayPower(PowerTypes type){
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
	public void markDirty() {
		markForUpdate();
		super.markDirty();
	}

	private boolean dirty = false;

	@Override
	public void markForUpdate() {
		this.dirty = true;
	}

	@Override
	public boolean needsUpdate() {
		return this.dirty;
	}

	@Override
	public void clean() {
		this.dirty = false;
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
}
