package net.chunk64.chinwe.commands;

import net.chunk64.chinwe.Chunk64;
import net.chunk64.chinwe.Config;
import net.chunk64.chinwe.util.C64Utils;
import net.chunk64.chinwe.util.CommandUtils;
import net.chunk64.chinwe.util.MoverManager;
import net.chunk64.chinwe.util.ParticleEffect;
import net.minecraft.server.v1_6_R2.WorldServer;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.TreeType;
import org.bukkit.block.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_6_R2.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Button;
import org.bukkit.material.Lever;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Command_tools implements CommandExecutor, Listener
{

	public static List<String> handyHelpers = new ArrayList<>();

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		try
		{
			if (cmd.getName().equalsIgnoreCase("mover"))
			{
				if (!CommandUtils.canUse(sender, cmd))
					return true;

				if (args.length == 0)
				{
					boolean enabled = MoverManager.movers.contains(sender.getName());
					if (enabled)
					{
						MoverManager.movers.remove(sender.getName());
						MoverManager.removeSelections(sender.getName());
					} else
						MoverManager.movers.add(sender.getName());

					C64Utils.message(sender, "&bMover &6" + (enabled ? "disabled" : "enabled") + "&b!");
					if (!enabled)
					{
						C64Utils.message(sender, "&bRight click blocks with a &6" + C64Utils.getFriendlyMaterial(Config.MoverTool) + "&b to select them, and left click anywhere to move them\n&7  - Sneak while left clicking to forget that block after moving it");
						C64Utils.message(sender, "&bDo the same with &6mobs&b! Sneak while left clicking to &6remember&b the mob after moving\n&7  - The opposite to moving blocks!");

					}
					return true;
				}

			} else if (cmd.getName().equalsIgnoreCase("handyhelper"))
			{
				if (!CommandUtils.canUse(sender, cmd))
					return true;

				if (args.length == 0)
				{
					boolean enabled = handyHelpers.contains(sender.getName());
					if (enabled)
					{
						handyHelpers.remove(sender.getName());
						MoverManager.removeSelections(sender.getName());
					} else
						handyHelpers.add(sender.getName());

					C64Utils.message(sender, "&bHandyHelper &6" + (enabled ? "disabled" : "enabled") + "&b!");
					if (!enabled)
						C64Utils.message(sender, "&bUse &6/" + label + " help &bfor help!");
					return true;
				}

				// Help
				if (args.length == 1 && args[0].equalsIgnoreCase("help"))
				{
					C64Utils.message(sender, "&bWith a &6" + C64Utils.getFriendlyMaterial(Config.HandyHelper) + "&b, &6left&b or &6right&b click from far away on: " +
							"&7buttons, levers, doors, trapdoors, gates, redstone lamps, TNT, noteblocks, jukeboxes, saplings, crops, potatoes, carrots, grass," +
							" mycelium, dirt, chests, dispensers, trapped chests, pumpkins, jack-o-lanterns and torches");
					return true;
				}


			}

		} catch (IllegalArgumentException e1)
		{
			C64Utils.error(sender, e1);
			return true;
		} catch (Exception e)
		{
			C64Utils.debugMessage("Error in command \"" + label + "\": " + e);
		}
		CommandUtils.sendUsage(sender, cmd, false);
		return true;

	}

	//	@EventHandler
	//	public void onHit(ProjectileHitEvent event)
	//	{
	//		if (event.getEntity().hasMetadata("HH")) event.getEntity().remove();
	//	}
	//
	//	@EventHandler
	//	public void onTarget(EntityDamageByEntityEvent event)
	//	{
	//		if (event.getDamager().hasMetadata("HH"))
	//		{
	//			event.setCancelled(true);
	//			event.getDamager().remove();
	//			Entity target = event.getEntity();
	//
	//			if (event.getEntity() instanceof Sheep) ((Sheep) target).setSheared(!((Sheep) target).isSheared());
	//		}
	//
	//
	//	}
	@EventHandler
	public void onInteract(PlayerInteractEvent event)
	{
		if (handyHelpers.contains(event.getPlayer().getName()))
		{
			if (event.getItem() != null && event.getItem().getTypeId() == Config.HandyHelper)
			{
				Player p = event.getPlayer();
				final Block target = p.getTargetBlock(null, 256);
				boolean success = false;
				/*
				// --- Entities --- \\
				// Only left/right click? Or sneaking?
				if (p.isSneaking())
				{
					Snowball a = p.launchProjectile(Snowball.class);
					a.setMetadata("HH", new FixedMetadataValue(Chunk64.c64, true));
					a.setVelocity(a.getVelocity().multiply(4));
					return;
				}
				*/

				// --- Redstone --- \\

				// Press buttons
				if (target.getType().toString().contains("BUTTON"))
				{
					long ticks = target.getType() == Material.STONE_BUTTON ? 10 : 15;
					new BukkitRunnable()
					{
						@Override
						public void run()
						{
							toggleLeverOrButton(target);
						}
					}.runTaskLater(Chunk64.c64, ticks);
					toggleLeverOrButton(target);
					success = true;
				}

				// Flip levers
				if (target.getType() == Material.LEVER)
				{
					toggleLeverOrButton(target);
					success = true;
				}

				// Open doors
				if (target.getType().toString().contains("DOOR") || target.getType() == Material.FENCE_GATE)
				{
					toggleDoor(getBase(target));

					// Double door
					if (!target.getType().toString().contains("TRAP"))
					{

						BlockFace bf = null;
						for (int i = 0; i < 4; i++)
							if (target.getRelative(BlockFace.values()[i]).getType() == target.getType())
							{
								bf = BlockFace.values()[i];
								break;
							}

						if (bf != null && getBase(target.getRelative(bf)).getData() != getBase(target).getData())
							toggleDoor(getBase(target.getRelative(bf)));
					}

					success = true;
				}

				// Turn on lamps
				if (target.getType().toString().contains("LAMP"))
				{
					WorldServer ws = ((CraftWorld) target.getWorld()).getHandle();

					boolean mem = ws.isStatic;
					if (!mem)
						ws.isStatic = true;

					if (target.getTypeId() == 123)
						target.setTypeIdAndData(Material.REDSTONE_LAMP_ON.getId(), (byte) 0, false);
					else
						target.setTypeIdAndData(Material.REDSTONE_LAMP_OFF.getId(), (byte) 0, false);

					if (!mem)
						ws.isStatic = false;
					success = true;
				}

				// Ignite TNT
				if (target.getType() == Material.TNT)
				{
					target.setTypeId(0);
					target.getWorld().spawn(target.getLocation().add(0.5, 0.25, 0.5), TNTPrimed.class);
					success = true;
				}

				// Play note
				if (target.getType() == Material.NOTE_BLOCK)
				{
					NoteBlock b = (NoteBlock) target.getState();
					if (event.getAction().toString().contains("RIGHT"))
						b.setRawNote((byte) (b.getRawNote() == 24 ? 0 : b.getRawNote() + 1));
					b.play();
					success = true;
				}

				// Toggle pistons - doesn't work
				//				if (target.getType().toString().contains("PISTON"))
				//				{
				//					Block pistonBlock = getBase(target);
				//					PistonBaseMaterial base = (PistonBaseMaterial) pistonBlock.getState().getData();
				//					Block extension = pistonBlock.getRelative(base.getFacing());
				//					boolean isExtended = extension.getType() == Material.PISTON_EXTENSION;
				//
				//					pistonBlock.setData((byte) (isExtended ? pistonBlock.getData() - 8 : pistonBlock.getData() + 8));
				//					if (isExtended) setType(extension, Material.AIR);
				//					else
				//					{
				//						setType(extension, Material.PISTON_EXTENSION);
				//						extension.setData(base.getData() < 6 ? base.getData() : (byte) (base.getData() - 8));
				//					}
				//					success = true;
				//				}

				// --- Growing --- \\

				// Sapling
				if (target.getType() == Material.SAPLING)
				{
					List<TreeType> tt = new ArrayList<>();

					switch (target.getData())
					{
						case 0:
							if (target.getBiome() == Biome.SWAMPLAND)
								tt.add(TreeType.SWAMP);
							else
							{
								tt.add(TreeType.BIG_TREE);
								tt.add(TreeType.TREE);
							}
							break;
						case 1:
							tt.add(TreeType.REDWOOD);
							tt.add(TreeType.TALL_REDWOOD);
							break;
						case 2:
							tt.add(TreeType.BIRCH);
							break;
						case 3:
							tt.add(TreeType.JUNGLE); // TODO Detect 4x4 jungle trees
							tt.add(TreeType.JUNGLE_BUSH);
							tt.add(TreeType.SMALL_JUNGLE);
							break;
						default:
							tt.add(TreeType.TREE);
					}

					byte origData = target.getData();
					target.setTypeId(0);
					if (target.getWorld().generateTree(target.getLocation(), tt.get(new Random().nextInt(tt.size()))))
						success = true;
					else
						target.setTypeIdAndData(Material.SAPLING.getId(), origData, false);
				}

				// Crops
				if (target.getType() == Material.CROPS || target.getType() == Material.POTATO || target.getType() == Material.CARROT)
				{
					target.setData((byte) (target.getData() == 7 ? 0 : 7));
					success = true;
				}


				// --- Other blocks --- \\
				if (target.getType() == Material.GRASS || target.getType() == Material.MYCEL || target.getType() == Material.DIRT)
				{
					if (target.getType() == Material.DIRT)
					{
						Material newMat = Material.GRASS;
						for (int i = 0; i < 4; i++)
							if (target.getRelative(BlockFace.values()[i]).getType() == Material.MYCEL)
							{
								newMat = Material.MYCEL;
								break;
							}
						setType(target, newMat);
					} else
						setType(target, Material.DIRT);
					success = true;
				}


				// Play record
				if (target.getType() == Material.JUKEBOX)
				{
					Jukebox b = (Jukebox) target.getState();
					if (b.isPlaying())
					{
						if (!b.hasMetadata("noDrop"))
							b.eject();
						else
						{
							b.setPlaying(Material.AIR);
							b.removeMetadata("noDrop", Chunk64.c64);
						}
					} else
					{
						Material toPlay = b.getPlaying() == Material.AIR ? Material.RECORD_4 : b.getPlaying();
						b.setPlaying(toPlay);
						b.setMetadata("noDrop", new FixedMetadataValue(Chunk64.c64, true));
					}

					success = true;
				}

				// Open chest
				if (target.getType().toString().contains("CHEST") && target.getType() != Material.ENDER_CHEST)
				{
					Chest c = (Chest) target.getState();
					boolean open = c.getBlockInventory().getViewers().size() != 0 || c.hasMetadata("openChest");
					for (Player pl : target.getWorld().getPlayers())
						pl.playNote(target.getLocation(), (byte) 1, (byte) (open ? 0 : 1));
					if (c.hasMetadata("openChest"))
						c.removeMetadata("openChest", Chunk64.c64);
					else
						c.setMetadata("openChest", new FixedMetadataValue(Chunk64.c64, true));
					success = true;
				}

				// Dispenser
				if (target.getType() == Material.DISPENSER)
				{
					Dispenser d = (Dispenser) target.getState();
					d.dispense();
					d.update();
					success = true;
				}

				// Toggle pumpkin
				if (target.getType() == Material.PUMPKIN || target.getType() == Material.JACK_O_LANTERN)
				{
					target.setType(target.getType() == Material.PUMPKIN ? Material.JACK_O_LANTERN : Material.PUMPKIN);
					success = true;
				}

				// Toggle torch
				if (target.getType().toString().contains("TORCH"))
				{
					target.setTypeIdAndData(target.getTypeId() == 50 ? 75 : 50, target.getData(), false);
					success = true;
				}

				if (success)
				{
					// C64Utils.effectBetweenLocations(p.getEyeLocation(), target.getLocation(), ParticleEffect.WITCH_MAGIC, 5);
					p.playSound(target.getLocation(), Sound.ITEM_PICKUP, 0.5F, 0.3F);
					ParticleEffect.sendToPlayer(ParticleEffect.SPLASH, p, target.getLocation(), 1, 1, 1, 1, 20);
					event.setCancelled(true);
				}

			}


		}

		// Stop free discs
		if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.JUKEBOX)
		{
			if (event.getClickedBlock().hasMetadata("noDrop"))
			{
				Jukebox b = (Jukebox) event.getClickedBlock().getState();
				b.setPlaying(Material.AIR);
				b.removeMetadata("noDrop", Chunk64.c64);
			}

		}

	}

	private static void toggleLeverOrButton(Block b)
	{
		MaterialData data = b.getState().getData();

		if (b.getType() == Material.LEVER)
			((Lever) data).setPowered(!((Lever) data).isPowered());
		else
			((Button) data).setPowered(!((Button) data).isPowered());

		BlockState state = b.getState();
		state.setData(data);
		state.update(true);
	}

	private static void toggleDoor(Block b)
	{
		if (!b.getType().toString().contains("TRAP"))
			b.setData((byte) (b.getData() < 4 ? b.getData() + 4 : b.getData() - 4));
		else
		{
			if (b.getData() < 8)
				b.setData((byte) (b.getData() < 4 ? b.getData() + 4 : b.getData() - 4));
			else
				b.setData((byte) (b.getData() < 12 ? b.getData() + 4 : b.getData() - 4));

		}
	}

	/**
	 * Gets the bottom half of the door if top is specified, or piston base
	 */
	private static Block getBase(Block b)
	{
		if (b.getType().toString().contains("DOOR"))
		{
			if (b.getRelative(BlockFace.DOWN).getType().toString().contains("DOOR"))
				return b.getRelative(BlockFace.DOWN);
			else
				return b;
		} else
		{
			for (int i = 0; i < 6; i++)
				if (b.getRelative(BlockFace.values()[i]).getType().toString().contains("BASE"))
					return b.getRelative(BlockFace.values()[i]);
			return b;
		}
	}

	private static void setType(Block b, Material mat)
	{
		WorldServer ws = ((CraftWorld) b.getWorld()).getHandle();

		boolean mem = ws.isStatic;
		if (!mem)
			ws.isStatic = true;

		if (b.getTypeId() == 123)
			b.setTypeIdAndData(mat.getId(), (byte) 0, false);
		else
			b.setTypeIdAndData(mat.getId(), (byte) 0, false);

		if (!mem)
			ws.isStatic = false;
	}


}
