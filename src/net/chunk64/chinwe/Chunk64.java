package net.chunk64.chinwe;

import net.chunk64.chinwe.commands.Command_spoof;
import net.chunk64.chinwe.commands.Command_staffchat;
import net.chunk64.chinwe.commands.Command_tools;
import net.chunk64.chinwe.listeners.PlayerListener;
import net.chunk64.chinwe.notes.NotesManager;
import net.chunk64.chinwe.util.C64Utils;
import net.chunk64.chinwe.util.CommandUtils;
import net.chunk64.chinwe.util.MoverManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class Chunk64 extends JavaPlugin
{

	public static Chunk64 c64;
	public static MotdManager motdManager;
	public static InventoryStore store;
	public static NotesManager noteManager;
	public static Random random = new Random();
	public static Map<Command, CommandUser> commandUsers = new HashMap<Command, CommandUser>();
	public static Set<PlayerData> playerData = new HashSet<PlayerData>();
	private boolean disablePlugin = false;

	public static enum CommandUser
	{
		PLAYER_ONLY("player-only"), CONSOLE_ONLY("console-only"), OWNERS_ONLY("owner-only"), ALL("all");

		private String user;

		CommandUser(String user)
		{
			this.user = user;
		}

		public String getUser()
		{
			return user;
		}

		public static CommandUser match(String s)
		{
			if (s == null)
				return ALL;
			if (s.startsWith("p"))
				return PLAYER_ONLY;
			else if (s.startsWith("c"))
				return CONSOLE_ONLY;
			else if (s.startsWith("o"))
				return OWNERS_ONLY;
			else
				return ALL;
		}

	}

	public void onEnable()
	{
		c64 = this;

		// Register commands and listeners
		registerCommands();
		registerListeners();
		if (disablePlugin)
		{
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		Config config = new Config(true);

		motdManager = new MotdManager();
		store = new InventoryStore();
		noteManager = new NotesManager(true);
		noteManager.load();

		// Load all playerdatas
		for (Player p : getServer().getOnlinePlayers())
		{
			PlayerData pd = new PlayerData(p.getName());
			pd.load();
		}
	}


	public void onDisable()
	{
		// Unload all
		for (PlayerData pd : playerData)
			pd.saveOnly();

		playerData.clear();

		// Restore all inventories
		store.restoreAll();

		// Notify all staffchatters
		for (String s : Command_staffchat.staffChatters)
		{
			Player p = Bukkit.getPlayerExact(s);
			if (p != null)
				C64Utils.message(p, "&bStaffChat was disabled due to a reload!");
		}

		// Save notes
		noteManager.save();


	}

	private void registerCommands()
	{
		try
		{
			int count = 0;

			for (String s : getDescription().getCommands().keySet())
			{
				PluginCommand c = getCommand(s);

				// Custom
				if (s.equals("spoff"))
					c.setExecutor(new Command_spoof());
				else if (s.equalsIgnoreCase("staffchattoggle"))
					c.setExecutor(new Command_staffchat());
				else if (s.equals("mover") || s.equals("handyhelper"))
					c.setExecutor(new Command_tools());
				else
					c.setExecutor((CommandExecutor) Class.forName("net.chunk64.chinwe.commands.Command_" + s).newInstance());

				commandUsers.put(c, CommandUser.match(c.getPermissionMessage()));

				c.setPermissionMessage(C64Utils.PREFIX + ChatColor.RED + CommandUtils.getRandomPermissionMessage());

				c.setPermission("chunk64." + c.getName());
				count++;
			}
			C64Utils.info("Registered " + count + " command" + (count == 1 ? "" : "s"));
		} catch (Exception e)
		{
			C64Utils.severe("Could not register commands, disabling plugin");
			disablePlugin = true;
		}

	}

	private void registerListeners()
	{

		try
		{
			getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
			getServer().getPluginManager().registerEvents(new MoverManager(), this);
			getServer().getPluginManager().registerEvents(new Command_tools(), this);

		} catch (Exception e)
		{
			C64Utils.severe("Could not register listeners" + (disablePlugin ? " either" : ", disabling plugin"));
			disablePlugin = true;
		}


	}

}
