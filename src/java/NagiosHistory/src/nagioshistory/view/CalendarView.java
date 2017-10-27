package nagioshistory.view;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import controlP5.ControlP5;
import controlP5.DropdownList;

import nagioshistory.DataSource;
import nagioshistory.NagiosHistory;
import nagioshistory.helper.CalendarCell;
import nagioshistory.helper.CalendarYearSelectorListener;
import nagioshistory.helper.Gradient;
import processing.core.PApplet;
import processing.core.PFont;

public class CalendarView extends View {

	ControlP5 cp5;

	DropdownList d1;

	private CalendarCell[][] grid;
	private Integer[] hl_cell;

	private DataSource db;

	private int rows = 7;
	private int cols = 53;

	private int cell_w, cell_h;

	private int left_legend_width = 0;
	private int top_legend_height = 50;

	private GregorianCalendar calendar;
	private int current_year = 2011;

	private Long[][] data;

	private int max_duration = 0;

	private PFont font;
	
	public CalendarView(int x, int y, int width, int height, DataSource db,
			PApplet p) {
		super(x, y, width, height);

		this.db = db;
		
		this.font = p.createFont("data/AmericanTypewriter-24.vlw", 24, true);

		// size of single cells within visualization
		this.cell_h = (this.width - 60 - this.left_legend_width) / this.cols;
		this.cell_w = this.cell_h;

		this.calendar = new GregorianCalendar();

		cp5 = new ControlP5(p);
		// create a DropdownList
		d1 = cp5.addDropdownList("select year").setPosition(this.x + 290, this.y + 23);
		setupYearSelector(p, d1);
		d1.setIndex(2);
		
		// storage for highlighted cell
		this.hl_cell 	 = new Integer[2];
		this.hl_cell[0]	 = null;
		this.hl_cell[1]	 = null;
		
		updateData();
	}

	private void updateData() {
		// this.calendar.setFirstDayOfWeek(Calendar.MONDAY);

		// preprocess data
		System.out.println("Preprocessed " + preprocessData() + " entries.");

		// generate new color mapping

		this.grid = new CalendarCell[this.cols][this.rows];

		// set first day of the year as starting point
		this.calendar.set(Calendar.YEAR, this.current_year);
		this.calendar.set(Calendar.MONTH, Calendar.JANUARY);
		this.calendar.set(Calendar.DAY_OF_MONTH, 1);

		int col_counter = this.calendar.get(Calendar.DAY_OF_WEEK) - 1;
		int first_day_of_month_indicator_w = 0;
		int first_day_of_month_indicator_h = 0;

		for (int i = 0; i < this.data.length; i++) {

			// Date curr_date = new Date((long) this.data[i][0]*1000L);
			Color color = new Color(0, 0, 0);

			if (this.data[i][0] == null) {
				this.calendar.set(Calendar.DAY_OF_YEAR, i + 1);
				System.out.println("Day:"
						+ this.calendar.get(Calendar.DAY_OF_YEAR));
				color = new Color(255, 255, 255);
			} else {
				this.calendar.setTimeInMillis(this.data[i][0] * 1000L);
//				color = Color.getHSBColor(1.0F - (float) this.data[i][1]
//						/ (float) max_duration, 1f, 0f);
//				color = colorMap[Math.max((int) Math.floor((this.data[i][1]/this.max_duration)*this.colorMap.length)-1, 0)];
				color = Gradient.getColor((this.data[i][1]*100/this.max_duration));
			}

			// calculate current col (week)
			int col = (int) Math.floor(col_counter / 7);

			int dayOfWeek = this.calendar.get(Calendar.DAY_OF_WEEK) - 1;
			int dayOfYear = this.calendar.get(Calendar.DAY_OF_YEAR);

			if (this.calendar.get(Calendar.DAY_OF_MONTH) == 1) {
				first_day_of_month_indicator_w += 1;
				first_day_of_month_indicator_h = 1;

			} else {
				first_day_of_month_indicator_h = 0;
			}

			// add current cell to grid
			{
				this.grid[col][dayOfWeek] = new CalendarCell(this.left_legend_width
						+ this.top_legend_height + (col * this.cell_w)
						+ (first_day_of_month_indicator_w), this.top_legend_height
						+ (dayOfWeek * this.cell_h), this.cell_w, this.cell_h
						- first_day_of_month_indicator_h);
	
				this.grid[col][dayOfWeek].setColor(color.getRed(),
						color.getGreen(), color.getBlue());
				
				this.grid[col][dayOfWeek].setValue(this.data[i][1]);
	
				if (first_day_of_month_indicator_h == 1) {
					this.grid[col][dayOfWeek].setRotate(true);
				}
				this.grid[col][dayOfWeek].setDay_in_year(dayOfYear);
			}

			// increment running column counter
			col_counter++;
		}
	}
	
