
package cs113.calendar.guiview;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import cs113.calendar.model.Appointment;

/**
 * Represents the current state of the SearchComponent. A search can select
 * for various things, each of which may or may not be present.
 * 
 * @author Owen Healy
 */
public class Search {
	
	/** Substring of appointment description. */
	private String description;
	/** Substring of appointment location. */
	private String location;
	
	/** Beginning of range. */
	private Calendar start;
	/** End of range. */
	private Calendar end;
	/** Match nothing. */
	private boolean failAll;
	
	/**
	 * Create a search and set all fields.
	 * 
	 * @param description Substring of description or null if no check.
	 * @param location Substring of location or null if no check.
	 * @param start Beginning of range or null if no check.
	 * @param end End of range or null if no check.
	 * @param failAll Match nothing.
	 */
	public Search(String description, String location,
			Calendar start, Calendar end, boolean failAll)
	{
		this.description  = description;
		this.location     = location;
		this.start        = start;
		this.end          = end;
		this.failAll      = failAll;
	}
	
	/**
	 * Apply this search to a collection of appointments and produce a subset.
	 * 
	 * @param input The starting collection.
	 * @return A matching subset.
	 */
	public ArrayList<Appointment> filterAppointments(
			Collection<Appointment> input)
	{
		ArrayList<Appointment> output = new ArrayList<Appointment>();
		
		for (Appointment app : input) {
			if (matches(app)) {
				output.add(app);
			}
		}
		
		return output;
	}
	
	/**
	 * Test if an appointment matches this search.
	 * 
	 * @param app An appointment to test.
	 * @return true if it matches, false if it does not.
	 */
	public boolean matches(Appointment app) {
		if (failAll) return false;
		
		if (description != null
				&& !app.getDescription().contains(description))
			return false;
		
		if (location != null
				&& !app.getLocation().contains(location))
			return false;
		
		if (start != null && !app.getEndTime().after(start))
			return false;
		
		if (end != null && !app.getStartTime().before(end))
			return false;
		
		return true;
	}
}
