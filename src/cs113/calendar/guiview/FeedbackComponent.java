
package cs113.calendar.guiview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Provides errors and warnings to the user.
 * 
 * This will appear as a strip along the bottom of the window. It listens
 * for changes to the ViewState, and on each change checks the current error
 * and the current warning. If they are non-null, it displays them.
 * 
 * @author Owen Healy
 */
public class FeedbackComponent extends JPanel {
	
	private static final long serialVersionUID = 5657567546L;
	
	/** Colors of so many things... */
	private static final Color
		messageColor = new Color(  0,   0,   0),
		warningColor = new Color(220, 100,   0),
		errorColor   = new Color(255,  50,   0);
	
	/** Individual fonts for each thing that can happen. */
	private static final Font
		messageFont   = new Font(null, Font.PLAIN, 14),
		warningFont   = new Font(null, Font.PLAIN, 14),
		errorFont     = new Font(null, Font.PLAIN, 14);
	
	/** Listen for state changes on this. */
	private ViewState viewState;
	
	/* Use this to display the problem. */
	private JLabel statusLabel;
	
	/**
	 * Create the feedback component and it is now ready to receive messages
	 * view ViewState.
	 * 
	 * @param viewState The application's ViewState.
	 */
	public FeedbackComponent(ViewState viewState) {
		this.viewState = viewState;
		
		statusLabel = new JLabel();
		
		setLayout(new BorderLayout());
		add(statusLabel, BorderLayout.CENTER);
		
		viewState.addChangeListener(new ViewChangeListener());
		
		updateStatus();
	}
	
	/**
	 * Update the contents and style of the label.
	 */
	private void updateStatus() {
		if (viewState.getError() != null) {
			statusLabel.setText(viewState.getError());
			setError();
		}
		else if (viewState.getWarning() != null) {
			statusLabel.setText(viewState.getWarning());
			setWarning();
		}
		else {
			statusLabel.setText("  ");
			setMessage();
		}
	}
	
	/**
	 * Make this message-style.
	 */
	private void setMessage() {
		statusLabel.setFont(messageFont);
		statusLabel.setForeground(messageColor);
	}
	
	/**
	 * Make this warning-style.
	 */
	private void setWarning() {
		statusLabel.setFont(warningFont);
		statusLabel.setForeground(warningColor);
	}
	
	/**
	 * Make this error-style.
	 */
	private void setError() {
		statusLabel.setFont(errorFont);
		statusLabel.setForeground(errorColor);
	}
	
	/**
	 * Listen for changes to the ViewState.
	 */
	private class ViewChangeListener implements ChangeListener {
		
		@Override
		public void stateChanged(ChangeEvent e) {
			updateStatus();
		}
	}
}
