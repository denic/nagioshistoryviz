require 'mysql'

class DbFillomatic
	
	include Logger
	
	def initialize (hosts)
		@host 		= "127.0.0.1"
		@user 		= "<USERNAME>"
		@password	= "<PASSWORD>"
		@db         = "<DATABASE>"
		
		@hosts		= hosts
		
		@conn_state = false
	end
	
	def connect
		@dbh = Mysql.real_connect(@host, @user, @password, @db)

		if (@dbh && $ENVIRONMENT=="development")
			log "Truncating table downtimes", 1
			res = @dbh.query("TRUNCATE downtimes")
		end

		@conn_state = true if @dbh
	end
	
	def disconnect
		@dbh.close if @dbh
		@conn_state = false unless @dbh
	end
	
	def export
		connect unless @conn_state
		
		if @conn_state
			@hosts.each do |host|
				host_available = @dbh.query("SELECT id from hosts WHERE name LIKE '#{host.first}'")
				
				if host_available.num_rows == 0
					log "Inserting new #{host.first} to hosts db", 1
					log "  INSERT INTO hosts (name) VALUES ('#{host.first}')", 2
					res = @dbh.query("INSERT INTO hosts (name) VALUES ('#{host.first}')");
				end

				# check if host was successfully inserted to db
				host_available = @dbh.query("SELECT id from hosts WHERE name LIKE '#{host.first}'")
				
				if host_available.num_rows == 0
					log "ERROR: host was not inserted to db, skipping.", 1
				elsif host_available.num_rows == 1
					host_id = host_available.fetch_hash['id']
					log "Host (id: #{host_id}) already in DB, updating downtimes", 1

					insert_query = "INSERT INTO downtimes (host_id, service_type, start, end) VALUES "
					
					do_insert = false

					host[1].each do |d|
					
						start_time 	= (Time.at(d.start_time.to_i)).strftime("%F %T")

						if d.end_time == ""
							end_time = (Time.at(d.start_time.to_i)).strftime("%F 23:59:00")
						else
							end_time 	= (Time.at(d.end_time.to_i)).strftime("%F %T")
						end
						
						insert_query += "('#{host_id}', '#{d.type}', '#{start_time}', '#{end_time}' ),"

						do_insert = true
					end

					if do_insert
						insert_query = "#{insert_query.chop};"
						log insert_query, 2

						res = @dbh.query(insert_query);
					end

				else
					log "ERROR, host has multiple entries in hosts table.", 1
				end
			end
		end
		
		disconnect
	end
	
end
