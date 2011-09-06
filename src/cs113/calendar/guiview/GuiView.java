package cs113.calendar.guiview;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cs113.calendar.control.Controller;
import cs113.calendar.model.Appointment;
import cs113.calendar.model.Backend;
import cs113.calendar.model.SerializableBackend;
import cs113.calendar.util.StorageException;

/**
 * Solitary window in the application used for all input and output.
 * 
 * @author Michael Koval
 */
public class GuiView extends JFrame {
	private static final long serialVersionUID = 2432613033351007603L;

	/**
	 * Window title while in the unauthorized state (i.e. not yet logged in).
	 */
	public static final String WINDOW_TITLE_UNAUTHORIZED = "Calendar - Login";
	/**
	 * Window title while in the month view.
	 */
	public static final String WINDOW_TITLE_MONTH = "Calendar - %s";
	/**
	 * Window title while in the week view.
	 */
	public static final String WINDOW_TITLE_WEEK = "Calendar - Week of %s";
	/**
	 * Window title while in the day view.
	 */
	public static final String WINDOW_TITLE_DAY = "Calendar - %s";
	/**
	 * Window title while in the search view.
	 */
	public static final String WINDOW_TITLE_SEARCH = "Calendar - Searching (%d results)";
	/**
	 * Window title while in the admin view (i.e. user creation/deletion).
	 */
	public static final String WINDOW_TITLE_ADMIN = "Calendar - Administration";

	/**
	 * Controller used to create, delete, search, and manipulate data.
	 */
	private Controller ctrl;
	/**
	 * Global state of the application's view.
	 */
	private ViewState state;
	/**
	 * Component that allows unauthorized users to log in and logged-in users to
	 * log out.
	 */
	private LoginComponent comp_login;
	/**
	 * Swap between the three major view states: Month, Week, and Day views.
	 */
	private StateSelectionComponent comp_states;
	/**
	 * Search for an event with the desired description or location.
	 */
	private SearchComponent comp_search;

	/**
	 * Primary graphical display of the calendar (i.e. "The Area").
	 */
	private MainArea comp_area;
	/**
	 * Fields used to edit an already-existing event.
	 */
	private AppointmentEditComponent comp_edit;

	/**
	 * Tells the user when something goes horribly, terribly wrong.
	 */
	private FeedbackComponent comp_feedback;

	/**
	 * Force the controller to write any pending data prior to the application's
	 * exit.
	 * 
	 * @author Michael Koval
	 */
	private class WindowCloseListener implements WindowListener {
		@Override
		public void windowActivated(WindowEvent e) {
		}

		@Override
		public void windowClosed(WindowEvent e) {

		}

		@Override
		public void windowClosing(WindowEvent e) {
			// One last change event before we slip away....
			state.setAppointment(null);
			
			// Save any updated data in the case of session-persistence.
			try {
				ctrl.writeAll();
			} catch (StorageException err) {
				JOptionPane.showMessageDialog(GuiView.this,
						"Unable to save data.");
			}
		}

		@Override
		public void windowDeactivated(WindowEvent e) {
		}

		@Override
		public void windowDeiconified(WindowEvent e) {
		}

		@Override
		public void windowIconified(WindowEvent e) {
		}

		@Override
		public void windowOpened(WindowEvent e) {
		}
	}

	/**
	 * Listens for changes in the global application state and updates the
	 * window's title accordingly.
	 * 
	 * @author Michael Koval
	 */
	private class StateListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			switch (state.getState()) {
			case UNAUTHORIZED: {
				setTitle(WINDOW_TITLE_UNAUTHORIZED);
				break;
			}
			case MONTH: {
				SimpleDateFormat formatter = new SimpleDateFormat("MMMM yyyy");
				Date day = state.getDate().getTime();
				setTitle(String.format(WINDOW_TITLE_MONTH, formatter
						.format(day)));
				break;
			}
			case WEEK: {
				SimpleDateFormat formatter = new SimpleDateFormat(
						"MMMM d, yyyy");

				// Find the first day in the week.
				GregorianCalendar day = (GregorianCalendar) state.getDate()
						.clone();
				int startOfWeek = day.getFirstDayOfWeek();
				while (day.get(Calendar.DAY_OF_WEEK) != startOfWeek) {
					day.add(Calendar.DATE, -1);
				}

				setTitle(String.format(WINDOW_TITLE_WEEK, formatter.format(day
						.getTime())));
				break;
			}
			case DAY: {
				SimpleDateFormat formatter = new SimpleDateFormat(
						"MMMM d, yyyy");
				setTitle(String.format(WINDOW_TITLE_DAY, formatter.format(state
						.getDate().getTime())));
				break;
			}
			case SEARCH:
				int num = 0;
				Collection<Appointment> results = state.getSearchResults();

				if (results != null) {
					num = results.size();
				}

				setTitle(String.format(WINDOW_TITLE_SEARCH, num));
				break;
			case ADMIN:
				setTitle(WINDOW_TITLE_ADMIN);
				break;
			}
		}
	}

	/**
	 * Create a new calendar window.
	 * 
	 * @param ctrl controller responsible for interfacing with the model
	 * @param state global application state monitored for changes
	 */
	public GuiView(Controller ctrl, ViewState state) {
		super(WINDOW_TITLE_UNAUTHORIZED);
		setSize(800, 600);

		if (ctrl == null) {
			throw new NullPointerException("Controller must be non-null");
		} else if (state == null) {
			throw new NullPointerException("Global state must be non-null.");
		}
		this.ctrl = ctrl;
		this.state = state;

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new GridBagLayout());
		GridBagConstraints c;

		// Login field.
		comp_login = new LoginComponent(ctrl, state, ViewState.State.MONTH);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(10, 20, 10, 5);
		add(comp_login, c);

		// Month, Week, or Day state selector.
		Map<ViewState.State, String> options = new HashMap<ViewState.State, String>();
		options.put(ViewState.State.MONTH, "Month");
		options.put(ViewState.State.WEEK, "Week");
		options.put(ViewState.State.DAY, "Day");
		comp_states = new StateSelectionComponent(state, options);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		add(comp_states, c);

		// Search field.
		comp_search = new SearchComponent(ctrl, state);
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(10, 5, 10, 20);
		add(comp_search, c);

		// Primary context-sensitive display area.
		comp_area = new MainArea(state, ctrl);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.BOTH;
		comp_area.setBackground(Color.BLACK);
		add(comp_area, c);

		// Appointment editing fields.
		comp_edit = new AppointmentEditComponent(ctrl, state);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(5, 20, 5, 20);
		add(comp_edit, c);

		comp_feedback = new FeedbackComponent(state);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.weightx = 0;
		c.weighty = 0;
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(comp_feedback, c);

		// Save data when this window is closed.
		addWindowListener(new WindowCloseListener());

		// Update the title of this window to match the current state.
		state.addChangeListener(new StateListener());
	}

	/**
	 * Instantiate a GuiView object, starting graphical interaction with the
	 * program.
	 * 
	 * @param args not applicable
	 */
	public static void main(String[] args) {
		// Create a backend object to manage all data persistence.
		Backend backend;
		try {
			backend = new SerializableBackend("data");
		} catch (StorageException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(),
					"Error Loading Data", ERROR);
			return;
		}

		// Manipulate the model only through the controller object.
		Controller ctrl = new Controller(backend);
		ViewState state = new ViewState(ctrl);

		state.setDate(new GregorianCalendar());

		JFrame window = new GuiView(ctrl, state);
		window.setVisible(true);
	}
}
