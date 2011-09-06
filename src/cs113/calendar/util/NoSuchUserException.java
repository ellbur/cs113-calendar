
package cs113.calendar.util;

/**
 * The user ID you requested does not exist.
 * 
 * @author Owen Healy
 */
public class NoSuchUserException extends Exception {
	
	private static final long serialVersionUID = 47503574L;
	
	/**
	 * The elusive user ID.
	 */
	private String userID;
	
	/**
	 * @param userID The user ID.
	 */
	public NoSuchUserException(String userID) {
		super("No such user: " + userID);
	}
	
	/**
	 * @return The mising user ID.
	 */
	public String getUserID() {
		return userID;
	}
}
