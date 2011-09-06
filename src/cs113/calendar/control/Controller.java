package cs113.calendar.control;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import cs113.calendar.model.Appointment;
import cs113.calendar.model.Backend;
import cs113.calendar.model.User;
import cs113.calendar.util.DuplicateUserIdException;
import cs113.calendar.util.IdenticalAppointmentException;
import cs113.calendar.util.InvalidDateException;
import cs113.calendar.util.NoSuchAppointmentException;
import cs113.calendar.util.NoSuchUserException;
import cs113.calendar.util.StorageException;

/**
 * Controller is responsible for high-level manipulation. It receives a Backend
 * from wich it can construct model classes. The Backend provides factory
 * methods for this purpose. After changes have been made, calling writeAll will
 * cause changes to the backend to be saved.
 * 
 * @author Owen Healy
 */
public class Controller {

	/**
	 * Backend we're acting on.
	 */
	private Backend backend;

	/**
	 * User that has been logged in.
	 */
	private User currentUser;

	/**
	 * True if we have logged in, false otherwise.
	 */
	private boolean loggedIn;

	/**
	 * Creates a new controller with specified backend. Is ready for a session
	 * to begin.
	 * 
	 * @param backend Backend to use.
	 */
	public Controller(Backend backend) {
		this.backend = backend;
	}

	/**
	 * Writes all changes in the backend. Call this before exiting the
	 * application or bad things will happen.
	 * 
	 * @throws StorageException If something went wrong in storing.
	 */
	public void writeAll() throws StorageException {
		backend.writeAll();
	}

	/**
	 * Get all user IDs that we have stored.
	 * 
	 * @return A list of user ID strings.
	 */
	public Set<String> listUsers() {
		return backend.getUserIds();
	}

	/**
	 * Get a User identified by user id.
	 * 
	 * @param id A string identification of the user.
	 * @return A user object.
	 * @throws NoSuchUserException if we have no user with that ID.
	 * @throws StorageException if something goes wrong in the backend.
	 */
	public User getUser(String id) throws NoSuchUserException, StorageException {
		try {
			return backend.readUser(id);
		} catch (NoSuchElementException nsee) {
			throw new NoSuchUserException(id);
		} catch (StorageException se) {
			throw se;
		}
	}

	/**
	 * Creates a user with a given id.
	 * 
	 * @param id A string id of the user.
	 * @param name The name of the user.
	 * @throws DuplicateUserIdException if the user id already exists.
	 */
	public void createUser(String id, String name)
			throws DuplicateUserIdException, StorageException {
		User user = backend.createUser(id, name);
		backend.writeUser(user);
	}

	/**
	 * Removes the user with the specified id from the application.
	 * 
	 * @param id The user's id.
	 * @throws NoSuchUserException if that user ID does not exist.
	 */
	public void deleteUser(String id) throws NoSuchUserException,
			StorageException {
		User user = null;

		try {
			user = backend.readUser(id);
		} catch (NoSuchElementException nsee) {
			throw new NoSuchUserException(id);
		}

		backend.deleteUser(user);
	}

	/**
	 * Sets the currently active user to one specified by an id.
	 * 
	 * @param id The user's id.
	 * @throws NoSuchUserException if there is no user with that ID.
	 */
	public void login(String id) throws NoSuchUserException, StorageException {
		try {
			currentUser = backend.readUser(id);
			loggedIn = true;
		} catch (NoSuchElementException nsee) {
			throw new NoSuchUserException(id);
		}
	}

	/**
	 * Unsets the currently active user, so no user is active. If we are not
	 * logged in, no action is performed.
	 */
	public void logout() {
		currentUser = null;
		loggedIn = false;
	}

	/**
	 * Saves all data associated with the currently active user.
	 * 
	 * @throws IllegalStateException if we are not logged in.
	 */
	public void save() throws IllegalStateException, StorageException {
		if (!loggedIn) {
			throw new IllegalStateException("Not logged in");
		}

		backend.writeUser(currentUser);
	}

	/**
	 * Lists all appointments that overlap with the given range. The
	 * appointments are sorted by (in this order): - start date - end date -
	 * description - location
	 * 
	 * @param start Start of the range.
	 * @param end End of the range.
	 * @return A list of Appointments.
	 * @throws IllegalStateException if we are not logged in.
	 */
	public List<Appointment> listAppointmentsInRange(Calendar start,
			Calendar end) throws IllegalStateException {
		if (!loggedIn) {
			throw new IllegalStateException("not logged in");
		}

		LinkedList<Appointment> selectedAppointments = new LinkedList<Appointment>();

		Collection<Appointment> allAppointments = currentUser.getAppointments();

		for (Appointment app : allAppointments) {
			if (app.getStartTime().before(end)) {
				if (app.getEndTime().after(start)) {
					selectedAppointments.add(app);
				}
			}
		}

		Collections.sort(selectedAppointments);

		return selectedAppointments;
	}

