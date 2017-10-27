class Parser

	include Logger

	attr_reader :hosts

	@logdir 	= ''
	@files 		= []
	@hosts		= {}

	# Fill in your regular expression for valid alert types,
	# make sure to also update the ALERT_TYPES hash!
	ALERTS 		= [
	  /^\[\d*\] CURRENT SERVICE STATE: \S*;SSH Login;CRITICAL/,
	  /^\[\d*\] CURRENT HOST STATE: \S*;DOWN;/,
	  /^\[\d*\] HOST ALERT: \S*;DOWN;/,
	  /^\[\d*\] SERVICE ALERT: \S*;SSH Login;CRITICAL;/,
	  /^\[\d*\] CURRENT SERVICE STATE: \S*;\[nrpe\] GSSD ok;CRITICAL;/,
	  /^\[\d*\] SERVICE ALERT: \S*;\[nrpe\] GSSD ok;CRITICAL;/,
	  
	  /^\[\d*\] CURRENT SERVICE STATE: \S*;SSH Login;OK/,
	  /^\[\d*\] CURRENT HOST STATE: \S*;UP;/,
	  /^\[\d*\] HOST ALERT: \S*;UP/,
	  /^\[\d*\] SERVICE ALERT: \S*;SSH Login;OK;/,
	  /^\[\d*\] CURRENT SERVICE STATE: \S*;\[nrpe\] GSSD ok;OK;/,
	  /^\[\d*\] SERVICE ALERT: \S*;\[nrpe\] GSSD ok;OK;/
	]

	# has to match order of error messages in ALERT array
	# used to identify similar alerts with different names, 
	# e.g. "CURRENT HOST STATE" and "HOST ALERT". Make sure to
	# also update the values in the parser (line 82 & 100)!
	ALERT_TYPES = {
		0 => "ssh_down",
		1 => "ping_down",
		2 => "ping_down",
		3 => "ssh_down",
		4 => "gssd_fail",
		5 => "gssd_fail",

		6 => "ssh_up",
		7 => "ping_up",
		8 => "ping_up",
		9 => "ssh_up",
		10 => "gssd_ok",
		11 => "gssd_ok"
	}
	
	def initialize(dir, *param_array)
		@hosts 	= {}
		@logdir = dir

		@files 	= Dir.glob(@logdir + (param_array.first ? "/#{param_array.first}" : "/*.log"))
		
		log "Found #{@files.size} log files in working dir: #{dir}", 1
		
	end

	def parse
		@files.each do |f|
			log "Processing file #{f}", 1

		  file = File.open f
		  
		  file.each do |line|
				alert_type = check_valid_line line
				log "Alert type #{ALERT_TYPES[alert_type]} found.", 2 if alert_type
				
				if alert_type

					hostname =  line.split(';').first.split(':').last.strip
					
					# generate entry for every host found
					unless @hosts.has_key? hostname
						@hosts[hostname] = [] 
						log "Found new host #{hostname}", 1
					end

					timestamp = line.split.first.gsub(/[\[\]]/, "") 
					
					# positive status found
					if (6..11) === alert_type
						@hosts[hostname].each do |s|
							
							s_type, s_state = s.type.split('_')
							a_type, a_state = ALERT_TYPES[alert_type].split('_')
							
							if (s.status == 1 && s_type == a_type && s_state != a_state)
								# find position of downtime entry
								pos = @hosts[hostname].index(s)

								if ((timestamp.to_i - @hosts[hostname][pos].start_time.to_i) < 86400)
									@hosts[hostname][pos].end_time = timestamp
									@hosts[hostname][pos].status = 0
									
									log "updated record", 2
								end
							end
						end
					
					# DOWN, CRITICAL alert found	
					elsif (0..5) === alert_type
						# check if alert already found
						found = false
						@hosts[hostname].each do |s|
							if (s.status == 1 && s.type == ALERT_TYPES[alert_type])
								found = true
							end
						end
						
						# push new record to host if alert not thrown yet
						@hosts[hostname] << Downtime.new(ALERT_TYPES[alert_type], timestamp) unless found
					end
				end

			end # file
			
			# clean up alerts without end, therefore they dont appear on the next event as end_time
			clean_up_open_alerts

		 	file.close
		
			log "Parser: Processed #{@hosts.size} hosts ...", 2
		end # files
	end

	def clean_up_open_alerts
		log "Cleaning up open alerts", 2

		@hosts.each do |host|
			hostname = host[0]
			host[1].each do |status|
				if (status.status == 1)
					# close alert
					pos = @hosts[hostname].index(status)

					# calculate end time
					t  = Time.at(@hosts[hostname][pos].start_time.to_i)
					nt = Time.local(t.year, t.month, t.day, 23, 59, 59)
							
					@hosts[hostname][pos].end_time = nt.to_i
					@hosts[hostname][pos].status = 0

					log "Cleaned alert from #{(Time.at(status.start_time.to_i)).strftime("%F %T")} to #{(Time.at(status.end_time.to_i)).strftime("%F %T")}", 2

				end
			end # end iter. statuses
		end # hosts.each

	end

	def check_valid_line(line)
	  result = nil
	  i = 0
	  
		begin
			ALERTS.each do |r|
				result = (line =~ r) ? i : result
				i+=1
			end
		rescue ArgumentError
			result = nil
		end
	  
	  return result
	end

	def inspect_hosts
		@hosts.each do |h|
			log "------------\n", 0
			log h.inspect, 0
		end
	end

end
