package cc.creativecomputing.controlui.timeline.view.transport;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTextField;

import cc.creativecomputing.controlui.controls.CCUIStyler;
import cc.creativecomputing.controlui.timeline.controller.TimelineContainer;
import cc.creativecomputing.controlui.timeline.view.SwingGuiConstants;
import cc.creativecomputing.core.util.CCFormatUtil;

class TimeField extends JTextField implements FocusListener, ActionListener, MouseListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = -852810213402377223L;

	private boolean _myIsActive = true;
	
	private TimelineContainer _myTimelineContainer;
	
	public TimeField(TimelineContainer theTimelineContainer) {
		_myTimelineContainer = theTimelineContainer;
		CCUIStyler.styleTransportComponent(this, 64, 20);
		setPreferredSize(new Dimension(80 * SwingGuiConstants.SCALE,20 * SwingGuiConstants.SCALE));
		
		addFocusListener(this);
		addActionListener(this);
		addMouseListener(this);
	}
	
	
	private double valueStringToTime(String theString) {
		String[] myParts = theString.split(":");
		
		if(myParts.length < 4)return -1;
		
		try {
			int myHours = Integer.parseInt(myParts[0]);
			int myMinutes = Integer.parseInt(myParts[1]);
			int mySeconds = Integer.parseInt(myParts[2]);
			int myMillis = Integer.parseInt(myParts[3]);
			
			return myHours * 3600 + myMinutes * 60 + mySeconds + myMillis * 0.001;
		} catch (NumberFormatException e) {
			return -1;
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent theE) {
		double myTime = valueStringToTime(getText());
		if(myTime >= 0)_myTimelineContainer.activeTimeline().transportController().time(myTime);
		_myIsActive = true;
	}

	
	@Override
	public void focusGained(FocusEvent theE) {
		_myIsActive = false;
	}

	
	@Override
	public void focusLost(FocusEvent theE) {
		_myIsActive = true;
	}
	
	private String timeToValueString(double theTime) {
		long myTime = (long)(theTime * 1000);
		long myMillis = myTime % 1000;
		myTime /= 1000;
		long mySeconds = myTime % 60;
		myTime /= 60;
		long myMinutes = myTime % 60;
		myTime /= 60;
		long myHours = myTime;
		
		StringBuffer myResult = new StringBuffer();
		
		myResult.append(CCFormatUtil.nf((int)myHours, 2));
		myResult.append(":");
		
		myResult.append(CCFormatUtil.nf((int)myMinutes, 2));
		myResult.append(":");
		
		myResult.append(CCFormatUtil.nf((int)mySeconds, 2));
		myResult.append(":");
		
		myResult.append(CCFormatUtil.nf((int)myMillis,3));
		return myResult.toString();
	}
	
	public void time(double theTime) {
		if(!_myIsActive)return;
		
		setText(timeToValueString(theTime));
	}

	@Override
	public void mouseClicked(MouseEvent theArg0) {
		_myIsActive = false;
	}

	@Override
	public void mouseEntered(MouseEvent theArg0) {}

	@Override
	public void mouseExited(MouseEvent theArg0) {}

	@Override
	public void mousePressed(MouseEvent theArg0) {}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent theArg0) {
		// TODO Auto-generated method stub
		
	}
}