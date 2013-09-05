package net.chunk64.chinwe.util;

import net.chunk64.chinwe.Chunk64;
import net.chunk64.chinwe.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;

public class C64Utils
{

	public static final String PREFIX = ChatColor.translateAlternateColorCodes('&', "&8[&3|&8] &r");
	public static final String ERROR_PREFIX = ChatColor.translateAlternateColorCodes('&', "&8[&c|&8] &r"); // TODO Change error prefix colour
	public static final String INVALID_PLAYERDATA = ChatColor.translateAlternateColorCodes('&', "&cThat player does not exist!");
	public static String timeHelp = "w:weeks d:days h:hours m:minutes s:seconds";

	private static enum Importance
	{
		NORMAL, WARNING, ERROR;
	}


	/**
	 * Logs with level INFO *
	 */
	public static void info (String msg)
	{
		Chunk64.c64.getLogger().log(Level.INFO, ChatColor.translateAlternateColorCodes('&', msg));
	}

	/**
	 * Logs with level WARNING *
	 */
	public static void warning (String msg)
	{
		Chunk64.c64.getLogger().log(Level.WARNING, ChatColor.translateAlternateColorCodes('&', msg));
	}

	/**
	 * Logs with level SEVERE *
	 */
	public static void severe (String msg)
	{
		Chunk64.c64.getLogger().log(Level.SEVERE, ChatColor.translateAlternateColorCodes('&', msg));
	}

	/**
	 * Message CommandSender with coloured message, and default importance
	 */
	public static void message (CommandSender sender, String msg)
	{
		sender.sendMessage(PREFIX + ChatColor.translateAlternateColorCodes('&', msg));
	}

	/**
	 * Message CommandSender with coloured message, and specified importance
	 */
	public static void message (CommandSender sender, String msg, Importance importance)
	{
		sender.sendMessage((importance == Importance.NORMAL ? PREFIX : ERROR_PREFIX) + ChatColor.translateAlternateColorCodes('&', msg));
	}

