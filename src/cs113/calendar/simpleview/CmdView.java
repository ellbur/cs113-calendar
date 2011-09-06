
package cs113.calendar.simpleview;

import java.io.File;

import cs113.calendar.control.Controller;
import cs113.calendar.model.Backend;
import cs113.calendar.model.SerializableBackend;
import cs113.calendar.util.StorageException;
import cs113.calendar.view.View;

/**
 * This is the main class for the command line view. It acts as a wrapper
 * for SimpleView.
 * 
 * @author Owen Healy
 */
public class CmdView {
	// ----------------------------------------------------------------------
	// ----------------------------------------------------------------------
	// MAIN
	
	/**
	 * Entry point for the program.
	 * Invokes SimpleView.runView.
	 * 
	 * Also constructs an appropriate model and controller, and passes them
	 * to the view.
	 */
	public static void main(String[] args) {
		Backend backend = null;
		
		String dirPath = "data";
		File dir = new File(dirPath);
		
		if (!dir.exists()) {
			try {
				dir.mkdirs();
			}
			catch (SecurityException se) {
				System.out.printf("Failed to create working directory: %s\n",
					se.toString());
				return;
			}
		}
		
		try {
			backend = new SerializableBackend("data");
		}
		catch (StorageException se) {
			System.out.printf("Error loading backend: %s\n", se.toString());
			return;
		}
		
		Controller controller = new Controller(backend);
		View view = new SimpleView();
		
		view.runView(controller, args);
	}
}
