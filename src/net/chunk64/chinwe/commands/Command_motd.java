package net.chunk64.chinwe.commands;

import net.chunk64.chinwe.Chunk64;
import net.chunk64.chinwe.MotdManager;
import net.chunk64.chinwe.util.C64Utils;
import net.chunk64.chinwe.util.CommandUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class Command_motd implements CommandExecutor
{

	@Override
	public boolean onCommand (CommandSender sender, Command cmd, String label, String[] args)
	{
		if (cmd.getName().equalsIgnoreCase(CommandUtils.getCommandName(this)))
		{
			try
			{
				if (!CommandUtils.canUse(sender, cmd)) return true;

				// Help
				if (args.length == 0)
				{
					CommandUtils.sendHelp(sender, "MOTD Help", label, ";View this help menu", "add <motd>;Add a MOTD", "list;List all MOTDs", "del <index>;Delete the specified MOTD", "clear;Delete all MOTDs","temp;Clear any temporary MOTD", "temp <motd>;Set a temporary, override MOTD;Only this MOTD will be showed");
					return true;
				}


				MotdManager m = Chunk64.motdManager;

				// List
				if (args.length == 1 && args[0].equalsIgnoreCase("list"))
				{
					if (m.getMotds().size() == 0)
						throw new IllegalArgumentException("There are no MOTDs to list!");

					C64Utils.message(sender, "&bShowing &6" + m.getMotds().size() + " &bMOTD" + (m.getMotds().size() == 1 ? "" : "s") + ": ");
					for (String s : m.getMotds())
						C64Utils.message(sender, "&6 - " + MotdManager.getIndex(s) + " &r" + s);

					return true;

				}

				// Add
				if (args.length >= 1 && args[0].equalsIgnoreCase("add"))
				{
					if (args.length == 1) throw new IllegalArgumentException("Enter a MOTD!");

					String motd = StringUtils.join(args, " ", 1, args.length);

					m.addMotd(motd);

					C64Utils.message(sender, "&bAdded MOTD " + MotdManager.getIndex(motd) + "&b: &r" + C64Utils.limitString(motd));
					C64Utils.notifyOps(sender.getName() + " added MOTD: " + C64Utils.limitString(motd), sender.getName());
					return true;

				}

				// Delete
				if (args.length == 2 && args[0].equalsIgnoreCase("del"))
				{
					if (!C64Utils.isInteger(args[1]))
						throw new IllegalArgumentException("Please enter a number!");

					int index = Integer.parseInt(args[1]);

					if (index >= m.getMotds().size() || index < 0)
						throw new IllegalArgumentException("A MOTD with that index does not exist!");

					String motd = m.getMotd(index, true);
					if (motd == null) throw new IllegalArgumentException("Unspecified");
					m.deleteMotd(motd);

					C64Utils.message(sender, "&bDeleted MOTD &8[&3" + index + "&8]&b: &r&m" + C64Utils.limitString(motd));
					C64Utils.notifyOps(sender.getName() + " deleted MOTD: " + C64Utils.limitString(motd), sender.getName());

					return true;
				}

				// Clear
				if (args.length == 1 && args[0].equalsIgnoreCase("clear"))
				{
					int a = m.getMotds().size();
					if (a == 0) throw new IllegalArgumentException("There are no MOTDs to clear!");

					m.setMotds(new ArrayList<String>());
					C64Utils.message(sender, "&bCleared all &6" + a + "&b MOTDs.");
					C64Utils.notifyOps(sender.getName() + " cleared all " + a + " MOTDs", sender.getName());
					return true;
				}

				// Temp
				if (args.length >= 1 && args[0].equalsIgnoreCase("temp"))
				{
					String override = args.length > 1 ? StringUtils.join(args, " ", 1, args.length) : null;

					m.setOverride(override);

					C64Utils.message(sender, "&bOverride " + (override == null ? "cleared." : "set to: &r" + C64Utils.limitString(override)));
					if (override == null) C64Utils.notifyOps(sender.getName() + " cleared the override MOTD", sender.getName());
					else C64Utils.notifyOps(sender.getName() + " set the the override MOTD to " + C64Utils.limitString(override), sender.getName());
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


