package net.chunk64.chinwe.commands;

import net.chunk64.chinwe.util.C64Utils;
import net.chunk64.chinwe.util.CommandUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Command_spoof implements CommandExecutor
{

	@Override
	public boolean onCommand (CommandSender sender, Command cmd, String label, String[] args)
	{
		if (cmd.getName().equalsIgnoreCase(CommandUtils.getCommandName(this)) || cmd.getName().equalsIgnoreCase("spoff"))
		{
			try
			{
				if (!CommandUtils.canUse(sender, cmd)) return true;

				if (args.length >= 2)
				{
					List<CommandSender> p = CommandUtils.getSpoofTarget(sender, args[0]);
					if (p.size() == 0) return true;


					boolean chat = cmd.getName().equalsIgnoreCase("spoff");
					String fullCommand = StringUtils.join(args, " ", 1, args.length);

					// Remove exempts
					Iterator<CommandSender> it = p.iterator();
					List<String> exempts = new ArrayList<String>();
					while (it.hasNext())
					{
						CommandSender s = it.next();
						if (CommandUtils.isExempt(sender, s, "chunk64.spoof.exempt", cmd.getName(), false))
						{
							exempts.add(s.getName());
							it.remove();
							continue;
						}
					}

					if (exempts.size() != 0)
						C64Utils.message(sender, "&c" + C64Utils.formatList(exempts, true) + " cannot be " + cmd.getName() + "ed!");

					if (p.size() == 0) return true;

					boolean all = p.size() > 1;
					for (CommandSender s : p)
					{

						String command = fullCommand.replaceAll("#", s.getName());
						if (s.getName().equalsIgnoreCase("console"))
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), chat ? "say " + command : command);

						else if (s instanceof Player && ((Player) s).isOnline())
							if (chat) ((Player) s).chat(command);

							else
							{
								boolean orig = s.isOp();
								s.setOp(true);
								Bukkit.dispatchCommand(s, command);
								s.setOp(orig);
							}
					}

					C64Utils.notifyOps(sender.getName() + " forced " + (all ? p.size() + " players" : p.get(0).getName()) + " to " + (chat ? "say: " : "run: /") + fullCommand, sender.getName());
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
