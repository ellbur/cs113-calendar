package cs113.calendar.guiview;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cs113.calendar.control.Controller;
import cs113.calendar.guiview.ViewState.State;

/**
 * Allows a user to graphically log in or logout using a simple set of text
 * controls and buttons. Automatically updates to respond to application state
 * (i.e. will always be in sync with the associated <code>ViewState</code>
 * object). While external changes to the application state <strong>are
 * supported</strong>, be wary of the state of the controller when changing the
 * application state, as it must match the authentication state of the global
 * application state for proper operation.
 * 
 * @author Michael Koval
 */
public class LoginComponent extends JPanel {
	private static final long serialVersionUID = 1214119665100516922L;

	/**
	 * Text displayed in the login field when it is unused (i.e. contains no
	 * user input).
	 */
	public final String LOGIN_PROMPT = "login";
	/**
	 * Text displayed on the logout button.
	 */
	public final String LOGOUT_PROMPT = "logout";
	/**
	 * Error message displayed after an invalid login attempt to notify the user
	 * of the authentication failure.
	 */
	public final String LOGIN_ERROR = "<html><strong>No user exists with that ID.</strong></html>";
	/**
	 * Description of the current user logged in. Use %s to represent the
	 * current user ID.
	 */
	public final String LOGOUT_DESC = "<html>Logged in as <u>%s</u>.</html>";
	/**
	 * Maximum length of any user ID displayed by this component.
	 */
	public final int USERID_MAX_LENGTH = 10;

	/**
	 * Controller responsible for validating login information and logging a
	 * user in.
	 */
	private Controller ctrl;

	/**
	 * Panels used to wrap login and logout controls. This allows for efficient
	 * swapping between the two modes of operation.
	 */
	private JPanel login, logout;
	/**
	 * Input where the user can input his or her user ID.
	 */
	private LabeledTextField field;
	/**
	 * Graphical method of submitting the login form.
	 */
	private JButton loginButton;
	/**
	 * Graphical method of logging out from the current user's account.
	 */
	private JButton logoutButton;
	/**
	 * Element in which any error messages can be displayed.
	 */
	private JLabel error;
	/**
	 * Label used to display the ID under which the current user is logged in
	 * as.
	 */
	private JLabel currentUser;
	/**
	 * Global application state, modified when the user logs in or logs out.
	 */
	private ViewState state;
	/**
	 * State that a newly authenticated user should be placed in.
	 */
	private ViewState.State defaultState;

	/**
	 * Listen for changes made to the contents of the login text field. Whenever
	 * the user changes this value any error messages should be cleared to
	 * prevent confusion.
	 * 
	 * @author Michael Koval
	 */
	private class ValueChangeListener implements ChangeListener {
		/**
		 * Clear all error messages if the user edits the text field. Also
		 * disables submission if the text field is empty.
		 */
		@Override
		public void stateChanged(ChangeEvent e) {
			hideError();
			loginButton.setEnabled(!field.getText().isEmpty());
		}
	}

	/**
	 * Listen for the user submitting the firm via pressing the enter key in the
	 * text field.
	 * 
	 * @author Michael Koval
	 */
	private class KeyboardSubmissionListener implements ActionListener {
		/**
		 * Pass the login attempt (i.e. form submission) to the button event
		 * handler. This is more elegant than manually submitting the form as it
		 * will briefly show the button in a depressed state. This notifies the
		 * user that the submission has been processed.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			loginButton.doClick();
		}
	}

	/**
	 * Listen for a click of the submission button, either by the user or via
	 * the <code>doClick()</code> method. This triggers an attempted login with
	 * the user ID specified by the contents of the text field.
	 * 
	 * @author Michael Koval
	 */
	private class SubmissionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			boolean isSuccessful = true;
			
			// TODO: Apologize to Mike for this bit of code.
			if (field.getText().equals("admin")) {
				state.setState(ViewState.State.ADMIN);
				return;
			}
			
			try {
				ctrl.login(field.getText());
			}
			// Set a flag that enables the display of a generic error message.
			catch (Exception e1) {
				// TODO: Display a different error message if there is an
				// unexpected StorageException.
				isSuccessful = false;
			}

