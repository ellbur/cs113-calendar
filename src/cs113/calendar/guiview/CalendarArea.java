
package cs113.calendar.guiview;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.plaf.PanelUI;
import javax.swing.plaf.basic.BasicToggleButtonUI;

import cs113.calendar.control.Controller;
import cs113.calendar.model.Appointment;
import cs113.calendar.util.IdenticalAppointmentException;
import cs113.calendar.util.InvalidDateException;

/**
 * This is the heart of the calendar. It tries to actually look like a
 * calendar. It has a zoomy-navigation thing that some people find
 * unpleasant but it allows you to un-scrunch appointments that are way close
 * together in a day.
 * 
 * @author Owen Healy
 */
public class CalendarArea extends JPanel {
	
	private static final long serialVersionUID = 72039745230417230L;
	
	/** These are the colors of just about everything. */
	private static final Color
		boxUnselectedColor      = new Color(200, 255, 220),
		boxSelectedColor        = new Color(150, 255, 220),
		boxTextColor            = new Color(  0,   0,   0),
		boxSecondaryTextColor   = new Color(100, 100, 100),
		
		backgroundColor         = new Color(255, 255, 255),
		hourLineColor           = new Color(150, 200, 150),
		dayLineColor            = new Color(100,  50, 100),
		
		headerColor             = new Color(100, 150, 100),
		dayNumberColor          = new Color(100, 100, 100),
		inactiveDayNumberColor  = new Color(200, 200, 200),
		hourLabelColor          = new Color(100, 100, 100);
	
	/** These are all our fonts. */
	private static final Font
		dayNumberFont  = new Font(null, Font.BOLD,  16),
		headerFont     = new Font(null, Font.PLAIN, 16),
		boxFont        = new Font(null, Font.PLAIN, 12),
		hourLabelFont  = new Font(Font.MONOSPACED, Font.PLAIN, 10);
	
	/** Margins for.... various things. Boxes. Like appointments. */
	private static final Insets
		bigBoxInsets   = new Insets(3, 3, 3, 3),
		smallBoxInsets = new Insets(0, 0, 0, 0),
		dayInsets      = new Insets(5, 5, 5, 5);
	
	/** Multiple of text height */
	private static final double headerHeightRate = 2.0;
	
	/** Handy little number. */
	private static final long ONE_HOUR = 1000 * 60 * 60;
	
	/** Handy little number. */
	private static final long ONE_DAY  = ONE_HOUR * 24;
	
	/**
	 * Date formats used for displaying headers, and for displaying times
	 * in the appointment boxes.
	 */
	private static final DateFormat
		dayOfWeekFormat = new SimpleDateFormat("EEEE"),
		boxDateFormat   = new SimpleDateFormat("hh:mm");
	
	/** Tells us what state the program is in. Also how we get
	 * the current search.
	 */
	private ViewState viewState;
	/** Allows us to interact with the stored appointments. */
	private Controller controller;
	
	/** Upper-left-hand corner. */
	private Calendar gridStartTime;
	/** Lower right-hand corner. */
	private Calendar gridEndTime;
	/** Upper-left-hand corner in milliseconds. */
	private long gridStartMillis;
	
	/** All the appointments that could be displayed. */
	private ArrayList<Appointment> appointments;
	/** All the days in the grid in a big list. */
	private ArrayList<DayCell> days;
	
	/** Days indexed by row,col. */
	private DayCell[][] dayGrid;
	/** Y-cell boundaries. */
	private int[] rowCellBoundaries;
	/** X-cell boundaries. */
	private int[] colCellBoundaries;
	
	/** Y-hour divisions indexed by row. */
	private HourDivision[] hourDivisions;
	/** Extra weight (above 1.0) given to hours with stuff in them. */
	private double hourWeight = 1.0;
	
	/** Rows in grid. */
	private int rows;
	
	/** Cols in grid. */
	private int cols;
	
	/** Displays in use. */
	private ArrayList<AppointmentDisplay> displays;
	/** Boxes in use. */
	private ArrayList<AppointmentBox> boxes;
	/** Displays in use indexed by the appointment that the talk about. */
	private HashMap<Appointment, AppointmentDisplay> displaysByAppointment;
	
	/** Left X. */
	private int gridX1;
	/** Upper Y. */
	private int gridY1;
	/** Right X. */
	private int gridX2;
	/** Lower Y. */
	private int gridY2;
	
	/** Current margin for appointment boxes. */
	private Insets boxInsets;
	
	/** Should we make lines on the hours? */
	private boolean linesOnHours;
	/** Should we label hours? */
	private boolean labelHours;
	/** Should we put numbers on the days? */
	private boolean dayNumbers;
	
	/** Draw these at the top of each column. */
	private String[] columnHeaders;
	/** When you've zoomed in, draw these at the top of the skinny columns. */
	private String[] shortColumnHeaders;
	
	/** The month we care about. Make labels for the other months a slightly
	 * whiter shade of pale.
	 */
	private int activeMonth;
	
	/** Zoomed-in row. */
	private int activeRow;
	/** Zoomed-in col. */
	private int activeCol;
	
	/** Don't ask. Just don't ask. OK, since you asked, this says that we
	 * are right now changing which appointment is selected, which, because it
	 * make trigger new ItemEvents that are in response to something that the
	 * user did not do, we need to ignore, or we'll get into an infinite
	 * cycle of events.
	 */
	private boolean selectionUpdateInProgress;
	/** The display that should be selected right now. */
	private AppointmentDisplay activeAppointmentDisplay;
	
	/**
	 * Create, layout, etc. Won't have any appointments in it at this point,
	 * but it should otherwise look OK. It will query the viewState to get
	 * the time it should be using.
	 * 
	 * The ViewState better give it a valid time or it won't make something
	 * pretty.
	 * 
	 * @param viewState Application's ViewState.
	 * @param controller Application's Controller.
	 */
	public CalendarArea(ViewState viewState, Controller controller) {
		this.viewState  = viewState;
		this.controller = controller;
		
		appointments = new ArrayList<Appointment>();
		
		displays = new ArrayList<AppointmentDisplay>();
		boxes    = new ArrayList<AppointmentBox>();
		displaysByAppointment = new HashMap<Appointment, AppointmentDisplay>();
		
		activeRow = -1;
		activeCol = -1;
		
		setupDayView(new GregorianCalendar());
		
		setLayout(null);
		setUI(new AreaUI());
		
		addComponentListener(new AreaComponentListener());
		addMouseListener(new AreaMouseListener());
	}
	