	/**
	 * Creates a new appointment for the current user. If the appointment is
	 * identical to an existing one, that cannot be handled by the program and
	 * will produce an IdenticalAppointmentException.
	 * 
	 * @param appointment The appointment to be created
	 * @throws IdenticalAppointmentException If this appointment is the same as
	 *             one already there.
	 * @throws IllegalStateException if we are not logged in.
	 */
	public void addAppointment(Appointment appointment)
			throws IdenticalAppointmentException, IllegalStateException {
		if (!loggedIn) {
			throw new IllegalStateException("not logged in");
		}

		Collection<Appointment> allAppointments = currentUser.getAppointments();

		if (allAppointments.contains(appointment)) {
			throw new IdenticalAppointmentException();
		}

		currentUser.addAppointment(appointment);
	}

	/**
	 * Creates a new Appointment object but does not add it to the current user.
	 * This could be used to add an appointment like this:
	 * 
	 * <pre>
	 * Appointment app = controller.createAppointment(descr, loc, start, end);
	 * controller.addAppointment(app);
	 * </pre>
	 * 
	 * @param description String description of the appointment
	 * @param location String location of the appointment
	 * @param start Start time
	 * @param end End time
	 * @return An appointment object.
	 * @throws InvalidDateException If the start or end are not appropriate.
	 */
	public Appointment createAppointment(String description, String location,
			Calendar start, Calendar end) throws InvalidDateException {
		try {
			return backend.createAppointment(description, location, start, end);
		} catch (InvalidDateException ide) {
			throw ide;
		}
	}
	
	/**
	 * This method checks to see whether calling addAppointment(rep) will
	 * execute successfully after deleteAppointment(old) has been called. In
	 * other words it checks whether rep is legit.
	 * 
	 * Some checks will already have been performed on `rep` via
	 * createAppointment. These checks are NOT performed again by this method.
	 * 
	 * @param old An existing appointment.
	 * @param rep A modification to `old`.
	 * @return true if `old` map be replaced with `rep`, false otherwise.
	 * 
	 * @throws IllegalStateException If we are not logged in.
	 */
	public boolean isModificationValid(Appointment old, Appointment rep)
		throws IllegalStateException
	{
		if (! loggedIn) {
			throw new IllegalStateException("not logged in");
		}
		
		boolean ok = false;
		
		Collection<Appointment> allApps = currentUser.getAppointments();
		if (allApps.contains(rep) && !rep.equals(old)) {
			ok = false;
		}
		else {
			ok = true;
		}
		
		return ok;
	}

	/**
	 * If the current user has an appointment that compares equal to this one,
	 * then we will delete it. Otherwise, a NoSuchAppointmentException will be
	 * thrown.
	 * 
	 * @param appointment The appointment to delete.
	 * @throws NoSuchAppointmentException if the user does not have an
	 *             appointment like this.
	 * @throws IllegalStateException if we are not logged in.
	 */
	public void deleteAppointment(Appointment appointment)
			throws IllegalStateException, NoSuchAppointmentException {
		if (!loggedIn) {
			throw new IllegalStateException("not logged in");
		}

		try {
			currentUser.removeAppointment(appointment);
		} catch (NoSuchElementException nsee) {
			throw new NoSuchAppointmentException(appointment);
		}
	}

	/**
	 * Checks all of the current user's appointments for ones that overlap with
	 * this one.
	 * 
	 * @param appointment The appointment against which to check conflicts.
	 * @return A list of conflicting appointments for the current user.
	 * @throws IllegalStateException if we are not logged in.
	 */
	public List<Appointment> findConflicts(Appointment appointment)
			throws IllegalStateException {
		if (!loggedIn) {
			throw new IllegalStateException("Not logged in");
		}

		LinkedList<Appointment> list = new LinkedList<Appointment>();
		Collection<Appointment> allApps = currentUser.getAppointments();

		for (Appointment app : allApps) {
			if (app.getStartTime().before(appointment.getEndTime())) {
				if (app.getEndTime().after(appointment.getStartTime())) {
					list.add(app);
				}
			}
		}

		Collections.sort(list);

		return list;
	}

	/**
	 * Get the ID of the currently logged in user.
	 * 
	 * @return The String ID of the currently logged in user.
	 * @throws IllegalStateException if we are not logged in.
	 */
	public String getCurrentUserID() throws IllegalStateException {
		if (!loggedIn) {
			throw new IllegalStateException("not logged in");
		}

		return currentUser.getUserId();
	}
	
	/**
	 * List all appointments for the current user.
	 * 
	 * @return A collection of all appointments.
	 * 
	 * @throws IllegalStateException If we are not logged in.
	 */
	public Collection<Appointment> listAllAppointments()
		throws IllegalStateException
	{
		if (! loggedIn) {
			throw new IllegalStateException("not logged in");
		}
		
		return currentUser.getAppointments();
	}
}
