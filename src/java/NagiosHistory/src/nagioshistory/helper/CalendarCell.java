package nagioshistory.helper;

import java.awt.Color;

import processing.core.PApplet;

public class CalendarCell {

	// A cell object knows about its location in the grid as well as its size
	// with the variables x,y,w,h.
	public float x, y; // x,y location
	public float w, h; // width and height

	private boolean rotate = false;
	
	private Color stroke_color;
	private float stroke_weight;
	
	private int day_in_year;
	private Long value;

	private int [] fill_color; // color of cell

	public int getDay_in_year() {
		return day_in_year;
	}

	public void setDay_in_year(int day_in_year) {
		this.day_in_year = day_in_year;
	}
	
	public Long getValue() {
		return value;
	}

	public void setValue(Long value) {
		this.value = value;
	}

	public boolean isRotate() {
		return rotate;
	}

	public void setRotate(boolean rotate) {
		this.rotate = rotate;
	}

	public void setFill_color(int[] fill_color) {
		this.fill_color = fill_color;
	}
	
	public void setStroke_weight(float weight) {
		this.stroke_weight = weight;
	}

	public void setStroke_color(Color stroke_color) {
		this.stroke_color = stroke_color;
	}

	  // Cell Constructor
	  public CalendarCell(float posX, float posY, float tempW, float tempH) {
	    x = posX;
	    y = posY;
	    w = tempW;
	    h = tempH;
	    
	    this.stroke_color = new Color(0, 0, 0);
	    this.stroke_weight = 1;
	    
	    fill_color = new int[3]; fill_color[0] = 255; fill_color[1] = 255; fill_color[2] = 255;
	  }
	  
	  public void setColor(int r, int g, int b) {
		  this.fill_color[0] = r;
		  this.fill_color[1] = g;
		  this.fill_color[2] = b;
	  }
	 
	  
	public void display(PApplet p) {
		p.pushMatrix();

			p.fill(this.fill_color[0], this.fill_color[1], this.fill_color[2]);
			
			p.stroke(this.stroke_color.getRed(), this.stroke_color.getGreen(), this.stroke_color.getBlue(), 1000);
			p.strokeWeight(this.stroke_weight);
		
			if (this.rotate) {
				p.strokeWeight((float) 1.0);
				p.translate(x+w/2, y+h/2);
				p.rotate((float) Math.toRadians(180));
				
				p.rect((-1 * w/2),(-1 * h/2)-1, w, h);
				
			} else {
				p.strokeWeight((float) 1.0);
				p.rect(x, y, w, h);
			}
		p.stroke(0); p.fill(255);
		
		p.popMatrix();
		
	}// end of display
	  
}//end Class Cell