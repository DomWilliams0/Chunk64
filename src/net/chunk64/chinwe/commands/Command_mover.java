package net.chunk64.chinwe.commands;

import net.chunk64.chinwe.Config;
import net.chunk64.chinwe.util.C64Utils;
import net.chunk64.chinwe.util.CommandUtils;
import net.chunk64.chinwe.util.MoverManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Command_mover implements CommandExecutor
{

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (cmd.getName().equalsIgnoreCase(CommandUtils.getCommandName(this)))
		{
			try
			{
				if (!CommandUtils.canUse(sender, cmd)) return true;

				if (args.length == 0)
				{
					boolean enabled = MoverManager.movers.contains(sender.getName());
					if (enabled)
					{
						MoverManager.movers.remove(sender.getName());
						MoverManager.removeSelections(sender.getName());
					}
					else MoverManager.movers.add(sender.getName());

					C64Utils.message(sender, "&bMover &6" + (enabled ? "disabled" : "enabled") + "&b!");
					if (!enabled)
					{
						C64Utils.message(sender, "&bRight click blocks with a &6" + Material.getMaterial(Config.MoverTool).toString().toLowerCase().replaceAll("_", " ") + "&b to select them, and left click anywhere to move them\n&7  - Sneak while left clicking to forget that block after moving it");
						C64Utils.message(sender, "&bDo the same with &6mobs&b! Sneak while left clicking to &6remember&b the mob after moving\n&7  - The opposite to moving blocks!");

					}
					return true;
				}


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
