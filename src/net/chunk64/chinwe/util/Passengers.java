package net.chunk64.chinwe.util;

import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class Passengers
{
	public static List<Entity> getPassengers(Entity mount)
	{

		Entity passenger = mount.getPassenger();
		List<Entity> currentPassengers = new ArrayList<Entity>();
		if (passenger != null)
		{
			currentPassengers.add(passenger);
			while (true)
			{
				if (passenger.getPassenger() == null) break;
				else passenger = passenger.getPassenger();

				currentPassengers.add(passenger);
			}
		}

		return currentPassengers;
	}

	public static Entity getTopPassenger(Entity mount)
	{
		Entity passenger = mount.getPassenger(), finalPassenger;
		if (passenger != null)
		{
			while (true)
			{
				if (passenger.getPassenger() == null)
				{
					finalPassenger = passenger;
					break;
				} else passenger = passenger.getPassenger();
			}
		} else finalPassenger = mount;

		return finalPassenger;
	}

	public static void eject(Entity entity)
	{
		if (entity.getVehicle() != null)
			entity.getVehicle().eject();
	}

	public static Entity getMount(Entity entity)
	{
		Entity vehicle = entity.getVehicle(), mount;

		// Self
		if (vehicle == null)
			return entity;

		while (true)
		{
			if (vehicle.getVehicle() == null)
			{
				mount = vehicle;
				break;
			} else vehicle = vehicle.getVehicle();
		}

		return mount;
	}

	public static void addPassenger(Entity rider, Entity mount) throws IllegalArgumentException
	{
		Entity top = Passengers.getTopPassenger(mount);

		if (top == rider) throw new IllegalArgumentException("An entity cannot ride itself!");
		if (Passengers.getPassengers(mount).contains(rider))
			throw new IllegalArgumentException("That entity is already a passenger!");
		//		if (rider == getMount(rider))
		//			throw new IllegalArgumentException("That entity cannot mount that entity!");


		eject(rider);
		top.setPassenger(rider);
	}


	/**
	 * Eject either a specific entity, or all riders
	 *
	 * @param rider Entity to eject, null for all
	 * @param mount The mount
	 */
	public static void ejectPassenger(Entity rider, Entity mount)
	{
		if (rider != null) Passengers.eject(rider);
		else for (Entity e : Passengers.getPassengers(mount))
			Passengers.eject(e);
	}

}


