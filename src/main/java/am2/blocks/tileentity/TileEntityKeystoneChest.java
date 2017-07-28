package am2.blocks.tileentity;

import java.util.Random;

import am2.api.blocks.IKeystoneLockable;
import am2.blocks.BlockKeystoneChest;
import am2.container.ContainerKeystoneChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.common.util.Constants;

public class TileEntityKeystoneChest extends TileEntityLockableLoot implements IInventory, ITickable, IKeystoneLockable<TileEntityKeystoneChest>{

	private NonNullList<ItemStack> inventory = NonNullList.<ItemStack>withSize(getSizeInventory(), ItemStack.EMPTY);
	public static final int keystoneSlot = 27;
	private float prevLidAngle = 0f;
	private float lidAngle = 0f;
	private int numPlayersUsing = 0;

	private static final float lidIncrement = 0.1f;

	public TileEntityKeystoneChest(){
		inventory = NonNullList.<ItemStack>withSize(getSizeInventory(), ItemStack.EMPTY);
	}

	@Override
	public int getSizeInventory(){
		return 30;
	}

	@Override
	public void update(){
		setPrevLidAngle(getLidAngle());
		if (numPlayersUsing > 0){
			if (getLidAngle() == 0){
				this.world.playSound(this.getPos().getX() + 0.5D, this.getPos().getY() + 0.5D, this.getPos().getZ() + 0.5D, SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.9F, true);
			}
			if (getLidAngle() < 1.0f){
				setLidAngle(getLidAngle() + lidIncrement);
			}else{
				setLidAngle(1.0f);
			}
		}else{
			if (getLidAngle() == 1.0f){
				this.world.playSound(this.getPos().getX() + 0.5D, this.getPos().getY() + 0.5D, this.getPos().getZ(), SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.9F, true);
			}
			if (getLidAngle() - lidIncrement > 0f){
				setLidAngle(getLidAngle() - lidIncrement);
			}else{
				setLidAngle(0f);
			}
		}
		world.markAndNotifyBlock(pos, world.getChunkFromBlockCoords(pos), world.getBlockState(pos), world.getBlockState(pos), 3);
	}

	@Override
	public boolean receiveClientEvent(int par1, int par2){
		if (par1 == 1){
			this.numPlayersUsing = par2;
			return true;
		}else{
			return super.receiveClientEvent(par1, par2);
		}
	}

	@Override
	public void openInventory(EntityPlayer player){
		if (this.numPlayersUsing < 0){
			this.numPlayersUsing = 0;
		}

		++this.numPlayersUsing;
		this.world.addBlockEvent(pos, getBlockType(), 1, this.numPlayersUsing);
	}

	@Override
	public void closeInventory(EntityPlayer player){
		if (this.getBlockType() != null && this.getBlockType() instanceof BlockKeystoneChest){
			--this.numPlayersUsing;
			this.world.addBlockEvent(pos, getBlockType(), 1, this.numPlayersUsing);
		}
	}

	@Override
	public ItemStack getStackInSlot(int slot){
		if (slot >= inventory.size())
			return ItemStack.EMPTY;
		return inventory.get(slot);
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
		return "KeystoneChestInventory";
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

		return entityplayer.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64D;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound){
		super.readFromNBT(nbttagcompound);
		NBTTagList nbttaglist = nbttagcompound.getTagList("KeystoneChestInventory", Constants.NBT.TAG_COMPOUND);
		inventory = NonNullList.<ItemStack>withSize(getSizeInventory(), ItemStack.EMPTY);;
		for (int i = 0; i < nbttaglist.tagCount(); i++){
			String tag = String.format("ArrayIndex", i);
			NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.getCompoundTagAt(i);
			byte byte0 = nbttagcompound1.getByte(tag);
			if (byte0 >= 0 && byte0 < inventory.size()){
				inventory.set(byte0, new ItemStack(nbttagcompound1));
			}
		}
		checkLootAndRead(nbttagcompound);
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
				inventory.get(i).writeToNBT(nbttagcompound1);
				nbttaglist.appendTag(nbttagcompound1);
			}
		}
		checkLootAndWrite(nbttagcompound);
		nbttagcompound.setTag("KeystoneChestInventory", nbttaglist);
		return nbttagcompound;
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
	}
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, getBlockMetadata(), writeToNBT(new NBTTagCompound()));
	}

	@Override
	public boolean hasCustomName(){
		return false;
	}

	public float getPrevLidAngle(){
		return prevLidAngle;
	}

	public void setPrevLidAngle(float prevLidAngle){
		this.prevLidAngle = prevLidAngle;
	}

	public float getLidAngle(){
		return lidAngle;
	}

	public void setLidAngle(float lidAngle){
		this.lidAngle = lidAngle;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack){
		return false;
	}

	@Override
	public ItemStack[] getRunesInKey(){
		ItemStack[] runes = new ItemStack[3];
		runes[0] = inventory.get(27);
		runes[1] = inventory.get(28);
		runes[2] = inventory.get(29);
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
	public void setField(int id, int value) {}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {}

	@Override
	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
		fillWithLoot(playerIn);
		return new ContainerKeystoneChest(playerInventory, this);
	}
	
	@Override
	public void fillWithLoot(EntityPlayer player) {
		if (this.lootTable != null) {
			LootTable loottable = this.world.getLootTableManager().getLootTableFromLocation(this.lootTable);
			this.lootTable = null;
			Random random;

			if (this.lootTableSeed == 0L) {
				random = new Random();
			} else {
				random = new Random(this.lootTableSeed);
			}

			LootContext.Builder lootcontext$builder = new LootContext.Builder((WorldServer) this.world);

			if (player != null) {
				lootcontext$builder.withLuck(player.getLuck());
			}
			InventoryBasic inv = new InventoryBasic("Wrapper", false, 27);
			for (int i = 0; i < 27; i++) {
				inv.setInventorySlotContents(i, getStackInSlot(i));
			}
			loottable.fillInventory(inv, random, lootcontext$builder.build());
			for (int i = 0; i < 27; i++) {
				this.setInventorySlotContents(i, inv.getStackInSlot(i));
			}
		}
	}
	
	@Override
	public String getGuiID() {
		return "arsmgica2:keystone_chest";
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
	protected NonNullList<ItemStack> getItems() {
		NonNullList<ItemStack> items = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);
		for (ItemStack itemstack : this.inventory){
			items.add(itemstack);
		}
		return items;
	}
}
