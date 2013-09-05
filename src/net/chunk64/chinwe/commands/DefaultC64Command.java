package net.chunk64.chinwe.commands;

import net.chunk64.chinwe.util.C64Utils;
import net.chunk64.chinwe.util.CommandUtils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DefaultC64Command implements CommandExecutor
{

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (cmd.getName().equalsIgnoreCase(CommandUtils.getCommandName(this)))
		{
			try
			{
				if (!CommandUtils.canUse(sender, cmd)) return true;
				
			} catch (IllegalArgumentException e1)
			{
				C64Utils.error(sender, e1);
				C64Utils.error(sender, e1);
				return true;
			} catch (Exception e)
			{
				C64Utils.debugMessage("Error in command \"" + label + "\": " + e);
			}
			CommandUtils.sendUsage(sender, cmd, false);
			return true;
		}

		return true;
	}

}
