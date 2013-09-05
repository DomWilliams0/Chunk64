package net.chunk64.chinwe.commands;

import net.chunk64.chinwe.util.C64Utils;
import net.chunk64.chinwe.util.CommandUtils;
import net.chunk64.chinwe.util.ImprovedOfflinePlayer;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Command_offline implements CommandExecutor
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
					C64Utils.message(sender, "&cAll these commands correspond to &ooffline&c players!");
					CommandUtils.sendHelp(sender, "Offline Help", label,";View this help menu", "gm <player> [0|1|2];Change a player's gamemode", "tp <player>;Teleport to a player", "move <player>;Teleport a player to you");
					return true;
				}

				if (args.length >= 2)
				{

					OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
					if (!p.hasPlayedBefore()) throw new IllegalArgumentException(args[1] + " has not played on this server!");
					ImprovedOfflinePlayer iop = new ImprovedOfflinePlayer(p);

					// Gamemode
					if ((args[0].equalsIgnoreCase("gm") || args[0].equalsIgnoreCase("gamemode")) && args.length <= 3)
					{
						GameMode gm = null;

						if (args.length == 3)
						{
							if (args[2].equalsIgnoreCase("creative") || args[2].equalsIgnoreCase("1"))
								gm = GameMode.CREATIVE;
							else if (args[2].equalsIgnoreCase("survival") || args[2].equalsIgnoreCase("0"))
								gm = GameMode.SURVIVAL;
							else if (args[2].equalsIgnoreCase("adventure") || args[2].equalsIgnoreCase("2"))
								gm = GameMode.ADVENTURE;
							else
								throw new IllegalArgumentException("Please enter 0, 1, 2, survival, creative or adventure!");
						}

						if (gm == null)
						{
							switch (iop.getGameMode())
							{
							case CREATIVE:
								gm = GameMode.ADVENTURE;
								break;
							case SURVIVAL:
								gm = GameMode.CREATIVE;
								break;
							case ADVENTURE:
								gm = GameMode.SURVIVAL;
								break;
							}
						}

						iop.setGameMode(gm);

						if (p.isOnline()) p.getPlayer().setGameMode(gm);

						C64Utils.message(sender, "&bYou set the gamemode of &6" + iop.getName() + "&b to &6" + gm.toString().toLowerCase());
						return true;
					}

					// Tp and move
					if ((args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("teleport") || args[0].equalsIgnoreCase("move")) && args.length == 2)
					{
						if (!(sender instanceof Player))
						{
							C64Utils.message(sender, "&c" + CommandUtils.PLAYER_ONLY);
							return true;
						}
						boolean move = args[0].equalsIgnoreCase("move");
						Player senderp = (Player) sender;

						Location loc = p.isOnline() ? move ? senderp.getLocation() : p.getPlayer().getLocation() : move ? senderp.getLocation() : iop.getLocation();
						String l = C64Utils.formatLocation(loc) + (p.isOnline() ? "&b anyway!" : "");

						if (p.isOnline())
						{
							if (move)
								p.getPlayer().teleport(senderp.getLocation());
							else
								senderp.teleport(p.getPlayer().getLocation());
						}
						else
						{
							if (move)
								iop.setLocation(senderp.getLocation());
							else
								senderp.teleport(iop.getLocation());
						}

						if (p.isOnline()) C64Utils.message(sender, "&6" + p.getName() + "&b is online!");
						if (move)
							C64Utils.message(sender, "&bTeleporting &6" + p.getName() + "&b to you at &6" + l);
						else
							C64Utils.message(sender, "&bTeleporting you to &6" + p.getName() + "&b at &6" + l);
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