	/**
	 * Messages CommandSender with exception message
	 */
	public static void error (CommandSender sender, Exception e)
	{
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ERROR_PREFIX + "&4Error: &c" + (e.getMessage() == null ? e : e.getMessage())));
	}

	/**
	 * Messages CommandSender with message
	 */
	public static void error (CommandSender sender, String e)
	{
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + "&4Error: &c" + e));
	}

	/**
	 * Debug message *
	 */
	public static void debugMessage (String msg)
	{
		// TODO Save to debug log file
		if (Config.Debug) Chunk64.c64.getLogger().log(Level.WARNING, "DEBUG: " + msg);
	}

	/**
	 * Notify all ops online (except those specified), and logs message to console
	 */
	public static void notifyOps (String msg, String... except)
	{
		// TODO Log to console too

		msg = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', msg));

		List<String> excepts = Arrays.asList(except);
		for (Player p : Bukkit.getOnlinePlayers())
			if (p.isOp() && !excepts.contains(p.getName())) message(p, "&7&o" + ChatColor.stripColor(msg));

		if (!excepts.contains("CONSOLE")) C64Utils.info(msg);
	}

	/**
	 * @param s Time in format XwXdXhXmXs
	 * @return total milliseconds;time in words
	 */
	public static String evaluateTime (String s)
	{
		List<String> list = new ArrayList<String>();

		String c;
		int goBack = 0;
		for (int i = 0; i < s.length(); i++)
		{
			c = String.valueOf(s.charAt(i));
			if (c.matches("[a-zA-Z]"))
			{
				list.add(s.substring(goBack, i + 1));
				goBack = i + 1;

			}
		}

		// Cleanse
		long amount;
		long total = 0;
		String ch;
		String[] str = new String[5];
		for (String st : list)
		{
			ch = st.substring(st.length() - 1);
			if (st.length() != 1 && ch.matches("[M,w,d,h,m,s]"))
			{
				// Total milliseconds
				amount = Integer.parseInt(st.substring(0, st.length() - 1));
				switch (ch)
				{
					case "s":
						total += (amount * 1000);
						str[4] = amount + " seconds";
						break;
					case "m":
						total += (amount * 1000 * 60);
						str[3] = amount + " minutes";
						break;
					case "h":
						total += (amount * 1000 * 3600);
						str[2] = amount + " hours";
						break;
					case "d":
						total += (amount * 1000 * 3600 * 24);
						str[1] = amount + " days";
						break;
					case "w":
						total += (amount * 1000 * 3600 * 24 * 7);
						str[0] = amount + " weeks";
						break;
				}

			}
		}

		// Append
		List<String> append = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		for (String st : str)
		{
			if (st != null) append.add(st);
		}
		for (int i = 0; i < append.size(); i++)
		{
			String st = append.get(i);
			if (i == append.size() - 1)
				sb.append(st);
			else if (i == append.size() - 2)
				sb.append(st + " and ");
			else
				sb.append(st + ", ");

		}

		return total + ";" + sb.toString();
	}

	/**
	 * Returns time in xw-d-h-m-s *
	 */
	public static String formatTime (long ms)
	{

		String[] units = new String[5];

		units[4] = ms / 1000 % 60 + "s";
		units[3] = ms / (60 * 1000) % 60 + "m";
		units[2] = ms / (60 * 60 * 1000) % 24 + "h";
		units[1] = ms / (24 * 60 * 60 * 1000) % 7 + "d";
		units[0] = ms / (7 * 24 * 60 * 60 * 1000) + "w";

		StringBuilder sb = new StringBuilder();
		for (String s : units)
		{
			if (!s.startsWith("0"))
			{
				sb.append(s);
				if (!s.equals(units[units.length - 1])) sb.append("-");
			}

		}

		return sb.toString().trim();
	}

	/**
	 * Formats a location to world,x,y,z for saving *
	 */
	public static String packageLocation (Location l)
	{
		if (l == null) return null;
		return l.getWorld().getName() + "," + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
	}

	/**
	 * Returns a location from a location in the format world,x,y,z *
	 */
	public static Location unpackageLocation (String s)
	{
		if (s == null) return null;
		String[] split = s.split(",");
		return new Location(Bukkit.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]));
	}

	public static String formatList (List<String> list, boolean and)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < list.size(); i++)
		{
			String st = list.get(i);
			if (i == list.size() - 1)
				sb.append(st);
			else if (i == list.size() - 2)
				sb.append(st + (and ? " and " : " or "));
			else
				sb.append(st + ", ");

		}
		return sb.toString();
	}

	/**
	 * Formats location to x, y, z in "world" *
	 */
	public static String formatLocation (Location l)
	{
		return l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ() + " in \"" + l.getWorld().getName() + "\"";
	}

	public static boolean hasPermission (CommandSender p, String string)
	{
		Permission perm = new Permission(string, PermissionDefault.FALSE);
		return p.hasPermission(perm);
	}

	public static boolean isDouble (String s)
	{
		try
		{
			Double.parseDouble(s);
			return true;
		} catch (NumberFormatException e)
		{
			return false;
		}

	}

	public static boolean isInteger (String s)
	{
		try
		{
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException e)
		{
			return false;
		}

	}


	public static String limitString (String s, int maxLength)
	{
		if (s.length() < maxLength) return s;

		return s.substring(0, maxLength - 3) + "...";
	}

	public static String limitString (String s)
	{
		return limitString(s, 40);
	}


	public static boolean isDoubleChest (Chest c)
	{
		if (c.getBlock().getRelative(BlockFace.EAST).getTypeId() == c.getTypeId()) return true;
		else if (c.getBlock().getRelative(BlockFace.WEST).getTypeId() == c.getTypeId()) return true;
		else if (c.getBlock().getRelative(BlockFace.NORTH).getTypeId() == c.getTypeId()) return true;
		else if (c.getBlock().getRelative(BlockFace.SOUTH).getTypeId() == c.getTypeId()) return true;

		else return false;
	}

	public static String getEntityName (LivingEntity e)
	{
		return e instanceof Player ? ((Player) e).getName() : e.getCustomName() == null ? "&ba &6" + e.getType().toString().toLowerCase().replaceAll("_", " ") : e.getCustomName();
	}

	public static void effectBetweenLocations (final Location from, final Location to, final ParticleEffect effect)
	{
		//		Vector direction = new Vector(to.getX() - from.getX(), to.getY() - from.getY(), to.getZ() - from.getY());
		Vector vector = to.toVector().subtract(from.toVector());
		int dist = (int) to.distance(from);
		final BlockIterator bi = new BlockIterator(from.getWorld(), from.toVector(), vector, 0, (dist > 100 ? 100 : dist));

		new BukkitRunnable()
		{
			Block b;

			public void run ()
			{
				try
				{
					b = bi.next();
					b = bi.next();
					if (b.getLocation().equals(to))
					{
						cancel();
						//						System.out.println("reached end");
					}


					ParticleEffect.sendToLocation(effect, b.getLocation(), 1, 1, 1, 0, 5);
				} catch (NoSuchElementException e)
				{
					//					System.out.println("no more");
					cancel();
				}

			}
		}.runTaskTimer(Chunk64.c64, 0L, 1L);

		//		while (bi.hasNext())
		//		{
		//			Block b = bi.next();
		//			if (b.getTypeId() == 0)
		//				ParticleEffect.sendToLocation(ParticleEffect.LAVA, b.getLocation(), 1, 1, 1, 1, 1);
		//		}


	}

	/*public static void wand (final Location from, final Location to, final ParticleEffect effect, final Location kb)
	{
		//		Vector direction = new Vector(to.getX() - from.getX(), to.getY() - from.getY(), to.getZ() - from.getY());
		Vector vector = to.toVector().subtract(from.toVector());
		int dist = (int) to.distance(from);
		final BlockIterator bi = new BlockIterator(from.getWorld(), from.toVector(), vector, 0, (dist > 150 ? 150 : dist));
		final int colour = new Random().nextInt(200);

		new BukkitRunnable()
		{
			Block b;

			public void run ()
			{
				try
				{
					b = bi.next();
					b = bi.next();
					if (b.getLocation().equals(to))
						cancel();

					ParticleEffect.sendToLocation(effect, b.getLocation(), 1, 1, 1, colour, 50);
				} catch (NoSuchElementException e)
				{
					cancel();

					ParticleEffect.sendToLocation(ParticleEffect.RED_DUST, getRandomRelativeLocation(b.getLocation(), 5), 2, 2, 2, 0, 80);
					int r = 3;
					for (int x = -r; x <= r; x++)
					{
						for (int y = -r; y <= r; y++)
						{
							for (int z = -r; z <= r; z++)
							{
								Block blo = b.getRelative(x, y, z);
								FallingBlock fb = blo.getWorld().spawnFallingBlock(blo.getLocation(), blo.getTypeId(), blo.getData());
								fb.setDropItem(false);
								knockbackTest(kb ,fb, 1);

								blo.setTypeId(0);

							}
						}
					}

				}


			}
		}.runTaskTimer(Chunk64.c64, 0L, 1L);





	}

	public static Location getRandomRelativeLocation (Location loc, int radius)
	{
		Random random = new Random();
		Location output = loc.clone();
		output.add((random.nextBoolean() ? 1 : -1) * random.nextInt(radius),
				(random.nextBoolean() ? 1 : -1) * random.nextInt(radius),
				(random.nextBoolean() ? 1 : -1) * random.nextInt(radius));
		return output;

	}

	public static void knockbackTest (Location loc, Entity entity, int power)
	{
		double xOff = entity.getLocation().getX() - loc.getX();
		double yOff = entity.getLocation().getY() - loc.getY();
		double zOff = entity.getLocation().getZ() - loc.getZ();

		entity.setVelocity(new Vector(xOff, yOff, zOff).normalize().multiply(power).add(new Vector(0, 0.2, 0)));

	}*/


}