	public void setCurrentYear(int year) {
		this.current_year = year;
		
		this.hl_cell[0] = null;
		this.hl_cell[1] = null;
		
		updateData();
	}
	
	public void setDay(int day) {
		setCurrentDay(day);
	}
	
	public void setCurrentDay(int day) {
		for (int col = 0; col < this.cols; col++) {
			for (int row = 0; row < this.rows; row++) {
				if (this.grid[col][row] != null) {
					CalendarCell cell = this.grid[col][row];
					
					if (cell.getDay_in_year() == day) {
						this.hl_cell[0] = col;
						this.hl_cell[1] = row;
					}
				}
			}
		}
	}

	private int preprocessData() {
		Long[][] dataRedux = new Long[numOfDaysInYear()][2];
		long[][] tempData = db.getAggregatedDowntimes(); // fetch data from
															// database

		long t_start = firstDayOfYearMillis();
		long t_end = lastDayOfYearMillis();

		long max_val = 0;

		// use another calendar instance to not get mixed up
		GregorianCalendar cal = new GregorianCalendar();

		System.out.println("Start: " + t_start + " End: " + t_end);

		int d_cnt = 0;
		for (int i = 0; i < tempData.length; i++) {
			Date curr_date = new Date(tempData[i][0] * 1000L);

			if (curr_date.getTime() >= t_start && curr_date.getTime() <= t_end) {
				cal.setTime(curr_date);
				int dayOfYear = cal.get(Calendar.DAY_OF_YEAR) - 1;

				dataRedux[dayOfYear][0] = tempData[i][0];
				dataRedux[dayOfYear][1] = tempData[i][1];

				// set maximum duration value
				max_val = (dataRedux[dayOfYear][1] > max_val) ? dataRedux[dayOfYear][1]
						: max_val;

				d_cnt++;
			}
		}

		this.data = dataRedux;
		this.max_duration = (int) max_val;

		return d_cnt;
	}

	private int numOfDaysInYear() {
		// set first day of the year as starting point
		this.calendar.set(Calendar.YEAR, this.current_year);
		this.calendar.set(Calendar.MONTH, Calendar.JANUARY);
		this.calendar.set(Calendar.DAY_OF_MONTH, 1);

		// number of days in this year
		return this.calendar.getActualMaximum(Calendar.DAY_OF_YEAR);
	}

	private long firstDayOfYearMillis() {
		// set first day of the year as starting point
		{
			this.calendar.set(Calendar.YEAR, this.current_year);
			this.calendar.set(Calendar.MONTH, Calendar.JANUARY);
			this.calendar.set(Calendar.DAY_OF_MONTH, 1);
			this.calendar.set(Calendar.HOUR, 0);
			this.calendar.set(Calendar.MINUTE, 0);
			this.calendar.set(Calendar.SECOND, 0);
			this.calendar.set(Calendar.MILLISECOND, 0);

		}

		return this.calendar.getTime().getTime();
	}