	/**
	 * Read the current state from the ViewState and update the view
	 * accordingly.
	 * 
	 * If it's a view we don't normally handle you shouldn't have called this
	 * method and we'll just pretend this little episode didn't happen.
	 */
	public void setView() {
		switch (viewState.getState()) {
		case MONTH:
			setMonthView(viewState.getDate());
			break;
		case WEEK:
			setWeekView(viewState.getDate());
			break;
		case DAY:
			setDayView(viewState.getDate());
			break;
		}
		
		activeAppointmentDisplay = null;
		
		activate: {
		
		Appointment toActivate = viewState.getAppointment();
		if (toActivate == null)
			break activate;
		
		AppointmentDisplay display = displaysByAppointment.get(toActivate);
		if (display == null)
			break activate;
		
		activeAppointmentDisplay = display;
		display.setSelected(true);
		
		}
	}
	
	/**
	 * Make this be a day view.
	 * 
	 * @param timeInDay Some time during the day.
	 */
	public void setDayView(Calendar timeInDay)
	{
		setupDayView(timeInDay);
		
		setupStructure();
		setupLayout();
	}
	
	/**
	 * Make this a week view.
	 * 
	 * @param timeInWeek Some time during the week.
	 */
	public void setWeekView(Calendar timeInWeek)
	{
		setupWeekView(timeInWeek);
		
		setupStructure();
		setupLayout();
	}
	
	/**
	 * Make this a month view.
	 * 
	 * @param timeInMonth Some time during the month.
	 */
	public void setMonthView(Calendar timeInMonth)
	{
		setupMonthView(timeInMonth);
		
		setupStructure();
		setupLayout();
	}
	
	/**
	 * Set up structural things like appointment boxes. You need to do this
	 * before you do the layout.
	 */
	private void setupStructure() {
		boxupAppointments();
	}
	
	/**
	 * Set up dimensionally things like the placement of boxes, the placement
	 * of the grid lines, the spacing of the hours.
	 */
	private void setupLayout() {
		layoutArea();
		layoutGrid();
		layoutHourDivisions();
		layoutBoxes();
		
		repaint();
	}
	
	/**
	 * Set the parameters that are appropriate for a day view.
	 * 
	 * @param timeInDay Some time in the day.
	 */
	private void setupDayView(Calendar timeInDay) {
		rows = 1;
		cols = 1;
		
		Calendar dayStart = startOfDay(timeInDay);
		
		gridStartTime   = (Calendar) dayStart.clone();
		gridEndTime     = (Calendar) dayStart.clone();
		gridEndTime.add(Calendar.DATE, 1);
		
		gridStartMillis = dayStart.getTimeInMillis();
		
		setupGrid();
		
		linesOnHours = true;
		labelHours   = true;
		dayNumbers = true;
		activeMonth = -1;
		hourWeight = 10.0;
		boxInsets = bigBoxInsets;
		columnHeaders = new String[] {
			dayOfWeekFormat.format(dayStart.getTime())
		};
		shortColumnHeaders = makeShortDayHeaders(columnHeaders);
	}
	
	/**
	 * Set the parameters that make this a week view.
	 * 
	 * @param timeInWeek Some time during the week.
	 */
	private void setupWeekView(Calendar timeInWeek) {
		rows = 1;
		cols = 7;
		
		Calendar weekStart = startOfDay(timeInWeek);
		while (weekStart.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
			weekStart.add(Calendar.DATE, -1);
		}
		
		
		gridStartTime   = (Calendar) weekStart.clone();
		gridEndTime     = (Calendar) weekStart.clone();
		gridEndTime.add(Calendar.DATE, 7);
		
		gridStartMillis = weekStart.getTimeInMillis();
		
		setupGrid();
		
		linesOnHours = true;
		labelHours   = true;
		dayNumbers = true;
		activeMonth = -1;
		hourWeight = 10.0;
		boxInsets = bigBoxInsets;
		columnHeaders = makeDayHeaders(weekStart);
		shortColumnHeaders = makeShortDayHeaders(columnHeaders);
	}
	
	/**
	 * Set the parameters to make this a month view.
	 * 
	 * @param timeInMonth Some time during the month.
	 */
	private void setupMonthView(Calendar timeInMonth) {
		rows = 5;
		cols = 7;
		
		Calendar monthStart = startOfDay(timeInMonth);
		monthStart.set(Calendar.DAY_OF_MONTH, 1);
		
		gridStartTime = (Calendar) monthStart.clone();
		while (gridStartTime.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
			gridStartTime.add(Calendar.DATE, -1);
		}
		gridStartMillis = gridStartTime.getTimeInMillis();
		
		gridEndTime = (Calendar) gridStartTime.clone();
		gridEndTime.add(Calendar.DATE, rows * cols);
		
		setupGrid();
		
		linesOnHours = false;
		labelHours = false;
		dayNumbers = true;
		hourWeight = 1000.0;
		boxInsets  = smallBoxInsets;
		activeMonth = monthStart.get(Calendar.MONTH);
		columnHeaders = makeDayHeaders(gridStartTime);
		shortColumnHeaders = makeShortDayHeaders(columnHeaders);
	}
	
	/**
	 * Gets the start of a day based on some time in it.
	 * 
	 * @param timeInDay Some time during the day.
	 * 
	 * @return The start of the day.
	 */
	private static Calendar startOfDay(Calendar timeInDay) {
		Calendar day = (Calendar) timeInDay.clone();
		
		day.set(Calendar.HOUR_OF_DAY, 0);
		day.set(Calendar.MINUTE, 0);
		day.set(Calendar.SECOND, 0);
		day.set(Calendar.MILLISECOND, 0);
		
		return  day;
	}
	
