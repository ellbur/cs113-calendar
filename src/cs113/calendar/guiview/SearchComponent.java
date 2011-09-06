package cs113.calendar.guiview;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cs113.calendar.control.Controller;

/**
 * Component responsible for monitoring updates to the search query and
 * notifying listening parties for potential changes in search results (i.e.
 * query changes). Actual searching occurs in the Search class.
 * 
 * @author Michael Koval
 */
public class SearchComponent extends JPanel {
	private static final long serialVersionUID = 2818154582778190278L;
	
	private static final String dateFormatString = "MM/dd/yyyy HH:mm";
	private static final DateFormat dateFormat =
		new SimpleDateFormat(dateFormatString);
	
	/**
	 * Text used to prompt the user to enter a search query.
	 */
	public static final String PROMPT = "search";
	/**
	 * Label used on the radio button to specify a description search.
	 */
	public static final String OPTION_DESCRIPTION = "Description";
	/**
	 * Label used on the radio button used to specify a location search.
	 */
	public static final String OPTION_LOCATION = "Location";

	/**
	 * Interface between this component and the application model.
	 */
	@SuppressWarnings("unused")
	private Controller ctrl;
	/**
	 * Global application state, used to notify other controls of entering and
	 * leaving search state.
	 */
	private ViewState state;
	/**
	 * State of the calendar prior to displaying search results.
	 */
	private ViewState.State prevState;
	
	private LabeledTextField startField;
	private LabeledTextField endField;
	
	/**
	 * Input used by the user to enter a search query.
	 */
	private LabeledTextField field;
	/**
	 * Force the two search types (description and location) to be mutually
	 * exclusive.
	 */
	private ButtonGroup group;
	/**
	 * Radio button for selecting the type of search to perform (i.e. searching
	 * the description or location attribute).
	 */
	private JRadioButton desc, loc;

	/**
	 * Notify all objects listening to this component of a potential change in
	 * search results. This occurs whenever the user changes the value of the
	 * text input.
	 * 
	 * @author Michael Koval
	 */
	private class QueryChangeListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			updateState();
		}
	}

	private class TypeChangeListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			updateState();
		}
	}

	private class GlobalStateListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			refreshEnabled();
		}
	}

	/**
	 * Creates a new search component that is capable of searching for a desired
	 * appointment description or location, as selected by one of two radio
	 * buttons.
	 * 
	 * @param ctrl controller used to execute the search
	 */
	public SearchComponent(Controller ctrl, ViewState state) {
		if (ctrl == null) {
			throw new NullPointerException("Controller must be non-null.");
		} else if (state == null) {
			throw new NullPointerException("Global state must be non-null.");
		}

		this.ctrl = ctrl;
		this.state = state;
		this.prevState = state.getState();

		field = new LabeledTextField(PROMPT, 20);
		desc = new JRadioButton(OPTION_DESCRIPTION);
		loc = new JRadioButton(OPTION_LOCATION);
		
		startField = new LabeledTextField("Start " + dateFormatString, 24);
		endField   = new LabeledTextField("End " + dateFormatString, 24);
		
		GregorianCalendar now = new GregorianCalendar();
		now.set(Calendar.HOUR_OF_DAY, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);
		
		Calendar start = (Calendar) now.clone();
		Calendar end   = (Calendar) now.clone();
		
		while (start.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)
			start.add(Calendar.DATE, -1);
		while (end.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)
			end.add(Calendar.DATE, 1);
		
		startField.setText(dateFormat.format(start.getTime()));
		endField.setText(dateFormat.format(end.getTime()));
		
		// Force the two radio buttons to be mutually exclusive.
		group = new ButtonGroup();
		group.add(desc);
		group.add(loc);
		desc.setSelected(true);

		// Prepare a window that consists of a search box above two radio
		// buttons. GridBagLayout is necessary as the search box should span the
		// horizontal space used by both adjacent radio buttons.
		GridBagConstraints c;
		setLayout(new GridBagLayout());
		
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(startField, c);
		
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(endField, c);
		
		// Search field.
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 2;
		c.gridx = 1;
		c.gridy = 0;
		add(field, c);

		// Description radio button.
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.NONE;
		c.gridx = 1;
		c.gridy = 1;
		add(desc, c);

		// Location radio button.
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.NONE;
		c.gridx = 2;
		c.gridy = 1;
		add(loc, c);

		state.addChangeListener(new GlobalStateListener());
		desc.addActionListener(new TypeChangeListener());
		loc.addActionListener(new TypeChangeListener());
		
		field.addChangeListener(new QueryChangeListener());
		startField.addChangeListener(new QueryChangeListener());
		endField.addChangeListener(new QueryChangeListener());

		refreshEnabled();
	}

	/**
	 * Disabled this SearchComponent, also clearing any currently-displayed
	 * search results.
	 */
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		refreshEnabled();
	}

	/**
	 * Refresh the enabled state all sub-components.
	 */
	protected void refreshEnabled() {
		boolean enabled = isEnabled();
		boolean auth = state.getState() != ViewState.State.UNAUTHORIZED;
		boolean admin = state.getState() == ViewState.State.ADMIN;
		
		boolean ok = enabled && auth && !admin;
		
		field.setEnabled(ok);
		desc.setEnabled(ok);
		loc.setEnabled(ok);
		startField.setEnabled(ok);
		endField.setEnabled(ok);

		if (!enabled || !auth || admin) {
			field.setText("");
		}
	}

	/**
	 * Change the global application state to reflect the current status of the
	 * search (i.e. <code>SEARCH</code> if a search is in progress or the
	 * previous state if no search is in progress).F
	 */
	private void updateState() {
		boolean blank = true;
		
		state.setError(null);
		state.setWarning(null);
		
		if (! field.getText().isEmpty())      blank = false;
		if (! startField.getText().isEmpty()) blank = false;
		if (! endField.getText().isEmpty())   blank = false;
		
		if (blank) {
			state.setState(prevState);
		} else if (state.getState() != ViewState.State.SEARCH) {
			prevState = state.getState();
			state.setState(ViewState.State.SEARCH);
		}
		
		boolean failAll = false;
		
		Calendar start = null;
		Calendar end   = null;
		
		try {
			start = makeDateWithFeedback(startField.getText());
			end   = makeDateWithFeedback(endField.getText());
		}
		catch (ParseException pe) {
			failAll = true;
		}
		
		if (start != null && end != null && end.before(start)) {
			failAll = true;
			state.setError("Start of range is after end of range");
		}
		
		state.setSearch(new Search(
			desc.isSelected() ? field.getText() : null,
			loc.isSelected() ? field.getText() : null,
			start, 
			end,
			failAll
		));
	}
	
	private Calendar makeDateWithFeedback(String text)
		throws ParseException
	{
		if (text.isEmpty()) return null;
		
		Calendar date = new GregorianCalendar();
		try {
			date.setTime(dateFormat.parse(text));
		}
		catch (ParseException pe) {
			state.setWarning("Do not understand " + text);
			throw pe;
		}
		
		return date;
	}
}
