package net.chunk64.chinwe.commands;

import net.chunk64.chinwe.Chunk64;
import net.chunk64.chinwe.util.C64Utils;
import net.chunk64.chinwe.util.CommandUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class Command_plugintoggle implements CommandExecutor
{

	static boolean allDisabled = false;

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (cmd.getName().equalsIgnoreCase(CommandUtils.getCommandName(this)))
		{

			try
			{
				if (!CommandUtils.canUse(sender, cmd))
					return true;

				PluginManager pm = Chunk64.c64.getServer().getPluginManager();

				if (args.length == 1)
				{

					boolean all = args[0].equals("*");

					// Find plugin
					Plugin pl = null;
					if (!all)
					{

						for (Plugin p : pm.getPlugins())
						{
							if (p.getName().toLowerCase().startsWith(args[0].toLowerCase()))
							{
								pl = p;
								break;
							}
						}
					}

					// Invalid plugin
					if (pl == null && !all)
						throw new IllegalArgumentException("&cCannot find a loaded plugin with the name \"" + args[0] + "\"!");

					if (pl != null && pl.getName().equals(Chunk64.c64.getName()))
						throw new IllegalArgumentException("&cCould not disable " + pl.getName() + "!");

					// All
					int count = 0;
					if (pl == null)
					{
						for (Plugin p : pm.getPlugins())
						{
							if (!p.getName().equals(Chunk64.c64.getName()))
							{
								if (!allDisabled) pm.disablePlugin(p);
								else pm.enablePlugin(p);
								count++;
							}
						}
						allDisabled = !allDisabled;
					}
					// Specific
					else
					{
						if (pm.isPluginEnabled(pl)) pm.disablePlugin(pl);
						else pm.enablePlugin(pl);
					}

					String status = pl == null ? allDisabled ? "disabled" : "enabled" : pm.isPluginEnabled(pl) ? "enabled" : "disabled";
					String name = pl == null ? count + " plugins" : pl.getName();
					C64Utils.message(sender, "&bSuccessfully " + status + " &6" + name);
					C64Utils.notifyOps(sender.getName() + " " + status + " " + name);
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
