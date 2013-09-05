package net.chunk64.chinwe;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.chunk64.chinwe.util.C64Utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class PlayerData
{
	private static File dir = new File(Chunk64.c64.getDataFolder(), "playerdata");

	private String name;
	private File file;
	private FileConfiguration yml;

	private Location haltLocation;
	private long unhaltTime, playTime, loginTime, unmuteTime;
	private List<String> ips, possibleAlts;
	private int measureMode;

	public PlayerData(String name)
	{
		this.name = name.toLowerCase();

		if (!dir.exists()) dir.mkdirs();

		file = getFile(name);
		yml = YamlConfiguration.loadConfiguration(file);
		yml.options().header("This is the playerdata file for " + name);
		try
		{
			yml.save(file);
		} catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	public static boolean fileExists(String name)
	{
		return getFile(name).exists();
	}

	public void load()
	{
		if (!Chunk64.playerData.contains(this))
		{
			Chunk64.playerData.add(this);
			// else
			// C64Utils.debugMessage("Playerdata for " + name +
			// " was already loaded on loading");

			// TODO Load everything from file
			this.haltLocation = C64Utils.unpackageLocation(yml.getString("locations.halt-location"));
			this.unhaltTime = yml.getLong("timestamps.unhalt-time");
			this.ips = yml.getStringList("ip-addresses");
			this.playTime = yml.getLong("timestamps.play-time");
			this.loginTime = yml.getLong("timestamps.login-time");
			this.possibleAlts = yml.getStringList("possible-alts");
			this.unmuteTime = yml.getLong("timestamps.unmute-time");
			this.setMeasureMode(yml.getInt("measure-mode"));
		}
	}

	public void unload()
	{
		if (Chunk64.playerData.contains(this))
		{
			Chunk64.playerData.remove(this);
			save();
		}
		// else
		// C64Utils.debugMessage("Playerdata for " + name +
		// " was already unloaded on unloading");
	}

	public static boolean isLoaded(String name)
	{
		for (PlayerData pd : Chunk64.playerData)
			if (pd.getName().equals(name)) return true;
		return false;

	}

	/** Gets an online or offline player's PlayerData, otherwise null **/
	public static PlayerData getData(String name)
	{
		if (Bukkit.getPlayer(name) != null) name = Bukkit.getPlayer(name).getName();

		for (PlayerData pd : Chunk64.playerData)
			if (pd.getName().equalsIgnoreCase(name)) return pd;

		for (File f : dir.listFiles())
		{
			if (f.getName().substring(0, f.getName().length() - 4).equals(name.toLowerCase()))
			{
				PlayerData pd = new PlayerData(name);
				pd.load();
				return pd;
			}
		}

		return null;
	}

	private static File getFile(String name)
	{
		File f = new File(dir, name.toLowerCase() + ".yml");
		if (!f.exists()) try
		{
			f.createNewFile();
		} catch (IOException e)
		{
			C64Utils.severe("Could not create " + f.getName());
			e.printStackTrace();
		}
		return f;

	}

	public static List<PlayerData> getAll()
	{
		List<PlayerData> pds = new ArrayList<PlayerData>();
		String name;
		for (File f : dir.listFiles())
		{
			name = f.getName().substring(0, f.getName().length() - 4).toLowerCase();
			pds.add(getData(name));
		}

		return pds;
	}

	public Player getPlayer()
	{
		return Bukkit.getPlayer(name);
	}

	public boolean isOnline()
	{
		return getPlayer() != null;
	}

	public void save()
	{
		try
		{
			yml.save(file);
		} catch (Exception e)
		{
			C64Utils.severe("Could not save playerdata for " + name);
		}
	}

	public String getName()
	{
		return name;
	}

	public Location getHaltLocation()
	{
		// return C64Utils.unpackageLocation(yml.getString("halt-location"));
		return this.haltLocation;
	}

	public void setHaltLocation(Location haltLocation)
	{
		this.haltLocation = haltLocation;
		yml.set("locations.halt-location", C64Utils.packageLocation(haltLocation));
	}

	public boolean isHalted()
	{
		return haltLocation != null;
	}

	public long getUnhaltTime()
	{
		return unhaltTime;
	}

	public void setUnhaltTime(long unhaltTime)
	{
		this.unhaltTime = unhaltTime;
		yml.set("timestamps.unhalt-time", unhaltTime <= 0 ? null : unhaltTime);
	}

	public List<String> getIps()
	{
		return ips;
	}

	public void setIps(List<String> ips)
	{
		this.ips = ips == null ? new ArrayList<String>() : ips;
		yml.set("ip-addresses", ips);
	}

	public void addIp(String ip)
	{
		List<String> current = ips;
		if (!current.contains(ip))
		{
			current.add(ip);
			this.ips = current;
			yml.set("ip-addresses", current);
		}
	}

	public List<String> getAlts()
	{
		return possibleAlts;
	}

	public void setAlts(List<String> alts)
	{
		this.possibleAlts = alts == null ? new ArrayList<String>() : alts;
		yml.set("possible-alts", alts);
	}

	public void addAlt(String alt)
	{
		List<String> current = possibleAlts;
		if (!current.contains(alt))
		{
			current.add(alt);
			this.possibleAlts = current;
			yml.set("possible-alts", current);
		}
	}

	public long getPlayTime()
	{
		return playTime;
	}
	
	public long getLivePlayTime()
	{
		return playTime + (isOnline() ? (System.currentTimeMillis() - getLoginTime()) : 0);
	}

	public void setPlayTime(long playTime)
	{
		this.playTime = playTime;
		yml.set("timestamps.play-time", playTime);
	}

	public long getLoginTime()
	{
		return loginTime;
	}

	public void setLoginTime(long loginTime)
	{
		this.loginTime = loginTime == 0 ? -1 : loginTime;
		yml.set("timestamps.login-time", loginTime <= 0 ? null : loginTime);
	}

	public long getUnmuteTime()
	{
		return unmuteTime;
	}

	public void setUnmuteTime(long unmuteTime)
	{
		this.unmuteTime = unmuteTime;
		yml.set("timestamps.unmute-time", unmuteTime <= 0 ? null : unmuteTime);
	}
	
	public boolean isMuted()
	{
		return unmuteTime > 0;
	}

	/** 0 = disabled, 1 = xyz, 2 = overall distance **/
	public int getMeasureMode()
	{
		return measureMode;
	}

	public void setMeasureMode(int measureMode)
	{
		this.measureMode = measureMode;
		yml.set("measure-mode", measureMode);
	}


}