			// Change the application state to reflect the user's new
			// permissions.
			if (isSuccessful) {
				// Reset the state of the login field.
				field.setText("");
				
				state.setState(defaultState);
			}
			// Print a generic error message about being unable to log in.
			else {
				showError();
			}
		}
	}

	/**
	 * Monitor the global application state for external changes to the
	 * application's status.
	 * 
	 * @author Michael Koval
	 */
	private class ViewStateListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			if (state.getState() == State.UNAUTHORIZED) {
				enterLoginState();
			} else {
				enterLogoutState();
			}
		}
	}

	/**
	 * Updates the state of the application and GUI when the user logs out via
	 * the logout button.
	 * 
	 * @author Michael Koval
	 */
	private class LogoutListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			ctrl.logout();

			setEnabled(isEnabled());

			state.setState(ViewState.State.UNAUTHORIZED);
		}
	}

	/**
	 * Constructs a new <code>LoginComponent</code> that uses <code>ctrl</code>
	 * to authenticate login attempts against the Model. Users are notified of
	 * invalid login attempts by a small red error message below the text field.
	 * 
	 * @param ctrl controller used to authenticate login attempts
	 * @param state wrapper around the global application state
	 * @param start state in which a newly logged-in user should begin
	 */
	public LoginComponent(Controller ctrl, ViewState state,
			ViewState.State start) {
		if (ctrl == null) {
			throw new NullPointerException("Controller must be non-null.");
		} else if (state == null) {
			throw new NullPointerException("Global state must be non-null.");
		} else if (start == null) {
			throw new NullPointerException("Starting state must be non-null.");
		}

		this.ctrl = ctrl;
		this.state = state;
		this.defaultState = start;

		CardLayout layout = new CardLayout();
		setLayout(layout);

		// Login view (displayed when the user is not logged in).
		login = new JPanel();
		{
			login.setLayout(new GridBagLayout());
			GridBagConstraints c;

			// Component used to enter the desired user's ID.
			field = new LabeledTextField(LOGIN_PROMPT, 15);
			c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 0;
			c.weightx = 1.0;
			c.fill = GridBagConstraints.HORIZONTAL;
			login.add(field, c);
			field.setMaximumSize(new Dimension(100, 30));

			// Button that allows the user to submit the form without use of the
			// keyboard (i.e. enter/return key). Disabled until the user enters
			// some text.
			loginButton = new JButton(LOGIN_PROMPT);
			c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = 0;
			c.weightx = 0.0;
			loginButton.setEnabled(false);
			login.add(loginButton, c);

			// Label displayed to alert the user of an invalid login attempt.
			error = new JLabel();
			c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 1;
			c.gridwidth = 2;
			c.insets = new Insets(5, 10, 5, 10);
			c.fill = GridBagConstraints.HORIZONTAL;
			error.setForeground(Color.RED);
			hideError();
			login.add(error, c);

			// Update the visibility of the error message and the enabled status
			// of the message based upon the input in the text field.
			field.addActionListener(new KeyboardSubmissionListener());
			field.addChangeListener(new ValueChangeListener());
			loginButton.addActionListener(new SubmissionListener());
		}

		// Logout view (displayed when the user is logged in).
		logout = new JPanel();
		{
			logout.setLayout(new GridBagLayout());
			GridBagConstraints c;

			// Create a dummy user id that contains the maximum number of wide
			// characters. This will ensure that this component is always of the
			// correct minimum size.
			// TODO: Replace this hack with a legitmate way of sizing this
			// component upon its creation.
			String dummy = "";
			for (int i = 0; i < USERID_MAX_LENGTH; ++i) {
				dummy += 'M';
			}

			// Description of the currently logged-in user.
			currentUser = new JLabel(String.format(LOGOUT_DESC, dummy));
			c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 0;
			c.weightx = 1;
			c.fill = GridBagConstraints.CENTER;
			c.insets = new Insets(0, 0, 10, 0);
			logout.add(currentUser, c);

			// Logout button.
			logoutButton = new JButton(LOGOUT_PROMPT);
			c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 1;
			c.fill = GridBagConstraints.NONE;
			c.anchor = GridBagConstraints.CENTER;
			logout.add(logoutButton, c);

			// Logout when the user presses the button.
			logoutButton.addActionListener(new LogoutListener());
		}

		add(login, "login");
		add(logout, "logout");

		// Put the component into the correct initial state (most likely
		// UNAUTHORIZED, if the application was initialized normally).
		if (state.getState() == ViewState.State.UNAUTHORIZED) {
			enterLoginState();
		} else {
			enterLogoutState();
		}

		// Update the state of this component when the state of the user
		// changes.
		state.addChangeListener(new ViewStateListener());
	}

	/**
	 * Check whether the current user is logged in using the controller
	 * associated with this component.
	 * 
	 * @return whether the current user is logged in.
	 */
	public boolean isLoggedIn() {
		try {
			ctrl.getCurrentUserID();
		} catch (IllegalStateException e) {
			return false;
		}
		return true;
	}

	/**
	 * Prevent the user from logging in via this control.
	 */
	@Override
	public void setEnabled(boolean enabled) {
		// Clear the current input if the enabled state changed.
		if (enabled != isEnabled()) {
			field.setText("");
			error.setVisible(false);
		}

		super.setEnabled(enabled);
		field.setEnabled(enabled);
		loginButton.setEnabled(!field.getText().isEmpty() && enabled);
		logoutButton.setEnabled(enabled);
	}

	/**
	 * Switch to the internal login state. This allows a user to enter his or
	 * her login information to authenticate his or herself. Despite the name of
	 * this method, this is called when the user <em>logs out</em>.
	 */
	private void enterLoginState() {
		hideError();
		field.clear();

		((CardLayout) getLayout()).show(this, "login");
	}

	/**
	 * Switch to the internal "logout" state. This allows an already-logged in
	 * user to logout, allowing the component to accept another user's login
	 * information. Despite the name of this method, it is called when the user
	 * <em>logs in</em>.
	 */
	private void enterLogoutState() {
		// TODO: Apologize to Mike
		if (state.getState() != ViewState.State.ADMIN) {
			String userid = ctrl.getCurrentUserID();
			currentUser.setText(String.format(LOGOUT_DESC, userid));
		}
		else {
			currentUser.setText("admin");
		}

		((CardLayout) getLayout()).show(this, "logout");
	}

	/**
	 * Display an error message to notify the user of an invalid login attempt.
	 */
	private void showError() {
		error.setText(LOGIN_ERROR);
	}

	/**
	 * Hide any error messages that may be currently displayed.
	 */
	private void hideError() {
		error.setText(" ");
	}
}
