package net.chunk64.chinwe.listeners;

import net.chunk64.chinwe.Chunk64;
import net.chunk64.chinwe.Config;
import net.chunk64.chinwe.InventoryStore;
import net.chunk64.chinwe.PlayerData;
import net.chunk64.chinwe.commands.Command_measure;
import net.chunk64.chinwe.commands.Command_ride;
import net.chunk64.chinwe.commands.Command_staffchat;
import net.chunk64.chinwe.util.C64Utils;
import net.chunk64.chinwe.util.ParticleEffect;
import net.chunk64.chinwe.util.Passengers;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class PlayerListener implements Listener
{
	@SuppressWarnings("unused")
	private Chunk64 plugin;

	public PlayerListener(Chunk64 chunk64)
	{
		this.plugin = chunk64;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		// Load
		PlayerData pd = new PlayerData(event.getPlayer().getName());
		pd.load();
		// Ip
		String ip = event.getPlayer().getAddress().getAddress().getHostAddress();
		pd.addIp(ip);

		// Alt
		for (PlayerData pdata : PlayerData.getAll())
		{
			if (pdata.getIps().contains(ip) && !pd.getName().equals(pdata.getName()))
			{
				pd.addAlt(pdata.getName());
				pdata.addAlt(pd.getName());
				pdata.save();
			}
		}

		// Login time
		pd.setLoginTime(System.currentTimeMillis());

		pd.save();
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		PlayerData pd = PlayerData.getData(event.getPlayer().getName());

		// Ip
		String ip = event.getPlayer().getAddress().getAddress().getHostAddress();
		pd.addIp(ip);

		// Play time
		pd.setPlayTime(pd.getPlayTime() + (System.currentTimeMillis() - pd.getLoginTime()));

		// Restore inventory
		try
		{
			if (Chunk64.store.isStored(event.getPlayer().getName()))
			{
				Chunk64.store.restore(event.getPlayer());
				C64Utils.info("Restored the inventory of " + event.getPlayer().getName() + " as they disconnected");
			}
		} catch (IllegalArgumentException e)
		{
			C64Utils.severe("Error while restoring " + event.getPlayer().getName() + "'s inventory: " + e.getMessage());
		}


		pd.save();

		pd.unload();
	}

	@EventHandler
	public void onMove(PlayerMoveEvent event)
	{
		if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockZ() == event.getTo().getBlockZ() && event.getFrom().getBlockY() == event.getTo().getBlockY())
			return;

		PlayerData pd = PlayerData.getData(event.getPlayer().getName());

		// Halted
		if (pd.isHalted())
		{

			// Check time
			if (pd.getUnhaltTime() != -1 && pd.getUnhaltTime() <= System.currentTimeMillis())
			{
				C64Utils.message(event.getPlayer(), "&bYou were &6unhalted&b because your unhalt time was reached.");
				pd.setHaltLocation(null);
				pd.setUnhaltTime(-1);
				pd.save();
				ParticleEffect.sendToLocation(ParticleEffect.EXPLODE, event.getPlayer().getLocation(), 1, 1, 1, 1, 80);
				event.getPlayer().getWorld().playEffect(event.getPlayer().getLocation(), Effect.STEP_SOUND, 7);
				return;
			}
			Location newLoc = pd.getHaltLocation();
			newLoc.setYaw(event.getFrom().getYaw());
			newLoc.setPitch(event.getFrom().getPitch());
			event.setTo(newLoc);
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		PlayerData pd = PlayerData.getData(event.getPlayer().getName());
		if (pd.isHalted())
		{
			event.setCancelled(true);

			// Check time
			if (pd.getUnhaltTime() != -1 && pd.getUnhaltTime() <= System.currentTimeMillis())
			{
				C64Utils.message(event.getPlayer(), "&bYou were &6unhalted&b because your unhalt time was reached.");
				pd.setHaltLocation(null);
				pd.setUnhaltTime(-1);
				pd.save();
				ParticleEffect.sendToLocation(ParticleEffect.EXPLODE, event.getPlayer().getLocation(), 1, 1, 1, 1, 80);
				event.getPlayer().getWorld().playEffect(event.getPlayer().getLocation(), Effect.STEP_SOUND, 7);
				return;
			}
		}

	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event)
	{
		PlayerData pd = PlayerData.getData(event.getPlayer().getName());
		if (pd.isHalted())
		{
			event.setCancelled(true);

			// Check time
			if (pd.getUnhaltTime() != -1 && pd.getUnhaltTime() <= System.currentTimeMillis())
			{
				C64Utils.message(event.getPlayer(), "&bYou were &6unhalted&b because your unhalt time was reached.");
				pd.setHaltLocation(null);
				pd.setUnhaltTime(-1);
				pd.save();
				ParticleEffect.sendToLocation(ParticleEffect.EXPLODE, event.getPlayer().getLocation(), 1, 1, 1, 1, 80);
				event.getPlayer().getWorld().playEffect(event.getPlayer().getLocation(), Effect.STEP_SOUND, 7);
				return;
			}
		}

	}

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event)
	{
		if (event.getDamager() instanceof Player)
		{
			// Halted
			PlayerData pd = PlayerData.getData(((Player) event.getDamager()).getName());
			if (pd.isHalted())
				event.setCancelled(true);

			Player p = (Player) event.getDamager();

			// Left click saddle
			if (Command_ride.toolUsers.contains(p.getName()) && event.getEntity() instanceof LivingEntity)
			{
				LivingEntity e = (LivingEntity) event.getEntity();
				try
				{
					// Force to ride you
					if (p.getItemInHand().getType() == Material.SADDLE)
					{
						event.setCancelled(true);
						Passengers.addPassenger((LivingEntity) event.getEntity(), p);
						C64Utils.message(p, "&bYou forced &6" + C64Utils.getEntityName((LivingEntity) event.getEntity()) + "&b to ride you!");
					}

					// Prise off single
					else if (p.getItemInHand().getType() == Material.STICK)
					{
						event.setCancelled(true);
						if (!e.isInsideVehicle())
							throw new IllegalArgumentException("That entity is not riding anything!");
						Passengers.ejectPassenger(e, Passengers.getMount(e));
						C64Utils.message(p, "&bYou prised off &6" + C64Utils.getEntityName(e) + "&b!");
					}

					// Select
					else if (p.getItemInHand().getType() == Material.STRING)
					{
						event.setCancelled(true);

						ArrayList<Integer> current = Command_ride.entitiesToStack.containsKey(p.getName()) ? Command_ride.entitiesToStack.get(p.getName()) : new ArrayList<Integer>();
						if (current.contains(e.getEntityId()))
							throw new IllegalArgumentException("That entity is already selected!");
						current.add(e.getEntityId());
						Command_ride.entitiesToStack.put(p.getName(), current);

						C64Utils.message(p, "&bAdded &6" + C64Utils.getEntityName(e) + "&b to the list, which now contains &6" + current.size() + "&b entit" + (current.size() == 1 ? "y!" : "ies!"));
						ParticleEffect.sendToLocation(ParticleEffect.CLOUD, e.getEyeLocation(), 1, 1, 1, 1, 5);

					}

				} catch (IllegalArgumentException e1)
				{
					C64Utils.error(p, e1);
				}

			}

		}
	}

	@EventHandler
	public void onEntityInteract(PlayerInteractEntityEvent event)
	{
		// Right clicking riding
		if (Command_ride.toolUsers.contains(event.getPlayer().getName()) && event.getRightClicked() instanceof LivingEntity)
		{
			LivingEntity e = (LivingEntity) event.getRightClicked();
			Player p = event.getPlayer();

			try
			{
				// Mount
				if (p.getItemInHand().getType() == Material.SADDLE)
				{
					event.setCancelled(true);
					Passengers.addPassenger(p, e);
					C64Utils.message(p, "&bYou mounted &6" + C64Utils.getEntityName(e) + "&b!");

				}
				// Prise all
				else if (p.getItemInHand().getType() == Material.STICK)
				{
					event.setCancelled(true);
					Passengers.ejectPassenger(null, e);
					if (Passengers.getPassengers(e).size() == 0)
						throw new IllegalArgumentException("That entity has no passengers!");
					C64Utils.message(p, "&bYou prised off all passengers riding on &6" + C64Utils.getEntityName(e) + "&b!");
				}

				// Stack
				else if (p.getItemInHand().getType() == Material.STRING)
				{
					event.setCancelled(true);
					if (!Command_ride.entitiesToStack.containsKey(p.getName()))
						throw new IllegalArgumentException("You don't have any entities selected!");


					ArrayList<Integer> entities = Command_ride.entitiesToStack.get(p.getName());
					try
					{

						System.out.println("stacking all on " + e.getEntityId());
						System.out.println("passengers of mount are ");
						for (Entity f : Passengers.getPassengers(e))
							System.out.println("another p: " + f.getEntityId());

						int count = 0;

						for (int i : entities)
						{
							for (LivingEntity en : p.getWorld().getLivingEntities())
								if (en.getEntityId() == i)
								{
									en.teleport(e.getLocation());
									Passengers.addPassenger(en, e);
									count++;
								}
						}

						ParticleEffect.sendToLocation(ParticleEffect.LAVA, e.getEyeLocation(), 1, 1, 1, 1, 1);
						C64Utils.message(p, "&bStacked &6" + count + "&b " + (entities.size() == 1 ? "entity" : "entities") + " onto &6" + C64Utils.getEntityName(e) + "&b!");
						Command_ride.entitiesToStack.remove(p.getName());
					} catch (IllegalArgumentException e2)
					{
						Command_ride.entitiesToStack.remove(p.getName());
					}


				}

			} catch (IllegalArgumentException e1)
			{
				C64Utils.error(event.getPlayer(), e1);
			}


		}

	}

	static BukkitTask ok;

	@EventHandler
	public void onInteract(PlayerInteractEvent event)
	{
		PlayerData pd = PlayerData.getData(event.getPlayer().getName());
		if (pd.isHalted())
			event.setCancelled(true);

		// Measure
		if (pd.getMeasureMode() != 0)
		{
			if (event.getMaterial().getId() == Config.MeasureStick && event.getAction().toString().contains("BLOCK"))
			{
				event.setCancelled(true);
				boolean leftClick = event.getAction() == Action.LEFT_CLICK_BLOCK;
				Location loc = event.getClickedBlock().getLocation();

				event.getPlayer().playEffect(loc, Effect.STEP_SOUND, 20);

				// TODO Temporarily show client side region?
				try
				{
					if (!Command_measure.setPoint(event.getPlayer().getName(), loc, leftClick))
						C64Utils.message(event.getPlayer(), "&b" + (leftClick ? "1st" : "2nd") + " point set at &6" + C64Utils.formatLocation(loc));
					else Command_measure.getDistance(event.getPlayer());
				} catch (IllegalArgumentException e)
				{
					C64Utils.error(event.getPlayer(), e);
				}
			}

		}

		// Chest store
		if (event.getPlayer().hasMetadata("chestStore") && event.getAction() == Action.LEFT_CLICK_BLOCK)
		{
			if (event.getClickedBlock().getState() instanceof Chest)
			{
				Player p = event.getPlayer();
				try
				{
					event.getPlayer().removeMetadata("chestStore", Chunk64.c64);
					Chest c = ((Chest) event.getClickedBlock().getState());
					InventoryStore store = Chunk64.store;
					String status;

					if (!store.shouldStore(c, p))
					{
						store.retrieveFromChest(p, c);
						status = "&bYou &6retrieved&b your inventory from &6that chest&b!";
					} else
					{
						store.storeInChest(p, c);
						status = "&bYou &6stored&b your inventory in &6that chest&b!";
					}
					C64Utils.message(p, status);
					event.setCancelled(true);


				} catch (IllegalArgumentException e)
				{
					C64Utils.error(p, e);
				}
			}
		}

	}

	@EventHandler
	public void onChat(final AsyncPlayerChatEvent event)
	{
		PlayerData pd = PlayerData.getData(event.getPlayer().getName());

		// Muted
		if (pd.isMuted())
		{
			// Check time
			if (pd.getUnmuteTime() != 1 && pd.getUnmuteTime() <= System.currentTimeMillis())
			{
				C64Utils.message(event.getPlayer(), "&bYou were &6unmuted&b because your unmute time was reached.");
				pd.setUnmuteTime(-1);
				pd.save();
				return;
			}

			event.setCancelled(true);

			C64Utils.message(event.getPlayer(), "&cYou're muted! Shut it!");
			C64Utils.notifyOps(event.getPlayer().getName() + " tried to say: \"" + event.getMessage() + "\"", event.getPlayer().getName());

		}

		// Staffchat
		if (Command_staffchat.staffChatters.contains(event.getPlayer().getName()))
		{
			Command_staffchat.staffChat(event.getPlayer(), event.getMessage());
			event.setCancelled(true);
		}

	}

	@EventHandler
	public void onCommandPreProcess(PlayerCommandPreprocessEvent event)
	{
		PlayerData pd = PlayerData.getData(event.getPlayer().getName());

		// Muted
		if (pd.isMuted())
		{
			String cmd = event.getMessage().split(" ")[0];
			if (Config.MuteCommands.contains(cmd.substring(1)))
			{
				event.setCancelled(true);
				C64Utils.message(event.getPlayer(), "&cYou're muted! Shut it!");
				C64Utils.notifyOps(event.getPlayer().getName() + " tried to use \"" + event.getMessage() + "\" while muted", event.getPlayer().getName());
			}
		}

	}

	@EventHandler
	public void onPing(ServerListPingEvent event)
	{
		// MOTD
		if (!Config.MotdPrefix.equalsIgnoreCase("none") && Chunk64.motdManager.getMotds().size() > 0)
		{
			if (Chunk64.motdManager.getOverride() == null)
				event.setMotd(ChatColor.translateAlternateColorCodes('&', Config.MotdPrefix + Chunk64.motdManager.next()));
			else
				event.setMotd(ChatColor.translateAlternateColorCodes('&', Config.MotdPrefix + Chunk64.motdManager.getOverride()));

		}
	}

	@EventHandler
	public void onLogin(PlayerLoginEvent event)
	{
		// Whitelist message
		if (event.getResult() == PlayerLoginEvent.Result.KICK_WHITELIST) if (!Config.WhitelistMessage.equals("none"))
			event.setKickMessage(Config.WhitelistMessage.replace("[player]", event.getPlayer().getName()).replaceAll("&", "§").replace("[newline]", ChatColor.stripColor("\n")));

	}

	// ----------------------------- TEMPORARY SHIT --------------------------------------- \\
	// TODO TEMP MINECART TEST
	// Must have swiftness effect to have fast cart?
	// Listen for enter cart, if have efefct set cart power, set back to normal
	// when they exit
	//	@EventHandler
	public void onVehicleMove(VehicleMoveEvent event)
	{
		if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockZ() == event.getTo().getBlockZ() && event.getFrom().getBlockY() == event.getTo().getBlockY())
			return;

		if (event.getVehicle() instanceof Minecart)
		{

			Minecart cart = (Minecart) event.getVehicle();

			cart.setMaxSpeed(20);
			cart.setSlowWhenEmpty(false);
			cart.setFlyingVelocityMod(new Vector(1, 5, 1));
			cart.setDerailedVelocityMod(new Vector(4, 4, 4));

			// Special boost
			if (event.getTo().getBlock().getLocation().add(0, -2, 0).getBlock().getType() == Material.REDSTONE_BLOCK)
			{
				cart.setVelocity(cart.getVelocity().normalize().multiply(2));
				if (!cart.isEmpty())
				{
					ParticleEffect.sendToLocation(ParticleEffect.EXPLODE, event.getTo(), 0, 0, 0, 1, 40);
					cart.getWorld().playSound(cart.getLocation(), Sound.GHAST_FIREBALL, 0.3F, 1);
				}
			}

		}

	}


}
