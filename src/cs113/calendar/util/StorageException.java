
package cs113.calendar.util;

/**
 * Something went wrong in the backend. Usually an IO issue.
 * 
 * @author Owen Healy
 */
public class StorageException extends Exception {
	
	private static final long serialVersionUID = 952430522;
	
	/**
	 * @param description description of the problem.
	 */
	public StorageException(String description) {
		super(description);
	}
}
