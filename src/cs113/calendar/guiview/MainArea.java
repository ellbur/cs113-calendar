
package cs113.calendar.guiview;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cs113.calendar.control.Controller;

/**
 * Wrapper to contain CalendarArea, ListArea, and AdminArea.
 * 
 * @author Owen Healy
 */
public class MainArea extends JPanel {
	
	private static final long serialVersionUID = 5202L;
	
	/** Application's ViewState. */
	private ViewState viewState;
	
	/** Selects which of the various views we swap in. */
	private CardLayout cardLayout;
	
	/** Grid-like calendar view. */
	private CalendarArea calendarArea;
	/** List-like view. */
	private ListArea listArea;
	/** Default to display when no other view is appropriate. */
	private JPanel blankArea;
	/** Admin view. */
	private AdminArea adminArea;
	
	private JPanel mainPanel;
	private JPanel helpPanel;
	
	private CardLayout helpCardLayout;
	
	private static final String
		calendarCard = "calendar",
		listCard     = "list",
		blankCard    = "blank",
		adminCard    = "admin";
	
	private static final String 
		calendarMessage =
			"Click on a day to zoom in. Double-click to select a day. "
			+ "Ctrl-click to create an appointment. "
			+ "Click an appointment to edit.",
		listMessage = "Click on appointment to edit.",
		blankMessage = "",
		adminMessage = "";
	
	/**
	 * Create a new MainArea and put it in the blank state.
	 * 
	 * @param viewState Application's ViewState.
	 * @param controller Application's Controller.
	 */
	public MainArea(ViewState viewState, Controller controller) {
		this.viewState  = viewState;
		
		mainPanel = new JPanel();
		helpPanel = new JPanel();
		
		calendarArea = new CalendarArea(viewState, controller);
		listArea = new ListArea(viewState, controller);
		adminArea = new AdminArea(viewState, controller);
		blankArea = new JPanel();
		
		cardLayout = new CardLayout();
		mainPanel.setLayout(cardLayout);
		
		helpCardLayout = new CardLayout();
		helpPanel.setLayout(helpCardLayout);
		
		mainPanel.add(blankArea, blankCard);
		mainPanel.add(calendarArea, calendarCard);
		mainPanel.add(listArea, listCard);
		mainPanel.add(adminArea, adminCard);
		
		helpPanel.add(makeLowerLabel(blankMessage), blankCard);
		helpPanel.add(makeLowerLabel(calendarMessage), calendarCard);
		helpPanel.add(makeLowerLabel(listMessage), listCard);
		helpPanel.add(makeLowerLabel(adminMessage), adminCard);
		
		setLayout(new GridBagLayout());
		GridBagConstraints gr = new GridBagConstraints();
		
		gr.gridx   = 0;
		gr.gridy   = 0;
		gr.fill    = GridBagConstraints.BOTH;
		gr.weighty = 1;
		gr.weightx = 1;
		add(mainPanel, gr);
		
		gr.gridy   = 1;
		gr.weighty = 0;
		gr.ipadx   = 5;
		gr.ipady   = 5;
		add(helpPanel, gr);
		
		viewState.addChangeListener(new StateChangeListener());
	}
	
	/**
	 * Set the displayed card in both the main area and the help area.
	 * 
	 * @param name Name of the card to set.
	 */
	private void setCard(String name) {
		cardLayout.show(mainPanel, name);
		helpCardLayout.show(helpPanel, name);
	}
	
	/**
	 * Update the displayed area based on the ViewState.
	 */
	private void updateState() {
		switch (viewState.getState()) {
		case DAY:
		case WEEK:
		case MONTH:
			setCard(calendarCard);
			calendarArea.setView();
			break;
			
		case SEARCH:
			setCard(listCard);
			updateSearch();
			break;
		
		case ADMIN:
			setCard(adminCard);
			break;
			
		default:
			setCard(blankCard);
		}
	}
	
	/**
	 * Get the new search results and give them to the list area.
	 */
	private void updateSearch() {
		listArea.setSearchResults(viewState.getSearchResults());
	}
	
	/**
	 * Helper method to make a label to go in the help area.
	 * 
	 * @param text Message to be displayed.
	 * @return An appropriately formatted JLabel.
	 */
	private JLabel makeLowerLabel(String text) {
		JLabel label = new JLabel(text);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		
		return label;
	}
	
	/**
	 * Listen for changes to the ViewState.
	 */
	private class StateChangeListener implements ChangeListener {
		
		@Override
		public void stateChanged(ChangeEvent e) {
			updateState();
		}
	}
}
