
package cs113.calendar.simpleview;

import cs113.calendar.control.*;
import java.util.*;

/**
 * Invoked to handle a command line typed by the user. This may be either
 * passed to the program at its execution, or during interactive mode.
 * 
 * @author Owen Healy
 */
public interface CommandLineAction {
	
	/**
	 * Invokes the command.
	 * 
	 * @param controller A Controller on which to operate.
	 * @param args Arguments (not including name of the command).
	 */
	public void doAction(Controller controller, List<String> args);
	
}
