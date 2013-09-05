package net.chunk64.chinwe.util;

import net.chunk64.chinwe.Config;
import net.minecraft.server.v1_6_R2.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;

import static org.bukkit.ChatColor.*;

public class RestartUtil
{

	public static void restart (CommandSender sender, String message) throws IllegalArgumentException
	{
		try
		{
			String startupScript = Config.StartupFileName;
			final File file = new File(startupScript);
			if (file.isFile())
			{
				for (Player p : Bukkit.getServer().getOnlinePlayers())
				{
					p.kickPlayer(message);
				}

				try
				{
					Thread.sleep(100);
				} catch (InterruptedException ex)
				{
				}

				MinecraftServer.getServer().ag().a();

				try
				{
					Thread.sleep(100);
				} catch (InterruptedException ex)
				{
				}

				try
				{
					MinecraftServer.getServer().stop();
				} catch (Throwable t)
				{
				}

				Thread shutdownHook = new Thread()
				{
					public void run ()
					{
						try
						{
							String os = System.getProperty("os.name").toLowerCase();
							if (os.contains("win"))
							{
								Runtime.getRuntime().exec("cmd /c start " + file.getPath());
							} else
							{
								Runtime.getRuntime().exec(new String[]{"sh", file.getPath()});
							}
						} catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				};

				shutdownHook.setDaemon(true);
				Runtime.getRuntime().addShutdownHook(shutdownHook);
			} else
			{
			if (sender != null) C64Utils.message(sender, "Could not find the startup script " + file.getName() + "! Stopping server...");
			C64Utils.severe("Could not find the startup script " + file.getName() + "! Stopping server...");
			}
			System.exit(0);
		} catch (Exception ex)
		{
		}
	}

	public static void restartDefaultMessage ()
	{
		restart(null, BOLD.toString() + RED + "Server restarting! " + BOLD.toString() + AQUA + "Please rejoin!");
	}

}