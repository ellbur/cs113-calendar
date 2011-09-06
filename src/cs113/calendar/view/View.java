
package cs113.calendar.view;

import cs113.calendar.control.*;

public interface View {
	
	/**
	 * Starts the view running. This method is not expected to return any
	 * time soon, but the caller should be able to handle the case that it
	 * does return before the view becomes obsolete.
	 * 
	 * @param controller The Controller on which the view should operate.
	 * @param args Command line arguments.
	 */
	public void runView(Controller controller, String[] args);
	
}
