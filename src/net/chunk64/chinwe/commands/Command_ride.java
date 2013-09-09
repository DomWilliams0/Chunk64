package net.chunk64.chinwe.commands;

import net.chunk64.chinwe.util.C64Utils;
import net.chunk64.chinwe.util.CommandUtils;
import net.chunk64.chinwe.util.Passengers;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Command_ride implements CommandExecutor
{

	public static List<String> toolUsers = new ArrayList<String>();
	public static HashMap<String, ArrayList<Integer>> entitiesToStack = new HashMap<String, ArrayList<Integer>>();

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (cmd.getName().equalsIgnoreCase(CommandUtils.getCommandName(this)))
		{
			try
			{
				if (!CommandUtils.canUse(sender, cmd))
					return true;
				// TODO Eject command, eject <player> , /eject yourself (whole stack), or click

				// Help
				if (args.length == 0)
				{
					CommandUtils.sendHelp(sender, "Riding Help", label, ";View this help menu", "<player>;Ride another player", "toggle;Toggle the riding tools", "eject [player];Eject yourself, or another player");
					return true;
				}

				Player psender = (Player) sender;
				if (args.length == 1)
				{
					Player target = Bukkit.getPlayer(args[0]);
					if (target != null)
					{
						// Ride another player
						psender.teleport(target);
						Passengers.addPassenger(psender, target);
						C64Utils.message(sender, "&bYou are now riding &6" + target.getName() + "&b!");
						C64Utils.message(target, "&6" + psender.getName() + "&b is now riding you!");
						return true;
					}

					// Tools
					if (args[0].equalsIgnoreCase("toggle"))
					{
						if (toolUsers.contains(sender.getName())) toolUsers.remove(sender.getName());
						else toolUsers.add(sender.getName());

						C64Utils.message(sender, "&bSaddle riding " + (toolUsers.contains(sender.getName()) ? "enabled! " +
								"\n&6Using a saddle: &7Right click entities to ride them, left click them to make them ride you" +
								"\n&6Using a stick: &7Left click entities to prise them off, right click to eject them all" +
								"\n&6Using string: &7Left click entities to select them, right click an entity to stack them all on!" : "disabled!"));
						return true;

					}


				}

				if (args[0].equalsIgnoreCase("eject"))
				{
					Player target = args.length == 1 ? psender : Bukkit.getPlayer(args[1]);
					if (target == null)
						throw new IllegalArgumentException(target.getName() + " is not online!");

					if (target.getVehicle() == null)
						throw new IllegalArgumentException((target == psender ? "You are" : target.getName() + " is") + " not riding anything!");

					Passengers.eject(target);
					C64Utils.message(sender, "&bYou ejected " + (target == psender ? "yourself!" : "&6" + target.getName()));
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
			CommandUtils.sendUsage(sender, cmd, true);
			return true;
		}

		return true;
	}

}
