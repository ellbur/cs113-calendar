
package cs113.calendar.simpleview;

import java.util.*;
import java.util.regex.*;
import java.io.*;

import cs113.calendar.control.*;
import cs113.calendar.util.*;
import cs113.calendar.view.*;
import cs113.calendar.model.*;

/**
 * Command line interface to the calendar.
 * 
 * As an implementation of View, this class receives a Controller from the call
 * to runView(). Operations then are performed on that controller, and
 * Controller.writeAll() will be called before the application exits.
 * 
 * @author Owen Healy
 */
public class SimpleView implements View {
	
	/**
	 * Controller that we are acting on.
	 */
	private Controller controller;
	
	/**
	 * Handles initial commands specified at program invocation.
	 */
	private CommandLineEngine entryEngine;
	
	/**
	 * Handles commands in interractive mode.
	 */
	private CommandLineEngine interactiveEngine;
	
	/**
	 * A command sets this to true to make the program exit.
	 */
	private boolean shouldExit;
	
	/**
	 * Starts the program by parsing the command supplied on the command line.
	 * 
	 * If the result of the command is to enter interactive mode, that will
	 * happen without this method returning. This method might not return
	 * for a very long time.
	 * 
	 * @param args Command line arguments.
	 */
	private void parseEntryCommand(String[] args) {
		setupEntryEngine();
		
		if (args.length == 0) {
			return;
		}
		
		String name = args[0];
		ArrayList<String> rest = new ArrayList<String>(args.length - 1);
		
		for (int i=1; i<args.length; i++) {
			rest.add(args[i]);
		}
		
		try {
			entryEngine.doCommandLine(controller, name, rest);
		}
		catch (NoSuchCommandException nsce) {
			System.out.printf("Error: no such command: %s\n", name);
		}
	}
	
	/**
	 * Logs in the user and then goes into interactive mode.
	 * This method is not going to return for awhile.
	 * 
	 * Errors that occur in here get sent to System.out, so we don't throw
	 * any exceptions.
	 * 
	 * @param user The user to login as.
	 */
	private void login(User user) {
		try {
			controller.login(user.getUserId());
		}
		catch (NoSuchUserException nsue) {
			System.out.printf("user %s does not exist\n", user.getUserId());
			return;
		}
		catch (StorageException se) {
			System.out.printf("Error: problem writing to storage: %s\n",
				se.toString());
			return;
		}
		
		startInteractiveMode();
	}
	
	/**
	 * Sets up interactive command and starts reading user input.
	 * 
	 * Does not return for a long time.
	 */
	private void startInteractiveMode() {
		
		setupInteractiveEngine();
		
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(System.in));
		
