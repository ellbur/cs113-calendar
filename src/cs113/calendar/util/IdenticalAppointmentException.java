
package cs113.calendar.util;

/**
 * 
 * Thrown when an appointment cannot be added because it is
 * the same as an existing one.
 * 
 * @author Owen Healy
 * 
 */
public class IdenticalAppointmentException extends Exception {
	
	private static final long serialVersionUID = -2982599530007799574L;
	
	/**
	 * Constructs an IdenticalAppointmentException.
	 */
	public IdenticalAppointmentException() {
		super("Identical appointments");
	}
}