	private long lastDayOfYearMillis() {
		{
			this.calendar.set(Calendar.DAY_OF_YEAR,
					this.calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
			this.calendar.set(Calendar.HOUR, 23);
			this.calendar.set(Calendar.MINUTE, 59);
			this.calendar.set(Calendar.SECOND, 59);
			this.calendar.set(Calendar.MILLISECOND, 999);
		}

		return this.calendar.getTime().getTime();
	}

	// ------------------------------------------------------------------------
	// draw methods
	// ------------------------------------------------------------------------
	public void draw(PApplet p) {
		p.colorMode(PApplet.RGB);
		p.fill(255,255,255);
		super.draw(p);
		resetRectBorders();

		p.textFont(this.font, 12);
		p.textAlign(PFont.CENTER);

		drawTopLegend(p);


		for (int i = 0; i < cols; i++) {
			for (int j = 0; j < rows; j++) {
				if (grid[i][j] != null) {
					grid[i][j].display(p);
				}
			}
		}

		if (p.mousePressed) {
			int left_border 	= this.x + this.left_legend_width;
			int right_border 	= this.x + this.width;
			int top_border 		= this.y + this.top_legend_height;
			int bottom_border	= this.y + (9 * this.cell_h) + this.top_legend_height;
			
			int mx = p.mouseX; int my = p.mouseY;
			
			if (mx >= left_border && mx <= right_border && my >= top_border && my <= bottom_border) {
				System.out.println("Click at " + mx + ":" + my);
				updateHighligthCell(mx, my, p);	
			}
		}

		drawTopLegend(p);
		drawLeftLegend(p);
		drawStatistics(p);
		drawTopMenu(p);
		
		drawHighlightCell(p);
	}
	
	private void resetRectBorders() {
		for (int col = 0; col < this.cols; col++) {
			for (int row = 0; row < this.rows; row++) {
				if (this.grid[col][row] != null) {
					this.grid[col][row]	.setStroke_color(new Color(0, 0, 0));
					this.grid[col][row].setStroke_weight(1.0F);
				}
			}
		}
	}

	private void drawTopLegend(PApplet p) {
		String months[] = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
				"Aug", "Sep", "Oct", "Nov", "Dec" };

		p.fill(0, 20, 20);
		for (int i = 0; i < 12; i++) {
			p.textAlign(PFont.CENTER);
			p.text(months[i], this.left_legend_width + 115 + ( (53*this.cell_w / 12.5F))
					* i, this.y + this.top_legend_height - 2);
		}
		p.fill(255);
	}

	private void drawLeftLegend(PApplet p) {
		String days[] = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };

