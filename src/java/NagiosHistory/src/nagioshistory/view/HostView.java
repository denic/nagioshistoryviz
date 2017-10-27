package nagioshistory.view;

import java.awt.Color;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import processing.core.PApplet;
import processing.core.PFont;
import controlP5.*;
import nagioshistory.DataSource;
import nagioshistory.NagiosHistory;
import nagioshistory.helper.Gradient;

public class HostView extends View implements ControlListener {

	private DataSource db;
	private PFont font;
	private GregorianCalendar calendar;
	private int hostid=1;

	private ControlP5 cp5;
	private MultiList ml;

	private final float margin_left = 250, margin_right=150;
	private final float margin_top = 20, margin_bottom=30;

	private static String services[][] = {{"200", "ping_down"}, {"0", "ssh_down"}, {"150", "gssd_fail"}};
	private long[][] dbData;
	private float[][] data = new float[services.length][365];	//one value for every day of the year
	private float max;
	private float min;

	public HostView(int x, int y, int width, int height, DataSource db,
			PApplet p) {
		super(x, y, width, height);

		this.db = db;

		this.font = p.createFont("data/AmericanTypewriter-24.vlw", 24, true);

		this.calendar = new GregorianCalendar(2011,0,1);

		this.cp5 = new ControlP5(p);
		// create a MultiList
		this.ml = cp5.addMultiList("Worst Hosts", this.x + 10, this.y +40, 150, 12);
		updateList();
		updateData();
	}

	public void setDay(int year, int month, int day)
	{
		this.calendar.set(year,month,day);
		this.updateList();
		this.updateData();

	}

	private void updateList() {
		String[][] hosts = db.getWorstHosts(this.calendar);
		this.ml.remove();
		this.ml = cp5.addMultiList("Worst Hosts", this.x + 10, this.y +40, 150, 12);
		for(String[] h: hosts)
		{
			ml.add(h[0],Integer.parseInt(h[1])).addListener(this);
		}
		if(hosts.length>0)
			this.hostid = Integer.parseInt(hosts[0][1]);
	}

