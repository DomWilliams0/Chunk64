package net.chunk64.chinwe.commands;

import net.chunk64.chinwe.Config;
import net.chunk64.chinwe.PlayerData;
import net.chunk64.chinwe.util.C64Utils;
import net.chunk64.chinwe.util.CommandUtils;
import net.chunk64.chinwe.util.ParticleEffect;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Command_halt implements CommandExecutor
{

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (cmd.getName().equalsIgnoreCase(CommandUtils.getCommandName(this)))
		{
			try
			{
				if (!CommandUtils.canUse(sender, cmd)) return true;

				if (args.length == 1 || args.length == 2)
				{
					Player target = Bukkit.getPlayer(args[0]);
					long current = System.currentTimeMillis();

					if (CommandUtils.isExempt(sender, target, "chunk64.halt.exempt", "halt", true)) return true;

					String[] haltSplit = args.length == 2 ? C64Utils.evaluateTime(args[1]).split(";") : null;
					long unhaltTime = args.length == 2 ? current + Long.parseLong(haltSplit[0]) : -1;

					// Invalid time given
					if (unhaltTime == current) throw new IllegalArgumentException("&cInvalid time given! Please use &oxy&c, where y is a time modifier: " + C64Utils.timeHelp);

					boolean offline = target == null;

					PlayerData pd = PlayerData.getData(offline ? args[0] : target.getName());

					if (!CommandUtils.isValidData(sender, pd)) return true;

					if (!offline)
					{
						ParticleEffect.sendToLocation(ParticleEffect.EXPLODE, target.getLocation(), 1, 1, 1, 1, 80);
						target.getWorld().playEffect(target.getLocation(), Effect.STEP_SOUND, 7);
					}

					// Unhalt
					if (pd.isHalted())
					{
						pd.setHaltLocation(null);
						pd.setUnhaltTime(-1);
						if (Config.WarnOnHalt && !offline) C64Utils.message(target, "&bYou were &6unhalted&b by &6" + sender.getName() + "&b.");
						C64Utils.message(sender, "&bYou unhalted &6" + pd.getName() + "&b.");
						C64Utils.notifyOps(String.format("%s unhalted %s.", sender.getName(), pd.getName()));
					}
					// Halt
					else
					{
						if (offline && !(sender instanceof Player)) throw new IllegalArgumentException("&cYou must be a player to halt offline players!");
						pd.setHaltLocation(offline ? ((Player) sender).getLocation() : target.getLocation());
						pd.setUnhaltTime(unhaltTime);
						String time = haltSplit != null ? " for &6" + haltSplit[1] + "&b." : ".";

						if (Config.WarnOnHalt && !offline) C64Utils.message(target, "&bYou were &6halted&b by &6" + sender.getName() + "&b" + time);
						C64Utils.message(sender, "&bYou halted &6" + pd.getName() + "&b" + time);
						C64Utils.notifyOps(String.format("%s halted %s%s", sender.getName(), pd.getName(), time));
					}

					pd.save();
					if (offline) pd.unload();
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
