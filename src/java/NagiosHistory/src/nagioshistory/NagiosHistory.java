package nagioshistory;

import java.util.HashMap;
import nagioshistory.view.*;
import processing.core.PApplet;
import controlP5.*;


public class NagiosHistory extends PApplet {
	
	public HashMap<String,View> views 	= new HashMap<String, View>();	//Hashmap with all Views
	
	private int window_width 	= 1000;
	private int window_height	= 699;
	DataSource db;
	
	public void setup() {
		size(window_width, window_height);
		background(255);
		db = new DataSource();
		db.connect();
		this.initGUI();
	}

	public void draw() {
		for (View view : views.values()) {
		    pushMatrix();
		    translate(view.x, view.y);
		    view.draw(this);
		    popMatrix();
		}
	}
	
	private void initGUI()
	{
		View calendarView = new CalendarView(0, 0, window_width, window_height/3,  db, this);
		View lineChartView = new LineChartView(0,233,window_width,window_height/3, db, this);
		View hostView = new HostView(0,466,window_width,window_height/3, db, this);
		
		views.put("calendarView", calendarView);
		views.put("lineChartView", lineChartView);
		views.put("hostView", hostView);
	}
	
	public static void main(String _args[]) {
		PApplet.main(new String[] { nagioshistory.NagiosHistory.class.getName() });
	}
}
