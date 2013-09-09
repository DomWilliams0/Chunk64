package net.chunk64.chinwe;

import net.chunk64.chinwe.util.C64Utils;
import org.bukkit.Bukkit;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class InventoryStore
{

	private Map<String, ItemStack[]> inventories = new HashMap<String, ItemStack[]>();
	private Map<String, ItemStack[]> armours = new HashMap<String, ItemStack[]>();

	public InventoryStore()
	{


	}


	public void store(Player p, boolean override) throws IllegalArgumentException
	{
		String player = p.getName();

		if (inventories.containsKey(player) && !override)
			throw new IllegalArgumentException("That inventory is already stored!");

		inventories.put(player, p.getInventory().getContents());
		armours.put(player, p.getInventory().getArmorContents());

		clearInventory(p);

	}


	public void restore(Player p) throws IllegalArgumentException
	{
		String player = p.getName();
		if (!inventories.containsKey(player))
			throw new IllegalArgumentException("That inventory is not stored!");

		ItemStack[] contents = inventories.get(player);
		ItemStack[] armour = armours.get(player);

		p.getInventory().setArmorContents(armour);
		p.getInventory().setContents(contents);

		inventories.remove(player);
		armours.remove(player);
	}

	public boolean isStored(String name)
	{
		return inventories.containsKey(name);
	}

	public void storeInChest(Player p, Chest chest) throws IllegalArgumentException
	{
		List<ItemStack> pInv = cleanse(p.getInventory());
		List<ItemStack> chestContents = cleanse(chest.getInventory());
		int maxChestSize = (C64Utils.isDoubleChest(chest) ? 54 : 27);

		int chestSize = chestContents.size();
		int invSize = pInv.size();

		if (invSize == 0)
			throw new IllegalArgumentException("You don't have anything in your inventory to deposit!");

		if (chestSize + invSize > maxChestSize)
			throw new IllegalArgumentException("There is not enough room in that chest!");

		chest.getInventory().setContents(combineInventories(pInv, chestContents));
		clearInventory(p);
	}

	public void retrieveFromChest(Player p, Chest chest) throws IllegalArgumentException
	{
		List<ItemStack> contents = cleanse(chest.getInventory());
		List<ItemStack> pInv = cleanse(p.getInventory());

		int chestSize = contents.size();
		int invSize = pInv.size();

		if (chestSize == 0)
			throw new IllegalArgumentException("You can't withdraw from an empty chest!");

		if (chestSize + invSize > p.getInventory().getContents().length)
			throw new IllegalArgumentException("There is not enough room in your inventory!");

		p.getInventory().setContents(combineInventories(pInv, contents));

		chest.getInventory().clear();
	}

	private ItemStack[] combineInventories(List<ItemStack> cleansedInv, List<ItemStack> cleansedItems)
	{
		cleansedInv.addAll(cleansedItems);
		ItemStack[] contents = new ItemStack[cleansedInv.size()];
		cleansedInv.toArray(contents);
		return contents;
	}

	private void clearInventory(Player p)
	{
		p.getInventory().clear();
		p.getInventory().setArmorContents(new ItemStack[p.getInventory().getArmorContents().length]);
	}

	/**
	 * Returns true if the player should store inventory in the chest, false if they should withdraw from it
	 */
	public boolean shouldStore(Chest chest, Player player)
	{
		double chestFilled = (double) cleanse(chest.getInventory()).size() / (double) chest.getInventory().getContents().length;
		double playerFilled = (double) cleanse(player.getInventory()).size() / (double) player.getInventory().getContents().length;

		// Favour storing in chest
		return playerFilled >= chestFilled;
	}

	public List<ItemStack> cleanse(Inventory inv)
	{
		List<ItemStack> contents = new ArrayList<>();
		for (ItemStack is : inv.getContents())
		{
			if (is == null)
				continue;
			if (is.getTypeId() == 0)
				continue;
			contents.add(is);
		}

		if (inv instanceof PlayerInventory)
		{
			for (ItemStack is : ((PlayerInventory) inv).getArmorContents())
			{
				if (is == null)
					continue;
				if (is.getTypeId() == 0)
					continue;
				contents.add(is);
			}
		}

		return contents;
	}

	public void restoreAll()
	{
		Iterator<String> it = inventories.keySet().iterator();
		while (it.hasNext())
		{
			String s = it.next();
			if (Bukkit.getPlayerExact(s) == null)
			{

				C64Utils.severe("Could not restore inventory of " + s);
				continue;
			}
			Player p = Bukkit.getPlayerExact(s);

			p.getInventory().setContents(inventories.get(s));
			p.getInventory().setArmorContents(armours.get(s));

			C64Utils.message(p, "&bYour inventory was restored due to a reload!");


		}

		armours.clear();
		;
		inventories.clear();

	}

}
