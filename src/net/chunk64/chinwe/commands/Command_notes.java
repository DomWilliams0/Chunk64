package net.chunk64.chinwe.commands;

import net.chunk64.chinwe.Chunk64;
import net.chunk64.chinwe.notes.Note;
import net.chunk64.chinwe.notes.NotesManager;
import net.chunk64.chinwe.util.C64Utils;
import net.chunk64.chinwe.util.CommandUtils;
import net.chunk64.chinwe.util.TimeParser;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.*;

public class Command_notes implements CommandExecutor
{

	public static HashMap<String, SortedMap<Integer, String>> notesPages = new HashMap<>();
	int pageLength = 10;

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
					CommandUtils.sendHelp(sender, "Player Notes Help", label, ";View this help menu", "read <playername>;View the notes about a player", "read [page number];Browse pages", "add <playername> <note>;Write a note", "del <player> <first few letters/words of note>\n;Delete a note");
					return true;
				}

				NotesManager manager = Chunk64.noteManager;

				if (args.length >= 2)
				{
					String playerName = args[1];

					// Read
					if (args[0].equalsIgnoreCase("read") && args.length == 2)
					{

						// List
						if (!C64Utils.isInteger(playerName))
						{
							if (!manager.hasNotes(playerName))
								throw new IllegalArgumentException(playerName + " has no notes to read!");

							HashMap<Integer, String> notes = new HashMap<>();
							int tempId = 1;
							for (Note n : manager.getNotes(playerName))
							{
								notes.put(tempId, TimeParser.parseLong(System.currentTimeMillis() - n.getTime(), true) + ";" + n.getCreator() + ";" + n.getMessage());
								tempId++;
							}

							String format = "&8[&3%d&8] &6%s &b: &bwritten by &6%s\n&c + &r%s";

							// Format:
							// [integer] time : written by SENDER
							//      - note

							SortedMap<Integer, String> notesToPage = new TreeMap<Integer, String>();

							Iterator<Integer> it = notes.keySet().iterator();
							int id, count = 0;
							String time, creator, note;
							while (it.hasNext())
							{
								id = it.next();
								count++;

								String[] split = notes.get(id).split(";");
								time = split[0];
								creator = split[1];
								note = split[2];

								notesToPage.put(count, ChatColor.translateAlternateColorCodes('&', String.format(format, id, time, creator, note)));
							}

							paginate(sender, playerName, notesToPage, 1, pageLength);

							notesPages.put(sender.getName(), notesToPage);
						}

						// Page
						else
						{
							int page = Integer.parseInt(playerName);

							if (!notesPages.containsKey(sender.getName()))
								throw new IllegalArgumentException("You don't have any notes to page through!");

							paginate(sender, playerName, notesPages.get(sender.getName()), page, pageLength);
							return true;
						}

						return true;

					}

					// Add
					if (args[0].equalsIgnoreCase("add"))
					{

						String message = StringUtils.join(args, " ", 2, args.length);

						if (message.length() == 0)
							throw new IllegalArgumentException("Please specify a message!");

						if (message.contains(";"))
							throw new IllegalArgumentException("Please don't use semicolons (;) in your notes!");


						Note note = new Note(message, sender.getName());

						if (manager.hasNote(playerName, note))
						{
							throw new IllegalArgumentException(playerName + " already has that note!");
						}

						manager.addNote(playerName, note);

						String shortenedMessage = C64Utils.limitString(message);
						C64Utils.message(sender, "&bYou added a new note to &6" + playerName + "&b: " + shortenedMessage);
						C64Utils.notifyOps(sender.getName() + " added a note to " + playerName + ": " + shortenedMessage, sender.getName());
						return true;
					}

					// Delete
					if (args[0].equalsIgnoreCase("del"))
					{

						if (args.length == 2)
							throw new IllegalArgumentException("Please specify the first few letters/words of a note to delete it!");

						List<Note> playersNotes = manager.getNotes(playerName);

						if (playersNotes.size() == 0)
							throw new IllegalArgumentException("There are no notes to delete!");

						String start = StringUtils.join(args, " ", 2, args.length);
						Note note = null;
						for (Note n : playersNotes)
							if (n.getMessage().toLowerCase().startsWith(start.toLowerCase()))
								note = n;

						if (note == null)
							throw new IllegalArgumentException("There were no notes beginning with \"" + start + "\"!");

						manager.deleteNote(playerName, note);

						String shortenedMessage = C64Utils.limitString(note.getMessage());
						C64Utils.message(sender, "&bYou deleted the note about &6" + playerName + "&b: " + shortenedMessage);
						C64Utils.notifyOps(sender.getName() + " deleted a note about" + playerName + ": " + shortenedMessage, sender.getName());
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
			CommandUtils.sendUsage(sender, cmd, true);
			return true;
		}

		return true;
	}

	// Written by gomeow <3
	private void paginate(CommandSender sender, String playerName, SortedMap<Integer, String> map, int page, int pageLength)
	{
		if (page == 1)
		{
			C64Utils.message(sender, ChatColor.GRAY + "------------------------");
			C64Utils.message(sender, ChatColor.translateAlternateColorCodes('&', "&bShowing &6" + map.size() + "&b " + (map.size() == 1 ? "note" : "notes") + " for " + playerName));
		}
		C64Utils.message(sender, ChatColor.GRAY + "------------");
		C64Utils.message(sender, ChatColor.translateAlternateColorCodes('&', "&bNotes&6: &bPage &b(&6" + String.valueOf(page) + " &bof&6 " +
				(((map.size() % pageLength) == 0) ? map.size() / pageLength : (map.size() / pageLength) + 1) + "&b)"));
		int i = 0, k = 0;
		page--;
		for (final Map.Entry<Integer, String> e : map.entrySet())
		{
			k++;
			if ((((page * pageLength) + i + 1) == k) && (k != ((page * pageLength) + pageLength + 1)))
			{
				i++;
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', e.getValue()));
			}
		}

		C64Utils.message(sender, ChatColor.GRAY + "------------------------");
	/*
	 * USAGE
	 * Collections.reverseOrder() is optional.
	 * It makes it so the highest numbers get shown first.
	 * Otherwise the lowest number will be shown first.
	 */
		// SortedMap<Integer, String> map = new TreeMap<Integer, String>(Collections.reverseOrder());
		// map.put(1, "Thing");
	/*
	 * The first parameter is the rank, and the second parameter is the text to be shown.
	 * The SortedMap will automatically sort it.
	 * Add to that map for as many values there are in the list you want to show.
	 */
		// paginate(sender, map, 1, 5);
	/*
	 * Pass the first parameter as the sender.
	 * The second parameter as the map.
	 * The third as the page number.
	 * The fourth as how long each page should be.
	 */
	}

}
