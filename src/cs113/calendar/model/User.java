package cs113.calendar.model;

import java.util.NoSuchElementException;
import java.util.SortedSet;

import cs113.calendar.util.IdenticalAppointmentException;
import cs113.calendar.util.ModelMismatchException;

/**
 * Single user of the Calendar application who is capable of attending
 * appointments. Each user is uniquely identified by a user ID, which is used
 * for authentication purposes and is non-uniquely identified by his or her full
 * name.
 * 
 * @author Michael Koval
 */
public abstract class User {
	/**
	 * Checks the equality of two user objects by comparing their unique user
	 * IDs. As user IDs are guaranteed to be unique, no other fields are
	 * compared.
	 * 
	 * @param obj object to which this user should be compared
	 * @return whether the two users are equal
	 */
	@Override
	public final boolean equals(Object obj) {
		if (obj instanceof User) {
			return getUserId() == ((User) obj).getUserId();
		}
		return false;
	}

	/**
	 * Gets this user's ID that uniquely identifies this user to the Calendar.
	 * 
	 * @return short string that uniquely identifies this user
	 */
	public abstract String getUserId();

	/**
	 * Gets the full name of this user as a string.
	 * 
	 * @return user's full name
	 */
	public abstract String getFullName();

	/**
	 * Returns a collection that contains all of appointments that this user is
	 * attending, sorted in their natural order. Appointments are in natural
	 * order when they are sorted in ascending order in the following priority:
	 * beginning, end, location, and description.
	 * 
	 * @return appointments this user is attending, sorted as above
	 */
	public abstract SortedSet<Appointment> getAppointments();

	/**
	 * Change the full name of this user as desired.
	 * 
	 * @param name user's new full name
	 */
	public abstract void setFullName(String name);

	/**
	 * Add the specified event to the list of events that this user is
	 * attending.
	 * 
	 * @param app appointment to be attended
	 * @throws ModelMismatchException when there is an attempt to add an
	 *             appointment that was created by another model type
	 * @throws IdenticalAppointmentException if this appointment is identical
	 *             to an existing one.
	 */
	public abstract void addAppointment(Appointment app)
			throws ModelMismatchException, IdenticalAppointmentException;

	/**
	 * Remove the specified appointment from the list of event that this user is
	 * attending. Throws a NoSuchElementException if this user is not attending
	 * the specified appointment.
	 * 
	 * @param app appointment to remove
	 * @throws NoSuchElementException if the specified element is non-existent
	 */
	public abstract void removeAppointment(Appointment app)
			throws NoSuchElementException;
}
