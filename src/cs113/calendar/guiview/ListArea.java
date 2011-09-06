
package cs113.calendar.guiview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import cs113.calendar.control.Controller;
import cs113.calendar.model.Appointment;

/**
 * This is used to display the results of a search. It displays them in a list.
 * Each item in the list is a button, and you can click on them to edit the
 * appointments.
 * 
 * @author Owen Healy
 */
public class ListArea extends JPanel {
	
	private static final long serialVersionUID = 275032503254L;

	/** Pretty colors. (Well, I thought they looked OK). */
	private static final Color
		listBackgroundColor = new Color(255, 255, 255),
		rowBackgroundColor  = new Color(200, 255, 200),
		textColor           = new Color(  0,   0,   0);
	
	/** Font for displayed things. */
	private static final Font
		labelFont = new Font(null, Font.PLAIN, 12);
	
	/** Format for displayed dates. */
	private static final DateFormat
		dateFormat = new SimpleDateFormat("K:mm a, EEE, MMM d, yyyy");
	
	/** Listen for changes to this.
	 * 
	 * Also this is how we get the current search results.
	 */
	private ViewState viewState;
	
	/** Currently displayed appointments (those that matched the search). */
	private ArrayList<Appointment> appointments;
	/** Current rows for displaying appointments. */
	private ArrayList<ListAreaRow> rows;
	/** Rows indexed by the appointments they display. */
	private HashMap<Appointment, ListAreaRow> rowsByAppointment;
	
	/** The panel where the list appears. */
	private ListPanel listPanel;
	
	/** One of these things again.
	 * 
	 * We need to block events while we're in the middle of a change.
	 */
	private boolean changingSelection = false;
	
	/**
	 * Create the list area. We are ready to start receiving events.
	 * 
	 * @param viewState Application's ViewState.
	 * @param controller Application's Controller.
	 */
	public ListArea(ViewState viewState, Controller controller) {
		this.viewState  = viewState;
		
		setBackground(listBackgroundColor);
		
		appointments = new ArrayList<Appointment>();
		rows = new ArrayList<ListAreaRow>();
		rowsByAppointment = new HashMap<Appointment, ListAreaRow>();
		
		listPanel = new ListPanel();
		listPanel.setBackground(listBackgroundColor);
		
		setLayout(new BorderLayout());
		add(listPanel, BorderLayout.CENTER);
	}
	
	/**
	 * Called by MainArea to set the current search results.
	 * 
	 * @param results The current search results.
	 */
	public void setSearchResults(Collection<Appointment> results) {
		if (results == null) {
			results = new ArrayList<Appointment>(0);
		}
			
		appointments = new ArrayList<Appointment>(results);
		
		redoList();
	}
	
	/**
	 * Redo the list display.
	 * In response to some change.
	 */
	private void redoList() {
		rowsByAppointment.clear();
		rows.clear();
		listPanel.clear();
		
		for (Appointment app : appointments) {
			addToList(app);
		}
		
		for (ListAreaRow row : rows) {
			listPanel.addRow(row);
		}
		listPanel.finish();
		
		Appointment selected = viewState.getAppointment();
		if (selected != null) {
			ListAreaRow row = rowsByAppointment.get(selected);
			if (row != null) row.setSelected(true);
		}
		
		validate();
		repaint();
	}
	
	/**
	 * Create a display row for an appointment.
	 * 
	 * @param app Appointment to be displayed.
	 */
	private void addToList(Appointment app) {
		ListAreaRow row = new ListAreaRow(app);
		
		rows.add(row);
		rowsByAppointment.put(app, row);
	}
	
	/**
	 * An appointment was selected or deselected.
	 * 
	 * @param app The appointment in question.
	 * @param selected Whether it was selected or deselected.
	 * (true if selected).
	 */
	private void updateAppointmentSelection(
		Appointment app, boolean selected)
	{
		if (changingSelection)
			return;
		
		changingSelection = true;
		
		if (selected) {
			viewState.setAppointment(app);
		}
		else {
			viewState.setAppointment(null);
		}
		
		changingSelection = false;
	}
	
	/**
	 * Displays an appointment in a little button.
	 */
	private class ListAreaRow extends JToggleButton {
		
		private static final long serialVersionUID = 525050520;
		
		/**
		 * Create the row and add the displaying labels.
		 * @param appointment The Appointment to be displayed.
		 */
		ListAreaRow(Appointment appointment) {
			JLabel startLabel;
			JLabel endLabel;
			JLabel descriptionLabel;
			JLabel locationLabel;
			
			startLabel = makeLabel(dateFormat.format(
				appointment.getStartTime().getTime()));
			endLabel = makeLabel(dateFormat.format(
					appointment.getEndTime().getTime()));
			
			descriptionLabel = makeLabel(appointment.getDescription());
			locationLabel = makeLabel(appointment.getLocation());
			
			setBackground(rowBackgroundColor);
			
			setLayout(new FlowLayout(FlowLayout.LEFT));
			add(startLabel);
			add(makeLabel(" - "));
			add(endLabel);
			add(makeLabel("    "));
			add(descriptionLabel);
			add(makeLabel("    "));
			add(locationLabel);
			add(Box.createGlue());
			
			addItemListener(new RowItemListener(appointment));
		}
		
		/**
		 * Helper method to create appropriately formatted labels.
		 * 
		 * @param text Contents of label.
		 * @return A label with said text.
		 */
		private JLabel makeLabel(String text) {
			JLabel label = new JLabel(text);
			label.setFont(labelFont);
			label.setForeground(textColor);
			
			return label;
		}
	}
	
	/**
	 * Listen for a row being selected or deselected.
	 */
	private class RowItemListener implements ItemListener {
		
		/** The appointment that might be selected. */
		Appointment appointment;
		
		/**
		 * 
		 * @param appointment The appointment that this row corresponds to.
		 */
		RowItemListener(Appointment appointment) {
			this.appointment = appointment;
		}
		
		@Override
		public void itemStateChanged(ItemEvent e) {
			updateAppointmentSelection(appointment,
				e.getStateChange() == ItemEvent.SELECTED);
		}
	}
}