	/**
	 * Shorten to something to put in the skinny columns when you zoom in.
	 * 
	 * @param dayHeaders The long form of the day headers.
	 * 
	 * @return A shortened form that takes up less space.
	 */
	private static String[] makeShortDayHeaders(String[] dayHeaders) {
		String[] headers = new String[dayHeaders.length];
		
		for (int i=0; i<headers.length; i++)
			headers[i] = dayHeaders[i].substring(0, 1);
		
		return headers;
	}
	
	/**
	 * Make strings to label the calendar days. Sunday, Monday etc.
	 * 
	 * @param weekStart The start of the week.
	 * 
	 * @return Strings corresponding to the days of the week.
	 */
	private static String[] makeDayHeaders(Calendar weekStart) {
		String[] headers = new String[7];
		
		Calendar roller = (Calendar) weekStart.clone();
		
		for (int i=0; i<7; i++) {
			headers[i] = dayOfWeekFormat.format(roller.getTime());
			roller.add(Calendar.DATE, 1);
		}
		
		return headers;
	}
	
	/**
	 * Determine where to place the boundaries of the grid.
	 */
	private void layoutArea() {
		FontMetrics fm = getGraphics().getFontMetrics(headerFont);
		int fontHeight = fm.getHeight() + fm.getDescent();
		
		int width  = getWidth();
		int height = getHeight();
		
		gridX1 = 0;
		gridX2 = width;
		gridY1 = (int) (headerHeightRate * fontHeight);
		gridY2 = height;
	}
	
	/**
	 * This creates the DayCell objects and puts them in to the grid.
	 * 
	 * It also creates the HourDivision objects for each row.
	 */
	private void setupGrid() {
		try {
			appointments = new ArrayList<Appointment>(
				controller.listAppointmentsInRange(
				gridStartTime, gridEndTime));
		}
		catch (IllegalStateException ise) {
			appointments = new ArrayList<Appointment>();
		}
		
		rowCellBoundaries = new int[rows + 1];
		colCellBoundaries = new int[cols + 1];
		
		days = new ArrayList<DayCell>(rows * cols);
		dayGrid = new DayCell[rows][cols];
		
		Calendar dayStart = (Calendar) gridStartTime.clone();
		Calendar dayEnd   = (Calendar) gridStartTime.clone();
		dayEnd.add(Calendar.DATE, 1);
		
		for (int i=0; i<rows; i++)
		for (int j=0; j<cols; j++) {
			DayCell day = new DayCell(i, j,
				(Calendar) dayStart.clone(),
				(Calendar) dayEnd.clone());
			
			days.add(day);
			dayGrid[i][j] = day;
			
			dayStart.add(Calendar.DATE, 1);
			dayEnd.add(Calendar.DATE, 1);
		}
		
		hourDivisions = new HourDivision[rows];
		for (int i=0; i<rows; i++) {
			hourDivisions[i] = new HourDivision(i);
		}
	}
	
	/**
	 * This lays out the x's and the y's in the grid.
	 * 
	 * It uses a weighted partition so that you can zoom in on a particular
	 * day.
	 */
	private void layoutGrid() {
		double[] rowWeights = new double[rows];
		for (int i=0; i<rows; i++) {
			rowWeights[i] = 1.0;
		}
		
		double[] colWeights = new double[cols];
		for (int j=0; j<cols; j++) {
			colWeights[j] = 1.0;
		}
		
		if (activeRow >= rows) activeRow = -1;
		if (activeCol >= cols) activeCol = -1;
		
		if (activeCol != -1 && activeRow != -1) {
			rowWeights[activeRow] = 7.0;
			colWeights[activeCol] = 3.0;
		}
		
		double rowCellBoundariesDouble[] = new double[rows + 1];
		double colCellBoundariesDouble[] = new double[cols + 1];
		
		weightedPartition(rowWeights, gridY1, gridY2,
			rowCellBoundariesDouble);
		weightedPartition(colWeights, gridX1, gridX2,
			colCellBoundariesDouble);
		
		for (int i=0; i<rows+1; i++)
			rowCellBoundaries[i] = (int) rowCellBoundariesDouble[i];
		for (int j=0; j<cols+1; j++)
			colCellBoundaries[j] = (int) colCellBoundariesDouble[j];
	}
	
	/**
	 * Turns a vector of weights into a vector of boundaries.
	 * 
	 * @param weights The weights.
	 * @param start The first boundary.
	 * @param end The last boundary.
	 * @param partition The vector to stick the partitions into.
	 */
	private static void weightedPartition(double[] weights,
		double start, double end, double[] partition)
	{
		double[] cumWeights = new double[weights.length];
		double sum = 0.0;
		
		for (int i=0; i<weights.length; i++) {
			cumWeights[i] = sum;
			sum += weights[i];
		}
		
		for (int i=0; i<weights.length; i++) {
			partition[i] = cumWeights[i] / sum * (end-start) + start;
		}
		partition[weights.length] = end;
	}
	
	/**
	 * Make boxes for the appointments.
	 * 
	 * Doesn't lay them out.
	 */
	private void boxupAppointments() {
		removeAll();
		displays.clear();
		boxes.clear();
		
		for (Appointment app : appointments) {
			boxupAppointment(app);
		}
	}
	
	/**
	 * Make a box for a particular appointment.
	 * 
	 * @param app An appointment to make a box for.
	 */
	private void boxupAppointment(Appointment app) {
		AppointmentDisplay display = new AppointmentDisplay(app);
		displays.add(display);
		displaysByAppointment.put(app, display);
		
		for (DayCell cell : days) {
			if (! cell.inCell(app)) continue;
			
			AppointmentBox box = cell.makeBox(display);
			
			cell.addBox(box);
			display.addBox(box);
			boxes.add(box);
			add(box.getComponent());
		}
	}
	
	/**
	 * Layout the hours within each row of the grid.
	 * 
	 * Gives more weight to hours that have stuff in them.
	 * Boxes must have been added for this to make sense.
	 */
	private void layoutHourDivisions() {
		for (int i=0; i<rows; i++) layoutHourDivision(i);
	}
	
