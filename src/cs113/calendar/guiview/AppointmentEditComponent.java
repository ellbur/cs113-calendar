package cs113.calendar.guiview;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cs113.calendar.control.Controller;
import cs113.calendar.model.Appointment;
import cs113.calendar.util.IdenticalAppointmentException;
import cs113.calendar.util.InvalidDateException;
import cs113.calendar.util.NoSuchAppointmentException;

/**
 * Controls that allow a user to edit an already-present event. In addition,
 * these controls allow the user to delete an event that was previously created.
 * 
 * @author Michael Koval
 */
public class AppointmentEditComponent extends JPanel {
	private static final long serialVersionUID = -3200217137624001811L;

	/**
	 * Date format used for input and display of Calendars.
	 */
	public static final String DATE_FORMAT = "MM/dd/yyyy HH:mm";

	/**
	 * Prompt appearing in the description text field.
	 */
	public static final String PROMPT_DESCRIPTION = "description";
	/**
	 * Prompt appearing in the location text field.
	 */
	public static final String PROMPT_LOCATION = "location";
	/**
	 * Prompt appearing in the starting time text field.
	 */
	public static final String PROMPT_START = DATE_FORMAT.toLowerCase();
	/**
	 * Prompt appearing in the ending time text field.
	 */
	public static final String PROMPT_END = DATE_FORMAT.toLowerCase();

	/**
	 * Label associated with the description text field.
	 */
	public static final String LABEL_DESCRIPTION = "Description: ";
	/**
	 * Label associated with the location text field.
	 */
	public static final String LABEL_LOCATION = "Location: ";
	/**
	 * Label associated with the starting time text field.
	 */
	public static final String LABEL_STARTS = "from ";
	/**
	 * Label associated with the ending time text field.
	 */
	public static final String LABEL_ENDS = "until ";
	/**
	 * Label used on the button used to delete an event.
	 */
	public static final String LABEL_DELETE = "Delete";

	/**
	 * Generic error message that cites an invalid field.
	 */
	public static final String MSG_ERROR = "Error: %s is invalid.";
	/**
	 * Error message if the starting time does not precede the ending time.
	 */
	public static final String MSG_ERROR_PRECEEDS = "Error: Modified appointment's ending time must occur after its beginning time.";
	/**
	 * Error message occurring if the addition of a coincident event is attempted.
	 */
	public static final String MSG_COINCIDENT = "Error: Modified appointment is coincident with another appointment. Changes will not be saved.";
	/**
	 * Warning to notify the user of conflicting events.
	 */
	public static final String MSG_CONFLICT = "Warning: Modified appointment conflicts with %d other appointments.";

	/**
	 * Controller responsible for interfacing with the Model.
	 */
	private Controller ctrl;
	/**
	 * Global application state.
	 */
	private ViewState state;
	/**
	 * Text field used for accepting input from the user.
	 */
	private LabeledTextField desc, loc, start, end;
	/**
	 * Button used to delete the current appointment.
	 */
	private JButton delete;
	/**
	 * Unedited appointment, preserved until the user selects a different
	 * appointment.
	 */
	private Appointment original;
	/**
	 * Edited appointment, potentially in an invalid state.
	 */
	private Appointment editing;
	/**
	 * Indicates some form of invalid data in the user-changeable input fields.
	 */
	private boolean error;
	/**
	 * Flag to prevent recursive event firing.
	 */
	private boolean fired;

	/**
	 * Responsible for updating the internal state of this component in response
	 * to changes in appointment selection by the user and other components.
	 * 
	 * @author Michael Koval
	 */
	private class StateListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			// This event listener is only interested in a change in the
			// currently selected appointment.
			if (original == state.getAppointment()) {
				return;
			}

			// Prevent recursive events from being fired. Oddly, it is very
			// important that this occurs after the above check.
			if (fired) {
				return;
			}
			fired = true;

			// Warning for future self: Think very deeply before modifying any
			// of the following conditions. They are quite delicate.

			// Method of checking if this is a just-created event that should be
			// deleted if invalid. Note that the method employed for checking
			// this condition is a huge, brittle hack. We should probably fix
			// this if there's time (yeah, right).
			boolean justCreated = (editing != null)
					&& editing.getDescription().isEmpty();

