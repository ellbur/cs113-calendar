
package cs113.calendar.util;

/**
 * An incorrectly formatted string date.
 * 
 * @author Owen Healy
 */
public class DateFormatException extends Exception {
	
	private static final long serialVersionUID = 750324L;
	
	/**
	 * The string that was not properly formatted.
	 */
	private String badString;
	
	/**
	 * The format we wanted it in.
	 */
	private String expectedFormat;
	
	/**
	 * Constructs a DateFormatException
	 * 
	 * @param badString the string we don't like.
	 * @param expectedFormat Human-readable description of the desired format.
	 */
	public DateFormatException(String badString, String expectedFormat) {
		this.badString      = badString;
		this.expectedFormat = expectedFormat;
	}
	
	/**
	 * @return The string that failed the format.
	 */
	public String getBadString () {
		return badString;
	}
	
	/**
	 * @return A human-readable description of the desired format.
	 */
	public String getExpectedFormat() {
		return expectedFormat;
	}
}