	/**
	 * Lays out the hours for one row of the grid.
	 * 
	 * @param row A row in the grid.
	 */
	private void layoutHourDivision(int row) {
		boolean[] interesting = new boolean[24];
		for (int i=0; i<24; i++) interesting[i] = false;
		
		for (int j=0; j<cols; j++) {
			checkColHours(row, j, interesting);
		}
		
		hourDivisions[row].setInteresting(interesting);
	}
	
	/**
	 * Check to see whether any of the hours in the day row,col have stuff
	 * in them. If they do, they are "interesting".
	 * 
	 * @param row A row in the grid.
	 * @param col A col in the grid.
	 * @param interesting A vector of hours. True if they're "interesting".
	 */
	private void checkColHours(int row, int col, boolean[] interesting) {
		DayCell day = dayGrid[row][col];
		long dayStart = day.getTimeStartMillis();
		
		for (AppointmentBox box : day.getBoxes()) {
			long boxStart = box.getStart();
			long boxEnd   = box.getEnd();
			
			int startHour = (int) Math.floor(
				(boxStart - dayStart) / ONE_HOUR );
			int endHour   = (int) Math.ceil(
				(boxEnd   - dayStart) / ONE_HOUR );
			if (endHour > 23) endHour = 23;
			
			for (int h=startHour; h<=endHour; h++)
				interesting[h] = true;
		}
	}
	
	/**
	 * Layout the boxes that correspond to appointments.
	 */
	private void layoutBoxes() {
		for (AppointmentBox box : boxes) {
			layoutBox(box);
		}
	}
	
	/**
	 * Set the x, y, width, height for an appointment box.
	 * 
	 * @param box A box to lay out.
	 */
	private void layoutBox(AppointmentBox box) {
		DayCell day = box.getDay();
		int col = day.getCol();
		
		Component comp = box.getComponent();
		
		int x1 = colCellBoundaries[col] + 2;
		int x2 = colCellBoundaries[col+1] - 1;
		
		int y1 = timeToY(box.getStart());
		int y2 = timeToY(box.getEnd());
		
		comp.setBounds(x1, y1, x2-x1, y2-y1);
	}
	
	/**
	 * Get the row in the grid that a time falls into.
	 * 
	 * @param time Time in mills.
	 * 
	 * @return Row in the grid.
	 */
	private int timeToRow(long time) {
		int num = (int) ((time-gridStartMillis) / ONE_DAY);
		return num / cols;
	}
	
	/**
	 * Get the Y coordinate that a time appears at.
	 * 
	 * @param time A time in mills.
	 * 
	 * @return a Y coordinate.
	 */
	private int timeToY(long time) {
		int row = timeToRow(time);
		if (row < 0)     return rowCellBoundaries[0];
		if (row >= rows) return rowCellBoundaries[rows];
		
		HourDivision hours = hourDivisions[row];
		return hours.timeToY(time);
	}
	
	/**
	 * Find the time corresponding to a point on the screen.
	 * 
	 * @param x An x coordinate on the screen.
	 * @param y A y coordinate on the screen.
	 * 
	 * @return A time in mills.
	 */
	private long xyToTime(int x, int y) {
		int row = yToRow(y);
		int col = xToCol(x);
		
		if (row == -1 || col == -1)
			return gridStartTime.getTimeInMillis();
		
		DayCell day = dayGrid[row][col];
		HourDivision div = hourDivisions[row];
		
		double h = div.yToHour(y);
		return (long) ( h*ONE_HOUR + day.getTimeStartMillis());
	}
	
	/**
	 * Find the column for an x coord.
	 * 
	 * @param x X coord on screen.
	 * 
	 * @return A column index.
	 */
	private int xToCol(int x) {
		if (x < colCellBoundaries[0]) return -1;
		if (x > colCellBoundaries[cols]) return -1;
		
		for (int j=0; j<cols; j++) {
			if (x < colCellBoundaries[j+1]) return j;
		}
		
		return -1;
	}
	
	/**
	 * Find the row corresponding to a y coordinate.
	 * 
	 * @param y Y coord on screen.
	 * 
	 * @return Row index.
	 */
	private int yToRow(int y) {
		if (y < rowCellBoundaries[0]) return -1;
		if (y > rowCellBoundaries[rows]) return -1;
		
		for (int i=0; i<rows; i++) {
			if (y < rowCellBoundaries[i+1]) return i;
		}
		
		return -1;
	}
	
	/**
	 * Draw the grid, the day labels, the week day labels, the hour labels.
	 * 
	 * @param gr Graphics object.
	 * @param comp Component to be painting (probably us?).
	 */
	private void draw(Graphics2D gr, Component comp) {
		int w  = comp.getWidth();
		int h  = comp.getHeight();
		
		gr.setColor(backgroundColor);
		gr.fillRect(0, 0, w, h);
		
		gr.setColor(dayLineColor);
		for (int i=0; i<rows; i++) {
			int y = rowCellBoundaries[i];
			gr.drawLine(0, y, w, y);
		}
		
		for (int j=1; j<cols; j++) {
			int x = colCellBoundaries[j];
			gr.drawLine(x, 0, x, h);
		}
		
		if (linesOnHours) {
			drawHourLines(gr, w, h);
		}
		if (labelHours) {
			drawHourLabels(gr);
		}
		
		drawColumnHeaders(gr);
		
		if (dayNumbers) {
			drawDayNumbers(gr);
		}
	}
	
	/**
	 * Draw the headers for the week days.
	 * 
	 * @param gr Graphics object.
	 */
	private void drawColumnHeaders(Graphics gr) {
		for (int j=0; j<cols; j++) {
			drawColumnHeader(gr, j);
		}
	}
	
