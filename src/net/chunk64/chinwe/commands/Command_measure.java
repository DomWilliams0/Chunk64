package net.chunk64.chinwe.commands;

import net.chunk64.chinwe.PlayerData;
import net.chunk64.chinwe.util.C64Utils;
import net.chunk64.chinwe.util.CommandUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class Command_measure implements CommandExecutor
{

	public static Map<String, Location[]> measurePoints = new HashMap<String, Location[]>();
	// TODO Tell player id of tool

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (cmd.getName().equalsIgnoreCase(CommandUtils.getCommandName(this)))
		{
			try
			{
				if (!CommandUtils.canUse(sender, cmd))
					return true;

				PlayerData pd = PlayerData.getData(sender.getName());
				String status = null;
				int original = pd.getMeasureMode();

				if (args.length == 0)
				{
					pd.setMeasureMode(pd.getMeasureMode() == 0 ? 1 : 0);
					status = pd.getMeasureMode() == 0 ? "disabled" : "XYZ mode";
				}
				if (args.length == 1 && args[0].equalsIgnoreCase("mode"))
				{
					int newMode = pd.getMeasureMode() == 1 ? 2 : 1;
					pd.setMeasureMode(newMode);
					status = newMode == 1 ? "XYZ mode" : "distance mode";
				}

				pd.save();

				if (status != null)
				{
					C64Utils.message(sender, "&bMeasure stick " + (pd.getMeasureMode() == 0 ? "&6disabled&b!" : "set to &6" + status + (original == 0 ? "&b and enabled" : "") + "&b!"));
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

	/**
	 * Adds the given point to a player's measure list
	 *
	 * @param name      Player name
	 * @param l         Location to add
	 * @param leftClick If click was left click
	 * @return True if both points are set, false otherwise
	 */
	public static boolean setPoint(String name, Location l, boolean leftClick) throws IllegalArgumentException
	{

		Location[] locs = measurePoints.containsKey(name) ? measurePoints.get(name) : new Location[2];

		if (leftClick)
		{
			locs[0] = l;
			locs[1] = null; // reset second point
		} else locs[1] = l;

		measurePoints.put(name, locs);

		if (locs[0] != null && locs[1] != null)
		{
			if (!locs[0].getWorld().getName().equals(locs[1].getWorld().getName()))
				throw new IllegalArgumentException("Both locations must be in the same world!");
			return true;
		}

		return false;
	}

	public static void getDistance(CommandSender sender)
	{
		if (!measurePoints.containsKey(sender.getName()))
			return;
		PlayerData pd = PlayerData.getData(sender.getName());
		if (pd == null)
			return;

		Location[] locs = measurePoints.get(sender.getName());

		if (pd.getMeasureMode() == 1)
		{
			//			C64Utils.message(sender, "&bX: &6" + Math.abs((locs[0].getBlockX() - locs[1].getBlockX())));
			//			C64Utils.message(sender, "&bY: &6" + Math.abs((locs[0].getBlockY() - locs[1].getBlockY())));
			//			C64Utils.message(sender, "&bZ: &6" + Math.abs((locs[0].getBlockZ() - locs[1].getBlockZ())));
			C64Utils.message(sender, "&bX: &6" + (locs[0].getBlockX() - locs[1].getBlockX()));
			C64Utils.message(sender, "&bY: &6" + (locs[0].getBlockY() - locs[1].getBlockY()));
			C64Utils.message(sender, "&bZ: &6" + (locs[0].getBlockZ() - locs[1].getBlockZ()));
			C64Utils.message(sender, "&8---");
		} else C64Utils.message(sender, "&bDistance: &6" + locs[0].distance(locs[1]));

	}

}
