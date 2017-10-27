package nagioshistory.view;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import processing.core.PApplet;
import processing.core.PFont;

import nagioshistory.DataSource;
import nagioshistory.NagiosHistory;

public class LineChartView extends View {
	private static String services[][] = {{"200", "ping_down"}, {"0", "ssh_down"}, {"150", "gssd_fail"}};
	private Calendar day;
	private long[][] dbData;
	private int[][] data = new int[services.length][24*4];	//one value for every quarter hour of the day
	private float min = Float.MAX_VALUE, max = Float.MIN_VALUE; 
	private DataSource db;
	
	private final float margin_left = 50, margin_right=150;
	private final float margin_top = 50, margin_bottom=30;
	
	PFont font;

	public LineChartView(int x, int y, int width, int height, DataSource db, PApplet p) {
		super(x,y,width,height);
		day = new GregorianCalendar();
		day.set(2011,0,1);
		this.db = db;
		this.font = p.createFont("data/AmericanTypewriter-24.vlw", 24, true);
		this.updateData();
	}

	private void updateData() {
		this.min = Float.MAX_VALUE;
		this.max = Float.MIN_VALUE; 
		for(int k=0;k<services.length;k++)
		{
			//reset values
			Arrays.fill(data[k], 0);
			dbData = db.getDowntimesPerDay(services[k][1], this.day);	//fetch data from database
			Calendar tmpCal = new GregorianCalendar();
			for(int i=0; i<dbData.length;i++)	//parse data
			{
				// to parse the data we check every interval and add 1 to every frame in this interval
				tmpCal.setTimeInMillis(dbData[i][0]*1000L);
				int startIndex = (int) (tmpCal.get(Calendar.HOUR_OF_DAY)*4+Math.floor(tmpCal.get(Calendar.MINUTE)/15d));
				tmpCal.setTimeInMillis(dbData[i][1]*1000L);
				int endIndex = (int) (tmpCal.get(Calendar.HOUR_OF_DAY)*4+Math.floor(tmpCal.get(Calendar.MINUTE)/15d));
				for(int j=startIndex; j<endIndex; j++)
					data[k][j]++;
			}
			for(int i=0; i<data[k].length;i++)
			{
				if(data[k][i]<this.min)
					this.min = data[k][i];
				if(data[k][i]>this.max)
					this.max = data[k][i];
			}
		}
	}

	public void setDay(int year, int month, int day)
	{
		this.day.set(year,month,day);
		this.updateData();

	}

	public void draw(PApplet p)
	{
		p.colorMode(PApplet.HSB);
		super.draw(p);

		
		p.stroke(0);
		p.fill(255);
		//p.rect(0, 0, 100F, 25F);
		p.fill(0);
		p.textFont(this.font, 20);
		p.textAlign(PFont.LEFT);
		p.text("Day", 2, 2, 100, 25);
		
		//don't draw anything if no day is selected
		if (!((CalendarView)((NagiosHistory)p).views.get("calendarView")).isDaySelected())
		{	
			p.textFont(this.font, 30);
			p.textAlign(PFont.CENTER);
			p.fill(100);
			p.text("No Day Selected",this.width/2-150, this.height/2-25, 300, 50);
			return;
		}
		
		//draw coordinate system
		drawCoordinateSystem(p);
		
		float coordinate_system_height = this.height -margin_top -margin_bottom - 30;
		//draw services
		for(int k=0; k<services.length;k++)
		{
			p.stroke(Integer.parseInt(services[k][0]),255,255);
			for(int i =1; i< 24*4-1 ; i++)
			{
				p.line(margin_left+ (i-1)/(24*4f)*(this.width-margin_left-margin_right), 
						this.height -margin_bottom - (PApplet.map(this.data[k][i-1], 0, this.max, 0, coordinate_system_height)),
						margin_left+ i/(24*4f)*(this.width-margin_left-margin_right),
						this.height -margin_bottom - (PApplet.map(this.data[k][i], 0, this.max, 0, coordinate_system_height)));
			}
		}
		
		//draw legend
		drawLegend(p);
	}

	private void drawLegend(PApplet p) {
		p.colorMode(PApplet.RGB);
		p.stroke(0, 0, 0);
		p.fill(255,255,255);
		
		int legend_width = 100, legend_height= 150;
		int margin = 20;
		p.rect(this.width - legend_width - margin,margin,legend_width, legend_height);
		p.pushMatrix();
		p.translate(this.width - legend_width - margin +10,margin+10);
		p.colorMode(PApplet.HSB);
		p.textAlign(PFont.LEFT);
		for(int k= 0; k<LineChartView.services.length;k++)
		{
			p.fill(Integer.parseInt(services[k][0]),255,255);
			p.rect(0, k*20, 10,10);
			p.text(services[k][1], 20, k*20+8);
		}
		p.popMatrix();
	}

	private void drawCoordinateSystem(PApplet p) {
		
		p.textFont(this.font, 12);
		p.textAlign(PFont.LEFT);
		p.fill(0,20,20);
		p.color(0, 0, 0);
		float coordinate_system_height = this.height -margin_top -margin_bottom - 30;
		int range = (int)(this.max-min);
		int steps = (max > 100) ? 20: 10; 
		//y-axis
		p.line(margin_left, margin_top, margin_left, this.height-margin_bottom);
		p.line(margin_left,margin_top,margin_left-5,margin_top+5);
		p.line(margin_left,margin_top,margin_left+5,margin_top+5);
		
		p.text("number of erroneous hosts", margin_left, margin_top -5);
		p.textAlign(PFont.RIGHT);
		for(int i=0; i<=range/steps; i++)
		{
			p.line(margin_left - 5, 
						this.height -margin_bottom - i/(float)(range/(float)steps)*coordinate_system_height,
						margin_left+ 5,
						this.height -margin_bottom - i/(float)(range/(float)steps)*coordinate_system_height);
			p.text(i*steps, margin_left-10, this.height -margin_bottom - i/(float)(range/(float)steps)*coordinate_system_height +5);
			
		}
		
		p.textAlign(PFont.LEFT);
		//x-axis
		p.line(margin_left, this.height-margin_bottom, this.width-margin_right, this.height-margin_bottom);
		p.line(this.width-margin_right, this.height-margin_bottom, this.width - margin_right - 5, this.height-margin_bottom - 5);
		p.line(this.width-margin_right, this.height-margin_bottom, this.width - margin_right - 5, this.height-margin_bottom + 5);
		p.text("time of day", this.width - margin_right, this.height - margin_bottom +15);
		p.textAlign(PFont.CENTER);
		for(int i=0; i<24; i++)
		{
			p.line(margin_left+ (i)/(24f)*(this.width-margin_left-margin_right), 
						this.height -margin_bottom - 5,
						margin_left+ (i)/(24f)*(this.width-margin_left-margin_right),
						this.height -margin_bottom + 5);
			p.text(i, margin_left+ (i)/(24f)*(this.width-margin_left-margin_right), this.height -margin_bottom + 20);
		}
	}

}
