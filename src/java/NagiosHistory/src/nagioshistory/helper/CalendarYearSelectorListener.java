package nagioshistory.helper;

import nagioshistory.view.CalendarView;
import controlP5.ControlEvent;
import controlP5.ControlListener;


public class CalendarYearSelectorListener implements ControlListener {
	
	private CalendarView cv = null;
	
	public CalendarYearSelectorListener (CalendarView v) {
		super();
		
		this.cv = v;
	}

	@Override
	public void controlEvent(ControlEvent e) {
		if (e.isGroup()) {
		    // check if the Event was triggered from a ControlGroup
			System.out.println("event from group : "+ e.getGroup().getValue()+" from " + e.getGroup());
			
			cv.setCurrentYear((int)e.getGroup().getValue());
		  } 
		  else if (e.isController()) {
			  System.out.println("event from controller : "+e.getController().getValue()+" from "+e.getController());
		  }
	}

}
