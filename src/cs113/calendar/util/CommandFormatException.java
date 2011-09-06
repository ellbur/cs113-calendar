
package cs113.calendar.util;

/**
 * Thrown when a user-entered command does not conform to the expected
 * format.
 * 
 * @author Owen Healy
 */
public class CommandFormatException extends Exception {
	
	private static final long serialVersionUID = 7530745L;
	
	/**
	 * The text that was not conforming.
	 */
	private String invalidString;
	
	/**
	 * How we wanted it.
	 */
	private String expectedFormat;
	
	/**
	 * @param invalidString The text that was not conforming.
	 * @param expectedFormat How we wanted it.
	 */
	public CommandFormatException(String invalidString, String expectedFormat)
	{
		super("Command does not conform to expected format");
		
		this.invalidString  = invalidString;
		this.expectedFormat = expectedFormat;
	}
	
	/**
	 * @return The text that was not conforming.
	 */
	String getInvalidString() {
		return invalidString;
	}
	
	/**
	 * @return The desired format.
	 */
	String getExpectedFormat() {
		return expectedFormat;
	}
}
