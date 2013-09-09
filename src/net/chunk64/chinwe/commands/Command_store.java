package net.chunk64.chinwe.commands;

import net.chunk64.chinwe.Chunk64;
import net.chunk64.chinwe.InventoryStore;
import net.chunk64.chinwe.util.C64Utils;
import net.chunk64.chinwe.util.CommandUtils;
import net.chunk64.chinwe.util.ParticleEffect;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class Command_store implements CommandExecutor
{


	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (cmd.getName().equalsIgnoreCase(CommandUtils.getCommandName(this)))
		{
			try
			{
				if (!CommandUtils.canUse(sender, cmd))
					return true;

				// Help
				if (args.length == 0)
				{
					CommandUtils.sendHelp(sender, "Storing Help", label, ";View this help menu", "inv;Temporarily store your inventory;&oIt will be restored on disconnect/reload", "chest;Store your inventory in the chest you're looking at");
					return true;
				}

				InventoryStore store = Chunk64.store;
				Player p = (Player) sender;

				// TODO Add to chest inventory instead of overriding

				if (args.length == 1 && (args[0].equalsIgnoreCase("inv")) || args[0].equalsIgnoreCase("chest"))
				{
					String status;
					boolean intoChest = args[0].equalsIgnoreCase("chest");

					if (!intoChest)
					{

						if (store.isStored(sender.getName()))
						{
							store.restore(p);
							status = "&bYou &6retrieved&b your inventory!";
						} else
						{
							store.store(p, false);
							status = "&bYou &6stored&b your inventory away!";
						}
					} else
					{
						Block target = p.getTargetBlock(null, 100);

						if (!(target.getState() instanceof Chest))
						{
							C64Utils.message(sender, "&bPlease click on a &6chest&b!");
							p.setMetadata("chestStore", new FixedMetadataValue(Chunk64.c64, true));
							return true;
						}

						Chest chest = (Chest) target.getState();
						if (!store.shouldStore(chest, p))
						{
							store.retrieveFromChest(p, chest);
							status = "&bYou &6retrieved&b your inventory from &6that chest&b!";
							C64Utils.effectBetweenLocations(chest.getLocation(), p.getEyeLocation(), ParticleEffect.WITCH_MAGIC, 5);
						} else
						{
							store.storeInChest(p, chest);
							status = "&bYou &6stored&b your inventory in &6that chest&b!";
							C64Utils.effectBetweenLocations(p.getEyeLocation(), chest.getLocation(), ParticleEffect.WITCH_MAGIC, 5);
						}


					}


					C64Utils.message(sender, status);
					return true;
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

		return true;
	}


}