	/**
	 * Draw a weekday header.
	 * 
	 * @param gr Graphics object.
	 * @param col The column to draw it for yo.
	 */
	private void drawColumnHeader(Graphics gr, int col) {
		String text = columnHeaders[col];
		if (activeCol != -1 && col != activeCol) {
			text = shortColumnHeaders[col];
		}
		
		gr.setColor(headerColor);
		gr.setFont(headerFont);
		FontMetrics met = gr.getFontMetrics();
		
		int x1 = colCellBoundaries[col];
		int x2 = colCellBoundaries[col+1];
		int len = met.stringWidth(text);
		
		int x = (x1+x2 - len)/2;
		int y = met.getHeight() + met.getDescent() + 4;
		
		gr.drawString(text, x, y);
	}
	
	/**
	 * Draw lines on the hours.
	 * 
	 * @param gr Graphics object.
	 * @param w Width of component.
	 * @param h Height of component.
	 */
	private void drawHourLines(Graphics2D gr, int w, int h) {
		gr.setColor(hourLineColor);
		
		for (int i=0; i<rows; i++) {
			drawHourLines(gr, w, h, i);
		}
	}
	
	/**
	 * Draw hour lines for one row.
	 * 
	 * @param gr Graphics object.
	 * @param w Width of component.
	 * @param h Height of component.
	 * @param row Row to draw lines for.
	 */
	private void drawHourLines(Graphics2D gr, int w, int h, int row) {
		HourDivision hourDivision = hourDivisions[row];
		for (int i=1; i<24; i++) {
			int y = hourDivision.hourToY(i);
			
			gr.drawLine(0, y, w, y);
		}
	}
	
	/**
	 * Label the hours that are big and visible.
	 * 
	 * @param gr Graphics object.
	 */
	private void drawHourLabels(Graphics2D gr) {
		for (int i=0; i<rows; i++) drawHourLabels(gr, i);
	}
	
	/**
	 * Label hours for one row.
	 * 
	 * @param gr Graphics object.
	 * @param row The row to label.
	 */
	private void drawHourLabels(Graphics2D gr, int row) {
		HourDivision div = hourDivisions[row];
		
		for (int i=1; i<24; i++) {
			if (div.isInteresting(i-1)) drawHourLabel(gr, row, i);
		}
	}
	
	/**
	 * Label one hour.
	 * 
	 * @param gr Graphics object.
	 * @param row Row its in.
	 * @param hour Hour to label.
	 */
	private void drawHourLabel(Graphics2D gr, int row, int hour) {
		gr.setColor(hourLabelColor);
		gr.setFont(hourLabelFont);
		
		String text = String.format("%02d:00", hour);
		
		int y = hourDivisions[row].hourToY(hour) - 2;;
		drawHourLabel(gr, row, 0,    y, text);
		drawHourLabel(gr, row, cols-1, y, text);
	}
	
	/**
	 * Draw the text of an hour label at a spec location.
	 * 
	 * @param gr Graphics object.
	 * @param row Row to draw in.
	 * @param col Column used to calc the X coord to draw at.
	 * @param y Y coord of text.
	 * @param text Text to draw.
	 */
	private void drawHourLabel(
		Graphics2D gr, int row, int col, int y, String text)
	{
		int x = colCellBoundaries[col] + 2;
		gr.drawString(text, x, y);
	}
	
	/**
	 * Draw the numbers in the day boxe.s
	 * 
	 * @param gr Graphics object.
	 */
	private void drawDayNumbers(Graphics2D gr) {
		for (int i=0; i<rows; i++)
		for (int j=0; j<cols; j++) {
			drawDayNumber(gr, i, j);
		}
	}
	
	/**
	 * Draw a number in a day box.
	 * 
	 * @param gr Graphics object.
	 * @param row Row of cell.
	 * @param col Col of cell.
	 */
	private void drawDayNumber(Graphics2D gr, int row, int col) {
		int x1 = colCellBoundaries[col];
		int y1 = rowCellBoundaries[row];
		
		DayCell day = dayGrid[row][col];
		Calendar date = day.getTimeStart();
		int number = date.get(Calendar.DAY_OF_MONTH);
		int month  = date.get(Calendar.MONTH);
		
		String text = String.valueOf(number);
		
		gr.setFont(dayNumberFont);
		FontMetrics met = gr.getFontMetrics();
		
		int x = x1 + dayInsets.left;
		int y = y1 + met.getHeight() + dayInsets.top;
		
		if (month == activeMonth || activeMonth == -1) {
			gr.setColor(dayNumberColor);
		}
		else {
			gr.setColor(inactiveDayNumberColor);
		}
		gr.drawString(text, x, y);
	}
	
	/**
	 * Update which boxes are considered selected.
	 * 
	 * @param box The box that was clicked on.
	 * @param selected True if it was selected.
	 */
	private void updateAppointmentSelection(
		AppointmentBox box, boolean selected)
	{
		if (selectionUpdateInProgress)
			return;
		selectionUpdateInProgress = true;
		
		AppointmentDisplay display = box.getDisplay();
		
		boolean wasSelected = display.isSelected();		
		display.setSelected(selected);
		
		if (display.isSelected() && !wasSelected) {
			activateAppointmentDisplay(display);
		}
		else if (!display.isSelected() && wasSelected) {
			deactivateAppointmentDisplay();
		}
		
		selectionUpdateInProgress = false;
	}
	
	/**
	 * Make a particular display the one that is being edited.
	 * 
	 * @param display The one that is going to be the one that is being
	 *  edited.
	 */
	private void activateAppointmentDisplay(AppointmentDisplay display) {
		if (activeAppointmentDisplay != null) {
			deactivateAppointmentDisplay();
		}
		
		activeAppointmentDisplay = display;
		
		viewState.setAppointment(display.getAppointment());
	}
	
	/**
	 * Make it so that no appointments are being edited.
	 */
	private void deactivateAppointmentDisplay() {
		if (activeAppointmentDisplay != null)
			activeAppointmentDisplay.setSelected(false);
		
		activeAppointmentDisplay = null;
		
		viewState.setAppointment(null);
	}
	
