
package cs113.calendar.util;

/**
 * A date that was not meaninful. Something like December 46.
 * 
 * @author Owen Healy
 */
public class InvalidDateException extends Exception {
	
	/**
	 * What was wrong with the date.
	 */
	private String description;
	
	private static final long serialVersionUID = 5319024;
	
	/**
	 * @param description What was wrong with the date.
	 */
	public InvalidDateException(String description) {
		super(description);
		
		this.description = description;
	}
	
	/**
	 * @return A description of what was wrong with the date.
	 */
	public String getDescription() {
		return description;
	}
}
