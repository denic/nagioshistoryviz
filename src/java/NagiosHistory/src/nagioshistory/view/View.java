package nagioshistory.view;

import processing.core.PApplet;

/**
 * Class to encapsulate a view.
 * @author Johannes Blobel <jblobel@mail.upb.de>
 * @author Dennis Bücker <debuec@mail.uni-paderborn.de>
 *
 */
public class View {
	public int x=0, y=0;	//position of view
	public int width=100, height=100; //size of view
	public Boolean border = true;
	
	public View()
	{
		
	}
	
	public View(int x, int y, int width, int height)
	{
		super();
		this.x=x;
		this.y=y;
		this.width=width;
		this.height=height;
	}
	
	public void draw(PApplet p)
	{
		if(this.border)
		{
			p.stroke(0);
			p.rect(0, 0, width, height);
		}
	}

}
