package am2.buffs;

import am2.defs.PotionEffectsDefs;
import am2.extensions.EntityExtension;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;

public class BuffEffectTemporalAnchor extends BuffEffect{

	private double x;
	private double y;
	private double z;
	private float rotationPitch;
	private float rotationYaw;
	private float rotationYawHead;

	private float mana;
	private float health;

	public BuffEffectTemporalAnchor(int duration, int amplifier){
		super(PotionEffectsDefs.temporalAnchor, duration, amplifier);
	}

	@Override
	public void applyEffect(EntityLivingBase entityliving){
		//store values from the entity
		x = entityliving.posX;
		y = entityliving.posY;
		z = entityliving.posZ;
		rotationPitch = entityliving.rotationPitch;
		rotationYaw = entityliving.rotationYaw;
		rotationYawHead = entityliving.rotationYawHead;
				
		health = entityliving.getHealth();
		mana = entityliving.getCapability(EntityExtension.INSTANCE, null).getCurrentMana();
	}

	@Override
	public void stopEffect(EntityLivingBase entityliving){
		entityliving.setPositionAndUpdate(x, y, z);
		entityliving.setRotationYawHead(rotationYaw);
		entityliving.rotationYawHead = rotationYawHead;
		entityliving.getCapability(EntityExtension.INSTANCE, null).setCurrentMana(mana);

		entityliving.setHealth(health);
		entityliving.fallDistance = 0;
	}

	@Override
	protected String spellBuffName(){
		return "Temporal Anchor";
	}
	
	@Override
	public void writeCustomNBT(NBTTagCompound nbt) {
		nbt.setDouble("X", x);
		nbt.setDouble("Y", y);
		nbt.setDouble("Z", z);
		nbt.setFloat("RotationPitch", rotationPitch);
		nbt.setFloat("RotationYaw", rotationYaw);
		nbt.setFloat("RotationYawHead", rotationYawHead);
		nbt.setFloat("Mana", mana);
		nbt.setFloat("Health", health);
	}
	
	@Override
	public void readCustomNBT(NBTTagCompound nbt) {
		x = nbt.getDouble("X");
		y = nbt.getDouble("Y");
		z = nbt.getDouble("Z");
		rotationPitch = nbt.getFloat("RotationPitch");
		rotationYaw = nbt.getFloat("RotationYaw");
		rotationYawHead = nbt.getFloat("RotationYawHead");
		health = nbt.getFloat("Health");
		mana = nbt.getInteger("Mana");
	}
	
	
}
