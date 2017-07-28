package am2.items;

import java.util.List;

import am2.utils.EntityUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

@SuppressWarnings("deprecation")
public class ItemJournal extends ItemArsMagica{

	private static final String KEY_NBT_XP = "Stored_XP";
	private static final String KEY_NBT_OWNER = "Owner";

	public ItemJournal(){
		super();
	}
	
	@Override
	public boolean hasEffect(ItemStack par1ItemStack){
		return true;
	}

	@Override
	public boolean getShareTag(){
		return true;
	}

	@Override
	public void addInformation(ItemStack journal, EntityPlayer player, List<String> list, boolean par4){
		String owner = getOwner(journal);
		if (owner == null){
			list.add(I18n.translateToLocal("am2.tooltip.unowned"));
			list.add(I18n.translateToLocal("am2.tooltip.journalUse"));
			return;
		}else{
			list.add(String.format(I18n.translateToLocal("am2.tooltip.journalOwner")));
			list.add(String.format(I18n.translateToLocal("am2.tooltip.journalOwner2"), owner));
		}

		if (owner.equals(player.getName()))
			list.add(String.format(I18n.translateToLocal("am2.tooltip.containedXP"), getXPInJournal(journal)));

		if (owner == null || owner.equals(player.getName()))
			list.add(I18n.translateToLocal("am2.tooltip.journalUse"));
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand){
		ItemStack journal = player.getHeldItem(hand);
		if (!player.world.isRemote){
			if (getOwner(journal) == null){
				setOwner(journal, player);
			}else if (!getOwner(journal).equals(player.getName())){
			  player.sendMessage(new TextComponentString(I18n.translateToLocal("am2.tooltip.notYourJournal")));
				return super.onItemRightClick(world, player, hand);
			}

			if (player.isSneaking()){
				int removedXP = EntityUtils.deductXP(10, player);
				addXPToJournal(journal, removedXP);
			}else{
				int amt = Math.min(getXPInJournal(journal), 10);
				if (amt > 0){
					player.addExperience(amt);
					deductXPFromJournal(journal, amt);
				}
			}
		}

		return super.onItemRightClick(world, player, hand);
	}

	private void addXPToJournal(ItemStack journal, int amount){
		if (!journal.hasTagCompound())
			journal.setTagCompound(new NBTTagCompound());
		journal.getTagCompound().setInteger(KEY_NBT_XP, journal.getTagCompound().getInteger(KEY_NBT_XP) + amount);
	}

	private void deductXPFromJournal(ItemStack journal, int amount){
		addXPToJournal(journal, -amount);
	}

	private int getXPInJournal(ItemStack journal){
		if (!journal.hasTagCompound())
			return 0;
		return journal.getTagCompound().getInteger(KEY_NBT_XP);
	}

	private String getOwner(ItemStack journal){
		if (!journal.hasTagCompound())
			return null;
		return journal.getTagCompound().getString(KEY_NBT_OWNER);
	}

	private void setOwner(ItemStack journal, EntityPlayer player){
		if (!journal.hasTagCompound())
			journal.setTagCompound(new NBTTagCompound());
		journal.getTagCompound().setString(KEY_NBT_OWNER, player.getName());
	}

}