	private void updateData()
	{
		for(MultiListButton bt : ml.getChildren())
		{
			if(bt.getValue() == this.hostid)
				bt.setColorBackground(150);
			else
				bt.setColorBackground(-16632755);
		}
		// get the linechart data
		this.min = Integer.MAX_VALUE;
		this.max = Integer.MIN_VALUE; 
		for(int k=0;k<services.length;k++)
		{
			//reset values
			dbData = db.getDowntimesPerYear(hostid,services[k][1], this.calendar);	//fetch data from database for first host
			Calendar tmpCal = new GregorianCalendar();
			Arrays.fill(data[k], 0l);
			for(int i=0; i<dbData.length;i++)	//parse data
			{
				tmpCal.setTimeInMillis(dbData[i][0]*1000L);
				data[k][tmpCal.get(Calendar.DAY_OF_YEAR)-1]=dbData[i][1]/60f/60f > 24? 24f : (float)dbData[i][1]/60f/60f ; //limit to 24h per day

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

	public void draw(PApplet p)
	{
		p.colorMode(PApplet.RGB);
		p.stroke(0, 0, 0);
		p.fill(255,255,255);
		super.draw(p);

		p.stroke(0);
		p.fill(255);
		//p.rect(0, 0, 100F, 25F);
		p.fill(0);
		p.textFont(this.font, 20);
		p.textAlign(PFont.LEFT);
		p.text("Hosts", 2, 2, 100, 25);

		//don't draw anything if no day is selected
		if (!((CalendarView)((NagiosHistory)p).views.get("calendarView")).isDaySelected())
		{	
			p.textFont(this.font, 30);
			p.textAlign(PFont.CENTER);
			p.fill(100);
			p.text("No Day Selected",this.width/2-150, this.height/2-25, 300, 50);
			this.ml.remove();
			return;
		}
		
		{//draw coordinate system
			drawCoordinateSystem(p);
		}
		float coordinate_system_height = this.height -margin_top -margin_bottom -30;
		
		{//draw services
			p.colorMode(PApplet.HSB);
			for(int k=0; k<services.length;k++)
			{
				p.stroke(Integer.parseInt(services[k][0]),255,255);
				for(int i =1; i< 365 ; i++)
				{
					p.line(margin_left+ (i-1)/(365f)*(this.width-margin_left-margin_right), 
							this.height -margin_bottom - (PApplet.map(this.data[k][i-1], 0, this.max, 0, coordinate_system_height)),
							margin_left+ i/(365f)*(this.width-margin_left-margin_right),
							this.height -margin_bottom - (PApplet.map(this.data[k][i], 0, this.max, 0, coordinate_system_height)));
				}
			}
		}
		p.colorMode(PApplet.RGB);
		//draw horizontal marker arrow
		p.stroke(255,0,0); //red
		p.strokeWeight(2);
		p.line(margin_left+ (calendar.get(Calendar.DAY_OF_YEAR)-1)/(365f)*(this.width-margin_left-margin_right),
				this.height -margin_bottom,
				margin_left+ (calendar.get(Calendar.DAY_OF_YEAR)-1)/(365f)*(this.width-margin_left-margin_right),
				this.height -margin_bottom  +20);
		p.line(margin_left+ (calendar.get(Calendar.DAY_OF_YEAR)-1)/(365f)*(this.width-margin_left-margin_right),
				this.height -margin_bottom,
				margin_left+ (calendar.get(Calendar.DAY_OF_YEAR)-1)/(365f)*(this.width-margin_left-margin_right) -5,
				this.height -margin_bottom  +5);
		p.line(margin_left+ (calendar.get(Calendar.DAY_OF_YEAR)-1)/(365f)*(this.width-margin_left-margin_right),
				this.height -margin_bottom,
				margin_left+ (calendar.get(Calendar.DAY_OF_YEAR)-1)/(365f)*(this.width-margin_left-margin_right) +5,
				this.height -margin_bottom  +5) ;

		//draw line where the mouse is
		if(p.mouseX > margin_left && p.mouseX < this.width - margin_right && p.mouseY > this.y + margin_top && p.mouseY < this.y + this.height -margin_bottom )
		{
			Calendar tmpCal = (Calendar) calendar.clone();
			p.stroke(0,0,0,50);
			p.line(p.mouseX, this.height -margin_bottom - coordinate_system_height, p.mouseX, this.height -margin_bottom);
			//System.out.println("x="+p.mouseX+" y="+p.mouseY);
			p.fill(0,0,0);
			p.strokeWeight(1);
			p.stroke(0,0,0);
			//p.rect(p.mouseX + 5, this.height -margin_bottom - coordinate_system_height - 15, 50, 10);
			int dayofyear = (int) PApplet.map(p.mouseX,
					margin_left, this.width - margin_right,
					1, 366);
			tmpCal.set(Calendar.DAY_OF_YEAR, dayofyear);
			p.textAlign(PFont.LEFT);
			p.text(tmpCal.get(Calendar.DAY_OF_MONTH)+"."+(tmpCal.get(Calendar.MONTH)+1)+"."+tmpCal.get(Calendar.YEAR),
					p.mouseX +5, this.height -margin_bottom - coordinate_system_height - 15);
			if(p.mousePressed)
			{
				((CalendarView)((NagiosHistory)p).views.get("calendarView")).setDay(dayofyear);
				((LineChartView)((NagiosHistory)p).views.get("lineChartView"))
				.setDay(tmpCal.get(Calendar.YEAR), tmpCal.get(Calendar.MONTH), tmpCal.get(Calendar.DAY_OF_MONTH));
				//update the host view
				this.setDay(tmpCal.get(Calendar.YEAR), tmpCal.get(Calendar.MONTH), tmpCal.get(Calendar.DAY_OF_MONTH));
			}
		}
		//draw legend
		p.strokeWeight(1);
		drawLegend(p);
		
		{ // description of hosts
 			p.textAlign(PFont.LEFT);
			p.fill(100);
			p.textFont(this.font, 10);
			
			p.text("The upper hosts are the one\n with the highest downtime at this day\n"+
					"(if downtime < 24h)",
					10, 190);
		}
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
		for(int k= 0; k<HostView.services.length;k++)
		{
			p.fill(Integer.parseInt(services[k][0]),255,255);
			p.rect(0, k*20, 10,10);
			p.text(services[k][1], 20, k*20+8);
		}
		p.popMatrix();
	}

	private void drawCoordinateSystem(PApplet p) {
		this.font = p.createFont("data/AmericanTypewriter-24.vlw", 24, true);
		p.textFont(this.font, 12);
		p.textAlign(PFont.LEFT);
		p.fill(0,20,20);
		p.color(0, 0, 0);
		float coordinate_system_height = this.height -margin_top -margin_bottom - 30;
		int range = (int)(this.max-min);
		//int steps = (max > 100) ? 20: 10;
		int steps=5;
		//y-axis
		p.line(margin_left, margin_top, margin_left, this.height-margin_bottom);
		p.line(margin_left,margin_top,margin_left-5,margin_top+5);
		p.line(margin_left,margin_top,margin_left+5,margin_top+5);

		p.text("downtime per day", margin_left, margin_top -5);
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
		p.text("day of the year", this.width - margin_right, this.height - margin_bottom +15);
		p.textAlign(PFont.CENTER);
		for(int i=0; i<365; i+=50)
		{
			p.line(margin_left+ (i)/(365f)*(this.width-margin_left-margin_right), 
					this.height -margin_bottom - 5,
					margin_left+ (i)/(365f)*(this.width-margin_left-margin_right),
					this.height -margin_bottom + 5);
			p.text(i, margin_left+ (i)/(365f)*(this.width-margin_left-margin_right), this.height -margin_bottom + 20);
		}
	}

	@Override
	public void controlEvent(ControlEvent e) {
		this.hostid = (int) e.getValue();
		updateData();
	}

}
