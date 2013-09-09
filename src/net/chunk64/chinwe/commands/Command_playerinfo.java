package net.chunk64.chinwe.commands;

import net.chunk64.chinwe.PlayerData;
import net.chunk64.chinwe.util.C64Utils;
import net.chunk64.chinwe.util.CommandUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Command_playerinfo implements CommandExecutor
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

				// Help
				if (args.length == 0)
				{
					CommandUtils.sendHelp(sender, "PlayerInfo Help", label, ";View this help menu", "<player>;View basic information about a player", "ip <player>;Lookup a player's IPs and possible alts", "time <player>;View a player's total play time");
					return true;
				}

				if (args.length == 1)
				{
					// TODO All player info
					sender.sendMessage("ALL INFO, TODO");
					return true;
				}

				if (args.length == 2)
				{

					PlayerData pd = PlayerData.getData(args[1]);
					if (!CommandUtils.isValidData(sender, pd))
						return true;

					// Ips and alts
					if (args[0].equalsIgnoreCase("ip"))
					{
						C64Utils.message(sender, "&bLoading IPs of &6" + pd.getName().toUpperCase());
						StringBuilder sb = new StringBuilder();
						sb.append("&8| ");
						for (String ip : pd.getIps())
							sb.append("&b" + ip + " &8| ");

						C64Utils.message(sender, sb.toString().trim());

						if (pd.getAlts().size() != 0)
							C64Utils.message(sender, "&cPossible alts: " + C64Utils.formatList(pd.getAlts(), true));

						return true;
					}

					// Times
					if (args[0].equalsIgnoreCase("time"))
					{
						// C64Utils.message(sender, "&b" + pd.getName() +
						// " joined however long ago");
						if (pd.getPlayTime() == 0 && pd.getLoginTime() == 0)
						{
							C64Utils.message(sender, "&6" + pd.getName().toUpperCase() + " &bhas not played for &6even a second&b!");
						} else
							C64Utils.message(sender, "&bIn total, &6" + pd.getName().toUpperCase() + " &bhas played for &6" + C64Utils.formatTime(pd.getLivePlayTime()));
						return true;
					}
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