			// Save the previous appointment (if the user was smart enough to
			// correct errors before de-selecting it). Note that a new
			// appointment is created to generate the required events (as
			// opposed to just changing properties, which fires no events).
			if (!error && !justCreated && editing != null) {
				// Delete the old appointment prior to adding the current
				// appointment to prevent a potential conflict.
				try {
					if (original != null) {
						ctrl.deleteAppointment(original);
					}
				} catch (NoSuchAppointmentException ex) {
					// This should never happen. If it does happen, who cares?
					// We were trying to delete it anyway!
				}

				// Add the current appointment. If this fails, we're SOL.
				try {
					ctrl.addAppointment(editing);
				} catch (IdenticalAppointmentException e1) {
					// This should also never happen, but who knows!?
					state.setError("Error: An unknown error has occurred.");
				}
			}
			// Throw away all the user's hard work if he or she did not complete
			// all of the required fields in a newly created event. He or she
			// should be more careful next time!
			else if (original != null
					&& (original.getDescription().isEmpty() || justCreated)) {
				try {
					ctrl.deleteAppointment(original);
				} catch (NoSuchAppointmentException ex) {
					// Should never happen, but who cares? We were going to
					// delete it anyway!
				}

				// Update the global state to trigger YACE.
				state.setAppointment(null);
			}

			// Clear any errors that may have been set by this component.
			state.setError(null);
			state.setWarning(null);
			// The preceding two lines are EXTREMELY IMPORTANT. Without these
			// lines, no change event is fired upon the modification of the
			// event. If these two lines were to precede the above event
			// modification, no such update would occur. We should probably make
			// this more explicit, if there is time (yeah, right).

			// Update all the fields if the selected appointment changes.
			SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
			original = state.getAppointment();

			// Nothing selected; nothing to see here.
			if (original == null) {
				editing = null;

				desc.setText("");
				loc.setText("");
				start.setText("");
				end.setText("");
			} else {
				editing = (Appointment) original.clone();

				desc.setText(editing.getDescription());
				loc.setText(editing.getLocation());
				start.setText(formatter
						.format(editing.getStartTime().getTime()));
				end.setText(formatter.format(editing.getEndTime().getTime()));
			}

