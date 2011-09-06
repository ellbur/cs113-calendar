
package cs113.calendar.guiview;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cs113.calendar.control.Controller;
import cs113.calendar.model.User;
import cs113.calendar.util.DuplicateUserIdException;
import cs113.calendar.util.NoSuchUserException;
import cs113.calendar.util.StorageException;

/**
 * AdminArea lets the user create and delete users. The user is presented
 * with a list of current users, and can delete each one, and has a button
 * to create a user.
 * 
 * @author Owen Healy
 */
public class AdminArea extends JPanel {
	
	private static final long serialVersionUID = 022525252L;
	
	/** Text describing a user. */
	private static final Font infoFont = new Font(null, Font.PLAIN, 12);
	
	/** Area background */
	private static final Color listBackgroundColor  = new Color(255, 255, 255);
	
	/** Row background */
	private static final Color rowBackgroundColor   = new Color(240, 240, 255);
	
	/** A label */
	private static final Color infoBackgroundColor  = new Color(240, 240, 255);
	
	/** Label text color */
	private static final Color infoForegroundColor  = new Color(  0,   0,   0);
	
	/** Gets us events about changing views */
	private ViewState viewState;
	
	/** Allows us access to users */
	private Controller controller;
	
	/** The place where the list goes */
	private ListPanel listPanel;
	
	/** Holds the create button */
	private JPanel lowerPanel;
	
	/** Create a new user */
	private JButton createButton;
	
	/** Where you type the stuff for the new user */
	private UserCreationRow currentCreation;
	
	/** Listen for changing input to clear the error label */
	private ClearMessageListener clearMessageListener;
	
	/**
	 * Create the admin area; set up list of users.
	 * 
	 * @param viewState The application's ViewState.
	 * @param controller The application's Controller.
	 */
	public AdminArea(ViewState viewState, Controller controller) {
		this.viewState  = viewState;
		this.controller = controller;
		
		clearMessageListener = new ClearMessageListener();
		
		listPanel = new ListPanel();
		listPanel.setBackground(listBackgroundColor);
		
		lowerPanel = new JPanel(new FlowLayout());
		
		createButton = new JButton("Create User");
		createButton.addActionListener(new CreateListener());
		createButton.addFocusListener(clearMessageListener);
		
		lowerPanel.add(createButton);
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(listPanel);
		add(lowerPanel);
		
		redoUsers();
	}
	
	/**
	 * Reconstruct the list of users.
	 * 
	 * If the currentCreation is non-null, it will be added to the
	 * end of the list.
	 */
	private void redoUsers() {
		listPanel.clear();
		
		for (String id : controller.listUsers()) {
			User user = null;
			
			try {
				user = controller.getUser(id);
			}
			catch (NoSuchUserException nsue) {
				// That really sucks.
			}
			catch (StorageException se) {
				// I..... that's bad. We need a way to handle this.
			}
			
			UserRow row = new UserRow(user);
			listPanel.addRow(row);
		}
		
		if (currentCreation != null) {
			listPanel.addRow(currentCreation);
		}
		
		listPanel.finish();
		
		validate();
		repaint();
		
		if (currentCreation != null) {
			listPanel.getScrollPane().getViewport().scrollRectToVisible(
					currentCreation.getBounds());
		}
	}
	
	/**
	 * Sets up user creation.
	 */
	private void createUser() {
		currentCreation = new UserCreationRow();
		createButton.setEnabled(false);
		
		redoUsers();
	}
	
	/**
	 * Gets rid of user creation.
	 */
	private void cancelCreateUser() {
		currentCreation = null;
		createButton.setEnabled(true);
		
		redoUsers();
	}
	
	/**
	 * Actually do the user creation. In response to somebody pressing enter.
	 * 
	 * If the user can't actually be created, put some error message in
	 * the status bar.
	 * 
	 * @param id The user ID.
	 * @param name The user's full name.
	 */
	private void createUser(String id, String name) {
		createButton.setEnabled(true);
		
		if (id.isEmpty()) {
			viewState.setError("Id is empty");
			return;
		}
		
		if (name.isEmpty()) {
			viewState.setError("Name is empty");
			return;
		}
		
		if (id.equals("admin")) {
			viewState.setError("Name admin is too special");
			return;
		}
		
		try {
			controller.createUser(id, name);
		}
		catch (StorageException se) {
			viewState.setError("Error in storage: " + se.getMessage());
		}
		catch (DuplicateUserIdException duie) {
			viewState.setError("User ID exists");
			return;
		}
		
		currentCreation = null;
		
		redoUsers();
	}
	
