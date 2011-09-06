package cs113.calendar.model;

import java.io.Serializable;
import java.util.Calendar;

import cs113.calendar.util.InvalidDateException;

/**
 * Appointment stored as a serialized Java object.
 * 
 * @author Michael Koval
 * @see cs113.calendar.model.Appointment
 */
public class SerializableAppointment extends Appointment implements
		Cloneable, Serializable {
	private static final long serialVersionUID = -5356929817250890582L;

	private String description;
	private Calendar startTime, endTime;
	private String location;

	/**
	 * Make a value-copy of this SerializableAppointment, fulfilling the
	 * contractual obligation of the Appointment interface.
	 */
	@Override
	public Object clone() {
		Calendar newStart = (Calendar) startTime.clone();
		Calendar newEnd = (Calendar) endTime.clone();

		Appointment cloned = null;
		try {
			cloned = new SerializableAppointment(description, location,
					newStart, newEnd);
		} catch (InvalidDateException e) {
			// This will never occur; implicitly returns null.
		}
		return cloned;
	}

	/**
	 * Constructs a new appointment that is stored as a serialized Java object.
	 * 
	 * @param desc short description of this appointment or an empty string to
	 *            indicate that there is no description
	 * @param loc location where this appointment occurs or <code>null</code> if
	 *            this appointment is not related to any specific location
	 * @param start starting date and time of the appointment
	 * @param end ending date and time of the appointment, must occur after the
	 *            starting time of the appointment
	 * @throws NullPointerException if <code>desc</code>, <code>start</code>, or
	 *             <code>end</code> are null
	 * @throws IllegalArgumentException if the ending time of this appointment
	 *             does not occur after the starting time
	 */
	protected SerializableAppointment(String desc, String loc, Calendar start,
			Calendar end) throws InvalidDateException {
		// Use the explicit setters to avoid duplicate checks for validity.
		setDescription(desc);
		setLocation(loc);
		setDuration(start, end);
	}

	/**
	 * @see cs113.calendar.model.Appointment#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * @see cs113.calendar.model.Appointment#getEndTime()
	 */
	@Override
	public Calendar getEndTime() {
		return endTime;
	}

	/**
	 * @see cs113.calendar.model.Appointment#getLocation()
	 */
	@Override
	public String getLocation() {
		return location;
	}

	/**
	 * @see cs113.calendar.model.Appointment#getStartTime()
	 */
	@Override
	public Calendar getStartTime() {
		return startTime;
	}

	/**
	 * @throws NullPointerException if the new description is <code>null</code>
	 * @see cs113.calendar.model.Appointment#setDescription(java.lang.String)
	 */
	@Override
	public void setDescription(String desc) {
		if (desc == null) {
			throw new NullPointerException("Description must be non-null.");
		}

		this.description = desc;
	}

	/**
	 * @throws InvalidDateException if the starting time exceeds the ending time
	 * @throws NullPointerException if either time is <code>null</code>
	 * @see cs113.calendar.model.Appointment#setDuration(java.util.Calendar,
	 *      java.util.Calendar)
	 */
	@Override
	public void setDuration(Calendar start, Calendar end)
			throws InvalidDateException {
		if (start == null || end == null) {
			throw new NullPointerException(
					"Start and end times must be non-null.");
		} else if (end.before(start)) {
			throw new InvalidDateException(
					"Ending time must exceed the starting time.");
		}

		this.startTime = start;
		this.endTime = end;
	}

	/**
	 * @see cs113.calendar.model.Appointment#setLocation(String)
	 */
	@Override
	public void setLocation(String loc) {
		this.location = loc;
	}
}
