package am2.extensions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

import am2.ArsMagica2;
import am2.api.ArsMagicaAPI;
import am2.api.SkillPointRegistry;
import am2.api.SkillRegistry;
import am2.api.compendium.CompendiumCategory;
import am2.api.compendium.CompendiumEntry;
import am2.api.extensions.ISkillData;
import am2.api.skill.Skill;
import am2.api.skill.SkillPoint;
import am2.api.spell.AbstractSpellPart;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellModifier;
import am2.api.spell.SpellShape;
import am2.lore.ArcaneCompendium;
import am2.packet.AMDataReader;
import am2.packet.AMDataWriter;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class SkillData implements ISkillData, ICapabilityProvider, ICapabilitySerializable<NBTBase> {
	
	private EntityPlayer player;
	public static final ResourceLocation ID = new ResourceLocation("arsmagica2:SkillData");
	
	private static final int SYNC_SKILLS = 0x1;
	private static final int SYNC_SKILL_POINTS = 0x2;
	
	private int syncCode = 0;
	
	private HashMap<Skill, Boolean> skills;
	private HashMap<SkillPoint, Integer> skillPoints;
	
	@CapabilityInject(value=ISkillData.class)
	public static Capability<ISkillData> INSTANCE = null;
	
	public SkillData() {
		this.skills = new HashMap<>();
		this.skillPoints = new HashMap<>();
	}
	
	public static ISkillData For(EntityLivingBase living) {
		return living.getCapability(INSTANCE, null);
	}
	
	public HashMap<Skill, Boolean> getSkills() {
		return skills;
	}
	
	public boolean hasSkill (String name) {
		if (player.capabilities.isCreativeMode) return true;
		if (ArsMagica2.disabledSkills.isSkillDisabled(name)) return true;
		Boolean bool = skills.get(SkillRegistry.getSkillFromName(name));
		return bool == null ? false : bool;
	}
	
	public void unlockSkill (String name) {
		if (SkillRegistry.getSkillFromName(name) == null)
			return;
		Skill skill = SkillRegistry.getSkillFromName(name);
		
		for (CompendiumEntry entry : CompendiumCategory.getAllEntries()) {
			if (ArsMagicaAPI.getSpellRegistry().getObject(skill.getRegistryName()) != null) {
				AbstractSpellPart part = ArsMagicaAPI.getSpellRegistry().getObject(skill.getRegistryName());
				for (Object obj : entry.getObjects()) {
					if (obj == part) {
						ArcaneCompendium.For(player).unlockEntry(entry.getID());
					}
				}
			} else {
				for (Object obj : entry.getObjects()) {
					if (obj == skill) {
						ArcaneCompendium.For(player).unlockEntry(entry.getID());
					}
				}
			}
		}
		
		setSkillPoint(skill.getPoint(), getSkillPoint(skill.getPoint()) - 1);
		skills.put(skill, true);
		this.syncCode |= SYNC_SKILLS;
	}
	
	public HashMap<SkillPoint, Integer> getSkillPoints() {
		return skillPoints;
	}
	
	public int getSkillPoint(SkillPoint point) {
		if (point == null)
			return 0;
		Integer integer = skillPoints.get(point);
		return integer == null ? 0 : integer.intValue();
	}
	
	public void setSkillPoint(SkillPoint point, int num) {
		if (!skillPoints.containsKey(point) || skillPoints.get(point).intValue() != num) {
			skillPoints.put(point, num);
			this.syncCode |= SYNC_SKILL_POINTS;
		}
	}

	public void init(EntityPlayer entity) {
		this.player = entity;
		HashMap<Skill, Boolean> skillMap = new HashMap<Skill, Boolean>();
		HashMap<SkillPoint, Integer> pointMap = new HashMap<SkillPoint, Integer>();
		for (Skill aff : ArsMagicaAPI.getSkillRegistry().getValues()) {
			skillMap.put(aff, false);
		}
		for (SkillPoint aff : SkillPointRegistry.getSkillPointMap().values()) {
			pointMap.put(aff, 0);
		}
		pointMap.put(SkillPoint.SKILL_POINT_1, 3);
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
		return new ISkillData.Storage().writeNBT(INSTANCE, this, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt) {
		new ISkillData.Storage().readNBT(INSTANCE, this, null, nbt);
	}

	@Override
	public boolean canLearn(String name) {
		if (SkillRegistry.getSkillFromName(name) == null) return false;
		if (ArsMagica2.disabledSkills.isSkillDisabled(name)) return false;
		for (String skill : SkillRegistry.getSkillFromName(name).getParents()) {
			Skill s = SkillRegistry.getSkillFromName(skill);
			if (s == null) continue;
			if (hasSkill(skill)) continue;
			return false;
		}
		if (getSkillPoint(SkillRegistry.getSkillFromName(name).getPoint()) <= 0)
			return false;
		return true;
	}

	@Override
	public ArrayList<String> getKnownShapes() {
		ArrayList<String> out = new ArrayList<>();
		for (Skill skill : ArsMagicaAPI.getSkillRegistry()) {
			AbstractSpellPart part = ArsMagicaAPI.getSpellRegistry().getValue(skill.getRegistryName());
			if ((this.hasSkill(skill.getRegistryName().toString()) || player.capabilities.isCreativeMode) && part != null && part instanceof SpellShape && !ArsMagica2.disabledSkills.isSkillDisabled(part.getRegistryName().toString()))
				out.add(skill.getID());
		}
		out.sort(new Comparator<String>() {public int compare(String o1, String o2) {return o1.compareTo(o2);}});
		return out;
	}

	@Override
	public ArrayList<String> getKnownComponents() {
		ArrayList<String> out = new ArrayList<>();
		for (Skill skill : ArsMagicaAPI.getSkillRegistry()) {
			AbstractSpellPart part = ArsMagicaAPI.getSpellRegistry().getValue(skill.getRegistryName());
			if ((this.hasSkill(skill.getRegistryName().toString()) || player.capabilities.isCreativeMode) && part != null && part instanceof SpellComponent && !ArsMagica2.disabledSkills.isSkillDisabled(part.getRegistryName().toString()))
				out.add(skill.getID());
		}
		out.sort(new Comparator<String>() {public int compare(String o1, String o2) {return o1.compareTo(o2);}});
		return out;
	}

	@Override
	public ArrayList<String> getKnownModifiers() {
		ArrayList<String> out = new ArrayList<>();
		for (Skill skill : ArsMagicaAPI.getSkillRegistry()) {
			AbstractSpellPart part = ArsMagicaAPI.getSpellRegistry().getValue(skill.getRegistryName());
			if ((this.hasSkill(skill.getRegistryName().toString()) || player.capabilities.isCreativeMode) && part != null && part instanceof SpellModifier && !ArsMagica2.disabledSkills.isSkillDisabled(part.getRegistryName().toString()))
				out.add(skill.getID());
		}
		out.sort(new Comparator<String>() {public int compare(String o1, String o2) {return o1.compareTo(o2);}});
		return out;
	}

	@Override
	public boolean shouldUpdate() {
		return syncCode != 0;
	}

	@Override
	public byte[] generateUpdatePacket() {
		AMDataWriter writer = new AMDataWriter();
		writer.add(syncCode);
		if ((syncCode & SYNC_SKILLS) == SYNC_SKILLS) {
			writer.add(skills.size());
			for (Entry<Skill, Boolean> entry : skills.entrySet()) {
				writer.add(entry.getKey().getRegistryName().toString());
				writer.add(entry.getValue().booleanValue());
			}
		}
		if ((syncCode & SYNC_SKILL_POINTS) == SYNC_SKILL_POINTS) {
			writer.add(skillPoints.size());
			for (Entry<SkillPoint, Integer> entry : skillPoints.entrySet()) {
				writer.add(entry.getKey().getName().toString());
				writer.add(entry.getValue().intValue());
			}
		}
		syncCode = 0;
		return writer.generate();
	}

	@Override
	public void handleUpdatePacket(byte[] bytes) {
		AMDataReader reader = new AMDataReader(bytes, false);
		int syncCode = reader.getInt();
		if ((syncCode & SYNC_SKILLS) == SYNC_SKILLS) {
			skills.clear();
			int size = reader.getInt();
			for (int i = 0; i < size; i++) {
				Skill key = ArsMagicaAPI.getSkillRegistry().getObject(new ResourceLocation(reader.getString()));
				boolean value = reader.getBoolean();
				if (key != null)
					skills.put(key, value);
			}
		}
		if ((syncCode & SYNC_SKILL_POINTS) == SYNC_SKILL_POINTS) {
			skillPoints.clear();
			int size = reader.getInt();
			for (int i = 0; i < size; i++) {
				SkillPoint key = SkillPointRegistry.fromName(reader.getString());
				int value = reader.getInt();
				if (key != null)
					skillPoints.put(key, value);
			}
		}
	}

	@Override
	public void forceUpdate() {
		syncCode = 0xFFFFFFFF;
	}

}
