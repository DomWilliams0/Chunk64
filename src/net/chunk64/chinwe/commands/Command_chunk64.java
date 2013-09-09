package net.chunk64.chinwe.commands;

import net.chunk64.chinwe.Chunk64;
import net.chunk64.chinwe.util.C64Utils;
import net.chunk64.chinwe.util.CommandUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Command_chunk64 implements CommandExecutor
{

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (cmd.getName().equalsIgnoreCase(CommandUtils.getCommandName(this)))
		{
			try
			{
				if (!CommandUtils.canUse(sender, cmd))
					return true;


				if (args.length == 0)
				{
					C64Utils.message(sender, "&6" + Chunk64.c64.getDescription().getFullName() + "&b created by &6" + Chunk64.c64.getDescription().getAuthors().get(0) + " &8-&7 www.chunk64.net");
					C64Utils.message(sender, "&bThere are &6" + Chunk64.commandUsers.size() + "&b commands registered, with &6" + Chunk64.playerData.size() + "&b player" + (Chunk64.playerData.size() == 1 ? "" : "s") + " loaded.");
					C64Utils.message(sender, "&bUse &6/help " + Chunk64.c64.getName().toLowerCase() + "&b to view the commands.");
					return true;
					// TODO Reload command
				}


			} catch (IllegalArgumentException e1)
			{
				C64Utils.error(sender, e1);
				return true;
			} catch (Exception e)
			{
				C64Utils.debugMessage("Error in command \"" + label + "\": " + e);
			}
			CommandUtils.sendUsage(sender, cmd, true);
			return true;
		}

		return true;
	}

}
