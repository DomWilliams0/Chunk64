package net.chunk64.chinwe;

import net.chunk64.chinwe.util.C64Utils;
import org.bukkit.configuration.Configuration;

import java.io.File;
import java.util.List;

public class Config
{
	private Configuration config;

	public static List<String> Owners, MuteCommands;
	public static boolean WarnOnHalt, Debug, ExemptOwners, SendAliases;
	public static int MeasureStick;
	public static String MotdPrefix, StaffChatPrefix, WhitelistMessage, StartupFileName;

	public Config(boolean log)
	{
		File configFile = new File(Chunk64.c64.getDataFolder(), "config.yml");
		config = Chunk64.c64.getConfig().getRoot();
		if (!configFile.exists())
		{
			Chunk64.c64.saveDefaultConfig();
//			if (log) C64Utils.message(plugin.getServer().getConsoleSender(), "config.yml not found, creating...");
			if (log) C64Utils.warning("config.yml not found, creating...");
		}
		else if (log) C64Utils.info("config.yml found and loaded.");

		// Load values
		Owners = config.getStringList("owner-names");
		MuteCommands = config.getStringList("mute-commands");
		WarnOnHalt = config.getBoolean("warn-on-halt");
		Debug = config.getBoolean("debug-mode");
		ExemptOwners = config.getBoolean("exempt-owners");
		SendAliases = config.getBoolean("send-aliases");
		MeasureStick = config.getInt("measure-stick");
		MotdPrefix = config.getString("motd-prefix");
		StaffChatPrefix = config.getString("staffchat-prefix");
		WhitelistMessage = config.getString("whitelist-message");
		StartupFileName = config.getString("startup-file-name");
	}

	// TODO Reload method?

}
