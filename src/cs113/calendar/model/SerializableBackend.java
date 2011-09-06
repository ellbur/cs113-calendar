package cs113.calendar.model;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import cs113.calendar.util.DuplicateUserIdException;
import cs113.calendar.util.InvalidDateException;
import cs113.calendar.util.ModelMismatchException;
import cs113.calendar.util.StorageException;

/**
 * Session-persistent storage using Java's serialization facilities (i.e. the
 * Serializable interface). All objects stored in this model must implement the
 * Serializable interface (i.e. SerializableAppointment and SerializableUser).
 * 
 * @author Michael Koval
 * @see SerializableUser
 * @see SerializableAppointment
 */
public class SerializableBackend implements Backend {
	public static final String STORAGE_FILE = "users.ser";

	private File file;
	private Map<String, SerializableUser> users;

	/**
	 * @param dir directory in which to store serialized data
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public SerializableBackend(String dir) throws StorageException {
		if (dir == null) {
			throw new NullPointerException(
					"Storage directory must be non-null.");
		}
		file = new File(dir + File.separatorChar + STORAGE_FILE);

		// Read the stored data from this object without any validation.
		Object obj = null;
		if (file.exists()) {
			try {
				ObjectInputStream os = new ObjectInputStream(
						new FileInputStream(file));
				obj = os.readObject();
				os.close();
			} catch (EOFException e) {
				// Empty files are implicitly handled by a null object
				// reference.
			} catch (IOException e) {
				throw new StorageException(e.getMessage());
			} catch (ClassNotFoundException e) {
				throw new StorageException(e.getMessage());
			}
		}

		// Load a previously-stored data, as we know it is of the correct type.
		// Note that instanceof returns false if the object is null.
		if (obj instanceof Map<?, ?>) {
			users = (Map<String, SerializableUser>) obj;
		}
		// There's some unknown data written in the file.
		else if (obj != null) {
			throw new StorageException("Storage file " + STORAGE_FILE
					+ " is in an invalid format.");
		}
		// Create an empty data collection.
		else {
			users = new HashMap<String, SerializableUser>();
		}
	}

	/**
	 * Creates a new SerializableUser object, capable of being used with a
	 * SerializableBackend. Does not store the user without an explicit call to
	 * writeUser().
	 * 
	 * @see cs113.calendar.model.SerializableUser#SerializableUser(String,
	 *      String)
	 * @see cs113.calendar.model.Backend#createUser(String, String)
	 */
	@Override
	public User createUser(String id, String name)
			throws DuplicateUserIdException, NullPointerException {
		if (users.containsKey(id)) {
			throw new DuplicateUserIdException(id);
		}
		return new SerializableUser(id, name);
	}

	/**
	 * Creates a new SerializableAppointment object, capable of being added to a
	 * SerializableUser object. Does not store the user without explicitly being
	 * added to a user object.
	 * 
	 * @see cs113.calendar.model.SerializableAppointment#SerializableAppointment(String,
	 *      String, Calendar, Calendar)
	 * @see cs113.calendar.model.Backend#createAppointment(String, String,
	 *      Calendar, Calendar)
	 */
	@Override
	public Appointment createAppointment(String desc, String loc,
			Calendar start, Calendar end) throws InvalidDateException,
			NullPointerException {
		return new SerializableAppointment(desc, loc, start, end);
	}

	/**
	 * @see cs113.calendar.model.Backend#deleteUser(cs113.calendar.model.User)
	 */
	@Override
	public void deleteUser(User user) throws ModelMismatchException {
		User old = users.remove(user.getUserId());
		if (old == null) {
			throw new NoSuchElementException("User is not found.");
		}
	}

	/**
	 * @see cs113.calendar.model.Backend#getUserIds()
	 */
	@Override
	public Set<String> getUserIds() {
		return users.keySet();
	}

	/**
	 * @see cs113.calendar.model.Backend#readUser(java.lang.String)
	 */
	@Override
	public User readUser(String id) throws NoSuchElementException {
		User user = users.get(id);
		if (user == null) {
			throw new NoSuchElementException(
					"No user exists with the specified id.");
		}
		return user;
	}

	/**
	 * @see cs113.calendar.model.Backend#writeUser(cs113.calendar.model.User)
	 */
	@Override
	public void writeUser(User user) {
		if (user instanceof Serializable) {
			users.put(user.getUserId(), (SerializableUser) user);
		} else {
			throw new ModelMismatchException();
		}
	}

	/**
	 * @see cs113.calendar.model.Backend#writeAll()
	 */
	@Override
	public void writeAll() throws StorageException {
		try {

			ObjectOutputStream os = new ObjectOutputStream(
					new FileOutputStream(file));
			os.writeObject(users);
			os.close();
		} catch (IOException ioe) {
			throw new StorageException(ioe.toString());
		}
	}
}
