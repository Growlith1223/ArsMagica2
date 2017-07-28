package am2.entity.ai;

import java.util.ArrayList;

import com.google.common.collect.Lists;

import am2.api.ArsMagicaAPI;
import am2.defs.SkillDefs;
import am2.extensions.EntityExtension;
import am2.extensions.SkillData;
import am2.utils.EntityUtils;
import am2.utils.SpellUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class EntityAIAllyManaLink extends EntityAIBase{

	private EntityCreature host;
	private static final ItemStack spellStack = SpellUtils.createSpellStack(new ArrayList<>(), Lists.newArrayList(ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "touch")), ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "mana_link"))), new NBTTagCompound());

	public EntityAIAllyManaLink(EntityCreature host){
		this.host = host;
	}

	@Override
	public boolean shouldExecute(){
		boolean isSummon = EntityUtils.isSummon(host);
		if (!isSummon)
			return false;
		EntityPlayer owner = getHostOwner();
		if (owner == null || !SkillData.For(owner).hasSkill(SkillDefs.MAGE_POSSE_2.getID()) || host.getDistanceSqToEntity(host) > 64D || EntityExtension.For(owner).isManaLinkedTo(host))
			return false;
		return true;
	}

	private EntityPlayer getHostOwner(){
		int ownerID = EntityUtils.getOwner(host);
		Entity owner = host.world.getEntityByID(ownerID);
		if (owner == null || !(owner instanceof EntityPlayer))
			return null;
		return (EntityPlayer)owner;
	}

	@Override
	public void updateTask(){
		EntityPlayer owner = getHostOwner();
		if (owner == null)
			return;
		if (host.getDistanceToEntity(owner) < 1)
			host.getNavigator().tryMoveToXYZ(host.posX, host.posY, host.posZ, 0.5f);
		else
			SpellUtils.applyStackStage(spellStack, host, owner, owner.posX, owner.posY, owner.posZ, null, host.world, false, false, 0);
	}

}
