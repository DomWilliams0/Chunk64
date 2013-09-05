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

	public InventoryStore ()
	{


	}


	public void store (Player p, boolean override) throws IllegalArgumentException
	{
		String player = p.getName();

		if (inventories.containsKey(player) && !override)
			throw new IllegalArgumentException("That inventory is already stored!");

		inventories.put(player, p.getInventory().getContents());
		armours.put(player, p.getInventory().getArmorContents());

		clearInventory(p);

	}


	public void restore (Player p) throws IllegalArgumentException
	{
		String player = p.getName();
		if (!inventories.containsKey(player)) throw new IllegalArgumentException("That inventory is not stored!");

		ItemStack[] contents = inventories.get(player);
		ItemStack[] armour = armours.get(player);

		p.getInventory().setArmorContents(armour);
		p.getInventory().setContents(contents);

		inventories.remove(player);
		armours.remove(player);
	}

	public boolean isStored (String name)
	{
		return inventories.containsKey(name);
	}

	public void storeInChest (Player p, Chest chest) throws IllegalArgumentException
	{
		PlayerInventory inv = p.getInventory();
		List<ItemStack> contents = new ArrayList<ItemStack>();

		for (ItemStack is : inv.getContents())
			if (is != null) contents.add(is);
		for (ItemStack is : inv.getArmorContents())
			if (is.getTypeId() != 0) contents.add(is);

		// Compare sizes
		// TODO Don't override chest inventory, add to it
		int chestSize = (C64Utils.isDoubleChest(chest) ? 54 : 27);


		if (contents.size() > chestSize) throw new IllegalArgumentException("There is not enough room in that chest!");

		chest.getInventory().setContents(contents.toArray(new ItemStack[contents.size()]));
		clearInventory(p);


	}

	public void retrieveFromChest (Player p, Chest chest) throws IllegalArgumentException
	{
		List<ItemStack> contents = cleanse(chest.getInventory());

		if (contents.size() > p.getInventory().getContents().length)
			throw new IllegalArgumentException("There is not enough room in your inventory!");

		// TODO Put on armour automatically?
		p.getInventory().setContents(contents.toArray(new ItemStack[contents.size()]));

		chest.getInventory().clear();
		;
	}

	private void clearInventory (Player p)
	{
		p.getInventory().clear();
		p.getInventory().setArmorContents(new ItemStack[p.getInventory().getArmorContents().length]);
	}

	public boolean isEmpty (Inventory inv)
	{
		for (ItemStack is : inv.getContents())
		{
			if (is == null) continue;
			if (is.getTypeId() == 0) continue;
			return false;
		}

		return true;

	}

	public List<ItemStack> cleanse (Inventory inv)
	{
		List<ItemStack> contents = new ArrayList<>();
		for (ItemStack is : inv.getContents())
		{
			if (is == null) continue;
			if (is.getTypeId() == 0) continue;
			contents.add(is);
			if (inv instanceof PlayerInventory)
				if (is.getTypeId() != 0) contents.add(is);
		}

		return contents;
	}

	public void restoreAll ()
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

		armours.clear();;
		inventories.clear();

	}

	public void addToInv(Inventory inv, ItemStack[] items)
	{




	}


}
