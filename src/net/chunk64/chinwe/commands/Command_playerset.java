package net.chunk64.chinwe.commands;

import net.chunk64.chinwe.Config;
import net.chunk64.chinwe.util.C64Utils;
import net.chunk64.chinwe.util.CommandUtils;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Command_playerset implements CommandExecutor
{

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (cmd.getName().equalsIgnoreCase(CommandUtils.getCommandName(this)))
		{
			try
			{
				if (!CommandUtils.canUse(sender, cmd)) return true;

				// Help
				if (args.length == 0)
				{
					CommandUtils.sendHelp(sender, "PlayerSet Help", label, ";View this help menu", "health <new> [player];Set health", "hunger <new> [player];Set hunger", "level <new> [player];Set exp level", "speed <new> [player];Set walk/fly speed");
					return true;
				}

				if (args.length == 2 && !(sender instanceof Player)) throw new IllegalArgumentException("You must specify a player!");

				Player p = args.length == 3 ? Bukkit.getPlayer(args[2]) : (Player) sender;

				if (!C64Utils.isDouble(args[1])) throw new IllegalArgumentException("&c\"" + args[1] + "\" is not a number!");

				double newValue = Double.parseDouble(args[1]);

				// Health
				if (args[0].equalsIgnoreCase("health"))
				{
					if (newValue < 0 || newValue > 20) throw new IllegalArgumentException("Health must be between 0 and 20!");
					p.setHealth(newValue);
				}

				// Hunger
				if (args[0].equalsIgnoreCase("hunger"))
				{
					if (newValue < 0 || newValue > 32767) throw new IllegalArgumentException("Hunger must be between 0 and 32767!");
					p.setFoodLevel((int) newValue);
				}

				// Level
				if (args[0].equalsIgnoreCase("level"))
				{
					if (newValue < 0 || newValue > 32767) throw new IllegalArgumentException("Exp levels must be between 0 and 32767!");
					p.setLevel((int) newValue);
					args[0] = "exp level";
				}

				// Speed
				if (args[0].equalsIgnoreCase("speed"))
				{
					if (newValue == 0)
					{
						if (!Config.Owners.contains(sender.getName()) && sender instanceof Player) throw new IllegalArgumentException("Only the owner(s) can set speed to 0!");
						if (Config.Owners.contains(p.getName()) && sender instanceof Player) throw new IllegalArgumentException("You can't set an owner's speed to 0!");
					}

					p.setWalkSpeed((float) newValue);
					p.setFlySpeed((float) newValue);
				}

				String who = p == sender ? "your own " : "&6" + p.getName() + "&b's ";

				C64Utils.message(sender, "&bYou set " + who + args[0].toLowerCase() + "&b to &6" + newValue);
				return true;

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
