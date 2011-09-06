
package cs113.calendar.util;

/**
 * User ID is the same as one that already exists.
 * 
 * @author Owen Healy
 */
public class DuplicateUserIdException extends Exception {
	
	private static final long serialVersionUID = 3190242090L;
	
	/**
	 * The user ID.
	 */
	private String userID;
	
	/**
	 * @param userID Theh user ID that existed before.
	 */
	public DuplicateUserIdException(String userID) {
		super("User " + userID + "exists");
		
		this.userID = userID;
	}
	
	/**
	 * @return The user ID that existed before.
	 */
	public String getUserID() {
		return userID;
	}
}
