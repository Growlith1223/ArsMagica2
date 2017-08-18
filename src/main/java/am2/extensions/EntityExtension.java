package am2.extensions;

import static am2.extensions.DataDefinitions.AFFINITY_HEAL_COOLDOWN;
import static am2.extensions.DataDefinitions.CONTENGENCY_STACK;
import static am2.extensions.DataDefinitions.CONTENGENCY_TYPE;
import static am2.extensions.DataDefinitions.CURRENT_LEVEL;
import static am2.extensions.DataDefinitions.CURRENT_MANA;
import static am2.extensions.DataDefinitions.CURRENT_MANA_FATIGUE;
import static am2.extensions.DataDefinitions.CURRENT_SUMMONS;
import static am2.extensions.DataDefinitions.CURRENT_XP;
import static am2.extensions.DataDefinitions.FALL_PROTECTION;
import static am2.extensions.DataDefinitions.FLIP_ROTATION;
import static am2.extensions.DataDefinitions.HEAL_COOLDOWN;
import static am2.extensions.DataDefinitions.IS_INVERTED;
import static am2.extensions.DataDefinitions.IS_SHRUNK;
import static am2.extensions.DataDefinitions.MANA_SHIELD;
import static am2.extensions.DataDefinitions.MARK_DIMENSION;
import static am2.extensions.DataDefinitions.MARK_X;
import static am2.extensions.DataDefinitions.MARK_Y;
import static am2.extensions.DataDefinitions.MARK_Z;
import static am2.extensions.DataDefinitions.PREV_FLIP_ROTATION;
import static am2.extensions.DataDefinitions.PREV_SHRINK_PCT;
import static am2.extensions.DataDefinitions.SHRINK_PCT;
import static am2.extensions.DataDefinitions.TK_DISTANCE;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.common.base.Optional;