			refreshEnabled();
			fired = false;
		}
	}

	/**
	 * Monitors for a change in a text field to check the new data for potential
	 * errors.
	 * 
	 * @author Michael Koval
	 */
	private class EditListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			// Prevent a deluge of events from being fired when the user selects
			// an event (as a ton of fields change at once).
			if (fired) {
				return;
			}
			fired = true;

			// Assume that the starting data is correct. That will quickly be
			// proven incorrect.
			error = false;
			state.setError(null);
			state.setWarning(null);

			// Verify the contents of the text fields.
			if (desc.getText().isEmpty()) {
				error = true;
				state.setError(String.format(MSG_ERROR, "Description"));
				fired = false;
				return;
			} else if (loc.getText().isEmpty()) {
				error = true;
				state.setError(String.format(MSG_ERROR, "Location"));
				fired = false;
				return;
			}

			// Parse start and end dates into a Calendar object.
			SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
			Calendar parsedStart = new GregorianCalendar();
			Calendar parsedEnd = new GregorianCalendar();

			try {
				parsedStart.setTime(formatter.parse(start.getText()));
			} catch (ParseException ex) {
				error = true;
				state.setError(String.format(MSG_ERROR, "Start time"));
				fired = false;
				return;
			}

			try {
				parsedEnd.setTime(formatter.parse(end.getText()));
			} catch (ParseException ex) {
				error = true;
				state.setError(String.format(MSG_ERROR, "End time"));
				fired = false;
				return;
			}

			// Even if the starting and ending dates are valid separately, the
			// pair can be invalid (Oh joy). This occurs if the starting date
			// does not precede the ending date (i.e. ending date is coincident
			// or earlier than the starting date).
			try {
				editing.setDuration(parsedStart, parsedEnd);
			} catch (InvalidDateException ex) {
				error = true;
				state.setError(MSG_ERROR_PRECEEDS);
			}

			// Oops. The input failed basic sanity checks. Do not pass Go, do
			// not collect $200.
			if (error) {
				fired = false;
				return;
			}

			editing.setDescription(desc.getText());
			editing.setLocation(loc.getText());

			// Ensure that the modified event is still valid.
			boolean isValid = ctrl.isModificationValid(original, editing);
			List<Appointment> conflicts = ctrl.findConflicts(editing);

			// Conflicting with the old event is not a problem, as the old event
			// will be removed.
			conflicts.remove(original);

			// Error-free, we're good to go!
			if (isValid && conflicts.isEmpty()) {
			}
			// There is no error, but this appointment overlaps another
			// appointment (i.e. conflict).
			else if (isValid && !conflicts.isEmpty()) {
				state.setWarning(String.format(MSG_CONFLICT, conflicts.size()));
			}
			// This event exactly overlaps another event (i.e. is coincident
			// with another event). What are the odds of that?
			else {
				error = true;
				state.setError(MSG_COINCIDENT);
			}

			fired = false;
		}
	}

	private class DeleteListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// This should never happen. Even if it does, we're going to pretend
			// it didn't to avoid printing a stack trace to the unsuspecting
			// user.
			if (original == null) {
				return;
			}

			try {
				ctrl.deleteAppointment(original);
			} catch (NoSuchAppointmentException ex) {
				// Never should happen, but if it does...Who cares? We were
				// going to delete the appointment anyway!
			}

			// Clear any remaining error messages caused by this event.
			state.setError(null);
			state.setWarning(null);

			// Trigger YACE (yet another change event), forcing the GUI to
			// redraw itself.
			original = null;
			editing = null;
			state.setAppointment(null);
		}
	}

	/**
	 * Initialize all of the fields necessary to edit an appointment (i.e.
	 * description, location, start time, end time, and a delete button).
	 * 
	 * @param state global application state
	 */
	public AppointmentEditComponent(Controller ctrl, ViewState state) {
		if (ctrl == null) {
			throw new NullPointerException("Controller must be non-null");
		} else if (state == null) {
			throw new NullPointerException("Global state must be non-null");
		}
		this.ctrl = ctrl;
		this.state = state;

		setLayout(new GridBagLayout());
		GridBagConstraints c;

		// Description.
		JLabel ldesc = new JLabel(LABEL_DESCRIPTION);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.0;
		c.anchor = GridBagConstraints.EAST;
		add(ldesc, c);

		desc = new LabeledTextField(PROMPT_DESCRIPTION, 20);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(desc, c);

		// Location.
		JLabel lloc = new JLabel(LABEL_LOCATION);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 0.0;
		c.anchor = GridBagConstraints.EAST;
		add(lloc, c);

		loc = new LabeledTextField(PROMPT_LOCATION, 20);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(loc, c);

		// Start date-time.
		JLabel lstart = new JLabel(LABEL_STARTS);
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 0;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.EAST;
		add(lstart, c);

		start = new LabeledTextField(PROMPT_START, 12);
		c = new GridBagConstraints();
		c.gridx = 3;
		c.gridy = 0;
		c.weightx = 0.0;
		add(start, c);

		// End date-time.
		JLabel lends = new JLabel(LABEL_ENDS);
		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 1;
		c.weightx = 0.0;
		c.anchor = GridBagConstraints.EAST;
		add(lends, c);

		end = new LabeledTextField(PROMPT_END, 12);
		c = new GridBagConstraints();
		c.gridx = 3;
		c.gridy = 1;
		c.weightx = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(end, c);

		// Huge delete button.
		delete = new JButton(LABEL_DELETE);
		c.gridx = 4;
		c.gridy = 0;
		c.gridwidth = 0;
		c.gridheight = 2;
		c.weightx = 0.0;
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.VERTICAL;
		c.insets = new Insets(5, 10, 5, 10);
		add(delete, c);

		// Correctly set the initial enabled/disabled state of the object.
		refreshEnabled();

		// Keep error messages in sync with the state of the inputs.
		ChangeListener listener = new EditListener();
		desc.addChangeListener(listener);
		loc.addChangeListener(listener);
		start.addChangeListener(listener);
		end.addChangeListener(listener);

		// Update the state of this object to match the global application
		// state.
		state.addChangeListener(new StateListener());

		// Delete the current appointment when the user clicks "Delete."
		delete.addActionListener(new DeleteListener());
	}

	/**
	 * Enable or disable this component and all of its controls.
	 */
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		refreshEnabled();
	}

	/**
	 * Update the enabled status of all sub-controls to match the global
	 * application state.
	 */
	protected void refreshEnabled() {
		boolean enabled = isEnabled();
		boolean authorized = state.getState() != ViewState.State.UNAUTHORIZED;
		boolean selection = state.getAppointment() != null;

		desc.setEnabled(enabled && authorized && selection);
		loc.setEnabled(enabled && authorized && selection);
		start.setEnabled(enabled && authorized && selection);
		end.setEnabled(enabled && authorized && selection);
		delete.setEnabled(enabled && authorized && selection);
	}
}
