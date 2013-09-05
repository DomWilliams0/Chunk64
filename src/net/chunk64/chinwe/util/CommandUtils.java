package net.chunk64.chinwe.util;

import net.chunk64.chinwe.Chunk64;
import net.chunk64.chinwe.Chunk64.CommandUser;
import net.chunk64.chinwe.Config;
import net.chunk64.chinwe.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandUtils
{
	private static final List<String> PERMISSION_MESSAGES = new ArrayList<String>();
	public static final String PLAYER_ONLY = "Only players can use that!";
	public static final String CONSOLE_ONLY = "Only the console can use that!";
	public static final String OWNER_ONLY = "Only the owner(s) can use that!";

	static
	{
		PERMISSION_MESSAGES.add("You don't have permission to use that!");
		PERMISSION_MESSAGES.add("You can't use that!!");
		PERMISSION_MESSAGES.add("Sorry, no permission.");
		PERMISSION_MESSAGES.add("No permission sonny!");
		PERMISSION_MESSAGES.add("You need moar permissions to do that.");
		PERMISSION_MESSAGES.add("Attempting that is futile!");
		PERMISSION_MESSAGES.add("You're wasting your time trying to do that.");
	}

	public static boolean canUse (CommandSender sender, Command cmd)
	{
		if (!Chunk64.commandUsers.containsKey(cmd)) return true;

		String msg = null;
		CommandUser user = Chunk64.commandUsers.get(cmd);

		if (user == CommandUser.PLAYER_ONLY || user == CommandUser.ALL)
		{
			if (sender instanceof Player && !sender.hasPermission(cmd.getPermission()))
				msg = getRandomPermissionMessage();
			if (!(sender instanceof Player) && user == CommandUser.PLAYER_ONLY) msg = PLAYER_ONLY;
		}

		if (user == CommandUser.CONSOLE_ONLY)
		{
			if (sender instanceof Player) msg = CONSOLE_ONLY;
		}

		if (user == CommandUser.OWNERS_ONLY)
		{
			// if (!sender.getName().equals("Chinwe")) msg =
			// "Only Chinwe can use that!";
			if (!Config.Owners.contains(sender.getName())) msg = OWNER_ONLY;
		}

		if (msg != null) C64Utils.message(sender, "&c" + msg);

		return msg == null;

	}

	/**
	 * @param sender The command sender
	 * @param s      The string to parse, either a player name or "console"
	 * @return The targeted commandsender, or null if offline
	 */
	public static CommandSender getOnlineTarget (CommandSender sender, String s)
	{
		if (s.equalsIgnoreCase("console")) return sender.getServer().getConsoleSender();
		Player p = Bukkit.getPlayer(s);
		if (p == null) C64Utils.error(sender, "&c" + s + " is not online!");
		return p;
	}

	public static List<CommandSender> getSpoofTarget (CommandSender sender, String s)
	{
		List<CommandSender> list = new ArrayList<CommandSender>();
		if (s.equalsIgnoreCase("console")) list.add(Bukkit.getConsoleSender());

		else if (s.equalsIgnoreCase("*")) for (Player pl : Bukkit.getOnlinePlayers()) list.add(pl);
		else
		{

			Player p = Bukkit.getPlayer(s);
			if (p == null) C64Utils.error(sender, s + " is not online!");
			else list.add(p);
		}

		return list;
	}

	public static void sendUsage (CommandSender sender, Command cmd, boolean helpMenu)
	{
		// if (cmd.getDescription() != null) C64Utils.message(sender,
		// "&cDescription: " + cmd.getDescription());
		if (!helpMenu)
			C64Utils.message(sender, "&cUsage: " + cmd.getUsage().replaceAll("<command>", cmd.getName()));
		else
			C64Utils.message(sender, "&cUse " + cmd.getUsage().replaceAll("<command>", cmd.getName()) + " to view the help menu.");
		if (cmd.getAliases().size() != 0 && Config.SendAliases)
			C64Utils.message(sender, "&cAliases: &o" + C64Utils.formatList(cmd.getAliases(), false));

	}

	public static String getRandomPermissionMessage ()
	{
		return PERMISSION_MESSAGES.get(Chunk64.random.nextInt(PERMISSION_MESSAGES.size()));
	}

	/**
	 * @param sender The CommandSender to send the help to
	 * @param help   command;description;optional point
	 * @param title  The title of the help menu
	 * @param label  The command label to precede
	 */
	public static void sendHelp (CommandSender sender, String title, String label, String... help)
	{
		C64Utils.message(sender, "&6--- &a" + title + " &6---");
		for (String s : help)
		{
			String[] split = s.split(";");
			C64Utils.message(sender, "&6| &b/" + label + " " + split[0] + " &6-&a " + split[1]);
			if (split.length == 3) C64Utils.message(sender, "    &c- &7" + split[2]);
		}
		C64Utils.message(sender, "&6--- &aEnd of help &6---");
	}

	// public static void sendHelp(CommandSender sender, List<String> help,
	// String title)
	// {
	// C64Utils.message(sender, "&6--- &a" + title + " &6---");
	// for (String s : help)
	// {
	// String[] split = s.split(";");
	// C64Utils.message(sender, "&6| &b" + split[0] + " &6-&a " + split[1]);
	// }
	// C64Utils.message(sender, "&6--- &aEnd of help &6---");
	// }

	public static String getCommandName (CommandExecutor executor)
	{
		return executor.getClass().getName().split("[.]")[4].substring(8);
	}

	public static boolean isValidData (CommandSender sender, PlayerData pd)
	{
		if (pd == null) C64Utils.message(sender, C64Utils.INVALID_PLAYERDATA);
		return pd != null;
	}

	/**
	 * Returns if the target is exempt or not, notifies all ops about the
	 * attempted action and punches the player
	 */
	public static boolean isExempt (CommandSender sender, CommandSender target, String exemptPermission, String action, boolean notify)
	{
		boolean bool = target instanceof Player && (C64Utils.hasPermission(target, exemptPermission) || (Config.ExemptOwners && Config.Owners.contains(target.getName())));
		if (bool && notify)
		{
			C64Utils.message(sender, "&cYou cannot " + action + " " + target.getName() + "! To shame!");
			C64Utils.notifyOps(sender.getName() + " tried to " + action + " " + target.getName() + "! To shame!");
			if (sender instanceof Player) ((Player) sender).damage(0.0);
		}
		return bool;
	}

}
