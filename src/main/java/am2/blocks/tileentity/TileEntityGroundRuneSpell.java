package am2.blocks.tileentity;

import am2.entity.EntityDummyCaster;
import am2.extensions.EntityExtension;
import am2.utils.DummyEntityPlayer;
import am2.utils.SpellUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

public class TileEntityGroundRuneSpell extends TileEntity implements ITickable{
	private ItemStack spellStack = null;
	private EntityPlayer caster = null;
	private String placedByName = null;

	private int numTriggers = 1;
	private boolean isPermanent = false;

	public TileEntityGroundRuneSpell(){
		
	}

	public void setSpellStack(ItemStack spellStack){
		this.spellStack = spellStack.copy();
	}
	
	public ItemStack getSpellStack() {
		return spellStack;
	}

	public void setNumTriggers(int triggers){
		this.numTriggers = triggers;
	}

	public int getNumTriggers(){
		return this.numTriggers;
	}

	public void setPermanent(boolean permanent){
		this.isPermanent = permanent;
	}

	public boolean getPermanent(){
		return this.isPermanent;
	}

	private void prepForActivate(){
		if (placedByName != null)
			caster = world.getPlayerEntityByName(placedByName);
		if (caster == null){
			caster = DummyEntityPlayer.fromEntityLiving(new EntityDummyCaster(world));
			EntityExtension.For(caster).setMagicLevelWithMana(99);
		}
	}
	
	public boolean canApply(EntityLivingBase entity) {
		if (spellStack == null) return false;
		prepForActivate();
		if (entity.getName().equals(placedByName)) return false;
		return true;
	}

	public boolean applySpellEffect(EntityLivingBase target){
		if (spellStack == null) return false;
		if (!canApply(target)) return false;
		prepForActivate();
		SpellUtils.applyStackStage(spellStack, caster, target, target.posX, target.posY, target.posZ, null, world, false, false, 0);
		return true;
	}

	public void setPlacedBy(EntityLivingBase caster){
		if (caster instanceof EntityPlayer) this.placedByName = ((EntityPlayer)caster).getName();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound){
		if (placedByName != null)
			compound.setString("placedByName", placedByName);
		if (spellStack != null)
			compound.setTag("spellStack", spellStack.writeToNBT(new NBTTagCompound()));
		compound.setInteger("numTrigger", numTriggers);
		compound.setBoolean("permanent", isPermanent);
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound){
		if (compound.hasKey("placedByName"))
			placedByName = compound.getString("placedByName");
		if (compound.hasKey("spellStack"))
			spellStack = new ItemStack(compound.getCompoundTag("spellStack"));
		numTriggers = compound.getInteger("numTrigger");
		isPermanent = compound.getBoolean("permanent");
	}
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, 0, writeToNBT(new NBTTagCompound()));
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
	}

	@Override
	public void update() {
		world.markAndNotifyBlock(pos, world.getChunkFromBlockCoords(pos), world.getBlockState(pos), world.getBlockState(pos), 2);
	}
}
