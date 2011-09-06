package cs113.calendar.guiview;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import cs113.calendar.control.Controller;
import cs113.calendar.model.Appointment;

/**
 * Singleton object used to represent the global state of the GUI. May take on
 * one of the states defined in the <code>State</code> enum.
 * 
 * @author Michael Koval
 */
public class ViewState {
	
	/** The poor change listeners. They don't know what they're getting
	 * themselves into.
	 * 
	 * This class produces lots of change events. You can't breath whithout
	 * someone noticing and firing a change event.
	 */
	private EventListenerList listeners;
	
	/** Application's controller. */
	private Controller controller;
	
	/** current error message; null if no error. */
	private String error;
	/** current warning message; null if no warning. */
	private String warning;
	/** current state. */
	private State state;
	/** Something inside the currently displayed time range. */
	private Calendar date;
	/** Currently edited appointment. */
	private Appointment appointment;
	/** Currently run search. */
	private Search search;
	
	/**
	 * Named states. The interface MUST be in exactly one of these states at any
	 * given time.
	 * 
	 * @author Michael Koval
	 */
	public enum State {
		/**
		 * Wall-calendar view of an entire month displayed in a grid. Does not
		 * show appointment duration.
		 */
		MONTH,
		/**
		 * Horizontal table that displays the seven days in the current week.
		 * Appointment size reflects duration.
		 */
		WEEK,
		/**
		 * Single day in the <code>WEEK</code> view expanded to fill the entire
		 * region.
		 */
		DAY,
		/**
		 * User is not validated and must login prior to interacting with the
		 * application
		 */
		UNAUTHORIZED,
		/**
		 * Special administrator interface used for creating and deleting users
		 */
		ADMIN,
		/**
		 * Displaying search results.
		 */
		SEARCH
	}

	/**
	 * Constructs a new ViewState, initialized to have a state of
	 * State.UNAUTHORIZED.
	 */
	public ViewState(Controller controller) {
		this.controller = controller;
		
		listeners = new EventListenerList();
		state = State.UNAUTHORIZED;
	}

	/**
	 * Change the state of the interface, notifying all eligible event
	 * listeners.
	 * 
	 * @param s new state of the interface
	 */
	public void setState(State s) {
		if (s == null) {
			throw new NullPointerException("State must be non-null.");
		}

		// Avoid issuing firing an event if the state did not change by ensuring
		// that we are not setting the state to its current value.
		if (s != state) {
			state = s;
			fireEvent(new ChangeEvent(this));
		}
	}
	
	/**
	 * Go into a week, month or day view identified by a time.
	 * 
	 * @param s The state to go into.
	 * @param date A time identifying the range to display.
	 */
	public void setState(State s, Calendar date) {
		if (s == null) {
			throw new NullPointerException("State must be non-null");
		}
		
		boolean changed = false;
		if (s != state || date != this.date)
			changed = true;
		
		this.state = s;
		this.date = date;
		
		if (changed) {
			fireEvent(new ChangeEvent(this));
		}
	}
	
	/**
	 * Change the currently displayed time range.
	 * 
	 * @param date Something inside the time range to move to.
	 */
	public void setDate(Calendar date) {
		boolean changed = false;

		if (date != this.date)
			changed = true;

		this.date = date;

		if (changed) {
			fireEvent(new ChangeEvent(this));
		}
	}

	/**
	 * Sets the currently selected appointment. Null if no appointment is
	 * selected.
	 * 
	 * @param app
	 */
	public void setAppointment(Appointment app) {
		this.appointment = app;

		fireEvent(new ChangeEvent(this));
	}

	/**
	 * Error message text should indicate a fatal error condition that requires
	 * corrective user action. Use a warning for non-fatal error conditions.
	 * 
	 * @param str message text or null, if no error occurred
	 */
	public void setError(String str) {
		boolean changed = isChange(error, str);
		error = str;
		
		if (changed)
			fireEvent(new ChangeEvent(this));
	}

	/**
	 * Warnings indicate a potentially undesirable condition that can be safely
	 * (or not) ignored.
	 * 
	 * @param str message text or null, if no warning is active
	 */
	public void setWarning(String str) {
		boolean changed = isChange(warning, str);
		warning = str;
		
		if (changed)
			fireEvent(new ChangeEvent(this));
	}

	/**
	 * @return current error message or null if there is no error
	 */
	public String getError() {
		return error;

	}

	/**
	 * @return string representation of the warning or null if there is no
	 *         warning
	 */
	public String getWarning() {
		return warning;
	}

	/**
	 * Gets the current state of the interface. Changed via the
	 * <code>setState()</code> method.
	 * 
	 * @return current state of the interface
	 */
	public State getState() {
		return state;
	}
	
	/**
	 * @return A time inside the currently displayed time range.
	 */
	public Calendar getDate() {
		return date;
	}

	/**
	 * Gets the currently selected appointment or null if no appointment is
	 * selected.
	 * 
	 * @return
	 */
	public Appointment getAppointment() {
		return appointment;
	}
	
	/**
	 * Get the current search.
	 * @return The current search. Undefined if we're not searching.
	 */
	public Search getSearch() {
		return search;
	}
	
	/**
	 * Get the current search results.
	 * @return The current search results. Undefined if we're not searching.
	 */
	public ArrayList<Appointment> getSearchResults() {
		if (search == null)
			return null;
		
		Collection<Appointment> allApps = controller.listAllAppointments();
		
		return search.filterAppointments(allApps);
	}
	
	/**
	 * Change the current search.
	 * @param search The search to move into.
	 */
	public void setSearch(Search search) {
		this.search = search;
		
		fireEvent(new ChangeEvent(this));
	}
	
	/**
	 * Listen to changes in the view state.
	 * @param listener A change listener.
	 */
	public void addChangeListener(ChangeListener listener) {
		listeners.add(ChangeListener.class, listener);
	}

	/**
	 * Notify all event listeners that the state of this object has changed.
	 * Called by the <code>setState()</code> method.
	 * 
	 * @param e event object to pass to the event handler
	 */
	protected void fireEvent(ChangeEvent e) {
		for (ChangeListener list : listeners.getListeners(ChangeListener.class)) {
			list.stateChanged(e);
		}
	}
	
	/**
	 * Check for changes between two possibly null strings.
	 * 
	 * @param before one String.
	 * @param after another String.
	 * @return true if args differ, false otherwise.
	 */
	private boolean isChange(String before, String after) {
		if (before == null && after == null)
			return false;
		else if (before == null || after == null)
			return true;
		else
			return !before.equals(after);
	}
}
