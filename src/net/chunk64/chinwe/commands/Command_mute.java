package net.chunk64.chinwe.commands;

import net.chunk64.chinwe.PlayerData;
import net.chunk64.chinwe.util.C64Utils;
import net.chunk64.chinwe.util.CommandUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Command_mute implements CommandExecutor
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

				if (args.length == 1 || args.length == 2)
				{

					PlayerData pd = PlayerData.getData(args[0]);
					if (!CommandUtils.isValidData(sender, pd))
						return true;
					boolean online = pd.isOnline();

					long current = System.currentTimeMillis();
					if (online && CommandUtils.isExempt(sender, pd.getPlayer(), "chunk64.mute.exempt", "mute", true))
						return true;

					String[] timeSplit = args.length == 2 ? C64Utils.evaluateTime(args[1]).split(";") : null;
					long unmuteTime = timeSplit != null ? current + Long.parseLong(timeSplit[0]) : 1;

					if (unmuteTime == current)
						throw new IllegalArgumentException("&cInvalid time given! Please use &oxy&c, where y is a time modifier: " + C64Utils.timeHelp);

					if (pd.isMuted()) pd.setUnmuteTime(-1);
					else pd.setUnmuteTime(unmuteTime);

					String status = pd.isMuted() ? "muted" : "unmuted";
					String time = timeSplit != null && pd.isMuted() ? "&b for &6" + timeSplit[1] + "&b." : ".";

					if (online)
						C64Utils.message(pd.getPlayer(), "&bYou were &6" + status + "&b by &6" + sender.getName() + time);
					C64Utils.message(sender, "&bYou " + status + " &6" + pd.getName() + time);
					C64Utils.notifyOps(String.format("%s " + status + " %s%s", sender.getName(), pd.getName(), time));

					pd.save();

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
