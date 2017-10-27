package nagioshistory;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DataSource {
	private Connection connection;
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;

	public void connect()
	{

		// This will load the MySQL driver, each DB has its own driver
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1/<DB>", "<USERNAME>","<PASSWORD>");
			statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		} catch (Exception e) {
			System.err.println("Error! Cannot connect to database :-(");
			e.printStackTrace();
		}
	}

	/**
	 * Returns the aggregated downtimes for each day of a specific service. 
	 * @return the aggregated downtimes for each day for the given service
	 * Format: result[i][0]=unix_timestamp
	 * Format: result[i][1]=downtime in seconds
	 * @param service can be "ping_down" or "ssh_down"
	 */
	public long[][] getAggregatedDowntimes(String service)
	{
		System.out.print("Fetching aggregated downtimes.");
		ResultSet resultSet = null;
		int rows=0;
		long result[][] = null;

		try {
			String query = "SELECT unix_timestamp(DATE(start)) as date" +
					",SUM(TIME_TO_SEC(TIMEDIFF(`end`,`start`)))  as duration " +
					"FROM `downtimes` ";
			if(service!=null && service!="")
				query+= "WHERE service_type = '"+ service + "'";
			query += "GROUP BY YEAR(`start`), MONTH(`start`), DAY(`start`)";
			resultSet = statement.executeQuery(query);
			//get number of rows
			resultSet.last();
			rows = resultSet.getRow();
			result = new long[rows][2];
			resultSet.beforeFirst();

			//write result to array
			while(resultSet.next())
			{
				result[resultSet.getRow()-1][0] = resultSet.getLong("date");
				result[resultSet.getRow()-1][1] = resultSet.getLong("duration");
			}
		} catch (SQLException e) {
			System.err.println("Cannot fetch data from database.");
			e.printStackTrace();
		}
		System.out.println(" Done.");
		return result;

	}

	/**
	 * Returns the aggregated downtimes for each day for all services together. 
	 * @return the aggregated downtimes for each day
	 */
	public long[][] getAggregatedDowntimes()
	{
		return getAggregatedDowntimes(null);
	}

	public String[][] getWorstHosts(Calendar day)
	{
		String dayString= day.get(Calendar.YEAR)+"-"+(day.get(Calendar.MONTH)+1)+"-"+day.get(Calendar.DAY_OF_MONTH);
		ResultSet resultSet = null;
		int rows=0;
		String result[][] = null;

		try {
			String query = "SELECT hosts.name AS hostname, " +
					"hosts.id AS hostid," +
					"SUM( TIME_TO_SEC( TIMEDIFF( downtimes.`end` , downtimes.`start` ) ) ) as `duration`" +
					"FROM downtimes, `hosts`" +
					"WHERE downtimes.`start` >= '"+ dayString +" 00:00:00' " +
					"AND downtimes.`end` <= '"+ dayString +" 23:59:59'" +
					"AND downtimes.`host_id` = hosts.`id`" +
					"GROUP BY `hostname`" +
					"HAVING duration < '86399'" +
					"ORDER by `duration` " +
					"DESC LIMIT 10";

			resultSet = statement.executeQuery(query);
			//get number of rows
			resultSet.last();
			rows = resultSet.getRow();
			result = new String[rows][2];
			resultSet.beforeFirst();

			//write result to array
			while(resultSet.next())
			{
				result[resultSet.getRow()-1][0] = resultSet.getString("hostname");
				result[resultSet.getRow()-1][1] = resultSet.getString("hostid");
			}
		} catch (SQLException e) {
			System.err.println("Cannot fetch data from database.");
			e.printStackTrace();
		}
		return result;

	}

	public long[][] getDowntimesPerDay(int hostid, String service, Calendar day)
	{
		String dayString= day.get(Calendar.YEAR)+"-"+(day.get(Calendar.MONTH)+1)+"-"+day.get(Calendar.DAY_OF_MONTH);
		//System.out.print("Fetching downtimes of "+service+" for day "+ dayString);
		ResultSet resultSet = null;
		int rows=0;
		long result[][] = null;

		try {
			String query = "SELECT UNIX_TIMESTAMP( `start` ) AS 'start'" +
					", UNIX_TIMESTAMP(`end` ) AS 'end'" + 
					"FROM `downtimes` " +
					"WHERE service_type = '"+service+"' ";
			if(hostid!=-1)
				query+="AND host_id = '" +hostid +"'";
			query+="AND START >= '"+ dayString +" 00:00:00' " +
					"AND END <= '"+ dayString +" 23:59:59'";
			resultSet = statement.executeQuery(query);
			//get number of rows
			resultSet.last();
			rows = resultSet.getRow();
			result = new long[rows][2];
			resultSet.beforeFirst();

			//write result to array
			while(resultSet.next())
			{
				result[resultSet.getRow()-1][0] = resultSet.getLong("start");
				result[resultSet.getRow()-1][1] = resultSet.getLong("end");
			}
		} catch (SQLException e) {
			System.err.println("Cannot fetch data from database.");
			e.printStackTrace();
		}
		return result;
	}

	public long[][] getDowntimesPerDay(String service, Calendar day)
	{
		return getDowntimesPerDay(-1, service, day);
	}

	public long[][] getDowntimesPerYear(int hostid, String service, Calendar year)
	{
		ResultSet resultSet = null;
		int rows=0;
		long result[][] = null;

		try {
			String query = "SELECT unix_timestamp(DATE(start)) as date" +
					",SUM(TIME_TO_SEC(TIMEDIFF(`end`,`start`)))  as duration " +
					"FROM `downtimes` ";
			if(service!=null && service!="")
				query+= "WHERE service_type = '"+ service + "'";
			if(hostid!=-1)
				query+="AND host_id = '" + hostid +"'";
			query += "GROUP BY YEAR(`start`), MONTH(`start`), DAY(`start`)";
			resultSet = statement.executeQuery(query);
			//get number of rows
			resultSet.last();
			rows = resultSet.getRow();
			result = new long[rows][2];
			resultSet.beforeFirst();

			//write result to array
			while(resultSet.next())
			{
				result[resultSet.getRow()-1][0] = resultSet.getLong("date");
				result[resultSet.getRow()-1][1] = resultSet.getLong("duration");
			}
		} catch (SQLException e) {
			System.err.println("Cannot fetch data from database.");
			e.printStackTrace();
		}
		return result;
	}

}
