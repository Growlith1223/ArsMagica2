package am2.extensions;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.common.base.Optional;

import am2.ArsMagica2;
import am2.api.ArsMagicaAPI;
import am2.api.event.PlayerMagicLevelChangeEvent;
import am2.api.extensions.IEntityExtension;
import am2.api.math.AMVector2;
import am2.armor.ArmorHelper;
import am2.armor.ArsMagicaArmorMaterial;
import am2.armor.infusions.GenericImbuement;
import am2.armor.infusions.ImbuementRegistry;
import am2.bosses.EntityLifeGuardian;
import am2.defs.ItemDefs;
import am2.defs.PotionEffectsDefs;
import am2.defs.SkillDefs;
import am2.packet.AMDataReader;
import am2.packet.AMDataWriter;
import am2.packet.AMNetHandler;
import am2.packet.AMPacketIDs;
import am2.particles.AMLineArc;
import am2.spell.ContingencyType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class EntityExtension implements IEntityExtension, ICapabilityProvider, ICapabilitySerializable<NBTBase> {

	public static final ResourceLocation ID = new ResourceLocation("arsmagica2:ExtendedProp");
	
	private static final int SYNC_CONTINGENCY = 0x1;
	private static final int SYNC_MARK = 0x2;
	private static final int SYNC_MANA = 0x4;
	private static final int SYNC_FATIGUE = 0x8;
	private static final int SYNC_LEVEL = 0x10;
	private static final int SYNC_XP = 0x20;
	private static final int SYNC_SUMMONS = 0x40;
	private static final int SYNC_FALL_PROTECTION = 0x80;
	private static final int SYNC_FLIP_ROTATION = 0x100;
	private static final int SYNC_INVERSION_STATE = 0x200;
	private static final int SYNC_SHRINK_STATE = 0x400;
	private static final int SYNC_TK_DISTANCE = 0x800;
	private static final int SYNC_MANA_SHIELD = 0x1000;
	private static final int SYNC_SHRINK_PERCENTAGE = 0x2000;
	private static final int SYNC_HEAL_COOLDOWN = 0x4000;
	private static final int SYNC_AFFINITY_HEAL_COOLDOWN = 0x8000;
	private static final int SYNC_DISABLE_GRAVITY = 0x10000;
	
	private static int baseTicksForFullRegen = 2400;
	private int ticksForFullRegen = baseTicksForFullRegen;
	public boolean isRecoveringKeystone;
	
	private Entity ent;
	
	@CapabilityInject(value = IEntityExtension.class)
	public static Capability<IEntityExtension> INSTANCE = null;
	
	private ArrayList<Integer> summon_ent_ids = new ArrayList<Integer>();
	private EntityLivingBase entity;

	private ArrayList<ManaLinkEntry> manaLinks = new ArrayList<>();
	public AMVector2 originalSize;
	public float shrinkAmount;
	public boolean astralBarrierBlocked = false;
	public float bankedInfusionHelm = 0f;
	public float bankedInfusionChest = 0f;
	public float bankedInfusionLegs = 0f;
	public float bankedInfusionBoots = 0f;
	
	private int syncCode = 0;
	
	private ContingencyType contingencyType = ContingencyType.NULL;
	private Optional<ItemStack> contingencyStack = Optional.absent();
	private double markX;
	private double markY;
	private double markZ;
	private int markDimension = -512;
	
	private float currentMana;
	private float currentFatigue;
	private float currentXP;
	private int currentLevel;
	private int currentSummons;
	private int healCooldown;
	private int affHealCooldown;
	private boolean isShrunk;
	private boolean isInverted;
	private float fallProtection;
	private float flipRotation;
	private float prevFlipRotation;
	private float shrinkPercentage;
	private float prevShrinkPercentage;
	private float TKDistance;
	private boolean disableGravity;
	private float manaShield;
	
	public ArrayList<ItemStack> runningStacks = new ArrayList<>();
	
	
	private void addSyncCode(int code) {
		this.syncCode |= code;
	}
	
	@Override
	public boolean hasEnoughtMana(float cost) {
		if (getCurrentMana() + getBonusCurrentMana() < cost)
			return false;
		return true;
	}
	
	@Override
	public void setContingency (ContingencyType type, ItemStack stack) {
		if (this.contingencyType != type || this.contingencyStack.orNull() != stack) {
			addSyncCode(SYNC_CONTINGENCY);
			this.contingencyType = type;
			this.contingencyStack = Optional.fromNullable(stack);
		}
	}
	
	@Override
	public ContingencyType getContingencyType() {
		return contingencyType;
	}
	
	@Override
	public ItemStack getContingencyStack() {
		return contingencyStack.orNull();
	}
	
	@Override
	public double getMarkX() {
		return markX;
	}
	
	@Override
	public double getMarkY() {
		return markY;
	}
	
	@Override
	public double getMarkZ() {
		return markZ;
	}
	
	@Override
	public int getMarkDimensionID() {
		return markDimension;
	}
	
	@Override
	public float getCurrentMana() {
		return currentMana;
	}
	
	@Override
	public int getCurrentLevel() {
		return currentLevel;
	}
	
	@Override
	public float getCurrentBurnout() {
		return currentFatigue;
	}
	
	@Override
	public int getCurrentSummons() {
		return currentSummons;
	}
	
	@Override
	public float getCurrentXP() {
		return currentXP;
	}
	
	@Override
	public int getHealCooldown() {
		return healCooldown;
	}
	
	@Override
	public void lowerHealCooldown(int amount) {
		setHealCooldown(Math.max(0, getHealCooldown() - amount));
	}
	
	@Override
	public void placeHealOnCooldown() {
		setHealCooldown(40);
	}
	
	@Override
	public void lowerAffinityHealCooldown (int amount) {
		setAffinityHealCooldown(Math.max(0, getAffinityHealCooldown() - amount));
	}
	
	@Override
	public int getAffinityHealCooldown() {
		return affHealCooldown;
	}
	
	@Override
	public void placeAffinityHealOnCooldown(boolean full) {
		setAffinityHealCooldown(full ? 40 : 20);
	}
	
	@Override
	public float getMaxMana() {
		float mana = (float)(Math.pow(getCurrentLevel(), 1.5f) * (85f * ((float)getCurrentLevel() / 100f)) + 500f);
		if (this.entity.isPotionActive(PotionEffectsDefs.manaBoost))
			mana *= 1 + (0.25 * (this.entity.getActivePotionEffect(PotionEffectsDefs.manaBoost).getAmplifier() + 1));
		return (float)(mana + this.entity.getAttributeMap().getAttributeInstance(ArsMagicaAPI.maxManaBonus).getAttributeValue());
	}
	
	@Override
	public float getMaxXP () {
		return (float)Math.pow(getCurrentLevel() * 0.25f, 1.5f);
	}
	
	@Override
	public float getMaxBurnout () {
		return getCurrentLevel() * 10 + 1;
	}
	
	@Override
	public void setAffinityHealCooldown(int affinityHealCooldown) {
		if (affinityHealCooldown != affHealCooldown) {
			addSyncCode(SYNC_AFFINITY_HEAL_COOLDOWN);
			this.affHealCooldown = affinityHealCooldown;
		}
	}
	
	@Override
	public void setCurrentBurnout(float currentBurnout) {
		if (this.currentFatigue != currentBurnout) {
			addSyncCode(SYNC_FATIGUE);
			this.currentFatigue = currentBurnout;
		}
	}
	
	@Override
	public void setCurrentLevel(int currentLevel) {
		if (currentLevel != this.currentLevel) {
			addSyncCode(SYNC_LEVEL);
			ticksForFullRegen = (int)Math.round(baseTicksForFullRegen * (0.75 - (0.25 * (getCurrentLevel() / 99f))));
			if (entity instanceof EntityPlayer)
				MinecraftForge.EVENT_BUS.post(new PlayerMagicLevelChangeEvent((EntityPlayer) entity, currentLevel));
			this.currentLevel = currentLevel;
		}
	}
	
	@Override
	public void setCurrentMana(float currentMana) {
		if (this.currentMana != currentMana) {
			addSyncCode(SYNC_MANA);
			this.currentMana = currentMana;
		}
	}
	
	@Override
	public void setCurrentSummons(int currentSummons) {
		if (this.currentSummons != currentSummons) {
			addSyncCode(SYNC_SUMMONS);
			this.currentSummons = currentSummons;
		}
	}
	
	@Override
	public void setCurrentXP(float currentXP) {
		if (this.currentXP != currentXP) {
			while (currentXP >= this.getMaxXP()) {
				currentXP -= this.getMaxXP();
				setMagicLevelWithMana(getCurrentLevel() + 1);
			}
			addSyncCode(SYNC_XP);
			this.currentXP = currentXP;
		}
	}
	
	@Override
	public void setHealCooldown(int healCooldown) {
		if (this.healCooldown != healCooldown) {
			addSyncCode(SYNC_HEAL_COOLDOWN);
			this.healCooldown = healCooldown;
		}
	}
	
	@Override
	public void setMarkX(double markX) {
		if (this.markX != markX) {
			addSyncCode(SYNC_MARK);
			this.markX = markX;
		}
	}
	
	@Override
	public void setMarkY(double markY) {
		if (this.markY != markY) {
			addSyncCode(SYNC_MARK);
			this.markY = markY;
		}
	}
	
	@Override
	public void setMarkZ(double markZ) {
		if (this.markZ != markZ) {
			addSyncCode(SYNC_MARK);
			this.markZ = markZ;
		}
	}
	
	@Override
	public void setMarkDimensionID(int markDimensionID) {
		if (this.markDimension != markDimensionID) {
			addSyncCode(SYNC_MARK);
			this.markDimension = markDimensionID;
		}
	}
	
	@Override
	public void setMark (double x, double y, double z, int dim) {
		setMarkX(x);
		setMarkY(y);
		setMarkZ(z);
		setMarkDimensionID(dim);
	}
	
	@Override
	public boolean isShrunk() {
		return isShrunk;
	}
	
	@Override
	public void setShrunk(boolean shrunk) {
		if (isShrunk != shrunk) {
			addSyncCode(SYNC_SHRINK_STATE);
			isShrunk = shrunk;
		}
	}
	
	@Override
	public void setInverted(boolean isInverted) {
		if (this.isInverted != isInverted) {
			addSyncCode(SYNC_INVERSION_STATE);
			this.isInverted = isInverted;
		}
	}
	
	@Override
	public void setFallProtection(float fallProtection) {
		if (this.fallProtection != fallProtection) {
			addSyncCode(SYNC_FALL_PROTECTION);
			this.fallProtection = fallProtection;
		}
	}
	
	@Override
	public boolean isInverted() {
		return isInverted;
	}
	
	@Override
	public float getFallProtection() {
		return fallProtection;
	}
	
	@Override
	public void addEntityReference(EntityLivingBase entity) {
		this.entity = entity;
		setOriginalSize(new AMVector2(entity.width, entity.height));
	}
	
	public void setOriginalSize(AMVector2 amVector2) {
		this.originalSize = amVector2;
	}
	
	public AMVector2 getOriginalSize(){
		return this.originalSize;
	}

	@Override
	public void init(EntityLivingBase entity) {
		this.addEntityReference(entity);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == INSTANCE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == INSTANCE)
			return (T) this;
		return null;
	}
	
	public static EntityExtension For(EntityLivingBase thePlayer) {
		return (EntityExtension) thePlayer.getCapability(INSTANCE, null);
	}
	
	@Override
	public NBTBase serializeNBT() {
		return new IEntityExtension.Storage().writeNBT(INSTANCE, this, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt) {
		new IEntityExtension.Storage().readNBT(INSTANCE, this, null, nbt);
	}

	@Override
	public boolean canHeal() {
		return getHealCooldown() <= 0;
	}

	@Override
	public int getMaxSummons() {
		int maxSummons = 1;
		if (entity instanceof EntityPlayer && SkillData.For((EntityPlayer)entity).hasSkill(SkillDefs.EXTRA_SUMMONS.getID()));
			maxSummons++;
		return maxSummons;
	}

	@Override
	public boolean addSummon(EntityCreature entityliving) {
		if (!entity.worldObj.isRemote){
			summon_ent_ids.add(entityliving.getEntityId());
			setCurrentSummons(getCurrentSummons() + 1);
		}
		return true;
	}

	@Override
	public boolean getCanHaveMoreSummons() {
		if (entity instanceof EntityLifeGuardian)
			return true;
		
		verifySummons();
		return this.getCurrentSummons() < getMaxSummons();
	}
	
	private void verifySummons(){
		for (int i = 0; i < summon_ent_ids.size(); ++i){
			int id = summon_ent_ids.get(i);
			Entity e = entity.worldObj.getEntityByID(id);
			if (e == null || !(e instanceof EntityLivingBase)){
				summon_ent_ids.remove(i);
				i--;
				removeSummon();
			}
		}
	}
	
	@Override
	public boolean removeSummon(){
		if (getCurrentSummons() == 0){
			return false;
		}
		if (!entity.worldObj.isRemote){
			setCurrentSummons(getCurrentSummons() - 1);
		}
		return true;
	}
	
	@Override
	public void updateManaLink(EntityLivingBase entity){
		ManaLinkEntry mle = new ManaLinkEntry(entity.getEntityId(), 20);
		if (!this.manaLinks.contains(mle))
			this.manaLinks.add(mle);
		else
			this.manaLinks.remove(mle);
		if (!this.entity.worldObj.isRemote)
			AMNetHandler.INSTANCE.sendPacketToAllClientsNear(entity.dimension, entity.posX, entity.posY, entity.posZ, 32, AMPacketIDs.MANA_LINK_UPDATE, getManaLinkUpdate());

	}
	
	@Override
	public void deductMana(float manaCost){
		float leftOver = manaCost - getCurrentMana();
		this.setCurrentMana(getCurrentMana() - manaCost);
		if (leftOver > 0){
			for (ManaLinkEntry entry : this.manaLinks){
				leftOver -= entry.deductMana(entity.worldObj, entity, leftOver);
				if (leftOver <= 0)
					break;
			}
		}
	}
	
	@Override
	public void cleanupManaLinks(){
		Iterator<ManaLinkEntry> it = this.manaLinks.iterator();
		while (it.hasNext()){
			ManaLinkEntry entry = it.next();
			Entity e = this.entity.worldObj.getEntityByID(entry.entityID);
			if (e == null)
				it.remove();
		}
	}
	
	@Override
	public float getBonusCurrentMana(){
		float bonus = 0;
		for (ManaLinkEntry entry : this.manaLinks){
			bonus += entry.getAdditionalCurrentMana(entity.worldObj, entity);
		}
		return bonus;
	}

	@Override
	public float getBonusMaxMana(){
		float bonus = 0;
		for (ManaLinkEntry entry : this.manaLinks){
			bonus += entry.getAdditionalMaxMana(entity.worldObj, entity);
		}
		return bonus;
	}
	
	@Override
	public boolean isManaLinkedTo(EntityLivingBase entity){
		for (ManaLinkEntry entry : manaLinks){
			if (entry.entityID == entity.getEntityId())
				return true;
		}
		return false;
	}
	
	@Override
	public void spawnManaLinkParticles(){
		if (entity.worldObj != null && entity.worldObj.isRemote){
			for (ManaLinkEntry entry : this.manaLinks){
				Entity e = entity.worldObj.getEntityByID(entry.entityID);
				if (e != null && e.getDistanceSqToEntity(entity) < entry.range && e.ticksExisted % 90 == 0){
					AMLineArc arc = (AMLineArc)ArsMagica2.proxy.particleManager.spawn(entity.worldObj, "textures/blocks/oreblockbluetopaz.png", e, entity);
					if (arc != null){
						arc.setIgnoreAge(false);
						arc.setRBGColorF(0.17f, 0.88f, 0.88f);
					}
				}
			}
		}
	}
	
	private class ManaLinkEntry{
		private final int entityID;
		private final int range;

		public ManaLinkEntry(int entityID, int range){
			this.entityID = entityID;
			this.range = range * range;
		}

		private EntityLivingBase getEntity(World world){
			Entity e = world.getEntityByID(entityID);
			if (e == null || !(e instanceof EntityLivingBase))
				return null;
			return (EntityLivingBase)e;
		}

		public float getAdditionalCurrentMana(World world, Entity host){
			EntityLivingBase e = getEntity(world);
			if (e == null || e.getDistanceSqToEntity(host) > range)
				return 0;
			return For(e).getCurrentMana();
		}

		public float getAdditionalMaxMana(World world, Entity host){
			EntityLivingBase e = getEntity(world);
			if (e == null || e.getDistanceSqToEntity(host) > range)
				return 0;
			return For(e).getMaxMana();
		}

		public float deductMana(World world, Entity host, float amt){
			EntityLivingBase e = getEntity(world);
			if (e == null || e.getDistanceSqToEntity(host) > range)
				return 0;
			amt = Math.min(For(e).getCurrentMana(), amt);
			For(e).deductMana(amt);
			return amt;
		}

		@Override
		public int hashCode(){
			return entityID;
		}

		@Override
		public boolean equals(Object obj){
			if (obj instanceof ManaLinkEntry)
				return ((ManaLinkEntry)obj).entityID == this.entityID;
			return false;
		}
	}

	@Override
	public boolean shouldReverseInput() {
		return getFlipRotation() > 0 || this.entity.isPotionActive(PotionEffectsDefs.scrambleSynapses);
	}

	@Override
	public boolean getIsFlipped() {
		return isInverted();
	}

	@Override
	public float getFlipRotation() {
		return flipRotation;
	}

	@Override
	public float getPrevFlipRotation() {
		return prevFlipRotation;
	}
	
	@Override
	public void setFlipRotation(float rot) {
		if (this.flipRotation != rot) {
			addSyncCode(SYNC_FLIP_ROTATION);
			this.flipRotation = rot;
		}
	}

	@Override
	public void setPrevFlipRotation(float rot) {
		if (this.prevFlipRotation != rot) {
			addSyncCode(SYNC_FLIP_ROTATION);
			this.prevFlipRotation = rot;
		}
	}

	@Override
	public float getShrinkPct() {
		return shrinkPercentage;
	}

	@Override
	public float getPrevShrinkPct() {
		return prevShrinkPercentage;
	}

	@Override
	public void setTKDistance(float TK_Distance) {
		if (this.TKDistance != TK_Distance) {
			addSyncCode(SYNC_TK_DISTANCE);
			this.TKDistance = TK_Distance;
		}
	}

	@Override
	public void addToTKDistance(float toAdd) {
		setTKDistance(getTKDistance() + toAdd);
	}

	@Override
	public float getTKDistance() {
		return TKDistance;
	}
	
	@Override
	public void syncTKDistance() {
		AMDataWriter writer = new AMDataWriter();
		writer.add(this.getTKDistance());
		AMNetHandler.INSTANCE.sendPacketToServer(AMPacketIDs.TK_DISTANCE_SYNC, writer.generate());
	}
	
	@Override
	public void manaBurnoutTick(){
		if (isGravityDisabled()){
			this.entity.motionY = 0;
		}
		float actualMaxMana = getMaxMana();
		if (getCurrentMana() < actualMaxMana) {
			if (entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isCreativeMode) {
				setCurrentMana(actualMaxMana);
			} else {
				if (getCurrentMana() < 0) {
					setCurrentMana(0);
				}

				int regenTicks = (int) Math.ceil(ticksForFullRegen * entity.getAttributeMap()
						.getAttributeInstance(ArsMagicaAPI.manaRegenTimeModifier).getAttributeValue());

				if (entity.isPotionActive(PotionEffectsDefs.manaRegen)) {
					PotionEffect pe = entity.getActivePotionEffect(PotionEffectsDefs.manaRegen);
					regenTicks *= Math.max(0.01, 1.0f - ((pe.getAmplifier() + 1) * 0.25f));
				}

				if (entity instanceof EntityPlayer) {
					EntityPlayer player = (EntityPlayer) entity;
					int armorSet = ArmorHelper.getFullArsMagicaArmorSet(player);
					if (armorSet == ArsMagicaArmorMaterial.MAGE.getMaterialID()) {
						regenTicks *= 0.8;
					} else if (armorSet == ArsMagicaArmorMaterial.BATTLEMAGE.getMaterialID()) {
						regenTicks *= 0.95;
					} else if (armorSet == ArsMagicaArmorMaterial.ARCHMAGE.getMaterialID()) {
						regenTicks *= 0.5;
					}

					if (SkillData.For(player).hasSkill(SkillDefs.MANA_REGEN_3.getID())) {
						regenTicks *= 0.7f;
					} else if (SkillData.For(player).hasSkill(SkillDefs.MANA_REGEN_2.getID())) {
						regenTicks *= 0.85f;
					} else if (SkillData.For(player).hasSkill(SkillDefs.MANA_REGEN_1.getID())) {
						regenTicks *= 0.95f;
					}

					int numArmorPieces = 0;
					for (int i = 0; i < 4; ++i) {
						ItemStack stack = player.inventory.armorInventory[i];
						if (ImbuementRegistry.instance.isImbuementPresent(stack, GenericImbuement.manaRegen))
							numArmorPieces++;
					}
					regenTicks *= 1.0f - (0.15f * numArmorPieces);
				}

				float manaToAdd = (actualMaxMana / regenTicks);

				setCurrentMana(getCurrentMana() + manaToAdd);
				if (getCurrentMana() > getMaxMana())
					setCurrentMana(getMaxMana());
			}
		} else if (getCurrentMana() > getMaxMana()) {
			float overloadMana = getCurrentMana() - getMaxMana();
			overloadMana = getCurrentMana() - getMaxMana();
			float toRemove = Math.max(overloadMana * 0.002f, 1.0f);
			deductMana(toRemove);
			if (entity instanceof EntityPlayer && SkillData.For(entity).hasSkill(SkillDefs.SHIELD_OVERLOAD.getID())) {
				addMagicShieldingCapped(toRemove / 500F);
			}
		}
		if (getManaShielding() > getMaxMagicShielding()) {
			float overload = getManaShielding() - (getMaxMagicShielding());
			float toRemove = Math.max(overload * 0.002f, 1.0f);
			if (getManaShielding() - toRemove < getMaxMagicShielding())
				toRemove = overload;
			setManaShielding(getManaShielding() - toRemove);
		}
		
		if (getCurrentBurnout() > 0) {
			int numArmorPieces = 0;
			if (entity instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) entity;
				for (int i = 0; i < 4; ++i) {
					ItemStack stack = player.inventory.armorInventory[i];
					if (stack == null) continue;
					if (ImbuementRegistry.instance.isImbuementPresent(stack, GenericImbuement.burnoutReduction))
						numArmorPieces++;
				}
			}
			float factor = (float) ((0.01f + (0.015f * numArmorPieces)) * entity.getAttributeMap()
					.getAttributeInstance(ArsMagicaAPI.burnoutReductionRate).getAttributeValue());
			float decreaseamt = factor * getCurrentLevel();
			setCurrentBurnout(getCurrentBurnout() - decreaseamt);
			if (getCurrentBurnout() < 0) {
				setCurrentBurnout(0);
			}
		}
	}
	
	public byte[] getManaLinkUpdate(){
		AMDataWriter writer = new AMDataWriter();
		writer.add(this.entity.getEntityId());
		writer.add(this.manaLinks.size());
		for (ManaLinkEntry entry : this.manaLinks)
			writer.add(entry.entityID);
		return writer.generate();
	}
	
	public void handleManaLinkUpdate(AMDataReader rdr) {
		this.manaLinks.clear();
		int numLinks = rdr.getInt();
		for (int i = 0; i < numLinks; ++i){
			Entity e = entity.worldObj.getEntityByID(rdr.getInt());
			if (e != null && e instanceof EntityLivingBase)
				updateManaLink((EntityLivingBase)e);
		}
	}
	
	@Override
	public boolean setMagicLevelWithMana(int level){
		if (level < 0) level = 0;
		setCurrentLevel(level);
		setCurrentMana(getMaxMana());
		setCurrentBurnout(0);
		return true;
	}

	@Override
	public void addMagicXP(float xp) {
		this.setCurrentXP(this.getCurrentXP() + xp);
	}

	@Override
	public void setDisableGravity(boolean b) {
		if (this.disableGravity != b) {
			addSyncCode(SYNC_DISABLE_GRAVITY);
			this.disableGravity = b;
		}
	}
	
	@Override
	public boolean isGravityDisabled() {
		return disableGravity;
	}

	@Override
	public Entity getInanimateTarget() {
		return ent;
	}

	@Override
	public void setInanimateTarget(Entity ent) {
		this.ent = ent;
	}
	
	public void flipTick(){
		//this.setInverted(true);
		boolean flipped = getIsFlipped();

		ItemStack boots = ((EntityPlayer)entity).inventory.armorInventory[0];
		if (boots == null || boots.getItem() != ItemDefs.enderBoots)
			setInverted(false);

		setPrevFlipRotation(getFlipRotation());
		if (flipped && getFlipRotation() < 180)
			setFlipRotation(getFlipRotation() + 15);
		else if (!flipped && getFlipRotation() > 0)
			setFlipRotation(getFlipRotation() - 15);
	}

	public void setShrinkPct(float shrinkPct) {
		if (prevShrinkPercentage != shrinkPct || prevShrinkPercentage != shrinkPercentage || shrinkPercentage != shrinkPct) {
			prevShrinkPercentage = this.shrinkPercentage;
			this.shrinkPercentage = shrinkPct;
			addSyncCode(SYNC_SHRINK_PERCENTAGE);
		}
	}
	
	@Override
	public float getManaShielding() {
		return manaShield;
	}
	
	@Override
	public void setManaShielding(float manaShield) {
		manaShield = Math.max(0, manaShield);
		if (manaShield != this.manaShield) {
			this.manaShield = manaShield;
			addSyncCode(SYNC_MANA_SHIELD);
		}
	}
	
	public float getMaxMagicShielding() {
		return getCurrentLevel() * 2;
	}
	
	public float protect(float damage) {
		float left = getManaShielding() - damage;
		setManaShielding(Math.max(0, left));
		if (left < 0)
			return -left;
		return 0;
	}

	public void addMagicShielding(float manaShield) {
		setManaShielding(getManaShielding() + manaShield);
	}
	
	public void addMagicShieldingCapped(float manaShield) {
		setManaShielding(Math.min(getManaShielding() + manaShield, getMaxMagicShielding()));
	}

	@Override
	public boolean shouldUpdate() {
		return syncCode != 0;
	}

	@Override
	public byte[] generateUpdatePacket() {
		AMDataWriter writer = new AMDataWriter();
		writer.add(syncCode);
		if ((syncCode & SYNC_CONTINGENCY) == SYNC_CONTINGENCY) {
			writer.add(contingencyType.name().toLowerCase());
			boolean present = contingencyStack.isPresent();
			writer.add(present);
			if (present)
				writer.add(contingencyStack.orNull());
		}
		if ((syncCode & SYNC_MARK) == SYNC_MARK) writer.add(markX).add(markY).add(markZ).add(markDimension);
		if ((syncCode & SYNC_MANA) == SYNC_MANA) writer.add(currentMana);
		if ((syncCode & SYNC_FATIGUE) == SYNC_FATIGUE) writer.add(currentFatigue);
		if ((syncCode & SYNC_LEVEL) == SYNC_LEVEL) writer.add(currentLevel);
		if ((syncCode & SYNC_XP) == SYNC_XP) writer.add(currentXP);
		if ((syncCode & SYNC_SUMMONS) == SYNC_SUMMONS) writer.add(currentSummons);
		if ((syncCode & SYNC_FALL_PROTECTION) == SYNC_FALL_PROTECTION) writer.add(fallProtection);
		if ((syncCode & SYNC_FLIP_ROTATION) == SYNC_FLIP_ROTATION) writer.add(flipRotation).add(prevFlipRotation);
		if ((syncCode & SYNC_INVERSION_STATE) == SYNC_INVERSION_STATE) writer.add(isInverted);
		if ((syncCode & SYNC_SHRINK_STATE) == SYNC_SHRINK_STATE) writer.add(isShrunk);
		if ((syncCode & SYNC_TK_DISTANCE) == SYNC_TK_DISTANCE) writer.add(TKDistance);
		if ((syncCode & SYNC_MANA_SHIELD) == SYNC_MANA_SHIELD) writer.add(manaShield);
		if ((syncCode & SYNC_SHRINK_PERCENTAGE) == SYNC_SHRINK_PERCENTAGE) writer.add(shrinkPercentage).add(prevShrinkPercentage);
		if ((syncCode & SYNC_HEAL_COOLDOWN) == SYNC_HEAL_COOLDOWN) writer.add(healCooldown);
		if ((syncCode & SYNC_AFFINITY_HEAL_COOLDOWN) == SYNC_AFFINITY_HEAL_COOLDOWN) writer.add(affHealCooldown);
		if ((syncCode & SYNC_DISABLE_GRAVITY) == SYNC_DISABLE_GRAVITY) writer.add(disableGravity);
		syncCode = 0;
		return writer.generate();
	}

	@Override
	public void handleUpdatePacket(byte[] bytes) {
		AMDataReader reader = new AMDataReader(bytes, false);
		int syncCode = reader.getInt();
		if ((syncCode & SYNC_CONTINGENCY) == SYNC_CONTINGENCY) {
			String name = reader.getString();
			this.contingencyType = ContingencyType.fromName(name); 
			if (reader.getBoolean())
				this.contingencyStack = Optional.fromNullable(reader.getItemStack());
			else
				this.contingencyStack = Optional.absent();
		}
		if ((syncCode & SYNC_MARK) == SYNC_MARK) {
			markX = reader.getDouble();
			markY = reader.getDouble();
			markZ = reader.getDouble();
			markDimension = reader.getInt();
		}
		if ((syncCode & SYNC_MANA) == SYNC_MANA) currentMana = reader.getFloat();
		if ((syncCode & SYNC_FATIGUE) == SYNC_FATIGUE) currentFatigue = reader.getFloat();
		if ((syncCode & SYNC_LEVEL) == SYNC_LEVEL) currentLevel = reader.getInt();
		if ((syncCode & SYNC_XP) == SYNC_XP) currentXP = reader.getFloat();
		if ((syncCode & SYNC_SUMMONS) == SYNC_SUMMONS) currentSummons = reader.getInt();
		if ((syncCode & SYNC_FALL_PROTECTION) == SYNC_FALL_PROTECTION) fallProtection = reader.getFloat();
		if ((syncCode & SYNC_FLIP_ROTATION) == SYNC_FLIP_ROTATION) {
			flipRotation = reader.getFloat();
			prevFlipRotation = reader.getFloat();
		}
		if ((syncCode & SYNC_INVERSION_STATE) == SYNC_INVERSION_STATE) isInverted = reader.getBoolean();
		if ((syncCode & SYNC_SHRINK_STATE) == SYNC_SHRINK_STATE) isShrunk = reader.getBoolean();
		if ((syncCode & SYNC_TK_DISTANCE) == SYNC_TK_DISTANCE) TKDistance = reader.getFloat();
		if ((syncCode & SYNC_MANA_SHIELD) == SYNC_MANA_SHIELD) manaShield = reader.getFloat();
		if ((syncCode & SYNC_SHRINK_PERCENTAGE) == SYNC_SHRINK_PERCENTAGE) {
			shrinkPercentage = reader.getFloat();
			prevShrinkPercentage = reader.getFloat();
		}
		if ((syncCode & SYNC_HEAL_COOLDOWN) == SYNC_HEAL_COOLDOWN) healCooldown = reader.getInt();
		if ((syncCode & SYNC_AFFINITY_HEAL_COOLDOWN) == SYNC_AFFINITY_HEAL_COOLDOWN) affHealCooldown = reader.getInt();
		if ((syncCode & SYNC_DISABLE_GRAVITY) == SYNC_DISABLE_GRAVITY) disableGravity = reader.getBoolean();
	}
	
	@Override
	public void forceUpdate() {
		syncCode = 0xFFFFFFFF;
	}
}
