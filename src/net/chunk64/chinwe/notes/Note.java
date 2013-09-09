package net.chunk64.chinwe.notes;

import org.bukkit.ChatColor;

public class Note
{
	private String message, creator;
	private long time;

	public Note(String message, String creator)
	{
		this.message = message;
		this.creator = creator;
		this.time = System.currentTimeMillis();
	}

	public Note(String message, String creator, long time)
	{
		this.message = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', message));
		this.creator = creator;
		this.time = time;
	}

	public String getMessage()
	{
		return message;
	}

	public long getTime()
	{
		return time;
	}

	public void setTime(long time)
	{
		this.time = time;
	}

	public String getCreator()
	{
		return creator;
	}

	public String toString()
	{
		return packageNote(this);
	}

	/**
	 * Packages up note for storage in format time|creator|message
	 */
	public static String packageNote(Note note)
	{
		return note.getTime() + ";" + note.getCreator() + ";" + note.getMessage();
	}

	public static Note unpackageNote(String packagedNote)
	{
		String[] split = packagedNote.split(";");
		return new Note(split[2], split[1], Long.parseLong(split[0]));

	}
}
