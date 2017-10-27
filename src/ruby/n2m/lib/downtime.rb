class Downtime

	attr_accessor :status, :type, :start_time, :end_time

	# has to match order of error messages in ALERT array
	ALERT_TYPES = {
		0 => "ssh",
		1 => "ping",
		2 => "ping",
		3 => "ssh",

		4 => "ssh",
		5 => "ping",
		6 => "ping",
		7 => "ssh"
	}
	
	def initialize(type, start)
		@status = 1
		@type = type
		@start_time = start
		@end_time = ""
	end

	def alert_type_to_s
		ALERT_TYPES[@type]	
	end

end