		// Main command loop
		for (;;) {
			
			String line;
			
			try {
				line = reader.readLine();
			}
			catch (IOException ioe) {
				System.out.println("Error: error reading from terminal");
				break;
			}
			
			if (line == null)
				break;
			
			try {
				interactiveEngine.doCommandLine(controller, line);
			}
			catch (NoSuchCommandException nsce) {
				System.out.printf("Error: no such command: %s\n",
						nsce.getCommand());
			}
			catch (CommandFormatException cfe) {
				System.out.printf("Error: incorrect format for command\n");
			}
			
			if (shouldExit) {
				break;
			}
		}
	}
	
	/**
	 * Sets up handlers for initial commands.
	 */
	private void setupEntryEngine() {
		entryEngine = new CommandLineEngine();
		
		entryEngine.addAction("listusers",  null, new ListUsersCommand());
		entryEngine.addAction("adduser",    null, new AddUserCommand());
		entryEngine.addAction("deleteuser", null, new DeleteUserCommand());
		entryEngine.addAction("login",      null, new LoginCommand());
	}
	
	/**
	 * Sets up handlers for interactive commands.
	 */
	private void setupInteractiveEngine() {
		interactiveEngine = new CommandLineEngine();
		
		interactiveEngine.addAction("create",
			"^\\s*\\\"(.*)\\\"\\s+\\\"(.*)\\\"\\s+(.+)\\s+(.+)\\s*$",
			new CreateCommand());
		interactiveEngine.addAction("delete",
			"^\\s*\\\"(.*)\\\"\\s+\\\"(.*)\\\"\\s+(.+)\\s+(.+)\\s*$",
			new DeleteCommand());	
		interactiveEngine.addAction("list",
			"^\\s*(.+)\\s+(.+)\\s*$",
			new ListCommand());
		interactiveEngine.addAction("logout",
			"",
			new LogoutCommand());
	}
	
	/**
	 * Called by a command handler to cause the program to exit.
	 */
	private void exitAfterCompletion() {
		shouldExit = true;
	}
	
	// -------------------------------------------------------------------
	
	/**
	 * Starts the view running. This is the first method called.
	 */
	@Override
	public void runView(Controller controller, String[] args) {
		this.controller = controller;
		
		parseEntryCommand(args);
		
		try {
			controller.writeAll();
		}
		catch (StorageException se) {
			System.out.printf("Error: problem saving stuff: %s\n",
				se.toString());
		}
	}
	
	// -------------------------------------------------------------------
	// View-specific utility methods
	
	/**
	 * 
	 * @throws DateFormatException if the string is not in a format we
	 *  want it to be in.
	 * @throws InvalidDateException if this is not a possible date.
	 */
	private static Calendar parseDate(String userString)
		throws DateFormatException, InvalidDateException
	{
		String patternString = "(\\d\\d)\\/(\\d\\d)\\/(\\d\\d\\d\\d)\\"
			+ "-(\\d\\d)\\:(\\d\\d)";
		
		// MM/DD/YYYY-HH:MM
		Pattern datePattern = Pattern.compile(patternString);
		
		Matcher matcher = datePattern.matcher(userString);
		
		if (! matcher.find()) {
			throw new DateFormatException(userString, "MM/DD/YYYY-HH:MM");
		}
		
		String monthString   = matcher.group(1);
		String dayString     = matcher.group(2);
		String yearString    = matcher.group(3);
		String hourString    = matcher.group(4);
		String minuteString  = matcher.group(5);
		
		int month  = 0;
		int day    = 0;
		int year   = 0;
		int hour   = 0;
		int minute = 0;
		
		try {
			// 0-indexed month
			month  = Integer.parseInt(monthString) - 1;
			
			day    = Integer.parseInt(dayString);
			year   = Integer.parseInt(yearString);
			hour   = Integer.parseInt(hourString);
			minute = Integer.parseInt(minuteString);
		}
		catch (NumberFormatException nfe) {
			throw new DateFormatException(userString, "MM/DD/YYYY-HH:MM");
		}
		
		GregorianCalendar cal = new GregorianCalendar(
				year, month, day, hour, minute);
		cal.set(Calendar.MILLISECOND, 0);
		
		// Sanity checks
		
		// can throw an InvalidDateException
		checkField(cal, Calendar.YEAR,          year,    "year");
		checkField(cal, Calendar.MONTH,         month,   "month");
		checkField(cal, Calendar.DAY_OF_MONTH,  day,     "day");
		checkField(cal, Calendar.HOUR_OF_DAY,   hour,    "hour");
		checkField(cal, Calendar.MINUTE,        minute,  "minute");
		
		return cal;
	}
	
	/**
	 * Checks the validity of a date field.
	 * 
	 * @param cal Calender to operate on.
	 * @param field index of the field.
	 * @param value value of the field.
	 * @param name name of the field.
	 * 
	 * @throws InvalidDateException if the field is not valid.
	 */
	private static void checkField(
			GregorianCalendar cal, int field, int value, String name)
		throws InvalidDateException
	{
		cal.set(field, cal.getGreatestMinimum(field));
		
		int max = cal.getActualMaximum(field);
		int min = cal.getActualMinimum(field);
		
		cal.set(field, value);
		
		if (value < min || max < value) {
			throw new InvalidDateException(String.format(
				"%s is out of range [%d, %d]", name, min, max));
		}
	}
	
	/**
	 * Converts a date to spec'd string representation.
	 * 
	 * @param date A Calednar date.
	 * @return a string representation in the form MM/DD/YYYY-HH:MM.
	 */
	private static String formatDate(Calendar date) {
		return String.format("%02d/%02d/%04d-%02d:%02d",
			date.get(Calendar.MONTH) + 1,
			date.get(Calendar.DAY_OF_MONTH),
			date.get(Calendar.YEAR),
			date.get(Calendar.HOUR),
			date.get(Calendar.MINUTE));
	}
	
	/**
	 * Parses a date from a string, printing to stdout if something goes wrong.
	 * 
	 * @param string The text supplied by the user.
	 * @return A Calendar representing the date.
	 * 
	 * @throws InvalidDateException If the date is not meaningful.
	 * @throws DateFormatException If the string is improperly formatted.
	 */
	private Calendar parseDateWithFeedback(String string)
		throws InvalidDateException, DateFormatException
	{
		try {
			return parseDate(string);
		}
		catch (DateFormatException dfe) {
			
			System.out.printf("Error: date '%s' does not "
					+ "conform to format '%s'\n",
				dfe.getBadString(),
				dfe.getExpectedFormat() );
			
			throw dfe;
		}
		catch (InvalidDateException ide) {
			
			System.out.printf("Error: invalid date '%s': %s",
				string, ide.getMessage());
			
			throw ide;
		}
	}
	
	/**
	 * Creates an appointment from a string representation, telling the user
	 * about any problems via stdout.
	 * 
	 * The Appointment object has NOT been saved, and must be added to the
	 * current user's appointments if that is desired.
	 * 
	 * @param descriptionString text description.
	 * @param locationString text location.
	 * @param startString start time of event.
	 * @param endString end time of event.
	 * 
	 * @return unsaved Appointment object
	 * 
	 * @throws InvalidDateException If the dates are not nice.
	 * @throws DateFormatException If the dates are not formatted correctly.
	 */
	private Appointment parseAppointmentWithFeedback(
			String descriptionString,
			String locationString,
			String startString,
			String endString)
		throws InvalidDateException, DateFormatException
	{
		Calendar start = null;
		Calendar end   = null;
		
		String description = descriptionString;
		String location    = locationString;
		
		start = parseDateWithFeedback(startString);
		end   = parseDateWithFeedback(endString);
		
		if (end.before(start)) {
			System.out.printf("Error: end before start\n");
			throw new InvalidDateException("end before start");
		}
		
		return controller.createAppointment(
				description, location, start, end);
	}
	
	/**
	 * Creates a string representation of an Appointment of the same form
	 * as would be input by the user to create an appointment.
	 * 
	 * @param app The Appointment to format.
	 * 
	 * @return A string representation of the appointment.
	 */
	private String formatAppointment(Appointment app) {
		return String.format("\"%s\" \"%s\" %s %s", 
			app.getDescription(),
			app.getLocation(),
			
			formatDate(app.getStartTime()),
			formatDate(app.getEndTime()));
	}
	
	// -------------------------------------------------------------------
	
	/**
	 * Lists all current user IDs.
	 */
	private class ListUsersCommand implements CommandLineAction {
		
		@Override
		public void doAction(Controller controller, List<String> args) {
			
			Set<String> ids = controller.listUsers();
			
			ArrayList<String> idList = new ArrayList<String>(ids);
			Collections.sort(idList);
			
			for (String id : idList) {
				System.out.printf("%s\n", id);
			}
		}
	}
	
	/**
	 * Adds a user based on a name and ID.
	 */
	class AddUserCommand implements CommandLineAction {
		
		@Override
		public void doAction(Controller controller, List<String> args) {
			
			// <userid> <username>
			if (args.size() != 2) {
				System.out.println("Error: adduser wants two arguments.");
				return;
			}
			
			String id   = args.get(0);
			String name = args.get(1);
			
			try {
				controller.createUser(id, name);
			}
			catch (DuplicateUserIdException e) {
				User user = null;
				
				try {
					user = controller.getUser(id);
				}
				catch (NoSuchUserException e2) {
					System.out.println("Error: internal error");
					return;
				}
				catch (StorageException se) {
					System.out.printf("Error: problem reading from " + 
						"storage: %s\n", se.toString());
					return;
				}
				
				System.out.printf("user %s already exists with name \"%s\"\n",
						user.getUserId(), user.getFullName());
				return;
			}
			catch (StorageException se) {
				System.out.printf("Error: problem writing to storage: %s\n",
					se.toString());
				return;
			}
			
			System.out.printf("created user %s with name \"%s\"\n",
					id, name);
		}
	}
	
	/**
	 * Deletes a user based on an ID.
	 */
	class DeleteUserCommand implements CommandLineAction {
		
		@Override
		public void doAction(Controller controller, List<String> args) {
			
			// <userid>
			if (args.size() != 1) {
				System.out.println("Error: deleteuser wants one argument.");
				return;
			}
			
			String id = args.get(0);
			
			try {
				controller.deleteUser(id);
			}
			catch (NoSuchUserException e) {
				System.out.printf("user %s does not exist\n", id);
				return;
			}
			catch (StorageException se) {
				System.out.printf("Error: problem writing to storage: %s\n",
					se.toString());
			}
			
			System.out.printf("deleted user %s\n", id);
		}
	}
	
	/**
	 * Starts interactive mode for a user ID.
	 */
	class LoginCommand implements CommandLineAction {
		
		@Override
		public void doAction(Controller controller, List<String> args) {
			
			// <userid>
			if (args.size() != 1) {
				System.out.println("Error: login wants one argument");
				return;
			}
			
			String id = args.get(0);
			User user = null;
			
			try {
				user = controller.getUser(id);
			}
			catch (NoSuchUserException e) {
				System.out.printf("user %s does not exist\n", id);
				return;
			}
			catch (StorageException se) {
				System.out.printf("Error: problem reading from storage: %s",
						se.toString());
				return;
			}
			
			login(user);
		}
	}
	
	/**
	 * In interactive mode, creates an appointment.
	 */
	class CreateCommand implements CommandLineAction {
		
		@Override
		public void doAction(Controller controller, List<String> args) {
			
			// description location start end
			if (args.size() != 4) {
				System.out.println("Error: creae wants 4 arguments");
				return;
			}
			
			Appointment app = null;
			
			try {
				app = parseAppointmentWithFeedback(
					args.get(0),
					args.get(1),
					args.get(2),
					args.get(3) );
			}
			catch (InvalidDateException e) {
				return;
			}
			catch (DateFormatException e) {
				return;
			}
			
			List<Appointment> conflicts = controller.findConflicts(app);
			
			try {
				controller.addAppointment(app);
			}
			catch (IdenticalAppointmentException iae) {
				System.out.printf("Appointment exists for user %s:\n",
					controller.getCurrentUserID());
				System.out.printf("%s\n", formatAppointment(app));
				
				return;
			}
			
			System.out.printf("created appointment for %s:\n",
				controller.getCurrentUserID());
			System.out.printf("%s\n", formatAppointment(app));
			
			for (Appointment apc : conflicts) {
				System.out.printf("conflicts with %s\n",
					formatAppointment(apc));
			}
		}
	}
	
	/**
	 * In interactive mode, deletes an appointment.
	 */
	class DeleteCommand implements CommandLineAction {
		
		@Override
		public void doAction(Controller controller, List<String> args) {
			
			// description location start end
			if (args.size() != 4) {
				System.out.printf("Error: delete expects 4 arguments\n");
				return;
			}
			
			Appointment app = null;
			
			try {
				app = parseAppointmentWithFeedback(
					args.get(0),
					args.get(1),
					args.get(2),
					args.get(3) );
			}
			catch (InvalidDateException e) {
				return;
			}
			catch (DateFormatException e) {
				return;
			}
			
			try {
				controller.deleteAppointment(app);
			}
			catch (NoSuchAppointmentException nsae) {
				System.out.printf("appointment does not exist for user %s:\n",
					controller.getCurrentUserID());
				System.out.printf("%s\n", formatAppointment(app));
				
				return;
			}
			
			System.out.printf("deleted appointment from user %s:\n",
				controller.getCurrentUserID());
			System.out.printf("%s\n",
				formatAppointment(app));
		}
	}
	
	/**
	 * In interactive mode, lists appointments in a range.
	 */
	class ListCommand implements CommandLineAction {
		
		@Override
		public void doAction(Controller controller, List<String> args) {
			
			// start end
			if (args.size() != 2) {
				System.out.printf("list wants 2 arguments\n");
				return;
			}
			
			Calendar start = null;
			Calendar end   = null;
			
			try {
				start = parseDateWithFeedback(args.get(0));
				end   = parseDateWithFeedback(args.get(1));
			}
			catch (InvalidDateException e) {
				return;
			}
			catch (DateFormatException e) {
				return;
			}
			
			List<Appointment> appList = controller.listAppointmentsInRange(
					start, end);
			
			System.out.printf("Appointments for user %s in range %s to %s:\n",
				controller.getCurrentUserID(),
				formatDate(start),
				formatDate(end));
			
			for (Appointment app : appList) {
				System.out.printf("%s\n", formatAppointment(app));
			}
		}
	}
	
	/**
	 * In interactive mode, logs out and causes the program to exit.
	 */
	class LogoutCommand implements CommandLineAction {
		
		@Override
		public void doAction(Controller controller, List<String> args) {
			
			// <no args>
			if (args.size() != 0) {
				System.out.printf("Error: logout expects no arguments");
				return;
			}
			
			exitAfterCompletion();
		}
	}
}
