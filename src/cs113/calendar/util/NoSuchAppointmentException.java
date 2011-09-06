
package cs113.calendar.util;

import cs113.calendar.model.*;

/**
 * The appointment you are looking for does not exist.
 * 
 * @author Owen Healy
 */
public class NoSuchAppointmentException extends Exception {
	
	private static final long serialVersionUID = 147250L;
	
	/**
	 * The appointment being hunted for.
	 */
	private Appointment appointment;
	
	/**
	 * @param apppointment The appointment that didn't exist.
	 */
	public NoSuchAppointmentException(Appointment apppointment) {
		super("Appointment does not exist");
		
		this.appointment = apppointment;
	}
	
	/**
	 * @return The appointment that didn't exist.
	 */
	Appointment getAppointment() {
		return appointment;
	}
}
