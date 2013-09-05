package net.chunk64.chinwe;

import net.chunk64.chinwe.util.C64Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MotdManager
{


	private static List<String> motds;
	private File file = new File(Chunk64.c64.getDataFolder(), "motds.yml");
	private FileConfiguration yml = null;
	private String override;

	public MotdManager ()
	{
		// Create file
		if (!file.exists())
			try
			{
				file.createNewFile();
				C64Utils.warning("motds.yml not found, creating...");

				yml = YamlConfiguration.loadConfiguration(file);
				yml.options().header("This is where all MOTDs are saved");
				yml.set("motds", Arrays.asList(Bukkit.getServer().getMotd()));
				yml.save(file);

			} catch (IOException e)
			{
				C64Utils.severe("Could not create motds.yml");
			}
		else
			C64Utils.info("motds.yml found and loaded.");

		if (yml == null) yml = YamlConfiguration.loadConfiguration(file);

		motds = yml.getStringList("motds");

	}

	public List<String> getMotds ()
	{
		return motds;
	}

	public void setMotds (List<String> motds)
	{
		this.motds = motds;
		yml.set("motds", motds);
	}

	public void addMotd (String s) throws IllegalArgumentException
	{
		List<String> current = getMotds();
		if (!current.contains(s))
		{
			current.add(s);
			setMotds(current);
			save();
		} else
			throw new IllegalArgumentException("That MOTD already exists!");
	}

	public void deleteMotd (String s) throws IllegalArgumentException
	{
		List<String> current = getMotds();
		if (current.contains(s))
		{
			current.remove(s);
			setMotds(current);
			save();
		} else
			throw new IllegalArgumentException("That MOTD does not exist!");

	}

	public String getMotd (int index, boolean elseNull)
	{
		try
		{
			return getMotds().get(index);
		} catch (ArrayIndexOutOfBoundsException e)
		{
			if (getMotds().size() >= 1 && !elseNull) return getMotds().get(0);
			else
				return null;
		}
	}

	public void save ()
	{
		try
		{
			yml.save(file);
		} catch (IOException e)
		{
			C64Utils.severe("Could not save motds.yml");
		}
	}

	/**
	 * @param motd MOTD
	 * @return [index] of specified MOTD
	 */
	public static String getIndex (String motd)
	{
		return "&8[&3" + motds.indexOf(motd) + "&8]";
	}

	public String next ()
	{
		return motds.get(Chunk64.random.nextInt(motds.size()));
	}


	public String getOverride ()
	{
		return override;
	}

	public void setOverride (String override)
	{
		this.override = override;
	}
}