	/**
	 * Helper function to make a nice little label.
	 * 
	 * @param text The contents -- this is a printf-like format string.
	 * @param args The format parameters.
	 * 
	 * @return A JLabel with the given formatted text.
	 */
	private JLabel makeLabel(String text, Object ... args) {
		JLabel label = new JLabel(String.format(text, args));
		
		label.setFont(infoFont);
		label.setBackground(infoBackgroundColor);
		label.setForeground(infoForegroundColor);
		
		return label;
	}
	
	/**
	 * A row in the list of users.
	 */
	class UserRow extends JPanel {
		
		private static final long serialVersionUID = 25255072L;
		
		/**
		 * Create the row and add the labels.
		 * 
		 * @param user The user to display info for.
		 */
		UserRow(User user) {
			setBackground(rowBackgroundColor);
			
			JButton deleteButton = new JButton("Delete");
			deleteButton.addActionListener(new DeleteListener(user));
			
			setLayout(new GridBagLayout());
			GridBagConstraints cr = new GridBagConstraints();
			
			cr.gridy   = 0;
			cr.fill    = GridBagConstraints.NONE;
			cr.weightx = 0;
			cr.weighty = 100;
			cr.insets  = new Insets(0, 0, 0, 20);
			
			cr.anchor  = GridBagConstraints.EAST;
			add(makeLabel("ID:"), cr);
			
			cr.anchor  = GridBagConstraints.WEST;
			add(makeLabel("%s", user.getUserId()), cr);
			
			cr.anchor  = GridBagConstraints.EAST;
			add(makeLabel("Name:"), cr);
			
			cr.anchor  = GridBagConstraints.WEST;
			add(makeLabel("%s", user.getFullName()), cr);
			
			cr.weightx = 1.0;
			add(Box.createGlue(), cr);
			
			cr.weightx = 0.0;
			cr.anchor  = GridBagConstraints.EAST;
			add(deleteButton, cr);
		}
	}
	
	/**
	 * A place where the user can type in the info about a new user
	 * and then click "Create" and it gets created!!:):):)
	 */
	private class UserCreationRow extends JPanel {
		
		private static final long serialVersionUID = 523052347L;
		
		/** Place to type in the ID */
		private JTextField idField;
		/** Plate to type in the name */
		private JTextField nameField;
		
		/** Makes the creation take place */
		private JButton doneButton;
		/** Cancel doing the creation */
		private JButton cancelButton;
		
		/**
		 * Create the row and add the various components.
		 */
		UserCreationRow() {
			idField   = new LabeledTextField("id", 10);
			nameField = new LabeledTextField("Name", 16);
			
			doneButton   = new JButton("OK");
			cancelButton = new JButton("Cancel");
			
			idField.addActionListener(new DoneListener());
			nameField.addActionListener(new DoneListener());
			doneButton.addActionListener(new DoneListener());
			cancelButton.addActionListener(new CancelListener());
			
			idField.addFocusListener(clearMessageListener);
			nameField.addFocusListener(clearMessageListener);
			doneButton.addFocusListener(clearMessageListener);
			cancelButton.addFocusListener(clearMessageListener);
			
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			add(makeLabel("Id:"));
			add(idField);
			add(Box.createHorizontalStrut(20));
			
			add(makeLabel("Name:"));
			add(nameField);
			add(Box.createHorizontalStrut(20));
			
			add(doneButton);
			add(Box.createHorizontalStrut(20));
			
			add(cancelButton);
		}
		
		/**
		 * Listens for the user to press "Done".
		 */
		private class DoneListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent e) {
				String id   = idField.getText();
				String name = nameField.getText();
				
				createUser(id, name);
			}
		}
		
		/**
		 * Listens for the user to press "Cancel".
		 */
		private class CancelListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent e) {
				cancelCreateUser();
			}
		}
	}
	
	/**
	 * There are these buttons next to the rows of the list that say
	 * "Delete" and if you press it this listener gets invoked and deletes
	 * the user.
	 */
	private class DeleteListener implements ActionListener {
		
		/** The user that will get DELETED. */
		private User user;
		
		/**
		 * Create the listener.
		 * 
		 * @param user The user that will get deleted.
		 */
		DeleteListener(User user) {
			this.user = user;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				controller.deleteUser(user.getUserId());
			}
			catch (StorageException se) {
				// Damn.
			}
			catch (NoSuchUserException nsue) {
				// I guess that's OK
			}
			
			redoUsers();
		}
	}
	
	/**
	 * Listens for the button at the bottom that says "Create".
	 */
	private class CreateListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			createUser();
		}
	}
	
	/**
	 * Listens for when the user DOES ANYTHING so we THINK it's safe to clear
	 * the error text at the bottom.
	 */
	private class ClearMessageListener implements FocusListener {
		
		@Override
		public void focusGained(FocusEvent e) {
			viewState.setWarning(null);
			viewState.setError(null);
		}
		
		@Override
		public void focusLost(FocusEvent e) {
			viewState.setWarning(null);
			viewState.setError(null);
		}
	}
}