import am2.ArsMagica2;
import am2.api.ArsMagicaAPI;
import am2.api.event.PlayerMagicLevelChangeEvent;
import am2.api.extensions.IDataSyncExtension;
import am2.api.extensions.IEntityExtension;
import am2.api.math.AMVector2;
import am2.armor.ArmorHelper;
import am2.armor.ArsMagicaArmorMaterial;
import am2.armor.infusions.GenericImbuement;
import am2.armor.infusions.ImbuementRegistry;
import am2.bosses.EntityLifeGuardian;
import am2.config.AMConfig;
import am2.defs.ItemDefs;
import am2.defs.PotionEffectsDefs;
import am2.defs.SkillDefs;
import am2.extensions.datamanager.DataSyncExtension;
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
	public ArrayList<ItemStack> runningStacks = new ArrayList<>();
		
	@Override
	public boolean hasEnoughtMana(float cost) {
		if (getCurrentMana() + getBonusCurrentMana() < cost)
			return false;
		return true;
	}
	
	@Override
	public void setContingency (ContingencyType type, ItemStack stack) {
		DataSyncExtension.For(entity).set(CONTENGENCY_TYPE, type.name().toLowerCase());
		DataSyncExtension.For(entity).set(CONTENGENCY_STACK, Optional.fromNullable(stack));
	}
	
	@Override
	public ContingencyType getContingencyType() {
		return ContingencyType.fromName(DataSyncExtension.For(entity).get(CONTENGENCY_TYPE));
	}
	
	@Override
	public ItemStack getContingencyStack() {
		return DataSyncExtension.For(entity).get(CONTENGENCY_STACK).orNull();
	}
	
	@Override
	public double getMarkX() {
		return DataSyncExtension.For(entity).get(MARK_X);
	}
	
	@Override
	public double getMarkY() {
		return DataSyncExtension.For(entity).get(MARK_Y);
	}
	
	@Override
	public double getMarkZ() {
		return DataSyncExtension.For(entity).get(MARK_Z);
	}
	
	@Override
	public int getMarkDimensionID() {
		return DataSyncExtension.For(entity).get(MARK_DIMENSION);
	}
	
	@Override
	public float getCurrentMana() {
		return DataSyncExtension.For(entity).get(CURRENT_MANA);
	}
	
	@Override
	public int getCurrentLevel() {
		return DataSyncExtension.For(entity).get(CURRENT_LEVEL);
	}
	
	@Override
	public float getCurrentBurnout() {
		return DataSyncExtension.For(entity).get(CURRENT_MANA_FATIGUE);
	}
	
	@Override
	public int getCurrentSummons() {
		return DataSyncExtension.For(entity).get(CURRENT_SUMMONS);
	}
	
	@Override
	public float getCurrentXP() {
		return DataSyncExtension.For(entity).get(CURRENT_XP);
	}
	
	@Override
	public int getHealCooldown() {
		return DataSyncExtension.For(entity).get(HEAL_COOLDOWN);
	}
	
	@Override
	public void lowerHealCooldown(int amount) {
		setHealCooldown(Math.max(0, getHealCooldown() - amount));
	}
	
	@Override
	public void placeHealOnCooldown() {
		DataSyncExtension.For(entity).set(HEAL_COOLDOWN, 40);
	}
	
	@Override
	public void lowerAffinityHealCooldown (int amount) {
		setAffinityHealCooldown(Math.max(0, getAffinityHealCooldown() - amount));
	}
	
	@Override
	public int getAffinityHealCooldown() {
		return DataSyncExtension.For(entity).get(AFFINITY_HEAL_COOLDOWN);
	}
	
	@Override
	public void placeAffinityHealOnCooldown(boolean full) {
		DataSyncExtension.For(entity).set(AFFINITY_HEAL_COOLDOWN, 40);
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
		DataSyncExtension.For(entity).set(AFFINITY_HEAL_COOLDOWN, affinityHealCooldown);
	}
	
	@Override
	public void setCurrentBurnout(float currentBurnout) {
		DataSyncExtension.For(entity).set(CURRENT_MANA_FATIGUE, currentBurnout);
	}
	
	@Override
	public void setCurrentLevel(int currentLevel) {
		ticksForFullRegen = (int)Math.round(baseTicksForFullRegen * (0.75 - (0.25 * (getCurrentLevel() / 99f))));
		if (entity instanceof EntityPlayer)
			MinecraftForge.EVENT_BUS.post(new PlayerMagicLevelChangeEvent((EntityPlayer) entity, currentLevel));
		DataSyncExtension.For(entity).set(CURRENT_LEVEL, currentLevel);
	}
	
	@Override
	public void setCurrentMana(float currentMana) {
		DataSyncExtension.For(entity).set(CURRENT_MANA, currentMana);
	}
	
	@Override
	public void setCurrentSummons(int currentSummons) {
		DataSyncExtension.For(entity).set(CURRENT_SUMMONS, currentSummons);
	}
	
	@Override
	public void setCurrentXP(float currentXP) {
		while (currentXP >= this.getMaxXP()) {
			currentXP -= this.getMaxXP();
			setMagicLevelWithMana(getCurrentLevel() + 1);
		}
		DataSyncExtension.For(entity).set(CURRENT_XP, currentXP);
	}
	
	@Override
	public void setHealCooldown(int healCooldown) {
		DataSyncExtension.For(entity).set(HEAL_COOLDOWN, healCooldown);
	}
	
	@Override
	public void setMarkX(double markX) {
		DataSyncExtension.For(entity).set(MARK_X, markX);
	}
	
	@Override
	public void setMarkY(double markY) {
		DataSyncExtension.For(entity).set(MARK_Y, markY);
	}
	
	@Override
	public void setMarkZ(double markZ) {
		DataSyncExtension.For(entity).set(MARK_Z, markZ);
	}
	
	@Override
	public void setMarkDimensionID(int markDimensionID) {
		DataSyncExtension.For(entity).set(MARK_DIMENSION, markDimensionID);
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
		return DataSyncExtension.For(entity).get(IS_SHRUNK);
	}
	
	@Override
	public void setShrunk(boolean shrunk) {
		DataSyncExtension.For(entity).set(IS_SHRUNK, shrunk);
	}
	
	@Override
	public void setInverted(boolean isInverted) {
		DataSyncExtension.For(entity).set(IS_INVERTED, isInverted);
	}
	
	@Override
	public void setFallProtection(float hasFallProtection) {
		DataSyncExtension.For(entity).set(FALL_PROTECTION, hasFallProtection);
	}
	
	@Override
	public boolean isInverted() {
		return DataSyncExtension.For(entity).get(IS_INVERTED) == null ? false : DataSyncExtension.For(entity).get(IS_INVERTED);
	}
	
	@Override
	public float getFallProtection() {
		return DataSyncExtension.For(entity).get(FALL_PROTECTION) == null ? 0 : DataSyncExtension.For(entity).get(FALL_PROTECTION);
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
	public void init(EntityLivingBase entity, IDataSyncExtension ext) {
		this.addEntityReference(entity);
		if (this.entity instanceof EntityPlayer) {
			ext.setWithSync(CURRENT_LEVEL, 0);
			ext.setWithSync(CURRENT_MANA, 0F);
			ext.setWithSync(CURRENT_MANA_FATIGUE, 0F);
			ext.setWithSync(CURRENT_XP, 0F);
			ext.setWithSync(CURRENT_SUMMONS, 0);
		} else {
			ext.setWithSync(CURRENT_LEVEL, 0);
			ext.setWithSync(CURRENT_MANA, 500F);
			ext.setWithSync(CURRENT_MANA_FATIGUE, 0F);
			ext.setWithSync(CURRENT_XP, 0F);
			ext.setWithSync(CURRENT_SUMMONS, 0);			
		}
		ext.setWithSync(HEAL_COOLDOWN, 0);
		ext.setWithSync(AFFINITY_HEAL_COOLDOWN, 0);
		ext.setWithSync(MARK_X, 0D);
		ext.setWithSync(MARK_Y, 0D);
		ext.setWithSync(MARK_Z, 0D);
		ext.setWithSync(MARK_DIMENSION, -512);
		ext.setWithSync(CONTENGENCY_STACK, Optional.absent());
		ext.setWithSync(CONTENGENCY_TYPE, "NULL");
		ext.setWithSync(FALL_PROTECTION, 0.0f);
		ext.setWithSync(IS_INVERTED, false);
		ext.setWithSync(IS_SHRUNK, false);
		ext.setWithSync(FLIP_ROTATION, 0.0f);
		ext.setWithSync(PREV_FLIP_ROTATION, 0.0f);
		ext.setWithSync(SHRINK_PCT, 0.0f);
		ext.setWithSync(PREV_SHRINK_PCT, 0.0f);
		ext.setWithSync(TK_DISTANCE, 8.0f);
		ext.setWithSync(DataDefinitions.DISABLE_GRAVITY, false);
		ext.setWithSync(MANA_SHIELD, 0f);
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
		return DataSyncExtension.For(entity).get(IS_INVERTED) == null ? false : DataSyncExtension.For(entity).get(IS_INVERTED);
	}

	@Override
	public float getFlipRotation() {
		return DataSyncExtension.For(entity).get(FLIP_ROTATION) == null ? 0 : DataSyncExtension.For(entity).get(FLIP_ROTATION);
	}

	@Override
	public float getPrevFlipRotation() {
		return DataSyncExtension.For(entity).get(PREV_FLIP_ROTATION) == null ? 0 : DataSyncExtension.For(entity).get(PREV_FLIP_ROTATION);
	}
	
	@Override
	public void setFlipRotation(float rot) {
		DataSyncExtension.For(entity).set(FLIP_ROTATION, rot);
	}

	@Override
	public void setPrevFlipRotation(float rot) {
		DataSyncExtension.For(entity).set(PREV_FLIP_ROTATION, rot);
	}

	@Override
	public float getShrinkPct() {
		return DataSyncExtension.For(entity).get(SHRINK_PCT) == null ? 0 : DataSyncExtension.For(entity).get(SHRINK_PCT);
	}

	@Override
	public float getPrevShrinkPct() {
		return DataSyncExtension.For(entity).get(PREV_SHRINK_PCT) == null ? 0 : DataSyncExtension.For(entity).get(PREV_SHRINK_PCT);
	}

	@Override
	public void setTKDistance(float TK_Distance) {
		DataSyncExtension.For(entity).set(TK_DISTANCE, TK_Distance);
	}

	@Override
	public void addToTKDistance(float toAdd) {
		setTKDistance(getTKDistance() + toAdd);
	}

	@Override
	public float getTKDistance() {
		return DataSyncExtension.For(entity).get(TK_DISTANCE) == null ? 0 : DataSyncExtension.For(entity).get(TK_DISTANCE);
	}
	
	@Override
	public void syncTKDistance() {
		AMDataWriter writer = new AMDataWriter();
		writer.add(this.getTKDistance());
		AMNetHandler.INSTANCE.sendPacketToServer(AMPacketIDs.TK_DISTANCE_SYNC, writer.generate());
	}
	
	@Override
	public void manaBurnoutTick(){
		boolean willForceRegen = false;
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
					willForceRegen = ArsMagica2.config.forceManaRegen();
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

				float manaToAdd = (float) (actualMaxMana / regenTicks);
				
				if (!willForceRegen)
					manaToAdd *= ArsMagica2.config.getManaRegenModifier();
				
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
		DataSyncExtension.For(entity).set(DataDefinitions.DISABLE_GRAVITY, b);
	}
	
	@Override
	public boolean isGravityDisabled() {
		return DataSyncExtension.For(entity).get(DataDefinitions.DISABLE_GRAVITY);
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
		DataSyncExtension.For(entity).set(PREV_SHRINK_PCT, getShrinkPct());
		DataSyncExtension.For(entity).set(SHRINK_PCT, shrinkPct);
	}
	
	@Override
	public float getManaShielding() {
		return DataSyncExtension.For(entity).get(MANA_SHIELD);
	}
	
	@Override
	public void setManaShielding(float manaShield) {
		manaShield = Math.max(0, manaShield);
		DataSyncExtension.For(entity).set(MANA_SHIELD, manaShield);
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
}