		p.textAlign(PFont.RIGHT);
		p.fill(0, 20, 20);
		for (int i = 0; i < 7; i++) {
			p.text(days[i], this.left_legend_width + 50, 15 + this.y
					+ this.top_legend_height + (this.cell_h * i));
		}
		p.fill(255);
	}
	
	private void drawStatistics(PApplet p) {
		GregorianCalendar cal = new GregorianCalendar();
		
		{ // description of color mapping
 			p.textAlign(PFont.LEFT);
			p.fill(100);
			p.textFont(this.font, 10);
			
			p.text("Colors mapped to the range from the optimum of 0 hours of downtime" + 
					" to the maximum (summed up) downtime of "  + this.max_duration/60/60 + 
					" hours per day.", 
					this.x + this.width - 240 - 130, this.height - 50, 240, 70);
		}
		
		p.pushMatrix();
			int off_x = this.width - 120;
			
			{
				p.fill(255);
				p.rect(off_x - 5, this.height - 50, 110, 45);
			}
			
			for (int i=0; i<100; i++) {
				Color c = Gradient.getColor((double) i);
			    p.stroke(c.getRed(), c.getGreen(), c.getBlue());
			    p.line (this.x + off_x + i, this.height - 30, this.x + off_x +i, this.height - 10);
			}
			
			{ // labels
				p.stroke(0, 0, 0);
				p.line (this.x + off_x, this.height - 35, this.x + off_x, this.height - 10);
				p.line (this.x + off_x + 100, this.height - 35, this.x + off_x + 100, this.height - 10);

				p.fill(0);
				
				p.textFont(this.font, 10);
				p.textAlign(PFont.LEFT);
				p.text("0", (this.x + off_x - 2), (this.height - 48), 20, 25);
				
				p.textAlign(PFont.RIGHT);
				p.textFont(this.font, 10);
				p.text(""+this.max_duration/60/60, this.x + off_x + 50, this.height - 48, 53, 25);
			}
			
		p.popMatrix();
		
		p.colorMode(PApplet.RGB);
		
		
		if (this.hl_cell[0] != null) {
			int col = this.hl_cell[0];
			int row = this.hl_cell[1];
			cal.set(Calendar.YEAR, this.current_year);
			cal.set(Calendar.DAY_OF_YEAR, this.grid[col][row].getDay_in_year());
			
			SimpleDateFormat date_format = new SimpleDateFormat("EEE, d MMM yyyy");
			
			p.textFont(this.font, 14);
			p.textAlign(PFont.LEFT);
			p.text(date_format.format(cal.getTime()), this.x + 500, this.y + 5, 300, 25);
		} else {
			p.textFont(this.font, 14);
			p.textAlign(PFont.LEFT);
			p.text("no day selected", this.x + 500, this.y + 5, 300, 25);
		}
		
		p.fill(255);
	}
	
	private void drawHighlightCell(PApplet p) {
		if (this.hl_cell[0] != null) {
			int col = this.hl_cell[0];
			int row = this.hl_cell[1];
			
			this.grid[col][row]	.setStroke_color(new Color(255, 0, 0));
			this.grid[col][row].setStroke_weight(10.0F);
			this.grid[col][row].display(p);	
		}
	}
	
	private void drawTopMenu(PApplet p) {
		p.stroke(0);
		p.fill(255);
		//p.rect(this.x, this.y, 100F, 25F);
		
		p.fill(0);
//		p.textFont(this.font, 30);
		p.textFont(this.font, 20);
		p.textAlign(PFont.LEFT);
		p.text("Calendar", this.x + 2, this.y + 2, 100, 25);
		
		p.textFont(this.font, 14);
		p.textAlign(PFont.LEFT);
		p.text("Please select year:", this.x + 150, this.y + 5, 150, 25);
		// + width of year selector 100?
		
		p.textFont(this.font, 14);
		p.textAlign(PFont.LEFT);
		p.text("| current day:", this.x + 400 , this.y + 5, 150, 25);

		p.fill(255);

	}

	private void updateHighligthCell(int mx, int my, PApplet p) {	
		for (int col = 0; col < this.cols; col++) {
			for (int row = 0; row < this.rows; row++) {
				if (this.grid[col][row] != null) {
					CalendarCell cell = this.grid[col][row];
					
					if ( (mx >= cell.x) && (mx <= cell.x + cell.w) && (my >= cell.y) && (my <= cell.y + cell.w)) {
						System.out.println("Click event at day " + cell.getDay_in_year() 
								+ " with value " + cell.getValue() + "(max: " + this.max_duration + ")");
						
						this.hl_cell[0] = col; this.hl_cell[1] = row;
						drawHighlightCell(p);
						
						//update the linechart view
						Calendar tmpCal = new GregorianCalendar();
						tmpCal.set(Calendar.YEAR, this.current_year);
						tmpCal.set(Calendar.DAY_OF_YEAR, cell.getDay_in_year());
						((LineChartView)((NagiosHistory)p).views.get("lineChartView"))
						.setDay(this.current_year, tmpCal.get(Calendar.MONTH), tmpCal.get(Calendar.DAY_OF_MONTH));
						//update the host view
						((HostView)((NagiosHistory)p).views.get("hostView"))
						.setDay(this.current_year, tmpCal.get(Calendar.MONTH), tmpCal.get(Calendar.DAY_OF_MONTH));
					}
				}
			}
		}
	}
	
	void setupYearSelector(PApplet p, DropdownList ddl) {
		// a convenience function to customize a DropdownList
		ddl.setBackgroundColor(p.color(190));
		ddl.setItemHeight(20);
		ddl.setBarHeight(15);

		for (int i = 9; i <= 12; i++) {
			ddl.addItem((new Integer(2000 + i).toString()), 2000+i);
		}

		// ddl.scroll(0);
		ddl.setColorBackground(p.color(60));
		ddl.setColorActive(p.color(255, 128));
		
		ddl.addListener(new CalendarYearSelectorListener(this));
	}
	
	public boolean isDaySelected()
	{
		return this.hl_cell[0] != null;
	}
}
