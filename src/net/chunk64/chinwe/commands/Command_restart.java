package net.chunk64.chinwe.commands;

import net.chunk64.chinwe.Config;
import net.chunk64.chinwe.util.C64Utils;
import net.chunk64.chinwe.util.CommandUtils;
import net.chunk64.chinwe.util.RestartUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.File;

public class Command_restart implements CommandExecutor
{

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (cmd.getName().equalsIgnoreCase(CommandUtils.getCommandName(this)))
		{
			try
			{
				if (!CommandUtils.canUse(sender, cmd)) return true;

				File startupFile = new File(Config.StartupFileName);
				if (Config.StartupFileName.equals("none"))
					throw new IllegalArgumentException("Restarting is disabled!");
				if (!startupFile.exists())
					throw new IllegalArgumentException("Could not find the startup script at \"" + Config.StartupFileName + "\"");


				String reason = args.length == 0 ? ChatColor.AQUA + "Server restarting!" : StringUtils.join(args, " ");

				C64Utils.message(sender, "&bRestarting the server!");
				RestartUtil.restart(sender, reason);
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
		}

		return true;
	}

}
