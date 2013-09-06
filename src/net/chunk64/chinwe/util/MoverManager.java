package net.chunk64.chinwe.util;

import net.chunk64.chinwe.Chunk64;
import net.chunk64.chinwe.Config;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MoverManager implements Listener
{

	private static final List<Integer> DISALLOWED_BLOCKS = new ArrayList<>();

	static
	{
		DISALLOWED_BLOCKS.add(Material.AIR.getId());
		DISALLOWED_BLOCKS.add(Material.CHEST.getId());
		DISALLOWED_BLOCKS.add(Material.TRAPPED_CHEST.getId());
		DISALLOWED_BLOCKS.add(Material.LOCKED_CHEST.getId());
		DISALLOWED_BLOCKS.add(Material.IRON_DOOR.getId());
		DISALLOWED_BLOCKS.add(Material.IRON_DOOR_BLOCK.getId());
		DISALLOWED_BLOCKS.add(Material.WOOD_DOOR.getId());
		DISALLOWED_BLOCKS.add(Material.WOODEN_DOOR.getId());
	}


	private static Map<String, Location> blockSelection = new HashMap<>();
	private static Map<String, UUID> entitySelection = new HashMap<>();
	private static List<String> playerEntities = new ArrayList<>();
	public static List<String> movers = new ArrayList<>();

	// TODO Cleaner up

	@EventHandler
	public void onInteract(PlayerInteractEvent event)
	{
		if (event.getItem() != null && event.getItem().getTypeId() == Config.MoverTool)
		{
			if (movers.contains(event.getPlayer().getName()))
			{
				final Player p = event.getPlayer();

				// Right click to select
				final Block target = p.getTargetBlock(null, 256);

				if (event.getAction().toString().contains("RIGHT") && target != null)
				{

					event.setCancelled(true);

					// Prevent clicking through entities
					if (playerEntities.contains(p.getName()))
					{
						playerEntities.remove(p.getName());
						return;
					}


					if (DISALLOWED_BLOCKS.contains(target.getTypeId())) return;


					blockSelection.put(p.getName(), target.getLocation());
					p.playEffect(target.getLocation(), Effect.STEP_SOUND, target.getTypeId());
				}

				// Left click to move
				else if (event.getAction().toString().contains("LEFT"))
				{
					event.setCancelled(true);
					if (entitySelection.containsKey(p.getName()))
					{
						// Move entity
						Entity entity = null;
						UUID id = entitySelection.get(p.getName());

						// Find entity
						for (Entity e : p.getWorld().getEntities())
						{
							if (e.getUniqueId() == id)
							{
								entity = e;
								break;
							}
						}

						// Wrong world/dead
						if (entity == null)
						{
							entitySelection.remove(p.getName());
							return;
						}

						final Location eLoc = entity.getLocation();
						final double distance = eLoc.distance(target.getLocation());

						final Entity finalEntity = entity;
						final Block finalTarget = getEmptyBlock(target);
						new BukkitRunnable()
						{
							public void run()
							{
								finalEntity.teleport(finalTarget.getLocation());
								eLoc.getWorld().playSound(finalTarget.getLocation(), Sound.CHICKEN_EGG_POP, 0.4F, 1);


								// Remove if not sneaking
								if (!p.isSneaking()) entitySelection.remove(p.getName());

								// We don't need this anymore
								if (playerEntities.contains(p.getName())) playerEntities.remove(p.getName());
							}
						}.runTaskLater(Chunk64.c64, (long) (distance));
						C64Utils.effectBetweenLocations(eLoc, finalTarget.getLocation(), ParticleEffect.RED_DUST, 20);

						return;
					}

					if (!blockSelection.containsKey(p.getName())) return;

					// Move block
					if (blockSelection.get(p.getName()).getWorld() != event.getPlayer().getWorld()) return;

					final Block b = p.getWorld().getBlockAt(blockSelection.get(p.getName()));

					if (DISALLOWED_BLOCKS.contains(target.getTypeId())) return;

					final Block finalTarget = getEmptyBlock(target);
					double distance = target.getLocation().distance(b.getLocation());

					final int id = b.getTypeId();
					final byte data = b.getData();

					new BukkitRunnable()
					{
						public void run()
						{
							finalTarget.setTypeIdAndData(id, data, false);
							finalTarget.getWorld().playSound(finalTarget.getLocation(), Sound.CHICKEN_EGG_POP, 0.4F, 1);
							ParticleEffect.sendToLocation(ParticleEffect.LARGE_SMOKE, finalTarget.getLocation(), 1, 1, 1, 0, 3);

							// Update selection, or remove if it doesn't exist anymore/player is sneaking
							if (finalTarget.getTypeId() == 0 || p.isSneaking()) blockSelection.remove(p.getName());
							else blockSelection.put(p.getName(), finalTarget.getLocation());
						}
					}.runTaskLater(Chunk64.c64, (long) (distance));


					b.setTypeId(0);
					b.getWorld().playSound(b.getLocation(), Sound.CHICKEN_EGG_POP, 0.4F, 1);

					C64Utils.effectBetweenLocations(b.getLocation(), finalTarget.getLocation(), ParticleEffect.MAGIC_CRIT, 10);

				}
			}
		}
	}

	@EventHandler
	public void onEntityInteract(PlayerInteractEntityEvent event)
	{
		if (movers.contains(event.getPlayer().getName()))
		{
			Player p = event.getPlayer();
			if (p.getItemInHand() != null && p.getItemInHand().getTypeId() == Config.MoverTool)
			{
				//				if (event.getRightClicked() instanceof LivingEntity)
				entitySelection.put(p.getName(), event.getRightClicked().getUniqueId());
				p.playEffect(event.getRightClicked().getLocation(), Effect.MOBSPAWNER_FLAMES, 0);
				if (!playerEntities.contains(p.getName())) playerEntities.add(p.getName());

				event.setCancelled(true);
			}


		}


	}

	public static void removeSelections(String player)
	{
		if (blockSelection.containsKey(player)) blockSelection.remove(player);
		if (entitySelection.containsKey(player)) entitySelection.remove(player);
		// TODO remove entity

	}

	private Block getEmptyBlock(Block b)
	{
		BlockFace bf = null;
		List<Integer> transparents = Arrays.asList(0, 31, 32);
		if (transparents.contains(b.getTypeId())) bf = null;
		else if (b.getRelative(BlockFace.UP).getTypeId() == 0) bf = BlockFace.UP;
		else if (b.getRelative(BlockFace.DOWN).getTypeId() == 0) bf = BlockFace.DOWN;
		else if (b.getRelative(BlockFace.EAST).getTypeId() == 0) bf = BlockFace.EAST;
		else if (b.getRelative(BlockFace.WEST).getTypeId() == 0) bf = BlockFace.WEST;
		else if (b.getRelative(BlockFace.NORTH).getTypeId() == 0) bf = BlockFace.NORTH;
		else if (b.getRelative(BlockFace.SOUTH).getTypeId() == 0) bf = BlockFace.SOUTH;

		return bf == null ? b : b.getRelative(bf);
	}


}
