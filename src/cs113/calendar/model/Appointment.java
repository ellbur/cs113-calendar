package cs113.calendar.model;

import java.util.Calendar;

import cs113.calendar.util.InvalidDateException;

/**
 * An appointment with an arbitrary description, location, starting time, and
 * ending time that are created by and attended by users.
 * 
 * @author Michael Koval
 */
public abstract class Appointment implements Cloneable, Comparable<Appointment> {
	/**
	 * Force child concrete implementations to be Cloneable.
	 */
	@Override
	public abstract Object clone();
	
	/**
	 * Two events are equal if and only if the description, location, start
	 * time, and end time are the same.
	 */
	public final boolean equals(Object obj) {
		if (obj instanceof Appointment) {
			Appointment app = (Appointment) obj;
			return compareTo(app) == 0;
		}
		return false;
	}

	/**
	 * The natural order of appointments is defined using the following
	 * priority: Start time, end time, location, and description.
	 * 
	 * @throws NullPointerException if the parameter is <code>null</code>, as
	 *             defined in the specification of the
	 *             <code>Compareable<T></code> interface.
	 * @see Comparable#compareTo(Object)
	 */
	@Override
	public final int compareTo(Appointment obj) throws NullPointerException {
		// Avoid undefined behavior when comparing the null appointment.
		if (obj == null) {
			throw new NullPointerException(
					"Unable to impose a total order on a set that contains null Appointments.");
		}
		// Compare the objects with the priority: Start Time > End Time >
		// Location > Description.
		else if (!getStartTime().equals(obj.getStartTime())) {
			return getStartTime().compareTo(obj.getStartTime());
		} else if (!getEndTime().equals(obj.getEndTime())) {
			return getEndTime().compareTo(obj.getEndTime());
		} else if (!getLocation().equals(obj.getLocation())) {
			return getLocation().compareTo(obj.getLocation());
		} else if (!getDescription().equals(obj.getDescription())) {
			return getDescription().compareTo(obj.getDescription());
		}
		// If all relevant attributes are equal, the objects are considered to
		// be of equal value.
		else {
			return 0;
		}
	}

	/**
	 * Gets the arbitrary description associated with this appointment.
	 * 
	 * @return arbitrary description of this appointment
	 */
	public abstract String getDescription();

	/**
	 * Gets the location at which this appointment will be held.
	 * 
	 * @return location at which this appointment will be held
	 */
	public abstract String getLocation();

	/**
	 * Date and time at which this appointment begins.
	 * 
	 * @return beginning of this appointment
	 */
	public abstract Calendar getStartTime();

	/**
	 * Date and time at which this appointment ends.
	 * 
	 * @return end of this appointment
	 */
	public abstract Calendar getEndTime();

	/**
	 * Sets the description of this appointment to an arbitrary value. Must not
	 * be null: events with no description should return an empty string.
	 * 
	 * @param desc new arbitrary description
	 */
	public abstract void setDescription(String desc);

	/**
	 * Sets the location of this appointment. Null if this appointment does not
	 * occur at any discrete location.
	 * 
	 * @param loc new location at which this event is occurring
	 */
	public abstract void setLocation(String loc);

	/**
	 * Changes the start and ending times of this event. An event's ending time
	 * must be after the event's starting time.
	 * 
	 * @param start beginning of this appointment
	 * @param end end of this appointment
	 */
	public abstract void setDuration(Calendar start, Calendar end) throws InvalidDateException;
}
