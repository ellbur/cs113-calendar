package cs113.calendar.model;

import java.util.Calendar;
import java.util.NoSuchElementException;
import java.util.Set;

import cs113.calendar.util.DuplicateUserIdException;
import cs113.calendar.util.InvalidDateException;
import cs113.calendar.util.ModelMismatchException;
import cs113.calendar.util.StorageException;

/**
 * Loads and stores all application data. Concrete implementations of this
 * interface define where the data is actually stored. Constructing code is
 * responsible for invoking the writeAll() method prior to destroying this
 * object or exiting the program.
 * 
 * @author Michael Koval
 */
public interface Backend {
	/**
	 * Constructs an abstract User object of the subclass corresponding with
	 * this model. Does not add this user to the model unless explicitly stored
	 * with the writeUser() method.
	 * 
	 * @param id id that uniquely identifies the newly-created user
	 * @param name full name of the newly-created user
	 * @return new user object
	 */
	User createUser(String id, String name) throws DuplicateUserIdException,
			NullPointerException;

	/**
	 * Constructs an abstract Appointment object of the subclass corresponding
	 * with this model. Does not add this appointment to the model unless
	 * explicitly added to a user object.
	 * 
	 * @param desc short description of this appointment or an empty string to
	 *            indicate that there is no description
	 * @param loc location where this appointment occurs or <code>null</code> if
	 *            this appointment is not related to any specific location
	 * @param start starting date and time of the appointment
	 * @param end ending date and time of the appointment, must occur after the
	 *            starting time of the appointment
	 * @return new appointment object
	 * @throws InvalidDateException if the start time occurs after the end time
	 *             of this appointment.
	 */
	Appointment createAppointment(String desc, String loc, Calendar start,
			Calendar end) throws NullPointerException, InvalidDateException;

	/**
	 * Gets a list of all existing user IDs stored to disk.
	 * 
	 * @return list of existing user IDs
	 */
	Set<String> getUserIds();

	/**
	 * Read a user from storage from disk into memory based upon the user's id.
	 * 
	 * @param id user id that uniquely identifies the desired user
	 * @return user object associated with the given user id
	 * @throws NoSuchElementException thrown if there is no user with the desired
	 *             user id
	 * @throws StorageException if there is a problem storing it.
	 */
	User readUser(String id) throws StorageException, NoSuchElementException;

	/**
	 * Writes the provided user to a persistent data source. This method is
	 * implementation-defined and the write operation may or may not be
	 * immediately executed. The implicit contract of this method, however,
	 * guarantees that the user will be written to disk before the application
	 * terminates.
	 * 
	 * @param user user to store in the persistent data source.
	 * @throws StorageException if there is a problem storing it.
	 */
	void writeUser(User user) throws StorageException, ModelMismatchException;

	/**
	 * Forces all data writes that may be queued by the writeUser() method to be
	 * written to the persistent data source.
	 * 
	 * @throws StorageException if there is a problem writing stuff.
	 */
	void writeAll() throws StorageException;

	/**
	 * Remove the provided user object from the disk's session-persistent
	 * storage.
	 * 
	 * @param user user to delete from disk
	 */
	void deleteUser(User user) throws ModelMismatchException;
}
