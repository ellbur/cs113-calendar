
package cs113.calendar.guiview;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Displays items in a list-like container with scroll bars.
 * 
 * @author Owen Healy
 */
public class ListPanel extends JPanel {
	
	private static final long serialVersionUID = 5225020;
	
	/** Provides scrolling. */
	private JScrollPane scrollPane;
	/** Inside the scroll pane. */
	private JPanel listPanel;
	
	/** Increments as we progress downwards. */
	private int currentRow;
	
	/**
	 * Create a new empty list.
	 */
	public ListPanel() {
		currentRow = 0;
		
		scrollPane = new JScrollPane(
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		listPanel = new JPanel();
		listPanel.setLayout(new GridBagLayout());
		
		scrollPane.getViewport().setView(listPanel);
		
		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
	}
	
	/**
	 * Get the scroll pane containing the list.
	 * 
	 * @return The scroll pane.
	 */
	public JScrollPane getScrollPane() {
		return scrollPane;
	}
	
	/**
	 * Adds a component to the list.
	 * 
	 * This should be called after clear() and before finish(), or it won't
	 * look right.
	 * 
	 * @param comp A component to be added.
	 */
	public void addRow(Component comp) {
		GridBagConstraints cr = new GridBagConstraints();
		cr.gridx   = 0;
		cr.gridy   = currentRow;
		cr.insets  = new Insets(10, 10, 0, 10);
		cr.anchor  = GridBagConstraints.NORTHWEST;
		cr.fill    = GridBagConstraints.HORIZONTAL;
		cr.weighty = 0;
		cr.weightx = 1;
		
		listPanel.add(comp, cr);
		
		currentRow++;
	}
	
	/**
	 * Done adding rows. Finish laying things out nice.
	 */
	public void finish() {
		GridBagConstraints cr = new GridBagConstraints();
		
		cr.weighty = 1;
		cr.gridx   = 0;
		cr.gridy   = currentRow;
		listPanel.add(Box.createGlue(), cr);
		
		listPanel.setBackground(this.getBackground());
		listPanel.validate();
	}
	
	/**
	 * Remove everything and start again.
	 */
	public void clear() {
		listPanel.removeAll();
		currentRow = 0;
		
		listPanel.validate();
	}
}
