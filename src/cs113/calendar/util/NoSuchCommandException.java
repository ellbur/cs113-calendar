
package cs113.calendar.util;

/**
 * Thrown when CommandLineEngine can't find the command we asked for. That's
 * probably the user's fault.
 * 
 * @author Owen Healy
 */
public class NoSuchCommandException extends Exception {
	
	private static final long serialVersionUID = -5180108671684201942L;
	
	/**
	 * Name of the command.
	 */
	private String command;
	
	/**
	 * Constructs a new NoSuchCommandException.
	 * 
	 * @param name The name of the command the user tried to run.
	 */
	public NoSuchCommandException(String command) {
		super("Do not have command called " + command);
		
		this.command = command;
	}
	
	/**
	 * Get the name of the command the user tried to run.
	 * 
	 * @return The name of the command.
	 */
	public String getCommand() {
		return command;
	}
}
