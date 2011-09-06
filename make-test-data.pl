#!/usr/bin/perl

our $command = "java -cp bin cs113.calendar.simpleview.CmdView";
our $data_file = "./data/users.ser";

sub cal {
	my $arg = shift;
	`$command $arg` or die("Failed to run $command $arg: $!");
}

sub cali {
	my $arg = shift;
	my $input = shift;
	
	open(CAL, "| $command $arg >/dev/null")
		or die("Failed to popen calendar: $!");
	
	print(CAL $input) or die("Failed to write input: $!");
}

# Clear it first so we start fresh
unlink($data_file) or die("Failed to clear data: $!");

cal('adduser testuser "J Mesmon"');
cali('login testuser', 'create "Thinking" "Morrow 2203" '
	. '11/17/2009-14:52 11/17/2009-16:30');
cali('login testuser', 'create "Thinking" "Morrow 2203" '
	. '11/19/2009-12:52 11/19/2009-16:30');

