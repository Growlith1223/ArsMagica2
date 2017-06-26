package am2.api.extensions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import am2.api.SkillPointRegistry;
import am2.api.skill.Skill;
import am2.api.skill.SkillPoint;
import am2.extensions.SkillData;
import am2.utils.NBTUtils;

public interface ISkillData {
	
	public boolean hasSkill (String name);
	
	public void unlockSkill (String name);
	
	public HashMap<Skill, Boolean> getSkills();
	
	public HashMap<SkillPoint, Integer> getSkillPoints();
	
	public int getSkillPoint(SkillPoint skill);
	
	public void setSkillPoint(SkillPoint point, int num);

	public boolean canLearn(String name);
	public boolean shouldUpdate();
	public byte[] generateUpdatePacket();
	public void handleUpdatePacket(byte[] bytes);
	public void forceUpdate();

	
	public static class Storage implements IStorage<ISkillData> {
		
		@Override
		public NBTBase writeNBT(Capability<ISkillData> capability, ISkillData instance, EnumFacing side) {
			NBTTagCompound nbt = new NBTTagCompound();
			NBTTagCompound am2Tag = NBTUtils.getAM2Tag(nbt);
			NBTTagList skillList = NBTUtils.addCompoundList(am2Tag, "Skills");
			NBTTagList skillPointList = NBTUtils.addCompoundList(am2Tag, "SkillPoints");
			for (Entry<Skill, Boolean> skill : instance.getSkills().entrySet()) {
				if (skill.getKey() == null)
					continue;
				NBTTagCompound tmp = new NBTTagCompound();
				tmp.setString("Skill", skill.getKey().getID());
				tmp.setBoolean("Unlocked", skill.getValue());
				skillList.appendTag(tmp);
			}
			for (Entry<SkillPoint, Integer> skill : instance.getSkillPoints().entrySet()) {
				if (skill.getKey() == null)
					continue;
				NBTTagCompound tmp = new NBTTagCompound();
				tmp.setString("Type", skill.getKey().getName());
				tmp.setInteger("Number", skill.getValue());
				skillPointList.appendTag(tmp);				
			}
			am2Tag.setTag("Skills", skillList);
			am2Tag.setTag("SkillPoints", skillPointList);
			return nbt;
		}

		@Override
		public void readNBT(Capability<ISkillData> capability, ISkillData instance, EnumFacing side, NBTBase nbt) {
			NBTTagCompound am2Tag = NBTUtils.getAM2Tag((NBTTagCompound) nbt);
			NBTTagList skillList = NBTUtils.addCompoundList(am2Tag, "Skills");
			NBTTagList skillPointList = NBTUtils.addCompoundList(am2Tag, "SkillPoints");
			for (int i = 0; i < skillList.tagCount(); i++) {
				NBTTagCompound tmp = skillList.getCompoundTagAt(i);
				if (tmp.getBoolean("Unlocked"))
					instance.unlockSkill(tmp.getString("Skill"));
			}
			for (int i = 0; i < skillPointList.tagCount(); i++) {
				NBTTagCompound tmp = skillPointList.getCompoundTagAt(i);
				instance.setSkillPoint(SkillPointRegistry.fromName(tmp.getString("Type")), tmp.getInteger("Number"));
			}
		}
	}
	
	public static class Factory implements Callable<ISkillData> {
		@Override
		public ISkillData call() throws Exception {
			return new SkillData();
		}
	}

	public ArrayList<String> getKnownShapes();

	public ArrayList<String> getKnownComponents();

	public ArrayList<String> getKnownModifiers();
}
