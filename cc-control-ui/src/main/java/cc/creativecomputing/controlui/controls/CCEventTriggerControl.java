package cc.creativecomputing.controlui.controls;

import java.awt.GridBagConstraints;

import javax.swing.JButton;
import javax.swing.JPanel;

import cc.creativecomputing.control.handles.CCEventTriggerHandle;
import cc.creativecomputing.control.handles.CCTriggerProgress.CCTriggerProgressListener;
import cc.creativecomputing.controlui.CCControlComponent;
import cc.creativecomputing.controlui.CCProgressWindow;

public class CCEventTriggerControl extends CCValueControl<Object, CCEventTriggerHandle>{
	
	private JButton _myButton;
	
	private CCProgressWindow _myProgressWindow;
	
	public CCEventTriggerControl(CCEventTriggerHandle theHandle, CCControlComponent theControlComponent){
		super(theHandle, theControlComponent);
 
        //Create the Button.

        _myButton = new JButton(theHandle.name());
        CCUIStyler.styleButton(_myButton, 103, 13);
        _myButton.addActionListener(theE -> {
			_myHandle.trigger();
		});
        
        theHandle.progress().events().add(new CCTriggerProgressListener() {
    		@Override
    		public void start() {
    			_myProgressWindow = new CCProgressWindow();
    		}
    		
    		@Override
    		public void progress(double theProgress) {
    			_myProgressWindow.progress(theProgress);
    		}
    		
    		@Override
    		public void end() {
    			_myProgressWindow.setVisible(false);
    		}
    		
    		@Override
    		public void interrupt() {
    			// TODO Auto-generated method stub
    			
    		}
    	});
	}
	
	@Override
	public Object value() {
		return null;
	}
	
	
	
	@Override
	public void addToComponent(JPanel thePanel, int theY, int theDepth) {
		thePanel.add(_myLabel, 	constraints(0, theY, GridBagConstraints.LINE_END, 	5, 5, 1, 5));
		thePanel.add(_myButton, constraints(1, theY, GridBagConstraints.LINE_START, 5, 4, 1, 5));
	}
}
