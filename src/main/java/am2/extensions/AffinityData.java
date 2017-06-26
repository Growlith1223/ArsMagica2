package am2.extensions;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import am2.api.extensions.IAffinityData;
import am2.packet.AMDataReader;
import am2.packet.AMDataWriter;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class AffinityData implements IAffinityData, ICapabilityProvider, ICapabilitySerializable<NBTBase> {
	public static final ResourceLocation ID = new ResourceLocation("arsmagica2:AffinityData");
	public static final String NIGHT_VISION = "night_vision";
	public static final String ICE_BRIDGE_STATE = "ice_bridge";
	public static final float MAX_DEPTH = 100F;
	private static final float ADJACENT_FACTOR = 0.25f;
	private static final float MINOR_OPPOSING_FACTOR = 0.5f;
	private static final float MAJOR_OPPOSING_FACTOR = 0.75f;
	
	private static final int SYNC_DEPTHS = 0x1;
	private static final int SYNC_ABILITY_BOOLEANS = 0x2;
	private static final int SYNC_ABILITY_FLOATS = 0x4;
	private static final int SYNC_COOLDOWNS = 0x8;
	private static final int SYNC_DIMINISHING_RETURNS = 0x10;
	
	private HashMap<Affinity, Double> depths;
	private HashMap<String, Boolean> abilityBools;
	private HashMap<String, Float> abilityFloats;
	private HashMap<String, Integer> cooldowns;
	private float diminishingReturns = 1.0F;
	public float accumulatedLifeRegen = 0.0f;
	public float accumulatedHungerRegen = 0.0f;
	
	private int syncCode = 0;
	
	@CapabilityInject(value = IAffinityData.class)
	public static Capability<IAffinityData> INSTANCE = null;
	
	public AffinityData() {
		this.depths = new HashMap<>();
		this.abilityBools = new HashMap<>();
		this.abilityFloats = new HashMap<>();
		this.cooldowns = new HashMap<>();
	}
	
	public static AffinityData For(EntityLivingBase living){
		return (AffinityData) living.getCapability(INSTANCE, null);
	}
	
	public double getAffinityDepth(Affinity aff) {
		Double depth = depths.get(aff);
		if (depth == null)
			depth = 0D;
		return depth / MAX_DEPTH;
	}
	
	public void setAffinityDepth (Affinity name, double value) {
		value = MathHelper.clamp_double(value, 0, MAX_DEPTH);
		if (value != getAffinityDepth(name)) {
			syncCode |= SYNC_DEPTHS;
			depths.put(name, value);
		}
	}
	
	public HashMap<Affinity, Double> getAffinities() {
		return depths;
	}

	public void init(EntityPlayer entity) {
		HashMap<Affinity, Double> map = new HashMap<>();
		for (Affinity aff : ArsMagicaAPI.getAffinityRegistry().getValues())
			map.put(aff, 0D);
	}
	
	@Override
	public boolean getAbilityBoolean(String name) {
		Boolean bool = getAbilityBooleanMap().get(name);
		return bool == null ? false : bool.booleanValue();
	}
	
	@Override
	public void addAbilityBoolean(String name, boolean bool) {
		if (getAbilityBoolean(name) != bool) {
			abilityBools.put(name, bool);
			syncCode |= SYNC_ABILITY_BOOLEANS;
		}
	}
	
	@Override
	public float getAbilityFloat(String name) {
		Float bool = getAbilityFloatMap().get(name);
		return bool == null ? 0f : bool.floatValue();
	}
	
	@Override
	public void addAbilityFloat(String name, float f) {
		if (getAbilityFloat(name) != f) {
			abilityFloats.put(name, f);
			syncCode |= SYNC_ABILITY_FLOATS;
		}
	}
	
	@Override
	public Map<String, Boolean> getAbilityBooleanMap() {
		return abilityBools;
	}
	
	@Override
	public Map<String, Float> getAbilityFloatMap() {
		return abilityFloats;
	}
	
	@Override
	public void addCooldown(String name, int cooldown) {
		if (getCooldown(name) != cooldown) {
			cooldowns.put(name, cooldown);
			syncCode |= SYNC_COOLDOWNS;
		}
	}
	
	@Override
	public int getCooldown(String name) {
		return cooldowns.get(name) == null ? 0 : cooldowns.get(name);
	}
	
	@Override
	public Map<String, Integer> getCooldowns() {
		return cooldowns;
	}
	
	@Override
	public float getDiminishingReturnsFactor(){
		return diminishingReturns;
	}
	
	@Override
	public void tickDiminishingReturns(){
		if (getDiminishingReturnsFactor() < 1.3f){
			diminishingReturns += 0.005f;
			syncCode |= SYNC_DIMINISHING_RETURNS;
		}
	}
	
	@Override
	public void addDiminishingReturns(boolean isChanneled){
		diminishingReturns -= isChanneled ? 0.1f : 0.3f;
		syncCode |= SYNC_DIMINISHING_RETURNS;
		if (diminishingReturns < 0) 
			diminishingReturns = 0F;
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

	@Override
	public NBTBase serializeNBT() {
		return new IAffinityData.Storage().writeNBT(INSTANCE, this, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt) {
		new IAffinityData.Storage().readNBT(INSTANCE, this, null, nbt);
	}

	@Override
	public Affinity[] getHighestAffinities() {
		double max1 = 0;
		double max2 = 0;
		Affinity maxAff1 = Affinity.NONE;
		Affinity maxAff2 = Affinity.NONE;
		for (Entry<Affinity, Double> entry : getAffinities().entrySet()) {
			if (entry.getValue() > max1) {
				max2 = max1;
				maxAff2 = maxAff1;
				max1 = entry.getValue();
				maxAff1 = entry.getKey();
			} else if (entry.getValue() > max2) {
				max2 = entry.getValue();
				maxAff2 = entry.getKey();
			}
		}
		return new Affinity[] {maxAff1, maxAff2};
	}
	
	@Override
	public byte[] generateUpdatePacket() {
		AMDataWriter writer = new AMDataWriter();
		writer.add(syncCode);
		if ((syncCode & SYNC_DEPTHS) == SYNC_DEPTHS) {
			writer.add(depths.size());
			for (Entry<Affinity, Double> entry : depths.entrySet()) {
				writer.add(entry.getKey().getRegistryName().toString());
				writer.add(entry.getValue().doubleValue());
			}
		}
		if ((syncCode & SYNC_ABILITY_BOOLEANS) == SYNC_ABILITY_BOOLEANS) {
			writer.add(abilityBools.size());
			for (Entry<String, Boolean> entry : abilityBools.entrySet()) {
				writer.add(entry.getKey());
				writer.add(entry.getValue().booleanValue());
			}
		}
		if ((syncCode & SYNC_ABILITY_FLOATS) == SYNC_ABILITY_FLOATS) {
			writer.add(abilityFloats.size());
			for (Entry<String, Float> entry : abilityFloats.entrySet()) {
				writer.add(entry.getKey());
				writer.add(entry.getValue().floatValue());
			}
		}
		if ((syncCode & SYNC_COOLDOWNS) == SYNC_COOLDOWNS) {
			writer.add(cooldowns.size());
			for (Entry<String, Integer> entry : cooldowns.entrySet()) {
				writer.add(entry.getKey());
				writer.add(entry.getValue());
			}
		}
		if ((syncCode & SYNC_DIMINISHING_RETURNS) == SYNC_DIMINISHING_RETURNS)
			writer.add(diminishingReturns);
		syncCode = 0;
		return writer.generate();
	}
	
	@Override
	public void handleUpdatePacket(byte[] bytes) {
		AMDataReader reader = new AMDataReader(bytes, false);
		int syncCode = reader.getInt();
		if ((syncCode & SYNC_DEPTHS) == SYNC_DEPTHS) {
			depths.clear();
			int size = reader.getInt();
			for (int i = 0; i < size; i++) {
				Affinity key = ArsMagicaAPI.getAffinityRegistry().getObject(new ResourceLocation(reader.getString()));
				double value = reader.getDouble();
				if (key != null)
					depths.put(key, value);
			}
		}
		if ((syncCode & SYNC_ABILITY_BOOLEANS) == SYNC_ABILITY_BOOLEANS) {
			abilityBools.clear();
			int size = reader.getInt();
			for (int i = 0; i < size; i++) {
				String key = reader.getString();
				boolean value = reader.getBoolean();
				if (key != null)
					abilityBools.put(key, value);
			}
		}
		if ((syncCode & SYNC_ABILITY_FLOATS) == SYNC_ABILITY_FLOATS) {
			abilityFloats.clear();
			int size = reader.getInt();
			for (int i = 0; i < size; i++) {
				String key = reader.getString();
				float value = reader.getFloat();
				if (key != null)
					abilityFloats.put(key, value);
			}
		}
		if ((syncCode & SYNC_COOLDOWNS) == SYNC_COOLDOWNS) {
			cooldowns.clear();
			int size = reader.getInt();
			for (int i = 0; i < size; i++) {
				String key = reader.getString();
				int value = reader.getInt();
				if (key != null)
					cooldowns.put(key, value);
			}
		}
		if ((syncCode & SYNC_DIMINISHING_RETURNS) == SYNC_DIMINISHING_RETURNS)
			diminishingReturns = reader.getFloat();
	}

	@Override
	public void incrementAffinity(Affinity affinity, float amt) {
		if (affinity == Affinity.NONE || isLocked()) return;

		float adjacentDecrement = amt * ADJACENT_FACTOR;
		float minorOppositeDecrement = amt * MINOR_OPPOSING_FACTOR;
		float majorOppositeDecrement = amt * MAJOR_OPPOSING_FACTOR;
		
		addToAffinity(affinity, amt);

		if (getAffinityDepth(affinity) * MAX_DEPTH == MAX_DEPTH){
			setLocked(true);
		}

		for (Affinity adjacent : affinity.getAdjacentAffinities()){
			subtractFromAffinity(adjacent, adjacentDecrement);
		}

		for (Affinity minorOpposite : affinity.getMinorOpposingAffinities()){
			subtractFromAffinity(minorOpposite, minorOppositeDecrement);
		}

		for (Affinity majorOpposite : affinity.getMajorOpposingAffinities()){
			subtractFromAffinity(majorOpposite, majorOppositeDecrement);
		}

		Affinity directOpposite = affinity.getOpposingAffinity();
		if (directOpposite != null){
			subtractFromAffinity(directOpposite, amt);
		}
	}
	
	private void addToAffinity(Affinity affinity, float amt){
		if (affinity == Affinity.NONE) return;
		double existingAmt = getAffinityDepth(affinity) * MAX_DEPTH;
		existingAmt += amt;
		if (existingAmt > MAX_DEPTH) existingAmt = MAX_DEPTH;
		else if (existingAmt < 0) existingAmt = 0;
		setAffinityDepth(affinity, existingAmt);
	}
	
	private void subtractFromAffinity(Affinity affinity, float amt){
		if (affinity == Affinity.NONE) return;
		double existingAmt = getAffinityDepth(affinity)  * MAX_DEPTH;
		existingAmt -= amt;
		if (existingAmt > MAX_DEPTH) existingAmt = MAX_DEPTH;
		else if (existingAmt < 0) existingAmt = 0;
		setAffinityDepth(affinity, existingAmt);
	}

	@Override
	public void setLocked(boolean b) {
		addAbilityBoolean("affinity_data_locked", b);
	}
	
	@Override
	public boolean isLocked() {
		return getAbilityBoolean("affinity_data_locked");
	}

	@Override
	public boolean shouldUpdate() {
		return syncCode != 0;
	}

	@Override
	public void forceUpdate() {
		syncCode = 0xFFFFFFFF;
	}
}
