package am2.commands;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import am2.extensions.AffinityData;
import am2.extensions.EntityExtension;
import am2.extensions.SkillData;
import am2.lore.ArcaneCompendium;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CommandArsMagica extends CommandBase {

	public CommandArsMagica() {
	}

	@Override
	public String getCommandName() {
		return "am2";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "commands.am2.usage";
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length == 0) throw new WrongUsageException("Wrong usage");
		else if (args[0].equalsIgnoreCase("magiclevel"))
			handleMagicLevelUp(server, sender, args);
		else if (args[0].equalsIgnoreCase("forcesync")) {
			EntityExtension.For(getCommandSenderAsPlayer(sender)).forceUpdate();
			AffinityData.For(getCommandSenderAsPlayer(sender)).forceUpdate();
			SkillData.For(getCommandSenderAsPlayer(sender)).forceUpdate();
			ArcaneCompendium.For(getCommandSenderAsPlayer(sender)).forceUpdate();
			notifyCommandListener(sender, this, "commands.am2.sync.successful", new Object[] {});
		}
	}
	
	private void handleMagicLevelUp(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 2) throw new WrongUsageException("Magic Level requires at least one argument");
		else if (args.length == 2) {
			int level = parseInt(args[1]);
			if (level < 1) level = 1;
            EntityPlayer entityplayer = getCommandSenderAsPlayer(sender);
            EntityExtension.For(entityplayer).setMagicLevelWithMana(level);
            notifyCommandListener(sender, this, "commands.am2.levelup.successful", new Object[] {entityplayer.getDisplayName(), level});
		} else if (args.length == 3) {
			int level = parseInt(args[1]);
			if (level < 1) level = 1;
			Entity ent = getEntity(server, sender, args[2]);
			if (ent instanceof EntityLivingBase) {
				EntityExtension.For((EntityLivingBase) ent).setMagicLevelWithMana(level);
			}
			notifyCommandListener(sender, this, "commands.am2.levelup.successful", new Object[] {ent.getDisplayName(), level});
		} else throw new WrongUsageException("Magic Level has too much arguments");
	}
	
	@Override
	public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
		if (args.length == 1) return getListOfStringsMatchingLastWord(args, Lists.newArrayList("magiclevel", "forcesync"));
		else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("magiclevel")) return Collections.emptyList();
		} else if (args.length == 3) {
			if (args[0].equalsIgnoreCase("magiclevel")) return getListOfStringsMatchingLastWord(args, server.getAllUsernames());
		}
		return Collections.emptyList();
	}

}
