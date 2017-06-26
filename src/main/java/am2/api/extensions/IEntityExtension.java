package am2.api.extensions;

import java.util.concurrent.Callable;

import am2.extensions.EntityExtension;
import am2.spell.ContingencyType;
import am2.utils.NBTUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public interface IEntityExtension {

	public boolean hasEnoughtMana(float f);
		
	public void setContingency (ContingencyType type, ItemStack stack);
	
	public ContingencyType getContingencyType();
	
	public ItemStack getContingencyStack();
	
	public double getMarkX();
	
	public double getMarkY();
	
	public double getMarkZ();
	
	public int getMarkDimensionID();
	
	public float getCurrentMana();
	
	public int getCurrentLevel();
	
	public float getCurrentBurnout();
	
	public int getCurrentSummons();
	
	public float getCurrentXP();
	
	public int getHealCooldown();
	
	public void lowerHealCooldown(int amount);
	
	public void placeHealOnCooldown();
	
	public void lowerAffinityHealCooldown (int amount);
	
	public int getAffinityHealCooldown();
	
	public void placeAffinityHealOnCooldown(boolean full);
	
	public float getMaxMana();
	
	public float getMaxXP ();
	
	public float getMaxBurnout ();
	
	public void setAffinityHealCooldown(int affinityHealCooldown);
	
	public void setCurrentBurnout(float currentBurnout);
	
	public void setCurrentLevel(int currentLevel);
	
	public void setCurrentMana(float currentMana);
	
	public void setCurrentSummons(int currentSummons);
	
	public void setCurrentXP(float currentXP);
	
	public void setHealCooldown(int healCooldown);
	
	public void setMarkX(double markX);
	
	public void setMarkY(double markY);
	
	public void setMarkZ(double markZ);
	
	public void setMarkDimensionID(int markDimensionID);
	
	public void setMark (double x, double y, double z, int dim);
	
	public void setShrunk(boolean shrunk);
	
	public boolean isShrunk();

	public void setInverted(boolean inverted);

	public void setFallProtection(float fallProtection);
	
	public boolean isInverted();
	
	public void addEntityReference(EntityLivingBase entity);
	
	public void init(EntityLivingBase entity);
	
	public boolean canHeal();
	
	public int getMaxSummons();
	
	public boolean shouldUpdate();
	public byte[] generateUpdatePacket();
	public void handleUpdatePacket(byte[] bytes);
	public void forceUpdate();
	
	public static class Storage implements IStorage<IEntityExtension> {
		
		@Override
		public NBTBase writeNBT(Capability<IEntityExtension> capability, IEntityExtension instance, EnumFacing side) {
			NBTTagCompound compound = new NBTTagCompound();
			NBTTagCompound am2tag = NBTUtils.getAM2Tag(compound);
			am2tag.setFloat("CurrentMana", instance.getCurrentMana());
			am2tag.setInteger("CurrentLevel", instance.getCurrentLevel());
			am2tag.setFloat("CurrentXP", instance.getCurrentXP());
			am2tag.setFloat("CurrentBurnout", instance.getCurrentBurnout());
			am2tag.setInteger("CurrentSummons", instance.getCurrentSummons());
			
			am2tag.setInteger("HealCooldown", instance.getHealCooldown());
			am2tag.setInteger("AffinityHealCooldown", instance.getAffinityHealCooldown());
			
			am2tag.setBoolean("Shrunk", instance.isShrunk());
			am2tag.setBoolean("Inverted", instance.isInverted());
			am2tag.setFloat("FallProtection", instance.getFallProtection());
			
			am2tag.setDouble("MarkX", instance.getMarkX());
			am2tag.setDouble("MarkY", instance.getMarkY());
			am2tag.setDouble("MarkZ", instance.getMarkZ());
			am2tag.setInteger("MarkDimensionId", instance.getMarkDimensionID());
			am2tag.setFloat("TK_Distance", instance.getTKDistance());
			am2tag.setFloat("ManaShielding", instance.getManaShielding());
			NBTTagCompound contingencyTag = NBTUtils.addTag(am2tag, "Contingency");
			if (instance.getContingencyType() != ContingencyType.NULL) {
				contingencyTag.setString("Type", instance.getContingencyType().name().toLowerCase());
				contingencyTag.setTag("Stack", instance.getContingencyStack().writeToNBT(new NBTTagCompound()));
			} else {
				contingencyTag.setString("Type", "null");			
			}
			return compound;
		}
	
		@Override
		public void readNBT(Capability<IEntityExtension> capability, IEntityExtension instance, EnumFacing side, NBTBase nbt) {
			NBTTagCompound am2tag = NBTUtils.getAM2Tag((NBTTagCompound)nbt);
			instance.setCurrentMana(am2tag.getFloat("CurrentMana"));
			instance.setCurrentLevel(am2tag.getInteger("CurrentLevel"));
			instance.setCurrentXP(am2tag.getFloat("CurrentXP"));
			instance.setCurrentBurnout(am2tag.getFloat("CurrentBurnout"));
			instance.setCurrentSummons(am2tag.getInteger("CurrentSummons"));
			
			instance.setHealCooldown(am2tag.getInteger("HealCooldown"));
			instance.setAffinityHealCooldown(am2tag.getInteger("AffinityHealCooldown"));
			
			instance.setShrunk(am2tag.getBoolean("Shrunk"));
			instance.setInverted(am2tag.getBoolean("Inverted"));
			instance.setFallProtection(am2tag.getFloat("FallProtection"));
			
			instance.setMarkX(am2tag.getDouble("MarkX"));
			instance.setMarkY(am2tag.getDouble("MarkY"));
			instance.setMarkZ(am2tag.getDouble("MarkZ"));
			instance.setMarkDimensionID(am2tag.getInteger("MarkDimensionId"));
			
			instance.setTKDistance(am2tag.getFloat("TK_Distance"));
			instance.setManaShielding(am2tag.getFloat("ManaShielding"));
			
			NBTTagCompound contingencyTag = NBTUtils.addTag(am2tag, "Contingency");
			if (!contingencyTag.hasKey("Type") || !contingencyTag.getString("Type").equals("null")) {
				instance.setContingency(ContingencyType.fromName(contingencyTag.getString("Type")), ItemStack.loadItemStackFromNBT(contingencyTag.getCompoundTag("Stack")));
			} else {
				instance.setContingency(ContingencyType.NULL, null);
			}
		}
	}
	
	public static class Factory implements Callable<IEntityExtension> {

		@Override
		public IEntityExtension call() throws Exception {
			return new EntityExtension();
		}
		
	}

	public boolean addSummon(EntityCreature entityliving);

	public boolean getCanHaveMoreSummons();

	public void updateManaLink(EntityLivingBase caster);

	public void deductMana(float amt);

	public void spawnManaLinkParticles();

	public boolean removeSummon();

	public boolean isManaLinkedTo(EntityLivingBase entity);

	public void cleanupManaLinks();

	public float getBonusMaxMana();

	public float getBonusCurrentMana();

	public boolean shouldReverseInput();

	public boolean getIsFlipped();

	public float getFlipRotation();

	public float getPrevFlipRotation();

	public float getShrinkPct();

	public float getPrevShrinkPct();
	
	public void setTKDistance(float newDist);
	
	public void addToTKDistance(float toAdd);
	
	public float getTKDistance();

	public void syncTKDistance();

	public float getFallProtection();

	public void manaBurnoutTick();

	public boolean setMagicLevelWithMana(int level);

	public void addMagicXP(float xp);

	public void setDisableGravity(boolean b);

	boolean isGravityDisabled();

	public Entity getInanimateTarget();

	public void setInanimateTarget(Entity ent);

	public void setFlipRotation(float rot);

	public void setPrevFlipRotation(float rot);

	public float getManaShielding();
	
	public void setManaShielding(float manaShielding);
}
