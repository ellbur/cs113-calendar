package cs113.calendar.model;

import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;

import cs113.calendar.util.IdenticalAppointmentException;

/**
 * Concrete implementation of the User interface, using the Serializable
 * interface for session-persistent storage.
 * 
 * @author Michael Koval
 */
public class SerializableUser extends User implements Serializable {
	private static final long serialVersionUID = -3151465357141715008L;

	private String id;
	private String name;
	private SortedSet<Appointment> apps;

	/**
	 * Constructs a session-persistent user that is stored via the Serializable
	 * interface. Once this user is constructed, his or her unique user id is
	 * immutable.
	 * 
	 * @param id short string that uniquely identifies this user
	 * @param name full name of this user
	 * @throws NullPointerException if the user id or name is <code>null</code>
	 */
	protected SerializableUser(String id, String name)
			throws NullPointerException {
		if (id == null) {
			throw new NullPointerException("User ID must be non-null.");
		}

		this.apps = new TreeSet<Appointment>();
		this.id = id;
		setFullName(name);
	}

	/**
	 * @throws IdenticalAppointmentException if this appointment shares the same
	 *             name, start time, end time, and location as another
	 *             appointment that this user was previously attending
	 * @see cs113.calendar.model.User#addAppointment(cs113.calendar.model.Appointment)
	 */
	@Override
	public void addAppointment(Appointment app)
			throws IdenticalAppointmentException {
		if (apps.contains(app)) {
			throw new IdenticalAppointmentException();
		}
		apps.add(app);
	}

	/**
	 * @see cs113.calendar.model.User#getAppointments()
	 */
	@Override
	public SortedSet<Appointment> getAppointments() {
		return apps;
	}

	/**
	 * @see cs113.calendar.model.User#getFullName()
	 */
	@Override
	public String getFullName() {
		return name;
	}

	/**
	 * @see cs113.calendar.model.User#getUserId()
	 */
	@Override
	public String getUserId() {
		return id;
	}

	/**
	 * @throws NoSuchElementException if this user is not attending
	 *             <code>app</code>
	 * @see cs113.calendar.model.User#removeAppointment(cs113.calendar.model.Appointment
	 *      )
	 */
	@Override
	public void removeAppointment(Appointment app)
			throws NoSuchElementException {
		boolean success = apps.remove(app);
		if (!success) {
			throw new NoSuchElementException(
					"User is not attending the appointment.");
		}
	}

	/**
	 * @throws NullPointerException if the new full name is <code>null</code>
	 * @see cs113.calendar.model.User#setFullName(java.lang.String)
	 */
	@Override
	public void setFullName(String name) {
		if (name == null) {
			throw new NullPointerException("Full name must be non-null.");
		}
		this.name = name;
	}

}
