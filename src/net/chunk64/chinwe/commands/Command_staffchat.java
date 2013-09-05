package net.chunk64.chinwe.commands;

import net.chunk64.chinwe.Config;
import net.chunk64.chinwe.util.C64Utils;
import net.chunk64.chinwe.util.CommandUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Command_staffchat implements CommandExecutor
{

	public static List<String> staffChatters = new ArrayList<String>();

	@Override
	public boolean onCommand (CommandSender sender, Command cmd, String label, String[] args)
	{
		if (cmd.getName().equalsIgnoreCase(CommandUtils.getCommandName(this)))
		{
			try
			{
				if (!CommandUtils.canUse(sender, cmd)) return true;


				if (args.length == 0) throw new IllegalArgumentException("Enter a message!");

				String message = StringUtils.join(Arrays.asList(args), " ");

				staffChat(sender, message);
				return true;


			} catch (IllegalArgumentException e1)
			{
				C64Utils.error(sender, e1);
				return true;
			} catch (Exception e)
			{
				C64Utils.debugMessage("Error in command \"" + label + "\": " + e);
			}
			CommandUtils.sendUsage(sender, cmd, false);


			return true;
		} else if (cmd.getName().equalsIgnoreCase(CommandUtils.getCommandName(this) + "toggle"))
		{
			if (staffChatters.contains(sender.getName()))
				staffChatters.remove(sender.getName());
			else
				staffChatters.add(sender.getName());

			C64Utils.message(sender, "&bStaffChat " + (staffChatters.contains(sender.getName()) ? "enabled!" : "disabled!"));
			return true;

		}

		return true;
	}

	public static void staffChat (CommandSender sender, String msg)
	{
		for (Player p : Bukkit.getOnlinePlayers())
			if (C64Utils.hasPermission(p, "chunk64.staffchat") || p.isOp())
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', Config.StaffChatPrefix + sender.getName() + "&3: &a" + msg));

		C64Utils.info("[StaffChat] " + sender.getName() + ": " + msg);
	}

}
