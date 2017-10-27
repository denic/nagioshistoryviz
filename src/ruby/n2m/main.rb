require 'pathname'
require 'pry'

require './lib/logger'
require './lib/parser'
require './lib/dbfillomatic'
require './lib/downtime'

$ENVIRONMENT = "development" # truncate table before insert
$LOGLEVEL = 1 # 0 = quite, 1 = std, >2 = verbose 

if ARGV[0]
	path = Pathname.new(ARGV[0]).realpath.to_s
	# parse data
	parser = Parser.new path, ARGV[1]
	parser.parse

	puts "Processed #{parser.hosts.size} hosts ..."

	# export to mysql

	exporter = DbFillomatic.new parser.hosts

	exporter.export
else
	puts "Usage: ./main.rb <path_to_log_dir>"
	puts "  optional: provide a specific filename for processing only this file"
end
