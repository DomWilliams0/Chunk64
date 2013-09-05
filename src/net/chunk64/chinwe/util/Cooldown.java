package net.chunk64.chinwe.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Cooldown
{
	private static Set<PlayerCooldown> cooldowns = new HashSet<PlayerCooldown>();

	public static void addCooldown(String cooldownName, String player, long lengthInMillis)
	{
		PlayerCooldown pc = new PlayerCooldown(cooldownName, player, lengthInMillis);
		Iterator<PlayerCooldown> it = cooldowns.iterator();
		// This section prevents duplicate cooldowns
		while (it.hasNext())
		{
			PlayerCooldown iterated = it.next();
			if (iterated.getPlayerName().equalsIgnoreCase(pc.getPlayerName()))
			{
				if (iterated.getCooldownName().equalsIgnoreCase(pc.getCooldownName()))
				{
					it.remove();
				}
			}
		}
		cooldowns.add(pc);
	}

	public static PlayerCooldown getCooldown(String cooldownName, String playerName)
	{
		Iterator<PlayerCooldown> it = cooldowns.iterator();
		while (it.hasNext())
		{
			PlayerCooldown pc = it.next();
			if (pc.getCooldownName().equalsIgnoreCase(cooldownName))
			{
				if (pc.getPlayerName().equalsIgnoreCase(playerName)) { return pc; }
			}
		}
		return null;
	}

}

class PlayerCooldown
{

	private long startTime;
	private String playerName;
	private String cooldownName;
	private long lengthInMillis;
	private long endTime;

	PlayerCooldown(String cooldownName, String player, long lengthInMillis)
	{
		this.cooldownName = cooldownName;
		this.startTime = System.currentTimeMillis();
		this.playerName = player;
		this.lengthInMillis = lengthInMillis;
		this.endTime = startTime + this.lengthInMillis;
	}

	@SuppressWarnings("unused")
	private PlayerCooldown()
	{
	}

	public boolean isOver()
	{
		return endTime < System.currentTimeMillis();
	}

	public int getTimeLeft()
	{
		return (int) (endTime - System.currentTimeMillis());
	}

	public String getPlayerName()
	{
		return playerName;
	}

	public String getCooldownName()
	{
		return cooldownName;
	}

	public void reset()
	{
		startTime = System.currentTimeMillis();
		endTime = startTime + lengthInMillis;
	}
}
