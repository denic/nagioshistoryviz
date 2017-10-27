module Logger
	def log(msg, level)
		if level <= $LOGLEVEL
			puts msg
		end
	end
end