	/**
	 * The user clicked on the calendar. Let's handle it.
	 * 
	 * @param ev The MouseEvent corresponding to the click.
	 */
	private void clickOnCalendar(MouseEvent ev) {
		if ((ev.getButton() & MouseEvent.BUTTON2) != 0) {
			createAppointment(ev);
		}
		else if (ev.isControlDown()) {
			createAppointment(ev);
		}
		else if (ev.getClickCount() > 1) {
			activateDay(ev);
		}
		else if (activeAppointmentDisplay != null) {
			deactivateAppointmentDisplay();
		}
		else {
			toggleActiveDay(ev);
		}
	}
	
	/**
	 * Change to a day view based on a click.
	 * 
	 * @param ev The MouseEvent that inspired the change.
	 */
	private void activateDay(MouseEvent ev) {
		int x = ev.getX();
		int y = ev.getY();
		
		int row = yToRow(y);
		int col = xToCol(x);
		
		Calendar dayStart = dayGrid[row][col].getTimeStart();
		
		activateDay(dayStart);
	}
	
	/**
	 * Change to a day view.
	 * 
	 * @param dayStart The start of the day to activate.
	 */
	private void activateDay(Calendar dayStart) {
		viewState.setState(ViewState.State.DAY, dayStart);
	}
	
	/**
	 * Create an appointment at a clicked location.
	 * 
	 * @param ev The click.
	 */
	private void createAppointment(MouseEvent ev) {
		int x = ev.getX();
		int y = ev.getY();
		
		long time = xyToTime(x, y);
		
		createAppointment(time);
	}
	
	/**
	 * Create an appointment beginning at a specified time.
	 * 
	 * @param time Millis time at which to create an appointment.
	 */
	private void createAppointment(long time) {
		Calendar start = new GregorianCalendar();
		start.setTimeInMillis(time);
		
		Calendar end = (Calendar) start.clone();
		end.add(Calendar.MINUTE, 80);
		
		Appointment app = null;
		
		try {
			app = controller.createAppointment("", "", start, end);
		}
		catch (InvalidDateException ide) {
			viewState.setError("Invalid date: " + ide.getMessage());
			return;
		}
		
		try {
			controller.addAppointment(app);
		}
		catch (IllegalStateException ise) {
			viewState.setError(ise.getMessage());
			return;
		}
		catch (IdenticalAppointmentException iae) {
			// This is not reallllly an error
			return;
		}
		
		viewState.setAppointment(app);
	}
	
	/**
	 * Change whether a day is zoomed in.
	 * 
	 * @param ev The click.
	 */
	private void toggleActiveDay(MouseEvent ev) {
		int x = ev.getX();
		int y = ev.getY();
		
		int row = yToRow(y);
		int col = xToCol(x);
		
		if (row == -1 || col == -1) {
			activeRow = -1;
			activeCol = -1;
		}
		else if (row == activeRow && col == activeCol) {
			activeRow = -1;
			activeCol = -1;
		}
		else {
			activeRow = row;
			activeCol = col;
		}
		
		setupLayout();
	}
	
	/**
	 * Component-specific rendering.
	 */
	private class AreaUI extends PanelUI {
		@Override
		public void paint(Graphics gr, JComponent comp) {
			draw((Graphics2D) gr, comp);
		}
	}
	
	/**
	 * Stories information about a day in the grid.
	 */
	private class DayCell {
		/** col in grid */
		private int col;
		
		/** start of day */
		private Calendar timeStart;
		/** end of day */
		private Calendar timeEnd;
		
		/** start of day in millis */
		private long timeStartMillis;
		/** end of day in millis */
		private long timeEndMillis;
		
		/** Boxes contained within this day. */
		private ArrayList<AppointmentBox> boxes;
		
		/**
		 * Create a day and set fields.
		 * 
		 * @param row Row in grid.
		 * @param col Col in grid.
		 * @param timeStart Start of day.
		 * @param timeEnd End of day.
		 */
		DayCell(int row, int col, Calendar timeStart, Calendar timeEnd)
		{
			this.col       = col;
			this.timeStart = timeStart;
			this.timeEnd   = timeEnd;
			
			timeStartMillis = timeStart.getTimeInMillis();
			timeEndMillis   = timeEnd.getTimeInMillis();
			
			boxes = new ArrayList<AppointmentBox>();
		}
		
		/**
		 * Get the grid col.
		 * @return The grid col.
		 */
		int getCol() {
			return col;
		}
		
		/**
		 * Get the start of the day.
		 * @return The start of the day.
		 */
		Calendar getTimeStart() {
			return timeStart;
		}
		
		/**
		 * Get the start of the day in millis.
		 * @return The start of the day in millis.
		 */
		long getTimeStartMillis() {
			return timeStartMillis;
		}
		
		/**
		 * Does this appointment overlay the cell?
		 * 
		 * @param app The appointment in question.
		 * @return True if some of it is in the cell, false otherwise.
		 */
		boolean inCell(Appointment app) {
			return app.getStartTime().before(timeEnd) &&
				app.getEndTime().after(timeStart);
		}
		
		/**
		 * Make a box for the portion of an appointment in this day.
		 * 
		 * @param display The display of this appointment (includes multiple
		 *  boxes for multiple days).
		 * 
		 * @return A box for the appointment in this day.
		 */
		AppointmentBox makeBox(AppointmentDisplay display) {
			Appointment app = display.getAppointment();
			
			long appStart = app.getStartTime().getTimeInMillis();
			long appEnd   = app.getEndTime().getTimeInMillis();
			
			long boxStart = Math.max(appStart, timeStartMillis);
			long boxEnd   = Math.min(appEnd, timeEndMillis);
			
			return new AppointmentBox(display, this, boxStart, boxEnd);
		}
		
		/**
		 * Add a box to the ones we own.
		 * @param box A box in this day.
		 */
		void addBox(AppointmentBox box) {
			boxes.add(box);
		}
		
		/**
		 * Get all the boxes in today.
		 * @return A list of all boxes in this day.
		 */
		ArrayList<AppointmentBox> getBoxes() {
			return new ArrayList<AppointmentBox>(boxes);
		}
	}
	
	/**
	 * How the lines on the hours are layed out within a row.
	 */
	private class HourDivision {
		/** the row we're in. */
		private int row;
		
