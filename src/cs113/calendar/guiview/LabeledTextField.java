package cs113.calendar.guiview;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Standard JTextField with a prompt super-imposed over the control in lieu of a
 * standard JLabel. The prompt is rendered in the <code>FOREGROUND_PROMPT</code>
 * color, in contrast to user input, which is rendered in the
 * <code>FOREGROUND_EDITED</code> color. Note that <code>getText()</code> will
 * return an empty string (i.e. <code>""</code>) if the prompt is being
 * displayed.
 * 
 * @author Michael Koval
 */
public class LabeledTextField extends JTextField {
	private static final long serialVersionUID = -3517422529974990006L;
	/**
	 * Foreground color used for normal text input.
	 */
	public static final Color FOREGROUND_EDITED = Color.BLACK;
	/**
	 * Foreground color for an enabled, but empty text field.
	 */
	public static final Color FOREGROUND_PROMPT = Color.GRAY;

	/**
	 * Text displayed when the field is empty.
	 */
	private String prompt;
	/**
	 * Whether the prompt should be restored upon exit of the field.
	 */
	private boolean edited;
	/**
	 * Flag to prevent excessive event recursion.
	 */
	private boolean fired;

	/**
	 * Monitors changes in focus to ensure the text color and content of the
	 * text field are always in sync with its actual state.
	 * 
	 * @author Michael Koval
	 */
	private class FocusChangeListener implements FocusListener {
		/**
		 * Clear the prompt from the text input and reset the color to
		 * <code>FOREGROUND_EDITED</code> if the field is currently empty.
		 */
		@Override
		public void focusGained(FocusEvent e) {
			if (!edited) {
				reset();
			}
		}

		/**
		 * Restore the prompt to the text input and change its color to
		 * <code>FOREGROUND_PROMPT</code> if the field was not changed.
		 */
		@Override
		public void focusLost(FocusEvent e) {
			if (!edited || getText().isEmpty()) {
				clear();
			}
		}
	}

	/**
	 * Monitor the <code>edited</code> status of the text field by using the
	 * <code>keyTyped()</code> event.
	 * 
	 * @author Michael Koval
	 */
	private class ValueChangeListener implements DocumentListener {
		@Override
		public void changedUpdate(DocumentEvent e) {
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			if (fired)
				return;

			edited = true;
			fired = true;
			fireChangeEvent(new ChangeEvent(this));
			fired = false;
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			if (fired)
				return;

			edited = true;
			fired = true;
			fireChangeEvent(new ChangeEvent(this));
			fired = false;
		}
	}

	/**
	 * Construct a new text field, complete with a <code>JLabel</code>-replacing
	 * prompt.
	 * 
	 * @param prompt label that fills the text field when it has not been
	 *            modified by the user
	 * @param length number of characters to be visible in the text field at
	 *            once (i.e. its width)
	 * @param
	 */
	public LabeledTextField(String prompt, int cols) {
		super(prompt, cols);

		this.prompt = prompt;
		clear();

		addFocusListener(new FocusChangeListener());
		getDocument().addDocumentListener(new ValueChangeListener());
	}

	/**
	 * Gets the current contents of this text field. Returns an empty string
	 * (i.e. <code>""</code>) if the prompt is being displayed.
	 */
	@Override
	public String getText() {
		if (edited) {
			return super.getText();
		} else {
			return "";
		}
	}

	/**
	 * Sets the current contents of this text field, updating the style and
	 * state of the text field as needed. Set the text to an empty string (i.e.
	 * <code>""</code>) to restore the prompt.
	 */
	@Override
	public void setText(String t) {
		if (t.isEmpty() && !hasFocus()) {
			clear();
		} else {
			reset();
			super.setText(t);
			edited = true;
		}
	}

	/**
	 * Resets this text field to its empty state, awaiting user input.
	 */
	protected void reset() {
		setForeground(FOREGROUND_EDITED);

		fired = true;
		super.setText("");
		fired = false;

		edited = false;
	}

	/**
	 * Clear this text field, restoring it to its unused state. This includes
	 * changing the style of the field and restoring the prompt text.
	 */
	protected void clear() {
		setForeground(FOREGROUND_PROMPT);

		fired = true;
		super.setText(prompt);
		fired = false;

		edited = false;
	}

	/**
	 * Listen for a change in the value in this text field (i.e. returned by
	 * <code>getText()</code>). Implicit contract guarantees this will be
	 * exactly <em>once</em> for every state changes.
	 * 
	 * @param listener event listener to be notified of a state changes
	 */
	public void addChangeListener(ChangeListener listener) {
		listenerList.add(ChangeListener.class, listener);
	}

	/**
	 * Remove the event listener that is listening for a state change.
	 * 
	 * @param listener event listener to be removed
	 */
	public void removeChangeListener(ChangeListener listener) {
		listenerList.remove(ChangeListener.class, listener);
	}

	/**
	 * Notify all pertinent event listeners of a change in the text value of
	 * this field.
	 */
	protected void fireChangeEvent(ChangeEvent e) {
		for (ChangeListener i : listenerList.getListeners(ChangeListener.class)) {
			i.stateChanged(e);
		}
	}
}
