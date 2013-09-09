package net.chunk64.chinwe.notes;

import net.chunk64.chinwe.Chunk64;
import net.chunk64.chinwe.util.C64Utils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotesManager
{

	private FileConfiguration yml;
	private File file;
	private Map<String, List<Note>> notes = new HashMap<>();


	public NotesManager(boolean log)
	{
		file = new File(Chunk64.c64.getDataFolder(), "notes.yml");
		if (!file.exists())
		{
			try
			{
				file.createNewFile();
			} catch (IOException e)
			{
				C64Utils.severe("Could not create notes.yml");
			}
			if (log)
				C64Utils.warning("notes.yml not found, creating...");
		} else if (log)
			C64Utils.info("notes.yml found and loaded.");

		yml = YamlConfiguration.loadConfiguration(file);
		yml.options().header("This is where all player notes are stored.\nUse /notes ingame to view the commands");
		save();
	}

	/**
	 * Only saves the file, save each player notes with saveNotes(player)
	 */
	public void save()
	{
		try
		{
			yml.save(file);
		} catch (IOException e)
		{
			C64Utils.severe("Could not save motds.yml");
		}
	}

	public void load()
	{
		try
		{

			if (!yml.contains("notes"))
				return;

			List<Note> noteList;
			for (String player : yml.getConfigurationSection("notes").getKeys(false))
			{
				noteList = new ArrayList<>();
				for (String note : yml.getStringList("notes." + player))
					noteList.add(Note.unpackageNote(note));
				setNotes(player, noteList);
			}

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public boolean hasNotes(String playerName)
	{
		return notes.containsKey(playerName.toLowerCase());
	}

	/**
	 * Returns an empty list if player has no notes
	 */
	public List<Note> getNotes(String playerName)
	{
		return hasNotes(playerName) ? notes.get(playerName.toLowerCase()) : new ArrayList<Note>();
	}

	public void setNotes(String playerName, List<Note> noteList)
	{
		notes.put(playerName.toLowerCase(), noteList);
	}

	public void addNote(String playerName, Note note)
	{
		List<Note> current = getNotes(playerName);
		current.add(note);
		setNotes(playerName, current);
		saveNotes(playerName, current);
	}

	/**
	 * Returns true if a player already has a note with ONLY the same message, otherwise false
	 */
	public boolean hasNote(String playerName, Note note)
	{
		for (Note n : getNotes(playerName))
			if (n.getMessage().equalsIgnoreCase(note.getMessage())) return true;

		return false;
	}

	public void deleteNote(String playerName, Note note)
	{
		List<Note> current = getNotes(playerName);
		if (!current.contains(note))
			return;
		current.remove(note);
		setNotes(playerName, current);
		saveNotes(playerName, current);

	}


	private void saveNotes(String playerName, List<Note> noteList)
	{
		List<String> packaged = new ArrayList<String>();
		for (Note n : noteList)
			packaged.add(Note.packageNote(n));

		yml.set("notes." + playerName.toLowerCase(), packaged);
		save();
	}


}