		/** Which of the hours have stuff in them? */
		private boolean[] interesting;
		/** Where (as percentage of total y) do the hours fall? */
		private double[] hourFractions;
		
		/**
		 * Construct a blank hour division.
		 * 
		 * @param row The row we're about.
		 */
		HourDivision(int row) {
			this.row = row;
			
			hourFractions = new double[25];
		}
		
		/**
		 * Set which hours are interesting.
		 * 
		 * @param interesting A vector saying whether each hour has stuff
		 * in it.
		 */
		void setInteresting(boolean[] interesting) {
			this.interesting = interesting;
			
			double[] weights = new double[24];
			
			for (int i=0; i<24; i++) {
				if (interesting[i]) weights[i] = hourWeight;
				else weights[i] = 1.0;
			}
			
			setWeights(weights);
		}
		
		/**
		 * Query whether a particular hour is interesting.
		 * 
		 * @param hour The hour to check.
		 * @return true if it's interesting, false otherwise.
		 */
		boolean isInteresting(int hour) {
			return interesting[hour];
		}
		
		/**
		 * Set the hour fractions based on weighted size for each hour.
		 * 
		 * @param weights The relative sizes of the hours.
		 */
		void setWeights(double[] weights) {
			weightedPartition(weights, 0.0, 1.0, hourFractions);
		}
		
		/**
		 * Get the hour at a screen y coord.
		 * 
		 * @param y A screen y coord.
		 * @return A fractional hour.
		 */
		double yToHour(int y) {
			double f = yToFraction(y);
			if (f <= 0.0) return 0;
			
			for (int i=0; i<24; i++) {
				if (hourFractions[i+1] < f) continue;
				
				double f1 = hourFractions[i];
				double f2 = hourFractions[i+1];
				
				double h1 = i;
				double h2 = i + 1;
				
				return (f - f1) / (f2 - f1) * (h2 - h1) + h1;
			}
			
			return 24.0;
		}
		
		/**
		 * How high up is this y?
		 * 
		 * @param y Y coord on screen.
		 * @return Fraction of total height.
		 */
		double yToFraction(int y) {
			return (double) (y - rowCellBoundaries[row])
				/ (rowCellBoundaries[row+1] - rowCellBoundaries[row]);
		}
		
		/**
		 * Get where within the vertical display a time falls.
		 * 
		 * @param time A time in millis.
		 * @return fraction of vertical size.
		 */
		double timeToFraction(long time) {
			long part = (time - gridStartMillis) % ONE_DAY;
			double hour = (double) part / ONE_HOUR;
			
			return hourToFraction(hour);
		}
		
		/**
		 * Get where within the vertical day an hour falls.
		 * 
		 * @param hour
		 * @return
		 */
		double hourToFraction(double hour) {
			int baseHour = (int) hour;
			double fractionHour = hour - baseHour;
			
			if (baseHour < 0) return 0.0;
			if (baseHour >= 24) return 1.0;
			
			double f1 = hourFractions[baseHour];
			double f2 = hourFractions[baseHour + 1];
			
			return f1 + (f2 - f1)*fractionHour;
		}
		
		/**
		 * Where vertically is this time?
		 * 
		 * @param time
		 * @return
		 */
		int timeToY(long time) {
			double f = timeToFraction(time);
			return fractionToY(f);
		}
		
		/**
		 * Get the screen y coord a given fraction down the cell.
		 * 
		 * @param f Vertical fraction
		 * @return screen y coord.
		 */
		int fractionToY(double f) {
			int y1 = rowCellBoundaries[row];
			int y2 = rowCellBoundaries[row + 1];
			
			return (int) (y1 + f*(y2-y1));
		}
		
		/**
		 * Get a screen y coord at a fractional hour.
		 * 
		 * @param hour A fractional hour.
		 * @return A screen y coord.
		 */
		int hourToY(double hour) {
			double f = hourToFraction(hour);
			return fractionToY(f);
		}
	}
	
	/**
	 * A bunch of boxes that display an appointment.
	 * 
	 * Why would we have multiple boxes for one appointment? Because days
	 * move left-to-right, if an appointment spans days it has to be split up.
	 */
	private class AppointmentDisplay {
		/** The appointment all these boxes are for. */
		private Appointment appointment;
		/** A box for each day it's in. */
		private ArrayList<AppointmentBox> boxes;
		/** True if this is the appointment being edited. */
		private boolean selected;
		
		/**
		 * Create the display with no boxes at present.
		 * 
		 * @param app The appointment to be displayed.
		 */
		AppointmentDisplay(Appointment app) {
			this.appointment = app;
			
			boxes = new ArrayList<AppointmentBox>();
		}
		
		/**
		 * Get the appointment for this display.
		 * @return The appointment for this display.
		 */
		Appointment getAppointment() {
			return appointment;
		}
		
		/**
		 * Add a box for this display.
		 * @param box a box for this appointment on some day.
		 */
		void addBox(AppointmentBox box) {
			boxes.add(box);
		}
		
		/**
		 * Is this the appointment being edited?
		 * @return True if this is the appointment being edited.
		 */
		boolean isSelected() {
			return selected;
		}
		
		/**
		 * Set whether this display is selected.
		 * 
		 * Also sets the visual appearance of the boxes to appear selected
		 * or not.
		 * 
		 * @param selected True if this is the appointment being edited,
		 * false otherwise.
		 */
		void setSelected(boolean selected) {
			this.selected = selected;
			
			for (AppointmentBox box : boxes) {
				box.setSelected(selected);
			}
		}
	}
	
	private class AppointmentBox implements Comparable<AppointmentBox> {
		private static final long serialVersionUID = 523052L;
		
		/** The display this is part of. */
		private AppointmentDisplay display;
		/** The day this is in. */
		private DayCell cell;
		
		/** Start of the box in millis. */
		private long start;
		/** End of the box in millis. */
		private long end;
		
		/** This serves as the visual display of the box. */
		private JToggleButton button;
		
