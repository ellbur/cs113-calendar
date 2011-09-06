package cs113.calendar.guiview;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Graphical component that consists of horizontal row of buttons, each of which
 * represents a discrete state. One can switch from one state to another by
 * activating the button associated with the desired state, triggering an action
 * to be fired.
 * 
 * @author Michael Koval
 */
public class StateSelectionComponent extends JPanel {
	private static final long serialVersionUID = -7563764915432584874L;

	/**
	 * Text displayed on the previous button.
	 */
	private final String BUTTON_PREV = "<";
	private final String BUTTON_NEXT = ">";
	/**
	 * Text displayed on the next button.
	 */
	private final int DAYS_IN_WEEK = 7;

	/**
	 * Previous and next buttons used for navigation.
	 */
	private JButton prev, next;
	/**
	 * Associative relationship between a state and its visible buttons. Not all
	 * states must have a visible button.
	 */
	private Map<ViewState.State, JButton> states;
	/**
	 * Global application state being dislpayed.
	 */
	private ViewState state;

	/**
	 * Change the state when the user clicks one of the buttons to trigger a
	 * state change.
	 * 
	 * @author Michael Koval
	 */
	private class ClickListener implements ActionListener {
		private ViewState.State fired;

		public ClickListener(ViewState.State state) {
			this.fired = state;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			state.setState(fired);
		}
	}

	/**
	 * Move forward or backwards through time with the "previous" and "next"
	 * arrows.
	 * 
	 * @author Michael Koval
	 */
	private class NavigationListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			Calendar newDate = (Calendar) state.getDate().clone();

			// Go backwards in time by the desired increment.
			if (e.getSource() == prev) {
				switch (state.getState()) {
				case DAY:
					newDate.add(Calendar.DAY_OF_MONTH, -1);
					break;
				case WEEK:
					newDate.add(Calendar.DAY_OF_MONTH, -DAYS_IN_WEEK);
					break;
				case MONTH:
					newDate.add(Calendar.MONTH, -1);
					break;
				}
			}
			// Go forwards in time by the desired increment.
			else if (e.getSource() == next) {
				switch (state.getState()) {
				case DAY:
					newDate.add(Calendar.DAY_OF_MONTH, 1);
					break;
				case WEEK:
					newDate.add(Calendar.DAY_OF_MONTH, DAYS_IN_WEEK);
					break;
				case MONTH:
					newDate.add(Calendar.MONTH, 1);
					break;
				}
			}

			if (!state.getDate().equals(newDate)) {
				state.setDate(newDate);
			}
		}
	}

	/**
	 * Update the visual state of this component to match the actual state of
	 * the application.
	 * 
	 * @author Michael Koval
	 */
	private class StateChangeListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			refreshEnabled();
		}
	}

	/**
	 * Construct a new StateSelectionComponent that allows for the selection of
	 * any of the states in the <code>options</code> parameter. This component
	 * begins in the state defined by the <code>start</code> parameter, which
	 * must be one of the states in the <code>options</code> parameter. States
	 * that are not include in <code>options</code> may be programmatically be
	 * selected, even if they do not appear in the interface.
	 * 
	 * @param state responsible for managing and restricting state changes
	 * @param options states that may be selected by the user on this component
	 * @throws IllegalArgumentException if <code>options</code> parameter is
	 *             empty
	 * @throws NoSuchElementException if <code>start</code> is not in
	 *             <code>options</code>
	 */
	public StateSelectionComponent(ViewState state,
			Map<ViewState.State, String> options) {
		if (options == null || state == null) {
			throw new NullPointerException("State options must be non-null");
		} else if (options.size() < 1) {
			throw new IllegalArgumentException(
					"At least one state must be specified.");
		}

		this.state = state;
		state.addChangeListener(new StateChangeListener());

		// Use a GridLayout to evenly space the state buttons in a horizontal
		// row.
		setLayout(new GridBagLayout());
		GridBagConstraints c;

		// Previous week/day/month button.
		prev = new JButton(BUTTON_PREV);
		prev.setMargin(new Insets(0, 0, 0, 0));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.0;
		c.weighty = 0.0;
		add(prev, c);

		// Create one equal-sized button for each potential state, with the
		// current state disabled.
		states = new HashMap<ViewState.State, JButton>(options.size());
		c = new GridBagConstraints();
		c.gridx = GridBagConstraints.RELATIVE;
		c.gridy = 0;
		c.weightx = 1.0 / states.size();
		c.weighty = 0.0;

		for (ViewState.State s : options.keySet()) {
			if (state == null) {
				throw new NullPointerException("States must be non-null.");
			}

			JButton button = new JButton(options.get(s));
			button.addActionListener(new ClickListener(s));

			add(button, c);
			states.put(s, button);
		}

		// Next week/day/month button.
		next = new JButton(BUTTON_NEXT);
		next.setMargin(new Insets(0, 0, 0, 0));
		c = new GridBagConstraints();
		c.gridx = GridBagConstraints.RELATIVE;
		c.gridy = 0;
		c.weightx = 0.0;
		c.weighty = 0.0;
		add(next, c);

		prev.addActionListener(new NavigationListener());
		next.addActionListener(new NavigationListener());

		// Disable the component if the user is not logged in.
		refreshEnabled();
	}

	/**
	 * Enables or disables the entire state selector, preventing the user from
	 * changing the state of this component.
	 */
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		refreshEnabled();
	}

	/**
	 * Update the enabled/disabled state of all subcomponents to match the
	 * current state.
	 */
	protected void refreshEnabled() {
		boolean enabled = isEnabled();
		boolean authorized = state.getState() != ViewState.State.UNAUTHORIZED;
		boolean admin = state.getState() == ViewState.State.ADMIN;
		boolean browsable = state.getState() == ViewState.State.MONTH
				|| state.getState() == ViewState.State.WEEK
				|| state.getState() == ViewState.State.DAY;

		// Previous/forward buttons are only enabled in browsable states.
		prev.setEnabled(enabled && browsable);
		next.setEnabled(enabled && browsable);

		// State selection buttons.
		for (JButton button : states.values()) {
			button.setEnabled(enabled && authorized && !admin);
		}

		// Disable the currently selected state to visually highlight it.
		if (states.containsKey(state.getState())) {
			states.get(state.getState()).setEnabled(false);
		}
	}
}
