package am2.blocks.tileentity;

import java.util.ArrayList;

import am2.api.blocks.IKeystoneLockable;
import am2.api.spell.SpellComponent;
import am2.defs.BlockDefs;
import am2.utils.SpellUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class TileEntitySpellSealedDoor extends TileEntity implements ITickable, IInventory, IKeystoneLockable<TileEntitySpellSealedDoor>{

	private NonNullList<ItemStack> inventory = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);

	private int lastAppliedTime = -1;
	private int closeTime = -1;
	private int curTime = 0;
	private int opentime = 40;

	private ArrayList<SpellComponent> appliedParts;
	private ArrayList<SpellComponent> key;

	public TileEntitySpellSealedDoor(){
		inventory = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);
		appliedParts = new ArrayList<SpellComponent>();
		key = new ArrayList<SpellComponent>();
	}

	@Override
	public ItemStack[] getRunesInKey(){
		ItemStack[] runes = new ItemStack[3];
		runes[0] = inventory.get(0);
		runes[1] = inventory.get(1);
		runes[2] = inventory.get(2);
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
	public int getSizeInventory(){
		return 4;
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
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
		return false;
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
		if (itemstack != null && itemstack.getCount() > getInventoryStackLimit()){
			itemstack.setCount(getInventoryStackLimit());
		}
	}

	@Override
	public String getName(){
		return "Spell Sealed Door";
	}

	@Override
	public int getInventoryStackLimit(){
		return 1;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer entityplayer){
		if (world.getTileEntity(pos) != this){
			return false;
		}
		return entityplayer.getDistanceSqToCenter(pos) <= 64D;
	}

	@Override
	public boolean hasCustomName(){
		return false;
	}

	@Override
	public void openInventory(EntityPlayer player){
	}

	@Override
	public void closeInventory(EntityPlayer player){
		analyzeSpellForKey();
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack){
		return false;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound){
		super.readFromNBT(nbttagcompound);
		NBTTagList nbttaglist = nbttagcompound.getTagList("SpellSealedDoorInventory", Constants.NBT.TAG_COMPOUND);
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

		nbttagcompound.setTag("SpellSealedDoorInventory", nbttaglist);
		return nbttagcompound;
	}

	@Override
	public void update(){

		if (!world.isRemote){
			curTime++;

			if (closeTime == -1 && lastAppliedTime != -1){
				if (curTime > lastAppliedTime + 10){
					clearAppliedParts();
					return;
				}
				if (checkKey()){
					clearAppliedParts();
					setOpenState(true);
					this.closeTime = curTime + opentime;
				}
			}

			if (closeTime != -1 && curTime > closeTime){
				clearAppliedParts();
				setOpenState(false);
				closeTime = -1;
			}
		}
	}

	private void setOpenState(boolean open){
		BlockDefs.spellSealedDoor.toggleDoor(world, pos, open);
	}

	public void addPartToCurrentKey(SpellComponent component){
		this.appliedParts.add(component);
		this.lastAppliedTime = curTime;
	}

	private boolean checkKey(){
		if (key.size() != appliedParts.size()) return false;
		if (key.equals(appliedParts)) return true;
		return false;
	}

	private void clearAppliedParts(){
		appliedParts.clear();
		lastAppliedTime = -1;
	}

	public void analyzeSpellForKey(){
		ItemStack spell = this.inventory.get(3);

		if (spell.isEmpty()) return;

		//if we're here, we have a spell to analyze!
		key.clear();
		int stages = SpellUtils.numStages(spell);

		for (int i = 0; i < stages; ++i){
			ArrayList<SpellComponent> components = SpellUtils.getComponentsForStage(spell, i);
			for (SpellComponent comp : components){
				key.add(comp);
			}
		}

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
}