		/**
		 * Create a box.
		 * 
		 * @param display Display this is part of.
		 * @param cell Day this is part of.
		 * @param start Start time in millis.
		 * @param end End time in millis.
		 */
		AppointmentBox(AppointmentDisplay display, DayCell cell,
				long start, long end)
		{
			this.display = display;
			this.cell    = cell;
			this.start   = start;
			this.end     = end;
			
			button = new JToggleButton();
			button.setUI(new AppointmentBoxUI(this, button));
			
			button.setVerticalAlignment(SwingConstants.TOP);
			button.setHorizontalAlignment(SwingConstants.LEFT);
			button.setMargin(boxInsets);
			
			button.setForeground(boxTextColor);
			
			button.addItemListener(new AppointmentBoxItemListener(this));
		}
		
		/**
		 * Get the component that displays the box.
		 * @return The component that displays the box.
		 */
		Component getComponent() {
			return button;
		}
		
		/**
		 * Get the display this is part of.
		 * @return The display this is part of.
		 */
		AppointmentDisplay getDisplay() {
			return display;
		}
		
		/**
		 * Get the day this is part of.
		 * @return The day this is part of.
		 */
		DayCell getDay() {
			return cell;
		}
		
		/**
		 * Set whether the box is selected.
		 * @param selected True if selected, false otherwise.
		 */
		void setSelected(boolean selected) {
			button.setSelected(selected);
		}
		
		/**
		 * Get start time in millis.
		 * @return Start time in millis.
		 */
		long getStart() {
			return start;
		}
		
		/**
		 * Get end time in millis.
		 * @return End time in millis.
		 */
		long getEnd() {
			return end;
		}
		
		@Override
		public int compareTo(AppointmentBox box) {
			if (start < box.start) return -1;
			if (start > box.start) return +1;
			return 0;
		}
	}
	
	/**
	 * Handles drawing of the appointment box area.
	 */
	private class AppointmentBoxUI extends BasicToggleButtonUI {
		/** The box that the component displays. */
		private AppointmentBox box;
		/** The actual component. */
		private JToggleButton button;
		
		/**
		 * Create this UI.
		 * 
		 * @param box The box represented by the component.
		 * @param button The component to be rendered.
		 */
		AppointmentBoxUI(AppointmentBox box, JToggleButton button) {
			this.box    = box;
			this.button = button;
		}
		
		@Override public void paint(Graphics gr, JComponent comp) {
			int w  = comp.getWidth();
			int h  = comp.getHeight();
			
			if (box.getDisplay().isSelected())
				gr.setColor(boxSelectedColor);
			else
				gr.setColor(boxUnselectedColor);
			
			gr.fillRect(0, 0, w, h);
			
			Rectangle bounds = button.getBounds();
			Insets insets = button.getInsets();
			
			bounds.x = insets.left;
			bounds.y = 0;
			bounds.width  -= insets.left + insets.right;
			bounds.height -= insets.bottom + insets.top;
			
			gr.setFont(boxFont);
			FontMetrics met = gr.getFontMetrics();
			
			Appointment app = box.getDisplay().getAppointment();
			
			String description = app.getDescription();
			String location    = app.getLocation();
			String start       = boxDateFormat.format(
				app.getStartTime().getTime());
			String end         = boxDateFormat.format(
				app.getEndTime().getTime());
			
			String times = String.format("%s - %s", start, end);
			
			int labelX = bounds.x;
			int labelY = bounds.y + met.getHeight();
			int height = met.getHeight();
			int width  = met.stringWidth(description);
			
			boolean drawRest = true;
			
			int cutoff1 = labelY;
			int cutoff2 = labelY + height*2 + 2;
			int cutoff = cutoff2;
			
			if (cutoff2 - bounds.y > bounds.height){
				drawRest = false;
				cutoff = cutoff1;
			}
			
			if (cutoff - bounds.y > bounds.height) {
				int currentPs = boxFont.getSize();
				int newPs = currentPs * bounds.height / (cutoff - bounds.y);
				
				gr.setFont(new Font(boxFont.getFamily(),
					boxFont.getStyle(), newPs));
				
				met = gr.getFontMetrics();
				height = met.getHeight();
				labelY = bounds.y + height;
			}
			
			gr.setColor(boxTextColor);
			gr.drawString(description, labelX, labelY);
			
			if (drawRest) {
				gr.setColor(boxSecondaryTextColor);
				gr.drawString(location,    labelX, labelY + height*1 + 2);
				gr.drawString(times,       labelX, labelY + height*2 + 2);
			}
			
			if (button.hasFocus()) {
				gr.setColor(boxTextColor);
				gr.drawLine(labelX, labelY+3, labelX + width, labelY+3);
			}
		}
	}
	
	/**
	 * Listen or clicking on an appointment box button.
	 */
	private class AppointmentBoxItemListener implements ItemListener {
		/** The box we're listener for. */
		AppointmentBox box;
		
		/**
		 * Create the listener.
		 * @param box The box we're listening for.
		 */
		AppointmentBoxItemListener(AppointmentBox box) {
			this.box = box;
		}
		
		@Override
		public void itemStateChanged(ItemEvent e) {
			updateAppointmentSelection(box,
				e.getStateChange() == ItemEvent.SELECTED);
		}
	}
	
	/**
	 * Listen for resizes so we can re-layout stuff.
	 */
	private class AreaComponentListener implements ComponentListener {
		
		@Override public void componentHidden(ComponentEvent e) { }
		@Override public void componentMoved(ComponentEvent e) { }
		@Override public void componentShown(ComponentEvent e) { }
		
		@Override
		public void componentResized(ComponentEvent e) {
			setupLayout();
		}
	}
	
	/**
	 * Listen for clicks and call clickOnCalendar().
	 * @see clickOnCalendar()
	 */
	private class AreaMouseListener implements MouseListener {
		
		@Override
		public void mouseClicked(MouseEvent e) {
			clickOnCalendar(e);
		}
		
		@Override public void mouseEntered(MouseEvent e) { }
		@Override public void mouseExited(MouseEvent e) { }
		@Override public void mousePressed(MouseEvent e) { }
		@Override public void mouseReleased(MouseEvent e) { }
	}
}
