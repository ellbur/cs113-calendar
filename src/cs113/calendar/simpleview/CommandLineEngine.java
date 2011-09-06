
package cs113.calendar.simpleview;

import cs113.calendar.control.*;

import cs113.calendar.util.CommandFormatException;
import cs113.calendar.util.NoSuchCommandException;

import java.util.*;
import java.util.regex.*;

/**
 * Handles a set of stored commands indexed by name.
 * 
 * After creating the CommandLineEngine, the client can add commands to it
 * with addAction(). These commands are stored by their name and can be
 * invoked with doCommandLine();
 * 
 * Commands can be given regex formats for argument parsing.
 * 
 * @author Owen Healy
 */
public class CommandLineEngine {
	
	/**
	 * Stored commands.
	 */
	private HashMap<String, CommandLineEntry> actionTable;
	
	/**
	 * Creates an empty CommandLineEngine, with no commands.
	 * 
	 * Commands may then be added with addAction().
	 */
	public CommandLineEngine() {
		actionTable = new HashMap<String, CommandLineEntry>();
	}
	
	/**
	 * Adds an action to the pool of actions that we can handle.
	 * 
	 * @param name Key that identifies this action.
	 * @param action The action to be added.
	 */
	public void addAction(
			String name,
			String argFormat,
			CommandLineAction action)
	{
		actionTable.put(name, new CommandLineEntry(name, argFormat, action));
	}
	
	/**
	 * Finds the named command, then invokes it, passing along the specified
	 * arguments.
	 * 
	 * @param controller The controller on which to apply the action.
	 * @param command The name of the command to invoke.
	 * @param args The arguments to the command.
	 * 
	 * @throws NoSuchCommandException if we can't find an appropriate
	 *  CommandLineAction.
	 */
	public void doCommandLine(
		Controller controller,
		String command,
		List<String> args
	)
		throws NoSuchCommandException
	{
		CommandLineEntry entry = actionTable.get(command);
		if (entry == null) {
			throw new NoSuchCommandException(command);
		}
		
		entry.getAction().doAction(controller, args);
	}
	
	/**
	 * Finds the first token. Uses this as a command name, and identifies
	 * the format string for the arguments, along with the implementation to
	 * be called.
	 * 
	 * Parsers the remainder of the string according to the prescribed format,
	 * and then invokes the command.
	 * 
	 * @param controller The controller on which to apply the action.
	 * @param text The text given to us.
	 * 
	 * @throws NoSuchCommandException if we can't find the command name in
	 *  our table.
	 * @throws CommandFormatException if the text does not conform to the
	 *  expected format.
	 */
	public void doCommandLine(Controller controller, String text)
		throws NoSuchCommandException, CommandFormatException
	{
		String command = null;
		String rest    = null;
		
		int spaceIndex = text.indexOf(' ');
		if (spaceIndex == -1) {
			command = text.trim();
			rest = "";
		}
		else {
			command = text.substring(0, spaceIndex);
			rest    = text.substring(spaceIndex+1);
		}
		
		CommandLineEntry entry = actionTable.get(command);
		if (entry == null) {
			throw new NoSuchCommandException(command);
		}
		
		Pattern pattern = Pattern.compile(entry.getFormat());
		Matcher matcher = pattern.matcher(rest);
		
		if (! matcher.find()) {
			throw new CommandFormatException(text, entry.getFormat());
		}
		
		int numArgs = matcher.groupCount();
		ArrayList<String> args = new ArrayList<String>(numArgs);
		
		for (int i=1; i<=numArgs; i++) {
			args.add(matcher.group(i));
		}
		
		entry.getAction().doAction(controller, args);
	}
	
	/**
	 * Wrapper for command names, formats and actions.
	 * 
	 * @author Owen Healy
	 */
	private class CommandLineEntry {
		
		/**
		 * Name of the command; used to invoke it.
		 */
		String name;
		
		/**
		 * Argument format. This can be null if the comand takes pre-parsed
		 * arguments. (e.g. from the initial command line arguments).
		 */
		String format;
		
		/**
		 * Invoked to process the command
		 */
		CommandLineAction action;
		
		/**
		 * Create a CommandLineEntry and initialize the fields.
		 * 
		 * @param name Name of the command.
		 * @param format Regex format of the command.
		 * @param action Invoked to process the command.
		 */
		CommandLineEntry(
				String name,
				String format,
				CommandLineAction action)
		{
			this.name   = name;
			this.format = format;
			this.action = action;
		}
		
		/**
		 * @return The name of the command.
		 */
		@SuppressWarnings("unused")
		String getName() {
			return name;
		}
		
		/**
		 * @return The regex format of the command, or null if it was not set.
		 */
		String getFormat() {
			return format;
		}
		
		/**
		 * @return A CommandLineAction associated with this command.
		 */
		CommandLineAction getAction() {
			return action;
		}
	}
}
